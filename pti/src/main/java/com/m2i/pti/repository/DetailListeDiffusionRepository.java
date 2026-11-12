package com.m2i.pti.repository;

import com.m2i.pti.domain.DetailListeDiffusion;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the DetailListeDiffusion entity.
 */
@SuppressWarnings("unused")
@Repository
public interface DetailListeDiffusionRepository extends JpaRepository<DetailListeDiffusion, Long> {
 List<DetailListeDiffusion> findDetailListeDiffusionByListeId(Long id);
 void deleteDetailListeDiffusionByListeId(Long id);
}
