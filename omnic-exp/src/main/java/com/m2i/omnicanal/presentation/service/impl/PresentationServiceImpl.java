package com.m2i.omnicanal.presentation.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.m2i.omnicanal.model.CasExecution;
import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.model.LigneSchemaEcriture;
import com.m2i.omnicanal.model.LigneTransaction;
import com.m2i.omnicanal.model.Operation;
import com.m2i.omnicanal.model.ParametreUtilisateur;
import com.m2i.omnicanal.model.SchemaEcriture;
import com.m2i.omnicanal.model.Transaction;
import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.model.util.JsonLigneTransaction;
import com.m2i.omnicanal.model.util.JsonTransaction;

import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.omnicanal.presentation.domain.AgenceFse;

import com.m2i.omnicanal.presentation.domain.FournisseurFSE;
import com.m2i.omnicanal.presentation.domain.LigneOper;
import com.m2i.omnicanal.presentation.domain.Oper;

import com.m2i.omnicanal.presentation.service.AgenceService;
import com.m2i.omnicanal.presentation.service.FournisseurServiceService;
import com.m2i.omnicanal.presentation.service.LigneOperationService;
import com.m2i.omnicanal.presentation.service.LigneSchemaEcritureService;
import com.m2i.omnicanal.presentation.service.OperationService;
import com.m2i.omnicanal.presentation.service.PresentationDetailService;
import com.m2i.omnicanal.presentation.service.PresentationService;
import com.m2i.omnicanal.presentation.service.SchemaEcritureService;
import com.m2i.omnicanal.presentation.service.SchemaMotif;
import com.m2i.omnicanal.presentation.service.UserService;
import com.m2i.omnicanal.presentation.domain.SchemaEcrit;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import com.m2i.omnicanal.presentation.service.HazelcastConfigCacheService;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.collections4.map.PassiveExpiringMap;

import org.json.simple.JSONObject;

import org.json.simple.parser.ParseException;

import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author cda
 */
@org.springframework.stereotype.Service
public class PresentationServiceImpl implements PresentationService {

    @Inject
    PresentationDetailService presentationDetailService;
    @Inject
    SchemaEcritureService schemaEcritureService;
    @Inject
    FournisseurServiceService fournisseurServiceService;
    @Inject
    OperationService operationService;
    @Inject
    UserService userService;
    @Inject
    AgenceService agenceService;
    @Inject
    LigneOperationService lgOperationService;
    @Inject
    LigneSchemaEcritureService ligneSchemaEcritureService;
    private final org.slf4j.Logger log = LoggerFactory.getLogger(PresentationServiceImpl.class);
    @Inject
    HazelcastConfigCacheService hazelcastConfigCacheService;
    PassiveExpiringMap<String, Integer> schemaMap = new PassiveExpiringMap<>(18000000);
    PassiveExpiringMap<String, List<SchemaEcriture>> schemalistShEcritureMap = new PassiveExpiringMap<>(18000000);
    private final ConcurrentHashMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    ///CacheObject<String> redisMescasage = new CacheObject(sha256hex, key);
    private List<SchemaEcriture> getlistShEcriture(TypeService typeService) {
        String keyMap = typeService.getCodeTypeService();
        List<SchemaEcriture> listShEcriture;
        if (schemalistShEcritureMap.containsKey(keyMap) && schemalistShEcritureMap.get(keyMap) != null) {

            listShEcriture = schemalistShEcritureMap.get(keyMap);

        } else {
            listShEcriture = schemaEcritureService.findByService(typeService);
            schemalistShEcritureMap.put(keyMap, listShEcriture);

        }
        return listShEcriture;
    }

    private Map<String, String> indexParams(List<ParametreUtilisateur> params) {
        Map<String, String> map = new HashMap<>(params.size() * 2);
        for (ParametreUtilisateur p : params) {
            map.put(p.getCodeParametre(), p.getValeur());
        }
        return map;
    }

