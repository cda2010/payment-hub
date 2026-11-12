package com.m2i.pti.repository;


import com.m2i.pti.domain.BwAccount;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;


/**
 * Spring Data  repository for the BwAccount entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BwAccountRepository extends JpaRepository<BwAccount, Long> {

    public List<BwAccount> findByBwCustomerId(Long cusId);
    
    
    @Query("select a from BwAccount a where a.numeroCompte=?1 and a.bwCustomer.msisdn =?2")
    public BwAccount findByNumeroCompteAndBwCustomerMsisdn(String numeroCompte,String msisdn);

    public BwAccount findByBwCustomerMsisdn(String msisdn);

    public BwAccount findByNumeroCompte(String numeroCompte);

}
