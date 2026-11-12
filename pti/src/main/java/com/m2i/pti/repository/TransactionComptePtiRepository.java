package com.m2i.pti.repository;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.EtatValidite;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.domain.enumeration.TypeTransactionCompte;
import com.m2i.pti.service.dto.ClientDTO;
import com.m2i.pti.service.dto.CompteDTO;
import com.m2i.pti.service.dto.TransactionComptePtiDTO;
import com.m2i.pti.service.dto.TransactionComptePtiOryx;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for the TransactionComptePti entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TransactionComptePtiRepository extends JpaRepository<TransactionComptePti, Long> {

    Page<TransactionComptePti> findByCompteAutreOrderByIdDesc(CompteAutre compteAutre, Pageable pageable);

    List<TransactionComptePti> findByCompteAutreOrderByIdDesc(CompteAutre compteAutre);

    List<TransactionComptePti> findByCompteAutreId(Long compteId);

    Integer countByCompteAutreId(Long compteAutreId);

    Page<TransactionComptePti> findByCompteAutreAndDateBetweenOrderByIdDesc(CompteAutre compteAutre, ZonedDateTime dateDebut, ZonedDateTime dateFin, Pageable pageable);

    List<TransactionComptePti> findByCompteAutreAndDateBetween(CompteAutre compteAutre, ZonedDateTime dateDebut, ZonedDateTime dateFin);

    List<TransactionComptePti> findByCompteAutreAndDateBetweenOrderByDateDesc(CompteAutre compteAutre, ZonedDateTime dateDebut, ZonedDateTime dateFin);

    List<TransactionComptePti> findByOperationIdAndEtatOrderByIdDesc(Long operationId, EtatValidite etatValidite);

    List<TransactionComptePti> findByCompteAutreAndDateBetweenAndOperationPointVenteIdOrderByIdDesc(CompteAutre compteAutre, ZonedDateTime dateDebut, ZonedDateTime dateFin, Long pointventeId);

    List<TransactionComptePti> findByCompteAutreAndDateBetweenOrderByIdDesc(CompteAutre compteAutre, ZonedDateTime dateDebut, ZonedDateTime dateFin);

    List<TransactionComptePti> findTop200ByCompteAutreAndDateBetweenOrderByIdDesc(CompteAutre compteAutre, ZonedDateTime dateDebut, ZonedDateTime dateFin);

    @Query("select t from TransactionComptePti t where t.compteAutre.id=:compteAutreId and t.date between :dateDebut and :dateFin")
    List<TransactionComptePti> findByCompteAutreIdAndDateBetweenOrderByIdDesc(@Param("compteAutreId") Long compteAutreId, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin);

    @Query("select t from TransactionComptePti t where t.operation.id=:operationId")
    Page<TransactionComptePti> findTransactionByOp(@Param("operationId") Long operationId, Pageable pageable);

    @Query("select cl from Client cl inner join Compte cpt on cl.id=cpt.client.id inner join Operation op on cpt.id=op.compteDest.id \n"
            + "inner join TransactionComptePti t on op.id=t.operation.id where t.date between :debut and :fin and cl.id=:idClient")
    List<Client> findTrans(@Param("idClient") Long idClient, @Param("debut") ZonedDateTime debut, @Param("fin") ZonedDateTime fin);

    @Query("select c from TransactionComptePti c inner join CompteAutre ca on c.compteAutre.id=ca.id inner join Compte cpt on ca.id=cpt.compteAutre.id\n"
            + "where c.compteAutre.id is not null and c.date between :debut and :fin and cpt.client.id=:idClient")
    List<TransactionComptePti> findByCli(@Param("idClient") Long idClient, @Param("debut") ZonedDateTime debut, @Param("fin") ZonedDateTime fin);

    @Query("select coalesce(sum(c.montant),0) from TransactionComptePti c inner join CompteAutre ca on c.compteAutre.id=ca.id inner join Compte cpt on ca.id=cpt.compteAutre.id\n"
            + "where c.compteAutre.id is not null and c.date between :debut and :fin and cpt.client.id=:idClient and c.type=:typeT ")
    BigDecimal findSommeByCli(@Param("idClient") Long idClient, @Param("debut") ZonedDateTime debut, @Param("fin") ZonedDateTime fin, @Param("typeT") TypeTransactionCompte typeT);

    @Query("select  c from Compte c inner join Client cli on c.client.id=cli.id where c.compteAutre is not null and c.valeurCompte is not null")
    List<Compte> findAllCompteClient();

    @Query("select  c from Compte c inner join Client cli on c.client.id=cli.id  where c.compteAutre is not null and c.valeurCompte is not null and c.compteAutre.codeFse=:codeFse ")
    List<Compte> findAllCompteClientNew(@Param("codeFse") String codeFse);

    @Query("select c from TransactionComptePti c inner join Operation o on c.operation.id=o.id inner join Compte cpt on o.compteSrc.id=cpt.id\n"
            + "inner join Client cli on cpt.client.id=cli.id where cpt.valeurCompte=:valeurCpt ")
    List<TransactionComptePti> findByValeurCpt(@Param("valeurCpt") String valeurCpt);

    @Query("select coalesce(sum(c.montant),0)  from TransactionComptePti c inner join CompteAutre ca on ca=c.compteAutre inner join Compte cpt on ca=cpt.compteAutre\n"
            + " where c.type=:etiquette and cpt.valeurCompte=:valCpt")
    BigDecimal findTotal(@Param("valCpt") String valCpt, @Param("etiquette") TypeTransactionCompte etiquette);

    @Query("select sum(c.soldeApres)  from TransactionComptePti c inner join Operation o on c.operation.id=o.id inner join Compte cpt on o.compteSrc.id=cpt.id\n"
            + "inner join Client cli on cpt.client.id=cli.id where c.type=:etiquette and cpt.valeurCompte=:valCpt")
    BigDecimal findSoldeApres(@Param("valCpt") String valCpt, @Param("etiquette") TypeTransactionCompte etiquette);

    @Query("select sum(c.soldeCourant)  from TransactionComptePti c inner join Operation o on c.operation.id=o.id inner join Compte cpt on o.compteSrc.id=cpt.id\n"
            + "inner join Client cli on cpt.client.id=cli.id where c.type=:etiquette and cpt.valeurCompte=:valCpt")
    BigDecimal findSoldeCourant(@Param("valCpt") String valCpt, @Param("etiquette") TypeTransactionCompte etiquette);

    @Query("select t from TransactionComptePti t inner join CompteAutre c on t.compteAutre.id=c.id inner join Compte cpt \n"
            + "on cpt.compteAutre.id=c.id where cpt.valeurCompte=:valeurCpt")
    List<TransactionComptePti> findByValeurCptA(@Param("valeurCpt") String valeurCpt);

    @Query("select coalesce(sum(t.montant),0)  from TransactionComptePti t inner join CompteAutre c on t.compteAutre.id=c.id inner join Compte cpt\n"
            + " on cpt.compteAutre.id=c.id where t.type=:etiquette and cpt.valeurCompte=:valCpt")
    BigDecimal findTotalA(@Param("valCpt") String valCpt, @Param("etiquette") TypeTransactionCompte etiquette);

    @Query("select sum(t.soldeApres)  from TransactionComptePti t inner join CompteAutre c on t.compteAutre.id=c.id inner join Compte cpt\n"
            + "on cpt.compteAutre.id=c.id where t.type=:etiquette and cpt.valeurCompte=:valCpt")
    BigDecimal findSoldeApresA(@Param("valCpt") String valCpt, @Param("etiquette") TypeTransactionCompte etiquette);

    @Query("select sum(c.soldeCourant)  from TransactionComptePti c inner join Operation o on c.operation.id=o.id inner join Compte cpt on o.compteSrc.id=cpt.id\n"
            + "where c.type=:etiquette and cpt.valeurCompte=:valCpt")
    BigDecimal findSoldeCourantA(@Param("valCpt") String valCpt, @Param("etiquette") TypeTransactionCompte etiquette);

    @Query("select coalesce(sum(ca.solde),0)  from CompteAutre ca inner join Compte cpt on ca=cpt.compteAutre\n"
            + " where  cpt.valeurCompte=:valCpt")
    BigDecimal findSoldeClients(@Param("valCpt") String valCpt);

    @Query("select coalesce(sum(ca.solde),0) from CompteAutre ca inner join Compte c on ca.id=c.compteAutre.id where c.valeurCompte is not null and  c.etiquette='CPTECOMACTEUR' and ca.codeFse=?1 ")
    BigDecimal findSoldeCommission(String codeFse);

    @Query("select coalesce(sum(ca.solde),0) from CompteAutre ca inner join Compte c on ca.id=c.compteAutre.id where c.valeurCompte is not null and c.etiquette='CPTEATTENTECOMM' and ca.codeFse=?1 ")
    BigDecimal findSoldeCommissionAtt(String codeFse);

    @Query("select coalesce(sum(ca.solde),0) from CompteAutre ca inner join Compte c on ca.id=c.compteAutre.id where c.valeurCompte is not null and c.etiquette='CPTETAXE' and ca.codeFse=?1 ")
    BigDecimal findSoldeTaxe(String codeFse);

    @Query("select coalesce(sum(ca.solde),0) from CompteAutre ca inner join Compte c on ca.id=c.compteAutre.id where c.valeurCompte is not null and c.etiquette='CPTFRSPLATEFORME' and ca.codeFse=?1 ")
    BigDecimal findSoldePlateforme(String codeFse);

    @Query("select coalesce(sum(ca.solde),0) from CompteAutre ca inner join\n"
            + "Compte c on ca.id=c.compteAutre.id  where c.valeurCompte is not null and c.etiquette='CPTEATTENTERETRAIT'  and ca.codeFse=?1 ")
    BigDecimal findSoldeTransfertATT(String codeFse);

    @Query("select uo from UniteOrganisation uo inner join TypeUniteOrganisation typeuo on uo.typeUO.id=typeuo.id where uo.typeUO.codeTypeUO !='FSE'"
            + "and uo.typeUO.codeTypeUO != 'PAYS' and uo.typeUO.codeTypeUO !='ZONE' and uo.supprime=false "
            + "and uo.typeUO.codeTypeUO =:agent "
    )
    List<UniteOrganisation> findAllForViewPaginatedSolde(@Param("agent") String agent);

    @Query("select uo from UniteOrganisation uo inner join TypeUniteOrganisation typeuo on uo.typeUO.id=typeuo.id where uo.typeUO.codeTypeUO !='FSE'"
            + "and uo.typeUO.codeTypeUO != 'PAYS' and uo.typeUO.codeTypeUO !='ZONE' and uo.supprime=false "
            + "and (uo.typeUO.codeTypeUO =:agent or uo.typeUO.codeTypeUO ='SOUS_DISTRIBUTEUR' or uo.typeUO.codeTypeUO ='UNITE_REGROUPEMENT') "
    )
    List<UniteOrganisation> findAllAgent(@Param("agent") String agent);

    @Query("select u.id from UniteOrganisation u where u.libelleUniteOrganisation ='Agence principale CORIS MONEY' ")
    Long findByLibelleUO();

    @Query("select uo from UniteOrganisation uo where uo.typeUO.codeTypeUO !='FSE'"
            + "and uo.typeUO.codeTypeUO != 'PAYS' and uo.typeUO.codeTypeUO !='ZONE' and uo.supprime=false ")
    List<UniteOrganisation> findUoList();

    @Query("select u from UniteOrganisation u where u.id=:pereUoId ")
    UniteOrganisation findByUoId(@Param("pereUoId") Long pereUoId);

    @Query("select t from TransactionComptePti t where t.compteAutre.id=:compteAutreId ")
    List<TransactionComptePti> findTransByCompteAutre(@Param("compteAutreId") Long compteAutreId);

    @Query("select t from TransactionComptePti t where t.compteAutre.id=:compteAutreId and (t.date between :debut and :fin or cast(:fin  as timestamp) is null) ")
    List<TransactionComptePti> findTransByCompteAutreDate(@Param("compteAutreId") Long compteAutreId, @Param("debut") ZonedDateTime debut, @Param("fin") ZonedDateTime fin);

    List<TransactionComptePti> findAllByEtat(EtatValidite etat);

    List<TransactionComptePti> findAllByDateBetween(ZonedDateTime date, ZonedDateTime date1);

    @Query(name = "Compte.findAllCompteClientOptimise", nativeQuery = true)
    List<CompteDTO> findAllCompteClientNewOptimise(@Param("codeFse") String codeFse);

    @Query(name = "Compte.findAllCptCliOptimise", nativeQuery = true)
    List<CompteDTO> findAllCompteClientOptimise();

    Integer countByOperationId(Long operationId);

    @Query(name = "TransactionComptePti.findByOperationId", nativeQuery = true)
    List<TransactionComptePtiDTO> findTransactionByOpOptimise(@Param("operationId") Long operationId, @Param("taille") Long taile, @Param("page") Long page);

    @Query("select count(t.id) from TransactionComptePti t "
            + "where (:compteAutreId is null or t.compteAutre.id=:compteAutreId) and (:jhiType is null or t.type=:jhiType) and (t.date between :dateDebut and  :dateFin or cast(:dateFin  as timestamp) is null) ")
    Integer countByCompteAutreAndDateBetweenOrderByIdDesc(@Param("compteAutreId") Long compteAutreId, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin, @Param("jhiType") TypeTransactionCompte jhiType);

    @Query(name = "TransactionComptePti.findByCompteAutreAndDateBetweenOrderByIdDescOptimise", nativeQuery = true)
    List<TransactionComptePtiDTO> findByCompteAutreAndDateBetweenOrderByIdDescOptimise(@Param("compteAutreId") Long compteAutreId, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin, @Param("jhiType") String jhiType, @Param("taille") Long taile, @Param("page") Long page);

    @Query("select t from TransactionComptePti t where t.operation.codeExterne=?1 and t.compteAutre.id =?2")
    public TransactionComptePti findByOperationAndCompteAutre(String codeExterne, Long compteAutreId);

    @Query("select t from TransactionComptePti t where t.operation.codeOperation=?1 and t.compteAutre.id =?2")
    public TransactionComptePti findByOperationAndCompteAutre2(String codeExterne, Long compteAutreId);

    @Query(name = "TransactionComptePti.findByCompteAutreAndDateBetweenAndOperationPointVenteIdOrderByIdDescOptimise", nativeQuery = true)
    List<TransactionComptePtiDTO> findByCompteAutreAndDateBetweenAndOperationPointVenteIdOrderByIdDescOptimise(@Param("compteAutreId") Long compteAutreId, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin, @Param("pointventeId") Long pointventeId);

    
    
    @Query(name = "TransactionComptePti.findByCompteAutreAndDateBetweenAndOperationPointVenteIdLimitOrderByIdDescOptimise", nativeQuery = true)
    List<TransactionComptePtiDTO> findByCompteAutreAndDateBetweenAndOperationPointVenteIdLimitOrderByIdDescOptimise(@Param("compteAutreId") Long compteAutreId, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin, @Param("pointventeId") Long pointventeId,@Param("taille") Long taille);

    
    
    @Query(name = "TransactionComptePti.findTop200ByCompteAutreAndDateBetweenOrderByIdDescOptimiser", nativeQuery = true)
    List<TransactionComptePtiDTO> findTop200ByCompteAutreAndDateBetweenOrderByIdDescOptimiser(@Param("compteAutreId") Long compteAutreId, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin);

    @Query(name = "TransactionComptePti.findByCompteAutreIdOptimise", nativeQuery = true)
    List<TransactionComptePtiDTO> findByCompteAutreIdOptimise(@Param("compteAutreId") Long compteAutreId);

    @Query(name = "TransactionComptePti.findTransByCompteAutreDateOptimiser", nativeQuery = true)
    List<TransactionComptePtiDTO> findTransByCompteAutreDateOptimiser(@Param("compteAutreId") Long compteAutreId, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin);
 @Query("select count(t) from TransactionComptePti t where t.operation.codeExterne=?1 and t.compteAutre.id =?2")
//    @Query(name = "TransactionComptePti.cpte", nativeQuery = true)
     //@Query(value="select count(*) as nombre from transaction_compte_pti t, operation op where t.operation_id=op.id and op.code_externe=?1 and t.compte_autre_id =?2")
    public Long findExisteOperation(@Param("codeExterne") String codeExterne, @Param("compteAutreId") Long compteAutreId);

    @Query(value = "select t from TransactionComptePti t ,Operation o where t.compteAutre.id=:compteAutreId and t.operation.id=o.id and o.pointVente.id=:pointVenteId order by t.id desc ", nativeQuery = true)
    List<TransactionComptePti> findByCompteAutreIdAndLimitPointVenteOrderByIdDesc(@Param("compteAutreId") Long compteAutreId, @Param("pointVenteId") Long pointVenteId);

    @Query("select t from TransactionComptePti t where t.compteAutre.id=:compteAutreId order by t.id desc  ")
    List<TransactionComptePti> findByCompteAutreIdAndLimitOrderByIdDesc(@Param("compteAutreId") Long compteAutreId);

    @Query(name = "TransactionComptePti.findLastThreeByCompteAutreIdDescOptimise", nativeQuery = true)
    List<TransactionComptePtiDTO> findTop3ByCompteAutreOrderByIdDesc(@Param("compteAutreId") Long compteAutreId);
    
    
    @Query(name="TransactionComptePti.queryResultAFB120",nativeQuery = true)
    List<TransactionComptePtiOryx> queryResultAFB120(@Param("valeurCompte") String valeurCompte, @Param("datedebut") LocalDateTime datedebut, @Param("datefin") LocalDateTime datefin);
 
 
    @Query(name="TransactionComptePti.queryResultAFB120Null",nativeQuery = true)
    List<TransactionComptePtiOryx> queryResultAFB120Null(@Param("valeurCompte") String valeurCompte, @Param("datedebut") LocalDateTime datedebut);
    

    
    
}
