package com.m2i.pti.repository;

import com.m2i.pti.domain.PointVente;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.service.dto.PointVenteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the PointVente entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PointVenteRepository extends JpaRepository<PointVente, Long> {

    @Query("SELECT coalesce(max(pv.id), 0) FROM PointVente pv ")
    Long getMaxId();

    Page<PointVente> findByUniteOrganisation(UniteOrganisation uniteOrganisation, Pageable pageable);

    List<PointVente> findByUniteOrganisation(UniteOrganisation uniteOrganisation);

    List<PointVente> findByUniteOrganisationId(Long uId);

    //@Query("SELECT p FROM PointVente p LEFT JOIN FETCH p.uniteOrganisation where  p.codePointVente=?1  ")
    @Query(name = "PointVente.findByCode", nativeQuery = true)
    PointVenteDTO findByPointVente(@Param("codePv") String codePv);

    @Query(name = "PointVente.findById", nativeQuery = true)
    PointVenteDTO findDtoById(@Param("id") Long id);

    @Query("SELECT p from PointVente p where p.codePointVente=?1 ")
    Optional<PointVente> findByCodePointVente(String code);

    @Query(value = "SELECT * from point_vente p where p.code_point_vente=?1 ", nativeQuery = true)
    Optional<PointVente> findTopByCodePointVente(String code);

    Optional<PointVente> findByLibellePointVenteAndUniteOrganisationId(String libelle, Long uoId);

    List<PointVente> findAll();

    @Query("select p.id from PointVente p where p.uniteOrganisation.id=?1 ")
    List<Long> findPvIdsOfUniteOrganisation(Long uniteOrganisationId);

    @Query(name = "PointVente.findByUoId", nativeQuery = true)
    List<PointVenteDTO> findByUniteOrganisationIdOptimise(@Param("uoId") Long uoId);

    @Query("select p.id from PointVente p ")
    List<Long> findAllIdOptimise();

    @Query(name = "PointVente.findNonActivePointVenteDTO", nativeQuery = true)
    List<PointVenteDTO> findNonActivePointVenteDTO();

    @Query(value = "Select p.id from point_vente p inner join unite_organisation u on p.unite_organisation_id=u.id where p.id IS NOT NULL and p.id not in"
            + "(select distinct(point_vente_id) from operation  where point_vente_id is not null and created_date  between cast(:debut as timestamp) and cast(:fin as timestamp ) ) and u.supprime=false", nativeQuery = true)
    List<BigInteger> findAllPVId(@Param("debut") ZonedDateTime debut, @Param("fin") ZonedDateTime fin);

    @Query(name = "PointVente.findPointVenteDTO", nativeQuery = true)
    PointVenteDTO findPointVenteDTO(@Param("id") BigInteger id);

    @Query(name = "PointVente.findByUoIdAndCord", nativeQuery = true)
    List<PointVenteDTO> findByUoIdAndCordOptimise(@Param("longitudeMin") String longitudeMin, @Param("longitudeMax") String longitudeMax, @Param("latitudeMin") String latitudeMin, @Param("latitudeMax") String latitudeMax);

}
