package com.m2i.pti.mobile.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.hash.Hashing;
import com.google.gson.Gson;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.domain.util.CacheObject;
import com.m2i.pti.domain.util.CodeErreur;
import com.m2i.pti.mobile.dto.*;
import com.m2i.pti.mobile.enumeration.MSexe;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.mobile.enumeration.MTypeOrganisation;
import com.m2i.pti.mobile.dto.MPublicite;
import com.m2i.pti.reporting.service.EtatStatsService;
import com.m2i.pti.repository.*;
import com.m2i.pti.security.Chiffrement;
import com.m2i.pti.service.*;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.mapper.DocumentMapper;
import com.m2i.pti.service.mapper.OperationMapper;
import com.m2i.pti.service.mapper.UniteOrganisationMapper;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import com.m2i.pti.web.rest.errors.CustomSettlementException;
import com.m2i.pti.web.rest.vm.ManagedUserVM;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.m2i.pti.service.dto.ProduitDTO;
import com.m2i.pti.external.domain.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StopWatch;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.ModelAndView;
import com.m2i.pti.external.service.ErreurConstants;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.springframework.data.repository.query.Param;

/**
 * Nom de classe : MobileService
 *
 * Description :Cette classe definit l'ensemble des opérations effectuées dans
 * les applications web et mobile Elle definit l'ens des r_gle de gestion de la
 * monnaie Version : 1.0 Date : 23/02/2020
 */
/**
 * Service de traitement des Opï¿½rations effectuï¿½ ï¿½ partir d'une
 * application externe
 */
@Service
//@Transactional

public class MobileService {

    @Value("${m2i.security.pti.images.windows}")
    private String imagesPathWindows;

    @Value("${m2i.security.pti.images.linux}")
    private String imagesPathLinux;

    private final TableauFraisService tableauFraisService;
    private final TypeServiceService typeServiceService;
    private final CanalService canalService;
    private final OperationService operationService;
    private final PointVenteService pointVenteService;
    private final UserService userService;
    private final CompteBanqueService compteBanqueService;
    private final CompteRepository compteRepository;
    private final CompteService compteService;
    private final UniteOrganisationService uniteOrganisationService;
    private final ApiService apiService;
    MotifOperationService motifOperationService;
    private final PersonneService personneService;

    private final PersonneRepository personneRepository;
    private final PersonnePhysiqueRepository personnePhysiqueRepository;
    private final BeneficiaireRepository beneficiaireRepository;
    private final EmetteurRepository emetteurRepository;
    private final OperationRepository operationRepository;
    private final OperationAuditService operationAuditService;
    private final Logger log = LoggerFactory.getLogger(MobileService.class);
    private final TaxeOperationService taxeOperationService;
    private final BilanTaxeService bilanTaxeService;
    private final OperationMapper operationMapper;
    private final ClientService clientService;
    private final CompteBanqueRepository compteBanqueRepository;
    private final CompteAutreRepository compteAutreRepository;
    private final DocumentRepository documentRepository;
    private final ClientRepository clientRepository;
    private final PieceRepository pieceRepository;
    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    private final MotifOperationRepository motifOperationRepository;
    private final TypePieceRepository typePieceRepository;
    private final TypeDocumentRepository typeDocumentRepository;
    private final ProfilActeurRepository profilActeurRepository;
    private final AutorisationService autorisationService;
    private final ParametreAppRepository parametreAppRepository;
    ServiceProfilRepository serviceProfilRepository;
    private final TerminalRepository terminalRepository;
    private final ClassificationFacturierRepository classificationFacturierRepository;
    private final FacturierRepository facturierRepository;
    private final ProduitRepository produitRepository;
    private final EcranRepository ecranRepository;
    private final ChampsRepository champsRepository;
    private final UserRepository userRepository;
    private final TypeReclamationRepository typeReclamationRepository;
    private final TransactionComptePtiService transactionComptePtiService;
    RepartitionOpService repartitionOpService;
    private final TypeReclamationService typeReclamationService;
    private final ReclamationService reclamationService;
    private final FactureRepository factureRepository;
    private final NotificationRepository notificationRepository;
    GroupeProfilActeurRepository groupeProfilActeurRepository;
    private TaskExecutor taskExecutor;
    private final UserControlService userControlService;
    private final FaqRepository faqRepository;
    private final DocumentService documentService;
    private final DocumentMapper documentMapper;
    private final EvenementOperationRepository evenementOperationRepository;
    private final EtatStatsService etatStatsService;
    private final UniteOrganisationMapper uniteOrganisationMapper;
    OperationAuditEventService operationAuditEventService;
    private final ExternalOTPRepository externalOTPRepository;

    UoDeviseService uoDeviseService;
    DeviseService deviseService;
    TauxChangeService tauxChangeService;
    TypeUniteOrganisationService typeUniteOrganisationService;
    EvenementOperationService evenementOperationService;
    PassiveExpiringMap<String, List<MotifOperation>> listeMotif = new PassiveExpiringMap<>(6400000);
    PassiveExpiringMap<String, List<UniteOrganisation>> organisationMapDTO = new PassiveExpiringMap<>(6400000);
    PassiveExpiringMap<String, List<TypeDocument>> listeMapDTOTypeDocument = new PassiveExpiringMap<>(6400000);
    PassiveExpiringMap<String, List<TypePiece>> listPieceMapDTO = new PassiveExpiringMap<>(6400000);
    PassiveExpiringMap<String, List<ClassificationFacturier>> listClassificationMap = new PassiveExpiringMap<>(600000);
    PassiveExpiringMap<String, List<Facturier>> listFacturierMap = new PassiveExpiringMap<>(600000);
    PassiveExpiringMap<String, List<Champs>> listChampsMap = new PassiveExpiringMap<>(10000);
    PassiveExpiringMap<String, String> sendExpiredTime = new PassiveExpiringMap<>(60000);

    PassiveExpiringMap<String, List<MListeCategorieFse>> listeCategorieFseMapDTO = new PassiveExpiringMap<>(6400000);
    PassiveExpiringMap<String, List<MGroupeUniteOrganisation>> groupeFseMapDTO = new PassiveExpiringMap<>(6400000);
    PassiveExpiringMap<String, List<MListeReseauServiceFse>> listeMListeReseauServiceFseMapDTO = new PassiveExpiringMap<>(6400000);

        PassiveExpiringMap<String, Long> montantSeuil = new PassiveExpiringMap<>(6400000);
   static String lMontantSeuil = "MONTANT_SEUIL_PI";
    
    static String lgroupeFse = "GROUPEFSE";
    static String lserviceFse = "SERVICEPARFSE";
    GroupeFseRepository groupeFseRepository;
    CategorieServiceRepository categorieServiceRepository;

    TypeServiceFseRepository typeServiceFseRepository;
    private final ActiviteClientService activiteClientService;
    CarteRepository carteRepository;
    HazelcastConfigCacheService hazelcastConfigCacheService;
    UniteOrganisationCorrespondanceRepository uniteOrganisationCorrespondanceRepository;
    TypeServiceReseauOrganisationRepository typeServiceReseauOrganisationRepository;
    private final int DELAIS = 600;
    private PubliciteRepository publiciteRepository;

    public MobileService(PubliciteRepository publiciteRepository, ExternalOTPRepository externalOTPRepository, TypeServiceFseRepository typeServiceFseRepository, CategorieServiceRepository categorieServiceRepository, GroupeFseRepository groupeFseRepository, TypeServiceReseauOrganisationRepository typeServiceReseauOrganisationRepository, UniteOrganisationCorrespondanceRepository uniteOrganisationCorrespondanceRepository, ActiviteClientService activiteClientService, HazelcastConfigCacheService hazelcastConfigCacheService, CarteRepository carteRepository, OperationAuditEventService operationAuditEventService, EvenementOperationService evenementOperationService, TypeUniteOrganisationService typeUniteOrganisationService, MotifOperationService motifOperationService, PersonneService personneService, DocumentService documentService, DocumentMapper documentMapper, DeviseService deviseService, TauxChangeService tauxChangeService, UoDeviseService uoDeviseService, EvenementOperationRepository evenementOperationRepository, FaqRepository faqRepository, TaskExecutor taskExecutor, NotificationRepository notificationRepository, RepartitionOpService repartitionOpService, GroupeProfilActeurRepository groupeProfilActeurRepository, TableauFraisService tableauFraisService, FactureRepository factureRepository, ReclamationService reclamationService, TypeReclamationRepository typeReclamationRepository, TypeReclamationService typeReclamationService, AutorisationService autorisationService, ClassificationFacturierRepository classificationFacturierRepository, TerminalRepository terminalRepository, ServiceProfilRepository serviceProfilRepository, ProfilActeurRepository profilActeurRepository, ParametreAppRepository parametreAppRepository, DocumentRepository documentRepository, TypeDocumentRepository typeDocumentRepository, TypeServiceService typeServiceService, CanalService canalService, OperationService operationService, PointVenteService pointVenteService, UserService userService, CompteBanqueService compteBanqueService, CompteRepository compteRepository, CompteService compteService, UniteOrganisationService uniteOrganisationService, ApiService apiService, PersonneRepository personneRepository, PersonnePhysiqueRepository personnePhysiqueRepository, BeneficiaireRepository beneficiaireRepository, EmetteurRepository emetteurRepository, OperationRepository operationRepository, OperationAuditService operationAuditService, TaxeOperationService taxeOperationService, BilanTaxeService bilanTaxeService, OperationMapper operationMapper, ClientService clientService, CompteBanqueRepository compteBanqueRepository, CompteAutreRepository compteAutreRepository, ClientRepository clientRepository, PieceRepository pieceRepository, TypeUniteOrganisationRepository typeUniteOrganisationRepository, UniteOrganisationRepository uniteOrganisationRepository, MotifOperationRepository motifOperationRepository, TypePieceRepository typePieceRepository, FacturierRepository facturierRepository, ProduitRepository produitRepository, EcranRepository ecranRepository, ChampsRepository champsRepository, UserRepository userRepository, TransactionComptePtiService transactionComptePtiService, UserControlService userControlService, EtatStatsService etatStatsService, UniteOrganisationMapper uniteOrganisationMapper) {
        this.motifOperationService = motifOperationService;
        this.evenementOperationService = evenementOperationService;
        this.typeUniteOrganisationService = typeUniteOrganisationService;
        this.deviseService = deviseService;
        this.tauxChangeService = tauxChangeService;
        this.personneService = personneService;
        this.typeServiceService = typeServiceService;
        this.faqRepository = faqRepository;
        this.taskExecutor = taskExecutor;
        this.notificationRepository = notificationRepository;
        this.repartitionOpService = repartitionOpService;
        this.groupeProfilActeurRepository = groupeProfilActeurRepository;
        this.canalService = canalService;
        this.operationService = operationService;
        this.pointVenteService = pointVenteService;
        this.userService = userService;
        this.compteBanqueService = compteBanqueService;
        this.compteRepository = compteRepository;
        this.compteService = compteService;
        this.uniteOrganisationService = uniteOrganisationService;
        this.apiService = apiService;
        this.personneRepository = personneRepository;
        this.personnePhysiqueRepository = personnePhysiqueRepository;
        this.beneficiaireRepository = beneficiaireRepository;
        this.emetteurRepository = emetteurRepository;
        this.operationRepository = operationRepository;
        this.operationAuditService = operationAuditService;
        this.taxeOperationService = taxeOperationService;
        this.bilanTaxeService = bilanTaxeService;
        this.operationMapper = operationMapper;
        this.clientService = clientService;
        this.compteBanqueRepository = compteBanqueRepository;
        this.compteAutreRepository = compteAutreRepository;
        this.clientRepository = clientRepository;
        this.pieceRepository = pieceRepository;
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.motifOperationRepository = motifOperationRepository;
        this.typePieceRepository = typePieceRepository;
        this.documentRepository = documentRepository;
        this.typeDocumentRepository = typeDocumentRepository;
        this.parametreAppRepository = parametreAppRepository;
        this.profilActeurRepository = profilActeurRepository;
        this.serviceProfilRepository = serviceProfilRepository;
        this.terminalRepository = terminalRepository;
        this.autorisationService = autorisationService;
        this.classificationFacturierRepository = classificationFacturierRepository;
        this.facturierRepository = facturierRepository;
        this.produitRepository = produitRepository;
        this.ecranRepository = ecranRepository;
        this.champsRepository = champsRepository;
        this.userRepository = userRepository;
        this.transactionComptePtiService = transactionComptePtiService;
        this.typeReclamationService = typeReclamationService;
        this.typeReclamationRepository = typeReclamationRepository;
        this.reclamationService = reclamationService;
        this.factureRepository = factureRepository;
        this.tableauFraisService = tableauFraisService;
        this.userControlService = userControlService;
        this.uniteOrganisationCorrespondanceRepository = uniteOrganisationCorrespondanceRepository;
        this.uoDeviseService = uoDeviseService;
        this.documentService = documentService;
        this.documentMapper = documentMapper;
        this.evenementOperationRepository = evenementOperationRepository;

        this.etatStatsService = etatStatsService;
        this.uniteOrganisationMapper = uniteOrganisationMapper;
        this.operationAuditEventService = operationAuditEventService;
        this.carteRepository = carteRepository;
        this.hazelcastConfigCacheService = hazelcastConfigCacheService;
        this.activiteClientService = activiteClientService;
        this.typeServiceReseauOrganisationRepository = typeServiceReseauOrganisationRepository;
        this.groupeFseRepository = groupeFseRepository;
        this.categorieServiceRepository = categorieServiceRepository;
        this.typeServiceFseRepository = typeServiceFseRepository;
        this.externalOTPRepository = externalOTPRepository;
        this.publiciteRepository = publiciteRepository;
    }

    /**
     * Service fournissant tous les motifs des opï¿½rations
     *
     * @return
     */
    public List<MEcran> getEcransOfProduit(Long id) {
        List<Ecran> l = ecranRepository.findEcransByProduitIdOrderByNumeroOrdreAsc(id);
        int longueur = l.size();
        Iterator it;
        int i;
        List<MEcran> retour = new ArrayList<MEcran>();
        for (i = 0; i < longueur; i++) {
            MEcran mecran = new MEcran();
            mecran.setId(l.get(i).getId());
            mecran.setNumeroOrdre(l.get(i).getNumeroOrdre());
            mecran.setProduit(l.get(i).getProduit().getNomProduit());
            mecran.setProduiId(l.get(i).getProduit().getId().intValue());
            List<Champs> listChamps = champsRepository.findAllByEcranId(l.get(i).getId());
            List<MChamps> listmChamps = new ArrayList();
            it = listChamps.iterator();
            while (it.hasNext()) {
                Champs ch = (Champs) it.next();
                MChamps mch = new MChamps();
                mch.setId(ch.getId());
                if (ch.getComposant().getBouton() != null) {
                    mch.setAction(ch.getComposant().getBouton().getAction());
                }
                if (ch.getComposant().getListe() != null) {
                    mch.setChoixmultiple(0);
                    mch.setOptions(ch.getComposant().getListe().getOptions());
                    if (mch.getOptions() != null) {
                        mch.setOptions(mch.getOptions().trim());
                    }
                    if (ch.getComposant().getListe().isMultiple() != null) {
                        mch.setChoixmultiple((ch.getComposant().getListe().isMultiple() == true) ? 1 : 0);

                    }
                    if (ch.getComposant().getListe().getEstStatic() != null) {

                        if (ch.getComposant().getListe().getEstStatic() == false) {
                            mch.setIsStatic(0);
                            if (ch.getSelectComponentData() != null) {
                                mch.setListeDataToGet(ch.getSelectComponentData().getData());
                            }
                        } else {
                            mch.setIsStatic(1);
                        }

                    }
                }
                mch.setCode(ch.getComposant().getCode());
                mch.setIsVisible(ch.getIsVisible());
                if (ch.getLabel() != null) {
                    mch.setLabel(ch.getLabel());
                } else {
                    mch.setLabel(ch.getComposant().getLabel());
                }
                mch.setMax(ch.getMax());
                mch.setMin(ch.getMin());
                mch.setMaxLength(ch.getMaxLength());
                mch.setMinLength(ch.getMinLength());
                mch.setPattern(ch.getPattern());
                mch.setType(ch.getComposant().getType());
                mch.setTypeComposant(ch.getComposant().getTypeComposant().name());
                mch.setaSaisir(ch.getaSaisir());
                if (ch.getObligatoire() != null) {
                    mch.setObligatoire(ch.getObligatoire());
                } else {
                    mch.setObligatoire(0);
                }
                mch.setDefaultValue(ch.getDefaultValue());
                listmChamps.add(mch);
            }

            mecran.setChamps(listmChamps);

            retour.add(mecran);
        }
        return retour;
    }

    public List<MPublicite> getPublicite(String codeindicatif) {
        List<MPublicite> list = new ArrayList();
        List<Publicite> listPublicite = publiciteRepository.findByCodeIndicatif(codeindicatif);
        listPublicite.forEach(pub -> {
            list.add(new MPublicite(pub.getId(), pub.getFrequence(), pub.getImageData(), pub.getContentType()));
        });
        return list;
    }

    /**
     * list des notifications
     *
     * @return
     */
    public List<MNotification> getNotification(String numero, String codeindicatif) {

        String num = codeindicatif + numero;
        List<MNotification> listMNotification = new ArrayList();
        List<Notification> listNotification = notificationRepository.findByDestinataireOrderByDateHeureEnvoieDesc(num);
        listNotification.forEach(n
                -> {
            if (n.getOperationId() != null) {
                String extraData = operationService.getExtraData(n.getOperationId());
                if (extraData != null) {
                    MlistParametre mlistParametre;
                    try {
                        mlistParametre = (MlistParametre) Util.unmarshall(extraData, MlistParametre.class);
                        listMNotification.add(new MNotification(n.getId(), n.getDateHeureEnvoie(), n.getContenu(), n.getDestinataire(), n.getCodeIndicatif(), n.getTypeNotification().name(), n.getOperationId(), mlistParametre.getParametre()));
                    } catch (JAXBException ex) {

                    }

                }

            } else {
                listMNotification.add(new MNotification(n.getId(), n.getDateHeureEnvoie(), n.getContenu(), n.getDestinataire(), n.getCodeIndicatif(), n.getTypeNotification().name()));
            }

        });
        return listMNotification;
    }

    /**
     * list des produits des facturier
     *
     * @return
     */
    public List<MProduit> getPoduitsOfFacturier(Long id) {
        List<Produit> l = produitRepository.findProduitByFacturierIdOrderByNumeroOrdreAsc(id);
        int longueur = l.size();
        int i;
        List<MProduit> retour = new ArrayList<MProduit>();
        for (i = 0; i < longueur; i++) {
            retour.add(new MProduit(l.get(i)));
        }
        return retour;
    }

    /**
     * retourne le facturier
     *
     * @return
     */
    public List<MProduit> getPoduitsOfFacturierFromUo(String codeUo) {
        Facturier facturier = facturierRepository.findByCodeUo(codeUo);
        if (facturier != null) {
            List<Produit> l = produitRepository.findProduitByFacturierIdOrderByNumeroOrdreAsc(facturier.getId());
            int longueur = l.size();
            int i;
            List<MProduit> retour = new ArrayList<MProduit>();
            for (i = 0; i < longueur; i++) {
                retour.add(new MProduit(l.get(i)));
            }
            return retour;
        }
        return null;

    }

    public List<MFacturier> getFacturierFromUo(String codeUo) {
        List<Facturier> listfacturier = facturierRepository.findListeFacturierByCodeUo(codeUo);
        int longueur = listfacturier.size();
        int i;
        List<MFacturier> retour = new ArrayList<MFacturier>();
        for (i = 0; i < longueur; i++) {
            MFacturier mfacturier = new MFacturier();
            mfacturier.setId(listfacturier.get(i).getId());
            if (listfacturier.get(i).getUniteOrganisation() != null) {
                mfacturier.setCodeUo(listfacturier.get(i).getUniteOrganisation().getCodeUO());
            }

            mfacturier.setCodeFacturier(listfacturier.get(i).getCodeFacturier());
            mfacturier.setNomFacturier(listfacturier.get(i).getNomFacturier());
            retour.add(mfacturier);
        }
        return retour;

    }

    public List<MfactureMarchand> getFactureMarchand(Long id) throws JAXBException {
        int i;
        //List<Facture> listfacture = factureRepository.findByProduitId(id);
        List<FactureDTO> listfacture = factureRepository.findFactureByProduitIdOptimise(id);
        int longueur = listfacture.size();
        List<MfactureMarchand> listfactureMarchand = new ArrayList<MfactureMarchand>();
        for (i = 0; i < longueur; i++) {
            MfactureMarchand mfactureMarchand = new MfactureMarchand();
            mfactureMarchand.setReferencePaiement(listfacture.get(i).getReferencePaiement());
            mfactureMarchand.setMontantFacture(listfacture.get(i).getMontantPaiement());
            mfactureMarchand.setReferenceFacture(listfacture.get(i).getReferenceFacature());
            mfactureMarchand.setDatePaiement(listfacture.get(i).getDatePaiement());
            String extradata = listfacture.get(i).getExtraData();
            String msg = "";
            MlistParametre mlistParametre = (MlistParametre) Util.unmarshall(extradata, MlistParametre.class);
            List<MParametre> mParametresRes = mlistParametre.getParametre();
            if (mParametresRes != null) {
                for (MParametre param : mParametresRes) {

                    msg = msg + param.getCodeParametre() + " : " + param.getValeur() + ",";

                }
                mfactureMarchand.setDonneExterne(msg);
            }
            listfactureMarchand.add(mfactureMarchand);
        }

        return listfactureMarchand;

    }

    public List<MParametre> getParametreFacture(String referencePaiement) throws JAXBException {
        Facture facture = factureRepository.findByReferencePaiement(referencePaiement);
        if (facture != null) {
            MlistParametre mlistParametre = (MlistParametre) Util.unmarshall(facture.getExtraData(), MlistParametre.class);
            List<MParametre> mParametresRes = mlistParametre.getParametre();
            return mParametresRes;
        }
        return null;
    }

    /**
     * list des faq
     *
     * @return
     */
    public List<MFaq> getFaq() {
        List<Faq> l = faqRepository.findAll();
        int longueur = l.size();
        int i;
        List<MFaq> retour = new ArrayList<MFaq>();
        for (i = 0; i < longueur; i++) {
            MFaq mFaq = new MFaq();
            mFaq.setQuestion(l.get(i).getQuestion());
            mFaq.setReponse(l.get(i).getReponse());
            retour.add(mFaq);
        }
        return retour;
    }

    /**
     * list des classification
     *
     * @return
     */
    public List<MFacturier> getFacturierOfClassification(Long id) {
        List<Facturier> l = facturierRepository.findAllByClassificationAndDiscarteTelephonie(id);
        int longueur = l.size();
        int i;
        List<MFacturier> retour = new ArrayList<MFacturier>();
        for (i = 0; i < longueur; i++) {
            MFacturier mf = new MFacturier();
            mf.setCodeFacturier(l.get(i).getCodeFacturier());
            mf.setId(l.get(i).getId());
            mf.setCodeUo(l.get(i).getUniteOrganisation().getCodeUO());
            mf.setNomFacturier(l.get(i).getNomFacturier());
            mf.setImage(l.get(i).getValeur());
            mf.setValeurContentType(l.get(i).getValeurContentType());
            retour.add(mf);
        }
        return retour;
    }

    public List<MFacturier> getFacturierOfClassificationAndCanal(Long id, String canal) {
        Canal cnl = canalService.findByCodeCanal(canal);
        List<MFacturier> retour = new ArrayList<MFacturier>();

        if (cnl != null) {
            List<Facturier> l;
            if (listFacturierMap.containsKey(id.toString() + canal) && listFacturierMap.get(id.toString() + canal) != null) {
                log.info("LE MOTIF EXIST DANS LA MAP");
                l = listFacturierMap.get(id.toString() + canal);
            } else {

                l = facturierRepository.findAllByClassificationAndDiscarteTelephonie(id, cnl.getId());
                if (l != null) {
                    listFacturierMap.put(id.toString() + canal, l);
                }

            }

            int longueur = l.size();
            int i;
            // List<MFacturier> retour = new ArrayList<MFacturier>();
            for (i = 0; i < longueur; i++) {
                MFacturier mf = new MFacturier();
                mf.setCodeFacturier(l.get(i).getCodeFacturier());
                mf.setId(l.get(i).getId());
                mf.setCodeUo(l.get(i).getUniteOrganisation().getCodeUO());
                mf.setNomFacturier(l.get(i).getNomFacturier());
                mf.setImage(l.get(i).getValeur());
                mf.setValeurContentType(l.get(i).getValeurContentType());
                retour.add(mf);
            }
        }

        return retour;
    }

    /**
     * list des classification
     *
     * @return
     */
    public List<MClassificationFacturier> getClassificationFacturierOfUo(String code) {
        List<MClassificationFacturier> retour = null;
        log.info("GET CLASSIFICATION");
        UniteOrganisation uo = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(code);
        if (uo != null) {
            List<ClassificationFacturier> lc;
            List<MotifOperation> motifs;//; = motifOperationRepository.findAll();
            if (listClassificationMap.containsKey(code) && listClassificationMap.get(code) != null) {
                log.info("LE MOTIF EXIST DANS LA MAP");
                lc = listClassificationMap.get(code);
            } else {
                lc = classificationFacturierRepository.findClassificationOfUo(uo.getId());
                if (lc != null) {
                    listClassificationMap.put(code, lc);
                }

            }
            int longueur = lc.size();
            int i;
            retour = new ArrayList<MClassificationFacturier>();
            for (i = 0; i < longueur; i++) {

                MClassificationFacturier m = new MClassificationFacturier();
                m.setId(lc.get(i).getId());
                m.setLibelleClassification(lc.get(i).getLibelleClassification());
                if (lc.get(i).getNiveauSuperieur() != null) {
                    m.setNiveauSuperieurId(lc.get(i).getNiveauSuperieur().getId());
                }
                retour.add(m);
            }

        }
        return retour;
    }

    /**
     * list des type de reclamation
     *
     * @return
     */
    public List<MTypeReclamation> getTypeReclamation() {
        List<TypeReclamation> typeRe = typeReclamationService.findAll();
        List<MTypeReclamation> mTypeReclamation = new ArrayList<>();
        typeRe.forEach(r -> {
            mTypeReclamation.add(new MTypeReclamation().id(r.getId()).libelleTypeReclamation(r.getLibelleTypeReclamation()));
        });
        return mTypeReclamation;
    }

    /**
     * list des motifs
     *
     * @return
     */
    public List<MMotif> getMotifs() {
        List<MotifOperation> motifs;//; = motifOperationRepository.findAll();
        if (listeMotif.containsKey("MOTIF") && listeMotif.get("MOTIF") != null) {
            log.info("LE MOTIF EXIST DANS LA MAP");
            motifs = listeMotif.get("MOTIF");
        } else {
            log.info("SELECTION DANS LA BASE");
            motifs = motifOperationRepository.findAll();
            if (motifs != null) {
                listeMotif.put("MOTIF", motifs);
            }

        }

        List<MMotif> mMotifs = new ArrayList<>();
        motifs.forEach(m -> {
            mMotifs.add(new MMotif().code(m.getCode()).libelle(m.getLibelle()));
        });
        return mMotifs;
    }

    /**
     * list des type de documents
     *
     * @return
     */
    public List<MtypeDoc> getTypeDocument() {
        List<TypeDocument> listetypedocument;
        if (listeMapDTOTypeDocument.containsKey("TYPEDOCUMENT")) {
            listetypedocument = listeMapDTOTypeDocument.get("TYPEDOCUMENT");
        } else {
            listetypedocument = typeDocumentRepository.findAll();
            if (listetypedocument != null) {
                listeMapDTOTypeDocument.put("TYPEDOCUMENT", listetypedocument);
            }

        }
        List<MtypeDoc> mtypeDoc = new ArrayList<>();
        listetypedocument.forEach(m
                -> {
            MtypeDoc mdoc = new MtypeDoc();
            mdoc.setCode(m.getCode());
            mdoc.setLibelle(m.getLibelle());
            mtypeDoc.add(mdoc);
        });
        return mtypeDoc;
    }

    public List<MListeCategorieFse> getServiceCategorie(List<MOrganisation> listOrganisation) {
        List<MListeCategorieFse> listeMListeCategorieFse = new ArrayList<>();
        List<CategorieService> listeCategorieService = categorieServiceRepository.findAll();
        if (listeCategorieFseMapDTO.containsKey(lserviceFse) && listeCategorieFseMapDTO.get(lserviceFse) != null) {
            listeMListeCategorieFse = listeCategorieFseMapDTO.get(lserviceFse);
        } else {

            MListeCategorieFse mListeCategorieFse = new MListeCategorieFse();

            List<MCategorieFse> listCategorie = new ArrayList<>();
            listeCategorieService.forEach(l -> {
                MCategorieFse mCategorieFse = new MCategorieFse();
                mCategorieFse.setCode(l.getCode());
                mCategorieFse.setLibelle(l.getLibelle());
                List<TypeServiceDTO> listTypeService = typeServiceFseRepository.findByCategorieService(l.getId());
                List<MListTypeServiceFse> listmListTypeServiceFse = new ArrayList<>();

                listTypeService.forEach(lts -> {

                    List<MtypeService> listTypeServiceFse = typeServiceFseRepository.findAllUoByTypeService(lts.getCode());
                    MListTypeServiceFse mListTypeServiceFse = new MListTypeServiceFse();

                    mListTypeServiceFse.setCodeService(lts.getCode());
                    mListTypeServiceFse.setLibeleService(lts.getDescription());
                    mListTypeServiceFse.setTypeServiceFse(listTypeServiceFse);
                    listmListTypeServiceFse.add(mListTypeServiceFse);

                });

                mCategorieFse.setListTypeService(listmListTypeServiceFse);
                listCategorie.add(mCategorieFse);

            });
            mListeCategorieFse.setListCategorie(listCategorie);
            listeMListeCategorieFse.add(mListeCategorieFse);

            listeCategorieFseMapDTO.put(lserviceFse, listeMListeCategorieFse);
        }

        return listeMListeCategorieFse;
    }

    /**
     * list des type de piece
     *
     * @param codePays
     * @return
     */
    public List<MTypePiece> getTypePiece(String codePays) {
        List<TypePiece> listeTypePiece;
        if (listPieceMapDTO.containsKey(codePays) && listPieceMapDTO.get(codePays) != null) {
            listeTypePiece = listPieceMapDTO.get(codePays);
        } else {
            listeTypePiece = typePieceRepository.findAll();
            listPieceMapDTO.put(codePays, listeTypePiece);
        }

        List<MTypePiece> mTypePiece = new ArrayList<>();
        listeTypePiece.forEach(m
                -> {
            ParametreApp tpiec = parametreAppRepository.findByTypeAndNomAndCodeFse(m.getEtiquette(), codePays);

            MTypePiece mtype = new MTypePiece();
            if (tpiec != null) {
                mtype.setExpireTime(tpiec.getValeur());
            } else {
                mtype.setExpireTime("0");
            }

            mtype.setCodeTypePiece(m.getCodeTypePiece());
            mtype.setLibelleTypePiece(m.getLibelleTypePiece());
            mTypePiece.add(mtype);
        });
        return mTypePiece;
    }

    /**
     * list des type de documents
     *
     * @return
     */
    public List<MTypePiece> getTypePiece() {
        List<TypePiece> listeTypePiece = typePieceRepository.findAll();
        List<MTypePiece> mTypePiece = new ArrayList<>();
        listeTypePiece.forEach(m
                -> {

            MTypePiece mtype = new MTypePiece();

            mtype.setCodeTypePiece(m.getCodeTypePiece());
            mtype.setLibelleTypePiece(m.getLibelleTypePiece());
            mTypePiece.add(mtype);
        });
        return mTypePiece;
    }

    /**
     * retourne un type UO à partir du code
     *
     * @return
     */
    public UniteOrganisation findUoByCode(String codeAgent) {
        UniteOrganisation agent = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(codeAgent, false);//Fse Dest
        return agent;
    }

    public boolean getIfMonnaieDef(String codeFse) {
        ParametreApp isMoney = parametreAppRepository.findByTypeAndNomAndCodeFse("EST_MONNAIE_ELECTRONIQUE", codeFse);
        if (isMoney != null) {
            return isMoney.getValeur().equals("1") ? true : false;
        }
        return true;
    }

    /**
     * retourne un type UO à partir du code
     *
     * @Return
     */
    public List<MAutorisation> findAutorisationUoByCode(String codeUo) {
        List<Autorisation> listeAutorisation = operationService.getListAutorisation(codeUo);//Fse Dest
        List<MAutorisation> mListeMAutorisation = new ArrayList<>();

        Iterator it = listeAutorisation.iterator();
        while (it.hasNext()) {
            Autorisation aut = (Autorisation) it.next();
            MAutorisation mautorisation = new MAutorisation();
            mautorisation.setService(aut.getTypeService().getCode());
            mListeMAutorisation.add(mautorisation);
        }

        return mListeMAutorisation;
    }

    /**
     * retourne un type UO à partir du code
     *
     * @param codeFse
     * @return
     */
    public UniteOrganisation findUoBycodeFSE(String codeFse) {
        UniteOrganisation uo = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(codeFse);//Fse Dest
        return uo;
    }

    /**
     * retourne un type UO à partir du code
     *
     * @param codePays
     * @return *
     */
    public MPointVente getCodeGabUo(String codePays) {
        String fseDef = getFournisseurMonnaieDef(codePays);
        MPointVente pv = new MPointVente();

        if (fseDef != null) {
            ParametreApp gab = parametreAppRepository.findByTypeAndNomAndCodeFse("AGENT_GAB", fseDef);
            //  pointVenteService.

            //  return gab.getValeur();
            Optional<PointVente> pointv = pointVenteService.findByCode(gab.getValeur());
            if (pointv.isPresent()) {
                pv.setCodePv(pointv.get().getCodePointVente());
                pv.setLibellePv(pointv.get().getLibellePointVente());
            }
        }
        return pv;
    }

    /**
     * retourne un type UO à partir du code
     *
     * @Return
     */
    public LocalDate convertStringToLocal(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        return localDate;
    }

    /**
     * Service d'envoi d'SMS
     *
     * @param numero
     * @param message
     * @return
     */
    public MResponse sendSms(String numero, String message) {
        Message msg = null;
        log.info("Appel Service d'sms vers ........................" + numero + " " + message);
        if (numero != null || numero.length() > 0 || !numero.isEmpty()) {
            try {
                msg = apiService.doEnvoiMsg(numero, message);
            } catch (Exception e) {
                log.info("Error", e.getMessage());
            }
        }
        if (msg != null) {
            if (msg.getCodeErr().equals("0")) {
                return new MResponse("0", "");
            } else {
                return new MResponse("-1", msg.getMsg());
            }
        } else {
            return new MResponse("-1", "Erreur");
        }

    }

    /**
     * service de suspension d'une opération
     *
     * @Return
     */
    public MResponse doSuspension(String codeOperation) {
        log.info("blocage de la transaction {}", codeOperation);
        OperationDTO operationDTO = operationService.findByCodeOperation(codeOperation);
        if (operationDTO != null) {
            OperationDTO operationRes = operationService.miseJourOperation(operationDTO);
            if (operationRes != null) {
                return new MResponse("0", "Transaction a ete bloquee");
            } else {
                return new MResponse("1", "Erreur");
            }
        } else {
            return new MResponse("1", "opération introuvable");
        }
    }

    /**
     *
     * @param login
     * @param codePv
     * @param codeActeur
     * @return
     */
    @Transactional
    public MResponse affecterUtilisateurPv(String login, String codePv, String codeActeur) {
        log.info(" debut Affectation du point de vente a l'utilisateur {}", login);
        UniteOrganisation uo = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(codeActeur, false);
        User user = userService.findeByLogin(login.toLowerCase());
        // Optional<PointVente> pv = pointVenteService.findByCode(codePv);

        PointVente opt = pointVenteService.findPointByCode(codePv);

        Optional<PointVente> pv = Optional.of(opt);

        if (user != null) {
            if (user.getUniteOrganisation().getId().equals(uo.getId())) {
                if (pv.isPresent() && pv.get().getUniteOrganisation().getId().equals(uo.getId())) {
                    /* controler si un utilisateur est dï¿½jï¿½ sur un point de vente */
                    User userAff = userService.findByPointVente(pv.get());

                    if (userAff != null) {
                        userAff.setPointVente(null);
                        User ua = userRepository.save(userAff);
                    }
                    user.setPointVente(pv.get());
                    User u = userRepository.save(user);
                    if (u != null) {
                        log.info(" fin affectation du point de vente a l'utilisateur {}", login);
                        return new MResponse("0", "Affectation effectuee avec succees");
                    }

                } else {
                    log.error("Point de vente incorrect {}", codePv);
                    return new MResponse("-1", "Point de vente incorrect");
                }

            } else {
                log.error("l'organisation de l'utilisateur est incorrect {}", codeActeur);
                return new MResponse("-1", "l'organisation de l'utilisateur est incorrect");
            }
        }

        return new MResponse("-1", "Erreur au cours de l'enregistrement ");

    }

    /**
     *
     * @param codeActeur
     * @param codePv
     * @return
     */
    public MResponse verifPointVente(String codeActeur, String codePv) {
        log.info(" verification du point de vente {}", codePv);
        UniteOrganisation uo = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(codeActeur, false);
        //     Optional<PointVente> pv = pointVenteService.findByCode(codePv);
        PointVente opt = pointVenteService.findPointByCode(codePv);

        Optional<PointVente> pv = Optional.of(opt);

        if (uo != null) {
            if (pv.isPresent()) {
                User user = userService.findByPointVente(pv.get());
                if (user != null) {
                    log.info("l'utilisateur " + user.getLastName() + " est deja affecte a ce point de vente");
                    return new MResponse("1", "l'utilisateur " + user.getLastName() + " est deja affecte a ce point de vente");
                } else {
                    log.info(" verification du point de vente terminï¿½ {}", codePv);
                    return new MResponse("0", "OK");
                }
            } else {
                log.error("ce point de vente n'existe pas {}", codePv);
                return new MResponse("-1", "ce point de vente n'existe pas");
            }
        } else {
            log.error("Erreur lors de l'affectation de l'utilisateur");
            return new MResponse("-1", "Erreur lors de l'affectation de l'utilisateur");
        }

    }

    public MResponse annulationOperation(String codeOperation) throws IOException, JAXBException {
        MResponse rep = operationService.annulationOperation(codeOperation);
        return rep;

        //
    }

    public Operation getOperationFromCode(String codeOperation) throws IOException, JAXBException {
        Operation operation = operationRepository.findByCodeOperation(codeOperation).orElse(null);
        return operation;
    }

    public MResponse envoiOperationMessage(Operation operation) {

        if (sendExpiredTime.containsKey(operation.getCodeOperation()) && sendExpiredTime.get(operation.getCodeOperation()) != null) {
            return new MResponse("-1", "Votre précédente demande est en cours de traitement", null);
        } else {

            sendExpiredTime.put(operation.getCodeOperation(), "1");
            List<MParametre> extraData = populateMessage(operation);
            envoiDesMessage(operation, extraData);
        }
        return new MResponse("1", "Opération effectuée...", null);
    }

    public MResponseOperation doannulationOperation(String codeOperation, String codePv) throws IOException, JAXBException {
        MResponseOperation rep = operationService.doannulationOperation(codeOperation, codePv);
        return rep;

        //
    }

    /**
     * Service permettant d'effectuer des opï¿½rations de transfert entre compte
     *
     * @param codeActeur
     * @return
     */
    public List<MPointVente> getListePointVente(String codeActeur) {
        UniteOrganisation uo = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(codeActeur, false);
        if (uo != null) {
            List<PointVenteDTO> listPv = pointVenteService.findByUO(uo.getId());
            Iterator it = listPv.iterator();
            List<MPointVente> listmPointVente = new ArrayList();
            while (it.hasNext()) {
                PointVenteDTO pvDto = (PointVenteDTO) it.next();
                MPointVente mPointVente = new MPointVente();
                mPointVente.setCodePv(pvDto.getCodePointVente());
                mPointVente.setLibellePv(pvDto.getLibellePointVente());
                PointVente pv = new PointVente();
                pv.setId(pvDto.getId());
                User user = userService.findByPointVente(pv);
                if (user != null) {
                    MUser muser = new MUser();
                    muser.setLogin(user.getLogin());
                    muser.setFirstName(user.getLastName());
                    muser.setLastName(user.getFirstName());
                    muser.setPhoneNumber(user.getPhoneNumber());
                    mPointVente.setUser(muser);
                }
                listmPointVente.add(mPointVente);
            }
            return listmPointVente;
        }
        return null;
    }

    /**
     * Operation en attente
     *
     * @param valeurCompte
     * @param typeCompte
     * @param codeAgence
     * @param codeFseTeneur
     * @param typeServ
     * @return
     */
    public List<MTransaction> getOpertationEntente(String valeurCompte, String typeCompte, String codeAgence, String codeFseTeneur, String typeServ) {
        List<MTransaction> listTransaction = new ArrayList();
        MTransaction mtrans;
        List<Operation> listOperation = null;
        //      findByCompteSourceAndEtat(Long typeServiceId,Compte compteSrc,StatutOperation statutOperation)
        TypeService typeService = typeServiceService.findByCodeService(typeServ);
        Compte cpt = getCompte(valeurCompte, typeCompte, codeAgence, codeFseTeneur);
        if (cpt != null) {
            listOperation = operationService.findByCompteSourceAndEtat(typeService.getId(), cpt, StatutOperation.ENATTENTE);
            Iterator it = listOperation.iterator();
            while (it.hasNext()) {
                Operation op = (Operation) it.next();
                mtrans = new MTransaction();

                if (op.getBeneficaire().getPersonne().getPersonnePhysique() != null) {
                    mtrans.setBeneficiaire(op.getBeneficaire().getPersonne().getPersonnePhysique().getNomClient() + " " + op.getBeneficaire().getPersonne().getPersonnePhysique().getPrenomClient());
                }
                if (op.getBeneficaire().getPersonne().getPersonneMorale() != null) {
                    mtrans.setBeneficiaire(op.getBeneficaire().getPersonne().getPersonneMorale().getRaisonSociale());
                }
                mtrans.setId(op.getId());
                mtrans.setDateOperation(op.getDateOperation());
                mtrans.setMontantFrais(op.getMontantFrais());
                mtrans.setCodeOperation(op.getCodeOperation());
                mtrans.setMontantOp(op.getMontantOp());
                mtrans.setMontantTaxe(op.getMontantTaxe());
                mtrans.setStatut(op.getStatut().name());
                listTransaction.add(mtrans);
            }

        }
        return listTransaction;
    }

    /**
     * Retrait à partir dun compte
     *
     * @param mOperation
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws JAXBException
     */
    /**
     * Retrait à partir dun compte
     *
     * @param mOperation
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws JAXBException
     */
    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponseOperation doOperationRetraitCompte(MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        Operation operation = new Operation();
        Boolean debitEstPossible = false;
        Compte cpteRetrait = null;
        try {
            log.info("BEGIN", mOperation);
            String description;
            log.info("Appel de la fonction retrait compte {}", mOperation);
            String destinataireMsg;
            String codeUoAcq = null;
            //  Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());
            BigDecimal montantTaxeClient = BigDecimal.ZERO;
            PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());

            Optional<PointVente> pointVenteExist = Optional.of(opt);

            log.info("Operation de retrait de puis le compte.....");
            if (mOperation.getCanal() == null) {

                showMessage("-1", "Le canal est obligatoire.");
            }

            if (mOperation.getCodePv() == null) {
                showMessage("-1", "Le point de vente est introuvable.");
            }

            if (mOperation.getTypeService() == null) {
                showMessage("-1", "Le type de service est obligatoire.");
            }

            if (mOperation.getMotif() == null) {
                showMessage("-1", "Le motif est obligatoire");
            }
            Canal canal = canalService.findCanal(mOperation.getCanal());
            //   Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
            operation = new Operation();
            TypeService typeService = typeServiceService.findByCodeService(mOperation.getTypeService());
            if (pointVenteExist.isPresent() && typeService != null && canal != null) {
                UniteOrganisation uoPaiement = pointVenteExist.get().getUniteOrganisation();
                uoPaiement = uniteOrganisationService.findOneById(uoPaiement.getId());
                log.info("Traitement de l'operation actuel  .....");
                UniteOrganisation organisation = pointVenteExist.get().getUniteOrganisation();
                User user = userService.findeByLogin(mOperation.getUserConnecte());
                //  operationService.userCanDoOperations(user);
                operationService.verifAutorisationOfTypeService(typeService, organisation);
                ZonedDateTime dateTime = ZonedDateTime.now();
                if (mOperation.getMotif() != null) {
                    MotifOperation motifOperation = motifOperationService.findByCode(mOperation.getMotif());
                    if (motifOperation != null) {
                        operation.setMotifOperation(motifOperation);
                    } else {
                        return showMessage("-1", "Le motif est obligatoire", operation);
                    }
                }
                if (mOperation.getCodeUoAcq() == null) {
                    codeUoAcq = pointVenteExist.get().getUniteOrganisation().getCodeUO();
                } else {
                    codeUoAcq = mOperation.getCodeUoAcq();
                }
                mOperation = getMontantCalculFrais(mOperation, mOperation.getCodePv(), mOperation.getCanal(), mOperation.getCodeUoEm(), codeUoAcq, mOperation.getMontant());
                /**
                 * *ON FAIT UN CONTROLE POUR VOIR SI LE CLIENT A UN EVENEMENT
                 * si le montant de l'evenement est superieur au montant à
                 * retirer
                 */

                if (Util.isBiggerThan(mOperation.getMontantFraisClient(), BigDecimal.ZERO)) {
                    montantTaxeClient = mOperation.getMontantTaxe();
                }
                if (mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_BIEN.toString())) {
                    description = "PAIEMENT DE BIEN ET SERVICE";
                } else {
                    description = "RETRAIT DEPUIS COMPTE";
                }
                operation.montantOp(mOperation.getMontant())
                        .montantTaxe(mOperation.getMontantTaxe())
                        .typeService(typeService)
                        .pointVente(pointVenteExist.get())
                        .description(description)
                        .statut(StatutOperation.ENATTENTE)
                        .canal(canal)
                        .codeOperation(RandomUtil.getCodeOperation(dateTime, uoPaiement.getCodeUO()))
                        .dateOperation(dateTime)
                        .montantOpEnDevise(mOperation.getMontant())
                        .user(user);
                //      operation = saveInformationPersonne(mOperation.getBeneficiaire(), Boolean.TRUE, operation);
                // if (!mOperation.getTypeService().equals(TypeServiceLibelle.CASH_ACCOUNT.toString())) {
                operation = saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operation);

                if (mOperation.getActeur() != null) {
                    if (mOperation.getActeur().equals(TypeProfilActeur.CLIENT.name())) {
                        //  Compte compteOperationActeur = compteService.getCompteOfOrganisationUser(organisation, EtiquetteCompteCode.CPTEOPERATIONACTEURPTI);
                        verifIfAutoriser(mOperation.getEmetteur().getCompte().getNumeroCompte(), mOperation.getActeur(), mOperation.getIdentifiant(), mOperation.getEmetteur().getTitulaire().getCodeTelephone());
                    }

                    /**/
                    if (mOperation.getActeur().equals(TypeProfilActeur.ORGANISATION.name())) {
                        /*On< verifie le compte du client*/
                        verifIfAutoriser(mOperation.getEmetteur().getCompte().getNumeroCompte(), TypeProfilActeur.CLIENT.name(), mOperation.getEmetteur().getTitulaire().getTelephone(), mOperation.getEmetteur().getTitulaire().getCodeTelephone());
                    }
                } else {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    throw new CustomParameterizedException("Impossible de donner suite a cette operation.");
                }

                //}
                // operation.compteDest(getCompte(mOperation.getBeneficiaire().getCompte(), operation.getBeneficaire().getPersonne()));

                /*  Lï¿½ on n'est dans une situation de cash to account**/
                operation.compteSrc(getCompte(mOperation.getEmetteur().getCompte(), operation.getEmetteur().getPersonne(), mOperation.getApplication(), Boolean.FALSE));
                /* le compte source est un compte client */
                if (mOperation.getTypeService().equals(TypeServiceLibelle.RETRAIT_COMPTE.toString())
                        || mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_BIEN.toString())) {
                    cpteRetrait = operation.getCompteSrc();
                    BigDecimal bgEve = evenementOperationRepository.findSommeEvenementOperation(cpteRetrait.getId(), TypeEvenementOperation.TRANSFERT_GRATUIT.toString());
                    log.info("Somme de l'evenement", bgEve);
                    log.info("Montant de l'operation", mOperation.getMontant());
                    if (Util.isBiggerThan(bgEve, BigDecimal.ZERO)) {/// s'il existe un evenement
                        log.info("------Evenement existe-----", bgEve);
                        if (Util.isBiggerThan(mOperation.getMontant(), bgEve)) {
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return showMessage("-1", "Vous devez retirer la totalité de votre transfert qui est de :" + bgEve.toString(), null);
                        } else {
                            log.info("Entrer retrait gratuit frais", mOperation.getMontantFraisClient());
                            log.info("Entrer retrait gratuit taxe", montantTaxeClient);
                            mOperation.setMontantFraisClient(BigDecimal.ZERO);
                            montantTaxeClient = BigDecimal.ZERO;
                            mOperation.setMontantTaxe(BigDecimal.ZERO);
                            mOperation.setMontantFrais(BigDecimal.ZERO);

                        }
                    }

                }

                if (operation.getCompteSrc().getCompteAutre() != null) {
                    debitEstPossible = compteService.peutDebiterCompte(operation.getCompteSrc(), Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(montantTaxeClient));
                    log.info("Controle du solde");
                } else if (operation.getCompteSrc().getCompteBanque() != null) {
                    Balance bal = apiService.getSolde(operation.getCompteSrc().getValeurCompte(), operation.getCompteSrc().getCompteBanque().getCodeAgence(), operation.getCompteSrc().getCompteBanque().getCodeFse());
                    Double solde = bal.getSolde();
                    BigDecimal soldeEnBigDecimal = new BigDecimal(solde);
                    BigDecimal mtop = Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(montantTaxeClient);
                    if (Util.isBiggerOrEqualThan(soldeEnBigDecimal, mtop)) {
                        debitEstPossible = true;
                    } else {
                        debitEstPossible = false;
                    }

                }

                if (!debitEstPossible) {
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return showMessage("-1", "Solde insuffisant.", null);
                }
                //Service de comptabilisation
                if (mOperation.getTypeService().equals(TypeServiceLibelle.RETRAIT_COMPTE.toString())
                        || mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_BIEN.toString())) {
                    log.info("Operation  Retrait depuis compte ou paiement de bien et service....");
                    UniteOrganisation fseSrc = null;
                    UniteOrganisation fseDest = null;
                    if (mOperation.getCodePvEm() != null) {
                        opt = pointVenteService.findPointByCode(mOperation.getCodePvEm());
                        Optional<PointVente> pointVenteEm = Optional.of(opt);
                        //  Optional<PointVente> pointVenteEm = pointVenteService.findByCode(mOperation.getCodePvEm());
                        if (pointVenteEm.isPresent()) {
                            fseSrc = pointVenteEm.get().getUniteOrganisation();
                        }
                    } else if (operation.getCompteSrc().getCompteBanque() != null) {
                        fseSrc = uniteOrganisationService.findByCodeFSE(operation.getCompteSrc().getCompteBanque().getCodeFse());
                    } else {
                        fseSrc = uniteOrganisationService.findByCodeFSE(operation.getCompteSrc().getCompteAutre().getCodeFse());
                    }

                    description = getDescription(operation);
                    operation.setDescription(description);
                    operation.uoEmetteur(fseSrc)
                            .statut(StatutOperation.VALIDE)
                            .uoAcquereur(uoPaiement)
                            .montantFraisClient(Util.getZeroIfNull(mOperation.getMontantFraisClient()))
                            .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                            .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()));
                    //Calcul des taxes de l'operation en cours
                    // operation = treatTaxeAndSaveOp(mOperation, operation, organisation);
                    operation = controleSeuil(operation);
                    operation.etape(Etape.PROCEED);
                    operation = operationRepository.save(operation);
                    operationService.calculRepartitionFrais(operation, fseSrc.getId(), operation.getUoAcquereur().getId());
                    /// operationAuditService.saveOpAudit(operation, null, StatutOperation.PAYE);
                    operationService.comptabilisationOperationMad(operation, pointVenteExist.get(), null);
                    operationAuditService.rechercheSuivi(operation);
                    BigDecimal moinsUn = new BigDecimal("-1");
                    evenementOperationService.saveEvt(operation, cpteRetrait, TypeEvenementOperation.TRANSFERT_GRATUIT.toString(), operation.getMontantOp().multiply(moinsUn), description, "");
                    log.info("Operation effectue avec succes");
                    log.info("FIN", mOperation);
                    return showMessage("0", "Operation effectue avec succes.", operation);
                } else {
                    return showMessage("-1", "Service non applicable", null);
                }
            } else {
                log.error("Le point de vente est introuvable");
                return showMessage("-1", "Le point de vente est introuvable.", null);
            }
        } catch (CustomParameterizedException e) {
            operationService.sendAck(operation, e.getMessage());
            //     TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operation, e.getMessage());
            //  TransactionAspectSupport.currentTransactionStatus().flush();
            return showMessage("-1", e.getMessage(), null);
        }

    }

    /**
     *
     * @param listparam
     * @param codeFse
     * @param type
     * @param operation
     * @param mOperation
     * @return
     * @throws JAXBException
     * @throws IOException
     */
    public SettlementResponse invoiceSettlement(List<MParametre> listparam, String codeFse, String type, Operation operation, MOperationFacture mOperation) {
        SettlementResponse param = null;
        String descReponse = "";
        String codeReponse = "";
        String identifiant = "";
        String typeAttribut = "";
        identifiant = codeFse;
        typeAttribut = "CODEFSE";
        try {
            if (type.equals("SETTLEMENT")) {
                param = apiService.settlement(listparam, codeFse);
                identifiant = codeFse;
                typeAttribut = "CODEFSE";
            } else if (type.equals("AIRTIME")) {
                if (mOperation != null) {
                    String codeFacturier = mOperation.getFacturier().getCodeFacturier();
                    String tel = mOperation.getBeneficiaire().getTitulaire().getCodeTelephone() + "" + mOperation.getBeneficiaire().getTitulaire().getTelephone();
                    identifiant = tel;
                    typeAttribut = "TELEPHONE";
                    param = apiService.sendAirtime(operation.getCodeOperation(), codeFacturier, tel, operation.getMontantOp().longValue() + "", codeFse, "0");

                }

            }
            if (param != null && param.getCodeMsg().equals("0")) {
                codeReponse = "0";
            } else {
                codeReponse = "-1";
            }
        } catch (Exception e) {
            codeReponse = "-1";
            descReponse = e.getMessage();
        } finally {
            String codeOperation = null;
            if (operation != null) {
                codeOperation = operation.getCodeOperation();
            }
            operationAuditEventService.sendOperationEvent(ActionService.ACT_SETTLEMENT, codeOperation, codeReponse, descReponse, identifiant, typeAttribut);
        }

        return param;
    }

    /**
     * Mise à jour d'une facture
     *
     * @param mOperation
     * @param oper
     * @param mParametresRes
     * @param transactionId
     * @param referenceId
     * @param messageRetour
     * @throws JAXBException
     */
    @Transactional
    public void updateFacture(MOperationFacture mOperation, Operation oper, List<MParametre> mParametresRes, String transactionId, String referenceId, String messageRetour, Facture facture) throws JAXBException {
        log.info("mise ï¿½ jour de la facture");
        // List<Facture> listFact = factureRepository.findByReferenceFacature(mOperation.getReferenceFacture());
        String strparam = "";
        if (facture != null) {
            Facture factureEnregistre = factureRepository.findById(facture.getId()).orElse(null);
            factureEnregistre.setDatePaiement(oper.getDateOperation().format(DateTimeFormatter.ISO_DATE_TIME));
            if (mOperation.getMontant() != null) {
                factureEnregistre.setMontantFacture(mOperation.getMontant().toString());
            } else {
                factureEnregistre.setMontantFacture("0");
            }
            factureEnregistre.setOperation(oper);
            factureEnregistre.setEtape(Etape.PROCEED);
            factureEnregistre.setMessageRet(messageRetour);
            factureEnregistre.setTransactionIdExt(transactionId);
            factureEnregistre.setReferencePaiement(oper.getCodeOperation());
            strparam = factureEnregistre.getStrExtraData();
            String extraData = factureEnregistre.getExtraData();
            MlistParametre mlistDonnee = (MlistParametre) Util.unmarshall(extraData, MlistParametre.class);
            /* la liste des parametres de retour */
            List<MParametre> mParametresRetour = mlistDonnee.getParametre();

            if (mParametresRes != null) {
                MlistParametre mlistParametre = new MlistParametre();
                mlistParametre.setParametre(mParametresRes);
                for (MParametre mParametre : mParametresRes) {
                    strparam = strparam + mParametre.getCodeParametre() + ":" + mParametre.getValeur();
                    mParametresRetour = ajoutParam(mParametresRetour, mParametre.getCodeParametre(), mParametre.getValeur());
                }
                mlistParametre.setParametre(mParametresRetour);

                factureEnregistre.setStrExtraData(strparam);
                String str = (String) Util.marshall(mlistParametre, String.class);
                factureEnregistre.setExtraData(str);
            }
            factureRepository.save(factureEnregistre);
        }
        /*
        Iterator it = listFact.iterator();
        String strparam = " ";
        ZonedDateTime dateTime = ZonedDateTime.now();
        if (listFact.size() <= 0) {

            Facture factNew = new Facture();
            factNew.setDatePaiement(oper.getDateOperation().format(DateTimeFormatter.ISO_DATE_TIME));
            factNew.setMontantFacture(mOperation.getMontant().toString());
            factNew.setOperation(oper);
            Produit p = new Produit();
            p.setId(mOperation.getProduit().getId());
            factNew.setProduit(p);
            factNew.setReferenceExt(referenceId);
            factNew.setMessageRet(messageRetour);
            factNew.setTransactionIdExt(transactionId);
            factNew.setReferenceFacature(mOperation.getReferenceFacture());
            factNew.setReferencePaiement(oper.getCodeOperation());

            factureRepository.save(factNew);
            log.info("Inserion de la facture" + mOperation.getReferenceFacture());
        } else {
            while (it.hasNext()) {
                Facture fact = (Facture) it.next();
                fact.setOperation(oper);
                fact.setReferencePaiement(oper.getCodeOperation());
                fact.setDatePaiement(dateTime.format(java.time.format.DateTimeFormatter.ISO_DATE_TIME));
                factureRepository.save(fact);
            }
            log.info("Mise ï¿½ jour terminï¿½e" + mOperation.getReferenceFacture());
        }
         */
    }

    @Transactional
    public Facture insertFacture(MOperationFacture mOperation, Operation oper, List<MParametre> mParametresRes) throws JAXBException {
        log.info("INSERTION DE LA FACTURE" + mOperation.getReferenceFacture());
        //      List<Facture> listFact = factureRepository.findByReferenceFacature(mOperation.getReferenceFacture());
        List<Facture> listFact = factureRepository.findByReferenceFacature(mOperation.getReferenceFacture());
        Facture fact = null;
        String codeOperation = null;
        if (listFact != null && listFact.size() <= 0) {
            String strparam = "";
            int i = 0;
            ZonedDateTime dateTime = ZonedDateTime.now();

            Facture factNew = new Facture();
            if (oper != null) {
                codeOperation = oper.getCodeOperation();
                factNew.setDatePaiement(oper.getDateOperation().format(DateTimeFormatter.ISO_DATE_TIME));
            }

            if (mOperation.getMontant() != null) {
                factNew.setMontantFacture(mOperation.getMontant().toString());
            } else {
                factNew.setMontantFacture("0");
            }

            //  factNew.setOperation(oper);
            Produit p = new Produit();
            p.setId(mOperation.getProduit().getId());
            factNew.setProduit(p);
            factNew.setEtape(Etape.INIT);
            factNew.setReferenceFacature(mOperation.getReferenceFacture());
            factNew.setReferencePaiement(codeOperation);
            if (mParametresRes != null) {
                MlistParametre mlistParametre = new MlistParametre();
                mlistParametre.setParametre(mParametresRes);
                for (MParametre mParametre : mParametresRes) {
                    if (i == 0) {
                        strparam = mParametre.getCodeParametre() + ":" + mParametre.getValeur();
                    } else {
                        strparam = strparam + " " + mParametre.getCodeParametre() + ":" + mParametre.getValeur();
                    }
                    i = i + 1;

                }
                factNew.setStrExtraData(strparam);
                String str = (String) Util.marshall(mlistParametre, String.class);
                factNew.setExtraData(str);
            }
            fact = factureRepository.save(factNew);
        }

        log.info("Inserion de la facture" + mOperation.getReferenceFacture());
        return fact;
    }

    /**
     *
     * @param mParametresRes
     * @param code
     * @param valeur
     * @return
     */
    private List<MParametre> ajoutParam(List<MParametre> mParametresRes, String code, String valeur) {
        if (mParametresRes != null) {
            if (rechercheParam(mParametresRes, code) == false) {
                MParametre p = new MParametre();
                p.setCodeParametre(code);
                p.setValeur(valeur);
                p.setAffiche(0);
                mParametresRes.add(p);
            }
        }
        return mParametresRes;
    }

    private List<MParametre> ajoutParam2(List<MParametre> mParametresRes, String code, String valeur) {
        if (mParametresRes != null) {
            if (rechercheParam(mParametresRes, code) == false) {
                MParametre p = new MParametre();
                p.setCodeParametre(code);
                p.setValeur(valeur);
                p.setAffiche(1);
                mParametresRes.add(p);
            }
        }
        return mParametresRes;
    }

    /**
     *
     * @param listparam
     * @param code
     * @return
     */
    private boolean rechercheParam(List<MParametre> mParametresRes, String code) {

        Iterator it = mParametresRes.iterator();
        MParametre param;
        while (it.hasNext()) {
            param = (MParametre) it.next();
            if (param.getCodeParametre().equals(code)) {
                return true;
            }
        }
        return false;
    }

    public String rechercheValeurParam(List<MParametre> mParametresRes, String code) {

        Iterator it = mParametresRes.iterator();
        MParametre param;
        while (it.hasNext()) {
            param = (MParametre) it.next();
            if (param.getCodeParametre().equals(code)) {
                return param.getValeur();
            }
        }
        return null;
    }

    /**
     * Verifier la validité du numéro de téléphone
     *
     * @param mOperation
     * @param operation
     * @param codeFse
     * @param type
     * @return
     */
    @Transactional(noRollbackFor = CustomSettlementException.class)
    private SettlementResponse verifAirtimeNumberofOperateur(MOperationFacture mOperation, String codeFse, String type) {

        try {
            List<MParametre> mParametresRes = new ArrayList<>();
            List<MParametre> mParametres = new ArrayList<>();
            if (mOperation.getListParam() != null && mOperation.getListParam().size() > 0) {
                mParametres = mOperation.getListParam();
            }
            if (mParametres != null) {
                mParametresRes = mParametres;
            }
            mParametresRes = ajoutParam(mParametresRes, "OPTION", "1");
            if (mOperation != null) {
                String codeFacturier = mOperation.getFacturier().getCodeFacturier();
                String tel = mOperation.getBeneficiaire().getTitulaire().getCodeTelephone() + "" + mOperation.getBeneficiaire().getTitulaire().getTelephone();
                SettlementResponse settlementResponse = apiService.sendAirtime("0", codeFacturier, tel, "0", codeFse, "1");
                if (settlementResponse != null && settlementResponse.getCodeMsg() != null && settlementResponse.getCodeMsg().equals("0")) {
                    return settlementResponse;
                } else {

                    throw new CustomSettlementException(settlementResponse.getMsg());

                }
            }

        } catch (Exception e) {
            throw new CustomSettlementException(e.getMessage());
        }
        return null;
    }

    /**
     * acquittement chez un partenaire
     *
     * @param mOperation
     * @param operation
     * @param codeFse
     * @param type
     * @throws JAXBException
     * @throws IOException
     */
    @Transactional(noRollbackFor = CustomSettlementException.class)
    private SettlementResponse verifIfSettlement(MOperationFacture mOperation, Operation operation, String codeFse, String type) throws JAXBException, IOException {
        String transactionId = null;
        String referenceId = null;
        String messageRetour = null;
        String codeReponse = null;
        Facture fact = null;
        String msg = "";
        log.info("Appel de la fonction settlement ");
        List<MParametre> mParametresRes = new ArrayList<>();
        List<MParametre> mParametres = new ArrayList<>();
        if (mOperation.getListParam() != null && mOperation.getListParam().size() > 0) {
            mParametres = mOperation.getListParam();
        }
        if (mParametres != null) {
            mParametresRes = mParametres;
        }
        if (operation != null) {
            if (mOperation.getReferenceFacture() == null) {
                mParametresRes = ajoutParam(mParametresRes, "REFERENCE", operation.getCodeOperation());
            } else {
                System.out.println(mOperation.getReferenceFacture());
                mParametresRes = ajoutParam(mParametresRes, "REFERENCE", mOperation.getReferenceFacture());
            }
            if ((operation.getEmetteur() != null) && (operation.getEmetteur().getPersonne() != null) && (operation.getEmetteur().getPersonne().getTelephone() != null)) {
                mParametresRes = ajoutParam(mParametresRes, "PHONENUMBER", operation.getEmetteur().getPersonne().getTelephone());
            }
            mParametresRes = ajoutParam(mParametresRes, "MONTANT", operation.getMontantOp().toString());
            mParametresRes = ajoutParam(mParametresRes, "REFERENCEOPERATION", operation.getCodeOperation());
            log.info("===REFERENCEOPERATION====" + operation.getCodeOperation());
            fact = insertFacture(mOperation, operation, mParametres);
        }
        SettlementResponse settlementResponse = invoiceSettlement(mParametres, codeFse, type, operation, mOperation);
        if (settlementResponse != null && settlementResponse.getCodeMsg() != null && settlementResponse.getCodeMsg().equals("0")) {
            transactionId = settlementResponse.getTransactionId();
            referenceId = settlementResponse.getReference();
            messageRetour = settlementResponse.getMsg();
            codeReponse = settlementResponse.getCodeMsg();
            if (settlementResponse.getCodeMsg().equals("0")) {
                if (settlementResponse.getmParametres() != null) {
                    mParametresRes = settlementResponse.getmParametres();
                }
                if (settlementResponse.getSendMsg() != null && settlementResponse.getSendMsg().equals(Boolean.TRUE)) {
                    if (mParametresRes != null) {
                        for (MParametre param : mParametresRes) {
                            if (param.getAffiche() == 1) {
                                String code = param.getCodeParametre().trim();
                                if (code.length() == 0) {
                                    msg = msg + param.getValeur() + "\n";
                                } else {
                                    msg = msg + param.getCodeParametre() + ":" + param.getValeur() + "\n";
                                }
                            }
                        }
                    }
                    HashMap<String, String> keyVal = new HashMap<String, String>();
                    keyVal.put("MSG", msg);
                    operationService.envoiSmsOperation("+" + mOperation.getEmetteur().getTitulaire().getCodeTelephone(), mOperation.getEmetteur().getTitulaire().getCodeTelephone() + mOperation.getEmetteur().getTitulaire().getTelephone(), "SETTLEMENT", keyVal);
                    System.out.println(msg);
                }
            } else {
                return settlementResponse;
            }

        } else {
            if (settlementResponse == null) {
                throw new CustomSettlementException("Erreur lors du traitement");
            }

        }
        log.info("FIN de l'Appel de la fonction settlement ");
        if (operation != null) {
            updateFacture(mOperation, operation, mParametresRes, transactionId, referenceId, messageRetour, fact);
        }

        return settlementResponse;
    }

    /**
     * Lever exception paiement facture
     *
     * @param settlementResponse verifIfSettlement
     */
    private void throwFactureOperationError(SettlementResponse settlementResponse) {
        if (settlementResponse != null && settlementResponse.getCodeMsg() != null && !"0".equals(settlementResponse.getCodeMsg())) {
            throw new CustomSettlementException(settlementResponse.getMsg());
        }
    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponse doOperationPaiementFactureDepuisCash(MOperationFacture mOperation) throws NoSuchAlgorithmException, IOException, JAXBException {
        log.info("Operation de paiement de facture..");
        String codeUoCode;
        Boolean debitEstPossible = false;
        Boolean traitementNormalPaiementFacture = true;
        List<MParametre> extraDataSettlement = null;
        String codeFse = "";
        //User userConnecte = userService.getUserWithAuthorities();
        //  userCanDoOperations(userConnecte);
        Operation operation = new Operation();
        try {
            if (mOperation.getFacturier() != null) {
                Long facId = mOperation.getFacturier().getId();
                String codeUo = getFacturierUo(facId);
                codeFse = getFacturierFSE(facId);
                String referencePaiement = "";
                operation = new Operation();
                //       Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());

                PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());

                Optional<PointVente> pointVenteExist = Optional.of(opt);

                User userConnecte = userService.findeByLogin(mOperation.getUserConnecte());
                //    Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
                Canal canal = canalService.findCanal(mOperation.getCanal());
                Produit produit = getFacturierProduit(mOperation.getProduit().getId());

                if (produit.getServiceAssocie() != null) {
                    TypeService typeServiceAssocie = produit.getServiceAssocie();

                    //si wallet to bank alors directement settlement
                    //si bank to wallet alors remplacer le type de service et continuer normalement
                    if (typeServiceAssocie.getCode().equals(TypeServiceLibelle.WALLET_TO_BANK.toString()) || typeServiceAssocie.getCode().equals(TypeServiceLibelle.BANK_TO_WALLET.toString())) {
                        if (typeServiceAssocie.getCode().equals(TypeServiceLibelle.WALLET_TO_BANK.toString())) {
                            mOperation.setTypeService(typeServiceAssocie.getCode());
                            traitementNormalPaiementFacture = false;
                        } else if (typeServiceAssocie.getCode().equals(TypeServiceLibelle.BANK_TO_WALLET.toString())) {
                            mOperation.setTypeService(typeServiceAssocie.getCode());
                        }
                    } else {
                        if (produit != null && produit.isTransactionnel() == false) {
                            mOperation.setTypeService("ACTIVE_COMPTE_BANK");
                        }
                    }

                }
                TypeService typeService = typeServiceService.findByCodeService(mOperation.getTypeService());

                if (typeService == null) {
                    return showMessageResult("-1", "Le service est indisponible actuellement veuillez reessayer plus tard", null, null);
                }

                if (traitementNormalPaiementFacture) {
                    UniteOrganisation uoDest = uniteOrganisationService.findByCode(codeUo);
                    String nomFacturier = uoDest.getLibelleUniteOrganisation();
                    UniteOrganisation fseDest = uniteOrganisationService.findUoParentByType(uoDest, TypeUO.FSE);
                    Compte compteOperationActeur = compteService.getCompteOfOrganisationUser(uoDest, EtiquetteCompteCode.CPTEOPERATIONACTEUR);

                    if (mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_FACTURE.toString()) || mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_FACTURE_DEPUIS_CASH.toString()) || mOperation.getTypeService().equals(TypeServiceLibelle.BANK_TO_WALLET.toString())) {
                        if (pointVenteExist.isPresent() && canal != null && userConnecte != null) {
                            UniteOrganisation uniteOrganisationSource = pointVenteExist.get().getUniteOrganisation();
                            uniteOrganisationSource = uniteOrganisationService.findOneById(uniteOrganisationSource.getId());
                            codeUoCode = uniteOrganisationSource.getCodeUO();
                            Compte compteOperationActeurSourcePTI = compteService.getCompteOfOrganisationUser(uniteOrganisationSource, EtiquetteCompteCode.CPTEOPERATIONACTEURPTI);
                            if (compteOperationActeurSourcePTI != null) {
                                verifIfAutoriser(compteOperationActeurSourcePTI.getValeurCompte(), mOperation.getActeur(), mOperation.getIdentifiant(), null);
                            }
                            Compte compteOperationActeurSource = compteService.getCompteOfOrganisationUser(uniteOrganisationSource, EtiquetteCompteCode.CPTEOPERATIONACTEUR);
                            if (!canal.getCodeCanal().equals("MOBILE") && !canal.getCodeCanal().equals("WEB") && !canal.getCodeCanal().equals("USSD")) {
                                return showMessageResult("-1", "Impossible de faire l'opération à travers ce canal." + canal.getCodeCanal(), null, null);
                                //  return new MResponseOperation("-1", "Impossible de faire l'opération à travers ce canal." + canal.getCodeCanal(), null);
                            }
                            log.debug("retrait%%" + compteOperationActeur.getCompteAutre());
                            log.debug("retrait%%" + mOperation.getCanal());
                            log.debug("retrait%%" + mOperation.getCodePv());
                            mOperation = getMontantCalculFraisFact(mOperation, mOperation.getCodePv(), mOperation.getCanal(), mOperation.getCodeUo(), mOperation.getFacturier().getCodeUo(), mOperation.getMontant());

                            BigDecimal mt = BigDecimal.ZERO;
                            BigDecimal montant = BigDecimal.ZERO;
                            BigDecimal taxe = BigDecimal.ZERO;
                            BigDecimal frais = BigDecimal.ZERO;
                            String description = mOperation.getTypeService();

                            BigDecimal montantTaxeClient = BigDecimal.ZERO;
                            if (Util.isBiggerThan(mOperation.getMontantFraisClient(), BigDecimal.ZERO)) {
                                montantTaxeClient = mOperation.getMontantTaxe();
                            }

                            if (mOperation.getMontantFrais() != null) {
                                frais = mOperation.getMontantFrais();
                            }
                            if (mOperation.getMontantTaxe() != null) {
                                taxe = mOperation.getMontantTaxe();
                            }
                            if (mOperation.getMontant() != null) {
                                montant = mOperation.getMontant();
                            }

                            if (compteOperationActeurSourcePTI.getCompteAutre() != null) {
                                debitEstPossible = compteService.peutDebiterCompte(compteOperationActeurSourcePTI, Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(montantTaxeClient));
                                //debitEstPossible = compteService.peutDebiterCompte(compteOperationActeurSourcePTI, frais.add(mOperation.getMontant()).add(taxe));
                            } else if (compteOperationActeurSourcePTI.getCompteBanque() != null) {
                                Balance bal = apiService.getSolde(compteOperationActeurSourcePTI.getValeurCompte(), compteOperationActeurSourcePTI.getCompteBanque().getCodeAgence(), compteOperationActeurSourcePTI.getCompteBanque().getCodeFse());
                                Double solde = bal.getSolde();
                                BigDecimal soldeEnBigDecimal = new BigDecimal(solde);
                                BigDecimal mtop = Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant().add(montantTaxeClient));
                                if (Util.isBiggerOrEqualThan(soldeEnBigDecimal, mtop)) {
                                    debitEstPossible = true;
                                } else {
                                    debitEstPossible = false;
                                }

                            }

                            if (debitEstPossible == true) {
                                //    typeService = typeServiceService.findByService(String.valueOf(TypeServiceLibelle.PAIEMENT_FACTURE_DEPUIS_CASH));
                                //       TypeService typeService = typeServiceService.findByCodeService(String.valueOf(TypeServiceLibelle.PAIEMENT_FACTURE_DEPUIS_CASH));
                                if (operationService.verifAutorisationOfTypeService(typeService, uniteOrganisationSource)) {

                                    if (mOperation.getMotif() != null) {
                                        MotifOperation motifOperation = motifOperationService.findByCode(mOperation.getMotif());
                                        if (motifOperation != null) {
                                            operation.setMotifOperation(motifOperation);
                                        } else {
                                            return showMessageResult("-1", "Le motif est obligatoire.", null, null);

                                        }
                                    }

                                    ZonedDateTime dateTime = ZonedDateTime.now();

                                    operation.montantOp(mOperation.getMontant())
                                            .montantTaxe(mOperation.getMontantTaxe())
                                            .typeService(typeService)
                                            .pointVente(pointVenteExist.get())
                                            .statut(StatutOperation.ENCOURS)
                                            .etape(Etape.INIT)
                                            .canal(canal)
                                            .description(description + " " + nomFacturier + " " + mOperation.getReferenceFacture())
                                            .codeOperation(RandomUtil.getCodeOperation(dateTime, codeUoCode))
                                            .dateOperation(dateTime)
                                            .montantOpEnDevise(mOperation.getMontant())
                                            .user(userConnecte);
                                    operation = saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operation);

                                    /*le compte destination */
                                    operation.compteDest(compteOperationActeur);
                                    operation.compteSrc(compteOperationActeurSource);
                                    operation.uoEmetteur(uniteOrganisationSource)
                                            .uoAcquereur(uoDest)
                                            .uoBeneficiaire(uoDest)
                                            .montantFraisClient(Util.getZeroIfNull(mOperation.getMontantFraisClient()))
                                            .montantAvoirClient(Util.getZeroIfNull(mOperation.getMontantAvoirClient()))
                                            .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                                            .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()));

                                    operation = operationRepository.save(operation);
                                    operationService.calculRepartitionFrais(operation, uniteOrganisationSource.getId(), fseDest.getId());
                                    checkPing(codeFse, mOperation.getListParam());
                                    extraDataSettlement = mOperation.getListParam();
                                    //   operationAuditService.saveOpAudit(operation, null, StatutOperation.PAYE);
                                    operationService.comptabilisationOperationPaiementFacture(operation, pointVenteExist.get());
                                    SettlementResponse settlementResponse = verifIfSettlement(mOperation, operation, codeFse, "SETTLEMENT");
                                    throwFactureOperationError(settlementResponse);
                                    operation.etape(Etape.PROCEED);
                                    operation.statut(StatutOperation.PAYE);
                                    operation = operationRepository.save(operation);
                                    extraDataSettlement = settlementResponse.getmParametres();
                                    List<MParametre> extraData = populateMessage(operation);
                                    extraDataSettlement = ajoutListParame(extraData, extraDataSettlement);
                                    return showMessageResult("0", "Operation effectue avec succes.", operation.getId().toString(), extraDataSettlement);
                                } else {
                                    return showMessageResult("-1", "Impossible d'effectuer cette opération", null, null);

                                }
                            } else {
                                return showMessageResult("-1", "Fonds insuffisants", null, null);

                            }

                        } else {
                            return showMessageResult("-1", "Le point de vente ou le canal  est introuvable.", null, null);

                        }
                    } else if (mOperation.getTypeService().equals(TypeServiceLibelle.ACTIVE_COMPTE_BANK.toString())) {
                        ZonedDateTime dateTime = ZonedDateTime.now();
                        String refacture = mOperation.getReferenceFacture() != null ? " Ref:" + mOperation.getReferenceFacture() : "";
                        referencePaiement = "SC" + RandomUtil.getCodeOperation(dateTime);
                        User user = userService.findByPointVente(pointVenteExist.get());
                        operation.montantOp(BigDecimal.ZERO)
                                .montantTaxe(BigDecimal.ZERO)
                                .typeService(typeService)
                                .pointVente(pointVenteExist.get())
                                .statut(StatutOperation.ENCOURS)
                                .canal(canal)
                                .motif(mOperation.getFacturier().getNomFacturier() + refacture)
                                .description("SOUSCRIPTION " + mOperation.getFacturier().getNomFacturier() + " " + mOperation.getReferenceFacture())
                                .codeOperation(referencePaiement)
                                .dateOperation(dateTime)
                                .montantOpEnDevise(BigDecimal.ZERO)
                                .user(user);
                        operation = operationRepository.save(operation);
                        SettlementResponse settlementResponse = verifIfSettlement(mOperation, operation, codeFse, "SETTLEMENT");

                        extraDataSettlement = settlementResponse.getmParametres();
                        List<MParametre> extraData = populateMessage(operation);
                        extraDataSettlement = ajoutListParame(extraData, extraDataSettlement);
                        throwFactureOperationError(settlementResponse);
                        operation.statut(StatutOperation.PAYE);
                        operation.etape(Etape.PROCEED);
                        operation = operationRepository.save(operation);
                        return showMessageResult("0", "Operation effectue avec succes.", operation.getId().toString(), extraDataSettlement);

                    } else {
                        log.error("Solde insuffisant ");
                        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        return showMessageResult("-1", "Solde insuffisant.", null, null);
                        // throw new CustomParameterizedException("Solde insuffisant.");
                    }
                } else {
                    SettlementResponse settlementResponse = verifIfSettlement(mOperation, null, codeFse, "SETTLEMENT");

                    if (settlementResponse != null) {

                        /**
                         * **Verifier que le settlement retourne bien le
                         * codeOperation=*
                         */
                        extraDataSettlement = settlementResponse.getmParametres();
                        Optional<Operation> oper = operationRepository.findByCodeOperation(settlementResponse.getTransactionId());
                        operation = oper.get();
                        List<MParametre> extraData = populateMessage(operation);
                        extraDataSettlement = ajoutListParame(extraData, extraDataSettlement);
                        throwFactureOperationError(settlementResponse);
                        return showMessageResult(settlementResponse.getCodeMsg(), settlementResponse.getMsg(), operation.getId().toString(), extraDataSettlement);
                    } else {
                        return showMessageResult("-1", "Impossible d'effectuer cette opération", null, null);
                    }

                }

            } else {
                //********//
                return showMessageResult("-1", "Impossible d'effectuer cette opération", null, null);
            }

        } catch (CustomParameterizedException e) {
            operationService.sendAckToOmnicanal(operation, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessageResult("-1", e.getMessage(), null, null);

        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operation, e.getMessage(), codeFse, extraDataSettlement);
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessageResult("-1", e.getMessage(), null, null);

        }

    }

    public boolean ifExist(String originalString, String key) {
        String sha256hex = Hashing.sha256().hashString(originalString, StandardCharsets.UTF_8).toString();
        CacheObject<String> redisMescasage = new CacheObject(sha256hex, key);
        CacheObject<String> resultat = hazelcastConfigCacheService.getValue(key);
        if (resultat != null && resultat.getContent().equals(sha256hex)) {
            return true;
        } else {
            hazelcastConfigCacheService.putValue(redisMescasage.getHashKey(), redisMescasage);
            return false;
        }

    }

    public Long getMontantSeuilPI() {

        if (montantSeuil.containsKey(lMontantSeuil) && montantSeuil.get(lMontantSeuil) != null) {
            return  montantSeuil.get(lMontantSeuil);
        } else {
           ParametreApp montantSeuilParam= parametreAppRepository.findByTypeAndNom(TypeParametre.SYSTEM, lMontantSeuil);
           if(montantSeuilParam!=null){
                 montantSeuil.put(lMontantSeuil, Long.parseLong(montantSeuilParam.getValeur()));
           }else{
                 montantSeuil.put(lMontantSeuil, 0L);
           }
          
            
        }
        return  montantSeuil.get(lMontantSeuil);
    }


    
    
    public List<MListeReseauServiceFse> listeReseauFse() {
        List<MListeReseauServiceFse> listMListeReseauServiceFse = new ArrayList();
        ParametreApp lparametrePays = parametreAppRepository.findByTypeAndNom(TypeParametre.SYSTEM, "LISTE_RESEAU_FSE");
        ParametreApp lparametrelisteService = parametreAppRepository.findByTypeAndNom(TypeParametre.SYSTEM, "LISTE_SERVICE_FSE");

        if (listeMListeReseauServiceFseMapDTO.containsKey(lserviceFse) && listeMListeReseauServiceFseMapDTO.get(lserviceFse) != null) {
            listMListeReseauServiceFse = listeMListeReseauServiceFseMapDTO.get(lserviceFse);
        } else {
            if (lparametrePays != null) {
                String[] listeService = lparametrelisteService.getValeur().split(",");
                String[] listPays = lparametrePays.getValeur().split(",");
                for (String codePays : listPays) {
                    MListeReseauServiceFse mListeReseauServiceFse = new MListeReseauServiceFse();
                    mListeReseauServiceFse.setCodePays(codePays);
                    List<MServiceReseau> listMServiceReseau = new ArrayList();

                    for (String codeService : listeService) {
                        MServiceReseau mServiceReseau = new MServiceReseau();
                        List<MOrganisation> lMorganisation = new ArrayList();
                        mServiceReseau.setCode(codeService);
                        List<TypeServiceReseauOrganisation> listTypeServiceReseauOrganisation = typeServiceReseauOrganisationRepository.getListTypeServiceReseau(codePays, codeService);
                        for (TypeServiceReseauOrganisation typeServiceReseauOrganisation : listTypeServiceReseauOrganisation) {
                            MOrganisation mOrga = new MOrganisation();
                            mOrga.setCodeFse(typeServiceReseauOrganisation.getUniteOrganisation().getCodeFse());
                            mOrga.setCodeUo(typeServiceReseauOrganisation.getUniteOrganisation().getCodeUO());
                            mOrga.setNom(typeServiceReseauOrganisation.getUniteOrganisation().getLibelleUniteOrganisation());
                            mOrga.setCodePvDef(typeServiceReseauOrganisation.getCodePv());
                            mOrga.setCodeGroupe(typeServiceReseauOrganisation.getUniteOrganisation().getGroupeFse().getCode());
                            lMorganisation.add(mOrga);
                        }
                        mServiceReseau.setListOrganisation(lMorganisation);
                        listMServiceReseau.add(mServiceReseau);

                    }
                    mListeReseauServiceFse.setListMServiceReseau(listMServiceReseau);

                    listMListeReseauServiceFse.add(mListeReseauServiceFse);
                }
                listeMListeReseauServiceFseMapDTO.put(lserviceFse, listMListeReseauServiceFse);
            }
        }

        return listMListeReseauServiceFse;

    }

    public List<MGroupeUniteOrganisation> findGroupeFse() {
        List<MGroupeUniteOrganisation> listgroupeFse = null;

        if (groupeFseMapDTO.containsKey(lgroupeFse) && groupeFseMapDTO.get(lgroupeFse) != null) {
            listgroupeFse = groupeFseMapDTO.get(lgroupeFse);
        } else {
            listgroupeFse = groupeFseRepository.findAllGroupeFse();
            if (listgroupeFse != null) {
                groupeFseMapDTO.put(lgroupeFse, listgroupeFse);
            }

        }

        return listgroupeFse;

    }

    /**
     * Paiement de facture
     *
     * @param mOperation
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws JAXBException
     */
    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponse doOperationPaiementFacture(MOperationFacture mOperation) throws NoSuchAlgorithmException, IOException, JAXBException {
        Operation operation = new Operation();
        BigDecimal montantTaxeClient = BigDecimal.ZERO;
        boolean traitementNormalPaiementFacture = true;

        List<MParametre> extraDataSettlement = null;
        String codeFse = "";
        try {
            log.info("Debut de l'operation de paiement de facture ");
            if (mOperation.getFacturier() != null) {
                Long facId = mOperation.getFacturier().getId();
                String codeUo = getFacturierUo(facId);
                codeFse = getFacturierFSE(facId);
                String referencePaiement = "";
                UniteOrganisation fseSrc;
                UniteOrganisation uoDest = uniteOrganisationService.findByCode(codeUo);
                String originalString = uoDest.getCodeUO() + "" + mOperation.getMontant().toString() + "" + mOperation.getIdentifiant();

                /**
                 * Si l'operation est encours de traitement
                 */
                boolean bool = ifExist(originalString, mOperation.getIdentifiant());
                if (bool) {
                    log.info("Une opération similaire est encours de traitement veuillez reessayer plus tard ");
                    return showMessageResult("-1", "Une opération similaire est encours de traitement veuillez reessayer plus tard", null, null);
                }
                /**
                 * Si l'operation est encours de traitement
                 */
                String nomFacturier = uoDest.getLibelleUniteOrganisation();
                UniteOrganisation fseDest = uniteOrganisationService.findUoParentByType(uoDest, TypeUO.FSE);
                Compte compteOperationActeur = compteService.getCompteOfOrganisationUser(uoDest, EtiquetteCompteCode.CPTEOPERATIONACTEUR);
                operation = new Operation();

                //     Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());
                PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());
                Optional<PointVente> pointVenteExist = Optional.of(opt);

                Produit produit = getFacturierProduit(mOperation.getProduit().getId());

                // Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
                Canal canal = canalService.findCanal(mOperation.getCanal());

                TypeService typeServiceAssocie = produit.getServiceAssocie();
                if (produit.getServiceAssocie() != null) {
                    typeServiceAssocie = produit.getServiceAssocie();

                    if (Boolean.TRUE.equals(produit.isTransactionnel())) {
                        String serviceCode = typeServiceAssocie.getCode();

                        // Cas spécifiques où le type de service est important
                        if (TypeServiceLibelle.WALLET_TO_BANK.toString().equals(serviceCode)
                                || TypeServiceLibelle.BANK_TO_WALLET.toString().equals(serviceCode)
                                || TypeServiceLibelle.ACCOUNT_ACCOUNT.toString().equals(serviceCode)) {

                            mOperation.setTypeService(serviceCode);

                            if (TypeServiceLibelle.WALLET_TO_BANK.toString().equals(serviceCode)) {
                                // Si wallet vers banque, on ne passe pas par le traitement normal de paiement facture
                                traitementNormalPaiementFacture = false;
                            }
                        }
                    } else {
                        String serviceCode = typeServiceAssocie.getCode();
                        // produit non transactionnel mais service associé présent
                        traitementNormalPaiementFacture = Boolean.TRUE.equals(produit.isTransactionnel());
                        mOperation.setTypeService(serviceCode);
                    }

                } else if (produit.getServiceAssocie() == null && Boolean.FALSE.equals(produit.isTransactionnel())) {
                    // Si aucun service associé et produit non transactionnel, définir un service par défaut
                    mOperation.setTypeService("ACTIVE_COMPTE_BANK");
                }

                TypeService typeService = typeServiceService.findByCodeService(mOperation.getTypeService());
                String description = mOperation.getTypeService();

                // TypeService typeService = typeServiceService.findByService(mOperation.getTypeService());
                if (typeService == null) {
                    return showMessageResult("-1", "Le service est indisponible actuellement veuillez reessayer plus tard", null, null);
                }

                if (mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_FACTURE.name()) || mOperation.getTypeService().equals(TypeServiceLibelle.BANK_TO_WALLET.name()) || mOperation.getTypeService().equals(TypeServiceLibelle.ACCOUNT_ACCOUNT.name())) {
                    if (pointVenteExist.isPresent() && typeService != null && canal != null) {
                        log.info("Traitement de l'operation de paiement de facture  .....");
                        UniteOrganisation organisation = pointVenteExist.get().getUniteOrganisation();
                        organisation = uniteOrganisationService.findOneById(organisation.getId());
                        User user = userService.findByPointVente(pointVenteExist.get());
                        ZonedDateTime dateTime = ZonedDateTime.now();
                        //recupération de la référence de la facture //
                        String refacture = mOperation.getReferenceFacture() != null ? " Ref:" + mOperation.getReferenceFacture() : "";
                        referencePaiement = RandomUtil.getCodeOperation(dateTime, organisation.getCodeUO());
                        operation.montantOp(mOperation.getMontant())
                                .montantTaxe(mOperation.getMontantTaxe())
                                .typeService(typeService)
                                .pointVente(pointVenteExist.get())
                                .statut(StatutOperation.ENCOURS)
                                .canal(canal)
                                .motif(mOperation.getFacturier().getNomFacturier() + refacture)
                                .description(description + " " + nomFacturier + " " + mOperation.getReferenceFacture())
                                .codeOperation(referencePaiement)
                                .dateOperation(dateTime)
                                .montantOpEnDevise(mOperation.getMontant())
                                .user(user);
                        operation = saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operation);
                        operation.compteSrc(getCompte(mOperation.getEmetteur().getCompte(), operation.getEmetteur().getPersonne(), mOperation.getApplication(), Boolean.FALSE));

                        if (Util.isBiggerThan(mOperation.getMontantFraisClient(), BigDecimal.ZERO)) {
                            montantTaxeClient = mOperation.getMontantTaxe();
                        }

                        if (operation.getCompteSrc().getCompteBanque() != null) {
                            fseSrc = uniteOrganisationService.findByCodeFSE(operation.getCompteSrc().getCompteBanque().getCodeFse());
                        } else {
                            fseSrc = uniteOrganisationService.findByCodeFSE(operation.getCompteSrc().getCompteAutre().getCodeFse());
                        }
                        mOperation = getMontantCalculFraisFact(mOperation, mOperation.getCodePv(), mOperation.getCanal(), fseSrc.getCodeUO(), uoDest.getCodeUO(), mOperation.getMontant());
                        Boolean debitEstPossible = false; //= compteService.peutDebiterCompte(operation.getCompteSrc(), Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(mOperation.getMontantTaxe()));

                        if (operation.getCompteSrc().getCompteAutre() != null) {
                            debitEstPossible = compteService.peutDebiterCompte(operation.getCompteSrc(), Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(montantTaxeClient));
                            log.info("Controle du solde");
                        } else if (operation.getCompteSrc().getCompteBanque() != null) {
                            Balance bal = apiService.getSolde(operation.getCompteSrc().getValeurCompte(), operation.getCompteSrc().getCompteBanque().getCodeAgence(), operation.getCompteSrc().getCompteBanque().getCodeFse());
                            Double solde = bal.getSolde();
                            BigDecimal soldeEnBigDecimal = new BigDecimal(solde);
                            BigDecimal mtop = Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(montantTaxeClient);
                            if (Util.isBiggerOrEqualThan(soldeEnBigDecimal, mtop)) {
                                debitEstPossible = true;
                            } else {
                                debitEstPossible = false;
                            }

                        }

                        if (debitEstPossible) {
                            operation.compteDest(compteOperationActeur);
                            operation.uoEmetteur(fseSrc)
                                    .uoAcquereur(uoDest)
                                    .uoBeneficiaire(uoDest)
                                    .montantFraisClient(Util.getZeroIfNull(mOperation.getMontantFraisClient()))
                                    .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                                    .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()));
                            operation = controleSeuil(operation);
                            operationService.calculRepartitionFrais(operation, fseSrc.getId(), uoDest.getId());
                            operation = operationRepository.save(operation);
                            checkPing(codeFse, mOperation.getListParam());
                            extraDataSettlement = mOperation.getListParam();
                            extraDataSettlement = ajoutParam(extraDataSettlement, "REFERENCEOPERATION", operation.getCodeOperation());
                            operationService.comptabilisationOperationPaiementFacture(operation, pointVenteExist.get());
                            SettlementResponse settlementResponse = verifIfSettlement(mOperation, operation, codeFse, "SETTLEMENT");
                            extraDataSettlement = settlementResponse.getmParametres();
                            List<MParametre> extraData = populateMessage(operation);
                            extraDataSettlement = ajoutListParame(extraData, extraDataSettlement);
                            throwFactureOperationError(settlementResponse);
                            operation.statut(StatutOperation.PAYE);
                            operation.etape(Etape.PROCEED);
                            operation = operationRepository.save(operation);
                            // operationAuditService.saveOpAudit(operation, null, StatutOperation.PAYE);
                            log.info("fin de l'operation de paiement de facture ");
                            //showMessageResult(String code, String message, String tansactionId, List<MParametre> extraData)

                            return showMessageResult("0", "Operation effectue avec succes.", operation.getId().toString(), extraDataSettlement);
                        } else {
                            log.error("Solde insuffisant ");
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return showMessageResult("-1", "Solde insuffisant.", null, null);
                            // throw new CustomParameterizedException("Solde insuffisant.");
                        }

                        //   operationService.calculRepartitionFrais(operation, fseSrc.getId(), fseDest.getId());
                    }
                } else {
                    if (mOperation.getTypeService().equals(TypeServiceLibelle.ACTIVE_COMPTE_BANK.name()) || typeService.getCode().equals(TypeServiceLibelle.LINK.name()) || typeService.getCode().equals(TypeServiceLibelle.DELINK.name())) {
                        operation = saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operation);
                        ZonedDateTime dateTime = ZonedDateTime.now();
                        String refacture = mOperation.getReferenceFacture() != null ? " Ref:" + mOperation.getReferenceFacture() : "";
                        referencePaiement = "SC" + RandomUtil.getCodeOperation(dateTime);
                        User user = userService.findByPointVente(pointVenteExist.get());
                        operation.montantOp(BigDecimal.ZERO)
                                .montantTaxe(BigDecimal.ZERO)
                                .typeService(typeService)
                                .pointVente(pointVenteExist.get())
                                .statut(StatutOperation.ENCOURS)
                                .canal(canal)
                                .motif(mOperation.getFacturier().getNomFacturier() + refacture)
                                .description("SOUSCRIPTION " + nomFacturier + " " + mOperation.getReferenceFacture())
                                .codeOperation(referencePaiement)
                                .dateOperation(dateTime)
                                .montantOpEnDevise(BigDecimal.ZERO)
                                .user(user);
                        operation.statut(StatutOperation.VALIDE);
                        operation.etape(Etape.PROCEED);
                        operation = operationRepository.save(operation);
                        SettlementResponse settlementResponse = verifIfSettlement(mOperation, operation, codeFse, "SETTLEMENT");
                        throwFactureOperationError(settlementResponse);
                        if (settlementResponse != null) {
                            extraDataSettlement = settlementResponse.getmParametres();
                            if (settlementResponse.getTransactionId() != null) {
                                Optional<Operation> oper = operationRepository.findByCodeOperation(settlementResponse.getTransactionId());
                                operation = oper.get();
                                List<MParametre> extraData = populateMessage(operation);
                                extraDataSettlement = ajoutListParame(extraData, extraDataSettlement);
                                return showMessageResult(settlementResponse.getCodeMsg(), settlementResponse.getMsg(), operation.getId().toString(), extraDataSettlement);
                            } else {
                                return showMessageResult(settlementResponse.getCodeMsg(), settlementResponse.getMsg(), null, extraDataSettlement);
                            }

                        } else {
                            return showMessageResult("-1", "Impossible d'effectuer cette opération", null, null);
                        }
                    }
                }

            }
            log.error("Erreur lors du paiement ");
            return showMessageResult("-1", "Erreur.", null, null);

        } catch (CustomParameterizedException e) {
            operationService.sendAckToOmnicanal(operation, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessageResult("-1", e.getMessage(), null, null);

        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operation, e.getMessage(), codeFse, extraDataSettlement);
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessageResult("-1", e.getMessage(), null, null);
        }

    }

    /**
     * Paiement de facture
     *
     * @param mOperation
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws JAXBException
     */
    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponse doOperationPrelevement(MOperationFacture mOperation) throws NoSuchAlgorithmException, IOException, JAXBException {
        log.info("======Debut de l'operation de PRELEVEMENT============= ");
        Operation operation = new Operation();
        BigDecimal montantTaxeClient = BigDecimal.ZERO;
        try {

            if (mOperation.getFacturier() != null) {
                Long facId = mOperation.getFacturier().getId();
                String codeUo = getFacturierUo(facId);
                String codeFse = getFacturierFSE(facId);
                String referencePaiement = "";
                UniteOrganisation fseSrc;
                UniteOrganisation uoDest = uniteOrganisationService.findByCode(codeUo);
                UniteOrganisation fseDest = uniteOrganisationService.findUoParentByType(uoDest, TypeUO.FSE);
                Compte compteOperationActeur = compteService.getCompteOfOrganisationUser(uoDest, EtiquetteCompteCode.CPTEOPERATIONACTEUR);
                operation = new Operation();

                //     Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());
                PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());
                Optional<PointVente> pointVenteExist = Optional.of(opt);

                Produit produit = getFacturierProduit(mOperation.getProduit().getId());

                // Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
                Canal canal = canalService.findCanal(mOperation.getCanal());

                if (produit != null && produit.isTransactionnel() == false) {
                    mOperation.setTypeService("ACTIVE_COMPTE_BANK");
                }
                TypeService typeService = typeServiceService.findByCodeService(mOperation.getTypeService());
                if (mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_FACTURE.toString())) {
                    if (pointVenteExist.isPresent() && typeService != null && canal != null) {
                        log.info("Traitement de l'operation de prelevement  .....");
                        UniteOrganisation organisation = pointVenteExist.get().getUniteOrganisation();
                        User user = userService.findByPointVente(pointVenteExist.get());
                        ZonedDateTime dateTime = ZonedDateTime.now();
                        //recupération de la référence de la facture //
                        String refacture = mOperation.getReferenceFacture() != null ? " Ref:" + mOperation.getReferenceFacture() : "";

                        referencePaiement = RandomUtil.getCodeOperation(dateTime);
                        //  referencePaiement = RandomUtil.getCodeOperation(dateTime);
                        operation.montantOp(mOperation.getMontant())
                                .montantTaxe(mOperation.getMontantTaxe())
                                .typeService(typeService)
                                .pointVente(pointVenteExist.get())
                                .statut(StatutOperation.ENCOURS)
                                .canal(canal)
                                .motif(mOperation.getFacturier().getNomFacturier() + refacture)
                                .description("PRELEVEMENT  " + uoDest.getLibelleUniteOrganisation() + " " + mOperation.getReferenceFacture())
                                .codeOperation(referencePaiement)
                                .dateOperation(dateTime)
                                .montantOpEnDevise(mOperation.getMontant())
                                .user(user);
                        operation = saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operation);
                        operation.compteSrc(getCompte(mOperation.getEmetteur().getCompte(), operation.getEmetteur().getPersonne(), mOperation.getApplication(), Boolean.FALSE));
                        if (Util.isBiggerThan(mOperation.getMontantFraisClient(), BigDecimal.ZERO)) {
                            montantTaxeClient = mOperation.getMontantTaxe();
                        }
                        if (operation.getCompteSrc().getCompteBanque() != null) {
                            fseSrc = uniteOrganisationService.findByCodeFSE(operation.getCompteSrc().getCompteBanque().getCodeFse());
                        } else {
                            fseSrc = uniteOrganisationService.findByCodeFSE(operation.getCompteSrc().getCompteAutre().getCodeFse());
                        }
                        mOperation = getMontantCalculFraisFact(mOperation, mOperation.getCodePv(), mOperation.getCanal(), fseSrc.getCodeUO(), uoDest.getCodeUO(), mOperation.getMontant());
                        Boolean debitEstPossible = false; //= compteService.peutDebiterCompte(operation.getCompteSrc(), Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(mOperation.getMontantTaxe()));

                        if (operation.getCompteSrc().getCompteAutre() != null) {
                            debitEstPossible = compteService.peutDebiterCompte(operation.getCompteSrc(), Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(montantTaxeClient));
                            log.info("Controle du solde");
                        } else if (operation.getCompteSrc().getCompteBanque() != null) {
                            Balance bal = apiService.getSolde(operation.getCompteSrc().getValeurCompte(), operation.getCompteSrc().getCompteBanque().getCodeAgence(), operation.getCompteSrc().getCompteBanque().getCodeFse());
                            Double solde = bal.getSolde();
                            BigDecimal soldeEnBigDecimal = new BigDecimal(solde);
                            BigDecimal mtop = Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(montantTaxeClient);
                            if (Util.isBiggerOrEqualThan(soldeEnBigDecimal, mtop)) {
                                debitEstPossible = true;
                            } else {
                                debitEstPossible = false;
                            }

                        }

                        if (debitEstPossible) {
                            operation.compteDest(compteOperationActeur);
                            operation.uoEmetteur(fseSrc)
                                    .uoAcquereur(uoDest)
                                    .uoBeneficiaire(uoDest)
                                    .montantFraisClient(Util.getZeroIfNull(mOperation.getMontantFraisClient()))
                                    .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                                    .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()));
                            //  operation = controleSeuil(operation);
                            operationService.calculRepartitionFrais(operation, fseSrc.getId(), uoDest.getId());
                            operation = operationRepository.save(operation);
                            operationService.comptabilisationOperationPaiementFacture(operation, pointVenteExist.get());
                            log.info("==ENTREE===SETTLEMENT==========");
                            SettlementResponse settlementResponse = verifIfSettlement(mOperation, operation, codeFse, "SETTLEMENT");
                            throwFactureOperationError(settlementResponse);
                            log.info("=====FIN SETTLEMENT==========");
                            operation.statut(StatutOperation.PAYE);
                            operation.etape(Etape.PROCEED);
                            operation = operationRepository.save(operation);
                            // operationAuditService.saveOpAudit(operation, null, StatutOperation.PAYE);
                            log.info("fin de l'operation de paiement de facture ");
                            //showMessageResult(String code, String message, String tansactionId, List<MParametre> extraData)
                            // List<MParametre> extraDataSettlement = settlementResponse.getmParametres();
                            ///  List<MParametre> extraData = populateMessage(operation);
                            //  extraDataSettlement = ajoutListParame(extraData, extraDataSettlement);
                            return showMessageResult("0", "Operation effectue avec succes.", operation.getId().toString(), null);
                        } else {
                            log.error("Solde insuffisant ");
                            throw new CustomSettlementException("Solde insuffisant.");
                        }

                        //   operationService.calculRepartitionFrais(operation, fseSrc.getId(), fseDest.getId());
                    }
                } else if (mOperation.getTypeService().equals(TypeServiceLibelle.ACTIVE_COMPTE_BANK.toString())) {
                    ZonedDateTime dateTime = ZonedDateTime.now();
                    String refacture = mOperation.getReferenceFacture() != null ? " Ref:" + mOperation.getReferenceFacture() : "";
                    referencePaiement = "SC" + RandomUtil.getCodeOperation(dateTime);
                    User user = userService.findByPointVente(pointVenteExist.get());
                    operation.montantOp(BigDecimal.ZERO)
                            .montantTaxe(BigDecimal.ZERO)
                            .typeService(typeService)
                            .pointVente(pointVenteExist.get())
                            .statut(StatutOperation.ENCOURS)
                            .canal(canal)
                            .motif(mOperation.getFacturier().getNomFacturier() + refacture)
                            .description("SOUSCRIPTION " + mOperation.getFacturier().getNomFacturier() + " " + mOperation.getReferenceFacture())
                            .codeOperation(referencePaiement)
                            .dateOperation(dateTime)
                            .montantOpEnDevise(BigDecimal.ZERO)
                            .user(user);

                    log.info("======APPEL DE LA FONCTION SETTLEMENT============= ");
                    SettlementResponse settlementResponse = verifIfSettlement(mOperation, operation, codeFse, "SETTLEMENT");
                    List<MParametre> extraDataSettlement = settlementResponse.getmParametres();
                    List<MParametre> extraData = populateMessage(operation);
                    extraDataSettlement = ajoutListParame(extraData, extraDataSettlement);

                    throwFactureOperationError(settlementResponse);

                    operation.statut(StatutOperation.PAYE);
                    operation.etape(Etape.PROCEED);
                    operation = operationRepository.save(operation);
                    return showMessageResult("0", "Operation effectue avec succes.", operation.getId().toString(), extraDataSettlement);
                    //  return showMessage("0", "Operation effectue avec succes.", operation);
                } else {
                    log.error("Erreur lors du paiement ");
                    return showMessageResult("-1", "Erreur.", null, null);
                }
            }
            log.error("Erreur lors du paiement ");

            return showMessageResult("-1", "Erreur.", null, null);

        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operation, e.getMessage());
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.info("======Catch============= {]", e.getMessage());
            return showMessageResult("-1", e.getMessage(), null, null);
        }

    }

    private List<MParametre> ajoutListParame(List<MParametre> extraDataSrc, List<MParametre> mParametresResDest) {
        for (MParametre m : extraDataSrc) {
            mParametresResDest = ajoutParam2(mParametresResDest, m.getCodeParametre(), m.getValeur());
        }
        return mParametresResDest;
    }

    /**
     * paiemet de credit téléphonique
     *
     * @param mOperation
     * @return
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws JAXBException
     */
    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponseOperation doOperationPaiementCredit(MOperationFacture mOperation) throws NoSuchAlgorithmException, IOException, JAXBException {
        log.info("Debut de l'operation de paiement de facture ");
        Operation operation = new Operation();
        List<MParametre> extraDataSettlement = null;
        if (mOperation.getFacturier() != null) {
            Long facId = mOperation.getFacturier().getId();
            String codeUo = getFacturierUo(facId);
            String codeFse = getFacturierFSE(facId);
            try {

                String referencePaiement = "";
                BigDecimal montantTaxeClient = BigDecimal.ZERO;
                String motifPaiement = "";
                UniteOrganisation uoDest = uniteOrganisationService.findByCode(codeUo);
                UniteOrganisation fseDest = uniteOrganisationService.findUoParentByType(uoDest, TypeUO.FSE);
                Compte compteOperationActeur = compteService.getCompteOfOrganisationUser(uoDest, EtiquetteCompteCode.CPTEOPERATIONACTEUR);
                operation = new Operation();
                TypeService typeService = typeServiceService.findByService(mOperation.getTypeService());
                if (typeService == null) {
                    return showMessage("-1", "Le service est indisponible actuellement veuillez reessayer plus tard", null);
                }
                // TypeService typeService = typeServiceService.findByCodeService(mOperation.getTypeService());

                // Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());
                PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());
                Optional<PointVente> pointVenteExist = Optional.of(opt);
                Canal canal = canalService.findCanal(mOperation.getCanal());
                //Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
                if (mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_AIRTIME.toString())) {
                    verifAirtimeNumberofOperateur(mOperation, codeFse, "AIRTIME");
                    if (pointVenteExist.isPresent() && typeService != null && canal != null) {
                        if (mOperation.getBeneficiaire() != null && mOperation.getBeneficiaire().getTitulaire() != null) {
                            motifPaiement = " au profit de" + mOperation.getBeneficiaire().getTitulaire().getCodeTelephone() + mOperation.getBeneficiaire().getTitulaire().getTelephone();
                        }
                        log.info("Traitement de l'operation de paiement de facture  .....");
                        UniteOrganisation organisation = pointVenteExist.get().getUniteOrganisation();
                        User user = userService.findByPointVente(pointVenteExist.get());
                        ZonedDateTime dateTime = ZonedDateTime.now();
                        referencePaiement = RandomUtil.getCodeOperation(dateTime);
                        //    mOperation = getMontantCalculFraisFact(mOperation, mOperation.getCodePv(), mOperation.getCanal(), fseSrc.getCodeUO(), uoDest.getCodeUO(), mOperation.getMontant());
                        operation.montantOp(mOperation.getMontant())
                                .montantTaxe(mOperation.getMontantTaxe())
                                .typeService(typeService)
                                .pointVente(pointVenteExist.get())
                                .statut(StatutOperation.ENCOURS)
                                .canal(canal)
                                .description("PAIEMENT DE CREDIT " + mOperation.getFacturier().getNomFacturier() + " " + mOperation.getReferenceFacture())
                                .codeOperation(RandomUtil.getCodeOperation(dateTime, uoDest.getCodeUO()))
                                .dateOperation(dateTime)
                                .motif(motifPaiement)
                                .montantOpEnDevise(mOperation.getMontant())
                                .user(user);
                        operation = saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operation);
                        // operation = saveInformationPersonne(mOperation.getBeneficiaire(), Boolean.TRUE, operation);
                        operation.compteSrc(getCompte(mOperation.getEmetteur().getCompte(), operation.getEmetteur().getPersonne(), mOperation.getApplication(), Boolean.FALSE));
                        Boolean debitEstPossible = compteService.peutDebiterCompte(operation.getCompteSrc(), Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(montantTaxeClient));
                        if (debitEstPossible) {
                            operation.compteDest(compteOperationActeur);
                            UniteOrganisation fseSrc;

                            if (operation.getCompteSrc().getCompteBanque() != null) {
                                fseSrc = uniteOrganisationService.findByCodeFSE(operation.getCompteSrc().getCompteBanque().getCodeFse());
                            } else {
                                fseSrc = uniteOrganisationService.findByCodeFSE(operation.getCompteSrc().getCompteAutre().getCodeFse());
                            }
//
                            mOperation = getMontantCalculFraisFact(mOperation, mOperation.getCodePv(), mOperation.getCanal(), fseSrc.getCodeUO(), uoDest.getCodeUO(), mOperation.getMontant());
                            operation.uoEmetteur(fseSrc)
                                    .uoAcquereur(uoDest)
                                    .uoBeneficiaire(uoDest)
                                    .montantAvoirClient(Util.getZeroIfNull(mOperation.getMontantAvoirClient()))
                                    .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                                    .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()));

                            operationService.calculRepartitionFrais(operation, fseSrc.getId(), fseDest.getId());
                            operation = operationRepository.save(operation);
                            //  operationAuditService.saveOpAudit(operation, null, StatutOperation.PAYE);

                            extraDataSettlement = mOperation.getListParam();
                            extraDataSettlement = ajoutParam(extraDataSettlement, "REFERENCEOPERATION", operation.getCodeOperation());
                            operationService.comptabilisationOperationPaiementCredit(operation, pointVenteExist.get(), mOperation, codeFse);
                            SettlementResponse settlementResponse = verifIfSettlement(mOperation, operation, codeFse, "AIRTIME");
                            extraDataSettlement = settlementResponse.getmParametres();

                            List<MParametre> extraData = populateMessage(operation);
                            extraDataSettlement = ajoutListParame(extraData, extraDataSettlement);
                            throwFactureOperationError(settlementResponse);
                            // operationAuditService.saveOpAudit(operation, null, StatutOperation.PAYE);
                            operation.statut(StatutOperation.PAYE);
                            operation.etape(Etape.PROCEED);
                            operation = operationRepository.save(operation);
                            log.info("fin de l'operation de paiement de facture ");
                            return showMessage("0", "Operation effectuee avec succes.", operation);
                        } else {
                            log.error("Solde insuffisant ");

                            throw new CustomParameterizedException("Solde insuffisant.");
                        }

                        //   operationService.calculRepartitionFrais(operation, fseSrc.getId(), fseDest.getId());
                    } else {
                        return showMessage("-1", "Erreur lors du traitement.", null);
                    }
                } else {
                    log.error("Erreur lors du paiement ");
                    return showMessage("-1", "Erreur.", null);
                }

            } catch (CustomParameterizedException e) {
                //operationService.sendAckToOmnicanal(operation);

                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return showMessage("-1", e.getMessage(), null);
            } catch (CustomSettlementException e) {
                operationService.sendAckToOmnicanal(operation, e.getMessage(), codeFse, extraDataSettlement);
                // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return showMessage("-1", e.getMessage(), null);
            }
        } else {
            return showMessage("-1", "Erreur.", null);
        }

    }

    @Async("taskExecutor")
    public void repartirCommission(Operation oper) {
        if (oper.getPointVente() != null) {
            UniteOrganisation uo = oper.getPointVente().getUniteOrganisation();
            OperationCommissionDTO opcom = new OperationCommissionDTO();
            opcom.setOperationId(oper.getId());
            opcom.setUoId(uo.getId());
            operationService.envoiRepartition(opcom);
        }

    }

    @Async("taskExecutor")
    public void sendCashBack(Operation oper) {
        if (oper != null && oper.getStatut().equals(StatutOperation.PAYE) && Util.isBiggerThan(Util.getZeroIfNull(oper.getMontantAvoirClient()), BigDecimal.ZERO)) {
            operationService.sendCashBack(oper.getId().toString());
        }

    }

    public MResponseOperation doOperationCashBack(Operation operation) throws IOException {
        Operation oper = new Operation();
        ZonedDateTime dateTime = ZonedDateTime.now();
        TypeService typeService = typeServiceService.findByService("CASH_BACK");
        MResponseOperation mResponseOperation = new MResponseOperation();
        UniteOrganisation fseDest = uniteOrganisationService.findUoParentByType(operation.getPointVente().getUniteOrganisation(), TypeUO.FSE);
        UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(fseDest);
        Compte compteOperationFrsPlateforme = compteService.getCompteOfOrganisationUser(fseDestAgenceSiege, EtiquetteCompteCode.CPTFRSPLATEFORME);
        oper.setCompteSrc(compteOperationFrsPlateforme);

        //    oper.setBeneficaire(operation.getEmetteur());
        oper.compteDest(operation.getCompteSrc());
        oper.montantOp(operation.getMontantAvoirClient())
                .montantFrais(Util.getZeroIfNull(BigDecimal.ZERO))
                .montantTaxe(Util.getZeroIfNull(BigDecimal.ZERO))
                .typeService(typeService)
                .codeExterne(operation.getCodeOperation())
                .pointVente(operation.getPointVente())
                .statut(StatutOperation.ENCOURS)
                .canal(operation.getCanal())
                .description(operation.getDescription())
                .codeOperation(RandomUtil.getCodeOperation(dateTime, operation.getPointVente().getUniteOrganisation().getCodeUO()))
                .dateOperation(dateTime)
                .montantOpEnDevise(operation.getMontantAvoirClient())
                .montantAvoirClient(Util.getZeroIfNull(BigDecimal.ZERO))
                .user(operation.getUser());
        oper.setBeneficaire(operation.getBeneficaire());
        String description = getDescription(oper);
        oper.setDescription(description);
        oper.setBeneficaire(operation.getBeneficaire());
        oper = operationRepository.save(oper);
        Result result = apiService.virementSimple(oper.getCompteSrc(), oper.getCompteDest(), oper.getMontantOp(), description, oper.getCodeOperation(), oper);
        if (result != null && result.getCodeErr().equals("0")) {
            oper.statut(StatutOperation.PAYE);
            envoiDesMessage(oper, null);
            mResponseOperation = new MResponseOperation(result.getCodeErr(), result.getMsg(), null);
        } else {
            oper.statut(StatutOperation.AMBIGUE);
            //  oper = operationRepository.save(oper);
            mResponseOperation = new MResponseOperation("-1", "Erreur", null);
        }
        oper = operationRepository.save(oper);
        return mResponseOperation;
    }

    public Operation getOperation(Long operId) {
        Operation oper = operationRepository.findByOperationId(operId);
        return oper;
    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponseOperation doOperationRetraitCardLess(Operation mOperation) throws NoSuchAlgorithmException, IOException, JAXBException {
        Operation operationRetrait = new Operation();
        try {
            log.info("Operation de RETRAIT en cours ....");
            PointVente pointVenteExist = mOperation.getPointVente();
            Canal canal = mOperation.getCanal();

            if (pointVenteExist != null && canal != null) {
                if (!canal.getCodeCanal().equals("MOBILE") && !canal.getCodeCanal().equals("USSD")) {
                    return new MResponseOperation("-1", "Impossible de faire l'opération à travers ce canal." + canal.getCodeCanal(), null);
                }
                OperationAllDTO operationAllDTO = operationService.findOne(mOperation.getId());
                if (operationAllDTO != null && operationAllDTO.getBeneficiaire() != null) {
                    log.debug("Operation de RETRAIT en cours %%....");
                    log.info("Operation de RETRAIT en cours %%....");
                    PersonneDTO beneficiaire = operationAllDTO.getBeneficiaire().getPersonneDTO();
                    String telephone = beneficiaire.getTelephone();
                    String codeTelephone = beneficiaire.getCodePaysTelephone();
                    if (telephone != null && codeTelephone != null) {

                        log.info("Ok pour les conditions de realisation ....");
                        Operation operation = operationMapper.toEntity(operationAllDTO.getOperationDTO());
                        operation.statut(StatutOperation.PAYE);
                        operation = operationRepository.save(operation);
                        Operation op = operation;

                        // operationAuditService.saveOpAudit(operation, operationAllDTO.getOperationDTO().getStatut(), StatutOperation.PAYE);
                        operationRetrait = new Operation();
                        TypeService typeService = typeServiceService.findByService(String.valueOf(TypeServiceLibelle.RETRAIT));
                        //TypeService typeService = typeServiceService.findByCodeService(String.valueOf(TypeServiceLibelle.RETRAIT));
                        // op.setTypeService(typeService);
                        //  op.setPointVente(pointVenteExist.get());
                        operationRetrait.statut(StatutOperation.PAYE)
                                .beneficaire(operation.getBeneficaire())
                                .canal(canal)
                                .description("OPERATION DE RETRAIT")
                                .emetteur(operation.getEmetteur())
                                .beneficaire(operation.getBeneficaire())
                                .typeService(typeService)
                                .pointVente(pointVenteExist)
                                .uoAcquereur(pointVenteExist.getUniteOrganisation())
                                .montantOp(operation.getMontantOp())
                                .montantFrais(Util.getZeroIfNull(repartitionOpService.getMontantByActeurAndOp(Acteur.ACQ, operation.getId())))
                                .montantFraisAcq(Util.getZeroIfNull(repartitionOpService.getMontantByActeurAndOp(Acteur.ACQ, operation.getId())))
                                .montantOpEnDevise(operation.getMontantOpEnDevise())
                                .dateOperation(ZonedDateTime.now())
                                .etape(Etape.PROCEED)
                                .uoEmetteur(pointVenteExist.getUniteOrganisation())
                                .devisePaiement(operation.getDevisePaiement())
                                .operationTraite(operation)
                                .codeOperation(RandomUtil.getCodeOperation(operation.getDateOperation()));
                        operationRetrait.setPieceBeneficiaire(operation.getPieceEmetteur());
                        operationRetrait = operationRepository.save(operationRetrait);
                        //  operationAuditService.saveOpAudit(operationRetrait, null, StatutOperation.PAYE);
                        // Result response = apiService.sendRequestForComptabilisationRetrait(operationRetrait, operationRetrait.getPointVente().getUniteOrganisation());
                        // instructionComptableService.comptabiliserOperation(operationRetrait, null, uoAcq);
                        //  operationService.comptabilisationRetrait(operation, operationRetrait.getPointVente().getUniteOrganisation(), operationRetrait);
                        return new MResponseOperation("0", "Opération effectué avec succès.", operationRetrait);
                    } else {
                        return new MResponseOperation("-1", "Le numï¿½ro du bï¿½nï¿½ficiaire est introuvable.", null);
                    }
                } else {
                    return new MResponseOperation("-1", "Impossible de traiter cette opération.", null);
                }

            } else {
                return new MResponseOperation("-1", "Le point de vente ou le canal ou le code retrait est introuvable.", null);
            }
        } catch (CustomParameterizedException e) {
            operationService.sendAckToOmnicanal(operationRetrait, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operationRetrait, e.getMessage());
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        }

    }

    @Transactional
    private Operation validationOperationCardless(Operation operation, String entityCode) throws IOException, NoSuchAlgorithmException, JAXBException {
        EvenementOperation evenement;
        if (operation.getStatut().equals(StatutOperation.ENATTENTE)) {
            com.m2i.omnicanal.outils.restclient.model.Message msg = apiService.cardLessTransactionStatus(operation.getCodeOperation(), entityCode);
            if (msg != null && msg.getCodeErr() != null && msg.getCodeErr().equals("0")) {
                Operation oper = operationService.comptabilisationOperationCardless(operation, operation.getPointVente());
                if (oper != null) {
                    //  MResponseOperation rep = doOperationRetraitCardLess(operation);
                    //if (rep != null && rep.getCode().equals("0")) {
                    evenement = evenementOperationRepository.findByOperation(operation);
                    if (evenement != null) {
                        evenement.setEstLevee(1);
                        evenement.setEnTraitement(Boolean.FALSE);
                        evenementOperationRepository.save(evenement);
                    }
                    repartirCommission(oper);
                    return operation;
                } else {
                    operationService.sendAckToOmnicanal(operation, "");
                    return null;
                }
            }

        }
        return null;
    }

    public OperationCardlessDTO verifcardless(OperationCardlessDTO oper) throws IOException, NoSuchAlgorithmException, JAXBException {
        Operation operation = operationRepository.findByOperationId(oper.getOperationId());
        com.m2i.omnicanal.outils.restclient.model.Message msg = apiService.cardLessTransactionStatus(operation.getCodeOperation(), oper.getEntityCode());
        if (msg != null && msg.getCodeErr() != null && msg.getCodeErr().equals("0")) {
            oper.setIsOk(1);
        } else {
            oper.setIsOk(0);
        }
        return oper;

    }

    public void sendOperationCardlessDTO(OperationCardlessDTO oper) {
        ///  apiService.executionCardless(oper);
    }

    public void cardlessTransactionSetllement(OperationCardlessDTO oper) throws IOException, NoSuchAlgorithmException, JAXBException {
        Operation operation = operationRepository.findByOperationId(oper.getOperationId());
        validationOperationCardless(operation, oper.getEntityCode());
    }

    /**
     * Envoi de message
     *
     * @param oper
     */
    @Async("taskExecutor")
    public void envoiDesMessage(Operation oper, List<MParametre> extraData) {
        String codeReponse = "0";
        String msg = "";
        try {
            if (oper.getTypeService().getCode().equals(TypeServiceLibelle.CASH_ACCOUNT.toString()) || oper.getTypeService().getCode().equals(TypeServiceLibelle.CASH_ACCOUNT_AGENT.toString())) {
                operationService.envoiMsgOperation(oper, "0");
                //  operationService.sendMessageCashToAccount(oper);
            } else if (oper.getTypeService().getCode().equals(TypeServiceLibelle.VENTE_DEPOSIT_CARDLESS.toString()) || oper.getTypeService().getCode().equals(TypeServiceLibelle.ACCOUNT_ACCOUNT.toString()) || oper.getTypeService().getCode().equals(TypeServiceLibelle.RETRAIT_CARDLESS.toString()) || oper.getTypeService().getCode().equals(TypeServiceLibelle.ACCOUNT_CASH.toString()) || oper.getTypeService().getCode().equals(TypeServiceLibelle.BANK_TO_WALLET.toString()) || oper.getTypeService().getCode().equals(TypeServiceLibelle.WALLET_TO_BANK.toString())) {
                // operationService.sendMessageAccountToAccount(oper);
                operationService.envoiMsgOperation(oper, "0");
            } else if (oper.getTypeService().getCode().equals(TypeServiceLibelle.PAIEMENT_FACTURE.toString()) || oper.getTypeService().getCode().equals(TypeServiceLibelle.PAIEMENT_AIRTIME.toString())) {
                // operationService.sendMessagePaiementFacture(oper);
                operationService.envoiMsgOperation(oper, "0");
            } else if (oper.getTypeService().getCode().equals(TypeServiceLibelle.PAIEMENT_BIEN.toString())) {
                //operationService.sendMessagePaiementBien(oper);
                operationService.envoiMsgOperation(oper, "0");
            } else if (oper.getTypeService().getCode().equals(TypeServiceLibelle.RECEPTION_TRANSFERT.toString())) {
                //operationService.sendMessageReception(oper);
                operationService.envoiMsgOperation(oper, "0");

            } else if (oper.getTypeService().getCode().equals(TypeServiceLibelle.PAIEMENT_FACTURE_DEPUIS_CASH.toString())) {
                //   operationService.sendMessagePaiementFacture(oper);

                operationService.envoiMsgOperation(oper, "0");
            } else if (oper.getTypeService().getCode().equals(TypeServiceLibelle.PAIEMENT_INTERNET.toString())) {
                operationService.envoiMsgOperation(oper, "0");

            } else if (oper.getTypeService().getCode().equals(TypeServiceLibelle.APPROVISIONNEMENT_INTERNE.toString())) {
                operationService.envoiMsgOperation(oper);
            } else if (oper.getTypeService().getCode().equals(TypeServiceLibelle.RETRAIT_COMPTE.toString())) {
                //   operationService.sendMessagePaiementFacture(oper);

                operationService.envoiMsgOperation(oper, "0");

            } else if (oper.getTypeService().getCode().equals(TypeServiceLibelle.TRANSFERT_MASSE_CASH.toString()) || oper.getTypeService().getCode().equals(TypeServiceLibelle.TRANSFERT_MASSE.toString())) {
                operationService.envoiMsgOperation(oper, "0");
            } else {
                operationService.envoiMsgOperation(oper, "0");
            }
            if (extraData != null) {
                MlistParametre mlistParametre = new MlistParametre();
                mlistParametre.setParametre(extraData);
                String str = (String) Util.marshall(mlistParametre, String.class);
                oper.setExtraData(str);
                operationService.save(oper);
            }
            msg = "MESSAGE ENREGISTRE";
        } catch (Exception e) {

            msg = e.getMessage();
            codeReponse = "-1";
        } finally {
            operationAuditEventService.sendOperationEvent(ActionService.ACT_ENVOI_SMS, oper.getCodeOperation(), codeReponse, msg, "", "");
        }

    }

    /**
     * Verification avant la validation de l'opï¿½ration
     *
     * @param
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public TableauCumulDTO getTableauCumulActeur(Operation mOperation) {
        PointVente pointVenteExist = mOperation.getPointVente();
        UniteOrganisation uniteOrganisationSource = null;
        uniteOrganisationSource = pointVenteExist.getUniteOrganisation();

        Canal canal = mOperation.getCanal();
        uniteOrganisationSource = uniteOrganisationService.findOneById(uniteOrganisationSource.getId());
        UniteOrganisation uoPays;
        if (!uniteOrganisationSource.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
            uoPays = uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());
        } else {
            uoPays = uniteOrganisationSource;
        }
        //   UniteOrganisation uoPays = uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());// uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.PAYS);
        TypeService typeService = mOperation.getTypeService();//

        TableauCumulDTO tab = null;
        ProfilActeur profilActeur = uniteOrganisationSource.getProfilActeur();
        if (typeService.isOperationClient() != null && !typeService.isOperationClient()) {
            tab = operationService.getTableauCumulDTO(uniteOrganisationSource.getId(),
                    typeService.getId(),
                    canal.getId(),
                    null,
                    null,
                    uoPays.getId(),
                    mOperation.getUoAcquereur().getId(), TypeRegle.ACTEUR,
                    mOperation.getMontantOp(), mOperation.getMontantOp(), profilActeur.getId(), null, null);
        } else {
            Long fseDestId = mOperation.getUoAcquereur().getId();
            UniteOrganisation fseDest = uniteOrganisationService.findOneById(fseDestId);//Fse Dest
            UniteOrganisation uoPaysDest;
            if (!fseDest.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
                uoPaysDest = uniteOrganisationService.findOneById(fseDest.getUoPaysId());
            } else {
                uoPaysDest = fseDest;
            }

            // uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq
            tab = operationService.getTableauCumulDTO(uniteOrganisationSource.getId(),
                    typeService.getId(),
                    canal.getId(),
                    null,
                    null,
                    uoPays.getId(),
                    uoPaysDest.getId(), TypeRegle.ACTEUR,
                    mOperation.getMontantOp(), mOperation.getMontantOp(), profilActeur.getId(), null, null);
        }

        return tab;

    }

    public TableauCumulDTO controleSeuil(BigDecimal montant, BigDecimal solde, Client client) {
        BigDecimal totalAvecSolde = montant.add(solde);
        Long profilClientID = null;
        Client clientCherche = clientService.findClient(client.getId());
        if (client != null) {
            profilClientID = clientCherche.getProfilActeur() != null ? clientCherche.getProfilActeur().getId() : null;
        }

        TableauCumulDTO tab = operationService.getTableauCumulDTO(null, null, null, null, client.getId(), null,
                null, TypeRegle.CLIENT,
                montant, totalAvecSolde, profilClientID, null, null);

        return tab;

    }

    public TableauCumulDTO controleSeuilMontant(BigDecimal montant, BigDecimal solde, Client client) {
        BigDecimal totalAvecSolde = montant.add(solde);
        Long profilClientID = null;
        Client clientCherche = clientService.findClient(client.getId());
        if (client != null) {
            profilClientID = clientCherche.getProfilActeur() != null ? clientCherche.getProfilActeur().getId() : null;
        }

        TableauCumulDTO tab = operationService.getTableauCumulVerifMontant(null, null, null, null, client.getId(), null,
                null, TypeRegle.CLIENT,
                montant, totalAvecSolde, profilClientID, null, null);

        return tab;

    }

    /**
     * Fonction de controle de seuil
     *
     * @param mOperation
     * @return
     */
    private Operation controleSeuil(Operation mOperation) {
        log.debug("ENTREE DANS LA FONCTION CONTROL SEUIL");
        TableauCumulDTO tab = getTableauCumulClient(mOperation);
        log.debug("ENTREE DANS LA FONCTION CONTROL SEUIL");
        if (tab != null) {
            if ((tab.getAction() != null) && tab.getAction().equals(Action.BLOQUE)) {
                throw new CustomParameterizedException(tab.getMessage());

            } else if ((tab.getAction() != null) && tab.getAction().equals(Action.NON_BLOQUANT)) {
                mOperation.setStatut(StatutOperation.ENATTENTE_VALIDATION);
            }
        }
        TableauCumulDTO tabacteur = getTableauCumulActeur(mOperation);
        if (tabacteur != null) {
            if ((tabacteur.getAction() != null) && tabacteur.getAction().equals(Action.BLOQUE)) {
                throw new CustomParameterizedException(tabacteur.getMessage());

            } else if ((tabacteur.getAction() != null) && tabacteur.getAction().equals(Action.NON_BLOQUANT)) {
                mOperation.setStatut(StatutOperation.ENATTENTE_VALIDATION);
            }
        }
        log.debug("FIN DANS LA FONCTION CONTROL SEUIL");
        return mOperation;
    }

    public Operation controleSeuil(Operation mOperation, UniteOrganisation UoSource, UniteOrganisation uoAcq, Personne emetteur, Personne beneficiaire) {
        TableauCumulDTO tab = getTableauCumulClient(mOperation, UoSource, uoAcq, emetteur, beneficiaire);
        if (tab != null) {
            if ((tab.getAction() != null) && tab.getAction().equals(Action.BLOQUE)) {
                throw new CustomParameterizedException(tab.getMessage());
            } else if ((tab.getAction() != null) && tab.getAction().equals(Action.NON_BLOQUANT)) {
                throw new CustomParameterizedException(tab.getMessage());
            }
        }
        return mOperation;
    }

    public TableauCumulDTO getTableauCumulClient(Operation mOperation, UniteOrganisation UoSource, UniteOrganisation uoAcq, Personne emetteur, Personne beneficiaire) {
        UniteOrganisation uniteOrganisationSource = null;
        UniteOrganisation uoPays;
        Long uoAcquereurId = null;
        if (!UoSource.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
            uoPays = uniteOrganisationService.findOneById(UoSource.getUoPaysId());
        } else {
            uoPays = uniteOrganisationSource;
        }

        //   UniteOrganisation uoPays = uniteOrganisationService.findOneById(UoSource.getUoPaysId());
        // UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(UoSource, TypeUO.PAYS);
        TypeService typeService = typeServiceService.findByService(TypeServiceLibelle.ACCOUNT_ACCOUNT.toString());

        //   Canal canal = canalService.findByCodeCanal("MOBILE");
        Canal canal = canalService.findCanal("MOBILE");
        TableauCumulDTO tab = null;
        if (mOperation.getUoAcquereur() != null) {
            if (mOperation.getUoAcquereur().getId() != null) {
                uoAcquereurId = mOperation.getUoAcquereur().getId();
            }
        }
        if (!mOperation.getTypeService().getCode().equals(TypeServiceLibelle.CASH_ACCOUNT.toString()) && !mOperation.getTypeService().getCode().equals(TypeServiceLibelle.CASH_ACCOUNT_AGENT.toString())) {
            if (emetteur != null) {

                Client client = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(emetteur.getId());
                if (client != null) {
                    ProfilActeur profilClient = client.getProfilActeur();
                    tab = operationService.getTableauCumulDTO(null, typeService.getId(), canal.getId(), null, client.getId(), uoPays.getId(),
                            uoAcq.getId(), TypeRegle.CLIENT,
                            mOperation.getMontantOp(), mOperation.getMontantOp(), profilClient.getId(), null, emetteur.getId());
                }
            }
        }

        if (tab != null) {
            return tab;
        }
        //****Bï¿½nï¿½ficiaire on contrï¿½le son solde******/

        BigDecimal mtop = mOperation.getMontantOp();
        BigDecimal totalAvecSolde = mtop;
        if (beneficiaire != null) {
            if (mOperation.getCompteDest() != null) {
                if (mOperation.getCompteDest().getCompteAutre() != null) {

                    totalAvecSolde = mtop.add(mOperation.getCompteDest().getCompteAutre().getSolde());

                    //      mtop.add(Util.getZeroIfNull(montant)), mOperation.getCompteDest().getCompteAutre().getSolde());
                }
            }
            Client benef = clientRepository.findTop1ByPersonneIdAndClientValideTrueAndSupprimeIsFalse(beneficiaire.getId());
            if (benef != null) {
                ProfilActeur profilClient = benef.getProfilActeur();
                tab = operationService.getTableauCumulDTO(null, typeService.getId(), canal.getId(), benef.getId(), null, uoPays.getId(),
                        uoAcq.getId(), TypeRegle.CLIENT,
                        mOperation.getMontantOp(), totalAvecSolde, profilClient.getId(), beneficiaire.getId(), null);
            } else if (beneficiaire.getId() != null) {
                if (mOperation.getUoAcquereur() != null) {
                    if (mOperation.getUoAcquereur().getId() != null) {
                        uoAcquereurId = mOperation.getUoAcquereur().getId();
                    }
                }
                tab = operationService.getTableauCumulDTO(null, typeService.getId(), canal.getId(), null, null, uoPays.getId(),
                        uoAcquereurId, TypeRegle.CLIENT,
                        mOperation.getMontantOp(), totalAvecSolde, null, beneficiaire.getId(), null);
            }
        }

        return tab;
    }

    /**
     * Verification avant la validation de l'opï¿½ration
     *
     * @param
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public TableauCumulDTO getTableauCumulClient(Operation mOperation) {
        log.info("ENTREE DANS LA FONCTION TABLEAU CUMUL");
        PointVente pointVenteExist = mOperation.getPointVente();
        UniteOrganisation uniteOrganisationSource = null;
        uniteOrganisationSource = pointVenteExist.getUniteOrganisation();
        Canal canal = mOperation.getCanal();
        uniteOrganisationSource = uniteOrganisationService.findOneById(uniteOrganisationSource.getId());

        UniteOrganisation uoPays;
        Long uoAcquereurId = null;
        if (!uniteOrganisationSource.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
            uoPays = uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());
        } else {
            uoPays = uniteOrganisationSource;
        }

        //  UniteOrganisation uoPays = uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());
        // UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.PAYS);
        TypeService typeService = mOperation.getTypeService();//
        Personne emetteur = null;
        Personne beneficiaire = null;
        //Recherche de la  personne effectuant l'operaiton
        if (!mOperation.getTypeService().getCode().equals(TypeServiceLibelle.CASH_ACCOUNT.name()) && !mOperation.getTypeService().getCode().equals(TypeServiceLibelle.CASH_ACCOUNT_AGENT.name())) {
            if (mOperation.getEmetteur() != null) {
                String telephone = mOperation.getEmetteur().getPersonne().getTelephone();
                String code = mOperation.getEmetteur().getPersonne().getTelephone();
                if (telephone != null && code != null) {
                    emetteur = mOperation.getEmetteur().getPersonne();
                }
            }
        }

        TableauCumulDTO tab = null;
        // ProfilActeur profilActeur = uniteOrganisationSource.getProfilActeur();
        /* Récupération de lid de l acquereur*/
        if (mOperation.getUoAcquereur() != null) {
            if (mOperation.getUoAcquereur().getId() != null) {
                uoAcquereurId = mOperation.getUoAcquereur().getId();
            }
        }
        if (emetteur != null) {
            Client client = clientService.findByPersonneId(emetteur.getId());
            //Client client = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(emetteur.getId());
            if (client != null) {
                ProfilActeur profilClient = client.getProfilActeur();
                tab = operationService.getTableauCumulDTO(null, typeService.getId(), canal.getId(), null, client.getId(), uoPays.getId(),
                        uoAcquereurId, TypeRegle.CLIENT,
                        mOperation.getMontantOp(), mOperation.getMontantOp(), profilClient.getId(), null, emetteur.getId());
            } else if (emetteur.getId() != null) {
                tab = operationService.getTableauCumulDTO(null, typeService.getId(), canal.getId(), null, null, uoPays.getId(),
                        uoAcquereurId, TypeRegle.CLIENT,
                        mOperation.getMontantOp(), mOperation.getMontantOp(), null, null, emetteur.getId());
            }
        }
        if (tab != null) {
            return tab;
        }
        //****Bï¿½nï¿½ficiaire on contrï¿½le son solde******/
        if (mOperation.getBeneficaire() != null) {
            String telephone = mOperation.getBeneficaire().getPersonne().getTelephone();
            String code = mOperation.getBeneficaire().getPersonne().getTelephone();
            if (telephone != null && code != null) {
                beneficiaire = mOperation.getBeneficaire().getPersonne();
            }
        }
        BigDecimal mtop = mOperation.getMontantOp();
        BigDecimal totalAvecSolde = mtop;
        if (beneficiaire != null) {
            if (mOperation.getCompteDest() != null) {
                if (mOperation.getCompteDest().getCompteAutre() != null) {

                    totalAvecSolde = mtop.add(mOperation.getCompteDest().getCompteAutre().getSolde());

                    //      mtop.add(Util.getZeroIfNull(montant)), mOperation.getCompteDest().getCompteAutre().getSolde());
                }
            }

            Client benef = clientService.findByPersonneId(beneficiaire.getId());
            //   Client benef = clientRepository.findTop1ByPersonneIdAndClientValideTrueAndSupprimeIsFalse(beneficiaire.getId());
            if (benef != null && benef.getId() != null) {
                ProfilActeur profilClient = benef.getProfilActeur();
                tab = operationService.getTableauCumulDTO(null, typeService.getId(), canal.getId(), benef.getId(), null, uoPays.getId(),
                        uoAcquereurId, TypeRegle.CLIENT,
                        mOperation.getMontantOp(), totalAvecSolde, profilClient.getId(), beneficiaire.getId(), null);
            } else if (beneficiaire != null && beneficiaire.getId() != null) {
                tab = operationService.getTableauCumulDTO(null, typeService.getId(), canal.getId(), null, null, uoPays.getId(),
                        uoAcquereurId, TypeRegle.CLIENT,
                        mOperation.getMontantOp(), totalAvecSolde, null, beneficiaire.getId(), null);
            }
        }
        log.info("FIN DANS LA FONCTION TABLEAU CUMUL");
        return tab;
    }

    public Compte verifIfAutoriser(String numeroCompte, String acteur, String identifiant, String codeIndicatif, Integer preAutorisation) {
        Compte cpt = null;
        if (preAutorisation != null && preAutorisation.equals(1)) {
            cpt = new Compte();
        } else {
            if (acteur.equals(TypeProfilActeur.CLIENT.name())) {
                Optional<Personne> searchPersonne = personneService.findPersonneByPhoneAndCodePays(identifiant, codeIndicatif);
                if (searchPersonne.isPresent()) {
                    Client client = clientService.findByPersonneId(searchPersonne.get().getId());
                    if (client != null) {
                        cpt = compteService.findIfCompteExisteWithClient(numeroCompte, client);
                    } else {
                        throw new CustomParameterizedException("Attention ce client ne peut pas effectuer cette operation.");
                    }

                }
            } else if (acteur.equals(TypeProfilActeur.ORGANISATION.name())) {
                UniteOrganisation uo = uniteOrganisationService.findByCode(identifiant);
                cpt = compteService.getCompteOrganisation(uo, numeroCompte);

            }

        }

        if (cpt != null) {
            return cpt;
        } else {
            throw new CustomParameterizedException("Attention vous ne pouvez pas effectuer cette operation.");
        }

    }

    /**
     *
     *
     * @Return
     */
    public Compte verifIfAutoriser(String numeroCompte, String acteur, String identifiant, String codeIndicatif) {

        Compte cpt = null;
        if (acteur.equals(TypeProfilActeur.CLIENT.name())) {
            Optional<Personne> searchPersonne = personneService.findPersonneByPhoneAndCodePays(identifiant, codeIndicatif);
            if (searchPersonne.isPresent()) {
                Client client = clientService.findByPersonneId(searchPersonne.get().getId());
                if (client != null) {
                    cpt = compteService.findIfCompteExisteWithClient(numeroCompte, client);
                } else {
                    throw new CustomParameterizedException("Attention ce client ne peut pas effectuer cette operation.");
                }

            }
        } else if (acteur.equals(TypeProfilActeur.ORGANISATION.name())) {

            UniteOrganisation uo = uniteOrganisationService.findByCode(identifiant);
            cpt = compteService.getCompteOrganisation(uo, numeroCompte);

        }
        if (cpt != null) {
            return cpt;
        } else {
            throw new CustomParameterizedException("Attention vous ne pouvez pas effectuer cette operation.");
        }

    }

    /**
     *
     * @param mOperation
     * @param codePv
     * @param canal
     * @param codeSrc
     * @param codeDest
     * @param montant
     * @return
     */
    public MOperation getMontantCalculFraisOld(MOperation mOperation, String codePv, String canal, String codeSrc, String codeDest, BigDecimal montant) {
        log.info("CALCUL DU MONTANT DES FRAIS .....{}", mOperation.getMontantFrais());
        MFrais mFrais = calculFrais(new MParamFrais(codePv, mOperation.getTypeService(), canal, codeSrc, codeDest, montant, mOperation.getEmetteur() != null ? mOperation.getEmetteur() : null), TypeFrais.FRAIS);
        log.info("CALCUL DU MONTANT DES FRAIS  .....{}", Util.getZeroIfNull(mFrais.getFraisOperation()));
        log.info("MONTANT DE LA TAXE  .....{}", Util.getZeroIfNull(mFrais.getTaxeOPeration()));
        mOperation.setMontantFraisClient(Util.getZeroIfNull(mFrais.getFraisOperation()));
        if (mFrais.getResponsable() != null && !mFrais.getResponsable().equals("CLIENT")) {
            mOperation.setMontantFraisClient(BigDecimal.ZERO);
        }
        mOperation.setMontantFrais(Util.getZeroIfNull(mFrais.getFraisOperation()));
        mOperation.setMontantTaxe(Util.getZeroIfNull(mFrais.getTaxeOPeration()));
        return mOperation;
    }

    public MOperation getMontantCalculFrais(MOperation mOperation, String codePv, String canal, String codeSrc, String codeDest, BigDecimal montant) {
        log.info("CALCUL DU MONTANT DES FRAIS .....{}", mOperation.getMontantFrais());
        MFrais mFrais = calculFrais(new MParamFrais(codePv, mOperation.getTypeService(), canal, codeSrc, codeDest, montant, mOperation.getEmetteur() != null ? mOperation.getEmetteur() : null), TypeFrais.FRAIS);
        log.info("CALCUL DU MONTANT DES FRAIS  .....{}", Util.getZeroIfNull(mFrais.getFraisOperation()));
        log.info("MONTANT DE LA TAXE  .....{}", Util.getZeroIfNull(mFrais.getTaxeOPeration()));
        mOperation.setMontantFraisClient(Util.getZeroIfNull(mFrais.getFraisOperation()));
        if (mFrais.getResponsable() != null && !mFrais.getResponsable().equals("CLIENT")) {
            mOperation.setMontantFraisClient(BigDecimal.ZERO);
        }

        mOperation.setMontantFrais(Util.getZeroIfNull(mFrais.getFraisOperation()));
        mOperation.setMontantTaxe(Util.getZeroIfNull(mFrais.getTaxeOPeration()));
        MFrais mavoir = calculFrais(new MParamFrais(codePv, mOperation.getTypeService(), canal, codeSrc, codeDest, montant, mOperation.getEmetteur() != null ? mOperation.getEmetteur() : null), TypeFrais.AVOIR);
        if (mavoir.getResponsable() != null) {
            mOperation.setMontantAvoirClient(Util.getZeroIfNull(mavoir.getFraisOperation()));
        }
        return mOperation;
    }

    private MOperationFacture getMontantCalculFraisFact(MOperationFacture mOperation, String codePv, String canal, String codeSrc, String codeDest, BigDecimal montant) {
        MFrais mFrais = calculFrais(new MParamFrais(codePv, mOperation.getTypeService(), canal, codeSrc, codeDest, montant, mOperation.getEmetteur() != null ? mOperation.getEmetteur() : null), TypeFrais.FRAIS);
        mOperation.setMontantFraisClient(Util.getZeroIfNull(mFrais.getFraisOperation()));
        if (mFrais.getResponsable() != null && !mFrais.getResponsable().equals("CLIENT")) {
            mOperation.setMontantFraisClient(BigDecimal.ZERO);
        }
        mOperation.setMontantFrais(Util.getZeroIfNull(mFrais.getFraisOperation()));
        mOperation.setMontantTaxe(Util.getZeroIfNull(mFrais.getTaxeOPeration()));
        MFrais mavoir = calculFrais(new MParamFrais(codePv, mOperation.getTypeService(), canal, codeSrc, codeDest, montant, mOperation.getEmetteur() != null ? mOperation.getEmetteur() : null), TypeFrais.AVOIR);
        if (mavoir.getResponsable() != null) {
            mOperation.setMontantAvoirClient(Util.getZeroIfNull(mavoir.getFraisOperation()));
        }
        return mOperation;
    }

    public List<MParametre> populateMessage(Operation operation) {
        List<MParametre> context = new ArrayList<>();
        DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatHeure = DateTimeFormatter.ofPattern("HH:mm:ss");
        String dateTimeRequest = operation.getDateOperation().format(formatDate);
        String timeRequest = operation.getDateOperation().format(formatHeure);
        context.add(new MParametre("REFERENCE", operation.getCodeOperation(), 1));
        context.add(new MParametre("MONTANT", operation.getMontantOp().doubleValue() + "", 1));
        context.add(new MParametre("DATEOPERATION", dateTimeRequest, 1));
        context.add(new MParametre("HEUREOPERATION", timeRequest, 1));
        context.add(new MParametre("TYPESERVICE", operation.getTypeService().getDescription().toUpperCase(), 1));
        if (Util.isBiggerThan(Util.getZeroIfNull(operation.getMontantFraisClient()), BigDecimal.ZERO)) {
            context.add(new MParametre("FRAIS", Util.getZeroIfNull(operation.getMontantFraisClient()).add(Util.getZeroIfNull(operation.getMontantTaxe())).doubleValue() + "", 1));
            context.add(new MParametre("TOTAL", Util.getZeroIfNull(operation.getMontantFrais()).add(Util.getZeroIfNull(operation.getMontantOp())).add(Util.getZeroIfNull(operation.getMontantTaxe())).doubleValue() + "", 1));
        } else {
            context.add(new MParametre("FRAIS", BigDecimal.ZERO.doubleValue() + "", 1));
            context.add(new MParametre("TOTAL", Util.getZeroIfNull(operation.getMontantOp()) + "", 1));
        }

        if (operation.getEmetteur() != null) {
            if (operation.getEmetteur().getPersonne() != null) {
                if (operation.getEmetteur().getPersonne().getPersonnePhysique() != null) {
                    context.add(new MParametre("EMETTEUR", operation.getEmetteur().getPersonne().getTelephone() + " " + operation.getEmetteur().getPersonne().getPersonnePhysique().getNomClient(), 1));
                }
                if (operation.getEmetteur().getPersonne().getPersonneMorale() != null) {
                    context.add(new MParametre("EMETTEUR", operation.getEmetteur().getPersonne().getTelephone() + "-" + operation.getEmetteur().getPersonne().getPersonneMorale().getRaisonSociale(), 1));
                }

            }
        }
        if (operation.getBeneficaire() != null) {
            if (operation.getBeneficaire().getPersonne().getPersonnePhysique() != null) {
                context.add(new MParametre("BENEFICIAIRE", operation.getBeneficaire().getPersonne().getPersonnePhysique().getNomClient(), 1));
            }
            if (operation.getBeneficaire().getPersonne().getPersonneMorale() != null) {
                context.add(new MParametre("BENEFICIAIRE", operation.getBeneficaire().getPersonne().getTelephone() + "-" + operation.getBeneficaire().getPersonne().getPersonneMorale().getRaisonSociale(), 1));
            }

        }
        if (operation.getCodeSecret() != null) {
            context.add(new MParametre("CODERETRAIT", operation.getCodeSecret(), 1));
        }
        return context;
    }

    public List<MParametre> returnMParameter(Operation operation) {
        List<MParametre> mParametres = new ArrayList<>();
        if (operation != null) {
            mParametres.add(new MParametre("REFERENCE", operation.getCodeOperation(), 1));
            if (operation.getBeneficaire() != null && operation.getBeneficaire().getPersonne() != null) {
                mParametres.add(new MParametre("BENEFICIAIRE", operation.getBeneficaire().getPersonne().getTelephone(), 1));
            }
            if (operation.getEmetteur() != null && operation.getEmetteur().getPersonne() != null) {
                mParametres.add(new MParametre("EMETEUR", operation.getEmetteur().getPersonne().getTelephone(), 1));
            }
        }
        return mParametres;
    }

    @Transactional
    public EvenementOperation desactiveEvt(Long id) {
        EvenementOperation evt = evenementOperationRepository.findEvenementById(id, TypeEvenementOperation.DOIT.name());
        if (evt != null) {
            evt.setEstLevee(1);
            evenementOperationRepository.save(evt);
        }
        return evt;
    }

    @Transactional
    public EvenementOperation desactiveEvt(EvenementOperation evt) {
        evt.setEstLevee(1);
        EvenementOperation event = evenementOperationRepository.save(evt);
        return evt;
    }

    @Transactional
    public EvenementOperation saveEvtOperation(Operation operation) {
        EvenementOperation evenementOperation = new EvenementOperation();
        BigDecimal moinsUn = BigDecimal.valueOf(-1L);
        BigDecimal montantEve = operation.getMontantOp().add(operation.getMontantFrais()).add(operation.getMontantTaxe()).multiply(moinsUn);
        evenementOperation.setOperation(operation);
        evenementOperation.setEstLevee(0);
        evenementOperation.setCompte(operation.getCompteSrc());
        evenementOperation.setMontant(montantEve);
        EvenementOperation evt = evenementOperationRepository.save(evenementOperation);
        return evt;
    }

    /**
     * Faire un retour **
     */
    public boolean doOperationExtourne(OperationControlDTO operationControlDTO) throws IOException, JAXBException {
        Message msg = apiService.checkFunction(operationControlDTO.getCodeFse(), ServiceConstants.CHECKTRANSACTIONSTATUS);
        if (msg != null && msg.getCodeErr() != null && msg.getCodeErr().equals("0")) {
            Message statusMsg = apiService.checkStatus(operationControlDTO.getCodeFse(), operationControlDTO.getExtraData());
            if (statusMsg != null && !statusMsg.getCodeErr().equals("0")) {
                LogOperation logOperation = operationService.getLogOperation(operationControlDTO.getCodeOperation());
                if (logOperation != null && logOperation.getCodeOperationEx() != null) {
                    apiService.sendAck(logOperation.getCodeOperationEx().toString());
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public EvenementOperation insertEvenementOperation(BigDecimal montant, Compte compte) {
        EvenementOperation evenementOperation = new EvenementOperation();
        BigDecimal moinsUn = BigDecimal.valueOf(-1L);
        BigDecimal montantEve = montant.multiply(moinsUn);
        evenementOperation.setEstLevee(0);
        evenementOperation.setTypeEvenement(TypeEvenementOperation.DOIT.toString());
        evenementOperation.setCompte(compte);
        evenementOperation.setMontant(montantEve);
        evenementOperation = evenementOperationRepository.save(evenementOperation);
        return evenementOperation;

    }

    public void checkPing(String codeFse, List<MParametre> listparam) throws IOException, JAXBException {
        String error = "";
        Message msgPing = apiService.checkFunction(codeFse, ServiceConstants.PING);
        if (msgPing != null && msgPing.getCodeErr() != null && msgPing.getCodeErr().equals("0")) {
            Message msg = apiService.checkPing(codeFse, listparam);
            if (msg != null) {
                if (msg.getCodeErr() == null || (msg.getCodeErr() != null && msg.getCodeErr().equals("-1"))) {
                    if (msg.getMsg() != null) {
                        error = msg.getMsg();
                    }
                    throw new CustomParameterizedException(error);
                }
            } else {
                throw new CustomParameterizedException(error);
            }
        }

    }

    public void setCardlessOperation(Operation operation, UniteOrganisation fseSrc, Optional<PointVente> pointVenteExist) throws IOException, JAXBException, NoSuchAlgorithmException {

        log.debug("DEBUT DU SERVICE CARDLESS .. " + operation.getCodeOperation());
        DecimalFormat df = new DecimalFormat("###");
        String description;
        // operation = operationService.comptabilisationOperationCardless(operation, pointVenteExist.get());
        /**
         * Recupération du code fse du fournisseur CardLess*
         */
        String codeFseDef = fseSrc.getCodeFse();
        String codeFseCardless = getCardLessFseCode(codeFseDef);
        String entityCode = getCardLessEntiteCode(codeFseDef);
        String error = "le système distant est inaccessible veuillez reessayer plus tard";
        /* Si existe */
        log.debug("VERIFICATION DU STATUT DU SERVEUR .. " + operation.getCodeOperation());
        List<MParametre> listMp = new ArrayList();
        MParametre mp = new MParametre("ENTITYCODE", entityCode);
        listMp.add(mp);
        Message msg = apiService.checkPing(codeFseCardless, listMp);
        log.debug("FIN DE LA VERIFICATION DU STATUT DU SERVEUR .. " + operation.getCodeOperation());

        if (msg != null) {
            if (msg.getCodeErr() == null || (msg.getCodeErr() != null && msg.getCodeErr().equals("-1"))) {
                if (msg.getMsg() != null) {
                    error = msg.getMsg();
                }
                log.debug(error + " " + operation.getCodeOperation());
                throw new CustomParameterizedException(error);
            }
        } else {
            log.debug("le système distant est inaccessible!! " + operation.getCodeOperation());
            throw new CustomParameterizedException(error);
        }
        log.debug("ENREGISTREMENT DE L'EVENEMENT  .." + operation.getCodeOperation());
        EvenementOperation evt = saveEvtOperation(operation);
        com.m2i.omnicanal.outils.restclient.model.Message result = null;
        //      TransactionComptePti trans = transactionComptePtiService.findByOperationAndCompteAutre(oper.getCodeOperation(), operation.getCompteSrc().getCompteAutre().getId());
        //if (oper != null && oper.getStatut().equals(StatutOperation.PAYE)) {
        /* Appel au cardless*/
        if (codeFseCardless != null && entityCode != null) {
            log.debug("GENERATION DU CODE CARDLESS .." + operation.getCodeOperation());
            result = apiService.setCardLessTransaction(
                    operation.getEmetteur().getPersonne().getTelephone(),
                    operation.getEmetteur().getPersonne().getTelephone(),
                    df.format(operation.getMontantOp().doubleValue()),
                    operation.getCodeOperation(),
                    entityCode,
                    codeFseCardless);
        } else {
            result = apiService.setCardLessTransaction(operation.getEmetteur().getPersonne().getTelephone(),
                    df.format(operation.getMontantOp().doubleValue()),
                    operation.getCodeOperation(),
                    "CM");
        }
        log.debug("FIN DE GENERATION DU CODE CARDLESS .." + operation.getCodeOperation());
        mp = new MParametre("REFERENCE", operation.getCodeOperation());
        listMp.add(mp);
        log.debug("VERIFICATION DU STATUT DE REQUETE  .." + operation.getCodeOperation());
        Message msgStatut = apiService.checkStatus(codeFseCardless, listMp);
        log.debug("FIN DE LA VERIFICATION  DU STATUT DE REQUETE  .." + operation.getCodeOperation());
        if (msgStatut != null && msgStatut.getCodeErr().equals("0")) {
            log.debug("DEBUT DE LA COMPTABILISATION   .." + operation.getCodeOperation());
            if (!operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.VENTE_DEPOSIT_CARDLESS))) {
                Operation oper = operationService.comptabilisationOperationCardless(operation, pointVenteExist.get());
                if (oper != null && oper.getStatut().equals(StatutOperation.PAYE)) {
                    description = getDescription(operation);
                    operation.setDescription(description);
                    operation.etape(Etape.PROCEED);
                    log.debug("FIN DE LA COMPTABILISATION  .." + operation.getCodeOperation());
                    log.debug("LEVEE DE L'EVENEMENT  .." + operation.getCodeOperation());
                    desactiveEvt(evt);
                    operation.setCodeSecret(result.getMsg());
                    operation = operationRepository.save(operation);
                } else {
                    log.debug("ERREUR LORS DE LA COMPTABILISATION  .." + operation.getCodeOperation());
                    operation.setStatut(StatutOperation.AMBIGUE);

                    operation = operationRepository.save(operation);
                    throw new CustomSettlementException("Le code de retrait n'a pas pu être généré!");

                }
            } else {
                desactiveEvt(evt);
                operation.setCodeSecret(result.getMsg());
                operation = operationRepository.save(operation);
            }

        } else {
            log.debug("ERREUR LORS DE LA COMPTABILISATION  .." + operation.getCodeOperation());
            log.debug("LEVEE DE L'EVENEMENT  .." + operation.getCodeOperation());
            desactiveEvt(evt);
            throw new CustomParameterizedException("le système distant est inaccessible!!");
        }

        log.debug("FIN  DU SERVICE CARDLESS .. " + operation.getCodeOperation());

    }

    @Transactional(rollbackFor = CustomParameterizedException.class, noRollbackFor = CustomSettlementException.class)
    //  @HystrixCommand(fallbackMethod = "defautMessage")
    public MResponseOperation doOperation(MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("DEBUT DE L'OPERATION" + mOperation.getIdentifiant());
        Operation operation = new Operation();
        MResponseOperation retourMsg = new MResponseOperation();
        String destinataireMsg;
        Boolean debitEstPossible = true;
        UniteOrganisation organisation = null;
        String codeUo = "";
        String description = "";
        BigDecimal montantTaxeClient = BigDecimal.ZERO;
        try {
            StopWatch stopWatch = new StopWatch("Measure Code Execution");
            stopWatch.start("1. Task");
            PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());
            Optional<PointVente> pointVenteExist = Optional.of(opt);
            stopWatch.stop();
            stopWatch.start("2. Task Recherche canal service");

            if (mOperation.getCanal() == null) {
                showMessage("-1", "Le canal est obligatoire.", null);
            }
            if (mOperation.getCodePv() == null) {
                showMessage("-1", "Le point de vente est introuvable.", null);
            }
            if (mOperation.getTypeService() == null) {
                showMessage("-1", "Le type de service est obligatoire.", null);
            }
            if (mOperation.getMotif() == null) {
                showMessage("-1", "Le motif est obligatoire", null);
            }

            //   Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
            Canal canal = canalService.findCanal(mOperation.getCanal());
            operation = new Operation();
            TypeService typeService = typeServiceService.findByService(mOperation.getTypeService());
            if (pointVenteExist.isPresent()) {
                organisation = pointVenteExist.get().getUniteOrganisation();
                organisation = uniteOrganisationService.findOneById(organisation.getId());
                codeUo = organisation.getCodeUO();
            }
            MInfos minfo = null;
            String codeUoAcq;
            if (mOperation.getCodeUoAcq() == null) {
                codeUoAcq = organisation.getCodeUO();
            } else {
                codeUoAcq = mOperation.getCodeUoAcq();
            }

            stopWatch.stop();
            stopWatch.start("3. Calcul frais");
            mOperation = getMontantCalculFrais(mOperation, mOperation.getCodePv(), mOperation.getCanal(), mOperation.getCodeUoEm(), codeUoAcq, mOperation.getMontant());
            stopWatch.stop();

            if (pointVenteExist.isPresent() && typeService != null && canal != null) {
                log.info("Traitement de l'operation actuel  .....");

                User user = userService.findByPointVente(pointVenteExist.get());
                //  operationService.userCanDoOperations(user);
                operationService.verifAutorisationOfTypeService(typeService, organisation);
                ZonedDateTime dateTime = ZonedDateTime.now();
                if (mOperation.getMotif() != null) {

                    MotifOperation motifOperation = motifOperationService.findByCode(mOperation.getMotif());

                    //    MotifOperation motifOperation = motifOperationRepository.findByCode(mOperation.getMotif());
                    if (motifOperation != null) {
                        operation.setMotifOperation(motifOperation);
                    } else {

                        return showMessage("-1", "Le motif est obligatoire", null);
                    }
                }
                if (Util.isBiggerThan(mOperation.getMontantFraisClient(), BigDecimal.ZERO)) {
                    montantTaxeClient = mOperation.getMontantTaxe();
                }

                operation.montantOp(mOperation.getMontant())
                        .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                        .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()))
                        .typeService(typeService)
                        .pointVente(pointVenteExist.get())
                        .statut(StatutOperation.ENATTENTE)
                        .canal(canal)
                        .description(mOperation.getDescription())
                        .codeOperation(RandomUtil.getCodeOperation(dateTime, codeUo))
                        .dateOperation(dateTime)
                        .montantOpEnDevise(mOperation.getMontant())
                        .montantAvoirClient(Util.getZeroIfNull(mOperation.getMontantAvoirClient()))
                        .user(user);
                log.info("RECHERCHE PERSONNE");
                stopWatch.start("4. Recherche personne");
                operation = saveInformationPersonne(mOperation.getBeneficiaire(), Boolean.TRUE, operation);
                // if (!mOperation.getTypeService().equals(TypeServiceLibelle.CASH_ACCOUNT.toString())) {
                operation = saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operation);
                stopWatch.stop();
                log.info("FIN DE LA RECHERCHE PERSONNE");
                //}
                /*le compte de destination n'intervient pas ï¿½ ce niveau*/
                if (!mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_AIRTIME.toString())
                        && !mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_BIEN_PAR_CODE.toString())
                        && !mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_BIEN.toString())
                        && !mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_INTERNET.toString())
                        && !mOperation.getTypeService().equals(TypeServiceLibelle.RETRAIT_CARDLESS.toString())
                        && !mOperation.getTypeService().equals(TypeServiceLibelle.ACCOUNT_CASH.toString())
                        && !mOperation.getTypeService().equals(TypeServiceLibelle.TRANSFERT_MASSE_CASH.toString())) {
                    log.info("RECHERCHE DU COMPTE DE DESTINATION ");

                    CompteOperationDTO compteOperationDTO = getCompte(mOperation.getBeneficiaire().getCompte(), operation.getBeneficaire().getPersonne(), mOperation.getApplication(), Boolean.TRUE, mOperation.getPreautorisaton());
                    if (compteOperationDTO != null) {
                        operation.compteDest(compteOperationDTO.getCompte());
                        operation.setCompteExterneDest(compteOperationDTO.getCompteExterne());
                        operation.setOperationExterne(compteOperationDTO.getOperationExterne());
                    }
                    log.info("FIN DE LA RECHERCHE DU COMPTE DE DESTINATION ");

                    //  operation.compteDest(getCompte(mOperation.getBeneficiaire().getCompte(), operation.getBeneficaire().getPersonne(), mOperation.getApplication(), Boolean.TRUE));
                    log.info("FIN DE LA RECHERCHE DU COMPTE DE DESTINATION ");
                }


                /*  Lï¿½ on n'est dans une situation de cash to account**/
                if (!mOperation.getTypeService().equals(TypeServiceLibelle.CASH_ACCOUNT.toString()) && !mOperation.getTypeService().equals(TypeServiceLibelle.CASH_ACCOUNT_AGENT.toString())) {
                    log.info("VERIFICATION DES AUTORISATIONS ");
                    verifIfAutoriser(mOperation.getEmetteur().getCompte().getNumeroCompte(), mOperation.getActeur(), mOperation.getIdentifiant(), mOperation.getEmetteur().getTitulaire().getCodeTelephone());
                    log.info("FIN VERIFICATION DES AUTORISATIONS ");
                    log.info("RECHERCHE DU COMPTE DE SOURCE ");

                    CompteOperationDTO compteOperationDTO = getCompte(mOperation.getEmetteur().getCompte(), operation.getEmetteur().getPersonne(), mOperation.getApplication(), Boolean.FALSE, mOperation.getPreautorisaton());
                    if (compteOperationDTO != null) {
                        operation.compteSrc(compteOperationDTO.getCompte());
                        operation.setCompteExterneSrc(compteOperationDTO.getCompteExterne());
                        operation.setOperationExterne(compteOperationDTO.getOperationExterne());
                    }

                    log.info("FIN RECHERCHE DU COMPTE DE SOURCE ");
                    log.info("CONTROLE DU SOLDE ");
                    if (operation.getCompteSrc() != null) {
                        if (operation.getCompteSrc().getCompteAutre() != null) {
                            debitEstPossible = compteService.peutDebiterCompte(operation.getCompteSrc(), Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(montantTaxeClient));
                        } else if (operation.getCompteSrc().getCompteBanque() != null || operation.getCompteSrc().getCarte() != null) {

                            Balance bal = new Balance();
                            if (operation.getCompteSrc().getCompteBanque() != null) {
                                bal = apiService.getSolde(operation.getCompteSrc().getValeurCompte(), operation.getCompteSrc().getCompteBanque().getCodeAgence(), operation.getCompteSrc().getCompteBanque().getCodeFse());
                            } else if (operation.getCompteSrc().getCarte() != null) {
                                bal = apiService.getSolde(operation.getCompteSrc().getValeurCompte(), operation.getCompteSrc().getCarte().getLast4Digit(), operation.getCompteSrc().getCarte().getCodeFse());
                            }

                            Double solde = bal.getSolde();
                            BigDecimal soldeEnBigDecimal = new BigDecimal(solde);
                            BigDecimal mtop = Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(montantTaxeClient);
                            if (Util.isBiggerOrEqualThan(soldeEnBigDecimal, mtop)) {
                                debitEstPossible = true;
                            } else {
                                debitEstPossible = false;
                            }

                        }
                    } else {
                        throw new CustomParameterizedException("Compte introuvable ou désactivé.");
                    }

                    if (!debitEstPossible) {
                        log.info("Solde insuffisant");
                        throw new CustomParameterizedException("Solde insuffisant.");
                        //    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                        //return showMessage("-1", "Solde insuffisant.",null);
                    }
                    log.info("FIN CONTROLE DU SOLDE ");
                }
//                else {
//                    /**
//                     * Vérificiation du compte *
//                     */
//                    verifIfAutoriser(mOperation.getEmetteur().getCompte().getNumeroCompte(), TypeProfilActeur.CLIENT.name(), mOperation.getEmetteur().getTitulaire().getTelephone(), mOperation.getEmetteur().getTitulaire().getCodeTelephone());
//                }
                //Service de comptabilisation
                log.info("ENREGISTREMENT DE L'OPERATION ");
                if (mOperation.getTypeService().equals(TypeServiceLibelle.ACCOUNT_ACCOUNT.toString()) || mOperation.getTypeService().equals(TypeServiceLibelle.BANK_TO_WALLET.toString()) || mOperation.getTypeService().equals(TypeServiceLibelle.WALLET_TO_BANK.toString()) || mOperation.getTypeService().equals(TypeServiceLibelle.CARD_TO_WALLET.toString()) || mOperation.getTypeService().equals(TypeServiceLibelle.WALLET_TO_CARD.toString())) {
                    String natureCompteSource = "";
                    log.info("Operation  ACCOUNT_ACCOUNT en cours.....");
                    UniteOrganisation fseSrc = null;
                    UniteOrganisation fseDest = null;
                    Compte cmpteClientDest = null;
                    Compte cmptClientSrc = null;
                    cmpteClientDest = operationService.getCompteExterne(operation, true);
                    cmptClientSrc = operationService.getCompteExterne(operation, false);

                    if (cmpteClientDest != null) {
                        if (cmpteClientDest.getCompteBanque() != null) {
                            fseDest = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(cmpteClientDest.getCompteBanque().getCodeFse());
                        } else if (operation.getCompteDest().getCompteAutre() != null) {
                            fseDest = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(cmpteClientDest.getCompteAutre().getCodeFse());
                        } else if (operation.getCompteDest().getCarte() != null) {
                            fseDest = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(cmpteClientDest.getCarte().getCodeFse());
                        }
                    } else {
                        throw new CustomParameterizedException("Le compte de destination est introuvable");
                    }

                    if (cmptClientSrc.getCompteBanque() != null) {
                        natureCompteSource = "WALLET";
                        fseSrc = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(cmptClientSrc.getCompteBanque().getCodeFse());
                    } else if (operation.getCompteSrc().getCompteAutre() != null) {
                        natureCompteSource = "BANQUE";
                        fseSrc = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(cmptClientSrc.getCompteAutre().getCodeFse());
                    } else if (cmptClientSrc.getCarte() != null) {
                        natureCompteSource = "CARTE";
                        fseSrc = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(cmptClientSrc.getCarte().getCodeFse());

                    }

                    operation.uoEmetteur(fseSrc)
                            .statut(StatutOperation.ENATTENTE)
                            .uoAcquereur(fseDest)
                            .description(mOperation.getDescription())
                            .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                            .montantFraisClient(Util.getZeroIfNull(mOperation.getMontantFraisClient()))
                            .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()));

                    //Calcul des taxes de l'operation en cours
                    //   operation = treatTaxeAndSaveOp(mOperation, operation, organisation);
                    log.info("CONTROLE DU SEUIL ");
                    operation = controleSeuil(operation);
                    log.info("FIN CONTROLE DU SEUIL ");
                    description = getDescription(operation);
                    if (description != null) {
                        if (mOperation.getDescription() != null) {
                            description = mOperation.getDescription() + " " + description;
                        }
                        operation.setDescription(description);
                    }

                    operation = operationRepository.save(operation);
                    log.info("SUITE ENREGISTREMENT DE L'OPERATION ");
                    if (!operation.getStatut().equals(StatutOperation.ENATTENTE_VALIDATION)) {
                        operationService.calculRepartitionFrais(operation, fseSrc.getId(), fseDest.getId());
                        // operationAuditService.saveOpAudit(operation, null, StatutOperation.PAYE);
                        stopWatch.start("1. compta");
                        log.info("CODE OPERATION {}", operation.getCodeOperation());

                        log.info("DESCRIPTION {}", operation.getDescription());

                        operationService.comptabilisationOperationMad(operation, pointVenteExist.get(), null);
                        operationAuditService.rechercheSuivi(operation);
                        stopWatch.stop();
                        System.out.println(stopWatch.prettyPrint());
                        return showMessage("0", "Operation effectuee avec succes.", operation);
                    } else {
                        log.info("Votre Operation est soumise a  validation" + operation.getCodeOperation());
                        return showMessage("0", "Votre operation est soumise a  validation", operation);
                    }

                } else if (mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_INTERNET.toString()) || mOperation.getTypeService().equals(TypeServiceLibelle.ACCOUNT_CASH.toString())
                        || mOperation.getTypeService().equals(TypeServiceLibelle.RETRAIT_CARDLESS.toString())
                        || mOperation.getTypeService().equals(TypeServiceLibelle.TRANSFERT_MASSE_CASH.toString())) { //ACCOUNT_CASH
                    log.info("operation de type ACCOUNT_CASH Ou PAIEMENT INTERNET OU RETRAIT CARD LESS en cours.....");
                    UniteOrganisation fseSrc;
                    if (operation.getCompteSrc().getCompteBanque() != null) {
                        fseSrc = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(operation.getCompteSrc().getCompteBanque().getCodeFse());
                    } else {
                        fseSrc = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(operation.getCompteSrc().getCompteAutre().getCodeFse());
                    }

                    //Pays de paiement
                    if (mOperation.getCodePays() != null) {
                        if (mOperation.getCodeUo() != null) {
                            UniteOrganisation uoPaiement = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(mOperation.getCodeUo(), false);
                            if (uoPaiement == null) {
                                return new MResponseOperation("-1", "Le code du lieu de paiement est incorrect." + mOperation.getCodeUo(), null);
                            }
                            operation.uoAcquereur(uoPaiement);
                            if (mOperation.getCodePays() != null) {
                                log.debug("Verification du pays de paimement ..%%");
                                UniteOrganisation paysPaiement = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(mOperation.getCodePays(), false);
                                TypeUniteOrganisation typeUniteOrganisation;
                                if (paysPaiement != null) {

                                    //typeUniteOrganisation = typeUniteOrganisationService.findByOne(paysPaiement.getTypeUO().getId());
                                    if (paysPaiement.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
                                        //  UniteOrganisation uoPaysCodeAgent = uniteOrganisationService.findUoParentByType(uoPaiement, TypeUO.PAYS);
                                        UniteOrganisation uoPaysCodeAgent;
                                        if (uoPaiement.getUoPaysId() != null) {
                                            uoPaysCodeAgent = uniteOrganisationService.findOneById(uoPaiement.getUoPaysId());
                                        } else {
                                            uoPaysCodeAgent = uoPaiement;
                                        }

                                        if (uoPaysCodeAgent != null && !uoPaysCodeAgent.getId().equals(paysPaiement.getId())) {

                                            return new MResponseOperation("-1", "Le lieu de paiement est incorrect par rapport au pays choisi.", null);
                                        }
                                    }
                                }

                            } else {
                                return new MResponseOperation("-1", "Le pays de paiement est obligatoire.", null);
                            }

                        } else {
                            log.info("Code pays : " + mOperation.getCodePays());
                            UniteOrganisation paysPaiement = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(mOperation.getCodePays(), false);
                            if (paysPaiement != null) {
                                operation.uoAcquereur(paysPaiement);
                            } else {
                                return new MResponseOperation("-1", "Le pays de paiement est obligatoire.", null);
                            }
                        }
                        description = getDescription(operation);
                        //Verification de la validitï¿½ du lieu de paiement
                        operation.uoEmetteur(fseSrc)
                                .description(mOperation.getDescription())
                                .statut(StatutOperation.ENATTENTE)
                                .description(description)
                                .montantFraisClient(Util.getZeroIfNull(mOperation.getMontantFraisClient()))
                                .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                                .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()));

                        //Calcul des taxes de l'operation en cours
                        //  operation = treatTaxeAndSaveOp(mOperation, operation, organisation);
                        operation = operationRepository.save(operation);
                        operation = controleSeuil(operation);
                        operation.etape(Etape.PROCEED);
                        operationService.calculRepartitionFrais(operation, fseSrc.getId(), operation.getUoEmetteur().getId());
                        operationAuditService.rechercheSuivi(operation);
                        //*Pour le cas du paiement internet et le cardless le compte n'est pas systématiquement débité **//
                        if (mOperation.getTypeService().equals(TypeServiceLibelle.RETRAIT_CARDLESS.toString()) || mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_INTERNET.toString())) {
                            if (mOperation.getTypeService().equals(TypeServiceLibelle.PAIEMENT_INTERNET.toString())) {
                                log.debug("DEBUT DU SERVICE PAIEMENT_INTERNET .. " + operation.getCodeOperation());
                                EvenementOperation evenementOperation = new EvenementOperation();
                                BigDecimal moinsUn = BigDecimal.valueOf(-1L);
                                BigDecimal montantEve = operation.getMontantOp().add(operation.getMontantFrais()).add(operation.getMontantTaxe()).multiply(moinsUn);
                                evenementOperation.setOperation(operation);
                                evenementOperation.setEstLevee(0);
                                evenementOperation.setTypeEvenement(TypeEvenementOperation.DOIT.toString());
                                evenementOperation.setCompte(operation.getCompteSrc());
                                evenementOperation.setMontant(montantEve);
                                evenementOperationRepository.save(evenementOperation);
                                String otps[] = RandomUtil.generateOtp();
                                String codeRetrait = otps[1];
                                log.info("Generation du code retrait " + codeRetrait);
                                int dureeExpire = operationService.getDureeExpire();
                                operation.commentaire(Base64.encodeBase64String(codeRetrait.getBytes()))
                                        .codeRetrait(RandomUtil.getMd5Code(codeRetrait))
                                        .statut(StatutOperation.PAYE)
                                        .dateExpiration(LocalDate.now().plusDays(dureeExpire))
                                        .duree(dureeExpire)
                                        .secret(otps[0]);
                                operation.setCodeSecret(codeRetrait);
                                operation = operationRepository.save(operation);
                                log.debug("FIN DU SERVICE PAIEMENT_INTERNET .. " + operation.getCodeOperation());
                            } else if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.RETRAIT_CARDLESS))) {
                                log.debug("DEBUT DU SERVICE CARDLESS .. " + operation.getCodeOperation());
                                DecimalFormat df = new DecimalFormat("###");

                                // operation = operationService.comptabilisationOperationCardless(operation, pointVenteExist.get());
                                /**
                                 * Recupération du code fse du fournisseur
                                 * CardLess*
                                 */
                                String codeFseDef = fseSrc.getCodeFse();
                                String codeFseCardless = getCardLessFseCode(codeFseDef);
                                String entityCode = getCardLessEntiteCode(codeFseDef);
                                String error = "le système distant est inaccessible veuillez reessayer plus tard";
                                /* Si existe */
                                log.debug("VERIFICATION DU STATUT DU SERVEUR .. " + operation.getCodeOperation());
                                List<MParametre> listMp = new ArrayList();
                                MParametre mp = new MParametre("ENTITYCODE", entityCode);
                                listMp.add(mp);
                                Message msg = apiService.checkPing(codeFseCardless, listMp);
                                log.debug("FIN DE LA VERIFICATION DU STATUT DU SERVEUR .. " + operation.getCodeOperation());
                                if (msg != null) {
                                    if (msg.getCodeErr() == null || (msg.getCodeErr() != null && msg.getCodeErr().equals("-1"))) {
                                        if (msg.getMsg() != null) {
                                            error = msg.getMsg();
                                        }
                                        log.debug(error + " " + operation.getCodeOperation());
                                        throw new CustomParameterizedException(error);
                                    }
                                } else {
                                    log.debug("le système distant est inaccessible!! " + operation.getCodeOperation());
                                    throw new CustomParameterizedException(error);
                                }
                                log.debug("ENREGISTREMENT DE L'EVENEMENT  .." + operation.getCodeOperation());
                                EvenementOperation evt = saveEvtOperation(operation);
                                com.m2i.omnicanal.outils.restclient.model.Message result = null;
                                //      TransactionComptePti trans = transactionComptePtiService.findByOperationAndCompteAutre(oper.getCodeOperation(), operation.getCompteSrc().getCompteAutre().getId());
                                //if (oper != null && oper.getStatut().equals(StatutOperation.PAYE)) {
                                /* Appel au cardless*/
                                if (codeFseCardless != null && entityCode != null) {
                                    log.debug("GENERATION DU CODE CARDLESS .." + operation.getCodeOperation());
                                    result = apiService.setCardLessTransaction(
                                            operation.getEmetteur().getPersonne().getTelephone(),
                                            operation.getEmetteur().getPersonne().getTelephone(),
                                            df.format(operation.getMontantOp().doubleValue()),
                                            operation.getCodeOperation(),
                                            entityCode,
                                            codeFseCardless);
                                } else {
                                    result = apiService.setCardLessTransaction(operation.getEmetteur().getPersonne().getTelephone(),
                                            df.format(operation.getMontantOp().doubleValue()),
                                            operation.getCodeOperation(),
                                            "CM");
                                }
                                if (result != null && result.getCodeErr().equals("0")) {
                                    log.debug("FIN DE GENERATION DU CODE CARDLESS .." + operation.getCodeOperation());
                                    mp = new MParametre("REFERENCE", operation.getCodeOperation());
                                    listMp.add(mp);
                                    log.debug("VERIFICATION DU STATUT DE REQUETE  .." + operation.getCodeOperation());
                                    Message msgStatut = apiService.checkStatus(codeFseCardless, listMp);
                                    log.debug("FIN DE LA VERIFICATION  DU STATUT DE REQUETE  .." + operation.getCodeOperation());
                                    if (msgStatut != null && msgStatut.getCodeErr().equals("0")) {
                                        log.debug("DEBUT DE LA COMPTABILISATION   .." + operation.getCodeOperation());
                                        Operation oper = operationService.comptabilisationOperationCardless(operation, pointVenteExist.get());
                                        if (oper != null && oper.getStatut().equals(StatutOperation.PAYE)) {
                                            description = getDescription(operation);
                                            operation.setDescription(description);
                                            operation.etape(Etape.PROCEED);
                                            log.debug("FIN DE LA COMPTABILISATION  .." + operation.getCodeOperation());
                                            log.debug("LEVEE DE L'EVENEMENT  .." + operation.getCodeOperation());
                                            desactiveEvt(evt);
                                            operation.setCodeSecret(result.getMsg());
                                            operation = operationRepository.save(operation);
                                        } else {
                                            log.debug("ERREUR LORS DE LA COMPTABILISATION  .." + operation.getCodeOperation());
                                            operation.setStatut(StatutOperation.AMBIGUE);

                                            operation = operationRepository.save(operation);
                                            throw new CustomSettlementException("Le code de retrait n'a pas pu être généré!");

                                        }
                                    } else {
                                        log.debug("ERREUR LORS DE LA COMPTABILISATION  .." + operation.getCodeOperation());
                                        log.debug("LEVEE DE L'EVENEMENT  .." + operation.getCodeOperation());
                                        desactiveEvt(evt);
                                        throw new CustomSettlementException("le système distant est inaccessible!!");
                                    }
                                } else {
                                    throw new CustomSettlementException("le système distant est inaccessible!!");
                                }

                                log.debug("FIN  DU SERVICE CARDLESS .. " + operation.getCodeOperation());

                            }

                        } else {
                            stopWatch.start("3. comptabilisationOperationMad");
                            operation = operationService.comptabilisationOperationMad(operation, pointVenteExist.get(), null);
                            stopWatch.stop();
                        }
                        log.info("{}" + stopWatch.prettyPrint());
                        //  mOperation.setCardLess(Boolean.FALSE);
                        /*
                        if (mOperation.getTypeService().equals(TypeServiceLibelle.RETRAIT_CARDLESS.toString())) {
                            log.info("Operation cardLess en cours ....");
                            MOperation mOpRetrait = new MOperation();
                            mOpRetrait.setCodeRetrait(new String(Base64.decodeBase64(operation.getCommentaire())));
                            mOpRetrait.setCanal(mOperation.getCanal());
                            mOpRetrait.setCodePv(mOperation.getCodePv());
                            mOpRetrait.setBeneficiaire(mOperation.getEmetteur());
                            return doOperationRetrait(mOpRetrait);
                        }*/
                        operationAuditService.rechercheSuivi(operation);
                        log.info("Operation effectue avec succes...." + operation.getCodeOperation());
                        return showMessage("0", "Operation effectuee avec succes.", operation);
                    } else {
                        return showMessage("-1", "Le pays de paiement est obligatoire.", null);
                    }
                } else if (mOperation.getTypeService().equals(TypeServiceLibelle.CASH_ACCOUNT.toString()) || mOperation.getTypeService().equals(TypeServiceLibelle.CASH_ACCOUNT_AGENT.toString())) {
                    log.info("Operation  CASH_ACCOUNT en cours.....");
                    UniteOrganisation fseSrc = null;
                    UniteOrganisation fseDest = null;
                    /*s'assurer que le compte d'opï¿½ration est bien le compte de l'organisation*/
                    Compte compteOp = compteService.getCompteOperationActeur(organisation, "CPTEOPERATIONACTEURPTI");
                    if (compteOp != null) {
                        verifIfAutoriser(compteOp.getValeurCompte(), mOperation.getActeur(), mOperation.getIdentifiant(), null);
                    }
                    operationService.verifFonds(Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(Util.getZeroIfNull(montantTaxeClient)).add(Util.getZeroIfNull(operation.getMontantOp())), organisation, EtiquetteCompteCode.valueOf(compteOp.getEtiquette()));
                    if (operation.getCompteDest() != null) {
                        if (operation.getCompteDest().getCompteBanque() != null) {
                            fseDest = uniteOrganisationService.findByCodeFSE(operation.getCompteDest().getCompteBanque().getCodeFse());
                        } else {
                            fseDest = uniteOrganisationService.findByCodeFSE(operation.getCompteDest().getCompteAutre().getCodeFse());
                        }
                    }
                    if (mOperation.getCodeUo() != null) {
                        /**
                         * *Retourner le FSE SOURCE de l'UO
                         */
                        fseSrc = uniteOrganisationService.findByCode(mOperation.getCodeUo());
                        if (fseSrc == null) {
                            return new MResponseOperation("-1", "Le code du lieu de paiement est incorrect." + mOperation.getCodeUo(), null);
                        }
                        description = getDescription(operation);
                        operation.setDescription(description);
                        operation.uoEmetteur(fseSrc)
                                .uoAcquereur(fseDest)
                                .statut(StatutOperation.ENATTENTE_VALIDATION)
                                .montantFraisClient(Util.getZeroIfNull(mOperation.getMontantFraisClient()))
                                .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                                .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()));

                        operation = operationRepository.save(operation);

                    } else {
                        return new MResponseOperation("-1", "Le code agent est incorrect", null);
                    }
                    //Calcul des taxes de l'operation en cours
                    //  operation = treatTaxeAndSaveOp(mOperation, operation, organisation);
                    operation.etape(Etape.PROCEED);
                    operation = operationRepository.save(operation);
                    operation = controleSeuil(operation);
                    operationService.calculRepartitionFrais(operation, fseSrc.getId(), fseDest.getId());
                    //  operationAuditService.saveOpAudit(operation, null, StatutOperation.PAYE);
                    operationService.comptabilisationCashAccount(operation.getId(), StatutOperation.VALIDE, "");
                    operationAuditService.rechercheSuivi(operation);
                    log.info("Operation effectue avec succes...." + operation.getCodeOperation());
                    return showMessage("0", "Operation effectuee avec succes.", operation);

                }
            }

            if (typeService == null) {

                return showMessage("-1", "le service est indisponible actuellement, veuillez reessayer plus tard ", null);
            }

            return showMessage("-1", "Le point de vente est introuvable.", null);
        } catch (CustomParameterizedException e) {
            operationService.sendAck(operation, e.getMessage());
            boolean exceptionOccured = true;

            operationService.sendAckToOmnicanal(operation, e.getMessage());
            CodeErreur codeErreur = operationService.verifMessageRetourOmnicanal(operation);
            if (codeErreur != null && !codeErreur.getCode().equals("-1")) {
                log.info("Une erreur est survenue lors de l'operation...." + codeErreur.getCode() + "- " + codeErreur.getLibelle());
                retourMsg = new MResponseOperation(codeErreur.getCode(), codeErreur.getLibelle(), null);

            } else {
                retourMsg = new MResponseOperation("-1", e.getMessage(), null);
            }
            //    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return retourMsg;

        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operation, e.getMessage());
            //  TransactionAspectSupport.currentTransactionStatus().flush();
            return showMessage("-1", e.getMessage(), null);
        }

    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    //  @HystrixCommand(fallbackMethod = "defautMessage")
    public MResponseOperation doOperationServiceExterieur(MOperation mOperation, String codePv,
            Compte compteSrc, Compte compteDest,
            Compte compteComm, BigDecimal montantOp,
            String reference, String api,
            String description, String donneurOrdre,
            String beneficiaire, String alias
    ) throws IOException, NoSuchAlgorithmException, JAXBException {
        Operation operation = new Operation();
        String destinataireMsg;
        String codeUo = null;
        MResponseOperation retourMsg = new MResponseOperation();
        Boolean debitEstPossible = true;
        UniteOrganisation organisation = null;
        try {
            //Optional<PointVente> pointVenteExist = pointVenteService.findByCode(codePv);

            PointVente opt = pointVenteService.findPointByCode(codePv);

            Optional<PointVente> pointVenteExist = Optional.of(opt);
            if (pointVenteExist.isPresent()) {
                organisation = pointVenteExist.get().getUniteOrganisation();
                organisation = uniteOrganisationService.findOneById(organisation.getId());
                codeUo = organisation.getCodeUO();
            }
            log.info("Init operation ï¿½ partir du mobile .....");
            //Canal canal = canalService.findByCodeCanal("MOBILE");
            Canal canal = canalService.findCanal("MOBILE");

            operation = new Operation();

            TypeService typeService = null;
            //= typeServiceService.findByService("VIREMENT_SERVICE_EXTERIEUR");
            // TypeService typeService = typeServiceService.findByCodeService("VIREMENT_SERVICE_EXTERIEUR");
            if (pointVenteExist.isPresent() && canal != null) {
                //  UniteOrganisation organisation = pointVenteExist.get().getUniteOrganisation();
                User user = userService.findByPointVente(pointVenteExist.get());
                ZonedDateTime dateTime = ZonedDateTime.now();
                MotifOperation motifOperation = motifOperationService.findByCode("M13");
                // MotifOperation motifOperation = motifOperationRepository.findByCode("M13");
                if (motifOperation != null) {
                    operation.setMotifOperation(motifOperation);
                }
                UniteOrganisation fseSrc = null;
                UniteOrganisation fseDest = null;
                if (compteSrc.getCompteAutre() != null) {
                    fseSrc = uniteOrganisationService.findByCodeFSE(compteSrc.getCompteAutre().getCodeFse());
                } else if (compteSrc.getCompteBanque() != null) {
                    fseSrc = uniteOrganisationService.findByCodeFSE(compteSrc.getCompteBanque().getCodeFse());
                }
                if (compteDest != null && compteDest.getCompteAutre() != null) {
                    fseDest = uniteOrganisationService.findByCodeFSE(compteDest.getCompteAutre().getCodeFse());
                } else if (compteDest != null && compteDest.getCompteBanque() != null) {
                    fseDest = uniteOrganisationService.findByCodeFSE(compteDest.getCompteBanque().getCodeFse());
                } else {
                    throw new CustomParameterizedException("Compte de destination introuvable");
                }
                operation = saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operation);
                operation = saveInformationPersonne(mOperation.getBeneficiaire(), Boolean.TRUE, operation);
                UniteOrganisation uoAcq = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(mOperation.getCodeUoAcq(), Boolean.FALSE);
                UniteOrganisation uoEmt = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(mOperation.getCodeUoEm(), Boolean.FALSE);
                if (api.equals("BankToWallet")) {
                    mOperation.setTypeService("BANK_TO_WALLET");
                    typeService = typeServiceService.findByService("BANK_TO_WALLET");
                    operation.compteSrc(getCompte(mOperation.getBeneficiaire().getCompte(), operation.getBeneficaire().getPersonne(), "PTI", Boolean.FALSE));
                    operation.setCompteDest(compteDest);
                } else if (api.equals("WalletToBank")) {
                    mOperation.setTypeService("WALLET_TO_BANK");
                    typeService = typeServiceService.findByService("WALLET_TO_BANK");
                    operation.setCompteSrc(compteSrc);
                    operation.compteDest(getCompte(mOperation.getBeneficiaire().getCompte(), operation.getBeneficaire().getPersonne(), "PTI", Boolean.TRUE));
                } else if (api.equals("PiToAccount")) {
                    mOperation.setTypeService(TypeServiceLibelle.PI_TO_ACCOUNT.name());
                    typeService = typeServiceService.findByService(TypeServiceLibelle.PI_TO_ACCOUNT.name());
                    operation.setCompteSrc(compteSrc);
                    operation.compteDest(getCompte(mOperation.getBeneficiaire().getCompte(), operation.getBeneficaire().getPersonne(), "PTI", Boolean.TRUE));

                } else if (api.equals("AccountToPi")) {
                    mOperation.setTypeService(TypeServiceLibelle.ACCOUNT_TO_PI.name());
                    typeService = typeServiceService.findByService(TypeServiceLibelle.ACCOUNT_TO_PI.name());
                    operation.compteSrc(getCompte(mOperation.getEmetteur().getCompte(), operation.getEmetteur().getPersonne(), "PTI", Boolean.FALSE));
                    operation.setCompteDest(compteDest);
                }

                String codeUoAcq;
                if (mOperation.getCodeUoAcq() == null) {
                    codeUoAcq = organisation.getCodeUO();
                } else {
                    codeUoAcq = mOperation.getCodeUoAcq();
                }

                if (typeService == null) {
                    throw new CustomParameterizedException("Le service n'est pas actif.");
                }
                /**
                 *
                 */
                //Calcul des frais
                mOperation = getMontantCalculFrais(mOperation, opt.getCodePointVente(), mOperation.getCanal(), mOperation.getCodeUoEm(), codeUoAcq, montantOp);
                operation.montantOp(montantOp)
                        .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                        .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()))
                        .typeService(typeService)
                        .pointVente(pointVenteExist.get())
                        .statut(StatutOperation.ENATTENTE)
                        .canal(canal)
                        .uoEmetteur(uoEmt)
                        .statut(StatutOperation.PAYE)
                        .uoAcquereur(uoAcq)
                        .montantFraisClient(Util.getZeroIfNull(mOperation.getMontantFraisClient()))
                        .description(description)
                        .codeOperation(RandomUtil.getCodeOperation(dateTime, codeUo))
                        .dateOperation(dateTime)
                        .montantOpEnDevise(Util.getZeroIfNull(BigDecimal.ZERO))
                        .user(user);
                operation.setCodeExterne(reference);

                //SI BANK TO WALETT
                if (api.equals("BankToWallet") || api.equals("AccountToPi")) {

                    if (mOperation.getEmetteur() != null) {
                        log.info("Numeroo compte emetteur" + mOperation.getEmetteur().getCompte().getNumeroCompte());
                    }
                    log.info("DEBUT Controle du solde");
                    //    verifIfAutoriser(mOperation.getEmetteur().getCompte().getNumeroCompte(), TypeProfilActeur.CLIENT.name(), mOperation.getIdentifiant(), mOperation.getBeneficiaire().getTitulaire().getCodeTelephone());
                    Balance bal = null;
                    if (compteSrc.getCompteAutre() != null) {
                        bal = apiService.getSolde(compteSrc.getValeurCompte(), compteSrc.getCompteAutre().getCodeFse(), operation.getCompteSrc().getCompteAutre().getCodeFse());
                        log.info("Controle du solde {}", compteSrc.getValeurCompte());
                    } else if (compteSrc.getCompteBanque() != null) {
                        log.info("DEBUT Controle du solde");
                        bal = apiService.getSolde(compteSrc.getValeurCompte(), compteSrc.getCompteBanque().getCodeAgence(), operation.getCompteSrc().getCompteBanque().getCodeFse());
                    }
                    if (bal != null) {
                        log.info("SUITE Controle du solde");
                        Double solde = bal.getSolde();
                        log.info("Le solde du compte " + solde);
                        BigDecimal soldeEnBigDecimal = new BigDecimal(solde);
                        log.info("Le solde du compte en bigdecimal" + soldeEnBigDecimal);
                        BigDecimal mtop = Util.getZeroIfNull(Util.getZeroIfNull(mOperation.getMontantFraisClient())).add(Util.getZeroIfNull(montantOp)).add(Util.getZeroIfNull(mOperation.getMontantTaxe()));
                        log.info("montant opération" + mtop);
                        if (Util.isBiggerOrEqualThan(soldeEnBigDecimal, mtop)) {
                            debitEstPossible = true;
                        } else {
                            debitEstPossible = false;
                        }
                    } else {
                        debitEstPossible = false;
                    }

                }

                if (!debitEstPossible) {
                    log.info("Solde insuffisant");
                    throw new CustomParameterizedException("Solde insuffisant.");
                    //    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    // return showMessage("-1", "Solde insuffisant.",null);
                }
                operation.uoEmetteur(fseSrc)
                        .statut(StatutOperation.PAYE)
                        .uoBeneficiaire(uoEmt)
                        .uoAcquereur(fseDest);
                log.info("CONTROLE SEUIL .....");
                operation = controleSeuil(operation);
                log.info("FIN CONTROLE .....");
                Operation operat = operationRepository.save(operation);
                operationService.comptabilisationOperationMad(operat, opt, compteComm);
                return showMessage("0", "Operation effectuee avec succes.", operation);
            }

        } catch (CustomParameterizedException e) {
            operationService.sendAck(operation, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            if (!debitEstPossible) {
                return showMessage(Integer.toString(ErreurConstants.SOLDE_INSUFFISANT), "Solde insuffisant", null);
            }
            return showMessage("-1", e.getMessage(), null);

        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operation, e.getMessage());
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

            CodeErreur codeErreur = operationService.verifMessageRetourOmnicanal(operation);
            if (codeErreur != null && !codeErreur.getCode().equals("-1")) {
                log.info("Une erreur est survenue lors de l'operation...." + codeErreur.getCode() + "- " + codeErreur.getLibelle());
                retourMsg = new MResponseOperation(codeErreur.getCode(), codeErreur.getLibelle(), null);

            } else {
                retourMsg = new MResponseOperation("-1", e.getMessage(), null);
            }
            //    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return retourMsg;
        }
        return null;
    }

    private Client getClient(Operation oper, String type) {
        Personne pers;
        Client client = null;
        if (type.equals("EMETTEUR")) {
            if (oper.getEmetteur() != null) {
                String telephone = oper.getEmetteur().getPersonne().getTelephone();
                String code = oper.getEmetteur().getPersonne().getTelephone();
                if (telephone != null && code != null) {
                    pers = oper.getEmetteur().getPersonne();
                    if (pers != null) {
                        client = clientService.findByPersonneId(pers.getId());
                    }
                }
            }
        } else if (type.equals("BENEFICIAIRE")) {
            if (oper.getBeneficaire() != null) {
                String telephone = oper.getBeneficaire().getPersonne().getTelephone();
                String code = oper.getBeneficaire().getPersonne().getTelephone();
                if (telephone != null && code != null) {
                    pers = oper.getBeneficaire().getPersonne();
                    if (pers != null) {
                        client = clientService.findByPersonneId(pers.getId());
                    }
                }
            }
        }

        return client;
    }

    public TableauCumulDTO getTableauCumulClient(Operation mOperation, Client cliemetteur, Client clibeneficiaire) {
        PointVente pointVenteExist = mOperation.getPointVente();
        UniteOrganisation uniteOrganisationSource = null;
        uniteOrganisationSource = pointVenteExist.getUniteOrganisation();
        Canal canal = mOperation.getCanal();
        uniteOrganisationSource = uniteOrganisationService.findOneById(uniteOrganisationSource.getId());

        UniteOrganisation uoPays;
        Long uoAcquereurId = null;
        if (!uniteOrganisationSource.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
            uoPays = uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());
        } else {
            uoPays = uniteOrganisationSource;
        }

        //  UniteOrganisation uoPays = uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());
        // UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.PAYS);
        TypeService typeService = mOperation.getTypeService();//
        Personne emetteur = null;
        Personne beneficiaire = null;
        //Recherche de la  personne effectuant l'operaiton
        if (mOperation.getEmetteur() != null) {
            String telephone = mOperation.getEmetteur().getPersonne().getTelephone();
            String code = mOperation.getEmetteur().getPersonne().getTelephone();
            if (telephone != null && code != null) {
                emetteur = mOperation.getEmetteur().getPersonne();
            }
        }
        TableauCumulDTO tab = null;
        // ProfilActeur profilActeur = uniteOrganisationSource.getProfilActeur();
        /* Récupération de lid de l acquereur*/
        if (mOperation.getUoAcquereur() != null) {
            if (mOperation.getUoAcquereur().getId() != null) {
                uoAcquereurId = mOperation.getUoAcquereur().getId();
            }
        }
        if (emetteur != null) {
            Client client = cliemetteur;
            //Client client = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(emetteur.getId());
            if (client != null) {
                ProfilActeur profilClient = client.getProfilActeur();
                tab = operationService.getTableauCumulDTO(null, typeService.getId(), canal.getId(), null, client.getId(), uoPays.getId(),
                        uoAcquereurId, TypeRegle.CLIENT,
                        mOperation.getMontantOp(), mOperation.getMontantOp(), profilClient.getId(), null, emetteur.getId());
            } else if (emetteur.getId() != null) {
                tab = operationService.getTableauCumulDTO(null, typeService.getId(), canal.getId(), null, null, uoPays.getId(),
                        uoAcquereurId, TypeRegle.CLIENT,
                        mOperation.getMontantOp(), mOperation.getMontantOp(), null, null, emetteur.getId());
            }
        }
        if (tab != null) {
            return tab;
        }
        //****Bï¿½nï¿½ficiaire on contrï¿½le son solde******/
        if (mOperation.getBeneficaire() != null) {
            String telephone = mOperation.getBeneficaire().getPersonne().getTelephone();
            String code = mOperation.getBeneficaire().getPersonne().getTelephone();
            if (telephone != null && code != null) {
                beneficiaire = mOperation.getBeneficaire().getPersonne();
            }
        }
        BigDecimal mtop = mOperation.getMontantOp();
        BigDecimal totalAvecSolde = mtop;
        if (beneficiaire != null) {
            if (mOperation.getCompteDest() != null) {
                if (mOperation.getCompteDest().getCompteAutre() != null) {

                    totalAvecSolde = mtop.add(mOperation.getCompteDest().getCompteAutre().getSolde());

                    //      mtop.add(Util.getZeroIfNull(montant)), mOperation.getCompteDest().getCompteAutre().getSolde());
                }
            }

            Client benef = clibeneficiaire;
            //   Client benef = clientRepository.findTop1ByPersonneIdAndClientValideTrueAndSupprimeIsFalse(beneficiaire.getId());
            if (benef != null && benef.getId() != null) {
                ProfilActeur profilClient = benef.getProfilActeur();
                tab = operationService.getTableauCumulDTO(null, typeService.getId(), canal.getId(), benef.getId(), null, uoPays.getId(),
                        uoAcquereurId, TypeRegle.CLIENT,
                        mOperation.getMontantOp(), totalAvecSolde, profilClient.getId(), beneficiaire.getId(), null);
            } else if (beneficiaire.getId() != null) {
                tab = operationService.getTableauCumulDTO(null, typeService.getId(), canal.getId(), null, null, uoPays.getId(),
                        uoAcquereurId, TypeRegle.CLIENT,
                        mOperation.getMontantOp(), totalAvecSolde, null, beneficiaire.getId(), null);
            }
        }

        return tab;
    }

    private Operation controleSeuil(Operation mOperation, Client emetteur, Client beneficiaire) {
        TableauCumulDTO tab = getTableauCumulClient(mOperation, emetteur, beneficiaire);
        if (tab != null) {
            if ((tab.getAction() != null) && tab.getAction().equals(Action.BLOQUE)) {
                throw new CustomParameterizedException(tab.getMessage());

            } else if ((tab.getAction() != null) && tab.getAction().equals(Action.NON_BLOQUANT)) {
                mOperation.setStatut(StatutOperation.ENATTENTE_VALIDATION);
            }
        }
        TableauCumulDTO tabacteur = getTableauCumulActeur(mOperation);
        if (tabacteur != null) {
            if ((tabacteur.getAction() != null) && tabacteur.getAction().equals(Action.BLOQUE)) {
                throw new CustomParameterizedException(tabacteur.getMessage());

            } else if ((tabacteur.getAction() != null) && tabacteur.getAction().equals(Action.NON_BLOQUANT)) {
                mOperation.setStatut(StatutOperation.ENATTENTE_VALIDATION);
            }
        }
        return mOperation;
    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponseOperation doOperationChargement(MOperation mOperation, Operation opera, String narration, Integer nbre, Compte compteCredit, Long ligneId) throws IOException, NoSuchAlgorithmException, JAXBException {
        Operation operation = null;
        StopWatch stopWatch = new StopWatch("1. COMPTABILISATION");
        try {

            // StopWatch stopWatch = new StopWatch("1. DEBUT DU TRAITEMENT");
            String destinataireMsg;
            Boolean debitEstPossible = true;

            // PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());
            //  Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());
            OperationOptimiseDTO operExist = operationService.findByCodeOperationEx(ligneId.toString());
            if (opera != null && operExist == null) {

                System.out.println("====================" + opera.getCodeOperation());
                PointVente opt = opera.getPointVente();
                Optional<PointVente> pointVenteExist = Optional.of(opt);

                log.info("Init operation a partir du mobile .....");
                if (mOperation.getCanal() == null) {
                    showMessage("-1", "Le canal est obligatoire.", null);
                }
                if (mOperation.getCodePv() == null) {
                    showMessage("-1", "Le point de vente est introuvable.", null);
                }
                if (mOperation.getTypeService() == null) {
                    showMessage("-1", "Le type de service est obligatoire.", null);
                }

                Canal canal = opera.getCanal();
                operation = new Operation();
                TypeService typeService = opera.getTypeService();

                //typeServiceService.findByService(mOperation.getTypeService());
                // TypeService typeService = typeServiceService.findByCodeService(mOperation.getTypeService());
                String codeExterne = null;
                BigDecimal mtTaxe = null;
                BigDecimal mFrais = null;
                String codeUo = "";
                codeExterne = ligneId.toString();
                if (opera != null) {
                    //  canal=opera.getCanal();
                    // typeService=opera.getTypeService();
                    if (nbre == 0) {
                        mtTaxe = opera.getMontantTaxe();
                        mFrais = opera.getMontantFrais();
                    }

                }

                if (pointVenteExist.isPresent() && typeService != null && canal != null) {

                    UniteOrganisation organisation = pointVenteExist.get().getUniteOrganisation();
                    organisation = uniteOrganisationService.findOneById(organisation.getId());
                    codeUo = organisation.getCodeUO();

                    log.info("Traitement de l'operation de chargement   .....");

                    log.info("------------TYPESERVUCE   ....." + typeService.getCode());
                    ZonedDateTime dateTime = ZonedDateTime.now();
                    if (mOperation.getMotif() != null) {

                        log.info("------------MOTIF   ....." + mOperation.getMotif());

                        MotifOperation motifOperation = opera.getMotifOperation();
                        if (motifOperation != null) {
                            operation.setMotifOperation(motifOperation);
                        } else {

                            return showMessage("-1", "Le motif est obligatoire", null);
                        }
                    }
                    operation.montantOp(mOperation.getMontant())
                            .montantTaxe(Util.getZeroIfNull(mtTaxe))
                            .montantFrais(Util.getZeroIfNull(mFrais))
                            .typeService(typeService)
                            .pointVente(pointVenteExist.get())
                            .statut(StatutOperation.ENATTENTE)
                            .canal(canal)
                            .uoAcquereur(opera.getUoAcquereur())
                            .description(mOperation.getDescription())
                            .codeOperation(RandomUtil.getCodeOperation(dateTime, codeUo))
                            .dateOperation(dateTime)
                            .codeExterne(codeExterne)
                            .montantOpEnDevise(mOperation.getMontant())
                            .user(opera.getUser());
                    operation.setNarration(narration);
                    stopWatch.start("1.RECHERCHE ORGANISATION ");
                    UniteOrganisation fseDest = null;
                    if (mOperation.getTypeService().equals(TypeServiceLibelle.TRANSFERT_MASSE.toString())) {
                        operation.compteDest(compteCredit);
                        operation.compteSrc(opera.getCompteSrc());
                        if (operation.getCompteDest() != null) {

                            fseDest = opera.getUoEmetteur();
                        }
                        operation.setEmetteur(opera.getEmetteur());
                        operation = saveInformationPersonne(mOperation.getBeneficiaire(), Boolean.TRUE, operation);

                    } else if (mOperation.getTypeService().equals(TypeServiceLibelle.TRANSFERT_MASSE_DEBIT.toString())) {
                        operation.compteSrc(compteCredit);
                        operation.compteDest(opera.getCompteDest());
                        if (operation.getCompteDest() != null) {
                            fseDest = opera.getUoEmetteur();
                        }
                        operation.setEmetteur(opera.getEmetteur());
                        operation = saveInformationPersonne(mOperation.getBeneficiaire(), Boolean.FALSE, operation);

                    } else {
                        operation.compteSrc(opera.getCompteSrc());
                        operation.setEmetteur(opera.getEmetteur());
                        operation = saveInformationPersonne(mOperation.getBeneficiaire(), Boolean.TRUE, operation);

                    }

                    stopWatch.stop();
                    /*  Lï¿½ on n'est dans une situation de cash to account**/
                    //  operation.compteSrc(getCompte(mOperation.getEmetteur().getCompte(), operation.getEmetteur().getPersonne(), mOperation.getApplication()));

                    UniteOrganisation fseSrc;

                    fseSrc = opera.getUoEmetteur();

                    //Service de comptabilisation
                    if (mOperation.getTypeService().equals(TypeServiceLibelle.TRANSFERT_MASSE_CASH.toString())) { //ACCOUNT_CASH
                        log.info("operation de type TRANSFERT_MASSE_CASH en cours.....");

                        //Pays de paiement
                        if (mOperation.getCodePays() != null) {
                            if (mOperation.getCodeUo() != null) {
                                UniteOrganisation uoPaiement = operation.getUoAcquereur();//uniteOrganisationService.findByCode(mOperation.getCodeUo());
                                if (uoPaiement == null) {
                                    return new MResponseOperation("-1", "Le code du lieu de paiement est incorrect." + mOperation.getCodeUo(), null);
                                }

                                operation.uoAcquereur(uoPaiement);
                                fseDest = uoPaiement;

                            } else {
                                log.info("Code pays : " + mOperation.getCodePays());
                                UniteOrganisation paysPaiement = opera.getUoAcquereur();// uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(mOperation.getCodePays(), false);
                                if (paysPaiement != null) {
                                    operation.uoAcquereur(paysPaiement);
                                } else {

                                    log.info("Infos : lieu de paiement introuvable introuvable");
                                    throw new CustomParameterizedException(" lieu de paiement introuvable introuvable.");
                                }
                            }
                            //Verification de la validitï¿½ du lieu de paiement
                            operation.uoEmetteur(fseSrc)
                                    .uoAcquereur(fseDest)
                                    .description("TRANSFERT MASSE A CASH")
                                    .statut(StatutOperation.ENATTENTE)
                                    .montantFraisClient(Util.getZeroIfNull(mOperation.getMontantFraisClient()))
                                    .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                                    .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()));

                            //Calcul des taxes de l'operation en cours
                            //  operation = treatTaxeAndSaveOp(mOperation, operation, organisation);
                            operation = operationRepository.save(operation);
                            Personne emetteur = null;
                            Personne beneficiaire = null;
                            //Recherche de la  personne effectuant l'operaiton

                            Client clientEmeteur = getClient(operation, "EMETTEUR");
                            Client clientBeneficiaire = getClient(operation, "BENEFICIAIRE");
                            operation = controleSeuil(operation, clientEmeteur, clientBeneficiaire);
                            if (!operation.getStatut().equals(StatutOperation.ENATTENTE_VALIDATION)) {
                                //   operationService.calculRepartitionFrais(operation, fseSrc.getId(), operation.getUoEmetteur().getId());
                                //    operationAuditService.saveOpAudit(operation, null, StatutOperation.ENATTENTE);
                                operation = operationService.comptabilisationOperationMad(operation, pointVenteExist.get(), null);
                                //  mOperation.setCardLess(Boolean.FALSE);
                                //  operationAuditService.rechercheSuivi(operation, clientEmeteur, clientBeneficiaire);
                                return showMessage("0", "Operation effectuee avec succes.", operation);
                            } else {
                                return showMessage("0", "Votre operation est soumise a validation", operation);
                            }

                        } else {
                            return showMessage("-1", "Le pays de paiement est obligatoire.", null);
                        }
                    } else if (mOperation.getTypeService().equals(TypeServiceLibelle.TRANSFERT_MASSE.toString()) || mOperation.getTypeService().equals(TypeServiceLibelle.TRANSFERT_MASSE_DEBIT.toString())) {
                        String description = "";
                        if (mOperation.getTypeService().equals(TypeServiceLibelle.TRANSFERT_MASSE.toString())) {
                            description = " TRANSFERT EN MASSE " + codeExterne + " " + narration;
                        } else {
                            description = "REGULATION EN MASSE " + codeExterne + " " + narration;
                        }
                        operation.uoEmetteur(fseSrc)
                                .uoAcquereur(fseSrc)
                                .description(description)
                                .statut(StatutOperation.PAYE);
                        operation.etape(Etape.PROCEED);
                        operation = operationRepository.save(operation);
                        Client clientEmeteur = getClient(operation, "EMETTEUR");
                        Client clientBeneficiaire = getClient(operation, "BENEFICIAIRE");
                        operation = controleSeuil(operation, clientEmeteur, clientBeneficiaire);
                        if (!operation.getStatut().equals(StatutOperation.ENATTENTE_VALIDATION)) {
                            stopWatch.start("1.Comptabilisation du transfert en masse ");
                            operation = operationService.comptabilisationMasse(operation);
                            //   operation = operationService.comptabilisationOperationMad(operation, pointVenteExist.get(), null);
                            stopWatch.stop();
                            System.out.println(stopWatch.prettyPrint());
                            return showMessage("0", "Operation effectuee avec succes.", operation);
                        } else {
                            return showMessage("0", "Votre operation est soumise a validation", operation);
                        }

                    }
                }
            } else {
                if (operExist != null) {
                    return showMessage("-1", "Cette ligne a été déjà exécutée Operation numéro " + operExist.getCodeOperation(), null);
                }

            }

        } catch (CustomParameterizedException e) {
            //operationService.sendAck(operation, e.getMessage());
            //  TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operation, e.getMessage());
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        }

        return showMessage("-1", "Le point de vente est introuvable.", null);
    }

    private MResponse showMessage(String code, String message) {
        return new MResponse(code, message);
    }

    private MResponse showMessageResult(String code, String message, String tansactionId, List<MParametre> extraData) {
        return new MResponse(code, message, tansactionId, extraData);
    }

    /**
     * @param code
     * @param message
     * @return
     */
    private MResponseOperation showMessage(String code, String message, Operation oper) {
        return new MResponseOperation(code, message, oper);
    }

    public MRetraitResponse getOperationDetail(String codeOperation) {
        return operationService.getDetailOperation(codeOperation);
    }

    @Transactional
    public MRetraitResponse getDetailRetrait(MOperation mOperation) throws NoSuchAlgorithmException {
        MResponse mresponse;
        MRetraitResponse mretrait = new MRetraitResponse();
        try {

            log.info("Operation de RETRAIT en cours ....");
            //    Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());
            PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());
            Optional<PointVente> pointVenteExist = Optional.of(opt);
            MPersonne mbenef = new MPersonne();
            MPersonne memetteur = new MPersonne();
            Canal canal = canalService.findCanal(mOperation.getCanal());
            //    Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
            if (pointVenteExist.isPresent() && canal != null && mOperation.getCodeRetrait() != null) {
                OperationAllDTO operationAllDTO = operationService.verificationOperationMobile(mOperation.getCodeRetrait(), pointVenteExist.get());
                if (operationAllDTO != null) {
                    //  Operation operation = operationMapper.toEntity(operationAllDTO.getOperationDTO());
                    PersonnePhysiqueDTO beneficiaire = operationAllDTO.getBeneficiaire().getPersonnePhysiqueDTO();
//                  PersonnePhysiqueDTO emeteur = operationAllDTO.getEmetteur().getPersonnePhysiqueDTO();
                    //     PersonneDTO emetteur = operationAllDTO.getEmetteur().getPersonneDTO();
                    BigDecimal mt = operationAllDTO.getOperationDTO().getMontantOp();
                    mbenef.setTelephone(operationAllDTO.getBeneficiaire().getPersonneDTO().getTelephone());
                    mbenef.setNom(beneficiaire.getNomClient());
                    mbenef.setCodeTelephone(operationAllDTO.getBeneficiaire().getPersonneDTO().getCodePaysTelephone());
                    mbenef.setPrenom(beneficiaire.getPrenomClient());
                    mbenef.setLieuNaissance(beneficiaire.getLieuNaissance());
                    //  mbenef.setSexe(MSexe.valueOf(benefi.getPersonnePhysique().getSexe()));
                    //  mbenef.setDateNaissance(benefi.getPersonnePhysique().getDateNaissance());

                    mresponse = new MResponse("0", "operation possible");
                    mretrait.setMontant(mt);
                    mretrait.setMpersonne(mbenef);

                } else {
                    mresponse = new MResponse("-1", "Impossible de faire l'opération introuvable.");
                }

            } else {
                mresponse = new MResponse("-1", "Impossible de faire l'opération  dans ce lieu de paiement.");
            }
            mretrait.setReponse(mresponse);
            return mretrait;
        } catch (CustomParameterizedException e) {
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            mresponse = new MResponse("-1", e.getMessage());
            mretrait.setReponse(mresponse);
            return mretrait;
        }

    }

    public MResponse createUser(MUser muser) {
        UniteOrganisation uo = null;
        User utilisateur;
        ManagedUserVM managedUserVM = null;

        if (muser.getCodeUo() != null) {
            uo = uniteOrganisationService.findByCodeAndTelephone(muser.getCodeUo(), muser.getNumeroTelephoneAgent(), muser.getCodeIndicatif());
            utilisateur = userService.findeByLogin(muser.getLogin().toLowerCase());
            String prof = getProfilAgent();

            if (uo != null && utilisateur == null && prof != null) {
                managedUserVM = new ManagedUserVM(null, muser.getLogin(), muser.getPassword(), muser.getFirstName(), muser.getLastName(),
                        muser.getEmail(), true, "", null, null, muser.getPhoneNumber(),
                        null, null, null, null,
                        null, null, null, uo.getId(), null, null, 0, null, null, 0, muser.getCodeIndicatif());
                managedUserVM.setProfilId(Long.parseLong(prof));
            } else {
                if (utilisateur != null) {
                    return new MResponse("0", "ok");
                }
                return new MResponse("-1", "l'unité d'organisation/profil introuvable ");
            }
        } else {
            utilisateur = userService.findeByLogin(muser.getLogin().toLowerCase());
            if (utilisateur == null) {
                Personne personneExist = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(muser.getNumeroTelephoneAgent(), muser.getCodeIndicatif());
                if (personneExist != null) {
                    Client c = clientRepository.findTop1ByPersonneIdAndClientValideTrueAndSupprimeIsFalse(personneExist.getId());
                    managedUserVM = new ManagedUserVM(null, muser.getLogin(), muser.getPassword(), muser.getFirstName(), muser.getLastName(),
                            muser.getEmail(), true, "", null, null, muser.getPhoneNumber(),
                            null, null, null, null,
                            null, null, null, null, null, null, 0, null, c.getId(), 0, muser.getCodeIndicatif());
                }
            } else {
                return new MResponse("0", "Opération effectuée avec succès.");
            }

        }
        if (managedUserVM != null) {
            User u = userService.createUser(managedUserVM);

            if (u != null) {
                return new MResponse("0", "Opération effectuée avec succï¿½s.");
            } else {
                return new MResponse("-1", "impossible de créer l'utilisateur.");
            }
        } else {
            return new MResponse("-1", "impossible de creer l'utilisateur.");
        }

    }

    public MResponse createTypeReclamation(MReclamation mreclamation) {
        if (mreclamation != null) {
            TypeReclamation typeReclamation = typeReclamationRepository.findById(mreclamation.getTypeReclamation()).orElse(null);
            Reclamation recl = new Reclamation();
            recl.setTypeReclamation(typeReclamation);
            recl.setContenu(mreclamation.getContenu());
            recl.setTelephone(mreclamation.getTelephone());
            recl.setQualiteAuteur(mreclamation.getQualiteAuteur());
            recl.setCodeActeur(mreclamation.getCodeActeur());
            //Ajout Heure de création
            recl.setDateHeureCreation(ZonedDateTime.now());
            Reclamation r = reclamationService.save(recl);
            if (r != null) {
                return new MResponse("0", "Operation effectuée avec succï¿½s.");
            } else {
                return new MResponse("-1", "impossible d'enregistrer la reclamation.");
            }

        } else {
            return new MResponse("-1", "impossible d'enregistrer la reclamation.");
        }

    }

    @Transactional
    public MResponseOperation doOperationRetraitCash(MOperation mOperation) throws NoSuchAlgorithmException, IOException, JAXBException {
        Operation operationRetrait = null;
        String codeUo = "";
        try {
            log.info("Operation de RETRAIT en cours ....");
            //   Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());
            PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());

            Optional<PointVente> pointVenteExist = Optional.of(opt);

            Canal canal = canalService.findCanal(mOperation.getCanal());
            //   Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
            log.debug("retrait%%" + mOperation.getCodeRetrait());
            log.debug("retrait%%" + mOperation.getCanal());
            log.debug("retrait%%" + mOperation.getCodePv());

            if (pointVenteExist.isPresent() && canal != null && mOperation.getCodeRetrait() != null) {
                UniteOrganisation uoAcq = pointVenteExist.get().getUniteOrganisation();
                uoAcq = uniteOrganisationService.findOneById(uoAcq.getId());
                codeUo = uoAcq.getCodeUO();
                if (!canal.getCodeCanal().equals("MOBILE") && !canal.getCodeCanal().equals("WEB") && !canal.getCodeCanal().equals("USSD")) {
                    return new MResponseOperation("-1", "Impossible de faire l'opération à travers ce canal." + canal.getCodeCanal(), null);
                }
                OperationAllDTO operationAllDTO = operationService.verificationOperationWithoutAction(mOperation.getCodeRetrait(), pointVenteExist.get());
                if (operationAllDTO != null && operationAllDTO.getBeneficiaire() != null) {
                    log.debug("Operation de RETRAIT en cours %%....");
                    log.info("Operation de RETRAIT en cours %%....");
                    PersonneDTO beneficiaire = operationAllDTO.getBeneficiaire().getPersonneDTO();
                    String telephone = beneficiaire.getTelephone();
                    String codeTelephone = beneficiaire.getCodePaysTelephone();
                    if (telephone != null && codeTelephone != null) {
                        if (telephone.equals(mOperation.getBeneficiaire().getTitulaire().getTelephone()) && codeTelephone.equals(mOperation.getBeneficiaire().getTitulaire().getCodeTelephone())) {
                            log.info("Ok pour les conditions de realisation ....");
                            Operation operation = operationMapper.toEntity(operationAllDTO.getOperationDTO());
                            operation.statut(StatutOperation.PAYE);

                            operation = operationRepository.save(operation);
                            Operation op = operation;
                            /**
                             * Il s'agit du paiment internet *
                             */
                            Boolean debitEstPossible = true;
                            if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.PAIEMENT_INTERNET))) {
                                log.info("Levée des évenements");
                                EvenementOperation evenementOperation = evenementOperationRepository.findByOperation(operation);
                                if (evenementOperation != null) {
                                    operation.setPointVente(pointVenteExist.get());
                                    operation.setUoAcquereur(uoAcq);
                                    operation = operationRepository.save(operation);
                                    operationService.validationOperationDeuxPhase(operation);
                                    evenementOperation.setEstLevee(1);
                                    evenementOperationRepository.save(evenementOperation);
                                    repartirCommission(operation);
                                    return new MResponseOperation("0", "Opération effectuée avec succès.", operation);
                                } else {
                                    return new MResponseOperation("-1", "Impossible de traiter cette opération.", null);
                                }
//                                if (operation.getCompteSrc().getCompteAutre() != null) {
//                                    debitEstPossible = compteService.peutDebiterCompte(operation.getCompteSrc(), Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(mOperation.getMontantTaxe()));
//                                    log.info("Controle du solde");
//                                } else if (operation.getCompteSrc().getCompteBanque() != null) {
//                                    Balance bal = apiService.getSolde(operation.getCompteSrc().getValeurCompte(), operation.getCompteSrc().getCompteBanque().getCodeAgence(), operation.getCompteSrc().getCompteBanque().getCodeFse());
//                                    Double solde = bal.getSolde();
//                                    BigDecimal soldeEnBigDecimal = new BigDecimal(solde);
//                                    BigDecimal mtop = Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(mOperation.getMontantTaxe());
//                                    if (Util.isBiggerOrEqualThan(soldeEnBigDecimal, mtop)) {
//                                        debitEstPossible = true;
//                                    } else {
//                                        debitEstPossible = false;
//                                    }
//
//                                }
//                                if (!debitEstPossible) {
//                                    log.info("Solde insuffisant");
//                                    throw new CustomParameterizedException("Solde insuffisant.");
//
//                                }
                            } else {
                                /**
                                 * fin du paiement internet *
                                 */
                                operationAuditService.saveOpAudit(operation, operationAllDTO.getOperationDTO().getStatut(), StatutOperation.PAYE);
                                operationRetrait = new Operation();
                                TypeService typeService = typeServiceService.findByService(String.valueOf(TypeServiceLibelle.RETRAIT));
                                // TypeService typeService = typeServiceService.findByCodeService(String.valueOf(TypeServiceLibelle.RETRAIT));
                                /* copy de l'opï¿½ration */
                                //mOperation = getMontantCalculFrais(mOperation, mOperation.getCodePv(), mOperation.getCanal(), mOperation.getCodeUoEm(), mOperation.getCodeUoAcq(), mOperation.getMontant());
                                operationRetrait.statut(StatutOperation.PAYE)
                                        .beneficaire(operation.getBeneficaire())
                                        .canal(canal)
                                        .emetteur(operation.getEmetteur())
                                        .beneficaire(operation.getBeneficaire())
                                        .typeService(typeService)
                                        .pointVente(pointVenteExist.get())
                                        .uoAcquereur(uoAcq)
                                        .montantFrais(Util.getZeroIfNull(repartitionOpService.getMontantByActeurAndOp(Acteur.ACQ, operation.getId())))
                                        .montantOp(operation.getMontantOp())
                                        .montantOpEnDevise(operation.getMontantOpEnDevise())
                                        .dateOperation(ZonedDateTime.now())
                                        .etape(Etape.PROCEED)
                                        .uoEmetteur(uoAcq)
                                        .devisePaiement(operation.getDevisePaiement())
                                        .operationTraite(operation)
                                        .codeOperation(RandomUtil.getCodeOperation(operation.getDateOperation(), codeUo));

                                operationRetrait.setPieceBeneficiaire(operation.getPieceEmetteur());
                                operationRetrait = operationRepository.save(operationRetrait);
                                // operationAuditService.saveOpAudit(operationRetrait, null, StatutOperation.PAYE);

                                operationService.comptabilisationRetrait(operation, uoAcq, operationRetrait);
                                operationAuditService.rechercheSuivi(operation);
                                log.info("Opération effectuée avec succès");
                                return new MResponseOperation("0", "Opération effectuée avec succès.", operationRetrait);
                            }

                        } else {
                            return new MResponseOperation("-1", "Le client n'est pas autorisation à faire ce retrait.", null);
                        }
                    } else {
                        return new MResponseOperation("-1", "Le numï¿½ro du bénéiciaire est introuvable.", null);
                    }
                } else {
                    return new MResponseOperation("-1", "Impossible de traiter cette opération.", null);
                }

            } else {
                return new MResponseOperation("-1", "Le point de vente ou le canal ou le code retrait est introuvable.", null);
            }
        } catch (CustomParameterizedException e) {
            operationService.sendAck(operationRetrait, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operationRetrait, e.getMessage());
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        }

    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponseOperation doOperationEnvoiCash(MOperation mOperation) throws NoSuchAlgorithmException, IOException, JAXBException {
        Operation operation = new Operation();
        String codeUo = "";
        try {
            log.info("Operation d'envoi cash en cours ...");
            log.debug("opï¿½ration d'envoi cash {}" + mOperation);
            //User userConnecte = userService.getUserWithAuthorities();
            //  userCanDoOperations(userConnecte);

            //  PointVente pointVente = userConnecte.getPointVente();
            operation = new Operation();
            log.debug("canal%%" + mOperation.getCanal());
            log.debug("codepv%%" + mOperation.getCodePv());
            log.debug("user connecte%%" + mOperation.getUserConnecte());
            //    Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());
            PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());

            Optional<PointVente> pointVenteExist = Optional.of(opt);

            User userConnecte = userService.findeByLogin(mOperation.getUserConnecte());
            Canal canal = canalService.findCanal(mOperation.getCanal());
            //  Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
            // ParametreApp nombreJours = parametreAppRepository.findByTypeAndNom(TypeParametre.AUTRE, "DELAI_EXPIRED_CODE");
            if (pointVenteExist.isPresent() && canal != null && userConnecte != null) {
                UniteOrganisation uniteOrganisationSource = pointVenteExist.get().getUniteOrganisation();
                uniteOrganisationSource = uniteOrganisationService.findOneById(uniteOrganisationSource.getId());
                codeUo = uniteOrganisationSource.getCodeUO();
                uniteOrganisationSource = uniteOrganisationService.findOneById(uniteOrganisationSource.getId());
                Compte compteOperationActeur = compteService.getCompteOfOrganisationUser(uniteOrganisationSource, EtiquetteCompteCode.CPTEOPERATIONACTEURPTI);
                if (compteOperationActeur != null) {
                    verifIfAutoriser(compteOperationActeur.getValeurCompte(), mOperation.getActeur(), mOperation.getIdentifiant(), null);
                    operationService.verifFonds(mOperation.getMontant(), uniteOrganisationSource, EtiquetteCompteCode.CPTEOPERATIONACTEURPTI);
                }

                log.debug("retrait%%" + compteOperationActeur.getCompteAutre());
                log.debug("retrait%%" + mOperation.getCanal());
                log.debug("retrait%%" + mOperation.getCodePv());
                /*Recalcul des frais */
                mOperation = getMontantCalculFrais(mOperation, mOperation.getCodePv(), mOperation.getCanal(), mOperation.getCodeUoEm(), mOperation.getCodeUoAcq(), mOperation.getMontant());

                BigDecimal mt = BigDecimal.ZERO;
                BigDecimal montant = BigDecimal.ZERO;
                BigDecimal taxe = BigDecimal.ZERO;
                BigDecimal frais = BigDecimal.ZERO;
                if (mOperation.getMontantFrais() != null) {
                    frais = mOperation.getMontantFrais();
                }
                if (mOperation.getMontantTaxe() != null) {
                    taxe = mOperation.getMontantTaxe();
                }
                if (mOperation.getMontant() != null) {
                    montant = mOperation.getMontant();
                }

                //   Boolean debitEstPossible = compteService.peutDebiterCompte(compteOperationActeur.getCompteAutre(), frais.add(mOperation.getMontant()).add(mt));
                TypeService typeService = typeServiceService.findByService(String.valueOf(TypeServiceLibelle.ENVOI_CASH));
                //   TypeService typeService = typeServiceService.findByCodeService(String.valueOf(TypeServiceLibelle.ENVOI_CASH));
                if (operationService.verifAutorisationOfTypeService(typeService, uniteOrganisationSource)) {
                    if (uniteOrganisationSource.getTypeUO().getLibelleTypeUO().equals("AGENCE") || uniteOrganisationSource.getTypeUO().getLibelleTypeUO().equals("MASTER") || uniteOrganisationSource.getTypeUO().getLibelleTypeUO().equals("SOUS_DISTRIBUTEUR")) {
                        if (mOperation.getMotif() != null) {

                            MotifOperation motifOperation = motifOperationService.findByCode(mOperation.getMotif());
                            if (motifOperation != null) {
                                operation.setMotifOperation(motifOperation);
                            } else {
                                return showMessage("-1", "Le motif est obligatoire", null);
                            }
                        }
                        String otps[] = RandomUtil.generateOtp();
                        String codeRetrait = otps[1];
                        String md5codeRetrait = RandomUtil.getMd5Code(codeRetrait);
                        int dureeExpire = operationService.getDureeExpire();
                        UniteOrganisation paysPaiement = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(mOperation.getCodeUoAcq(), false);
                        if (paysPaiement == null) {
                            return new MResponseOperation("-1", "Veuillez préciser le lieu de paiement", null);
                        }
                        operation.dateOperation(ZonedDateTime.now())
                                .commentaire(codeRetrait)
                                .codeRetrait(RandomUtil.getMd5Code(codeRetrait))
                                .commentaire(Base64.encodeBase64String(codeRetrait.getBytes()))
                                .secret(otps[0])
                                .canal(canal)
                                .pointVente(pointVenteExist.get())
                                .typeService(typeService)
                                .dateExpiration(LocalDate.now().plusDays(dureeExpire))
                                .etape(Etape.PROCEED)
                                .uoEmetteur(uniteOrganisationSource)
                                .uoAcquereur(paysPaiement)
                                .montantFrais(frais)
                                .montantTaxe(taxe)
                                .montantOp(montant)
                                .user(userConnecte)
                                .statut(StatutOperation.ENATTENTE)
                                .codeExterne(operation.getCodeExterne())
                                .codeOperation(RandomUtil.getCodeOperation(operation.getDateOperation(), codeUo));
                        operation.setDuree(dureeExpire);

                        operation = saveInformationPersonne(mOperation.getBeneficiaire(), Boolean.TRUE, operation);
                        operation = saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operation);
                        operation = operationRepository.save(operation);
                        operation = controleSeuil(operation);
                        operationService.calculRepartitionFrais(operation, uniteOrganisationSource.getId(), operation.getUoEmetteur().getId());
                        //  operationAuditService.saveOpAudit(operation, null, operation.getStatut());
                        operationService.comptabilisationMobileEnvoiCash(operation.getId());

                        operationAuditService.rechercheSuivi(operation);

                        return new MResponseOperation("0", "Opération effectuée avec succès", operation);

                    } else {
                        return new MResponseOperation("-1", "Lieu d'envoi incorrect", null);
                    }
                } else {
                    return new MResponseOperation("-1", "Impossible d'effectuer cette opération", null);
                }

            } else {
                return new MResponseOperation("-1", "Le point de vente ou le canal  est introuvable.", null);
            }
        } catch (CustomParameterizedException e) {
            operationService.sendAckToOmnicanal(operation, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operation, e.getMessage());
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        }

    }

    public String getFacturierFse(Long id) {
        String codeUo = null;
        Facturier fac = facturierRepository.findById(id).orElse(null);
        codeUo = fac.getUniteOrganisation().getCodeUO();

        return codeUo;
    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponseOperation doOperationApprovisionnement(MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        Operation operationApprovis = null;
        String description;
        try {

            BigDecimal montant = BigDecimal.ZERO;
            if (mOperation != null) {

                log.info(" : Operation d'approvissionnement en cours ...");
                User userConnecte = userService.findeByLogin(mOperation.getUserConnecte());

                PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());
                Optional<PointVente> pointVenteExist = Optional.of(opt);

                log.info(" : Operation d'approvissionnement par {} ...", userConnecte);
                operationService.userCanDoOperations(userConnecte);

                if (userConnecte != null && mOperation.getTypeService().equals(TypeServiceLibelle.VENTE_DEPOSIT_CARDLESS.toString())) {
                    List<UserControlDTO> checkUserAutorise = userControlService.findUserAutoriseCardless(ActionEnum.ACTIVE_VENTE_DEPOSIT_CARDLESS.name(), userConnecte.getPhoneNumber());
                    if (checkUserAutorise.isEmpty()) {
                        return new MResponseOperation("-1", "L'utilisateur n'est pas Autorisé à effectuer cette opération", null);
                    }

                    PointVente pv = pointVenteService.findByOne(userConnecte.getPointVente().getId());
                    pointVenteExist = Optional.of(pv);

                }

                UniteOrganisation userOrganisation = userConnecte.getUniteOrganisation();
                userOrganisation = uniteOrganisationService.findOneById(userOrganisation.getId());
                //Si c'est un master on verifie la disponibilitï¿½ du montant necessaire ï¿½ l'approvisionnement
                log.info(" : Operation d'approvissionnement type {} ...", String.valueOf(mOperation.getTypeService()));
                TypeService typeService = typeServiceService.findByService(String.valueOf(mOperation.getTypeService()));
                // TypeService typeService = typeServiceService.findByCodeService(String.valueOf(mOperation.getTypeService()));//
                Canal canal = canalService.findCanal(mOperation.getCanal());
                // Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
                log.info("VERIFICATION AUTORISATION  ...");
                if (pointVenteExist.isPresent() && typeService != null && canal != null) {
                    if (operationService.verifAutorisationOfTypeService(typeService, userOrganisation)) {
                        Compte compteOp = compteService.getCompteOperationActeur(userOrganisation, "CPTEOPERATIONACTEURPTI");
                        if (userOrganisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.MASTER)) || userOrganisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.MARCHAND)) || userOrganisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.SOUS_DISTRIBUTEUR))) {
                            operationService.verifFonds(mOperation.getMontant(), userOrganisation, EtiquetteCompteCode.valueOf(compteOp.getEtiquette()));
                        }

                        Compte compteOperationActeurSource = compteService.getCompteOperationActeur(userOrganisation);
                        log.info("RECUPERATION COMPTE SOURCE {}  ...", compteOperationActeurSource);
                        if (compteOp != null) {
                            verifIfAutoriser(compteOp.getValeurCompte(), mOperation.getActeur(), mOperation.getIdentifiant(), null);
                        }
                        //verification de la validitï¿½ du bordereau en agence
                        UniteOrganisation organisation = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(mOperation.getCodeUo(), false);

                        Compte compteOperationDest = compteService.getCompteOperationActeur(organisation);
                        log.info("RECUPERATION COMPTE DEST {}  ...", compteOperationDest);
                        if (organisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.MARCHAND))) {
                            throw new CustomParameterizedException("Le marchand ne peut pas acheter de déposit.");
                        }
                        Operation operationApprovisionnement = new Operation();
                        if (mOperation.getMontant() != null) {
                            montant = mOperation.getMontant();
                        }

                        if (mOperation.getTypeService().equals(TypeServiceLibelle.VENTE_DEPOSIT_CARDLESS.toString())) {
                            operationApprovisionnement = saveInformationPersonne(mOperation.getEmetteur(), Boolean.FALSE, operationApprovisionnement);
                        }
                        mOperation = getMontantCalculFrais(mOperation, mOperation.getCodePv(), mOperation.getCanal(), mOperation.getCodeUoEm(), mOperation.getCodeUoAcq(), mOperation.getMontant());
                        //init operation
                        log.info("ENREGISTREMENT OPERATION APPROVISSIONNEMENT COMPTE DEST   ...");
                        operationApprovisionnement.statut(StatutOperation.ENATTENTE)
                                .beneficaire(null)
                                .canal(canal)
                                .typeService(typeService)
                                .pointVente(pointVenteExist.get())
                                .montantOp(montant)
                                .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                                .montantFraisAcq(Util.getZeroIfNull(mOperation.getMontantTaxe()))
                                .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()))
                                .dateOperation(ZonedDateTime.now())
                                .etape(Etape.PROCEED)
                                .uoAcquereur(organisation)
                                .uoEmetteur(userOrganisation)
                                .user(userConnecte)
                                .codeOperation(RandomUtil.getCodeOperation(operationApprovisionnement.getDateOperation(), userOrganisation.getCodeUO()));

                        operationApprovisionnement.compteDest(compteOperationDest);
                        operationApprovisionnement.compteSrc(compteOperationActeurSource);
                        description = getDescription(operationApprovisionnement);
                        if (description != null) {
                            if (mOperation.getDescription() != null) {
                                description = mOperation.getDescription() + " " + description;
                            }
                            operationApprovisionnement.setDescription(description);
                        }
                        operationApprovis = operationRepository.save(operationApprovisionnement);//Enregistrement d'une operation
                        operationService.calculRepartitionFrais(operationApprovis, userOrganisation.getId(), organisation.getId());

                        if (mOperation.getTypeService().equals(TypeServiceLibelle.VENTE_DEPOSIT_CARDLESS.toString())) {
                            UniteOrganisation uoCardeless = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteOperationDest.getCompteAutre().getCodeFse());
                            if (compteOperationDest.getCompteAutre() != null) {
                                setCardlessOperation(operationApprovis, uoCardeless, pointVenteExist);
                            }
                        }
                        operationService.comptablisationApprovisionnement(operationApprovis.getId());
                        log.info("FIN ENREGISTREMENT OPERATION APPROVISSIONNEMENT COMPTE DEST {}  ...", operationApprovis);

                        return new MResponseOperation("0", "Opï¿½ration effectuï¿½e avec succï¿½s", operationApprovis);
                    }
                } else {
                    return new MResponseOperation("-1", "Type de service inactif", null);
                }

            } else {
                return new MResponseOperation("-1", "Impossible de traiter cette opération.", null);
            }
            return new MResponseOperation("-1", "Impossible de traiter cette opération.", null);
        } catch (CustomParameterizedException e) {
            operationService.sendAckToOmnicanal(operationApprovis, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new MResponseOperation("-1", e.getMessage(), null);
        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operationApprovis, e.getMessage());
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        }

    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponseOperation doOperationPaiementMarchandPI(PointVente opt, UniteOrganisation uoSrc, UniteOrganisation uoDest, MOperation mOperation, Compte compteSrc, Compte compteDest, String donneurOrdre, String beneficiaire) throws IOException, NoSuchAlgorithmException, JAXBException {
        Operation operationApprovis = null;
        String description;
        try {
            BigDecimal montant = BigDecimal.ZERO;
            if (mOperation != null) {
                log.info(" : Operation d'approvissionnement en cours ...");

                Optional<PointVente> pointVenteExist = Optional.of(opt);
                log.info(" : Operation Paiement Marchand PI {} ...", String.valueOf(mOperation.getTypeService()));
                TypeService typeService = typeServiceService.findByService(String.valueOf(mOperation.getTypeService()));
                // TypeService typeService = typeServiceService.findByCodeService(String.valueOf(mOperation.getTypeService()));//
                Canal canal = canalService.findCanal(mOperation.getCanal());
                // Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
                log.info("VERIFICATION AUTORISATION  ...");
                if (pointVenteExist.isPresent() && typeService != null && canal != null) {

                    if (operationService.verifAutorisationOfTypeService(typeService, compteSrc.getUniteOrganisation())) {

                        //verification de la validitï¿½ du bordereau en agence
                        Operation operationApprovisionnement = new Operation();
                        if (mOperation.getMontant() != null) {
                            montant = mOperation.getMontant();
                        }
                        mOperation = getMontantCalculFrais(mOperation, mOperation.getCodePv(), mOperation.getCanal(), mOperation.getCodeUoEm(), mOperation.getCodeUoAcq(), mOperation.getMontant());
                        //init operation
                        log.info("ENREGISTREMENT OPERATION APPROVISSIONNEMENT COMPTE DEST   ...");
                        operationApprovisionnement.statut(StatutOperation.ENATTENTE)
                                .beneficaire(null)
                                .canal(canal)
                                .emetteur(null)
                                .beneficaire(null)
                                .typeService(typeService)
                                .pointVente(pointVenteExist.get())
                                .montantOp(montant)
                                .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                                .montantFraisAcq(Util.getZeroIfNull(mOperation.getMontantTaxe()))
                                .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()))
                                .dateOperation(ZonedDateTime.now())
                                .etape(Etape.PROCEED)
                                .uoAcquereur(uoDest)
                                .uoEmetteur(uoSrc)
                                .codeOperation(RandomUtil.getCodeOperation(operationApprovisionnement.getDateOperation(), uoSrc.getCodeUO()));

                        operationApprovisionnement.compteDest(compteDest);
                        operationApprovisionnement.compteSrc(compteSrc);
                        operationApprovisionnement.setDonneurOrdre(donneurOrdre);
                        description = getDescription(operationApprovisionnement);
                        if (description != null) {
                            if (mOperation.getDescription() != null) {
                                description = mOperation.getDescription() + " " + description;
                            }
                            operationApprovisionnement.setDescription(description);
                        }
                        operationApprovis = operationRepository.save(operationApprovisionnement);//Enregistrement d'une operation
                        operationService.calculRepartitionFrais(operationApprovis, uoSrc.getId(), uoDest.getId());

                        OperationDTO op = operationService.comptabilisationServiceExterieur(operationApprovis, compteSrc, compteDest, operationApprovis.getMontantOp(), operationApprovis.getCodeOperation(), operationApprovis.getDescription(), donneurOrdre, beneficiaire);
                        log.info("FIN ENREGISTREMENT OPERATION APPROVISSIONNEMENT COMPTE DEST {}  ...", operationApprovis);
                        if (op != null) {
                            operationApprovis.statut(StatutOperation.PAYE);
                            operationRepository.save(operationApprovis);
                        }

                        return new MResponseOperation("0", "Opération effectuée avec succes", operationApprovis);
                    }
                } else {
                    return new MResponseOperation("-1", "Type de service inactif", null);
                }

            } else {
                return new MResponseOperation("-1", "Impossible de traiter cette opération.", null);
            }
            return new MResponseOperation("-1", "Impossible de traiter cette opération.", null);
        } catch (CustomParameterizedException e) {
            operationService.sendAckToOmnicanal(operationApprovis, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new MResponseOperation("-1", e.getMessage(), null);
        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operationApprovis, e.getMessage());
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        }

    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponseOperation doDepositToBank(MOperation mOperation) throws IOException, NoSuchAlgorithmException, JAXBException {
        Operation operationApprovis = null;
        String codeUo;
        try {
            BigDecimal montant = BigDecimal.ZERO;
            if (mOperation != null) {
                log.info(" : Operation d'approvissionnement en cours ...");
                User userConnecte = userService.findeByLogin(mOperation.getUserConnecte());
                operationService.userCanDoOperations(userConnecte);
                UniteOrganisation userOrganisation = userConnecte.getUniteOrganisation();
                UniteOrganisation fseDest = uniteOrganisationService.findUoParentByType(userOrganisation, TypeUO.FSE);
                UniteOrganisation uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(fseDest);
                PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());
                Optional<PointVente> pointVenteExist = Optional.of(opt);
                // Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());
                if (!pointVenteExist.isPresent()) {
                    return new MResponseOperation("-1", "Point de vente introuvable.", null);
                }
                Compte compteOp = null;
                Compte compteOperationActeurSource = null;
                Compte compteOperationDest = null;
                UniteOrganisation organisationDest = null;
                UniteOrganisation organisationSrc = null;
                //Si c'est un master on verifie la disponibilitï¿½ du montant necessaire ï¿½ l'approvisionnement

                TypeService typeService = typeServiceService.findByService(String.valueOf(mOperation.getTypeService()));
                Canal canal = canalService.findCanal(mOperation.getCanal());
                // Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
                if (operationService.verifAutorisationOfTypeService(typeService, userOrganisation)) {

                    if (mOperation.getTypeService().equals(TypeServiceLibelle.AUTO_ALLOCATION.toString())) {
                        // la source est un compte bancaire
                        compteOperationActeurSource = compteService.getCompteOperationActeur(userOrganisation, "CPTACTORBANK");
                        //* la destination est compte money électronique
                        compteOperationDest = compteService.getCompteOperationActeur(userOrganisation);
                        //organisationDest = userOrganisation;
                        if (compteOperationDest.getCompteBanque() != null) {
                            organisationDest = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteOperationDest.getCompteBanque().getCodeFse());
                        } else {
                            organisationDest = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteOperationDest.getCompteAutre().getCodeFse());
                        }
                        if (compteOperationActeurSource.getCompteBanque() != null) {
                            organisationSrc = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteOperationActeurSource.getCompteBanque().getCodeFse());
                        } else {
                            organisationSrc = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteOperationActeurSource.getCompteAutre().getCodeFse());
                        }
                    } else if (mOperation.getTypeService().equals(TypeServiceLibelle.SWEEP_DEPOSIT_TO_BANK.toString())) {
                        if (userOrganisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.MASTER)) || userOrganisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.MARCHAND)) || userOrganisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.SOUS_DISTRIBUTEUR))) {
                            operationService.verifFonds(mOperation.getMontant(), userOrganisation, EtiquetteCompteCode.CPTEOPERATIONACTEURPTI);
                        }
                        compteOperationActeurSource = compteService.getCompteOperationActeur(userOrganisation);//recupération du compte de l'acteur
                        compteOp = compteService.getCompteOperationActeur(userOrganisation, "CPTEOPERATIONACTEURPTI");
                        // la source est un compte bancaire
                        compteOperationDest = compteService.getCompteOperationActeur(userOrganisation, "CPTACTORBANK");

                        if (compteOperationDest.getCompteBanque() != null) {
                            organisationDest = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteOperationDest.getCompteBanque().getCodeFse());
                        } else {
                            organisationDest = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteOperationDest.getCompteAutre().getCodeFse());
                        }
                        /**
                         * Organisation des
                         */

                        //    organisationDest = uoDestAgenceSiege;
                        organisationSrc = userOrganisation;
                    }

                    if (compteOp != null) {
                        verifIfAutoriser(compteOp.getValeurCompte(), mOperation.getActeur(), mOperation.getIdentifiant(), null);
                    }
                    //verification de la validitï¿½ du bordereau en agence

                    // Compte compteOperationDest = compteService.getCompteOperationActeur(uoDestAgenceSiege);
                    if (organisationDest.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.MARCHAND))) {
                        throw new CustomParameterizedException("Le marchand ne peut pas acheter de déposit.");
                    }
                    Operation operationApprovisionnement = new Operation();
                    if (mOperation.getMontant() != null) {
                        montant = mOperation.getMontant();
                    }
                    if (organisationSrc != null) {
                        codeUo = organisationSrc.getCodeUO();
                    }
                    mOperation = getMontantCalculFrais(mOperation, mOperation.getCodePv(), mOperation.getCanal(), mOperation.getCodeUoEm(), mOperation.getCodeUoAcq(), mOperation.getMontant());
                    //init operation
                    operationApprovisionnement.statut(StatutOperation.ENATTENTE)
                            .beneficaire(null)
                            .canal(canal)
                            .emetteur(null)
                            .beneficaire(null)
                            .typeService(typeService)
                            .pointVente(userConnecte.getPointVente())
                            .montantOp(montant)
                            .montantFrais(Util.getZeroIfNull(mOperation.getMontantFrais()))
                            .montantFraisAcq(Util.getZeroIfNull(mOperation.getMontantTaxe()))
                            .montantTaxe(Util.getZeroIfNull(mOperation.getMontantTaxe()))
                            .dateOperation(ZonedDateTime.now())
                            .etape(Etape.PROCEED)
                            .uoAcquereur(organisationDest)
                            .uoEmetteur(organisationSrc)
                            .user(userConnecte)
                            .codeOperation(RandomUtil.getCodeOperation(operationApprovisionnement.getDateOperation(), organisationSrc.getCodeUO()));
                    operationApprovisionnement.compteDest(compteOperationDest);
                    /**
                     * compte bank
                     */
                    operationApprovisionnement.compteSrc(compteOperationActeurSource);
                    operationApprovis = operationRepository.save(operationApprovisionnement);//Enregistrement d'une operation
                    operationService.calculRepartitionFrais(operationApprovis, organisationSrc.getId(), organisationDest.getId());
                    operationService.comptabilisationOperationMad(operationApprovisionnement, pointVenteExist.get(), null);
                    operationApprovisionnement.setStatut(StatutOperation.PAYE);
                    operationApprovis = operationRepository.save(operationApprovisionnement);
                    return new MResponseOperation("0", "Opï¿½ration effectuï¿½e avec succï¿½s", operationApprovis);
                }
            } else {
                return new MResponseOperation("-1", "Impossible de traiter cette opération.", null);
            }
            return new MResponseOperation("-1", "Impossible de traiter cette opération.", null);
        } catch (CustomParameterizedException e) {
            operationService.sendAckToOmnicanal(operationApprovis, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new MResponseOperation("-1", e.getMessage(), null);
        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operationApprovis, e.getMessage());
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        }

    }

    /**
     * Service permettant de faire un transfert cash vers compte.
     *
     * @param mOperation
     * @return
     */
    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponseOperation doOperationTransfertVersCompte(MOperation mOperation) throws NoSuchAlgorithmException, IOException, JAXBException {
        Operation operationRetrait = new Operation();
        String codeUo;
        try {
            log.info("Operation de RETRAIT en cours ....");
            // Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());
            PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());

            Optional<PointVente> pointVenteExist = Optional.of(opt);

            Canal canal = canalService.findCanal(mOperation.getCanal());
            //  Canal canal = canalService.findByCodeCanal(mOperation.getCanal());
            log.debug("retrait%%" + mOperation.getCodeRetrait());
            log.debug("retrait%%" + mOperation.getCanal());
            log.debug("retrait%%" + mOperation.getCodePv());

            if (pointVenteExist.isPresent() && canal != null && mOperation.getCodeRetrait() != null) {

                UniteOrganisation organisation = uniteOrganisationService.findOneById(pointVenteExist.get().getUniteOrganisation().getId());
                codeUo = organisation.getCodeUO();
                if (!canal.getCodeCanal().equals("MOBILE") && !canal.getCodeCanal().equals("USSD")) {
                    return new MResponseOperation("-1", "Impossible de faire l'opération à travers ce canal." + canal.getCodeCanal(), null);
                }
                if (mOperation.getBeneficiaire() != null
                        && mOperation.getBeneficiaire().getTitulaire() != null
                        && mOperation.getBeneficiaire().getTitulaire().getTelephone() != null) {
                    OperationAllDTO operationAllDTO = operationService.verificationOperationWithoutAction(mOperation.getCodeRetrait(), pointVenteExist.get());
                    if (operationAllDTO != null && operationAllDTO.getBeneficiaire() != null) {
                        log.debug("Operation de RETRAIT en cours %%....");
                        log.info("Operation de RETRAIT en cours %%....");
                        PersonneDTO beneficiaire = operationAllDTO.getBeneficiaire().getPersonneDTO();
                        String telephone = beneficiaire.getTelephone();
                        String codeTelephone = beneficiaire.getCodePaysTelephone();
                        if (telephone != null && codeTelephone != null) {
                            if (telephone.equals(mOperation.getBeneficiaire().getTitulaire().getTelephone())
                                    && codeTelephone.equals(mOperation.getBeneficiaire().getTitulaire().getCodeTelephone())) {
                                log.info("Ok pour les conditions de realisation ....");
                                Operation operation = operationMapper.toEntity(operationAllDTO.getOperationDTO());
                                operation.statut(StatutOperation.PAYE);
                                operation = operationRepository.save(operation);
                                //  operationAuditService.saveOpAudit(operation, operationAllDTO.getOperationDTO().getStatut(), StatutOperation.PAYE);
                                operationRetrait = new Operation();
                                TypeService typeService = typeServiceService.findByService(String.valueOf(TypeServiceLibelle.RECEPTION_TRANSFERT));
                                operationRetrait.compteDest(getCompte(mOperation.getBeneficiaire().getCompte(), operation.getBeneficaire().getPersonne(), mOperation.getApplication(), Boolean.TRUE));
                                operationRetrait.statut(StatutOperation.ENATTENTE)
                                        .beneficaire(operation.getBeneficaire())
                                        .canal(canal)
                                        .emetteur(operation.getEmetteur())
                                        .beneficaire(operation.getBeneficaire())
                                        .typeService(typeService)
                                        .pointVente(pointVenteExist.get())
                                        .uoAcquereur(pointVenteExist.get().getUniteOrganisation())
                                        .montantOp(operation.getMontantOp())
                                        .montantOpEnDevise(operation.getMontantOpEnDevise())
                                        .dateOperation(ZonedDateTime.now())
                                        .etape(Etape.PROCEED)
                                        .uoEmetteur(pointVenteExist.get().getUniteOrganisation())
                                        .devisePaiement(operation.getDevisePaiement())
                                        .operationTraite(operation)
                                        .codeOperation(RandomUtil.getCodeOperation(operation.getDateOperation(), codeUo));
                                operationRetrait.setPieceBeneficiaire(operation.getPieceEmetteur());

                                operationRetrait = controleSeuil(operationRetrait);

                                operationRetrait = operationRepository.save(operationRetrait);
                                //  operationAuditService.saveOpAudit(operationRetrait, null, StatutOperation.PAYE);
                                Result response = null;

                                Long fseDestId = operationRetrait.getUoAcquereur().getId();
                                Long fseSrcId = operationRetrait.getUoEmetteur().getId();
                                UniteOrganisation fseDest = uniteOrganisationService.findById(fseDestId);//Fse Dest
                                if (!fseDest.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.toString())) {
                                    if (fseDest.getTypeUO().getLibelleTypeUO().equals(TypeUO.PAYS.toString())) {
                                        fseDest = uniteOrganisationService.getFilialeOfUniteOrganisation(fseDest);
                                    } else {
                                        fseDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.FSE);
                                    }
                                }
                                UniteOrganisation fseSrc = uniteOrganisationService.findById(fseSrcId); //Fse Acq

                                if (!fseSrc.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.toString())) {
                                    if (fseSrc.getTypeUO().getLibelleTypeUO().equals(TypeUO.PAYS.toString())) {
                                        fseSrc = uniteOrganisationService.getFilialeOfUniteOrganisation(fseSrc);
                                    } else {
                                        fseSrc = uniteOrganisationService.findUoParentByType(fseSrc, TypeUO.FSE);
                                    }
                                }

                                //  UniteOrganisation uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(fseDest);
                                // UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);
                                //UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq
                                //UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(pointVenteExist.get().getUniteOrganisation(), TypeUO.PAYS);
                                //Result response = apiService.sendRequestComptabilisationOfCashAccountToOmnicanal(uoPaysSrc, uoPaysDest, operation, fseSrc, fseDest, compte, Boolean.TRUE, Boolean.TRUE);
                                //  verifResultOfOmnicanal(response, operation);
                                //response =    apiService.sendRequestComptabilisationOfCashTransfertToOmnicanal(uoPaysSrc, uoPaysDest,operationRetrait,  fseSrc,fseDest, operationRetrait.getCompteDest(), Boolean.TRUE, Boolean.TRUE);
                                operationService.comptabilisationReceptionCash(operationRetrait.getId());

                                operationAuditService.rechercheSuivi(operation);

                                return new MResponseOperation("0", "Opération effectuée avec succées.", operationRetrait);

                            } else {
                                return new MResponseOperation("-1", "Le client n'est pas autorisation faire ce retrait.", null);
                            }
                        } else {
                            return new MResponseOperation("-1", "Le numï¿½ro du bnénéficiaire est introuvable.", null);
                        }
                    } else {
                        return new MResponseOperation("-1", "Impossible de traiter cette opération.", null);
                    }
                } else {
                    return new MResponseOperation("-1", "Les informations du benefiaire sont incorrectes.", null);
                }
            } else {
                return new MResponseOperation("-1", "Le point de vente ou le canal ou le code retrait est introuvable.", null);
            }
        } catch (CustomParameterizedException e) {
            operationService.sendAckToOmnicanal(operationRetrait, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operationRetrait, e.getMessage());
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        }

    }

    /**
     * Service permettant de faire un retrait de fonds.
     *
     * @param mOperation
     * @return
     */
    @Transactional(noRollbackFor = CustomSettlementException.class)
    public MResponseOperation doOperationRetrait(MOperation mOperation) throws NoSuchAlgorithmException, IOException, JAXBException {
        Operation operationRetrait = new Operation();
        String codeUo = "";
        try {
            log.info("Operation de RETRAIT en cours ....");
            //   Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mOperation.getCodePv());
            PointVente opt = pointVenteService.findPointByCode(mOperation.getCodePv());

            Optional<PointVente> pointVenteExist = Optional.of(opt);

            Canal canal = canalService.findCanal(mOperation.getCanal());
            // Canal canal = canalService.findByCodeCanal(mOperation.getCanal());

            if (pointVenteExist.isPresent() && canal != null && mOperation.getCodeRetrait() != null) {
                UniteOrganisation organisation = uniteOrganisationService.findOneById(pointVenteExist.get().getUniteOrganisation().getId());
                codeUo = organisation.getCodeUO();
                if (!canal.getCodeCanal().equals("MOBILE")) {
                    return new MResponseOperation("-1", "Impossible de faire l'opération à travers ce canal." + canal.getCodeCanal(), null);
                }
                if (mOperation.getBeneficiaire() != null
                        && mOperation.getBeneficiaire().getTitulaire() != null
                        && mOperation.getBeneficiaire().getTitulaire().getTelephone() != null) {
                    OperationAllDTO operationAllDTO = operationService.verificationOperation(mOperation.getCodeRetrait(), pointVenteExist.get());
                    if (operationAllDTO != null && operationAllDTO.getBeneficiaire() != null) {
                        log.debug("Operation de RETRAIT en cours %%....");
                        log.info("Operation de RETRAIT en cours %%....");
                        PersonneDTO beneficiaire = operationAllDTO.getBeneficiaire().getPersonneDTO();
                        String telephone = beneficiaire.getTelephone();
                        String codeTelephone = beneficiaire.getCodePaysTelephone();
                        if (telephone != null && codeTelephone != null) {
                            if (telephone.equals(mOperation.getBeneficiaire().getTitulaire().getTelephone())
                                    && codeTelephone.equals(mOperation.getBeneficiaire().getTitulaire().getCodeTelephone())) {
                                log.info("Ok pour les conditions de realisation ....");
                                Operation operation = operationMapper.toEntity(operationAllDTO.getOperationDTO());
                                operation.statut(StatutOperation.PAYE);
                                operation = operationRepository.save(operation);
                                Operation op = operation;

                                //   operationAuditService.saveOpAudit(operation, operationAllDTO.getOperationDTO().getStatut(), StatutOperation.PAYE);
                                operationRetrait = new Operation();
                                TypeService typeService = typeServiceService.findByService(String.valueOf(TypeServiceLibelle.RETRAIT));
                                // op.setTypeService(typeService);
                                //  op.setPointVente(pointVenteExist.get());
                                operationRetrait.statut(StatutOperation.PAYE)
                                        .beneficaire(operation.getBeneficaire())
                                        .canal(canal)
                                        .description("OPERATION DE RETRAIT")
                                        .emetteur(operation.getEmetteur())
                                        .beneficaire(operation.getBeneficaire())
                                        .typeService(typeService)
                                        .pointVente(pointVenteExist.get())
                                        .uoAcquereur(pointVenteExist.get().getUniteOrganisation())
                                        .montantOp(operation.getMontantOp())
                                        .montantFrais(Util.getZeroIfNull(repartitionOpService.getMontantByActeurAndOp(Acteur.ACQ, operation.getId())))
                                        .montantFraisAcq(Util.getZeroIfNull(repartitionOpService.getMontantByActeurAndOp(Acteur.ACQ, operation.getId())))
                                        .montantOpEnDevise(operation.getMontantOpEnDevise())
                                        .dateOperation(ZonedDateTime.now())
                                        .etape(Etape.PROCEED)
                                        .uoEmetteur(pointVenteExist.get().getUniteOrganisation())
                                        .devisePaiement(operation.getDevisePaiement())
                                        .operationTraite(operation)
                                        .codeOperation(RandomUtil.getCodeOperation(operation.getDateOperation(), codeUo));
                                operationRetrait.setPieceBeneficiaire(operation.getPieceEmetteur());
                                operationRetrait = operationRepository.save(operationRetrait);
                                // operationAuditService.saveOpAudit(operationRetrait, null, StatutOperation.PAYE);
                                // Result response = apiService.sendRequestForComptabilisationRetrait(operationRetrait, operationRetrait.getPointVente().getUniteOrganisation());
                                // instructionComptableService.comptabiliserOperation(operationRetrait, null, uoAcq);
                                operationService.comptabilisationRetrait(operation, operationRetrait.getPointVente().getUniteOrganisation(), operationRetrait);

                                operationAuditService.rechercheSuivi(operation);

                                return new MResponseOperation("0", "Opération effectué avec succès.", operationRetrait);

                            } else {
                                return new MResponseOperation("-1", "Le client n'est pas autorisï¿½ ï¿½ faire ce retrait.", null);
                            }
                        } else {
                            return new MResponseOperation("-1", "Le numï¿½ro du bï¿½nï¿½ficiaire est introuvable.", null);
                        }
                    } else {
                        return new MResponseOperation("-1", "Impossible de traiter cette opération.", null);
                    }
                } else {
                    return new MResponseOperation("-1", "Les informations du benefiaire sont incorrectes.", null);
                }
            } else {
                return new MResponseOperation("-1", "Le point de vente ou le canal ou le code retrait est introuvable.", null);
            }
        } catch (CustomParameterizedException e) {
            operationService.sendAckToOmnicanal(operationRetrait, e.getMessage());
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        } catch (CustomSettlementException e) {
            operationService.sendAckToOmnicanal(operationRetrait, e.getMessage());
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return showMessage("-1", e.getMessage(), null);
        }

    }

    /**
     * Faire la souscription du compte d'un client
     */
    private class MessageSender implements Runnable {

        TypeService typeservice;
        Personne personne;

        public MessageSender(TypeService typeservice, Personne personne) {
            this.typeservice = typeservice;
            this.personne = personne;
        }

        public void run() {
            operationService.envoiMsgOperation(typeservice, personne);
        }

    }

    public MResponse doSouscription(MClient client) {
        String message;
        Personne personne = savePersonne(client.getPersonne());
        //  getCompte(client.getCompte(), personne);
        //   saveNewCompte(client.getCompte(), personne);
        if (personne != null) {
            saveDocIdentification(personne, client.getDocIdentification());
            /**/
            Client c = saveClient(client, personne);
            if (c != null) {
                Compte cpt = creationCompte(client, personne);
                if (cpt != null) {
                    TypeService typeservice = new TypeService();
                    typeservice.setCode("ENROLEMENT");
                    taskExecutor.execute(new MessageSender(typeservice, personne));
                    return new MResponse("0", "Opération effectuée avec succès.");

                } else {
                    return new MResponse("0", "Impossible de créer le compte");
                }
            } else {
                return new MResponse("-1", "Erreur lors de la création du compte");
            }
        } else {
            return new MResponse("0", "le client existe ou Erreur lors de la création du compte");
            // throw new CustomParameterizedException("Impossible d'enrï¿½ler ce client.");
        }

    }

    public MResponseOrga doInscriptionTerminal(MTerminal terminal) {
        UniteOrganisation uo = uniteOrganisationService.findByCodeAndTelephone(terminal.getCodeUo(), terminal.getTelephone(), terminal.getCodeIndicatifPays());
        MOrganisation morga = new MOrganisation();
        User utilisateur = userService.findByNumeroTelephone(terminal.getTelephone(), terminal.getLogin().toLowerCase());
        Terminal terminalFind = terminalRepository.findByUiid(terminal.getUuid());
        if (uo != null) {
            morga.setCodeUo(uo.getCodeUO());
            morga.setNom(uo.getLibelleUniteOrganisation());
            if (!uo.getTypeUO().getCodeTypeUO().equals("MARCHAND")) {
                morga.setType(MTypeOrganisation.valueOf("AGENT"));
                terminal.setTypeTerminal("AGENT");
            } else {
                morga.setType(MTypeOrganisation.valueOf("MARCHAND"));
                terminal.setTypeTerminal("MARCHAND");
            }

        } else {
            return new MResponseOrga("-1", "Erreur lors de l'enregistrement ACTEUR non reconnu", null);
        }
        if (terminalFind != null) {
            return new MResponseOrga("0", "Terminal existe", morga);
        } else {
            Terminal term = new Terminal();
            term.setUuid(terminal.getUuid());
            term.setModel(terminal.getModel());
            term.setPlatform(terminal.getPlatform());
            term.setTypeTerminal(TypeTerminal.valueOf(terminal.getTypeTerminal()));
            term.setCodeUo(terminal.getCodeUo());
            term.setCodeTerminal(terminal.getTelephone());
            term.setCodePays(terminal.getCodeIndicatifPays());
            term.setManufacturer(terminal.getManufacturer());
            Terminal termS = null;
            if (uo != null && utilisateur != null) {
                if (utilisateur.getUniteOrganisation().equals(uo)) {
                    termS = terminalRepository.save(term);
                } else {
                    log.error("Erreur lors de l'enregistrement l'utiliateur n'appartient Ã  cet acteur");
                    return new MResponseOrga("-1", "Erreur lors de l'enregistrement l'utiliateur n'appartient Ã  cet acteur", null);
                }
                //  getCompte(client.getCompte(), personne);
                //   saveNewCompte(client.getCompte(), personne);
                if (termS == null) {
                    log.error("Erreur lors de l'enregistrement");
                    return new MResponseOrga("-1", "Erreur lors de l'enregistrement ", null);
                } else {
                    // Précision que l'utilisateur utilise l'application mobile
                    utilisateur.setIsBackOffice(0);
                    userRepository.save(utilisateur);
                    log.debug("Terminal enregistrï¿½ avec succï¿½s");
                    return new MResponseOrga("0", "Terminal enregistré avec succès", morga);
                }
            } else {
                return new MResponseOrga("-1", "Erreur lors de l'enregistrement l'utiliateur n'appartient ï¿½ cet acteur", null);
            }
        }

    }

    public Compte creationCompte(MClient client, Personne personne) {
        log.info(" création du compte......");
        String codeFse = client.getCodeFse();
        return saveCompteMobile(personne, codeFse);
    }

    private String getNumeroCompteCourt() {

        String numeroCourt = compteService.findMaxNumeroCourt();
        String compte = null;
        if (numeroCourt != null) {
            Long numeroEnEntier = Long.parseLong(numeroCourt) + 1;
            compte = String.format("%08d", numeroEnEntier);
        }
        return compte;
    }

    private String getNumeroCompte(String numeroCourt, String codeFse) {
        /* le numï¿½ro de tï¿½lephone contitue le numero de compte*/
        String numeroRandom = RandomStringUtils.randomNumeric(1);
        String cptCompte = codeFse.concat(numeroCourt) + numeroRandom;
        return cptCompte;
    }

    private Compte saveCompteMobile(Personne personne, String codeFse) {
        Compte compte;
        Client clientExist = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personne.getId());
        if (clientExist != null) {
            String numeroCourt = getNumeroCompteCourt();
            String valeurCpte = getNumeroCompte(numeroCourt, codeFse);
            Compte cpte = compteService.findByCompteFse(valeurCpte, codeFse);
            if (cpte == null) {
                compte = new Compte();
                compte.valeurCompte(valeurCpte)
                        .client(clientExist)
                        .etiquette(EtiquetteCompteCode.CPTECLIENT.toString());
                UniteOrganisation fseTeneur = uniteOrganisationService.findByCodeFSE(codeFse);
                Compte compteExist = compteService.findComptePrincipal(clientExist);

                if (fseTeneur != null) {
                    if (compteExist != null) {
                        //  compte.setClasseCompte(ClasseCompte.SOUSCOMPTE);
                    } else {
                        compte.setClasseCompte(ClasseCompte.PRINCIPAL);
                        compte.categorieCompte(CategorieCompte.AUTRE);
                        compte.setStatutCompte(StatutCompte.ACTIVE);
                        compte.setEtatCompte(EtatCompte.OUVERT);
                        CompteAutre compteAutre = new CompteAutre();
                        compteAutre.setCodeFse(codeFse);
                        compteAutre.setSolde(BigDecimal.ZERO);
                        compteAutre = compteAutreRepository.save(compteAutre);
                        compte.compteAutre(compteAutre);
                        compte.setNumeroCourt(numeroCourt);
                        compteRepository.save(compte);
                    }

                } else {
                    log.error("Fournisseur de service introuvable");
                    throw new CustomParameterizedException("Fournisseur de service introuvable.");
                }
            } else {
                compte = cpte;
                log.error("le compte existe déjà ");
            }

            ///                throw new CustomParameterizedException("le compte existe dÃ©jÃ .");
        } else {
            throw new CustomParameterizedException("Client introuvable");
        }

        return compte;
    }

    public MResponse desactiveCLient(MPersonne personne) {
        Personne personneExist = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(personne.getTelephone(), personne.getCodeTelephone());
        if (personneExist != null) {
            Client clientExist = clientRepository.findByPersonneIdAndClientValideTrueAndSupprimeIsFalse(personneExist.getId());
            if (clientExist != null) {
                clientExist.setClientValide(Boolean.FALSE);
                Client client = clientRepository.save(clientExist);
                //Enrengistrement de la dï¿½sactivation
                String codePays = personneExist.getCodePaysTelephone();
                String login;
                if (personneExist.getTypePersonne().equals(TypePersonne.MORALE)) {
                    login = personneExist.getEmail();
                } else {
                    login = personneExist.getTelephone();
                }
                userControlService.disableUser(login, codePays, "CLIENT");
                //fin enregistrement
                if (client != null) {
                    log.debug("desactivation du client {}", client);
                    return new MResponse("0", "OK");
                }

            } else {
                log.error("Client introuvable");
                return new MResponse("-1", "Client introuvable");
            }
        } else {

            return new MResponse("-1", "Client introuvable");
        }
        log.error("Client introuvable");
        return new MResponse("-1", "Client introuvable");
    }

    public MResponse desactiveAgent(MPersonne personne) {
        User user = userService.findeByLogin(personne.getTelephone());

        if (user != null) {
            user.setActivated(false);
            userRepository.save(user);
            userControlService.disableUser(personne.getTelephone(), personne.getCodeTelephone(), "AGENT");
            return new MResponse("0", "OK");

        } else {
            log.error("Client introuvable");
            return new MResponse("-1", "Client introuvable");
        }

    }

    public String isAuthentificationForte() {
        ParametreApp fournisseur = parametreAppRepository.findByTypeAndNom(TypeParametre.SYSTEM, "AUTHENTIFICATION_FORTE");
        if (fournisseur != null) {
            return fournisseur.getValeur();
        }
        return null;
    }

    public String getCardLessFseCode(String codeFseDef) {
        ParametreApp fournisseur = parametreAppRepository.findByTypeAndNomAndCodeFse("CARDLESS_CODE_FSE", codeFseDef);
        if (fournisseur != null) {
            return fournisseur.getValeur();
        }
        return null;
    }

    public String getCardLessEntiteCode(String codeFseDef) {
        ParametreApp fournisseur = parametreAppRepository.findByTypeAndNomAndCodeFse("CARDLESS_ENTITY_CODE", codeFseDef);
        if (fournisseur != null) {
            return fournisseur.getValeur();
        }
        return null;
    }

    public String getProfilAgent() {
        ParametreApp profil = parametreAppRepository.findByTypeAndNom(TypeParametre.SYSTEM, "PROFIL_AGENT_PAR_DEFAUT");
        if (profil != null) {
            return profil.getValeur();
        }
        return null;
    }

    public String getCodeSecretAExlure() {
        ParametreApp fournisseur = parametreAppRepository.findByTypeAndNom(TypeParametre.AUTRE, "CODE_SECRET_EXCLU");
        if (fournisseur != null) {
            return fournisseur.getValeur();
        }
        return null;
    }

    public String getLongueurNumTelephone(String codeFse) {
        ParametreApp longueurTel = parametreAppRepository.findByTypeAndNomAndCodeFse("LONGUEUR_NUM_TELEPHONE ", codeFse);
        if (longueurTel != null) {
            return longueurTel.getValeur();
        }
        return null;
    }

    public String getAgeLimite(String codeFse) {

        ParametreApp ageLimite = parametreAppRepository.findByTypeAndNomAndCodeFse("AGE_LIMITE", codeFse);

        if (ageLimite != null) {
            return ageLimite.getValeur();
        }
        return null;
    }

    public String getDevise(String codeFse) {

        ParametreApp devise = parametreAppRepository.findByTypeAndNomAndCodeFse("DEVISE", codeFse);

        if (devise != null) {
            return devise.getValeur();
        }
        return "FCFA";
    }

    public String getFournisseurMonnaieDef(String codePays) {
        ParametreApp fournisseur = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", codePays);
        if (fournisseur != null) {
            return fournisseur.getValeur();
        }
        return null;
    }

    public String getFournisseurDigital(String codePays) {
        ParametreApp fournisseur = parametreAppRepository.findByTypeAndNomAndCodeFse("CODE_FSE_DIGITAL", codePays);
        if (fournisseur != null) {
            return fournisseur.getValeur();
        }
        return null;
    }

    public String getIfCreateAccount(String codeFse) {
        ParametreApp fournisseur = parametreAppRepository.findByTypeAndNomAndCodeFse("CREATE_ACCOUNT", codeFse);
        if (fournisseur != null) {
            return fournisseur.getValeur();
        }
        return null;
    }

    public String getCodePointVente(String codeFse) {
        ParametreApp pos = parametreAppRepository.findByTypeAndNomAndCodeFse("MOBILE_POS", codeFse);
        if (pos != null) {
            return pos.getValeur();
        }
        return null;
    }

    public Client saveClient(MClient mclient, Personne personne) {
        /**
         * definition du profil du client
         */
        ServiceProfil serviceProfil = new ServiceProfil();
        Client client;
        //Client clientExist = clientRepository.findTop1ByPersonneIdAndClientValideTrueAndSupprimeIsFalse(personne.getId());
        Client clientExist = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personne.getId());
        ParametreApp service = parametreAppRepository.findByTypeAndNomAndCodeFse("SERVICE_CLIENT_NON_IDENTIFIE", mclient.getCodeFse());
        if (clientExist != null) {
            client = clientExist;
        } else {

            ProfilActeur profilActeur = null;
            client = new Client();
            client.setCodeClient(RandomStringUtils.randomNumeric(10) + RandomUtil.generateAlphaNumeric(2).toUpperCase());
            client.setClientExterne(0);
            client.setClientValide(Boolean.TRUE);
            client.setClientIdentifie(Boolean.FALSE);
            client.setDalaisRegularisation(LocalDate.now().plusMonths(3));
            ParametreApp groupe = new ParametreApp();
            /*SET PROFILE GROUP*/
            if (mclient.getProfil() != null) {
                groupe.setValeur(mclient.getProfil());
            } else {
                groupe = parametreAppRepository.findByTypeAndNomAndCodeFse("PROFIL_CLIENT_CLIENT_NON_IDENTIFIE", mclient.getCodeFse());
            }

            if (groupe != null) {
                GroupeProfilActeur groupeProfilActeur = groupeProfilActeurRepository.findById(Long.parseLong(groupe.getValeur())).orElse(null);
                client = clientService.setGroupe(groupeProfilActeur, client);
                //    profilActeur = profilActeurRepository.findById(Long.parseLong(profil.getValeur())).orElse(null);
                // client.setProfilActeur(profilActeur);

            }
            if (service != null) {
                serviceProfil = serviceProfilRepository.findById(Long.parseLong(service.getValeur())).orElse(null);
                client.setProfil(serviceProfil);
            }
        }
        /* enregistrement de l'unite d'organisation createur */
        if (mclient.getCodePv() != null) {
            UniteOrganisation uo = uniteOrganisationService.findByCodePv(mclient.getCodePv());
            client.setUniteOrganisationCreateur(uo);

        }
        //  client.setProfil(profil);
        client.setPersonne(personne);
        client = clientRepository.save(client);
        if (client != null) {
            // if (service != null) {
            // serviceProfil = serviceProfilRepository.findOneWithEagerRelationships(Long.parseLong(service.getValeur()));
            //    profileAct = profilActeurRepository.findById(profilacteur).orElse(null); // definition du profil
            // if (serviceProfil != null) {

            //  List<Long> serviceIds = serviceProfil.getTypeServices().stream().map(typeService -> typeService.getId()).collect(Collectors.toList());
            //   autorisationService.saveAutoriastions(client, serviceIds);
            //  }
            //  }
            System.out.println("SAVED DOC");
            saveDocument(mclient.getPersonne().getImageDocumentIdentification(), client);
        }
        return client;
    }

    /**
     * Enregistrement du document d'identification
     *
     * @param personne
     * @param docIdentification
     */
    public void saveDocIdentification(Personne personne, MDocIdentification docIdentification) {
        if (docIdentification != null) {
            Piece piece = null;
            List<Piece> listPiece = pieceRepository.findListPieceByTitulaireId(personne.getId());
            if (listPiece != null && listPiece.size() > 0) {
                piece = listPiece.get(0);
            }

            if (piece == null) {
                Piece newPiece = new Piece();
                newPiece.setReferencePiece(docIdentification.getReference());
                newPiece.setTitulaire(personne);
                newPiece.setDateExpiration(docIdentification.getDateExpiration());
                newPiece.setDateDelivrance(docIdentification.getDateDelivrance());
                newPiece.setLieuDelivrance(docIdentification.getLieuDelivrance());
                TypePiece type = new TypePiece();
                if (docIdentification.getTypePiece() != null) {
                    System.out.println("Type piece-----------" + docIdentification.getTypePiece().getCodeTypePiece());
                    Optional<TypePiece> typePiece = typePieceRepository.findByCodeTypePiece(docIdentification.getTypePiece().getCodeTypePiece());
                    if (typePiece.isPresent()) {

                        type = typePiece.get();
                    }
                }
                Optional<Piece> pieceExist = pieceRepository.findTopByTypePieceIdAndReferencePiece(type.getId(), docIdentification.getReference());
                if (!pieceExist.isPresent()) {
                    newPiece.setTypePiece(type);

                    pieceRepository.save(newPiece);
                }
                //newPiece.setTypePiece();
            }
        }
    }

    /**
     * Le hash permet de retourner une empreinte de la chaine passï¿½e en
     * paramï¿½tre compare le nom et le prenom puis le hash et le nom de la
     * mï¿½re
     *
     * @param personne
     * @return
     */
    private String getHash(MPersonne personne) {
        String hash = "";
        String dateNaiss = null;
        if (personne != null) {
            if (personne.getDateNaissance() != null) {
                dateNaiss = personne.getDateNaissance().format(DateTimeFormatter.ISO_DATE);
            }
            if (dateNaiss != null) {

                if (personne.getNom().compareToIgnoreCase(personne.getPrenom()) > 0) {
                    hash = personne.getNom() + "_" + personne.getPrenom();
                } else {
                    hash = personne.getPrenom() + "_" + personne.getNom();
                }

                if (hash.compareToIgnoreCase(personne.getNomDelaMere()) > 0) {
                    hash = hash + "_" + personne.getNomDelaMere();
                } else {
                    hash = personne.getNomDelaMere() + "_" + hash;
                }

                hash = hash + "_" + dateNaiss;
            }

        }

        return hash;
    }

    public MResponse findPersonneInscrit(String codePays, String email, String telephone, String qualif) {

        Personne personneExist = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndEmail(telephone, codePays, email);

        if (personneExist != null) {
            if (qualif.equals("MORALE")) {
                if (personneExist.getPersonneMorale() != null) {
                    return new MResponse("0", "Personne exite");
                }
            } else if (qualif.equals("PHYSIQUE")) {
                if (personneExist.getPersonnePhysique() != null) {
                    return new MResponse("0", "Personne exite");
                }
            }

        } else {
            return new MResponse("-1", "Erreur");
        }
        return new MResponse("-1", "Erreur");
    }

    public Compte findComptePersonne(String tel, String codePays) {
        log.debug("get compte of " + tel + "and" + codePays);
        Compte cpt = null;
        Personne personneExist = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(tel, codePays);
        if (personneExist != null) {
            Client clientExist = clientRepository.findTop1ByPersonneIdAndClientValideTrueAndSupprimeIsFalse(personneExist.getId());
            if (clientExist != null) {
                cpt = compteService.findComptePrincipal(clientExist);
            }
        }
        return cpt;
    }

    public ConcurrentHashMap<String, Object> findCompteByPersonne(String tel, String codePays) {
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<>();
        map.put("compte", null);
        map.put("motif", "");
        log.debug("get compte of " + tel + "and" + codePays);
        Compte cpt = null;
        Personne personneExist = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(tel, codePays);
        if (personneExist != null) {
            Client clientExist = clientRepository.findTop1ByPersonneIdAndClientValideTrueAndSupprimeIsFalse(personneExist.getId());
            if (clientExist != null) {
                cpt = compteService.findComptePrincipal(clientExist);
                map.put("compte", cpt);
                if (cpt == null) {
                    map.put("motif", "le compte est introuvable " + tel + " est introuvable");
                }
            } else {
                map.put("motif", "le client est introuvable " + tel);
            }
        } else {
            map.put("motif", "la personne correspondant au numéro " + tel + " est introuvable");

        }
        return map;
    }

    public MResponse findPersonnePhysique(String codePays, String email, String telephone) {

        Personne personneExist = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndEmail(telephone, codePays, email);
        if (personneExist != null) {
            if (personneExist.getPersonnePhysique() != null) {
                return new MResponse("0", "Personne exite");
            }
        } else {
            return new MResponse("-1", "Erreur");
        }
        return new MResponse("-1", "Erreur");
    }

    private Personne enregistrePersonne(MPersonne personne, Personne newPersonne, PersonnePhysique personnePhysique) {
        String hash = getHash(personne);
        Chiffrement.hashString(hash);
        newPersonne.setKeyHash(hash);
        newPersonne.setCodePaysTelephone(personne.getCodeTelephone());
        newPersonne.setTelephone(personne.getTelephone());
        newPersonne.setEmail(personne.getEmail());
        newPersonne.setPersonnePhysique(personnePhysique);
        newPersonne.setTypePersonne(TypePersonne.PHYSIQUE);
        newPersonne.setActivated(true);
        newPersonne.setAdresse(personne.getAdresse());
        newPersonne = personneRepository.save(newPersonne);
        return newPersonne;
    }

    /**
     * @param personne
     * @return
     */
    public Personne savePersonne(MPersonne personne) {
        PersonnePhysique personnePhysique;
        Personne personneExist = personneRepository.findTopByTelephoneAndCodePaysTelephone(personne.getTelephone(), personne.getCodeTelephone());
        System.out.println("---------------{ }" + personneExist);
        if (personneExist != null) {
            if (personneExist.isModifiable() == true) {
                log.info("Personne existe deja %%");
                personnePhysique = saveExternePersonnePhysique(personne, personneExist);
                personneExist.setModifiable(false);
                personneExist = enregistrePersonne(personne, personneExist, personnePhysique);

            }
            return personneExist;
        } else {
            personnePhysique = saveExternePersonnePhysique(personne, null);
            Personne newPersonne = new Personne();
            // Chiffrement chiff=new Chiffrement();
            newPersonne = enregistrePersonne(personne, newPersonne, personnePhysique);
            return newPersonne;
        }
    }

    /**
     * @param personne
     * @return
     */
    private PersonnePhysique saveExternePersonnePhysique(MPersonne personne, Personne pers) {
        PersonnePhysique personnePhysique;
        if (pers == null) {
            personnePhysique = new PersonnePhysique();
        } else {
            personnePhysique = pers.getPersonnePhysique();
        }

        personnePhysique.setNomClient(personne.getNom());
        personnePhysique.setDateNaissance(personne.getDateNaissance());
        personnePhysique.setPrenomClient(personne.getPrenom());
        personnePhysique.setPersonnePrevenir(personne.getPersonneAprevenir());
        personnePhysique.setNomMereClient(personne.getNomDelaMere());
        if (personne.getSexe() != null) {
            personnePhysique.setSexe(Sexe.valueOf(personne.getSexe().name()));
        }
        personnePhysique.setDateNaissance(personne.getDateNaissance());
        personnePhysique.setLieuNaissance(personne.getLieuNaissance());
        personnePhysique = personnePhysiqueRepository.save(personnePhysique);
        return personnePhysique;
    }

    /**
     * Calcul des taxes lors de l'enregistrement d'une opï¿½ration
     *
     * @param mOperation
     * @param operation
     * @param organisation
     * @return
     */
    private Operation treatTaxeAndSaveOp(MOperation mOperation, Operation operation, UniteOrganisation organisation) {
        BigDecimal montantTaxe = BigDecimal.ZERO;
        List<TaxeOperationDTO> taxeOperations = calculTaxe(organisation.getId(), mOperation.getTypeService(), operation.getMontantFrais(), mOperation.getMontant(), organisation);
        if (taxeOperations != null) {
            for (TaxeOperationDTO taxeOperation : taxeOperations) {
                montantTaxe = montantTaxe.add(Util.getZeroIfNull(taxeOperation.getMontant()));
            }
            operation.montantTaxe(montantTaxe);
            operation.montantFrais(Util.getZeroIfNull(operation.getMontantFrais()).subtract(montantTaxe));
            operation = operationRepository.save(operation);
            bilanTaxeService.saveBilanTaxes(operation, taxeOperations.stream().collect(Collectors.toSet()));
        }
        return operation;
    }

    private List<TaxeOperationDTO> calculTaxe(Long organisationId, String typeServiceLibelle, BigDecimal montantFrais, BigDecimal montant, UniteOrganisation uoSource) {
        log.debug("*************op id" + organisationId);
        return taxeOperationService.searchTaxeOperation(organisationId, typeServiceLibelle, montantFrais, montant, uoSource);
    }

    /**
     * Service de calcul des frais ï¿½ travers canal distant
     *
     * @param mParamFrais
     * @return
     */
    public MFrais calculFrais(MParamFrais mParamFrais, TypeFrais typeFrais) {
        PointVente opt = pointVenteService.findPointByCode(mParamFrais.getCodePv());
        //     Optional<PointVente> pointVenteExist = pointVenteService.findByCode(mParamFrais.getCodePv());
        if (opt != null) {
            Optional<PointVente> pointVenteExist = Optional.of(opt);
            if (pointVenteExist.isPresent()) {
                MFrais mFrais = new MFrais();
                Long fseDestId = null;
                Long fseSrcId = null;
                if (mParamFrais.getCodeUoDest() != null) {
                    UniteOrganisation fseDest = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(mParamFrais.getCodeUoDest(), false);
                    if (fseDest != null) {
                        fseDestId = fseDest.getId();
                        System.out.println("****************" + fseDestId);
                    }
                }
                if (mParamFrais.getCodeUoSrc() != null) {
                    UniteOrganisation fsSrc = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(mParamFrais.getCodeUoSrc(), false);
                    if (fsSrc != null) {
                        fseSrcId = fsSrc.getId();
                    } else {
                        /**
                         * Recherche le code FSE SOURCE au cas oï¿½ la source
                         * est null
                         */
                        UniteOrganisation fseCode = uniteOrganisationService.findByCodeFSE(mParamFrais.getCodeUoSrc());
                        if (fseCode != null) {
                            fseSrcId = fseCode.getId();
                        }
                    }
                }
                BigDecimal montantFrais = BigDecimal.ZERO;
                HashMap<String, Object> resFraisClient = null;
                HashMap<String, Object> resFrais = null;
                /*On regarde du cï¿½tï¿½ client on calcul les frais applicables au client*/
                if (mParamFrais.getEmetteur() != null) {
                    MPersonne titulaire = mParamFrais.getEmetteur().getTitulaire();
                    if (titulaire.getTelephone() != null && titulaire.getCodeTelephone() != null) {
                        Optional<Personne> searchPersonne = personneService.findPersonneByPhoneAndCodePays(titulaire.getTelephone(), titulaire.getCodeTelephone());//Inclure le code
                        if (searchPersonne.isPresent()) {

                            Personne pers = searchPersonne.get();
                            log.info("-----------NFORMATION FRAIS--------");
                            log.info("TYPESERVICE {}", mParamFrais.getTypeService());
                            log.info("CANAL {}", mParamFrais.getCanal());
                            log.info("FSE SRC {}", fseSrcId);
                            log.info("FSE DEST {}", fseDestId);
                            log.info("FSE DEST {}", fseDestId);
                            log.info("MONTANT {}", mParamFrais.getMontant());
                            log.info("Pers {}", Optional.of(pers));
                            log.info("-------FIN INFO FRAIS---------------");
                            resFraisClient = calCulFrais(mParamFrais.getTypeService(), mParamFrais.getCanal(),
                                    fseDestId, fseSrcId,
                                    mParamFrais.getMontant(), null, pointVenteExist.get(), Optional.of(pers));
                        }
                    }

                }
                /*les frais issus du rofil client prime sur celui de l'agent*/
                if ((resFraisClient != null) && Util.isBiggerThan((BigDecimal) resFraisClient.get("frais"), BigDecimal.ZERO)) {
                    resFrais = resFraisClient;
                } else {
                    resFrais = calCulFrais(mParamFrais.getTypeService(), mParamFrais.getCanal(),
                            fseDestId, fseSrcId,
                            mParamFrais.getMontant(), null, pointVenteExist.get(), Optional.empty());

                }

                montantFrais = (BigDecimal) resFrais.get("frais");
                BigDecimal montantTaxe = BigDecimal.ZERO;
                BigDecimal montantTaxeDeductible = BigDecimal.ZERO; //deductible des commissions
                List<TaxeOperationDTO> taxeOperations = calculTaxe(fseDestId,
                        mParamFrais.getTypeService(),
                        montantFrais, mParamFrais.getMontant(), pointVenteExist.get().getUniteOrganisation());
                if (taxeOperations != null) {
                    for (TaxeOperationDTO taxeOperation : taxeOperations) {
                        montantTaxe = montantTaxe.add(Util.getZeroIfNull(taxeOperation.getMontant())).setScale(0, RoundingMode.HALF_UP);
                        if (!taxeOperation.getAppliqueSur().equals(MontantsAComptabilise.LIBRE)) {
                            montantTaxeDeductible = montantTaxeDeductible.add(Util.getZeroIfNull(taxeOperation.getMontant())).setScale(0, RoundingMode.HALF_UP);
                        }
                    }
                }
                if (montantFrais != null && montantTaxe != null) {
                    montantTaxeDeductible = montantTaxeDeductible.setScale(0, RoundingMode.HALF_UP);
                    mFrais.setFraisOperation(montantFrais.subtract(montantTaxeDeductible));
                } else {
                    mFrais.setFraisOperation(BigDecimal.ZERO);
                }
                mFrais.setTaxeOPeration(montantTaxe);
                mFrais.setResponsable((String) resFrais.get("resp"));
                return mFrais;
            }
        }

        throw new CustomParameterizedException("Le pointe de vente est introuvable.");
    }

    /**
     * @param typeService
     * @param canal
     * @param fseDestId
     * @param fseSrcId
     * @param montant
     * @param montantFrais
     * @return
     */
    private HashMap<String, Object> calCulFrais(String typeService, String canal, Long fseDestId, Long fseSrcId,
            BigDecimal montant, BigDecimal montantFrais,
            PointVente pointVente, Optional<Personne> pers) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("frais", null);
        map.put("resp", "");
        BigDecimal baseCalul=montant;
        if (montantFrais == null) {
            LigneFraisDTO ligneFrais;
            log.info("Calcul des frais de l'operation");
            Long montantSeuil= getMontantSeuilPI();
            if (pers.isPresent()) {
                ligneFrais = operationService.calculFrais(canal, TypeServiceLibelle.valueOf(typeService), fseDestId, montant.doubleValue(), fseSrcId, pointVente, pers, montantSeuil);
            } else {
                ligneFrais = operationService.calculFrais(canal, TypeServiceLibelle.valueOf(typeService), fseDestId, montant.doubleValue(), fseSrcId, pointVente, Optional.empty(),montantSeuil);
            }

            if (ligneFrais != null && ligneFrais.getTypeFrais() != null) {
                TableauFraisDTO t = tableauFraisService.findOne(ligneFrais.getTableauFraisId());
                if (t != null) {
                    if (ligneFrais.getTypeFrais().equals(TypeTarif.FIXE)) {
                        map.put("frais", BigDecimal.valueOf(ligneFrais.getValeur()));
                        map.put("resp", t.getTypeRegle());
                        return map;
                    } else {
                        if(ligneFrais.getBaseCalcul()!=null && ligneFrais.getBaseCalcul()>0 ){
                            baseCalul=BigDecimal.valueOf(ligneFrais.getBaseCalcul());
                        }
                        map.put("frais", Util.pourcentage(baseCalul, BigDecimal.valueOf(ligneFrais.getValeur())));
                        map.put("resp", t.getTypeRegle());
                        return map;
//                }
                    }
                }

            }
        }
        return map;
    }

    /**
     * @param infos
     * @param estBeneficiare
     */
    public Operation saveInformationPersonne(MInfos infos, boolean estBeneficiare, Operation operation) {
        if (infos == null) {
            return operation;
        }

        MPersonne titulaire = infos.getTitulaire();
        if (titulaire.getTelephone() != null && titulaire.getCodeTelephone() != null) {
            System.out.println("--titulaire.getTelephone()---");
            System.out.println(titulaire.getTelephone());
            System.out.println("--titulaire.getTelephone()---");
            // TODO: 22/02/2024 Prise en compte des beneficiaires enregistré plusieurs fois
            Optional<Personne> searchPersonne = personneService.findPersonneByPhoneAndCodePays(titulaire.getTelephone(), titulaire.getCodeTelephone());//Inclure le code
            if (searchPersonne != null && searchPersonne.isPresent()) {
                Personne p = searchPersonne.get();
                if (estBeneficiare) {
                    Beneficiaire beneficiaire; //= beneficiaireRepository.findByPersonneId(p.getId());
                    List<Beneficiaire> listebeneficiaire = beneficiaireRepository.findAllByPersonneId(p.getId());
                    if (listebeneficiaire != null && listebeneficiaire.size() > 0) {
                        beneficiaire = listebeneficiaire.get(0);
                    } else { //Nouveau benefiaire
                        beneficiaire = new Beneficiaire();
                        beneficiaire.personne(p);
                        beneficiaire = beneficiaireRepository.save(beneficiaire);

                    }
                    operation.setBeneficaire(beneficiaire);
                    List<Piece> pieces = pieceRepository.findByTitulaireId(p.getId());
                    if (!pieces.isEmpty()) {
                        operation.setPieceBeneficiaire(pieces.get(0));
                    }
                } else {
                    System.out.println("--p.getId()---");
                    System.out.println(p.getId());
                    System.out.println("--p.getId()---");
                    Emetteur emetteur; //emetteurRepository.findByPersonneId(p.getId());
                    List<Emetteur> listeemetteur = emetteurRepository.findAllByPersonneId(p.getId());
                    if (listeemetteur != null && listeemetteur.size() > 0) {
                        emetteur = listeemetteur.get(0);
                    } else {
                        emetteur = new Emetteur();
                        emetteur.personne(p);
                        emetteur = emetteurRepository.save(emetteur);

                    }
                    operation.setEmetteur(emetteur);
                    List<Piece> pieces = pieceRepository.findByTitulaireId(p.getId());
                    if (!pieces.isEmpty()) {
                        operation.setPieceEmetteur(pieces.get(0));
                    }
                }
            } else {
                PersonnePhysique personnePhysique = saveExternePersonnePhysique(titulaire, null);
                Personne personne = new Personne();
                personne.telephone(infos.getTitulaire().getTelephone());
                personne.setCodePaysTelephone(infos.getTitulaire().getCodeTelephone());
                personne.setTypePersonne(TypePersonne.PHYSIQUE);
                personne.personnePhysique(personnePhysique);
                /**
                 * Permet de marquer un client de passage. en cas d'enrolement
                 * pouvoir modifier les infos
                 */
                personne.setModifiable(true);
                personne = personneRepository.save(personne);
                if (estBeneficiare) {
                    Beneficiaire beneficiaire = new Beneficiaire();
                    beneficiaire.personne(personne);
                    beneficiaire = beneficiaireRepository.save(beneficiaire);
                    operation.setBeneficaire(beneficiaire);
                } else {
                    Emetteur emetteur = new Emetteur();
                    emetteur.personne(personne);
                    emetteur = emetteurRepository.save(emetteur);
                    operation.setEmetteur(emetteur);
                }
            }
        }

        return operation;
    }

    /**
     * Recherche des comptes d'un client ï¿½ travers son numero de telephone
     *
     * @param telephone
     * @return
     */
    public MDevise getDeviseDefault(Long codeUo) {
        MDevise mDevise = new MDevise();
        //    UniteOrganisation uo = uniteOrganisationService.findByCode(code)
        UoDeviseDTO uoDeviseDTO = uoDeviseService.findByActeurDefault(codeUo);
        if (uoDeviseDTO != null) {
            mDevise.setCode(uoDeviseDTO.getCode());
            mDevise.setLibelle(uoDeviseDTO.getDeviseLibelle());
        }

        return mDevise;
    }

    public BigDecimal getTauxChangeAchat(String devDepart, String devArrivee, LocalDate dateTaux) {
        Devise deviseDepart = deviseService.findByCode(devDepart);
        Devise deviseArrivee = deviseService.findByCode(devArrivee);
        TauxChangeDTO tauxChangeDTO = tauxChangeService.findByCriteria(deviseDepart.getId(), deviseArrivee.getId(), LocalDate.now(), TypeChange.ACHAT);
        if (tauxChangeDTO != null) {
            return tauxChangeDTO.getTaux();
        } else {
            return BigDecimal.ONE;
        }
    }

    public BigDecimal getTauxChangeVente(String devDepart, String devArrivee, LocalDate dateTaux) {
        Devise deviseDepart = deviseService.findByCode(devDepart);
        Devise deviseArrivee = deviseService.findByCode(devArrivee);
        TauxChangeDTO tauxChangeDTO = tauxChangeService.findByCriteria(deviseDepart.getId(), deviseArrivee.getId(), LocalDate.now(), TypeChange.VENTE);
        if (tauxChangeDTO != null) {
            return tauxChangeDTO.getTaux();
        } else {
            return BigDecimal.ONE;
        }
    }

    /**
     * r
     *
     * @param typeCompte
     * @param valeurCompte
     * @return
     */
    public Compte getCompte(String valeurCompte, String typeCompte, String codeAgence, String codeFseTeneur) {
        if ("".equals(valeurCompte)) {
            return null;
        }
        Compte compteExist = compteRepository.findTop1ByValeurCompteAndEtiquette(valeurCompte, EtiquetteCompteCode.CPTECLIENT.toString());
        if (compteExist != null) {//Le numero de compte existe
            if (typeCompte.equals(MTypeCompte.BANQUE)) {
                CompteBanque compteBanqueExist = compteExist.getCompteBanque();
                if (codeAgence.equals(compteBanqueExist.getCodeAgence())
                        // && mCompte.getRib().equals(compteBanqueExist.getRib())
                        && codeFseTeneur.equals(compteBanqueExist.getCodeFse())) {
                    return compteExist;
                } else {

                }
            } else if (codeFseTeneur.equals(compteExist.getCompteAutre().getCodeFse())) {
                return compteExist;
            }
        }
        return null;
    }

    public Compte getCompteX(MCompte mCompte, Personne personne, String application, Boolean estDestination) {
        String malias = "";
        Compte compteExist = null;
        if (application != null) {
            if (application.equals("PTI")) {
                if (mCompte.getTypeCompte().equals(MTypeCompte.BANQUE)) {
                    malias = compteService.getCRC32Checksum(mCompte.getCodeAgence(), mCompte.getNumeroCompte(), personne.getTelephone(), mCompte.getTeneurCompte());
                    if (estDestination.equals(Boolean.TRUE)) {
                        compteExist = compteRepository.findCompteByAlias(malias, mCompte.getTeneurCompte());
                    } else {
                        compteExist = compteRepository.findByAlias(malias, mCompte.getTeneurCompte());
                    }

                    if (compteExist == null) {
                        compteExist = compteRepository.findTop1ByValeurCompteAndEtiquetteAndCompteBanque(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString(), mCompte.getCodeBanque(), mCompte.getCodeAgence(), mCompte.getTeneurCompte(), malias);
                    }

                } else {
                    compteExist = compteRepository.findTop1ByValeurCompteAndEtiquette(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString());
                    // compteExist = compteRepository.findTop1ByValeurCompteAndEtiquette(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString());
                }

                if (compteExist == null) {
                    if (estDestination.equals(Boolean.TRUE)) {
                        return saveNewCompte(mCompte, personne, malias);
                    }

                } else {
                    return compteExist;
                }
            }

        }

        Client clientExist = clientService.findByPersonneId(personne.getId());
        // Client clientExist = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personne.getId());
        if (clientExist != null) {
            if (mCompte != null && mCompte.getTypeCompte().equals(MTypeCompte.AUTRE)) {
                compteExist = compteService.findComptePrincipal(clientExist);
            }

        } else {
            return null;
        }
        if (compteExist == null) {
            compteExist = compteRepository.findTop1ByValeurCompteAndEtiquette(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString());
        }

        if (compteExist != null) {//Le numero de compte existe
            if (mCompte.getTypeCompte().equals(MTypeCompte.BANQUE)) {
                CompteBanque compteBanqueExist = compteExist.getCompteBanque();
                if (mCompte.getCodeAgence().equals(compteBanqueExist.getCodeAgence())
                        // && mCompte.getRib().equals(compteBanqueExist.getRib())
                        && mCompte.getTeneurCompte().equals(compteBanqueExist.getCodeFse())) {
                    return compteExist;
                } else {
                    return saveNewCompte(mCompte, personne, malias);
                }
            } else if (mCompte.getTeneurCompte().equals(compteExist.getCompteAutre().getCodeFse())) {
                return compteExist;
            } else {
                return null;
            }
        } else { //Enregistrement des informations du compte
            return null;
        }
    }

    public Compte getCompte(MCompte mCompte, Personne personne, String application, Boolean estDestination) {

        Compte compteExist = null;
        if (application != null) {
            if (application.equals("PTI") || application.equals("AUTRE")) {
                if (mCompte.getAlias() != null) {
                    if (estDestination.equals(Boolean.TRUE)) {
                        compteExist = compteRepository.findCompteByAlias(mCompte.getAlias(), mCompte.getTeneurCompte());
                    } else {
                        compteExist = compteRepository.findByAlias(mCompte.getAlias(), mCompte.getTeneurCompte());
                    }
                    if (compteExist == null) {
                        return saveNewCompte(mCompte, personne, mCompte.getAlias());
                    }
                    return compteExist;
                } else {
                    if (mCompte.getTypeCompte().equals(MTypeCompte.BANQUE)) {
                        compteExist = compteRepository.findTop1ByValeurCompteAndEtiquetteAndCompteBanque(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString(), mCompte.getCodeBanque(), mCompte.getCodeAgence(), mCompte.getTeneurCompte());
                    } else {
                        compteExist = compteRepository.findTop1ByValeurCompteAndEtiquette(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString());
                        // compteExist = compteRepository.findTop1ByValeurCompteAndEtiquette(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString());
                    }
                    if (compteExist == null) {
                        return saveNewCompte(mCompte, personne);
                    } else {
                        return compteExist;
                    }
                }

            }

        }

        Client clientExist = clientService.findByPersonneId(personne.getId());
        // Client clientExist = clientRepository.findTop1ByPersonneIdAndS0upprimeIsFalse(personne.getId());
        if (clientExist != null) {
            if (mCompte != null) {
                if (mCompte.getTypeCompte().equals(MTypeCompte.AUTRE)) {
                    compteExist = compteService.findComptePrincipal(clientExist);
                } else if (mCompte.getTypeCompte().equals(MTypeCompte.CARTE)) {
                    compteExist = compteRepository.findTop1ByValeurCompteAndEtiquette(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString());
                }
            }

        } else {
            return null;
        }
        if (compteExist == null) {
            compteExist = compteRepository.findTop1ByValeurCompteAndEtiquette(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString());
        }

        if (compteExist != null) {//Le numero de compte existe
            if (mCompte.getTypeCompte().equals(MTypeCompte.BANQUE)) {
                CompteBanque compteBanqueExist = compteExist.getCompteBanque();
                if (mCompte.getCodeAgence().equals(compteBanqueExist.getCodeAgence())
                        // && mCompte.getRib().equals(compteBanqueExist.getRib())
                        && mCompte.getTeneurCompte().equals(compteBanqueExist.getCodeFse())) {
                    return compteExist;
                } else {
                    return saveNewCompte(mCompte, personne);
                }
            } else if (compteExist.getCompteAutre() != null && mCompte.getTeneurCompte().equals(compteExist.getCompteAutre().getCodeFse())) {
                return compteExist;
            } else if (compteExist.getCarte() != null && mCompte.getTeneurCompte().equals(compteExist.getCarte().getCodeFse())) {
                return compteExist;
            } else {
                return null;
            }
        } else { //Enregistrement des informations du compte
            if (estDestination == false) {
                if (mCompte.getTypeCompte().equals(MTypeCompte.CARTE)) {
                    return saveNewCompte(mCompte, personne);
                } else {
                    return null;
                }
            }

        }
        return null;
    }

    public CompteOperationDTO getCompte(MCompte mCompte, Personne personne, String application, Boolean estDestination, Integer preAutorisation) throws JAXBException {
        Compte compteExist = null;
        if (application != null) {
            if (application.equals("PTI") || application.equals("AUTRE")) {
                if (mCompte.getAlias() != null) {
                    if (estDestination.equals(Boolean.TRUE)) {
                        compteExist = compteRepository.findCompteByAlias(mCompte.getAlias(), mCompte.getTeneurCompte());
                    } else {
                        compteExist = compteRepository.findByAlias(mCompte.getAlias(), mCompte.getTeneurCompte());
                    }
                    if (compteExist == null) {
                        compteExist = saveNewCompte(mCompte, personne, mCompte.getAlias());
                        return new CompteOperationDTO(compteExist, null, "0");
                    }
                    return new CompteOperationDTO(compteExist, null, "0");
                } else {
                    if (mCompte.getTypeCompte().equals(MTypeCompte.BANQUE)) {
                        compteExist = compteRepository.findTop1ByValeurCompteAndEtiquetteAndCompteBanque(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString(), mCompte.getCodeBanque(), mCompte.getCodeAgence(), mCompte.getTeneurCompte());
                    } else {
                        compteExist = compteRepository.findTop1ByValeurCompteAndEtiquette(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString());
                        // compteExist = compteRepository.findTop1ByValeurCompteAndEtiquette(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString());
                    }
                    if (compteExist == null) {
                        compteExist = saveNewCompte(mCompte, personne);
                        return new CompteOperationDTO(compteExist, null, "0");
                    } else {
                        return new CompteOperationDTO(compteExist, null, "0");
                    }
                }

            }
        }

        Client clientExist = clientService.findByPersonneId(personne.getId());
        // Client clientExist = clientRepository.findTop1ByPersonneIdAndS0upprimeIsFalse(personne.getId());
        if (clientExist != null) {
            if (mCompte != null) {
                if (mCompte.getTypeCompte().equals(MTypeCompte.AUTRE)) {
                    compteExist = compteService.findComptePrincipal(clientExist);
                } else if (mCompte.getTypeCompte().equals(MTypeCompte.CARTE)) {
                    compteExist = compteRepository.findTop1ByValeurCompteAndEtiquette(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString());
                }
            }

        }
        if (compteExist == null) {
            compteExist = compteRepository.findTop1ByValeurCompteAndEtiquette(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString());
        }

        if (compteExist != null) {//Le numero de compte existe
            if (mCompte.getTypeCompte().equals(MTypeCompte.BANQUE)) {
                CompteBanque compteBanqueExist = compteExist.getCompteBanque();
                if (mCompte.getCodeAgence().equals(compteBanqueExist.getCodeAgence())
                        // && mCompte.getRib().equals(compteBanqueExist.getRib())
                        && mCompte.getTeneurCompte().equals(compteBanqueExist.getCodeFse())) {
                    return new CompteOperationDTO(compteExist, null, "0");
                } else {
                    compteExist = saveNewCompte(mCompte, personne);
                    return new CompteOperationDTO(compteExist, null, "0");
                }
            } else if (compteExist.getCompteAutre() != null && mCompte.getTeneurCompte().equals(compteExist.getCompteAutre().getCodeFse())) {
                return new CompteOperationDTO(compteExist, null, "0");
            } else if (compteExist.getCarte() != null && mCompte.getTeneurCompte().equals(compteExist.getCarte().getCodeFse())) {
                return new CompteOperationDTO(compteExist, null, "0");
            } else {
                return null;
            }
        } else { //Enregistrement des informations du compte
            if ((preAutorisation != null && preAutorisation.equals(1))) {
                compteExist = compteRepository.findTop1ByValeurCompte(mCompte.getNumeroCompte(), EtiquetteCompteCode.CPTECLIENT.toString());
                if (compteExist != null) {
                    if (estDestination.equals(Boolean.TRUE) && compteExist.getStatutCompte().equals(StatutCompte.DESACTIVE)) {
                        return null;
                    } else if (compteExist.getEtatCompte().equals(EtatCompte.FERME) || compteExist.getStatutCompte().equals(StatutCompte.DESACTIVE)) {
                        return null;
                    }
                } else {
                    UniteOrganisationCorrespondance uo = uniteOrganisationCorrespondanceRepository.findByCodeTeneur(mCompte.getTeneurCompte());
                    if (uo != null && uo.getCompteCorrespondance() != null) {
                        compteExist = uo.getCompteCorrespondance();
                        String strCompte = (String) Util.marshall(mCompte, String.class
                        );
                        return new CompteOperationDTO(compteExist, strCompte, "1");
                    }
                    return null;
                }

            }

        }
        return null;
    }

    /**
     * Recherche du compte
     *
     * @param mCompte
     * @return
     */
    public MInfos getInfosCompte(String alias, String codeFse) {
        MInfos minfos = null;
        minfos = compteService.findByalias(alias, codeFse);
        return minfos;
    }

    public MInfos getInfosCompte(String alias) {
        MInfos minfos = null;
        minfos = compteService.findByalias(alias);
        return minfos;
    }

    private ClientAllDTO verifAutorisation(MCompte compte, String codeService, String telephone, String codeTelephone) {
        log.info("verif autorisation operation ï¿½ partir du mobile .....");
        if (compte.getTeneurCompte() != null) {
            UniteOrganisation fse = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compte.getTeneurCompte());
            if (fse != null) {
                return clientService.findByClient(telephone, compte.getNumeroCompte(), codeService,
                        compte.getCodeAgence(), compte.getRib(), fse.getId(), codeTelephone);
            }
        }
        return null;
    }

    /**
     * Service verifiant la souscription pour utilisation du mobile pour faire
     * les operations
     *
     * @param code
     * @param telephone
     * @return
     */
    public List<MAutorisation> veriAuthClient(String code, String telephone) {
        List<MAutorisation> mAutorisations = new ArrayList<>();

        clientService.getAutorisation(code, telephone).forEach(a -> {
            mAutorisations.add(new MAutorisation(a.getTypeServiceLibelle()));
        });
        return mAutorisations;
    }

    private Compte saveNewCompte(MCompte mCompte, Personne personne, String alias) {
        Compte compte = new Compte();
        Client client = new Client();
        Client clientExist = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personne.getId());
        if (clientExist != null) {
            log.info("Client existe dans la bd%");
            client = clientExist;
        } else {
            ParametreApp groupe = parametreAppRepository.findByTypeAndNomAndCodeFse("PROFIL_CLIENT_CLIENT_NON_IDENTIFIE", mCompte.getTeneurCompte());
            if (groupe != null) {
                GroupeProfilActeur groupeProfilActeur = groupeProfilActeurRepository.findById(Long.parseLong(groupe.getValeur())).orElse(null);
                client = clientService.setGroupe(groupeProfilActeur, client);
                //    profilActeur = profilActeurRepository.findById(Long.parseLong(profil.getValeur())).orElse(null);
                // client.setProfilActeur(profilActeur);
            }
            ParametreApp service = parametreAppRepository.findByTypeAndNomAndCodeFse("SERVICE_CLIENT_NON_IDENTIFIE", mCompte.getTeneurCompte());
            if (service != null) {
                ServiceProfil serviceProfil = serviceProfilRepository.findById(Long.parseLong(service.getValeur())).orElse(null);
                client.setProfil(serviceProfil);
            }
            log.info("Client introuvable%");
            client.setCodeClient(RandomStringUtils.randomNumeric(10) + RandomUtil.generateAlphaNumeric(2).toUpperCase());
            client.setPersonne(personne);
            client.setClientValide(Boolean.TRUE);
            client.setClientExterne(1);
            client = clientRepository.save(client);
        }
        compte.valeurCompte(mCompte.getNumeroCompte())
                .client(client)
                .etiquette(EtiquetteCompteCode.CPTECLIENT.toString());
        UniteOrganisation fseTeneur = uniteOrganisationService.findByCodeFSE(mCompte.getTeneurCompte());
        if (fseTeneur != null) {
            if (mCompte.getTypeCompte().equals(MTypeCompte.BANQUE)) {
                compte.categorieCompte(CategorieCompte.BANQUE);
                CompteBanque compteBanque = new CompteBanque();
                compteBanque.codeBanque(fseTeneur.getCodeBanque())
                        .codeFse(mCompte.getTeneurCompte())
                        .rib(mCompte.getRib())
                        .codeAgence(mCompte.getCodeAgence());
                compteBanque.setAlias(alias);
                compteBanque = compteBanqueRepository.save(compteBanque);
                compte.setCompteBanque(compteBanque);
            } else { //Compte autre
                compte.setEtatCompte(EtatCompte.OUVERT);
                compte.setStatutCompte(StatutCompte.ACTIVE);
                compte.categorieCompte(CategorieCompte.AUTRE);
                compte.setClasseCompte(ClasseCompte.PRINCIPAL);
                CompteAutre compteAutre = new CompteAutre();
                compteAutre.setCodeFse(mCompte.getTeneurCompte());
                compteAutre.setSolde(BigDecimal.ZERO);
                compteAutre = compteAutreRepository.save(compteAutre);
                compte.compteAutre(compteAutre);
            }
            compteRepository.save(compte);
            return compte;
        } else {
            throw new CustomParameterizedException("Fournisseur de service introuvable.");
        }
    }

    /**
     * Enregistrement d'un compte
     *
     * @param mCompte
     * @param personne
     * @return
     */
    private Compte saveNewCompte(MCompte mCompte, Personne personne) {
        Compte compte = new Compte();
        Client client = new Client();
        Client clientExist = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personne.getId());
        if (clientExist != null) {
            log.info("Client existe dans la bd%");
            client = clientExist;
        } else {
            ParametreApp groupe = parametreAppRepository.findByTypeAndNomAndCodeFse("PROFIL_CLIENT_CLIENT_NON_IDENTIFIE", mCompte.getTeneurCompte());
            if (groupe != null) {
                GroupeProfilActeur groupeProfilActeur = groupeProfilActeurRepository.findById(Long.parseLong(groupe.getValeur())).orElse(null);
                client = clientService.setGroupe(groupeProfilActeur, client);
                //    profilActeur = profilActeurRepository.findById(Long.parseLong(profil.getValeur())).orElse(null);
                // client.setProfilActeur(profilActeur);
            }
            ParametreApp service = parametreAppRepository.findByTypeAndNomAndCodeFse("SERVICE_CLIENT_NON_IDENTIFIE", mCompte.getTeneurCompte());
            if (service != null) {
                ServiceProfil serviceProfil = serviceProfilRepository.findById(Long.parseLong(service.getValeur())).orElse(null);
                client.setProfil(serviceProfil);
            }
            log.info("Client introuvable%");
            client.setCodeClient(RandomStringUtils.randomNumeric(10) + RandomUtil.generateAlphaNumeric(2).toUpperCase());
            client.setPersonne(personne);
            client.setClientValide(Boolean.TRUE);
            client.setClientExterne(1);
            client = clientRepository.save(client);
        }
        compte.valeurCompte(mCompte.getNumeroCompte())
                .client(client)
                .etiquette(EtiquetteCompteCode.CPTECLIENT.toString());
        UniteOrganisation fseTeneur = uniteOrganisationService.findByCodeFSE(mCompte.getTeneurCompte());
        if (fseTeneur != null) {
            if (mCompte.getTypeCompte().equals(MTypeCompte.BANQUE)) {
                compte.categorieCompte(CategorieCompte.BANQUE);
                CompteBanque compteBanque = new CompteBanque();
                compteBanque.codeBanque(fseTeneur.getCodeBanque())
                        .codeFse(mCompte.getTeneurCompte())
                        .rib(mCompte.getRib())
                        .codeAgence(mCompte.getCodeAgence());
                compteBanque = compteBanqueRepository.save(compteBanque);
                compte.setCompteBanque(compteBanque);
            } else if (mCompte.getTypeCompte().equals(MTypeCompte.AUTRE)) { //Compte autre
                compte.setEtatCompte(EtatCompte.OUVERT);
                compte.setStatutCompte(StatutCompte.ACTIVE);
                compte.categorieCompte(CategorieCompte.AUTRE);
                compte.setClasseCompte(ClasseCompte.PRINCIPAL);
                CompteAutre compteAutre = new CompteAutre();
                compteAutre.setCodeFse(mCompte.getTeneurCompte());
                compteAutre.setSolde(BigDecimal.ZERO);
                compteAutre = compteAutreRepository.save(compteAutre);
                compte.compteAutre(compteAutre);
            } else if (mCompte.getTypeCompte().equals(MTypeCompte.CARTE)) {
                compte.categorieCompte(CategorieCompte.CARTE);
                compte.setStatutCompte(StatutCompte.DESACTIVE);
                Carte carte = new Carte();
                carte.setCodeFse(mCompte.getTeneurCompte());
                carte.setSolde(BigDecimal.ZERO);
                carte.setLast4Digit(mCompte.getLas4Digit());
                carte = carteRepository.save(carte);
                compte.setCarte(carte);

            }
            compteRepository.save(compte);
            return compte;
        } else {
            throw new CustomParameterizedException("Fournisseur de service introuvable.");
        }
    }

    /**
     *
     * @param imageDocumentIdentification
     * @param client
     */
    private void saveDocument(List<MDocument> imageDocumentIdentification, Client client) {
        String telephone = client.getPersonne().getCodePaysTelephone() + client.getPersonne().getTelephone();
        final String keyDirectory = (SystemUtils.IS_OS_LINUX ? imagesPathLinux : imagesPathWindows);

        if (imageDocumentIdentification != null) {
            imageDocumentIdentification.forEach(imgdoc -> {
                Document document = new Document();
                TypeDocument typeDoc = typeDocumentRepository.findById(Long.parseLong(imgdoc.getTypeDocument())).orElse(null);
                document.setTypeDocument(typeDoc);
                document.setValeur(imgdoc.getValeur());
                document.setValeurContentType(imgdoc.getValeurContentType());
                document.setTitulaire(client);
                DocumentDTO documentDTO = documentMapper.toDto(document);
                try {
                    if (clientService.verifClientSavedImage()) {
                        //documentService.saveImageClientToFolder(documentDTO, telephone, client.getId(), false);
                        documentService.saveDocInMinioFromMobile(documentDTO, client);
                    } else {
                        documentRepository.save(document);
                    }

                } catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                /*  if (clientService.verifClientSavedImage()) {
                    String finalDirectory = keyDirectory.replace("\\", "/");
                    log.debug("**--------------" + finalDirectory);
                    String contentType = documentDTO.getValeurContentType();
                    String typeImage = documentService.TransformToPng(contentType);
                    String imageName = documentDTO.getTypeDocumentCode().replace(" ", "_");
                    document.setValeur(null);
                    document.setImageFolder(finalDirectory + "/" + telephone + "/" + client.getId().toString() + "/" + imageName + "." + typeImage);
                }*/

            });
        }

    }

    /**
     *
     * @param codeUo
     * @param etiquette
     * @return
     */
    public Compte findComptesAgent(String codeUo, String etiquette) {
        UniteOrganisation organisationSrc = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(codeUo, false);
        //  UniteOrganisation organisationSrc = uniteOrganisationRepository.findByCodeUOAndSupprimeIsFalse(codeUo);
        Compte compteOperationActeur = compteService.getCompteOfOrganisation(organisationSrc, EtiquetteCompteCode.valueOf(etiquette));//compte reseau
        return compteOperationActeur;
    }

    /**
     * Recherche des comptes d'un client ï¿½ travers son numero de telephone
     *
     * @param telephone
     * @return
     */
    public List<CompteDTO> findComptes(String telephone, String code) {

        Personne personne = personneRepository.findTop1ByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(telephone, code);
        if (personne != null) {
            Client client = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personne.getId());
            if (client.getClientValide() == Boolean.TRUE) {
                if (client != null) {
                    return compteService.findClientAllComptes2(client);
                }
            }

        }
        return Collections.emptyList();
    }

    /**
     * @param telephone
     * @param code
     * @return
     */
    public MPersonne findInfos(String telephone, String code) {
        MPersonne infos = new MPersonne();
        log.info("Recherche d'une personne......" + telephone + "#" + code);
        Personne personne = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(telephone, code);
        log.info("Recherche d'une personne in ......");
        if (personne != null) {
            if (personne != null) {
                log.info("Recherche des informations d'une personne......");
                infos.setCodeTelephone(code);
                infos.setTelephone(telephone);
                infos.setEmail(personne.getEmail());
                if (personne.getTypePersonne().equals(TypePersonne.PHYSIQUE)) {
                    PersonnePhysique personnePhysique = personne.getPersonnePhysique();
                    if (personnePhysique != null) {
                        infos.setSexe(personnePhysique.getSexe() != null ? MSexe.valueOf(personnePhysique.getSexe().name()) : MSexe.NON_DEFINI);
                        infos.setNom(personnePhysique.getNomClient());
                        infos.setPrenom(personnePhysique.getPrenomClient());
                        infos.setDateNaissance(personnePhysique.getDateNaissance());
                        infos.setLieuNaissance(personnePhysique.getLieuNaissance());
                        infos.setProfession(personne.getBrancheActivite() != null ? personne.getBrancheActivite().getLibelle() : "");
                    }
                    List<Piece> pieces = pieceRepository.findByTitulaireId(personne.getId());
                    //Mise ï¿½ jour des informations du document d'identification
                    List<MDocIdentification> docIdentifications = new ArrayList<>();
                    pieces.forEach(p -> {
                        List<MDocIdentification> verif = docIdentifications.stream().filter(d -> d.getReference().equals(p.getReferencePiece())).collect(Collectors.toList());
                        if (verif.isEmpty()) {
                            MDocIdentification docIdentification = new MDocIdentification();
                            docIdentification.setReference(p.getReferencePiece());
                            MTypePiece typepiece = new MTypePiece();
                            typepiece.setCodeTypePiece(p.getTypePiece().getCodeTypePiece());
                            typepiece.setLibelleTypePiece(p.getTypePiece().getLibelleTypePiece());
                            docIdentification.setTypePiece(typepiece);
                            docIdentifications.add(docIdentification);
                        }
                    });
                    infos.setDocIdentifications(docIdentifications);
                    log.info("Fin de la recherche......");
                    return infos;
                } else {
                    throw new CustomParameterizedException("C'est pas une personne physique.");
                }
            }
        }
        return null;
    }

    public Client getCustomer(String telephone, String code) {

        Personne personne = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(telephone, code);
        if (personne != null) {
            Client client = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personne.getId());
            if (client != null) {
                return client;
            }
        }
        return null;
    }

    /**
     *
     * @param telephone
     * @param code
     * @param acteur
     * @param codeActeur
     * @return
     */
    public MOrganisation findInfosOrga(String codeActeur) {
        MOrganisation mOrga = null;
        UniteOrganisation organisationSrc = uniteOrganisationRepository.findByCodeUOAndSupprimeIsFalse(codeActeur);
        if (organisationSrc != null) {
            mOrga = new MOrganisation();
            mOrga.setNom(organisationSrc.getLibelleUniteOrganisation());
            mOrga.setCodePere(organisationSrc.getPereUO().getCodeUO());
            mOrga.setCodeUo(organisationSrc.getCodeUO());
            mOrga.setType(MTypeOrganisation.valueOf(organisationSrc.getTypeUO().getCodeTypeUO()));
            mOrga.setTypeDuPere(MTypeOrganisation.valueOf(organisationSrc.getPereUO().getTypeUO().getCodeTypeUO()));
        }
        return mOrga;
    }

    /**
     *
     * @param telephone
     * @param code
     * @param acteur
     * @param codeActeur
     * @return
     */
    public MPersonne findInfosPersonne(String telephone, String code, String acteur, String codeActeur) {
        MPersonne mPersonne = new MPersonne();
        Personne personne = null;
        if (acteur.equals(TypeProfilActeur.CLIENT.name())) {
            Optional<Personne> pers = personneService.findPersonneByPhoneAndCodePaysActive(telephone, code);
            if (pers.isPresent() && pers.get() != null) {
                personne = personneRepository.findByPersonneId(pers.get().getId());
            }
            if (personne != null) {
                if (personne.getPersonnePhysique() != null) {
                    mPersonne.setNom(personne.getPersonnePhysique().getNomClient());
                    mPersonne.setPrenom(personne.getPersonnePhysique().getPrenomClient());
                    mPersonne.setSexe(personne.getPersonnePhysique().getSexe() != null ? MSexe.valueOf(personne.getPersonnePhysique().getSexe().name()) : MSexe.NON_DEFINI);
                } else if (personne.getPersonneMorale() != null) {
                    mPersonne.setNom(personne.getPersonneMorale().getRaisonSociale());
                }
            }

        } else if (acteur.equals(TypeProfilActeur.ORGANISATION.name())) {
            UniteOrganisation organisationSrc = uniteOrganisationRepository.findByCodeUOAndSupprimeIsFalse(codeActeur);

            if (organisationSrc != null) {
                mPersonne.setNom(organisationSrc.getLibelleUniteOrganisation());
                if (organisationSrc.getPereUO() != null) {
                    mPersonne.setPrenom(organisationSrc.getPereUO().getCodeUO() + "-" + organisationSrc.getPereUO().getLibelleUniteOrganisation() + " Tel:" + organisationSrc.getPereUO().getTelephone());
                }

            }
        }

        return mPersonne;
    }

    /**
     *
     * @param telephone
     * @param code
     * @return
     */
    public MClient findInfosClient(String telephone, String code) {
        MPersonne infos = new MPersonne();
        MClient mclient = new MClient();
        log.info("---------------#Entrer de la fonction#------------------");
        log.info("Recherche d'une personne......" + telephone + "#" + code);
        Personne personne = null;
        Optional<Personne> pers = personneService.findPersonneByPhoneAndCodePaysActive(telephone, code);
        if (pers.isPresent() && pers.get() != null) {
            personne = personneRepository.findByPersonneId(pers.get().getId());
        }
        if (personne != null) {
            log.info("Récupération de la personne......{ }", personne);
            Client client = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personne.getId());
            if (client != null) {
                log.info("Récupération du client......{ }", client);
                Compte compte = compteService.findComptePrincipal(client);
                if (compte != null) {
                    log.info("Récupération du compte......{ }", compte);
                    MCompte mcpt = new MCompte();
                    mcpt.setNumeroCompte(compte.getValeurCompte());
                    mcpt.setTypeCompte(MTypeCompte.valueOf(compte.getCategorieCompte().name()));
                    if (compte.getCompteAutre() != null) {
                        mcpt.setCodeBanque(compte.getCompteAutre().getCodeFse());
                        mcpt.setTeneurCompte(compte.getCompteAutre().getCodeFse());
                    }
                    if (compte.getCompteBanque() != null) {
                        mcpt.setCodeBanque(compte.getCompteAutre().getCodeFse());
                        mcpt.setTeneurCompte(compte.getCompteAutre().getCodeFse());
                    }
                    mclient.setIdtId(client.getTagIdentityRef());
                    mclient.setCompte(mcpt);
                }
            }
            log.info("Recherche d'une personne in ......");

            log.info("Recherche des informations d'une personne......");
            infos.setCodeTelephone(code);
            infos.setTelephone(telephone);
            infos.setEmail(personne.getEmail());
            if (personne.getTypePersonne().equals(TypePersonne.PHYSIQUE)) {
                PersonnePhysique personnePhysique = personne.getPersonnePhysique();
                if (personnePhysique != null) {

                    String paysResidence = personnePhysique.getPaysResidence() != null ? personnePhysique.getPaysResidence().getCode() : null;
                    String nationalite = personnePhysique.getNationalite() != null ? personnePhysique.getNationalite().getCode() : null;
                    infos.setSexe(personnePhysique.getSexe() != null ? MSexe.valueOf(personnePhysique.getSexe().name()) : MSexe.NON_DEFINI);
                    infos.setNom(personnePhysique.getNomClient());
                    infos.setPrenom(personnePhysique.getPrenomClient());
                    infos.setDateNaissance(personnePhysique.getDateNaissance());
                    infos.setLieuNaissance(personnePhysique.getLieuNaissance());
                    infos.setEmail(personne.getEmail());
                    infos.setPaysResidence(paysResidence);

                    infos.setCodeNationalite(nationalite);
                    infos.setAdresse(personne.getAdresse());
                    infos.setNomDelaMere(personnePhysique.getNomMereClient());
                    infos.setProfession(personne.getBrancheActivite() != null ? personne.getBrancheActivite().getLibelle() : "");
                }
                List<Piece> pieces = pieceRepository.findByTitulaireId(personne.getId());
                if (pieces.size() > 0) {
                    Piece p = pieces.get(0);
                    MDocIdentification docIdentification = new MDocIdentification();
                    docIdentification.setReference(p.getReferencePiece());
                    //  docIdentification.setDateExpiration(p.getDateExpiration());
                    //   docIdentification.setDateDelivrance(p.getDateDelivrance());
                    docIdentification.setLieuDelivrance(p.getLieuDelivrance());
                    MTypePiece typepiece = new MTypePiece();
                    typepiece.setCodeTypePiece(p.getTypePiece().getCodeTypePiece());
                    typepiece.setLibelleTypePiece(p.getTypePiece().getLibelleTypePiece());
                    docIdentification.setTypePiece(typepiece);
                    mclient.setDocIdentification(docIdentification);
                }
            } else {
                PersonneMorale personneMorale = personne.getPersonneMorale();
                infos.setNom(personneMorale.getRaisonSociale());
                infos.setPrenom("");
            }
            //Mise ï¿½ jour des informations du document d'identificatio

            mclient.setPersonne(infos);
            log.info("Fin de la recherche......");
            return mclient;

        }

        return null;
    }

    public AgentInformation findCodeActeurExterne(String codeActeur, String codeFse) throws IOException, IOException, JAXBException {
        AgentInformation cust = apiService.getInfoAgentCompte(codeActeur, codeFse);
        return cust;
    }

    public MOperation getdoOperationExternalParameter(String strMOperation) throws JAXBException, JAXBException, IOException, IOException {
        Gson gson = new Gson();
        MOperation mOperation = gson.fromJson(strMOperation, MOperation.class);

        return mOperation;
    }

    public MOperationFacture getdoOperationFactExternalParameter(String strMOperation) throws JAXBException, JAXBException, IOException, IOException {
        Gson gson = new Gson();
        MOperationFacture mOperation = gson.fromJson(strMOperation, MOperationFacture.class);
        return mOperation;
    }

    public MResponse doOperationExternal(MOperation mOperation, String codeFse) throws JAXBException, JAXBException, IOException, IOException {
        StopWatch stopWatch = new StopWatch("Measure Code Execution");
        List<MParametre> mParametres = new ArrayList();
        stopWatch.start("1. Marchalling");

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

        Optional<Personne> pers;
        boolean debitEstPossible = false;
        Balance bal = new Balance();
        bal.setSolde(0D);
        MResponse mResponse = new MResponse();
        if (mOperation.getEmetteur() != null && mOperation.getEmetteur().getTitulaire() != null && mOperation.getEmetteur().getTitulaire().getTelephone() != null && mOperation.getEmetteur().getTitulaire().getCodeTelephone() != null) {
            Compte cpt = verifIfAutoriser(mOperation.getEmetteur().getCompte().getNumeroCompte(), mOperation.getActeur(), mOperation.getIdentifiant(), mOperation.getEmetteur().getTitulaire().getCodeTelephone(), 0);
            mOperation.setPreautorisaton(1);
            String jsonStr = ow.writeValueAsString(mOperation);
            pers = personneService.findPersonneByPhoneAndCodePaysActive(mOperation.getEmetteur().getTitulaire().getTelephone(), mOperation.getEmetteur().getTitulaire().getCodeTelephone());
            if (pers.isPresent()) {
                ActiviteClient activite = activiteClientService.save(mOperation, pers.get(), StatutOperation.ENCOURS, codeFse);
                CompteOperationDTO compteOperationDTO = getCompte(mOperation.getEmetteur().getCompte(), pers.get(), mOperation.getApplication(), Boolean.FALSE, mOperation.getPreautorisaton());
                if (compteOperationDTO != null) {
                    if (compteOperationDTO.getCompte().getCompteBanque() != null) {
                        bal = apiService.getSolde(compteOperationDTO.getCompte().getValeurCompte(), compteOperationDTO.getCompte().getCompteBanque().getCodeAgence(), compteOperationDTO.getCompte().getCompteBanque().getCodeFse());
                    } else if (compteOperationDTO.getCompte().getCompteAutre() != null) {
                        bal = apiService.getSolde(compteOperationDTO.getCompte().getValeurCompte(), compteOperationDTO.getCompte().getCompteAutre().getCodeFse(), compteOperationDTO.getCompte().getCompteAutre().getCodeFse());
                    } else if (compteOperationDTO.getCompte().getCarte() != null) {
                        bal = apiService.getSolde(compteOperationDTO.getCompte().getValeurCompte(), compteOperationDTO.getCompte().getCarte().getLast4Digit(), compteOperationDTO.getCompte().getCarte().getCodeFse());
                    }

                    Double solde = bal.getSolde();
                    BigDecimal soldeEnBigDecimal = new BigDecimal(solde);
                    BigDecimal mtop = Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(mOperation.getMontantTaxe());
                    if (Util.isBiggerOrEqualThan(soldeEnBigDecimal, mtop)) {
                        debitEstPossible = true;
                        /**
                         * Mettre en place la possibilité de vérifier le solde
                         * du compte
                         */

                        //  String str = (String) Util.marshall(mOperation, String.class);
                        if (cpt != null) {

                            stopWatch.stop();
                            stopWatch.start("1. Settlement");
                            mParametres.add(new MParametre("STROPERATION", jsonStr, 1));
                            mParametres.add(new MParametre("TYPESERVICE", mOperation.getTypeService(), 1));
                            mResponse = apiService.settlementTransaction(mParametres, codeFse);
                            if (mResponse != null && mResponse.getCode() != null && mResponse.getCode().equals("0")) {

                                activite.setStatut(StatutOperation.PAYE);
                                activite.setReference(mResponse.getTansactionId());

                            } else {
                                activite.setStatut(StatutOperation.REJETE);

                            }
                            activiteClientService.save(activite);
                            stopWatch.stop();

                        } else {

                            return new MResponse("-1", "Attention vous ne pouvez pas effectuer cette operation.", null);

                        }

                    } else {
                        debitEstPossible = false;
                        return new MResponse("-1", "le solde du client est insufisant", null);

                    }
                } else {
                    return new MResponse("-1", "le compte est introuvable", null);

                }
            } else {
                return new MResponse("-1", "le compte est introuvable", null);
            }

        }

        return mResponse;
    }

    public MResponse doOperationExternalFacture(MOperationFacture mOperation, String codeFse) throws JAXBException, JAXBException, IOException, IOException {
        StopWatch stopWatch = new StopWatch("Measure Code Execution");
        List<MParametre> mParametres = new ArrayList();
        stopWatch.start("1. Marchalling");

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonStr = ow.writeValueAsString(mOperation);

        /**
         * Mettre en place la possibilité de vérifier le solde du compte
         */
        //  String str = (String) Util.marshall(mOperation, String.class);
        Optional<Personne> pers;
        boolean debitEstPossible = false;
        Balance bal = new Balance();
        bal.setSolde(0D);

        MResponse mResponse = new MResponse();
        if (mOperation.getEmetteur() != null && mOperation.getEmetteur().getTitulaire() != null && mOperation.getEmetteur().getTitulaire().getTelephone() != null && mOperation.getEmetteur().getTitulaire().getCodeTelephone() != null) {
            pers = personneService.findPersonneByPhoneAndCodePaysActive(mOperation.getEmetteur().getTitulaire().getTelephone(), mOperation.getEmetteur().getTitulaire().getCodeTelephone());
            if (pers.isPresent()) {
                CompteOperationDTO compteOperationDTO = getCompte(mOperation.getBeneficiaire().getCompte(), pers.get(), mOperation.getApplication(), Boolean.FALSE, mOperation.getPreautorisaton());
                if (compteOperationDTO != null) {
                    if (compteOperationDTO.getCompte().getCompteBanque() != null) {
                        bal = apiService.getSolde(compteOperationDTO.getCompte().getValeurCompte(), compteOperationDTO.getCompte().getCompteBanque().getCodeAgence(), compteOperationDTO.getCompte().getCompteBanque().getCodeFse());
                    } else if (compteOperationDTO.getCompte().getCompteAutre() != null) {
                        bal = apiService.getSolde(compteOperationDTO.getCompte().getValeurCompte(), compteOperationDTO.getCompte().getCompteAutre().getCodeFse(), compteOperationDTO.getCompte().getCompteAutre().getCodeFse());
                    } else if (compteOperationDTO.getCompte().getCarte() != null) {
                        bal = apiService.getSolde(compteOperationDTO.getCompte().getValeurCompte(), compteOperationDTO.getCompte().getCarte().getLast4Digit(), compteOperationDTO.getCompte().getCarte().getCodeFse());
                    }

                    Double solde = bal.getSolde();
                    BigDecimal soldeEnBigDecimal = new BigDecimal(solde);
                    BigDecimal mtop = Util.getZeroIfNull(mOperation.getMontantFraisClient()).add(mOperation.getMontant()).add(mOperation.getMontantTaxe());
                    if (Util.isBiggerOrEqualThan(soldeEnBigDecimal, mtop)) {
                        debitEstPossible = true;
                        /**
                         * Mettre en place la possibilité de vérifier le solde
                         * du compte
                         */
                        Compte cpt = verifIfAutoriser(mOperation.getEmetteur().getCompte().getNumeroCompte(), mOperation.getActeur(), mOperation.getIdentifiant(), mOperation.getEmetteur().getTitulaire().getCodeTelephone(), 0);
                        //  String str = (String) Util.marshall(mOperation, String.class);

                        if (cpt != null) {
                            mOperation.setPreautorisaton(1);
                            stopWatch.stop();
                            stopWatch.start("1. Settlement");
                            mParametres.add(new MParametre("STROPERATION", jsonStr, 1));
                            mParametres.add(new MParametre("TYPESERVICE", mOperation.getTypeService(), 1));
                            mResponse = apiService.settlementTransaction(mParametres, codeFse);
                            stopWatch.stop();

                        } else {

                            return new MResponse("-1", "Attention vous ne pouvez pas effectuer cette operation.", null);

                        }

                    } else {
                        debitEstPossible = false;
                        return new MResponse("-1", "le solde du client est insufisant", null);

                    }
                } else {
                    return new MResponse("-1", "le compte est introuvable", null);

                }
            } else {
                return new MResponse("-1", "le compte est introuvable", null);
            }

        }

        return mResponse;
    }

    public GrData getListFacturierFromFse(String codeFse, String secteur) throws IOException, JAXBException {
        List<MParametre> mParametres = new ArrayList();
        GrData grData = null;
        UniteOrganisation fseTeneur = uniteOrganisationService.findByCodeFSE(codeFse);
        if (fseTeneur != null) {
            mParametres.add(new MParametre("CODEUO", fseTeneur.getCodeUO(), 1));
            mParametres.add(new MParametre("SECTEUR", secteur, 1));
            grData = apiService.getListeFacturier(mParametres, codeFse);
        }

        return grData;
    }

    public MDataTransaction getInfosDataTransaction(String codeFse,
            String telephone,
            String code,
            String canal,
            String typeService,
            String codePv,
            String codeUoDest,
            String codeUoSrc,
            String montant, String codeAgence, String typeCompte,
            String valeurCompte,
            String teneur, String codeacteur, String alias) throws IOException, JAXBException {
        List<MParametre> mParametres = new ArrayList();
        mParametres.add(new MParametre("TELEPHONE", telephone, 1));
        mParametres.add(new MParametre("CODE", code, 1));
        mParametres.add(new MParametre("CANAL", canal, 1));
        mParametres.add(new MParametre("TYPESERVICE", typeService, 1));
        mParametres.add(new MParametre("CODEPV", codePv, 1));
        mParametres.add(new MParametre("MONTANT", montant.toString(), 1));
        mParametres.add(new MParametre("CODEAGENCE", codeAgence, 1));
        mParametres.add(new MParametre("TYPECOMPTE", typeCompte, 1));
        mParametres.add(new MParametre("VALEURCOMPTE", valeurCompte, 1));
        mParametres.add(new MParametre("TENEUR", teneur, 1));
        if (codeacteur != null) {
            mParametres.add(new MParametre("CODEACTEUR", codeacteur, 1));
        } else {
            mParametres.add(new MParametre("CODEACTEUR", "0", 1));
        }
        mParametres.add(new MParametre("ALIAS", alias, 1));
        mParametres.add(new MParametre("CODEUODEST", codeUoDest, 1));
        mParametres.add(new MParametre("CODEUOSRC", codeUoSrc, 1));
        MDataTransaction dataTransaction = apiService.getInfosDataTransaction(mParametres, codeFse);
        return dataTransaction;

    }

    /**
     * *
     * Fonction permettant de retourner un client externe
     *
     * @param telephone
     * @param code
     * @param codeFse
     * @return
     * @throws IOException
     * @throws IOException
     * @throws JAXBException
     */
    public MClient findInfosClientExterne(String telephone, String code, String codeFse) throws IOException, IOException, JAXBException {
        CustomerInformation cust = apiService.getInfoClientCompte(telephone, code, codeFse);
        if (cust != null) {
            MPersonne infos = new MPersonne();
            MClient mclient = new MClient();
            log.info("---------------#Entrer de la fonction#------------------");
            log.info("Recherche d'une personne......" + telephone + "#" + code);

            MCompte mcpt = new MCompte();
            if (cust != null && cust.getListCompte().size() > 0) {
                mcpt.setNumeroCompte(cust.getListCompte().get(0).getNumeroCompte());
                mcpt.setTypeCompte(MTypeCompte.valueOf(cust.getListCompte().get(0).getTypeCompte()));

                mcpt.setTeneurCompte(cust.getListCompte().get(0).getTeneurCompte());
                if (cust.getListCompte().get(0).getTypeCompte().equals("BANQUE")) {
                    mcpt.setCodeAgence(cust.getListCompte().get(0).getCodeAgence());
                    mcpt.setCodeBanque(cust.getListCompte().get(0).getCodeBanque());
                    mcpt.setTeneurCompte(codeFse);
                    infos.setCodeTelephone(cust.getCodeIndicatif());
                    infos.setTelephone(cust.getTelephone());
                } else if (cust.getListCompte().get(0).getTypeCompte().equals("CARTE")) {
                    mcpt.setLas4Digit(cust.getListCompte().get(0).getCodeAgence());
                    mcpt.setTeneurCompte(codeFse);
                    infos.setCodeTelephone(cust.getCodeIndicatif());
                    infos.setTelephone(telephone);
                } else if (cust.getListCompte().get(0).getTypeCompte().equals("AUTRE")) {
                    infos.setCodeTelephone(cust.getCodeIndicatif());
                    infos.setTelephone(telephone);
                }
                mclient.setCompte(mcpt);
            }
            log.info("Recherche d'une personne in ......");

            log.info("Recherche des informations d'une personne......");

            infos.setSexe(cust.getSexe() != null ? MSexe.valueOf("MASCULIN") : MSexe.NON_DEFINI);
            infos.setNom(cust.getNom());
            infos.setPrenom(cust.getPrenom());
            // infos.setDateNaissance(personnePhysique.getDateNaissance());
            infos.setLieuNaissance(cust.getLieuNaissance());
            //Mise ï¿½ jour des informations du document d'identificatio
            mclient.setPersonne(infos);

            log.info(
                    "Fin de la recherche......");
            log.info(
                    "Fin de la recherche......{}", infos);

            mclient.setPersonne(infos);

            log.info(
                    "Fin de la recherche......{}", mclient);
            return mclient;
        }
        return null;

    }

    /**
     *
     * @param codeFse
     * @return
     */
    public List<MLocalisationOrganisation> findLocalisationByType(String codeFse, String latitudeMin, String latitudeMax, String longitudeMin, String longitudeMax) {
        UniteOrganisation uo = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(codeFse);
        List<PointVenteDTO> pvList = pointVenteService.findAllCordByFSE(uo, longitudeMin, longitudeMax, latitudeMin, latitudeMax);
        List<MLocalisationOrganisation> listMLocalisationOrganisation = new ArrayList();
        pvList.forEach((PointVenteDTO pointVenteDTO) -> {
            PointVente pv = pointVenteService.findPointByCode(pointVenteDTO.getCodePointVente());
            if (pointVenteDTO.getEstSurCard().equals(Boolean.TRUE)) {
                log.debug("-------***--------------" + pointVenteDTO.getUniteOrganisationId());
                UniteOrganisationDTO uo1 = uniteOrganisationService.findOneOptimse(pointVenteDTO.getUniteOrganisationId());
                MLocalisationOrganisation mLocalisationOrganisation = new MLocalisationOrganisation();
                mLocalisationOrganisation.setCodePv(pointVenteDTO.getCodePointVente());
                mLocalisationOrganisation.setLatitude(pointVenteDTO.getLatitude());
                mLocalisationOrganisation.setLongitude(pointVenteDTO.getLongitude());
                mLocalisationOrganisation.setType(MTypeOrganisation.valueOf(pv.getUniteOrganisation().getTypeUO().getCodeTypeUO()));
                if (uo1 != null) {
                    mLocalisationOrganisation.setTelephone(uo1.getTelephone());
                    mLocalisationOrganisation.setAdresse(uo1.getAdresse());
                    mLocalisationOrganisation.setCodeAgent(uo1.getCodeUO());
                    mLocalisationOrganisation.setNomOrganisation(uo1.getLibelleUniteOrganisation());
                }
                listMLocalisationOrganisation.add(mLocalisationOrganisation);
            }
        });
        return listMLocalisationOrganisation;

    }

    /**
     * Liste des organisations en fonction du type (FSE ou PAYS)
     *
     * @param typeOrganisation
     * @return
     */
    public List<MOrganisation> findOraganisationByType(MTypeOrganisation typeOrganisation) {
        Optional<TypeUniteOrganisation> typeExist = typeUniteOrganisationRepository.findByCodeTypeUO(String.valueOf(typeOrganisation));
        if (typeExist.isPresent()) {
            List<UniteOrganisation> organisations;
            if (organisationMapDTO.containsKey(typeExist.get().getCodeTypeUO()) && organisationMapDTO.get(typeExist.get().getCodeTypeUO()) != null) {
                organisations = organisationMapDTO.get(typeExist.get().getCodeTypeUO());
            } else {
                organisations = uniteOrganisationService.findByType(typeExist.get().getId());
                if (organisations != null) {
                    organisationMapDTO.put(typeExist.get().getCodeTypeUO(), organisations);
                }

            }
            return getMOrganisations(typeOrganisation, organisations, null);
        }
        return Collections.emptyList();
    }

    public MOrganisation findOraganisationByCode(String codeUo) {
        UniteOrganisation organisation = uniteOrganisationService.findByCode(codeUo);
        return getMOrganisation(organisation);
    }

    /**
     *
     * @param codePv
     * @return
     */
    public MOrganisation findOraganisationByCodePV(String codePv) {
        UniteOrganisation organisation = uniteOrganisationService.findByCodePv(codePv);
        return getMOrganisation(organisation);
    }

    /**
     *
     * @param organisation
     * @return
     */
    private MOrganisation getMOrganisation(UniteOrganisation organisation) {

        MOrganisation org = new MOrganisation();
        if (organisation != null) {
            org.setType(MTypeOrganisation.valueOf(organisation.getTypeUO().getCodeTypeUO()));
            org.setCodePere(organisation.getPereUO().getCodeUO());
            org.setCodeUo(organisation.getCodeUO());
            org.setNom(organisation.getLibelleUniteOrganisation());
            org.setTypeDuPere(MTypeOrganisation.valueOf(organisation.getPereUO().getTypeUO().getCodeTypeUO()));
        }

        return org;

    }

    List<MDevise> mListeDevise;

    private List<MDevise> geListeDevise(Long uoId) {
        List<MDevise> listDevise = new ArrayList<>();
        List<UoDeviseDTO> uoDeviseDTO = uoDeviseService.findByActeur(uoId);
        uoDeviseDTO.forEach(uoDev -> {
            MDevise devise = new MDevise();
            devise.setCode(uoDev.getCode());
            devise.setLibelle(uoDev.getDeviseLibelle());
            listDevise.add(devise);
        });
        return listDevise;
    }

    /**
     * Recherche les organisations d'un type donnï¿½
     *
     * @param typeOrganisation
     * @param organisations
     * @return
     */
    private List<MOrganisation> getMOrganisations(MTypeOrganisation typeOrganisation, List<UniteOrganisation> organisations, String codeUo) {
        List<MOrganisation> organisationList = new ArrayList<>();
        organisations.forEach(organisation -> {
            MOrganisation org = new MOrganisation();
            List<MDevise> mListeDevise = geListeDevise(organisation.getId());
            MDevise devise;
            devise = getDeviseDefault(organisation.getId());
            org.setCodeBanque(organisation.getCodeBanque());
            org.setCodeFse(organisation.getCodeFse());
            org.setNom(organisation.getLibelleUniteOrganisation());
            org.setType(typeOrganisation);
            org.setCodePere(codeUo);
            org.setDevise(devise);
            org.setmListeDevise(mListeDevise);
            org.setCodeUo(organisation.getCodeUO());
            org.setCodePays(organisation.getCodePays());
            organisationList.add(org);
        });
        return organisationList;
    }

    /**
     *
     */
    private List<MOrganisation> getMOrganisations(MTypeOrganisation typeOrganisation, List<UniteOrganisation> organisations) {
        List<MOrganisation> organisationList = new ArrayList<>();
        organisations.forEach(organisation -> {
            MOrganisation org = new MOrganisation();
            org.setCodeBanque(organisation.getCodeBanque());
            org.setCodeFse(organisation.getCodeFse());
            org.setNom(organisation.getLibelleUniteOrganisation());
            org.setType(typeOrganisation);
            if (organisation.getGroupeFse() != null) {
                org.setCodeGroupe(organisation.getGroupeFse().getCode());
            }

            if (organisation.getPereUO() != null) {
                org.setCodePere(organisation.getPereUO().getCodeUO());
            }

            org.setCodeUo(organisation.getCodeUO());
            org.setCodePays(organisation.getCodePays());
            organisationList.add(org);
        });
        return organisationList;
    }

    /**
     * Recherche les organisation fils d'un type donnï¿½ d'une organisation
     *
     * @param codeUo
     * @param type
     * @return
     */
    public List<MOrganisation> getAllUniteOrganisationsFilsByType(String codeUo, MTypeOrganisation type) {

        UniteOrganisation uo = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(codeUo, Boolean.FALSE);
        ArrayList<UniteOrganisation> listFils = new ArrayList<>();
        uniteOrganisationService.findAllUoFilsByType(TypeUO.valueOf(type.toString()), uo, listFils);
        return getMOrganisations(type, listFils, codeUo);

    }

    /**
     *
     * @param codePays
     * @param type
     * @return
     */
    public List<MOrganisation> getAllUniteOrganisationsCodePays(String codePays, MTypeOrganisation type) {
        TypeUniteOrganisation typeExist = typeUniteOrganisationService.findByCodeType(String.valueOf(type));
        if (typeExist != null) {
            List<UniteOrganisation> listFils;
            if (organisationMapDTO.containsKey(String.valueOf(type)) && organisationMapDTO.get(String.valueOf(type)) != null) {
                listFils = organisationMapDTO.get(String.valueOf(type));
            } else {
                listFils = uniteOrganisationRepository.findByTypeUOAndSupprimeIsFalseOrderByLibelleUniteOrganisation(typeExist);
                if (listFils != null) {
                    organisationMapDTO.put(String.valueOf(type), listFils);
                }

            }
            return getMOrganisations(type, listFils);
        }
        return Collections.emptyList();

    }

    /**
     *
     * @param id
     * @return
     */
    public Produit getFacturierProduit(Long id) {
        Produit prod = produitRepository.findById(id).orElse(null);
        return prod;
    }

    /**
     *
     */
    public String getFacturierFSE(Long id) {
        String codeUo = null;
        Facturier fac = facturierRepository.findById(id).orElse(null);
        // if (fac.getTypeFacturier().equals(TypeFacturier.FSE)) {

        //   codeUo = fac.getUniteOrganisation().getCodeFse();
        //} else {
        codeUo = fac.getFse();
        // }
        return codeUo;
    }

    /**
     *
     */
    public List<MFacturier> getOperateurTelephonie(String codeFse) {
        UniteOrganisation uo = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(codeFse);
        if (uo != null) {
            List<Facturier> listFac = facturierRepository.findByPossesseurAndSecteurActivite(uo, SecteurActivite.TELEPHONIE);
            List<MFacturier> listeFacturier = new ArrayList();
            listFac.forEach(fac -> {
                MFacturier mf = new MFacturier();
                mf.setCodeFacturier(fac.getCodeFacturier());
                mf.setCodeUo(fac.getUniteOrganisation().getCodeUO());
                mf.setId(fac.getId());
                mf.setNomFacturier(fac.getNomFacturier());
                listeFacturier.add(mf);
            });

            return listeFacturier;
        }
        return null;

    }

    private String getDescription(Operation operation) {
        String desc = "";
        String telephone = "";
        String nomOrganisation = "";
        if (operation.getPointVente().getUniteOrganisation().getLibelleUniteOrganisation() != null) {
            nomOrganisation = operation.getPointVente().getUniteOrganisation().getLibelleUniteOrganisation();
        }

        String typeOperation = operation.getTypeService().getDescription();
        String reference = operation.getCodeOperation();
        desc = typeOperation + " " + nomOrganisation;
        if (reference != null) {
            if (reference.length() > 0) {
                desc = desc + " " + reference;
            }
        }
        if (operation.getEmetteur() != null) {
            if (operation.getEmetteur().getPersonne() != null) {
                if (operation.getEmetteur().getPersonne().getTelephone() != null) {
                    telephone = operation.getEmetteur().getPersonne().getTelephone();
                    if (operation.getEmetteur().getPersonne().getPersonnePhysique() != null) {

                        desc = desc + " de " + operation.getEmetteur().getPersonne().getPersonnePhysique().getNomClient() + " " + telephone;
                    } else {
                        desc = desc + " de " + telephone;
                    }

                }

            }
        }
        if (operation.getTypeService().getCode().equals(TypeServiceLibelle.PAIEMENT_FACTURE.name())) {
            if (operation.getMotif() != null) {
                desc = desc + ", " + operation.getMotif();
            }

        }
        if (operation.getBeneficaire() != null) {
            if (operation.getBeneficaire().getPersonne() != null) {
                if (operation.getBeneficaire().getPersonne().getTelephone() != null) {
                    telephone = operation.getBeneficaire().getPersonne().getTelephone();
                    if (operation.getBeneficaire().getPersonne().getPersonnePhysique() != null) {
                        if (operation.getEmetteur() != null && operation.getBeneficaire().getPersonne().equals(operation.getEmetteur().getPersonne())) {
                            desc = desc + " vers lui même ";
                        } else {
                            desc = desc + " vers " + operation.getBeneficaire().getPersonne().getPersonnePhysique().getNomClient() + " " + telephone;
                        }

                    } else {
                        desc = desc + " vers " + telephone;
                    }

                }

            }
        }

        return desc;
    }

    public ModelAndView getPrint(ApplicationContext context, List<MParametre> mParametres, String codeOperation) {
        return etatStatsService.printRecu(context, mParametres, codeOperation);
    }

    /**
     *
     * @param id
     * @return
     */
    public String getFacturierUo(Long id) {
        String codeUo = null;
        Facturier fac = facturierRepository.findById(id).orElse(null);
        if (fac.getTypeFacturier().equals(TypeFacturier.FSE) || (fac.getTypeFacturier().equals(TypeFacturier.MARCHAND))) {
            codeUo = fac.getUniteOrganisation().getCodeUO();
        }
        return codeUo;
    }

    public ExternalResponse getStatus(String codeOperation, String option) {
        return operationService.getOperationStatus(codeOperation, option);
    }

    /**
     *
     */
    public void sendMail(String email, String etiquette, HashMap<String, String> keyVal) {
        operationService.envoiMailOperation(email, etiquette, keyVal);
    }

    /**
     *
     * @param numeroCompte
     * @param codeFse
     * @param cpt
     * @param dateDebut
     * @param dateFin
     * @param codePv
     * @return
     * @throws JAXBException
     */
    public Statement getAccountHistTransPoint(String numeroCompte, String codeFse, ZonedDateTime dateDebut, ZonedDateTime dateFin, String codePv) throws JAXBException {
        Message msg = new Message();
        List<Mouvement> listTransact = new ArrayList<Mouvement>();
        String description = "";
        String codeOperation = "";
        boolean trouve = false;
        Integer resultat = -1;
        Statement stmt = new Statement();
        Integer errCode = -1;
        String codeExterne = "";
        String errMsg = null;
        Long montant;
        List<TransactionComptePti> listTrans = null;
        Optional<PointVente> pointVenteExist = null;

        Compte cpt = compteService.findByCompteFse(numeroCompte, codeFse);
        if (cpt.getCompteAutre() != null) {

            if (codePv != null) {
                // pointVenteExist = pointVenteService.findByCode(codePv);

                PointVente opt = pointVenteService.findPointByCode(codePv);

                pointVenteExist = Optional.of(opt);

            }
            if (pointVenteExist.isPresent()) {
                listTrans = transactionComptePtiService.findByCompteAutrePointVente(cpt.getCompteAutre().getId(), dateDebut, dateFin, pointVenteExist.get().getId());
            } else {
                listTrans = transactionComptePtiService.findByCompteAutre(cpt.getCompteAutre().getId(), dateDebut, dateFin);
            }

            Iterator it = listTrans.iterator();
            while (it.hasNext()) {
                codeExterne = "";
                Mouvement mv = new Mouvement();
                TransactionComptePti trans = (TransactionComptePti) it.next();
                if (trans.getOperation() != null) {
                    codeExterne = trans.getOperation().getCodeExterne();
                    //   Operation oper= operationService.findByCodeOperationEx(codeExterne);
                    if (trans.getOperation() != null) {
                        if (trans.getOperation().getDescription() != null) {
                            description = trans.getOperation().getDescription();
                        } else {
                            description = trans.getOperation().getTypeService() != null ? trans.getOperation().getTypeService().getDescription() : "";
                        }

                    }

//                                description=trans.getOperation().getPointVente().getUniteOrganisation().getLibelleUniteOrganisation();
                }
                mv.setDescription(description);
                mv.setCodeOperation(codeExterne);
                mv.setDateMvt(trans.getDate().toLocalDateTime().toString());
                mv.setMontantMvt(trans.getMontant().doubleValue());
                if (trans.getType().equals(TypeTransactionCompte.CREDIT)) {
                    mv.setSens("C");
                } else {
                    mv.setSens("D");
                }
                listTransact.add(mv);
            }
            stmt.setListeMouvement(listTransact);

        }

        return stmt;
    }

    public Statement getMiniRelevePoint(String numeroCompte, String codeFse, String codePv) throws JAXBException {
        Message msg = new Message();
        List<Mouvement> listTransact = new ArrayList<Mouvement>();
        String description = "";
        boolean trouve = false;
        Integer resultat = -1;
        Statement stmt = new Statement();
        Integer errCode = -1;
        String codeExterne = "";
        String errMsg = null;
        Long montant;
        Integer nombr = 1;
        List<TransactionComptePtiDTO> listTrans = null;
        Optional<PointVente> pointVenteExist = null;
        Compte cpt = compteService.findByCompteFse(numeroCompte, codeFse);
        if (cpt.getCompteAutre() != null) {
            if (codePv != null) {
                // pointVenteExist = pointVenteService.findByCode(codePv);
                PointVente opt = pointVenteService.findPointByCode(codePv);
                pointVenteExist = Optional.of(opt);

            }
            if (pointVenteExist.isPresent()) {
                listTrans = transactionComptePtiService.findByCompteAutrePointVenteMiniReleve(cpt.getCompteAutre().getId(), pointVenteExist.get().getId(), 5);
            } else {
                listTrans = transactionComptePtiService.findByCompteAutrePointVenteMiniReleve(cpt.getCompteAutre().getId(), null, 5);
            }
            Iterator it = listTrans.iterator();
            while (it.hasNext()) {
                codeExterne = "";
                Mouvement mv = new Mouvement();
                TransactionComptePtiDTO trans = (TransactionComptePtiDTO) it.next();

                //   Operation oper= operationService.findByCodeOperationEx(codeExterne);
                description = trans.getDescription();

//                                description=trans.getOperation().getPointVente().getUniteOrganisation().getLibelleUniteOrganisation();
                mv.setDescription(description);
                mv.setDateMvt(trans.getDate().toLocalDateTime().toString());
                mv.setMontantMvt(trans.getMontant().doubleValue());
                if (trans.getType().equals(TypeTransactionCompte.CREDIT)) {
                    mv.setSens("C");
                } else {
                    mv.setSens("D");
                }
                listTransact.add(mv);
            }
            stmt.setListeMouvement(listTransact);

        }

        return stmt;
    }

    /**
     *
     * @param cpt
     * @param dateDebut
     * @param dateFin
     * @return
     * @throws JAXBException
     */
    public Message getAccountHistTrans(Compte cpt, ZonedDateTime dateDebut, ZonedDateTime dateFin) throws JAXBException {
        Message msg = new Message();
        List<Mouvement> listTransact = new ArrayList<Mouvement>();
        String description = "";
        boolean trouve = false;
        Integer resultat = -1;
        Statement stmt = new Statement();
        Integer errCode = -1;
        String codeExterne = "";
        String errMsg = null;
        Long montant;
        List<TransactionComptePti> listTrans = null;
        Optional<PointVente> pointVenteExist = null;
        try {
            if (cpt.getCompteAutre() != null) {
                listTrans = transactionComptePtiService.findByCompteAutre(cpt.getCompteAutre().getId(), dateDebut, dateFin);
                Iterator it = listTrans.iterator();
                while (it.hasNext()) {
                    codeExterne = "";
                    Mouvement mv = new Mouvement();
                    TransactionComptePti trans = (TransactionComptePti) it.next();
                    if (trans.getOperation() != null) {
                        codeExterne = trans.getOperation().getCodeExterne();
                        //   Operation oper= operationService.findByCodeOperationEx(codeExterne);
                        if (trans.getOperation() != null) {
                            if (trans.getOperation().getDescription() != null) {
                                description = trans.getOperation().getDescription();
                            } else {
                                description = trans.getOperation().getTypeService() != null ? trans.getOperation().getTypeService().getDescription() : "";
                            }

                        }

//                                description=trans.getOperation().getPointVente().getUniteOrganisation().getLibelleUniteOrganisation();
                    }
                    mv.setDescription(description);
                    mv.setCodeOperation(codeExterne);
                    mv.setDateMvt(trans.getDate().toLocalDateTime().toString());
                    mv.setMontantMvt(trans.getMontant().doubleValue());
                    if (trans.getType().equals(TypeTransactionCompte.CREDIT)) {
                        mv.setSens("C");
                    } else {
                        mv.setSens("D");
                    }
                    listTransact.add(mv);
                }
                stmt.setListeMouvement(listTransact);
                String str = (String) Util.marshall(stmt, String.class
                );
                msg.setText(str);
                if (listTrans.size() > 0) {
                    msg.setCodeErr("0");
                    msg.setMsg("OK");
                } else {
                    msg.setCodeErr("-1");
                    msg.setMsg("Attention !! Aucune ligne retournée");
                }

            }

            //listTransactionPrepaid.sort(Comparator.comparing(TransactionPrepaid::getDateMvt));
        } catch (Exception e) {
            e.printStackTrace();
            msg.setCodeErr("-1");
            msg.setMsg(e.getMessage());
            return msg;
        } finally {

        }

        return msg;
    }

    public Message getMiniReleveTrans(Compte cpt, String nombre) throws JAXBException {
        Message msg = new Message();
        List<Mouvement> listTransact = new ArrayList<Mouvement>();
        String description = "";
        boolean trouve = false;
        Integer resultat = -1;
        Statement stmt = new Statement();
        Integer errCode = -1;
        String codeExterne = "";
        String errMsg = null;
        Long montant;
        List<TransactionComptePtiDTO> listTrans = null;
        Optional<PointVente> pointVenteExist = null;

        try {
            if (cpt.getCompteAutre() != null) {
                listTrans = transactionComptePtiService.findByCompteAutrePointVenteMiniReleve(cpt.getCompteAutre().getId(), null, Integer.getInteger(nombre));
                Iterator it = listTrans.iterator();
                while (it.hasNext()) {
                    codeExterne = "";
                    Mouvement mv = new Mouvement();
                    TransactionComptePtiDTO trans = (TransactionComptePtiDTO) it.next();
                    if (trans.getOperationId() != null) {
                        codeExterne = trans.getCodeExterne();
                        //   Operation oper= operationService.findByCodeOperationEx(codeExterne);
                        if (trans.getOperationId() != null) {
                            Operation operation = operationRepository.findByOperationId(trans.getOperationId());

                            if (operation != null) {

                                description = operation.getDescription();
                            } else {
                                description = trans.getTypeService() != null ? trans.getDescription() : "";
                            }

                        }

//                                description=trans.getOperation().getPointVente().getUniteOrganisation().getLibelleUniteOrganisation();
                    }
                    mv.setDescription(description);
                    mv.setDateMvt(trans.getDate().toLocalDateTime().toString());
                    mv.setMontantMvt(trans.getMontant().doubleValue());
                    if (trans.getType().equals(TypeTransactionCompte.CREDIT)) {
                        mv.setSens("C");
                    } else {
                        mv.setSens("D");
                    }
                    listTransact.add(mv);
                }
                stmt.setListeMouvement(listTransact);
                String str = (String) Util.marshall(stmt, String.class
                );
                msg.setText(str);
                if (listTrans.size() > 0) {
                    msg.setCodeErr("0");
                    msg.setMsg("OK");
                } else {
                    msg.setCodeErr("-1");
                    msg.setMsg("Attention !! Aucune ligne retournée");
                }

            }

            //listTransactionPrepaid.sort(Comparator.comparing(TransactionPrepaid::getDateMvt));
        } catch (Exception e) {
            e.printStackTrace();
            msg.setCodeErr("-1");
            msg.setMsg(e.getMessage());
            return msg;
        } finally {

        }

        return msg;
    }

    public GrData getFacturierAndAttribute(String codeUo, String secteur) throws IOException {
        List<String> tabSecteur = new ArrayList();
        if (secteur.equals("ALL")) {
            for (SecteurActivite act : SecteurActivite.values()) {
                tabSecteur.add(act.name());
            }
        } else if (secteur.equals("AIRTIME")) {
            tabSecteur.add(SecteurActivite.TELEPHONIE.name());
        } else if (secteur.equals("GENERAL")) {
            for (SecteurActivite act : SecteurActivite.values()) {
                if (!act.equals(SecteurActivite.WALLET) && !act.equals(SecteurActivite.TELEPHONIE)) {
                    tabSecteur.add(act.name());
                }
            }
        } else if (secteur.equals("WALLET")) {
            tabSecteur.add(SecteurActivite.WALLET.name());
        }
        GrData grData = new GrData();
        if (tabSecteur.size() > 0) {
            List<ExFacturier> listfacturier = facturierRepository.findListeFacturierByPossesseur(codeUo, tabSecteur);
            int longueurFact = listfacturier.size();
            System.out.println("nombre " + longueurFact);
            int i;
            List<ExFacturier> lExFacturier = new ArrayList<ExFacturier>();
            for (i = 0; i < longueurFact; i++) {
                List<ProduitDTO> lproduit = produitRepository.findAllProduitByFacturierIdOrderByNumeroOrdreAsc(listfacturier.get(i).getId());
                int longueurp = lproduit.size();
                List<ExProduit> listProduit = new ArrayList<ExProduit>();
                int j;
                for (j = 0; j < longueurp; j++) {
                    List<MEcran> listE = getEcransOfProduit(lproduit.get(j).getId());
                    ExProduit exProduit = new ExProduit();
                    exProduit.setId(lproduit.get(j).getId());
                    exProduit.setNomProduit(lproduit.get(j).getNomProduit());
                    exProduit.setActif(lproduit.get(j).isActif());
                    exProduit.setNumeroOrdre(lproduit.get(j).getNumeroOrdre());
                    exProduit.setMontant(lproduit.get(j).getMontant());
                    exProduit.setaUnMontant(lproduit.get(j).isaUnMontant());
                    exProduit.setEcrans(listE);
                    listProduit.add(exProduit);
                }
                ExFacturier mfacturier = new ExFacturier();
                mfacturier.setId(listfacturier.get(i).getId());

                mfacturier.setCodeFacturier(listfacturier.get(i).getCodeFacturier());
                mfacturier.setNomFacturier(listfacturier.get(i).getNomFacturier());
                mfacturier.setProduit(listProduit);
                mfacturier.setImage(listfacturier.get(i).getImage());
                mfacturier.setValeurContentType(listfacturier.get(i).getValeurContentType());

                lExFacturier.add(mfacturier);
            }

            grData.setExfacturier(lExFacturier);
        }

        return grData;
    }

    private List<String> getTabSecteur(String secteur) {
        List<String> tabSecteur = new ArrayList();
        if (secteur.equals("ALL")) {
            for (SecteurActivite act : SecteurActivite.values()) {
                tabSecteur.add(act.name());
            }
        } else if (secteur.equals("AIRTIME")) {
            tabSecteur.add(SecteurActivite.TELEPHONIE.name());
        } else if (secteur.equals("GENERAL")) {
            for (SecteurActivite act : SecteurActivite.values()) {
                if (!act.equals(SecteurActivite.WALLET) && !act.equals(SecteurActivite.TELEPHONIE)) {
                    tabSecteur.add(act.name());
                }
            }
        } else if (secteur.equals("WALLET")) {
            tabSecteur.add(SecteurActivite.WALLET.name());
        }
        return tabSecteur;
    }

    public GrData getFacturierAndAttributeUoId(String codeUo, String secteur) throws IOException {
        List<String> tabSecteur = getTabSecteur(secteur);

        GrData grData = new GrData();
        if (tabSecteur.size() > 0) {
            ExFacturier efacturier = facturierRepository.findListeFacturierByPossesseurUo(codeUo, tabSecteur);
            int i;
            List<ExFacturier> lExFacturier = new ArrayList<ExFacturier>();
            List<ProduitDTO> lproduit = produitRepository.findAllProduitByFacturierIdOrderByNumeroOrdreAsc(efacturier.getId());
            int longueurp = lproduit.size();
            List<ExProduit> listProduit = new ArrayList<ExProduit>();
            int j;
            for (j = 0; j < longueurp; j++) {
                List<MEcran> listE = getEcransOfProduit(lproduit.get(j).getId());
                ExProduit exProduit = new ExProduit();
                exProduit.setId(lproduit.get(j).getId());
                exProduit.setNomProduit(lproduit.get(j).getNomProduit());
                exProduit.setActif(lproduit.get(j).isActif());
                exProduit.setNumeroOrdre(lproduit.get(j).getNumeroOrdre());
                exProduit.setMontant(lproduit.get(j).getMontant());
                exProduit.setaUnMontant(lproduit.get(j).isaUnMontant());
                exProduit.setEcrans(listE);
                listProduit.add(exProduit);
            }
            ExFacturier mfacturier = new ExFacturier();
            mfacturier.setId(efacturier.getId());
            mfacturier.setCodeUo(efacturier.getCodeUo());
            mfacturier.setCodeFacturier(efacturier.getCodeFacturier());
            mfacturier.setNomFacturier(efacturier.getNomFacturier());
            mfacturier.setProduit(listProduit);
            mfacturier.setImage(efacturier.getImage());
            mfacturier.setValeurContentType(efacturier.getValeurContentType());
            lExFacturier.add(mfacturier);
            grData.setExfacturier(lExFacturier);
        }

        return grData;
    }

    public List<MfactureMarchand> getFactureByFacturierAndPersonne(Long persId, Long facturierId, Long taille, Long limite) throws JAXBException {
        int i;
        //List<Facture> listfacture = factureRepository.findByProduitId(id);
        List<FactureDTO> listfacture = factureRepository.findFactureByFacturierAndPersonne(persId, facturierId, taille, limite);
        int longueur = listfacture.size();
        List<MfactureMarchand> listfactureMarchand = new ArrayList<MfactureMarchand>();
        for (i = 0; i < longueur; i++) {
            MfactureMarchand mfactureMarchand = new MfactureMarchand();
            mfactureMarchand.setReferencePaiement(listfacture.get(i).getReferencePaiement());
            mfactureMarchand.setMontantFacture(listfacture.get(i).getMontantPaiement());
            mfactureMarchand.setReferenceFacture(listfacture.get(i).getReferenceFacature());
            mfactureMarchand.setDatePaiement(listfacture.get(i).getDatePaiement());
            mfactureMarchand.setNomProduit(listfacture.get(i).getProduit());
            mfactureMarchand.setRefExterne(listfacture.get(i).getReferenceExt());
            String extradata = listfacture.get(i).getExtraData();
            String msg = "";
            MlistParametre mlistParametre = (MlistParametre) Util.unmarshall(extradata, MlistParametre.class);
            List<MParametre> mParametresRes = mlistParametre.getParametre();
            if (mParametresRes != null) {
                for (MParametre param : mParametresRes) {

                    msg = msg + param.getCodeParametre() + " : " + param.getValeur() + ",";

                }
                mfactureMarchand.setDonneExterne(msg);
            }
            listfactureMarchand.add(mfactureMarchand);
        }

        return listfactureMarchand;

    }

    public Message generateAndSendCodeOTP(String valeurCompte, String codeAgence, String teneur) {
        String[] otpAndSecret = RandomUtil.generateOtp(DELAIS);
        Message message = new Message();
        try {
            if (otpAndSecret.length >= 1) {
                MClient mclient = findInfosClientExterne(valeurCompte, codeAgence, teneur);
                if (mclient != null && mclient.getPersonne() != null && mclient.getPersonne().getTelephone() != null) {
                    //   String str = (String) Util.marshall(mclient, String.class);

                    String codePays = mclient.getPersonne().getCodeTelephone();
                    String telephone = mclient.getPersonne().getTelephone();

                    String secret = otpAndSecret[0];
                    String otp = otpAndSecret[1];
                    System.out.println(otp);
                    ExternalOTP externalOTP = new ExternalOTP();
                    externalOTP.setCodePays(mclient.getPersonne().getCodeTelephone());
                    externalOTP.setIdentifiant(valeurCompte);
                    externalOTP.setTelephone(mclient.getPersonne().getTelephone());
                    externalOTP.setSecret(secret);
                    externalOTP.setOtp(otp);
                    externalOTP.setCodeFacturier(teneur);
                    System.out.println(externalOTP.toString());
                    externalOTPRepository.save(externalOTP);
                    apiService.envoiSms(codePays + telephone, "Votre code de confirmation de la liaison est: " + otp, "OTP", codePays);
                    message.setCodeErr("0");
                    message.setMsg(codePays + telephone.substring(0, 3) + "____" + telephone.substring(telephone.length() - 2, telephone.length()));
                    message.setText(otp);
                } else {
                    message.setCodeErr("-1");
                    message.setMsg("Le client est inexistant, ou numéro incorrect!");
                }
                // Optional<Personne> searchPersonne = personneService.findPersonneByPhoneAndCodePays(telephone, codePays);//Inclure le code

            }

        } catch (Exception e) {
        }

        return message;
    }

    public List<MOrganisation> getAllUniteOrganisationsFilsByUoId(String telephone, String codePays) {
        ArrayList<UniteOrganisationDTO> listFils = new ArrayList<>();
        ArrayList<UniteOrganisationDTO> listAll = new ArrayList<>();
        List<UniteOrganisationDTO> result = new ArrayList<>();
        List<MOrganisation> organisationList = new ArrayList<>();
        Personne personne = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(telephone, codePays);
        log.debug("personne" + personne);
        if (personne != null) {
            ClientDTO clientDTO = clientRepository.findClientPersonne(personne.getId());
            if (clientDTO != null && clientDTO.getId() != null) {
                log.debug("clientid******************-----{ }" + clientDTO.getReseauId());
                UniteOrganisationDTO uo = uniteOrganisationService.findOneOptimse(clientDTO.getReseauId());
                if (uo != null && uo.getId() != null) {
                    result = uniteOrganisationService.findAllUoFilsOptimise(uo, listAll, listFils);
                }
                if (result != null) {
                    log.debug("result*-***---" + result.size());
                    result.forEach(uO -> {
                        log.debug("Entrer" + uO.getTypeUOCode());
                        if (uO.getTypeUOCode().equals(TypeUO.SOUS_DISTRIBUTEUR.name()) || uO.getTypeUOCode().equals(TypeUO.MARCHAND.name())) {
                            log.debug("okkkkkk");
                            MOrganisation org = new MOrganisation();
                            List<MDevise> mListeDevise = geListeDevise(uO.getId());
                            MDevise devise;
                            devise = getDeviseDefault(uO.getId());
                            org.setCodeBanque(uO.getCodeBanque());
                            org.setCodeFse(uO.getCodeFse());
                            org.setNom(uO.getLibelleUniteOrganisation());
                            org.setType(MTypeOrganisation.valueOf(uO.getTypeUOCode()));
                            org.setDevise(devise);
                            org.setmListeDevise(mListeDevise);
                            org.setCodeUo(uO.getCodeUO());
                            org.setCodePays(uO.getCodePays());
                            organisationList.add(org);
                        }
                    });
                }
                return organisationList;
            }
        }

        return Collections.emptyList();

    }

    @Transactional
    public MResponse doActionNumeroCardless(String login, String codePv, String codeActeur, String action) {
        log.info(" debut daction du service cardless a l'utilisateur {}", login);
        UniteOrganisation uo = uniteOrganisationService.findByCodeUOAndSupprimeIsFalse(codeActeur, false);
        User user = userService.findeByLogin(login.toLowerCase());
        // Optional<PointVente> pv = pointVenteService.findByCode(codePv);

        PointVente opt = pointVenteService.findPointByCode(codePv);

        Optional<PointVente> pv = Optional.of(opt);

        if (user != null) {
            if (user.getUniteOrganisation().getId().equals(uo.getId())) {
                /* controler si un utilisateur est dï¿½jï¿½ sur un point de vente */
                List<UserControlDTO> checkUserAutorise = userControlService.findUserAutoriseCardless(ActionEnum.ACTIVE_VENTE_DEPOSIT_CARDLESS.name(), user.getPhoneNumber());
                if (action != null) {
                    if (action.equals("ADD")) {
                        if (!checkUserAutorise.isEmpty()) {
                            return new MResponse("-1", "Utilisateur déja autoriser.");
                        }
                        UserControlDTO userControlDTO = new UserControlDTO();
                        userControlDTO.setAction(TypeControl.ACTIVE_VENTE_DEPOSIT_CARDLESS);
                        userControlDTO.setTypeActeur("AGENT");
                        userControlDTO.setCodePays(user.getCodeIndicatif());
                        userControlDTO.setLogin(user.getPhoneNumber());
                        userControlDTO.setSupprime(Boolean.FALSE);
                        userControlDTO = userControlService.save(userControlDTO);
                        if (userControlDTO != null) {
                            log.info(" fin d'autorisation de l'utilisateur {}", login);
                            return new MResponse("0", "Autorisation au service cardless effectuee avec succees");
                        }
                    } else if (action.equals("REMOVE")) {
                        if (checkUserAutorise.size() > 0) {
                            for (UserControlDTO userC : checkUserAutorise) {
                                if (userC.getId() != null) {
                                    userC.setSupprime(Boolean.TRUE);
                                    userC.setAction(TypeControl.ACTIVE_VENTE_DEPOSIT_CARDLESS);
                                    userControlService.save(userC);
                                }
                            }
                            return new MResponse("0", "Utilisateur retirer avec succes");
                        }
                        return new MResponse("-1", "Utilisateur non autorisée");

                    } else {
                        return new MResponse("-1", "Erreur au cours de l'enregistrement");
                    }
                }

            } else {
                log.error("l'organisation de l'utilisateur est incorrect {}", codeActeur);
                return new MResponse("-1", "l'organisation de l'utilisateur est incorrect");
            }
        }

        return new MResponse("-1", "Erreur au cours de l'enregistrement ");

    }

    public Message verifyOTP(String otp, String identifiant) throws JAXBException {
        Message message = new Message();
        message.setCodeErr("-1");
        message.setMsg("OTP invalide");
        ExtBwCustomer extBwCustomer = new ExtBwCustomer();
        ExternalOTP externalOTP = externalOTPRepository.findOTPValide(otp, identifiant);
        AtomicReference<Boolean> retour = new AtomicReference<>(false);
        if (externalOTP != null) {
            System.out.println("CODE OTP FOUND");
            if (externalOTP.getOtp().equals(otp) && RandomUtil.verifyOtp(externalOTP.getSecret(), otp, DELAIS)) {
                externalOTP.setValide(false);
                externalOTP.setSecret("xxx");
                extBwCustomer.setCodePays(externalOTP.getCodePays());
                extBwCustomer.setMsisdn(externalOTP.getTelephone());
                extBwCustomer.setOtp(externalOTP.getOtp());
                extBwCustomer.setNumeroCompte(externalOTP.getIdentifiant());
                externalOTPRepository.save(externalOTP);
                message.setCodeErr("0");
                message.setMsg("Opération éffectuée avec succès !");

            } else {
                message.setCodeErr("-1");
                message.setMsg("OTP expiré");
            }

        } else {
            message.setCodeErr("-1");
            message.setMsg("OTP invalide");
        }
        return message;
    }

    public PointVente updateAliasPoint(String codePv, String alias) {
        PointVente pv = null;
        PointVente opt = pointVenteService.findPointByCode(codePv);
        if (opt != null && alias != null) {
            opt.setAlias(alias);
            pv = pointVenteService.save(opt);
        }
        return pv;
    }

}
