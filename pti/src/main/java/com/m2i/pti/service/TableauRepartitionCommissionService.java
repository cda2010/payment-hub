package com.m2i.pti.service;

import com.m2i.pti.domain.GroupRepartition;
import com.m2i.pti.domain.TableauRepartitionCommission;
import com.m2i.pti.repository.GroupRepartitionRepository;
import com.m2i.pti.repository.TableauRepartitionCommissionRepository;
import com.m2i.pti.service.dto.GroupRepartitionDTO;
import com.m2i.pti.service.dto.TableauRepartitionCommissionDTO;
import com.m2i.pti.service.mapper.TableauRepartitionCommissionMapper;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service Implementation for managing TableauRepartitionCommission.
 */
@Service
@Transactional
public class TableauRepartitionCommissionService {

    private final Logger log = LoggerFactory.getLogger(TableauRepartitionCommissionService.class);

    private final TableauRepartitionCommissionRepository tableauRepartitionCommissionRepository;

    private final TableauRepartitionCommissionMapper tableauRepartitionCommissionMapper;
    private final GroupRepartitionRepository groupRepartitionRepository;

    public TableauRepartitionCommissionService(TableauRepartitionCommissionRepository tableauRepartitionCommissionRepository, TableauRepartitionCommissionMapper tableauRepartitionCommissionMapper, GroupRepartitionRepository groupRepartitionRepository) {
        this.tableauRepartitionCommissionRepository = tableauRepartitionCommissionRepository;
        this.tableauRepartitionCommissionMapper = tableauRepartitionCommissionMapper;
        this.groupRepartitionRepository = groupRepartitionRepository;
    }

    /**
     * Save a tableauRepartitionCommission.
     *
     * @param tableauRepartitionCommissionDTO the entity to save
     * @return the persisted entity
     */
    public TableauRepartitionCommissionDTO save(TableauRepartitionCommissionDTO tableauRepartitionCommissionDTO) {
        log.debug("Request to save TableauRepartitionCommission : {}", tableauRepartitionCommissionDTO);
        List<TableauRepartitionCommission> verifTableau = tableauRepartitionCommissionRepository.findByTypeServiceIdAndNiveaoOperationAndActivatedTrue(tableauRepartitionCommissionDTO.getTypeServiceId(), tableauRepartitionCommissionDTO.getNiveaoOperation());
/*
        if (tableauRepartitionCommissionDTO.getDateDebut().isAfter(tableauRepartitionCommissionDTO.getDateFin()))
            throw new CustomParameterizedException("La date de fin doit être supérieure à la date de debut!");
*/
        TableauRepartitionCommission tableauRepartitionCommission = tableauRepartitionCommissionMapper.toEntity(tableauRepartitionCommissionDTO);
        tableauRepartitionCommission = tableauRepartitionCommissionRepository.save(tableauRepartitionCommission);
        return tableauRepartitionCommissionMapper.toDto(tableauRepartitionCommission);
    }

    /**
     * Get all the tableauRepartitionCommissions.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TableauRepartitionCommissionDTO> findAll(Pageable pageable) {
        log.debug("Request to get all TableauRepartitionCommissions");
        return tableauRepartitionCommissionRepository.findAll(pageable)
                .map(tableauRepartitionCommissionMapper::toDto);
    }
    @Transactional(readOnly = true)
    public Page<TableauRepartitionCommissionDTO> findAllByGroup(Long groupId,Pageable pageable) {
        log.debug("Request to get all TableauRepartitionCommissions");
        GroupRepartition groupRepartition=groupRepartitionRepository.findById(groupId).orElse(null);
        return tableauRepartitionCommissionRepository.findAllByGroup(groupRepartition,pageable)
                .map(tableauRepartitionCommissionMapper::toDto);
    }

    public List<TableauRepartitionCommissionDTO> findAll() {
        return tableauRepartitionCommissionMapper.toDto(tableauRepartitionCommissionRepository.findAll());
    }

    /**
     * Get one tableauRepartitionCommission by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public TableauRepartitionCommissionDTO findOne(Long id) {
        log.debug("Request to get TableauRepartitionCommission : {}", id);
        TableauRepartitionCommission tableauRepartitionCommission = tableauRepartitionCommissionRepository.findById(id).orElse(null);
        return tableauRepartitionCommissionMapper.toDto(tableauRepartitionCommission);
    }

    /**
     * Delete the tableauRepartitionCommission by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete TableauRepartitionCommission : {}", id);
        tableauRepartitionCommissionRepository.deleteById(id);
    }

    public Page<TableauRepartitionCommissionDTO> findAllByMultiC(Long typeServiceId, Integer niveau, Pageable pageable) {
        return tableauRepartitionCommissionRepository.findTableauRepartitionCommissionByMultiParams(typeServiceId, niveau, pageable).map(tableauRepartitionCommissionMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<TableauRepartitionCommissionDTO> findAllByGroupOptimise(Long groupId,Pageable pageable) {
        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize()*pageable.getPageNumber());
        int totaltr = (int) tableauRepartitionCommissionRepository.count();

        Pageable pageable1= new Pageable() {
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
        log.debug("Request to get all TableauRepartitionCommissions");
        GroupRepartitionDTO groupRepartition=groupRepartitionRepository.findOneOptimise(groupId);
        List<TableauRepartitionCommissionDTO> tableauRepartitionCommissionDTOS = tableauRepartitionCommissionRepository.findByGroupeIdOptimise(groupRepartition.getId(),taille,page);
        Page<TableauRepartitionCommissionDTO> retour = new PageImpl<>(tableauRepartitionCommissionDTOS, pageable1, totaltr);
        return retour;
    }

}
