package com.m2i.pti.repository;

import java.util.List;

import com.m2i.pti.domain.DetailsLot;

import com.m2i.pti.service.dto.ClientDTO;
import com.m2i.pti.service.dto.DetailsLotDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DetailsLotRepository extends JpaRepository<DetailsLot, Long> {

    List<DetailsLot> findDetailsLotByLotId(Long idLot);
    
    @Query("select d from DetailsLot d where d.client.id=?1 and d.lot.id=?2 ")
    DetailsLot findDetailsLotByClientIdAndLotId(Long clientId,Long lotId);

    @Query(name = "DetailsLot.findDetailsLotByLotIdOptimiser",nativeQuery = true)
    List<DetailsLotDTO> findDetailsLotByLotIdOptimiser(@Param("lotId") Long  lotId);
    
    @Query("select d from DetailsLot d where d.ligneChargementFichier.id=?1 and d.lot.id=?2 ")
    DetailsLot findDetailsLotByLigneFichierIdAndLotId(Long ligneChargementFichierId,Long lotId);

}
