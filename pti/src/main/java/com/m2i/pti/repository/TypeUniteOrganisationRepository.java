package com.m2i.pti.repository;

import com.m2i.pti.domain.TypeUniteOrganisation;
import com.m2i.pti.service.dto.TypeUniteOrganisationDTO;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;


/**
 * Spring Data JPA repository for the TypeUniteOrganisation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TypeUniteOrganisationRepository extends JpaRepository<TypeUniteOrganisation, Long> {
    TypeUniteOrganisation findByLibelleTypeUO(String type);

    TypeUniteOrganisation findByNiveau(Integer niveau);

    @Query("select t from TypeUniteOrganisation t where t.niveau=?1 order by t.id asc ")
    List<TypeUniteOrganisation> findListByNiveau(Integer niveau);

    @Query("SELECT typ FROM TypeUniteOrganisation typ where typ.niveau != -1")
    List<TypeUniteOrganisation> findByNiveauDifUR();

    @Query("SELECT typ FROM TypeUniteOrganisation typ where typ.niveau>=2")
    List<TypeUniteOrganisation> findByNiveauActeur();
 @Query("SELECT typ FROM TypeUniteOrganisation typ where typ.codeTypeUO=?1")
    Optional<TypeUniteOrganisation> findByCodeTypeUO(String code);

    @Query(name = "TypeUniteOrganisation.findByNiveau", nativeQuery = true)
    List<TypeUniteOrganisationDTO> findByNiveau();


    @Query(name = "TypeUniteOrganisation.findOne", nativeQuery = true)
    TypeUniteOrganisationDTO findDtoById(@Param("id") Long  id);
    
    
    @Query(name = "TypeUniteOrganisation.findCodetype", nativeQuery = true)
    TypeUniteOrganisationDTO findCodetype(@Param("code") String  code);

    @Query(name = "TypeUniteOrganisation.findLibelleTypeUO", nativeQuery = true)
    TypeUniteOrganisationDTO findLibelleTypeUO(@Param("libelle") String  libelle);

    @Query(name = "TypeUniteOrganisation.findAllTUO", nativeQuery = true)
    List<TypeUniteOrganisationDTO> findAllTUO(@Param("taille") Long taille, @Param("page") Long page);

    @Query(value = "select authority_name as name from type_unite_organisation_role where type_unite_organisation_id = CAST(CAST (:id AS TEXT) AS BIGINT ) ", nativeQuery = true)
    List<String> findRoleByTUOId(@Param("id") Long  id);

    @Query(name = "TypeUniteOrganisation.findListByNiveauOptimise", nativeQuery = true)
    List<TypeUniteOrganisationDTO> findListByNiveauOptimise(@Param("niveau") Integer niveau);


    @Query(name = "TypeUniteOrganisation.findAllLibelleTypeUO", nativeQuery = true)
    List<TypeUniteOrganisationDTO> findAllLibelleTypeUO(@Param("libelle") List<String>  libelle);

    @Query(value = "select t.id from type_unite_organisation t where t.libelle_type_uo  in (:libelle) ", nativeQuery = true)
    List<Long> findAllTypeUOId(@Param("libelle") List<String>  libelle);
    @Query(value = "select * from type_unite_organisation tuo inner join unite_organisation uo on tuo.id=uo.typeuo_id where uo.id=:id", nativeQuery = true)
    TypeUniteOrganisation findTypeUniteOrganisationByUo(@Param("id") Long uoid) ;
}
