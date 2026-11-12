package com.m2i.pti.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2i.pti.cryptographie.CryptageService;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.Jour;
import com.m2i.pti.domain.enumeration.Statut;
import com.m2i.pti.domain.enumeration.TypeParametre;
import com.m2i.pti.mobile.dto.MDataReturn;
import com.m2i.pti.repository.*;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.security.SecurityUtils;
import com.m2i.pti.service.dto.SignedObjectVM;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.security.otp.api.Base32;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Pour intercepter l'authenticvation des utilisaqteurs afin d'authentifier en
 * incluant la SFD
 */
@Component("userDetailsService")
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(UserDetailsService.class);
    private final CryptageService cryptageService;
    private final UserRepository userRepository;
    @Value("${m2i.security.pti.keys.path.windows}")
    private String prvKeysPathWindows;
    @Value("${m2i.security.pti.keys.path.linux}")
    private String prvKeysPathLinux;
    @Value("${m2i.security.pti.privatekeys.extension}")
    private String prvKeysExtension;

    private final MachineAuditEventRepository machineAuditEventRepository;

    private final MachineRepository machineRepository;

    private final ParametreAppService parametreAppService;

    private final HoraireAuthentificationService horaireAuthentificationService;

    private final VerifOtpRepository verifOtpRepository;
    EntityAuditEventRepository auditingEntityRepository;

    public UserDetailsService(EntityAuditEventRepository auditingEntityRepository, CryptageService cryptageService, UserRepository userRepository, MachineAuditEventRepository machineAuditEventRepository, MachineRepository machineRepository, ParametreAppService parametreAppService, HoraireAuthentificationService horaireAuthentificationService, VerifOtpRepository verifOtpRepository) {
        this.cryptageService = cryptageService;
        this.userRepository = userRepository;
        this.machineAuditEventRepository = machineAuditEventRepository;
        this.machineRepository = machineRepository;
        this.parametreAppService = parametreAppService;
        this.horaireAuthentificationService = horaireAuthentificationService;
        this.auditingEntityRepository = auditingEntityRepository;
        this.verifOtpRepository = verifOtpRepository;
    }

    /**
     * Authentication en surchargeant la methode loadUserByUsername
     */
    @Override
    public UserDetails loadUserByUsername(final String loginData) {
        ObjectMapper mapper = new ObjectMapper();
        int jsonExist=0;
        log.debug("verification des droits");
        //On ignore les champs inconnus dans l'objet JSON lors de la déserialisation
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        try {
            Optional<User> userFromDatabase = Optional.empty();
            if (loginData!=null && loginData.length()>0){
                jsonExist=loginData.indexOf("{");
            }
          if ((verifAuthentificationForteActivation() && jsonExist>-1)) {
                final SignedObjectVM obj = mapper.readValue(loginData, SignedObjectVM.class);
                final SignedObjectVM signedObjectVM = SignedObjectVM.decode(obj);
                userFromDatabase = this.userRepository.findOneByLogin(signedObjectVM.getLogin());
                if (userFromDatabase.isPresent()) {
                    User user = userFromDatabase.get();
                    if (!user.getActivated()) {
                        throw new CustomParameterizedException("Votre compte a été désactivé !");
                    }
                    List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
                            .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                            .collect(Collectors.toList());
                    UniteOrganisation organisation = user.getUniteOrganisation();
                    log.debug("-----Host Id---"+signedObjectVM.getHostId());
                    Machine machine = machineRepository.findOneByHostId(signedObjectVM.getHostId());
                    //todo: s'il s'agit de l'administrateur, ignore d'abord la vérification, mais pas pour l'instant !
                    if (user.getAuthorities().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AuthoritiesConstants.ADMIN))) {

                        if (machine != null && organisation != null) {
                            String secret = machine.getTempPass();
                            String token = user.getLogin();

                            /* if (machine.getOrganisation().getId() != organisation.getId()) {
                                throw new CustomParameterizedException("Cette machine n'appartient pas à votre organisation!");
                            }*/
                            if (Util.isNullOrFalse(machine.getEnabled())) {
                                addFailure(user);
                                throw new CustomParameterizedException("Votre machine a été désactivé!");
                            }
                            this.addMachineAuditEventService(machine, signedObjectVM.getLogin());
                        } else {
                            addFailure(user);
                            throw new CustomParameterizedException("Votre machine est introuvable!");
                        }

                         String p12Pass = Base32.random();

                        machine.setTempPass(p12Pass);
                        machineRepository.save(machine);
                        resetFailure(user);
                        verifHoraireAuthentification(user);
                        return new org.springframework.security.core.userdetails.User(user.getLogin(),
                                user.getPassword(),
                                grantedAuthorities);

                    }
                    verifHoraireAuthentification(user);
                    resetFailure(user);
                    return new org.springframework.security.core.userdetails.User(user.getLogin(),
                            user.getPassword(),
                            grantedAuthorities);
                } else {
                    throw new CustomParameterizedException("Identifiants invalides...!");
                }
            } else {//Authentification n'est pas prise en compte
                userFromDatabase = this.userRepository.findOneByLogin(loginData);
                if (userFromDatabase.isPresent()) {
                    User user = userFromDatabase.get();
                    resetFailure(user);
                    verifHoraireAuthentification(user);
                    if (!user.getActivated()) {
                        throw new CustomParameterizedException("Votre compte a été désactivé !");
                    }
                    List<GrantedAuthority> grantedAuthorities = user.getAuthorities().stream()
                            .map(authority -> new SimpleGrantedAuthority(authority.getName()))
                            .collect(Collectors.toList());

                    return new org.springframework.security.core.userdetails.User(user.getLogin(),
                            user.getPassword(),
                            grantedAuthorities);
                } else {
                    throw new CustomParameterizedException("Identifiants invalides!");
                }
            }
        } catch (IOException e) {
            log.error("Erreur d'authentification", e);
            throw new CustomParameterizedException("%%Les informations de connexion sont invalides, assurez-vous que votre machine a été authentifiée.");
        }
    }

    
    
     public MDataReturn getUserTempPass2(String data) {
        MDataReturn mDataReturn=new MDataReturn();
        String secret = "";
         String hostId = new String(Base64.getDecoder().decode(data.trim()));
            // Optional<User> userFromDatabase = Optional.empty();
            if ((verifAuthentificationForteActivation())) {
                Machine machine = machineRepository.findOneByHostId(hostId);
                if (machine != null) {
                     secret = machine.getTempPass();
                 
                      mDataReturn.setDonnee(secret);
                }
            }
        
        return mDataReturn;
    }


    public UserQrCode getUserTempPass(String data) {
        String userConnecter= SecurityUtils.getCurrentUserLogin();
        UserQrCode pass=new UserQrCode();
        String secret = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        try {
            // Optional<User> userFromDatabase = Optional.empty();
            if ((verifAuthentificationForteActivation())) {
                final SignedObjectVM obj = mapper.readValue(data, SignedObjectVM.class);
                final SignedObjectVM signedObjectVM = SignedObjectVM.decode(obj);
                Machine machine = machineRepository.findOneByHostId(signedObjectVM.getHostId());
                if (machine != null) {
                    secret = machine.getTempPass();
                }
                pass.setTempPass(secret);
                List<VerifOtp> listOtp=verifOtpRepository.findByLogin(userConnecter);
                if(listOtp.size()>0){
                    verifOtpRepository.deleteAll(listOtp);
                }

            }
        } catch (IOException e) {
            log.error("Erreur d'authentification", e);
            throw new CustomParameterizedException("%%Les informations de connexion sont invalides, assurez-vous que votre machine a été authentifiée.");
        }
        return pass;
    }

    /**
     *
     */
    public void addMachineAuditEventService(Machine machine, String login) {
        MachineAuditEvent machineAuditEvent = new MachineAuditEvent();
        machineAuditEvent.setMachine(machine);
        machineAuditEvent.setStatut(Statut.AUTHENTICATION_SUCCESS);
        machineAuditEvent.setAuditEventDate(Instant.now());
        machineAuditEvent.setPrincipal(login);

        EntityAuditEvent auditedEntity = new EntityAuditEvent();
        auditedEntity.setAction(Statut.AUTHENTICATION_SUCCESS.toString());
        auditedEntity.setEntityId(1L);
        auditedEntity.setEntityType("CONNEXION");
        auditedEntity.setModifiedBy(login);
        auditedEntity.setCommitVersion(1);
        auditedEntity.setModifiedDate(Instant.now());
        auditedEntity.setAdresse(machine.getAdresseIp());
        machineAuditEventRepository.save(machineAuditEvent);
        auditingEntityRepository.save(auditedEntity);
    }

    /**
     *
     */
    public void addFailure(User user) {
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

    /**
     *
     */
    public void resetFailure(User user) {
        user.setDateDerniereConnexion(ZonedDateTime.now());
        if (user.getNbrFailure() == null) {
            user.setNbrFailure(0);
        }
        if (user.getNbrFailure() > 0) {
            user.setNbrFailure(0);
        }
        userRepository.save(user);
    }

    /**
     * Verification de l'etat d'activation de l'authentification forte
     */
    public Boolean verifAuthentificationForteActivation() {
        ParametreApp parametreApp = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "AUTHENTIFICATION_FORTE");
        if (parametreApp != null) {
            String valeur = parametreApp.getValeur();
            if (valeur.equals("0")) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }


    public Boolean verifAllowMachineActivation(){
        ParametreApp parametreApp = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "ALLOW_MACHINE");
        if (parametreApp != null) {
            String valeur = parametreApp.getValeur();
            if (valeur.equals("0")) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }

    }

    /**
     *
     * @param user
     */
    public void verifHoraireAuthentification(User user) {
        if (user.getAuthorities().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AuthoritiesConstants.ADMIN))) {
            log.debug("Verif horaire authentification ...");
            UniteOrganisation organisation = user.getUniteOrganisation();
            if (organisation != null) {
                ProfilActeur profilActeur = organisation.getProfilActeur();
                if (profilActeur != null) {
                    List<HoraireAuthentification> horaireAuthentifications = horaireAuthentificationService.findAllBy(getDay(), profilActeur.getId());
                    log.debug("Nombre horaire : " + horaireAuthentifications.size());
                    if (!horaireAuthentifications.isEmpty()) {
                        HoraireAuthentification horaireAuthentification = horaireAuthentifications.stream().findFirst().get();
                        log.debug("Periode : " + horaireAuthentification.getDateDebut() + "  " + horaireAuthentification.getDateFin());
                        DateFormat formatter = new SimpleDateFormat("HH:mm");
                        try {
                            Time heureDebut = new Time(formatter.parse(horaireAuthentification.getHeureDebut()).getTime());
                            Time heureFin = new Time(formatter.parse(horaireAuthentification.getHeureFin()).getTime());
                            Time localTime = Time.valueOf(LocalTime.now());
                            if (heureDebut.after(localTime) || localTime.after(heureFin)) {
                                throw new CustomParameterizedException("Vous n'êtes pas autorisé à vous connecté à cette heure!");
                            }
                        } catch (ParseException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }
            }
        }
    }

    private Jour getDay() {
        Date now = new Date();
        List<String> days = Arrays.asList("DIMANCHE", "LUNDI", "MARDI", "MERCREDI", "JEUDI", "VENDREDI", "SAMEDI");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        int numDay = calendar.get(Calendar.DAY_OF_WEEK);
        String day = days.get(numDay - 1);
        return Jour.valueOf(day);
    }

}
