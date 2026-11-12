package com.m2i.pti.gateway.rest;

import com.m2i.pti.mobile.rest.*;
import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.outils.util.ErrorException;
import com.m2i.pti.domain.Canal;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.CompteAutre;
import com.m2i.pti.domain.EtiquetteCompte;
import com.m2i.pti.domain.Facture;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.EvenementOperation;
import com.m2i.pti.domain.Personne;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.Action;
import com.m2i.pti.domain.enumeration.EtatCompte;
import com.m2i.pti.domain.enumeration.EtiquetteCompteCode;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.domain.enumeration.TypeEvenementOperation;
import com.m2i.pti.domain.enumeration.TypeServiceLibelle;
import com.m2i.pti.domain.enumeration.TypeTransactionCompte;
import com.m2i.pti.mobile.dto.*;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.EtiquetteCompteRepository;
import com.m2i.pti.repository.FactureRepository;
import com.m2i.pti.repository.EvenementOperationRepository;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.BilanOperationService;
import com.m2i.pti.service.CanalService;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.TransactionComptePtiService;
import com.m2i.pti.service.TypeServiceService;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.service.dto.Balance;
import com.m2i.pti.service.dto.CollectionFacture;
import com.m2i.pti.service.dto.CustomerAccount;
import com.m2i.pti.service.dto.CustomerInformation;
import com.m2i.pti.service.dto.ElementFacture;
import com.m2i.pti.service.dto.FactureFournisseur;
import com.m2i.pti.service.dto.ListAccount;
import com.m2i.pti.service.dto.Message;
import com.m2i.pti.service.dto.MessageRet;
import com.m2i.pti.service.dto.RegleFacturier;
import com.m2i.pti.service.dto.StoppageData;
import com.m2i.pti.service.dto.TableauCumulDTO;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.LocalDate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StopWatch;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;

@RestController
@RequestMapping("/api/gateway")
public class GatewayResource {

    private final Logger log = LoggerFactory.getLogger(MobileResource.class);
    private final MobileService mobileService;
    private final CompteService compteService;
    private final ApiService apiService;
    private final TransactionComptePtiService transactionComptePtiService;
    private final OperationRepository operationRepository;
    private final FactureRepository factureRepository;
    private final EtiquetteCompteRepository etiquetteCompteRepository;
    EvenementOperationRepository evenementOperationRepository;
    UniteOrganisationRepository uniteOrganisationRepository;
    UniteOrganisationService uniteOrganisationService;
    BilanOperationService bilanOperationService;
    TypeServiceService typeServiceService;
    CanalService canalService;

    public GatewayResource(CanalService canalService, TypeServiceService typeServiceService, BilanOperationService bilanOperationService, UniteOrganisationService uniteOrganisationService, UniteOrganisationRepository uniteOrganisationRepository, EvenementOperationRepository evenementOperationRepository, EtiquetteCompteRepository etiquetteCompteRepository, FactureRepository factureRepository, OperationRepository operationRepository, MobileService mobileService, ApiService apiService, CompteService compteService, TransactionComptePtiService transactionComptePtiService) {
        this.mobileService = mobileService;
        this.apiService = apiService;
        this.compteService = compteService;
        this.transactionComptePtiService = transactionComptePtiService;
        this.operationRepository = operationRepository;
        this.factureRepository = factureRepository;
        this.etiquetteCompteRepository = etiquetteCompteRepository;
        this.evenementOperationRepository = evenementOperationRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.uniteOrganisationService = uniteOrganisationService;
        this.bilanOperationService = bilanOperationService;
        this.typeServiceService = typeServiceService;
        this.canalService = canalService;
    }

