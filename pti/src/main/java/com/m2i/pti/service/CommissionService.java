package com.m2i.pti.service;

import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.reporting.dto.XCommissionDTO;
import com.m2i.pti.repository.CommissionRepository;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.repository.TypeUniteOrganisationRepository;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.mapper.CommissionMapper;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.easyBatch.Read;
import com.m2i.pti.service.kafka.MessageListener;
import com.m2i.pti.service.kafka.MessageProducer;
import com.m2i.pti.web.rest.ChargementCompteResource;
import com.m2i.pti.web.rest.errors.ChargementException;
import com.m2i.pti.web.rest.errors.CustomSettlementException;
import java.io.*;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.easybatch.core.job.Job;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;

/**
 * Service Implementation for managing Commission.
 */
@Service

public class CommissionService {

    private final Logger log = LoggerFactory.getLogger(CommissionService.class);

    private final CommissionRepository commissionRepository;

    private final CommissionMapper commissionMapper;

    private final CompteService compteService;
    private final OperationRepository operationRepository;
    private final TransactionComptePtiService transactionComptePtiService;

    private final ApiService apiService;

    private final FileGeneratorService fileGeneratorService;

    private String pathname;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;
    private final UniteOrganisationService uniteOrganisationService;
    MessageProducer messageProducer;
    private final Read readService;
    OperationAuditEventService operationAuditEventService;

    public CommissionService(OperationAuditEventService operationAuditEventService, Read readService, MessageProducer messageProducer, OperationRepository operationRepository, UniteOrganisationRepository uniteOrganisationRepository, UniteOrganisationService uniteOrganisationService, CommissionRepository commissionRepository, CommissionMapper commissionMapper, CompteService compteService, TransactionComptePtiService transactionComptePtiService, ApiService apiService, FileGeneratorService fileGeneratorService, TypeUniteOrganisationRepository typeUniteOrganisationRepository) {
        this.commissionRepository = commissionRepository;
        this.commissionMapper = commissionMapper;
        this.compteService = compteService;
        this.transactionComptePtiService = transactionComptePtiService;
        this.apiService = apiService;
        this.uniteOrganisationService = uniteOrganisationService;
        this.fileGeneratorService = fileGeneratorService;
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.operationRepository = operationRepository;
        this.messageProducer = messageProducer;
        this.readService = readService;
        this.operationAuditEventService = operationAuditEventService;
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }

    /**
     * Save a commission.
     *
     * @param commissionDTO the entity to save
     * @return the persisted entity
     */
    public CommissionDTO save(CommissionDTO commissionDTO) {
        log.info("Request to save Commission : {}", commissionDTO);
        Commission commission = commissionMapper.toEntity(commissionDTO);
        commission = commissionRepository.save(commission);
        return commissionMapper.toDto(commission);
    }