    @Override
    @Transactional // une transaction claire pour tout l’enchaînement
    public Transaction findTransaction(String codeUo, TypeService typeService, List<ParametreUtilisateur> listparamUser, String reference, String canal) {
        log.info("RECHERCHE DE LA LIGNE DE TRANSACTION");
        Transaction trans = new Transaction();

        try {
            if (typeService != null) {
                List<SchemaEcriture> listShEcriture = getlistShEcriture(typeService); //schemaEcritureService.findByService(typeService);
                Set<LigneSchemaEcriture> listLigneShEcriture = new LinkedHashSet<>();
                boolean bool = false;
                FournisseurFSE fseSource = new FournisseurFSE();
                FournisseurFSE fseDest = new FournisseurFSE();
                Map<String, String> pidx = indexParams(listparamUser);
                fseSource = fournisseurServiceService.findUoByCodeFse(pidx.get("CODEFSESOURCE"));
                fseDest = fournisseurServiceService.findUoByCodeFse(pidx.get("CODEFSEDEST"));

                CasExecution cas = "NORMAL".equals(pidx.get("CAS")) ? CasExecution.NORMAL : CasExecution.REVERSION;
                String cdeAgenceSource = pidx.get("CODEAGENCESOURCE");
                String cdeAgenceDest = pidx.get("CODEAGENCEDEST");
                String codeOperationEx = (reference != null) ? reference : pidx.get("REFERENCE");
                AgenceFse agenceFseSource = rechercheAgence(cdeAgenceSource, fseSource);
                AgenceFse agenceFseDest = rechercheAgence(cdeAgenceDest, fseDest);
                String lockKey = typeService.getCodeTypeService() + "|" + codeOperationEx;
                ReentrantLock lock = locks.computeIfAbsent(lockKey, k -> new ReentrantLock());
                lock.lock();
                try {
                    
                for (SchemaEcriture schemaEcriture : listShEcriture) {
                    if (bool) {
                        break;
                    }
                    if (cdeAgenceSource != null && cdeAgenceDest != null) {
                        bool = rechercheSchema(fseSource, fseDest, schemaEcriture, cdeAgenceSource, cdeAgenceDest);
                    } else {
                        bool = rechercheSchema(fseSource, fseDest, schemaEcriture);
                    }
                    if (bool == true) {
                        Set<LigneSchemaEcriture> listLigneShEcritureCopy = new LinkedHashSet<>(schemaEcriture.getConstitues());
                        listLigneShEcriture = trierLigneSchema(listLigneShEcritureCopy);

                    }
                }
                if (bool == true) {
                    trans.setFse(null);
                    trans.setService(null);
                    trans.setTerminal(canal);
                    trans.setReferenceEx(codeOperationEx);

                    Set<LigneTransaction> lignesConstruites = getLigneTransaction(agenceFseSource, agenceFseDest, codeUo, fseSource, fseDest, listparamUser, new LinkedHashSet<>(listLigneShEcriture), cas);
                    Oper op = new Oper();
                    op.setFseCodeDest(fseDest.getCodeFse());
                    op.setFseCodeSource(fseSource.getCodeFse());
                    /*DEFINITION DU TERMINAL**/
                    op.setTerminal(canal);
                    JsonTransaction json = new JsonTransaction();
                    trans.setLigneTransaction(lignesConstruites);
                    json.setTransation(trans);
                    op.setTextTransaction(json);
                    op.setTypeService(typeService);
                    op.setReference(codeOperationEx);
                    op.setDateOperation(ZonedDateTime.now());
 
                    final Operation opSaved = operationService.save(op);

                    List<LigneTransaction> tableauLigneTransaction = new ArrayList<>(lignesConstruites.size());


                    for (LigneTransaction lg : lignesConstruites) {
                        JsonLigneTransaction lgJson = new JsonLigneTransaction();
                        lgJson.setLigneTransation(lg);
                        LigneOper lp = new LigneOper();
                        lp.setTextLigneTransaction(lgJson);
                        lp.setIdLgSchema(lg.getIdSchema());
                        lp.setOperation(opSaved);
                        lp.setRevers(0);
                        lp.setExecute(0);
                        LigneOper res = lgOperationService.save(lp);
                        lg.setIdligne(res.getId() == null ? null : res.getId().toString());
                        tableauLigneTransaction.add(lg);
                    }

                  
                    trans.setId(opSaved.getId());
                    trans.setLigneTransaction(new LinkedHashSet<>(tableauLigneTransaction));

                    log.info("RECHERCHE AVEC SUCCES DE LA LIGNE DE TRANSACTION");
                } else {
                    trans = null;
                }
                } catch (Exception e) {
                    log.error("findTransaction failed: {}", e.getMessage(), e);
                    throw e; // ✅ rollback transaction
                } finally {
                    try {
                        lock.unlock();
                    } finally {
                        if (!lock.hasQueuedThreads()) {
                            locks.remove(lockKey, lock);
                        }
                    }
                }

            } else {
                trans = null;
            }
            log.info("FIN RECHERCHE DE LA LIGNE DE TRANSACTION");
        } catch (Exception e) {
            System.out.println("Error" + e);
        }
        return trans;
    }

    private Set<LigneSchemaEcriture> trierLigneSchema(Set<LigneSchemaEcriture> ligneSchema) {
        Set<LigneSchemaEcriture> ligneSch = ligneSchema.stream().sorted((x1, x2) -> (int) (x1.getOrdre() - x2.getOrdre())).collect(Collectors.toSet());
        return ligneSch;
    }

