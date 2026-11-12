/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service.impl;

import com.m2i.omnicanal.model.Agence;
import com.m2i.omnicanal.model.CasExecution;
import com.m2i.omnicanal.model.Compte;
import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.model.LigneSchemaEcriture;
import com.m2i.omnicanal.model.LigneTransaction;
import com.m2i.omnicanal.model.Parametre;
import com.m2i.omnicanal.model.ParametreMethode;
import com.m2i.omnicanal.model.ParametreSchema;
import com.m2i.omnicanal.model.ParametreUtilisateur;
import com.m2i.omnicanal.model.Relation;
import com.m2i.omnicanal.model.TypeService;
import com.m2i.omnicanal.model.enumeration.CategorieParametre;
import com.m2i.omnicanal.model.enumeration.CodeParametreMethode;
import com.m2i.omnicanal.model.enumeration.ImputeAgence;
import com.m2i.omnicanal.model.enumeration.ParamatreType;
import com.m2i.omnicanal.model.enumeration.ProprieteApi;
import com.m2i.omnicanal.model.enumeration.TypeEnvoi;
import com.m2i.omnicanal.model.enumeration.TypeMethode;
import com.m2i.omnicanal.presentation.domain.AgenceFse;
import com.m2i.omnicanal.presentation.domain.CompteFse;
import com.m2i.omnicanal.presentation.domain.FournisseurFSE;
import com.m2i.omnicanal.presentation.domain.InterfaceUser;
import com.m2i.omnicanal.presentation.domain.MethodeFonction;
import com.m2i.omnicanal.presentation.domain.Parameters;
import com.m2i.omnicanal.presentation.domain.dto.ContextRelation;
import com.m2i.omnicanal.presentation.domain.dto.FournisseurServiceAgence;

import com.m2i.omnicanal.presentation.repository.AgenceRepository;
import com.m2i.omnicanal.presentation.repository.MethodeFonctionRepository;
import com.m2i.omnicanal.presentation.repository.ServiceMethodeRepository;
import com.m2i.omnicanal.presentation.repository.CompteRepository;
import com.m2i.omnicanal.presentation.repository.InterfaceUtilisateurRepository;
import com.m2i.omnicanal.presentation.repository.RelationRepository;
import com.m2i.omnicanal.presentation.service.AgenceService;
import com.m2i.omnicanal.presentation.service.CompteService;
import com.m2i.omnicanal.presentation.service.FournisseurServiceService;
import com.m2i.omnicanal.presentation.service.InterfaceUtilisateurService;

import com.m2i.omnicanal.presentation.service.PresentationDetailService;
import com.m2i.omnicanal.presentation.web.rest.MiddleWareResource;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.StopWatch;

/**
 *
 * @author cda Cette classe est un service permettant de constituer les lignes
 * de transactions
 */
@org.springframework.stereotype.Service
public class PresentationDetailServiceImpl implements PresentationDetailService {

    @Inject
    CompteRepository compteRepository;
    @Inject
    RelationRepository relationRepository;
    @Inject
    InterfaceUtilisateurRepository interfaceUtilisateurRepository;

    @Inject
    FournisseurServiceService fournisseurServiceService;
    @Inject
    AgenceRepository agenceRepository;

    final InterfaceUtilisateurService interfaceUtilisateurService;
    AgenceService agenceService;
    private final Logger log = LoggerFactory.getLogger(PresentationDetailServiceImpl.class);

    public PresentationDetailServiceImpl(AgenceService agenceService, InterfaceUtilisateurService interfaceUtilisateurService) {
        this.interfaceUtilisateurService = interfaceUtilisateurService;
        this.agenceService = agenceService;
    }

    /**
     *
     * @param fourn
     * @param serv
     * @param typeMetode
     * @return the MethodeFonction
     */
    @Cacheable(value = "interfaceUser", key = "#p0 + '|' + #p1+ '|' + #p2", sync = true)
    public InterfaceUser getInterfaceUser(String codeFse, TypeMethode typeMethode, TypeEnvoi typeEnvoi) {
        InterfaceUser interfaceUser = interfaceUtilisateurService.getInterfaceUser(codeFse, typeMethode, typeEnvoi);
        return interfaceUser;
    }

    private MethodeFonction getMethodeApiService(FournisseurService FseSource, FournisseurService FseDest, ProprieteApi ApiAutiliser, TypeMethode typeMethode) {
        FournisseurService fournisseur = null;
        String keyMap = "";
        switch (ApiAutiliser) {
            case FSESOURCE:
                /*recuperer le fournisseur source*/
                fournisseur = FseSource;
                break;
            case FSEDESTINATAIRE:
                fournisseur = FseDest;
                /*recuperer le fournisseur destinataire*/
                break;
            case FSEPARTENAIRE:

                fournisseur = fournisseurServiceService.findByFseCompense(FseDest);
                //        fournisseur= relationRepository.findPartenaireRelation(FseSource,FseDest,serv.getTypeService());
                //   fournisseur=FsePartenaire;
                //recuperer le fournisseur partenaire
                break;
        }
        if (fournisseur != null) {
            keyMap = fournisseur.getCodeFse() + typeMethode.name() + TypeEnvoi.REQUEST.name();

            InterfaceUser interfaceUt = getInterfaceUser(fournisseur.getCodeFse(), typeMethode, TypeEnvoi.REQUEST);
//interfaceUtilisateurRepository.findParTypeMethode(fournisseur.getCodeFse(), typeMethode, TypeEnvoi.REQUEST);
            if (interfaceUt != null) {
                return (MethodeFonction) interfaceUt.getMethode();
            } else {
                return null;
            }
        } else {
            return null;
        }

        //.findMethodeByFournisseurService(fournisseur,serv,typeMethode);
    }

    private boolean findInListx(FournisseurServiceAgence cible, List<FournisseurServiceAgence> liste) {
        String code = cible.getFse().getCodeFse();
        return liste.stream().anyMatch(f -> code.equals(f.getFse().getCodeFse()));
    }

