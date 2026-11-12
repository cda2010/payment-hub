package com.m2i.pti.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.m2i.pti.domain.ParametreApp;
import com.m2i.pti.domain.UserControl;

import com.m2i.pti.domain.enumeration.TypeControl;
import com.m2i.pti.domain.enumeration.TypeParametre;
import com.m2i.pti.mobile.dto.UserExist;
import com.m2i.pti.repository.PersonneRepository;
import com.m2i.pti.repository.UserListeBlancheRepository;
import com.m2i.pti.repository.UserControlRepository;
import com.m2i.pti.service.dto.PersonneDTO;
import com.m2i.pti.service.dto.UserControlDTO;
import com.m2i.pti.service.mapper.UserControlMapper;
import com.m2i.pti.service.util.RandomUtil;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Service Implementation for managing UserControl.
 */
@Service
@Transactional
public class UserControlService {

    private final Logger log = LoggerFactory.getLogger(UserControlService.class);

    private final UserControlRepository userControlRepository;

    private final UserControlMapper userControlMapper;
    private final ApiService apiService;
    private final PasswordEncoder passwordEncoder;
    private final PersonneRepository personneRepository;
    RecepteurAlarmeService recepteurAlarmeService;
    ParametreAppService parametreAppService;
    UserListeBlancheRepository userListeBlancheRepository;

    public UserControlService(UserListeBlancheRepository userListeBlancheRepository, ParametreAppService parametreAppService, RecepteurAlarmeService recepteurAlarmeService, ApiService apiService, UserControlRepository userControlRepository, UserControlMapper userControlMapper, PasswordEncoder passwordEncoder, PersonneRepository personneRepository) {
        this.userControlRepository = userControlRepository;
        this.userControlMapper = userControlMapper;
        this.passwordEncoder = passwordEncoder;
        this.apiService = apiService;
        this.recepteurAlarmeService = recepteurAlarmeService;
        this.parametreAppService = parametreAppService;
        this.userListeBlancheRepository = userListeBlancheRepository;
        this.personneRepository = personneRepository;
    }

    /**
     * Save a userControl.
     *
     * @param userControlDTO the entity to save
     * @return the persisted entity
     */
    public UserControlDTO save(UserControlDTO userControlDTO) {
        log.info("Request to save UserControl : {}", userControlDTO);
        UserControl userControl = userControlMapper.toEntity(userControlDTO);
        userControl = userControlRepository.save(userControl);
        return userControlMapper.toDto(userControl);
    }

