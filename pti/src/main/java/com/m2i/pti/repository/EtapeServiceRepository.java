package com.m2i.pti.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.m2i.pti.domain.EtapeService;
import com.m2i.pti.service.dto.EtapeServiceDTO;

@SuppressWarnings("unused")
@Repository
public interface EtapeServiceRepository extends JpaRepository<EtapeService, Long>{
    @Query(name = "TransactionComptePti.findByCompteAutreAndDateBetweenOrderByIdDescOptimise", nativeQuery = true)
    List<EtapeServiceDTO> findByEtapeServiceOrderByIdDescOptimise(@Param("compteAutreId") Long compteAutreId);

    @Query(value = "select eta.id, eta.action_service, eta.type_service_id, ts.code FROM etape_service eta inner join type_service ts on ts.id = eta.type_service_id WHERE (:actionService is null or eta.action_service = CAST(:actionService AS TEXT)) order by eta.id desc", nativeQuery = true)
    List<Object[]> findByCriteria(@Param("actionService") String actionService);
}
