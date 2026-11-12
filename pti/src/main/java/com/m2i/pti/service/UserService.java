package com.m2i.pti.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.m2i.pti.config.Constants;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.TypeControl;
import com.m2i.pti.domain.enumeration.TypeParametre;
import com.m2i.pti.repository.*;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.security.SecurityUtils;
import com.m2i.pti.service.dto.ProfilDTO;
import com.m2i.pti.service.dto.UpdateUserDTO;
import com.m2i.pti.service.dto.UserDTO;
import com.m2i.pti.service.dto.UserToUpdate;
import com.m2i.pti.service.kafka.MessageProducer;
import com.m2i.pti.service.mapper.UserMapper;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import com.m2i.pti.web.rest.vm.ManagedUserVM;
import com.m2i.pti.web.rest.vm.PasswordVM;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {

    private static final String TAG = "";
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final String USERS_CACHE = "users";

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthorityRepository authorityRepository;

    private final CacheManager cacheManager;

    private final UserMapper userMapper;

    private final ProfilRepository profilRepository;

    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final ParametreAppService parametreAppService;
    private final UserControlRepository userControlRepository;

    /**
     * @param userRepository
     * @param passwordEncoder
     * @param authorityRepository
     * @param cacheManager
     * @param userMapper
     * @param profilRepository
     * @param uniteOrganisationRepository
     * @param parametreAppService
     * @param userControlRepository
     * @param messageProducer
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthorityRepository authorityRepository, CacheManager cacheManager, UserMapper userMapper, ProfilRepository profilRepository, UniteOrganisationRepository uniteOrganisationRepository, ParametreAppService parametreAppService, UserControlRepository userControlRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authorityRepository = authorityRepository;
        this.cacheManager = cacheManager;
        this.userMapper = userMapper;
        this.profilRepository = profilRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.parametreAppService = parametreAppService;
        this.userControlRepository = userControlRepository;
    }

    public User findByPointVente(PointVente pv) {
        return userRepository.findByPointVente(pv);
    }

    /**
     * @param key
     * @return
     */
    public Optional<User> activateRegistration(String key) {
        log.debug("Activating user for activation key {}", key);
        return userRepository.findOneByActivationKey(key)
                .map(user -> {
                    // activate given user for the registration key.
                    user.setActivated(true);
                    user.setActivationKey(null);
                    if (cacheManager.getCache(USERS_CACHE) != null) {
                        cacheManager.getCache(USERS_CACHE).evict(user.getLogin());
                    }
                    log.debug("Activated user: {}", user);
                    return user;
                });
    }

    /**
     * @param newPassword
     * @param key
     * @return
     */
    public Optional<User> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);
        boolean passwrdIsCorrect = Pattern.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[-+!*$@%_])([-+!*$@%_\\w]{8,50})$", newPassword);

        return userRepository.findOneByResetKey(key)
                .filter(user -> user.getResetDate().isAfter(Instant.now().minusSeconds(86400)))
                .map(user -> {
                    if (!passwrdIsCorrect) {
                        throw new CustomParameterizedException("La syntaxe du mots de passe est incorrecte");
                    } else {
                        Profil profil = user.getProfil();
                        if (profil != null) {
                            Set<Authority> profilRoles = profil.getRoles();
                            Set<Authority> authorities = new HashSet<>();
                            profilRoles.forEach(authority -> {
                                Authority autho = new Authority();
                                autho.setName(authority.getName());
                                authorities.add(autho);
                            });
                            if (!authorities.isEmpty()) {
                                user.setAuthorities(authorities);
                                user.setProfil(profil);
                            } else {
                                throw new CustomParameterizedException("Liste vide");
                            }
                        }
                        user.setPassword(passwordEncoder.encode(newPassword));
                        user.setResetKey(null);
                        user.setResetDate(null);
                    }
                    if (cacheManager.getCache(USERS_CACHE) != null) {
                        cacheManager.getCache(USERS_CACHE).evict(user.getLogin());
                    }
                    return user;
                });

    }

    /**
     * @param mail
     * @return
     */
    public Optional<User> requestPasswordReset(String mail) {
        return userRepository.findOneByEmailIgnoreCase(mail)
                .filter(User::getActivated)
                .map(user -> {
                    user.setResetKey(RandomUtil.generateResetKey());
                    user.setResetDate(Instant.now());
                    if (cacheManager.getCache(USERS_CACHE) != null) {
                        cacheManager.getCache(USERS_CACHE).evict(user.getLogin());
                    }
                    return user;
                });
    }

    /**
     * @param userDTO
     * @return
     */
    public User registerUser(ManagedUserVM userDTO) {

        User newUser = new User();
        Authority authority = authorityRepository.findById(AuthoritiesConstants.USER).orElse(null);
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(userDTO.getPassword());
        newUser.setLogin(userDTO.getLogin());
        newUser.setProfil(profilRepository.findById(userDTO.getProfilId()).orElse(null));
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        newUser.setEmail(userDTO.getEmail());
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        log.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    /**
     * *
     * Enreigistrement des informations d'un nouveau utilisateur
     *
     * @param managedUserVM
     * @return
     */
    @Transactional
    public User createUser(ManagedUserVM managedUserVM) {
        User user = new User();
        if (managedUserVM.getId() != null) {
            user = userRepository.findById(managedUserVM.getId()).orElse(null);
        }
        user.setLogin(managedUserVM.getLogin());
        user.setFirstName(managedUserVM.getFirstName());
        user.setLastName(managedUserVM.getLastName());
        user.setEmail(managedUserVM.getEmail());
        user.setPhoneNumber(managedUserVM.getPhoneNumber());
        user.setCodeIndicatif(managedUserVM.getCodeIndicatif());
        user.setImageUrl(managedUserVM.getImageUrl());
        if (managedUserVM.getClientId() != null) {
            Client c = new Client();
            c.setId(managedUserVM.getClientId());
            user.setClient(c);
        }

        user.setIsBackOffice(managedUserVM.getIsBackOffice());
        if (managedUserVM.getUniteOrganisationId() != null) {
            user.setUniteOrganisation(uniteOrganisationRepository.findById(managedUserVM.getUniteOrganisationId()).orElse(null));
        }

        if (managedUserVM.getLangKey() == null) {
            user.setLangKey(Constants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(managedUserVM.getLangKey());
        }

        if (managedUserVM.getProfilId() != null) {
            Profil profil = profilRepository.findOneWithEagerRelationships(managedUserVM.getProfilId());
            if (profil != null) {
                user.setProfil(profil);
            } else {
                throw new CustomParameterizedException("Profil introuvable !");
            }
        }
        String passwrd = managedUserVM.getPassword();
        String encryptedPassword = passwordEncoder.encode(passwrd);
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(false);
        userRepository.save(user);
        return user;
    }

    /**
     * Update basic information (first name, last name, email, language) for the
     * current user.
     *
     * @param firstName first name of user
     * @param lastName last name of user
     * @param email email id of user
     * @param langKey language key
     * @param imageUrl image URL of user
     */
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().toLowerCase()).ifPresent(user -> {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);
            user.setLangKey(langKey);
            user.setImageUrl(imageUrl);
            if (cacheManager.getCache(USERS_CACHE) != null) {
                cacheManager.getCache(USERS_CACHE).evict(user.getLogin());
            }
        });
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update
     * @return updated user
     */
    public Optional<UserDTO> updateUser(ManagedUserVM userDTO) {
        //Mise à jour du User

        UserToUpdate userToUpdate = new UserToUpdate();
        AtomicReference<UpdateUserDTO> oldUser = new AtomicReference<>(new UpdateUserDTO());
        Boolean activated;
        String currentLogin = SecurityUtils.getCurrentUserLogin();
        if (userDTO != null && currentLogin.equals(userDTO.getLogin())) {
            activated = false;
        } else {
            activated = userDTO.isActivated();
        }

        if (userDTO.getId() != null) {

        }

        Set<Authority> useAuthorities = userRepository.findById(userDTO.getId()).orElse(null).getAuthorities();

        User userExist = userRepository
                .findById(userDTO.getId())
                .map(user -> {
                    User curentUser = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().toLowerCase()).get();
                    if (curentUser != null) {
                        if (user.getActivated() != userDTO.isActivated() && userDTO.isActivated()) {
                            if (user.getCreatedBy() != null && user.getCreatedBy().equals(curentUser.getLogin())) {

                                throw new CustomParameterizedException("Vous ne puvez pas activer un utilisateur que vous avez creé !");
                            }
                        }
                    }
                    User userOld1 = userRepository.findById(userDTO.getId()).orElse(null);
                    oldUser.set(getValueOfUserToUpdate(userOld1));
                    if (userOld1.getPointVente() != null) {
                        if (!userDTO.getUniteOrganisationId().equals(userOld1.getUniteOrganisation().getId())) {
                            user.setPointVente(null);
                        }
                    }
                    user.setLogin(userDTO.getLogin());
                    user.setFirstName(userDTO.getFirstName());
                    user.setLastName(userDTO.getLastName());
                    user.setEmail(userDTO.getEmail());
                    user.setPhoneNumber(userDTO.getPhoneNumber());
                    user.setCodeIndicatif(userDTO.getCodeIndicatif());
                    user.setImageUrl(userDTO.getImageUrl());
                    user.setActivated(activated);
                    user.setLangKey(userDTO.getLangKey());
                    user.setNbrFailure(userDTO.getNbrFailure());
                    user.setUniteOrganisation(uniteOrganisationRepository.findById(userDTO.getUniteOrganisationId()).orElse(null));
                    Set<Authority> managedAuthorities = user.getAuthorities();
                    managedAuthorities.clear();
                    user.setAuthorities(useAuthorities);
                    useAuthorities.forEach(authority -> {
                        Authority newA = authorityRepository.findById(authority.getName()).orElse(null);
                        if (newA != null) {
                            managedAuthorities.add(newA);
                        }
                    });
                    /*userDTO.getAuthorities().stream()
                            .map(authorityRepository::findOne)
                            .forEach(managedAuthorities::add);*/

                    //User userOld =userRepository.findById(userDTO.getId()).orElse(null);
                    /*
                    Mise à jour si le profil de l'utilisateur a été definie
                     */
                    if (userDTO.getProfilId() != null) {
                        Profil profil = profilRepository.findOneWithEagerRelationships(userDTO.getProfilId());
                        if (profil != null) {
                            Set<Authority> profilRoles = profil.getRoles();
                            Set<Authority> authorities = new HashSet<>();
                            profilRoles.forEach(authority -> {
                                Authority autho = new Authority();
                                autho.setName(authority.getName());
                                authorities.add(autho);
                            });
                            if (!authorities.isEmpty()) {
                                user.setAuthorities(authorities);
                                User exist = userRepository.findById(userDTO.getId()).orElse(null);
                                if (exist.getProfil() != profil) {
                                    user.setProfilPrecedent(exist.getProfil());
                                }
                                user.setProfil(profil);
                            } else {
                                throw new CustomParameterizedException("Liste vide");
                            }
                        } else {
                            throw new CustomParameterizedException("Profil introuvable !");
                        }
                    }

                    if (cacheManager.getCache(USERS_CACHE) != null) {
                        cacheManager.getCache(USERS_CACHE).evict(user.getLogin());
                    }

                    UniteOrganisation uniteOrganisation = user.getUniteOrganisation();
                    if (uniteOrganisation != null && (uniteOrganisation.getEstSiege() == null || !uniteOrganisation.getEstSiege())) {
                        List<UserControl> olds = userControlRepository.findAllByLoginAndSupprimeFalse(user.getLogin());
                        if (olds != null) {
                            olds.forEach(old -> {
                                if (!old.getSupprime() && old.getAction().equals(TypeControl.DISABLE)) {

                                    old.setSupprime(true);
                                    userControlRepository.save(old);
                                }

                            });
                        }
                        //Ajout d'une entrée dans userControl
                        if (!user.getActivated()) {
                            UserControl userControl = new UserControl();
                            userControl.setLogin(user.getLogin());
                            userControl.setAction(TypeControl.DISABLE);
                            userControl.setSupprime(false);
                            userControlRepository.save(userControl);
                        }
                    }
                    return user;
                }).get();
        userExist = userRepository.save(userExist);
        UpdateUserDTO newUser = getValueOfUserToUpdate(userExist);
        if (userExist.getIsBackOffice() != null && userExist.getIsBackOffice() == 0) {
            UserControl userUpdateData = userControlRepository.findIfDesactive(oldUser.get().getLogin(), oldUser.get().getCodeIndicatif(), TypeControl.UPDATE_DATA);
            if (userUpdateData != null) {
               updateDataAccount(userUpdateData.getLogin(), userUpdateData.getCodePays(), "AGENT", newUser);
              //  throw new CustomParameterizedException("Une modification non encore validée est encours. Veuillez contacter l'administrateur !!!");
            } else {
                updateDataAccount(oldUser.get().getLogin(), oldUser.get().getCodeIndicatif(), "AGENT", newUser);
            }

        }

        //  userToUpdate.setOldUser(oldUser.get());
        //  userToUpdate.setNewUser(newUser);
        //   messageProducer.sendMessageUpdateUserTelephone(userToUpdate);
        return Optional.of(userMapper.userToUserDTO(userExist));
    }

    /**
     * @param login
     */
    public void deleteUser(String login) {
        userRepository.findOneByLogin(login.toLowerCase()).ifPresent(user -> {
            userRepository.delete(user);
            if (cacheManager.getCache(USERS_CACHE) != null) {
                cacheManager.getCache(USERS_CACHE).evict(login);
            }
        });
    }

    private String convertToJson(Object body) {
        ObjectMapper mapper = new ObjectMapper();
        String json = null;

        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            json = ow.writeValueAsString(body);

        } catch (IOException e) {

        }
        return json;
    }

    public void updateDataAccount(String login, String codePays, String typeActeur, Object data) {
        log.debug("update data " + login);
        UserControl userControl = new UserControl();
        userControl.setAction(TypeControl.UPDATE_DATA);
        userControl.setLogin(login);
        userControl.setTypeActeur(typeActeur);
        userControl.setCodePays(codePays);
        String strData = convertToJson(data);
        userControl.setData(strData);
        List<UserControl> olds = userControlRepository.findAllByLoginAndCodePaysAndActionSupprimeFalse(userControl.getLogin(), userControl.getCodePays(), TypeControl.UPDATE_DATA);
        if (olds != null) {
            olds.forEach(old -> {
                old.setSupprime(true);
                userControlRepository.save(old);
            });
        }
        userControlRepository.save(userControl);

    }

    /**
     * @param password
     */
    public void changePassword(String password) {
        userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().toLowerCase()).ifPresent(user -> {
            String encryptedPassword = passwordEncoder.encode(password);
            user.setPassword(encryptedPassword);
            if (cacheManager.getCache(USERS_CACHE) != null) {
                cacheManager.getCache(USERS_CACHE).evict(user.getLogin());
            }
        });
    }

    public boolean changePassword(User user, String password) {
        String encryptedPassword = passwordEncoder.encode(password);
        if (!this.passwordEncoder.matches(encryptedPassword, user.getPassword())) {

            user.setPassword(encryptedPassword);
            this.userRepository.save(user);
            if (cacheManager.getCache(USERS_CACHE) != null) {
                cacheManager.getCache(USERS_CACHE).evict(user.getLogin());
            }
            return true;
        } else {
            return false;
        }

    }

    /**
     * Verification de la validité de l'ancien mot de passe de l'utilisateur
     *
     * @param password
     * @return
     */
    public ResponseEntity changePassword(PasswordVM password) {
        Optional<User> oneByLogin = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().toLowerCase());

        if (oneByLogin.isPresent()) {
            String clearPasswod = password.getOldPassword();
            if (this.passwordEncoder.matches(clearPasswod, oneByLogin.get().getPassword())) {
                //   this.resetUserPassword(oneByLogin.get().getId());
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                log.error(TAG + " L'ancien mot de passe est incorrect");
                return new ResponseEntity<>("L'ancien mot de passe est incorrect", HttpStatus.BAD_REQUEST);
            }
        } else {
            log.error(TAG + " Impossible de trouver l'utilisateur ayant le login " + SecurityUtils.getCurrentUserLogin());
            return new ResponseEntity<>("Mot de passe incorrect", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Géneration de mot de passe aleatoire pour un utilisateur donnée
     *
     * @param userId
     * @return
     */
    public PasswordVM resetUserPassword(Long userId) {
        if (!this.userRepository.existsById(userId)) {
            throw new CustomParameterizedException("L'utilisateur ayant l'id = " + userId + " n'existe pas");
        }
        User user = this.userRepository.findById(userId).orElse(null);
        if (user != null) {
            String newPassword = RandomUtil.generatePassword();
            user.setPassword(this.passwordEncoder.encode(newPassword));
            user.setResetKey(RandomUtil.generateResetKey());
            user.setResetDate(Instant.now());
            this.userRepository.save(user);
            return new PasswordVM(newPassword, null);
        } else {
            throw new CustomParameterizedException("L'agent n'a pas de compte d'utilisateur");
        }
    }

    /**
     * @param pageable
     * @param uoId
     * @param profilId
     * @return
     */
    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsers(Pageable pageable, Long uoId, Long profilId, String login, String telephone) {
        List<UserDTO> userList = new ArrayList<>();
        userList = userMapper.usersToUserDTOs(userRepository.findUserWithCriteria(uoId, profilId, login, telephone));
        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > userList.size() ? userList.size() : (start + pageable.getPageSize());
        Page<UserDTO> retour = new PageImpl<UserDTO>(userList.subList(start, end), pageable, userList.size());
        return retour;
    }

    /**
     * @param login
     * @return
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthoritiesByLogin(String login) {
        return userRepository.findOneWithAuthoritiesByLogin(login);
    }

    /**
     * Récupération de l'utilisateur et ses roles
     *
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public User getUserWithAuthorities(Long id) {
        return userRepository.findOneWithAuthoritiesById(id);
    }

    /**
     * Récupération des roles de l'utilisateur connecté
     *
     * @return
     */
    @Transactional(readOnly = true)
    public User getUserWithAuthoritiesX() {
        log.debug("User connecté " + SecurityUtils.getCurrentUserLogin());
        return userRepository.findOneWithAuthoritiesByLogin(SecurityUtils.getCurrentUserLogin()).orElse(null);
    }

    @Transactional(readOnly = true)
    public User getUserWithAuthorities() {
        log.debug("User connecté " + SecurityUtils.getCurrentUserLogin());
        Optional<User> user = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin());
        return user.orElse(null);

    }

    @Transactional(readOnly = true)
    public User getUserConnecte() {
        log.debug("User connecté " + SecurityUtils.getCurrentUserLogin());
        return userRepository.findByLogin(SecurityUtils.getCurrentUserLogin());
    }

    @Transactional(readOnly = true)
    public User getUserConnecteOptimise() {
        log.debug("User connecté " + SecurityUtils.getCurrentUserLogin());
        UserDTO us = userRepository.findByLoginOptimise(SecurityUtils.getCurrentUserLogin());
        User user = userMapper.userDTOToUser(us);
        return user;
    }

    @Transactional(readOnly = true)
    public User getUserConnecteOptimise(String login) {
        UserDTO us = userRepository.findByLoginOptimise(login);
        User user = userMapper.userDTOToUser(us);
        return user;
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    /*    @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotActivatedUsers() {
        List<User> users = userRepository.findAllByActivatedIsFalseAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS));
        for (User user : users) {
            userRepository.delete(user);
            if (cacheManager.getCache(USERS_CACHE) != null) {
                cacheManager.getCache(USERS_CACHE).evict(user.getLogin());
            }
        }
    }*/
    /**
     * Not activated users should be automatically disabled after 3 days.
     * <p>
     * This is scheduled to get fired everyday, at 01:00 (am).
     */
    // @Scheduled(cron = "0 0 1 * * ?")
    public void removeNotConnectedUsers() {
        log.debug("%%%% Mise à jour des utilisateurs %%%%" + ZonedDateTime.now().minus(Integer.parseInt(5 + ""), ChronoUnit.DAYS));
        ParametreApp param = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "NBR_JOUR_USER_INACTIF");
        String nbr;
        if (param != null) {
            nbr = param.getValeur();
        } else {
            nbr = "5";//5 jours par default
        }
        List<User> users = userRepository.findAllByActivatedIsTrueAndDateDerniereConnexionBefore(ZonedDateTime.now().minus(Integer.parseInt(nbr), ChronoUnit.DAYS));
        for (User user : users) {
            user.setActivated(Boolean.TRUE);
            userRepository.save(user);
            if (cacheManager.getCache(USERS_CACHE) != null) {
                cacheManager.getCache(USERS_CACHE).evict(user.getLogin());
            }
        }
    }

    public User findOne(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Liste des utilisateur en fonction d'un profil
     *
     * @param profilDTO
     * @return
     */
    public List<User> findAllUserByProfil(ProfilDTO profilDTO) {
        return userRepository.findByProfilId(profilDTO.getId());
    }

    /**
     * Liste des utilisateurs en fonction de l'utilisateur connecté
     *
     * @param pageable
     * @return
     */
    public Page<UserDTO> findAllUserByOrganisationConnecte(Pageable pageable) {
        User userConnecte = this.getUserWithAuthorities();
        if (userConnecte != null) {
            UserDTO user = userMapper.userToUserDTO(userConnecte);
            UniteOrganisation uo = uniteOrganisationRepository.findById(user.getUniteOrganisationId()).orElse(null);
            if (uo != null) {
                return userRepository.findByUniteOrganisationId(uo.getId(), pageable).map(UserDTO::new);
            } else {
                return new PageImpl<User>(Collections.emptyList()).map(UserDTO::new);
            }
        }
        return new PageImpl<User>(Collections.emptyList()).map(UserDTO::new);
    }

    public List<UserDTO> findAllUserByOrganisationConnecteWithoutPage() {
        User userConnecte = this.getUserWithAuthorities();
        if (userConnecte != null) {
            UserDTO user = userMapper.userToUserDTO(userConnecte);
            UniteOrganisation uo = uniteOrganisationRepository.findById(user.getUniteOrganisationId()).orElse(null);
            if (uo != null) {
                return userMapper.usersToUserDTOs(userRepository.findByUniteOrganisationId(uo.getId()));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public Page<UserDTO> findAllUserByOrganisation(Long organisationId, Pageable pageable) {

        return userRepository.findByUniteOrganisationId(organisationId, pageable).map(userMapper::userToUserDTO);
    }

    /**
     * Récupération des utilisateurs en fonction du point de venyte
     *
     * @param pointVenteId
     * @return
     */
    public List<User> findAllUserByPointVente(Long pointVenteId) {
        return userRepository.findByPointVenteId(pointVenteId);
    }

    /**
     * Liste des utilisateur de la plateforme
     *
     * @return
     */
    public List<User> findAllUser() {
        if (!getUserWithAuthorities().getAuthorities().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AuthoritiesConstants.ADMIN))) {
            return userRepository.findAll();
        }
        return Collections.emptyList();
    }

    /**
     * @param pointVenteId
     * @return
     */
    public List<UserDTO> findAllUserDTOByPointVente(Long pointVenteId) {
        return userMapper.usersToUserDTOs(userRepository.findByPointVenteId(pointVenteId));
    }

    /**
     * @return a list of all the authorities
     */
    public List<String> getAuthorities() {
        return authorityRepository.findAll().stream().map(Authority::getName).collect(Collectors.toList());
    }

    public ManagedUserVM findUserConnect() {
        Optional<User> oneByLogin = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().toLowerCase());
        ManagedUserVM managedUserVM = new ManagedUserVM();
        if (oneByLogin.isPresent()) {
            User user = this.userRepository.findById(oneByLogin.get().getId()).orElse(null);
            UserDTO userDTO = userMapper.userToUserDTO(user);
            managedUserVM = new ManagedUserVM(userDTO.getId(), userDTO.getLogin(), null, userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(), userDTO.isActivated(), userDTO.getImageUrl(), userDTO.getLangKey(), userDTO.getResetKey(), userDTO.getPhoneNumber(), userDTO.getCreatedBy(), userDTO.getCreatedDate(), userDTO.getLastModifiedBy(), userDTO.getLastModifiedDate(), userDTO.getAuthorities(), userDTO.getProfilId(), userDTO.getProfilNom(), userDTO.getUniteOrganisationId(), userDTO.getUniteOrganisationNom(), userDTO.getPointVenteId(), userDTO.getNbrFailure(), userDTO.getDateDerniereConnexion(), null, 1, null);
        }
        return managedUserVM;
    }

    /**
     * Mise à jour du nombre de tentative d'echec d'authentification d'un
     * utilisateur
     *
     * @param login
     */
    public void updateNbFailuerByLogin(String login) {
        Optional<User> userExist = userRepository.findOneByLogin(login.toLowerCase());
        if (userExist.isPresent()) {
            User user = userExist.get();
            if (user.getNbrFailure() == null) {
                user.setNbrFailure(0);
            }
            ParametreApp param = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "NBR_TENTATIVE_CONNEXION");
            String nbrTentative = "";
            if (param != null) {
                nbrTentative = param.getValeur();
            } else {
                nbrTentative = "3";
            }
            if (user.getNbrFailure() < Integer.parseInt(nbrTentative) - 1) {
                user.setNbrFailure(user.getNbrFailure() + 1);
                userRepository.save(user);
            } else {
                user.setActivated(Boolean.FALSE);
                userRepository.save(user);
            }
        }
    }

    // TODO: 01/12/2021 Récupération de l'utilisateur par téléphone et login 
    public User findByNumeroTelephone(String telephone, String login) {
        User ut = userRepository.findByNumeroTelephone(telephone, login);
        return ut;
    }

    // TODO: 01/12/2021 Récupération de l'utilisateur par son login 
    public User findeByLogin(String login) {
        User ut = userRepository.findByLogin(login.toLowerCase());
        return ut;
    }

    public User findByNumeroTelephoneAndCode(String numeroTelephoneAgent, String codeIndicatif) {
        //    User ut=    userRepository.findByNumeroTelephoneAndCode(numeroTelephoneAgent,codeIndicatif);

        return null;
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsersOptimise(Pageable pageable, Long uoId, Long profilId, String login, String telephone) {
        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        List<Long> organisations = uoId == null ? null : Collections.singletonList(uoId);
        String loginValue = login == null ? "" : login;
        String telephoneValue = telephone == null ? "" : telephone;
        int totalUser = uoId == null
                ? userRepository.countManagedOpen(profilId, loginValue, telephoneValue)
                : userRepository.countUserWithCriteriaOptimise(organisations, profilId, login, telephone);

        Pageable pageable1 = new Pageable() {
            @Override
            public int getPageNumber() {
                return 0;
            }

            @Override
            public int getPageSize() {
                return 0;
            }

            @Override
            public long getOffset() {
                return 0;
            }

            @Override
            public Pageable withPage(int pageNumber) {
                return this;
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public Pageable next() {
                return null;
            }

            @Override
            public Pageable previousOrFirst() {
                return null;
            }

            @Override
            public Pageable first() {
                return null;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }
        };
        List<UserDTO> userDTOList = uoId == null
                ? userRepository.findManagedOpen(profilId, loginValue, telephoneValue, taille, page)
                : userRepository.findUserWithCriteriaOptimiseUserAdmin(organisations, profilId, login, telephone, taille, page);
        Page<UserDTO> retour = new PageImpl<>(userDTOList, pageable1, totalUser);
        return retour;
    }

    public UpdateUserDTO getValueOfUserToUpdate(User user) {
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setId(user.getId());
        updateUserDTO.setCodeIndicatif(user.getCodeIndicatif());
        updateUserDTO.setEmail(user.getEmail());
        updateUserDTO.setFirstName(user.getFirstName());
        updateUserDTO.setLastName(user.getLastName());
        updateUserDTO.setLogin(user.getLogin());
        updateUserDTO.setPhoneNumber(user.getPhoneNumber());
        if (user.getUniteOrganisation() != null) {
            updateUserDTO.setCodeActeur(user.getUniteOrganisation().getCodeUO());
        }
        return updateUserDTO;
    }

}
