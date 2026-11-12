package com.m2i.pti.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.m2i.pti.domain.ParamCanal;
import com.m2i.pti.domain.enumeration.Application;
import com.m2i.pti.service.dto.ParamCanalDTO;

/**
 * Spring Data JPA repository for the Canal entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ParamCanalRepository extends JpaRepository<ParamCanal, Long> {

    @Query(name = "ParamCanal.findParamCanal", nativeQuery = true)
    List<ParamCanalDTO> getAllParamCanalApplication();

    @Query(name = "ParamCanal.findParamCanalStatut", nativeQuery = true)
    List<ParamCanalDTO> getAllParamCanalApplicationStatut(@Param("canal") String canal);
    @Query("select c from ParamCanal c where (c.application= :application or c.application is null) and (c.typeService.id =:typeServiceId or c.typeService.id is null) and (c.uniteOrganisation.id =:uniteOrganisationId or  c.uniteOrganisation.id is null) order by c.poids desc ")
    List<ParamCanal> getParamCanalApplication(@Param("typeServiceId") Long typeServiceId, @Param("application") Application application, @Param("uniteOrganisationId") Long uniteOrganisationId);

}
