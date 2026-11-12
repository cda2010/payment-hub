package com.m2i.pti.service;

import com.m2i.omnicanal.outils.util.ErrorException;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.ActionService;
import com.m2i.pti.domain.enumeration.CategorieCompte;
import com.m2i.pti.domain.enumeration.EtatCompte;
import com.m2i.pti.domain.enumeration.EtatValidite;
import com.m2i.pti.domain.enumeration.StatutCompte;
import com.m2i.pti.domain.enumeration.TypeTransactionCompte;
import com.m2i.pti.repository.*;
import com.m2i.pti.service.dto.CompteDTO;
import com.m2i.pti.service.dto.TransactionComptePtiDTO;
import com.m2i.pti.service.mapper.TransactionComptePtiMapper;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.m2i.pti.domain.enumeration.InstructionEcriture;
import com.m2i.pti.web.rest.errors.CustomSettlementException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Service Implementation for managing TransactionComptePti.
 */
@Service
@Transactional
public class TransactionComptePtiService {

    private final Logger log = LoggerFactory.getLogger(TransactionComptePtiService.class);

    private final TransactionComptePtiRepository transactionComptePtiRepository;

    private final TransactionComptePtiMapper transactionComptePtiMapper;

    private final CompteAutreRepository compteAutreRepository;

    private final EtiquetteCompteRepository etiquetteCompteRepository;

    private UniteOrganisationRepository uniteOrganisationRepository;
    private final CompteRepository compteRepository;
    private Page<TransactionComptePtiDTO> map;
    private final CompteService compteService;
    private final ClientRepository clientRepository;
    private final TransactionTemplate transactionTemplate;
    CompteEventService compteEventService;
    OperationAuditEventService operationAuditEventService;

    public TransactionComptePtiService(OperationAuditEventService operationAuditEventService, CompteEventService compteEventService, TransactionTemplate transactionTemplate, TransactionComptePtiRepository transactionComptePtiRepository, TransactionComptePtiMapper transactionComptePtiMapper, CompteAutreRepository compteAutreRepository, EtiquetteCompteRepository etiquetteCompteRepository, UniteOrganisationRepository uniteOrganisationRepository, CompteRepository compteRepository, CompteService compteService, ClientRepository clientRepository) {
        this.transactionComptePtiRepository = transactionComptePtiRepository;
        this.transactionComptePtiMapper = transactionComptePtiMapper;
        this.compteAutreRepository = compteAutreRepository;
        this.etiquetteCompteRepository = etiquetteCompteRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.compteRepository = compteRepository;
        this.compteService = compteService;
        this.clientRepository = clientRepository;
        this.transactionTemplate = transactionTemplate;
        this.compteEventService = compteEventService;
        this.operationAuditEventService = operationAuditEventService;
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        this.transactionTemplate.setTimeout(30); // 30 seco
    }

    /**
     * Save a transactionComptePti.
     *
     * @param transactionComptePtiDTO the entity to save
     * @return the persisted entity
     */
    public TransactionComptePtiDTO save(TransactionComptePtiDTO transactionComptePtiDTO) {
        log.debug("Request to save TransactionComptePti : {}", transactionComptePtiDTO);
        TransactionComptePti transactionComptePti = transactionComptePtiMapper.toEntity(transactionComptePtiDTO);
        transactionComptePti = transactionComptePtiRepository.save(transactionComptePti);
        return transactionComptePtiMapper.toDto(transactionComptePti);
    }

