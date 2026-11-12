package com.m2i.pti.repository;

import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.OperationSuivis;
import com.m2i.pti.service.dto.OperationSuivisDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data JPA repository for the OperationSuivis entity.
 */
@SuppressWarnings("unused")
@Repository
public interface OperationSuivisRepository extends JpaRepository<OperationSuivis, Long> {
    @Query("select c from Client c inner join Personne p on c.personne.id=p.id inner join PersonnePhysique t on p.personnePhysique.id=t.id where t.nomClient=?1 ")
    public List<Client> findByNom(String nom);

    @Query("select c from Client c inner join Personne p on c.personne.id=p.id inner join PersonnePhysique t on p.personnePhysique.id=t.id where t.prenomClient=?1")
    public List<Client> findByPrenom(String prenom);

    @Query("select c from Client c inner join Personne p on c.personne.id=p.id inner join PersonnePhysique t on p.personnePhysique.id=t.id " +
            "inner join Nationalite m on t.nationalite.id=m.id where m.code=?1 and t.nomClient=?2")
    public List<Client> findByCodeAndNom(String code, String nom);

    @Query("select c from Client c inner join Personne p on c.personne.id=p.id inner join PersonnePhysique t on p.personnePhysique.id=t.id " +
            "inner join Nationalite m on t.nationalite.id=m.id where p.telephone=?1 and t.nomClient=?2")
    public List<Client> findByTelAndNom(String tel, String nom);

    @Query("select c from Client c inner join Personne p on c.personne.id=p.id inner join PersonnePhysique t on p.personnePhysique.id=t.id " +
            "inner join Nationalite m on t.nationalite.id=m.id where m.code=?1 and p.telephone=?2 and t.nomClient=?3")
    public List<Client> findByCodeAndTelAndNom(String code, String tel, String nom);
    @Query("select p from OperationSuivis p where p.client.id=?1")
    public Page<OperationSuivis> findOperaSuivisByClient(Long id, Pageable pageable);

    @Query(name = "OperationSuivis.findByClientId",nativeQuery = true)
    public List<OperationSuivisDTO> findAllOperationSuivisByClientId(@Param("clientId") Long clientId);
}