    public Set<LigneSchemaEcriture> findSchema(TypeService typeService, FournisseurService fseSource, FournisseurService fseDest) {
        boolean bool = false;
        List<SchemaEcriture> listShEcriture = getlistShEcriture(typeService);// schemaEcritureService.findByService(typeService);
        Set<LigneSchemaEcriture> listLigneShEcriture = new LinkedHashSet<>();
        Iterator it = listShEcriture.iterator();
        SchemaEcriture schemaEcriture = new SchemaEcriture();
        if (fseSource != null && fseDest != null) {
            while (it.hasNext() && bool == false) {
                schemaEcriture = (SchemaEcriture) it.next();
                bool = rechercheSchema(fseSource, fseDest, schemaEcriture);
                if (bool == true) {
                    listLigneShEcriture = schemaEcriture.getConstitues();
                }
            }
            LigneSchemaEcriture lgschemaEcriture = new LigneSchemaEcriture();
            /*
         while(it.hasNext() ) {
              lgschemaEcriture=(LigneSchemaEcriture) it.next();
                  lgschemaEcriture.getInscrits()
         }*/
        }
        if (listLigneShEcriture.isEmpty()) {
            return null;
        } else {
            return listLigneShEcriture;
        }
    }

    private boolean rechercheSchema(FournisseurService fseSource, FournisseurService fseDest, SchemaEcriture shEcriture) {
        return rechercheSchema(fseSource, fseDest, shEcriture, null, null);
    }

    private boolean rechercheSchema(FournisseurService fseSource, FournisseurService fseDest, SchemaEcriture shEcriture, String codeAgenceSource, String codeAgenceDest) {
        if (!(shEcriture instanceof SchemaEcrit schema)) {
            return false;
        }
        String motif = schema.getPatternCode();
        if (motif == null || motif.isBlank()) {
            log.warn("Schema {} sans motif", shEcriture.getId());
            return false;
        }
        boolean sameFse = Objects.equals(fseSource.getCodeFse(), fseDest.getCodeFse());
        boolean samePays = memePays(fseSource, fseDest);
        int siege = codeAgenceSource != null && codeAgenceSource.equals(codeAgenceDest) ? 1 : 0;
        boolean bothBanque = "BANQUE".equals(fseSource.getStatut()) && "BANQUE".equals(fseDest.getStatut());
        return SchemaMotif.correspond(motif, sameFse, samePays, siege, bothBanque, fseSource.getCodeFse(), fseDest.getCodeFse());
    }

    private boolean memePays(FournisseurService source, FournisseurService dest) {
        if (source.getPays() == null || dest.getPays() == null) {
            return false;
        }
        return Objects.equals(source.getPays().getId(), dest.getPays().getId());
    }

    private AgenceFse rechercheAgence(String codeAgence, FournisseurFSE fse) {
        AgenceFse agence = agenceService.findByCodeAgenceAndFournisseurServiceId(codeAgence, fse.getId());
        return agence;

    }

    private String rechercheValeurDansTableau(List<ParametreUtilisateur> listeParametre, String param) {

        for (ParametreUtilisateur elementParam : listeParametre) {
            if (elementParam.getCodeParametre().equals(param)) {
                return elementParam.getValeur();
            }
        }
        return null;
    }



    private Set<LigneTransaction> getLigneTransaction(AgenceFse agenceSource, AgenceFse agenceDest, String codeUo, FournisseurFSE fseSource, FournisseurFSE fseDest, List<ParametreUtilisateur> listparamUser, Set<LigneSchemaEcriture> listLigneShEcriture, CasExecution cas) throws Exception {
        Set<LigneTransaction> ligneTransaction = new HashSet<LigneTransaction>();
        ligneTransaction = presentationDetailService.getTransactionInterne(agenceSource, agenceDest, codeUo, fseSource, fseDest, listparamUser, listLigneShEcriture, cas);
        //   Set<LigneTransaction> ligneTrans = trierLigneTransaction(ligneTransaction);

        return ligneTransaction;
    }

    @Override
    public ResultMessage findByNumeroCouche(List<ResultMessage> rMsg, String numeroCouche) {
        Iterator it = rMsg.iterator();
        ResultMessage elementMsg = new ResultMessage();
        ResultMessage resultMessage = new ResultMessage();
        while (it.hasNext()) {
            elementMsg = (ResultMessage) it.next();
            if (elementMsg.getNumCouche().equals(numeroCouche)) {
                resultMessage.setErrCode(elementMsg.getErrCode());
                resultMessage.setMsg(elementMsg.getMsg());
                resultMessage.setNumCouche(elementMsg.getNumCouche());
                return resultMessage;
            }
        }
        return resultMessage;
    }

