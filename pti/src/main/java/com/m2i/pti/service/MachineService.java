package com.m2i.pti.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.m2i.pti.cryptographie.CryptageService;
import com.m2i.pti.domain.Machine;
import com.m2i.pti.domain.ParametreApp;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.VerifOtp;
import com.m2i.pti.domain.enumeration.TypeParametre;
import com.m2i.pti.mobile.dto.MDataReturn;
import com.m2i.pti.repository.MachineRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.repository.VerifOtpRepository;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.security.SecurityUtils;
import com.m2i.pti.security.otp.api.Base32;
import com.m2i.pti.service.dto.MachineDTO;
import com.m2i.pti.service.dto.SignedObjectVM;
import com.m2i.pti.service.mapper.MachineMapper;
import com.m2i.pti.service.mapper.UniteOrganisationMapper;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing Machine.
 */
@Service
@Transactional
public class MachineService {

    private final Logger log = LoggerFactory.getLogger(MachineService.class);

    private final MachineRepository machineRepository;
    @Value("${m2i.security.pti.clientAllow.file.windows}")
    private String clientAllowWindows;

    @Value("${m2i.security.pti.clientAllow.file.linux}")
    private String clientAllowLinux;

    private final MachineMapper machineMapper;

    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final UniteOrganisationService uniteOrganisationService;

    private final CryptageService cryptageService;

    private final UniteOrganisationMapper uniteOrganisationMapper;

    private final MailService mailService;

    private final ParametreAppService parametreAppService;

    private final ApiService apiService;

    private final VerifOtpRepository verifOtpRepository;

    public MachineService(MachineRepository machineRepository, MachineMapper machineMapper, UniteOrganisationRepository uniteOrganisationRepository, UniteOrganisationService uniteOrganisationService, CryptageService cryptageService, UniteOrganisationMapper uniteOrganisationMapper, MailService mailService, ParametreAppService parametreAppService, ApiService apiService, VerifOtpRepository verifOtpRepository) {
        this.machineRepository = machineRepository;
        this.machineMapper = machineMapper;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.uniteOrganisationService = uniteOrganisationService;
        this.cryptageService = cryptageService;
        this.uniteOrganisationMapper = uniteOrganisationMapper;
        this.mailService = mailService;
        this.parametreAppService = parametreAppService;
        this.apiService = apiService;
        this.verifOtpRepository = verifOtpRepository;
    }

    /**
     * Save a machine.
     *
     * @param machineDTO the entity to save
     * @return the persisted entity
     */
    /* public MachineDTO save(MachineDTO machineDTO) {
        log.info("Request to save Machine : {}", machineDTO);
        Machine machine = machineMapper.toEntity(machineDTO);
        machine = machineRepository.save(machine);
        return machineMapper.toDto(machine);
    }*/
    /**
     * Save a machine.
     *
     * @param machineDTO the entity to save
     * @return the persisted entity
     */
    public MachineDTO save(MachineDTO machineDTO) throws IOException {
        log.info("Request to save Machine : {}", machineDTO);
        Machine machine = machineMapper.toEntity(machineDTO);
        if (machine.getId() == null) {
            if (machine.getOrganisation() == null) {
                throw new CustomParameterizedException("L'organisation est invalide !");
            } else {
                InputStream is = null;
                BufferedReader bfReader = null;
                try {
                    is = new ByteArrayInputStream(machine.getDetailsMachine());
                    bfReader = new BufferedReader(new InputStreamReader(is));
                    String temp = null;
                    while ((temp = bfReader.readLine()) != null) {
                        log.info("Infos   ------" + temp);
                        UniteOrganisation organisation = machine.getOrganisation();
                        machine = new Gson().fromJson(temp, Machine.class);
                        machine.setOrganisation(organisation);
                        machine.setAdresseIp(machineDTO.getAdresseIp());
                        machine.setEmailCertificat(machineDTO.getEmailCertificat());
                        machine.setEmailCodeAcces(machineDTO.getEmailCodeAcces());
                        machine.setDetailsMachine(machineDTO.getDetailsMachine());
                        machine.setDetailsMachineContentType(machineDTO.getDetailsMachineContentType());
                        machine.setSupprime(false);
                        final String decodedHostId = new String(Base64.getDecoder().decode(machine.getHostId()));
                        final Machine mach = this.machineRepository.findOneByHostIdAndSupprime(decodedHostId,false);
                        if (mach != null && Util.isNullOrFalse(mach.getEnabled())) {
                            throw new CustomParameterizedException("Cette machine a déjà été enregistrée. Contactez votre administrateur pour l'activer.");
                        } else if (mach != null) {
                            throw new CustomParameterizedException("Cette machine a déjà été enregistrée et activée.");
                        }
                    }
                } catch (IOException e) {
                    System.out.print(e.getMessage());
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (Exception ex) {

                    }
                }
                //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
            }
            machine.setHostId(new String(Base64.getDecoder().decode(machine.getHostId())));
            machine.setUuid(UUID.randomUUID().toString().replace("-", ""));
            machine.setEnabled(false);
            machine = machineRepository.save(machine);
        }
        return machineMapper.toDto(machine);
    }