    @Secured(AuthoritiesConstants.ADMIN)
    @PostMapping("/historique")
    @Timed
    public Message getHistorique(@RequestParam("numeroCompte") String numeroCompte,
            @RequestParam("codeFse") String codeFse, @RequestParam("debutPeriode") String debutPeriode,
            @RequestParam("finPeriode") String finPeriode) throws IOException, NoSuchAlgorithmException, JAXBException {
        Compte cpte = compteService.findByCompteFse(numeroCompte, codeFse);

//        Set<TransactionComptePti> transactionComptePtis = cpte.getCompteAutre().getTransactionComptePtis();
        ZonedDateTime vDatedebut = getZoneDateTimeFromPeriode(debutPeriode);
        ZonedDateTime vDatefin = getZoneDateTimeFromPeriode(finPeriode);

        Message msg = mobileService.getAccountHistTrans(cpte, vDatedebut, vDatefin);
        // Message msg = new Message();
        /*
        Statement stmt = new Statement();
        boolean trouve = false;
        List<Mouvement> listTrans = new ArrayList<Mouvement>();

        List<TransactionComptePti> resultTrans = transactionComptePtis.stream()
                .collect(Collectors.toList());
        if (resultTrans.size() > 0) {
            trouve = true;
            resultTrans.forEach(trans -> {
                Mouvement mv = new Mouvement();
                mv.setMontantMvt(trans.getMontant().doubleValue());
                mv.setDateMvt(trans.getDate().toLocalDateTime().format(java.time.format.DateTimeFormatter.ISO_DATE));
                mv.setSens(trans.getType().name());
                mv.setDescription(trans.getOperation().getCodeOperation() + " - " + trans.getOperation().getMotif() + " Solde Après:" + trans.getSoldeApres() + " frais: " + trans.getOperation().getMontantFrais());
                listTrans.add(mv);
            });
        } else {
            trouve = false;
            msg.setCodeErr("-1");
            msg.setMsg("Attention !! Aucune ligne retournée");
        }
        stmt.setListeMouvement(listTrans);
        String str = (String) Util.marshall(stmt, String.class);
        msg.setText(str);*/
        return msg;
    }

    @Secured(AuthoritiesConstants.ADMIN)
    @PostMapping("/mini-releve")
    @Timed
    public Message getMiniReleve(@RequestParam("numeroCompte") String numeroCompte,
            @RequestParam("codeFse") String codeFse, @RequestParam("nombre") String nbre) throws JAXBException {

        Compte cpte = compteService.findByCompteFseOptimise(numeroCompte, codeFse);
        Message msg = mobileService.getMiniReleveTrans(cpte, (nbre));
        return msg;
    }

    public static String padLeft(String s, int n) {
        return String.format("%" + n + "s", s).replace(' ', '0');
    }

    private ZonedDateTime getZoneDateTimeFromPeriode(String dateRech) {
        ZoneId zoneId = ZoneId.of("UTC");
        String[] paramDate = dateRech.split("-");
        if (paramDate.length == 3) {
            String jj = paramDate[0];
            String mm = paramDate[1];
            /*mois*/
            String aa = paramDate[2];
            /*annee*/
            String jourP = padLeft(jj, 2);/*jour **/
            String mmP = padLeft(mm, 2);
            /**
             * mois pad *
             */
            return ZonedDateTime.of(Integer.parseInt(aa), Integer.parseInt(mmP), Integer.parseInt(jourP), 00, 00, 00, 1234, zoneId);
        }

        return null;

    }

    private TableauCumulDTO controleSeuil(Compte compte, BigDecimal montant, String sens) {
        if (compte != null && compte.getEtiquette().equals(EtiquetteCompteCode.CPTECLIENT.name()) && compte.getCompteAutre() != null) {

            if (compte.getClient() != null) {
                if ("D".equals(sens)) {
                    return mobileService.controleSeuilMontant(montant, compte.getCompteAutre().getSolde(), compte.getClient());
                } else {
                    return mobileService.controleSeuil(montant, compte.getCompteAutre().getSolde(), compte.getClient());
                }

            }

        }
        return null;
    }