    @Override
    public List<ResultMessage> findResponseEntity(List<ResultMessage> rMsg, String numeroCouche) {
        Iterator it = rMsg.iterator();
        ObjectMapper mapper = new ObjectMapper();
        List<ResultMessage> res = null;
        ResultMessage resultMessage = new ResultMessage();
        while (it.hasNext()) {
            resultMessage = (ResultMessage) it.next();
            if (resultMessage.getNumCouche().equals(numeroCouche) && resultMessage.getResponseEntity() != null) {
                res = (List<ResultMessage>) resultMessage.getResponseEntity();
            }
        }
        return res;
    }

    @Override
    public JSONObject findResponseJson(List<ResultMessage> rMsg, String numeroCouche) {
        JSONObject json = new JSONObject();
        try {
            json.put("reponse", rMsg);
        } catch (Exception ex) {
            Logger.getLogger(PresentationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return json;
    }

    @Override
    public JSONObject getJsonObject(String numCouche, int errCode, String msg, String responseType, Object responseEntity) {
        JSONObject object = new JSONObject();
        object.put("numCouche", numCouche);
        object.put("msg", msg);
        object.put("responseType", responseType);
        object.put("errCode", errCode);
        object.put("responseEntity", responseEntity);
        return object;

    }

    @Override
    public ResultMessage getResultMessage(String numCouche, int errCode, String msg, String responseType, Object responseEntity) {
        ResultMessage object = new ResultMessage();
        object.setErrCode(errCode);
        object.setMsg(msg);
        object.setNumCouche(numCouche);
        object.setResponseEntity(responseEntity);
        object.setResponseType(responseType);

        return object;

    }

    // imports utiles
// import com.google.gson.JsonElement;
// import com.google.gson.JsonArray;
// import com.google.gson.JsonObject;
// import java.util.List;
    public boolean rechercheSucess(JsonElement jsonElement, List<String> list) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return false;
        }

        if (jsonElement.isJsonArray()) {
            JsonArray arr = jsonElement.getAsJsonArray();
            for (JsonElement el : arr) {
                if (!el.isJsonObject()) {
                    continue;
                }
                JsonObject obj = el.getAsJsonObject();
                long code = obj.has("errCode") && obj.get("errCode").isJsonPrimitive()
                        ? obj.get("errCode").getAsLong()
                        : -1L; // considère comme erreur si absent
                if (code != 0) {
                    list.add(obj.toString());
                } else {
                    return true;
                }
            }
        } else if (jsonElement.isJsonObject()) {
            JsonObject root = jsonElement.getAsJsonObject();
            JsonElement rep = root.get("reponse");
            if (rep != null && rep.isJsonArray()) {
                JsonArray arr = rep.getAsJsonArray();
                for (JsonElement el : arr) {
                    if (!el.isJsonObject()) {
                        continue;
                    }
                    JsonObject obj = el.getAsJsonObject();
                    long code = obj.has("errCode") && obj.get("errCode").isJsonPrimitive()
                            ? obj.get("errCode").getAsLong()
                            : -1L;
                    if (code != 0) {
                        list.add(obj.toString());
                    } else {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static boolean recherche(String key, JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull() || key == null) {
            return false;
        }

        if (jsonElement.isJsonArray()) {
            JsonArray arr = jsonElement.getAsJsonArray();
            for (JsonElement el : arr) {
                if (el.isJsonObject() && el.getAsJsonObject().has(key)) {
                    return true;
                }
            }
        } else if (jsonElement.isJsonObject()) {
            return jsonElement.getAsJsonObject().has(key);
        }
        return false;
    }

    /**
     *
     * @param key
     * @param jsonElement
     * @param list
     * @return
     * @throws ParseException
     */
    @Override

    public List<String> check(String key, JsonElement jsonElement, List<String> list) {
        String key2 = "responseEntity";
        if (jsonElement.isJsonArray()) {
            for (JsonElement jsonElement1 : jsonElement.getAsJsonArray()) {
                check(key, jsonElement1, list);
            }
        } else if (jsonElement.isJsonObject()) {
            Set<Map.Entry<String, JsonElement>> entrySet = jsonElement
                    .getAsJsonObject().entrySet();

            for (Map.Entry<String, JsonElement> entry : entrySet) {
                String key1 = entry.getKey();
                if (key1.equals(key)) {

                    if (!"null".equals(entry.getValue().toString())) {
                        try {
                            if (recherche(key2, entry.getValue())) {

                                list.add(entry.getValue().toString());
                            }
                        } catch (Exception e) {

                        }
                    }
                }

                //JSONObject jsonObject = (JSONObject) obj;
                check(key, entry.getValue(), list);

            }

        } else if (jsonElement.toString().equals(key)) {
            list.add(jsonElement.toString());
        }
        return list;
    }

    @Override
    public Object setJsonResponseEntity(List<ResultMessage> rMsg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
