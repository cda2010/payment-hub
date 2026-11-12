package com.m2i.pti.repository;

import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.Personne;
import com.m2i.pti.domain.enumeration.TypePersonne;
import com.m2i.pti.service.dto.ClientDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;


/**
 * Spring Data JPA repository for the Client entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findTopByCodeClientAndSupprimeIsFalse(String codeClient);

    //Client findByCodeClient(String codeClient);
    @Query("SELECT c FROM Client c inner join c.personne p WHERE c.supprime=false  and (p.typePersonne = :typePersonne and(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            "and (:telephone is null  or p.telephone  like :telephone )" +
            "and (:adresse is null or  p.adresse like :adresse)))")
    List<Client> findClientByTypePersonneAndCriteria(@Param("typePersonne") TypePersonne typePersonne,
                                                     @Param("code") String code,
                                                     @Param("telephone") String telephone,
                                                     @Param("adresse") String adresse);

    @Query("SELECT c FROM Client c inner join c.personne p on c.personne=p inner join PersonnePhysique pp on p.personnePhysique=pp WHERE " +
            "(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            "and (:telephone is null  or :telephone='' or UPPER(p.telephone)  like UPPER('%'||:telephone||'%') )" +
            "and (:adresse is null or :adresse='' or UPPER(p.adresse)  like UPPER('%'||:adresse||'%'))" +
            "and (:nom is null or :nom=''  or UPPER(pp.nomClient)   like UPPER('%'||:nom||'%'))" +
            "and (:prenom is null or :prenom='' or UPPER(pp.prenomClient)  like UPPER('%'||:prenom||'%'))" +
            "and (c.supprime is null or c.supprime=false)) order by c.id desc")
    List<Client> findClientPhysiqueByCriteria(       @Param("code") String code,
                                                     @Param("telephone") String telephone,
                                                     @Param("adresse") String adresse,
                                                     @Param("nom") String nom,
                                                     @Param("prenom") String prenom);

    @Query("SELECT c FROM Client c inner join c.personne p on c.personne=p inner join PersonnePhysique pp on p.personnePhysique=pp WHERE " +
            "(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            "and (:telephone is null  or :telephone='' or UPPER(p.telephone)  like UPPER('%'||:telephone||'%') )" +
            "and (:adresse is null or :adresse='' or UPPER(p.adresse)  like UPPER('%'||:adresse||'%'))" +
            "and (:nom is null or :nom=''  or UPPER(pp.nomClient)   like UPPER('%'||:nom||'%'))" +
            "and (:prenom is null or :prenom='' or UPPER(pp.prenomClient)  like UPPER('%'||:prenom||'%'))" +
            "and(c.createdDate between cast(:dateDebut as timestamp) and cast(:dateFin as timestamp) )"+
            "and (c.supprime is null or c.supprime=false)) order by c.id desc")
    List<Client> findClientPhysiqueByCriteriaDate(   @Param("code") String code,
                                                     @Param("telephone") String telephone,
                                                     @Param("adresse") String adresse,
                                                     @Param("nom") String nom,
                                                     @Param("prenom") String prenom,
                                                     @Param("dateDebut") ZonedDateTime dateDebut,
                                                     @Param("dateFin") ZonedDateTime dateFin
    );

    @Query("SELECT c FROM Client c inner join c.personne p on c.personne=p inner join PersonneMorale pm on p.personneMorale=pm WHERE " +
            "(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            " and (:telephone is null or :telephone=''  or UPPER(p.telephone)  like UPPER('%'||:telephone||'%'))" +
            " and (:adresse is null or :adresse='' or   UPPER(p.adresse)  like UPPER('%'||:adresse||'%'))" +
            " and (:raisonSociale is null or :raisonSociale='' or  UPPER(pm.raisonSociale) like UPPER('%'||:raisonSociale||'%'))" +
            " and (c.supprime is null or c.supprime=false )) order by c.id desc")
    List<Client> findClientMoraleByCriteria(@Param("code") String code,
                                            @Param("telephone") String telephone,
                                            @Param("adresse") String adresse,
                                            @Param("raisonSociale") String raisonSociale

    );


    @Query("SELECT c FROM Client c inner join c.personne p on c.personne=p inner join PersonneMorale pm on p.personneMorale=pm WHERE " +
            "(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            " and (:telephone is null or :telephone=''  or UPPER(p.telephone)  like UPPER('%'||:telephone||'%'))" +
            " and (:adresse is null or :adresse='' or   UPPER(p.adresse)  like UPPER('%'||:adresse||'%'))" +
            " and (:raisonSociale is null or :raisonSociale='' or  UPPER(pm.raisonSociale) like UPPER('%'||:raisonSociale||'%'))" +
            "and(c.createdDate between cast(:dateDebut as timestamp) and cast(:dateFin as timestamp) )"+
            " and (c.supprime is null or c.supprime=false )) order by c.id desc")
    List<Client> findClientMoraleByCriteriaDate(@Param("code") String code,
                                            @Param("telephone") String telephone,
                                            @Param("adresse") String adresse,
                                            @Param("raisonSociale") String raisonSociale,
                                            @Param("dateDebut") ZonedDateTime dateDebut,
                                            @Param("dateFin") ZonedDateTime dateFin
    );


    @Query("SELECT c FROM Client c inner join c.personne p on c.personne=p inner join PersonnePhysique pp on p.personnePhysique=pp WHERE " +
            "(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            "and (:telephone is null  or :telephone='' or UPPER(p.telephone)  like UPPER('%'||:telephone||'%') )" +
            "and (:adresse is null or :adresse='' or UPPER(p.adresse)  like UPPER('%'||:adresse||'%'))" +
            "and (:nom is null or :nom=''  or UPPER(pp.nomClient)   like UPPER('%'||:nom||'%'))" +
            "and (:prenom is null or :prenom='' or UPPER(pp.prenomClient)  like UPPER('%'||:prenom||'%'))" +
            "and (c.supprime is null or c.supprime=false)) order by c.id desc")
    Page<Client> findClientPhysiqueByCriteriaPage(       @Param("code") String code,
                                                         @Param("telephone") String telephone,
                                                         @Param("adresse") String adresse,
                                                         @Param("nom") String nom,
                                                         @Param("prenom") String prenom,
                                                         Pageable pageable);


    @Query("SELECT c FROM Client c inner join c.personne p on c.personne=p inner join PersonneMorale pm on p.personneMorale=pm WHERE " +
            "(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            " and (:telephone is null or :telephone=''  or UPPER(p.telephone)  like UPPER('%'||:telephone||'%'))" +
            " and (:adresse is null or :adresse='' or   UPPER(p.adresse)  like UPPER('%'||:adresse||'%'))" +
            " and (:raisonSociale is null or :raisonSociale='' or  UPPER(pm.raisonSociale) like UPPER('%'||:raisonSociale||'%'))" +
            " and (c.supprime is null or c.supprime=false )) order by c.id desc")
    Page<Client> findClientMoraleByCriteriaPage(@Param("code") String code,
                                                @Param("telephone") String telephone,
                                                @Param("adresse") String adresse,
                                                @Param("raisonSociale") String raisonSociale,
                                                Pageable pageable
    );





    @Query("SELECT c FROM Client c inner join c.personne p WHERE p.typePersonne = ?1 " +
            "and p.codePaysTelephone=?2 and p.telephone=?3 and c.supprime=false ")
    List<Client> findClientByTypePersonneAndCodeAndTelephonne(TypePersonne typePersonne,String code,String telephone);
 @Query("SELECT c FROM Client c  WHERE c.codeClient = ?1 and c.supprime=false ")
    List<Client> findByCodeClientAndSupprimeIsFalse(String codeClient);
  @Query("SELECT c FROM Client c  WHERE c.personne.id = ?1 and c.supprime=false ")
    List<Client> findByPersonneAndSupprimeIsFalse(Personne pers);
  @Query("SELECT c FROM Client c  WHERE c.personne.id = ?1 and c.supprime=false and c.clientValide=true ")
    Client findByPersonneIdAndClientValideTrueAndSupprimeIsFalse(Long personneId);

    Client findTop1ByPersonneIdAndClientValideTrueAndSupprimeIsFalse(Long personneId);

    @Query("SELECT c FROM Client c  WHERE c.personne.id = ?1 and c.supprime=false ")
    Client findTop1ByPersonneIdAndSupprimeIsFalse(Long personneId);

    List<Client> findByProfilIdAndSupprimeIsFalse(Long profilId);

    Page<Client> findByClientValideAndSupprimeIsFalse(Boolean clientValid,Pageable pageable);

    Page<Client> findByClientValideAndPersonneIdAndSupprimeIsFalse(Boolean clientValid,Long personneId,Pageable pageable);

    @Query("select c from Client c where c.supprime=false " )
    Page<Client> findlient(Pageable pageable);

    List<Client> findByClientValideAndSupprimeIsFalse(Boolean TRUE);
    @Query("select c from Client c where c.clientValide=:b and c.supprime=false ")
    List<Client> findAllClient1(@Param("b") Boolean b);
    @Query("select c from Client c where (c.clientValide=:b or c.clientValide is null) and c.supprime=false ")
    List<Client> findAllClient2(@Param("b") Boolean b);
    
      
    @Query(name = "Client.findClientMoralPersonne",nativeQuery = true)
     List<ClientDTO> findAllClientMorale();
    
    @Query("select c from Client c inner join Personne p on c.personne.id=p.id inner join PersonneMorale pm \n" +
            "on p.personneMorale.id=pm.id and c.supprime=false")
    List<Client> findByTypePer();
    @Query("select c from Compte c  where c.client.id=?1 ")
    List<Compte> findCptEntrp(Long id);

    @Query("select c from Client c")
    List<Client> findAllClient();
    @Query("select distinct c from Client c inner join Personne per on c.personne.id=per.id inner join PersonnePhysique p on " +
            "per.personnePhysique.id=p.id  inner join Compte cpt on cpt.client.id=c.id " +
            "where (" +
            "(:nom is null or p.nomClient like ('%'||:nom||'%') )" +
            "and (:telephone is null  or per.telephone like :telephone)"+
            "and (:numeroCpt is null or cpt.valeurCompte like :numeroCpt ) ) ")
    List<Client> findAllClients(@Param("nom") String nom,@Param("telephone") String telephone,
                                @Param("numeroCpt") String numeroCpt);

    @Query("select distinct c from Client c inner join Personne per on c.personne.id=per.id inner join PersonneMorale p on " +
            "per.personneMorale.id=p.id  inner join Compte cpt on cpt.client.id=c.id " +
            "where (" +
            "(:nom is null or p.raisonSociale like('%'||:nom||'%') ) " +
            "and (:telephone is null  or per.telephone like :telephone)"+
            "and (:numeroCpt is null or cpt.valeurCompte like :numeroCpt)  ) ")
    List<Client> findAllClientsEntrp(@Param("nom") String nom,@Param("telephone") String telephone,
                                     @Param("numeroCpt") String numeroCpt);

    List<Client> findAllBySupprimeIsFalse();

    public Client findByPersonneIdAndSupprimeIsFalse(Long id);

    @Query("select c from ChargementTransfert c where c.client.id=:idclient order by c.id desc ")
    List<ChargementTransfert> findChargement(@Param("idclient") Long idclient);



    // importation
    Optional<Client> findByTagIdentityRef(Integer identit);
    List<Client> findByTagIdentityRefIsNotNull();
    Optional<Client> findByCliRef(Integer identit);

    @Query("select count(c) from Client c inner join Personne p on c.personne.id=p.id inner join PersonnePhysique pp on p.personnePhysique.id=pp.id inner join Compte cpt on cpt.client.id=c.id inner join CompteAutre ca on cpt.compteAutre.id=ca.id where p.typePersonne=:typePersonne and c.supprime=false")
    Integer findByTypePersonne(@Param("typePersonne") TypePersonne typePersonne);

  
    
    

@Query(name = "Client.findPersonneById", nativeQuery = true)
    ClientDTO findClientPersonne(@Param("personneId") Long personneId);

    @Query(name = "Client.findClientPhysiqueByCriteria",nativeQuery = true)
    List<ClientDTO> findClientPhysiqueByCriteriaOptimise(@Param("code") String code, @Param("telephone") String telephone, @Param("adresse") String adresse, @Param("nom") String nom, @Param("prenom") String prenom, @Param("taille") Long taille, @Param("page") Long page);
    
    @Query(name = "Client.findClientPhysiqueByCarteCriteria",nativeQuery = true)
    List<ClientDTO> findClientPhysiqueByCarteCriteriaOptimise(@Param("code") String code, @Param("telephone") String telephone, @Param("adresse") String adresse, @Param("nom") String nom, @Param("prenom") String prenom, @Param("cardNumber") String cardNumber, @Param("last4Digit") String last4Digit, @Param("taille") Long taille, @Param("page") Long page);

    @Query(name = "Client.findClientPhysiqueByWalletCriteria", nativeQuery = true)
    List<ClientDTO> findClientPhysiqueByWalletCriteriaOptimise(@Param("code") String code, @Param("telephone") String telephone, @Param("adresse") String adresse, @Param("nom") String nom, @Param("prenom") String prenom, @Param("walletNumber") String walletNumber, @Param("taille") Long taille, @Param("page") Long page);
        
    @Query(name = "Client.findClientMoralByCriteria",nativeQuery = true)
    List<ClientDTO> findClientMoralByCriteriaOptimise(@Param("code") String code, @Param("telephone") String telephone, @Param("adresse") String adresse, @Param("raisonSociale") String raisonSociale, @Param("taille") Long taille,@Param("page") Long page);

    @Query(name = "Client.findClientMoralReportByCriteriaDate",nativeQuery = true)
    List<ClientDTO> findClientMoraleByCriteriaDateReportOptimise( @Param("code") String code, @Param("telephone") String telephone, @Param("adresse") String adresse,  @Param("raisonSociale") String raisonSociale,@Param("dateDebut") ZonedDateTime dateDebut,@Param("dateFin") ZonedDateTime dateFin,@Param("taille") Long taille, @Param("page") Long page);

    @Query(name = "Client.findClientPhysiqueReportByCriteriaDate",nativeQuery = true)
    List<ClientDTO> findClientPhysiueByCriteriaDateReportOptimise( @Param("code") String code, @Param("telephone") String telephone, @Param("adresse") String adresse,  @Param("nom") String nom,@Param("prenom") String prenom,@Param("dateDebut") ZonedDateTime dateDebut,@Param("dateFin") ZonedDateTime dateFin, @Param("taille") Long taille, @Param("page") Long page);

    @Query(name = "Client.findAllClientRegulation",nativeQuery = true)
    List<ClientDTO> findAllClientRegulation(@Param("nom") String nom,@Param("telephone") String telephone,
                                  @Param("numeroCpt") String numeroCpt);


    @Query("SELECT c FROM Client c inner join c.personne p on c.personne=p inner join PersonnePhysique pp on p.personnePhysique=pp WHERE " +
            "(UPPER(pp.nomClient) like UPPER('%'||:nomClient||'%'))" )
    Page<Client> findByNomClient( @Param("nomClient") String nomClient, Pageable pageable);

    @Query("SELECT c FROM Client c inner join c.personne p on c.personne=p inner join PersonnePhysique pp on p.personnePhysique=pp WHERE " +
            "(UPPER(pp.nomClient) like UPPER('%'||:nomClient||'%'))" )
    List<Client> findByNom( @Param("nomClient") String nomClient);

    @Query("SELECT count (c.id)FROM Client c inner join c.personne p on c.personne=p inner join PersonnePhysique pp on p.personnePhysique=pp WHERE " +
            "(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            "and (:telephone is null  or :telephone='' or UPPER(p.telephone)  like UPPER('%'||:telephone||'%') )" +
            "and (:adresse is null or :adresse='' or UPPER(p.adresse)  like UPPER('%'||:adresse||'%'))" +
            "and (:nom is null or :nom=''  or UPPER(pp.nomClient)   like UPPER('%'||:nom||'%'))" +
            "and (:prenom is null or :prenom='' or UPPER(pp.prenomClient)  like UPPER('%'||:prenom||'%'))" +
            "and (c.supprime is null or c.supprime=false)" +
            "and ( cast(:dateDebut as timestamp) is null   or c.createdDate between :dateDebut   and :dateFin  ))")
    Integer countClientPhysiqueByCriteria(@Param("code") String code, @Param("telephone") String telephone, @Param("adresse") String adresse, @Param("nom") String nom, @Param("prenom") String prenom,@Param("dateDebut") ZonedDateTime dateDebut,@Param("dateFin") ZonedDateTime dateFin);

    @Query("SELECT count (c.id) FROM Client c inner join c.personne p on c.personne=p inner join PersonneMorale pm on p.personneMorale=pm WHERE " +
            "(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            " and (:telephone is null or :telephone=''  or UPPER(p.telephone)  like UPPER('%'||:telephone||'%'))" +
            " and (:adresse is null or :adresse='' or   UPPER(p.adresse)  like UPPER('%'||:adresse||'%'))" +
            " and (:raisonSociale is null or :raisonSociale='' or  UPPER(pm.raisonSociale) like UPPER('%'||:raisonSociale||'%'))" +
            " and (c.supprime is null or c.supprime=false ) " +
            "and cast(:dateDebut as timestamp)  is null or c.createdDate between :dateDebut   and :dateFin  )")
    Integer countClientMoraleByCriteria(@Param("code") String code, @Param("telephone") String telephone, @Param("adresse") String adresse, @Param("raisonSociale") String raisonSociale,@Param("dateDebut") ZonedDateTime dateDebut,@Param("dateFin") ZonedDateTime dateFin);

    @Query(name = "Client.findClientByClientId",nativeQuery = true)
    ClientDTO findClientByClientIdOptimise(@Param("clientId") Long clientId);

    @Query(name = "Client.findAllClient2",nativeQuery = true)
    List<ClientDTO> findAllClient2Optimise(@Param("clientValide") Boolean clientValide);

    @Query(name = "Client.findAllClient1",nativeQuery = true)
    List<ClientDTO> findAllClient1Optimise(@Param("clientValide") Boolean clientValide);

    @Query("SELECT count (c.id)FROM Client c inner join c.personne p on c.personne=p inner join PersonnePhysique pp on p.personnePhysique=pp WHERE " +
            "(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            "and (:telephone is null  or :telephone='' or UPPER(p.telephone)  like UPPER('%'||:telephone||'%') )" +
            "and (:adresse is null or :adresse='' or UPPER(p.adresse)  like UPPER('%'||:adresse||'%'))" +
            "and (:nom is null or :nom=''  or UPPER(pp.nomClient)   like UPPER('%'||:nom||'%'))" +
            "and (:prenom is null or :prenom='' or UPPER(pp.prenomClient)  like UPPER('%'||:prenom||'%'))" +
            "and (c.supprime is null or c.supprime=false)" +
            "and ( cast(:dateDebut as timestamp) is null   or c.createdDate between cast(:dateDebut as timestamp)   and cast(:dateFin as timestamp)  ))")
    Integer countClientPhysiqueByCriteriaEtat(@Param("code") String code, @Param("telephone") String telephone, @Param("adresse") String adresse, @Param("nom") String nom, @Param("prenom") String prenom,@Param("dateDebut") ZonedDateTime dateDebut,@Param("dateFin") ZonedDateTime dateFin);

    @Query("SELECT count (c.id) FROM Client c inner join c.personne p on c.personne=p inner join PersonneMorale pm on p.personneMorale=pm WHERE " +
            "(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            " and (:telephone is null or :telephone=''  or UPPER(p.telephone)  like UPPER('%'||:telephone||'%'))" +
            " and (:adresse is null or :adresse='' or   UPPER(p.adresse)  like UPPER('%'||:adresse||'%'))" +
            " and (:raisonSociale is null or :raisonSociale='' or  UPPER(pm.raisonSociale) like UPPER('%'||:raisonSociale||'%'))" +
            " and (c.supprime is null or c.supprime=false ) " +
            "and cast(:dateDebut as timestamp)  is null or c.createdDate between cast(:dateDebut as timestamp)   and cast(:dateFin as timestamp)  )")
    Integer countClientMoraleByCriteriaEtat(@Param("code") String code, @Param("telephone") String telephone, @Param("adresse") String adresse, @Param("raisonSociale") String raisonSociale,@Param("dateDebut") ZonedDateTime dateDebut,@Param("dateFin") ZonedDateTime dateFin);

    @Query(name = "Client.clientIdentifier",nativeQuery = true)
    List<ClientDTO> findAllClientIdentifier(@Param("dateDebut") ZonedDateTime dateDebut,@Param("dateFin") ZonedDateTime dateFin);

    @Query(value="select count(*) from client c,compte cpt where cpt.client_id=c.id and cpt.compte_autre_id is not null and compte_banque_id is null and c.supprime=false and cpt.created_date between :debut and :fin",nativeQuery = true)
    Integer nombreCompteOuvert(@Param("debut") ZonedDateTime debut,@Param("fin") ZonedDateTime fin);
    
     @Query("SELECT count (DISTINCT c.id) FROM Client c inner join c.personne p on c.personne=p inner join PersonnePhysique pp on p.personnePhysique=pp inner join Compte cpt on cpt.client.id=c.id inner join Carte ca on cpt.carte.id=ca.id  WHERE " +
            "(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            "and (:telephone is null  or :telephone='' or UPPER(p.telephone)  like UPPER('%'||:telephone||'%') )" +
            "and (:adresse is null or :adresse='' or UPPER(p.adresse)  like UPPER('%'||:adresse||'%'))" +
            "and (:nom is null or :nom=''  or UPPER(pp.nomClient)   like UPPER('%'||:nom||'%'))" +
            "and (:prenom is null or :prenom='' or UPPER(pp.prenomClient)  like UPPER('%'||:prenom||'%'))" +
            "and (c.supprime is null or c.supprime=false)" +
            "and (:cardNumber is null or :cardNumber='' or UPPER(cpt.valeurCompte) like UPPER('%'||:cardNumber||'%') )" +
            "and (:last4Digit is null or :last4Digit='' or ca.last4Digit = :last4Digit)" +
            "and ( cast(:dateDebut as timestamp) is null   or c.createdDate between :dateDebut   and :dateFin  ))")
    Integer countClientPhysiqueCarteByCriteria(@Param("code") String code, @Param("telephone") String telephone, @Param("adresse") String adresse, @Param("nom") String nom, @Param("prenom") String prenom, @Param("cardNumber") String cardNumber, @Param("last4Digit") String last4Digit, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin);

    @Query("SELECT count(DISTINCT c.id) FROM Client c inner join c.personne p on c.personne=p inner join PersonnePhysique pp on p.personnePhysique=pp inner join Compte cpt on cpt.client.id=c.id WHERE " +
            "(" +
            "(:code is null or p.codePaysTelephone=:code)" +
            "and (:telephone is null  or :telephone='' or UPPER(p.telephone)  like UPPER('%'||:telephone||'%') )" +
            "and (:adresse is null or :adresse='' or UPPER(p.adresse)  like UPPER('%'||:adresse||'%'))" +
            "and (:nom is null or :nom=''  or UPPER(pp.nomClient)   like UPPER('%'||:nom||'%'))" +
            "and (:prenom is null or :prenom='' or UPPER(pp.prenomClient)  like UPPER('%'||:prenom||'%'))" +
            "and (c.supprime is null or c.supprime=false)" +
            "and (:walletNumber is null or :walletNumber='' or UPPER(cpt.valeurCompte) like UPPER('%'||:walletNumber||'%'))" +
            "and ( cast(:dateDebut as timestamp) is null   or c.createdDate between :dateDebut   and :dateFin  ))")
    Integer countClientPhysiqueWalletByCriteria(@Param("code") String code, @Param("telephone") String telephone, @Param("adresse") String adresse, @Param("nom") String nom, @Param("prenom") String prenom, @Param("walletNumber") String walletNumber, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin);
    
}
