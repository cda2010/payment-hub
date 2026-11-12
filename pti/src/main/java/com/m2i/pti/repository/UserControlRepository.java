package com.m2i.pti.repository;

import com.m2i.pti.domain.UserControl;
import com.m2i.pti.domain.enumeration.TypeControl;
import com.m2i.pti.service.dto.BilanOperationDTO;
import com.m2i.pti.service.dto.UserControlDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Spring Data JPA repository for the UserControl entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UserControlRepository extends JpaRepository<UserControl, Long> {

    @Query(value = "select uc from UserControl uc where uc.login=:login and uc.codePays=:codePays "
            + "and (coalesce(uc.password,:password)=:password) "
            + "and uc.supprime=false")
    List<UserControl> findAllByCritera(@Param("login") String login, @Param("codePays") String codePays, @Param("password") String password);
   @Query(value = "select uc from UserControl uc where uc.login=:login and uc.codePays=:codePays and  uc.action=:type  and uc.supprime=false")
    List<UserControl> findAllByLoginAndCodePaysAndActionSupprimeFalse(@Param("login") String login,@Param("codePays")  String codePays, @Param("type") TypeControl type);
    List<UserControl> findAllByLoginAndCodePaysAndSupprimeFalse(String login, String codePays);

    @Query(value = "select uc from UserControl uc where uc.login=?1 and (uc.codePays is null or uc.codePays=?2 )"
            + " and uc.action=?3 "
            + "and uc.supprime=false")
    public UserControl findIfDesactive(String login, String codePays, TypeControl typeControl);

    List<UserControl> findAllByLoginAndSupprimeFalse(String login);

    // @Query(value = "select * from user_control uc where uc.action=:typeControl and supprime=true and uc.created_date between CAST(CAST(:dateDebut as text) as timestamp) and CAST(CAST(:dateFin as text) as timestamp) ",nativeQuery = true)
    @Query(name = "UserControl.findAllByAction", nativeQuery = true)
    List<UserControlDTO> findAllByActionAndCreatedDate(@Param("typeControl") String typeControl, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin);

    @Query("SELECT count (u.id) FROM UserControl u  WHERE " +
            "(" +
            "(u.login=:telephone ) and  (u.createdDate between cast(:dateDebut as timestamp) and  cast(:dateFin as timestamp) or cast(:dateDebut  as timestamp) is null) )")
    Integer countUserControlByPersonne(@Param("telephone") String telephone, @Param("dateDebut") ZonedDateTime dateDebut, @Param("dateFin") ZonedDateTime dateFin);

    @Query("SELECT count (u.id) FROM UserControl u  WHERE " +
            "(" +
            "(u.login=:telephone ) )")
    Integer countUserControlByPersonneWithoutDate(@Param("telephone") String telephone);


 @Query(name = "UserControl.findAllByPersonneDate",nativeQuery = true)
    List<UserControlDTO> findUserControleByPersonneIdDate(@Param("telephone") String telephone, @Param("debut") ZonedDateTime debut, @Param("fin") ZonedDateTime fin, @Param("taille") Long taille, @Param("page") Long page);

    @Query(name = "UserControl.findAllByPersonne",nativeQuery = true)
    List<UserControlDTO> findUserControleByPersonneId(@Param("telephone") String telephone, @Param("taille") Long taille, @Param("page") Long page);
@Query(name = "UserControl.findUserByAction",nativeQuery = true)
    List<UserControlDTO> findUserByAction(@Param("action") String action, @Param("telephone") String telephone);
}
