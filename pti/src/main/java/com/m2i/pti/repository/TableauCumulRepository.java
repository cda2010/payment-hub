package com.m2i.pti.repository;

import com.m2i.pti.domain.TableauCumul;
import com.m2i.pti.domain.TableauFrais;
import com.m2i.pti.domain.enumeration.Temps;
import com.m2i.pti.domain.enumeration.TypeRegle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the TableauCumul entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TableauCumulRepository extends JpaRepository<TableauCumul, Long> {

    @Query(value = "select f from TableauCumul f where (f.typeRegle = :typeRegle or f.typeRegle is null ) and "
            + "(f.typeService.id = :typeServiceId or f.typeService.id is null ) and"
            + " (f.canal.id = :canalId or f.canal.id is null ) and (f.beneficiaire.id = :clientBeneficiaireId or f.beneficiaire.id is null)"
            + " AND (f.emetteur.id = :clientEmetteurId or f.emetteur.id is null) "
            + " AND (f.temps = :temps or f.temps is null) "
            + " AND (f.organisationEm.id = :uoEmetteurId or f.organisationEm.id  is null) "
            + " AND (f.organisationAcq.id = :uoAcqId or f.organisationAcq.id is null ) "
            + " AND f.id  in (SELECT pt.tableau_cumuls_id "
            + " FROM ProfilActeurTableauCumul pt  where pt.profil_acteurs_id = :profilId)"
            + " AND f.activated = true order by f.poidsNum desc ")
    List<TableauCumul> findFraisByMultiParamsWithProfil(@Param("typeRegle") TypeRegle typeRegle, @Param("typeServiceId") Long typeServiceId, @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId, @Param("clientEmetteurId") Long clientEmetteurId,
            @Param("uoEmetteurId") Long uoEmetteurId, @Param("uoAcqId") Long uoAcqId, @Param("temps") Temps temps, @Param("profilId") Long profilId);

    @Query("select f from TableauCumul f where (f.typeRegle = :typeRegle or f.typeRegle is null ) and (f.typeService.id = :typeServiceId or f.typeService.id is null ) And (f.canal.id = :canalId or f.canal.id is null ) And (f.beneficiaire.id = :clientBeneficiaireId or f.beneficiaire.id is null)"
            + "And (f.emetteur.id = :clientEmetteurId or f.emetteur.id is null) "
            + "And (f.temps = :temps or f.temps is null) "
            + "And (f.organisationEm.id = :uoEmetteurId or f.organisationEm.id  is null) "
            + "And (f.organisationAcq.id  = :uoAcqId or f.organisationAcq.id is null ) "
            + "And f.activated= true order by f.poidsNum desc ")
    List<TableauCumul> findFraisByMultiParams(@Param("typeRegle") TypeRegle typeRegle, @Param("typeServiceId") Long typeServiceId, @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId, @Param("clientEmetteurId") Long clientEmetteurId,
            @Param("uoEmetteurId") Long uoEmetteurId, @Param("uoAcqId") Long uoAcqId, @Param("temps") Temps temps);

    @Query(value = "select f from TableauCumul f where (f.typeService.id = :typeServiceId or :typeServiceId is null ) And (f.canal.id = :canalId or :canalId is null ) And (f.beneficiaire.id = :clientBeneficiaireId or :clientBeneficiaireId is null)"
            + " And (f.emetteur.id = :clientEmetteurId or :clientEmetteurId is null) And (f.organisationEm.id in :uoEmetteurIds or :uoEmetteurIds  is null) And (f.organisationAcq.id in :uOAcqIds or :uOAcqIds is null) order by f.typeService.id",
            countQuery = "select count(*) from TableauFrais f where (f.typeService.id = :typeServiceId or :typeServiceId is null ) And (f.canal.id = :canalId or :canalId is null ) And (f.beneficiaire.id = :clientBeneficiaireId or :clientBeneficiaireId is null)"
            + " And (f.emetteur.id = :clientEmetteurId or :clientEmetteurId is null) And (f.organisationEm.id in :uoEmetteurIds or :uoEmetteurIds  is null) And (f.organisationAcq.id in :uOAcqIds or :uOAcqIds is null) GROUP  by f.typeService.id order by f.typeService.id")
    Page<TableauCumul> findFraisByMultiCriteria(@Param("typeServiceId") Long typeServiceId, @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId, @Param("clientEmetteurId") Long clientEmetteurId,
            @Param("uoEmetteurIds") List<Long> uoEmetteurIds, @Param("uOAcqIds") List<Long> uOAcqIds, Pageable pageable);

    List<TableauCumul> findByTypeRegle(TypeRegle typeRegle);

}
