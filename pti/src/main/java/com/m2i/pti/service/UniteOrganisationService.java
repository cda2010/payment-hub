package com.m2i.pti.service;

import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.repository.*;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.mapper.*;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.collections4.map.PassiveExpiringMap;

/**
 * Service Implementation for managing UniteOrganisation.
 */
@Service

public class UniteOrganisationService {

    private final Logger log = LoggerFactory.getLogger(UniteOrganisationService.class);

    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;
    private final TypeUniteOrganisationService typeUniteOrganisationService;
    private final TransactionComptePtiRepository transactionComptePtiRepository;

    private final PersonneMapper personneMapper;

    private final PersonneMoraleMapper personneMoraleMapper;

    private final PersonnePhysiqueMapper personnePhysiqueMapper;

    private final PersonneRepository personneRepository;

    private final PointVenteRepository pointVenteRepository;

    private final CompteRepository compteRepository;

    private final PersonneMoraleRepository personneMoraleRepository;

    private final PersonnePhysiqueRepository personnePhysiqueRepository;

    private final PieceMapper pieceMapper;

    private final PieceRepository pieceRepository;

    private final UniteOrganisationMapper uniteOrganisationMapper;

    private final UserService userService;

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final CompteBanqueRepository compteBanqueRepository;

    private final EtiquetteCompteRepository etiquetteCompteRepository;
    ParametreAppRepository parametreAppRepository;
    private final CompteAutreRepository compteAutreRepository;

    private final AutorisationRepository autorisationRepository;
    private final ServiceProfilRepository serviceProfilRepository;
    private final CommercialeRepository commercialeRepository;
    private final GroupeProfilActeurRepository groupeProfilActeurRepository;
    private final ProfilActeurRepository profilActeurRepository;
    private final LocaliteMapper localiteMapper;
    private final LocaliteRepository localiteRepository;
    PassiveExpiringMap<String, UniteOrganisationDTO> uniteOrganisationMapByID = new PassiveExpiringMap<>(3600000);
    PassiveExpiringMap<String, UniteOrganisationDTO> uniteOrganisationCODEFSEMapByID = new PassiveExpiringMap<>(1800000);
    private final CompteMapper compteMapper;

    public UniteOrganisationService(CompteMapper compteMapper, ParametreAppRepository parametreAppRepository, UniteOrganisationRepository uniteOrganisationRepository, TypeUniteOrganisationRepository typeUniteOrganisationRepository, TypeUniteOrganisationService typeUniteOrganisationService, TransactionComptePtiRepository transactionComptePtiRepository, PersonneService personneService, PersonneMapper personneMapper, ProfilRepository profilRepository, PersonneMoraleMapper personneMoraleMapper, PersonnePhysiqueMapper personnePhysiqueMapper, PersonneRepository personneRepository, PointVenteRepository pointVenteRepository, CompteRepository compteRepository, PersonneMoraleRepository personneMoraleRepository, PersonnePhysiqueRepository personnePhysiqueRepository, PieceMapper pieceMapper, PieceRepository pieceRepository, UniteOrganisationMapper uniteOrganisationMapper, UserService userService, UserRepository userRepository, UserMapper userMapper, CompteBanqueRepository compteBanqueRepository, EtiquetteCompteRepository etiquetteCompteRepository, CompteAutreRepository compteAutreRepository, AutorisationRepository autorisationRepository, ServiceProfilRepository serviceProfilRepository, CommercialeRepository commercialeRepository, GroupeProfilActeurRepository groupeProfilActeurRepository, ProfilActeurRepository profilActeurRepository, LocaliteMapper localiteMapper, LocaliteRepository localiteRepository) {
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.typeUniteOrganisationService = typeUniteOrganisationService;
        this.transactionComptePtiRepository = transactionComptePtiRepository;
        this.personneMapper = personneMapper;
        this.personneMoraleMapper = personneMoraleMapper;
        this.personnePhysiqueMapper = personnePhysiqueMapper;
        this.personneRepository = personneRepository;
        this.pointVenteRepository = pointVenteRepository;
        this.compteRepository = compteRepository;
        this.personneMoraleRepository = personneMoraleRepository;
        this.personnePhysiqueRepository = personnePhysiqueRepository;
        this.pieceMapper = pieceMapper;
        this.pieceRepository = pieceRepository;
        this.uniteOrganisationMapper = uniteOrganisationMapper;
        this.userService = userService;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.compteBanqueRepository = compteBanqueRepository;
        this.etiquetteCompteRepository = etiquetteCompteRepository;
        this.compteAutreRepository = compteAutreRepository;
        this.autorisationRepository = autorisationRepository;
        this.serviceProfilRepository = serviceProfilRepository;
        this.commercialeRepository = commercialeRepository;
        this.groupeProfilActeurRepository = groupeProfilActeurRepository;
        this.profilActeurRepository = profilActeurRepository;
        this.parametreAppRepository = parametreAppRepository;
        this.localiteMapper = localiteMapper;
        this.compteMapper = compteMapper;
        this.localiteRepository = localiteRepository;
    }

    /**
     * Save a uniteOrganisation. getFseAgenceSiege
     *
     * @param uniteOrganisationDTO the entity to save
     * @return the persisted entity
     */
    @Transactional
    public UniteOrganisationDTO save(UniteOrganisationDTO uniteOrganisationDTO) {
        log.debug("Request to save UniteOrganisation : {}", uniteOrganisationDTO);
        Long serviceProfilId = uniteOrganisationDTO.getServiceProfilId();
        if (serviceProfilId == null) {
            throw new CustomParameterizedException("Veuillez renseigner le profil pour les services");
        }
        Boolean saveAutorisations = false;
        if (uniteOrganisationDTO.getId() != null) {
            UniteOrganisation oldUO = findById(uniteOrganisationDTO.getId());
            //   UniteOrganisation oldUO = uniteOrganisationRepository.findDtoById(uniteOrganisationDTO.getId());
            log.debug("oldUO" + oldUO);
            if (oldUO.getServiceProfil().getId() == null) {
                autorisationRepository.deleteByUniteOrganisation(oldUO);
                saveAutorisations = true;
            } else if (oldUO.getServiceProfil().getId() != uniteOrganisationDTO.getServiceProfilId() && uniteOrganisationDTO.getServiceProfilId() != null) {
                //autorisationRepository.deleteByUniteOrganisation(oldUO);
                List<Autorisation> roles = autorisationRepository.findByUniteOrganisation(oldUO);
                autorisationRepository.deleteAll(roles);
                saveAutorisations = true;
            }
        } else {
            saveAutorisations = true;
        }
        UniteOrganisation uniteOrganisation = uniteOrganisationMapper.toEntity(uniteOrganisationDTO);

        if (uniteOrganisationDTO.getCommercialeId() != null) {
            uniteOrganisation.setCommerciale(commercialeRepository.findById(uniteOrganisationDTO.getCommercialeId()).orElse(null));
        }
        GroupeProfilActeur groupeProfilActeur = null;
        if (uniteOrganisationDTO.getGroupeProfilActeurId() != null) {
            groupeProfilActeur = groupeProfilActeurRepository.findById(uniteOrganisationDTO.getGroupeProfilActeurId()).orElse(null);
        } else {
            Optional<GroupeProfilActeur> defaultGroup = groupeProfilActeurRepository.findByTypeAndActiveTrue(TypeProfilActeur.ORGANISATION);
            if (defaultGroup.isPresent()) {
                groupeProfilActeur = defaultGroup.get();
            }
        }
        if (uniteOrganisationDTO.getTypeUOId() != null) {
            uniteOrganisation.setTypeUO(typeUniteOrganisationRepository.findById(uniteOrganisationDTO.getTypeUOId()).orElse(null));
        }
        uniteOrganisation = setGroupe(groupeProfilActeur, uniteOrganisation);

        /* Save Localite  */
        LocaliteDTO localiteDTO = uniteOrganisationDTO.getLocaliteDTO();
        if (localiteDTO != null) {
            if (localiteDTO.getId() != null) {
                Localite localite = localiteRepository.save(localiteMapper.toEntity(localiteDTO));
                if (localite.getId() != null) {
                    uniteOrganisation.setLocalite(localite);
                }
            }
        }
        if (uniteOrganisation.getCreatedDate() == null) {
            uniteOrganisation.setCreatedDate(Instant.now());
        }
        //uniteOrganisation = uniteOrganisationRepository.save(uniteOrganisation);

        if (!uniteOrganisation.getTypeUO().getCodeTypeUO().equals("FSE") && !uniteOrganisation.getTypeUO().getCodeTypeUO().equals("PAYS") && !uniteOrganisation.getTypeUO().getCodeTypeUO().equals("UNITE_REGROUPEMENT") && !uniteOrganisation.getTypeUO().getCodeTypeUO().equals("ZONE")) {
            UniteOrganisation uopays = findUoParentByType(uniteOrganisation, TypeUO.PAYS);
            uniteOrganisation.setUoPaysId(uopays.getId());
            if (uopays != null) {
                uniteOrganisation = uniteOrganisationRepository.save(uniteOrganisation);
            }
        } else {
            uniteOrganisation = uniteOrganisationRepository.save(uniteOrganisation);
        }

        if (saveAutorisations && uniteOrganisation.getServiceProfil() != null) {
            ServiceProfil serviceProfil = serviceProfilRepository.findOneWithEagerRelationships(serviceProfilId);
            if (serviceProfil != null) {
                for (TypeService typeService : serviceProfil.getTypeServices()) {
                    Autorisation autorisation = new Autorisation();
                    autorisation.setUniteOrganisation(uniteOrganisation);
                    autorisation.setTypeService(typeService);
                    autorisationRepository.save(autorisation);
                }
            }

        }
        return uniteOrganisationMapper.toDto(uniteOrganisation);
    }

    public UniteOrganisation setGroupe(GroupeProfilActeur groupeProfilActeur, UniteOrganisation uniteOrganisation) {
        if (groupeProfilActeur != null && groupeProfilActeur.getType().equals(TypeProfilActeur.ORGANISATION)) {
            uniteOrganisation.setGroupeProfilActeur(groupeProfilActeur);
            List<ProfilActeur> profilActeurs = profilActeurRepository.findAllProfilsByGroupe(groupeProfilActeur.getId());
            if (profilActeurs != null && profilActeurs.get(0) != null) {
                uniteOrganisation.setProfilActeur(profilActeurs.get(0));
            }
        }

        return uniteOrganisation;
    }

