package com.m2i.pti.service;

import com.m2i.pti.domain.BilanOperation;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.Personne;
import com.m2i.pti.repository.BilanOperationRepository;
import com.m2i.pti.repository.ClientRepository;
import com.m2i.pti.service.dto.BilanOperationDTO;
import com.m2i.pti.service.dto.ClientDTO;
import com.m2i.pti.service.mapper.BilanOperationMapper;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import com.m2i.pti.web.rest.errors.CustomSettlementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import jakarta.persistence.OptimisticLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;

/**
 * Service Implementation for managing BilanOperation.
 */
@Service

public class BilanOperationService {

    private final Logger log = LoggerFactory.getLogger(BilanOperationService.class);

    private final BilanOperationRepository bilanOperationRepository;

    private final BilanOperationMapper bilanOperationMapper;

    private final ClientRepository clientRepository;

    public BilanOperationService(BilanOperationRepository bilanOperationRepository, BilanOperationMapper bilanOperationMapper, ClientRepository clientRepository) {
        this.bilanOperationRepository = bilanOperationRepository;
        this.bilanOperationMapper = bilanOperationMapper;
        this.clientRepository = clientRepository;
    }

    /**
     * Save a bilanOperation.
     *
     * @param bilanOperationDTO the entity to save
     * @return the persisted entity
     */
    public BilanOperationDTO save(BilanOperationDTO bilanOperationDTO) {
        log.debug("Request to save BilanOperation : {}", bilanOperationDTO);
        BilanOperation bilanOperation = bilanOperationMapper.toEntity(bilanOperationDTO);
        bilanOperation = bilanOperationRepository.save(bilanOperation);
        return bilanOperationMapper.toDto(bilanOperation);
    }

    public void saveB(List<BilanOperation> bilanOperations, Long acteurId, Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
            Long uoEmetteurId, Long uoAcqId, BigDecimal montant) {
        if (!bilanOperations.isEmpty()) {
            log.debug("Mise à jour bilan %%%" + acteurId);
            BilanOperation bilanOperation = bilanOperations.get(0);
            BigDecimal montantBilan = bilanOperation.getMontant();
            Integer nombre = bilanOperation.getNombre();
            bilanOperation.setMontant(montantBilan.add(montant));
            bilanOperation.setNombre(nombre + 1);
            bilanOperationRepository.save(bilanOperation);
        } else {
            BilanOperationDTO bilanOperationDTO = new BilanOperationDTO();
            bilanOperationDTO.setActeurId(acteurId);
            bilanOperationDTO.setTypeServiceId(typeServiceId);
            bilanOperationDTO.setDateDerniereOperation(LocalDate.now());
            bilanOperationDTO.setOrganisationAcqId(uoAcqId);
            bilanOperationDTO.setOrganisationEmId(uoEmetteurId);
            bilanOperationDTO.setMontant(montant);
            bilanOperationDTO.setNombre(1);
            bilanOperationDTO.setCanalId(canalId);
            bilanOperationDTO.setClientBeneficiaireId(clientBeneficiaireId);
            bilanOperationDTO.setClientEmetteurId(clientEmetteurId);
            this.save(bilanOperationDTO);
        }

    }

