package com.m2i.pti.service;

import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.reporting.dto.ListeClientDTO;
import com.m2i.pti.repository.*;
import com.m2i.pti.security.Chiffrement;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.mapper.*;
import com.m2i.pti.service.minio.MinioService;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.m2i.pti.security.SecurityUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import com.m2i.pti.service.kafka.MessageProducer;

/**
 * Service Implementation for managing Client.
 */
@Service
@Transactional
public class ClientService {

    private final Logger log = LoggerFactory.getLogger(ClientService.class);

    @Value("${m2i.security.pti.images.windows}")
    private String imagesPathWindows;

    @Value("${m2i.security.pti.images.linux}")
    private String imagesPathLinux;

    private final ClientRepository clientRepository;

    private final ClientMapper clientMapper;

    private final PersonneMoraleMapper personneMoraleMapper;

    private final PersonnePhysiqueMapper personnePhysiqueMapper;

    private final ChargementTransfertMapper chargementTransfertMapper;

    private final PersonneRepository personneRepository;

    private final PersonneMoraleRepository personneMoraleRepository;

    private final PersonnePhysiqueRepository personnePhysiqueRepository;

    private final PieceMapper pieceMapper;

    private final PieceRepository pieceRepository;

    private final PersonneMapper personneMapper;

    private final CompteRepository compteRepository;

    private final CompteBanqueRepository compteBanqueRepository;

    private final CompteAutreRepository compteAutreRepository;

    private final CompteMapper compteMapper;

    private final CompteBanqueMapper compteBanqueMapper;

    private final DocumentMapper documentMapper;

    private CompteAutreMapper compteAutreMapper;

    private final AutorisationRepository autorisationRepository;

    private final AutorisationService autorisationService;

    private final TypeServiceService typeServiceService;

    private final ServiceProfilRepository serviceProfilRepository;

    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final NationaliteService nationaliteService;

    private final DocumentService documentService;

    private final ParametreAppService parametreAppService;

    private final ApiService apiService;
    private final DocumentRepository documentRepository;
    private final ParametreAppRepository parametreAppRepository;
    private final ProfilActeurRepository profilActeurRepository;
    private final GroupeProfilActeurRepository groupeProfilActeurRepository;
    private final UserControlService userControlService;
    private final UserControlRepository userControlRepository;
    private final OperationRepository operationRepository;
    MessageProducer messageProducer;
    private final LocaliteRepository localiteRepository;
    private final CarteMapper carteMapper;
    private final ChargementTransfertRepository chargementTransfertRepository;

    private final TransactionComptePtiRepository transactionComptePtiRepository;
    private final CompteService compteService;
    private final CarteRepository carteRepository;
    private final MinioService minioService;
    private final InfosuppRepository infosuppRepository;

    public ClientService(CarteMapper carteMapper, CarteRepository carteRepository, CompteService compteService, MessageProducer messageProducer, ClientRepository clientRepository, ClientMapper clientMapper, PersonneMoraleMapper personneMoraleMapper, PersonnePhysiqueMapper personnePhysiqueMapper, ChargementTransfertMapper chargementTransfertMapper, PersonneRepository personneRepository, PersonneMoraleRepository personneMoraleRepository, PersonnePhysiqueRepository personnePhysiqueRepository, PieceMapper pieceMapper, PieceRepository pieceRepository, PersonneMapper personneMapper, CompteRepository compteRepository, CompteBanqueRepository compteBanqueRepository, CompteAutreRepository compteAutreRepository, CompteMapper compteMapper, CompteBanqueMapper compteBanqueMapper, DocumentMapper documentMapper, CompteAutreMapper compteAutreMapper, AutorisationRepository autorisationRepository, AutorisationService autorisationService, TypeServiceService typeServiceService, ServiceProfilRepository serviceProfilRepository, UniteOrganisationRepository uniteOrganisationRepository, NationaliteService nationaliteService, DocumentService documentService, ParametreAppService parametreAppService, ApiService apiService, DocumentRepository documentRepository, ParametreAppRepository parametreAppRepository, ProfilActeurRepository profilActeurRepository, GroupeProfilActeurRepository groupeProfilActeurRepository, UserControlService userControlService, UserControlRepository userControlRepository, OperationRepository operationRepository, LocaliteRepository localiteRepository, ChargementTransfertRepository chargementTransfertRepository, TransactionComptePtiRepository transactionComptePtiRepository, MinioService minioService,InfosuppRepository infosuppRepository) {
        this.carteMapper = carteMapper;
        this.clientRepository = clientRepository;
        this.carteRepository = carteRepository;
        this.clientMapper = clientMapper;
        this.personneMoraleMapper = personneMoraleMapper;
        this.personnePhysiqueMapper = personnePhysiqueMapper;
        this.chargementTransfertMapper = chargementTransfertMapper;
        this.personneRepository = personneRepository;
        this.personneMoraleRepository = personneMoraleRepository;
        this.personnePhysiqueRepository = personnePhysiqueRepository;
        this.pieceMapper = pieceMapper;
        this.pieceRepository = pieceRepository;
        this.personneMapper = personneMapper;
        this.compteRepository = compteRepository;
        this.compteBanqueRepository = compteBanqueRepository;
        this.compteAutreRepository = compteAutreRepository;
        this.compteMapper = compteMapper;
        this.compteBanqueMapper = compteBanqueMapper;
        this.documentMapper = documentMapper;
        this.compteAutreMapper = compteAutreMapper;
        this.autorisationRepository = autorisationRepository;
        this.autorisationService = autorisationService;
        this.typeServiceService = typeServiceService;
        this.serviceProfilRepository = serviceProfilRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.nationaliteService = nationaliteService;
        this.documentService = documentService;
        this.parametreAppService = parametreAppService;
        this.apiService = apiService;
        this.documentRepository = documentRepository;
        this.parametreAppRepository = parametreAppRepository;
        this.profilActeurRepository = profilActeurRepository;
        this.groupeProfilActeurRepository = groupeProfilActeurRepository;
        this.userControlService = userControlService;
        this.userControlRepository = userControlRepository;
        this.operationRepository = operationRepository;
        this.localiteRepository = localiteRepository;
        this.chargementTransfertRepository = chargementTransfertRepository;
        this.transactionComptePtiRepository = transactionComptePtiRepository;
        this.messageProducer = messageProducer;
        this.compteService = compteService;
        this.minioService = minioService;
        this.infosuppRepository= infosuppRepository;
    }

    /**
     * Save a client.
     *
     * @param clientDTO the entity to save
     * @return the persisted entity
     */
    public ClientDTO save(ClientDTO clientDTO) {
        log.info("Request to save Client : {}", clientDTO);
        Client client = clientMapper.toEntity(clientDTO);
        client = clientRepository.save(client);
        return clientMapper.toDto(client);
    }

    public void saveEventAudit(String entityId, String objet, String action) {
        ActionEventDTO actionEvt = new ActionEventDTO(entityId, SecurityUtils.getCurrentUserLogin(), objet, action);
        messageProducer.sendMessageActionEvent(actionEvt);
    }

    public ClientDTO changeStatut(Long id) {
        Client client = clientRepository.findById(id).orElse(null);
        if (client != null) {
            client.setClientValide(!client.getClientValide());
            String action = client.getClientValide() == true ? "CLIENT_DESACTIVE" : "CLIENT_ACTIVE";
            client = clientRepository.save(client);
            saveEventAudit(id.toString(), "CLIENT", action);
            //Suppression des anciennes entree dans userControl
            UserControl userControl = new UserControl();
            if (!client.getClientValide()) {
                userControl.setAction(TypeControl.DISABLE);
            } else {
                userControl.setAction(TypeControl.ENABLED);
            }
            if (client.getPersonne() != null && client.getPersonne().getTypePersonne().equals(TypePersonne.PHYSIQUE)) {
                userControl.setLogin(client.getPersonne().getTelephone());
                userControl.setCodePays(client.getPersonne().getCodePaysTelephone());
                userControl.setTypeActeur("CLIENT");
            } else if (client.getPersonne() != null) {
                userControl.setLogin(client.getPersonne().getEmail());
                userControl.setCodePays(client.getPersonne().getCodePaysTelephone());
                userControl.setTypeActeur("ENTREPRISE");
            }
            List<UserControl> olds = userControlRepository.findAllByLoginAndCodePaysAndSupprimeFalse(userControl.getLogin(), userControl.getCodePays());
            if (olds != null) {
                olds.forEach(old -> {
                    if (!old.getSupprime() && old.getAction().equals(TypeControl.DISABLE)) {
                        old.setSupprime(true);
                        userControlRepository.save(old);
                    }

                });
            }
            //Ajout d'une entrée dans userControl
            userControlRepository.save(userControl);
            /* if (!client.getClientValide()) {
                userControlRepository.save(userControl);
            }*/
            return clientMapper.toDto(client);
        } else {
            return null;
        }
    }

    public Client setGroupe(GroupeProfilActeur groupeProfilActeur, Client client) {
        if (groupeProfilActeur != null && groupeProfilActeur.getType().equals(TypeProfilActeur.CLIENT)) {
            client.setGroupeProfilActeur(groupeProfilActeur);
            List<ProfilActeur> profilActeurs = profilActeurRepository.findAllProfilsByGroupe(groupeProfilActeur.getId());
            if (profilActeurs != null && profilActeurs.size() > 0) {
                profilActeurs.forEach(profilActeur -> {
                    if (profilActeur != null) {
                        if (profilActeur.getDestineA() != null && profilActeur.getDestineA().equals(DestinationProfilActeur.CLIENT_IDENTIFIE)) {
                            if (client.getClientIdentifie() != null) {
                                if (client.getClientIdentifie()) {
                                    client.setProfilActeur(profilActeur);
                                }
                            }

                        } else if (profilActeur.getDestineA() != null && profilActeur.getDestineA().equals(DestinationProfilActeur.CLIENT_NON_IDENTIFIE)) {
                            if (client.getClientIdentifie() == null || !client.getClientIdentifie()) {
                                client.setProfilActeur(profilActeur);
                            }
                        }
                    }
                });
            }

        }
        return client;
    }

    public ClientDTO identifier(Long id) {
        Client client = clientRepository.findById(id).orElse(null);
        if (client != null) {
            //String action = client.getClientValide() == true ? "CLIENT_DESACTIVE" : "CLIENT_ACTIVE";
            String action = ActionEnum.CLIENT_IDENTIFIE.name();
            ActionEventDTO actionEvt = new ActionEventDTO(client.getId().toString(), SecurityUtils.getCurrentUserLogin(), "CLIENT", action);
            messageProducer.sendMessageActionEvent(actionEvt);

            client.setClientIdentifie(true);
            client = setGroupe(client.getGroupeProfilActeur(), client);
            client.setDateValidation(LocalDate.now());
            String currentLogin = SecurityUtils.getCurrentUserLogin();

            client.setUserValide(currentLogin);
            client = clientRepository.save(client);
            return clientMapper.toDto(client);
        } else {
            return null;
        }
    }

