package com.m2i.pti.web.rest;

import com.m2i.pti.config.Constants;
import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.TypeControl;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.UserControlRepository;
import com.m2i.pti.repository.UserRepository;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.service.*;
import com.m2i.pti.service.dto.InfoUserOfUo;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import com.m2i.pti.service.dto.UserDTO;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import com.m2i.pti.web.rest.errors.EmailAlreadyUsedException;
import com.m2i.pti.web.rest.errors.LoginAlreadyUsedException;
import com.m2i.pti.web.rest.errors.*;
import com.m2i.pti.web.rest.vm.ManagedUserVM;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import com.m2i.pti.web.rest.vm.PasswordVM;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.ApiParam;
import com.m2i.pti.mobile.dto.MDataReturn;
import com.m2i.pti.service.pentaho.ExecuteTransformation;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import jakarta.xml.bind.JAXBException;

/**
 * REST controller for managing users.
 * <p>
 * This class accesses the User entity, and needs to fetch its collection of
 * authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship
 * between User and Authority, and send everything to the client side: there
 * would be no View Model and DTO, a lot less code, and an outer-join which
 * would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities,
 * because people will quite often do relationships with the user, and we don't
 * want them to get the authorities all the time for nothing (for performance
 * reasons). This is the #1 goal: we should not impact our users' application
 * because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is
 * not a real issue as we have by default a second-level cache. This means on
 * the first HTTP call we do the n+1 requests, but then all authorities come
 * from the cache, so in fact it's much better than doing an outer join (which
 * will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO
 * layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this
 * case.
 */
@RestController
@RequestMapping("/api")
public class UserResource {

    private final Logger log = LoggerFactory.getLogger(UserResource.class);

    private static final String ENTITY_NAME = "userManagement";

    private final UserRepository userRepository;

private final ExecuteTransformation executeTransformation;
    private final UserService userService;

    private final UserDetailsService userDetailsService;

    private final UniteOrganisationService uniteOrganisationService;

    private final ApiService apiService;
    private final MobileService mobileService;
    private final UserControlRepository userControlRepository;

    public UserResource(ExecuteTransformation executeTransformation,MobileService mobileService, UserRepository userRepository,
            UserService userService, UserDetailsService userDetailsService, UniteOrganisationService uniteOrganisationService, ApiService apiService, UserControlRepository userControlRepository) {
this.executeTransformation=executeTransformation;
        this.userRepository = userRepository;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.uniteOrganisationService = uniteOrganisationService;
        this.apiService = apiService;
        this.mobileService = mobileService;
        this.userControlRepository = userControlRepository;
    }

