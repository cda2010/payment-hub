package com.m2i.pti.repository;

import com.m2i.pti.domain.TableauFrais;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the TableauFrais entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TableauFraisRepository extends JpaRepository<TableauFrais, Long> {


    @Query(value = "select f from TableauFrais f  where (f.typeService.id=:typeServiceId or f.typeService.id is null ) And (f.canal.id=:canalId or f.canal.id is null ) And (f.beneficiaire.id=:clientBeneficiaireId or f.beneficiaire.id is null)" +
            " And (f.emetteur.id=:clientEmetteurId or f.emetteur.id is null) " +
            "And (f.organisationEm.id in :uoEmetteurIds or f.organisationEm.id  is null) " +
            "And (f.organisationAcq.id in :uOAcqIds or f.organisationAcq.id is null )  " +
            "and f.activated=true " +
            " order by f.poidsNum desc ")
    List<TableauFrais> findFraisByMultiParams(@Param("typeServiceId") Long typeServiceId, @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId, @Param("clientEmetteurId") Long clientEmetteurId,
                                              @Param("uoEmetteurIds") List<Long> uoEmetteurIds, @Param("uOAcqIds") List<Long> uOAcqIds);


    @Query(value = "select f from TableauFrais f  where (f.typeService.id=:typeServiceId or f.typeService.id is null ) And (f.canal.id=:canalId or f.canal.id is null ) And (f.beneficiaire.id=:clientBeneficiaireId or f.beneficiaire.id is null)" +
            " And (f.emetteur.id=:clientEmetteurId or f.emetteur.id is null) " +
            "And (f.organisationEm.id in :uoEmetteurIds or f.organisationEm.id  is null) " +
            "And (f.organisationAcq.id in :uOAcqIds or f.organisationAcq.id is null )  " +
            "and f.activated = true " +
            "and f.id  in (SELECT pt.tableau_frais_id " +
            "FROM ProfilActeurTableauFrais pt  where pt.profil_acteurs_id=:profilId)" +
            " order by f.poidsNum desc ")
    List<TableauFrais> findFraisByMultiParamsWithProfil(@Param("typeServiceId") Long typeServiceId, @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId, @Param("clientEmetteurId") Long clientEmetteurId,
                                                        @Param("uoEmetteurIds") List<Long> uoEmetteurIds, @Param("uOAcqIds") List<Long> uOAcqIds, @Param("profilId") Long profilId);


    @Query(value = "select f from TableauFrais f where (f.typeService.id=:typeServiceId or :typeServiceId is null ) And (f.canal.id=:canalId or :canalId is null ) And (f.beneficiaire.id=:clientBeneficiaireId or :clientBeneficiaireId is null)" +
            " And (f.emetteur.id=:clientEmetteurId or :clientEmetteurId is null) And (f.organisationEm.id in :uoEmetteurIds or :uoEmetteurIds  is null) And (f.organisationAcq.id in :uOAcqIds or :uOAcqIds is null) order by f.typeService.id",
            countQuery = "select count(*) from TableauFrais f where (f.typeService.id=:typeServiceId or :typeServiceId is null ) And (f.canal.id=:canalId or :canalId is null ) And (f.beneficiaire.id=:clientBeneficiaireId or :clientBeneficiaireId is null)" +
                    " And (f.emetteur.id=:clientEmetteurId or :clientEmetteurId is null) And (f.organisationEm.id in :uoEmetteurIds or :uoEmetteurIds  is null) And (f.organisationAcq.id in :uOAcqIds or :uOAcqIds is null)")

    Page<TableauFrais> findFraisByMultiCriteria(@Param("typeServiceId") Long typeServiceId, @Param("canalId") Long canalId, @Param("clientBeneficiaireId") Long clientBeneficiaireId, @Param("clientEmetteurId") Long clientEmetteurId,
                                                @Param("uoEmetteurIds") List<Long> uoEmetteurIds, @Param("uOAcqIds") List<Long> uOAcqIds, Pageable pageable);

}
