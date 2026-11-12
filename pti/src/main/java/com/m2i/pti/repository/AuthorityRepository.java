package com.m2i.pti.repository;

import com.m2i.pti.domain.Authority;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

/**
 * Spring Data JPA repository for the Authority entity.
 */
public interface AuthorityRepository extends JpaRepository<Authority, String> {
    Authority findAllByName(String name);
}