    private ZonedDateTime convertStringToLocalDate(String dateTobeConverted) {
        java.time.LocalDate dateLoc = java.time.LocalDate.parse(dateTobeConverted);
        ZonedDateTime zonedDateTimeOf = ZonedDateTime.of(dateLoc.getYear(), dateLoc.getMonthValue(), dateLoc.getDayOfMonth(), 0, 0, 0, 0, ZoneId.of("UTC"));

        return zonedDateTimeOf;

    }

    @Secured(AuthoritiesConstants.ADMIN)
    @PostMapping("/ministatement")
    @Timed
    public MResponse doOperationRetrait(@RequestBody MOperation mOperation) throws IOException, NoSuchAlgorithmException {
        log.debug("REST request to save Operation : {}", mOperation);
        return null;// mobileService.doOperationRetrait(mOperation);
    }

    @Secured(AuthoritiesConstants.ADMIN)
    @PostMapping("/infosclient")
    @Timed
    public MPersonne doSouscription(@RequestBody MClient client) {
        return null;
    }

    @Secured(AuthoritiesConstants.ADMIN)
    @GetMapping("/solde")
    @Timed
    public Message getSolde(
            @RequestParam("numeroCompte") String numeroCompte,
            @RequestParam("codeFse") String codeFse
    ) throws IOException, JAXBException {
        Compte cpte = compteService.findByCompteFseOptimise(numeroCompte, codeFse);

        Balance bal = new Balance();
        Message msg = new Message();
        if (cpte != null) {
            BigDecimal bgDec = BigDecimal.ZERO;

            BigDecimal somEve = evenementOperationRepository.findSommeEvenementOperation(cpte.getId(), TypeEvenementOperation.DOIT.name());
            //  List<EvenementOperation> listEve = evenementOperationRepository.findAllEvenementOperation(cpte);
            //  for (EvenementOperation eve : listEve) {
            bgDec = somEve;
            // }
            BigDecimal solde = cpte.getCompteAutre().getSolde().add(bgDec);
            msg.setMsg("ok");
            msg.setCodeErr("0");
            if (solde != null) {
                bal.setSolde(solde.doubleValue());

            } else {
                bal.setSolde(0D);
            }
        } else {
            log.debug("Compte inexistant");
            msg.setMsg("erreur compte inexistant!!");
            msg.setCodeErr("-1");
            bal.setSolde(0D);
        }

        String str = (String) Util.marshall(bal, String.class);
        msg.setText(str);

        return msg;

    }

