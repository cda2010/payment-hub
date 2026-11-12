package com.m2i.pti.repository;

import com.m2i.pti.domain.BlackList;
import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.enumeration.ActionBlackList;
import com.m2i.pti.service.dto.BlackListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Spring Data JPA repository for the BlackList entity.
 */
@SuppressWarnings("unused")
@Repository
public interface BlackListRepository extends JpaRepository<BlackList, Long> {
    /*@Query("select b.id  from Blacklist b, Client c where b.id=?1 ")
    public List<BlackList> queryClient(Long id);*/
    @Query("select c from Client c inner join Personne p on c.personne.id=p.id inner join PersonnePhysique t on p.personnePhysique.id=t.id\n" +
            "where p.codePaysTelephone=?1")
    List<Client> findByCode(String id);
    @Query("select c from Client c inner join Personne p on c.personne.id=p.id")
    List<Client> queryAll();
    @Query("select c from Client c inner join Personne p on c.personne.id=p.id where p.telephone=?1")
    List<Client> findByTel(String id);
    @Query("select c from Client c inner join Personne p on c.personne.id=p.id inner join PersonnePhysique t on p.personnePhysique.id=t.id\n" +
            "where p.codePaysTelephone=?1 and p.telephone=?2")
    List<Client> findByCodeAndTel(String code, String tel);
    @Query("select COALESCE(count(c),0) from BlackList c where c.client=?1 and c.actionBlackList=?2")
    public Integer countByClientAndActionBlackList(Client clem, ActionBlackList actionBlackList);

    Page<BlackList> findAllBySupprimeFalse(Pageable pageable);

    @Query("select count(b.id) from BlackList b ,Client c,Personne p,PersonnePhysique ph where b.client.id=c.id and c.personne.id=p.id\n" +
            "and p.personnePhysique.id=ph.id  and :telephone is null or p.telephone=:telephone\n" +
            "and (:nomClient is null or ph.nomClient=:nomClient) and (:prenomClient is null or ph.prenomClient=:prenomClient) and (:actionBlackList is null or b.actionBlackList=:actionBlackList) and b.supprime=true ")
    Integer countWithCriteria(@Param("telephone") String telephone,@Param("actionBlackList") String actionBlackList,
                                           @Param("nomClient") String nomClient,@Param("prenomClient") String prenomClient );

    @Query(name = "BlackList.findAll",nativeQuery = true)
    List<BlackListDTO> findAllWithCriteria(@Param("telephone") String telephone,@Param("actionBlackList") String actionBlackList,
                                           @Param("nomClient") String nomClient,@Param("prenomClient") String prenomClient,@Param("taille") Long taille, @Param("page") Long page);
}
