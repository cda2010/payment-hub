/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

/**
 *
 * @author cda
 */
import com.m2i.pti.domain.OperationAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@SuppressWarnings("unused")
@Repository
public interface OperationAuditEventRepository extends JpaRepository<OperationAuditEvent, Long> {

}
