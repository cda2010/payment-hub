package com.m2i.pti.service;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.mobile.dto.MCompte;
import com.m2i.pti.mobile.dto.MInfos;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.mobile.dto.MPersonne;
import com.m2i.pti.repository.*;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.security.SecurityUtils;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.kafka.MessageProducer;
import com.m2i.pti.service.mapper.*;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.CustomSettlementException;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import jakarta.persistence.OptimisticLockException;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.cache.annotation.Cacheable;

/**
 * Service Implementation for managing Compte.
 */
@Service
@Transactional
public class CompteService {

    private final Logger log = LoggerFactory.getLogger(CompteService.class);
    private final TransactionTemplate transactionTemplate;
    private final CompteRepository compteRepository;
    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final CompteBanqueService compteBanqueService;
    CarteMapper carteMapper;
    private final CompteAutreService compteAutreService;

    private final CompteAutreMapper compteAutreMapper;

    private final CompteMapper compteMapper;

    private final CompteAutreRepository compteAutreRepository;

    private final EtiquetteCompteRepository etiquetteCompteRepository;

    private final EtiquetteCompteMapper etiquetteCompteMapper;

    private final UserService userService;

    private final UniteOrganisationService uniteOrganisationService;

    private final PieceRepository pieceRepository;

    private final PersonneMapper personneMapper;

    private final PersonneService personneService;

    private final ParametreAppService parametreAppService;

    private final PieceMapper pieceMapper;

    private final CompteBanqueMapper compteBanqueMapper;

    private final PersonneRepository personneRepository;
    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;
    EvenementOperationRepository evenementOperationRepository;
    private final UserRepository userRepository;
    EvenementOperationService evenementOperationService;
    private CompteAutre compte;
    private Object lock;
    private final MessageProducer messageProducer;

    public CompteService(CarteMapper carteMapper, MessageProducer messageProducer, EvenementOperationRepository evenementOperationRepository, EvenementOperationService evenementOperationService, TransactionTemplate transactionTemplate, CompteRepository compteRepository, UniteOrganisationRepository uniteOrganisationRepository, CompteBanqueService compteBanqueService, CompteAutreService compteAutreService, CompteAutreMapper compteAutreMapper, CompteMapper compteMapper, CompteAutreRepository compteAutreRepository, EtiquetteCompteRepository etiquetteCompteRepository, EtiquetteCompteMapper etiquetteCompteMapper, UserService userService, UniteOrganisationService uniteOrganisationService, PieceRepository pieceRepository, PersonneMapper personneMapper, PersonneService personneService, ParametreAppService parametreAppService, PieceMapper pieceMapper, CompteBanqueMapper compteBanqueMapper, PersonneRepository personneRepository, TypeUniteOrganisationRepository typeUniteOrganisationRepository, UserRepository userRepository) {
        this.compteRepository = compteRepository;
        this.carteMapper = carteMapper;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.compteBanqueService = compteBanqueService;
        this.compteAutreService = compteAutreService;
        this.compteAutreMapper = compteAutreMapper;
        this.compteMapper = compteMapper;
        this.compteAutreRepository = compteAutreRepository;
        this.etiquetteCompteRepository = etiquetteCompteRepository;
        this.etiquetteCompteMapper = etiquetteCompteMapper;
        this.userService = userService;
        this.uniteOrganisationService = uniteOrganisationService;
        this.pieceRepository = pieceRepository;
        this.personneMapper = personneMapper;
        this.personneService = personneService;
        this.parametreAppService = parametreAppService;
        this.pieceMapper = pieceMapper;
        this.compteBanqueMapper = compteBanqueMapper;
        this.transactionTemplate = transactionTemplate;
        this.personneRepository = personneRepository;
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.userRepository = userRepository;
        this.evenementOperationService = evenementOperationService;
        this.evenementOperationRepository = evenementOperationRepository;
        lock = new Object();
        this.transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        this.transactionTemplate.setTimeout(30); // 30 seco
        this.messageProducer = messageProducer;

    }

    public Compte saveCompte(CompteDTO compteDTO) {
        log.info("Request to save Compte : {}", compteDTO);
        Compte compte = compteMapper.toEntity(compteDTO);
        Compte cpte = compteRepository.save(compte);
        return cpte;
    }

    /**
     * Save a compte.
     *
     * @param compteDTO the entity to save
     * @return the persisted entity
     */
    public CompteDTO save(CompteDTO compteDTO) {
        log.info("Request to save Compte : {}", compteDTO);
        Compte compte = compteMapper.toEntity(compteDTO);
        if (compteDTO.getCompteAutreId() != null) {
            compte = compteRepository.save(compte);
            return compteMapper.toDto(compte);
        }
        return null;
    }

    /**
     * Enregistrement d'un compte
     *
     * @param compteDTO the entity to save
     * @return the persisted entity
     */
    public CompteDTO saveNew(CompteDTO compteDTO) {
        Compte compte = null;
        CompteAutre compteAutre = null;
        if (compteDTO.getId() == null) {
            if (compteDTO.getUniteOrganisationId() != null && compteDTO.getEtiquette() != null) {
                if (!compteRepository.findByUniteOrganisationAndEtiquette(uniteOrganisationRepository.findById(compteDTO.getUniteOrganisationId()).orElse(null), compteDTO.getEtiquette()).isEmpty()) {
                    throw new CustomParameterizedException("Un compte existe déja dans cette unité Organisation avec l'etiquette " + compteDTO.getEtiquette());
                }
            }
        } else {
            compte = compteRepository.findById(compteDTO.getId()).orElse(null);
            if (!compte.getEtiquette().equals(compteDTO.getEtiquette())
                    && compteDTO.getUniteOrganisationId() != null) {
                if (!compteRepository.findByUniteOrganisationAndEtiquette(uniteOrganisationRepository.findById(compteDTO.getUniteOrganisationId()).orElse(null), compteDTO.getEtiquette()).isEmpty()) {
                    throw new CustomParameterizedException("Un compte existe déja dans cette unité Organisation avec l'etiquette " + compteDTO.getEtiquette());
                }
            }
        }
        if (compte != null) {
            CompteBanqueDTO cptBdto = compteDTO.getCompteBanqueDTO();
            CompteAutreDTO cptAdto = compteDTO.getCompteAutreDTO();
            if (compte.getCompteBanque() != null) {
                cptBdto.setId(compte.getCompteBanque().getId());
                compteDTO.setCompteBanqueDTO(cptBdto);
            }
            if (compte.getCompteAutre() != null) {
                cptAdto.setId(compte.getCompteAutre().getId());
                compteDTO.setCompteAutreDTO(cptAdto);
            }
        }
        if (!compteDTO.isSelectCompte() && !compteDTO.isCreerCompte()) {

            compteDTO = this.saveCompteBanqueOrAutre(compteDTO);

        }

        if (compteDTO.isCreerCompte()) {

            compteDTO = this.saveCompteBanqueOrAutre(compteDTO);
        }

        return compteDTO;
    }

    /**
     * Save a compteBanqueOrAutre.
     *
     * @param compteDTO the entity to save
     * @return the persisted entity
     */
    public CompteDTO saveCompteBanqueOrAutre(CompteDTO compteDTO) {

        if (compteDTO.getCategorieCompte() == CategorieCompte.BANQUE) {
            // Client c = clientService.findClient(compteDTO.getClientId());
            //     String alias= this.getCRC32Checksum(compteDTO.getCompteBanqueDTO().getCodeAgence(), compteDTO.getValeurCompte(), c.getPersonne().getTelephone(), compteDTO.getCompteBanqueDTO().getCodeFse());
            CompteBanqueDTO compteBanqueDTO = compteDTO.getCompteBanqueDTO();
            // compteBanqueDTO.setAlias(alias);
            compteBanqueDTO = compteBanqueService.save(compteBanqueDTO);
            compteDTO.setCompteBanqueId(compteBanqueDTO.getId());
            compteDTO.setCompteBanqueDTO(compteBanqueDTO);
        }

        if (compteDTO.getCategorieCompte() == CategorieCompte.AUTRE) {

            CompteAutreDTO compteAutreDTO = compteDTO.getCompteAutreDTO();
            CompteAutre compteAutre = new CompteAutre();
            compteAutre.setCodeFse(compteAutreDTO.getCodeFse());
            compteAutre.setSolde(BigDecimal.ZERO);
            compteAutre.setVersion(0);
            CompteAutre compteAutreSave = compteAutreService.save(compteAutre);
            compteDTO.setCompteAutreId(compteAutreSave.getId());
            compteAutreDTO.setId(compteAutreSave.getId());
            compteDTO.setCompteAutreDTO(compteAutreDTO);

        }
        Compte updateCompte = compteMapper.toEntity(compteDTO);
        //compteAutreRepository.save(updateCompte.getCompteAutre());
        //compteRepository.save(updateCompte);

        log.debug("compte-----" + compteDTO);
        return compteDTO;
    }

