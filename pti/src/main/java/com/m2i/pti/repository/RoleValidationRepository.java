package com.m2i.pti.repository;

import com.m2i.pti.domain.RoleValidation;
import com.m2i.pti.service.dto.RoleValidationDTO;
import java.util.List;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the RoleValidation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RoleValidationRepository
  extends JpaRepository<RoleValidation, Long> {
  @Query("Select v from RoleValidation v where v.typeService.id=?1")
  public List<RoleValidation> findByTypeServiceId(Long typeService);

  @Query(name = "RoleValidation.findAllRoleValidation", nativeQuery = true)
  List<RoleValidationDTO> findAllRoleValidation();
}
