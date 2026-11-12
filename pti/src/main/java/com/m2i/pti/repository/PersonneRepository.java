package com.m2i.pti.repository;

import com.m2i.pti.domain.Personne;
import com.m2i.pti.service.dto.PersonneDTO;

import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.query.Param;


/**
 * Spring Data JPA repository for the Personne entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PersonneRepository extends JpaRepository<Personne, Long> {
    // Optional<Personne> findOneByTelephone(String telephone);

    //   List<Personne> findByTelephone(String telephone);
  Personne findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(String telephone, String code);
  Personne findTopByTelephoneAndCodePaysTelephone(String telephone, String code);
  Personne findTop1ByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(String telephone, String code);

  Optional<Personne> findTop1ByTelephoneAndCodePaysTelephone(String telephone, String code);
    List<Personne> findByTelephoneAndCodePaysTelephone(String telephone, String code);

    //   Personne findTopByTelephone(String telephone);

    List<Personne> findByTelephoneAndPersonnePhysiqueIn(String telephone, List<Long> ids);

    List<Personne> findByTelephoneAndPersonnePhysiqueId(String telephone, Long personneId);

    Optional<Personne> findByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(String telephone,String code);
@Query("select c.personne from Client c order by c.id desc")
    public List<Personne> findAllPersonneClient();
  @Query("select p from Personne p where p.telephone=?1 and codePaysTelephone=?2")  
  Optional<Personne> findPersonneByPhoneAndCodePays(String telephone, String code);
@Query(name = "Personne.findPersonneByPhoneAndCodePays", nativeQuery = true)
  PersonneDTO findPersonneByPhoneAndCodeIndicatif( @Param("telephone") String telephone ,@Param("codeIndicatif") String codeIndicatif );
  
  @Query(name = "Personne.findPersonneByPhoneAndCodePaysAndActivated", nativeQuery = true)
  PersonneDTO findPersonneByPhoneAndCodeIndicatifActivated( @Param("telephone") String telephone ,@Param("codeIndicatif") String codeIndicatif );
  
  
    @Query("select p from Personne p where p.id=?1")
  Personne findByPersonneId(Long personneId);
  
    public Personne findTopByTelephoneAndCodePaysTelephoneAndEmail(String telephone, String codePays, String email);

    @Query("SELECT   COUNT(*) AS nbr_doublon, ph.nomClient,  p.telephone\n" +
            "FROM     PersonnePhysique ph, Personne p where ph.id=p.personnePhysique.id\n" +
            "GROUP BY ph.nomClient,p.telephone\n" +
            "HAVING   COUNT(*) > 1")
    public List<Object[]> getDoublonNomTel();

    @Query("select p from Personne p order by p.id desc ")
    List<Personne> findPersonne();

    @Query("select count (p.telephone) from Personne p where p.telephone=?1")
    Integer countByTelephone(String telephone);

    @Query("select count(p.keyHash) from Personne p where p.keyHash=?1")
    Integer countByHashKey(String hashkey);

    @Query("select count(ph.nomClient) from Personne p inner join PersonnePhysique ph on ph.id=p.personnePhysique.id where ph.nomClient=?1 ")
    Integer countByClientName(String nomClient);

    @Query("select count(ph.prenomClient) from Personne p inner join PersonnePhysique ph on ph.id=p.personnePhysique.id where ph.prenomClient=?1 ")
    Integer countByClientFirstName(String prenomClient);

    @Query("select count(*) from Personne p inner join PersonnePhysique ph on ph.id=p.personnePhysique.id where ph.nomClient=?1  and ph.prenomClient=?2 ")
    Integer countByClientFirstNameAndName(String nomClient,String prenomClient);

    @Query("select count(*) from Personne p inner join PersonnePhysique ph on ph.id=p.personnePhysique.id where ph.nomClient=?1  and p.telephone=?2 ")
    Integer countByClientNameAndTelephone(String nomClient,String telephone);

    @Query("select count(*) from Personne p inner join PersonnePhysique ph on ph.id=p.personnePhysique.id where ph.prenomClient=?1  and p.telephone=?2 ")
    Integer countByClientFirstNameAndTelephone(String prenomClient,String telephone);

    @Query("select count(*) from Personne p inner join PersonnePhysique ph on ph.id=p.personnePhysique.id where ph.nomClient=?1 and ph.prenomClient=?2  and p.telephone=?3 ")
    Integer countByClientFirstNameAndNameAndTelephone(String nomClient,String prenomClient,String telephone);

    @Query("select count(*) from Personne p inner join Piece pi on pi.titulaire.id=p.id inner join TypePiece tp on pi.typePiece.id=tp.id where tp.codeTypePiece=?1 and pi.referencePiece=?2")
    Integer countByPiece(String codeTypePiece, String referencePiece);

    @Query("select count(*) from Personne p inner join Piece pi on pi.titulaire.id=p.id inner join TypePiece tp on pi.typePiece.id=tp.id where tp.codeTypePiece=?1 and pi.referencePiece=?2 and p.keyHash=?3")
    Integer countByPieceAndHashcode(String codeTypePiece, String referencePiece,String hash);


    @Query("select p from Personne p inner join Piece pi on pi.titulaire.id=p.id inner join TypePiece tp on pi.typePiece.id=tp.id where tp.codeTypePiece=?1 and pi.referencePiece=?2")
    List<Personne> countPersonneByPiece(String codeTypePiece, String referencePiece);


    @Query("select p from Personne p where p.keyHash=?1")
    List<Personne> countPersonneByHashKey(String hashkey);

    @Query("select p from Personne p inner join Piece pi on pi.titulaire.id=p.id inner join TypePiece tp on pi.typePiece.id=tp.id where tp.codeTypePiece=?1 and pi.referencePiece=?2 and p.keyHash=?3")
    List<Personne> countPersonneByPieceAndHashcode(String codeTypePiece, String referencePiece,String hash);


    

  @Query(name = "Personne.findOne", nativeQuery = true)
  PersonneDTO findPersonneById( @Param("id") Long id  );


  @Query(name = "Personne.findBackofficeOne", nativeQuery = true)
  PersonneDTO findPersonneBackofficeById( @Param("id") Long id  );
    

    @Query(name = "Personne.findOne", nativeQuery = true)
    PersonneDTO findPersonneOneById( @Param("personneId") Long personneId);

    @Query("select count(p.id) from Personne p")
    Integer countPersonne();

  @Query(name = "Personne.findOneByBeneficiaireId", nativeQuery = true)
  PersonneDTO findPersonneByBeneficiaireId( @Param("id") Long id  );

  @Query(name = "Personne.findAllOptimiser", nativeQuery = true)
  List<PersonneDTO> findAllOptimiser(@Param("hashkey") String hashkey);

  @Query(name = "Personne.findAllByTpePieceAndRefPiece", nativeQuery = true)
  List<PersonneDTO> findAllByTpePieceAndRefPiece(@Param("codeTypePiece") String codeTypePiece,@Param("referencePiece")  String referencePiece,@Param("hash") String hash);

  @Query(nativeQuery = true,value="select count (p.telephone) from personne p,client c,compte cpt  where  c.personne_id=p.id and cpt.client_id=c.id and p.telephone=?1")
  Integer countClientByTelephone(String telephone);
  @Query(name = "Personne.findDoublonByTypePiece", nativeQuery = true)
  List<PersonneDTO> findDoublonByTypePiece(@Param("typePieceId") Long typePieceId,@Param("taille") Long taille,@Param("page") Long page);
  @Query(name = "Personne.findDoublonByHash", nativeQuery = true)
  List<PersonneDTO> findDoublonByHash(@Param("taille") Long taille,@Param("page") Long page);
  @Query(name = "Personne.findDoublonByTypePieceAndHash", nativeQuery = true)
  List<PersonneDTO> findDoublonByTypePieceAndHash(@Param("typePieceId")  Long typePieceId,@Param("taille") Long taille,@Param("page") Long page);
  @Query("select p from Personne p inner join Piece pi on pi.titulaire.id=p.id inner join TypePiece tp on pi.typePiece.id=tp.id where pi.referencePiece=?1")
  List<Personne>  findPersonneByPiece(String referencePiece);
}
