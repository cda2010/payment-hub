package com.m2i.pti.repository;

import com.m2i.pti.domain.HoraireAuthentification;
import com.m2i.pti.domain.enumeration.Jour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data JPA repository for the HoraireAuthentification entity.
 */
@SuppressWarnings("unused")
@Repository
public interface HoraireAuthentificationRepository extends JpaRepository<HoraireAuthentification, Long> {

  /*  @Query(value = "select h from HoraireAuthentification h  where (h.jour=:jour or h.jour is null )" +
            " and ((h.dateDebut<= :dateDebut) and (h.dateFin>= :dateFin or h.dateFin is null )) " +
            "and h.id  in (SELECT pt.horaire_authentifications_id " +
            "FROM ProfilActeurHoraireAuthentification pt  where pt.profil_acteurs_id=:profilId) order by h.dateDebut desc ")
    List<HoraireAuthentification> findByMultiParamsWithProfil(@Param("jour") Jour jour, @Param("dateDebut") LocalDate dateDebut, @Param("dateFin") LocalDate dateFin, @Param("profilId") Long profilId);
*/
    @Query(value = "select h from HoraireAuthentification h  where (h.jour=:jour or h.jour is null ) " +
            "and h.id  in (SELECT pt.horaire_authentifications_id " +
            "FROM ProfilActeurHoraireAuthentification pt  where pt.profil_acteurs_id=:profilId)  and h.activated=true order by h.dateDebut desc ")
    List<HoraireAuthentification> findByMultiParamsWithProfil(@Param("jour") Jour jour,@Param("profilId") Long profilId);


}
