package com.m2i.pti.repository;

import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.EvenementOperation;
import java.math.BigDecimal;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

/**
 * Spring Data JPA repository for the EvenementOperation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface EvenementOperationRepository extends JpaRepository<EvenementOperation, Long> {
     @Query("select e from EvenementOperation e where  e.estLevee=0 and (e.enTraitement is null or e.enTraitement  =false )")
       List<EvenementOperation> findOperationNonLeveeEtNonEntraitement();
       
       @Query("select e from EvenementOperation e where e.operation.typeService.code=?1 and e.estLevee=0 and (e.enTraitement is null or e.enTraitement  =false )")
       List<EvenementOperation> findOperationNonLeveeEtNonEntraitementCardLess(String typeService);
       
 @Query("select e from EvenementOperation e where  e.estLevee=0 ")
       List<EvenementOperation> findOperationNonLevee();
    @Query("select e from EvenementOperation e where e.operation=?1 and e.estLevee=0 ")
    EvenementOperation findByOperation(Operation operation);

    
    @Query(nativeQuery=true, value="select * from evenement_operation where id =?1 and coalesce(est_levee,0)=0 and coalesce(type_evenement,?2)=?2")
    EvenementOperation findEvenementById(Long id, String typeEve);
    
    @Query("select e from EvenementOperation e where e.compte=?1 and e.estLevee=0 ")
    List<EvenementOperation> findAllEvenementOperation(Compte cpt);
    
    @Query(nativeQuery=true, value="select coalesce(sum(montant),0) from evenement_operation where compte_id =?1 and coalesce(est_levee,0)=0 and coalesce(type_evenement,?2)=?2")
    BigDecimal findSommeEvenementOperation(Long cpt, String typeEve);
    
    @Query("select e from EvenementOperation e inner join Compte c on e.compte.id=c.id inner join Client cl on c.client.id=cl.id inner join Personne p on cl.personne.id=p.id where (" +
            " (:telephone is null or :telephone='' or p.telephone=:telephone ) and (:estLeve is null or e.estLevee=:estLeve) and (:cpt is null or e.compte.id=:cpt ) and (e.createdDate between :dateDebut and :dateFin or cast(:dateDebut as timestamp) is null  ) ) ")
    Page<EvenementOperation> fndByCompteAndCriteria(@Param("cpt") Long cpt,
                                                    @Param("dateDebut")  ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin, @Param("telephone") String telephone, @Param("estLeve") Integer estLeve , Pageable pageable);
}
