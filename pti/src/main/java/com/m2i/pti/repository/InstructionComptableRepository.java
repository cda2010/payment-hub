package com.m2i.pti.repository;

import com.m2i.pti.domain.InstructionComptable;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.enumeration.InstructionEcriture;
import com.m2i.pti.domain.enumeration.MontantsAComptabiliser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the InstructionComptable entity.
 */
@SuppressWarnings("unused")
@Repository
public interface InstructionComptableRepository extends JpaRepository<InstructionComptable, Long> {
    List<InstructionComptable> findByTypeServiceId(Long typeServiceId);
  @Query(value = "select ic from InstructionComptable ic where ic.typeService=?1 order by  ic.numeroOrdre")
    List<InstructionComptable> findByTypeServiceOrderByNumeroOrdre(TypeService typeService);
 @Query(value = "select ic from InstructionComptable ic where ic.typeService=?1 and etiquetteComptable like concat('%',?2) order by  ic.numeroOrdre")
    List<InstructionComptable> findByTypeServiceOrderByNumeroOrdreAndType(TypeService typeService , String type);
    
    
    @Query(value = "select ic from InstructionComptable ic where  (ic.typeService.id=:typeServiceId or :typeServiceId is null) and (ic.instruction=:instruction or :instruction is null) and (ic.etiquetteComptable=:etiquetteComptable or :etiquetteComptable is null) and (ic.montant=:montant or :montant is null ) order by ic.numeroOrdre")
    Page<InstructionComptable> findInstructionByMultiParams(@Param("typeServiceId") Long typeServiceId, @Param("instruction") InstructionEcriture instruction, @Param("etiquetteComptable") String etiquetteComptable, @Param("montant") MontantsAComptabiliser montant, Pageable pageable);
}
