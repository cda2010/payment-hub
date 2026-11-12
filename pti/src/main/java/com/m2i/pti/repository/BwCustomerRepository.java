package com.m2i.pti.repository;


import com.m2i.pti.domain.BwCustomer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data repository for the BwCustomer entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BwCustomerRepository extends JpaRepository<BwCustomer, Long> {
    
    @Query("select b from BwCustomer b where (?1 is null or  b.nomCustomer like concat(?1,'%') )  and  (?2 is null or  b.prenomCustomer  like concat(?2,'%') )  and  (?3 is null or  b.msisdn like ?3 )   and b.valide=?4 and  b.uniteOrganisation.id=?5")
    public Page<BwCustomer> findAll(Pageable pageable, String nom, String prenom, String telephone,boolean valide,Long  uoId);
    
    
    @Query("select b from BwCustomer b where b.nomCustomer like coalesce(concat(?1,'%'), b.nomCustomer)  and b.prenomCustomer like coalesce(concat(?2,'%'),b.prenomCustomer)  and b.msisdn=coalesce(?3,b.msisdn) and b.valide=?4")
    public Page<BwCustomer> findAll(Pageable pageable, String nom, String prenom, String telephone,boolean valide);
   @Query("select b from BwCustomer b where b.msisdn =?1")
    public BwCustomer findByTelephone(String msisdn);
    public BwCustomer findByMsisdn(String msisdn);

}