    public Compte getCompteOfOrganisation(UniteOrganisation uniteOrganisation, EtiquetteCompteCode etiquette) {
        if (uniteOrganisation != null) {

            CompteDTO cpte = compteRepository.findTop1ByUniteOrganisationAndEtiquette(uniteOrganisation.getId(), String.valueOf(etiquette));
            Compte compte = compteMapper.toEntity(cpte);
            /*
                Si le compte obtenu est associÃ© Ã  un comptePrincipale
                on utilisera le compte principale
             */
            if (compte != null) {
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
     * Enregistrement d'un compte par default
     *
     * @param etiquette
     * @param uniteOrganisation
     */
    public void createDefaultCompte(String etiquette, UniteOrganisation uniteOrganisation) {

        //Enregistrement des informations du compteBanque
        log.debug("enregistrement du compte de l'UO");
        CompteBanque compteBanque = new CompteBanque();
        CompteAutre compteAutre = new CompteAutre();
        Compte compte = new Compte();
        Compte cpt = null;
        ParametreApp usebkaccount = parametreAppRepository.findByTypeAndNom(TypeParametre.AUTRE, "USE_DEFAULT_BANK_ACCOUNT");
        EtiquetteCompte eti = etiquetteCompteRepository.findTop1ByCode(etiquette);
        if (eti.isTransversal()) {
            /**
             * Rechercher l'agence principal si *
             */
            if (uniteOrganisation.getEstSiege() == false) {
                if (uniteOrganisation != null) {
                    UniteOrganisation uoAgencePrincipal = getUoAgenceSiege(uniteOrganisation.getPereUO());
                    cpt = getCompteOfOrganisation(uoAgencePrincipal, EtiquetteCompteCode.valueOf(etiquette));
                    compte.setComptePincipale(cpt);
                }
            }

        }
        UniteOrganisation uo = uniteOrganisationRepository.findById(uniteOrganisation.getPereUO().getId()).orElse(null);
        Long max;
        String code;
        // recherche du fse de l'UO
        UniteOrganisation fseUO = getFSEOfUniteOrganisation(uo);
        if (usebkaccount != null && usebkaccount.getValeur().equals("0")) {
            compteAutre.solde(BigDecimal.ZERO);
            if (fseUO != null) {
                compteAutre.setCodeFse(fseUO.getCodeFse());
            }
            compteAutre = compteAutreRepository.save(compteAutre);

            max = compteRepository.getMaxId();

            code = getString(max);
            if (cpt != null && compte.getComptePincipale() != null) {
                compte.setValeurCompte(cpt.getValeurCompte());
            } else {
                compte.setValeurCompte(code);
            }
            if (!eti.isTransversal() || uniteOrganisation.getEstSiege() == true) {

                compte.setCompteAutre(compteAutre);
            }

            compte.setCategorieCompte(CategorieCompte.AUTRE);
            compte.setFirstCategorieCompte(FirstCategorieCompte.COMPTE_INTERNE);

        } else {//Pour les comptes banques
            compteBanque.codeAgence(" ")
                    .codeBanque(" ")
                    .setRib(" ");
            compteBanque = compteBanqueRepository.save(compteBanque);
            compte.setFirstCategorieCompte(FirstCategorieCompte.COMPTE_EXTERNE);
            compte.compteBanque(compteBanque)
                    .categorieCompte(CategorieCompte.BANQUE);
        }

        if (etiquette.endsWith("PTI")) {//Pour les comptes internes
            compteAutre.solde(BigDecimal.ZERO);
            if (fseUO != null) {
                compteAutre.setCodeFse(fseUO.getCodeFse());
            }

            max = compteRepository.getMaxId();
            code = getString(max);
            compte.setValeurCompte(code);
            if (!eti.isTransversal()) {
                compteAutre = compteAutreRepository.save(compteAutre);
                compte.setCompteAutre(compteAutre);
            }

            compte.setCategorieCompte(CategorieCompte.AUTRE);
            compte.setFirstCategorieCompte(FirstCategorieCompte.COMPTE_INTERNE);
        }

        compte.etiquette(etiquette)
                .uniteOrganisation(uniteOrganisation);
        compteRepository.save(compte);
    }

    /**
     * Generation d'un code
     *
     * @param max
     * @return
     */
    public String getString(Long max) {
        String code;
        if (max > 0) {
            List<String> allCompte = new ArrayList<>();
         //  allCompte = compteRepository.getAllValeurCompte();
            //Integer maxVal= allCompte.stream().mapToInt(Integer::parseInt).max().orElse(-1);
            // String cptMax= compteRepository.getMaxofValeurCompte();
            // Compte lastCompte = compteRepository.findById(max).orElse(null);
            // Integer lastId = Integer.parseInt(lastCompte.getValeurCompte())+1;
            // Integer lastValCpt= Integer.parseInt(cptMax) +1;
            //lastValCpt = lastValCpt+ Integer.parseInt(test);
            // code = String.format("%09d", lastValCpt );
          /*  List<Long> numeroListLong = new ArrayList<>();
            allCompte.stream().map(s -> {
                return numeroListLong.add(Long.valueOf(s));
            }).collect(Collectors.toList());*/
            String numeroMax =  compteRepository.getMaxofValeurCompte(); //   Collections.max(allCompte);

            Long l = new Long(numeroMax) + 1;
            //     RandomStringUtils.randomNumeric(1)
            String randNumber = RandomStringUtils.randomNumeric(1);
            // lastId = lastId+ Integer.parseInt(test);
            l = l + Long.parseLong(randNumber);
            //code = String.format("%09d", lastId );
            code = String.format("%09d", l);
        } else {
            code = String.format("%09d", 1);
        }

        return code;
    }

    /**
     * Generation du code d'une unite d'organisation
     *
     * @param typeUoId
     * @return
     */
    private String generateCodeUo(Long typeUoId) {
        if (typeUoId != null) {
            TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationService.findByOne(typeUoId);
            if (typeUniteOrganisation != null) {
                Integer count = uniteOrganisationRepository.countByTypeUOAndSupprimeIsFalse(typeUniteOrganisation);
                String code;
                if (count > 0) {
                    // code = String.format("%07d", count + 1) + RandomStringUtils.randomNumeric(3);
                    code = String.format("%04d", count + 1) + RandomStringUtils.randomNumeric(3);
                } else {
                    // code = String.format("%07d", 1) + RandomStringUtils.randomNumeric(3);
                    code = String.format("%04d", 1) + RandomStringUtils.randomNumeric(3);
                }
                return code.toUpperCase();
            }
        }
        return null;
    }

    private String generatePointVente(UniteOrganisationDTO uniteOrganisationDTO) {
        String codeUo = uniteOrganisationDTO.getCodeUO();
        String pv = codeUo + RandomStringUtils.randomNumeric(3);
        return pv.toUpperCase();
    }

    /**
     * Enregistrer une nouvelle unite d'organisation
     *
     * @param uniteOrganisationDTO the entity to save
     * @return the persisted entity
     */
    public UniteOrganisationDTO saveNew(UniteOrganisationDTO uniteOrganisationDTO) {

        uniteOrganisationDTO = this.saveInfosRepresentant(uniteOrganisationDTO.getPersonneAllDTO(), uniteOrganisationDTO);
        //si estSiege est true alors je verifie s'il n'y a pas une autre agence siege de la meme filiale
        if (uniteOrganisationDTO.isEstSiege() != null) {
            if (uniteOrganisationDTO.isEstSiege() == true && uniteOrganisationDTO.getPereUOId() != null) {
                UniteOrganisation uniteOrganisationPere = uniteOrganisationRepository.findByUoId(uniteOrganisationDTO.getPereUOId());
                TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationService.findByOne(uniteOrganisationDTO.getTypeUOId());
                List<UniteOrganisation> uniteOrganisationList = uniteOrganisationRepository.findByPereUOAndTypeUOAndEstSiegeAndSupprimeIsFalse(uniteOrganisationPere, typeUniteOrganisation, Boolean.TRUE);

                if (uniteOrganisationList.size() >= 1 && uniteOrganisationDTO.getId() == null) {
                    throw new CustomParameterizedException(uniteOrganisationPere.getLibelleUniteOrganisation().toUpperCase() + " possÃ¨de dÃ©ja une " + typeUniteOrganisation.getLibelleTypeUO() + " SiÃ¨ge ( " + uniteOrganisationList.get(0).getLibelleUniteOrganisation().toUpperCase() + " )");
                }
                if (uniteOrganisationDTO.getId() != null && uniteOrganisationList.size() >= 1) {
                    if (uniteOrganisationDTO.getId() != uniteOrganisationList.get(0).getId()) {
                        throw new CustomParameterizedException(uniteOrganisationPere.getLibelleUniteOrganisation().toUpperCase() + " possÃ¨de dÃ©ja une " + typeUniteOrganisation.getLibelleTypeUO() + " SiÃ¨ge ( " + uniteOrganisationList.get(0).getLibelleUniteOrganisation().toUpperCase() + " )");
                    }
                }
            }
        }

        Integer max;
        if (uniteOrganisationDTO.getPereUOId() == null) {
            max = uniteOrganisationRepository.getNbreElementWherePereIdIsNull();
        } else {
            max = uniteOrganisationRepository.getNbreElement(uniteOrganisationDTO.getPereUOId());
        }
        uniteOrganisationDTO.setNiveau(this.getNiveau(uniteOrganisationDTO));
        uniteOrganisationDTO.setNumeroOrdre(this.getNumeroOrdre(max));
        if (uniteOrganisationDTO.getEstUniteRegroupement() != null && !uniteOrganisationDTO.getEstUniteRegroupement()) {
            this.regleDeGestionUO(uniteOrganisationDTO);
        }
        if (uniteOrganisationDTO.getId() == null) {
            //uniteOrganisationDTO.setCodeUO(this.getCodeUO());
            uniteOrganisationDTO.setFeuille(true);
            //Si j'enregistre le premier fils d'un element le met le boolean isfeuille de cette element a false
            if (uniteOrganisationDTO.getPereUOId() != null && uniteOrganisationDTO.getNumeroOrdre() == 1) {
                UniteOrganisationDTO uniteOrganisationDTOPere = this.findOne(uniteOrganisationDTO.getPereUOId());
                uniteOrganisationDTOPere.setFeuille(false);
                this.save(uniteOrganisationDTOPere);
            }
        }
        if (uniteOrganisationDTO.isEstSiege() == null) {

            uniteOrganisationDTO.setEstSiege(false);
        }
        if (uniteOrganisationDTO.isEstFiliale() == null) {
            uniteOrganisationDTO.setEstFiliale(false);

        }
        if (uniteOrganisationDTO.getId() == null) {
            uniteOrganisationDTO.setCodeUO(generateCodeUo(uniteOrganisationDTO.getTypeUOId()));
        }

        UniteOrganisationDTO uniteOrganisationDTONew = this.save(uniteOrganisationDTO);
        if (uniteOrganisationDTONew.getId() != null) {
            TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationService.findByOne(uniteOrganisationDTONew.getTypeUOId());
            if (!typeUniteOrganisation.getCodeTypeUO().equals("FSE") && !typeUniteOrganisation.getCodeTypeUO().equals("PAYS") && !typeUniteOrganisation.getCodeTypeUO().equals("UNITE_REGROUPEMENT") && !typeUniteOrganisation.getCodeTypeUO().equals("ZONE")) {
                // PointVenteDTO pointVente= getCodePointVente();
                List<PointVente> pvOfUo = pointVenteRepository.findByUniteOrganisation(uniteOrganisationMapper.toEntity(uniteOrganisationDTONew));
                if (pvOfUo.size() < 1) {
                    PointVente pv = new PointVente();
                    pv.setCodePointVente(generatePointVente(uniteOrganisationDTONew));
                    pv.setUniteOrganisation(uniteOrganisationMapper.toEntity(uniteOrganisationDTONew));
                    pv.setLibellePointVente("PV PAR DEFAUT");
                    pv.setLatitude("0");
                    pv.setLongitude("0");
                    pointVenteRepository.save(pv);
                }
            }
            uniteOrganisationDTONew = this.setPaysAndZoneOfUo(uniteOrganisationDTONew.getId());

        }
        TypeUniteOrganisation typeUo = typeUniteOrganisationService.findByOne(uniteOrganisationDTO.getTypeUOId());
        //Generation des comptes obligatoires de l'organisation en cours de creation
        if (uniteOrganisationDTO.getId() == null) {
            List<EtiquetteCompte> etiquetteCompteList = etiquetteCompteRepository.findByObligatoire(Boolean.TRUE);
            if (etiquetteCompteList != null && typeUo.getNiveau() >= 2) {
                for (EtiquetteCompte etiquetteCompte : etiquetteCompteList) {
                    createDefaultCompte(etiquetteCompte.getCode(), uniteOrganisationMapper.toEntity(uniteOrganisationDTONew));
                }
            }
        }
        return uniteOrganisationDTONew;
    }

    public PointVenteDTO getCodePointVente() {
        Long max = pointVenteRepository.getMaxId();
        String code;
        if (max > 0) {
            PointVente lastPointVente = pointVenteRepository.findById(max).orElse(null);
            Integer lastId = Integer.parseInt(lastPointVente.getCodePointVente().substring(3));
            code = String.format("%03d", lastId + 1);
            code = "PV" + code;
        } else {
            code = String.format("%03d", 1);
            code = "PV" + code;
        }
        PointVenteDTO pointVenteDTO = new PointVenteDTO();
        pointVenteDTO.setCodePointVente(code);
        return pointVenteDTO;
    }

    public UniteOrganisationDTO setPaysAndZoneOfUo(Long uoId) {
        UniteOrganisation uniteOrganisation = uniteOrganisationRepository.findByUoId(uoId);
        UniteOrganisation fseUo = this.findUoParentByType(uniteOrganisation, TypeUO.FSE);
        UniteOrganisation paysUo = this.findUoParentByType(uniteOrganisation, TypeUO.PAYS);
        UniteOrganisation zoneUo = this.findUoParentByType(uniteOrganisation, TypeUO.ZONE);
        if (uniteOrganisation.getTypeUO().getLibelleTypeUO().equals("PAYS")) {
            uniteOrganisation.setUoZoneId(uniteOrganisation.getPereUO().getId());
        } else if (uniteOrganisation.getTypeUO().getLibelleTypeUO().equals("FSE")) {
            uniteOrganisation.setUoPaysId(paysUo.getId());
            uniteOrganisation.setUoZoneId(zoneUo.getId());
        } else if (uniteOrganisation.getTypeUO().getLibelleTypeUO().equals("UNITE_REGROUPEMENT") && uniteOrganisation.getPereUO() != null) {
            UniteOrganisation pereUO = uniteOrganisationRepository.findByUoId(uniteOrganisation.getPereUO().getId());
            if (pereUO.getTypeUO().getLibelleTypeUO().equals("PAYS")) {
                uniteOrganisation.setUoZoneId(zoneUo.getId());
                uniteOrganisation.setUoPaysId(paysUo.getId());
            } else if (pereUO.getTypeUO().getLibelleTypeUO().equals("ZONE")) {
                uniteOrganisation.setUoZoneId(zoneUo.getId());
            } else if (pereUO.getTypeUO().getLibelleTypeUO().equals("FSE")) {
                uniteOrganisation.setUoZoneId(zoneUo.getId());
                uniteOrganisation.setUoPaysId(paysUo.getId());
                uniteOrganisation.setUoFseId(fseUo.getId());
            }
        } else if (!uniteOrganisation.getTypeUO().getLibelleTypeUO().equals("ZONE")) {
            uniteOrganisation.setUoZoneId(zoneUo.getId());
            uniteOrganisation.setUoPaysId(paysUo.getId());
            uniteOrganisation.setUoFseId(fseUo.getId());
        }
        uniteOrganisation = uniteOrganisationRepository.save(uniteOrganisation);

        return uniteOrganisationMapper.toDto(uniteOrganisation);
    }

    /**
     * @param nbreElement
     * @return
     * @throws NumberFormatException
     */
    public Integer getNumeroOrdre(int nbreElement) throws NumberFormatException {
        Integer code;
        if (nbreElement > 0) {
            code = nbreElement + 1;
        } else {
            code = 1;
        }
        return code;
    }

    /**
     * Generation du code de l'unite d'organisation
     *
     * @return
     */
    public String getCodeUO() {
        Long max = uniteOrganisationRepository.getMaxId();
        String code;
        if (max > 0) {
            UniteOrganisation uniteOrganisationLast = uniteOrganisationRepository.findById(max).orElse(null);
            if (uniteOrganisationLast.getCodeUO().contains("UO_")) {
                Integer lastId = Integer.parseInt(uniteOrganisationLast.getCodeUO().substring(3));
                code = String.format("%04d", lastId + 1);
                code = "UO_" + code;
            } else {
                code = String.format("%04d", 1);
                code = "UO_" + code;
            }
        } else {
            code = String.format("%04d", 1);
            code = "UO_" + code;
        }
        return code;
    }

    /**
     * Recherche le niveau d'une organisation pere
     *
     * @param id
     * @param niveau
     * @return
     */
    public Integer findfirtPereNiveau(Long id, Integer niveau) {
        if (niveau != (-1)) {
            return niveau;
        }
        UniteOrganisation uniteOrganisation = uniteOrganisationRepository.findById(id).orElse(null);
        if (this.findOne(id).getPereUOId() == null) {
            return 0;
        }
        UniteOrganisation uniteOrganisationPere = uniteOrganisation.getPereUO();
        TypeUniteOrganisation typeUniteOrganisationPere = uniteOrganisationPere.getTypeUO();
        return findfirtPereNiveau(uniteOrganisationPere.getId(), typeUniteOrganisationPere.getNiveau());
    }

    /**
     * VÃ©rification de la rÃ¨gle de gestion etabli pour la gestion de
     * l'hiÃ©rarchie
     *
     * @param uniteOrganisationDTO
     */
    public void regleDeGestionUO(UniteOrganisationDTO uniteOrganisationDTO) {
        TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationService.findByOne(uniteOrganisationDTO.getTypeUOId());
        log.debug("type uo" + typeUniteOrganisation.toString());
        UniteOrganisationDTO uniteOrganisationDTOPere;
        TypeUniteOrganisation typeUniteOrganisationPere = new TypeUniteOrganisation();
        if (uniteOrganisationDTO.getPereUOId() != null) {
            uniteOrganisationDTOPere = this.findOne(uniteOrganisationDTO.getPereUOId());
            typeUniteOrganisationPere = typeUniteOrganisationRepository.findById(uniteOrganisationDTOPere.getTypeUOId()).orElse(null);
        }
        log.debug("type uo pere" + typeUniteOrganisationPere.toString());

        if (uniteOrganisationDTO.getPereUOId() == null && typeUniteOrganisation.getNiveau() != 1 && typeUniteOrganisation.getNiveau() != (-1)) {
            throw new CustomParameterizedException("veuiller choisir un TypeUO FSE,UNITE DE REGROUPEMENT ou PAYS ");
        }
        if (uniteOrganisationDTO.getPereUOId() != null && typeUniteOrganisation.getNiveau() != -1) {
            if (typeUniteOrganisation.getNiveau() != typeUniteOrganisationPere.getNiveau() + 1) {
                if ((this.findfirtPereNiveau(uniteOrganisationDTO.getPereUOId(), typeUniteOrganisationPere.getNiveau()) != (typeUniteOrganisation.getNiveau() - 1)) || (this.findfirtPereNiveau(uniteOrganisationDTO.getPereUOId(), typeUniteOrganisationPere.getNiveau()) != 0)) {
                    Integer niveau = this.findfirtPereNiveau(uniteOrganisationDTO.getPereUOId(), typeUniteOrganisationPere.getNiveau()) + 1;
                    log.debug("niveau---" + niveau);
                    throw new CustomParameterizedException("veuiller choisir un TypeUO " + typeUniteOrganisationRepository.findByNiveau(niveau).getLibelleTypeUO() + ", UNITE DE REGROUPEMENT ou PAYS");
                }
            }
        }
    }

    /**
     * Enregistrement ou mise Ã jour des informations d'un representant
     *
     * @param personneAllDTO
     * @param uniteOrganisationDTO
     * @return
     */
    private UniteOrganisationDTO saveInfosRepresentant(PersonneAllDTO personneAllDTO, UniteOrganisationDTO uniteOrganisationDTO) {
        if (personneAllDTO != null) {
            Personne personne = personneMapper.toEntity(personneAllDTO.getPersonneDTO());
            Piece piece = pieceMapper.toEntity(personneAllDTO.getPieceDTO());
            PersonnePhysique personnePhysique = personnePhysiqueMapper.toEntity(personneAllDTO.getPersonnePhysiqueDTO());
            PersonneMorale personneMorale = personneMoraleMapper.toEntity(personneAllDTO.getPersonneMoraleDTO());
            if (personne.getId() != null) {
                /*La personne existe dejÃ  dans la base
                    Mise Ã  jour des information de la
                    -Piece
                    -PersonnePhysique
                    -PersonneMorale
                 */
                if (personneAllDTO.getPieceDTO() != null) {
                    log.debug("piece----" + piece);
                    piece.setTitulaire(personne);
                }
                pieceRepository.save(piece);
                if (personne.getTypePersonne().equals(TypePersonne.PHYSIQUE)) { //PersonnePhysiqye
                    personnePhysique = personnePhysiqueRepository.save(personnePhysique);
                    if (personnePhysique.getId() == null) {
                        personne.setPersonnePhysique(personnePhysique);
                    }
                } else {
                    personneMorale = personneMoraleRepository.save(personneMorale);
                    if (personneMorale.getId() == null) {
                        personne.setPersonneMorale(personneMorale);
                    }
                }

                if (personneAllDTO.getPersonneDTO().getLocaliteId() != null) {
                    Localite localite = localiteRepository.findById(personneAllDTO.getPersonneDTO().getLocaliteId()).orElse(null);
                    if (localite != null && localite.getId() != null) {
                        localite.setNomProvince(personneAllDTO.getPersonneDTO().getLocaliteProvince());
                        localite.setQuartier(personneAllDTO.getPersonneDTO().getLocaliteQuartier());
                        localite.setNomRegion(personneAllDTO.getPersonneDTO().getLocaliteRegion());
                        localite.setNomVille(personneAllDTO.getPersonneDTO().getLocaliteVille());
                        localite.setNomCommune(personneAllDTO.getPersonneDTO().getLocaliteCommune());
                        localite.setSecteur(personneAllDTO.getPersonneDTO().getLocaliteSecteur());
                        localite = localiteRepository.save(localite);
                        personne.setLocalite(localite);
                    }
                } else {
                    Localite localiteN = new Localite();
                    localiteN.setNomProvince(personneAllDTO.getPersonneDTO().getLocaliteProvince());
                    localiteN.setQuartier(personneAllDTO.getPersonneDTO().getLocaliteQuartier());
                    localiteN.setNomRegion(personneAllDTO.getPersonneDTO().getLocaliteRegion());
                    localiteN.setNomVille(personneAllDTO.getPersonneDTO().getLocaliteVille());
                    localiteN.setSecteur(personneAllDTO.getPersonneDTO().getLocaliteSecteur());
                    localiteN.setNomCommune(personneAllDTO.getPersonneDTO().getLocaliteCommune());
                    localiteN = localiteRepository.save(localiteN);
                    if (localiteN.getId() != null) {
                        personne.setLocalite(localiteN);
                        //personneRepository.save(personne);
                    }
                }
                personne = personneRepository.save(personne);
            } else /*La personne n' existe pas dans la base
                    Enregistrement de
                    -Personne
                    -Piece
                    -PersonnePhysique
                    -PersonneMorale
             */ if (personne.getTypePersonne().equals(TypePersonne.PHYSIQUE)) { //PersonnePhysique
                personnePhysique = personnePhysiqueRepository.save(personnePhysique);
                personne.setPersonnePhysique(personnePhysique);
                personne = personneRepository.save(personne);
                if (piece != null) {
                    piece.setTitulaire(personne);
                    pieceRepository.save(piece);
                }
            } else {
                piece = pieceRepository.save(piece);
                personneMorale = personneMoraleRepository.save(personneMorale);
                personne.setPersonneMorale(personneMorale);
                personne = personneRepository.save(personne);
                if (piece != null) {
                    piece.setTitulaire(personne);
                    pieceRepository.save(piece);
                }
            }

            Localite localite = new Localite();
            localite.setNomProvince(personneAllDTO.getPersonneDTO().getLocaliteProvince());
            localite.setQuartier(personneAllDTO.getPersonneDTO().getLocaliteQuartier());
            localite.setNomRegion(personneAllDTO.getPersonneDTO().getLocaliteRegion());
            localite.setNomVille(personneAllDTO.getPersonneDTO().getLocaliteVille());
            localite.setNomProvince(personneAllDTO.getPersonneDTO().getLocaliteProvince());
            localite = localiteRepository.save(localite);
            if (localite.getId() != null) {
                personne.setLocalite(localite);
                personneRepository.save(personne);
            }
            uniteOrganisationDTO.setRepresentantId(personne.getId());
            return uniteOrganisationDTO;
        }
        return uniteOrganisationDTO;
    }

    /**
     * Get all the uniteOrganisations.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<UniteOrganisationDTO> findAll(
            String libelleUniteOrganisation,
            String codeUO,
            Integer numeroOrdre,
            String typeUO,
            Long groupeId,
            Long villeId,
            String adresse,
            Pageable pageable) {
        return uniteOrganisationRepository.findAllForViewPaginated(libelleUniteOrganisation, codeUO, numeroOrdre, typeUO, groupeId, villeId, adresse, pageable)
                .map(uniteOrganisationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<UniteOrganisationDTO> findByPereUoId(String libelleUniteOrganisation, String codeUO, Integer numeroOrdre, String typeUO, Long pereUoId, Long groupeId, Long villeId, String adresse, Pageable pageable) {
        List<UniteOrganisation> list = new ArrayList<>();
        list = uniteOrganisationRepository.findForViewPaginated();

        List<UniteOrganisation> fils = new ArrayList<UniteOrganisation>();
        List<UniteOrganisationDTO> result = new ArrayList<UniteOrganisationDTO>();

        list.forEach(uniteOrganisation -> {

            if (uniteOrganisation.getPereUO() != null && uniteOrganisation.getPereUO().getId().equals(pereUoId)) {
                fils.add(uniteOrganisation);
                if (((libelleUniteOrganisation == null || libelleUniteOrganisation.equals("")) || (uniteOrganisation.getLibelleUniteOrganisation() != null && uniteOrganisation.getLibelleUniteOrganisation().toUpperCase().contains(libelleUniteOrganisation.toUpperCase())))
                        && ((typeUO == null || typeUO.equals("")) || (uniteOrganisation.getTypeUO() != null && uniteOrganisation.getTypeUO().getCodeTypeUO().equalsIgnoreCase(typeUO)))
                        && ((codeUO == null || codeUO.equals("")) || (uniteOrganisation.getCodeUO() != null && uniteOrganisation.getCodeUO().toUpperCase().contains(codeUO.toUpperCase())))
                        && ((numeroOrdre == null) || (uniteOrganisation.getNumeroOrdre() != null && uniteOrganisation.getNumeroOrdre() == numeroOrdre))
                        && ((groupeId == null) || (uniteOrganisation.getGroupeProfilActeur() != null && uniteOrganisation.getGroupeProfilActeur().getId() == groupeId))
                        && ((villeId == null) || (uniteOrganisation.getVille() != null && uniteOrganisation.getVille().getId() == villeId))
                        && ((adresse == null || adresse.equals("")) || (uniteOrganisation.getAdresse() != null && uniteOrganisation.getAdresse().toUpperCase().contains(adresse.toUpperCase())))) {
                    result.add(uniteOrganisationMapper.toDto(uniteOrganisation));
                }
            }
        });

        while (!fils.isEmpty()) {
            List<UniteOrganisation> oldFils = new ArrayList<UniteOrganisation>(fils);
            fils.clear();
            for (UniteOrganisation pere : oldFils) {
                list.forEach(uo -> {
                    if (uo.getPereUO() != null && uo.getPereUO().getId().equals(pere.getId())) {
                        fils.add(uo);
                        if (((libelleUniteOrganisation == null || libelleUniteOrganisation.equals("")) || (uo.getLibelleUniteOrganisation() != null && uo.getLibelleUniteOrganisation().toUpperCase().contains(libelleUniteOrganisation.toUpperCase())))
                                && ((typeUO == null || typeUO.equals("")) || (uo.getTypeUO() != null && uo.getTypeUO().getCodeTypeUO().equalsIgnoreCase(typeUO)))
                                && ((codeUO == null || codeUO.equals("")) || (uo.getCodeUO() != null && uo.getCodeUO().toUpperCase().contains(codeUO.toUpperCase())))
                                && ((numeroOrdre == null) || (uo.getNumeroOrdre() != null && uo.getNumeroOrdre().equals(numeroOrdre)))
                                && ((groupeId == null) || (uo.getGroupeProfilActeur() != null && uo.getGroupeProfilActeur().getId() == groupeId))
                                && ((villeId == null) || (uo.getVille() != null && uo.getVille().getId().equals(villeId)))
                                && ((adresse == null || adresse.equals("")) || (uo.getAdresse() != null && uo.getAdresse().toUpperCase().contains(adresse.toUpperCase())))) {
                            result.add(uniteOrganisationMapper.toDto(uo));
                        }
                    }

                });
            }

        }

        /*Transformation de la liste en page*/
        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > result.size() ? result.size() : (start + pageable.getPageSize());
        Page<UniteOrganisationDTO> retour = new PageImpl<UniteOrganisationDTO>(result.subList(start, end), pageable, result.size());
        return retour;
    }

    // TODO: 24/05/2022 Implémentation de la méthode native sur le filtre des organisations
    @Transactional(readOnly = true)
    public List<UniteOrganisationDTO> findByPereUoIdOptimise(String libelleUniteOrganisation, String codeUO, Integer numeroOrdre, String typeUO, Long pereUoId, Long groupeId, Long villeId, String adresse, Pageable pageable) {
        List<UniteOrganisationDTO> list = uniteOrganisationRepository.findForViewPaginatedOptimise();
        List<UniteOrganisationDTO> fils = new ArrayList<UniteOrganisationDTO>();
        List<UniteOrganisationDTO> result = new ArrayList<UniteOrganisationDTO>();

        list.forEach(uniteOrganisationDTO -> {

            if (uniteOrganisationDTO.getPereUOId() != null && uniteOrganisationDTO.getPereUOId().equals(pereUoId)) {

                fils.add(uniteOrganisationDTO);
                if (((libelleUniteOrganisation == null || libelleUniteOrganisation.equals("")) || (uniteOrganisationDTO.getLibelleUniteOrganisation() != null && uniteOrganisationDTO.getLibelleUniteOrganisation().toUpperCase().contains(libelleUniteOrganisation.toUpperCase())))
                        && ((typeUO == null || typeUO.equals("")) || (uniteOrganisationDTO.getTypeUOId() != null && uniteOrganisationDTO.getTypeUOCode().equalsIgnoreCase(typeUO)))
                        && ((codeUO == null || codeUO.equals("")) || (uniteOrganisationDTO.getCodeUO() != null && uniteOrganisationDTO.getCodeUO().toUpperCase().contains(codeUO.toUpperCase())))
                        && ((numeroOrdre == null) || (uniteOrganisationDTO.getNumeroOrdre() != null && uniteOrganisationDTO.getNumeroOrdre().equals(numeroOrdre)))
                        && ((groupeId == null) || (uniteOrganisationDTO.getGroupeProfilActeurId() != null && uniteOrganisationDTO.getGroupeProfilActeurId().equals(groupeId)))
                        && ((villeId == null) || (uniteOrganisationDTO.getVilleId() != null && uniteOrganisationDTO.getVilleId().equals(villeId)))
                        && ((adresse == null || adresse.equals("")) || (uniteOrganisationDTO.getAdresse() != null && uniteOrganisationDTO.getAdresse().toUpperCase().contains(adresse.toUpperCase())))) {
                    result.add(uniteOrganisationDTO);
                }
            }
        });

        while (!fils.isEmpty()) {
            List<UniteOrganisationDTO> oldFils = new ArrayList<UniteOrganisationDTO>(fils);
            fils.clear();
            for (UniteOrganisationDTO pere : oldFils) {
                list.forEach(uo -> {
                    if (uo.getPereUOId() != null && uo.getPereUOId().equals(pere.getId())) {
                        fils.add(uo);
                        if (((libelleUniteOrganisation == null || libelleUniteOrganisation.equals("")) || (uo.getLibelleUniteOrganisation() != null && uo.getLibelleUniteOrganisation().toUpperCase().contains(libelleUniteOrganisation.toUpperCase())))
                                && ((typeUO == null || typeUO.equals("")) || (uo.getTypeUOId() != null && uo.getTypeUOCode().equalsIgnoreCase(typeUO)))
                                && ((codeUO == null || codeUO.equals("")) || (uo.getCodeUO() != null && uo.getCodeUO().toUpperCase().contains(codeUO.toUpperCase())))
                                && ((numeroOrdre == null) || (uo.getNumeroOrdre() != null && uo.getNumeroOrdre().equals(numeroOrdre)))
                                && ((groupeId == null) || (uo.getGroupeProfilActeurId() != null && uo.getGroupeProfilActeurId().equals(groupeId)))
                                && ((villeId == null) || (uo.getVilleId() != null && uo.getVilleId().equals(villeId)))
                                && ((adresse == null || adresse.equals("")) || (uo.getAdresse() != null && uo.getAdresse().toUpperCase().contains(adresse.toUpperCase())))) {
                            result.add(uo);
                        }
                    }
                });
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public List<UniteOrganisationDTO> findByPereUoDate(ZonedDateTime dateDebut, ZonedDateTime dateFin, String typeUO) {
        List<UniteOrganisation> list = new ArrayList<>();

        if (dateDebut != null && dateFin != null) {
            list = uniteOrganisationRepository.findForViewPaginatedIntervalDate(dateDebut, dateFin, typeUO);
        } else if (dateDebut != null) {
            list = uniteOrganisationRepository.findForViewPaginatedIntervalDate(dateDebut, dateDebut.plusHours(23), typeUO);
        } else if (dateFin != null) {

            list = uniteOrganisationRepository.findForViewPaginatedIntervalDate(dateFin, dateFin.plusHours(23), typeUO);
        } else {
            list = uniteOrganisationRepository.findAllByTypeUo(typeUO);
        }
        return uniteOrganisationMapper.toDto(list);
    }

    @Transactional(readOnly = true)
    public Page<UniteOrganisationDTO> findByPereUoIdOp(String libelleUniteOrganisation, String codeUO, Integer numeroOrdre, String typeUO, Long pereUoId, Long groupeId, Long villeId, String adresse, Pageable pageable) {
        List<UniteOrganisationDTO> list = uniteOrganisationRepository.findForViewListOptimise();
        List<UniteOrganisationDTO> fils = new ArrayList<>();
        List<UniteOrganisationDTO> result = new ArrayList<UniteOrganisationDTO>();
        list.forEach(uniteOrganisation -> {

            if (uniteOrganisation.getPereUOId() != null && uniteOrganisation.getPereUOId().equals(pereUoId)) {

                fils.add(uniteOrganisation);
                if (((libelleUniteOrganisation == null || libelleUniteOrganisation.equals("")) || (uniteOrganisation.getLibelleUniteOrganisation() != null && uniteOrganisation.getLibelleUniteOrganisation().toUpperCase().contains(libelleUniteOrganisation.toUpperCase())))
                        && ((typeUO == null || typeUO.equals("")) || (uniteOrganisation.getTypeUOId() != null && uniteOrganisation.getTypeUOCode().equalsIgnoreCase(typeUO)))
                        && ((codeUO == null || codeUO.equals("")) || (uniteOrganisation.getCodeUO() != null && uniteOrganisation.getCodeUO().toUpperCase().contains(codeUO.toUpperCase())))
                        && ((numeroOrdre == null) || (uniteOrganisation.getNumeroOrdre() != null && uniteOrganisation.getNumeroOrdre().equals(numeroOrdre)))
                        && ((groupeId == null) || (uniteOrganisation.getGroupeProfilActeurId() != null && uniteOrganisation.getGroupeProfilActeurId().equals(groupeId)))
                        && ((villeId == null) || (uniteOrganisation.getVilleId() != null && uniteOrganisation.getVilleId().equals(villeId)))
                        && ((adresse == null || adresse.equals("")) || (uniteOrganisation.getAdresse() != null && uniteOrganisation.getAdresse().toUpperCase().contains(adresse.toUpperCase())))) {
                    result.add(uniteOrganisation);
                }
            }
        });
        while (!fils.isEmpty()) {
            List<UniteOrganisationDTO> oldFils = new ArrayList<>(fils);
            fils.clear();
            for (UniteOrganisationDTO pere : oldFils) {
                list.forEach(uo -> {
                    if (uo.getPereUOId() != null && uo.getPereUOId().equals(pere.getId())) {
                        fils.add(uo);
                        if (((libelleUniteOrganisation == null || libelleUniteOrganisation.equals("")) || (uo.getLibelleUniteOrganisation() != null && uo.getLibelleUniteOrganisation().toUpperCase().contains(libelleUniteOrganisation.toUpperCase())))
                                && ((typeUO == null || typeUO.equals("")) || (uo.getTypeUOId() != null && uo.getTypeUOCode().equalsIgnoreCase(typeUO)))
                                && ((codeUO == null || codeUO.equals("")) || (uo.getCodeUO() != null && uo.getCodeUO().toUpperCase().contains(codeUO.toUpperCase())))
                                && ((numeroOrdre == null) || (uo.getNumeroOrdre() != null && uo.getNumeroOrdre().equals(numeroOrdre)))
                                && ((groupeId == null) || (uo.getGroupeProfilActeurId() != null && uo.getGroupeProfilActeurId().equals(groupeId)))
                                && ((villeId == null) || (uo.getVilleId() != null && uo.getVilleId().equals(villeId)))
                                && ((adresse == null || adresse.equals("")) || (uo.getAdresse() != null && uo.getAdresse().toUpperCase().contains(adresse.toUpperCase())))) {
                            result.add(uo);
                        }
                    }
                });
            }
        }
        /*Transformation de la liste en page*/
        int start = (int) pageable.getOffset();
        int end = result.size();
        Page<UniteOrganisationDTO> retour = new PageImpl<UniteOrganisationDTO>(result.subList(start, end), pageable, result.size());
        return retour;
    }

    /**
     * Liste des unites d'organisations du reseau de l'unite d'organisation de
     * l'utilisateur connectÃ©
     *
     * @return the list of entities
     */
    public List<UniteOrganisationDTO> findByUserConnect() {
        User userConnecte = userService.getUserWithAuthorities();
        if (userConnecte != null) {
            UserDTO user = userMapper.userToUserDTO(userConnecte);
            if (user.getAuthorities().contains(AuthoritiesConstants.ADMIN_SYSTEME)) {
                return this.findAllInList();
            }
            UniteOrganisation uo = userConnecte.getUniteOrganisation();
            if (uo != null) {
                ArrayList<UniteOrganisation> listAll = new ArrayList<>();
                ArrayList<UniteOrganisation> listFils = new ArrayList<>();
                listAll.addAll(this.findAllUoFils(uo, listAll, listFils));
                listAll.add(uo);

                ArrayList<UniteOrganisation> listPeres = new ArrayList<>();
                ArrayList<UniteOrganisation> list = new ArrayList<>();

                if (uo.getPereUO() != null) {
                    listPeres.addAll(this.findAllUoParents(uo.getPereUO(), list));
                }
                listPeres.addAll(listAll);
                return uniteOrganisationMapper.toDto(listPeres);
            } else {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    /**
     * Get all the uniteOrganisations.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<UniteOrganisationDTO> findAllInList() {

        List<UniteOrganisationDTO> uniteOrganisationDTOList = uniteOrganisationRepository.findForViewPaginatedOptimiseNew();
        //List<UniteOrganisationDTO> uniteOrganisationDTOList = uniteOrganisationMapper.toDto(uniteOrganisationRepository.findAllBySupprimeIsFalse());
        List<UniteOrganisationDTO> uniteOrganisationDTOListNew = new ArrayList<UniteOrganisationDTO>();
        for (UniteOrganisationDTO uniteOrganisationDTO : uniteOrganisationDTOList) {
            if (uniteOrganisationDTO.getTypeUOId() != null) {
                TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationRepository.findById(uniteOrganisationDTO.getTypeUOId()).orElse(null);
                Integer niveau = typeUniteOrganisation.getNiveau();
                if (niveau >= 2) {
                    uniteOrganisationDTO.setWithPV(true);
                } else {
                    uniteOrganisationDTO.setWithPV(false);
                }
            }
            uniteOrganisationDTOListNew.add(uniteOrganisationDTO);
        }
        return uniteOrganisationDTOListNew;

    }

    /**
     * Get one uniteOrganisation by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    public UniteOrganisationDTO findOne(Long id) {
        log.debug("Request to get UniteOrganisation : {}", id);

        UniteOrganisationDTO uniteOrganisationdto = uniteOrganisationRepository.findDtoById(id);

        // uniteOrganisationdto = uniteOrganisationMapper.toDto(uniteOrganisation);
        return uniteOrganisationdto;

    }

    public UniteOrganisation findById(Long id) {
        log.debug("Request to get UniteOrganisation : {}", id);
        UniteOrganisation uniteOrganisation = new UniteOrganisation();

        UniteOrganisationDTO uniteOrganisationdto = uniteOrganisationRepository.findDtoById(id);

        uniteOrganisation = uniteOrganisationMapper.toEntity(uniteOrganisationdto);
        return uniteOrganisation;
    }

    public void delete(Long id) {
        UniteOrganisation uniteOrganisation = uniteOrganisationRepository.findById(id).orElse(null);
        if (!verifOrganisationHaveSolde(uniteOrganisation)) {
            uniteOrganisation.setSupprime(true);
            uniteOrganisationRepository.save(uniteOrganisation);
        } else {
            throw new CustomParameterizedException("Impossible de supprimer une organisation qui a un solde");
        }

    }

    /**
     * Delete the uniteOrganisation by id.
     *
     * @param id the id of the entity
     */
    public void deleteOld(Long id) {
        log.debug("Request to delete UniteOrganisation : {}", id);
        UniteOrganisationDTO uniteOrganisationDTO = this.findOne(id);
        UniteOrganisation uniteOrganisation = uniteOrganisationRepository.findById(id).orElse(null);
        UniteOrganisationDTO uniteOrganisationDTOPere = new UniteOrganisationDTO();
        Boolean var = false;
        if (!pointVenteRepository.findByUniteOrganisation(uniteOrganisation).isEmpty()) {
            throw new CustomParameterizedException("Cette unite d'organisation possede des points de vente donc elle ne peut Ãªtre supprimÃ©e.");
        }
        if (!compteRepository.findByUniteOrganisation(uniteOrganisation).isEmpty()) {
            throw new CustomParameterizedException("Cette unite d'organisation possede des comptes donc elle ne peut Ãªtre supprimÃ©e.");
        }
        if (uniteOrganisation.getPereUO() != null) {
            var = true;
            uniteOrganisationDTOPere = this.findOne(uniteOrganisationDTO.getPereUOId());
        }

        List<UniteOrganisation> listFils = uniteOrganisationRepository.findFilsOfOrganisation(id);
        if (listFils.isEmpty()) {
            uniteOrganisationRepository.deleteById(id);
        } else {
            throw new CustomParameterizedException("Cette unite d'organisation possede des sous-unite-organisations donc elle ne peut Ãªtre supprimÃ©e.");
        }
        if (var == true && uniteOrganisationDTOPere.isFeuille() == false && uniteOrganisationRepository.findByPereUOAndSupprimeIsFalse(uniteOrganisationRepository.findById(uniteOrganisationDTOPere.getId()).orElse(null)).isEmpty()) {
            uniteOrganisationDTOPere.setFeuille(true);
            this.save(uniteOrganisationDTOPere);
        }
    }

    /**
     * Get all the uniteOrganisations.
     *
     * @return list des organisations en fonction du typeUO
     */
    public List<UniteOrganisationDTO> findAllByTypeUo(TypeUO type) {

        log.debug("Request to get all UniteOrganisations");
        if (type != null) {
            TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationRepository.findByLibelleTypeUO(String.valueOf(type));
            if (typeUniteOrganisation != null) {
                //return uniteOrganisationMapper.toDto(uniteOrganisationRepository.findByTypeUOAndSupprimeIsFalse(typeUniteOrganisation));
                return uniteOrganisationRepository.findByTypeUOAndSupprimeIsFalseOptimise(typeUniteOrganisation.getId());

            } else {
                throw new CustomParameterizedException("Le type uo n'est pas valide ");
            }
        } else {
            throw new CustomParameterizedException("Le type uo n'est pas valide ");
        }
    }

    public List<UniteOrganisationDTO> findAllByTypeUoOptimise(TypeUO type) {

        log.debug("Request to get all UniteOrganisations");
        if (type != null) {
            TypeUniteOrganisationDTO typeUniteOrganisation = typeUniteOrganisationRepository.findLibelleTypeUO(String.valueOf(type));
            if (typeUniteOrganisation != null) {
                return uniteOrganisationRepository.findByTypeUoOptimise(typeUniteOrganisation.getId());
            } else {
                throw new CustomParameterizedException("Le type uo n'est pas valide ");
            }
        } else {
            throw new CustomParameterizedException("Le type uo n'est pas valide ");
        }
    }

    /**
     * Get some uniteOrganisations.
     *
     * @return the list des organisations en fonction du typeUO
     */
    public List<UniteOrganisationDTO> findByTypeUo(Long typeId) {
        log.debug("Request to get all UniteOrganisations");
        List<UniteOrganisation> uniteOrganisation = uniteOrganisationRepository.findByTypeUOAndSupprimeIsFalseOrderByLibelleUniteOrganisation(typeUniteOrganisationRepository.findById(typeId).orElse(null));
        return uniteOrganisationMapper.toDto(uniteOrganisation);
    }

    public List<UniteOrganisation> findByType(Long typeId) {
        log.debug("Request to get all UniteOrganisations");
        List<UniteOrganisation> uniteOrganisations = uniteOrganisationRepository.findByTypeUOAndSupprimeIsFalseOrderByLibelleUniteOrganisation(typeUniteOrganisationRepository.findById(typeId).orElse(null));
        return uniteOrganisations;
    }

    /**
     * Recherche le niveau d'une organisation
     *
     * @param uniteOrganisationDTO
     * @return
     * @throws NumberFormatException
     */
    public Integer getNiveau(UniteOrganisationDTO uniteOrganisationDTO) throws NumberFormatException {
        Integer niveau;
        if (uniteOrganisationDTO.getPereUOId() == null) {
            niveau = 1;
        } else {
            UniteOrganisationDTO pere = this.findOne(uniteOrganisationDTO.getPereUOId());
            Integer niveauPere = pere.getNiveau();
            niveau = niveauPere + 1;
        }
        return niveau;
    }

    /**
     * Recherche du niveau d'une organisation en fonction de son id
     *
     * @param organisationId
     * @return
     */
    public Integer getUONiveau(Long organisationId) {
        if (organisationId != null) {
            UniteOrganisation uniteOrganisation = uniteOrganisationRepository.findById(organisationId).orElse(null);
            if (uniteOrganisation != null) {
                return uniteOrganisation.getNiveau();
            } else {
                throw new CustomParameterizedException("L'unite d'organisation est introuvable.");
            }
        } else {
            throw new CustomParameterizedException("Identifiant invalide.");
        }
    }

    /**
     * Recherchde de l'oragnisation de l'agence siÃ¨ge
     *
     * @return
     */
    public UniteOrganisation getUoAgenceSiege(UniteOrganisation organisation) {
        UniteOrganisation uoAgenceSiege;
        UniteOrganisation uniteOrganisationLast = organisation;

        uniteOrganisationLast = uniteOrganisationRepository.findById(organisation.getId()).orElse(null);
        UniteOrganisation org = uniteOrganisationRepository.findById(organisation.getId()).orElse(null);
        if (uniteOrganisationLast.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.name())) {
            uoAgenceSiege = this.getUoAgenceSiegeWithUoFse(org);
            return uoAgenceSiege;
        }
        UniteOrganisation uoPays = this.findUoParentByType(uniteOrganisationLast, TypeUO.PAYS);
        UniteOrganisation uoFse = this.getUoFseFilialeWithUoPays(uoPays);
        uoAgenceSiege = this.getUoAgenceSiegeWithUoFse(uoFse);
        return uoAgenceSiege;
    }

    /**
     * Recherche des organisations de niveau pÃ¨re d'une organisation
     *
     * @param uniteOrganisation
     * @param list
     * @return
     */
    public List<UniteOrganisation> findAllUoParents(UniteOrganisation uniteOrganisation, ArrayList<UniteOrganisation> list) {
        UniteOrganisation uo = findOneById(uniteOrganisation.getId());
        if (uo.getNiveau() > 0) {
            list.add(uo);
        }
        if (uo.getPereUO() == null) {
            return list;
        } else {
            UniteOrganisation unitePere = findOneById(uo.getPereUO().getId());
            return findAllUoParents(unitePere, list);
        }
    }

    /**
     * L'UO parent en fonction du type d'une unitÃ© d'organisation
     *
     * @param uniteOrganisation
     * @param typeUO
     * @return
     */
    public UniteOrganisation findUoParentByType(UniteOrganisation uniteOrganisation, TypeUO typeUO) {
        TypeUniteOrganisation typeUniteOrganisation;
        String libellTypeUo;
        if (uniteOrganisation.getTypeUO().getLibelleTypeUO() == null) {
            typeUniteOrganisation = typeUniteOrganisationRepository.findById(uniteOrganisation.getTypeUO().getId()).orElse(null);
            libellTypeUo = typeUniteOrganisation.getLibelleTypeUO();
        } else {
            libellTypeUo = uniteOrganisation.getTypeUO().getLibelleTypeUO();

        }
        if (libellTypeUo.equals(String.valueOf(typeUO))) {
            return uniteOrganisation;
        }
        if (uniteOrganisation.getPereUO() == null) {
            return null;
        } else {
            UniteOrganisation unitePere = findOneById(uniteOrganisation.getPereUO().getId());
            return findUoParentByType(unitePere, typeUO);
        }
    }

    /**
     * Liste des identifiants des parents d'une organisation
     *
     * @param uniteOrganisation
     * @param list
     * @return
     */
    public List<Long> findAllUoParentsIds(UniteOrganisation uniteOrganisation, ArrayList<Long> list) {
        // UniteOrganisation uo = findOneById(uniteOrganisation.getId());
        if (uniteOrganisation.getNiveau() > 0) {
            list.add(uniteOrganisation.getId());
        }
        if (uniteOrganisation.getPereUO() == null) {
            return list;
        } else {
            UniteOrganisation unitePere = findOneById(uniteOrganisation.getPereUO().getId());
            return findAllUoParentsIds(unitePere, list);
        }
    }

    /**
     * Liste des identifiants des parents d'une organisation
     *
     * @param uniteOrganisation
     * @param list
     * @return
     */
    public List<Long> findAllUoParentsIdsCanDoOperation(UniteOrganisation uniteOrganisation, ArrayList<Long> list) {

        UniteOrganisation uoPere = null;
        if (uniteOrganisation != null && uniteOrganisation.getPereUO() != null) {
            uoPere = findOneById(uniteOrganisation.getPereUO().getId());
        }

        if (uniteOrganisation != null) {
            if (uniteOrganisation.getTypeUO() != null && uniteOrganisation.getTypeUO().getNiveau() > 1) {
                list.add(uniteOrganisation.getId());
            }
            if (uoPere == null) {
                return list;
            }
            if (uoPere.getTypeUO().getCodeTypeUO().equals(TypeUO.AGENCE.name())) {
                return list;
            } else {
                return findAllUoParentsIdsCanDoOperation(uoPere, list);
            }
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Recherche des uo fils
     *
     * @param uniteOrganisation
     * @param list
     * @param listFils
     * @return
     */
    public List<UniteOrganisation> findAllUoFils(UniteOrganisation uniteOrganisation, ArrayList<UniteOrganisation> list,
            ArrayList<UniteOrganisation> listFils) {
        List<UniteOrganisationDTO> uoFilsDto = uniteOrganisationRepository.findByPereUOAndSupprimeIsFalseOptimiser(uniteOrganisation.getId());
        List<UniteOrganisation> uoFils = uniteOrganisationMapper.toEntity(uoFilsDto);
        if (uoFils.isEmpty()) {
            return listFils;
        } else {
            listFils.addAll(uoFils);
            for (UniteOrganisation uo : uoFils) {
                findAllUoFils(uo, list, listFils);
            }
            return listFils;
        }
    }

    public List<UniteOrganisation> findAllUoFils(UniteOrganisation uniteOrganisation) {
        List<UniteOrganisationDTO> uoFilsDto = uniteOrganisationRepository.findByFseId(uniteOrganisation.getId());
        List<UniteOrganisation> uoFils = uniteOrganisationMapper.toEntity(uoFilsDto);
        return uoFils;

    }

    /**
     *
     * @param uniteOrganisation
     * @param list
     * @param listFils
     * @param villeId
     * @return
     */
    public List<UniteOrganisation> findAllUoFilsMultiCriteria(UniteOrganisation uniteOrganisation, ArrayList<UniteOrganisation> list,
            ArrayList<UniteOrganisation> listFils, Long villeId) {
        List<UniteOrganisation> uoFils;
        uoFils = uniteOrganisationRepository.findByPereUOAndSupprimeIsFalse(uniteOrganisation);
        if (uoFils.isEmpty()) {
            return listFils;
        } else {
            if (villeId != null) {
                listFils.addAll(uoFils.stream().filter(uo -> uo.getVille().getId().equals(villeId)).collect(Collectors.toList()));
            } else {
                listFils.addAll(uoFils);
            }
            for (UniteOrganisation uo : uoFils) {
                findAllUoFilsMultiCriteria(uo, list, listFils, villeId);
            }
            return listFils;
        }
    }

    /**
     * Recherche des uo fils par type
     *
     * @param type
     * @param uniteOrganisation
     * @param listFils
     * @return
     */
    public List<UniteOrganisationDTO> findAllUoFilsByTypeFiltre(TypeUO type, UniteOrganisation uniteOrganisation, ArrayList<UniteOrganisation> listFils) {
        List<UniteOrganisation> uoFils = uniteOrganisationRepository.findByPereUOAndSupprimeIsFalse(uniteOrganisation);
        if (uoFils.isEmpty()) {
            return uniteOrganisationMapper.toDto(listFils);
        } else {
            Optional<UniteOrganisation> uoExist = uoFils.stream().findFirst();
            if (uoExist.isPresent()) {
                listFils.addAll(uoFils.stream().filter(uo -> uo.getTypeUO().getLibelleTypeUO().equals(String.valueOf(type))).collect(Collectors.toList()));
                //   if (uoExist.get().getTypeUO().getLibelleTypeUO().equals(String.valueOf(type)))
                //    listFils.addAll(uoFils);
                //  else
                for (UniteOrganisation uo : uoFils) {
                    findAllUoFilsByTypeFiltre(type, uo, listFils);
                }
            }

            return uniteOrganisationMapper.toDto(listFils);
        }
    }

    public List<UniteOrganisationDTO> findAllUoFilsByTypeFiltreOptimiser(TypeUO type, Long uniteOrganisationId, ArrayList<UniteOrganisationDTO> listFils) {
        List<UniteOrganisationDTO> uoFils = uniteOrganisationRepository.findByPereUOAndSupprimeIsFalseOptimiser(uniteOrganisationId);
        if (uoFils.isEmpty()) {
            return listFils;
        } else {
            Optional<UniteOrganisationDTO> uoExist = uoFils.stream().findFirst();
            if (uoExist.isPresent()) {
                listFils.addAll(uoFils.stream().filter(uo -> uo.getTypeUOLibelleTypeUO().equals(String.valueOf(type))).collect(Collectors.toList()));
                for (UniteOrganisationDTO uo : uoFils) {
                    findAllUoFilsByTypeFiltreOptimiser(type, uo.getId(), listFils);
                }
            }

            return listFils;
        }
    }

    /**
     * Recherche des uo fils par type
     *
     * @param type
     * @param uniteOrganisation
     * @param listFils
     * @return
     */
    public List<UniteOrganisationDTO> findAllUoFilsByType(TypeUO type, UniteOrganisation uniteOrganisation, ArrayList<UniteOrganisation> listFils) {
        List<UniteOrganisation> uoFils = uniteOrganisationRepository.findByPereUOAndSupprimeIsFalse(uniteOrganisation);
        if (uoFils.isEmpty()) {
            return uniteOrganisationMapper.toDto(listFils);
        } else {
            Optional<UniteOrganisation> uoExist = uoFils.stream().findFirst();
            if (uoExist.isPresent()) {
                if (uoExist.get().getTypeUO().getLibelleTypeUO().equals(String.valueOf(type))) {
                    listFils.addAll(uoFils);
                } else {
                    for (UniteOrganisation uo : uoFils) {
                        findAllUoFilsByType(type, uo, listFils);
                    }
                }
            }

            return uniteOrganisationMapper.toDto(listFils);
        }
    }

    /**
     * L'unite d'organisation de l'utilisateur connecte d'un point de vente
     * --Utilisateur d'un PV : on recupere l'UO du PV --Utilisateur d'une agence
     *
     * @return
     */
    public UniteOrganisation getUoOfUserConnecte() {
        User userConnecte = userService.getUserWithAuthorities();
        if (userConnecte != null) {
            //  PointVente pointVente = userConnecte.getPointVente();
            //  if (pointVente != null) {
            // return pointVente.getUniteOrganisation();
            //   } else {
            return userConnecte.getUniteOrganisation();
            //  }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public UniteOrganisationDTO getUoDTOOfUserConnecte() {
        User userConnecte = userService.getUserWithAuthorities();
        if (userConnecte != null) {
            // PointVente pointVente = userConnecte.getPointVente();
            //  if (pointVente != null) {
            // return getUniteOrganisationDTO(pointVente.getUniteOrganisation());
            //  } else {
            return getUniteOrganisationDTO(userConnecte.getUniteOrganisation());
            // }
        }
        return null;
    }

    /**
     *
     * @param uniteOrganisation
     * @return
     */
    private UniteOrganisationDTO getUniteOrganisationDTO(UniteOrganisation uniteOrganisation) {
        UniteOrganisation uo = uniteOrganisation;
        UniteOrganisation uoPays = this.findUoParentByType(uo, TypeUO.PAYS);
        UniteOrganisation uoFse = this.findUoParentByType(uo, TypeUO.FSE);
        UniteOrganisationDTO uoDto = uniteOrganisationMapper.toDto(uo);
        if (uoFse != null) {
            uoDto.setFournisseurId(uoFse.getId());
        }

        if (uoPays != null) {
            uoDto.setUoPaysLibelle(uoPays.getLibelleUniteOrganisation());
        }
        return uoDto;
    }

    /**
     * Liste des Fils d'une organsation d'un type donnÃ©
     *
     * @param uoId
     * @param libelleTypeUO
     * @return
     */
    public List<UniteOrganisationDTO> findUoAllFilsByType(Long uoId, TypeUO libelleTypeUO) {
        UniteOrganisation uniteOrganisation = null;
        if (uoId == null) {
            UniteOrganisation uniteOrganis = getUoOfUserConnecte();
            UniteOrganisation uoPaysSrc = this.findOneById(uniteOrganis.getUoPaysId());
            // UniteOrganisation uoPaysSrc = findUoParentByType(uniteOrganisation, TypeUO.PAYS);
            uoId = uoPaysSrc.getId();
            uniteOrganisation = uoPaysSrc;
        } else {
            uniteOrganisation = new UniteOrganisation();
            UniteOrganisationDTO uniteODTo = uniteOrganisationRepository.findDtoById(uoId);
            uniteOrganisation = uniteOrganisationMapper.toEntity(uniteODTo);
            // BeanUtils.copyProperties(uniteOrganisation, uniteODTo);
        }

        TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationRepository.findByLibelleTypeUO(String.valueOf(libelleTypeUO));
        if (uniteOrganisation != null && typeUniteOrganisation != null) {
            List<UniteOrganisationDTO> listUoDto = new ArrayList();
            //  return uniteOrganisationMapper.toDto(uniteOrganisationRepository.findByPereUOAndTypeUOAndSupprimeIsFalse(uniteOrganisation, typeUniteOrganisation));
            List<UniteOrganisationDTO> listUo = uniteOrganisationRepository.findByPereUoAndType(uniteOrganisation.getId(), typeUniteOrganisation.getId());
            // List<UniteOrganisation> listUo=   uniteOrganisationRepository.findByPereUoAndType(uniteOrganisation.getId(), typeUniteOrganisation.getId());

            return listUo;
        }
        return Collections.emptyList();
    }

    /**
     * La filiale d'une organisation -On recupere l'organisation FSE -si c'est
     * la filiale OK -si non on recupere le pays du FSE obtenu -on recherche la
     * filiale
     */
    public UniteOrganisation getFilialeOfUniteOrganisation(UniteOrganisation organisation) {
        UniteOrganisation uoPays;
        if (!organisation.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
            uoPays = this.findOneById(organisation.getUoPaysId());
        } else {
            uoPays = organisation;
        }

        //UniteOrganisation uoPays = this.findOneById(organisation.getUoPaysId());
        //UniteOrganisation uoPays = findUoParentByType(organisation, TypeUO.PAYS);
        return getUoFseFilialeWithUoPays(uoPays);
    }

    public UniteOrganisation getFSEOfUniteOrganisation(UniteOrganisation organisation) {
        UniteOrganisation uoFSE = findUoParentByType(organisation, TypeUO.FSE);
        return uoFSE;
    }

    /**
     * Recherche filiale de l'organisation de l'utilisateur connectÃ©
     *
     * @return
     */
    public UniteOrganisation getFilialeOfUniteOrganisationConnecte() {
        UniteOrganisation uoPays = findUoParentByType(getUoOfUserConnecte(), TypeUO.PAYS);
        return getUoFseFilialeWithUoPays(uoPays);
    }

    /**
     * Retourne la filiale d'une uniteOrganisation d'un pays on recherche la
     * filiale parmis les FSE qui sont associÃ©s au pays si l'on ne retrouve pas
     * le FSE(filiale) on recherche les UniteRegroupements du pays puis on
     * recherche parmis les fils de chaque uniteRegroupe le FSE filiale
     */
    public UniteOrganisation getUoFseFilialeWithUoPays(UniteOrganisation uoPays) {
        if (uoPays != null) {
            TypeUniteOrganisation typeUniteOrganisa = typeUniteOrganisationRepository.findById(uoPays.getTypeUO().getId()).orElse(null);
            if (typeUniteOrganisa.getLibelleTypeUO().equals(String.valueOf(TypeUO.PAYS))) {
                TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationRepository.findByLibelleTypeUO(String.valueOf(TypeUO.FSE));
                UniteOrganisation uniteOrganisationFiliale = uniteOrganisationRepository.findByPereUOAndTypeUOAndEstFilialeAndSupprimeIsFalse(uoPays, typeUniteOrganisation, Boolean.TRUE);

                if (uniteOrganisationFiliale != null) {
                    return uniteOrganisationFiliale;
                } else {
                    throw new CustomParameterizedException("La Filiale de ce pays  est introuvable :" + uoPays.getLibelleUniteOrganisation());
                }
            } else if (uoPays.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.FSE))) {
                return uoPays;
            } else {
                throw new CustomParameterizedException("L'organisation " + uoPays.getLibelleUniteOrganisation() + " n'est pas de type Pays!");
            }
        } else {
            throw new CustomParameterizedException("L'organisation est invalide");
        }
    }

    /**
     * @param fseDest
     * @return
     */
    public UniteOrganisation getFseAgenceSiege(UniteOrganisation fseDest) {
        log.info("AGENCE SIEGE %: " + fseDest.getLibelleUniteOrganisation());
        UniteOrganisation uoDestAgenceSiege;
        if (fseDest != null && fseDest.isEstFiliale()) {
            uoDestAgenceSiege = this.getUoAgenceSiegeWithUoFse(fseDest);
        } else if (fseDest.getCodeFse() != null) {
            uoDestAgenceSiege = this.getUoAgenceSiegeWithUoFse(fseDest);
        } else {
            UniteOrganisation fseDestPays = this.findOneById(fseDest.getUoPaysId());
            //   UniteOrganisation fseDestPays = this.findUoParentByType(fseDest, TypeUO.PAYS);
            UniteOrganisation fseDestFiliale = this.getUoFseFilialeWithUoPays(fseDestPays);
            uoDestAgenceSiege = this.getUoAgenceSiegeWithUoFse(fseDestFiliale);
        }
        return uoDestAgenceSiege;
    }

    /**
     * Retourne l'agence siege d'une uniteOrganisation d'un FSE on recherche
     * l'agence parmis les Agences qui sont associÃ©s au FSE si l'on ne retrouve
     * pas l'AGENCE(siege) on recherche les UniteRegroupements du FSE puis on
     * recherche parmis les fils de chaque uniteRegroupement le FSE filiale
     */
    public UniteOrganisation getUoAgenceSiegeWithUoFse(UniteOrganisation uoFse) {
        if (uoFse != null) {
            TypeUniteOrganisation typeUniteOrganisa = typeUniteOrganisationService.findByOne(uoFse.getTypeUO().getId());
            if (typeUniteOrganisa.getLibelleTypeUO().equals(String.valueOf(TypeUO.FSE))) {
                TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationRepository.findByLibelleTypeUO(String.valueOf(TypeUO.AGENCE));
                List<UniteOrganisation> uniteOrganisationSieges = uniteOrganisationRepository.findByPereUOAndTypeUOAndEstSiegeAndSupprimeIsFalse(uoFse, typeUniteOrganisation, Boolean.TRUE);
                Optional<UniteOrganisation> uniteOrganisationExist = uniteOrganisationSieges.stream().findFirst();
                if (uniteOrganisationExist.isPresent()) {
                    return uniteOrganisationExist.get();
                } else {
                    return null;
                }
            } else {
                throw new CustomParameterizedException("L'organisation n'est pas de type FSE " + uoFse.getLibelleUniteOrganisation() + "  " + uoFse.getTypeUO());
            }
        } else {
            throw new CustomParameterizedException("L'organisation est invalide");
        }
    }

    /**
     * Recherche le pere d'une organisation d'un type donnÃ©e
     *
     * @param uoId
     * @param libelleTypeUO
     * @return
     */
    public UniteOrganisationDTO findPereByType(Long uoId, TypeUO libelleTypeUO) {
        if (uoId != null) {
            UniteOrganisation uniteOrganisation = uniteOrganisationRepository.findById(uoId).orElse(null);
            return uniteOrganisationMapper.toDto(this.findUoParentByType(uniteOrganisation, libelleTypeUO));
        }
        return null;
    }

    /**
     * Liste de tous les fils d'une organisation(pas seulement les fils directs)
     *
     * @param uniteOrganisation
     * @param listAll
     * @param listFilsDirect
     * @param listFils
     * @param i
     * @return
     */
    public List<UniteOrganisation> findUoFilsOfAnUO(UniteOrganisation uniteOrganisation, ArrayList<UniteOrganisation> listAll, ArrayList<UniteOrganisation> listFilsDirect, ArrayList<UniteOrganisation> listFils, Integer i) {

        if (listFilsDirect.isEmpty()) {
            listFilsDirect = (ArrayList<UniteOrganisation>) uniteOrganisationRepository.findByPereUOAndSupprimeIsFalse(uniteOrganisation);
            listAll.add(uniteOrganisation);
        }

        if (!listFilsDirect.isEmpty()) {
            if (uniteOrganisationRepository.findByPereUOAndSupprimeIsFalse(listFilsDirect.get(i)) != null) {
                listFils.addAll(uniteOrganisationRepository.findByPereUOAndSupprimeIsFalse(listFilsDirect.get(i)));
            }
        } else {
            return listAll;
        }

        if (i == (listFilsDirect.size() - 1)) {
            i = 0;
            listAll.addAll(listFilsDirect);
            listAll.addAll(listFils);
            listFilsDirect = listFils;
            listFils.clear();
            if (listFilsDirect.isEmpty()) {
                return listAll;
            } else {
                return findUoFilsOfAnUO(listFilsDirect.get(i), listAll, listFilsDirect, listFils, 0);
            }
        } else {
            i++;
            return findUoFilsOfAnUO(listFilsDirect.get(i), listAll, listFilsDirect, listFils, i);
        }
    }

    /**
     * Recherche de tous les utilisateurs d'une organisation
     *
     * @param pageable
     * @param uoId
     * @param profilId
     * @return
     */
    public Page<UserDTO> findAllUserByOrganisation(Pageable pageable, Long uoId, Long profilId, String login, String telephone) {

        User userConnecte = userService.getUserWithAuthorities();
        List<UniteOrganisation> uniteOrganisationList = new ArrayList<>();
        List<UserDTO> userDTOList = new ArrayList<>();
        if (userConnecte != null) {
            UserDTO user = userMapper.userToUserDTO(userConnecte);
            if (user.getAuthorities().contains(AuthoritiesConstants.ADMIN_SYSTEME)) {
                return userService.getAllManagedUsers(pageable, uoId, profilId, login, telephone);
            }
            UniteOrganisation uo = uniteOrganisationRepository.findById(user.getUniteOrganisationId()).orElse(null);
            if (uo != null) {
                return getAllManagedUsersOfUserConnected(pageable, uo, profilId, login, telephone);

                /* ArrayList<UniteOrganisation> listAll = new ArrayList<>();
                ArrayList<UniteOrganisation> listPeres = new ArrayList<>();
                ArrayList<UniteOrganisation> listFils = new ArrayList<>();
                uniteOrganisationList = this.findUoFilsOfAnUO(uo, listAll, listPeres, listFils, 0);
                uniteOrganisationList.stream().forEach(uniteOrganisation -> {
                    userDTOList.addAll(userMapper.usersToUserDTOs(userRepository.findByUniteOrganisationId(uniteOrganisation.getId())));
                });
                return new PageImpl<UserDTO>(userDTOList);*/
            } else {
                return new PageImpl<User>(Collections.emptyList()).map(UserDTO::new);
            }
        }
        return new PageImpl<User>(Collections.emptyList()).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsersOfUserConnected(Pageable pageable, UniteOrganisation uo, Long profilId, String login, String telephone) {
        List<UniteOrganisation> uniteOrganisationList = new ArrayList<>();
        List<UserDTO> userDTOList = new ArrayList<>();
        ArrayList<UniteOrganisation> listAll = new ArrayList<>();
        ArrayList<UniteOrganisation> listPeres = new ArrayList<>();
        ArrayList<UniteOrganisation> listFils = new ArrayList<>();
        uniteOrganisationList = this.findUoFilsOfAnUO(uo, listAll, listPeres, listFils, 0);
        uniteOrganisationList.stream().forEach(uniteOrganisation -> {
            //userRepository.findByUniteOrganisationId(uniteOrganisation.getId()
            userDTOList.addAll(userMapper.usersToUserDTOs(userRepository.findUserWithCriteria(uniteOrganisation.getId(), profilId, login, telephone)));
        });

        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > userDTOList.size() ? userDTOList.size() : (start + pageable.getPageSize());
        Page<UserDTO> retour = new PageImpl<UserDTO>(userDTOList.subList(start, end), pageable, userDTOList.size());
        return retour;
    }

    /**
     * Recuperation du code du FSE source l'organisation puis son organisation
     * de type FSE
     */
    public String getCodeUoFse(UniteOrganisation uniteOrganisationSource) {
        UniteOrganisation fseSource = this.getFSEOfUniteOrganisation(uniteOrganisationSource);
        return fseSource.getCodeFse();
    }

    /**
     *
     * @param uniteOrganisation
     * @return
     */
    public UniteOrganisation getUoFse(UniteOrganisation uniteOrganisation) {
        if (uniteOrganisation.getTypeUO().equals(typeUniteOrganisationRepository.findByLibelleTypeUO("PAYS"))) {
            return getUoFseFilialeWithUoPays(uniteOrganisation);
        }
        UniteOrganisation fseSource = this.getFilialeOfUniteOrganisation(uniteOrganisation);
        return fseSource;
    }

    /**
     *
     * @param codeFse
     * @return
     */
    public UniteOrganisation findFseByCode(String codeFse) {
        TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationRepository.findByCodeTypeUO(String.valueOf(TypeUO.FSE)).get();
        return uniteOrganisationRepository.findByTypeUOAndCodeFseAndSupprimeIsFalseOrderByLibelleUniteOrganisation(typeUniteOrganisation, codeFse);
    }

    public UniteOrganisation findPaysByCode(String codeFse) {
        TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationRepository.findByCodeTypeUO(String.valueOf(TypeUO.FSE)).get();
        return uniteOrganisationRepository.findByTypeUOAndCodeFseAndSupprimeIsFalseOrderByLibelleUniteOrganisation(typeUniteOrganisation, codeFse);
    }

    /**
     *
     * @param code
     * @return
     */
    public UniteOrganisation findByCodeFSE(String code) {
        UniteOrganisationDTO uoDto;
        if (uniteOrganisationCODEFSEMapByID.containsKey(code) && uniteOrganisationCODEFSEMapByID.get(code) != null) {
            uoDto = uniteOrganisationCODEFSEMapByID.get(code);
        } else {

            uoDto = uniteOrganisationRepository.findCodeFseAndSupprimeIsFalse(code);
            if (uoDto != null) {
                uniteOrganisationCODEFSEMapByID.put(code, uoDto);
            }

        }

        return uniteOrganisationMapper.toEntity(uoDto);
        //  return uniteOrganisationRepository.findByCodeFseAndSupprimeIsFalse(code);
    }

    public UniteOrganisation findByCode(String code) {
        return uniteOrganisationRepository.findByCodeUOAndSupprimeIsFalseAndEstActifIsTrue(code);
    }

    public UniteOrganisation findByCodeAndTelephone(String codeUo, String telephone, String codePays) {
        return uniteOrganisationRepository.findByCodeAndTelephone(codeUo, telephone);
    }

    public UniteOrganisation findByCodeUOAndSupprimeIsFalse(String codeUo, Boolean isSupprime) {
        UniteOrganisationDTO uoDto;
        if (uniteOrganisationMapByID.containsKey(codeUo) && uniteOrganisationMapByID.get(codeUo) != null) {
            uoDto = uniteOrganisationMapByID.get(codeUo);
        } else {
            uoDto = uniteOrganisationRepository.findByCodeUOAndSupprimeIsFalse(codeUo, isSupprime);
            if (uoDto != null) {
                uniteOrganisationMapByID.put(codeUo, uoDto);
            }

        }

        return uniteOrganisationMapper.toEntity(uoDto);

    }

    public UniteOrganisation findByCodeFseAndSupprimeIsFalse(String codefse) {
        UniteOrganisationDTO uoDto;
        if (uniteOrganisationCODEFSEMapByID.containsKey(codefse) && uniteOrganisationCODEFSEMapByID.get(codefse) != null) {
            uoDto = uniteOrganisationCODEFSEMapByID.get(codefse);
            log.info("Find Unite organisation from cache");
        } else {
            uoDto = uniteOrganisationRepository.findCodeFseAndSupprimeIsFalse(codefse);
            if (uoDto != null) {
                log.info("Find Unite organisation " + uoDto.toString());
                uniteOrganisationCODEFSEMapByID.put(codefse, uoDto);
            }

        }

        return uniteOrganisationMapper.toEntity(uoDto);

    }

    public UniteOrganisation findOneById(Long id) {
        log.info("Find Unite organisation " + id);
        UniteOrganisationDTO uoDto;
        if (uniteOrganisationMapByID.containsKey(id.toString()) && uniteOrganisationMapByID.get(id.toString()) != null) {
            uoDto = uniteOrganisationMapByID.get(id.toString());
        } else {
            uoDto = uniteOrganisationRepository.findDtoById(id);
            if (uoDto != null) {
                uniteOrganisationMapByID.put(id.toString(), uoDto);
            }
        }
        return uniteOrganisationMapper.toEntity(uoDto);
    }

    public UniteOrganisation findXOneById(Long id) {
        UniteOrganisationDTO uoDto = uniteOrganisationRepository.findDtoById(id);
        UniteOrganisation uo = new UniteOrganisation();
        if (uoDto != null) {
            BeanUtils.copyProperties(uoDto, uo);
        }

        if (uo != null) {
            return uo;
        }

        return null;

    }

    @Transactional(readOnly = true)
    public SommeSolde getSoldeDistribution(Long pereUoId, String typeUO, String codeFse) {
        SommeSolde soldeD = new SommeSolde();
        // List<UniteOrganisation> principal= new ArrayList<>();
        List<Compte> compteP = new ArrayList<>();
        List<Compte> compteAll = new ArrayList<>();
        Page<UniteOrganisationDTO> principal;
        List<UniteOrganisationDTO> principal_new;
        final BigDecimal[] totalS = {new BigDecimal("0")};
        UniteOrganisation uo = new UniteOrganisation();
        uo = transactionComptePtiRepository.findByUoId(pereUoId);
        BigDecimal total = new BigDecimal("0");
        //BigDecimal totalS = new BigDecimal("0");
        Pageable pageable = new Pageable() {
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
        //principal = findByPereUoIdOp("", "", null, typeUO, pereUoId, null, null, "", pageable);
        // principal_new = findByPereUoIdOptimise("", "", null, typeUO, pereUoId, null, null, "", pageable);
        TypeUniteOrganisation typeAgence = typeUniteOrganisationRepository.findByLibelleTypeUO(TypeUO.AGENCE.name());
        if (typeAgence != null) {
            principal_new = uniteOrganisationRepository.findAgenceDigitaleOptimise(typeAgence.getId());
            if (principal_new.size() > 0) {

                for (UniteOrganisationDTO liste : principal_new) {
                    log.debug("*----- Begin---------" + liste.getEstSiege());
                    if (liste.isEstSiege()) {
                        //List<Compte> comptePnew = compteRepository.findByUOCPTA(prin.getId());
                        //Compte comptePnew=compteRepository.findByUOCPTA(prin.getId());
                        // compteAll.addAll(comptePnew);
                        BigDecimal soldeDistribu = compteRepository.getSoldeTotalDistribution(codeFse, EtiquetteCompteCode.CPTEOPERATIONACTEUR.name(), liste.getId());
                        if(soldeDistribu!=null){
                            totalS[0] = totalS[0].add(soldeDistribu);
                        }
                        
                    }
                }

                log.debug("*----- Fin1---------");
                /* compteAll.forEach(cpt -> {
                BigDecimal totalNew = transactionComptePtiRepository.findSoldeClients(cpt.getValeurCompte());
                totalS[0] = totalS[0].add(totalNew);
            });*/
                log.debug("*----- Fin2---------");

            }
            soldeD.setSolde(totalS[0]);
        }

        return soldeD;
    }

    /*    @Transactional(readOnly = true)
    public SommeSolde getSommeSoldeMarchands(Long pereUoId, String typeUO,String codeFse) {
        SommeSolde soldeD = new SommeSolde();
        SommeSolde soldeM = new SommeSolde();
        List<Long> typeUoIds=new ArrayList<>();
        List<Compte> compteS = new ArrayList<>();
        List<Compte> compteAll = new ArrayList<>();
        Page<UniteOrganisationDTO> principal;
        List<UniteOrganisationDTO> principal_new;
        List<UniteOrganisation> marchand = new ArrayList<>();
        BigDecimal total = new BigDecimal("0");
        final BigDecimal[] totalS = {new BigDecimal("0")};
        Pageable pageable = new Pageable() {
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
        if (pereUoId != null) {
            TypeUniteOrganisation typeAgence=typeUniteOrganisationRepository.findByLibelleTypeUO(typeUO);
            typeUoIds.add(typeAgence.getId());
            principal_new =uniteOrganisationRepository.findAgentOptimise(typeUoIds);
            log.debug("begin---------------Marchands");
            AtomicInteger i = new AtomicInteger();
            principal_new.forEach(prin -> {
                if (!prin.isEstSiege()) {
                    i.set(i.get() + 1);
                    //List<Compte> compteSNew = compteRepository.findByUOCPTA(prin.getId());
                    log.debug("begin---------------cpt");
                    log.debug("begin---------------i");
                    //Compte compteSNew=compteRepository.findByUOCPTA(prin.getId());
                    BigDecimal soldeDistribu =compteRepository.getSoldeTotalDistribution(codeFse,EtiquetteCompteCode.CPTEOPERATIONACTEUR.name(),prin.getId());
                    //compteAll.addAll(compteSNew);
                    if(soldeDistribu!=null){
                        totalS[0] = totalS[0].add(soldeDistribu);
                    }
                }
            });

        } else {
            principal = findAll("", "", null, typeUO, null, null, "", pageable);
            marchand = transactionComptePtiRepository.findAllAgent(typeUO);
            if (marchand != null) {
                for (UniteOrganisation cpt : marchand) {
                    if (!cpt.isEstSiege()) {
                        compteAll.addAll(compteS);
                    }
                }
            }
        }
        compteAll.forEach(cpt -> {
            BigDecimal totalNew = transactionComptePtiRepository.findSoldeClients(cpt.getValeurCompte());
            totalS[0] = totalS[0].add(totalNew);
        });
        soldeD.setSolde(totalS[0]);
        soldeD.setSolde(totalS[0]);
        log.debug("Fin---------------Marchands");

        return soldeD;
    }*/
    @Transactional(readOnly = true)
    public SommeSolde getSommeSoldeMarchands(Long pereUoId, String typeUO, String codeFse) {
        SommeSolde soldeD = new SommeSolde();
        SommeSolde soldeM = new SommeSolde();
        List<Long> typeUoIds = new ArrayList<>();
        List<Compte> compteS = new ArrayList<>();
        List<Compte> compteAll = new ArrayList<>();
        Page<UniteOrganisationDTO> principal;
        List<UniteOrganisationDTO> principal_new;
        List<UniteOrganisation> marchand = new ArrayList<>();
        BigDecimal total = new BigDecimal("0");
        final BigDecimal[] totalS = {new BigDecimal("0")};

        TypeUniteOrganisation typeAgence = typeUniteOrganisationRepository.findByLibelleTypeUO(typeUO);
        typeUoIds.add(typeAgence.getId());
        log.debug("begin---------------Marchands");
        AtomicInteger i = new AtomicInteger();
        log.debug("begin---------------cpt");
        log.debug("begin---------------i");
        BigDecimal soldeDistribu = compteRepository.getSoldeTotalAgent(typeUO, codeFse, EtiquetteCompteCode.CPTEOPERATIONACTEUR.name());
        if (soldeDistribu != null) {
            totalS[0] = totalS[0].add(soldeDistribu);
        }
        soldeD.setSolde(totalS[0]);
        log.debug("Fin---------------Marchands");

        return soldeD;
    }

    public SommeSolde getSommeSoldeClients(Long pereUoId, String codeFse) {
        List<CompteDTO> listCompte = new ArrayList<>();
        SommeSolde solde = new SommeSolde();
        final BigDecimal[] totalS = {new BigDecimal("0")};
        BigDecimal total = new BigDecimal("0");
        UniteOrganisation uo = new UniteOrganisation();
        log.debug("code fse************" + codeFse);
        //listCompte = transactionComptePtiRepository.findAllCompteClientNew(codeFse);
        BigDecimal sommeSoldeClient = compteRepository.getSoldeTotalClient(codeFse);
        solde.setSolde(sommeSoldeClient);
        /*listCompte = transactionComptePtiRepository.findAllCompteClientNewOptimise(codeFse);
        if (listCompte != null) {
            log.debug("*----- Begin---Client------");
            listCompte.forEach(cpt -> {
                BigDecimal totalNew = transactionComptePtiRepository.findSoldeClients(cpt.getValeurCompte());
                totalS[0] = totalS[0].add(totalNew);
            });
            log.debug("*----- Fin---Client------");
            solde.setSolde(totalS[0]);
        }*/
        return solde;
    }

    @Transactional(readOnly = true)
    public SommeSolde getSoldeAgent(Long pereUoId, String agent, String codeFse) {
        SommeSolde soldeM = new SommeSolde();
        List<Compte> agentS = new ArrayList<>();
        List<Compte> agentAll = new ArrayList<>();
        List<Long> typeUoIds = new ArrayList<>();
        List<UniteOrganisationDTO> principal_new;
        List<String> listeTypeUO = new ArrayList<>();
        List<UniteOrganisation> marchand = new ArrayList<>();
        BigDecimal total = new BigDecimal("0");
        final BigDecimal[] totalS = {new BigDecimal("0")};
        Pageable pageable = new Pageable() {
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
        Page<UniteOrganisationDTO> uo1, uo2, uo3;
        List<UniteOrganisation> liste = new ArrayList<>();
        if (agent.equals("MASTER")) {
            listeTypeUO.add("MASTER");
            listeTypeUO.add("SOUS_DISTRIBUTEUR");
            listeTypeUO.add("UNITE_REGROUPEMENT");
            typeUoIds = typeUniteOrganisationRepository.findAllTypeUOId(listeTypeUO);
            principal_new = uniteOrganisationRepository.findAgentOptimise(typeUoIds);
            principal_new.forEach(prin -> {
                if (!prin.isEstSiege()) {
                    log.debug("id----" + prin.getId());
                    BigDecimal soldeDistribu = compteRepository.getSoldeTotalDistribution(codeFse, EtiquetteCompteCode.CPTEOPERATIONACTEUR.name(), prin.getId());
                    if (soldeDistribu != null) {
                        totalS[0] = totalS[0].add(soldeDistribu);
                    }
                }
            });

        }
        soldeM.setSolde(totalS[0]);
        return soldeM;
    }

    @Transactional(readOnly = true)
    public SommeSolde getSommeSoldeCommission(Long pereUoId, String codeFse) {
        SommeSolde soldeM = new SommeSolde();
        BigDecimal totalS = new BigDecimal("0");
        SommeSolde soldeP = new SommeSolde();
        List<UniteOrganisation> list = new ArrayList<>();
        totalS = transactionComptePtiRepository.findSoldeCommission(codeFse);
        soldeM.setSolde(totalS);
        return soldeM;
    }

    @Transactional(readOnly = true)
    public SommeSolde getSommeSoldeCommissionAtt(Long pereUoId, String codeFse) {
        BigDecimal totalS = new BigDecimal("0");
        SommeSolde soldeP = new SommeSolde();
        List<UniteOrganisation> list = new ArrayList<>();
        totalS = transactionComptePtiRepository.findSoldeCommissionAtt(codeFse);
        soldeP.setSolde(totalS);
        return soldeP;
    }

    @Transactional(readOnly = true)
    public SommeSolde getSoldeTransfert(Long pereUoId, String codeFse) {

        SommeSolde soldeM = new SommeSolde();
        BigDecimal totalS = new BigDecimal("0");
        totalS = transactionComptePtiRepository.findSoldeTransfertATT(codeFse);
        soldeM.setSolde(totalS);
        return soldeM;
    }

    @Transactional(readOnly = true)
    public SommeSolde getSoldeTaxe(Long pereUoId, String codeFse) {
        SommeSolde soldeM = new SommeSolde();
        BigDecimal totalS = new BigDecimal("0");
        totalS = transactionComptePtiRepository.findSoldeTaxe(codeFse);
        soldeM.setSolde(totalS);
        return soldeM;
    }

    @Transactional(readOnly = true)
    public SommeSolde getSommeSoldePlateForme(Long pereUoId, String codeFse) {
        SommeSolde soldeP = new SommeSolde();
        BigDecimal totalP = new BigDecimal("0");
        totalP = transactionComptePtiRepository.findSoldePlateforme(codeFse);
        soldeP.setSolde(totalP);
        return soldeP;
    }

    /*    public HashMap getAllSolde(Long pereUoId) {
        Map<String, BigDecimal> soldes = new HashMap<>();
        UniteOrganisation uo = new UniteOrganisation();
        String codeFse=null;
        uo = transactionComptePtiRepository.findByUoId(pereUoId);
        if (uo != null) {
            codeFse = uo.getCodeFse();
        }
        SommeSolde soldeD = getSoldeDistribution(pereUoId, "AGENCE",codeFse);
        SommeSolde soldeC = getSommeSoldeClients(pereUoId,codeFse);
        SommeSolde soldeM = getSommeSoldeMarchands(pereUoId, "MARCHAND",codeFse);
        SommeSolde soldeAgent = getSoldeAgent(pereUoId, "MASTER",codeFse);
        //- SommeSolde soldess = getSoldeAgent(pereUoId, "SOUS_DISTRIBUTEUR");
        SommeSolde soldeAgence = getSommeSoldeMarchands(pereUoId, "AGENCE",codeFse);
        SommeSolde soldeCom = getSommeSoldeCommission(pereUoId,codeFse);
        SommeSolde soldeComAtt = getSommeSoldeCommissionAtt(pereUoId,codeFse);
        SommeSolde soldeTransf = getSoldeTransfert(pereUoId,codeFse);
        SommeSolde soldeTaxe = getSoldeTaxe(pereUoId,codeFse);
        SommeSolde soldePlateforme = getSommeSoldePlateForme(pereUoId,codeFse);
        soldes.put("distribution", soldeD.getSolde());
        soldes.put("clients", soldeC.getSolde());
        soldes.put("marchands", soldeM.getSolde());
        soldes.put("master", soldeAgent.getSolde());
        // soldes.put("ssd", soldess.getSolde());
        soldes.put("agence", soldeAgence.getSolde());
        soldes.put("commission", soldeCom.getSolde());
        soldes.put("commissionAtt", soldeComAtt.getSolde());
        soldes.put("transfertAtt", soldeTransf.getSolde());
        soldes.put("taxe", soldeTaxe.getSolde());
        soldes.put("plateforme", soldePlateforme.getSolde());

        return (HashMap) soldes;
    }*/
    public HashMap getAllSolde(Long pereUoId) {
        Map<String, BigDecimal> soldes = new HashMap<>();
        UniteOrganisation uo = new UniteOrganisation();
        String codeFse = null;
        uo = transactionComptePtiRepository.findByUoId(pereUoId);
        if (uo != null) {
            codeFse = uo.getCodeFse();
        }
        SommeSolde soldeD = getSoldeDistribution(pereUoId, "AGENCE", codeFse);
        SommeSolde soldeC = getSommeSoldeClients(pereUoId, codeFse);
        SommeSolde soldeM = getSommeSoldeMarchands(pereUoId, "MARCHAND", codeFse);
        SommeSolde soldeAgent = getSommeSoldeMarchands(pereUoId, "MASTER", codeFse);
        SommeSolde soldeSousdistributeur = getSommeSoldeMarchands(pereUoId, "SOUS_DISTRIBUTEUR", codeFse);
        //- SommeSolde soldess = getSoldeAgent(pereUoId, "SOUS_DISTRIBUTEUR");
        SommeSolde soldeAgence = getSommeSoldeMarchands(pereUoId, "AGENCE", codeFse);
        SommeSolde soldeCom = getSommeSoldeCommission(pereUoId, codeFse);
        SommeSolde soldeComAtt = getSommeSoldeCommissionAtt(pereUoId, codeFse);
        SommeSolde soldeTransf = getSoldeTransfert(pereUoId, codeFse);
        SommeSolde soldeTaxe = getSoldeTaxe(pereUoId, codeFse);
        SommeSolde soldePlateforme = getSommeSoldePlateForme(pereUoId, codeFse);
        soldes.put("distribution", soldeD.getSolde());
        soldes.put("clients", soldeC.getSolde());
        soldes.put("marchands", soldeM.getSolde());
        soldes.put("master", soldeAgent.getSolde().add(soldeSousdistributeur.getSolde()));
        // soldes.put("ssd", soldess.getSolde());
        soldes.put("agence", soldeAgence.getSolde());
        soldes.put("commission", soldeCom.getSolde());
        soldes.put("commissionAtt", soldeComAtt.getSolde());
        soldes.put("transfertAtt", soldeTransf.getSolde());
        soldes.put("taxe", soldeTaxe.getSolde());
        soldes.put("plateforme", soldePlateforme.getSolde());

        return (HashMap) soldes;
    }

    public UniteOrganisation findByCodePv(String codePv) {
        return uniteOrganisationRepository.findByCodePv(codePv);
    }

    public List<UniteOrganisationDTO> findByProfilService(Long profilServiceId, Long pereUoId) {
        Pageable pageable = new Pageable() {
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
        Page<UniteOrganisationDTO> pageUO;
        List<UniteOrganisationDTO> list = new ArrayList<>();
        pageUO = findByPereUoIdOp(null, null, null, null, pereUoId, null, null, null, pageable);
        for (UniteOrganisationDTO uo : pageUO) {
            if (uo.getServiceProfilId().equals(profilServiceId)) {
                list.add(uo);
            }
        }
        return list;
    }

    public InfoUserOfUo getUserOOfUo(Long uniteOrganisationId) {
        InfoUserOfUo infoUserOfUo = new InfoUserOfUo();
        UniteOrganisationDTO uniteOrganisationDTO = uniteOrganisationMapper.toDto(uniteOrganisationRepository.findById(uniteOrganisationId).orElse(null));
        infoUserOfUo.setUniteOrganisationDTO(uniteOrganisationDTO);
        Page<UserDTO> userDTOPage = userService.findAllUserByOrganisation(uniteOrganisationDTO.getId(), null);
        if (userDTOPage.getContent().size() > 0) {
            infoUserOfUo.setUserDTO(userDTOPage.getContent().get(0));
        }
        return infoUserOfUo;
    }

    public List<UniteOrganisationDTO> getUoListId() {
        return null;
    }

    public Boolean verifOrganisationHaveSolde(UniteOrganisation organisation) {
        Boolean isSolde = false;
        List<Compte> ListeCompte = compteRepository.findByUniteOrganisation(organisation);

        for (Compte cpt : ListeCompte) {
            CompteAutre compteAutre = compteAutreRepository.findCompteAutreById(cpt.getId());
            if (compteAutre != null) {
                if (compteAutre.getSolde().compareTo(BigDecimal.ZERO) > 0) {
                    isSolde = true;
                }
            }

        }
        return isSolde;
    }

    public List<UniteOrganisationDTO> findAllOptimiseByTypeUo(String typeUo) {
        Long typeUoId = typeUniteOrganisationRepository.findByLibelleTypeUO(typeUo).getId();
        log.debug("*---------typeUoId---------" + typeUoId);
        return uniteOrganisationRepository.findAllOptimiseByTypeUo(typeUoId);
    }

    @Transactional(readOnly = true)
    public Page<UniteOrganisationDTO> findAllByPereUoIdOptimise(String libelleUniteOrganisation, String codeUO, Integer numeroOrdre, String typeUO, Long pereUoId, Long groupeId, Long villeId, String adresse, Pageable pageable) {
        List<UniteOrganisationDTO> list = new ArrayList<>();

        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalUo = uniteOrganisationRepository.countFindForViewPaginated(null, null, null, null, null, null, null);
        log.debug("Nombre compter*------" + totalUo);
        //  taille = Long.valueOf(totalUo);
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
        list = uniteOrganisationRepository.findAllForViewPaginatedOptimise(null, null, null, null, null, null, null, taille, page);
        //log.debug("Resultat trouver*------" + list);
        List<UniteOrganisationDTO> fils = new ArrayList<UniteOrganisationDTO>();
        List<UniteOrganisationDTO> result = new ArrayList<UniteOrganisationDTO>();

        list.forEach(uniteOrganisation -> {
            if (uniteOrganisation.getPereUOId() != null && uniteOrganisation.getPereUOId().equals(pereUoId)) {
                log.debug("getPereUOId---------------------" + uniteOrganisation.getPereUOId());
                uniteOrganisation.setPereUOLibelleUniteOrganisation(uniteOrganisationRepository.getLibelleUniteOrganisation(uniteOrganisation.getPereUOId()));
                fils.add(uniteOrganisation);
                System.out.println("============== fils ========================= " + fils.size());
                if (((libelleUniteOrganisation == null || libelleUniteOrganisation.equals("")) || (uniteOrganisation.getLibelleUniteOrganisation() != null && uniteOrganisation.getLibelleUniteOrganisation().toUpperCase().contains(libelleUniteOrganisation.toUpperCase())))
                        && ((typeUO == null || typeUO.equals("")) || (uniteOrganisation.getTypeUOCode() != null && uniteOrganisation.getTypeUOCode().equalsIgnoreCase(typeUO)))
                        && ((codeUO == null || codeUO.equals("")) || (uniteOrganisation.getCodeUO() != null && uniteOrganisation.getCodeUO().toUpperCase().contains(codeUO.toUpperCase())))
                        && ((numeroOrdre == null) || (uniteOrganisation.getNumeroOrdre() != null && uniteOrganisation.getNumeroOrdre() == numeroOrdre))
                        && ((groupeId == null) || (uniteOrganisation.getGroupeProfilActeurId() != null && uniteOrganisation.getGroupeProfilActeurId() == groupeId))
                        && ((villeId == null) || (uniteOrganisation.getVilleId() != null && uniteOrganisation.getVilleId() == villeId))
                        && ((adresse == null || adresse.equals("")) || (uniteOrganisation.getAdresse() != null && uniteOrganisation.getAdresse().toUpperCase().contains(adresse.toUpperCase())))) {
                    result.add(uniteOrganisation);
                }
            }
        });

        while (!fils.isEmpty()) {
            List<UniteOrganisationDTO> oldFils = new ArrayList<UniteOrganisationDTO>(fils);
            fils.clear();
            for (UniteOrganisationDTO pere : oldFils) {
                list.forEach(uo -> {
                    if (uo.getPereUOId() != null && uo.getPereUOId().equals(pere.getId())) {
                        // uo.setPereUOLibelleUniteOrganisation(uniteOrganisationRepository.getLibelleUniteOrganisation(uo.getPereUOId()));
                        fils.add(uo);
                        if (((libelleUniteOrganisation == null || libelleUniteOrganisation.equals("")) || (uo.getLibelleUniteOrganisation() != null && uo.getLibelleUniteOrganisation().toUpperCase().contains(libelleUniteOrganisation.toUpperCase())))
                                && ((typeUO == null || typeUO.equals("")) || (uo.getTypeUOCode() != null && uo.getTypeUOCode().equalsIgnoreCase(typeUO)))
                                && ((codeUO == null || codeUO.equals("")) || (uo.getCodeUO() != null && uo.getCodeUO().toUpperCase().contains(codeUO.toUpperCase())))
                                && ((numeroOrdre == null) || (uo.getNumeroOrdre() != null && uo.getNumeroOrdre().equals(numeroOrdre)))
                                && ((groupeId == null) || (uo.getGroupeProfilActeurId() != null && uo.getGroupeProfilActeurId() == groupeId))
                                && ((villeId == null) || (uo.getVilleId() != null && uo.getVilleId().equals(villeId)))
                                && ((adresse == null || adresse.equals("")) || (uo.getAdresse() != null && uo.getAdresse().toUpperCase().contains(adresse.toUpperCase())))) {
                            result.add(uo);
                        }
                    }
                });
            }
        }
        //totalUo = result.size();
        log.debug("Resultat page*------" + page);
        log.debug("Resultat taille*------" + taille);

        Page<UniteOrganisationDTO> retour = new PageImpl<>(result, pageable1, totalUo);
        return retour;
    }

    @Transactional(readOnly = true)
    public Page<UniteOrganisationDTO> findAllOptimise(
            String libelleUniteOrganisation,
            String codeUO,
            Integer numeroOrdre,
            String typeUO,
            Long groupeId,
            Long villeId,
            String adresse,
            Pageable pageable) {
        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalUo = uniteOrganisationRepository.countFindForViewPaginated(libelleUniteOrganisation, codeUO, numeroOrdre, typeUO, groupeId, villeId, adresse);

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
        List<UniteOrganisationDTO> uniteOrganisationDTOS = uniteOrganisationRepository.findAllForViewPaginatedOptimise(libelleUniteOrganisation, codeUO, numeroOrdre, typeUO, groupeId, villeId, adresse, taille, page);
        Page<UniteOrganisationDTO> retour = new PageImpl<>(uniteOrganisationDTOS, pageable1, totalUo);
        return retour;
    }

    /** Enfants directs d'une unité, une page SQL. Ne parcourt pas la descendance. */
    @Transactional(readOnly = true)
    public Page<UniteOrganisationDTO> findChildren(Long pereId, Pageable pageable) {
        int size = Math.max(pageable.getPageSize(), 1);
        long offset = (long) size * Math.max(pageable.getPageNumber(), 0);
        long total = uniteOrganisationRepository.countChildren(pereId);
        List<UniteOrganisationDTO> rows = uniteOrganisationRepository.findChildrenPage(pereId, (long) size, offset);
        return new PageImpl<>(rows, pageable, total);
    }

    public UniteOrganisation getPereUOId(Long uoId) {
        Long pereUoId = null;
        UniteOrganisation uniteOrganisation = new UniteOrganisation();
        if (uoId != null) {
            log.debug("uoId*****" + uoId);
            //UniteOrganisationDTO uniteOrganisationDTO=uniteOrganisationRepository.findDtoById(uoId);
            UniteOrganisation uniteOrganisationDTO = uniteOrganisationRepository.findById(uoId).orElse(null);
            log.debug("review1111" + uniteOrganisationDTO.getPereUO().getId());
            if (uniteOrganisationDTO.getPereUO() != null && !uniteOrganisationDTO.getPereUO().getTypeUO().getCodeTypeUO().equals(TypeUO.FSE.name())) {
                log.debug("TEST entrer");
                getPereUOId(uniteOrganisationDTO.getPereUO().getId());
            }
            uniteOrganisation = uniteOrganisationDTO;
        }
        log.debug("pereUoId,,,," + uniteOrganisation);
        return uniteOrganisation;
    }

    public List<UniteOrganisationDTO> findAllUoFilsOptimise(UniteOrganisationDTO uniteOrganisation, ArrayList<UniteOrganisationDTO> list, ArrayList<UniteOrganisationDTO> listFils) {

        List<UniteOrganisationDTO> uoFils = uniteOrganisationRepository.findByPereUOOptimise(uniteOrganisation.getId());
        if (uoFils.isEmpty()) {
            return listFils;
        } else {
            listFils.addAll(uoFils);
            for (UniteOrganisationDTO uo : uoFils) {
                findAllUoFilsOptimise(uo, list, listFils);
            }
            return listFils;
        }
    }

    public Page<UserDTO> findAllUserByOrganisationOptimise(Pageable pageable, Long uoId, Long profilId, String login, String telephone) {

        User userConnecte = userService.getUserWithAuthorities();
        if (userConnecte != null) {
            UserDTO user = userMapper.userToUserDTO(userConnecte);
            if (user.getAuthorities().contains(AuthoritiesConstants.ADMIN_SYSTEME)) {
                return userService.getAllManagedUsersOptimise(pageable, uoId, profilId, login, telephone);
            }
            UniteOrganisationDTO uo = uniteOrganisationRepository.findDtoById(user.getUniteOrganisationId());
            if (uo != null) {
                return getAllManagedUsersOfUserConnectedOptimise(pageable, uo, profilId, login, telephone);
            } else {
                return new PageImpl<User>(Collections.emptyList()).map(UserDTO::new);
            }
        }
        return new PageImpl<User>(Collections.emptyList()).map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> getAllManagedUsersOfUserConnectedOptimise(Pageable pageable, UniteOrganisationDTO uo, Long profilId, String login, String telephone) {

        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalUser;

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

        List<UniteOrganisationDTO> uniteOrganisationList = new ArrayList<>();
        List<UserDTO> userDTOList = new ArrayList<>();
        List<Long> uoIds = new ArrayList<>();
        ArrayList<UniteOrganisationDTO> listAll = new ArrayList<>();
        ArrayList<UniteOrganisationDTO> listPeres = new ArrayList<>();
        ArrayList<UniteOrganisationDTO> listFils = new ArrayList<>();
        uniteOrganisationList = this.findUoFilsOfAnUOOptimise(uo, listAll, listPeres, listFils, 0);
        uniteOrganisationList.stream().forEach(uniteOrganisation -> {
            uoIds.add(uniteOrganisation.getId());
        });
        System.out.println("========================= UNITE ORGANSATION ID =================== " + uoIds);
        totalUser = userRepository.countUserWithCriteriaOptimise(uoIds, profilId, login, telephone);
        userDTOList = userRepository.findUserWithCriteriaOptimise(uoIds, profilId, login, telephone, taille, page);
        Page<UserDTO> retour = new PageImpl<>(userDTOList, pageable1, totalUser);
        return retour;
    }

    public List<UniteOrganisationDTO> findUoFilsOfAnUOOptimise(UniteOrganisationDTO uniteOrganisation, ArrayList<UniteOrganisationDTO> listAll, ArrayList<UniteOrganisationDTO> listFilsDirect, ArrayList<UniteOrganisationDTO> listFils, Integer i) {

        if (listFilsDirect.isEmpty()) {
            listFilsDirect = (ArrayList<UniteOrganisationDTO>) uniteOrganisationRepository.findByPereUOOptimise(uniteOrganisation.getId());
            listAll.add(uniteOrganisation);
        }

        if (!listFilsDirect.isEmpty()) {
            List<UniteOrganisationDTO> uniteOrganisationDTOS = uniteOrganisationRepository.findByPereUOOptimise(listFilsDirect.get(i).getId());
            if (uniteOrganisationDTOS.size() > 0) {
                listFils.addAll(uniteOrganisationDTOS);
            }
        } else {
            return listAll;
        }

        if (i == (listFilsDirect.size() - 1)) {
            i = 0;
            listAll.addAll(listFilsDirect);
            listAll.addAll(listFils);
            listFilsDirect = listFils;
            listFils.clear();
            if (listFilsDirect.isEmpty()) {
                return listAll;
            } else {
                return findUoFilsOfAnUOOptimise(listFilsDirect.get(i), listAll, listFilsDirect, listFils, 0);
            }
        } else {
            i++;
            return findUoFilsOfAnUOOptimise(listFilsDirect.get(i), listAll, listFilsDirect, listFils, i);
        }
    }

    public UniteOrganisationDTO findOneOptimse(Long id) {
        log.debug("Request to get UniteOrganisation : {}", id);
        return uniteOrganisationRepository.findDtoById(id);
    }

    public UniteOrganisationDTO findPereByTypeOptimise(Long uoId, TypeUO libelleTypeUO) {
        if (uoId != null) {
            UniteOrganisationDTO uniteOrganisation = uniteOrganisationRepository.findDtoById(uoId);
            return this.findUoParentByTypeOptimise(uniteOrganisation, libelleTypeUO);
        }
        return null;
    }

    public UniteOrganisationDTO findUoParentByTypeOptimise(UniteOrganisationDTO uniteOrganisation, TypeUO typeUO) {
        log.debug("uniteeeeeeeeeee" + uniteOrganisation);
        if (uniteOrganisation != null && uniteOrganisation.getTypeUOLibelleTypeUO().equals(String.valueOf(typeUO))) {
            return uniteOrganisation;
        }
        if (uniteOrganisation == null) {
            return null;
        } else {
            UniteOrganisation unitePere = findOneById(uniteOrganisation.getPereUOId());
            return findUoParentByTypeOptimise(uniteOrganisationMapper.toDto(unitePere), typeUO);
        }
    }

    @Transactional(readOnly = true)
    public List<UniteOrganisationDTO> findByTypeUoOptimise(Long typeId) {
        log.debug("Request to get all UniteOrganisations");
        return uniteOrganisationRepository.findByTypeUOAndSupprimeIsFalseOptimise(typeId);
    }

    public Compte createDefaultCompteTagPays(String etiquette, UniteOrganisation uniteOrganisation) {
        //Enregistrement des informations du compteBanque
        CompteBanque compteBanque = new CompteBanque();
        CompteAutre compteAutre = new CompteAutre();
        Compte compte = new Compte();
        Compte cpt = null;
        ParametreApp usebkaccount = parametreAppRepository.findByTypeAndNom(TypeParametre.AUTRE, "USE_DEFAULT_BANK_ACCOUNT");
        EtiquetteCompte eti = etiquetteCompteRepository.findTop1ByCode(etiquette);
        //verif if compte exist
        if (eti.isTransversal()) {
            /**
             * Rechercher l'agence principal si *
             */
            if (uniteOrganisation.getEstSiege() == false) {
                if (uniteOrganisation != null) {
                    UniteOrganisation uoAgencePrincipal = getUoAgenceSiege(uniteOrganisation.getPereUO());
                    cpt = getCompteOfOrganisation(uoAgencePrincipal, EtiquetteCompteCode.valueOf(etiquette));
                    compte.setComptePincipale(cpt);
                }
            }

        }
        UniteOrganisation uo = uniteOrganisationRepository.findById(uniteOrganisation.getPereUO().getId()).orElse(null);
        Long max;
        String code;
        // recherche du fse de l'UO
        UniteOrganisation fseUO = getFSEOfUniteOrganisation(uo);
        if (usebkaccount != null && usebkaccount.getValeur().equals("0")) {
            compteAutre.solde(BigDecimal.ZERO);
            if (fseUO != null) {
                compteAutre.setCodeFse(fseUO.getCodeFse());
            }
            compteAutre = compteAutreRepository.save(compteAutre);
            max = compteRepository.getMaxId();
            code = getString(max);
            if (cpt != null && compte.getComptePincipale() != null) {
                compte.setValeurCompte(cpt.getValeurCompte());
            } else {
                compte.setValeurCompte(code);
            }
            if (!eti.isTransversal() || uniteOrganisation.getEstSiege() == true) {

                compte.setCompteAutre(compteAutre);
            }
            compte.setCategorieCompte(CategorieCompte.AUTRE);
            compte.setFirstCategorieCompte(FirstCategorieCompte.COMPTE_INTERNE);

        } else {//Pour les comptes banques
            compteBanque.codeAgence(" ")
                    .codeBanque(" ")
                    .setRib(" ");
            compteBanque = compteBanqueRepository.save(compteBanque);
            compte.setFirstCategorieCompte(FirstCategorieCompte.COMPTE_EXTERNE);
            compte.compteBanque(compteBanque)
                    .categorieCompte(CategorieCompte.BANQUE);
        }

        if (etiquette.endsWith("PTI")) {//Pour les comptes internes
            compteAutre.solde(BigDecimal.ZERO);
            if (fseUO != null) {
                compteAutre.setCodeFse(fseUO.getCodeFse());
            }

            max = compteRepository.getMaxId();
            code = getString(max);
            compte.setValeurCompte(code);
            if (!eti.isTransversal()) {
                compteAutre = compteAutreRepository.save(compteAutre);
                compte.setCompteAutre(compteAutre);
            }

            compte.setCategorieCompte(CategorieCompte.AUTRE);
            compte.setFirstCategorieCompte(FirstCategorieCompte.COMPTE_INTERNE);
        }

        compte.etiquette(etiquette)
                .uniteOrganisation(uniteOrganisation);
        return compte;

    }

    public void createDefaultCompteTp(String etiquette, UniteOrganisation uniteOrganisation) {

        //Enregistrement des informations du compteBanque
        log.debug("enregistrement du compte de l'UO");
        CompteBanque compteBanque = new CompteBanque();
        CompteAutre compteAutre = new CompteAutre();
        Compte compte = new Compte();
        Compte cpt = null;
        UniteOrganisation fseUO = null;
        ParametreApp usebkaccount = parametreAppRepository.findByTypeAndNom(TypeParametre.AUTRE, "USE_DEFAULT_BANK_ACCOUNT");
        EtiquetteCompte eti = etiquetteCompteRepository.findTop1ByCode(etiquette);
        if (eti.isTransversal()) {
            /**
             * Rechercher l'agence principal si *
             */
            if (uniteOrganisation.getEstSiege() == false) {
                if (uniteOrganisation != null) {
                    UniteOrganisation uoAgencePrincipal = getUoAgenceSiege(uniteOrganisation.getPereUO());
                    cpt = getCompteOfOrganisation(uoAgencePrincipal, EtiquetteCompteCode.valueOf(etiquette));
                    compte.setComptePincipale(cpt);
                }
            }

        }
        if (uniteOrganisation.getPereUO() != null) {
            UniteOrganisation uo = uniteOrganisationRepository.findById(uniteOrganisation.getPereUO().getId()).orElse(null);
            fseUO = getFSEOfUniteOrganisation(uo);
        }
        Long max;
        String code;
        // recherche du fse de l'UO
        if (usebkaccount != null && usebkaccount.getValeur().equals("0")) {
            compteAutre.solde(BigDecimal.ZERO);

            if (fseUO != null) {
                compteAutre.setCodeFse(fseUO.getCodeFse());
            }

            compteAutre = compteAutreRepository.saveAndFlush(compteAutre);

            max = compteRepository.getMaxId();

            code = getString(max);
            if (cpt != null && compte.getComptePincipale() != null) {
                compte.setValeurCompte(cpt.getValeurCompte());
            } else {
                compte.setValeurCompte(code);
            }
            if (!eti.isTransversal() || uniteOrganisation.getEstSiege() == true) {

                compte.setCompteAutre(compteAutre);
            }

            compte.setCategorieCompte(CategorieCompte.AUTRE);
            compte.setFirstCategorieCompte(FirstCategorieCompte.COMPTE_INTERNE);

        } else {//Pour les comptes banques
            compteBanque.codeAgence(" ")
                    .codeBanque(" ")
                    .setRib(" ");
            compteBanque = compteBanqueRepository.save(compteBanque);
            compte.setFirstCategorieCompte(FirstCategorieCompte.COMPTE_EXTERNE);
            compte.compteBanque(compteBanque)
                    .categorieCompte(CategorieCompte.BANQUE);
        }

        if (etiquette.endsWith("PTI")) {//Pour les comptes internes
            compteAutre.solde(BigDecimal.ZERO);
            if (fseUO != null) {
                compteAutre.setCodeFse(fseUO.getCodeFse());
            }

            max = compteRepository.getMaxId();
            code = getString(max);
            compte.setValeurCompte(code);
            if (!eti.isTransversal()) {
                compteAutre = compteAutreRepository.saveAndFlush(compteAutre);
                compte.setCompteAutre(compteAutre);
            }

            compte.setCategorieCompte(CategorieCompte.AUTRE);
            compte.setFirstCategorieCompte(FirstCategorieCompte.COMPTE_INTERNE);
        }

        compte.etiquette(etiquette)
                .uniteOrganisation(uniteOrganisation);
        compteRepository.saveAndFlush(compte);
    }

}