    public UserExist verification(String login, String codePays, String password) {
        UserExist userExist = new UserExist();
        boolean restrictMode = false;
        ParametreApp parametreApp = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "MODE_RESTRICT");
        if (parametreApp != null) {
            if (parametreApp.getValeur().equals("0")) {
                restrictMode = false;
            } else if (parametreApp.getValeur().equals("1")) {
                Long nbreUser = userListeBlancheRepository.findCoundtByLogin(login);
                if (nbreUser > 0) {
                    restrictMode = false;
                } else {
                    restrictMode = true;
                    userExist = null;
                }

            }
        }
        //      List<UserControl> userControls = userControlRepository.findAllByCritera(login, codePays, password);
        log.info("Verification de la validite du user {}", login);
        if (restrictMode == false) {
            UserControl userCtrlDesactive = userControlRepository.findIfDesactive(login, codePays, TypeControl.DISABLE);
            UserControl userCtrlReinit = userControlRepository.findIfDesactive(login, codePays, TypeControl.RESET_PASSWORD);
            UserControl userCtrlRemoved = userControlRepository.findIfDesactive(login, codePays, TypeControl.USER_REMOVED);
            UserControl userAddAccount = userControlRepository.findIfDesactive(login, codePays, TypeControl.UPDATE_ACCOUNT);
            UserControl userUpdateData = userControlRepository.findIfDesactive(login, codePays, TypeControl.UPDATE_DATA);
            if (userCtrlRemoved != null) {
                userExist.setUserRemoved(true);
                userExist.setId(userCtrlRemoved.getId());
            } else {
                if (userCtrlDesactive != null) {
                    userExist.setDisabled(true);
                    userExist.setId(userCtrlDesactive.getId());
                }
                userCtrlReinit = userControlRepository.findIfDesactive(login, codePays, TypeControl.RESET_PASSWORD);
                if (userCtrlReinit != null) {
                    userExist.setPasswordReset(true);
                    //  userControl.setSupprime(true);
                    userExist.setId(userCtrlReinit.getId());
                    if (userCtrlReinit.getPassword() != null && userCtrlReinit.getPassword().equals(password)) {
                        userExist.setPasswordMatch(true);
                    }
                } else if (userAddAccount != null) {
                    userExist.setUpdateAccount(true);
                    userExist.setId(userAddAccount.getId());

                } else if (userUpdateData != null) {
                    userExist.setUpdateData(true);
                    userExist.setId(userUpdateData.getId());
                    userExist.setData(userUpdateData.getData());
                }
            }

            /*
        if(userControls!=null){
            userControls.forEach(userControl -> {
                if(userControl.getAction().equals(TypeControl.DISABLE)){
                    userExist.setDisabled(true);
                    userExist.setId(userControl.getId());
                }else {
                    userExist.setPasswordReset(true);
                    userControl.setSupprime(true);
                    userExist.setId(userControl.getId());
                }
            });
        }*/
            log.debug("Fin de la Verification de la validite du user {}", login);
            log.debug("Retour de la fonction {}", userExist.getDisabled());
        }