    /**
     * Enregistrant des traces de debit ou de credit d'un compte
     *
     * @param compteAutre
     * @param montant
     * @param typeTransactionCompte
     * @param operation
     * @return
     */
    public TransactionComptePti saveTransactionOfCompte(CompteAutre compteAutre, CompteAutre compteAutreOp, BigDecimal montant, TypeTransactionCompte typeTransactionCompte, Operation operation) {
        log.info("DEBUT ENREGISTREMENT DE LA TRANSACTION" + operation.getCodeExterne());
        try {
            if (compteAutre != null) {
                System.out.println(" saved transaction  ok--------------");
                TransactionComptePti transactionComptePti = new TransactionComptePti();
                transactionComptePti.setCompteAutreOp(compteAutreOp);
                transactionComptePti.setCompteAutre(compteAutre);
                if (typeTransactionCompte.equals(TypeTransactionCompte.DEBIT)) {
                    transactionComptePti = transactionComptePti.withdraw(montant);
                    // transactionComptePti.soldeApres(compteAutre.getSolde().subtract(montant.abs()));
                } else {
                    transactionComptePti = transactionComptePti.deposit(montant);
                    // transactionComptePti.soldeCourant(compteAutre.getSolde().subtract(montant));
                    // transactionComptePti.soldeApres(compteAutre.getSolde().add(montant.abs()));
                }
                transactionComptePti
                        .date(ZonedDateTime.now())
                        .montant(montant)
                        .etat(EtatValidite.VALIDE)
                        .type(typeTransactionCompte)
                        .operation(operation);
                //  transactionComptePti.setSoldeCourant(compteAutre.getSolde());
                transactionComptePtiRepository.save(transactionComptePti);
                log.info("FIN ENREGISTREMENT DE LA TRANSACTION" + operation.getCodeExterne());
                return transactionComptePti;
            } else {
                log.info("FIN ENREGISTREMENT DE LA TRANSACTION AVEC ERREUR" + operation.getCodeExterne());
                throw new CustomParameterizedException("Le compte n'est pas un compte PTI");
            }
        } catch (Exception e) {
            log.info("FIN ENREGISTREMENT DE LA TRANSACTION AVEC ERREUR" + operation.getCodeExterne() + " " + e.getMessage());
            throw new CustomParameterizedException(e.getMessage());
        }

    }

    public TransactionComptePti saveTransactionOfComptePTI(CompteAutre compteAutre, BigDecimal montant, TypeTransactionCompte typeTransactionCompte, Operation operation) {
        if (compteAutre != null) {
            TransactionComptePti transactionComptePti = new TransactionComptePti();
            transactionComptePti.setCompteAutre(compteAutre);
            BigDecimal moinsUn = new BigDecimal("-1");
            if (typeTransactionCompte.equals(TypeTransactionCompte.DEBIT)) {
                transactionComptePti = transactionComptePti.withdraw(montant.multiply(moinsUn));

            } else {
                // transactionComptePti.soldeCourant(compteAutre.getSolde().subtract(montant));
                transactionComptePti = transactionComptePti.deposit(montant.multiply(moinsUn));
            }
            transactionComptePti
                    .date(ZonedDateTime.now())
                    .montant(montant)
                    .etat(EtatValidite.VALIDE)
                    .type(typeTransactionCompte)
                    .operation(operation);
            //    transactionComptePti.setSoldeCourant(compteAutre.getSolde());
            transactionComptePtiRepository.save(transactionComptePti);

            return transactionComptePti;
        } else {
            throw new CustomParameterizedException("Le compte n'est pas un compte PTI");
        }
    }

