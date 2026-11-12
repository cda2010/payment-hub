/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.m2i.pti.domain.TypeServiceFse;
import com.m2i.pti.mobile.dto.MtypeService;
import com.m2i.pti.service.dto.TypeServiceDTO;
import com.m2i.pti.service.dto.TypeServiceFseDTO;

/**
 *
 * @author cda
 */
@Repository
public interface TypeServiceFseRepository extends JpaRepository<TypeServiceFse, Long> {

    @Query(name = "typeServiceGroupe.findAllTypeService", nativeQuery = true)
    public List<MtypeService> findTypeServiceByCodeUoAndCategorie(@Param("categorieServiceId") Long categorieServiceId, @Param("codeUo") String codeUo);

    @Query(name = "typeServiceGroupe.findCategorieTypeService", nativeQuery = true)
    public List<MtypeService> findTypeServiceCategorie(@Param("categorieServiceId") Long categorieServiceId);
    
      @Query(name = "typeServiceGroupe.findAllByTypeService", nativeQuery = true)
    public List<MtypeService> findAllUoByTypeService(@Param("codeService") String codeService);

    
    @Query(name = "typeServiceGroupe.findAllTypeService", nativeQuery = true)
    public List<MtypeService> findTypeServiceByCodeUo(@Param("categorieServiceId") Long categorieServiceId, @Param("codeUo") String codeUo);
    
    @Query(name = "typeServiceFse.findAllTypeServiceFse", nativeQuery = true)
    List<Object[]> findTypeServiceFseAll();

    @Query(name = "typeServiceFse.findOneTypeService", nativeQuery = true)
    List<TypeServiceFseDTO> findOneTypeService(@Param("uniteOrganisationId") Long uniteOrganisationId);
 @Query(name = "TypeService.findByCategorieService", nativeQuery = true)
    List<TypeServiceDTO> findByCategorieService(@Param("idCategorieService") Long idCategorieService);
}
