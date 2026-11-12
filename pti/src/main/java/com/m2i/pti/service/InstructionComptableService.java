package com.m2i.pti.service;

import com.m2i.omnicanal.outils.util.ErrorException;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.mobile.dto.InstructionComptableExecuteDTO;
import com.m2i.pti.mobile.dto.MIntructionComptable;
import com.m2i.pti.repository.InstructionComptableRegistreRepository;
import com.m2i.pti.repository.InstructionComptableRepository;
import com.m2i.pti.service.dto.InstructionComptableDTO;
import com.m2i.pti.service.dto.TransactionComptePtiDTO;
import com.m2i.pti.service.kafka.MessageProducer;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import com.m2i.pti.service.mapper.InstructionComptableMapper;
import com.m2i.pti.service.mapper.TransactionComptePtiMapper;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.ChargementException;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import com.m2i.pti.web.rest.errors.CustomSettlementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.m2i.pti.mobile.dto.InstructionComptableExecuteDTO;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.lang3.SerializationUtils;

/**
 * Service Implementation for managing InstructionComptable.
 */
@Service
@Transactional
public class InstructionComptableService {

    private final Logger log = LoggerFactory.getLogger(InstructionComptableService.class);

    private final InstructionComptableRepository instructionComptableRepository;

    private final InstructionComptableMapper instructionComptableMapper;
    private final TransactionComptePtiMapper transactionComptePtiMapper;
    private final CompteService compteService;
    InstructionComptableRegistreRepository instructionComptableRegistreRepository;
    private final UniteOrganisationService uniteOrganisationService;

    private final TransactionComptePtiService transactionComptePtiService;

    private final RepartitionOpService repartitionOpService;
   private final  MessageProducer messageProducer;
    public InstructionComptableService(MessageProducer messageProducer,InstructionComptableRegistreRepository instructionComptableRegistreRepository, TransactionComptePtiMapper transactionComptePtiMapper, InstructionComptableRepository instructionComptableRepository, InstructionComptableMapper instructionComptableMapper, CompteService compteService, UniteOrganisationService uniteOrganisationService, TransactionComptePtiService transactionComptePtiService, RepartitionOpService repartitionOpService) {
        this.instructionComptableRepository = instructionComptableRepository;
        this.instructionComptableMapper = instructionComptableMapper;
        this.compteService = compteService;
        this.uniteOrganisationService = uniteOrganisationService;
        this.transactionComptePtiService = transactionComptePtiService;
        this.repartitionOpService = repartitionOpService;
        this.transactionComptePtiMapper = transactionComptePtiMapper;
        this.instructionComptableRegistreRepository = instructionComptableRegistreRepository;
         this.messageProducer=messageProducer;
    }

    /**
     * Save a instructionComptable.
     *
     * @param instructionComptableDTO the entity to save
     * @return the persisted entity
     */
    public InstructionComptableDTO save(InstructionComptableDTO instructionComptableDTO) {
        InstructionComptable instructionComptable = instructionComptableMapper.toEntity(instructionComptableDTO);
        instructionComptable = instructionComptableRepository.save(instructionComptable);
        return instructionComptableMapper.toDto(instructionComptable);
    }

