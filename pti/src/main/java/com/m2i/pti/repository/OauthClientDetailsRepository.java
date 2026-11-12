package com.m2i.pti.repository;

import com.m2i.pti.domain.OauthClientDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;



@Repository
public interface OauthClientDetailsRepository extends JpaRepository<OauthClientDetails,String> {

    @Query("SELECT o from OauthClientDetails o where o.client_id=?1")
    OauthClientDetails findClient_id(String cli);
    @Query("select o from OauthClientDetails o ")
    Page<OauthClientDetails> findAllOauth(Pageable page);
}
