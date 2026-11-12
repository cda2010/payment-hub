package com.m2i.pti.repository;

import com.m2i.pti.domain.Champs;
import com.m2i.pti.service.dto.ChampsListeDTO;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the Champs entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChampsRepository extends JpaRepository<Champs, Long> {
    @Query("select c from Champs c where c.ecran.id=?1")
    List<Champs> findAllByEcranId(Long ecranId);

    @Query("select new com.m2i.pti.service.dto.ChampsListeDTO(c.id, c.aSaisir, c.min, c.max, c.minLength, c.maxLength, c.isVisible, c.obligatoire, c.pattern, c.label, c.defaultValue, comp.id, comp.label, comp.type, comp.typeComposant) from Champs c left join c.composant comp where c.ecran.id = :id order by c.id")
    List<ChampsListeDTO> findListeByEcran(@Param("id") Long id);
}
