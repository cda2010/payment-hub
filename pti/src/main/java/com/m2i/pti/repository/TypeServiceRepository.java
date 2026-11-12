package com.m2i.pti.repository;

import com.m2i.pti.domain.TypeService;
import com.m2i.pti.service.dto.TypeServiceDTO;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import java.util.List;
import org.springframework.data.repository.query.Param;


/**
 * Spring Data JPA repository for the TypeService entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TypeServiceRepository extends JpaRepository<TypeService, Long> {
    
      @Query(name = "TypeService.findByCodeService2", nativeQuery = true)
   TypeServiceDTO findByServiceCode(@Param("code") String code);
    
    
  @Query(name = "TypeService.findByCodeService", nativeQuery = true)
   TypeServiceDTO findByCode(@Param("code") String code);
    TypeService findTopByCode(String libelle);
    Boolean existsByLibelle(String libelle);
    Boolean existsByCode(String libelle);
    List<TypeService> findByOperationClientTrue();
    List<TypeService> findByFraisApplicableTrue();
    @Query(value = "SELECT typ FROM TypeService typ where typ.code='APPEL_FOND' or typ.libelle='_ACCOUNT_TO_ACCOUNT'")
    List<TypeService> getSomeTypeService();

    @Query(name = "TypeService.findOne", nativeQuery = true)
    TypeServiceDTO findOneOptimise(@Param("id") Long id);

    @Query(name = "TypeService.findAllTypeService", nativeQuery = true)
    List<TypeServiceDTO> findAllTypeService();
 @Query(name = "TypeService.findAllTypeServiceCat", nativeQuery = true)
    List<TypeServiceDTO> findAllTypeServiceCat();
    @Query(value = "SELECT typ FROM TypeService typ where typ.code='RETRAIT' or typ.code='RETRAIT_COMPTE'")
    List<TypeService> getRetraitOrRetraitCompte();


}
