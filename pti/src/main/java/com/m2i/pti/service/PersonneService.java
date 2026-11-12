package com.m2i.pti.service;

import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.repository.*;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.mapper.*;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;

/**
 * Service Implementation for managing Personne.
 */
@Service
@Transactional
public class PersonneService {

    private final Logger log = LoggerFactory.getLogger(PersonneService.class);

    private final PersonneRepository personneRepository;

    private final PersonneMapper personneMapper;

    private final PieceService pieceService;

    private final PieceMapper pieceMapper;

    private final PersonneMoraleMapper personneMoraleMapper;

    private final PersonnePhysiqueMapper personnePhysiqueMapper;

    private final CompteRepository compteRepository;

    private final PieceRepository pieceRepository;

    private final PersonnePhysiqueService personnePhysiqueService;

    private final PersonneMoraleService personneMoraleService;

    private final TypePieceRepository typePieceRepository;

    private final CompteMapper compteMapper;

    private final LienRepository lienRepository;

    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final ClientRepository clientRepository;

    private final PersonnePhysiqueRepository personnePhysiqueRepository;
    private final PersonneMoraleRepository personneMoraleRepository;

    public PersonneService(PersonneRepository personneRepository, PersonneMapper personneMapper, PieceService pieceService, PieceMapper pieceMapper, PersonneMoraleMapper personneMoraleMapper, PersonnePhysiqueMapper personnePhysiqueMapper, CompteRepository compteRepository, PieceRepository pieceRepository, PersonnePhysiqueService personnePhysiqueService, PersonneMoraleService personneMoraleService, TypePieceRepository typePieceRepository, CompteMapper compteMapper, LienRepository lienRepository, UniteOrganisationRepository uniteOrganisationRepository, ClientRepository clientRepository, PersonnePhysiqueRepository personnePhysiqueRepository, PersonneMoraleRepository personneMoraleRepository) {
        this.personneRepository = personneRepository;
        this.personneMapper = personneMapper;
        this.pieceService = pieceService;
        this.pieceMapper = pieceMapper;
        this.personneMoraleMapper = personneMoraleMapper;
        this.personnePhysiqueMapper = personnePhysiqueMapper;
        this.compteRepository = compteRepository;
        this.pieceRepository = pieceRepository;
        this.personnePhysiqueService = personnePhysiqueService;
        this.personneMoraleService = personneMoraleService;
        this.typePieceRepository = typePieceRepository;
        this.compteMapper = compteMapper;
        this.lienRepository = lienRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.clientRepository = clientRepository;
        this.personnePhysiqueRepository = personnePhysiqueRepository;
        this.personneMoraleRepository = personneMoraleRepository;
    }

    /**
     * Save a personne.
     *
     * @param personneDTO the entity to save
     * @return the persisted entity
     */
    public PersonneDTO save(PersonneDTO personneDTO) {
        log.debug("Request to save Personne : {}", personneDTO);
        Personne personne = personneMapper.toEntity(personneDTO);
        personne = personneRepository.save(personne);
        return personneMapper.toDto(personne);
    }

    /**
     * Get all the personnes.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<PersonneDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Personnes");
        return personneRepository.findAll(pageable)
                .map(personneMapper::toDto);
    }

    /**
     * Recherche d'une personne en fonction { telephone
     * docIdentification(typePiece,referenecePiece) }
     *
     * @param typeId
     * @param reference
     * @param telephone
     * @return
     */
    public PersonneAllDTO findByDocIdentification(Long typeId, String reference, String telephone) {
        List<Piece> pieces = pieceService.findByTypeAndReference(typeId, reference);
        if (!pieces.isEmpty()) {
            PersonneAllDTO personneAllDTO = new PersonneAllDTO();
            PersonneDTO personneDTO = new PersonneDTO();
            personneDTO.setTelephone(telephone);
            personneAllDTO.setPersonneDTO(personneDTO);
            personneAllDTO.setPieceDTO(pieceMapper.toDto(pieces.get(0)));
            for (Piece piece : pieces) {
                Personne titulaire = piece.getTitulaire();
                if (titulaire != null) {

                    personneAllDTO.setPersonneDTO(personneMapper.toDto(titulaire));
                    personneAllDTO.setPieceDTO(pieceMapper.toDto(piece));
                    if (titulaire.getTelephone() != null && titulaire.getTelephone().equals(telephone)) {
                        getInfosSup(titulaire, personneAllDTO);
                        return personneAllDTO;
                    }
                }
            }
            return personneAllDTO;
        }
        return null;
    }