    public int ecritureCompteExPTI(Compte compteSrc, Compte compteDest, BigDecimal montant, Operation oper) {
        return (int) transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                CompteAutre ca = null;
                ecritureComptePTI(compteSrc, compteDest, montant, oper);
                return 1;
            }
        });
    }

    public int ecritureComptex(Compte compteSrc, Compte compteDest, BigDecimal montant, Operation oper) {
        return (int) transactionTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                CompteAutre ca = null;
                try {
                    ecritureCompte(compteSrc, compteDest, montant, oper);
                } catch (Exception e) {
                    log.info("ERREUR DE LA COMPTABILISATION " + " " + oper.getCodeOperation() + " " + e.getMessage());
                    throw new CustomSettlementException(e.getMessage());
                }

                return 1;
            }
        });
    }

    public void ecritureCompte(Compte compteSrc, Compte compteDest, BigDecimal montant, Operation oper) throws CustomParameterizedException, InterruptedException {
        boolean status = true;
        log.info("DEBUT ECRITURE COMPTABILISATION " + oper.getCodeExterne());
        CompteAutre cptDebit = null;
        CompteAutre cptCredit = null;
        boolean apayer = false;
        String codeReponse = "0";
        String descReponse = "";
        try {
            if (compteSrc != null && compteDest != null && !compteSrc.equals(compteDest) && compteSrc.getCompteAutre() != null && compteDest.getCompteAutre() != null) {
                if (compteService.peutDebiterCompte(compteSrc, montant)) {
                    log.info("DEBUT ECRITURE DEBIT " + oper.getCodeExterne());
                    cptDebit = compteService.debiterCompteT(compteSrc, montant);
                    log.info("FIN ECRITURE DEBIT " + oper.getCodeExterne());
                    if (cptDebit != null) {
                        TransactionComptePti transptiDebit = this.saveTransactionOfCompte(cptDebit, compteDest.getCompteAutre(), montant, TypeTransactionCompte.DEBIT, oper);
                        compteEventService.sendCompteEvent(compteSrc, InstructionEcriture.DEBITER.name(), montant, oper);
                        log.info("DEBUT ECRITURE CREDIT " + oper.getCodeExterne());
                        cptCredit = compteService.crediterCompteT(compteDest, montant);
                        log.info("FIN ECRITURE CREDIT " + oper.getCodeExterne());
                        if (cptCredit != null) {
                            TransactionComptePti transptiCredit = this.saveTransactionOfCompte(cptCredit, cptDebit, montant, TypeTransactionCompte.CREDIT, oper);
                            compteEventService.sendCompteEvent(compteDest, InstructionEcriture.CREDITER.name(), montant, oper);
                        } else {
                            log.info("ERREUR FIN ECRITURE CREDIT " + oper.getCodeExterne());
                            throw new CustomParameterizedException("Erreur lors du virement!");
                        }

                    } else {
                        log.info("ERREUR FIN ECRITURE DEBIT " + oper.getCodeExterne());
                        throw new CustomParameterizedException("Erreur lors du virement!");
                    }

                    //   Compte src=   compteService.findCompteOne(compteSrc.getId());
                    //   Compte dest=   compteService.findCompteOne(compteDest.getId());
                } else {
                    throw new CustomParameterizedException("Solde du compte source insuffisant!");
                }
            } else {
                throw new CustomParameterizedException("L'un des compte est invalide!");
            }
        } catch (CustomParameterizedException e) {
            throw new CustomParameterizedException(e.getMessage());
        } finally {
          
        }
        // TransactionComptePti transp = transactionComptePtiRepository.findByOperationAndCompteAutre(oper.getCodeExterne(), compteDest.getCompteAutre().getId());
        //  if (transp == null) {

        log.info("FIN ECRITURE COMPTABILISATION " + oper.getCodeExterne());
        //  } else {
        //    throw new CustomParameterizedException("Attention cette transaction a été déjà executée");
        // }

    }

    public void ecritureComptePTI(Compte compteSrc, Compte compteDest, BigDecimal montant, Operation oper) {
        boolean status = true;
        if (compteSrc != null && compteDest != null && !compteSrc.equals(compteDest) && compteSrc.getCompteAutre() != null && compteDest.getCompteAutre() != null) {

            CompteAutre cptDebit = compteService.debiterCompteAutrePTI(compteSrc.getCompteAutre(), montant);

            if (cptDebit != null) {
                TransactionComptePti transptiDebit = this.saveTransactionOfComptePTI(cptDebit, montant, TypeTransactionCompte.DEBIT, oper);
                CompteAutre cptCredit = compteService.crediterCompteAutrePTI(compteDest.getCompteAutre(), montant);
                if (cptCredit != null) {
                    TransactionComptePti transptiCredit = this.saveTransactionOfComptePTI(cptCredit, montant, TypeTransactionCompte.CREDIT, oper);

                } else {
                    throw new CustomParameterizedException("Erreur lors du virement!");
                }

            }

            //   Compte src=   compteService.findCompteOne(compteSrc.getId());
            //   Compte dest=   compteService.findCompteOne(compteDest.getId());
        } else {
            throw new CustomParameterizedException("L'un des compte est invalide!");
        }
    }

    /**
     * Get all the transactionComptePtis.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TransactionComptePtiDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TransactionComptePtis");
        return transactionComptePtiRepository.findAll(pageable)
                .map(transactionComptePtiMapper::toDto);
    }

    /**
     * Recherche des transactions en fonction de plusieurs critères
     *
     * @param compteAutreId
     * @param dateDebut
     * @param dateFin
     * @param pageable
     * @return
     */
    @Transactional(readOnly = true)
    public Page<TransactionComptePtiDTO> findByCompteAutre(Long compteAutreId, ZonedDateTime dateDebut, ZonedDateTime dateFin, Pageable pageable) {
        log.debug("Request to get some TransactionComptePtis");
        CompteAutre compteAutre = new CompteAutre();
        if (compteAutreId != null) {
            Compte compte = compteRepository.findByCompteAutreId(compteAutreId);
            if (compte != null && compte.getComptePincipale() != null) {
                if (compte.getCompteAutre() != null) {
                    compteAutreId = compte.getComptePincipale().getCompteAutre().getId();
                }
            }
        }
        if (dateDebut != null && dateFin != null) {
            return transactionComptePtiRepository.findByCompteAutreAndDateBetweenOrderByIdDesc(compteAutreRepository.findById(compteAutreId).orElse(null), dateDebut, dateFin, pageable)
                    .map(transactionComptePtiMapper::toDto);
        } else if (dateDebut != null) {
            return transactionComptePtiRepository.findByCompteAutreAndDateBetweenOrderByIdDesc(compteAutreRepository.findById(compteAutreId).orElse(null), dateDebut, dateDebut.plusHours(23), pageable)
                    .map(transactionComptePtiMapper::toDto);
        }
        return transactionComptePtiRepository.findByCompteAutreOrderByIdDesc(compteAutreRepository.findById(compteAutreId).orElse(null), pageable)
                .map(transactionComptePtiMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TransactionComptePtiDTO> findByCompteAutreOptimise(Long compteAutreId, ZonedDateTime dateDebut, ZonedDateTime dateFin, String jhiType, Pageable pageable) {
        log.debug("Request to get some TransactionComptePtis");
        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalTrs;
        List<TransactionComptePtiDTO> trs;
        TypeTransactionCompte typetransaction = null;
        log.debug("-------------jhiType" + jhiType);
        if (jhiType != null && !jhiType.equals("")) {
            typetransaction = TypeTransactionCompte.valueOf(jhiType);
        } else {
            jhiType = null;
        }

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
        if (compteAutreId != null) {
            CompteDTO compte = compteRepository.findByCompteAutreIdOptimise(compteAutreId);
            if (compte != null && compte.getComptePincipaleId() != null) {
                if (compte.getCompteAutreId() != null) {
                    compteAutreId = compteRepository.getCompteAutreIdByComptePrincipaleId(compte.getComptePincipaleId());
                }
            }
        }
        if (dateDebut != null && dateFin != null) {
            totalTrs = transactionComptePtiRepository.countByCompteAutreAndDateBetweenOrderByIdDesc(compteAutreId, dateDebut, dateFin.plusHours(23).plusMinutes(59), typetransaction);
            log.debug("total" + totalTrs);
            trs = transactionComptePtiRepository.findByCompteAutreAndDateBetweenOrderByIdDescOptimise(compteAutreId, dateDebut, dateFin.plusHours(23).plusMinutes(59), jhiType, taille, page);
            log.debug("trs" + trs.size());
        } else if (dateDebut != null) {
            totalTrs = transactionComptePtiRepository.countByCompteAutreAndDateBetweenOrderByIdDesc(compteAutreId, dateDebut, dateDebut.plusHours(23).plusMinutes(59), typetransaction);
            trs = transactionComptePtiRepository.findByCompteAutreAndDateBetweenOrderByIdDescOptimise(compteAutreId, dateDebut, dateDebut.plusHours(23).plusMinutes(59), jhiType, taille, page);
        } else {
            totalTrs = transactionComptePtiRepository.countByCompteAutreAndDateBetweenOrderByIdDesc(compteAutreId, null, null, typetransaction);
            trs = transactionComptePtiRepository.findByCompteAutreAndDateBetweenOrderByIdDescOptimise(compteAutreId, null, null, jhiType, taille, page);
        }
        Page<TransactionComptePtiDTO> retour = new PageImpl<>(trs, pageable1, totalTrs);
        return retour;
    }

    @Transactional(readOnly = true)
    public List<TransactionComptePtiDTO> findListByCompteAutre(Long compteAutreId, ZonedDateTime dateDebut, ZonedDateTime dateFin) {
        log.debug("Request to get some List TransactionComptePtis");
        CompteAutre compteAutre = new CompteAutre();
        if (compteAutreId != null) {
            Compte compte = compteRepository.findByCompteAutreId(compteAutreId);
            if (compte.getComptePincipale() != null) {
                if (compte.getCompteAutre() != null) {
                    compteAutreId = compte.getComptePincipale().getCompteAutre().getId();
                }
            }
        }
        if (dateDebut != null && dateFin != null) {
            List<TransactionComptePti> list = transactionComptePtiRepository.findByCompteAutreAndDateBetweenOrderByIdDesc(compteAutreRepository.findById(compteAutreId).orElse(null), dateDebut, dateFin.plusHours(23));
            List<TransactionComptePtiDTO> retour = new ArrayList<TransactionComptePtiDTO>();
            for (TransactionComptePti item : list) {
                retour.add(transactionComptePtiMapper.toDto(item));
            }
            return retour;
        } else if (dateDebut != null) {
            List<TransactionComptePti> list = transactionComptePtiRepository.findByCompteAutreAndDateBetweenOrderByIdDesc(compteAutreRepository.findById(compteAutreId).orElse(null), dateDebut, dateDebut.plusHours(23));
            List<TransactionComptePtiDTO> retour = new ArrayList<TransactionComptePtiDTO>();
            for (TransactionComptePti item : list) {
                retour.add(transactionComptePtiMapper.toDto(item));
            }
            return retour;
        }
        List<TransactionComptePti> list = transactionComptePtiRepository.findByCompteAutreOrderByIdDesc(compteAutreRepository.findById(compteAutreId).orElse(null));
        List<TransactionComptePtiDTO> retour = new ArrayList<TransactionComptePtiDTO>();
        for (TransactionComptePti item : list) {
            retour.add(transactionComptePtiMapper.toDto(item));
        }
        return retour;
    }

    @Transactional(readOnly = true)
    public List<TransactionComptePti> findByCompteAutre(Long compteAutreId, ZonedDateTime dateDebut, ZonedDateTime dateFin) {
        log.debug("Request to get some TransactionComptePtis");
        CompteAutre compteAutre = new CompteAutre();

        if (dateDebut != null && dateFin != null) {
            return transactionComptePtiRepository.findTop200ByCompteAutreAndDateBetweenOrderByIdDesc(compteAutreRepository.findById(compteAutreId).orElse(null), dateDebut, dateFin.plusHours(23));
        } else if (dateDebut != null) {
            return transactionComptePtiRepository.findTop200ByCompteAutreAndDateBetweenOrderByIdDesc(compteAutreRepository.findById(compteAutreId).orElse(null), dateDebut, dateDebut.plusHours(23));

        }

        return null;
    }

    @Transactional(readOnly = true)
    public List<TransactionComptePtiDTO> findByCompteAutreOptimiser(Long compteAutreId, ZonedDateTime dateDebut, ZonedDateTime dateFin) {
        log.debug("Request to get some TransactionComptePtis---");
        CompteAutre compteAutre = new CompteAutre();
        compteAutre = compteAutreRepository.findById(compteAutreId).orElse(null);
        if (dateDebut != null && dateFin != null) {
            return transactionComptePtiRepository.findTop200ByCompteAutreAndDateBetweenOrderByIdDescOptimiser(compteAutre.getId(), dateDebut, dateFin.plusHours(23));
        } else if (dateDebut != null) {
            return transactionComptePtiRepository.findTop200ByCompteAutreAndDateBetweenOrderByIdDescOptimiser(compteAutre.getId(), dateDebut, dateDebut.plusHours(23));

        }

        return null;
    }

    public List<TransactionComptePti> findByCompteAutrePointVente(Long compteAutreId, ZonedDateTime dateDebut, ZonedDateTime dateFin, Long pointVenteId) {
        log.debug("Request to get some TransactionComptePtis");
        CompteAutre compteAutre = new CompteAutre();

        if (dateDebut != null && dateFin != null) {
            return transactionComptePtiRepository.findByCompteAutreAndDateBetweenAndOperationPointVenteIdOrderByIdDesc(compteAutreRepository.findById(compteAutreId).orElse(null), dateDebut, dateFin.plusHours(23), pointVenteId);
        } else if (dateDebut != null) {
            return transactionComptePtiRepository.findByCompteAutreAndDateBetweenAndOperationPointVenteIdOrderByIdDesc(compteAutreRepository.findById(compteAutreId).orElse(null), dateDebut, dateDebut.plusHours(23), pointVenteId);

        }
        return null;
    }

    public List<TransactionComptePtiDTO> findByCompteAutrePointVenteMiniReleve(Long compteAutreId, Long pointVenteId, Integer nombre) {
        log.debug("Request to get some TransactionComptePtis");
        CompteAutre compteAutre = new CompteAutre();
        Long tail = (long) 5;
        compteAutre = compteAutreRepository.findById(compteAutreId).orElse(null);
        if (pointVenteId != null) {
            return transactionComptePtiRepository.findByCompteAutreAndDateBetweenAndOperationPointVenteIdLimitOrderByIdDescOptimise(compteAutre.getId(), null, null, pointVenteId, 3L);
        }
        return transactionComptePtiRepository.findByCompteAutreAndDateBetweenOrderByIdDescOptimise(compteAutre.getId(), null, null, null, (long) 3, (long) 1);
    }

    public List<TransactionComptePtiDTO> findByCompteAutrePointVenteOptimiser(Long compteAutreId, ZonedDateTime dateDebut, ZonedDateTime dateFin, Long pointVenteId) {
        log.debug("Request to get some TransactionComptePtis---");
        CompteAutre compteAutre = new CompteAutre();
        CompteAutre ca = compteAutreRepository.findById(compteAutreId).orElse(null);
        if (dateDebut != null && dateFin != null) {
            return transactionComptePtiRepository.findByCompteAutreAndDateBetweenAndOperationPointVenteIdOrderByIdDescOptimise(ca.getId(), dateDebut, dateFin.plusHours(23), pointVenteId);
        } else if (dateDebut != null) {
            return transactionComptePtiRepository.findByCompteAutreAndDateBetweenAndOperationPointVenteIdOrderByIdDescOptimise(ca.getId(), dateDebut, dateDebut.plusHours(23), pointVenteId);

        }
        return null;
    }

    /**
     * Solde d'un compte en fonction du bilan des transactions
     *
     * @param compteAutreId
     * @return
     */
    public BigDecimal bilanTransactionByCompte(Long compteAutreId) {
        if (compteAutreId != null) {
            Compte compte = compteRepository.findByCompteAutreId(compteAutreId);
            if (compte.getComptePincipale() != null) {
                if (compte.getCompteAutre() != null) {
                    compteAutreId = compte.getComptePincipale().getCompteAutre().getId();
                    log.debug("COMPTE AUTRE %%" + compteAutreId);
                }
            }
        }

        List<TransactionComptePti> transactionComptePtis = transactionComptePtiRepository.findByCompteAutreId(compteAutreId);
        BigDecimal montant = BigDecimal.ZERO;
        for (TransactionComptePti t : transactionComptePtis) {

            switch (t.getType()) {
                case DEBIT:
                    montant = montant.subtract(t.getMontant());
                    break;
                case CREDIT:
                    montant = montant.add(t.getMontant());
                    break;
            }
            log.debug("Montant : " + montant + "#" + t.getType().name());

        }
        return montant;
    }

    /**
     * Get one transactionComptePti by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public TransactionComptePtiDTO findOne(Long id) {
        log.debug("Request to get TransactionComptePti : {}", id);
        TransactionComptePti transactionComptePti = transactionComptePtiRepository.findById(id).orElse(null);
        return transactionComptePtiMapper.toDto(transactionComptePti);
    }

    /**
     * Delete the transactionComptePti by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete TransactionComptePti : {}", id);
        transactionComptePtiRepository.deleteById(id);
    }

    public List<TransactionComptePti> findByOperationIdAnnuler(Long operationId) {
        List<TransactionComptePti> transactionComptePtis = transactionComptePtiRepository.findByOperationIdAndEtatOrderByIdDesc(operationId, EtatValidite.VALIDE);

        return transactionComptePtis;
    }

    private void ecritureCompte(TypeTransactionCompte sens, Compte comptePTISrc, Compte comptePTIDest, BigDecimal montant, Operation operation) throws ErrorException {
        CompteAutre compteAutre = comptePTISrc.getCompteAutre();
        boolean status = false;
        CompteAutre copyCompte = (CompteAutre) compteAutre.clone();
        if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
            if (sens.equals(TypeTransactionCompte.CREDIT)) {
                if (comptePTISrc.getEtiquette().endsWith("PTI")) {
                    //montantDebit = montant.multiply(new BigDecimal("-1"));
                    CompteAutre compteCredit = compteService.crediterCompteAutre(compteAutre, montant);
                    saveTransactionOfComptePTI(compteCredit, montant, TypeTransactionCompte.CREDIT, operation);

                } else {
                    CompteAutre compteCredit = compteService.crediterCompte(comptePTISrc, montant);
                    saveTransactionOfCompte(compteCredit, comptePTIDest.getCompteAutre(), montant, TypeTransactionCompte.CREDIT, operation);
                }
            } else if (sens.equals(TypeTransactionCompte.DEBIT)) {
                if (comptePTISrc.getEtiquette().endsWith("PTI")) {
                    CompteAutre compteDebit = compteService.debiterCompteAutre(comptePTISrc.getCompteAutre(), montant);
                    saveTransactionOfComptePTI(compteDebit, montant, TypeTransactionCompte.DEBIT, operation);
                } else {
                    CompteAutre compteDebit = compteService.debiterCompte(comptePTISrc, montant);
                    saveTransactionOfCompte(compteDebit, comptePTIDest.getCompteAutre(), montant, TypeTransactionCompte.DEBIT, operation);

                }
            }
        }
    }

    /**
     * Liste des transactions associé à une operation
     *
     * @param operationId
     * @return
     */
    public List<TransactionComptePtiDTO> findByOperationId(Long operationId
    ) throws ErrorException {
        List<TransactionComptePti> transactionComptePtis = transactionComptePtiRepository.findByOperationIdAndEtatOrderByIdDesc(operationId, EtatValidite.VALIDE);
        for (int i = 0; i < transactionComptePtis.size(); i = i + 2) {
            TransactionComptePti t1 = transactionComptePtis.get(i);
            TransactionComptePti t2 = transactionComptePtis.get(i + 1);
            Compte cptT1 = compteService.findByCompteAutreId(t1.getCompteAutre().getId());
            Compte cptT2 = compteService.findByCompteAutreId(t2.getCompteAutre().getId());
            if (t1.getType().equals(TypeTransactionCompte.CREDIT)) {
                ecritureCompte(TypeTransactionCompte.DEBIT, cptT1, cptT2, t1.getMontant(), t1.getOperation());
                ecritureCompte(TypeTransactionCompte.CREDIT, cptT2, cptT1, t2.getMontant(), t2.getOperation());

            } else {

                ecritureCompte(TypeTransactionCompte.DEBIT, cptT2, cptT1, t2.getMontant(), t2.getOperation());
                ecritureCompte(TypeTransactionCompte.CREDIT, cptT1, cptT2, t1.getMontant(), t1.getOperation());

            }
        }
        return transactionComptePtiMapper.toDto(transactionComptePtis);
    }

    /**
     * Service permettant de faire une opération de DEBIT & CREDIT
     *
     * @param numSrc
     * @param numDest
     * @param montant
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void ecritureCompte(String numSrc, String numDest,
            BigDecimal montant
    ) {
        Compte compteSrc = compteRepository.findTopByValeurCompteAndCategorieCompte(numSrc, CategorieCompte.AUTRE);
        Compte compteDest = compteRepository.findTopByValeurCompteAndCategorieCompte(numDest, CategorieCompte.AUTRE);
        if (compteSrc != null && compteDest != null && compteSrc.getCompteAutre() != null && compteDest.getCompteAutre() != null) {
            if (compteService.peutDebiterCompte(compteSrc, montant)) {
                compteService.debiterCompte(compteSrc, montant);
                this.saveTransactionOfCompte(compteSrc.getCompteAutre(), compteDest.getCompteAutre(), montant, TypeTransactionCompte.DEBIT, null);
                compteService.crediterCompte(compteDest, montant);
                this.saveTransactionOfCompte(compteDest.getCompteAutre(), compteSrc.getCompteAutre(), montant, TypeTransactionCompte.CREDIT, null);
            } else {
                throw new CustomParameterizedException("Solde du compte source insuffisant!");
            }
        } else {
            throw new CustomParameterizedException("L'un des compte est invalide!");
        }
    }

    public List<TransactionComptePti> findByCompteAutre(CompteAutre compteAutre, ZonedDateTime dateDebut, ZonedDateTime dateFin) {
        return transactionComptePtiRepository.findByCompteAutreAndDateBetweenOrderByIdDesc(compteAutre, dateDebut, dateFin.plusHours(23));
    }

    public List<TransactionComptePti> findByCompteAutreMiniReleve(CompteAutre compteAutre, Integer nombre) {
        return transactionComptePtiRepository.findByCompteAutreIdAndLimitOrderByIdDesc(compteAutre.getId());
    }

    /**
     * Retourner la Liste des transactions associé à une operation
     *
     *
     */
    public Page<TransactionComptePtiDTO> findByOperation(Long operationId, Pageable pageable) {
        Page<TransactionComptePtiDTO> transactionComptePti = transactionComptePtiRepository.findTransactionByOp(operationId, pageable).map(transactionComptePtiMapper::toDto);
        transactionComptePti.forEach(t -> {
            Compte cpt = compteRepository.findByCompteAutreId(t.getCompteAutreId());
            if (cpt != null) {
                t.setValeurCompte(cpt.getValeurCompte());
                String etCpt = etiquetteCompteRepository.finByEtiquette(cpt.getEtiquette());
                if (etCpt != null) {
                    t.setLibelleCompte(etCpt);
                }
                t.setEtiquette(cpt.getEtiquette());
                if (cpt.getClient() != null) {
                    if (cpt.getClient().getPersonne() != null) {
                        t.setClientTelephone(cpt.getClient().getPersonne().getTelephone());
                    }
                }
                if (cpt.getUniteOrganisation() != null) {
                    t.setOrganisationLibelle(cpt.getUniteOrganisation().getLibelleUniteOrganisation());
                }
            }
        });
        return transactionComptePti;
    }

    public Page<TransactionComptePtiDTO> findByOperationOptimise(Long operationId, Pageable pageable) {

        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());

        int totalTcpt = transactionComptePtiRepository.countByOperationId(operationId);

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
        List<TransactionComptePtiDTO> transactionComptePti = transactionComptePtiRepository.findTransactionByOpOptimise(operationId, taille, page);
        transactionComptePti.forEach(t -> {
            CompteDTO cpt = compteRepository.findByCompteAutreIdOptimise(t.getCompteAutreId());
            t.setValeurCompte(cpt.getValeurCompte());
            t.setEtiquette(cpt.getEtiquette());
            String etCpt = etiquetteCompteRepository.finByEtiquette(cpt.getEtiquette());
            if (etCpt != null) {
                t.setLibelleCompte(etCpt);
            }
            if (cpt.getClientId() != null) {
                Client client = clientRepository.findById(cpt.getClientId()).orElse(null);
                if (client.getPersonne() != null) {
                    t.setClientTelephone(client.getPersonne().getTelephone());
                }
            }
            if (cpt.getUniteOrganisationId() != null) {
                t.setOrganisationLibelle(uniteOrganisationRepository.getLibelleUniteOrganisation(cpt.getUniteOrganisationId()));
            }
        });
        Page<TransactionComptePtiDTO> retour = new PageImpl<>(transactionComptePti, pageable1, totalTcpt);
        return retour;
    }

    public List<TransactionComptePti> findByCompteAutreId(Long compteAutreId, ZonedDateTime dateFin) {
        if (dateFin != null) {
            return transactionComptePtiRepository.findByCompteAutreAndDateBetweenOrderByIdDesc(compteAutreRepository.findById(compteAutreId).orElse(null), dateFin, dateFin.plusHours(23));

        } else {
            return transactionComptePtiRepository.findByCompteAutreId(compteAutreId);
        }
    }

    public TransactionComptePti findByOperationAndCompteAutre(String codeExterne, Long compteAutreId) {
        TransactionComptePti trans = transactionComptePtiRepository.findByOperationAndCompteAutre(codeExterne, compteAutreId);
        return trans;
    }

    public TransactionComptePti findByOperationAndCompteAutre2(String codeExterne, Long compteAutreId) {
        TransactionComptePti trans = transactionComptePtiRepository.findByOperationAndCompteAutre2(codeExterne, compteAutreId);
        return trans;
    }

    public Long findExisteOperation(String codeExterne, Long compteAutreId) {
        Long trans = transactionComptePtiRepository.findExisteOperation(codeExterne, compteAutreId);
        return trans;
    }
}