        return userExist;
    }

    public void resetPassword(String telephone, String login, String codePays, String typeActeur) {
        log.debug("Reinitialisation du mot de passe {}", login);
        try {
            UserControl userControl = new UserControl();
            userControl.setAction(TypeControl.RESET_PASSWORD);
            userControl.setLogin(login);
            userControl.setTypeActeur(typeActeur);
            userControl.setCodePays(codePays);
            String password = RandomUtil.generatePasswordNumeric();
            userControl.setPassword(RandomUtil.encodeHash(password));
            log.info("Renitialisation du mot de passe de " + login);
            if (typeActeur.equals("ENTREPRISE")) {
                apiService.sendMail(login, "Votre nouveau mot de passe est :" + password);
                apiService.envoiSms(codePays + "" + telephone, "Votre nouveau mot de passe est :" + password,"OTP",codePays);
            } else {
                apiService.envoiSms(codePays + "" + telephone, "Votre nouveau mot de passe est :" + password,"OTP",codePays);
            }
            log.info("Votre nouveau mot de passe est {}", password);

            List<UserControl> olds = userControlRepository.findAllByLoginAndCodePaysAndSupprimeFalse(userControl.getLogin(), userControl.getCodePays());
            if (olds != null) {
                olds.forEach(old -> {
                    old.setSupprime(true);
                    userControlRepository.save(old);
                });
            }

            userControlRepository.save(userControl);
            this.recepteurAlarmeService.sendAlarme("Mot de passe de l'utilisateur: " + login + " a ete reinitialise", "ACTIVE_CLIENT");
            log.info("fin de la reinitialisation du mot de passe de " + login);
        } catch (Exception e) {

        }

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

   

    public void addAccount(String login, String codePays, String typeActeur) {
        log.debug("add account " + login);
        UserControl userControl = new UserControl();
        userControl.setAction(TypeControl.UPDATE_ACCOUNT);
        userControl.setLogin(login);
        userControl.setTypeActeur(typeActeur);
        userControl.setCodePays(codePays);
        List<UserControl> olds = userControlRepository.findAllByLoginAndCodePaysAndActionSupprimeFalse(userControl.getLogin(), userControl.getCodePays(), TypeControl.UPDATE_ACCOUNT);
        if (olds != null) {
            olds.forEach(old -> {
                old.setSupprime(true);
                userControlRepository.save(old);
            });
        }
        userControlRepository.save(userControl);

    }

    public void disableUser(String login, String codePays, String typeActeur) {
        log.debug("désactivation de l'utilisateur " + login);
        UserControl userControl = new UserControl();
        userControl.setAction(TypeControl.DISABLE);
        userControl.setLogin(login);
        userControl.setTypeActeur(typeActeur);
        userControl.setCodePays(codePays);
        List<UserControl> olds = userControlRepository.findAllByLoginAndCodePaysAndSupprimeFalse(userControl.getLogin(), userControl.getCodePays());
        if (olds != null) {
            olds.forEach(old -> {
                old.setSupprime(true);
                userControlRepository.save(old);
            });
        }
        userControlRepository.save(userControl);
        this.recepteurAlarmeService.sendAlarme("Desactivation de l'utilisateur: " + login, "ACTIVE_CLIENT");
    }

    public void removeUser(String login, String codePays, String typeActeur) {
        log.debug("supprime de l'utilisateur " + login);
        UserControl userControl = new UserControl();
        userControl.setAction(TypeControl.USER_REMOVED);
        userControl.setLogin(login);
        userControl.setTypeActeur(typeActeur);
        userControl.setCodePays(codePays);
        List<UserControl> olds = userControlRepository.findAllByLoginAndCodePaysAndSupprimeFalse(userControl.getLogin(), userControl.getCodePays());
        if (olds != null) {
            olds.forEach(old -> {
                old.setSupprime(true);
                userControlRepository.save(old);
            });
        }
        userControlRepository.save(userControl);
        this.recepteurAlarmeService.sendAlarme("suppression de l'utilisateur: " + login, "ACTIVE_CLIENT");
    }

    /**
     * Get all the userControls.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<UserControlDTO> findAll(Pageable pageable) {
        log.debug("Request to get all UserControls");
        return userControlRepository.findAll(pageable)
                .map(userControlMapper::toDto);
    }

    /**
     * Get one userControl by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public UserControlDTO findOne(Long id) {
        log.debug("Request to get UserControl : {}", id);
        UserControl userControl = userControlRepository.findById(id).orElse(null);
        return userControlMapper.toDto(userControl);
    }

    /**
     * Delete the userControl by id.
     *
     * @param id the id of the entity
     */
    public Boolean delete(Long id) {
        log.debug("Request to delete UserControl : {}", id);
        UserControl userControl = userControlRepository.findById(id).orElse(null);
        if (userControl != null) {
            userControl.setSupprime(true);
            userControlRepository.save(userControl);
            return true;
        }
        return false;
        //userControlRepository.deleteById(id);
    }

    public Page<UserControlDTO> findAllByPersonne(Long personneId, ZonedDateTime datedebut, ZonedDateTime dateFin, Pageable pageable) {
        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalBilan = 0;
        List<UserControlDTO> userControlDTOS = null;
        String telephone="";
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

        if (personneId!=null){
            PersonneDTO personneDTO =personneRepository.findPersonneById(personneId);
            if(personneDTO!=null){
                telephone=personneDTO.getTelephone();
            }
        }

        if(datedebut!=null && dateFin!=null){
            totalBilan = userControlRepository.countUserControlByPersonne(telephone,datedebut,dateFin);
            userControlDTOS=userControlRepository.findUserControleByPersonneIdDate(telephone,datedebut,dateFin,taille,page);

        }else if(datedebut!=null){
            userControlDTOS=userControlRepository.findUserControleByPersonneIdDate(telephone,datedebut,datedebut,taille,page);

        }else {
            totalBilan = userControlRepository.countUserControlByPersonneWithoutDate(telephone);
            userControlDTOS=userControlRepository.findUserControleByPersonneId(telephone,taille,page);
        }
        Page<UserControlDTO> result=new PageImpl<>(userControlDTOS,pageable1,totalBilan);
        return result;
    }
    public List<UserControlDTO> findUserAutoriseCardless(String action,String telephone){
        List<UserControlDTO> checkUser=userControlRepository.findUserByAction(action,telephone);
        if(checkUser!=null){
            return checkUser;
        }
        return null;
    }
}
