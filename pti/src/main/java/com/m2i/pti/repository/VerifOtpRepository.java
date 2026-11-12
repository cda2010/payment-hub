package com.m2i.pti.repository;

import com.m2i.pti.domain.VerifOtp;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the VerifOtp entity.
 */
@SuppressWarnings("unused")
@Repository
public interface VerifOtpRepository extends JpaRepository<VerifOtp, Long> {

    VerifOtp findByLoginAndHostIdAndOtp(String login,String hostId,String otp);

    List<VerifOtp> findByLogin(String login);
}