    public boolean useTwoOperationAccount() {
        ParametreApp parametreApp = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "USE_TWO_OPERATION_ACCOUNT");

        if (parametreApp != null && parametreApp.getValeur().equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    @Transactional(readOnly = true)
    private EtiquetteCompteCode getEtiquetteCompte(String etiquette) {
        ParametreApp parametreApp = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "AFFICHER_COMPTE_PTI");
        String etiquetteFinale = etiquette;
        if (etiquette.indexOf("PTI") > 0) {
            etiquetteFinale = etiquette.substring(0, etiquette.indexOf("PTI"));
        }

        if (parametreApp != null && parametreApp.getValeur().equals("1")) {
            return EtiquetteCompteCode.valueOf(etiquette);
        } else {
            return EtiquetteCompteCode.valueOf(etiquetteFinale);
        }
    }

    /**
     * Get all the comptes.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<CompteDTO> findAll(Pageable pageable) {
        log.info("Request to get all Comptes");
        return compteRepository.findAll(pageable)
                .map(compteMapper::toDto);
    }

    /**
     * Get the comptes BY UO.
     *
     * @param id information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<CompteDTO> findByUO(Long id) {
        log.info("Request to get all Comptes");
        //UniteOrganisation uniteOrganisation = uniteOrganisationRepository.findById(id).orElse(null);
        UniteOrganisationDTO uniteOrganisation = uniteOrganisationRepository.findDtoById(id);
        TypeUniteOrganisationDTO typeUniteOrganisation = typeUniteOrganisationRepository.findDtoById(uniteOrganisation.getTypeUOId());
        ParametreApp parametreApp = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "AFFICHER_COMPTE_PTI");
        List<CompteDTO> compteDTOList = null;
        Boolean afficherComptePti = true;
        if (parametreApp != null) {
            String valeur = parametreApp.getValeur();
            if (valeur.equals("0")) {
                afficherComptePti = false;
            } else {
                afficherComptePti = true;
            }
        }
        if (afficherComptePti) {
            compteDTOList = compteRepository.findByUniteOrganisationOptimise(uniteOrganisation.getId());
        } else {
            compteDTOList = compteRepository.findByUniteOrganisationWithoutPtiOptimise(uniteOrganisation.getId());
        }

        for (CompteDTO compteDTO : compteDTOList) {
            compteDTO.setTransversal(false);
            EtiquetteCompteDTO etiquetteCompteDTO = etiquetteCompteMapper.toDto(etiquetteCompteRepository.findTop1ByCode(compteDTO.getEtiquette()));
            if (etiquetteCompteDTO != null && etiquetteCompteDTO.getNiveau() != null) {
                if (etiquetteCompteDTO.isTransversal()) {
                    if (typeUniteOrganisation.getNiveau() >= etiquetteCompteDTO.getNiveau()) {
                        if (uniteOrganisation.isEstSiege().equals(true)) {
                            compteDTO.setTransversal(false);
                        } else {
                            compteDTO.setTransversal(true);
                        }
                    }
                }
            }
            if (etiquetteCompteDTO != null && etiquetteCompteDTO.getLibelle() != null) {
                compteDTO.setLibelleEtiquette(etiquetteCompteDTO.getLibelle());
            }

            if (compteDTO.isTransversal()) {
                if (compteDTO.getComptePincipaleId() != null) {//Recherche du compte principale
                    Compte comptePrincipal = compteRepository.findById(compteDTO.getComptePincipaleId()).orElse(null);
                    compteDTO.setValeurCompte(comptePrincipal.getValeurCompte());
                }
                compteDTO.setSelectCompte(true);
                compteDTO.setCreerCompte(false);
            } else {
                compteDTO.setCreerCompte(true);
                compteDTO.setSelectCompte(false);
            }
            if (compteDTO.getCompteAutreId() != null) {
                compteDTO.setSoldeCompte(compteAutreRepository.getSoldeById(compteDTO.getCompteAutreId()));
            }
            log.debug("compteDTO.getCompteAutreId()" + compteDTO.getCompteAutreId());

        }
        return compteDTOList;
    }

    /**
     * @return
     */
    @Transactional(readOnly = true)
    public CompteDTO getValeurCompte() {
        Long max = compteRepository.getMaxId();
        String code;
        /*        if (max > 0) {
            Compte lastCompte = compteRepository.findById(max).orElse(null);
            Integer lastId = Integer.parseInt(lastCompte.getValeurCompte());
            code = String.format("%09d", lastId + 1);
        } else {
            code = String.format("%09d", 1);
        }*/
        code = uniteOrganisationService.getString(max);
        CompteDTO compteDTO = new CompteDTO();
        compteDTO.setValeurCompte(code);
        return compteDTO;
    }

