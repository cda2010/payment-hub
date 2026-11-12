package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.presentation.domain.Authority;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Authority entity.
 */
public interface AuthorityRepository extends EntityRepository<Authority, String> {
}
