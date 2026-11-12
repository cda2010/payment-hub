package com.m2i.pti.repository;

import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.CompteAutre;
import com.m2i.pti.domain.CompteBanque;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.CategorieCompte;
import com.m2i.pti.domain.enumeration.ClasseCompte;
import com.m2i.pti.domain.enumeration.EtatCompte;
import com.m2i.pti.domain.enumeration.StatutCompte;
import com.m2i.pti.reporting.dto.XBanqueReportDTO;
import com.m2i.pti.service.dto.CompteDTO;
import com.m2i.pti.service.dto.XCompteBanqueDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

/**
 * Spring Data JPA repository for the Compte entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CompteRepository extends JpaRepository<Compte, Long> {

    @Query("SELECT coalesce(max(fs.id), 0) FROM Compte fs where fs.firstCategorieCompte='COMPTE_INTERNE' ")
    Long getMaxId();

    List<Compte> findByUniteOrganisation(UniteOrganisation uniteOrganisation);

    @Query("select c from Compte c where c.uniteOrganisation.id=:uniteOrganisationId and c.etiquette not like '%PTI%' ")
    List<Compte> findByUniteOrganisationWithoutPti(@Param("uniteOrganisationId") Long uniteOrganisationId);

    @Query(value = "select c from  Compte c where c.compteAutre is not null")
    List<Compte> findComptesAutres();

    @Query(value = "select c from  Compte c where c.client=?1")
    Compte findByClient(Client client);

    @Query(value = "select c from  Compte c where c.compteBanque=?1")
    Compte findByCompteBanque(CompteBanque compteBanque);

    @Query(value = "select c from  Compte c where c.compteAutre.id=?1")
    Compte findByCompteAutreId(Long compteAutreId);

    List<Compte> findByClientIdAndClasseCompte(Long clientId, ClasseCompte classeCompte);

    Compte findByClientAndValeurCompte(Client client, String valeurCompte);

    Compte findTopByClientAndValeurCompte(Client client, String valeurCompte);

    Compte findTopByClientAndClasseCompte(Client client, ClasseCompte classeCompte);

    List<Compte> findByClientId(Long clientId);

    @Query(value = "select c from  Compte c where c.client.id=?1 and c.statutCompte='ACTIVE'")
    List<Compte> findByClientIdAndStatut(Long clientId);
@Query("select c from Compte c, CompteBanque cb where c.compteBanque.id=cb.id and cb.alias =?1 and cb.codeFse=?2")
    Compte findCompteByAlias( String alias,String codeFse);
    
    Compte findByClientIdAndValeurCompte(Long clientId, String valeurCompte);

    List<Compte> findByUniteOrganisationAndEtiquette(UniteOrganisation uniteOrganisation, String etiquette);

    @Query(name = "Compte.findTopByUniteOrganisationAndEtiquette", nativeQuery = true)
    CompteDTO findUniteOrganisationByAndEtiquette(@Param("uoId") Long uoId, @Param("etiquette") String etiquette);
    @Query(name = "Compte.findTopByUniteOrganisationAndEtiquette", nativeQuery = true)
    CompteDTO findTop1ByUniteOrganisationAndEtiquette(@Param("uoId") Long uoId, @Param("etiquette") String etiquette);

    Compte findTop1ByUniteOrganisationAndValeurCompte(UniteOrganisation uniteOrganisation, String valeurCompte);

    Compte findTop1ByUniteOrganisationIdAndEtiquette(Long uniteOrganisationId, String etiquette);

    Page<Compte> findByUniteOrganisation(UniteOrganisation uniteOrganisation, Pageable pageable);

    @Query("select c from Compte c where c.etatCompte='OUVERT' and c.statutCompte='ACTIVE' and c.valeurCompte=?1 and c.etiquette=?2 ")
    Compte findTop1ByValeurCompteAndEtiquette(String valeur, String etiquette);

    
      @Query("select c from Compte c where  c.valeurCompte=?1 and c.etiquette=?2 ")
    Compte findTop1ByValeurCompte(String valeur, String etiquette);
    
    @Query(name = "Compte.findTopByValeurCompteAndEtiquette", nativeQuery = true)
    CompteDTO findTopByValeurCompteAndEtiquette(@Param("valeur") String valeur, @Param("etiquette") String etiquette);

    @Query(name = "Compte.findTopByValeurCompte", nativeQuery = true)
    CompteDTO findTopByValeurCompte(@Param("valeur") String valeur);

    
     @Query("select count(c) from Compte c where c.statutCompte='ACTIVE' and c.client.id is not null ")
    Long findNbreCompteActif();
    
    
    @Query(name = "Compte.findAllCompteActif", nativeQuery = true)
    List<CompteDTO> findAllCompteClientActif(@Param("limit") Long limit, @Param("offset") Long offset);

    @Query("select  c from Compte c where c.valeurCompte=?1 and c.etiquette=?2 and c.compteBanque.codeBanque=?3 and c.compteBanque.codeAgence=?4 and c.compteBanque.codeFse=?5")
    Compte findTop1ByValeurCompteAndEtiquetteAndCompteBanque(String valeur, String etiquette, String codeBanque, String codeAgence, String codeFse);
    
    
      @Query("select  c from Compte c where (c.compteBanque.codeFse=?5 and c.compteBanque.alias=?6) or (c.valeurCompte=?1 and c.etiquette=?2 and c.compteBanque.codeBanque=?3 and c.compteBanque.codeAgence=?4 and c.compteBanque.codeFse=?5)   ")
    Compte findTop1ByValeurCompteAndEtiquetteAndCompteBanque(String valeur, String etiquette, String codeBanque, String codeAgence, String codeFse, String alias);

    List<Compte> findByValeurCompte(String valeurCompte);

    @Query("select c from Compte c where (:valeur is null or :valeur='' or c.valeurCompte like ('%'||:valeur||'%')) ")
    List<Compte> findByValCptUp(@Param("valeur") String valeur);

    @Query("select c from Compte c, CompteBanque cb where c.compteBanque.id=cb.id and cb.alias =?1 and cb.codeFse=?2 and cb.alias is not null and  c.etatCompte='OUVERT' and c.statutCompte='ACTIVE' ")
    Compte findByAlias(String alias, String codeFse);
    
     @Query("select c from Compte c where  c.alias =?1 and c.alias is not null and  c.etatCompte='OUVERT' and c.statutCompte='ACTIVE' ")
    Compte findByAlias(String alias);

    Compte findTopByValeurCompteAndCategorieCompte(String valeur, CategorieCompte categorieCompte);

    Page<Compte> findByEtiquette(String etiquette, Pageable pageable);

    Page<Compte> findByCategorieCompte(CategorieCompte categorieCompte, Pageable pageable);

    Page<Compte> findByEtiquetteAndCategorieCompte(String etiquette, Pageable pageable, CategorieCompte categorieCompte);

    Page<Compte> findByEtiquetteAndUniteOrganisationId(String etiquette, Long uoId, Pageable pageable);

    Page<Compte> findByCategorieCompteAndUniteOrganisationId(CategorieCompte categorieCompte, Long uoId, Pageable pageable);

    Page<Compte> findByValeurCompteAndUniteOrganisationId(String valeurCompte, Long uoId, Pageable pageable);

    Page<Compte> findByEtiquetteAndUniteOrganisationIdAndCategorieCompteAndValeurCompte(String etiquette, Long uoId, CategorieCompte categorieCompte, String valeurCompte, Pageable pageable);
    // @Lock(LockModeType.PESSIMISTIC_READ )

    @Query("select c from Compte c where c.valeurCompte= ?1 and coalesce(c.compteAutre.codeFse,?2)=?2")
    public Compte findByCompteAutreId(String cmpte, String codeFse);

    // @Lock(LockModeType.PESSIMISTIC_READ)
    // @Query("select c.compteAutre from Compte c where c.id=?1 FOR UPDATE")
    //@Lock(LockModeType.PESSIMISTIC_READ)

    @Query("select c from Compte c where c.valeurCompte= ?1 and coalesce(c.compteAutre.codeFse,?2)=?2")
    public Compte findByValeurCompteAndCodeFse(String cmpte, String codeFse);
    
    
    
    
       @Query(name = "Compte.findCompteWithValeurAndCodeFse", nativeQuery = true)
    public CompteDTO findByValeurCompteAndCodeFseOptimise(@Param("valeurCompte")  String valeurCompte,  @Param("codeFse")  String codeFse );

    @Query("select c from Compte c where c.valeurCompte= ?1 and coalesce(c.compteAutre.codeFse,?2)=?2")
    public Compte findByValeurCompteAndCodeFse2(String cmpte, String codeFse);

    @Query("select c from Compte c where  coalesce(c.compteBanque.codeFse,?1)=?1 and c.compteBanque.alias =?2   and c.etatCompte='OUVERT'")
    public Compte findByValeurCompteBanqueAndCodeFseAndAlias(String codeFse, String alias);

    @Query("select c from Compte c where c.valeurCompte= ?1  and coalesce(c.compteBanque.codeFse,?2)=?2 and c.client.id=?3 and c.etatCompte='OUVERT'")
    public Compte findByValeurCompteBanqueAndCodeFseClient(String cmpte, String codeFse, Long idClient);

    @Query("select c from Compte c where c.valeurCompte= ?1 and coalesce(c.compteBanque.codeFse,?2)=?2")
    public Compte findByValeurCompteBanqueAndCodeFse(String cmpte, String codeFse);

    @Query("select c from Compte c where c.valeurCompte= ?1 and coalesce(c.compteAutre.codeFse,?2)=?2")
    List<Compte> findByValeurCompteAndCodeFseUo(String cmpte, String codeFse);

    public Compte findTopByClientIdAndClasseCompte(Long id, ClasseCompte classeCompte);

    @Query("select c from Compte c inner join UniteOrganisation uo on c.uniteOrganisation.id=uo.id inner join TypeUniteOrganisation t \n"
            + "on uo.typeUO.id=t.id where c.compteAutre.id  is not null and  t.codeTypeUO=:typeA ")
    List<Compte> findCompteAgent(@Param("typeA") String typeA);

    @Query("select c from Compte c inner join UniteOrganisation uo on c.uniteOrganisation.id=uo.id inner join TypeUniteOrganisation t \n"
            + "on uo.typeUO.id=t.id where c.compteAutre.id  is not null and c.valeurCompte is not null and  t.codeTypeUO=:typeA and uo.pereUO.id=:fse or uo.pereUO.id=:idFSE ")
    List<Compte> findCompteAgentAndFse(@Param("typeA") String typeA, @Param("idFSE") Long idFSE, @Param("fse") Long fse);

    @Query("select c from Compte c inner join UniteOrganisation uo on c.uniteOrganisation.id=uo.id inner join TypeUniteOrganisation t \n"
            + "on uo.typeUO.id=t.id where c.compteAutre.id  is not null and c.valeurCompte is not null and  t.codeTypeUO=:typeA and uo.pereUO.id=:fse ")
    List<Compte> findCompteAgentAndFSE(@Param("typeA") String typeA, @Param("fse") Long fse);

    @Query("select c from Compte c where c.uniteOrganisation.id=:uoId and c.valeurCompte is not null and c.etiquette like '%ACTEUR' ")
    List<Compte> findByUO(@Param("uoId") Long uoId);

    @Query("select c from Compte c where c.uniteOrganisation.id=:uoId and c.valeurCompte is not null and c.compteAutre.id  is not null and (c.etiquette='CPTEOPERATIONACTEUR') ")
    List<Compte> findByUOCPTA(@Param("uoId") Long uoId);

    @Query("select c from Compte c where c.uniteOrganisation.id=:uoId and c.valeurCompte is not null and c.compteAutre.id  is not null and c.etiquette='CPTEOPERATIONACTEUR' ")
    List<Compte> findByUOCPTAgent(@Param("uoId") Long uoId);

    @Query("select count(c.compteAutre.id) from Compte c where c.client.id=:clientId ")
    Integer getCountCptAutre(@Param("clientId") Long clientId);

    @Query("select c from Compte c where c.client.id is not null and c.valeurCompte is not null")
    List<Compte> findCompteOfClient();

    @Query("select c from Compte c where c.client.id is not null and c.client.supprime=false and c.valeurCompte is not null")
    List<Compte> findCompteOfClientActif();

    @Query("select max(c.valeurCompte) from Compte c where c.uniteOrganisation.id is not null and c.valeurCompte is not null and c.compteAutre.id is not null")
    String getMaxofValeurCompte();

    @Query("select c.valeurCompte from Compte c  where c.uniteOrganisation.id is not null and c.valeurCompte is not null ")
    List<String> getAllValeurCompte();

    @Query("select coalesce(max(c.numeroCourt),'0') from Compte c where c.client.id is not null and c.etiquette='CPTECLIENT' and c.numeroCourt is not null ")
    public String findMaxNumeroCourt();

    List<Compte> findByEtatCompte(EtatCompte etatCompte);

    List<Compte> findByEtatCompteAndClientIsNotNull(EtatCompte etatCompte);

    List<Compte> findByStatutCompte(StatutCompte statutCompte);

    List<Compte> findByStatutCompteAndClientIsNotNull(StatutCompte statutCompte);

    List<Compte> findByStatutCompteOrEtatCompteAndClientIsNotNull(StatutCompte statutCompte, EtatCompte etatCompte);

    Optional<Compte> findCompteByValeurCompteAndClientIsNotNull(String valeurCompte);

    Optional<Compte> findCompteByClientIsNotNullAndClient_PersonneId(Long personneId);

    List<Compte> findCompteByClientIsNotNullAndClient_Personne_Telephone(String valeurCompte);

    List<Compte> findByClientIsNotNullAndClientClientIdentifieIsFalseAndClientSupprimeIsFalse();

    List<Compte> findByClientIsNotNullAndClientClientIdentifieIsTrueAndClientSupprimeIsFalse();

    @Query("select c from Compte c where c.uniteOrganisation.id=?1 and c.etiquette='CPTEOPERATIONACTEURPTI'")
    Compte findCopmteOperationPTi(Long uoId);

    @Query(name = "Compte.findOneById", nativeQuery = true)
    public CompteDTO findByNativeId(@Param("id") Long id);

    @Query(name = "Compte.findBanqueOneById", nativeQuery = true)
    public CompteDTO findCompteBanqueByNativeId(@Param("id") Long id);
    
    
    
    
    @Query(name = "Compte.findCarteOneById", nativeQuery = true)
    public CompteDTO findCompteCarteByNativeId(@Param("id") Long id);

    @Query("select c from Compte c where c.valeurCompte=?1 and c.client=?2")
    public Compte findTopByValeurCompteAndClient(String numeroCompte, Client client);

    @Query(name = "Compte.findByValeurCompteAndClientId", nativeQuery = true)
    public CompteDTO findTopByValeurCompteAndClient(@Param("valeur") String valeur, @Param("clientId") Long clientId);

    @Query(name = "Compte.findByClasseAndClientId", nativeQuery = true)
    public CompteDTO findTopByClientIdAndClasseCompte(@Param("clientId") Long clientId, @Param("classe") String classe);

    @Query(name = "Compte.findByClientIdOptimise", nativeQuery = true)
    List<CompteDTO> findByClientIdOptimise(@Param("clientId") Long clientId);

    @Query(name = "Compte.findCompteByClientIdAndClasseCompte", nativeQuery = true)
    CompteDTO findByClientIdAndClasseCompteToReport(@Param("clientId") Long clientId, @Param("classeCompte") String classeCompte);

    @Query(name = "Compte.findByCompteAutreId", nativeQuery = true)
    CompteDTO findByCompteAutreIdOptimise(@Param("compteAutreId") Long compteAutreId);

    @Query(name = "Compte.findCompteByIdReport", nativeQuery = true)
    CompteDTO findCompteByIdReport(@Param("compteId") Long compteId);

    @Query(value = "select c.compte_autre_id from compte c where c.compte_pincipale_id=:cpId", nativeQuery = true)
    Long getCompteAutreIdByComptePrincipaleId(@Param("cpId") Long id);

    @Query(name = "Compte.findByUniteOrganisationOptimise", nativeQuery = true)
    List<CompteDTO> findByUniteOrganisationOptimise(@Param("uoId") Long uoId);

    @Query(name = "Compte.findByUniteOrganisationWithoutPtiOptimise", nativeQuery = true)
    List<CompteDTO> findByUniteOrganisationWithoutPtiOptimise(@Param("uoId") Long uoId);

    @Query(name = "Compte.findValeurCompteAndClientIdPeriode", nativeQuery = true)
    List<CompteDTO> findValeurCompteAndClientIdPeriode(@Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin);

    @Query(name = "Compte.findValeurCompteAndClientIdActifIn3months", nativeQuery = true)
    List<CompteDTO> findValeurCompteAndClientIdActifIn3months(@Param("taille") Long taille, @Param("page") Long page, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin);

    @Query(name = "Compte.findValeurCompteAndCategorieCompteAndClientIdPeriode", nativeQuery = true)
    List<CompteDTO> findValeurCompteAndCategorieCompteAndClientIdPeriode(@Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin);

    @Query(name = "Compte.findValeurCompteAndCategorieCompteAndClientIdPeriode1", nativeQuery = true)
    List<CompteDTO> findValeurCompteAndCategorieCompteAndClientIdPeriode1(@Param("dateDebut") ZonedDateTime dateDebut);

    @Query(name = "Compte.findValeurCompteAndCategorieCompteAndClientIdPeriode2", nativeQuery = true)
    List<CompteDTO> findValeurCompteAndCategorieCompteAndClientIdPeriode2(@Param("dateFin") ZonedDateTime dateFin);

    @Query(name = "Compte.findCptOpAndCptComByUoIdOpt", nativeQuery = true)
    List<CompteDTO> findCptOpAndCptComByUoIdOpt(@Param("uoId") Long uoId, @Param("etiquettes") List<String> etiquettes);

    @Query(nativeQuery = true, value = "Select  count(c.id) from compte as c inner join client as cli on c.client_id = cli.id inner join compte_autre ca on c.compte_autre_id=ca.id  and ca.last_modified_date between :debut and :fin ")
    Integer countValeurCompteAndClientIdActifIn3months(@Param("debut") ZonedDateTime debut, @Param("fin") ZonedDateTime fin);

    @Query(nativeQuery = true, value = "select coalesce(sum(ca.solde),0) from compte cpt,compte_autre ca where cpt.compte_autre_id=ca.id and cpt.client_id is not null and "
            + " cpt.valeur_compte is not null and cpt.etiquette='CPTECLIENT' and ca.code_fse=:codeFse ")
    BigDecimal getSoldeTotalClient(@Param("codeFse") String codeFse);

    @Query(nativeQuery = true, value = "select coalesce(ca.solde,0) from compte cpt,compte_autre ca where cpt.compte_autre_id=ca.id and cpt.unite_organisation_id is not null and "
            + " cpt.valeur_compte is not null and cpt.etiquette=:etiquette and cpt.unite_organisation_id=:uoId and ca.code_fse=:codeFse ")
    BigDecimal getSoldeTotalDistribution(@Param("codeFse") String codeFse, @Param("etiquette") String etiquette, @Param("uoId") Long uoId);

    @Query(name = "Compte.findAllCompteBanqueActif", nativeQuery = true)
    List<CompteDTO> findAllCompteBanqueActif(@Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin, @Param("statut") String statut);

    @Query(name = "Compte.findAllCompteBanqueDesactive", nativeQuery = true)
    List<CompteDTO> findAllCompteBanqueDesactive(@Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin, @Param("statut") String statut);

    @Query(name = "Compte.findAllCompteBanque", nativeQuery = true)
    List<CompteDTO> findAllCompteBanque(@Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin);

    @Query(nativeQuery = true, value = "select sum(ca.solde) from compte cpt,compte_autre ca ,unite_organisation uo, type_unite_organisation tporgan where "
            + " cpt.compte_autre_id=ca.id and  cpt.valeur_compte is not null and cpt.unite_organisation_id is not null and"
            + " cpt.valeur_compte is not null and cpt.etiquette=:etiquette and ca.code_fse= CAST(:codeFse as text)"
            + " and typeuo_id= tporgan.id and code_type_uo = CAST(:typeUo As text)"
            + " and cpt.unite_organisation_id= uo.id and uo.est_siege=false")
    BigDecimal getSoldeTotalAgent(@Param("typeUo") String typeUo, @Param("codeFse") String codeFse, @Param("etiquette") String etiquette);

    @Query(nativeQuery = true, value = "Select  count(c.id) from compte as c inner join unite_organisation  as u on c.unite_organisation_id = u.id inner join compte_autre ca on c.compte_autre_id=ca.id and u.typeuo_id=:typeUoId  and ca.last_modified_date between :debut and :fin ")
    Integer countValeurCompteAndAgentIdActifIn3months(@Param("typeUoId") Long typeUoId, @Param("debut") ZonedDateTime debut, @Param("fin") ZonedDateTime fin);

    @Query(name = "Compte.findValeurCompteAndAgentIdActifIn3months", nativeQuery = true)
    List<CompteDTO> findValeurCompteAndAgentIdActifIn3months(@Param("typeUoId") Long typeUoId, @Param("taille") Long taille, @Param("page") Long page, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin);

}