    /**
     * Get all the comptes.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<CompteDTO> findAll1(Pageable pageable, Long organisationId, String etiquette, CategorieCompte categorieCompte, String valeurCompte) {
        log.info("Request to get all Comptes");
        Page<CompteDTO> page;
        List<CompteDTO> listCpt;
        User user = userService.getUserWithAuthorities();
        if (organisationId == null && user.getAuthorities().stream().map(authority -> authority.getName()).collect(Collectors.toList()).contains(AuthoritiesConstants.ADMIN_SYSTEME)) {
            if (categorieCompte != null && etiquette != null && valeurCompte != null) {
                page = compteRepository.findByEtiquette(etiquette, pageable)
                        .map(compteMapper::toDto);
            } else if (categorieCompte != null) {
                page = compteRepository.findByCategorieCompte(categorieCompte, pageable)
                        .map(compteMapper::toDto);
            } else if (etiquette != null) {
                page = compteRepository.findByEtiquette(etiquette, pageable)
                        .map(compteMapper::toDto);
            } else if (valeurCompte != null) {
                listCpt = compteMapper.toDto(compteRepository.findByValCptUp(valeurCompte));
                page = new PageImpl<>(listCpt, pageable, listCpt.size());
            } else {
                page = compteRepository.findAll(pageable)
                        .map(compteMapper::toDto);
            }
        } else {
            Long uoId = 0L;
            if (organisationId == null) {
                UniteOrganisation organisation = uniteOrganisationService.getUoOfUserConnecte();
                uoId = organisation.getId();
            } else {
                uoId = organisationId;
            }

            if (etiquette != null && categorieCompte != null && valeurCompte != null) {
                page = compteRepository.findByEtiquetteAndUniteOrganisationIdAndCategorieCompteAndValeurCompte(etiquette, uoId, categorieCompte, valeurCompte, pageable)
                        .map(compteMapper::toDto);
            } else if (etiquette != null) {
                page = compteRepository.findByEtiquetteAndUniteOrganisationId(etiquette, uoId, pageable)
                        .map(compteMapper::toDto);
            } else if (categorieCompte != null) {
                page = compteRepository.findByCategorieCompteAndUniteOrganisationId(categorieCompte, uoId, pageable)
                        .map(compteMapper::toDto);
            } else if (valeurCompte != null) {
                page = compteRepository.findByValeurCompteAndUniteOrganisationId(valeurCompte, uoId, pageable)
                        .map(compteMapper::toDto);
            } else {
                page = compteRepository.findByUniteOrganisation(uniteOrganisationRepository.findById(uoId).orElse(null), pageable)
                        .map(compteMapper::toDto);
            }
        }

        for (CompteDTO compteDTO : page) {
            if (compteDTO.getClientId() != null) {
                compteDTO.setEstClient(true);
            } else {
                compteDTO.setEstClient(false);
            }
            if (compteDTO.getUniteOrganisationId() != null) {
                compteDTO.setEstUO(true);
            } else {
                compteDTO.setEstUO(false);
            }
            CompteAutre compteAutre = new CompteAutre();
            if (compteDTO.getComptePincipaleId() == null) {
                if (compteDTO.getCompteAutreId() != null) {
                    compteAutre = compteAutreRepository.findById(compteDTO.getCompteAutreId()).orElse(null);
                }
            } else {
                Compte comptePrincipale = compteRepository.findById(compteDTO.getComptePincipaleId()).orElse(null);
                compteAutre = comptePrincipale.getCompteAutre();
            }

            if (compteAutre != null) {
                compteDTO.setSoldeCompte(compteAutre.getSolde());
            }
        }
        return page;
    }

    public Compte findByCompteFseUO(String cmpte, String codeFse) {
        System.out.println("Compte -------------------------------" + cmpte + "et codeFse" + codeFse);
        log.debug("Compte -------------------------------" + cmpte + "et codeFse" + codeFse);
        List<Compte> listcpte = compteRepository.findByValeurCompteAndCodeFseUo(cmpte, codeFse);

        for (Compte cpt : listcpte) {
            if (cpt.getComptePincipale() == null || cpt.getClient() != null) {
                return cpt;
            } else {
                return cpt.getComptePincipale();
            }

        }
        return null;

        /*
        if (cpte != null) {
            return cpte;
        } else {
            return null;
        }*/
    }

    /**
     * @param uoId
     * @param etiquette
     * @return
     */
    @Transactional(readOnly = true)
    public List<CompteDTO> findCompteAgenceSiegeAndOrMaster(Long uoId, String etiquette) {
        log.info("Request to get all Comptes");
        UniteOrganisation uniteOrganisationOld = uniteOrganisationRepository.findById(uoId).orElse(null);
        List<UniteOrganisation> uniteOrganisationList = new ArrayList<>();
        List<CompteDTO> compteDTOS = new ArrayList<>();
        //recherche de l'agence siege
        UniteOrganisation uniteOrganisationAgence = uniteOrganisationService.findUoParentByType(uniteOrganisationOld, TypeUO.AGENCE);
        if (uniteOrganisationAgence.isEstSiege() != null) {
            if (!uniteOrganisationAgence.isEstSiege()) {
                List<UniteOrganisation> uniteOrganisations = uniteOrganisationRepository.findByPereUOAndSupprimeIsFalse(uniteOrganisationAgence.getPereUO());
                for (UniteOrganisation uniteOrganisation : uniteOrganisations) {
                    if (uniteOrganisation.isEstSiege()) {
                        uniteOrganisationAgence = uniteOrganisation;
                    }
                }
            }
        }
        uniteOrganisationList.add(uniteOrganisationAgence);
        //rechercher le master de l'UO
        if (uniteOrganisationOld.getTypeUO().getNiveau() == 4) {
            UniteOrganisation uniteOrganisationMaster = uniteOrganisationService.findUoParentByType(uniteOrganisationOld, TypeUO.MASTER);
            uniteOrganisationList.add(uniteOrganisationMaster);
        }
        //Rechercher les comptes de L'agence siege et (ou) du Master
        for (UniteOrganisation uniteOrganisation : uniteOrganisationList) {
            List<CompteDTO> compteDTOList = this.findByUO(uniteOrganisation.getId());
            for (CompteDTO compteDTO : compteDTOList) {
                if (compteDTO.getComptePincipaleId() == null) {
                    if (compteDTO.getEtiquette().equals(etiquette)) {
                        compteDTOS.add(compteDTO);
                    }
                }
            }
        }
        return compteDTOS;
    }

    /**
     * Get one compte by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public CompteDTO findOne(Long id) {
        log.info("Request to get Compte : {}", id);
        Compte compte = compteRepository.findById(id).orElse(null);
        return compteMapper.toDto(compte);
    }

    @Transactional(readOnly = true)
    public CompteAutre findByValeurCompte(String valeurCompte) {
        log.info("Request to get Compte : {}", valeurCompte);
        List<Compte> comptes = compteRepository.findByValeurCompte(valeurCompte);

        if (comptes.size() > 0) {
            return comptes.get(0).getCompteAutre();
        }
        return null;
    }

    @Transactional(readOnly = true)
    public CompteAutreDTO findCompteAutreByValeurCompte(String valeurCompte) {
        log.info("Request to get Compte : {}", valeurCompte);
        return compteAutreRepository.getCompteAutre(valeurCompte);
    }

    @Transactional(readOnly = true)
    public Compte getCptOfClient(Long clientId, String valeurCompte) {
        log.info("Request to get Compte of client : {}", valeurCompte + clientId);
        List<Compte> compte = compteRepository.findByClientIdAndClasseCompte(clientId, ClasseCompte.PRINCIPAL);
        if (compte.size() > 0) {
            return compte.get(0);
        } else {
            return new Compte();
        }
    }

    @Transactional(readOnly = true)
    @Timed
    public HashMap getCountCptAutre(Long clientId, String valeurCompte) {
        HashMap<String, Integer> valu = new HashMap<>();
        Integer count = compteRepository.getCountCptAutre(clientId);
        valu.put("numero", count);
        if (valeurCompte != null) {
            List<Compte> cpt = compteRepository.findByValeurCompte(valeurCompte);
            if (cpt.size() > 0) {
                throw new CustomParameterizedException("Valeur compte existe déja!! Veuillez changer votre numéro de téléphone");
            }
        }
        return valu;
    }

    public Compte findCompteOne(Long id) {
        log.info("Request to get Compte : {}", id);
        Compte compte = compteRepository.findById(id).orElse(null);
        return compte;
    }

    @Transactional
    public CompteDTO changerEtatCompte(Long id) {
        Compte compte = compteRepository.findById(id).orElse(null);
        if (compte != null && compte.getCategorieCompte().equals(CategorieCompte.AUTRE)) {
            if (compte.getEtatCompte() == null || compte.getEtatCompte().equals(EtatCompte.FERME)) {
                compte.setEtatCompte(EtatCompte.OUVERT);
                compte = compteRepository.save(compte);
                if (compte.getClient() != null) {
                    if (compte.getClient().getPersonne() != null) {
                        PersonneDTO personne = personneService.findOne(compte.getClient().getPersonne().getId());
                        personne.setActivated(true);
                        personneService.save(personne);
                    }
                }
            } else if (compte.getCompteAutre() != null && (compte.getCompteAutre().getSolde() == null || compte.getCompteAutre().getSolde().doubleValue() == 0)) {
                compte.setEtatCompte(EtatCompte.FERME);
                compte.setStatutCompte(StatutCompte.DESACTIVE);

                String action = compte.getStatutCompte().equals(StatutCompte.DESACTIVE) ? "COMPTE_DESACTIVE" : "COMPTE_ACTIVE";
                ActionEventDTO actionEvt = new ActionEventDTO(compte.getId().toString(), SecurityUtils.getCurrentUserLogin(), "COMPTE", action);
                messageProducer.sendMessageActionEvent(actionEvt);

                compte = compteRepository.save(compte);
                if (compte.getClient() != null) {
                    if (compte.getClient().getPersonne() != null) {
                        PersonneDTO personne = personneService.findOne(compte.getClient().getPersonne().getId());
                        personne.setActivated(false);
                        personneService.save(personne);
                    }
                }
            }
        } else {
            throw new CustomParameterizedException("Compte intouvable.");
        }
        return compteMapper.toDto(compte);
    }

    @Transactional
    public CompteDTO changerStatutCompte(Long id) {
        Compte compte = compteRepository.findById(id).orElse(null);
        if (compte != null && compte.getCategorieCompte().equals(CategorieCompte.AUTRE)) {
            if (compte.getStatutCompte() != null && compte.getStatutCompte().equals(StatutCompte.ACTIVE)) {
                compte.setStatutCompte(StatutCompte.DESACTIVE);
            } else {
                compte.setStatutCompte(StatutCompte.ACTIVE);
            }
            compte = compteRepository.save(compte);
        }
        return compteMapper.toDto(compte);
    }

    @Transactional
    public CompteDTO changerStatutCompteBanque(Long id) {

        User curentUser = userRepository.findOneByLogin(SecurityUtils.getCurrentUserLogin().toLowerCase()).get();

        Compte compte = compteRepository.findById(id).orElse(null);
        if (compte != null && compte.getCategorieCompte().equals(CategorieCompte.BANQUE)) {

            if (compte.getStatutCompte() != null && compte.getStatutCompte().equals(StatutCompte.ACTIVE)) {
                compte.setStatutCompte(StatutCompte.DESACTIVE);
            } else {
                log.debug("***********----------OK-------***********");

                if (curentUser.getId() != null) {
                    if (compte.getCreatedBy() != null && compte.getCreatedBy().equals(curentUser.getLogin())) {
                        throw new CustomParameterizedException("Impossible de valider ce compte !!!");
                    }
                }
                compte.setStatutCompte(StatutCompte.ACTIVE);
                compte.setEtatCompte(EtatCompte.OUVERT);
                // operationService.envoiMsgOperation(typeServiceRepository.findTopByCode(TypeServiceLibelle.ACTIVE_COMPTE_BANK.name()),compte.getClient().getPersonne());

            }

            log.debug("***********-----------------***********" + compte.getStatutCompte());
            compte = compteRepository.save(compte);
        }

        return compteMapper.toDto(compte);
    }

    /**
     * Delete the compte by id.
     *
     * @param id the id of the entity
     */
    @Transactional
    public void delete(Long id) {
        log.info("Request to delete Compte : {}", id);
        compteRepository.deleteById(id);
    }

    /**
     * Compte banque de l'utilisateur connecte
     *
     * @param etiquette
     * @return
     */
    public CompteBanque getCompteBanqueOfOrganisationConnecte(String etiquette) {
        UniteOrganisation uniteOrganisation = uniteOrganisationService.getUoOfUserConnecte();
        UniteOrganisation uoAgence = uniteOrganisationService.findUoParentByType(uniteOrganisation, TypeUO.AGENCE);
        if (uoAgence != null) {

            CompteDTO cpte = compteRepository.findTop1ByUniteOrganisationAndEtiquette(uniteOrganisation.getId(), etiquette);
            Compte compte = compteMapper.toEntity(cpte);
            if (compte != null) {
                return compte.getCompteBanque();
            }
        }
        return null;
    }

    /**
     * Compte de l'utilisateur connecte en fonction de l'etiquette
     *
     * @param etiquette
     * @return
     */
    public Compte getCompteByEtiquette(EtiquetteCompteCode etiquette) {
        UniteOrganisation uniteOrganisation = uniteOrganisationService.getUoOfUserConnecte();
        if (uniteOrganisation != null) {
            CompteDTO cpte = compteRepository.findTop1ByUniteOrganisationAndEtiquette(uniteOrganisation.getId(), String.valueOf(EtiquetteCompteCode.CPTATTENTEOPAGENCEPTI));
            Compte compte = compteMapper.toEntity(cpte);
            return compte;
        }
        return null;
    }

    @Transactional(readOnly = true)
    public Compte findByCompteFse(String cmpte, String codeFse) {
        System.out.println("Compte -------------------------------" + cmpte + "et codeFse" + codeFse);
        log.debug("Compte -------------------------------" + cmpte + "et codeFse" + codeFse);
        Compte cpte = compteRepository.findByValeurCompteAndCodeFse(cmpte, codeFse);
        if (cpte != null) {
            return cpte;
        } else {
            return null;
        }
    }

    //@Cacheable(  value = "SoldeCompte", key = "#p0 + '|' + #p1", sync = true)
    @Transactional(readOnly = true)
    public Compte findByCompteFseOptimise(String cmpte, String codeFse) {
        System.out.println("Compte -------------------------------" + cmpte + "et codeFse" + codeFse);
        log.debug("Compte -------------------------------" + cmpte + "et codeFse" + codeFse);
        CompteDTO cpte = compteRepository.findByValeurCompteAndCodeFseOptimise(cmpte, codeFse);
        Compte compte = compteMapper.toEntity(cpte);
        if (cpte != null) {
            return compte;
        } else {
            return null;
        }
    }
    

    @Transactional(readOnly = true)
    public Compte findCompteFse(String cmpte, String codeFse) {
        System.out.println("Compte -------------------------------" + cmpte + "et codeFse" + codeFse);
        log.debug("Compte -------------------------------" + cmpte + "et codeFse" + codeFse);
        Compte cpte = compteRepository.findByValeurCompteAndCodeFse(cmpte, codeFse);
        if (cpte != null) {
            return cpte;
        } else {
            return null;
        }
    }

    /**
     * Compte de l'utilisateur connecte en fonction de l'etiquette
     *
     * @param etiquette
     * @return
     */
    public Compte getCompteOfOrganisationConnecte(EtiquetteCompteCode etiquette) {
        UniteOrganisation uniteOrganisation = uniteOrganisationService.getUoOfUserConnecte();
        if (uniteOrganisation != null) {

            EtiquetteCompteCode eti = getEtiquetteCompte(etiquette.name());
            CompteDTO cpte = compteRepository.findTop1ByUniteOrganisationAndEtiquette(uniteOrganisation.getId(), String.valueOf(eti));
            Compte compte = compteMapper.toEntity(cpte);
            /*
                Si le compte obtenu est associé à un comptePrincipale
                on utilisera le compte principale
             */
            if (compte != null) {
                if (compte.getComptePincipale() != null) {
                    return compte.getComptePincipale();
                } else {
                    return compte;
                }
            } else {
                throw new CustomParameterizedException("Compte introuvable,Il faut parametre le compte " + etiquette);
            }
        }
        return null;
    }

    /**
     * Compte de l'utilisateur connecte en fonction de l'etiquette
     *
     * @param etiquette
     * @return
     */
    public Compte getCompteOfOrganisationUser(UniteOrganisation uniteOrganisation, EtiquetteCompteCode etiquette) {

        if (uniteOrganisation != null) {
            EtiquetteCompteCode eti = getEtiquetteCompte(etiquette.name());
            CompteDTO cpte = compteRepository.findTop1ByUniteOrganisationAndEtiquette(uniteOrganisation.getId(), String.valueOf(eti));
            /*
                Si le compte obtenu est associé à un comptePrincipale
                on utilisera le compte principale
             */

            Compte compte = compteMapper.toEntity(cpte);
            if (cpte != null) {
                compte = compteRepository.findById(cpte.getId()).orElse(null);
                if (compte != null) {
                    if (compte.getComptePincipale() != null) {
                        return compte.getComptePincipale();
                    } else {
                        return compte;
                    }
                } else {
                    throw new CustomParameterizedException("Compte introuvable,Il faut parametre le compte " + etiquette);
                }
            }

        }
        return null;
    }

    /**
     * Compte d'une unite d'organisation en fonction de l'etiquette
     *
     * @param uniteOrganisation
     * @param etiquette
     * @return
     */
    public Compte getCompteOfOrganisation(UniteOrganisation uniteOrganisation, EtiquetteCompteCode etiquette) {
        if (uniteOrganisation != null) {
            EtiquetteCompteCode eti = getEtiquetteCompte(etiquette.name());
            CompteDTO cpte = compteRepository.findUniteOrganisationByAndEtiquette(uniteOrganisation.getId(), String.valueOf(eti));
            /*
                Si le compte obtenu est associé à un comptePrincipale
                on utilisera le compte principale
             */
            Compte compte = compteMapper.toEntity(cpte);
            if (compte != null) {
                if (compte.getCompteAutre() != null && compte.getCompteAutre().getId() != null) {
                    CompteAutre cptAutre = compteAutreService.findCompteAutreById(compte.getCompteAutre().getId());
                    compte.setCompteAutre(cptAutre);
                    compte.setCompteBanque(null);
                } else if (compte.getCompteBanque() != null && compte.getCompteBanque().getId() != null) {
                    CompteBanque cptBque = compteBanqueService.findByCompteId(compte.getCompteBanque().getId());
                    compte.setCompteBanque(cptBque);
                    compte.setCompteAutre(null);
                }
                if (compte.getComptePincipale() != null) {
                    return compte.getComptePincipale();
                } else {
                    return compte;
                }
            } else {
                throw new CustomParameterizedException("Le compte " + etiquette.name() + " de " + uniteOrganisation.getLibelleUniteOrganisation() + " est introuvable!");
            }
        }
        return null;
    }

    public Compte getCompteOfOrganisationEtiquette(UniteOrganisation uniteOrganisation, EtiquetteCompteCode etiquette) {
        if (uniteOrganisation != null) {
            EtiquetteCompteCode eti = getEtiquetteCompte(etiquette.name());
            CompteDTO cpte = compteRepository.findUniteOrganisationByAndEtiquette(uniteOrganisation.getId(), String.valueOf(eti));
            Compte compte = compteMapper.toEntity(cpte);
            if (compte != null) {
                if (compte.getCompteAutre() != null && compte.getCompteAutre().getId() != null) {
                    CompteAutre cptAutre = compteAutreService.findCompteAutreById(compte.getCompteAutre().getId());
                    compte.setCompteAutre(cptAutre);
                    compte.setCompteBanque(null);
                } else if (compte.getCompteBanque() != null && compte.getCompteBanque().getId() != null) {
                    CompteBanque cptBque = compteBanqueService.findByCompteId(compte.getCompteBanque().getId());
                    compte.setCompteBanque(cptBque);
                    compte.setCompteAutre(null);
                }
                if (compte.getComptePincipale() != null) {
                    return compte.getComptePincipale();
                } else {
                    return compte;
                }
            }
        }
        return null;
    }

    /*
     * @param uniteOrganisation
     * @param etiquette
     * @return
     */
    public Compte getCompteOfOrganisationPTI(UniteOrganisation uniteOrganisation, EtiquetteCompteCode etiquette) {
        if (uniteOrganisation != null) {
            CompteDTO cpte = compteRepository.findUniteOrganisationByAndEtiquette(uniteOrganisation.getId(), etiquette.name());
            Compte compte = compteMapper.toEntity(cpte);
            if (compte != null) {
                compte = compteRepository.findById(compte.getId()).orElse(null);
                if (compte.getComptePincipale() != null) {
                    return compte.getComptePincipale();
                } else {
                    return compte;
                }
            } else {
                throw new CustomParameterizedException("Le compte " + etiquette.name() + " de " + uniteOrganisation.getLibelleUniteOrganisation() + " est introuvable!");
            }
        }
        return null;
    }

    /**
     * @param uniteOrganisation
     * @param etiquette
     * @return
     */
    public CompteBanque getCompteBanqueOfOrganisation(UniteOrganisation uniteOrganisation, EtiquetteCompteCode etiquette) {
        if (uniteOrganisation != null) {
            CompteDTO cpte = compteRepository.findTop1ByUniteOrganisationAndEtiquette(uniteOrganisation.getId(), String.valueOf(EtiquetteCompteCode.CPTATTENTEOPAGENCEPTI));

            Compte compte = compteMapper.toEntity(cpte);
            return compte.getCompteBanque();
        }
        return null;
    }

    /**
     * Compte autre de l'utilisateur connecte en fonction de l'etiquette
     *
     * @param etiquette
     * @return
     */
    public CompteAutre getCompteAutreOfOrganisationConnecte(String etiquette) {
        UniteOrganisation uniteOrganisation = uniteOrganisationService.getUoOfUserConnecte();
        if (uniteOrganisation != null) {
            CompteDTO cpte = compteRepository.findTop1ByUniteOrganisationAndEtiquette(uniteOrganisation.getId(), String.valueOf(etiquette));
            CompteAutre compteAutre = compteAutreRepository.findCompteAutreById(cpte.getCompteAutreId());
            Compte compte = compteMapper.toEntity(cpte);
            compte.setCompteAutre(compteAutre);
            if (compte != null) {
                return compte.getCompteAutre();
            }
        }
        return null;
    }

    /**
     * Compte autre de l'utilisateur connecte en fonction de l'etiquette
     *
     * @param etiquette
     * @return
     */
    public CompteAutreDTO getCompteAutreOfUserConnecte(String etiquette) {
        CompteAutre compteAutre = new CompteAutre();
        if (this.getCompteAutreOfOrganisationConnecte(etiquette) != null) {
            compteAutre = this.getCompteAutreOfOrganisationConnecte(etiquette);
        }
        return compteAutreMapper.toDto(compteAutre);
    }

    /**
     * Recherche des comptes d'un client
     *
     * @param client
     * @return
     */
    public List<CompteDTO> findClientAllComptes(Client client) {
        List<Compte> comptes = compteRepository.findByClientId(client.getId());
        if (comptes != null) {
            List<CompteDTO> compteDTOS = new ArrayList<>();
            comptes = comptes.stream().filter(distinctByKey(p -> p.getValeurCompte())).collect(Collectors.toList());
            for (Compte c : comptes) {
                CompteDTO compteDTO = compteMapper.toDto(c);
                if (compteDTO.getCompteBanqueId() != null) {
                    compteDTO.setCompteBanqueDTO(compteBanqueMapper.toDto(c.getCompteBanque()));
                } else if (compteDTO.getCompteAutreId() != null) {
                    compteDTO.setCompteAutreDTO(compteAutreMapper.toDto(c.getCompteAutre()));
                } else if (compteDTO.getCarteId() != null) {

                    compteDTO.setCarteDTO(carteMapper.toDto(c.getCarte()));
                }

                compteDTOS.add(compteDTO);
            }
            return compteDTOS;
        }
        return Collections.emptyList();
    }

    public List<CompteDTO> findClientAllComptes2(Client client) {
        List<Compte> comptes = compteRepository.findByClientIdAndStatut(client.getId());
        if (comptes != null) {
            List<CompteDTO> compteDTOS = new ArrayList<>();
            comptes = comptes.stream().filter(distinctByKey(p -> p.getValeurCompte())).collect(Collectors.toList());
            for (Compte c : comptes) {
                CompteDTO compteDTO = compteMapper.toDto(c);
                if (compteDTO.getCompteBanqueId() != null) {
                    compteDTO.setCompteBanqueDTO(compteBanqueMapper.toDto(c.getCompteBanque()));
                } else if (compteDTO.getCompteAutreId() != null) {
                    compteDTO.setCompteAutreDTO(compteAutreMapper.toDto(c.getCompteAutre()));
                } else if (compteDTO.getCarteId() != null) {

                    compteDTO.setCarteDTO(carteMapper.toDto(c.getCarte()));
                }

                compteDTOS.add(compteDTO);
            }
            return compteDTOS;
        }
        return Collections.emptyList();
    }

    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public CompteAutre debiterCompteAutre(CompteAutre cc, BigDecimal montant) {

        return (CompteAutre) transactionTemplate.execute(new TransactionCallback() {

            // the code in this method executes in a transactional context
            public Object doInTransaction(TransactionStatus status) {

                CompteAutre ca = debiterCompteAutrePTI(cc, montant);

                return ca;
            }
        });

    }

    public CompteAutre crediterCompteAutre(CompteAutre cc, BigDecimal montant) {

        return (CompteAutre) transactionTemplate.execute(new TransactionCallback() {

            // the code in this method executes in a transactional context
            public Object doInTransaction(TransactionStatus status) {

                CompteAutre ca = crediterCompteAutrePTI(cc, montant);
                return ca;
            }
        });

    }

    public CompteAutre crediterCompteAutrePTI(CompteAutre cc, BigDecimal montant) {
        CompteAutre ca = null;
        CompteAutre retour = null;
        int ret = 0;
        BigDecimal moinsUn = new BigDecimal("-1");
        BigDecimal mtacrediter = montant.abs().multiply(moinsUn).setScale(0, RoundingMode.HALF_UP);
        try {
            if (cc != null) {
                CompteAutre cpte = compteAutreRepository.findById(cc.getId()).orElse(null);
                ca = cpte;
                if (montant == null || BigDecimal.ZERO.equals(montant)) {//Pour un débit du compte de zéro franc on l'ignore au lieu de générer une exeption!
                    return retour;
                }
                if (!Util.estPositifStrict(montant)) {//Si le montant est null ou négatif
                    return retour;
                }

                retour = this.compteAutreRepository.save(ca.depositPTI(mtacrediter));

            } else {
                throw new CustomParameterizedException("Le compte client est introuvable");
            }
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {

            throw new CustomSettlementException("Serveur surchargé");

        }

        return retour;
    }

    public CompteAutre debiterCompteAutrePTI(CompteAutre cc, BigDecimal montant) {
        CompteAutre ca;
        BigDecimal mtadebiter = null;
        int ret = 0;
        CompteAutre retour = null;
        try {
            if (cc != null) {
                CompteAutre cpte = compteAutreRepository.findById(cc.getId()).orElse(null);
                // compte= compteAutreRepository.findById(cc.getCompteAutre().getId()).orElse(null);

                if (Util.estNullOuZero(montant)) {//Pour un débit du compte de zéro franc on l'ignore au lieu de générer une exeption!
                    return retour;
                }
                if (!Util.estPositifStrict(montant)) {//Si le montant est null ou négatif
                    throw new CustomParameterizedException("Impossible de d\u00e9diter le compte d'un montant invalide : " + montant + ".");
                }
                BigDecimal moinsUn = new BigDecimal("-1");

                mtadebiter = montant.abs().multiply(moinsUn).setScale(0, RoundingMode.HALF_UP);
                retour = this.compteAutreRepository.save(cpte.withdrawPTI(mtadebiter));

                //  transactionComptePtiService.saveTransactionOfCompte(cc, montant, TypeTransactionCompte.DEBIT, operation);
            } else {
                throw new CustomParameterizedException("Le compte est introuvable");
            }

        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {

            throw new CustomSettlementException("Serveur surchargé");
        }

        return retour;
    }

    /**
     * Methode permetant de debiter un compte PTI
     *
     * @param cc
     * @param montant
     * @return
     * @throws CustomParameterizedException
     */
    @Transactional
    public CompteAutre debiterCompteAutrexx(CompteAutre cc, BigDecimal montant) throws CustomParameterizedException {
        CompteAutre retour;
        if (cc != null) {
            //compte= compteAutreRepository.findById(cc.getId()).orElse(null);
            BigDecimal solde = null;
            BigDecimal mtadebiter = null;
            synchronized (this) {
                if (Util.estNullOuZero(montant)) {//Pour un débit du compte de zéro franc on l'ignore au lieu de générer une exeption!
                    return cc;
                }
                if (!Util.estPositifStrict(montant)) {//Si le montant est null ou négatif
                    throw new CustomParameterizedException("Impossible de d\u00e9diter le compte d'un montant invalide : " + montant + ".");
                }
                solde = cc.getSolde();
                cc.setSolde(Util.getZeroIfNull(solde));
                BigDecimal moinsUn = new BigDecimal("-1");

                mtadebiter = montant.abs().multiply(moinsUn).setScale(0, RoundingMode.HALF_UP);

                solde = solde.subtract(mtadebiter);
                cc.setSolde(solde);
                retour = compteAutreRepository.save(cc);

            }

        } else {
            throw new CustomParameterizedException("Le compte est introuvable");
        }
        return retour;
    }

    public CompteAutre debiterCompte(Compte cc, BigDecimal montant) {

        return (CompteAutre) transactionTemplate.execute(new TransactionCallback() {

            // the code in this method executes in a transactional context
            public Object doInTransaction(TransactionStatus status) {
                //   CompteAutre ca = null;

                CompteAutre ca = debiterCompteT(cc, montant);

                return ca;
            }
        });

    }

    public CompteAutre debiterCompteT(Compte cc, BigDecimal montant) throws CustomParameterizedException {
        CompteAutre ca;
        CompteAutre retour = null;
        int ret = 0;
        try {
            if (cc != null) {
                CompteAutre cpte = compteAutreRepository.findById(cc.getCompteAutre().getId()).orElse(null);

                ca = cpte;
                if ((cc.getEtatCompte() != null && cc.getEtatCompte().equals(EtatCompte.FERME)) || (cc.getStatutCompte() != null && cc.getStatutCompte().equals(StatutCompte.DESACTIVE))) {
                    throw new CustomParameterizedException("Impossible  de d\u00e9diter le compte, compte desactive ou ferme ");
                } else {

                    if (Util.estNullOuZero(montant)) {//Pour un débit du compte de zéro franc on l'ignore au lieu de générer une exeption!
                        return null;
                    }
                    if (!Util.estPositifStrict(montant)) {//Si le montant est null ou négatif
                        throw new CustomParameterizedException("Impossible de d\u00e9diter le compte d'un montant invalide : " + montant + ".");
                    }
                    retour = this.compteAutreRepository.save(ca.withdraw(montant));

                }

                //  transactionComptePtiService.saveTransactionOfCompte(cc, montant, TypeTransactionCompte.DEBIT, operation);
            } else {
                throw new CustomParameterizedException("Le compte est introuvable");
            }

        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            throw new CustomSettlementException("Serveur surchargé veuillez essayer à nouveau");
        }

        return retour;
    }

    /**
     * Vérifier la disponibilite d'un montant dans un compte
     *
     * @param cc
     * @param montant
     * @return
     */
    public boolean peutDebiterCompteXXXXXX(CompteAutre cc, BigDecimal montant) {
        if (cc != null) {

            if (!Util.estPositifStrict(montant)) {//Si le montant est null ou négatif
                throw new CustomParameterizedException("Impossible de d\u00e9diter le compte d'un montant invalide : " + montant + ".");
            }

            cc.setSolde(Util.getZeroIfNull(cc.getSolde()));
            log.info("Montant ..." + montant);
            log.info("Solde ..." + cc.getSolde());
            if (!Util.isBiggerOrEqualThan(cc.getSolde(), montant)) {
                return Boolean.FALSE;
            }
        } else {
            throw new CustomParameterizedException("Le compte est introuvable");
        }
        return Boolean.TRUE;
    }

    @Transactional(readOnly = true)
    public boolean peutDebiterCompte(Compte ca, BigDecimal montant) {
        if (ca.getEtiquette().equals(EtiquetteCompteCode.CPTECLIENT.name())) {
            if ((ca.getEtatCompte() != null && ca.getEtatCompte().equals(EtatCompte.FERME)) || (ca.getStatutCompte() != null && ca.getStatutCompte().equals(StatutCompte.DESACTIVE))) {
                throw new CustomParameterizedException("le compte est fermé ou desactivé");
            }
        }

        if (ca == null) {
            throw new CustomParameterizedException("Le compte est introuvable");
        }
        CompteAutre cc = null;
        EtiquetteCompte eti = etiquetteCompteRepository.findTop1ByCode(ca.getEtiquette());
        if (eti.getEstDebiteur() != null && eti.getEstDebiteur()) {
            return Boolean.TRUE;
        }
        /*Somme des evenements*/
        BigDecimal bgEve = evenementOperationRepository.findSommeEvenementOperation(ca.getId(), TypeEvenementOperation.DOIT.toString());
        BigDecimal moinsUn;
        cc = compteAutreRepository.findCompteAutreById(ca.getCompteAutre().getId());
        if (cc != null) {

            if (!Util.estPositifStrict(montant)) {//Si le montant est null ou négatif
                throw new CustomParameterizedException("Impossible de d\u00e9diter le compte d'un montant invalide : " + montant + ".");
            }
            if (ca.getEtiquette().endsWith("PTI")) {
                moinsUn = new BigDecimal("-1");
            } else {
                moinsUn = new BigDecimal("1");
            }

            cc.setSolde(Util.getZeroIfNull(cc.getSolde()));
            BigDecimal solde;
            /**
             * Vérification du solde
             */

            solde = cc.getSolde().add(bgEve);

            log.info("Solde ..." + solde);
            log.info("Montant ..." + montant);
            if (!Util.isBiggerOrEqualThan(solde.multiply(moinsUn), montant)) {
                return Boolean.FALSE;
            }
        } else {
            throw new CustomParameterizedException("Le compte est introuvable");
        }
        return Boolean.TRUE;
    }

    @Transactional(readOnly = true)
    public boolean peutDebiterComptePTI(CompteAutre cc, BigDecimal montant) {
        if (cc != null) {

            if (!Util.estPositifStrict(montant)) {//Si le montant est null ou négatif
                throw new CustomParameterizedException("Impossible de d\u00e9diter le compte d'un montant invalide : " + montant + ".");
            }
            cc.setSolde(Util.getZeroIfNull(cc.getSolde()));
            log.info("Solde ..." + cc.getSolde());
            log.info("montant ..." + montant);
            BigDecimal moinsUn = new BigDecimal("-1");
            if (!Util.isBiggerOrEqualThan(cc.getSolde().multiply(moinsUn), montant)) {
                return Boolean.FALSE;
            }
        } else {
            throw new CustomParameterizedException("Le compte est introuvable");
        }
        return Boolean.TRUE;
    }

    public CompteAutre crediterCompte(Compte cc, BigDecimal montant) {

        return (CompteAutre) transactionTemplate.execute(new TransactionCallback() {

            // the code in this method executes in a transactional context
            public Object doInTransaction(TransactionStatus status) {
                CompteAutre ca = crediterCompteT(cc, montant);
                return ca;
            }
        });

    }

    public CompteAutre crediterCompteT(Compte cc, BigDecimal montant) throws CustomParameterizedException {
        CompteAutre ca;
        CompteAutre retour = null;
        int ret = 0;
        try {
            if (cc != null) {
                CompteAutre cpte = compteAutreRepository.findById(cc.getCompteAutre().getId()).orElse(null);
                // compte= compteAutreRepository.findById(cc.getCompteAutre().getId()).orElse(null);
                ca = cpte;
                if (montant == null || BigDecimal.ZERO.equals(montant)) {//Pour un débit du compte de zéro franc on l'ignore au lieu de générer une exeption!
                    return null;
                }
                if (!Util.estPositifStrict(montant)) {//Si le montant est null ou négatif
                    return null;
                }

                if ((cc.getEtatCompte() != null && cc.getEtatCompte().equals(EtatCompte.FERME))) {
                    throw new CustomParameterizedException("Impossible  de crediter le compte, compte  ferme ");
                } else {

                    retour = this.compteAutreRepository.save(ca.deposit(montant));
                    if (retour != null) {
                        ret = 1;
                    }
                }

            } else {
                throw new CustomParameterizedException("Le compte client est introuvable");
            }
        } catch (OptimisticLockException | ObjectOptimisticLockingFailureException e) {
            throw new CustomParameterizedException("Serveur surchargé veuillez essayer à nouveau");
        }

        return retour;
    }

    /**
     * Créditer le compte d'un montant donné
     *
     * @param cc L'instance du compte à créditer
     * @param montant Le montant à créditer
     * @return Le compte avec un solde courant augmenté de montant
     * @throws CustomParameterizedException Au cas où le compte est introuvable
     * ou le solde est négatif(=0)
     */
    @Transactional
    public CompteAutre crediterCompteAutreXX(CompteAutre cc, BigDecimal montant) throws CustomParameterizedException {
        CompteAutre retour;
        if (cc != null) {

            synchronized (this) {
                // compte= compteAutreRepository.findById(cc.getId()).orElse(null);
                if (montant == null || BigDecimal.ZERO.equals(montant)) {//Pour un débit du compte de zéro franc on l'ignore au lieu de générer une exeption!
                    return cc;
                }
                if (!Util.estPositifStrict(montant)) {//Si le montant est null ou négatif
                    return cc;
                }
                BigDecimal solde = cc.getSolde();
                cc.setSolde(solde == null ? BigDecimal.ZERO : solde);
                BigDecimal moinsUn = new BigDecimal("-1");
                BigDecimal mtacrediter = montant.abs().multiply(moinsUn).setScale(0, RoundingMode.HALF_UP);
                cc.setSolde(solde.add(mtacrediter));
                retour = this.compteAutreRepository.save(cc);
            }

        } else {
            throw new CustomParameterizedException("Le compte client est introuvable");
        }
        return retour;
    }

    /**
     * Recupere le compte Operation Acteur utilisé pour la realisation d'une
     * opération
     *
     * @param operation
     * @return
     */
    @Transactional(readOnly = true)
    public Compte getCompteOperationActeur(Operation operation) {
        UniteOrganisation uniteOrganisationSource = operation.getPointVente().getUniteOrganisation();
        return getCompteOperationActeur(uniteOrganisationSource);
    }

    /**
     * @param organisationSrc
     * @return
     */
    @Transactional(readOnly = true)
    public Compte getCompteOperationActeur(UniteOrganisation organisationSrc) {

        Compte compteOperationActeur = this.getCompteOfOrganisation(organisationSrc, EtiquetteCompteCode.CPTEOPERATIONACTEUR);//compte reseau
        return compteOperationActeur;
    }

    @Transactional(readOnly = true)
    public Compte getCompteOperationActeur(UniteOrganisation organisationSrc, String etiquette, String valeurCompte) {
        Compte compteOperationActeur = this.getCompteOfOrganisation(organisationSrc, EtiquetteCompteCode.valueOf(etiquette));//compte reseau
        if (compteOperationActeur != null) {
            if (valeurCompte.equals(compteOperationActeur.getValeurCompte())) {
                return compteOperationActeur;
            }
        }
        return null;

    }

    @Transactional(readOnly = true)
    public Compte getCompteOperationActeur(UniteOrganisation organisationSrc, String etiquette) {

        // EtiquetteCompteCode eti = getEtiquetteCompte(etiquette);
        // etiquette = eti.name();
        Compte compteOperationActeur = this.getCompteOfOrganisation(organisationSrc, EtiquetteCompteCode.valueOf(etiquette));//compte reseau
        return compteOperationActeur;
    }

    public Compte getCompteOrganisation(UniteOrganisation organisationSrc, String valeurCompte) {

        Compte compte = compteRepository.findTop1ByUniteOrganisationAndValeurCompte(organisationSrc, valeurCompte);

        return compte;

    }

    /**
     *
     * =======
     *
     * }
     *
     * /**
     * >>>>>>> 9a5974b06940c2563e1a640e178e86631b3a90a2 Recherche d'un compte
     * existant d'une personne
     *
     * @param valeurCompte
     * @param personne
     * @return
     */
    @Transactional(readOnly = true)
    public Compte getCompteExistant(String valeurCompte, Personne personne) {
        List<Compte> compteExistantAll = compteRepository.findByValeurCompte(valeurCompte);
        if (compteExistantAll.isEmpty()) {
            return null;
        } else {
            for (Compte compteExistant : compteExistantAll) {
                Client clientOfCompte = compteExistant.getClient();
                if (clientOfCompte != null) {
                    Personne personneExist = clientOfCompte.getPersonne();
                    if (personne != null) {
                        if (personneExist.getTelephone().equals(personne.getTelephone())) {
                            return compteExistant;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param type
     * @param codeBanque
     * @param codeAgence
     * @param rib
     * @param valeurCompte
     * @return
     */
    public Page<PersonneAllDTO> findByCriteria(String type, String codeBanque, String codeAgence, String rib, String valeurCompte, Pageable pageable) {
        List<PersonneAllDTO> personnes = new ArrayList<>();
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
        int totalClient = 0;
        totalClient = personneRepository.countPersonne();
        if (type.equals("BANQUE")) {
            List<CompteBanque> compteBanques = compteBanqueService.findByCriteria(codeBanque, codeAgence, rib, valeurCompte);
            compteBanques.forEach(compteBanque -> {
                PersonneAllDTO p = findTitulaire(compteRepository.findByCompteBanque(compteBanque));
                if ((p != null)) {
                    List<PersonneAllDTO> verif = personnes.stream().filter(pers -> pers.getPersonneDTO().getTelephone().equals(p.getPersonneDTO().getTelephone())).collect(Collectors.toList());
                    if (verif.isEmpty()) {
                        personnes.add(p);
                    }
                }
            });
        } else {
            List<Compte> comptes = compteRepository.findByValeurCompte(valeurCompte);
            comptes.forEach(compte -> {
                PersonneAllDTO p = findTitulaire(compte);
                if ((p != null)) {
                    List<PersonneAllDTO> verif = personnes.stream().filter(pers -> pers.getPersonneDTO().getTelephone().equals(p.getPersonneDTO().getTelephone())).collect(Collectors.toList());
                    if (verif.isEmpty()) {
                        personnes.add(p);
                    }
                }
            });
        }
        Page<PersonneAllDTO> retour = new PageImpl<>(personnes, pageable1, totalClient);
        return retour;
    }

    /**
     * @param compte
     * @return
     */
    @Transactional(readOnly = true)
    private PersonneAllDTO findTitulaire(Compte compte) {
        if (compte != null) {
            if (compte != null) {
                Client client = compte.getClient();
                if (client != null) {
                    Personne personne = client.getPersonne();
                    if (personne != null) {
                        PersonneAllDTO personneAllDTO = new PersonneAllDTO();
                        PersonneDTO personneDTO = personneMapper.toDto(personne);
                        personneAllDTO.setPersonneMoraleDTO(personneService.getPersonneMorale(personneDTO));
                        personneAllDTO.setPersonnePhysiqueDTO(personneService.getPersonnePhysique(personneDTO));
                        personneAllDTO.setPersonneDTO(personneDTO);
                        Piece piece = pieceRepository.findTopByTitulaireId(personne.getId());
                        if (piece != null) {
                            personneAllDTO.setPieceDTO(pieceMapper.toDto(piece));
                        }
                        return personneAllDTO;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Service retournant le solde d'un compte interne de PTI sur la base du
     * numero de compte
     *
     * @param numero
     */
    @Transactional(readOnly = true)
    public BigDecimal getSolde(String numero) {
        Compte compte = compteRepository.findTopByValeurCompteAndCategorieCompte(numero, CategorieCompte.AUTRE);
        if (compte != null && compte.getCompteAutre() != null) {
            return compte.getCompteAutre().getSolde();
        }
        return BigDecimal.ZERO;
    }

    public Compte findComptePrincipal(Client client) {
        log.debug("cli" + client);
        Compte compte = compteMapper.toEntity(compteRepository.findTopByClientIdAndClasseCompte(client.getId(), ClasseCompte.PRINCIPAL.name()));
        return compte;
    }

    public String getCRC32Checksum(String agence, String numeroCompte, String msisdn, String bankCible) {
        String concatCpt = msisdn + bankCible + agence + numeroCompte;
        byte[] bytes = concatCpt.getBytes();
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        String valeur = String.format("%14d", crc32.getValue()).replace(' ', '0');
        return valeur;
    }

    @Transactional(readOnly = true)
    public MInfos findByalias(String alias, String codeFse) {
        log.debug("VERIFICATION DE L'ALIAS" + alias + "-" + codeFse);

        Compte compte = compteRepository.findByAlias(alias, codeFse);

        if (compte != null && compte.getCompteBanque() != null) {
            MCompte mcompte = new MCompte();
            MInfos minfo = new MInfos();
            MPersonne mpersonne = new MPersonne();
            mcompte.setCodeAgence(compte.getCompteBanque().getCodeAgence());
            mcompte.setNumeroCompte(compte.getValeurCompte());
            mcompte.setCodeBanque(compte.getCompteBanque().getCodeBanque());
            mcompte.setAlias(alias);
            mcompte.setTeneurCompte(codeFse);
            if (compte.getCompteBanque() != null) {
                mcompte.setTypeCompte(MTypeCompte.valueOf("BANQUE"));
            } else if (compte.getCompteAutre() != null) {
                mcompte.setTypeCompte(MTypeCompte.valueOf("AUTRE"));
            }

            if (compte.getClient() != null) {
                if (compte.getClient().getPersonne() != null && compte.getClient().getPersonne().getPersonnePhysique() != null) {
                    mpersonne.setNom(compte.getClient().getPersonne().getPersonnePhysique().getNomClient());
                }
                mpersonne.setTelephone(compte.getClient().getPersonne().getTelephone());
                mpersonne.setCodeTelephone(compte.getClient().getPersonne().getCodePaysTelephone());
                minfo.setCompte(mcompte);
                minfo.setTitulaire(mpersonne);
            }
            return minfo;
        }
        return null;
    }

    @Transactional(readOnly = true)
    public MInfos findByalias(String alias) {
        log.debug("VERIFICATION DE L'ALIAS" + alias);
        Compte compte = compteRepository.findByAlias(alias);
        if (compte != null && (compte.getCompteBanque() != null || compte.getCarte() != null)) {
            MCompte mcompte = new MCompte();
            MInfos minfo = new MInfos();
            MPersonne mpersonne = new MPersonne();

            mcompte.setNumeroCompte(compte.getValeurCompte());

            mcompte.setAlias(alias);

            if (compte.getCompteBanque() != null) {
                mcompte.setCodeAgence(compte.getCompteBanque().getCodeAgence());
                mcompte.setTeneurCompte(compte.getCompteBanque().getCodeFse());
                mcompte.setCodeBanque(compte.getCompteBanque().getCodeBanque());
                mcompte.setTypeCompte(MTypeCompte.valueOf("BANQUE"));
            } else if (compte.getCompteAutre() != null) {
                mcompte.setTeneurCompte(compte.getCompteAutre().getCodeFse());
                mcompte.setTypeCompte(MTypeCompte.valueOf("AUTRE"));
            } else {
                mcompte.setTeneurCompte(compte.getCarte().getCodeFse());
                mcompte.setTypeCompte(MTypeCompte.valueOf("CARTE"));
            }

            if (compte.getClient() != null) {
                if (compte.getClient().getPersonne() != null && compte.getClient().getPersonne().getPersonnePhysique() != null) {
                    mpersonne.setNom(compte.getClient().getPersonne().getPersonnePhysique().getNomClient());
                }
                mpersonne.setTelephone(compte.getClient().getPersonne().getTelephone());
                mpersonne.setCodeTelephone(compte.getClient().getPersonne().getCodePaysTelephone());
                minfo.setCompte(mcompte);
                minfo.setTitulaire(mpersonne);
            }
            return minfo;
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<CompteDTO> findCompteOfClient() {
        return compteMapper.toDto(compteRepository.findCompteOfClient());
    }

    public List<CompteDTO> findCompteOfClientActif() {
        return compteMapper.toDto(compteRepository.findCompteOfClientActif());
        //   return compteMapper.toDto(compteRepository.findCompteOfClientActif(StatutCompte.ACTIVE));
    }

    public List<CompteDTO> findCompteAllClientActif(Long limit, Long offset) {
        return compteRepository.findAllCompteClientActif(limit, offset);
        //   return compteMapper.toDto(compteRepository.findCompteOfClientActif(StatutCompte.ACTIVE));
    }

    public Long findNbreCompteActif() {
        return compteRepository.findNbreCompteActif();
        //   return compteMapper.toDto(compteRepository.findCompteOfClientActif(StatutCompte.ACTIVE));
    }

    public CompteDTO getCptOfvalCpt(String valeurCompte) {
        List<Compte> comptes = compteRepository.findByValeurCompte(valeurCompte);
        if (comptes.size() > 0) {
            return compteMapper.toDto(comptes.get(0));
        }
        return new CompteDTO();

    }

    @Transactional(readOnly = true)
    public Compte findIfCompteExiste(String numeroCompte, Client client) {
        Compte compte = compteRepository.findTopByValeurCompteAndClient(numeroCompte, client);
        return compte;
    }

    public Compte findIfCompteExisteWithClient(String numeroCompte, Client client) {
        CompteDTO cpte = compteRepository.findTopByValeurCompteAndClient(numeroCompte, client.getId());
        Compte compte = compteMapper.toEntity(cpte);
        return compte;
    }

    @Transactional(readOnly = true)
    public String findMaxNumeroCourt() {
        String compteMax = compteRepository.findMaxNumeroCourt();
        return compteMax;
    }

    public String getNumeroCompteOfClient(Long codeFse) {
        if (codeFse != null) {
            String numeroCourt = getNumeroCompteCourt();
            String valeurCpte = getNumeroCompte(numeroCourt, codeFse.toString());
            return valeurCpte;
        } else {
            throw new CustomParameterizedException("Fournisseur de service introuvable.");
        }

    }

    private String getNumeroCompteCourt() {

        String numeroCourt = findMaxNumeroCourt();
        String compte = null;
        if (numeroCourt != null) {
            Long numeroEnEntier = Long.parseLong(numeroCourt) + 1;
            compte = String.format("%08d", numeroEnEntier);
        }
        return compte;
    }

    private String getNumeroCompte(String numeroCourt, String codeFse) {
        /* le numï¿½ro de tï¿½lephone contitue le numero de compte*/
        String numeroRandom = RandomStringUtils.randomNumeric(1);
        String cptCompte = codeFse.concat(numeroCourt) + numeroRandom;
        return cptCompte;
    }

    public List<Compte> findByNumeroTel(String numeroTel) {
        List<Compte> comptes = compteRepository.findCompteByClientIsNotNullAndClient_Personne_Telephone(numeroTel);
        return comptes;
    }

    public List<Compte> findByUoId(UniteOrganisation organisation) {
        return compteRepository.findByUniteOrganisation(organisation);
    }

    public Compte findTopByValeurCompteAndEtiquette(String valeur, String etiquette) {
        return compteMapper.toEntity(compteRepository.findTopByValeurCompteAndEtiquette(valeur, etiquette));

    }

    Compte findByCompteAutreId(Long compteAutreId) {
        Compte compte = compteRepository.findByCompteAutreId(compteAutreId);
        return compte;
    }

    Compte findCompteAutreByOne(Long compteId) {
        CompteDTO cpte = compteRepository.findByNativeId(compteId);
        Compte compte = compteMapper.toEntity(cpte);
        return compte;
    }

    Compte findCompteBanqueByOne(Long compteId) {
        CompteDTO cpte = compteRepository.findCompteBanqueByNativeId(compteId);
        Compte compte = compteMapper.toEntity(cpte);
        return compte;
    }

    Compte findCompteCarteByOne(Long compteId) {
        CompteDTO cpte = compteRepository.findCompteCarteByNativeId(compteId);
        Compte compte = compteMapper.toEntity(cpte);
        return compte;
    }

    public void saveCompteOfOrganisation(Long organisationId) {
        UniteOrganisation uniteOrganisation = uniteOrganisationRepository.findByUoId(organisationId);
        TypeUniteOrganisation typeUo = typeUniteOrganisationRepository.findById(uniteOrganisation.getTypeUO().getId()).orElse(null);
        List<EtiquetteCompte> etiquetteCompteList = etiquetteCompteRepository.findByObligatoire(Boolean.TRUE);
        if (uniteOrganisation != null && uniteOrganisation.getId() != null) {
            if (etiquetteCompteList != null && typeUo.getNiveau() >= 2) {
                for (EtiquetteCompte etiquetteCompte : etiquetteCompteList) {
                    uniteOrganisationService.createDefaultCompte(etiquetteCompte.getCode(), uniteOrganisation);
                }
            }
        }
    }
}
