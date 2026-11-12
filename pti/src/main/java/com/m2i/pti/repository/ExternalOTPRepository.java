package com.m2i.pti.repository;

import com.m2i.pti.domain.ExternalOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface ExternalOTPRepository extends JpaRepository<ExternalOTP,Long> {
    @Query("select c from ExternalOTP  c where codePays=?1 and telephone=?2 and valide=?3") 
    List<ExternalOTP> findAllByCodePaysAndTelephoneAndValide(String codePays, String telephone, Boolean valide);
@Query("select c from ExternalOTP  c where c.codePays=?1 and c.telephone=?2 and c.valide=true and c.otp=?3") 
    public ExternalOTP findOTPValide(String codePays, String telephone, String otp );
    
    @Query("select c from ExternalOTP  c where c.codePays=?1 and c.telephone=?2 and c.valide=true and c.otp=?3 and c.identifiant=?4") 
    public ExternalOTP findOTPValide(String codePays, String telephone, String otp, String identifiant );
    
       @Query("select c from ExternalOTP  c where c.valide=true and c.otp=?1 and c.identifiant=?2") 
    public ExternalOTP findOTPValide( String otp, String identifiant );
}