package com.m2i.pti.repository;

import com.m2i.pti.domain.Machine;
import com.m2i.pti.service.dto.MachineDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


/**
 * Spring Data  repository for the Machine entity.
 */
@SuppressWarnings("unused")
@Repository
public interface MachineRepository extends JpaRepository<Machine, Long> {
    Optional<Machine> findById(Long id);
     void deleteById(Long id);
    Machine findOneByUuid(String uuid);

    boolean existsByHostId(String hostId);

    Machine findOneByHostId(String hostId);

    Machine findOneByHostIdAndSupprime(String hostId,Boolean supprime);

    Page<Machine> findOneByOrganisationId(Long orgId, Pageable pageable);

    @Query("select m from Machine m where (" +
            "(m.organisation.id=:orgId) and" +
            "(m.supprime=false) and" +
            "(:adresseIp is null or :adresseIp='' or m.adresseIp like('%'||:adresseIp||'%') ) )")
    Page<Machine> findByOrganisationIdWithCriteria(Pageable pageable,@Param("orgId") Long orgId,@Param("adresseIp") String adresseIp);

    @Query("select m from Machine m where m.supprime=false and (:adresseIp is null or :adresseIp='' or m.adresseIp like ('%'||:adresseIp||'%')  ) ")
    Page<Machine> findWithCriteria( Pageable pageable,@Param("adresseIp") String adresseIp);


    @Query(value = "select count(m.id) from machine m where (" +
            "(CAST(:orgId AS text) IS NULL OR CAST(:orgId AS text) = '' OR m.organisation_id = CAST(NULLIF(CAST(:orgId AS text), '') AS BIGINT)) and" +
            "(m.supprime is null or m.supprime=false) and" +
            "(:adresseIp is null or :adresseIp='' or m.adresse_ip like('%'||CAST (:adresseIp as text)||'%') ) )",nativeQuery = true)
    Integer countWithCriteria(@Param("orgId") Long orgId,@Param("adresseIp") String adresseIp);

    @Query(name = "Machine.findAllByCriteria",nativeQuery = true)
    List<MachineDTO> findAllByCriteria(@Param("orgId") Long orgId, @Param("adresseIp") String adresseIp, @Param("taille") Long taille, @Param("page") Long page);


}
