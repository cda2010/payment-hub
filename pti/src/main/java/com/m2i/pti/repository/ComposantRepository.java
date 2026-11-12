package com.m2i.pti.repository;

import com.m2i.pti.domain.Composant;
import com.m2i.pti.service.dto.ComposantListeDTO;
import java.util.List;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Composant entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ComposantRepository extends JpaRepository<Composant, Long> {

    @Query("select c from Composant c")
    List<Composant> findAllListe();

    @Query("select new com.m2i.pti.service.dto.ComposantListeDTO(c.id, c.label, c.type, c.typeComposant) from Composant c order by c.label")
    List<ComposantListeDTO> findLabels();
}
