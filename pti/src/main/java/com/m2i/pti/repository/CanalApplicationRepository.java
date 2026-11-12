package com.m2i.pti.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.m2i.pti.domain.CanalApplication;
import com.m2i.pti.service.dto.CanalApplicationDTO; 

/**
 * Spring Data JPA repository for the Canal entity.
 */
@SuppressWarnings("unused") 
@Repository
public interface CanalApplicationRepository extends JpaRepository<CanalApplication, Long> {
    @Query(name = "CanalApplication.findCanalApplication", nativeQuery = true)
    List<CanalApplicationDTO> getAllCanalApplication();

    @Query(name = "CanalApplication.findCanalApplicationStatut", nativeQuery = true)
    List<CanalApplicationDTO> getAllCanalApplicationStatut(@Param("canal") String canal);
}