    @PostMapping("/infoClientCompte")
    @Timed
    public Message getInfoClientCompte(
            @RequestParam("telephone") String telephone,
            @RequestParam("code") String code
    ) throws JAXBException {
        Message msg = new Message();
        CustomerInformation customer = new CustomerInformation();
        MClient client = mobileService.findInfosClient(telephone, code);
        if (client != null) {
            customer.setAgence(code);
            java.time.LocalDate dateNaissance = client.getPersonne().getDateNaissance();

            customer.setDateNaissance(
                    dateNaissance != null
                            ? dateNaissance.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                            : null
            );
            customer.setPaysResidence(client.getPersonne().getPaysResidence());
            customer.setNationalite(client.getPersonne().getCodeNationalite());
            List<com.pti.emeraude.gateway.domain.MParametre> listmParametres = new ArrayList<>();
            com.pti.emeraude.gateway.domain.MParametre mParametre = new com.pti.emeraude.gateway.domain.MParametre();
            mParametre.setCodeParametre("PAYSRESIDENCE");
            mParametre.setValeur(client.getPersonne().getPaysResidence());
            listmParametres.add(mParametre);
            mParametre = new com.pti.emeraude.gateway.domain.MParametre();
            mParametre.setCodeParametre("NATIONALITE");
            mParametre.setValeur(client.getPersonne().getCodeNationalite());
            listmParametres.add(mParametre);
            customer.setExtraParametres(listmParametres);
            /*
            PAYSRESIDENCE
            PAYSNAISSANCE
            VILLE
            NATIONALITE
             */
            customer.setLieuNaissance(client.getPersonne().getLieuNaissance());
            customer.setNom(client.getPersonne().getNom());
            customer.setPrenom(client.getPersonne().getPrenom());
            if (client.getDocIdentification() != null) {
                customer.setReference(client.getDocIdentification().getReference());
                customer.setPiece(client.getDocIdentification().getTypePiece().getCodeTypePiece());
                if (client.getDocIdentification().getDateDelivrance() != null) {
                    customer.setDateDelivre(client.getDocIdentification().getDateDelivrance().format(java.time.format.DateTimeFormatter.ISO_DATE));
                    customer.setDateExpire(client.getDocIdentification().getDateExpiration().format(java.time.format.DateTimeFormatter.ISO_DATE));
                }
            }

            if (client.getPersonne() != null && client.getPersonne().getSexe() != null) {
                customer.setSexe(client.getPersonne().getSexe().name());
            }
            customer.setAdresse(client.getPersonne().getAdresse());

            customer.setTypeClient("PHYSIQUE");
            customer.setAutoriteDelivre("GOV");
            customer.setCodeIndicatif(code);
            customer.setTelephone(telephone);
            customer.setEmail(client.getPersonne().getEmail());
            customer.setTitre("");

            List<CustomerAccount> lisacc = new ArrayList();
            CustomerAccount acc = new CustomerAccount();
            acc.setCodeAgence(code);
            acc.setTeneurCompte(client.getCompte().getTeneurCompte());
            acc.setNumeroCompte(client.getCompte().getNumeroCompte());
            acc.setTypeCompte("AUTRE");
            lisacc.add(acc);
            ListAccount listAccount = new ListAccount();
            customer.setListCompte(lisacc);

            msg.setMsg("ok");
            msg.setCodeErr("0");
        } else {
            msg.setMsg("client inexistant");
            msg.setCodeErr("-1");
        }

        String str = (String) Util.marshall(customer, String.class);
        msg.setText(str);

//            if (infoClient.getCodeClient()!=null){
//                 msg.setCodeErr("0");
//                 msg.setInfoClient(infoClient);
//            }else{
//                 msg.setCodeErr("-1");
//                 msg.setMsg("Erreur");
//            }
        return msg;
    }

    private Operation setOperation(Compte cptDebit, Compte cptCredit, String reference, String description, BigDecimal montant) {
        log.info("FORMATAGE DE L'OPERATION POUR COMPTABILISATION");
        StopWatch stopWatch = new StopWatch("Measure virement SETOPERATION");
        if (cptCredit.getEtiquette().equals(EtiquetteCompteCode.CPTETAXE.name())) {
            description = description + " TAXE";
        }
        if (cptCredit.getEtiquette().equals(EtiquetteCompteCode.CPTEATTENTECOMM.name())) {
            description = description + " COMMISSION";
        }
        String uuid = UUID.randomUUID().toString().toUpperCase();
        log.info("GENERATION DU UUID {}", uuid);
        Personne emeteur = null;
        Personne beneficiaire;
        Operation oper = new Operation();
        oper.setCodeExterne(reference);
        oper.setMotif(description);
        oper.setDateOperation(ZonedDateTime.now());
        oper.setCodeOperation(uuid);
        //   oper.setCanal(canal); canal à rechercher;
        oper.setStatut(StatutOperation.PAYE);
        oper.setMontantOp(montant);
        oper.setCompteSrc(cptDebit);
        oper.setCompteDest(cptCredit);
        oper.setDescription(description);
        /*
        if (cptCredit != null && cptCredit.getEtiquette().equals(EtiquetteCompteCode.CPTECLIENT.name()) && cptCredit.getCompteAutre() != null) {
            UniteOrganisation UoSource = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(cptCredit.getCompteAutre().getCodeFse());
            beneficiaire = cptCredit.getClient().getPersonne();
            //   String codePv  mobileService.getCodePointVente(cptCredit.getCompteAutre().getCodeFse());
            if (UoSource != null) {
                UniteOrganisation uoAcq = UoSource;
                if (cptCredit.getClient() != null && cptCredit.getClient().getPersonne() != null) {
                    if (cptDebit != null && cptDebit.getEtiquette().equals(EtiquetteCompteCode.CPTECLIENT.name()) && cptDebit.getCompteAutre() != null) {
                        emeteur = cptDebit.getClient().getPersonne();
                    }
                    mobileService.controleSeuil(oper, UoSource, uoAcq, emeteur, beneficiaire);
                }
            }
        }*/
        Operation operSave = operationRepository.save(oper);
        stopWatch.start("1. EXECUTE ECRITURE");
        transactionComptePtiService.ecritureComptex(cptDebit, cptCredit, montant, operSave);
        log.info("FIN FORMATAGE DE L'OPERATION POUR COMPTABILISATION " + reference);
        stopWatch.stop();
        System.out.println(stopWatch.prettyPrint());
        return operSave;
    }

