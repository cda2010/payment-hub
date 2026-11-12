package com.m2i.omnicanal.application.repository;

import com.m2i.omnicanal.application.domain.Authority;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for the Authority entity.
 */
public interface AuthorityRepository extends EntityRepository<Authority, String> {
}
