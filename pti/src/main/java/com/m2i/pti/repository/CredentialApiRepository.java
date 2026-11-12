package com.m2i.pti.repository;

import com.m2i.pti.domain.CredentialApi;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.Optional;


/**
 * Spring Data JPA repository for the CredentialApi entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CredentialApiRepository extends JpaRepository<CredentialApi, Long> {

    @Query(value ="select c from CredentialApi c where c.client_id=:client_id")
    Optional<CredentialApi> findByClientID(@Param("client_id") String client_id);
}