    @PostMapping("/getmontant")
    @Timed
    public Message getMontant(@RequestParam("montant") String montant, @RequestParam("nombre") String nombre) throws JAXBException {
        ElementFacture elmfact = new ElementFacture();
        Long nbre = Long.parseLong(nombre);
        Double mt = Long.parseLong(montant) * 1D;
        Message msg = new Message();
        msg.setMsg("ok");
        msg.setCodeErr("0");
        Double total = nbre * mt * 1D;
        elmfact.setMontant(mt);
        elmfact.setNombre(nbre);
        elmfact.setTotal(total);
        String str = (String) Util.marshall(elmfact, String.class);
        msg.setText(str);
        return msg;
    }

    @PostMapping("/getregle")
    @Timed
    public Message getRegle(@RequestParam("reference") String reference, @RequestParam("montant") String montant) throws JAXBException {
        RegleFacturier reglFact = new RegleFacturier();
        FactureFournisseur fs = new FactureFournisseur();
        reglFact.setMessage("OK");
        reglFact.setAction("OK");
        fs.setReference(reference);
        fs.setMontant(montant);
        reglFact.setFacture(fs);
        String str = (String) Util.marshall(reglFact, String.class);
        Message msg = new Message();
        msg.setMsg("ok");
        msg.setCodeErr("0");
        msg.setText(str);
        return msg;
    }

    @PostMapping("/updatePaiement")
    @Timed
    public Message updateFacture(@RequestParam("reference") String reference, @RequestParam("transactionId") String transactionId) throws JAXBException {
        //String str = (String) Util.marshall(reglFact, String.class);
        Message msg = new Message();
        msg.setMsg("ok");
        msg.setCodeErr("0");
        msg.setText("");
        return msg;

    }

    @PostMapping("/getFacture")
    @Timed
    public Message getFacture(@RequestParam("reference") String reference) throws JAXBException {
        Message msg = new Message();
        List<Facture> listFact = factureRepository.findByReferenceFacature(reference);
        List<FactureFournisseur> listFactureFournisseur = new ArrayList();
        Iterator it = listFact.iterator();
        while (it.hasNext()) {
            Facture fact = (Facture) it.next();
            FactureFournisseur fs = new FactureFournisseur();
            fs.setReference(fact.getReferenceFacature());
            fs.setMontant(fact.getMontantFacture());
            fs.setDescription(fact.getReferenceFacature());
            listFactureFournisseur.add(fs);
        }
        CollectionFacture collfac = new CollectionFacture();
        collfac.setListeFacture(listFactureFournisseur);
        msg.setMsg("ok");
        msg.setCodeErr("0");
        String str = (String) Util.marshall(collfac, String.class);
        msg.setText(str);
        return msg;
    }