    /**
     * Get all the instructionComptables.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<InstructionComptableDTO> findAll(Pageable pageable) {
        log.info("Request to get all InstructionComptables");
        return instructionComptableRepository.findAll(pageable)
                .map(instructionComptableMapper::toDto);
    }

    /**
     * Get all the instructionComptables.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<InstructionComptableDTO> findAllByMultiParams(Pageable pageable, Long typeServiceId, InstructionEcriture instruction, String etiquette, MontantsAComptabiliser montant) {
        return instructionComptableRepository.findInstructionByMultiParams(typeServiceId, instruction, etiquette, montant, pageable).map(instructionComptableMapper::toDto);
    }

    /**
     * Get one instructionComptable by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public InstructionComptableDTO findOne(Long id) {
        log.info("Request to get InstructionComptable : {}", id);
        InstructionComptable instructionComptable = instructionComptableRepository.findById(id).orElse(null);
        return instructionComptableMapper.toDto(instructionComptable);
    }

    /**
     * Delete the instructionComptable by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.info("Request to delete InstructionComptable : {}", id);
        instructionComptableRepository.deleteById(id);
    }

    /**
     * -Comptabilisation d'une operation en fonction du type de service
     * -Concerne uniquement les comptes internes à PTI
     */
    public void comptabiliserOperation(Operation operation, UniteOrganisation organsation, UniteOrganisation orgSrc) {
        log.info("Comptabilisation de l'operation su PTI");
        List<InstructionComptable> instructionComptables = instructionComptableRepository.findByTypeServiceOrderByNumeroOrdre(operation.getTypeService());//
        
        
        instructionComptables.forEach(instructionComptable -> {
            String etiquette = instructionComptable.getEtiquetteComptable();
            Compte comptePTI = getCompteOfComptabilisation(organsation, orgSrc, instructionComptable, etiquette);

            ecritureComptable(operation, instructionComptable, etiquette, comptePTI);

        });
    }

    public void comptabiliserOperationSimple(Operation operation, Compte compteSrc, Compte compteDest, UniteOrganisation uoEm, UniteOrganisation uoAcquereur) {
        log.info("Comptabilisation de l'operation su PTI");

        List<InstructionComptable> instructionComptables = instructionComptableRepository.findByTypeServiceOrderByNumeroOrdre(operation.getTypeService());//
        instructionComptables.forEach(instructionComptable -> {
            Compte comptePTI;
            Compte comptePTIFin;
            String etiquette = instructionComptable.getEtiquetteComptable();

            if (etiquette.equals("CPTESOURCE")) {

                comptePTI = compteService.getCompteOfOrganisationPTI(uoEm, EtiquetteCompteCode.valueOf(compteSrc.getEtiquette()));

            } else {
                comptePTI = compteService.getCompteOfOrganisationPTI(uoAcquereur, EtiquetteCompteCode.valueOf(compteDest.getEtiquette()));
            }

            ecritureComptable(operation, instructionComptable, etiquette, comptePTI);

        });
    }

    private String getDescription(Operation operation) {
        String desc = "";
        String telephone = "";
        String nomOrganisation = operation.getPointVente().getUniteOrganisation().getLibelleUniteOrganisation();
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
        if (operation.getBeneficaire() != null) {
            if (operation.getBeneficaire().getPersonne() != null) {
                if (operation.getBeneficaire().getPersonne().getTelephone() != null) {
                    telephone = operation.getBeneficaire().getPersonne().getTelephone();
                    if (operation.getBeneficaire().getPersonne().getPersonnePhysique() != null) {

                        desc = desc + " vers " + operation.getBeneficaire().getPersonne().getPersonnePhysique().getNomClient() + " " + telephone;
                    } else {
                        desc = desc + " vers " + telephone;
                    }

                }

            }
        }

        return desc;
    }

    private List<MIntructionComptable> getInstruction(List<InstructionComptable> instructionComptables) {
        Integer codeOper = 0;
        Integer compte = 1;
        List<MIntructionComptable> listMIntructionComptable = new ArrayList();;
        List<InstructionComptable> listInt = new ArrayList();

        for (InstructionComptable instructionComptable : instructionComptables) {
            String etiquette = instructionComptable.getEtiquetteComptable();
            listInt.add(instructionComptable);
            if (compte % 2 == 0) {
                listMIntructionComptable.add(new MIntructionComptable(listInt));
                codeOper = codeOper + 1;
                listInt = new ArrayList();
            }

            compte = compte + 1;

        }
        return listMIntructionComptable;
    }