    public List<PersonneAllDTO> findSansParamAll(Long typeId) {

        List<Personne> personnes = personneRepository.findAllPersonneClient();
        List<PersonneAllDTO> allDTOS = new ArrayList<>();

        for (Personne personne : personnes) {
            PersonneAllDTO personneAllDTO = new PersonneAllDTO();
            PersonneDTO personneDTO = personneMapper.toDto(personne);
            personneAllDTO.setPersonneMoraleDTO(getPersonneMorale(personneDTO));
            personneAllDTO.setPersonnePhysiqueDTO(getPersonnePhysique(personneDTO));
            personneAllDTO.setPersonneDTO(personneMapper.toDto(personne));
            allDTOS.add(personneAllDTO);

        }

        return allDTOS;
    }

    /**
     * @param telephone
     * @return
     */
    public List<PersonneAllDTO> findByTelephone(String telephone, Long typeId, String reference, String code) {
        List<Personne> personnes = personneRepository.findByTelephoneAndCodePaysTelephone(telephone, code);
        List<PersonneAllDTO> allDTOS = new ArrayList<>();
        if (telephone != null && telephone != "" && telephone.length() > 0) {
            for (Personne personne : personnes) {
                PersonneAllDTO personneAllDTO = new PersonneAllDTO();
                PersonneDTO personneDTO = personneMapper.toDto(personne);
                personneAllDTO.setPersonneMoraleDTO(getPersonneMorale(personneDTO));
                personneAllDTO.setPersonnePhysiqueDTO(getPersonnePhysique(personneDTO));
                personneAllDTO.setPersonneDTO(personneMapper.toDto(personne));
                if (typeId != null && reference != null) {
                    List<Piece> pieces = pieceRepository.findByTitulaireIdAndTypePieceIdAndReferencePiece(personne.getId(), typeId, reference);
                    personneAllDTO.setPieces(pieceMapper.toDto(pieces));
                    for (Piece piece : pieces) {
                        personneAllDTO.setPieceDTO(pieceMapper.toDto(piece));
                        allDTOS.add(personneAllDTO);
                    }
                } else {
                    /*List<Piece> pieces = pieceRepository.findByTitulaireId(personne.getId());
                    for (Piece piece : pieces) {
                        PersonneAllDTO personneAllDTOCopy = new PersonneAllDTO(personneAllDTO);
                        personneAllDTOCopy.setPieceDTO(pieceMapper.toDto(piece));
                        allDTOS.add(personneAllDTOCopy);
                    }*/
                    allDTOS.add(personneAllDTO);
                }
            }
        } else if (typeId != null && reference != null) {
            List<Piece> pieces = pieceService.findByTypeAndReference(typeId, reference);
            if (!pieces.isEmpty()) {
                for (Piece piece : pieces) {
                    Personne titulaire = piece.getTitulaire();
                    if (titulaire != null) {

                        List<PersonneAllDTO> verif = allDTOS.stream().filter(p -> p.getPersonneDTO().getTelephone().equals(titulaire.getTelephone())).collect(Collectors.toList());
                        if (verif.isEmpty()) {
                            PersonneDTO personneDTO = personneMapper.toDto(titulaire);
                            PersonneAllDTO personneAllDTO = new PersonneAllDTO();
                            personneAllDTO.setPersonneDTO(personneMapper.toDto(titulaire));
                            //personneAllDTO.setPieceDTO(pieceMapper.toDto(piece));
                            personneAllDTO.setPersonneMoraleDTO(getPersonneMorale(personneDTO));
                            personneAllDTO.setPersonnePhysiqueDTO(getPersonnePhysique(personneDTO));
                            allDTOS.add(personneAllDTO);
                        }
                    }
                }
            }
        }
        return allDTOS;
    }

