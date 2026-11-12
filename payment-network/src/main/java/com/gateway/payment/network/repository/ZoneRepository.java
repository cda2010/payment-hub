package com.gateway.payment.network.repository;

import com.gateway.payment.network.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ZoneRepository extends JpaRepository<Zone, String> {

    List<Zone> findAllByOrderByCode();
}