    private Compte returnCompte(Operation operation, InstructionComptable instructionComptable, UniteOrganisation agenceDest, UniteOrganisation orgSrc,
            RegleCommission regleCommission) {
        Compte comptePTI = null;
        String etiquette;
        etiquette = instructionComptable.getEtiquetteComptable();
        if (etiquette.endsWith("PTI")) {
            comptePTI = getCompteOfComptabilisationWithRegle(operation.getTypeService(), regleCommission, agenceDest, orgSrc, instructionComptable, etiquette);
        } else if (etiquette.equals(EtiquetteCompteCode.CPTECLIENT.name())) {
            comptePTI = getCompteOfComptabilisationCompteClient(operation, instructionComptable);
        } else {
            comptePTI = getCompteOfComptabilisationWithRegle(operation.getTypeService(), regleCommission, agenceDest, orgSrc, instructionComptable, etiquette);
        }

        log.debug("COMPTE==================" + comptePTI);
        //  comptePTI = compteService.findCompteOne(comptePTI.getId());
        return comptePTI;
    }

    private void comptabilisation(Compte comptePTI, String etiquette, Operation operation, InstructionComptable instructionComptable, UniteOrganisation agenceDest, UniteOrganisation orgSrc,
            RegleCommission regleCommission) {
        operation.setDescription(getDescription(operation));
        try {
            ecritureComptable(operation, instructionComptable, etiquette, comptePTI);
        } catch (ChargementException ex) {
            TransactionComptePti trans = transactionComptePtiService.findByOperationAndCompteAutre2(operation.getCodeOperation(), comptePTI.getCompteAutre().getId());
            if (trans == null) {
                ecritureComptable(operation, instructionComptable, etiquette, comptePTI);
            }
        }
    }

    public InstructionComptableRegistre enregistreInstructionComptableRegistre(Operation operation, UniteOrganisation agenceDest, UniteOrganisation orgSrc,
            RegleCommission regleCommission) {
        InstructionComptableRegistre instructionComptableRegistre = new InstructionComptableRegistre();
        instructionComptableRegistre.setOperation(operation);
        instructionComptableRegistre.setRegleCommission(regleCommission);
        instructionComptableRegistre.setUoDestAgenceSiege(agenceDest);
        instructionComptableRegistre.setUoSrcAgenceSiege(orgSrc);
        instructionComptableRegistre.setIsConsumed(Boolean.FALSE);
        InstructionComptableRegistre ins = instructionComptableRegistreRepository.save(instructionComptableRegistre);
        InstructionComptableExecuteDTO intcpt=new InstructionComptableExecuteDTO();
        intcpt.setInstructionId(ins.getId());
        messageProducer.sendMessageInstructionComptable(intcpt);
        return ins;
    }
    
    

    public void comptabiliserOperationWithRegle(Operation operation, UniteOrganisation agenceDest, UniteOrganisation orgSrc,
            RegleCommission regleCommission) throws ErrorException {

        try {
            log.info("Comptabilisation de l'operation sur PTI");
            List<InstructionComptable> listinstruction;
            //    List<InstructionComptable> instructionComptablesnew = new ArrayList();
            List<InstructionComptable> instructionComptables = instructionComptableRepository.findByTypeServiceOrderByNumeroOrdre(operation.getTypeService());//
            List<MIntructionComptable> listIns = getInstruction(instructionComptables);
            int i = 0;
            while (i < listIns.size()) {
                listinstruction = listIns.get(i).getListInstructionComptable();
                Compte compteDebit;
                compteDebit = returnCompte(operation, listinstruction.get(0), agenceDest, orgSrc,
                        regleCommission);
                if (compteDebit == null) {
                    break;
                }
                String etiquette = listinstruction.get(0).getEtiquetteComptable();
                synchronized (compteDebit) {
                    comptabilisation(compteDebit, etiquette, operation, listinstruction.get(0), agenceDest, orgSrc, regleCommission);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                    Compte compteCredit = returnCompte(operation, listinstruction.get(1), agenceDest, orgSrc,
                            regleCommission);

                    synchronized (compteCredit) {
                        String etiquetteC = listinstruction.get(1).getEtiquetteComptable();
                        comptabilisation(compteCredit, etiquetteC, operation, listinstruction.get(1), agenceDest, orgSrc, regleCommission);
                    }
                }

                i = i + 1;
            }
        } catch (Exception e) {
            throw new CustomSettlementException(e.getMessage());
        }

    }

