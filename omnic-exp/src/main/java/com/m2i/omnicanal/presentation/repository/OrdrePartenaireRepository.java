package com.m2i.omnicanal.presentation.repository;

import com.m2i.omnicanal.presentation.execution.OrdrePartenaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdrePartenaireRepository extends JpaRepository<OrdrePartenaire, Long> {

    Optional<OrdrePartenaire> findByCleIdempotence(String cleIdempotence);
}