    /* public List<PersonneAllDTO> findByDocIdentification(String telephone, Long typeId, String reference) {
        List<Personne> personnes = personneRepository.findByTelephone(telephone);
        List<PersonneAllDTO> allDTOS = new ArrayList<>();
        for (Personne personne : personnes) {
            PersonneAllDTO personneAllDTO = new PersonneAllDTO();
            PersonneDTO personneDTO = personneMapper.toDto(personne);
            personneAllDTO.setPersonneMoraleDTO(getPersonneMorale(personneDTO));
            personneAllDTO.setPersonnePhysiqueDTO(getPersonnePhysique(personneDTO));
            personneAllDTO.setPersonneDTO(personneMapper.toDto(personne));
            if (typeId != null && reference != null) {
                List<Piece> pieces = pieceRepository.findByTitulaireIdAndTypePieceIdAndReferencePiece(personne.getId(), typeId, reference);
                personneAllDTO.setPieces(pieceMapper.toDto(pieces));
                for (Piece piece : pieces) {
                    personneAllDTO.setPieceDTO(pieceMapper.toDto(piece));
                    allDTOS.add(personneAllDTO);
                }
            } else {
                List<Piece> pieces = pieceRepository.findByTitulaireId(personne.getId());
                for (Piece piece : pieces) {
                    personneAllDTO.setPieceDTO(pieceMapper.toDto(piece));
                    allDTOS.add(personneAllDTO);
                }
            }
        }
        return allDTOS;
    }*/
    public PersonnePhysiqueDTO getPersonnePhysique(PersonneDTO personne) {
        if (personne.getPersonnePhysiqueId() != null) {
            return personnePhysiqueService.findOne(personne.getPersonnePhysiqueId());
        }
        return null;
    }

    public PersonneMoraleDTO getPersonneMorale(PersonneDTO personne) {
        if (personne.getPersonneMoraleId() != null) {
            return personneMoraleService.findOne(personne.getPersonneMoraleId());
        }
        return null;
    }

