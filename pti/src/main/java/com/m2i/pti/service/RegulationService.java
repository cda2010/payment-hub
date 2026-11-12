package com.m2i.pti.service;

import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.domain.enumeration.TypeServiceLibelle;
import com.m2i.pti.repository.BeneficiaireRepository;
import com.m2i.pti.repository.CompteRepository;
import com.m2i.pti.repository.EmetteurRepository;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.mapper.OperationMapper;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@Transactional
public class RegulationService {

    private final Logger log = LoggerFactory.getLogger(OperationService.class);
    private final CompteRepository compteRepository;
    private final CompteService compteService;
    private final PersonneService personneService;
    private final ClientService clientService;
    private final TypeServiceService typeServiceService;
    private final OperationRepository operationRepository;
    private final OperationMapper operationMapper;
    private final EmetteurRepository emetteurRepository;
    private final BeneficiaireRepository beneficiaireRepository;
    private final CompteAutreService compteAutreService;
    private final UniteOrganisationService uniteOrganisationService;
    private final UserService userService;
    private final OperationService operationService;

    public RegulationService(CompteRepository compteRepository, CompteService compteService, PersonneService personneService, ClientService clientService, TypeServiceService typeServiceService, OperationRepository operationRepository, OperationMapper operationMapper, EmetteurRepository emetteurRepository, BeneficiaireRepository beneficiaireRepository, CompteAutreService compteAutreService, UniteOrganisationService uniteOrganisationService, UserService userService, OperationService operationService) {
        this.compteRepository = compteRepository;
        this.compteService = compteService;
        this.personneService = personneService;
        this.clientService = clientService;
        this.typeServiceService = typeServiceService;
        this.operationRepository = operationRepository;
        this.operationMapper = operationMapper;
        this.emetteurRepository = emetteurRepository;
        this.beneficiaireRepository = beneficiaireRepository;
        this.compteAutreService = compteAutreService;
        this.uniteOrganisationService = uniteOrganisationService;
        this.userService = userService;
        this.operationService = operationService;
    }

    @Transactional
    public OperationDTO doOperationCompteACompte(OperationAllDTO operationAllDTO, String typeSrvc) {
        log.debug("----------début du service de régulation----------" + operationAllDTO.getOperationDTO());
        OperationDTO operationDTO = new OperationDTO();
        operationDTO = operationAllDTO.getOperationDTO();
        CompteDTO cptD = new CompteDTO();
        CompteDTO cptS = new CompteDTO();
        Client client = new Client();
        Emetteur emetteur = new Emetteur();
        Beneficiaire beneficiaire = new Beneficiaire();
        cptS = compteService.findOne(operationDTO.getCompteSrcId());
        cptD = compteService.findOne(operationDTO.getCompteDestId());
        TypeService typeService = typeServiceService.findByCodeService(String.valueOf(TypeServiceLibelle.REGULATION));
        System.out.println("--service--" + typeService);

        User userConnecte = userService.getUserWithAuthorities();
        PointVente pointVente = userConnecte.getPointVente();
        if (pointVente != null) {
            if (cptS != null) {
                if (cptS.getClientId() != null) {
                    client = clientService.findClient(cptS.getClientId());
                    if (client != null) {
                        if (client.getPersonne() != null) {
                            emetteur = emetteurRepository.findByPersonneId(client.getPersonne().getId());
                            if (emetteur != null) {
                                operationDTO.setEmetteurId(emetteur.getId());
                            } else {
                                Emetteur eme = new Emetteur();
                                eme.setPersonne(client.getPersonne());
                                emetteurRepository.save(eme);
                                operationDTO.setEmetteurId(eme.getId());
                            }
                        }
                        if (cptS.getCompteAutreId() != null) {
                            CompteAutreDTO c = compteAutreService.findOne(cptS.getCompteAutreId());
                            if (c.getCodeFse() != null) {
                                UniteOrganisation uoE = uniteOrganisationService.findFseByCode(c.getCodeFse());
                                if (uoE != null) {
                                    operationDTO.setUoEmetteurId(uoE.getId());
                                }
                            }
                        }
                    }
                }
                if (cptS.getUniteOrganisationId() != null) {
                    if (cptS.getCompteAutreId() != null) {

                        CompteAutreDTO c = compteAutreService.findOne(cptS.getCompteAutreId());
                        if (c.getCodeFse() != null) {
                            UniteOrganisation uoE = uniteOrganisationService.findFseByCode(c.getCodeFse());
                            if (uoE != null) {
                                operationDTO.setUoEmetteurId(uoE.getId());
                            }
                        } else {
                            operationDTO.setUoEmetteurId(cptS.getUniteOrganisationId());
                        }
                    }
                }
            }
            if (cptD != null) {
                if (cptD.getClientId() != null) {
                    client = clientService.findClient(cptD.getClientId());
                    if (client != null) {
                        if (client.getPersonne() != null) {
                            beneficiaire = beneficiaireRepository.findByPersonneId(client.getPersonne().getId());
                            if (beneficiaire != null) {
                                operationDTO.setBeneficaireId(beneficiaire.getId());
                            } else {
                                Beneficiaire bene = new Beneficiaire();
                                bene.setPersonne(client.getPersonne());
                                beneficiaireRepository.save(bene);
                                operationDTO.setBeneficaireId(bene.getId());
                            }
                        }
                        if (cptD.getCompteAutreId() != null) {
                            CompteAutreDTO c = compteAutreService.findOne(cptD.getCompteAutreId());
                            if (c.getCodeFse() != null) {
                                UniteOrganisation uoAcq = uniteOrganisationService.findFseByCode(c.getCodeFse());
                                if (uoAcq != null) {
                                    operationDTO.setUoAcquereurId(uoAcq.getId());
                                }
                            }
                        }
                    }

                }
                if (cptD.getUniteOrganisationId() != null) {
                    if (cptD.getCompteAutreId() != null) {
                        CompteAutreDTO cD = compteAutreService.findOne(cptD.getCompteAutreId());
                        if (cD.getCodeFse() != null) {
                            UniteOrganisation uoA = uniteOrganisationService.findFseByCode(cD.getCodeFse());
                            if (uoA != null) {
                                operationDTO.setUoAcquereurId(uoA.getId());
                            }
                        } else {
                            operationDTO.setUoAcquereurId(cptD.getUniteOrganisationId());
                        }
                    }
                }
            }
            operationDTO.setCommentaire(operationDTO.getCommentaire());
            operationDTO.setMotif(operationDTO.getMotif());
            operationDTO.setTypeServiceId(typeService.getId());
            operationDTO.setDateOperation(ZonedDateTime.now());
            operationDTO.setCodeOperation(RandomUtil.getCodeOperation(operationDTO.getDateOperation(), pointVente.getUniteOrganisation().getCodeUO()));
            operationDTO.setStatut(StatutOperation.ENATTENTE_VALIDATION);
            operationDTO.setPointVenteId(pointVente.getId());
            operationDTO.setUserId(userConnecte.getId());
            operationRepository.save(operationMapper.toEntity(operationDTO));

            return operationDTO;
        }
        log.debug("----------fin du service de régulation----------" + operationDTO);
        throw new CustomParameterizedException("l'utulisateur n'a pas de point de vente.");

    }
}
