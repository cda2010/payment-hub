package com.m2i.pti.repository;

import com.m2i.pti.domain.Lot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LotRepository extends JpaRepository<Lot, Long>{
    Page<Lot> findByTypeActeur(String typeActeur, Pageable pageable);
    
}