    public PersonneAllDTO getPersonneAllWithInfoClientCBI(InfoClientCBI infoClientCBI, String codeFse, CustomerInformation info) {
        PersonnePhysique personnePhysique = new PersonnePhysique();
        PersonneMorale personneMorale = new PersonneMorale();
        Piece piece = new Piece();
        Personne personne = new Personne();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        if (infoClientCBI.getTypeClient().equals("PHYSIQUE")) {
            personne.typePersonne(TypePersonne.PHYSIQUE);
             if (infoClientCBI.getDateNaissance()!=null){
              personnePhysique.setDateNaissance(LocalDate.parse(infoClientCBI.getDateNaissance().trim(), formatter));
          }
            personnePhysique.lieuNaissance(infoClientCBI.getLieuNaissance().trim())
                    .nomClient(infoClientCBI.getNom().trim())
                    .prenomClient(infoClientCBI.getPrenom().trim())
                    .sexe(Sexe.valueOf(infoClientCBI.getSexe().trim()));

            if (infoClientCBI.getReference() != null && !infoClientCBI.getReference().trim().equals("")) {
                if (infoClientCBI.getPiece() != null) {
                    Optional<TypePiece> typePiece = typePieceRepository.findByCodeTypePiece(infoClientCBI.getPiece().trim());
                    if (typePiece.isPresent()) {
                        piece.setTypePiece(typePiece.get());
                    }
                }

                if (infoClientCBI.getDateExpire() != null) {
                    DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    piece.dateExpiration(LocalDate.parse(infoClientCBI.getDateExpire().trim(), outputFormat));
                }
                if (infoClientCBI.getDateDelivre() != null) {
                    piece.dateDelivrance(LocalDate.parse(infoClientCBI.getDateDelivre().trim(), formatter));
                }

                piece.lieuDelivrance(infoClientCBI.getLieuDelivre().trim() + " " + infoClientCBI.getAutoriteDelivre().trim())
                        .referencePiece(infoClientCBI.getReference())
                        .referencePiece(infoClientCBI.getReference().trim());
            }

        } else if (infoClientCBI.getTypeClient().equals("MORALE")) {
            personne.typePersonne(TypePersonne.MORALE);
            personneMorale.setRaisonSociale(infoClientCBI.getNom());
        }

        PersonneAllDTO personneAllDTO = new PersonneAllDTO();
        personneAllDTO.setPersonnePhysiqueDTO(personnePhysiqueMapper.toDto(personnePhysique));
        personneAllDTO.setPersonneMoraleDTO(personneMoraleMapper.toDto(personneMorale));
        personneAllDTO.setPieceDTO(pieceMapper.toDto(piece));
        personneAllDTO.setPersonneDTO(personneMapper.toDto(personne));
        UniteOrganisation fse = uniteOrganisationRepository.findByCodeFseAndSupprimeIsFalse(codeFse);

        if (info != null) {
            if (info.getListCompte() != null && info.getListCompte().size() > 0) {
                String typeCompte = info.getListCompte().get(0).getTypeCompte();
                if (typeCompte.equals(CategorieCompte.CARTE.name())) {
                    for (CompteCBI compteCBI : infoClientCBI.getListCompte()) {
                        Carte carte = new Carte();
                        carte.setLast4Digit(info.getListCompte().get(0).getNumeroCompte());
                        carte.setCodeFse(codeFse);
                        carte.setNomOnCard("");
                        Compte compte = new Compte();
                        Client client = new Client();                      
                        compte.setValeurCompte(compteCBI.getNumeroCompte());
                        compte.setCategorieCompte(CategorieCompte.CARTE);
                        compte.setEtiquette(String.valueOf(EtiquetteCompteCode.CPTECLIENT));
                        compte.setFirstCategorieCompte(FirstCategorieCompte.COMPTE_EXTERNE);
                        compte.setClient(client);
                        compteCBI.setCompteDTO(compte);
                        compteCBI.setClientDTO(client);
                        compteCBI.setType("CARTE");
                        personneAllDTO.setCompteCBIDTO(compteCBI);
                    }
                } else if (typeCompte.equals(CategorieCompte.BANQUE.name())) {
                    if (infoClientCBI.getListCompte() != null) {
                        for (CompteCBI compteCBI : infoClientCBI.getListCompte()) {
                            CompteBanque compteBanque = new CompteBanque();
                            if (compteCBI.getCle() != null) {
                                compteBanque.setRib(compteCBI.getCle());
                            }
                            if (compteCBI.getAgence() != null) {
                                compteBanque.setCodeAgence(compteCBI.getAgence());
                            }
                            if (fse != null) {
                                compteBanque.setCodeBanque(fse.getCodeBanque());
                            }
                            compteBanque.setCodeFse(codeFse);
                            Compte compte = new Compte();
                            Client client = new Client();
                            client.codeClient(infoClientCBI.getCodeClient().trim());
                            compte.setValeurCompte(compteCBI.getNumeroCompte());
                            compte.setCategorieCompte(CategorieCompte.BANQUE);
                            compte.setEtiquette(String.valueOf(EtiquetteCompteCode.CPTECLIENT));
                            compte.setFirstCategorieCompte(FirstCategorieCompte.COMPTE_EXTERNE);
                            compte.setClient(client);
                            compte.setCompteBanque(compteBanque);
                            compteCBI.setCompteDTO(compte);
                            compteCBI.setCompteBanqueDTO(compteBanque);
                            compteCBI.setClientDTO(client);
                            compteCBI.setType("Banque");
                            personneAllDTO.setCompteCBIDTO(compteCBI);
                        }

                    }
                }

            }
        }

//        infoClientCBI.getListCompte().forEach(compteCBI -> {
//            
//        });
        return personneAllDTO;
    }

    private void getInfosSup(Personne titulaire, PersonneAllDTO personneAllDTO) {
        if (titulaire.getTypePersonne().equals(TypePersonne.PHYSIQUE)) {
            if (titulaire.getPersonnePhysique() != null) {
                personneAllDTO.setPersonnePhysiqueDTO(personnePhysiqueMapper.toDto(titulaire.getPersonnePhysique()));
            }
        } else if (titulaire.getPersonneMorale() != null) {
            personneAllDTO.setPersonneMoraleDTO(personneMoraleMapper.toDto(titulaire.getPersonneMorale()));
        }
    }