    @PostMapping("/getFactureSonabel")
    @Timed
    public Message getFactureSONABEL(@RequestParam("reference") String reference, @RequestParam("codeexpl") String codeexpl) throws JAXBException {
        Message msg = new Message();
        List<Facture> listFact = factureRepository.findByReferenceFacature(reference);
        List<FactureFournisseur> listFactureFournisseur = new ArrayList();
        Iterator it = listFact.iterator();
        while (it.hasNext()) {
            Facture fact = (Facture) it.next();
            FactureFournisseur fs = new FactureFournisseur();
            fs.setReference(fact.getReferenceFacature());
            fs.setMontant(fact.getMontantFacture());
            fs.setDescription(fact.getReferenceFacature());
            listFactureFournisseur.add(fs);
        }
        CollectionFacture collfac = new CollectionFacture();
        collfac.setListeFacture(listFactureFournisseur);
        msg.setMsg("ok");
        msg.setCodeErr("0");
        String str = (String) Util.marshall(collfac, String.class);
        msg.setText(str);
        return msg;
    }

    @PostMapping("/virements")
    @Timed
    /* @Caching(evict = {
        @CacheEvict(value = "SoldeCompte", key = "#compteDebiteur + '|' + #codeFseDebit", beforeInvocation = false)
        ,
    @CacheEvict(value = "SoldeCompte", key = "#compteCredit + '|' + #codeFseCredit", beforeInvocation = false)
    })*/
    public MessageRet doOperationTransfert(
            @RequestParam("compteDebiteur") String compteDebiteur,
            @RequestParam("compteCredit") String compteCredit,
            @RequestParam("codeFseCredit") String codeFseCredit,
            @RequestParam("codeFseDebit") String codeFseDebit,
            @RequestParam("reference") String reference,
            @RequestParam("description") String description,
            @RequestParam("montant") String montantDebit
    ) throws JAXBException {
        MessageRet msg = new MessageRet();
        String canal = "CORISMONEY";
        //         } catch (CustomParameterizedException e) {
        try {
            StopWatch stopWatch = new StopWatch("Measure virement");
            log.info("DEBUT COMPTABILISATION SUR LE GATEWAY " + reference);
            System.out.println("CPT...compteDebiteur: " + compteDebiteur);
            System.out.println("compteCredit...compteCredit: " + compteCredit);
            System.out.println("CODE...codeFseDebit: " + codeFseDebit);
            stopWatch.start("1. Recherche compt debit");
            log.info("DEBUT COMPTABILISATION SUR LE GATEWAY - RECHERCHE DU COMPTE DEBIT" + reference);
            Compte cDebit = compteService.findByCompteFseOptimise(compteDebiteur, codeFseDebit);
            cDebit = compteService.findCompteOne(cDebit.getId());
            stopWatch.stop();
            stopWatch.start("1. Recherche compt credit");
            log.info("DEBUT COMPTABILISATION SUR LE GATEWAY - RECHERCHE DU COMPTE CREDIT" + reference);
            Compte cCredit = compteService.findByCompteFseOptimise(compteCredit, codeFseCredit);
            cCredit = compteService.findCompteOne(cCredit.getId());
            stopWatch.stop();
            BigDecimal montantApres = null;

            if ((cDebit != null) && (cCredit != null)) {

                if (!cDebit.equals(cCredit)) {
                    stopWatch.start("1. Recherche compt etiquette");
                    EtiquetteCompte eti = etiquetteCompteRepository.findTop1ByCode(cDebit.getEtiquette());
                    stopWatch.stop();
                    if (eti != null) {
                        boolean debiteur;
                        if (eti.getEstDebiteur() != null) {
                            debiteur = eti.getEstDebiteur();
                        } else {
                            debiteur = false;
                        }
                        if (debiteur) {
                            montantApres = BigDecimal.ZERO;
                        } else {
                            montantApres = cDebit.getCompteAutre().getSolde().subtract(BigDecimal.valueOf(Double.parseDouble(montantDebit)));
                            log.debug("montantApres " + montantApres);
                        }
                    } else {
                        montantApres = cDebit.getCompteAutre().getSolde().subtract(BigDecimal.valueOf(Double.parseDouble(montantDebit)));
                        log.debug("montantApres000 " + montantApres);
                    }
                    if (montantApres.doubleValue() >= 0) {
                        stopWatch.start("1. Execute");
                        Operation op = setOperation(cDebit, cCredit, reference, description, BigDecimal.valueOf(Double.parseDouble(montantDebit)));
                        stopWatch.stop();
                        System.out.println(stopWatch.prettyPrint());
                        if (op != null) {
                            log.info("SUCCES DE LA COMPTABILISATION " + reference);
                            msg.setCodeErr("0");
                            msg.setMsg("Opération effectuée avec succès");
                            //  msg.setTransactionId(op.getCodeOperation());
                            return msg;
                        }
                    } else {

                        msg.setCodeErr("-1");
                        msg.setCodeErr("-1");
                        msg.setMsg("Solde insuffisant");
                        log.info("ERREUR DE LA COMPTABILISATION SUR LE GATEWAY " + msg.getMsg() + " " + reference);
                        return msg;
                    }
                } else {
                    msg.setCodeErr("-1");
                    msg.setMsg("le compte source et le compte de destination sont identiques");
                    log.info("ERREUR DE LA COMPTABILISATION SUR LE GATEWAY " + msg.getMsg() + " " + reference);
                    return msg;
                }
            } else {
                msg.setCodeErr("-1");
                msg.setMsg("Compte inexistant");
                log.info("ERREUR DE LA COMPTABILISATION SUR LE GATEWAY " + msg.getMsg() + " " + reference);
                return msg;
            }
            log.info("FIN DE LA COMPTABILISATION " + reference);
        } catch (CustomParameterizedException e) {
            msg.setCodeErr("-1");
            msg.setMsg(e.getMessage());
            log.info("ERREUR DE LA COMPTABILISATION  SUR LE GATEWAY" + msg.getMsg() + " " + reference);
            return msg;

        }

        msg.setCodeErr("-1");
        msg.setMsg("Une erreur s'est produite lors du transfert");
        log.info("ERREUR DE LA COMPTABILISATION  SUR LE GATEWAY" + msg.getMsg() + " " + reference);
        return msg;

//    return apiService.getSolde(numeroCompte, agence, codeFse);
    }