    /**
     * Get all the machines.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<MachineDTO> findAll() {
        log.info("Request to get all Machines");
        return machineRepository.findAll().stream()
                .map(machineMapper::toDto)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Delete the machine by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.info("Request to delete Machine : {}", id);
        Machine machine=machineRepository.findById(id).orElse(null);
        String newHostId=null;
        if(machine!=null){
            if(!machine.getEnabled()){
                newHostId=machine.getHostId()+"_OLD";
                machine.setSupprime(true);
                machine.setHostId(newHostId);
                machineRepository.save(machine);
            } else{
                throw new CustomParameterizedException("Echec de l'opération Veillez désactiver la machine!");
            }

        } else {
            throw new CustomParameterizedException("Machine introuvable");
        }
       // machineRepository.deleteById(id);
    }

    /**
     * Activation d'une machine, en générant le certificat
     *
     * @param machineDTO : La machine à activer
     * @return
     * @throws IOException
     */
    @Transactional
    public MachineDTO activate(MachineDTO machineDTO) throws IOException {
        log.info("Request to activate Machine : {}", machineDTO);
        Machine machine = this.machineRepository.findById(machineDTO.getId()).orElse(null);
        machine.setEnabled(false);
        machine = machineRepository.save(machine);
        final String p12Pass = Base32.random();
        machine.setTempPass(p12Pass);
        //this.cryptageService.generateKeys(machine.getOrganisation(), machineDTO, p12Pass);
        return machineMapper.toDto(machine);
    }

    @Transactional
    public MachineDTO retrieveCertificat(String uuid) throws IOException {
        log.info("Request to activate Machine : {}", uuid);
        Machine machine = this.machineRepository.findOneByUuid(uuid);
        if (machine == null) {
            throw new CustomParameterizedException("Impossible de trouver la machine identifier par {} ", uuid);
        }
        final String p12Pass = RandomUtil.generateAlphaNumeric(6);

        if (verifAllowMachineActivation()) {
            System.out.println("-----TG%S---------");
            System.out.println("--------------");
            apiService.activeMachine(machine.getUuid());
        }
        machine.setEnabled(true);
        machine = machineRepository.save(machine);
        /*        File file = this.cryptageService.generateKeys(machine.getOrganisation(), machineMapper.toDto(machine), p12Pass);
        if (file != null) {
            String allowFileName = (SystemUtils.IS_OS_LINUX ? clientAllowLinux : clientAllowWindows);
            File fileAllow = new File(allowFileName);
            String content = "";
            if (machine.getAdresseIp() != null) {
                content = "allow " + machine.getAdresseIp() + ";";
            }
            String pass = "";
            if (machine.getTempPass() != null)
                pass = machine.getTempPass();
            if (fileAllow.exists()) {
                log.info("Le fichier existe dejà %act%" + allowFileName);
                Util.readLineByLineAndWrite(allowFileName, content);
            } else {
                fileAllow.createNewFile();
                Util.readLineByLineAndWrite(allowFileName, content);
            }
            mailService.sendEmail(machine.getEmailCertificat(), "certificat", "Bonjour, Vous avez ci-joint le certificat pour acceder à la platemforme.", true, false, file);
            mailService.sendEmail(machine.getEmailCodeAcces(), "Code du certificat", "Bonjour, Le code d'importation du certificat est " + pass, false, false);
         */
        //}
        return machineMapper.toDto(machine);
    }

