package com.m2i.pti.repository;

import com.m2i.pti.domain.ChargementMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Spring Data JPA repository for the ChargementMessage entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ChargementMessageRepository extends JpaRepository<ChargementMessage, Long> {
    @Query("select count(c)   from ChargementMessage c where c.hashFichier=?1")
    public Integer findByHashFichier(String hash);
    @Query("select count(c)  from ChargementMessage c where c.nomFichier=?1  ")
    public Integer findByNomFichier(String nomFichier);

    @Query("select m from ChargementMessage m order by m.id DESC ")
    List<ChargementMessage> findAllMessage();

    @Transactional
    @Query("select m from ChargementMessage m order by m.id DESC ")
    Page<ChargementMessage> findAllMessagePage(Pageable pageable);
}
