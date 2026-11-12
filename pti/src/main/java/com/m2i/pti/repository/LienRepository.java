package com.m2i.pti.repository;

import com.m2i.pti.domain.Lien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the Lien entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LienRepository extends JpaRepository<Lien, Long> {
        @Query("select count(*) from Lien l where l.personne.id=:personneId and l.lienPotentiel=:lienId")
        Integer countPersonneAnExsisting(@Param("personneId") Long personneId,@Param("lienId") Long lienId);

        List<Lien> findAllByPersonneIdAndLienPotentiel(Long idP, Long lienP);

        @Query("select l from Lien l order by l.lienPotentiel")
        Page<Lien> findAllByLienPotentiel(Pageable pageable);

}