    /**
     * Fournie toutes les machines S'il s'agit d'un utilisateur de SFD, alors on
     * lui renvoie les machines de sa SFD, sinon si c'est un administrateur on
     * lui renvoie toutes les machines.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<MachineDTO> findAll(Pageable pageable, @RequestParam(required = false) String adresseIp) {
        log.info("Request to get all Machine" + adresseIp);
        if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN_SYSTEME)) {
            return machineRepository.findWithCriteria(pageable, adresseIp)
                    .map(machineMapper::toDto);
        } else {
            UniteOrganisation uniteOrganisation = uniteOrganisationService.getUoOfUserConnecte();
            if (Objects.isNull(uniteOrganisation)) {
                throw new CustomParameterizedException("Impossible de recupérer l'identifiant de votre structure.");
            }
            return machineRepository.findByOrganisationIdWithCriteria(pageable, uniteOrganisation.getId(), adresseIp)
                    .map(machineMapper::toDto);
        }
    }

    @Transactional(readOnly = true)
    public Page<MachineDTO> findAllOptimise(Pageable pageable, @RequestParam(required = false) String adresseIp) {
        log.info("Request to get all Machine" + adresseIp);

        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize()*pageable.getPageNumber());
        int totalMachine;
        List<MachineDTO> machineDTOS;

        Pageable pageable1= new Pageable() {
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

        if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN_SYSTEME)) {
            totalMachine = machineRepository.countWithCriteria(null,adresseIp);
            machineDTOS = machineRepository.findAllByCriteria(null,adresseIp,taille,page);
        } else {
            UniteOrganisation uniteOrganisation = uniteOrganisationService.getUoOfUserConnecte();
            if (Objects.isNull(uniteOrganisation)) {
                throw new CustomParameterizedException("Impossible de recupérer l'identifiant de votre structure.");
            }
            totalMachine = machineRepository.countWithCriteria(uniteOrganisation.getId(),adresseIp);
            machineDTOS = machineRepository.findAllByCriteria(uniteOrganisation.getId(), adresseIp,taille,page);
        }
        Page<MachineDTO> retour = new PageImpl<>(machineDTOS, pageable1, totalMachine);
        return retour;
    }


    /**
     * Get one machine by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public MachineDTO findOne(Long id) {
        Machine machine = machineRepository.findById(id).orElse(null);
        if (!SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN_SYSTEME)) {
            UniteOrganisation uniteOrganisation = uniteOrganisationService.getUoOfUserConnecte();
            if (Objects.isNull(uniteOrganisation)) {
                throw new CustomParameterizedException("Impossible de recupérer l'identifiant de votre structure.");
            }
            //L'utilisateur tente d'accéder à une machine d'une autre Organisation
            if (!Objects.equals(uniteOrganisation.getId(), machine.getOrganisation().getId())) {
                throw new CustomParameterizedException("Vous ne pouvez pas accéder aux informations de cette machine");
            }
        }
        return machineMapper.toDto(machine);
    }

    /**
     * Désactiver une machine
     *
     * @param id the id of the entity
     */
    public void disable(Long id) throws IOException {
        String allowFileName = (SystemUtils.IS_OS_LINUX ? clientAllowLinux : clientAllowWindows);
        File fileAllow = new File(allowFileName);
        log.warn("\n\n---Demande de suppression d'une machine : {}" + allowFileName, id);

        Machine machine = machineRepository.findById(id).orElse(null);
        // UniteOrganisation uniteOrganisation = uniteOrganisationService.getUoOfUserConnecte();

        String content = "";
        if (machine.getAdresseIp() != null) {
            content = "deny " + machine.getAdresseIp() + ";";
        }
        if (fileAllow.exists()) {
            Util.readLineByLineAndWrite(allowFileName, content);
        } else {
            fileAllow.createNewFile();
            Util.readLineByLineAndWrite(allowFileName, content);
        }
        //L'utilisateur tente d'accéder à une machine d'une autre Organisation
        /*   if (!Objects.equals(uniteOrganisation.getId(), machine.getOrganisation().getId())) {
                throw new CustomParameterizedException("Vous ne pouvez pas accéder aux informations de cette machine");
            }*/
        machine.setEnabled(false);
        machineRepository.save(machine);
    }

