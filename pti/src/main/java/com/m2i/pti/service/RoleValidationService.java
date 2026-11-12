package com.m2i.pti.service;

import com.m2i.pti.domain.Authority;
import com.m2i.pti.domain.RoleValidation;
import com.m2i.pti.repository.AuthorityRepository;
import com.m2i.pti.repository.RoleValidationRepository;
import com.m2i.pti.service.dto.RoleValidationDTO;
import com.m2i.pti.service.mapper.RoleValidationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service Implementation for managing RoleValidation.
 */
@Service
@Transactional
public class RoleValidationService {

    private final Logger log = LoggerFactory.getLogger(RoleValidationService.class);

    private final RoleValidationRepository roleValidationRepository;

    private final RoleValidationMapper roleValidationMapper;
    private final AuthorityRepository authorityRepository;

    public RoleValidationService(RoleValidationRepository roleValidationRepository, RoleValidationMapper roleValidationMapper, AuthorityRepository authorityRepository) {
        this.roleValidationRepository = roleValidationRepository;
        this.roleValidationMapper = roleValidationMapper;
        this.authorityRepository = authorityRepository;
    }

    /**
     * Save a roleValidation.
     *
     * @param roleValidationDTO the entity to save
     * @return the persisted entity
     */
    public RoleValidationDTO save(RoleValidationDTO roleValidationDTO) {
        log.debug("Request to save RoleValidation : {}", roleValidationDTO);
        RoleValidation roleValidation = roleValidationMapper.toEntity(roleValidationDTO);
        roleValidation = roleValidationRepository.save(roleValidation);
        return roleValidationMapper.toDto(roleValidation);
    }

    /**
     *  Get all the roleValidations.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RoleValidationDTO> findAll(Pageable pageable) {
        log.debug("Request to get all RoleValidations");
        return roleValidationRepository.findAll(pageable)
            .map(roleValidationMapper::toDto);
    }
    @Transactional(readOnly = true)
    public List<Authority>findAllAuthorities(){
        return authorityRepository.findAll();
    }

    
     @Transactional(readOnly = true)
    public List<RoleValidation> findByTypeService(Long typeServiceId) {
        log.debug("Request to get all RoleValidations");
        return roleValidationRepository.findByTypeServiceId(typeServiceId);
    }
    
    /**
     *  Get one roleValidation by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public RoleValidationDTO findOne(Long id) {
        log.debug("Request to get RoleValidation : {}", id);
        RoleValidation roleValidation = roleValidationRepository.findById(id).orElse(null);
        return roleValidationMapper.toDto(roleValidation);
    }

    /**
     *  Delete the  roleValidation by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete RoleValidation : {}", id);
        roleValidationRepository.deleteById(id);
    }
}