    /**
     * Get all the commissions.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<CommissionDTO> findAll(Pageable pageable, Long uoId, Boolean estPaye, ZonedDateTime dateEnregistrement, ZonedDateTime dateFin) {
        ZonedDateTime dateTime = null;
        if (dateEnregistrement != null && dateFin != null) {
            log.debug("oK");
            dateFin = dateFin.plusHours(23);
        } else if (dateEnregistrement != null) {
            dateFin = dateEnregistrement.plusHours(23);
        } else if (dateFin != null) {
            dateEnregistrement = ZonedDateTime.now().minusMonths(12);
            dateFin = dateFin.plusHours(23);
        }
        /*if (dateEnregistrement != null) {
            dateTime = dateEnregistrement.plusHours(24);
        }*/
        return commissionRepository.findCommissionByMultiCriteria(estPaye, uoId, dateEnregistrement, dateFin, pageable).map(commissionMapper::toDto);
    }

    /**
     * Repartition des commissions Acq
     */
    public List<ResponseEntity> repartitionCommissionAcq() {
        List<Commission> commissions = commissionRepository.findByEstPayeAndEstEmetteurAndEstComptabilise(Boolean.TRUE, Boolean.FALSE, Boolean.FALSE);
        List<Long> organisationIds = commissions.stream().map(commission -> commission.getOrganisation().getId()).collect(Collectors.toList());
        List<ResponseEntity> response = new ArrayList<>();
        organisationIds.stream().distinct().collect(Collectors.toList()).forEach(orgId -> {
            List<Commission> commissionsOfBenf = commissions.stream().filter(comm -> comm.getOrganisation().getId() == orgId)
                    .collect(Collectors.toList());
            try {
                response.add(repartitionCommissionAcqByOperation(commissionsOfBenf));
            } catch (IOException e) {
                log.info(e.getMessage());
            }
        });
        return response;
    }

    /**
     * Repartition des commissions destinés aux acquereurs
     */
    public ResponseEntity repartitionCommissionAcqByOperation(List<Commission> commissions) throws IOException {
        Optional<Commission> commissionExist = commissions.stream().findFirst();
        if (commissionExist.isPresent()) {
            UniteOrganisation uoAcq = commissionExist.get().getOrganisation();
            if (!uoAcq.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.PAYS))) {
                UniteOrganisation fseSource = uniteOrganisationService.getFilialeOfUniteOrganisation(uoAcq);
                UniteOrganisation agenceSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(fseSource);
                Compte compteDebit = compteService.getCompteOfOrganisation(agenceSiege, EtiquetteCompteCode.CPTECOMACQ);
                BigDecimal montant = BigDecimal.ZERO;
                Compte compteCredit = compteService.getCompteOfOrganisation(uoAcq, EtiquetteCompteCode.CPTECOMACTEUR);
                for (Commission commission : commissions) {
                    UniteOrganisation uoBeneficiaire = commission.getOrganisation();
                    montant = montant.add(commission.getMontant());
                    commission.estComptabilise(Boolean.TRUE);
                    commissionRepository.save(commission);
                }
                return apiService.comptabiliseFraisComAcq(compteCredit, compteDebit, montant, fseSource);
            }
        }
        return null;
    }

    /**
     * *
     * Repartition des commissions en fonction du type
     *
     * @param acteur
     * @param etiquetteCompteCode
     */
    @Transactional
    public void repartitionCommissionSurPti(Acteur acteur, EtiquetteCompteCode etiquetteCompteCode) {
        List<Commission> commissions = commissionRepository.findByActeurAndEstPaye(acteur, Boolean.FALSE);
        Compte compteSrc;
        for (Commission commission : commissions) {
            compteSrc = compteService.getCompteOfOrganisation(commission.getOrganisation(), etiquetteCompteCode);
            Compte compteDest = compteService.getCompteOfOrganisationPTI(commission.getOrganisation(), EtiquetteCompteCode.CPTECOMACTEURPTI);
            if (compteSrc != null && compteDest != null) {
                BigDecimal montant = commission.getMontant();
                if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
                    compteService.debiterCompteAutre(compteSrc.getCompteAutre(), montant);
                    transactionComptePtiService.saveTransactionOfCompte(compteSrc.getCompteAutre(), compteDest.getCompteAutre(), montant, TypeTransactionCompte.DEBIT, null);
                    compteService.crediterCompteAutre(compteDest.getCompteAutre(), montant);
                    transactionComptePtiService.saveTransactionOfCompte(compteDest.getCompteAutre(), compteSrc.getCompteAutre(), montant, TypeTransactionCompte.CREDIT, null);
                    commission.estPaye(Boolean.TRUE);
                    commissionRepository.save(commission);
                }
            } else {
                log.info("Compte de commission est introuvable ");
            }
        }
    }

    /**
     * -Repartition des commissions de chaque acteur en utilisant l'omnicanal
     * -On debite le compteOperationActeur de l'acteur ayant effe
     */
    /* public List<ResponseEntity> repartitionCommissionWithOmnicanal() {
        List<Commission> commissions = commissionRepository.findByEstPayeAndEstEmetteurAndEstComptabiliseAndEtat(Boolean.TRUE, Boolean.TRUE, Boolean.FALSE, EtatValidite.VALIDE);
        List<Long> operationIds = commissions.stream().map(commission -> commission.getOperation().getId()).collect(Collectors.toList());
        List<ResponseEntity> response = new ArrayList<>();
        operationIds.stream().distinct().collect(Collectors.toList()).forEach(opId -> {
            List<Commission> commissionsOfOp = commissions.stream().filter(comm -> comm.getOperation().getId() == opId)
                    .collect(Collectors.toList());
            UniteOrganisation uoEm = commissionsOfOp.stream().findFirst().get().getOperation().getPointVente().getUniteOrganisation();
            response.add(repartitionByOperation(commissionsOfOp, uoEm));
        });
        return response;
    }*/
    /**
     * Repartition Par Operation
     */
    public void repartitionCommissionWithOmnicanal() throws IOException {
        Compte compteCommissionSrc;
        log.info("Debut de la repartition des commissions de la journée .............");
        Optional<TypeUniteOrganisation> typeUniteOrganisation = typeUniteOrganisationRepository.findByCodeTypeUO(TypeUO.FSE.name());
        if (typeUniteOrganisation.isPresent()) {
            List<UniteOrganisation> fseList = uniteOrganisationService.findByType(typeUniteOrganisation.get().getId());
            for (UniteOrganisation f : fseList) {
                UniteOrganisation agencesSiege = uniteOrganisationService.getFseAgenceSiege(f);
                log.info("Traitement en cours ::: " + f.getLibelleUniteOrganisation());
                //Recherche du compte commission
                compteCommissionSrc = compteService.getCompteOfOrganisation(agencesSiege, EtiquetteCompteCode.CPTEATTENTECOMM);
                String structureFormat = "";
                String allInfosVirement = "";
                ArrayList<UniteOrganisation> listAll = new ArrayList<>();
                ArrayList<UniteOrganisation> listPeres = new ArrayList<>();
                ArrayList<UniteOrganisation> listFils = new ArrayList<>();
                List<UniteOrganisation> uosFils = uniteOrganisationService.findUoFilsOfAnUO(f, listAll, listPeres, listFils, 0);
                List<Commission> commissions = new ArrayList<>();
                for (UniteOrganisation acteur : uosFils) {
                    log.info("Traitement en cours pour l'acteur ::: " + acteur.getLibelleUniteOrganisation());
                    List<Commission> commissionsActeurs = commissionRepository.findByEstPayeAndEstComptabiliseAndEtatAndOrganisationId(Boolean.TRUE, Boolean.FALSE, EtatValidite.VALIDE, acteur.getId());
                    if (!commissionsActeurs.isEmpty()) {
                        Commission c = new Commission();
                        c.setOrganisation(acteur);
                        c.setMontant(BigDecimal.valueOf(commissionsActeurs.stream().mapToDouble(co -> co.getMontant().doubleValue()).sum()));
                        commissions.add(c);
                    }
                }
                if (compteCommissionSrc != null && compteCommissionSrc.getCompteBanque() != null) {//Les informations du compte à debiter
                    log.info("Debut du traitement ..");
                    String valSeparateur = "|";
                    String separateurComb = "=";
                    CompteBanque compteBanqueOperationActeur = compteCommissionSrc.getCompteBanque();
                    structureFormat = getStringParam(true, structureFormat, "codeAgenceDebit", compteBanqueOperationActeur.getCodeAgence(), valSeparateur, separateurComb);
                    structureFormat = getStringParam(true, structureFormat, "codeBanqueDebit", compteBanqueOperationActeur.getCodeBanque(), valSeparateur, separateurComb);
                    structureFormat = getStringParam(true, structureFormat, "nomDonneurOrdre", "ApplicationPti", valSeparateur, separateurComb);
                    structureFormat = getStringParam(true, structureFormat, "compteDebiteur", compteCommissionSrc.getValeurCompte(), valSeparateur, separateurComb);
                    structureFormat += "listeDonneeVirement={";

                    if (!commissions.isEmpty()) {
                        int nbr = commissions.size();
                        int i = 0;
                        for (Commission commission : commissions) { //Les informations des comptes à crediter
                            i++;
                            Compte compte = compteService.getCompteOfOrganisation(commission.getOrganisation(), EtiquetteCompteCode.CPTECOMACTEUR);
                            if (compteCommissionSrc != null && compte != null) {
                                BigDecimal montant = commission.getMontant();
                                if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
                                    CompteBanque compteBanque = compte.getCompteBanque();
                                    String virement = "";
                                    String valSeparateur2 = "$";
                                    String separateurComb2 = "@";
                                    virement = getStringParam(true, virement, "codeAgence", compteBanque.getCodeAgence(), valSeparateur2, separateurComb2);
                                    virement = getStringParam(true, virement, "codeBanque", compteBanque.getCodeBanque(), valSeparateur2, separateurComb2);
                                    virement = getStringParam(true, virement, "montant", String.valueOf(montant.intValue()), valSeparateur2, separateurComb2);
                                    virement = getStringParam(true, virement, "compte", compte.getValeurCompte(), valSeparateur2, separateurComb2);
                                    virement = getStringParam(true, virement, "currency", "952", valSeparateur2, separateurComb2);
                                    virement = getStringParam(true, virement, "beneficiaire", commission.getOrganisation().getLibelleUniteOrganisation(), valSeparateur2, separateurComb2);
                                    virement = getStringParam(false, virement, "motif", "comptabilisation des commissions", valSeparateur2, separateurComb2);
                                    if (i < nbr) {
                                        virement += "!";
                                    }
                                    allInfosVirement += virement;
                                }
                            } else {
                                log.info("Un Compte de comptabilisation est introuvable ");
                            }
                        }
                        structureFormat += allInfosVirement + "}";
                        log.info("Chaine de caractère de comptabilisation :" + structureFormat);
                        ResultMessage result = apiService.comptabiliseFraisComEmetteur(structureFormat);
                        if (result != null && result.getErrCode() == 0) {
                            log.info("Comptabilisation des commissions effectuées avec succès.");
                            updateAllCommissions();
                        } else {
                            log.info("Echec de la comptabilisation des commissions.");
                        }
                    }
                } else if (!commissions.isEmpty()) {
                    int nbr = commissions.size();
                    int i = 0;
                    for (Commission commission : commissions) { //Les informations des comptes à crediter
                        i++;

                        Compte compte = compteService.getCompteOfOrganisation(commission.getOrganisation(), EtiquetteCompteCode.CPTECOMACTEUR);
                        if (compteCommissionSrc != null && compte != null) {
                            BigDecimal montant = commission.getMontant();
                            if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
                                if (compte.getCompteAutre() != null) {
                                    CompteAutre compteAutre = compte.getCompteAutre();
                                    String description = "Paiement de la commission " + commission.getOrganisation().getLibelleUniteOrganisation();
                                    //+ commission.getOperation().getCodeOperation() + " " + commission.getOrganisation().getLibelleUniteOrganisation();
                                    Result result = apiService.virementSimple(compteCommissionSrc, compte, montant, description, commission.getOperation().getCodeOperation(), commission.getOperation());
                                    if (result != null && result.getCodeErr().equals("0")) {
                                        log.info("Comptabilisation des commissions effectuées avec succès.");
                                        //+ commission.getOrganisation().getLibelleUniteOrganisation());
                                        commission.setEstComptabilise(Boolean.TRUE);
                                        commissionRepository.save(commission);
                                    } else {
                                        log.info("Echec de la comptabilisation des commissions.");
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    private String getStringParam(Boolean separateur, String origin, String code, String valeur, String valSeparateur, String separateurComb) {
        if (separateur) {
            origin += code + separateurComb + valeur + valSeparateur;
        } else {
            origin += code + separateurComb + valeur;
        }
        return origin;
    }

    /**
     * Service de comptabilisation de toutes les commissions sur l'omnicanal
     *
     * @param etiquetteCompteDebit
     * @param etiquettecompteCredit
     */
    @Transactional
    public ResultMessage comptabilisationCommissions(EtiquetteCompteCode etiquetteCompteDebit, EtiquetteCompteCode etiquettecompteCredit) throws IOException {
        Optional<TypeUniteOrganisation> typeUniteOrganisation = typeUniteOrganisationRepository.findByCodeTypeUO(String.valueOf(TypeUO.FSE));
        if (typeUniteOrganisation.isPresent()) {
            //Liste de toutes les agences du système
            List<UniteOrganisation> uniteOrganisations = uniteOrganisationService.findByType(typeUniteOrganisation.get().getId());
            for (UniteOrganisation fse : uniteOrganisations) { //Faire le bilan des commissions par agence
                UniteOrganisation uoSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(fse);
                ArrayList<UniteOrganisation> listAll = new ArrayList<>();
                ArrayList<UniteOrganisation> listFils = new ArrayList<>();
                listAll.addAll(uniteOrganisationService.findAllUoFils(fse, listAll, listFils));
                listAll.add(fse);
                List<Commission> commsActeurs = new ArrayList<>();
                for (UniteOrganisation uoFils : listAll) { //Bilan pour chaque fils du reseau de l'agence
                    List<Commission> commissions = commissionRepository.findByEstComptabiliseAndOrganisationId(Boolean.FALSE, uoFils.getId());
                    BigDecimal montant = BigDecimal.ZERO;
                    for (Commission commission : commissions) {
                        montant = montant.add(commission.getMontant());
                        commission.estPaye(Boolean.TRUE);
                        commission.datePaiement(ZonedDateTime.now());
                        commissionRepository.save(commission);
                    }
                    //Bilan des commissions de l'organisation en cours de traitement
                    if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
                        Commission commission = new Commission();
                        commission.organisation(uoFils)
                                .montant(montant);
                        commsActeurs.add(commission);
                    }
                }
                Double montantTotal = commsActeurs.stream().mapToDouble(comm -> comm.getMontant().doubleValue()).sum();
                /*  On debite le compte CPTEATTENTECOMTAXE de l'agence
                    On credite le compte commission  de chaque fils du reseau de l'agence
                 */
                Compte compteDebit = compteService.getCompteOfOrganisation(uoSiege, etiquetteCompteDebit);
                if (compteDebit != null) {
                    return getInfosComptabilisation(commsActeurs, compteDebit, etiquettecompteCredit);
                } else {
                    throw new CustomParameterizedException("Compte invalide!");
                }
            }
        }
        throw new CustomParameterizedException("Compte invalide!");
    }

    /**
     * @param commissions
     * @param compteDebit
     * @param etiquetteCompteCredit
     * @return
     */
    public ResultMessage getInfosComptabilisation(List<Commission> commissions, Compte compteDebit, EtiquetteCompteCode etiquetteCompteCredit) throws IOException {
        if (!commissions.isEmpty()) {
            String structureFormat = "";
            if (compteDebit != null) { //Les informations du compte à debiter
                String valSeparateur = "|";
                String separateurComb = "=";
                CompteBanque compteBanqueOperationActeur = compteDebit.getCompteBanque();
                structureFormat = getStringParam(true, structureFormat, "codeAgenceDebit", compteBanqueOperationActeur.getCodeAgence(), valSeparateur, separateurComb);
                structureFormat = getStringParam(true, structureFormat, "codeBanqueDebit", compteBanqueOperationActeur.getCodeBanque(), valSeparateur, separateurComb);
                structureFormat = getStringParam(true, structureFormat, "nomDonneurOrdre", "ApplicationPti", valSeparateur, separateurComb);
                structureFormat = getStringParam(true, structureFormat, "compteDebiteur", compteDebit.getValeurCompte(), valSeparateur, separateurComb);
                structureFormat += "listeDonneeVirement={";
            }
            int nbr = commissions.size();
            int i = 0;
            String allInfosVirement = "";
            for (Commission commission : commissions) { //Les informations des comptes à crediter
                i++;
                Compte compte = compteService.getCompteOfOrganisation(commission.getOrganisation(), etiquetteCompteCredit);
                if (compteDebit != null && compte != null) {
                    BigDecimal montant = commission.getMontant();
                    if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
                        CompteBanque compteBanque = compte.getCompteBanque();
                        String virement = "";
                        String valSeparateur = "$";
                        String separateurComb = "@";
                        virement = getStringParam(true, virement, "codeAgence", compteBanque.getCodeAgence(), valSeparateur, separateurComb);
                        virement = getStringParam(true, virement, "codeBanque", compteBanque.getCodeBanque(), valSeparateur, separateurComb);
                        virement = getStringParam(true, virement, "montant", String.valueOf(montant.intValue()), valSeparateur, separateurComb);
                        virement = getStringParam(true, virement, "compte", compte.getValeurCompte(), valSeparateur, separateurComb);
                        virement = getStringParam(true, virement, "currency", "952", valSeparateur, separateurComb);
                        virement = getStringParam(true, virement, "beneficiaire", commission.getOrganisation().getLibelleUniteOrganisation(), valSeparateur, separateurComb);
                        virement = getStringParam(false, virement, "motif", "comptabilisation des commissions", valSeparateur, separateurComb);
                        if (i < nbr) {
                            virement += "!";
                        }
                        allInfosVirement += virement;
                    }
                } else {
                    log.info("Compte de commission est introuvable ");
                }
            }
            structureFormat += allInfosVirement + "}";
            return apiService.comptabiliseFraisComEmetteur(structureFormat);
        }
        throw new CustomParameterizedException("%%Compte invalide !");
    }

    /**
     * Get one commission by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public CommissionDTO findOne(Long id) {
        log.info("Request to get Commission : {}", id);
        Commission commission = commissionRepository.findById(id).orElse(null);
        return commissionMapper.toDto(commission);
    }

    /**
     * Delete the commission by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.info("Request to delete Commission : {}", id);
        commissionRepository.deleteById(id);
    }

    public void annulationRepartitionCommission(Long operationId) {
        log.info("Annulation des commissions ");

        List<Commission> commissions = commissionRepository.findByOperationIdAndEstComptabiliseAndEtat(operationId, Boolean.FALSE, EtatValidite.VALIDE);
        commissions.forEach(c -> {
            c.setEtat(EtatValidite.ANNULEE);
            Compte compteSrc = compteService.getCompteOfOrganisationPTI(c.getOrganisation(), EtiquetteCompteCode.CPTECOMACTEURPTI);
            compteService.debiterCompteAutre(compteSrc.getCompteAutre(), c.getMontant());
            transactionComptePtiService.saveTransactionOfCompte(compteSrc.getCompteAutre(), null, c.getMontant(), TypeTransactionCompte.DEBIT, null);
            commissionRepository.save(c);

            // le compte de contrepartie**//
        }
        );
    }

    /**
     * Mise à jour des commissions après comptabilisation sur l'omnicanal
     */
    public void updateAllCommissions() {
        this.commissionRepository.findByEstComptabiliseAndEtat(Boolean.FALSE, EtatValidite.VALIDE).forEach(
                c -> {
                    c.setEstComptabilise(Boolean.TRUE);
                    commissionRepository.save(c);
                });
    }

    /**
     *
     * @param commissions
     * @return
     */
    public List<XCommissionDTO> getAllCommisssion(List<Commission> commissions) {
        List<XCommissionDTO> jasperCommissions = new ArrayList<>();
        for (Commission commission : commissions) {
            XCommissionDTO xItem = new XCommissionDTO();
            xItem.setDateOp(Util.formatDate(commission.getDateEnregistrement()));
            xItem.setMontant(commission.getMontant());
            xItem.setOrganisation(commission.getOrganisation().getLibelleUniteOrganisation());
            xItem.setTypeService(commission.getOperation().getTypeService().getLibelle());
            xItem.setActeur(commission.getActeur() != null ? commission.getActeur().name() : "");
            xItem.setEstPaye(commission.getEstPaye() ? "Oui" : "Non");
            xItem.setEtat(commission.getEtat() != null ? commission.getEtat().name() : "");
            jasperCommissions.add(xItem);
        }
        return jasperCommissions.stream().sorted(Comparator.comparing(XCommissionDTO::getTypeService)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CommissionDTO> getCommissionOfOperation(Long operationId, Pageable pageable) {
        Page<CommissionDTO> commissionDTOS = null;
        commissionDTOS = commissionRepository.findByOperationId(operationId, pageable).map(commissionMapper::toDto);
        return commissionDTOS;
    }

    @Transactional(readOnly = true)
    public InputStream getDatas(Pageable pageable, Long uoId, Boolean estPaye, ZonedDateTime dateEnregistrement) throws FileNotFoundException {
        ZonedDateTime dateTime = null;
        if (dateEnregistrement != null) {
            dateTime = dateEnregistrement.plusHours(24);
        }
        List<Commission> commissions = commissionRepository.findCommissionByMultiCriteria(estPaye, uoId, dateEnregistrement, dateTime, pageable).getContent();
        List<XCommissionDTO> datas = getAllCommisssion(commissions);
        String pathname = System.getProperty("user.home") + File.separator + "commissions" + RandomUtil.generateAlphaNumeric(2) + ".csv";
        log.debug("PATH : " + pathname);
        final File csvFileName = new File(pathname);
        if (fileGeneratorService.generateCSV(csvFileName, datas.toArray())) {
            return new FileInputStream(csvFileName);
        } else {
            throw new CustomParameterizedException("La génération du fichier a échoué");
        }
    }

    public InputStream getDatasOptimise(Pageable pageable, Long uoId, Boolean estPaye, ZonedDateTime dateEnregistrement) throws FileNotFoundException {
        int totalComm;
        int nbrPage;
        List<CommissionDTO> commissions = new ArrayList<>();
        ZonedDateTime dateTime = null;
        if (dateEnregistrement != null) {
            dateTime = dateEnregistrement.plusHours(24);
        }
        totalComm = commissionRepository.countCommissionByMultiCriteria(estPaye, uoId, dateEnregistrement, dateTime);
        nbrPage = (totalComm / 200);
        for (int i = 0; i <= nbrPage; i++) {
            Pageable page = PageRequest.of(i, 200, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
            Long taille = new Long(page.getPageSize());
            Long pag = new Long(page.getPageSize() * page.getPageNumber());
            commissions.addAll(commissionRepository.findCommissionByMultiCriteriaOptimise(estPaye, uoId, dateEnregistrement, dateTime, taille, pag));

        }
        List<XCommissionDTO> datas = getAllCommisssionOptimise(commissions);
        String pathname = System.getProperty("user.home") + File.separator + "commissions" + RandomUtil.generateAlphaNumeric(2) + ".csv";
        log.debug("PATH : " + pathname);
        final File csvFileName = new File(pathname);
        if (fileGeneratorService.generateCSV(csvFileName, datas.toArray())) {
            return new FileInputStream(csvFileName);
        } else {
            throw new CustomParameterizedException("La génération du fichier a échoué");
        }
    }

    public List<XCommissionDTO> getAllCommisssionOptimise(List<CommissionDTO> commissions) {
        List<XCommissionDTO> jasperCommissions = new ArrayList<>();
        for (CommissionDTO commission : commissions) {
            XCommissionDTO xItem = new XCommissionDTO();
            xItem.setDateOp(Util.formatDate(commission.getDateEnregistrement()));
            xItem.setMontant(commission.getMontant());
            xItem.setOrganisation(commission.getOrganisationLibelleUniteOrganisation());
            xItem.setTypeService(commission.getTypeService());
            xItem.setActeur(commission.getActeur() != null ? commission.getActeur().name() : "");
            xItem.setEstPaye(commission.getEstPaye() ? "Oui" : "Non");
            xItem.setEtat(commission.getEtat() != null ? commission.getEtat().name() : "");
            jasperCommissions.add(xItem);
        }
        return jasperCommissions.stream().sorted(Comparator.comparing(XCommissionDTO::getTypeService)).collect(Collectors.toList());
    }

    /**
     * *
     * Repartition des commissions en fonction du type
     *
     * @param acteur
     * @param etiquetteCompteCode
     */
    public void repartitionCommissionInterne(Long operId, Compte compteSource, Compte compteDest, BigDecimal montant) {
        Operation oper = operationRepository.findById(operId).orElse(null);
        if (oper != null) {
            if (compteSource != null && compteDest != null) {
                if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
                    transactionComptePtiService.ecritureCompteExPTI(compteSource, compteDest, montant, oper);

//                    synchronized (compteSource.getCompteAutre()) {
//                        compteService.debiterCompteAutre(compteSource.getCompteAutre(), montant);
//                        transactionComptePtiService.saveTransactionOfComptePTI(compteSource.getCompteAutre(), montant, TypeTransactionCompte.DEBIT, oper);
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                        }
//                        synchronized (compteDest.getCompteAutre()) {
//                            compteService.crediterCompteAutre(compteDest.getCompteAutre(), montant);
//                            transactionComptePtiService.saveTransactionOfComptePTI(compteDest.getCompteAutre(), montant, TypeTransactionCompte.CREDIT, oper);
//                        }
//                    }
                }
            } else {
                log.info("Compte de commission est introuvable ");
            }
        }

    }

    private void miseAjourEncours(List<Commission> commissionsActeurs) {
        for (Commission commission : commissionsActeurs) { //Les informations des comptes à crediter

            //   commission.setEstComptabilise(Boolean.TRUE);
            //commissionRepository.save(commission);
            log.info("Comptabilisation des commissions effectuées avec succès.");
            //+ commission.getOrganisation().getLibelleUniteOrganisation());
            commission.setEnTraitement(Boolean.TRUE);

            commissionRepository.save(commission);

        }
    }

    private void miseAjourCOmmission(List<Commission> commissionsActeurs) {
        for (Commission commission : commissionsActeurs) { //Les informations des comptes à crediter
            //   commission.setEstComptabilise(Boolean.TRUE);
            //commissionRepository.save(commission);
            log.info("Comptabilisation des commissions effectuées avec succès.");
            //+ commission.getOrganisation().getLibelleUniteOrganisation());

            commission.setEstPaye(Boolean.TRUE);
            commission.setEstRegle(Boolean.FALSE);
            commission.setEstComptabilise(Boolean.TRUE);
            commission.setEnTraitement(Boolean.FALSE);
            commissionRepository.save(commission);

        }
    }

    private void miseAjourCOmmission(Commission commission) {

        //   commission.setEstComptabilise(Boolean.TRUE);
        //commissionRepository.save(commission);
        log.info("Comptabilisation des commissions effectuées avec succès.");
        //+ commission.getOrganisation().getLibelleUniteOrganisation());
        commission.setEstPaye(Boolean.TRUE);
        commission.setEstRegle(Boolean.FALSE);
        commission.setEstComptabilise(Boolean.TRUE);
        commission.setEnTraitement(Boolean.FALSE);
        commissionRepository.save(commission);

    }

    public List<Commission> getListCommission(CommissionDTO cm, List<Commission> liste) {

        liste.add(commissionMapper.toEntity(cm));

        return liste;
    }

    public void repartitionDesCommissions() throws IOException {
        List<Commission> commissionsActeurs = commissionRepository.findByEstPayeAndEstComptabiliseAndEtatAndEnTraitement(Boolean.FALSE, Boolean.FALSE, EtatValidite.VALIDE, Boolean.FALSE);
        for (Commission com : commissionsActeurs) {
            OperationCommissionDTO opcommdto = new OperationCommissionDTO();
            opcommdto.setOperationId(com.getOperation().getId());
            opcommdto.setUoId(com.getOrganisation().getId());
            this.messageProducer.sendMessageCommissionParUo(opcommdto);

        }

    }

    /**
     * cette fonction permet de repartir les commissions
     *
     * @param uoId
     * @param montantCommission
     * @param operationId
     * @param commissionId
     * @throws java.io.IOException
     */
    public void comptablisationCommission(Long uoId, BigDecimal montantCommission, Long operationId, Long commissionId) throws IOException {
        Compte compteCommissionSrc = null;
        Compte compteCommissionSrcPti = null;
        Commission comm = commissionRepository.findById(commissionId).orElse(null);
        UniteOrganisation uniteOBeneficiaire = uniteOrganisationService.findOneById(uoId);
        if (uniteOBeneficiaire.getTypeUO().getNiveau() > 1) {
            UniteOrganisation uoFse = uniteOrganisationService.getFSEOfUniteOrganisation(uniteOBeneficiaire);
            UniteOrganisation agencesSiege = uniteOrganisationService.getFseAgenceSiege(uoFse);

            compteCommissionSrc = compteService.getCompteOfOrganisation(agencesSiege, EtiquetteCompteCode.CPTEATTENTECOMM);
            //    compteCommissionSrcPti = compteService.getCompteOfOrganisationPTI(agencesSiege, EtiquetteCompteCode.CPTEATTENTECOMPTI);

            Compte compte = compteService.getCompteOfOrganisation(uniteOBeneficiaire, EtiquetteCompteCode.CPTECOMACTEUR);
            //   Compte compteDestPti = compteService.getCompteOfOrganisationPTI(uniteOBeneficiaire, EtiquetteCompteCode.CPTECOMACTEURPTI);
            String description = "Paiement de la commission " + uniteOBeneficiaire.getLibelleUniteOrganisation();
            if (compteCommissionSrc != null && compte != null) {
                BigDecimal montant = montantCommission;
                log.info(description);
                TransactionComptePti trans = transactionComptePtiService.findByOperationAndCompteAutre(comm.getOperation().getCodeOperation(), compte.getCompteAutre().getId());
                if (trans == null) {
                    Result result = apiService.virementSimple(compteCommissionSrc, compte, montant, description, comm.getOperation().getCodeOperation(),comm.getOperation());
                    if (result != null && result.getCodeErr().equals("0")) {
                        log.info("Comptabilisation des commissions effectuées avec succès." + result.getCodeErr().equals("0"));
                        log.info("Comptabilisation des commissions effectuées avec succès.");
                        //  repartitionCommissionInterne(operationId, compteCommissionSrcPti, compteDestPti, montant);
                        miseAjourCOmmission(comm);
                    } else {
                        log.info("Echec de la comptabilisation des commissions.");
                    }

                } else {
                    miseAjourCOmmission(comm);
                }
            }
        }

    }

    @Transactional(noRollbackFor = CustomParameterizedException.class)
    public void repartitionDesCommissionParUo(Long uoId, Long operationId) throws IOException, InterruptedException {
        OperationDTO oper = operationRepository.findOperationById(operationId);
        String codeReponse = "0";
        String descReponse = "";
        String description = "";
        String identifiant = "";
        boolean apayer = false;
        try {
            if (operationId != null) {
                String allInfosVirement = "";
                Compte compteCommissionSrc = null;
                Compte compteCommissionSrcPti = null;
                List<Commission> commissionsActeurs = null;
                Commission comm = null;
                String structureFormat = "";
                List<Commission> commissions = new ArrayList<>();
                UniteOrganisation uniteO = uniteOrganisationService.findById(uoId);
                if (uniteO.getTypeUO().getNiveau() > 1) {
                    UniteOrganisation uoFse = uniteOrganisationService.getFSEOfUniteOrganisation(uniteO);
                    UniteOrganisation agencesSiege = uniteOrganisationService.getFseAgenceSiege(uoFse);
                    log.info("Traitement en cours ::: " + uniteO.getLibelleUniteOrganisation());
                    //Recherche du compte commission
                    compteCommissionSrc = compteService.getCompteOfOrganisation(agencesSiege, EtiquetteCompteCode.CPTEATTENTECOMM);
                    compteCommissionSrcPti = compteService.getCompteOfOrganisationPTI(agencesSiege, EtiquetteCompteCode.CPTEATTENTECOMPTI);
                    ArrayList<UniteOrganisation> listAll = new ArrayList<>();
                    ArrayList<UniteOrganisation> listPeres = new ArrayList<>();
                    ArrayList<UniteOrganisation> listFils = new ArrayList<>();
                    log.info("Traitement en cours pour l'acteur ::: " + uniteO.getLibelleUniteOrganisation());
                    commissionsActeurs = commissionRepository.findByEstPayeAndEstComptabiliseAndEtatAndEnTraitementAndOrganisationIdAndOperationId(Boolean.FALSE, Boolean.FALSE, EtatValidite.VALIDE, Boolean.FALSE, uniteO.getId(), operationId);
                    if (!commissionsActeurs.isEmpty()) {
                        Commission c = new Commission();
                        c.setOrganisation(uniteO);
                        c.setOperation(commissionsActeurs.get(0).getOperation());
                        c.setMontant(BigDecimal.valueOf(commissionsActeurs.stream().mapToDouble(co -> co.getMontant().doubleValue()).sum()));
                        comm = c;
                        commissions.add(c);
                        miseAjourEncours(commissionsActeurs);
                    }

                    if (compteCommissionSrc != null && compteCommissionSrc.getCompteBanque() != null) {//Les informations du compte à debiter
                        log.info("Debut du traitement ..");
                        String valSeparateur = "|";
                        String separateurComb = "=";
                        CompteBanque compteBanqueOperationActeur = compteCommissionSrc.getCompteBanque();
                        structureFormat = getStringParam(true, structureFormat, "codeAgenceDebit", compteBanqueOperationActeur.getCodeAgence(), valSeparateur, separateurComb);
                        structureFormat = getStringParam(true, structureFormat, "codeBanqueDebit", compteBanqueOperationActeur.getCodeBanque(), valSeparateur, separateurComb);
                        structureFormat = getStringParam(true, structureFormat, "nomDonneurOrdre", "ApplicationPti", valSeparateur, separateurComb);
                        structureFormat = getStringParam(true, structureFormat, "compteDebiteur", compteCommissionSrc.getValeurCompte(), valSeparateur, separateurComb);
                        structureFormat += "listeDonneeVirement={";

                        if (!commissions.isEmpty()) {
                            int nbr = commissions.size();
                            int i = 0;
                            for (Commission commission : commissionsActeurs) { //Les informations des comptes à crediter
                                i++;
                                Compte compte = compteService.getCompteOfOrganisation(commission.getOrganisation(), EtiquetteCompteCode.CPTECOMACTEUR);
                                if (compteCommissionSrc != null && compte != null) {
                                    BigDecimal montant = commission.getMontant();
                                    if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
                                        CompteBanque compteBanque = compte.getCompteBanque();
                                        String virement = "";
                                        String valSeparateur2 = "$";
                                        String separateurComb2 = "@";
                                        virement = getStringParam(true, virement, "codeAgence", compteBanque.getCodeAgence(), valSeparateur2, separateurComb2);
                                        virement = getStringParam(true, virement, "codeBanque", compteBanque.getCodeBanque(), valSeparateur2, separateurComb2);
                                        virement = getStringParam(true, virement, "montant", String.valueOf(montant.intValue()), valSeparateur2, separateurComb2);
                                        virement = getStringParam(true, virement, "compte", compte.getValeurCompte(), valSeparateur2, separateurComb2);
                                        virement = getStringParam(true, virement, "currency", "952", valSeparateur2, separateurComb2);
                                        virement = getStringParam(true, virement, "beneficiaire", commission.getOrganisation().getLibelleUniteOrganisation(), valSeparateur2, separateurComb2);
                                        virement = getStringParam(false, virement, "motif", "comptabilisation des commissions", valSeparateur2, separateurComb2);
                                        if (i < nbr) {
                                            virement += "!";
                                        }
                                        allInfosVirement += virement;
                                    }
                                } else {
                                    log.info("Un Compte de comptabilisation est introuvable ");
                                }
                            }
                            structureFormat += allInfosVirement + "}";
                            log.info("Chaine de caractère de comptabilisation :" + structureFormat);
                            ResultMessage result = apiService.comptabiliseFraisComEmetteur(structureFormat);
                            if (result != null && result.getErrCode() == 0) {
                                log.info("Comptabilisation des commissions effectuées avec succès.");
                                updateAllCommissions();
                            } else {
                                log.info("Echec de la comptabilisation des commissions.");
                            }
                        }
                    } else if (!commissions.isEmpty()) {
                        if (comm != null) {
                            Compte compte = compteService.getCompteOfOrganisation(comm.getOrganisation(), EtiquetteCompteCode.CPTECOMACTEUR);
                            Compte compteDestPti = compteService.getCompteOfOrganisationPTI(comm.getOrganisation(), EtiquetteCompteCode.CPTECOMACTEURPTI);
                            if (compteCommissionSrc != null && compte != null) {
                                BigDecimal montant = comm.getMontant();
                                if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {

                                    if (compte.getCompteAutre() != null) {
                                        CompteAutre compteAutre = compte.getCompteAutre();
                                        identifiant = compte.getCompteAutre().getId().toString();
                                        description = "Paiement de la commission " + comm.getOrganisation().getLibelleUniteOrganisation();

                                        //+ commission.getOperation().getCodeOperation() + " " + commission.getOrganisation().getLibelleUniteOrganisation();
                                        // Result result = apiService.virementSimple(compteCommissionSrc, compte, montant, description);
                                        //if (result != null && result.getCodeErr().equals("0")) {
                                        //+ commission.getOrganisation().getLibelleUniteOrganisation());
                                        int nbr = commissions.size();
                                        int i = 0;
                                        // TransactionComptePti trans = transactionComptePtiService.findByOperationAndCompteAutre(comm.getOperation().getCodeOperation(), compte.getCompteAutre().getId());
                                        descReponse = description;
                                        Long ifExiste = transactionComptePtiService.findExisteOperation(comm.getOperation().getCodeOperation(), compte.getCompteAutre().getId());
                                        if (ifExiste <= 0) {
                                            Result result = apiService.virementSimple(compteCommissionSrc, compte, montant, description, comm.getOperation().getCodeOperation(), comm.getOperation());
                                            if (result != null && result.getCodeErr() != null && result.getCodeErr().equals("0")) {
                                                log.info("Comptabilisation des commissions effectuées avec succès." + result.getCodeErr().equals("0"));
                                                log.info("Comptabilisation des commissions effectuées avec succès.");
                                                /**
                                                 * Si la commission a été
                                                 * effectivement payé *
                                                 */
                                                apayer = true;
                                                miseAjourCOmmission(commissionsActeurs);
                                                try {
                                                    repartitionCommissionInterne(operationId, compteCommissionSrcPti, compteDestPti, montant);

                                                } catch (ChargementException e) {
                                                    Thread.sleep(1000);
                                                    repartitionCommissionInterne(operationId, compteCommissionSrcPti, compteDestPti, montant);
                                                }

                                            } else {
                                                apayer = false;
                                                codeReponse = "-1";
                                            }
                                        } else {
                                            miseAjourCOmmission(commissionsActeurs);
                                            log.info("Commission déjà comptabilisé.");
                                        }

                                        //} else {
                                        //   log.info("Echec de la comptabilisation des commissions.");
                                        // }
                                    } else if (compte.getCompteBanque() != null) {
                                        description = "Paiement de la commission " + comm.getOrganisation().getLibelleUniteOrganisation();
                                        identifiant = compte.getCompteBanque().getId().toString();
                                        descReponse = description;
                                        TransactionComptePti trans = transactionComptePtiService.findByOperationAndCompteAutre(comm.getOperation().getCodeOperation(), compteDestPti.getCompteAutre().getId());
                                        if (trans == null) {
                                            Result result = apiService.virementSimple(compteCommissionSrc, compte, montant, description, comm.getOperation().getCodeOperation(),comm.getOperation());
                                            if (result != null && result.getCodeErr() != null && result.getCodeErr().equals("0")) {
                                                log.info("Comptabilisation des commissions effectuées avec succès." + result.getCodeErr().equals("0"));
                                                  apayer = true;
                                                log.info("Comptabilisation des commissions effectuées avec succès.");
                                                miseAjourCOmmission(commissionsActeurs);
                                                try {
                                                    repartitionCommissionInterne(operationId, compteCommissionSrcPti, compteDestPti, montant);

                                                } catch (ChargementException e) {
                                                    Thread.sleep(1000);
                                                    repartitionCommissionInterne(operationId, compteCommissionSrcPti, compteDestPti, montant);
                                                }

                                            }
                                        }

                                    }

                                }

                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            codeReponse = "-1";
            descReponse = e.getMessage();
        } finally {
            if (apayer) {
                operationAuditEventService.sendOperationEvent(ActionService.ACT_COMPA_COMMISSION, oper.getCodeOperation(), codeReponse, descReponse, identifiant, "COMPTE");
            }

        }
        log.info("Comptabilisation des commission");

    }

    public void repartitionCommissionWithId(Long uoId, Long operationId) throws IOException {
        UniteOrganisation uo = uniteOrganisationService.findById(uoId);
        log.info("Debut de la repartition des commissions de la journée .............");
        ArrayList<Long> list = new ArrayList();
        List<Long> listIdUo = uniteOrganisationService.findAllUoParentsIds(uo, list);
        List<Commission> commissionsActeurs = null;

        String structureFormat = "";
        List<Commission> commissions = new ArrayList<>();
        String allInfosVirement = "";
        for (Long f : listIdUo) {
            OperationCommissionDTO opcommdto = new OperationCommissionDTO();
            opcommdto.setOperationId(operationId);
            opcommdto.setUoId(f);
            this.messageProducer.sendMessageCommissionParUo(opcommdto);

        }
    }

    @Transactional(readOnly = true)
    public Page<CommissionDTO> findAllOptimise(Pageable pageable, Long uoId, Boolean estPaye, ZonedDateTime dateEnregistrement, ZonedDateTime dateFin) {

        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalComm = commissionRepository.countCommissionByMultiCriteria(estPaye, uoId, dateEnregistrement, dateFin);
        System.out.println("==================== TOTAL COMMISSION ====================== " + totalComm);
        Pageable pageable1 = new Pageable() {
            @Override
            public int getPageNumber() {
                return 0;
            }

            @Override
            public int getPageSize() {
                return 0;
            }

            @Override
            public long getOffset() {
                return 0;
            }

            @Override
            public Pageable withPage(int pageNumber) {
                return this;
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public Pageable next() {
                return null;
            }

            @Override
            public Pageable previousOrFirst() {
                return null;
            }

            @Override
            public Pageable first() {
                return null;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }
        };
        if (dateEnregistrement != null && dateFin != null) {
            log.debug("oK");
            dateFin = dateFin.plusHours(23);
        } else if (dateEnregistrement != null) {
            dateFin = dateEnregistrement.plusHours(23);
        } else if (dateFin != null) {
            dateEnregistrement = ZonedDateTime.now().minusMonths(12);
            dateFin = dateFin.plusHours(23);
        }
        List<CommissionDTO> commissionDTOS = commissionRepository.findCommissionByMultiCriteriaOptimise(estPaye, uoId, dateEnregistrement, dateFin, taille, page);
        Page<CommissionDTO> retour = new PageImpl<>(commissionDTOS, pageable1, totalComm);
        return retour;
    }

    public void repartitionCommissionBatchWithId(List<CommissionDTO> listeCommission) throws IOException {
        Job job = (Job) readService.getBatch(listeCommission, new CommissionService.CommissionReaderProcessor());
    }

    public class CommissionReaderProcessor implements RecordProcessor {

        public CommissionReaderProcessor() {

        }

        @Override
        public Record processRecord(Record record) throws Exception {
            CommissionDTO commission = (CommissionDTO) record.getPayload();
            comptablisationCommission(commission.getOrganisationId(), commission.getMontant(), commission.getOperationId(), commission.getId());
            return record;
        }
    }

}
