package com.m2i.pti.repository;

import com.m2i.pti.domain.TypeUniteOrganisation;
import com.m2i.pti.domain.UniteOrganisation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Spring Data JPA repository for the UniteOrganisation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UniteOrganisationRepository extends JpaRepository<UniteOrganisation, Long> {
 


    @Query("SELECT coalesce(max(uo.id), 0) FROM UniteOrganisation uo where uo.supprime=false ")
    Long getMaxId();

    @Query("SELECT COUNT(*) FROM UniteOrganisation org where org.supprime=false  and org.pereUO.id=:pereId")
    Integer getNbreElement(@Param("pereId") Long pereId);

    @Query("SELECT COUNT(*) FROM UniteOrganisation org where org.supprime=false  and org.pereUO.id=null")
    Integer getNbreElementWherePereIdIsNull();

    Integer countByTypeUOAndSupprimeIsFalse(TypeUniteOrganisation type);

    List<UniteOrganisation> findByPereUONullAndSupprimeIsFalse();

    UniteOrganisation findByCodeUO(String codeUo);

    List<UniteOrganisation> findByPereUO(UniteOrganisation pereUO);
    @Query("select uo from UniteOrganisation uo where uo.codeUO=?1 and uo.supprime=false")
     UniteOrganisation findByCodeUOAndSupprimeIsFalse(String codeUo);
    @Query(name = "UniteOrganisation.findByCodeUOAndSupprimeIsFalse", nativeQuery = true)
    UniteOrganisationDTO findByCodeUOAndSupprimeIsFalse(@Param("codeUo") String codeUo  ,@Param("issupp") boolean    issupp);
    @Query(name = "UniteOrganisation.findByCodeFseAndSupprimeIsFalse", nativeQuery = true)
    UniteOrganisationDTO findCodeFseAndSupprimeIsFalse(@Param("codefse") String codefse );
    
      @Query(name = "UniteOrganisation.findByUoFse", nativeQuery = true)
   List<UniteOrganisationDTO>findByFseId(@Param("uofseId") Long uofseId );
 
    
    @Query(name = "UniteOrganisation.findOne", nativeQuery = true)
    UniteOrganisationDTO findDtoById(@Param("id") Long id );
    
    
    UniteOrganisation findByCodeUOAndSupprimeIsFalseAndEstActifIsTrue(String codeUo);

    List<UniteOrganisation> findByTypeUOAndSupprimeIsFalseOrderByLibelleUniteOrganisation(TypeUniteOrganisation typeUO);

    List<UniteOrganisation> findByTypeUOAndSupprimeIsFalse(TypeUniteOrganisation typeUO);




    UniteOrganisation findByTypeUOAndCodeFseAndSupprimeIsFalseOrderByLibelleUniteOrganisation(TypeUniteOrganisation typeUO, String codeFse);

    UniteOrganisation findByTypeUOAndCodeFseAndSupprimeIsFalse(TypeUniteOrganisation typeUO, String codeFse);
    @Query("select p from UniteOrganisation p where  p.codeFse= ?1 and p.supprime=false ")
    UniteOrganisation findByCodeFseAndSupprimeIsFalse(String codeFse);

    List<UniteOrganisation> findByPereUOAndSupprimeIsFalse(UniteOrganisation pereUO);

    @Query(name="UniteOrganisation.findByPereUOAndSupprimeIsFalseOptimiser",nativeQuery = true)
    List<UniteOrganisationDTO> findByPereUOAndSupprimeIsFalseOptimiser(@Param("pereUOID") Long pereUOId);

    @Query(name = "UniteOrganisation.findChildrenPage", nativeQuery = true)
    List<UniteOrganisationDTO> findChildrenPage(@Param("pereUOID") Long pereUOId, @Param("taille") Long taille, @Param("page") Long page);

    @Query("select count(u.id) from UniteOrganisation u where u.pereUO.id = :pereId and (u.supprime is null or u.supprime = false)")
    long countChildren(@Param("pereId") Long pereId);

    List<UniteOrganisation> findByPereUOAndVilleIdAndSupprimeIsFalse(UniteOrganisation pereUO, Long villeId);
    
     @Query("select p from UniteOrganisation p where  p.pereUO= ?1 and p.supprime=false ")
    List<UniteOrganisation>  findFilsOfOrganisation(Long  uoId);

    List<UniteOrganisation> findByPereUOAndTypeUOAndSupprimeIsFalse(UniteOrganisation pereId, TypeUniteOrganisation type);

    UniteOrganisation findByPereUOAndTypeUOAndEstFilialeAndSupprimeIsFalse(UniteOrganisation pereId, TypeUniteOrganisation type, Boolean estFiliale);

    @Query("select p from UniteOrganisation p where p.pereUO=?1    and p.typeUO= ?2 and p.estSiege=?3 and p.supprime=false")
    List<UniteOrganisation> findByPereUOAndTypeUOAndEstSiegeAndSupprimeIsFalse(UniteOrganisation pereUO, TypeUniteOrganisation typeUO, Boolean estSiege);

    @Query(value = "select uo.libelle_unite_organisation from unite_organisation uo where uo.id= CAST(CAST (:uoId as TEXT) as BIGINT )  and uo.supprime=false",nativeQuery = true)
    String getLibelleUniteOrganisation(@Param("uoId") Long uoId);


    //   UniteOrganisation findByPereUOAndTypeUOAndEstSiege(UniteOrganisation pereUO,TypeUniteOrganisation typeUO,Boolean estSiege);
    @Query("select p from UniteOrganisation p where p.codeUO=?1    and p.telephone= ?2")
    public UniteOrganisation findByCodeAndTelephone(String codeUo, String telephone);

    Page<UniteOrganisation> findAllBySupprimeIsFalse(Pageable pageable);

    @Query("select uo from UniteOrganisation uo where uo.typeUO.codeTypeUO !='FSE'"
            + "and uo.typeUO.codeTypeUO != 'PAYS' and uo.typeUO.codeTypeUO !='ZONE' and uo.supprime=false "
            + "and (:libelleUniteOrganisation is null or :libelleUniteOrganisation='' or uo.libelleUniteOrganisation like:libelleUniteOrganisation)"
            + "and (:codeUO is null or :codeUO='' or uo.codeUO like:codeUO )"
            + "and (:typeUO is null or uo.typeUO.codeTypeUO = :typeUO)"
            + "and (:numeroOrdre is null or uo.numeroOrdre=:numeroOrdre)"
            + "and (:groupeId is null or uo.groupeProfilActeur.id=:groupeId)"
            + "and (:villeId is null or uo.ville.id=:villeId)"
            + "and (:adresse is null or :adresse='' or uo.adresse like :adresse)")
    Page<UniteOrganisation> findAllForViewPaginatedNotIgnoreCase(@Param("libelleUniteOrganisation") String libelleUniteOrganisation,
            @Param("codeUO") String codeUO,
            @Param("numeroOrdre") Integer numeroOrdre,
            @Param("typeUO") String typeUO,
            @Param("groupeId") Long groupeId,
            @Param("villeId") Long villeId,
            @Param("adresse") String adresse,
            Pageable pageable);

    @Query("select uo from UniteOrganisation uo where uo.typeUO.codeTypeUO !='FSE'"
            + "and uo.typeUO.codeTypeUO != 'PAYS' and uo.typeUO.codeTypeUO !='ZONE' and ( uo.supprime is null or uo.supprime=false )"
            + "and (:libelleUniteOrganisation is null or :libelleUniteOrganisation='' or UPPER(uo.libelleUniteOrganisation)  like UPPER('%'||:libelleUniteOrganisation||'%'))"
            + "and ( :codeUO is null or :codeUO='' or UPPER(uo.codeUO) like UPPER('%'||:codeUO||'%'))"
            + "and (:typeUO is null or  uo.typeUO.codeTypeUO =:typeUO)"
            + "and (:numeroOrdre is null or uo.numeroOrdre=:numeroOrdre)"
            + "and (:groupeId is null or uo.groupeProfilActeur.id=:groupeId)"
            + "and (:villeId is null or uo.ville.id=:villeId)"
            + "and (:adresse is null or :adresse='' or UPPER(uo.adresse)  like UPPER('%'||:adresse||'%') )"
    )
    Page<UniteOrganisation> findAllForViewPaginated(@Param("libelleUniteOrganisation") String libelleUniteOrganisation,
            @Param("codeUO") String codeUO,
            @Param("numeroOrdre") Integer numeroOrdre,
            @Param("typeUO") String typeUO,
            @Param("groupeId") Long groupeId,
            @Param("villeId") Long villeId,
            @Param("adresse") String adresse,
            Pageable pageable);

    @Query("select uo from UniteOrganisation uo where uo.typeUO.codeTypeUO !='FSE'"
            + "and uo.typeUO.codeTypeUO != 'PAYS' and uo.typeUO.codeTypeUO !='ZONE'"
            + "and (uo.libelleUniteOrganisation like:libelleUniteOrganisation or :libelleUniteOrganisation is null)"
            + "and (uo.codeUO like:codeUO or :codeUO is null)"
            + "and (uo.typeUO.codeTypeUO like :typeUO or :typeUO is null)"
            + "and (uo.numeroOrdre=:numeroOrdre or :numeroOrdre is null)")
    List<UniteOrganisation> findAllForViewPaginated1(@Param("libelleUniteOrganisation") String libelleUniteOrganisation,
            @Param("codeUO") String codeUO,
            @Param("numeroOrdre") Integer numeroOrdre,
            @Param("typeUO") String typeUO
    );

    @Query("select uo from UniteOrganisation uo where uo.typeUO.codeTypeUO !='FSE'"
            + "and uo.typeUO.codeTypeUO != 'PAYS' and uo.typeUO.codeTypeUO !='ZONE' and (uo.supprime is null or uo.supprime=false )")
    List<UniteOrganisation> findForViewPaginated();

    @Query("select uo from UniteOrganisation uo where uo.typeUO.codeTypeUO=:typeUO "
            + "and (uo.createdDate between cast(:dateDebut as timestamp) and cast(:dateFin as timestamp) )"
            + " and (uo.supprime is null or uo.supprime=false )")
    List<UniteOrganisation> findForViewPaginatedIntervalDate(@Param("dateDebut") ZonedDateTime dateDebut,
            @Param("dateFin") ZonedDateTime dateFin, @Param("typeUO") String typeUO);

    @Query("select uo from UniteOrganisation uo where uo.typeUO.codeTypeUO=:typeUO "
            + " and (uo.supprime is null or uo.supprime=false )")
    List<UniteOrganisation> findAllByTypeUo(@Param("typeUO") String typeUO);

    @Query("select uo from UniteOrganisation uo inner join TypeUniteOrganisation t on uo.typeUO.id=t.id where uo.pereUO.id=:fse and t.codeTypeUO=:typeUo")
    List<UniteOrganisation> findByType(@Param("fse") Long fse, @Param("typeUo") String typeUo);

    @Query("select uo from UniteOrganisation uo inner join TypeUniteOrganisation t on uo.typeUO.id=t.id where uo.pereUO.id=:fse and t.codeTypeUO=:typeUo"
            + " or uo.pereUO.id=:idFSE")
    List<UniteOrganisation> findByType1(@Param("fse") Long fse, @Param("idFSE") Long idFSE, @Param("typeUo") String typeUo);

    @Query("select uo.id from UniteOrganisation uo where uo.libelleUniteOrganisation='Agence principale CORIS MONEY' ")
    Long findFSE();

    @Query("select uo.libelleUniteOrganisation from UniteOrganisation uo where uo.id=:fse")
    String findNomFSE(@Param("fse") Long fse);

    @Query("select uo from  UniteOrganisation uo inner join TypeUniteOrganisation t on uo.typeUO.id=t.id where t.codeTypeUO='MASTER' or\n"
            + "t.codeTypeUO='SOUS_DISTRIBUTEUR'")
    List<UniteOrganisation> findUOrganisation();

    @Query("select uo from UniteOrganisation uo inner join TypeUniteOrganisation typeuo on uo.typeUO.id=typeuo.id where uo.typeUO.codeTypeUO !='FSE'"
            + "and uo.typeUO.codeTypeUO != 'PAYS' and uo.typeUO.codeTypeUO !='ZONE' and uo.supprime=false "
            + "and uo.typeUO.codeTypeUO =:agent"
    )
    List<UniteOrganisation> findAllForViewPaginatedSolde(@Param("agent") String agent);

    // importation
    Optional<UniteOrganisation> findByTagIdentityRef(Integer identit);

    List<UniteOrganisation> findByTagIdentityRefIsNotNull();

    List<UniteOrganisation> findByMarchandIdIsNotNull();

    List<UniteOrganisation> findByAgentIdIsNotNullOrderByIdDesc();


    @Query("select uo from UniteOrganisation uo, PointVente pv where uo.id=pv.uniteOrganisation.id and pv.codePointVente=?1")
    public UniteOrganisation findByCodePv(String codePv);

    @Query("select u from UniteOrganisation u where u.serviceProfil.id=?1")
    List<UniteOrganisation> findByProfilService(Long profilServiceId);

    List<UniteOrganisation> findAllBySupprimeIsFalse();

    @Query("select u from UniteOrganisation u where u.id in (:agentIds)")
    List<UniteOrganisation> findAllInList(@Param("agentIds") List<Long> agentIds);

    @Query("select u.id from UniteOrganisation u ")
    List<Integer> getUoListId(@Param("pereuoId") Long pereuoID);

    @Query(name = "UniteOrganisation.findAllOptimise",nativeQuery = true)
    List<UniteOrganisationDTO> findForViewPaginatedOptimise();

    @Query(name="UniteOrganisation.findAllOptimiseByTypeUo",nativeQuery = true)
    List<UniteOrganisationDTO> findAllOptimiseByTypeUo(@Param("typeUoId") Long typeUoId);

    @Query(name = "UniteOrganisation.findByTypeUo", nativeQuery = true)
    List<UniteOrganisationDTO> findByTypeUoOptimise(@Param("id") Long id );

    @Query(name = "UniteOrganisation.findForViewPaginatedOptimise",nativeQuery = true)
    List<UniteOrganisationDTO> findAllForViewPaginatedOptimise(@Param("libelleUniteOrganisation") String libelleUniteOrganisation,
                                                               @Param("codeUO") String codeUO,
                                                               @Param("numeroOrdre") Integer numeroOrdre,
                                                               @Param("typeUO") String typeUO,
                                                               @Param("groupeId") Long groupeId,
                                                               @Param("villeId") Long villeId,
                                                               @Param("adresse") String adresse,
                                                               @Param("taille") Long taille, @Param("page") Long page);



    @Query("select count(uo.id) from UniteOrganisation uo where uo.typeUO.codeTypeUO !='FSE'"
            + "and uo.typeUO.codeTypeUO != 'PAYS' and uo.typeUO.codeTypeUO !='ZONE' and ( uo.supprime is null or uo.supprime=false )"
            + "and (:libelleUniteOrganisation is null or :libelleUniteOrganisation='' or UPPER(uo.libelleUniteOrganisation)  like UPPER('%'||:libelleUniteOrganisation||'%'))"
            + "and ( :codeUO is null or :codeUO='' or UPPER(uo.codeUO) like UPPER('%'||:codeUO||'%'))"
            + "and (:typeUO is null or  uo.typeUO.codeTypeUO =:typeUO)"
            + "and (:numeroOrdre is null or uo.numeroOrdre=:numeroOrdre)"
            + "and (:groupeId is null or uo.groupeProfilActeur.id=:groupeId)"
            + "and (:villeId is null or uo.ville.id=:villeId)"
            + "and (:adresse is null or :adresse='' or UPPER(uo.adresse)  like UPPER('%'||:adresse||'%') )"
    )
    Integer countFindForViewPaginated(@Param("libelleUniteOrganisation") String libelleUniteOrganisation,
                                      @Param("codeUO") String codeUO,
                                      @Param("numeroOrdre") Integer numeroOrdre,
                                      @Param("typeUO") String typeUO,
                                      @Param("groupeId") Long groupeId,
                                      @Param("villeId") Long villeId,
                                      @Param("adresse") String adresse);

    @Query(name = "UniteOrganisation.findByPereUo", nativeQuery = true)
    List<UniteOrganisationDTO> findByPereUOOptimise(@Param("id") Long id );

    @Query(name = "UniteOrganisation.findByPereUoAndType", nativeQuery = true)
    List<UniteOrganisationDTO> findByPereUoAndType(@Param("id") Long id, @Param("typeId") Long typeId  );
    
    @Query(name = "UniteOrganisation.findByTypeUOAndSupprimeIsFalseOptimise", nativeQuery = true)
    List<UniteOrganisationDTO> findByTypeUOAndSupprimeIsFalseOptimise(@Param("typeUoId") Long typeUoId );


    @Query(value = "select uo.libelle_unite_organisation from unite_organisation uo where uo.code_fse=:codeFse",nativeQuery = true)
    String getLibelleUniteOrganisationByCodeFse(@Param("codeFse") String codeFse);

    @Query(name = "UniteOrganisation.findForViewListOptimise",nativeQuery = true)
    List<UniteOrganisationDTO> findForViewListOptimise();

    @Query("select u from UniteOrganisation u where u.id=?1")
    UniteOrganisation findByUoId(Long id);

    Boolean existsByMarchandId(Integer merId);
    Boolean existsByAgentId(Integer agtId);
    UniteOrganisation findByAgentId(Integer parseInt);

   @Query(name = "UniteOrganisation.findAllOptimiseNew",nativeQuery = true)
   List<UniteOrganisationDTO> findForViewPaginatedOptimiseNew();

   List<UniteOrganisation> findByMarchandIdIsNotNullOrderByIdDesc();

   List<UniteOrganisation> findByTypeUOAndSupprimeIsFalseOrderByIdDesc(TypeUniteOrganisation typeUO);

   @Query(name = "UniteOrganisation.findAgenceDigitaleOptimise", nativeQuery = true)
   List<UniteOrganisationDTO> findAgenceDigitaleOptimise(@Param("typeUoId") Long typeUoId );

   @Query(name = "UniteOrganisation.findAgentOptimise", nativeQuery = true)
   List<UniteOrganisationDTO> findAgentOptimise(@Param("typeUoId") List<Long> typeUoId );

   @Query("select u from UniteOrganisation u where u.typeUO.id=?1 and u.supprime=false and u.estMigrer=true ")
   List<UniteOrganisation> findByTypeUOAndSupprimeIsFalseOrderByIdDesc(Long typeUoId);

   @Query(name="UniteOrganisation.findAllByDate",nativeQuery = true)
   List<UniteOrganisationDTO> findAllPointVenteByDate(@Param("pvIds") List<Long> pvIds);

   @Query(value = "select distinct(point_vente_id) from operation where point_vente_id is not null and date_operation between :debut and :fin ",nativeQuery = true)
   List<Long> pointVenteIdsByDate(@Param("debut") ZonedDateTime debut,@Param("fin") ZonedDateTime fin);

    @Query(name = "UniteOrganisation.findAllOptimiseForReport",nativeQuery = true)
    List<UniteOrganisationDTO> findAllOptimiseForReport(@Param("typeUO") String typeUO,@Param("debut") ZonedDateTime debut,
                                                               @Param("taille") Long taille, @Param("page") Long page);
    
    List<UniteOrganisation> findByLibelleUniteOrganisationAndTypeUOOrderByIdDesc(String libelle,TypeUniteOrganisation typeUo);

}