    public ClientDTO setProfileActeur(Long id, Long profileId) {
        Client client = clientRepository.findById(id).orElse(null);
        if (client != null) {
            client.setProfilActeur(profilActeurRepository.findById(profileId).orElse(null));
            client = clientRepository.save(client);
            return clientMapper.toDto(client);
        } else {
            return null;
        }
    }

    /**
     * Service permettant la souscription d'un client
     *
     * @param clientAllDTO
     * @return
     */
    @Transactional
    public ClientDTO saveInfosClient(ClientAllDTO clientAllDTO) {
        log.info("Souscription d'un client ");
        //log.debug("localite-----------"+clientAllDTO.getLocalite());
        Personne personne = personneMapper.toEntity(clientAllDTO.getPersonneDTO());
        UniteOrganisation reseau=null;
        InfoSupp info=null;
        //si le client est nouveau
        if (personne.getId() == null) {
            personne.setActivated(true);
        }
        if (personne != null && personne.getTypePersonne().equals(TypePersonne.PHYSIQUE)) {
            personne.setKeyHash(getHash(clientAllDTO.getPersonnePhysiqueDTO()));
        }
        Piece piece = pieceMapper.toEntity(clientAllDTO.getPieceDTO());
        PersonnePhysique personnePhysique = personnePhysiqueMapper.toEntity(clientAllDTO.getPersonnePhysiqueDTO());
        PersonneMorale personneMorale = personneMoraleMapper.toEntity(clientAllDTO.getPersonneMoraleDTO());
        Client client = clientMapper.toEntity(clientAllDTO.getClientDTO());
//nouveau
        if (personne != null && personne.getId() == null & personneRepository.findPersonneByPhoneAndCodePays(personne.getTelephone(), personne.getCodePaysTelephone()).isPresent()) {
            personne=checkExisteClient(personne,personne.getTelephone(),personne.getCodePaysTelephone());
        }

        log.info("Definir le profil du client en cours");
        Long profilId = null;
        ServiceProfil serviceProfil = new ServiceProfil();
        GroupeProfilActeur groupeProfilActeur = null;
        if (clientAllDTO.getClientDTO() != null) {
            profilId = clientAllDTO.getClientDTO().getProfilId();
            //  profilacteur=clientAllDTO.getClientDTO().getProfilActeurId();
            if (clientAllDTO.getClientDTO().getProfilId() != null) {
                serviceProfil = serviceProfilRepository.findById(profilId).orElse(null);
                //    profileAct = profilActeurRepository.findById(profilacteur).orElse(null); // definition du profil
                if (serviceProfil != null) {
                    clientAllDTO.setTypeServiceIds(serviceProfil.getTypeServices().stream().map(typeService -> typeService.getId()).collect(Collectors.toList()));
                }
            }
            if (clientAllDTO.getClientDTO().getGroupeProfilActeurId() != null) {
                groupeProfilActeur = groupeProfilActeurRepository.findById(clientAllDTO.getClientDTO().getGroupeProfilActeurId()).orElse(null);
            } else {
                Optional<GroupeProfilActeur> defaultGroup = groupeProfilActeurRepository.findByTypeAndActiveTrue(TypeProfilActeur.CLIENT);
                if (defaultGroup.isPresent()) {
                    groupeProfilActeur = defaultGroup.get();
                }
            }
            if(clientAllDTO.getClientDTO().getReseauId()!=null){
                reseau=uniteOrganisationRepository.findById(clientAllDTO.getClientDTO().getReseauId()).orElse(null);
            }
            if(clientAllDTO.getClientDTO().getInfosuppId()!=null){
                info=infosuppRepository.findById(clientAllDTO.getClientDTO().getInfosuppId()).orElse(null);
            }
        }
       

        Boolean find = false;
        String telephone = personne.getTelephone();
        String codeTelephone = personne.getCodePaysTelephone();
        /* Save Localite  */
        Localite localite = clientAllDTO.getLocalite();
        if (localite != null) {
            localite = localiteRepository.save(localite);
            if (localite.getId() != null) {
                personne.setLocalite(localite);
            }
        }

        if (personne.getId() != null) {//Personne existe deja dans la base de donnée
            log.info("Informations existant .%...");
            PersonnePhysique personnePhysiqueNew;
           Personne   personneExistDeja=null;
            Optional<Personne>   personneExist = personneRepository.findPersonneByPhoneAndCodePays(personne.getTelephone(), personne.getCodePaysTelephone());
            if (personneExist.isPresent()) {
                if (personneExist.get().isModifiable() == true) {
                    personnePhysiqueNew = saveExternePersonnePhysique(personne, personneExist.get());
                    personneExist.get().setModifiable(false);
                    personneExistDeja = enregistrePersonne(personne, personneExist.get(), personnePhysiqueNew);
                }else {
                  personneExistDeja=personneExist.get();
                }
            }
            //personne = personneRepository.save(personneExist);
            if (personne.getTypePersonne().equals(TypePersonne.MORALE)) {
                PersonneMorale pm=personneMoraleRepository.save(personneMorale);
                personne.setPersonneMorale(pm);
                personneRepository.save(personne);
            } else {
                PersonnePhysique py=personnePhysiqueRepository.save(personnePhysique);
                personne.setPersonnePhysique(py);
                personneRepository.save(personne);
            }
            /*if (personne.getTypePersonne().equals(TypePersonne.PHYSIQUE)) {
                personnePhysiqueRepository.save(personnePhysique);
            } else {
                personneMoraleRepository.save(personneMorale);
            }*/

            client = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personne.getId());
            if (client != null) {
                client.setClientValide(Boolean.TRUE);
                client.setProfil(serviceProfil);
                client = setGroupe(groupeProfilActeur, client);
                personne = personneRepository.save(personne);
                client.setPersonne(personne);
                client.setReseau(reseau);
                client.setInfoSupp(info);
                client = clientRepository.save(client);
                personneRepository.getOne(client.getPersonne().getId());
                autorisationService.saveAutoriastions(client, clientAllDTO.getTypeServiceIds());
            } else {
                Client clientNew = new Client();
                clientNew.setPersonne(personneExistDeja);
                clientNew.setClientValide(Boolean.TRUE);
                clientNew.setProfil(serviceProfil);
                clientNew.setDalaisRegularisation(LocalDate.now().plusMonths(3));
                clientNew = setGroupe(groupeProfilActeur, clientNew);
                clientNew.setReseau(reseau);
                client.setInfoSupp(info);
                clientNew = clientRepository.save(clientNew);
                autorisationService.saveAutoriastions(clientNew, clientAllDTO.getTypeServiceIds());
                client = clientNew;
            }
        } else {
            log.info("telephone verif .%...");

            /* if (telephone != null) {
                log.info("telephone existant .%...");

                Personne searchPersonne = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(telephone, codeTelephone);
                if (searchPersonne!=null) {
                    //Personne p = searchPersonne;
                    PersonnePhysique physique = searchPersonne.getPersonnePhysique();
                    PersonneMorale morale = searchPersonne.getPersonneMorale();
                    personne.setId(searchPersonne.getId());
                    if (morale != null)
                        personneMorale.setId(morale.getId());
                    else personnePhysique.setId(physique.getId());
                    find = true;
                    client = clientRepository.findByPersonneIdAndClientValideTrueAndSupprimeIsFalse(searchPersonne.getId());
                    if (client != null) {
                        log.info("nouveao client .%...");
                        client.setCodeClient(RandomUtil.generateAlphaNumeric(6));
                        client.setPersonne(personne);
                        client.setClientValide(Boolean.TRUE);
                        client = setGroupe(groupeProfilActeur, client);
                        client = clientRepository.save(client);
                        autorisationService.saveAutoriastions(client, clientAllDTO.getTypeServiceIds());
                    }
                }
            }*/
            //Personne,PersonnePhysique,PersonneMorale,Client,Compte
            if (personne.getTypePersonne().equals(TypePersonne.PHYSIQUE)) { //PersonnePhysiqye
                log.debug("PERSONNE PHYSIQUE%% ........................");
                log.info("PERSONNE PHYSIQUE%% ........................");
                personnePhysique = personnePhysiqueRepository.save(personnePhysique);
                personne.setPersonnePhysique(personnePhysique);
                personne = personneRepository.save(personne);
                client.setCodeClient(RandomStringUtils.randomNumeric(10) + RandomUtil.generateAlphaNumeric(2).toUpperCase());
                client.setPersonne(personne);
                client.setClientValide(Boolean.TRUE);
                client = setGroupe(groupeProfilActeur, client);
                client.setDalaisRegularisation(LocalDate.now().plusMonths(3));
                client = clientRepository.save(client);
                autorisationService.saveAutoriastions(client, clientAllDTO.getTypeServiceIds());
                saveInfosPieces(personne, piece);
            } else { //PersonneMorale
                log.debug("PERSONNE MORALE%%..............................");
                personneMorale = personneMoraleRepository.save(personneMorale);
                personne.setPersonneMorale(personneMorale);
                personne = personneRepository.save(personne);
                saveInfosPieces(personne, piece);
                client.setPersonne(personne);
                client.setClientValide(Boolean.TRUE);
                client = setGroupe(groupeProfilActeur, client);
                client.setDalaisRegularisation(LocalDate.now().plusMonths(3));
                client = clientRepository.save(client);
            }
        }

        /* Si compte à créer**/
        if (clientAllDTO.isIsCreateCompte() == true) {
            log.debug("CREATION COMPTE%% ........................");
        }