    /**
     * Get all the bilanOperations.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<BilanOperationDTO> findAll(Pageable pageable) {
        log.debug("Request to get all BilanOperations");
        return bilanOperationRepository.findAll(pageable)
                .map(bilanOperationMapper::toDto);
    }

    public List<BilanOperation> findAllByActeurAndMultiCriteria(Long acteurId, Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
            Long uoEmetteurId, Long uoAcqId, LocalDate date) {
        log.debug("Request to get all TableauFrais");
        if (acteurId != null) {
            return bilanOperationRepository.findFraisByMultiParamsAndActeur(acteurId, typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmetteurId, uoAcqId, date);
        } else {

            return bilanOperationRepository.findFraisByMultiParamsAndClientX(typeServiceId, canalId, clientBeneficiaireId, uoEmetteurId, uoAcqId, date);
        }
    }

    public BigDecimal findTotalBilanJour(Long typeServiceId, Long clientEmeteurId
    ) {
        log.debug("Request to get all TableauFrais");

        List<BilanOperation> listBilan = bilanOperationRepository.findBilanClient(typeServiceId, clientEmeteurId, LocalDate.now());

        BigDecimal total = listBilan.stream()
                .map(BilanOperation::getMontant)
                .filter(Objects::nonNull)
                .filter(m -> m.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total;

    }

    /**
     * Get one bilanOperation by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public BilanOperationDTO findOne(Long id) {
        log.debug("Request to get BilanOperation : {}", id);
        BilanOperation bilanOperation = bilanOperationRepository.findById(id).orElse(null);
        return bilanOperationMapper.toDto(bilanOperation);
    }

    /**
     * Delete the bilanOperation by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete BilanOperation : {}", id);
        bilanOperationRepository.deleteById(id);
    }

    /**
     * Enregistrement du bilan journalier
     *
     * @param acteurId
     * @param typeServiceId
     * @param canalId
     * @param clientBeneficiaireId
     * @param clientEmetteurId
     * @param uoEmetteurId
     * @param uoAcqId
     * @param montant
     */
    @Async("taskExecutor")
    public void saveCumulJournalierActeur(Long acteurId, Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
            Long uoEmetteurId, Long uoAcqId, BigDecimal montant) {

        try {
            log.info("Sauvegarde du bilan de l'operation");
            List<BilanOperation> bilanOperations = this.findAllByActeurAndMultiCriteria(acteurId, typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmetteurId, uoAcqId, LocalDate.now());
            //       BigDecimal mtop = Util.getZeroIfNull(operation.getMontantFrais()).add(operation.getMontantOp()).add(Util.getZeroIfNull(operation.getMontantTaxe()));
            this.saveB(bilanOperations, acteurId, typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmetteurId, uoAcqId, montant);
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            throw new CustomSettlementException("Serveur surchargé veuillez essayer à nouveau");
        }

    }

    @Async("taskExecutor")
    public void saveCumulJournalierClient(Long acteurId, Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
            Long uoEmetteurId, Operation operation) {
        try {

            log.debug("Enregistrement du cumul journalier pour le client");
            Personne beneficiaire = null;
            Personne personne = null;
            Long benId = null;
            if (operation.getBeneficaire() != null) {
                beneficiaire = operation.getBeneficaire().getPersonne();
                if (beneficiaire != null) {
                    benId = beneficiaire.getId();
                }
            }
            if (operation.getEmetteur() != null) {
                personne = operation.getEmetteur().getPersonne();
            }

            if (personne != null) {
                log.debug("Cumul client");
                //    Client clientBeneficiaire = clientRepository.findTop1ByPersonneId(operation.getBeneficaire().getPersonne().getId());
                List<BilanOperation> bilanOperations = this.findAllByActeurAndMultiCriteria(null, typeServiceId, canalId, benId, personne.getId(),
                        uoEmetteurId, operation.getUoAcquereur().getId(), LocalDate.now());
                BigDecimal mtaxe = BigDecimal.ZERO;
                if (Util.estPositifStrict(operation.getMontantFraisClient())) {
                    mtaxe = operation.getMontantTaxe();
                }
                BigDecimal mtop = Util.getZeroIfNull(operation.getMontantFraisClient()).add(operation.getMontantOp()).add(Util.getZeroIfNull(mtaxe));
                this.saveB(bilanOperations, null, typeServiceId, canalId, benId, personne.getId(), uoEmetteurId, operation.getUoAcquereur().getId(), mtop);
            }
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            throw new CustomSettlementException("Serveur surchargé veuillez essayer à nouveau");
        }

    }

    public Page<BilanOperationDTO> findallByPersonneoptimiser(Long personneId, ZonedDateTime datedebut, ZonedDateTime dateFin, Pageable pageable) {
        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalBilan = 0;
        List<BilanOperationDTO> pbilanOperations = null;

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
        if (datedebut != null && dateFin != null) {
            totalBilan = bilanOperationRepository.countBilanOperationByPersonne(personneId, datedebut.toLocalDate(), dateFin.toLocalDate());
            pbilanOperations = bilanOperationRepository.findBilanOpByPersonneIdDate(personneId, datedebut.toLocalDate(), dateFin.toLocalDate(), taille, page);

        } else if (datedebut != null) {
            pbilanOperations = bilanOperationRepository.findBilanOpByPersonneIdDate(personneId, datedebut.toLocalDate(), datedebut.toLocalDate(), taille, page);

        } else {
            LocalDate d = null;
            totalBilan = bilanOperationRepository.countBilanOperationByPersonne(personneId, null, null);
            pbilanOperations = bilanOperationRepository.findBilanOpByPersonneId(personneId, taille, page);
        }
        Page<BilanOperationDTO> result = new PageImpl<>(pbilanOperations, pageable1, totalBilan);
        return result;
    }
}
