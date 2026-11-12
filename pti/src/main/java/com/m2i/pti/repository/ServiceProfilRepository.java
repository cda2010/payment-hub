package com.m2i.pti.repository;

import com.m2i.pti.domain.ServiceProfil;
import com.m2i.pti.domain.enumeration.TypeProfilService;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Spring Data JPA repository for the ServiceProfil entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ServiceProfilRepository extends JpaRepository<ServiceProfil, Long> {
    @Query("select distinct service_profil from ServiceProfil service_profil left join fetch service_profil.typeServices")
    List<ServiceProfil> findAllWithEagerRelationships();

    @Query("select service_profil from ServiceProfil service_profil left join fetch service_profil.typeServices where service_profil.id =:id")
    ServiceProfil findOneWithEagerRelationships(@Param("id") Long id);
    List<ServiceProfil> findAllByTypeProfilService(TypeProfilService typeProfilService);

    @Query(value = "select nom from service_profil where id=CAST(CAST (:sProfilId as TEXT) as BIGINT ) ",nativeQuery = true)
    String getNomServiceProfil(@Param("sProfilId") Long sProfilId);

}