    private boolean findInList(FournisseurServiceAgence fournisseurServiceRech, List<FournisseurServiceAgence> listFseSource) {
        Iterator it = listFseSource.iterator();
        boolean bool = false;
        String codeFse = fournisseurServiceRech.getFse().getCodeFse();
        while (it.hasNext() && bool == false) {
            FournisseurServiceAgence fournisseurService = (FournisseurServiceAgence) it.next();
            if (codeFse.equals(fournisseurService.getFse().getCodeFse())) {
                bool = true;
                return true;
            } else {
                bool = false;
                //  listFseSource.add(fournisseurServiceRech);
            }

        }

        return false;
    }

    private List<FournisseurServiceAgence> rechercheList(List<FournisseurServiceAgence> a, List<FournisseurServiceAgence> b) {
        List<FournisseurServiceAgence> out = new ArrayList<>(a.size() + b.size());
        out.addAll(a);
        out.addAll(b);
        return out;
    }

    /*
    private List<FournisseurServiceAgence> rechercheList(List<FournisseurServiceAgence> listFseSource, List<FournisseurServiceAgence> listFseRech) {
        List<FournisseurServiceAgence> listFseSourceFinal = listFseSource;

        Iterator itD = listFseRech.iterator(); //* la liste utilisée pour la recherche
        boolean bool;
        for (FournisseurServiceAgence fournisseurServiceRech : listFseRech) {
            listFseSourceFinal.add(fournisseurServiceRech);
        }

        return listFseSourceFinal;
    }*/
    private List<FournisseurServiceAgence> inverseList(List<FournisseurServiceAgence> list) {
        List<FournisseurServiceAgence> listFseInserve = new ArrayList<>(list);
        java.util.Collections.reverse(listFseInserve);
        return listFseInserve;
    }

    /*
    private List<FournisseurServiceAgence> inverseList(List<FournisseurServiceAgence> listFse) {
        int tailleTableau = listFse.size();
        List<FournisseurServiceAgence> listFseInserve = new ArrayList();
        int i = tailleTableau - 1;
        while (i >= 0) {
            listFseInserve.add(listFse.get(i));
            i = i - 1;
        }
        return listFseInserve;
    }
     */
    private List<ContextRelation> getListeFournRelation(List<FournisseurServiceAgence> listFse) {
        int j = 1;
        int i;
        List<ContextRelation> listContextRelation = new ArrayList();
        int tailleTableau = listFse.size();
        /*S pour source*/
        ContextRelation ctx;
        i = 0;
        if (tailleTableau > 1) {
            while (i < tailleTableau - 1) {
                ctx = new ContextRelation();
                ctx.setFseSource(listFse.get(i).getFse());
                ctx.setAgenceSource(listFse.get(i).getAgence());
                ctx.setFseDest(listFse.get(j).getFse());
                ctx.setAgenceDest(listFse.get(j).getAgence());
                listContextRelation.add(ctx);

                j = j + 1;
                i = j;
                j = i + 1;
            }
        } else if (tailleTableau == 1) {
            ctx = new ContextRelation();
            ctx.setFseSource(listFse.get(0).getFse());
            ctx.setAgenceSource(listFse.get(0).getAgence());
            ctx.setFseDest(listFse.get(0).getFse());
            ctx.setAgenceDest(listFse.get(0).getAgence());
            listContextRelation.add(ctx);
        }

        return listContextRelation;
    }

    private List<FournisseurServiceAgence> findFseIntervenant(FournisseurService fse, List<FournisseurServiceAgence> listFse, FournisseurService fseLien) {
        FournisseurService fsePartenaire = null;
        if (fse.getCodeFse().equals(fseLien.getCodeFse())) {
            return listFse;
        } else {
            if (fse.getPays().equals(fseLien.getPays())) {
                return listFse;
            } else {
                fsePartenaire = fse.getFseCompense();
                /* directe*/
                if (fsePartenaire != null) {
                    if (fsePartenaire.getCodeFse().equals(fseLien.getCodeFse())) {
                        return listFse;
                    } else {
                        FournisseurServiceAgence fournisseurServiceAgence = new FournisseurServiceAgence();
                        fournisseurServiceAgence.setFse(fsePartenaire);
                        Agence ag = agenceService.findAgenceSiege(fsePartenaire);
                        fournisseurServiceAgence.setAgence(ag);
                        listFse.add(fournisseurServiceAgence);
                        return findFseIntervenant(fsePartenaire, listFse, fseLien);
                    }

                } else {
                    return listFse;
                }
            }

        }

    }

    private List<ContextRelation> findListeRelation(List<FournisseurServiceAgence> listFseSource) {

        List<ContextRelation> listContextRelation = new ArrayList();

        listContextRelation = getListeFournRelation(listFseSource);

        return listContextRelation;
    }

    private Set<LigneSchemaEcriture> trierLigneSchema(Set<LigneSchemaEcriture> ligneSchema) {
        Set<LigneSchemaEcriture> ligneSch = ligneSchema.stream().sorted((x1, x2) -> (int) (x1.getOrdre() - x2.getOrdre())).collect(Collectors.toSet());
        return ligneSch;
    }