    public void comptabiliserOperationWithRegleRRR(Operation operation, UniteOrganisation agenceDest, UniteOrganisation orgSrc,
            RegleCommission regleCommission) {
        log.info("Comptabilisation de l'operation sur PTI");
        List<InstructionComptable> listinstruction;
        List<InstructionComptable> instructionComptables = instructionComptableRepository.findByTypeServiceOrderByNumeroOrdre(operation.getTypeService());//
        List<MIntructionComptable> listIns = getInstruction(instructionComptables);
        int i = 0;
        while (i < listIns.size()) {
            listinstruction = listIns.get(i).getListInstructionComptable();
        }

        instructionComptables.forEach(instructionComptable -> {
            String etiquette;
            etiquette = instructionComptable.getEtiquetteComptable();
            Compte comptePTI = null;
            operation.setDescription(getDescription(operation));
            if (etiquette.endsWith("PTI")) {
                comptePTI = getCompteOfComptabilisationWithRegle(operation.getTypeService(), regleCommission, agenceDest, orgSrc, instructionComptable, etiquette);
            } else if (etiquette.equals(EtiquetteCompteCode.CPTECLIENT.name())) {
                comptePTI = getCompteOfComptabilisationCompteClient(operation, instructionComptable);
            } else {
                comptePTI = getCompteOfComptabilisationWithRegle(operation.getTypeService(), regleCommission, agenceDest, orgSrc, instructionComptable, etiquette);
            }

            ecritureComptable(operation, instructionComptable, etiquette, comptePTI);

        });
    }

