package com.m2i.pti.repository;

import com.m2i.pti.domain.ActiviteClient;
import com.m2i.pti.domain.Bouton;
import com.m2i.pti.service.dto.ActiviteClientDTO;
import com.m2i.pti.service.dto.CompteDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;


/**
 * Spring Data JPA repository for the Bouton entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ActiviteClientRepository extends JpaRepository<ActiviteClient, Long> {
   @Query(name = "activiteClientDTO.findActivite", nativeQuery = true)
    List<ActiviteClientDTO> findActiviteClient(@Param("telephone") String telephone,@Param("codeTelephone") String codeTelephone);
}
