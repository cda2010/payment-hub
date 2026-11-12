/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import com.m2i.pti.domain.InstructionComptableRegistre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author cda
 */
@SuppressWarnings("unused")
@Repository
public interface InstructionComptableRegistreRepository extends JpaRepository<InstructionComptableRegistre, Long> {
    
}
