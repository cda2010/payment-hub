/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import com.m2i.pti.domain.ExternalAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

/**
 *
 * @author cda
 */
@SuppressWarnings("unused")
@Repository
public interface ExternalAccountRepository extends JpaRepository<ExternalAccount, Long>{
    @Query("select e from ExternalAccount e where e.alias= ?1 and e.codeOperateur=?2 and coalesce(e.etatCompteExterne,true) = true")
    ExternalAccount findExternalAccountByAliasAndCodeOperateur(String alias, String codeOperateur);
}