    /**
     * Get one personne by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public PersonneDTO findOne(Long id) {
        log.debug("Request to get Personne : {}", id);
        Personne personne = personneRepository.findById(id).orElse(null);
        return personneMapper.toDto(personne);
    }

    @Transactional(readOnly = true)
    public PersonneAllDTO finAllInfoOfPersonne(Long id) {
        log.debug("Request to get Personne : {}", id);
        if (id != null) {
            PersonneAllDTO personneAllDTO = new PersonneAllDTO();
            Personne personne = personneRepository.findById(id).orElse(null);
            personneAllDTO.setPersonneDTO(personneMapper.toDto(personne));
            /*List<Piece> allPieces = pieceRepository.findByTitulaireId(personne.getId());
            Optional<Piece> pieceExist = allPieces.stream().findFirst();
            if (pieceExist.isPresent()) {
                Piece piece = pieceExist.get();
                personneAllDTO.setPieceDTO(pieceMapper.toDto(piece));
            }*/
            getInfosSup(personne, personneAllDTO);
            return personneAllDTO;
        } else {
            throw new CustomParameterizedException("Erreur de connexion!");
        }
    }

    /**
     * Delete the personne by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Personne : {}", id);
        personneRepository.deleteById(id);
    }

    public boolean isActived(String telephone, String code) {
        log.debug("Verif etat %%" + telephone + " " + code);
        return personneRepository.findByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(telephone, code).isPresent();
    }

    public List<Lien> saveDoublon(DoublonDTO doublonDTO) {
        log.debug("View liste ----" + doublonDTO);
        /* PersonneDTO maxPersonneId=personneDTOList.stream()
                .min(Comparator.comparing(PersonneDTO::getId))
                .orElseThrow(NoSuchElementException::new);*/
        Long maxPersonneId = doublonDTO.getListePersonneId().stream()
                .min(Comparator.comparing(Long::longValue)).orElseThrow(NoSuchElementException::new);
        List<PersonneDTO> result = new ArrayList();
        List<Lien> listlien = new ArrayList<>();
        for (Long ids : doublonDTO.getListePersonneId()) {
            PersonneDTO personneDTO = personneMapper.toDto(personneRepository.getOne(ids));
            result.add(personneDTO);
        }
        Personne maxP = personneRepository.getOne(maxPersonneId);

        if (result.size() > 0) {
            for (PersonneDTO p : result) {
                Personne pt = personneMapper.toEntity(p);
                if (!maxPersonneId.equals(p.getId())) {
                    List<Lien> lienVerify = lienRepository.findAllByPersonneIdAndLienPotentiel(pt.getId(), maxP.getId());
                    if (lienVerify.size() < 1) {
                        Lien lien = new Lien();
                        lien.setPersonne(pt);
                        if (maxP.getId() != null) {
                            lien.setLienPotentiel(maxP.getId());
                        }
                        lien.setValider(false);
                        listlien.add(lien);
                    }
                }

            }
            listlien = lienRepository.saveAll(listlien);
            log.debug("\n\n\n\n\n----Start------");
            log.debug("---------- Taille " + listlien.size());
            log.debug("----lien save : {}", lienRepository.saveAll(listlien));
            log.debug("\n\n\n\n\n----Finish------");
            return listlien;
        } else {
            return null;
        }

    }
    public Page<PersonneDTO> getDoublonNew(Boolean hash, Long typePieceId, Pageable pageable) {
        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalPersonne = 0;
        List<PersonneDTO> personnePage=null;

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
        TypePiece typePiece = new TypePiece();
        totalPersonne =personneRepository.countPersonne();
        if (typePieceId != null) {
            typePiece = typePieceRepository.findById(typePieceId).orElse(null);
        }
        if (Boolean.TRUE.equals(hash) && typePieceId == null) {
            personnePage=personneRepository.findDoublonByHash(taille,page);
            Page<PersonneDTO> retour = new PageImpl<>(personnePage, pageable1, totalPersonne);
            return retour;
        }

        if (Boolean.FALSE.equals(hash) && typePieceId != null) {
            personnePage= personneRepository.findDoublonByTypePiece(typePiece.getId(),taille,page);
            Page<PersonneDTO> retour = new PageImpl<>(personnePage, pageable1, totalPersonne);
            return retour;
        }

        if (Boolean.TRUE.equals(hash) && typePieceId != null) {
            personnePage=personneRepository.findDoublonByTypePieceAndHash(typePiece.getId(),taille,page);
            Page<PersonneDTO> retour = new PageImpl<>(personnePage, pageable1, totalPersonne);
            return retour;
        }

        return null;
    }

    public List<PersonneDTO> getDoublon(Boolean hash, Long typePieceId) {
        TypePiece typePiece = new TypePiece();
        Lien lien = new Lien();
        List<Personne> personneList = new ArrayList<>();
        List<PersonneDTO> listP = new ArrayList<>();
        PersonneDTO personneDTO = new PersonneDTO();
        PersonneDatas personneDatas = new PersonneDatas();
        if (typePieceId != null) {
            typePiece = typePieceRepository.findById(typePieceId).orElse(null);
        }
        List<PersonneDTO> list = personneRepository.findAllOptimiser(null);
        List<LienDTO> listeLien = new ArrayList<>();
        LienDTO pLien = new LienDTO();
        for (PersonneDTO l : list) {
            System.out.println("-----Personne entrer------------" + l.getId());
            if (!hash && typePieceId != null) {
                System.out.println("---Type piece------" + typePieceId);
                Piece personnePiece = pieceRepository.findTopByTitulaireId(l.getId());
                if (personnePiece != null && typePiece != null) {
                    System.out.println("---Type t------" + typePiece.toString());
                    System.out.println("--Personne-Piece------" + personnePiece.toString());
                    List<PersonneDTO> resultP = personneRepository.findAllByTpePieceAndRefPiece(typePiece.getCodeTypePiece(), personnePiece.getReferencePiece(), null);
                    //Integer nbr = personneRepository.countByPiece(typePiece.getCodeTypePiece(), personnePiece.getReferencePiece());
                    if (resultP.size() > 1) {
                        System.out.println("-----DOublon show------------" + resultP.size());
                        Integer lienVerify;
                        for (PersonneDTO p : resultP) {
                            if (!l.getId().equals(p.getId())) {
                                System.out.println("----------" + l.getId());
                                System.out.println("---YES ---");
                                System.out.println("----------" + p.getId());
                                personneDTO = p;
                                personneDTO.setPersonneLien(l.getId());
                                personneDTO.setTypePiece(typePiece.getLibelleTypePiece());
                                personneDTO.setRefPiece(personnePiece.getReferencePiece());
                                listP.add(personneDTO);
                                personneDTO = new PersonneDTO();

                            }
                        }

                    }
                }
            }
            if (hash && typePieceId == null) {
                System.out.println("---Hash code------");
                if (l.getKeyHash() != null) {
                    List<PersonneDTO> resultP = personneRepository.findAllOptimiser(l.getKeyHash());
                    if (resultP.size() > 1) {
                        //personneList.add(l);
                        for (PersonneDTO p : resultP) {
                            if (!l.getId().equals(p.getId())) {
                                System.out.println("----------" + l.getId());
                                System.out.println("---YES ---");
                                System.out.println("----------" + p.getId());
                                personneDTO = p;
                                personneDTO.setPersonneLien(l.getId());
                                personneDTO.setTypePiece(typePiece.getLibelleTypePiece());
                                listP.add(personneDTO);
                                personneDTO = new PersonneDTO();

                            }
                        }
                    }
                }
            }
            if (hash && typePieceId != null) {
                System.out.println("---Piece--et Hash----");
                System.out.println("---Id------" + typePieceId);
                Piece personnePiece = pieceRepository.findTopByTitulaireId(l.getId());
                if (personnePiece != null && typePiece != null && l.getKeyHash() != null) {
                    System.out.println("---Type t------" + typePiece.toString());
                    System.out.println("--Personne-Piece------" + personnePiece.toString());
                    List<PersonneDTO> resultP = personneRepository.findAllByTpePieceAndRefPiece(typePiece.getCodeTypePiece(), personnePiece.getReferencePiece(), l.getKeyHash());
                    if (resultP.size() > 1) {
                        //personneList.add(l);
                        for (PersonneDTO p : resultP) {
                            if (!l.getId().equals(p.getId())) {
                                System.out.println("----------" + l.getId());
                                System.out.println("---YES ---");
                                System.out.println("----------" + p.getId());

                                personneDTO = p;
                                personneDTO.setPersonneLien(l.getId());
                                personneDTO.setTypePiece(typePiece.getLibelleTypePiece());
                                personneDTO.setRefPiece(personnePiece.getReferencePiece());
                                listP.add(personneDTO);
                                personneDTO = new PersonneDTO();

                            }
                        }
                    }
                }

            }
        }

        if (listP.size() > 0) {
            return listP;
        }
        return null;
    }

    public PersonneDTO getInfosLien(Long lienId) {
        Personne personne = personneRepository.getOne(lienId);
        PersonneDTO personneDTO = new PersonneDTO();
        if (personne != null) {
            personneDTO = personneMapper.toDto(personne);
            personneDTO.setRefPiece("");
            personneDTO.setRefPiece("");
        }
        return personneDTO;
    }

    public Optional<Personne> findPersonneByPhoneAndCodePaysActive(String telephone, String codeTelephone) {

        log.debug("Recherche dans personne telephone : {}", codeTelephone, "telephone ", telephone);
        PersonneDTO searchPersonne = personneRepository.findPersonneByPhoneAndCodeIndicatifActivated(telephone, codeTelephone);
        if (searchPersonne != null) {
            return Optional.of(personneMapper.toEntity(searchPersonne));
        } else {
            return Optional.empty();
        }

    }

    public Optional<Personne> findPersonneByPhoneAndCodePays(String telephone, String codeTelephone) {

        log.debug("Recherche dans personne telephone : {}", codeTelephone);
        log.debug("telephone : {}", telephone);
        PersonneDTO searchPersonne = personneRepository.findPersonneByPhoneAndCodeIndicatif(telephone, codeTelephone);
        if (searchPersonne != null) {
            return Optional.of(personneMapper.toEntity(searchPersonne));
        } else {
            return Optional.empty();
        }

    }

    @Transactional(readOnly = true)
    public PersonneAllDTO finAllInfoOfPersonneOptimise(Long id) {
        log.debug("Request to get Personne : {}", id);
        if (id != null) {
            PersonneAllDTO personneAllDTO = new PersonneAllDTO();
            PersonneDTO personne = personneRepository.findPersonneById(id);
            personneAllDTO.setPersonneDTO(personne);
            getInfosSupOptimise(personne, personneAllDTO);
            return personneAllDTO;
        } else {
            throw new CustomParameterizedException("Erreur de connexion!");
        }
    }

    private void getInfosSupOptimise(PersonneDTO titulaire, PersonneAllDTO personneAllDTO) {
        if (titulaire.getTypePersonne().equals(TypePersonne.PHYSIQUE)) {
            if (titulaire.getPersonnePhysiqueId() != null) {
                personneAllDTO.setPersonnePhysiqueDTO(personnePhysiqueRepository.findByIdReport(titulaire.getPersonnePhysiqueId()));
            }
        } else if (titulaire.getPersonneMoraleId() != null) {
            personneAllDTO.setPersonneMoraleDTO(personneMoraleMapper.toDto(personneMoraleRepository.findById(titulaire.getPersonneMoraleId()).orElse(null)));
        }
    }
    public List<Personne> getDoublouByPieceorHash(PersonneDTO personneDTO){
        List<Personne> list=null;
        if(personneDTO.getRefPiece()!=null){
            list=personneRepository.findPersonneByPiece(personneDTO.getRefPiece());
        } else if(personneDTO.getKeyHash()!=null) {
            list=personneRepository.countPersonneByHashKey(personneDTO.getKeyHash());
        } else if (personneDTO.getKeyHash()!=null && personneDTO.getRefPiece()!=null) {
            list=personneRepository.countPersonneByHashKey(personneDTO.getKeyHash());
        }
        return list;
    }

}
