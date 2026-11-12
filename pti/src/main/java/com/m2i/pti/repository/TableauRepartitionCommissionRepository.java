package com.m2i.pti.repository;


import com.m2i.pti.domain.GroupRepartition;
import com.m2i.pti.domain.TableauRepartitionCommission;
import com.m2i.pti.service.dto.TableauRepartitionCommissionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import org.springframework.cache.annotation.Cacheable;


/**
 * Spring Data JPA repository for the TableauRepartitionCommission entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TableauRepartitionCommissionRepository extends JpaRepository<TableauRepartitionCommission, Long> {
    
    Page<TableauRepartitionCommission> findByTypeServiceIdAndActivatedIsTrue(Long typeServiceId, Pageable pageable);
 
    List<TableauRepartitionCommission> findByTypeServiceIdAndNiveaoOperationAndActivatedTrue(Long typeServiceId, Integer niveau);
 
    List<TableauRepartitionCommission> findByTypeServiceIdAndNiveaoOperationAndOrganisationIdAndActivatedIsTrue(Long typeServiceId, Integer niveau, Long organisationId);

    @Query("select c from TableauRepartitionCommission c where (c.typeService.id=:typeServiceId or c.typeService.id is null ) " +
            "And (c.niveaoOperation=:niveau or c.niveaoOperation is null ) " +
            "and c.activated=true " +
            "And c.id  in (SELECT pt.tableau_repartition_commissions_id " +
            "FROM ProfilActeurTableauRepartitionCommission pt  where pt.profil_acteurs_id=:profilId)" +
            "and c.activated=true order by c.typeService.id desc ")
   
    List<TableauRepartitionCommission> findTableauRepartitionCommissionByMultiParams(@Param("typeServiceId") Long typeServiceId, @Param("niveau") Integer niveau, @Param("profilId") Long profilId);
    @Query("select c from TableauRepartitionCommission c where (c.typeService.id=:typeServiceId or :typeServiceId is null ) And (c.niveaoOperation=:niveau or :niveau is null ) order by c.typeService.id desc ")
    Page<TableauRepartitionCommission> findTableauRepartitionCommissionByMultiParams(@Param("typeServiceId") Long typeServiceId, @Param("niveau") Integer niveau, Pageable pageable);
 
    Set<TableauRepartitionCommission> findAllByGroup(GroupRepartition groupRepartition);
   
    Page<TableauRepartitionCommission> findAllByGroup(GroupRepartition groupRepartition, Pageable pageable);

    @Query(name = "TableauRepartitionCommission.findByGroupeId", nativeQuery = true)
    List<TableauRepartitionCommissionDTO> findByGroupeIdOptimise(@Param("groupId") Long groupId, @Param("taille") Long taille, @Param("page") Long page);


}
