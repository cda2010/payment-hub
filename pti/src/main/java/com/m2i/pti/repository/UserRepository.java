package com.m2i.pti.repository;

import com.m2i.pti.domain.PointVente;
import com.m2i.pti.domain.User;
import com.m2i.pti.service.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.time.Instant;
import org.springframework.data.jpa.repository.Query;

/**
 * Spring Data JPA repository for the User entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findOneByActivationKey(String activationKey);

    List<User> findAllByActivatedIsFalseAndCreatedDateBefore(Instant dateTime);

    List<User> findAllByActivatedIsTrueAndDateDerniereConnexionBefore(ZonedDateTime dateTime);

    Optional<User> findOneByResetKey(String resetKey);

    Optional<User> findOneByEmailIgnoreCase(String email);

    User findByPointVente(PointVente pointVente);

    Optional<User> findOneByPhoneNumberIgnoreCase(String phoneNumber);

    List<User> findByProfilId(Long profilId);

    List<User> findByPointVenteId(Long pointVenteId);

    Page<User> findByUniteOrganisationId(Long profilId, Pageable pageable);

    List<User> findByUniteOrganisationId(Long uoId);

    Optional<User> findOneByEmail(String email);

    Optional<User> findOneByLogin(String login);

    @EntityGraph(attributePaths = "authorities")
    User findOneWithAuthoritiesById(Long id);

    @EntityGraph(attributePaths = "authorities")
    Optional<User> findOneWithAuthoritiesByLogin(String login);

    Page<User> findAllByLoginNot(Pageable pageable, String login);

    Page<User> findAllByLoginNotAndProfilId(Pageable pageable, String login, Long profilId);

    Page<User> findAllByLoginNotAndUniteOrganisationId(Pageable pageable, String login, Long uoId);

    Page<User> findAllByLoginNotAndProfilIdAndUniteOrganisationId(Pageable pageable, String login, Long profilId, Long uoId);
    @Query("select c from User c where c.phoneNumber=?1 and LOWER(c.login)=?2")
    public User findByNumeroTelephone(String telephone, String login);
   @Query("select c from User c where LOWER(c.login)=?1")
    public User findByLogin(String login);

   @Query(name = "User.findUser",nativeQuery = true)
    UserDTO findByLoginOptimise(@Param("login") String login);
 
            
     @Query("select u from User u where (" +
             "(:uoId is null or u.uniteOrganisation.id=:uoId) and " +
             "(:profilId is null or u.profil.id=:profilId) and " +
             "(:login is null or :login='' or u.login like ('%'||:login||'%')) and " +
             "(:telephone is null or :telephone='' or u.phoneNumber like('%'||:telephone||'%') )  ) order by u.id desc ")
    public List<User> findUserWithCriteria(@Param("uoId") Long uoId, @Param("profilId") Long profilId, @Param("login") String login, @Param("telephone") String telephone);

    @Query(name = "User.findAllUserIfAdmin",nativeQuery = true)
    List<UserDTO> findUserWithCriteriaOptimiseUserAdmin(@Param("uoId") List<Long> uoId, @Param("profilId") Long profilId, @Param("login") String login, @Param("telephone") String telephone , @Param("taille") Long taille, @Param("page") Long page);

    @Query(name = "User.findManagedOpen", nativeQuery = true)
    List<UserDTO> findManagedOpen(@Param("profilId") Long profilId, @Param("login") String login, @Param("telephone") String telephone, @Param("taille") Long taille, @Param("page") Long page);

    @Query("select count(u.id) from User u where u.uniteOrganisation is not null and (:profilId is null or u.profil.id = :profilId) and (:login is null or :login = '' or u.login like concat('%', :login, '%')) and (:telephone is null or :telephone = '' or u.phoneNumber like concat('%', :telephone, '%'))")
    Integer countManagedOpen(@Param("profilId") Long profilId, @Param("login") String login, @Param("telephone") String telephone);

    @Query(name = "User.findAllUser",nativeQuery = true)
    List<UserDTO> findUserWithCriteriaOptimise(@Param("uoId") List<Long> uoId, @Param("profilId") Long profilId, @Param("login") String login, @Param("telephone") String telephone , @Param("taille") Long taille, @Param("page") Long page);


    @Query("select count(u.id) from User u where (" +
            "(u.profil.id is not null ) and " +
            "((:uoId) is null or u.uniteOrganisation.id in (:uoId)) and " +
            "(:profilId is null or u.profil.id=:profilId) and " +
            "(:login is null or :login='' or u.login like ('%'||:login||'%')) and " +
            "(:telephone is null or :telephone='' or u.phoneNumber like('%'||:telephone||'%') )  )")
    Integer countUserWithCriteriaOptimise(@Param("uoId") List<Long> uoId, @Param("profilId") Long profilId, @Param("login") String login, @Param("telephone") String telephone );



}
