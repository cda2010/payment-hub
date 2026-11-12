package com.m2i.pti.repository;

import com.m2i.pti.domain.MachineAuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data  repository for the MachineAuditEvent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MachineAuditEventRepository extends JpaRepository<MachineAuditEvent, Long> {

    Page<MachineAuditEvent> findByMachineId(Long machineId, Pageable pageable);
    List<MachineAuditEvent> findByMachineId(Long machineId);
}