    private boolean isTraiteligneSuivante(Set<LigneTransaction> tableau, LigneSchemaEcriture lg) {
        for (LigneTransaction t : tableau) {
            if (t.getIdSchema() != null && t.getIdSchema().equals(lg.getId()) && !lg.isCanDuplicate()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Set<LigneTransaction> getTransactionInterne(
            AgenceFse agenceSource,
            AgenceFse agenceDest,
            String codeUo,
            FournisseurFSE fseSource,
            FournisseurFSE fseDest,
            List<ParametreUtilisateur> listparamUser,
            Set<LigneSchemaEcriture> listLigneShEcriture,
            CasExecution cas) throws Exception {
        Set<LigneTransaction> result = new LinkedHashSet<>();   // <-- ordre stable

        // Set<LigneTransaction> result = new HashSet<>();
        StopWatch stopWatch = new StopWatch("Measure  getTransactionInterne");

        // Prépare les couples FSE-Agence
        List<FournisseurServiceAgence> listFseSource = new ArrayList<>();
        FournisseurServiceAgence fsaSrc = new FournisseurServiceAgence();
        fsaSrc.setFse(fseSource);
        fsaSrc.setAgence(agenceSource);
        listFseSource.add(fsaSrc);

        List<FournisseurServiceAgence> listFseDest = new ArrayList<>();
        FournisseurServiceAgence fsaDst = new FournisseurServiceAgence();
        fsaDst.setFse(fseDest);
        fsaDst.setAgence(agenceDest);
        listFseDest.add(fsaDst);

        // Détermine la liste finale d'intervenants
        List<FournisseurServiceAgence> listFseS = findFseIntervenant(fseSource, listFseSource, fseDest);
        List<FournisseurServiceAgence> listFseD = findFseIntervenant(fseDest, listFseDest, fseSource);
        List<FournisseurServiceAgence> listFseFinal = rechercheList(listFseS, inverseList(listFseD));

        // Construit les relations de contexte
        List<ContextRelation> listContextRelation = findListeRelation(listFseFinal);

        List<LigneSchemaEcriture> lignesShTriees = new ArrayList<>(listLigneShEcriture);
        lignesShTriees.sort(Comparator
                .comparing(LigneSchemaEcriture::getOrdre, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(LigneSchemaEcriture::getId, Comparator.nullsLast(Long::compareTo)));

        int indexRelation = 0;
        int tailleChaine = listContextRelation.size();
        stopWatch.start("1. getTransactionInterne");
        for (ContextRelation ctx : listContextRelation) {

            for (LigneSchemaEcriture ligne : lignesShTriees) {

                // Si la ligne a déjà été traitée / doit être sautée, on continue
                if (!isTraiteligneSuivante(result, ligne)) {
                    continue;
                }

                // Copie modifiable des paramètres
                List<ParametreSchema> params = new ArrayList<>(ligne.getInscrits());

                // Gestion duplication multi-maillons
                boolean canDuplicate = (tailleChaine > 1) && ligne.isCanDuplicate();
                if (canDuplicate) {
                    // Premier maillon : on ajuste si le FSE dest du contexte ≠ FSE dest final
                    if (indexRelation == 0) {
                        if (!ctx.getFseDest().getCodeFse().equals(fseDest.getCodeFse())) {
                            params = changeParamSchema(params, CodeParametreMethode.CPTDEST.toString());
                        }
                    }
                    // Maillons intermédiaires/finaux : on ajuste si le FSE source du contexte ≠ FSE source final
                    if (indexRelation > 0 && indexRelation <= tailleChaine - 1) {
                        if (!ctx.getFseSource().getCodeFse().equals(fseSource.getCodeFse())) {
                            params = changeParamSchema(params, CodeParametreMethode.CPTSOURCE.toString());
                        }
                    }
                }

                // Debug éventuel
                //    Set<ParametreSchema> setParams = new HashSet<>(params);
                Set<ParametreSchema> setParams = new LinkedHashSet<>(params); // ordre conservé

                LigneTransaction lt = getLigneTransaction(
                        ctx.getAgenceSource(),
                        ctx.getAgenceDest(),
                        ligne.getSchemaEcriture().getTypeService(),
                        codeUo,
                        ctx.getFseSource(),
                        ctx.getFseDest(),
                        ligne.getApiUtil(),
                        listparamUser,
                        ligne.getTypeMethode(),
                        setParams,
                        ligne.getAgenceImput(),
                        cas
                );

                lt.setPriorite(ligne.getPriorite());
                lt.setOrdre(ligne.getOrdre());
                lt.setIdSchema(ligne.getId());

                result.add(lt);
            }

            indexRelation++;
        }

        List<LigneTransaction> ordered = new ArrayList<>(result);
        ordered.sort(
                Comparator.comparing(LigneTransaction::getOrdre, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(LigneTransaction::getIdSchema, Comparator.nullsLast(Long::compareTo))
                        .thenComparing(LigneTransaction::getIdligne, Comparator.nullsLast(String::compareTo))
        );

        stopWatch.stop();
        return new LinkedHashSet<>(ordered);
        //  return result;
    }

    private Set<Parametre> getReverse(Set<Parametre> listParma) {
        //Set<Parametre> out = new HashSet<>(listParma.size());
        Set<Parametre> out = new LinkedHashSet<>(listParma.size());

        for (Parametre p : listParma) {
            Parametre c = new Parametre();
            // copie des champs utiles…
            c.setTypeParametre(p.getTypeParametre());
            c.setValeur(p.getValeur());
            c.setCodeParametre(p.getCodeParametre());
            c.setCodeParametreRevers(p.getCodeParametreRevers());
            c.setCodeParametreFonction(p.getCodeParametreFonction());

            if (p.getCodeParametreRevers() != null) {
                String tmp = c.getCodeParametre();
                c.setCodeParametre(c.getCodeParametreRevers());
                c.setCodeParametreRevers(tmp);
            }
            if (c.getCodeParametreFonction() != null) {
                if (c.getCodeParametreFonction().equals(CodeParametreMethode.CPTSOURCE.toString())) {
                    c.setCodeParametreFonction(CodeParametreMethode.CPTDEST.toString());
                } else if (c.getCodeParametreFonction().equals(CodeParametreMethode.CPTDEST.toString())) {
                    c.setCodeParametreFonction(CodeParametreMethode.CPTSOURCE.toString());
                }
            }
            out.add(c);
        }
        return out;
    }

    /*
    private Set<Parametre> getReverse(Set<Parametre> listParma) {

        Set<Parametre> newListParama = new HashSet();
        for (Parametre param : listParma) {

            if (param.getCodeParametreRevers() != null) {
           
                String codeParametre = param.getCodeParametre();
                param.setCodeParametre(param.getCodeParametreRevers());
                param.setCodeParametreRevers(codeParametre);

            }
            if (param.getCodeParametreFonction() != null) {
                if (param.getCodeParametreFonction().equals(CodeParametreMethode.CPTSOURCE.toString())) {
                    param.setCodeParametreFonction(CodeParametreMethode.CPTDEST.toString());
                } else if (param.getCodeParametreFonction().equals(CodeParametreMethode.CPTDEST.toString())) {
                    param.setCodeParametreFonction(CodeParametreMethode.CPTSOURCE.toString());
                }
            }

            newListParama.add(param);
        }
        return newListParama;
    }*/
    private boolean isParamIsZero(Set<Parametre> listParma) {
        String valeurMontant;
        for (Parametre param : listParma) {

            if (param.getCodeParametreFonction() != null && param.getCodeParametreFonction().equals(CodeParametreMethode.MONTANT.toString()) && param.getValeur() != null) {
                valeurMontant = "0";
                if (param.getValeur().length() > 0) {
                    valeurMontant = param.getValeur();
                }
                if (Double.parseDouble(valeurMontant) == 0D) {
                    return true;
                }
            }

        }
        return false;
    }

    private LigneTransaction getLigneTransaction(Agence agenceSource, Agence agenceDest, TypeService typeService, String codeUo, FournisseurService fseSource, FournisseurService fseDest, ProprieteApi ApiAutiliser, List<ParametreUtilisateur> listparamUser, TypeMethode typeMethode, Set<ParametreSchema> constitues, ImputeAgence agenceImput, CasExecution cas) throws Exception {

        StopWatch stopWatch = new StopWatch("MESURE AGECNCE getLigneTransaction");
        LigneTransaction lignTrans = new LigneTransaction();
        InterfaceUser interfaceUtil = getInterfaceUtilisateur(fseSource, fseDest, typeMethode, ApiAutiliser);/*recupère l'interface utilisateur de la methode en question*/
        Set<ParametreUtilisateur> paramUserInterface = interfaceUtil.getConstitues();

        MethodeFonction metFionction;
        metFionction = getMethodeApiService(fseSource, fseDest, ApiAutiliser, typeMethode);

        lignTrans.setMethodeApi(metFionction);
        Set<ParametreMethode> paramMethode;
        //=  metFionction.getPossedes();
        //modification le 03/07/2017  
        //getParametresMethodeValue( codeUo, fseSource,  fseDest, listparamUser,paramMethode,  typeService,ApiAutiliser,constitues,  paramUserInterface);

        /* fonction permettant de comparer les données utilisateur avec les données paramètres interface. renseigner les valeurs en cas d'égalite, et retourne la lis  */
        stopWatch.start("1. RECUPERATION DES USERINTERFACE");

        Set<ParametreUtilisateur> paramReturnedUserInterface = getParametresInterfaceValue(listparamUser, paramUserInterface);
        stopWatch.stop();
        stopWatch.start("1. RECUPERATION DES USERINTERFACE getParametresMethodeValue");
        paramMethode = getParametresMethodeValue(agenceSource, agenceDest, codeUo, fseSource, fseDest, listparamUser, typeService, ApiAutiliser, constitues, paramReturnedUserInterface, typeMethode, agenceImput);
        stopWatch.stop();
        stopWatch.start("1. RECUPERATION DES USERINTERFACE getParametres");
        Set<Parametre> listeParam = getParametres(codeUo, fseSource, fseDest, listparamUser, paramMethode, typeService, ApiAutiliser, constitues, paramReturnedUserInterface);
        /*Pour gérer la reversion lorsqu'il s agit d'un cas normal, ou un cas de reversion*/
        stopWatch.stop();
        boolean isParamIszero = isParamIsZero(listeParam);
        if (isParamIszero == true) {
            lignTrans.setZeroExiste(1);
        } else {
            lignTrans.setZeroExiste(0);
        }
        Set<Parametre> listeParamNew;
        if (cas.equals(CasExecution.NORMAL)) {
            lignTrans.setParametreService(listeParam);
        } else {
            listeParamNew = getReverse(listeParam);
            lignTrans.setParametreService(listeParamNew);
        }
        ajouterOtp(lignTrans.getParametreService(), listparamUser);
        return lignTrans;
    }

    /** Le mode OTP du fournisseur lit ce code sur la ligne, même s'il n'est pas un paramètre de méthode. */
    private static void ajouterOtp(Set<Parametre> ligne, List<ParametreUtilisateur> recus) {
        if (ligne == null || recus == null) {
            return;
        }
        for (Parametre existant : ligne) {
            if (existant.getCodeParametre() != null && estOtp(existant.getCodeParametre()) && renseigne(existant.getValeur())) {
                return;
            }
        }
        for (ParametreUtilisateur recu : recus) {
            if (recu.getCodeParametre() == null || !estOtp(recu.getCodeParametre()) || !renseigne(recu.getValeur())) {
                continue;
            }
            Parameters otp = new Parameters();
            otp.setCodeParametre("otp");
            otp.setValeur(recu.getValeur().trim());
            ligne.add(otp);
            return;
        }
    }

    private static boolean estOtp(String code) {
        String valeur = code.trim().toUpperCase(java.util.Locale.ROOT);
        return "OTP".equals(valeur) || "CODEOTP".equals(valeur) || "CODE_OTP".equals(valeur);
    }

    private InterfaceUser getInterfaceUtilisateur(FournisseurService fseSource, FournisseurService fseDest, TypeMethode typeMethode, ProprieteApi ApiAutiliser) {
        FournisseurService fournisseur = null;
        switch (ApiAutiliser) {
            case FSESOURCE:
                /*recuperer le fournisseur source*/
                fournisseur = fseSource;
                break;
            case FSEDESTINATAIRE:
                fournisseur = fseDest;
                /*recuperer le fournisseur destinataire*/
                break;
            case FSEPARTENAIRE:
                fournisseur = fournisseurServiceService.findByFseCompense(fseDest);
                //        fournisseur= relationRepository.findPartenaireRelation(FseSource,FseDest,serv.getTypeService());
                //   fournisseur=FsePartenaire;
                //recuperer le fournisseur partenaire
                break;
        }
        InterfaceUser interfaceUt = getInterfaceUser(fournisseur.getCodeFse(), typeMethode, TypeEnvoi.REQUEST);

        // InterfaceUser interfaceUt = interfaceUtilisateurRepository.findParTypeMethode(fournisseur.getCodeFse(), typeMethode, TypeEnvoi.REQUEST);
        return interfaceUt;
    }

    private String rechercheParamUtil(List<ParametreUtilisateur> listeParametre, String param, ProprieteApi ApiAutiliser) {
        String valeur = null;
        Iterator it = listeParametre.iterator();
        ParametreUtilisateur elementParam;
        while (it.hasNext()) {
            elementParam = (ParametreUtilisateur) it.next();
            if (elementParam.getCodeParametre().equals(param)) {
                if ((elementParam.getParametreApi() == ApiAutiliser) || (elementParam.getParametreApi() == null) || (elementParam.getParametreApi().equals(ProprieteApi.EMPTY))) {
                    /*On recherche le paramètre à utiliser pour l'api sélectionné*/
                    return elementParam.getValeur();
                }

            }

        }
        return valeur;
    }

    public static Object executerMethode(Object objet, String nomMethode,
            Object[] parametres) throws Exception {
        Object retour = null;
        Class[] typeParametres = null;
        if (parametres != null) {
            typeParametres = new Class[parametres.length];
            for (int i = 0; i < parametres.length; ++i) {
                typeParametres[i] = parametres[i].getClass();
            }
        }

        Method m = objet.getClass().getMethod(nomMethode, typeParametres);

        if (Modifier.isStatic(m.getModifiers())) {
            retour = m.invoke(null, parametres);
        } else {
            retour = m.invoke(objet, parametres);
        }
        return retour;
    }

    private ParametreUtilisateur copyOfInterfaceUser(ParametreUtilisateur s) {
        ParametreUtilisateur c = new ParametreUtilisateur();
        c.setId(s.getId());
        c.setCodeParametre(s.getCodeParametre());
        c.setCodeParametreMethode(s.getCodeParametreMethode());
        c.setAttribut(s.getAttribut());
        c.setParametreApi(s.getParametreApi());
        c.setTypeParametre(s.getTypeParametre());
        c.setCodeParametreRevers(s.getCodeParametreRevers());
        c.setInterfaceUtilisateur(s.getInterfaceUtilisateur());
        c.setCodeParametreFonction(s.getCodeParametreFonction());
        c.setNomParametre(s.getNomParametre());
        c.setValeur(s.getValeur());
          //log.info("Variable================= {}", s.getCodeParametre() + ":" + s.getValeur());
        return c;
    }

    public Set<ParametreUtilisateur> getParametresInterfaceValue(List<ParametreUtilisateur> listparamUser, Set<ParametreUtilisateur> paramUserInterface) {
        List<ParametreUtilisateur> ifaceParams = new ArrayList<>(paramUserInterface);
        ifaceParams.sort(
                java.util.Comparator
                        .comparing(ParametreUtilisateur::getId, java.util.Comparator.nullsLast(Long::compareTo))
                        .thenComparing(ParametreUtilisateur::getCodeParametreMethode, java.util.Comparator.nullsLast(String::compareTo))
        );
        Set<ParametreUtilisateur> listeParametersInterface = new java.util.LinkedHashSet<>(ifaceParams.size());

        for (ParametreUtilisateur elementParamInterface : ifaceParams) {
            ParametreUtilisateur copie = copyOfInterfaceUser(elementParamInterface);
            String valeur = premiereRenseignee(
                    valeurPourCode(listparamUser, copie.getCodeParametreMethode()),
                    valeurPourCode(listparamUser, copie.getCodeParametre()),
                    copie.getValeur());
            copie.setValeur(renseigne(valeur) ? valeur : null);
            listeParametersInterface.add(copie);
        }
        return listeParametersInterface;

    }

    private ParametreMethode copyOfParamMethode(ParametreMethode m) {
        ParametreMethode pm = new ParametreMethode();
        pm.setId(m.getId());
        pm.setCodeParametre(m.getCodeParametre());
        pm.setCodeParametreFonction(m.getCodeParametreFonction());
        pm.setCodeParametreRevers(m.getCodeParametreRevers());
        pm.setTypeParametre(m.getTypeParametre());
        pm.setCategorieParam(m.getCategorieParam());
        pm.setValeur(m.getValeur());
        pm.setNomExterne(m.getNomExterne());
        pm.setMethode(m.getMethode());
        pm.setNomParametre(m.getNomParametre());

        // … ajoute ce qu’il faut (ordre, libellés, etc.)
        return pm;
    }

    @SuppressWarnings("unchecked")

    private Set<ParametreMethode> getParametresMethodeValue(Agence agenceSource, Agence agenceDest, String codeUo, FournisseurService fseSource, FournisseurService fseDest, List<ParametreUtilisateur> listparamUser, TypeService service, ProprieteApi ApiAutiliser, Set<ParametreSchema> paramSchema, Set<ParametreUtilisateur> paramUserInterface, TypeMethode typeMethode, ImputeAgence agenceImput) throws Exception {
        MethodeFonction metFionction;
        metFionction = getMethodeApiService(fseSource, fseDest, ApiAutiliser, typeMethode);
        Set<ParametreMethode> paramMethode = metFionction.getPossedes();
        Set<ParametreMethode> listeParameters = new LinkedHashSet<>();
        List<ParametreMethode> methodes = new ArrayList<>(paramMethode);
        methodes.sort(Comparator.comparing(ParametreMethode::getId, Comparator.nullsLast(Long::compareTo)));

        try {
            for (ParametreMethode elementParamMethode : methodes) {
                ParametreMethode pm = copyOfParamMethode(elementParamMethode);
                ParametreUtilisateur interfaceParam = interfaceDuParametre(paramUserInterface, pm.getCodeParametre());
                if (interfaceParam != null) {
                    String role = interfaceParam.getCodeParametreMethode();
                    String valeur = renseigne(interfaceParam.getValeur()) ? interfaceParam.getValeur() : null;
                    if (renseigne(role)) {
                        String depuisSchema = rechercheParamNonAssigne(agenceSource, agenceDest, interfaceParam.getAttribut(), codeUo, fseSource, fseDest, role, paramSchema, listparamUser, ApiAutiliser, agenceImput);
                        if (renseigne(depuisSchema)) {
                            valeur = depuisSchema;
                        }
                        pm.setCodeParametreFonction(role);
                    }
                    pm.setValeur(valeur == null ? "" : valeur);
                }
                listeParameters.add(pm);
            }
            propagerAlias(listeParameters, paramUserInterface);
        } catch (Exception e) {
            log.error("Résolution des paramètres de méthode interrompue", e);
        }

        return listeParameters;

    }

    private Set<Parametre> getParametres(String codeUo, FournisseurService fseSource, FournisseurService fseDest, List<ParametreUtilisateur> listparamUser, Set<ParametreMethode> constitues, TypeService service, ProprieteApi ApiAutiliser, Set<ParametreSchema> paramSchema, Set<ParametreUtilisateur> paramUserInterface) {
        // Set<Parametre> listeParameters = new HashSet();
        Set<Parametre> listeParameters = new LinkedHashSet<>();

        String valeur = null;
        //    Iterator itParam = constitues.iterator();
        for (ParametreMethode elementParam : constitues) {

            if (elementParam.getCategorieParam().equals(CategorieParametre.USER)) {

                // valeur= rechercheParamUtil( listparamUser,  elementParam.getCodeParametre(), ApiAutiliser);
            } else if (elementParam.getCategorieParam().equals(CategorieParametre.SYSTEM)) {
                // valeur=rechercheParamSystem(   fseSource,  fseDest, elementParam.getCodeParametre(), service);
            }
            Parameters param = new Parameters();
            //if (valeur!=null){
            //      param.setValeur(valeur);
            //}else{
            param.setValeur(elementParam.getValeur());
            // }

            //içi on fait une correspondance entre le code paramètre et celui de la méthode à appeler//
            param.setCodeParametre(elementParam.getCodeParametre());
            param.setCodeParametreFonction(elementParam.getCodeParametreFonction());
            param.setTypeParametre(elementParam.getTypeParametre());
            param.setCodeParametreRevers(elementParam.getCodeParametreRevers());

            // System.out.println("code Parametre"+elementParam.getCodeParametre() +" " +elementParam.getCodeParametreFonction()+" "+elementParam.getValeur() );
            listeParameters.add(param);

        }
        return listeParameters;

    }

    /**
     * cette fonction recherche dans les valeurs transmisses par l'utilisateur
     * si le codeParamètreUtilisateur est egal à l'etiquette de la ligne de
     * schema
     *
     * @param codeParam
     * @param listparamUser
     * @return
     */
    private String rechercheParamUtilisateur(String codeParam, List<ParametreUtilisateur> listparamUser) {
        boolean trouve = false;
        String valeur = "";
        Iterator iter = listparamUser.iterator();
        List<ParametreUtilisateur> listparamGlobal = new ArrayList();
        listparamGlobal = listparamUser;
        ParametreUtilisateur elementUtilisateurParam;
        while (iter.hasNext() && trouve == false) {
            elementUtilisateurParam = (ParametreUtilisateur) iter.next();
            if (elementUtilisateurParam.getCodeParametre().equals(codeParam)) {
                trouve = true;
                if (trouve) {
                    valeur = elementUtilisateurParam.getValeur();
                }

            } else {
                valeur = "";
            }

        }

        return valeur;
    }

    /*si relation, rechercher directement sans tenir compte des autres paramètre*/
 /**/
    private CompteFse getCompteUtilisateur(String cpte) {
        String[] parametre = cpte.split(Pattern.quote("#"));
        CompteFse compteFse = new CompteFse();
        int size = parametre.length;
        String agence = "";
        String banque = "";
        if (parametre.length <= 0) {
            compteFse.setNumeroCompte(cpte);
        } else {
            String compte = parametre[0];
            if (size >= 3) {
                agence = parametre[1];
                banque = parametre[2];
            } else if (size == 2) {
                agence = parametre[1];
            }
            compteFse.setCodeAgence(agence);
            compteFse.setCodeBanque(banque);
            compteFse.setNumeroCompte(compte);
        }

        return compteFse;

    }

    List<ParametreSchema> changeParamSchema(List<ParametreSchema> paramSchema, String role) {
        List<ParametreSchema> newParametreSchema = new ArrayList<>();
        for (ParametreSchema elementSchemaParam : paramSchema) {
            ParametreSchema newelementSchemaParam = new ParametreSchema();
            newelementSchemaParam.setCategorieParam(elementSchemaParam.getCategorieParam());
            newelementSchemaParam.setCodeParametre(elementSchemaParam.getCodeParametre());
            newelementSchemaParam.setCodeParametreFonction(elementSchemaParam.getCodeParametreFonction());
            newelementSchemaParam.setCodeParametreMethode(elementSchemaParam.getCodeParametreMethode());
            newelementSchemaParam.setCodeParametreRevers(elementSchemaParam.getCodeParametreRevers());
            newelementSchemaParam.setId(elementSchemaParam.getId());
            newelementSchemaParam.setLigneSchemaEcriture(elementSchemaParam.getLigneSchemaEcriture());
            newelementSchemaParam.setNomParametre(elementSchemaParam.getNomParametre());
            newelementSchemaParam.setTypeParametre(elementSchemaParam.getTypeParametre());
            newelementSchemaParam.setValeur(elementSchemaParam.getValeur());
            boolean memeRole = role.equals(elementSchemaParam.getCodeParametreMethode())
                    || role.equals(elementSchemaParam.getCodeParametreFonction());
            if (memeRole && renseigne(elementSchemaParam.getCodeParametreRevers())) {
                newelementSchemaParam.setCodeParametre(elementSchemaParam.getCodeParametreRevers());
            }
            newParametreSchema.add(newelementSchemaParam);
        }
        return newParametreSchema;
    }

    private String rechercheParamNonAssigne(
            Agence agenceSource,
            Agence agenceDest,
            String attribut,
            String codeUo,
            FournisseurService fseSource,
            FournisseurService fseDest,
            String codeParamGen,
            Set<ParametreSchema> paramSchema,
            List<ParametreUtilisateur> listparamUser,
            ProprieteApi apiAUtiliser,
            ImputeAgence agenceImput) throws Exception {

        List<ParametreSchema> candidats = new ArrayList<>();
        if (paramSchema != null) {
            for (ParametreSchema p : paramSchema) {
                String role = renseigne(p.getCodeParametreMethode()) ? p.getCodeParametreMethode() : p.getCodeParametre();
                if (codeParamGen.equals(role)) {
                    candidats.add(p);
                }
            }
        }
        candidats.sort(Comparator.comparingInt(p -> codeParamGen.equals(p.getCodeParametre()) ? 1 : 0));
        if (candidats.isEmpty()) {
            return "";
        }

        // 2) Résolution fournisseur/partenaires selon l’API + codeParametreMethode (pour PARTENAIRE)
        FournisseurService fournisseur = null;
        FournisseurService fsePartenaireSource = fseSource;
        FournisseurService fsePartenaireDest = fseDest;

        switch (apiAUtiliser) {
            case FSESOURCE: {
                fournisseur = fseSource;
                // pays différents -> partenaire côté destination
                if (!fseSource.getPays().equals(fseDest.getPays())) {
                    FournisseurService cand = fournisseurServiceService.findByFseCompense(fseSource);
                    fsePartenaireDest = (cand != null) ? cand : fseDest;
                }
                break;
            }
            case FSEDESTINATAIRE: {
                fournisseur = fseDest;
                if (!fseSource.getPays().equals(fseDest.getPays())) {
                    FournisseurService cand = fournisseurServiceService.findByFseCompense(fseDest);
                    fsePartenaireSource = (cand != null) ? cand : fseSource;
                }
                break;
            }
            case FSEPARTENAIRE: {
                // dépend de CPTSOURCE / CPTDEST porté par le ParametreSchema ciblé
                String codePM = codeParamGen;
                if (CodeParametreMethode.CPTSOURCE.toString().equals(codePM)) {
                    fournisseur = fournisseurServiceService.findByFseCompense(fseSource);
                    if (fournisseur == null) {
                        fournisseur = fseSource; // fallback
                    }
                    fsePartenaireDest = fournisseur; // partenaire côté dest
                    fsePartenaireSource = fseSource;
                } else if (CodeParametreMethode.CPTDEST.toString().equals(codePM)) {
                    fournisseur = fournisseurServiceService.findByFseCompense(fseDest);
                    if (fournisseur == null) {
                        fournisseur = fseDest; // fallback
                    }
                    fsePartenaireDest = fseDest;
                    fsePartenaireSource = fournisseur; // partenaire côté source
                } else {
                    // par défaut, se rabattre sur dest
                    fournisseur = (fournisseurServiceService.findByFseCompense(fseDest) != null)
                            ? fournisseurServiceService.findByFseCompense(fseDest)
                            : fseDest;
                }
                break;
            }
        }
        if (fournisseur == null) {
            fournisseur = fseDest; // dernier filet de sécurité
        }
        // 3) Résolution de l’agence selon ImputeAgence
        Agence agence = null;
        if (agenceImput != null) {
            switch (agenceImput) {
                case AGENCESOURCE:
                    agence = agenceSource;
                    fournisseur = fseSource;
                    break;
                case AGENCEDEST:
                    agence = agenceDest;
                    // fournisseur = fseDest; // utile si tu veux forcer
                    break;
                case AGENCESIEGESOURCE:
                case AGENCESIEGEDEST:
                    // selon ta version, c’est le siège du "fournisseur" courant
                    agence = agenceService.findAgenceSiege(fournisseur);
                    break;
            }
        }

        for (ParametreSchema candidat : candidats) {
            String saisie = rechercheParamUtilisateur(candidat.getCodeParametre(), listparamUser);
            if (renseigne(saisie)) {
                return appliquerAttribut(saisie, attribut);
            }
            if (candidat.getLigneSchemaEcriture() == null
                    || candidat.getLigneSchemaEcriture().getSchemaEcriture() == null
                    || candidat.getLigneSchemaEcriture().getSchemaEcriture().getTypeService() == null) {
                if (renseigne(candidat.getValeur())) {
                    return candidat.getValeur();
                }
                continue;
            }
            Long typeServiceId = candidat.getLigneSchemaEcriture().getSchemaEcriture().getTypeService().getId();
            String codeParam = candidat.getCodeParametre();
            CompteFse cpte = null;
            if (!fseSource.getCodeFse().equals(fseDest.getCodeFse())) {
                Relation relation = relationRepository.findRelation(
                        fsePartenaireSource,
                        fsePartenaireDest,
                        candidat.getLigneSchemaEcriture().getSchemaEcriture().getTypeService());
                if (relation != null) {
                    cpte = compteRepository.findCompteFse(
                            codeParam,
                            agence,
                            typeServiceId,
                            relation.getId(),
                            fournisseur.getCodeFse()
                    );
                }
            } else {
                cpte = compteRepository.findCompteFse(
                        agence,
                        fournisseur.getCodeFse(),
                        codeParam,
                        codeUo,
                        typeServiceId
                );
            }
            if (cpte != null) {
                String extrait = appliquerAttributCompte(cpte, attribut);
                if (renseigne(extrait)) {
                    return extrait;
                }
            }
            if (renseigne(candidat.getValeur())) {
                return candidat.getValeur();
            }
        }
        return "";
    }

    private String appliquerAttribut(String valeur, String attribut) throws Exception {
        if (!renseigne(attribut)) {
            return valeur;
        }
        return appliquerAttributCompte(getCompteUtilisateur(valeur), attribut);
    }

    private String appliquerAttributCompte(CompteFse compte, String attribut) throws Exception {
        if (compte == null) {
            return "";
        }
        if (!renseigne(attribut)) {
            return compte.getNumeroCompte() == null ? "" : compte.getNumeroCompte();
        }
        Object extrait = executerMethode(compte, attribut, null);
        return extrait == null ? "" : String.valueOf(extrait);
    }

    private ParametreUtilisateur interfaceDuParametre(Set<ParametreUtilisateur> interfaceParams, String code) {
        if (!renseigne(code) || interfaceParams == null) {
            return null;
        }
        ParametreUtilisateur parRole = null;
        for (ParametreUtilisateur param : interfaceParams) {
            if (code.equals(param.getCodeParametre())) {
                return param;
            }
            if (parRole == null && code.equals(param.getCodeParametreMethode())) {
                parRole = param;
            }
        }
        return parRole;
    }

    private void propagerAlias(Set<ParametreMethode> params, Set<ParametreUtilisateur> interfaceParams) {
        if (params == null || interfaceParams == null) {
            return;
        }
        for (ParametreUtilisateur definition : interfaceParams) {
            if (!renseigne(definition.getCodeParametre()) || !renseigne(definition.getCodeParametreMethode())) {
                continue;
            }
            ParametreMethode parCode = parametreMethode(params, definition.getCodeParametre());
            ParametreMethode parRole = parametreMethode(params, definition.getCodeParametreMethode());
            if (parCode == null || parRole == null || parCode == parRole) {
                continue;
            }
            if (renseigne(parRole.getValeur()) && !renseigne(parCode.getValeur())) {
                parCode.setValeur(parRole.getValeur());
            } else if (renseigne(parCode.getValeur()) && !renseigne(parRole.getValeur())) {
                parRole.setValeur(parCode.getValeur());
            }
        }
    }

    private ParametreMethode parametreMethode(Set<ParametreMethode> params, String code) {
        for (ParametreMethode param : params) {
            if (code.equals(param.getCodeParametre())) {
                return param;
            }
        }
        return null;
    }

    private String valeurPourCode(List<ParametreUtilisateur> listparamUser, String code) {
        if (!renseigne(code) || listparamUser == null) {
            return null;
        }
        for (ParametreUtilisateur param : listparamUser) {
            if (code.equals(param.getCodeParametre()) && renseigne(param.getValeur())) {
                return param.getValeur();
            }
        }
        return null;
    }

    private static String premiereRenseignee(String... valeurs) {
        if (valeurs == null) {
            return null;
        }
        for (String valeur : valeurs) {
            if (renseigne(valeur)) {
                return valeur;
            }
        }
        return null;
    }

    private static boolean renseigne(String valeur) {
        return valeur != null && !valeur.isBlank();
    }

    private String rechercheParamNonAssigneXXX(Agence agenceSource, Agence agenceDest, String attribut, String codeUo, FournisseurService fseSource, FournisseurService fseDest, String codeParamGen, Set<ParametreSchema> paramSchema, List<ParametreUtilisateur> listparamUser, ProprieteApi ApiAutiliser, ImputeAgence agenceImput) throws Exception {
        FournisseurService fournisseur = null;
        FournisseurService fsePartenaireDest = fseDest;
        FournisseurService fsePartenaireSource = fseSource;
        /* par défaut l'agence source du compte source*/
        Agence agence = null;
        String valeur = "";
        Iterator iter = paramSchema.iterator();
        //  ParametreSchema elementSchemaParam;
        CompteFse cpte = null;
        for (ParametreSchema elementSchemaParam : paramSchema) {

            switch (ApiAutiliser) {
                case FSESOURCE: {
                    /*recuperer le fournisseur source*/
                    fournisseur = fseSource;
                    if (!fseSource.getPays().equals(fseDest.getPays())) {
                        fsePartenaireDest = fournisseurServiceService.findByFseCompense(fseSource);
                        if (fsePartenaireDest == null) {
                            fsePartenaireDest = fseDest;
                        }
                    }

                    break;
                }
                case FSEDESTINATAIRE:
                    fournisseur = fseDest;
                    fsePartenaireDest = fseDest;
                    if (!fseSource.getPays().equals(fseDest.getPays())) {
                        fsePartenaireSource = fournisseurServiceService.findByFseCompense(fseDest);
                        if (fsePartenaireSource == null) {
                            fsePartenaireSource = fseSource;
                        }
                    }

                    /*recuperer le fournisseur destinataire*/
                    break;
                case FSEPARTENAIRE:

                    if (elementSchemaParam.getCodeParametreMethode().equals(CodeParametreMethode.CPTSOURCE.toString())) {
                        fournisseur = fournisseurServiceService.findByFseCompense(fseSource);
                        fsePartenaireDest = fournisseur;
                        fsePartenaireSource = fseSource;
                    } else if (elementSchemaParam.getCodeParametreMethode().equals(CodeParametreMethode.CPTDEST.toString())) {

                        fournisseur = fournisseurServiceService.findByFseCompense(fseDest);
                        fsePartenaireDest = fseDest;
                        fsePartenaireSource = fournisseur;
                    }

                    //        fournisseur= relationRepository.findPartenaireRelation(FseSource,FseDest,serv.getTypeService());
                    //   fournisseur=FsePartenaire;
                    //recuperer le fournisseur partenaire
                    break;
            }
            if (agenceImput != null) {
                switch (agenceImput) {
                    case AGENCESOURCE:
                        /*l'agence source source*/
                        fournisseur = fseSource;
                        agence = agenceSource;
                        break;
                    case AGENCEDEST:

                        agence = agenceDest;
                        /*recuperer le fournisseur destinataire*/
                        break;
                    case AGENCESIEGESOURCE:
                        agence = agenceRepository.findAgenceSiege(fournisseur);
                        /*recuperer le fournisseur destinataire*/
                        break;
                    case AGENCESIEGEDEST:
                        agence = agenceRepository.findAgenceSiege(fournisseur);
                        /*recuperer le fournisseur destinataire*/
                        break;

                }

            }

            if (!codeParamGen.equals(elementSchemaParam.getCodeParametreMethode())) {
                continue;
            }

            if (elementSchemaParam.getCodeParametreMethode().equals(codeParamGen)) {

                /**
                 * Rechercher d'abord dans la liste des paramètres envoyé en
                 * appelant RecherParamUtilisateur *
                 */
                String val = rechercheParamUtilisateur(elementSchemaParam.getCodeParametre(), listparamUser);

                if (val != null && !val.isEmpty()) {
                    if (attribut != null) {
                        CompteFse c = getCompteUtilisateur(val);
                        return (String) executerMethode(c, attribut, null);
                    }
                    return val;
                }
                /*
                if (val.length() > 0) {
                    if (attribut != null) {
                        cpte = getCompteUtilisateur(val);
                        valeur = (String) executerMethode(cpte, attribut, null);
                    } else {
                        valeur = val;
                    }
                } else {
                    valeur = "";
                }*/

                if (valeur.length() <= 0) {
                    if (!fseSource.getCodeFse().equals(fseDest.getCodeFse())) {

                        Relation relation = relationRepository.findRelation(fsePartenaireSource, fsePartenaireDest, elementSchemaParam.getLigneSchemaEcriture().getSchemaEcriture().getTypeService());

                        cpte = compteRepository.findCompteFse(elementSchemaParam.getCodeParametre(), agence, elementSchemaParam.getLigneSchemaEcriture().getSchemaEcriture().getTypeService().getId(), relation.getId(), fournisseur.getCodeFse());

                    } else {
                        cpte = compteRepository.findCompteFse(agence, fournisseur.getCodeFse(), elementSchemaParam.getCodeParametre(), codeUo, elementSchemaParam.getLigneSchemaEcriture().getSchemaEcriture().getTypeService().getId());
                    }

                    if (cpte != null) {
                        if (attribut != null) {
                            valeur = (String) executerMethode(cpte, attribut, null);
                            //* si valeur ="" instancier la nouvelle classe
                        } else {
                            valeur = cpte.getNumeroCompte();
                        }
                        //  valeur= cpte.getNumeroCompte();
                    }
                }

                return valeur;
            }

        }

        return valeur;
    }

}
