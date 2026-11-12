package com.m2i.pti.service;

import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.RepartitionOp;
import com.m2i.pti.domain.enumeration.Acteur;
import com.m2i.pti.repository.RepartitionOpRepository;
import com.m2i.pti.service.dto.RepartitionOpDTO;
import com.m2i.pti.service.mapper.RepartitionOpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;


/**
 * Service Implementation for managing RepartitionOp.
 */
@Service
@Transactional
public class RepartitionOpService {

    private final Logger log = LoggerFactory.getLogger(RepartitionOpService.class);

    private final RepartitionOpRepository repartitionOpRepository;

    private final RepartitionOpMapper repartitionOpMapper;

    public RepartitionOpService(RepartitionOpRepository repartitionOpRepository, RepartitionOpMapper repartitionOpMapper) {
        this.repartitionOpRepository = repartitionOpRepository;
        this.repartitionOpMapper = repartitionOpMapper;
    }

    /**
     * Save a repartitionOp.
     *
     * @param repartitionOpDTO the entity to save
     * @return the persisted entity
     */
    public RepartitionOpDTO save(RepartitionOpDTO repartitionOpDTO) {
        log.debug("Request to save RepartitionOp : {}", repartitionOpDTO);
        RepartitionOp repartitionOp = repartitionOpMapper.toEntity(repartitionOpDTO);
        repartitionOp = repartitionOpRepository.save(repartitionOp);
        return repartitionOpMapper.toDto(repartitionOp);
    }

    /**
     *
     * @param acteur
     * @param operationId
     * @return
     */
    public BigDecimal getMontantByActeurAndOp(Acteur acteur, Long operationId) {
        RepartitionOp repartitionOp = repartitionOpRepository.findByActeurAndOperationId(acteur, operationId);
        return repartitionOp != null ? repartitionOp.getMontant() : null;
    }

    /**
     * @param acteur
     * @param montant
     * @param op
     * @return
     */
    public void save(Acteur acteur, BigDecimal montant, Operation op) {
        RepartitionOp repartitionOp = new RepartitionOp();
        repartitionOp.acteur(acteur)
                .montant(montant)
                .operation(op)
                .date(ZonedDateTime.now());
        repartitionOpRepository.save(repartitionOp);
    }

    /**
     * Get all the repartitionOps.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RepartitionOpDTO> findAll(Pageable pageable) {
        log.debug("Request to get all RepartitionOps");
        return repartitionOpRepository.findAll(pageable)
                .map(repartitionOpMapper::toDto);
    }

    /**
     * Get one repartitionOp by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public RepartitionOpDTO findOne(Long id) {
        log.debug("Request to get RepartitionOp : {}", id);
        RepartitionOp repartitionOp = repartitionOpRepository.findById(id).orElse(null);
        return repartitionOpMapper.toDto(repartitionOp);
    }

    /**
     * Delete the  repartitionOp by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete RepartitionOp : {}", id);
        repartitionOpRepository.deleteById(id);
    }
}
