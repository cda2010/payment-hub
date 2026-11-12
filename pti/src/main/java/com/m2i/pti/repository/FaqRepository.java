package com.m2i.pti.repository;

import com.m2i.pti.domain.Faq;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Faq entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

}