    @Secured(AuthoritiesConstants.ADMIN)
    @GetMapping("/opposition")
    @Timed
    public Message getOpposition(
            @RequestParam("numeroCompte") String numeroCompte,
            @RequestParam("codeFse") String codeFse,
            @RequestParam("montant") String montant,
            @RequestParam("sens") String sens,
            @RequestParam("isInit") String isInit, @RequestParam(name = "eventId", required = false) String eventId
    ) throws IOException, JAXBException {

        StopWatch stopWatch = new StopWatch("MESURE DE STOPPAGE ");
        log.info("ENTREE DE LA FONCTION GET STOPPAGE");
        log.debug("ENTREE DE LA FONCTION GET STOPPAGE");
        stopWatch.start("1. DEBUT");
        StoppageData stoppageData = new StoppageData();
        boolean stoppage = false;
        List<MParametre> listMParametre = new ArrayList();
        stoppageData.setCode("1");
        MParametre mParametre = new MParametre("0", "OK");
        Compte cpte = compteService.findByCompteFseOptimise(numeroCompte, codeFse);

        Message msg = new Message();
        BigDecimal mt = BigDecimal.valueOf(Double.parseDouble(montant));
        msg.setCodeErr("-1");
        stopWatch.stop();
        if (cpte != null) {
            cpte = compteService.findCompteOne(cpte.getId());
            if ("1".equals(isInit)) {
                stopWatch.start("1. DEBUT");
                log.info("ENTRE DANS INIT {}", numeroCompte);
                log.info("CONTROLE DU SEUIL   {}", numeroCompte);
                TableauCumulDTO tab;

                tab = controleSeuil(cpte, mt, sens);

                log.info("FIN CONTROLE DU SEUIL   {}", numeroCompte);
                if ((tab != null && tab.getAction() != null) && tab.getAction().equals(Action.BLOQUE)) {
                    log.info("IL Y A UN BLOCAGE DU FAIT DE LA LIMITRE   {}", numeroCompte);
                    log.info("IL Y A UN BLOCAGE DU FAIT DE LA LIMITRE   {}  {} ", numeroCompte, tab.getMessage());
                    listMParametre.add(new MParametre("-1", tab.getMessage()));
                    stoppage = true;
                }
            }

            if ("D".equals(sens)) {
                if ("1".equals(isInit)) {
                    BigDecimal somEve = evenementOperationRepository.findSommeEvenementOperation(cpte.getId(), TypeEvenementOperation.DOIT.name());

                    // }
                    BigDecimal soldeEnBigDecimal = cpte.getCompteAutre().getSolde();
                    BigDecimal solde = cpte.getCompteAutre().getSolde().add(somEve.abs());
                    BigDecimal montantApres = solde.subtract(mt);
                    if (montantApres.doubleValue() < 0) {
                        stoppage = true;
                        listMParametre.add(new MParametre("-1", "SOLDE INSUFFISANT"));
                        log.info("LE SOLDE DU COMPTE EST INSUFFISANT   {}", numeroCompte);
                    }

                    //}
                    msg.setMsg("ok");
                    if (stoppage) {
                        log.info("IL Y A UN STOPPAGE SUR LE COMPTE    {}", numeroCompte);
                        stoppageData.setCode("1");
                        msg.setMsg("nok");
                        msg.setCodeErr("-1");
                        stoppageData.setmParametre(listMParametre);
                    } else {
                        if (cpte != null) {
                            EvenementOperation evt = mobileService.insertEvenementOperation(mt, cpte);

                            if (evt != null) {
                                log.info("INSERTION DE L'EVENEMENT    {}", numeroCompte);
                                msg.setCodeErr("0");
                                soldeEnBigDecimal = cpte.getCompteAutre().getSolde().add(somEve);;
                                stoppageData.setAvailableBalance(soldeEnBigDecimal.subtract(mt).toString());
                                stoppageData.setIdEvent(evt.getId().toString());
                                log.info("INSERTION DE L'EVENEMENT    {}", evt.getId().toString());
                            }

                        }

                    }
                } else {
                    log.info("DESACTIVATION DE L'EVENEMENT    {}", numeroCompte);
                    EvenementOperation evt = mobileService.desactiveEvt(Long.parseLong(eventId));
                    if (evt != null) {
                        msg.setCodeErr("0");
                    } else {
                        log.info("L'EVENEMENT EST DEJA LEVE    {}", numeroCompte);
                        mParametre = new MParametre("-1", "EVENEMENT DEJA LEVE OU INEXISTANT");
                        listMParametre.add(mParametre);
                        stoppageData.setIdEvent("-1");
                        stoppageData.setmParametre(listMParametre);
                    }

                }
            }
        } else {
            log.info("COMPTE INEXISTANT   {}", numeroCompte);
            stoppage = true;
            mParametre = new MParametre("-1", "COMPTE INEXISTANT");
            listMParametre.add(mParametre);
            log.debug("Compte inexistant");
            msg.setMsg("erreur compte inexistant!!");
            stoppageData.setmParametre(listMParametre);
            msg.setCodeErr("-1");
        }
        if (stoppage) {
            log.info("STOPPAGE   {}", numeroCompte);
            stoppageData.setCode("1");
            msg.setCodeErr("-1");
            stoppageData.setmParametre(listMParametre);
        } else {
            log.info("PAS DE STOPPAGE   {}", numeroCompte);
            stoppageData.setCode("0");
            msg.setCodeErr("0");
        }

        String str = (String) Util.marshall(stoppageData, String.class);
        msg.setText(str);
        log.info("STRING DE RETOUR   {}", str);
        return msg;

    }

}
