package com.m2i.pti.service;

import com.m2i.pti.domain.MachineAuditEvent;
import com.m2i.pti.repository.MachineAuditEventRepository;
import com.m2i.pti.service.dto.MachineAuditEventDTO;
import com.m2i.pti.service.mapper.MachineAuditEventMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service Implementation for managing MachineAuditEvent.
 */
@Service
@Transactional
public class MachineAuditEventService {

    private final Logger log = LoggerFactory.getLogger(MachineAuditEventService.class);

    private final MachineAuditEventRepository machineAuditEventRepository;

    private final MachineAuditEventMapper machineAuditEventMapper;

    private final UserService userService;

    public MachineAuditEventService(MachineAuditEventRepository machineAuditEventRepository, MachineAuditEventMapper machineAuditEventMapper, UserService userService) {
        this.machineAuditEventRepository = machineAuditEventRepository;
        this.machineAuditEventMapper = machineAuditEventMapper;
        this.userService = userService;
    }

    /**
     * Save a machineAuditEvent.
     *
     * @param machineAuditEventDTO the entity to save
     * @return the persisted entity
     */
    public MachineAuditEventDTO save(MachineAuditEventDTO machineAuditEventDTO) {
        log.info("Request to save MachineAuditEvent : {}", machineAuditEventDTO);
        MachineAuditEvent machineAuditEvent = machineAuditEventMapper.toEntity(machineAuditEventDTO);
        machineAuditEvent = machineAuditEventRepository.save(machineAuditEvent);
        return machineAuditEventMapper.toDto(machineAuditEvent);
    }


    /**
     * Get all the machineAuditEvents.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<MachineAuditEventDTO> findAll(Pageable pageable, Long machineId) {
        log.info("Request to get all MachineAuditEvents");
        return machineAuditEventRepository.findByMachineId(machineId, pageable)
                .map(machineAuditEventMapper::toDto);
    }


    /**
     * Get one machineAuditEvent by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public MachineAuditEventDTO findOne(Long id) {
        log.info("Request to get MachineAuditEvent : {}", id);
        return machineAuditEventMapper.toDto(machineAuditEventRepository.findById(id).orElse(null));
    }

    /**
     * Delete the machineAuditEvent by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.info("Request to delete MachineAuditEvent : {}", id);
        machineAuditEventRepository.deleteById(id);
    }

    /**
     * @param id
     * @return
     */
    public List<MachineAuditEventDTO> findByMachineId(Long id) {
        log.info("Request to get MachineAuditEvent : {}", id);
        return machineAuditEventMapper.toDto(machineAuditEventRepository.findByMachineId(id));
    }
}