    private void ecritureComptable(Operation operation, InstructionComptable instructionComptable, String etiquette, Compte comptePTI) {
        if (comptePTI != null) {
            BigDecimal montant = getMontantComptabilisation(instructionComptable.getMontant(), operation);
            CompteAutre compteAutre = comptePTI.getCompteAutre();
            CompteAutre copyCompte = (CompteAutre) compteAutre.clone(); // on fait une copie du compte 
            BigDecimal montantDebit = BigDecimal.ZERO;
            montantDebit = montant;
            if (instructionComptable.getInstruction().equals(InstructionEcriture.CREDITER)) {
                if (compteAutre != null) {
                    if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
                        if (comptePTI.getEtiquette().endsWith("PTI")) {
                            //montantDebit = montant.multiply(new BigDecimal("-1"));
                            compteService.crediterCompteAutre(comptePTI.getCompteAutre(), montant);
                            transactionComptePtiService.saveTransactionOfComptePTI(compteAutre, montant, TypeTransactionCompte.CREDIT, operation);
                        } else {
                            compteService.crediterCompte(comptePTI, montant);
                            transactionComptePtiService.saveTransactionOfCompte(compteAutre, null, montant, TypeTransactionCompte.CREDIT, operation);
                        }
                    }
                } else {
                    throw new CustomParameterizedException("Le Compte " + etiquette + " est Introuvable ");
                }
            } else if (instructionComptable.getInstruction().equals(InstructionEcriture.DEBITER)) {
                if (compteAutre != null) {
                    if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
                        if (comptePTI.getEtiquette().endsWith("PTI")) {
                            compteService.debiterCompteAutre(comptePTI.getCompteAutre(), montant);
                            transactionComptePtiService.saveTransactionOfComptePTI(compteAutre, montant, TypeTransactionCompte.DEBIT, operation);
                        } else {
                            compteService.debiterCompte(comptePTI, montant);
                            transactionComptePtiService.saveTransactionOfCompte(compteAutre, null, montant, TypeTransactionCompte.DEBIT, operation);
                        }
                    }

                } else {
                    throw new CustomParameterizedException("Le Compte " + etiquette + " est Introuvable " + instructionComptable.getNumeroOrdre() + " " + comptePTI.getUniteOrganisation().getLibelleUniteOrganisation());
                }
            } else {
            }

        } else {
            throw new CustomParameterizedException("Le Compte " + etiquette + " est Introuvable ");
        }
    }

    public void comptabiliserOperation(BigDecimal montantOp, BigDecimal montantFraisAcq, BigDecimal montantFraisEmt, UniteOrganisation organsation, UniteOrganisation orgSrc, TypeService typeService) {
        if (organsation == null) {
            UniteOrganisation uniteOrganisation = uniteOrganisationService.getUoOfUserConnecte();
        }
        List<InstructionComptable> instructionComptables = instructionComptableRepository.findByTypeServiceOrderByNumeroOrdre(typeService);//
        instructionComptables.forEach(instructionComptable -> {
            String etiquette = instructionComptable.getEtiquetteComptable();
            Compte comptePTI = new Compte();
            comptePTI = getCompteOfComptabilisation(organsation, orgSrc, instructionComptable, etiquette);
            BigDecimal montant = BigDecimal.ZERO;
            if (comptePTI != null) {
                switch (instructionComptable.getMontant()) {
                    case FRAIS:
                        montant = montantFraisAcq.add(montantFraisEmt);
                        break;
                    case MONTANT:
                        montant = montantOp;
                        break;
                    case FRAISACQ:
                        montant = montantFraisAcq;
                        break;
                    case FRAISEMT:
                        montant = montantFraisEmt;
                        break;
                }
                CompteAutre compteAutre = comptePTI.getCompteAutre();
                if (instructionComptable.getInstruction().equals(InstructionEcriture.CREDITER)) {
                    compteService.crediterCompteAutre(compteAutre, montant);
                    if (compteAutre != null) {
                        if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
                            transactionComptePtiService.saveTransactionOfCompte(compteAutre, null, montant, TypeTransactionCompte.CREDIT, null);
                        }
                    } else {
                        throw new CustomParameterizedException("Le Compte " + etiquette + " est Introuvable ");
                    }
                } else if (instructionComptable.getInstruction().equals(InstructionEcriture.DEBITER)) {
                    compteService.debiterCompteAutre(compteAutre, montant);
                    if (compteAutre != null) {
                        if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
                            transactionComptePtiService.saveTransactionOfCompte(compteAutre, null, montant, TypeTransactionCompte.DEBIT, null);
                        }
                    } else {
                        throw new CustomSettlementException("Le Compte " + etiquette + " est Introuvable ");
                    }
                } else {
                }
            }
        });
    }

    private Compte getCompteOfComptabilisationCompteClient(Operation operation, InstructionComptable instructionComptable) {
        Compte comptePTI;

        if (instructionComptable.getInstructionUo().equals(InstructionUo.UO_SOURCE)) {

            comptePTI = operation.getCompteSrc();
        } else {

            comptePTI = operation.getCompteDest();
        }

        return comptePTI;

    }

    /**
     * @param typeService
     * @param regleCommission
     * @param agenceDest
     * @param instructionComptable
     * @param etiquette
     * @return
     */
    private Compte getCompteOfComptabilisationWithRegle(TypeService typeService, RegleCommission regleCommission, UniteOrganisation agenceDest, UniteOrganisation orgSrc, InstructionComptable instructionComptable, String etiquette) {
        Compte comptePTI;
        if (regleCommission != null && etiquette.equals(String.valueOf(EtiquetteCompteCode.CPTESRCFRAISPTI))) {
            return verifRegleCommissionInPti(regleCommission);
        } else {
            //Les frais sont à la charge de l'acteur
            if (etiquette.equals(String.valueOf(EtiquetteCompteCode.CPTESRCFRAISPTI))) {
                if (typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.CASH_ACCOUNT))
                        || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.ENVOI_CASH)) || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.TRANSFERT_MASSE_CASH)) || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.PAIEMENT_FACTURE_DEPUIS_CASH))) {
                    etiquette = String.valueOf(EtiquetteCompteCode.CPTEOPERATIONACTEURPTI);
                } else if (typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.ACCOUNT_ACCOUNT))
                        || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.ACCOUNT_CASH)) || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.RETRAIT_COMPTE)) || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.PAIEMENT_FACTURE)) || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.PAIEMENT_BIEN)) || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.BANK_TO_WALLET)) || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.WALLET_TO_BANK))) {
                    etiquette = String.valueOf(EtiquetteCompteCode.CPTECLIENTPTI);
                }
            }
            if (instructionComptable.getInstructionUo().equals(InstructionUo.UO_SOURCE)) {
                comptePTI = compteService.getCompteOfOrganisationPTI(orgSrc, EtiquetteCompteCode.valueOf(etiquette));
            } else//UO_DEST
            {
                if (agenceDest != null) {
                    comptePTI = compteService.getCompteOfOrganisationPTI(agenceDest, EtiquetteCompteCode.valueOf(etiquette));
                } else {
                    throw new CustomSettlementException("Compte de comptabilisation est introuvable.");
                }
            }
            return comptePTI;
        }
    }

    private Compte getCompteOfComptabilisation(UniteOrganisation agenceDest, UniteOrganisation orgSrc, InstructionComptable instructionComptable, String etiquette) {
        Compte comptePTI;
        if (instructionComptable.getInstructionUo().equals(InstructionUo.UO_SOURCE)) {
            comptePTI = compteService.getCompteOfOrganisationPTI(orgSrc, EtiquetteCompteCode.valueOf(etiquette));
        } else//UO_DEST
        {
            if (agenceDest != null) {
                comptePTI = compteService.getCompteOfOrganisationPTI(agenceDest, EtiquetteCompteCode.valueOf(etiquette));
            } else {
                throw new CustomParameterizedException("Compte introuvable");
            }
        }
        return comptePTI;
    }

    /**
     * @param regleCommission
     * @return
     */
    private Compte verifRegleCommissionInPti(RegleCommission regleCommission) {
        if (regleCommission != null) {
            String etiquette = regleCommission.getEtiquette().endsWith("PTI") ? regleCommission.getEtiquette() : regleCommission.getEtiquette() + "PTI";
            UniteOrganisation priseEnCharge = regleCommission.getPriseEnCharge();
            if (priseEnCharge != null) {
                UniteOrganisation agence = uniteOrganisationService.getUoAgenceSiegeWithUoFse(priseEnCharge);
                Compte comptePTI = compteService.getCompteOfOrganisationPTI(agence, EtiquetteCompteCode.valueOf(etiquette));
                if (comptePTI != null) {
                    return comptePTI;
                } else {
                    throw new CustomParameterizedException("Le compte " + regleCommission.getEtiquette() + " de l' agence est introuvable!");
                }
            }
        }
        return null;
    }

    /*
        Calcul du montant à comptabiliser pour chaque instruction comptable
     */
    public BigDecimal getMontantComptabilisation(MontantsAComptabiliser montantsAComptabiliser, Operation operation) {
        switch (montantsAComptabiliser) {
            case FRAIS:
                return Util.getZeroIfNull(operation.getMontantFrais()).setScale(2, RoundingMode.HALF_UP);
            case MONTANT:
                // return Util.getZeroIfNull(operation.getMontantOp()).setScale(2, RoundingMode.HALF_UP);
                return Util.getZeroIfNull(operation.getMontantFrais()).add(Util.getZeroIfNull(operation.getMontantTaxe())).add(Util.getZeroIfNull(operation.getMontantOp())).setScale(2, RoundingMode.HALF_UP);
            case MONTANTHT:
                return Util.getZeroIfNull(operation.getMontantOp()).setScale(2, RoundingMode.HALF_UP);
            case MONTANTCOMM:
                return Util.getZeroIfNull(operation.getMontantFrais()).setScale(2, RoundingMode.HALF_UP);
            case MONTANTTAXE:
                return Util.getZeroIfNull(operation.getMontantTaxe()).setScale(2, RoundingMode.HALF_UP);
            case MONTANTTVA:
                return Util.getZeroIfNull(operation.getMontantTaxe()).setScale(2, RoundingMode.HALF_UP);
            case MONTANTHTCOMACQ:
                BigDecimal b = Util.getZeroIfNull(repartitionOpService.getMontantByActeurAndOp(Acteur.ACQ, operation.getId()));
                return Util.getZeroIfNull(operation.getMontantOp().add(b)).setScale(2, RoundingMode.HALF_UP);
            case MONTANTCOMACQ:
                return Util.getZeroIfNull(repartitionOpService.getMontantByActeurAndOp(Acteur.ACQ, operation.getId())).setScale(2, RoundingMode.HALF_UP);
            case FRAISACQ:
                return Util.getZeroIfNull(repartitionOpService.getMontantByActeurAndOp(Acteur.ACQ, operation.getId())).setScale(2, RoundingMode.HALF_UP);
            case FRAISEMT:
                return Util.getZeroIfNull(repartitionOpService.getMontantByActeurAndOp(Acteur.EM, operation.getId())).setScale(2, RoundingMode.HALF_UP);
            case MONTANTCOMEMT:
                return Util.getZeroIfNull(repartitionOpService.getMontantByActeurAndOp(Acteur.EM, operation.getId())).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    /**
     * cette fonction permet d'executer la comptabilisation d'une reversion
     *
     * @param operationId
     */
    /**
     * cette fonction permet d'executer la comptabilisation d'une reversion
     *
     * @param operationId
     * @return
     */
    public void reversionOperation(Long operationId) {
        if (operationId != null) {
            List<TransactionComptePti> listtrans = transactionComptePtiService.findByOperationIdAnnuler(operationId);
            if (listtrans != null && listtrans.size() > 0) {
                listtrans.stream().forEach(trans -> {
                    TransactionComptePti transactionComptePti = trans;
                    Operation op = transactionComptePti.getOperation();
                    BigDecimal montant = trans.getMontant();
                    Compte cpt = compteService.findByCompteAutreId(trans.getCompteAutre().getId());
                    Compte cptDest = compteService.findByCompteAutreId(trans.getCompteAutreOp().getId());
                    CompteAutre compteAutreDest = null;
                    if (cptDest != null) {
                        compteAutreDest = cptDest.getCompteAutre();
                    }
                    CompteAutre copyCompte = (CompteAutre) cpt.getCompteAutre().clone(); // on fait une copie du compte 

                    CompteAutre copyCompteAutre = (CompteAutre) cpt.getCompteAutre().clone(); // on fait une copie du compte 
                    //    CompteAutre copyCompte = cpt.getCompteAutre(); // on fait une copie du compte 
                    if (transactionComptePti.getType().equals(TypeTransactionCompte.DEBIT)) {
                        if (cpt.getEtiquette().endsWith("PTI")) {
                            compteService.crediterCompteAutre(cpt.getCompteAutre(), trans.getMontant());
                            transactionComptePtiService.saveTransactionOfComptePTI(copyCompte, montant, TypeTransactionCompte.CREDIT, op);
                        } else {
                            compteService.crediterCompte(cpt, trans.getMontant());
                            transactionComptePtiService.saveTransactionOfCompte(copyCompte, compteAutreDest, montant, TypeTransactionCompte.CREDIT, op);
                        }
                    } else if (transactionComptePti.getType().equals(TypeTransactionCompte.CREDIT)) {
                        if (cpt.getEtiquette().endsWith("PTI")) {
                            compteService.debiterCompteAutre(cpt.getCompteAutre(), trans.getMontant());
                            transactionComptePtiService.saveTransactionOfComptePTI(copyCompte, montant, TypeTransactionCompte.DEBIT, op);
                        } else {
                            compteService.debiterCompte(cpt, trans.getMontant());
                            transactionComptePtiService.saveTransactionOfCompte(copyCompte, compteAutreDest, montant, TypeTransactionCompte.DEBIT, op);
                        }
                    }

                });
            }

        }

    }

}