    /**
     * POST /users : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends
     * an mail with an activation link. The user needs to be activated on
     * creation.
     *
     * @param managedUserVM the user to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new user, or with status 400 (Bad Request) if the login or email is
     * already in use
     * @throws URISyntaxException if the Location URI syntax is incorrect
     * @throws BadRequestAlertException 400 (Bad Request) if the login or email
     * is already in use
     */
    @PostMapping("/users")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREATE_USER})
    public ResponseEntity createUser(@Valid @RequestBody ManagedUserVM managedUserVM) throws URISyntaxException  , IOException, JAXBException {
        log.debug("REST request to save User : {}", managedUserVM);
        if (managedUserVM.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an ID", ENTITY_NAME, "idexists");
        } else if (userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase()).isPresent()) {
            throw new CustomParameterizedException("Le login est déjà utilisé");
        } else if (userRepository.findOneByEmailIgnoreCase(managedUserVM.getEmail()).isPresent()) {
            throw new CustomParameterizedException("L'email est déjà utilisé");
        } else if (userRepository.findOneByPhoneNumberIgnoreCase(managedUserVM.getPhoneNumber()).isPresent()) {
            throw new CustomParameterizedException("Le numero de téléphone est déjà utilisé");
        } else {
            String passwrd = RandomUtil.generatePassword();
            managedUserVM.setPassword(passwrd);
            User newUser = userService.createUser(managedUserVM);
            apiService.envoiSms(newUser.getCodeIndicatif() + newUser.getPhoneNumber(), "Votre de mot passe est :" + passwrd + " CBI, la Banque autrement.","OTP",newUser.getCodeIndicatif());
            sendMailToUser(newUser, passwrd);
            return ResponseEntity.created(new URI("/api/users/" + newUser.getLogin()))
                    .headers(HeaderUtil.createAlert("userManagement.created", newUser.getLogin()))
                    .body(newUser);
        }
    }

    /**
     * PUT /users : Updates an existing User.
     *
     * @param managedUserVM the user to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * user
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is
     * already in use
     * @throws PhoneNumberAlreadyUsedException 400 (Bad Request) if the phone
     * number is already in use
     * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is
     * already in use
     */
    @PutMapping("/users")
    @Timed
    @Secured({AuthoritiesConstants.ADMIN, AuthoritiesConstants.CREATE_USER})
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody ManagedUserVM managedUserVM) {
        log.debug("REST request to update User : {}", managedUserVM);
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(managedUserVM.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(managedUserVM.getId()))) {
            throw new EmailAlreadyUsedException();
        }

        Optional<User> existingUser1 = userRepository.findOneByPhoneNumberIgnoreCase(managedUserVM.getPhoneNumber());
        if (existingUser1.isPresent() && (!existingUser1.get().getId().equals(managedUserVM.getId()))) {
            throw new CustomParameterizedException("Le numero de téléphone est déjà utilisé");
        }
        existingUser = userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase());
        if (existingUser.isPresent() && (!existingUser.get().getId().equals(managedUserVM.getId()))) {
            throw new LoginAlreadyUsedException();
        }
        Optional<UserDTO> updatedUser = userService.updateUser(managedUserVM);

        return ResponseUtil.wrapOrNotFound(updatedUser,
                HeaderUtil.createAlert("userManagement.updated", managedUserVM.getLogin()));
    }

    /**
     * GET /users : get all users.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body all users
     */
    @GetMapping("/users")
    @Timed
    public ResponseEntity<List<UserDTO>> getAllUsers(@ApiParam Pageable pageable, @RequestParam(required = false) Long uoId, @RequestParam(required = false) Long profilId, @RequestParam(required = false) String login, @RequestParam(required = false) String telephone) {
        log.debug("Request to get all users");
        final Page<UserDTO> page = uniteOrganisationService.findAllUserByOrganisation(pageable, uoId, profilId, login, telephone);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/users/byUniteOrganisation/{uniteOrganisationId}")
    @Timed
    public ResponseEntity<List<UserDTO>> getAllUsersOfUo(@PathVariable(required = true) Long uniteOrganisationId, Pageable pageable) {
        Page<UserDTO> page = userService.findAllUserByOrganisation(uniteOrganisationId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users/byUniteOrganisation");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/users/update_nbr_failure")
    @Timed
    public void updateUserNbrFailure(@RequestParam(required = true) String login) {
        userService.updateNbFailuerByLogin(login);
    }

    /**
     * GET /users : get all users.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and with body all users
     */
    @GetMapping("/users/unite-organisation")
    @Timed
    public ResponseEntity<List<UserDTO>> getAllUsersOfUniteOrganisation(@ApiParam Pageable pageable) {
        final Page<UserDTO> page = userService.findAllUserByOrganisationConnecte(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users/unite-organisation");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/users/unite-organisation-without-page")
    @Timed
    public List<UserDTO> getAllUsersOfUniteOrganisationWithoutPage() {
        return userService.findAllUserByOrganisationConnecteWithoutPage();
    }

    @GetMapping("/users/point-vente")
    @Timed
    public List<UserDTO> getAllUsersOfPointVente(@RequestParam Long pointVenteId) {
        return userService.findAllUserDTOByPointVente(pointVenteId);
    }

    /**
     * @return a string list of the all of the roles
     */
    @GetMapping("/users/authorities")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN)
    public List<String> getAuthorities() {
        return userService.getAuthorities();
    }

    /**
     * GET /users/:login : get the "login" user.
     *
     * @param login the login of the user to find
     * @return the ResponseEntity with status 200 (OK) and with body the "login"
     * user, or with status 404 (Not Found)
     */
    @GetMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    public ResponseEntity<UserDTO> getUser(@PathVariable String login) {
        log.debug("REST request to get User : {}", login);
        return ResponseUtil.wrapOrNotFound(
                userService.getUserWithAuthoritiesByLogin(login)
                .map(UserDTO::new));
    }

    @GetMapping("/users/login/{id}")
    @Timed
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        log.debug("REST request to get User : {}", id);
        User user = userService.findOne(id);
        return new ResponseEntity<>(user, null, HttpStatus.OK);

    }

    /**
     * DELETE /users/:login : delete the "login" User.
     *
     * @param login the login of the user to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @Timed
    @Secured(AuthoritiesConstants.ADMIN_SYSTEME)
    public ResponseEntity<Void> deleteUser(@PathVariable String login) {
        log.debug("REST request to delete User: {}", login);
        userService.deleteUser(login);
        return ResponseEntity.ok().headers(HeaderUtil.createAlert("userManagement.deleted", login)).build();
    }

    @GetMapping("/users/getUserConnect")
    @Timed
    public ManagedUserVM getUserConnect() {
        return userService.findUserConnect();
    }

    private void sendMailToUser(User newUser, String passwrd) {
        HashMap<String, String> keyVal = new HashMap<String, String>();
        if (newUser.getEmail() != null) {
            keyVal.put("PASSWORD", passwrd);
            keyVal.put("USERLOGIN", newUser.getLastName().toUpperCase());
            mobileService.sendMail(newUser.getEmail(), "ENVOIPASSWORD", keyVal);
        }
    }

    @Transactional
    @PostMapping("/users/changePassword")
    @Timed
    public ResponseEntity changePwdUser(ManagedUserVM managedUserVM, PasswordVM passwordVM) {
        log.debug("REST request to save User : {}", managedUserVM);
        ResponseEntity responseEntity = userService.changePassword(passwordVM);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            String passwrd = RandomUtil.generatePassword();
            managedUserVM.setPassword(passwrd);
            User newUser = userService.createUser(managedUserVM);
            apiService.sendMessage(newUser.getPhoneNumber(), passwrd, "");
            sendMailToUser(newUser, passwrd);

            //  mailService.sendPasswordMail(newUser, passwrd);
        }
        return responseEntity;
    }

    @Transactional
    @PostMapping("/users/initPassword")
    @Timed
    @Secured(AuthoritiesConstants.CREATE_USER)
    public ResponseEntity<Void> changePwdUser(@RequestBody ManagedUserVM managedUserVM) {
        log.debug("REST request to save User : {}", managedUserVM);

        String passwrd;
        String passwrdNotEncode;
        //* lorsque l'utilisateur n'est pas du backoffice *//
        if (managedUserVM.getIsBackOffice() != null && managedUserVM.getIsBackOffice().equals(0)) {
            passwrdNotEncode = RandomUtil.generatePasswordNumeric();
            log.debug("MOT DE PASSE: {}", passwrdNotEncode);
           passwrd = RandomUtil.encodeHash(passwrdNotEncode);
          
                managedUserVM.setPassword(passwrdNotEncode);
        } else {
            passwrd = RandomUtil.generatePassword();
            /// TODO: 12/07/2023 Les mots de passe de Tout les utilisateurs réinitialiser seront en numériques   
            //passwrd = RandomUtil.generatePasswordNumeric();
            passwrdNotEncode = passwrd;
                managedUserVM.setPassword(passwrd);
        }
    
        User newUser = userService.createUser(managedUserVM);
        String numero= "+"+newUser.getCodeIndicatif()+newUser.getPhoneNumber();
        apiService.sendMessage(numero, "Votre nouveau mot de passe est:" + passwrdNotEncode, newUser.getCodeIndicatif());
        // mailService.sendPasswordMail(newUser, passwrdNotEncode);
        sendMailToUser(newUser, passwrdNotEncode);
        Set<Authority> authorities = new HashSet<>();
        newUser.setAuthorities(authorities);
        // newUser.setProfil(null);
        userRepository.save(newUser);
        UniteOrganisation uniteOrganisation = newUser.getUniteOrganisation();
        if ((  uniteOrganisation != null && (uniteOrganisation.getEstSiege() == null || !uniteOrganisation.getEstSiege())) || (managedUserVM.getIsBackOffice() != null && managedUserVM.getIsBackOffice().equals(0)) ) {
            List<UserControl> olds = userControlRepository.findAllByLoginAndSupprimeFalse(newUser.getLogin());
            if (olds != null) {
                olds.forEach(old -> {
                    if (!old.getSupprime() && old.getAction().equals(TypeControl.RESET_PASSWORD)) {
                        old.setSupprime(true);
                        userControlRepository.save(old);
                    }
                });
            }
            //Ajout d'une entrée dans userControl
            UserControl userControl = new UserControl();
            userControl.setLogin(newUser.getLogin());
            userControl.setAction(TypeControl.RESET_PASSWORD);
            userControl.setPassword(passwrd);
            userControl.setSupprime(false);
            userControlRepository.save(userControl);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/all_users")
    @Timed
    public List<User> getAllUsers() {
        return userService.findAllUser();
    }

    @GetMapping("/users/temps_pass/{data}")
    @Timed
    public UserQrCode getTempPass(@PathVariable String data) {
        
        return userDetailsService.getUserTempPass(data);
    }

@GetMapping("/users/temps_pass2")
    @Timed
    public MDataReturn getTempPass2(@RequestParam String hostId) {
        return userDetailsService.getUserTempPass2(hostId);
    }


    @GetMapping("/sendInfoOfUo")
    @Timed
    public InfoUserOfUo sendInfoUO(@RequestParam Long organisationId){
        log.debug("Request to send message");
        String login = null,codeUo = null,libelle = null,numero=null,codeIndicatif=null;
        InfoUserOfUo infoUserOfUo =uniteOrganisationService.getUserOOfUo(organisationId);
        if(infoUserOfUo.getUserDTO()!=null){
            if(infoUserOfUo.getUserDTO().getId()!=null){
                login=infoUserOfUo.getUserDTO().getLogin();
            }
        }
        if(infoUserOfUo.getUniteOrganisationDTO()!=null){
            if(infoUserOfUo.getUniteOrganisationDTO().getId()!=null){
                codeUo=infoUserOfUo.getUniteOrganisationDTO().getCodeUO();
                libelle=infoUserOfUo.getUniteOrganisationDTO().getLibelleUniteOrganisation();
                numero=infoUserOfUo.getUniteOrganisationDTO().getTelephone();
                codeIndicatif=infoUserOfUo.getUniteOrganisationDTO().getCodePaysTelephone();
            }
        }
        String message="Info de connexion pour"+" "+libelle+" "+"login "+login+" "+"et codeUo "+codeUo+" "+"et telephone "+numero;
        numero="+"+codeIndicatif+numero;
        apiService.sendMessage(numero,message,codeIndicatif);
        return infoUserOfUo;
    }

    @GetMapping("/users_optimise")
    @Timed
    public ResponseEntity<List<UserDTO>> getAllUsersOptimise(@ApiParam Pageable pageable, @RequestParam(required = false) Long uoId, @RequestParam(required = false) Long profilId, @RequestParam(required = false) String login, @RequestParam(required = false) String telephone) {
        log.debug("Request to get all users");
        final Page<UserDTO> page = uniteOrganisationService.findAllUserByOrganisationOptimise(pageable, uoId, profilId, login, telephone);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/users");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
@GetMapping("/users/trans")
    @Timed
    public void exectrans(@RequestParam Long exportId,@RequestParam Long fileExportId) {
         executeTransformation.execTrans(exportId,fileExportId);
    }

    @GetMapping("/users/cardless")
    @Timed
    public  com.m2i.omnicanal.outils.restclient.model.Message exectranscardl(@RequestParam String reference,@RequestParam String entityCode,@RequestParam String codeFse) throws IOException {
              com.m2i.omnicanal.outils.restclient.model.Message mes=      apiService.setCardLessTransaction("76477115", "76477115", "5000",reference, entityCode, codeFse);
    return mes;
           }

}