    /**
     * Service de verification de token
     *
     * @param loginData
     * @param token
     */
    public Boolean verifToken(final String loginData, String token) throws IOException {
        String userConnecter=SecurityUtils.getCurrentUserLogin();
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE);
        mapper.disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
        final SignedObjectVM obj;

        obj = mapper.readValue(loginData, SignedObjectVM.class);
        final SignedObjectVM signedObjectVM = SignedObjectVM.decode(obj);
        Machine machine = machineRepository.findOneByHostId(signedObjectVM.getHostId());
        if (machine != null) {
            String secret = machine.getTempPass();
            String tokenBack = RandomUtil.generateToken(secret);
            // if(token!=null&&token.length()<6) StringUtils.chomp()
            log.info("verification du token " + token + "/" + tokenBack + " avec le secret : " + secret + "   " + token.equals(tokenBack));
            if (!tokenBack.trim().equals(token.trim())) {
                log.info("Code validation incorrect");
                throw new CustomParameterizedException("Code de validation incorrect.");
            }
            VerifOtp verifOtp=verifOtpRepository.findByLoginAndHostIdAndOtp(userConnecter,signedObjectVM.getHostId(),token);
            log.debug("---------verifOtp----{ }----"+verifOtp);
            if(verifOtp!=null&& verifOtp.getOtp().equals(token)){
                log.info("Code validation déja utilser");
                throw new CustomParameterizedException("Code de validation déja utuliser.");
            }else {
                VerifOtp newVerifOtp=new VerifOtp();
                newVerifOtp.setHostId(signedObjectVM.getHostId());
                newVerifOtp.setLogin(userConnecter);
                newVerifOtp.setOtp(token);
                verifOtpRepository.save(newVerifOtp);
            }
        }
        return Boolean.TRUE;
    }

    public MDataReturn verifTokenPti(final String loginData, String token) throws IOException {
         log.debug("================verification du token================");
            log.debug("================loginData:================"+loginData);
        String hostId = new String(Base64.getDecoder().decode(loginData.trim()));
          log.debug("================Host Id:================"+hostId);
        MDataReturn mDataReturn = new MDataReturn();

        Machine machine = machineRepository.findOneByHostId(hostId);
        if (machine != null) {
            String secret = machine.getTempPass();
            String tokenBack = RandomUtil.generateToken(secret);
            // if(token!=null&&token.length()<6) StringUtils.chomp()
            log.info("verification du token " + token + "/" + tokenBack + " avec le secret : " + secret + "   " + token.equals(tokenBack));
            if (!tokenBack.trim().equals(token.trim())) {
                log.info("Code validation incorrect");
                mDataReturn.setDonnee("1");

            } else {
                mDataReturn.setDonnee("0");
            }
        }
  log.debug("================FIN verification du token================");
        return mDataReturn;
    }

    public Boolean verifAllowMachineActivation() {
        ParametreApp parametreApp = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "CERTIFICAT_CLIENT");
        System.out.println("test ------" + parametreApp);
        System.out.println("------------d---------");
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

    public MachineDTO updateMachineNew(MachineDTO machineDTO) {
        Machine machine=machineRepository.findById(machineDTO.getId()).orElse(null);
        machine.setAdresseIp(machineDTO.getAdresseIp());
        machine=machineRepository.save(machine);
        return machineMapper.toDto(machine);
    }
}