        saveInfosPieces(personne, piece);
        enregistrementInfoComptes(clientAllDTO, client);
        if(verifClientSavedImage()){
            enregistrementDocumentMinio(clientAllDTO.getDocumentDTOS(), client);
        } else {
            enregistrementDocument(clientAllDTO.getDocumentDTOS(), client);
        }
        if (clientAllDTO.getClientDTO().getId() == null) {
            if (client.getId() != null && client.getPersonne().getPersonnePhysique() != null) {
                apiService.sendMessage(personne.getTelephone(), "Bienvenu cher(e) client(e). CBI, la Banque Autrement", personne.getCodePaysTelephone());

            }
            if (client.getId() != null && client.getPersonne().getPersonneMorale() != null) {
                apiService.sendMail(personne.getEmail(), "Bienvenu cher(e) client(e) votre entreprise à été créer avec succès. CBI, la Banque Autrement");

            }
        }
        return clientMapper.toDto(client);
    }

    /**
     * Enregistrement des documents d'identification
     *
     * @param personne
     * @param piece
     */
    private void saveInfosPieces(Personne personne, Piece piece) {
        if (piece != null) {
            List<Piece> pieces = pieceRepository.findByTitulaireIdAndTypePieceIdAndReferencePiece(personne.getId(), piece.getTypePiece().getId(), piece.getReferencePiece());
            if (pieces.isEmpty()) {
                piece.setTitulaire(personne);
            }
            pieceRepository.save(piece);
        }
    }

    /**
     * Enregistrement des documents d'un client
     *
     * @param documentDTOS
     */
    private void enregistrementDocument(List<DocumentDTO> documentDTOS, Client client) {
        log.debug("Documents----------------" + documentDTOS);
        Boolean removed = false;
        final String keyDirectory = (SystemUtils.IS_OS_LINUX ? imagesPathLinux : imagesPathWindows);
        String telephone = client.getPersonne().getCodePaysTelephone() + client.getPersonne().getTelephone();
        if (client.getId() == null) {
            throw new CustomParameterizedException("Client invalide!");
        }
        log.debug("client id------------" + client.getId());
        List<DocumentDTO> oldDocument = documentMapper.toDto(documentRepository.findByTitulaireId(client.getId()));
        log.debug("client id 2------------" + client.getId());
        if (oldDocument != null && documentDTOS != null) {
            if (oldDocument.size() >= documentDTOS.size()) {
                System.out.println("-+------ok removed--+--------");
                System.out.println("-+------ok removed--+--------");
                removed = true;
                oldDocument.removeAll(documentDTOS);
            }
        }
        if (removed) {
            for (DocumentDTO deleteDoc : oldDocument) {
                if (deleteDoc.getImageFolder() != null && verifClientSavedImage()) {
                    File img = new File(deleteDoc.getImageFolder());
                    if (img.isFile()) {
                        img.delete();
                    }
                }
                documentService.delete(deleteDoc.getId());

            }
        }

        if (documentDTOS != null) {
            documentDTOS.forEach(documentDTO -> {
                Document document = documentMapper.toEntity(documentDTO);
                document.setTitulaire(client);
                log.debug("*****************folder****" + document.getImageFolder());

                try {
                    if (document.getId() == null) {
                        if (verifClientSavedImage()) {
                            documentService.saveImageClientToFolder(documentDTO, telephone, client.getId(), true);
                        }
                    } else {
                        Document oldDoc = documentRepository.findById(document.getId()).orElse(null);
                        if (verifClientSavedImage()) {
                            if (oldDoc.getValeur() == null && document.getValeur() != null) {
                                File img = new File(oldDoc.getImageFolder());
                                if (img.isFile()) {
                                    img.delete();
                                }
                                documentService.saveImageClientToFolder(documentDTO, telephone, client.getId(), true);
                            }

                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (verifClientSavedImage()) {
                    if (document.getId() != null) {
                        Document oldDoc = documentRepository.findById(document.getId()).orElse(null);
                        if (oldDoc.getValeur() == null) {
                            if (SystemUtils.IS_OS_WINDOWS) {
                                String contentType = documentDTO.getValeurContentType();
                                String finalDirectory = keyDirectory.replace("\\", "/");
                                String typeImage = documentService.TransformToPngBackOffice(contentType);
                                String imageName = documentDTO.getTypeDocumentCode().replace(" ", "_");
                                document.setImageFolder(finalDirectory + "/" + telephone + "/" + client.getId().toString() + "/" + imageName + "." + typeImage);
                            } else {

                                String contentType = documentDTO.getValeurContentType();
                                String typeImage = documentService.TransformToPngBackOffice(contentType);
                                String imageName = documentDTO.getTypeDocumentCode().replace(" ", "_");
                                document.setImageFolder(keyDirectory + "/" + telephone + "/" + client.getId().toString() + "/" + imageName + "." + typeImage);

                            }
                            log.debug("**--------------" + keyDirectory);
                            document.setValeur(null);
                        }
                    } else {
                        if (SystemUtils.IS_OS_WINDOWS) {
                            String contentType = documentDTO.getValeurContentType();
                            String finalDirectory = keyDirectory.replace("\\", "/");
                            String typeImage = documentService.TransformToPngBackOffice(contentType);
                            String imageName = documentDTO.getTypeDocumentCode().replace(" ", "_");
                            document.setImageFolder(finalDirectory + "/" + telephone + "/" + client.getId().toString() + "/" + imageName + "." + typeImage);
                        } else {

                            String contentType = documentDTO.getValeurContentType();
                            String typeImage = documentService.TransformToPngBackOffice(contentType);
                            String imageName = documentDTO.getTypeDocumentCode().replace(" ", "_");
                            document.setImageFolder(keyDirectory + "/" + telephone + "/" + client.getId().toString() + "/" + imageName + "." + typeImage);

                        }
                        log.debug("**--------------" + keyDirectory);
                        document.setValeur(null);
                    }

                }

                documentRepository.save(document);

            });
        }
    }

    private void enregistrementDocumentMinio(List<DocumentDTO> documentDTOS, Client client)  {
        log.debug("Documents----------------" + documentDTOS.size());
        for(DocumentDTO doc: documentDTOS) {
            try {
                if(doc.getId()==null) {
                    minioService.saveDocumentCLientInFolder(doc, client);
                }else {
                    minioService.updateDocumentCLientInFolder(doc, client);
                }
            } catch (IOException | InvalidKeyException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
        log.debug("END SAVED DOCUMENT");
    }

    public Boolean verifClientSavedImage() {
        ParametreApp parametreApp = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "CLIENT_IMAGE_SAVED");
        if (parametreApp != null) {
            String valeur = parametreApp.getValeur();
            if (valeur.equals("0")) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }

    }

    /**
     * @param personneAllDTO
     * @param client
     */
    private void enregistrementInfoComptes(ClientAllDTO personneAllDTO, Client client) {
        if (client.getId() == null) {
            throw new CustomParameterizedException("Client invalide!");
        }
        List<CompteDTO> compteDTOS = personneAllDTO.getCompteDTOS();
        for (CompteDTO compteCBI : compteDTOS) {
            Compte compteExist = compteRepository.findTopByClientAndValeurCompte(client, compteCBI.getValeurCompte());
            if (compteExist == null) {
                compteCBI.setEtatCompte(EtatCompte.OUVERT);
                saveAllComptes(client, compteCBI);
            } else {
                Compte a = compteMapper.toEntity(compteCBI);
                compteExist.setEtatCompte(compteCBI.getEtatCompte());
                if (compteExist.getCompteAutre() != null && compteExist.getCompteAutre().getId() != null) {
                    CompteAutre compteAutre = compteAutreRepository.getOne(compteExist.getCompteAutre().getId());
                    if (compteAutre.getSolde() != null && compteAutre.getSolde().doubleValue() > 0) {
                        compteExist.setEtatCompte(EtatCompte.OUVERT);
                    }
                } else if (compteExist.getCompteBanque() != null) {
                    compteExist.setEtatCompte(EtatCompte.OUVERT);
                }
                compteExist.setStatutCompte(compteCBI.getStatutCompte());
                compteExist.setCategorieCompte(compteCBI.getCategorieCompte());
                compteExist.setClasseCompte(compteExist.getClasseCompte());
                compteExist.setValeurCompte(compteCBI.getValeurCompte());
                compteRepository.save(compteExist);
            }
        }
        log.debug("FIn------------------------------");

    }

    /**
     * @param client
     * @param compteCBI
     */
    private void saveAllComptes(Client client, CompteDTO compteCBI) {
        Compte compte = compteMapper.toEntity(compteCBI);
        String telephone = "";
        String malias = null;
        String codeTelephone = "";
        String typePersonne = "";
        Compte ctrlCpt = compteRepository.findTopByClientAndClasseCompte(client, ClasseCompte.PRINCIPAL);
        if (compteCBI.getCodeFse() != null) {
            Long codeFseId = Long.parseLong(compteCBI.getCodeFse());
            UniteOrganisation teneurCompte = uniteOrganisationRepository.findById(codeFseId).orElse(null);
            if (teneurCompte != null) {
                if (compteCBI.getCategorieCompte().equals(CategorieCompte.BANQUE)) {//Enregistrement des informations d'un compteBanque
                    CompteBanque compteBanque = compteBanqueMapper.toEntity(compteCBI.getCompteBanqueDTO());
                    compteBanque.setCodeFse(teneurCompte.getCodeFse());
                    if (client != null) {
                        telephone = client.getPersonne().getTelephone();
                        codeTelephone = client.getPersonne().getCodePaysTelephone();
                        typePersonne = client.getPersonne().getTypePersonne().name();
                        malias = compteService.getCRC32Checksum(compteBanque.getCodeAgence(), compte.getValeurCompte(), client.getPersonne().getTelephone(), compteBanque.getCodeFse());
                        compte.setAlias(malias);
                        compteBanque.setAlias(malias);
                    }

                    compteBanque = compteBanqueRepository.save(compteBanque);

                    if (compteBanque != null) {
                        userControlService.addAccount(telephone, codeTelephone, typePersonne);
                    }

                    compte.categorieCompte(CategorieCompte.BANQUE)
                            .etiquette(String.valueOf(EtiquetteCompteCode.CPTECLIENT))
                            .compteBanque(compteBanque)
                            .compteAutre(null)
                            .carte(null)
                            .client(client);

                } else if (compteCBI.getCategorieCompte().equals(CategorieCompte.AUTRE)) {//Compte autre

                    String numeroCourt = getNumeroCompteCourt();
                    String valeurCompte = getNumeroCompteOfClient(teneurCompte.getCodeFse());
                    CompteAutre compteAutre = new CompteAutre();
                    compteAutre.solde(BigDecimal.ZERO);
                    compteAutre.setCodeFse(teneurCompte.getCodeFse());
                    compteAutre = compteAutreRepository.save(compteAutre);
                    if (ctrlCpt != null) {

                        compte.setClasseCompte(ClasseCompte.SOUSCOMPTE);
                    } else {

                        compte.setClasseCompte(ClasseCompte.PRINCIPAL);
                        compte.setValeurCompte(valeurCompte);
                        compte.setNumeroCourt(numeroCourt);
                    }
                    compte.categorieCompte(compteCBI.getCategorieCompte())
                            .etiquette(String.valueOf(EtiquetteCompteCode.CPTECLIENT))
                            .compteAutre(compteAutre)
                            .carte(null)
                            .client(client);
                } else if (compteCBI.getCategorieCompte().equals(CategorieCompte.CARTE)) {
                    Carte carteMapper = this.carteMapper.toEntity(compteCBI.getCarteDTO());
                    Carte carte = new Carte();
                    carte.setLast4Digit(carteMapper.getLast4Digit());
                    carte.setCodeFse(teneurCompte.getCodeFse());
                    carteRepository.save(carte);
                    compte.setStatutCompte(StatutCompte.DESACTIVE);
                    compte.categorieCompte(compteCBI.getCategorieCompte())
                            
                            .etiquette(String.valueOf(EtiquetteCompteCode.CPTECLIENT))
                            .carte(carte)
                            .compteAutre(null)
                            .compteBanque(null)
                            .client(client);
                }
                log.debug("cpt---------------" + compte);

                compteRepository.save(compte);

            }

        }
    }

    /**
     * Recherche des informations d'un compte pour la realisation d'une
     * transaction bancaire à travers le compte d'un client ayant fait une
     * souscription
     *
     * @param telephone
     * @param numCompte
     * @param codeService
     * @param codeAgence
     * @param rib
     * @param fseId
     * @return
     */
    public ClientAllDTO findByClient(String telephone, String numCompte, String codeService,
            String codeAgence, String rib,
            Long fseId, String codeTelephone) {
        TypeService typeService = typeServiceService.findByCodeService(TypeServiceLibelle.valueOf(codeService).toString());//
        if (typeService == null) {
            throw new CustomParameterizedException("Ce type de service est invalide.");
        }
        Personne personne = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(telephone, codeTelephone);
        if (personne != null) {
            log.info("Personne % " + personne.getId());
            Client client = clientRepository.findTop1ByPersonneIdAndClientValideTrueAndSupprimeIsFalse(personne.getId());
            if (client != null && client.getClientValide() != null) {
                Compte compte = compteRepository.findTopByClientAndValeurCompte(client, numCompte);
                log.debug("COMPTE : " + numCompte + " " + client.getCodeClient());
                if (compte != null) {
                    if (fseId != null) {
                        UniteOrganisation teneurCompte = uniteOrganisationRepository.findById(fseId).orElse(null);
                        if (teneurCompte != null) {
                            if (compte.getCategorieCompte().equals(CategorieCompte.BANQUE)) {
                                if (!compte.getCompteBanque().getRib().equals(rib)
                                        || !compte.getCompteBanque().getCodeAgence().equals(codeAgence)
                                        || !compte.getCompteBanque().getCodeFse().equals(teneurCompte.getCodeFse())) {
                                    throw new CustomParameterizedException("Le compte " + codeAgence + " " + numCompte + " " + rib + " est introuvable!");
                                }
                            } else {
                                log.info("C%%" + compte.getCompteAutre().getCodeFse());
                                if (!compte.getCompteAutre().getCodeFse().equals(teneurCompte.getCodeFse())) {
                                    throw new CustomParameterizedException("Le compte " + numCompte + " est introuvable.");
                                }
                            }
                            List<Autorisation> autorisation = autorisationService.findByClientIdAndTypeServiceId(client.getId(), typeService.getId());
                            if (!autorisation.isEmpty()) {
                                ClientAllDTO clientDTO = getClientAllDTO(client, compte);
                                return clientDTO;
                            } else {
                                throw new CustomParameterizedException("Ce client n'a pas souscrit à ce service." + client.getId() + " " + typeService.getLibelle());
                            }
                        } else {
                            throw new CustomParameterizedException("Fournisseur de service introuvable.");
                        }
                    } else {
                        throw new CustomParameterizedException("Fournisseur de service introuvable.");
                    }

                } else {
                    throw new CustomParameterizedException("Le client n'a pas souscrit pour ce compte ou compte invalide.");
                }
            } else {
                throw new CustomParameterizedException("Ce client n'a pas souscrit à ce type de service.");
            }
        } else {
            throw new CustomParameterizedException("Ce client n'a pas pas souscrit à ce service.");

        }
    }

    /**
     * @param code
     * @param telephone
     * @return
     */
    public List<AutorisationDTO> getAutorisation(String code, String telephone) {
        List<AutorisationDTO> mlist = new ArrayList();
        PersonneDTO personne = personneRepository.findPersonneByPhoneAndCodeIndicatifActivated(telephone, code);
        if (personne != null) {
            log.info("Personne % " + personne.getId());
            Client client = clientRepository.findTop1ByPersonneIdAndClientValideTrueAndSupprimeIsFalse(personne.getId());
            if (client != null) {
                List<TypeService> serviceIds = client.getProfil().getTypeServices().stream().map(typeService -> typeService).collect(Collectors.toList());
                serviceIds.forEach(a -> {
                    AutorisationDTO aut = new AutorisationDTO();
                    aut.setTypeServiceId(a.getId());
                    aut.setTypeServiceLibelle(a.getLibelle());
                    mlist.add(aut);
                });
                return mlist;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Get all the clients.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<ClientAllDTO> findAll(Pageable pageable, String telephone, String code) {
        List<ClientAllDTO> clientAlls = new ArrayList<>();
        if (telephone != null && code != null) {
            Personne personne = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(telephone, code);
            if (personne != null) {
                clientRepository.findByClientValideAndPersonneIdAndSupprimeIsFalse(Boolean.TRUE, personne.getId(), pageable).forEach(c -> {
                    ClientAllDTO clientDTO = getClientAllDTO(c, null);
                    clientAlls.add(clientDTO);
                });
                return clientAlls;
            }
        }
        clientRepository.findByClientValideAndSupprimeIsFalse(Boolean.TRUE, pageable).forEach(client -> {
            ClientAllDTO clientDTO = getClientAllDTO(client, null);
            clientAlls.add(clientDTO);
        });
        return clientAlls;
    }

    @Transactional(readOnly = true)
    public List<ClientAllDTO> findAllClient(String nom, String telephone, String numeroCpt) {
        List<ClientAllDTO> clientAlls = new ArrayList<>();
        int max;
        int nbrPage;
        List<ClientDTO> xListe = new ArrayList<>();
        log.debug("bye----------------------1" + nom + "telephone" + telephone + "numeroCpt" + numeroCpt);

        if (nom == null && telephone == null && numeroCpt == null) {
            max = clientRepository.countClientPhysiqueByCriteria(null, null, null, nom, null, null, null);
            nbrPage = (max / 1000);
            for (int j = 0; j <= nbrPage; j++) {
                Pageable page = PageRequest.of(j, 1000, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
                Long taille = new Long(page.getPageSize());
                Long pag = new Long(page.getPageSize() * page.getPageNumber());
                xListe.addAll(clientRepository.findClientPhysiqueByCriteriaOptimise(null, null, null, null, null, taille, pag));
            }
            xListe.forEach(client -> {
                ClientAllDTO clientDTO = getClientAllDTOOptimise(client, null);
                clientAlls.add(clientDTO);
            });
            log.debug("bye----------------------");
        } else {

            clientRepository.findAllClientRegulation(nom, telephone, numeroCpt).forEach(c -> {
                ClientAllDTO clientDTO = getClientAllDTOOptimise(c, null);
                clientAlls.add(clientDTO);
                log.debug("result" + c);
            });
            log.debug("bye--------------------2--");
        }
        log.debug("Fin----------------------");
        return clientAlls;
    }

    @Transactional(readOnly = true)
    public List<ClientAllDTO> findAllClientEntrp(String nom, String telephone, String numeroCpt) {
        List<ClientAllDTO> clientAlls = new ArrayList<>();
        if (nom == null && telephone == null && numeroCpt == null) {
            clientRepository.findClientByTypePersonneAndCriteria(TypePersonne.MORALE, null, telephone, null).forEach(client -> {
                ClientAllDTO clientDTO = getClientAllDTO(client, null);
                clientAlls.add(clientDTO);
            });
        } else {
            clientRepository.findAllClientsEntrp(nom, telephone, numeroCpt).forEach(c -> {
                ClientAllDTO clientDTO = getClientAllDTO(c, null);
                clientAlls.add(clientDTO);
            });
        }
        return clientAlls;
    }

    @Transactional(readOnly = true)
    public Page<ClientAllDTO> findByTypePersonne(TypePersonne typePersonne, String code, String telephone, String adresse, String nom, String prenom, String raisonSociale, String numeroPiece, ZonedDateTime dateDebut, ZonedDateTime dateFin, Pageable pageable) {
        List<ClientAllDTO> result = new ArrayList<>();
        List<Client> clients = null;
        if (typePersonne != null && typePersonne.equals(TypePersonne.MORALE)) {
            if (dateDebut != null && dateFin != null) {
                clients = clientRepository.findClientMoraleByCriteriaDate(code, telephone, adresse, raisonSociale, dateDebut, dateFin);

            } else if (dateDebut != null) {
                clients = clientRepository.findClientMoraleByCriteriaDate(code, telephone, adresse, raisonSociale, dateDebut, dateDebut);
            } else if (dateFin != null) {
                clients = clientRepository.findClientMoraleByCriteriaDate(code, telephone, adresse, raisonSociale, dateFin, dateFin);
            } else {
                clients = clientRepository.findClientMoraleByCriteria(code, telephone, adresse, raisonSociale);

            }
        } else if (typePersonne != null && typePersonne.equals(TypePersonne.PHYSIQUE)) {
            if (dateDebut != null && dateFin != null) {
                clients = clientRepository.findClientPhysiqueByCriteriaDate(code, telephone, adresse, nom, prenom, dateDebut, dateFin);

            } else if (dateDebut != null) {
                clients = clientRepository.findClientPhysiqueByCriteriaDate(code, telephone, adresse, nom, prenom, dateDebut, dateDebut);

            } else {
                clients = clientRepository.findClientPhysiqueByCriteria(code, telephone, adresse, nom, prenom);

            }
        }

        if (clients != null) {
            clients.forEach(client -> {
                if (numeroPiece == null || numeroPiece.equals("") || clientPiecesContain(client, numeroPiece)) {
                    result.add(getClientAllDTO(client, null));
                }
            });
        }
        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > result.size() ? result.size() : (start + pageable.getPageSize());
        Page<ClientAllDTO> retour = new PageImpl<ClientAllDTO>(result.subList(start, end), pageable, result.size());
        return retour;
    }

    @Transactional(readOnly = true)
    public Page<Client> findByTypePersonnePage(TypePersonne typePersonne, String code, String telephone, String adresse, String nom, String prenom, String raisonSociale, String numeroPiece, Pageable pageable) {
        Page<Client> pclient = null;
        if (typePersonne != null && typePersonne.equals(TypePersonne.MORALE)) {
            pclient = clientRepository.findClientMoraleByCriteriaPage(code, telephone, adresse, raisonSociale, pageable);
        } else if (typePersonne != null && typePersonne.equals(TypePersonne.PHYSIQUE)) {
            pclient = clientRepository.findClientPhysiqueByCriteriaPage(code, telephone, adresse, nom, prenom, pageable);
        }

        return pclient;
    }

    @Transactional(readOnly = true)
    public Page<ClientDTO> findByTypePersonnePageOptimise(TypePersonne typePersonne, String code, String telephone, String adresse, String nom, String prenom, String raisonSociale, String numeroPiece, Pageable pageable) {
        ZonedDateTime dateDebut = null;
        ZonedDateTime dateFin = null;
        List<ClientDTO> pclient = null;
        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalClient = 0;

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

        if (typePersonne != null && typePersonne.equals(TypePersonne.MORALE)) {
            totalClient = clientRepository.countClientMoraleByCriteria(code, telephone, adresse, raisonSociale, null, null);
            pclient = clientRepository.findClientMoralByCriteriaOptimise(code, telephone, adresse, raisonSociale, taille, page);
        } else if (typePersonne != null && typePersonne.equals(TypePersonne.PHYSIQUE)) {
            totalClient = clientRepository.countClientPhysiqueByCriteria(code, telephone, adresse, nom, prenom, null, null);
            pclient = clientRepository.findClientPhysiqueByCriteriaOptimise(code, telephone, adresse, nom, prenom, taille, page);
        }
        Page<ClientDTO> retour = new PageImpl<>(pclient, pageable1, totalClient);
        return retour;
    }

    public List<ClientAllDTO> getAllDTO(Page<Client> pclient, String numeroPiece) {
        List<ClientAllDTO> result = new ArrayList<>();
        if (pclient != null) {
            pclient.forEach(client -> {
                if (numeroPiece == null || numeroPiece.equals("") || clientPiecesContain(client, numeroPiece)) {
                    result.add(getClientAllDTO(client, null));
                }
            });
        }
        return result;
    }

    public List<ClientAllDTO> getAllDTOOptimise(Page<ClientDTO> pclient, String numeroPiece) {
        List<ClientAllDTO> result = new ArrayList<>();
        if (pclient != null) {
            pclient.forEach(client -> {
                if (numeroPiece == null || numeroPiece.equals("") || clientPiecesContainOptimise(client, numeroPiece)) {
                    result.add(getClientAllDTOOptimise(client, null));
                }
            });
        }
        return result;
    }

    private boolean clientPiecesContain(Client client, String numeroPiece) {
        if (client != null && client.getPersonne() != null) {
            List<Piece> pieces = pieceRepository.findByTitulaireId(client.getPersonne().getId());
            if (pieces != null && !pieces.isEmpty()) {
                for (Piece piece : pieces) {
                    if (piece.getReferencePiece() != null && piece.getReferencePiece().toUpperCase().contains(numeroPiece.toUpperCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean clientPiecesContainOptimise(ClientDTO client, String numeroPiece) {
        if (client != null && client.getPersonneId() != null) {
            List<String> pieces = pieceRepository.findReferencePieceByTitulaireIdOptimise(client.getPersonneId());
            if (pieces != null && !pieces.isEmpty()) {
                for (String piece : pieces) {
                    if (piece != null && piece.toUpperCase().contains(numeroPiece.toUpperCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean clientPiecesContainRapport(ClientDTO client, String numeroPiece) {
        if (client != null && client.getPersonneId() != null) {
            List<String> pieces = pieceRepository.findReferencePieceByTitulaireIdOptimise(client.getPersonneId());
            if (pieces != null && !pieces.isEmpty()) {
                for (String piece : pieces) {
                    if (piece != null && piece.toUpperCase().contains(numeroPiece.toUpperCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean clientCompteContainValeur(ClientDTO client, String valeurCompte) {
        if (client == null || client.getId() == null || valeurCompte == null || valeurCompte.trim().isEmpty()) {
            return false;
        }
        List<CompteDTO> comptes = compteRepository.findByClientIdOptimise(client.getId());
        if (comptes != null) {
            String filter = valeurCompte.trim().toUpperCase();
            for (CompteDTO compte : comptes) {
                if (compte.getValeurCompte() != null && compte.getValeurCompte().toUpperCase().contains(filter)) {
                    return true;
                }
            }
        }
        return false;
    }

    private ClientAllDTO getClientPersonneMorale(Client client, Compte compte) {
        ClientAllDTO clientDTO = new ClientAllDTO();
        Personne personne = client.getPersonne();
        Personne p = personneRepository.findByPersonneId(personne.getId());
        clientDTO.setPersonneDTO(personneMapper.toDto(p));
        clientDTO.setClientDTO(clientMapper.toDto(client));
        if (p != null) {
            if (p.getPersonneMorale() != null) {
                PersonneMorale personneMorale = p.getPersonneMorale();
                clientDTO.setPersonneMoraleDTO(personneMoraleMapper.toDto(personneMorale));
                clientDTO.setNom(personneMorale.getRaisonSociale());
                clientDTO.setTelephone(personne.getTelephone());
            }

        }
        //clientDTO.setDocumentDTOS(documentMapper.toDto(documentRepository.findByTitulaireId(client.getId())));
        return clientDTO;
    }

    /**
     * @param compte
     * @return
     * @ param client
     */
    private ClientAllDTO getClientAllDTO(Client client, Compte compte) {
        ClientAllDTO clientDTO = new ClientAllDTO();
        Personne personne = client.getPersonne();
        clientDTO.setPersonneDTO(personneMapper.toDto(personne));

        clientDTO.setClientDTO(clientMapper.toDto(client));
        if (personne != null) {
            if (personne.getPersonneMorale() != null) {
                PersonneMorale personneMorale = personne.getPersonneMorale();
                clientDTO.setPersonneMoraleDTO(personneMoraleMapper.toDto(personneMorale));
                clientDTO.setNom(personneMorale.getRaisonSociale());
                clientDTO.setTelephone(personne.getTelephone());
            }
            if (personne.getPersonnePhysique() != null) {
                PersonnePhysique personnePhysique = personne.getPersonnePhysique();
                clientDTO.setNom(personnePhysique.getNomClient() + " " + personnePhysique.getPrenomClient());
                clientDTO.setTelephone(personne.getTelephone());
                clientDTO.setPersonnePhysiqueDTO(personnePhysiqueMapper.toDto(personnePhysique));
            }
            Piece piece = pieceRepository.findTopByTitulaireId(personne.getId());
            if (piece != null) {
                clientDTO.setPieceDTO(pieceMapper.toDto(piece));
            }
            if (compte == null) {
                List<Compte> comptes = compteRepository.findByClientId(client.getId());
                List<CompteDTO> compteDTOS = new ArrayList<>();
                if (comptes != null) {
                    for (Compte c : comptes) {
                        CompteDTO compteDTO = compteMapper.toDto(c);
                        if (c.getCompteBanque() != null) {
                            compteDTO.setCompteBanqueDTO(compteBanqueMapper.toDto(c.getCompteBanque()));
                        } else {
                            compteDTO.setCompteAutreDTO(compteAutreMapper.toDto(c.getCompteAutre()));
                        }
                        compteDTOS.add(compteDTO);
                    }
                    clientDTO.setCompteDTOS(compteDTOS);
                }
            } else {
                List<Compte> comptes = new ArrayList<>(Arrays.asList(compte));
                List<CompteDTO> compteDTOS = new ArrayList<>();
                if (comptes != null) {
                    for (Compte c : comptes) {
                        CompteDTO compteDTO = compteMapper.toDto(c);
                        if (c.getCompteBanque() != null) {
                            compteDTO.setCompteBanqueDTO(compteBanqueMapper.toDto(c.getCompteBanque()));
                        } else {
                            compteDTO.setCompteAutreDTO(compteAutreMapper.toDto(c.getCompteAutre()));
                        }
                        compteDTOS.add(compteDTO);
                    }
                    clientDTO.setCompteDTOS(compteDTOS);
                }

            }
        }
        //clientDTO.setDocumentDTOS(documentMapper.toDto(documentRepository.findByTitulaireId(client.getId())));
        return clientDTO;
    }

    private ClientAllDTO getClientAllDTOOptimise(ClientDTO client, Compte compte) {
        log.debug("*-----------**AgentEnroleur------" + client.getUniteOrganisationCreateurId());
        log.debug("*-----------**AgentEnroleur------" + client.getId());
        ClientAllDTO clientDTO = new ClientAllDTO();
        PersonneDTO personne = personneRepository.findPersonneBackofficeById(client.getPersonneId());
        clientDTO.setPersonneDTO(personne);
        clientDTO.setClientDTO(client);
        if (client.getUniteOrganisationCreateurId() != null) {
            clientDTO.getClientDTO().setAgentEnroleur(uniteOrganisationRepository.getLibelleUniteOrganisation(client.getUniteOrganisationCreateurId()));
        }
        if (client.getProfilId() != null) {
            clientDTO.getClientDTO().setProfilNom(serviceProfilRepository.getNomServiceProfil(client.getProfilId()));
        }
        if (client.getProfilActeurId() != null) {
            clientDTO.getClientDTO().setProfilActeurNom(profilActeurRepository.getNomProfilActeur(client.getProfilActeurId()));
        }
        if (client.getGroupeProfilActeurId() != null) {
            clientDTO.getClientDTO().setGroupeProfilActeurNom(groupeProfilActeurRepository.getNomGroupeProfilActeur(client.getGroupeProfilActeurId()));
        }
        if (personne != null) {
            if (personne.getPersonneMoraleId() != null) {
                PersonneMorale personneMorale = personneMoraleRepository.findById(personne.getPersonneMoraleId()).orElse(null);
                clientDTO.setPersonneMoraleDTO(personneMoraleMapper.toDto(personneMorale));
                clientDTO.setNom(personneMorale.getRaisonSociale());
                clientDTO.setTelephone(personne.getTelephone());
            }
            if (personne.getPersonnePhysiqueId() != null) {
                PersonnePhysiqueDTO personnePhysique = personnePhysiqueRepository.findPersonneByIdClient(personne.getPersonnePhysiqueId());
                clientDTO.setNom(personnePhysique.getNomClient() + " " + personnePhysique.getPrenomClient());
                clientDTO.setTelephone(personne.getTelephone());
                clientDTO.setPersonnePhysiqueDTO(personnePhysique);
            }
            List<Piece> lpiece = pieceRepository.findListPieceByTitulaireId(personne.getId());
            if (lpiece != null && lpiece.size() > 0) {
                Piece piece = lpiece.get(0);
                if (piece != null) {
                    clientDTO.setPieceDTO(pieceMapper.toDto(piece));
                }
            }

            if (compte == null) {
                List<CompteDTO> comptes = compteRepository.findByClientIdOptimise(client.getId());
                List<CompteDTO> compteDTOS = new ArrayList<>();
                if (comptes != null) {
                    for (CompteDTO c : comptes) {
                        c.setClientCodeClient(client.getCodeClient());
                        if (c.getCompteBanqueId() != null) {
                            c.setCompteBanqueDTO(compteBanqueMapper.toDto(compteBanqueRepository.findById(c.getCompteBanqueId()).orElse(null)));
                        } else {
                            c.setCompteAutreDTO(compteAutreRepository.findByCompteAutreIdOptimise(c.getCompteAutreId()));
                        }
                        if (c.getCompteAutreDTO() != null) {
                            c.setSoldeCompte(c.getCompteAutreDTO().getSolde());
                        }

                        compteDTOS.add(c);
                    }
                    clientDTO.setCompteDTOS(compteDTOS);
                }
            } else {
                List<Compte> comptes = new ArrayList<>(Arrays.asList(compte));
                List<CompteDTO> compteDTOS = new ArrayList<>();
                if (comptes != null) {
                    for (Compte c : comptes) {
                        CompteDTO compteDTO = compteMapper.toDto(c);
                        if (c.getCompteBanque() != null) {
                            compteDTO.setCompteBanqueDTO(compteBanqueMapper.toDto(c.getCompteBanque()));
                        } else {
                            compteDTO.setCompteAutreDTO(compteAutreMapper.toDto(c.getCompteAutre()));
                        }
                        compteDTOS.add(compteDTO);
                    }
                    clientDTO.setCompteDTOS(compteDTOS);
                }
            }
        }
        return clientDTO;
    }

    private ClientAllDTO getClientAllDTOWithImage(Client client, Compte compte) {
        ClientAllDTO clientDTO = new ClientAllDTO();
        Personne personne = client.getPersonne();
        clientDTO.setPersonneDTO(personneMapper.toDto(personne));

        clientDTO.setClientDTO(clientMapper.toDto(client));
        if(clientDTO.getClientDTO()!=null && client.getReseau()!=null){
            clientDTO.getClientDTO().setReseauId(client.getReseau().getId());
        }
        if(clientDTO.getClientDTO()!=null && client.getInfoSupp()!=null){
            clientDTO.getClientDTO().setInfosuppId(client.getInfoSupp().getId());
        }
        if (personne != null) {
            if (personne.getPersonneMorale() != null) {
                PersonneMorale personneMorale = personne.getPersonneMorale();
                clientDTO.setPersonneMoraleDTO(personneMoraleMapper.toDto(personneMorale));
                clientDTO.setNom(personneMorale.getRaisonSociale());
                clientDTO.setTelephone(personne.getTelephone());
            }
            if (personne.getPersonnePhysique() != null) {
                PersonnePhysique personnePhysique = personne.getPersonnePhysique();
                clientDTO.setNom(personnePhysique.getNomClient() + " " + personnePhysique.getPrenomClient());
                clientDTO.setTelephone(personne.getTelephone());
                clientDTO.setPersonnePhysiqueDTO(personnePhysiqueMapper.toDto(personnePhysique));
            }
            Piece piece = pieceRepository.findTopByTitulaireId(personne.getId());
            if (piece != null) {
                clientDTO.setPieceDTO(pieceMapper.toDto(piece));
            }
            if (compte == null) {
                List<Compte> comptes = compteRepository.findByClientId(client.getId());
                List<CompteDTO> compteDTOS = new ArrayList<>();
                if (comptes != null) {
                    for (Compte c : comptes) {
                        CompteDTO compteDTO = compteMapper.toDto(c);
                        if (c.getCompteBanque() != null) {
                            compteDTO.setCompteBanqueDTO(compteBanqueMapper.toDto(c.getCompteBanque()));
                        } else if (c.getCompteAutre() != null){
                            compteDTO.setCompteAutreDTO(compteAutreMapper.toDto(c.getCompteAutre()));
                        } else if (c.getCarte() != null){
                            compteDTO.setCarteDTO(carteMapper.toDto(c.getCarte()));
                        }
                        compteDTOS.add(compteDTO);
                    }
                    clientDTO.setCompteDTOS(compteDTOS);
                }
            } else {
                List<Compte> comptes = new ArrayList<>(Arrays.asList(compte));
                List<CompteDTO> compteDTOS = new ArrayList<>();
                if (comptes != null) {
                    for (Compte c : comptes) {
                        CompteDTO compteDTO = compteMapper.toDto(c);
                        if (c.getCompteBanque() != null) {
                            compteDTO.setCompteBanqueDTO(compteBanqueMapper.toDto(c.getCompteBanque()));
                        } else {
                            compteDTO.setCompteAutreDTO(compteAutreMapper.toDto(c.getCompteAutre()));
                        }
                        compteDTOS.add(compteDTO);
                    }
                    clientDTO.setCompteDTOS(compteDTOS);
                }

            }
        }
        clientDTO.setDocumentDTOS(documentMapper.toDto(documentRepository.findByTitulaireId(client.getId())));
        return clientDTO;
    }

    private ListeClientDTO getClientAllDTOReport(ClientDTO client, TypePersonne persone) {
        ListeClientDTO clientDTO = new ListeClientDTO();
        PersonneDTO personne = personneRepository.findPersonneById(client.getPersonneId());
        if (personne != null) {
            if (persone.equals(TypePersonne.MORALE)) {
                if (personne.getPersonneMoraleId() != null) {
                    PersonneMorale personneMorale = personneMoraleRepository.findById(personne.getPersonneMoraleId()).orElse(null);
                    clientDTO.setNomClient(personneMorale.getRaisonSociale());
                    clientDTO.setPrenomClient(personneMorale.getNumreoIfu());
                    clientDTO.setTelephone(personne.getTelephone());
                    clientDTO.setDirigent(personneMorale.getDirigeant());
                    clientDTO.setSiegeSocial(personneMorale.getSiegeSocial());
                    clientDTO.setAdresse(personne.getAdresse());
                    clientDTO.setId(client.getId());
                    if (client.getProfilActeurId() != null) {
                        clientDTO.setProfil(profilActeurRepository.getNomProfilActeur(client.getProfilActeurId()));
                    }
                    if (client.getGroupeProfilActeurId() != null) {
                        clientDTO.setGroupeProfilActeur(groupeProfilActeurRepository.getNomGroupeProfilActeur(client.getGroupeProfilActeurId()));
                    }
                    CompteDTO compte = compteRepository.findByClientIdAndClasseCompteToReport(client.getId(), ClasseCompte.PRINCIPAL.name());
                    if (compte != null && compte.getCompteAutreId() != null) {
                        clientDTO.setSolde(compte.getSoldeCompte());
                        if (compte.getCompteAutreId() != null) {
                            clientDTO.setNbrTransaction(transactionComptePtiRepository.countByCompteAutreId(compte.getCompteAutreId()));
                        }
                    }
                    if (client.getCreatedDate() != null) {
                        ZoneId zone = ZoneId.systemDefault();
                        ZonedDateTime zdt = client.getCreatedDate().atZone(zone);
                        clientDTO.setDateCreation(formatDateOnlyWithTime(zdt));
                    }
                    if (client.getClientValide() != null) {
                        clientDTO.setStatut("Actif");
                    } else {
                        clientDTO.setStatut("Inactif");
                    }
                }
            }
            if (persone.equals(TypePersonne.PHYSIQUE)) {
                if (personne.getPersonnePhysiqueId() != null) {
                    PersonnePhysiqueDTO personnePhysique = personnePhysiqueRepository.findByIdReport(personne.getPersonnePhysiqueId());
                    clientDTO.setNomClient(personnePhysique.getNomClient());
                    clientDTO.setPrenomClient(personnePhysique.getPrenomClient());
                    clientDTO.setTelephone(personne.getTelephone());
                    clientDTO.setNationalite(personnePhysique.getNationaliteLibelle());
                    clientDTO.setAdresse(personne.getAdresse());
                    clientDTO.setId(client.getId());
                    if (personnePhysique.getPaysResidenceId() != null) {
                        clientDTO.setPaysResidence(nationaliteService.getLibelleById(personnePhysique.getPaysResidenceId()));
                    }
                    clientDTO.setDateNaiss(personnePhysique.getDateNaissance());

                    if (personnePhysique.getSexe() != null) {
                        clientDTO.setSexe(personnePhysique.getSexe().name());
                    }

                    if (client.getProfilActeurId() != null) {
                        clientDTO.setProfil(profilActeurRepository.getNomProfilActeur(client.getProfilActeurId()));
                    }
                    if (client.getGroupeProfilActeurId() != null) {
                        clientDTO.setGroupeProfilActeur(groupeProfilActeurRepository.getNomGroupeProfilActeur(client.getGroupeProfilActeurId()));
                    }
                    CompteDTO compte = compteRepository.findByClientIdAndClasseCompteToReport(client.getId(), ClasseCompte.PRINCIPAL.name());
                    if (compte != null && compte.getCompteAutreId() != null) {
                        clientDTO.setSolde(compteAutreRepository.getSoldeById(compte.getCompteAutreId()));
                        if (compte.getCompteAutreId() != null) {
                            // clientDTO.setNbrTransaction(transactionComptePtiRepository.countByCompteAutreId(compte.getCompteAutreId()));
                            clientDTO.setNbrTransaction(0);
                        }
                    }
                    if (client.getClientValide() != null) {
                        clientDTO.setStatut("Actif");
                    } else {
                        clientDTO.setStatut("Inactif");
                    }
                    if (client.getCreatedDate() != null) {
                        ZoneId zone = ZoneId.systemDefault();
                        ZonedDateTime zdt = client.getCreatedDate().atZone(zone);
                        clientDTO.setDateCreation(formatDateOnlyWithTime(zdt));
                    }
                }
            }
        }
        return clientDTO;
    }
    
    public Customers getClientAllDTOExternal(ClientDTO client, Compte compte,String type) {
        log.debug("*-----------**AgentEnroleur------" + client.getId());
        ClientAllDTO clientDTO = new ClientAllDTO();
        Customers customers = new Customers();
        PersonneDTO personne = personneRepository.findPersonneBackofficeById(client.getPersonneId());
        customers.setWalletId(client.getId().toString());
        if (personne != null) {
            if (personne.getPersonnePhysiqueId() != null) {
                PersonnePhysiqueDTO personnePhysique = personnePhysiqueRepository.findPersonneByIdClient(personne.getPersonnePhysiqueId());
                customers.setLastName(personnePhysique.getNomClient());
                customers.setFirstName(personnePhysique.getPrenomClient());
                customers.setPhoneNumber(personne.getTelephone());
                if (personne.isActivated()) {
                    customers.setWalletStatus("ACTIVE");
                } else {
                    customers.setWalletStatus("SUSPENDU");
                }
            }
            List<Piece> lpiece = pieceRepository.findListPieceByTitulaireId(personne.getId());
            if (lpiece != null && lpiece.size() > 0) {
                Piece piece = lpiece.get(0);
                if (piece != null) {
                    customers.setIdentityDocumentNumber(piece.getReferencePiece());
                    if (piece.getTypePiece() != null && piece.getTypePiece().getCodeTypePiece() != null) {
                        customers.setIdentityDocumentType(piece.getTypePiece().getCodeTypePiece());
                    }
                }
            }

            if (compte == null) {
                List<CompteDTO> comptes = compteRepository.findByClientIdOptimise(client.getId());
                List<CompteDTO> compteDTOS = new ArrayList<>();
                if (comptes != null) {
                    for (CompteDTO c : comptes) {
                        c.setClientCodeClient(client.getCodeClient());
                        if (c.getCompteAutreId() != null) {
                            c.setCompteAutreDTO(compteAutreRepository.findByCompteAutreIdOptimise(c.getCompteAutreId()));
                        }
                        if (type.equals("GTP")) {
                            if (c.getCategorieCompte().equals(CategorieCompte.CARTE)) {
                                customers.setCardNumber(c.getValeurCompte());
                                customers.setCardStatus(resolveCardStatus(c.getEtatCompte(), c.getStatutCompte()));
                            } else if (c.getCategorieCompte().equals(CategorieCompte.AUTRE)) {
                                customers.setCustomerNumber(c.getValeurCompte());
                            }
                        } else {
                            if (c.getCompteAutreId() != null) {
                                customers.setCustomerNumber(c.getValeurCompte());
                            }
                        }
                        if (c.getEtatCompte() != null && c.getEtatCompte().equals(EtatCompte.FERME)) {
                            customers.setWalletStatus("CLOSED");
                        }
                    }
                }
            } else {
                List<Compte> comptes = new ArrayList<>(Arrays.asList(compte));
                List<CompteDTO> compteDTOS = new ArrayList<>();
                if (comptes != null) {
                    for (Compte c : comptes) {
                        CompteDTO compteDTO = compteMapper.toDto(c);
                        if (type.equals("COMO") && c.getCompteAutre() != null) {
                            if (c.getCompteAutre() != null) {
                                customers.setCustomerNumber(c.getValeurCompte());
                            }
                            if (c.getEtatCompte() != null && c.getEtatCompte().equals(EtatCompte.FERME)) {
                                customers.setWalletStatus("CLOSED");
                            }
                        } else {
                            if (c.getCategorieCompte().equals(CategorieCompte.CARTE)) {
                                customers.setCardNumber(c.getValeurCompte());
                                customers.setCardStatus(resolveCardStatus(c.getEtatCompte(), c.getStatutCompte()));
                            } else if (c.getCategorieCompte().equals(CategorieCompte.AUTRE)) {
                                customers.setCustomerNumber(c.getValeurCompte());
                            }
                            if (c.getEtatCompte() != null && c.getEtatCompte().equals(EtatCompte.FERME)) {
                                customers.setWalletStatus("CLOSED");
                            }
                        }
                    }
                }
            }
        }
        return customers;
    }

    private String resolveCardStatus(EtatCompte etatCompte, StatutCompte statutCompte) {
        if (etatCompte != null && EtatCompte.FERME.equals(etatCompte)) {
            return "CLOSED";
        }
        if (statutCompte != null) {
            return statutCompte.name();
        }
        return "ACTIVE";
    }

    @Transactional(readOnly = true)
    public ClientDTO findOne(Long id) {
        log.debug("Request to get Client : {}", id);
        Client client = clientRepository.findById(id).orElse(null);
        return clientMapper.toDto(client);
    }

    /**
     * Get one client by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Client findClient(Long id) {
        log.debug("Request to get Client : {}", id);
        Client client = clientRepository.findById(id).orElse(null);
        return client;
    }

    //@Transactional(readOnly = true)
    public List<ChargementTransfert> findChargement(Long id) {
        List<ChargementTransfert> chargementTransferts = new ArrayList<>();
        System.out.println("--------OKKKK-------" + id);
        //chargementTransferts = clientRepository.findChargement(id);
        chargementTransferts = chargementTransfertRepository.findAllByClientId(id);

        return chargementTransferts;
    }

    @Transactional(readOnly = true)
    public Page<ChargementTransfert> findChargementPage(Long id, Pageable pageable) {
        Page<ChargementTransfert> chargementTransferts = null;
        System.out.println("--------OKKKK-------" + id);
        chargementTransferts = chargementTransfertRepository.findAllByClientIdPage(id, pageable);

        return chargementTransferts;
    }

    /**
     * Delete the client by id.
     *
     * @param id the id of the entity
     */
    @Transactional
    public void delete(Long id) {
        log.debug("Request to delete Client : {}", id);
        Client client = this.findClient(id);
        if (client != null) {
            verifSuppressionClient(client);
            client.setSupprime(true);
            clientRepository.save(client);
            Personne personne = personneRepository.findById(client.getPersonne().getId()).orElse(null);
            userControlService.removeUser(personne.getTelephone(), personne.getCodePaysTelephone(), TypeProfilActeur.CLIENT.name());
            personne.setTelephone(personne.getTelephone() + "_" + personne.getId());
            personneRepository.save(personne);
            saveEventAudit(id.toString(), "CLIENT", "SUPPRIME_COMPTE");
            //UserControl login,codePays,typeActeur
            /*List<Compte> comptes = compteRepository.findByClientId(client.getId());
            if (comptes != null) {
                comptes.forEach(compte -> {
                    compte.setValeurCompte(compte.getValeurCompte() + "_" + client.getId());
                    compteRepository.save(compte);
                });
            }*/
        } else {
            throw new CustomParameterizedException("Client introuvable");
        }
    }

    public void verifSuppressionClient(Client client) {
        if (client != null) {
            if (client.getClientValide()) {

                throw new CustomParameterizedException("Impossible de supprimé un client Actif");
            }
            List<Compte> comptes = compteRepository.findByClientId(client.getId());
            if (!comptes.isEmpty()) {
                comptes.forEach(compte -> {
                    if (compte.getEtatCompte().equals(EtatCompte.OUVERT) || compte.getStatutCompte().equals(StatutCompte.ACTIVE)) {

                        throw new CustomParameterizedException("Impossible de supprimé un client dont le compte est ouvert ou actif");
                    }
                });
            }
        }
    }

    /**
     * @param personneId
     * @return
     */
    public ClientAllDTO findAllByPersonneId(Long personneId) {
        PersonneDTO personne = personneRepository.findPersonneById(personneId);
        if (personne != null) {
            Client client = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(personneId);
            if (client != null) {
                return getClientAllDTOWithImage(client, null);
            }
        }
        return null;
    }

    public Client findByPersonneId(Long personneId) {
        Personne personne = personneRepository.findByPersonneId(personneId);
        Client client;
        //      PersonneDTO persDto = personneRepository.findPersonneById(personneId);
        /*
        if (persDto != null) {
            BeanUtils.copyProperties(persDto, personne);
        }
         */
        if (personne != null) {
            ClientDTO clientDto = clientRepository.findClientPersonne(personneId);
            if (clientDto != null) {

                client = clientMapper.toEntity(clientDto);

                return client;

            }

        }
        return null;
    }

    public List<ClientAllDTO> findByTypePer() {

        List<ClientAllDTO> clientAlls = new ArrayList<>();

        clientRepository.findAllClientMorale().forEach(client -> {
            ClientAllDTO clientDTO = getClientPersonneMorale(clientMapper.toEntity(client), null);
            clientAlls.add(clientDTO);
        });
        return clientAlls;

    }

    public List<Compte> findCptEntrp(Long id) {
        return clientRepository.findCptEntrp(id);
    }

    public List<ClientAllDTO> findAll() {
        List<ClientAllDTO> clientAlls = new ArrayList<>();

        clientRepository.findByClientValideAndSupprimeIsFalse(Boolean.TRUE).forEach(client -> {
            ClientAllDTO clientDTO = getClientAllDTO(client, null);
            clientAlls.add(clientDTO);
        });
        return clientAlls;
    }

    private String getHash(PersonnePhysiqueDTO personne) {
        String hash = "";
        String dateNaiss = "";
        if (personne != null) {
            if (personne.getDateNaissance() != null) {
                dateNaiss = personne.getDateNaissance().format(DateTimeFormatter.ISO_DATE);
            }
            if (personne.getNomClient().compareToIgnoreCase(personne.getPrenomClient()) > 0) {
                hash = personne.getNomClient() + "_" + personne.getPrenomClient();
            } else {
                hash = personne.getPrenomClient() + "_" + personne.getNomClient();
            }

            if (personne.getNomMereClient() != null && hash.compareToIgnoreCase(personne.getNomMereClient()) > 0) {
                hash = hash + "_" + personne.getNomMereClient();
            } else {
                hash = personne.getNomMereClient() + "_" + hash;
            }

            hash = hash + "_" + dateNaiss;

        }

        return hash;
    }

    public PersonnePhysique saveExternePersonnePhysique(Personne personne, Personne pers) {
        PersonnePhysique personnePhysique;
        if (pers == null) {
            personnePhysique = new PersonnePhysique();
        } else {
            personnePhysique = pers.getPersonnePhysique();
        }

        personnePhysique.setNomClient(personne.getPersonnePhysique().getNomClient());
        personnePhysique.setDateNaissance(personne.getPersonnePhysique().getDateNaissance());
        personnePhysique.setPrenomClient(personne.getPersonnePhysique().getPrenomClient());
        personnePhysique.setPersonnePrevenir(personne.getPersonnePhysique().getPersonnePrevenir());
        personnePhysique.setNomMereClient(personne.getPersonnePhysique().getNomMereClient());
        if (personne.getPersonnePhysique().getSexe() != null) {
            personnePhysique.setSexe(Sexe.valueOf(personne.getPersonnePhysique().getSexe().name()));
        }
        personnePhysique.setDateNaissance(personne.getPersonnePhysique().getDateNaissance());
        personnePhysique.setLieuNaissance(personne.getPersonnePhysique().getLieuNaissance());
        personnePhysique = personnePhysiqueRepository.save(personnePhysique);
        return personnePhysique;
    }

    public Personne enregistrePersonne(Personne personne, Personne newPersonne, PersonnePhysique personnePhysiqueNew) {
        String hash = getHash(personnePhysiqueMapper.toDto(personne.getPersonnePhysique()));
        Chiffrement.hashString(hash);
        newPersonne.setKeyHash(hash);
        newPersonne.setCodePaysTelephone(personne.getCodePaysTelephone());
        newPersonne.setTelephone(personne.getTelephone());
        newPersonne.setEmail(personne.getEmail());
        newPersonne.setPersonnePhysique(personnePhysiqueNew);
        newPersonne.setTypePersonne(TypePersonne.PHYSIQUE);
        newPersonne.setActivated(true);
        newPersonne.setAdresse(personne.getTelephone());
        newPersonne = personneRepository.save(newPersonne);
        return newPersonne;
    }

    public String getNumeroCompteOfClient(String codeFse) {
        if (codeFse != null) {
            String numeroCourt = getNumeroCompteCourt();
            String valeurCpte = getNumeroCompte(numeroCourt, codeFse);
            return valeurCpte;
        } else {
            throw new CustomParameterizedException("Fournisseur de service introuvable.");
        }

    }

    private String getNumeroCompteCourt() {

        String numeroCourt = compteRepository.findMaxNumeroCourt();
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

    public CompteBanque saveCompteBanqueOfClient(XCompteBanqueDTO xCompteBanqueDTO) {
        CompteBanque compteBanque = new CompteBanque();
        compteBanque.setCodeFse(xCompteBanqueDTO.getCodeFse());
        compteBanque.setCodeAgence(xCompteBanqueDTO.getCodeAgence());
        compteBanque.setCodeBanque(xCompteBanqueDTO.getCodeBanque());
        compteBanque.setRib(xCompteBanqueDTO.getRib());

        Client client = clientRepository.findById(xCompteBanqueDTO.getClientId()).orElse(null);
        String telephone = "";
        String malias = null;
        String codeTelephone = "";
        String typePersonne = "";
        Compte compte = new Compte();
        if (client != null) {
            telephone = client.getPersonne().getTelephone();
            codeTelephone = client.getPersonne().getCodePaysTelephone();
            typePersonne = client.getPersonne().getTypePersonne().name();

            malias = compteService.getCRC32Checksum(compteBanque.getCodeAgence(), xCompteBanqueDTO.getValeurCompte(), client.getPersonne().getTelephone(), compteBanque.getCodeFse());
            compteBanque.setAlias(malias);
        }
        compteBanque = compteBanqueRepository.save(compteBanque);
        compte.setValeurCompte(xCompteBanqueDTO.getValeurCompte());
        compte.setStatutCompte(StatutCompte.DESACTIVE);
        compte.setEtatCompte(EtatCompte.FERME);
        compte.categorieCompte(CategorieCompte.BANQUE)
                .etiquette(String.valueOf(EtiquetteCompteCode.CPTECLIENT))
                .compteBanque(compteBanque)
                .client(client);
        Compte cpte = compteRepository.save(compte);
        String action = "AJOUT_COMPTE";
        ActionEventDTO actionEvt = new ActionEventDTO(cpte.getId().toString(), SecurityUtils.getCurrentUserLogin(), "COMPTE", action);
        messageProducer.sendMessageActionEvent(actionEvt);

        if (cpte != null) {
            userControlService.addAccount(telephone, codeTelephone, typePersonne);
        }
        return compteBanque;
    }

    public Boolean deleteCompteBanqueOfClient(Long compteBanqueId) {
        if (compteBanqueId != null) {
            List<Operation> listOp = operationRepository.findOperationByCompteBanque(compteBanqueId);
            if (listOp.size() <= 0) {
                CompteBanque compteBanqueRemove = compteBanqueRepository.getOne(compteBanqueId);
                if (compteBanqueRemove.getId() != null) {
                    Compte compteRemove = compteRepository.findByCompteBanque(compteBanqueRemove);
                    if (compteRemove.getId() != null) {
                        compteBanqueRepository.delete(compteBanqueRemove);
                        compteRepository.delete(compteRemove);
                        return true;
                    }
                }
            } else {
                throw new CustomParameterizedException("Impossible de supprimer le compte banque");
            }

        }
        return false;
    }

    public HashMap findByTypePersonne(TypePersonne typePersonne) {
        log.debug("********1111*******" + typePersonne);
        Map<String, Integer> total = new HashMap<>();
        Integer totalClient = clientRepository.findByTypePersonne(typePersonne);
        total.put("total", totalClient);
        return (HashMap) total;
    }

    public String formatDateOnlyWithTime(ZonedDateTime dateTime) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(dateTime);
    }

    // TODO: 17/06/2022 Service à révoir pour réutiliser l'existant
    @Transactional
    public Page<Client> findClientsFiltered(Pageable pageable, String nomClient) {
        log.debug("Request to get clients filtered");
        return clientRepository.findByNomClient(nomClient, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Client> findAlls(int page, int size) {
        log.debug("Request to get all Clients");
        return clientRepository.findAll(PageRequest.of(page, size));
    }

    @Transactional(readOnly = true)
    public List<Client> findAllss(String nomClient) {
        log.debug("Request to get all Clients");
        return clientRepository.findByNom(nomClient);
    }

    @Transactional(readOnly = true)
    public List<ListeClientDTO> findByTypePersonneReportOptimise(TypePersonne typePersonne, String code, String telephone, String adresse, String nom, String prenom, String raisonSociale, String numeroPiece, ZonedDateTime dateDebut, ZonedDateTime dateFin, Long taille, Long page) {
        List<ListeClientDTO> result = new ArrayList<>();
        List<ClientDTO> clients = null;
        if (typePersonne != null && typePersonne.equals(TypePersonne.MORALE)) {
            if (dateDebut != null && dateFin != null) {
                clients = clientRepository.findClientMoraleByCriteriaDateReportOptimise(code, telephone, adresse, raisonSociale, dateDebut, dateFin, taille, page);
            } else if (dateDebut != null) {
                clients = clientRepository.findClientMoraleByCriteriaDateReportOptimise(code, telephone, adresse, raisonSociale, dateDebut, dateDebut, taille, page);
            } else if (dateFin != null) {
                clients = clientRepository.findClientMoraleByCriteriaDateReportOptimise(code, telephone, adresse, raisonSociale, dateFin, dateFin, taille, page);
            } else {
                clients = clientRepository.findClientMoraleByCriteriaDateReportOptimise(code, telephone, adresse, raisonSociale, dateDebut, dateFin, taille, page);
            }
        } else if (typePersonne != null && typePersonne.equals(TypePersonne.PHYSIQUE)) {
            if (dateDebut != null && dateFin != null) {
                clients = clientRepository.findClientPhysiueByCriteriaDateReportOptimise(code, telephone, adresse, nom, prenom, dateDebut, dateFin, taille, page);
            } else if (dateDebut != null) {
                clients = clientRepository.findClientPhysiueByCriteriaDateReportOptimise(code, telephone, adresse, nom, prenom, dateDebut, dateDebut, taille, page);
            } else {
                clients = clientRepository.findClientPhysiueByCriteriaDateReportOptimise(code, telephone, adresse, nom, prenom, dateDebut, dateFin, taille, page);
            }
        }
        if (clients != null) {
            clients.parallelStream().forEach(client -> {
                if (numeroPiece == null || numeroPiece.equals("") || clientPiecesContainRapport(client, numeroPiece)) {
                    result.add(getClientAllDTOReport(client, typePersonne));
                }
            });
        }
        return result;
    }

    public Compte addCompteComo(ClientDTO clientDTO) {
        String codeFseSrc = "";
        if (clientDTO.getPersonneId() != null) {
            PersonneDTO personneDTO = personneRepository.findPersonneById(clientDTO.getPersonneId());
            ParametreApp parametreApp = parametreAppRepository.findByTypeAndNomAndCodeFse("FOURNISSEUR_MONNAIE", personneDTO.getCodePaysTelephone());
            if (parametreApp != null && parametreApp.getValeur() != null) {
                codeFseSrc = parametreApp.getValeur();
            }
        }
        CompteDTO compteDTO = new CompteDTO();
        compteDTO.setEtatCompte(EtatCompte.OUVERT);
        compteDTO.setCodeFse(codeFseSrc);
        compteDTO.setClasseCompte(ClasseCompte.PRINCIPAL);
        compteDTO.setCategorieCompte(CategorieCompte.AUTRE);
        compteDTO.setStatutCompte(StatutCompte.ACTIVE);
        compteDTO.setEtiquette(String.valueOf(EtiquetteCompteCode.CPTECLIENT));
        compteDTO.setClientId(clientDTO.getId());
        compteDTO.setNumeroCourt(getNumeroCompteCourt());
        compteDTO.setValeurCompte(getNumeroCompteOfClient(codeFseSrc));
        Compte compte = saveCompteAutreComo(compteDTO);
        compte = compteRepository.save(compte);
        return compte;
    }

    public Compte saveCompteAutreComo(CompteDTO compteDTO) {
        Compte cpt = compteMapper.toEntity(compteDTO);
        CompteAutre compteAutre = new CompteAutre();
        compteAutre.setCodeFse(compteDTO.getCodeFse());
        compteAutre = compteAutreRepository.save(compteAutre);
        cpt.setCarte(null);
        cpt.setCompteAutre(compteAutre);
        return cpt;
    }

    public Personne checkExisteClient(Personne personne,String telephone,String codeTelephone){
        Personne personneExiste=personneRepository.findPersonneByPhoneAndCodePays(telephone, codeTelephone).get();
        Client clientExiste=clientRepository.findByPersonneIdAndSupprimeIsFalse(personneExiste.getId());
        if(clientExiste!=null && clientExiste.getId()!=null){
            List<CompteDTO> compteExiste=compteRepository.findByClientIdOptimise(clientExiste.getId());
            if(compteExiste.size()>0){
                throw new CustomParameterizedException("Ce numéro de téléphone est utilisé par un autre client");
            }
        }
        personneExiste.setActivated(personne.isActivated());
        personneExiste.setAdresse(personne.getAdresse());
        personneExiste.setEmail(personne.getEmail());
        personneExiste.setKeyHash(personne.getKeyHash());
        personneExiste.setTypePersonne(personne.getTypePersonne());
        personneExiste.setBrancheActivite(personne.getBrancheActivite());
        personneExiste.setClientSalaire(personne.getClientSalaire());
        personneExiste.setPaysCompte(personne.getPaysCompte());
        personneExiste.setModifiable(personne.isModifiable());
        personneExiste.setPersonneMorale(personne.getPersonneMorale());
        personneExiste.setPersonnePhysique(personne.getPersonnePhysique());
        personneExiste.setPersonnePhysique(personne.getPersonnePhysique());
        return personneExiste;
    }




}
