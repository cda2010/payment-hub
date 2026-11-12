package com.m2i.pti.service;

import java.io.ByteArrayOutputStream;

import com.m2i.pti.domain.*;
import com.m2i.pti.reporting.dto.XQrOrganisation;
import com.m2i.pti.repository.*;
import com.m2i.pti.service.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.m2i.pti.reporting.dto.XQrClient;
import com.m2i.pti.reporting.dto.XQrClientCode;
import com.m2i.pti.reporting.service.CryptageCodeQRService;
import com.m2i.pti.service.mapper.ClientMapper;
import com.m2i.pti.service.mapper.DetailsLotMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class DetailsLotService {

    private final Logger log = LoggerFactory.getLogger(DetailsLotService.class);

    private final DetailsLotRepository detailsLotRepository;

    private final DetailsLotMapper detailsLotMapper;

    private final ClientService clientService;

    private final ClientRepository clientRepository;

    private final CompteRepository compteRepository;

    private final PersonneRepository personneRepository;
    private final PersonnePhysiqueRepository personnePhysiqueRepository;
    private final NationaliteRepository nationaliteRepository;
    private LigneChargementFichierRepository ligneChargementFichierRepository;

    // private static final String PARAM_DATASOURCE = "datasource";
    private final CryptageCodeQRService cryptageCodeQRService;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;
    private final PointVenteRepository pointVenteRepository;
    CompteService compteService;
    private ClientMapper clientMapper;

    public DetailsLotService(ClientMapper clientMapper, DetailsLotRepository detailsLotRepository, DetailsLotMapper detailsLotMapper, ClientService clientService,
                             ClientRepository clientRepository, CompteRepository compteRepository, PersonneRepository personneRepository, PersonnePhysiqueRepository personnePhysiqueRepository, LigneChargementFichierRepository ligneChargementFichierRepository, NationaliteRepository nationaliteRepository, CryptageCodeQRService cryptageCodeQRService, UniteOrganisationRepository uniteOrganisationRepository, TypeUniteOrganisationRepository typeUniteOrganisationRepository, PointVenteRepository pointVenteRepository) {
        this.detailsLotRepository = detailsLotRepository;
        this.detailsLotMapper = detailsLotMapper;
        this.clientService = clientService;
        this.clientRepository = clientRepository;
        this.compteRepository = compteRepository;
        this.personneRepository = personneRepository;
        this.personnePhysiqueRepository = personnePhysiqueRepository;
        this.nationaliteRepository = nationaliteRepository;
        this.cryptageCodeQRService = cryptageCodeQRService;
        this.clientMapper = clientMapper;
        this.ligneChargementFichierRepository=ligneChargementFichierRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.pointVenteRepository = pointVenteRepository;
    }

    public DetailsLotDTO save(DetailsLotDTO detailsLotDTO) {

        log.debug("Request to save lot : {}", detailsLotDTO);
        DetailsLot detailsLot = detailsLotMapper.toEntity(detailsLotDTO);
        if (detailsLotDTO.getClientId() != null) {
            //detailsLot.setClient(detailsLotDTO.getClient());

            ClientDTO client = clientRepository.findClientByClientIdOptimise(detailsLotDTO.getClientId());
            Client cli = clientMapper.toEntity(client);
            //List<CompteDTO> listCpt = compteRepository.findByClientIdOptimise(client.getId());
            Compte compte = compteService.findComptePrincipal(cli);
            log.debug("comptes--------- { } " + compte);
            log.debug("client ---------{ } " + client);
            log.debug("detailsLot.getClient().getId()---------{ } " + detailsLot.getClient().getId());
           // Compte comptes[] = new Compte[n];
            //log.debug("compt --------- " + listCpt.toArray());
            //System.arraycopy(listCpt.toArray(), 0, comptes, 0, n);

            byte[] bytes = this.getQRCodeImage(qrClientlot(client, compte).getQrCodeData(), 300, 300);
            if (bytes != null && bytes.length > 0) {
                //detailsLot.setLot(detailsLotDTO.getLot());
                //detailsLot.setQrCode(bytes);
                detailsLot = detailsLotRepository.save(detailsLot);
                log.debug("finale lot : {}", detailsLot);
            }
            return detailsLotMapper.toDto(detailsLot);
        } else {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public Page<DetailsLot> findAll(Pageable pageable) {
        log.debug("Request to get all DetailsLots");
        return detailsLotRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public DetailsLotDTO findOne(Long id) {
        log.debug("Request to get DetailsLot : {}", id);
        DetailsLot dlot = detailsLotRepository.findById(id).orElse(null);
        return detailsLotMapper.toDto(dlot);
    }

    public void delete(Long id) {
        log.debug("Request to delete DetailsLot : {}", id);
        detailsLotRepository.deleteById(id);
    }

    public byte[] getQRCodeImage(String text, int width, int height) {

        byte[] pngData = null;
        try {

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

            pngData = pngOutputStream.toByteArray();
        } catch (Exception e) {
            // TODO: handle exception
            log.debug(e.getMessage());
        }
        return pngData;
    }

    public XQrClientCode qrClientlot(ClientDTO clientDTO, Compte cpt) {

        ObjectMapper mapper = new ObjectMapper();
        XQrClientCode xQrCode = new XQrClientCode();
        XQrClient xQrClient = new XQrClient();
        String json = null;
        Compte compte = null;
        //    Client client = clientService.findClient(idClient);
        log.debug("client------{ }"+clientDTO);
        if (clientDTO != null && cpt != null) {

            if (clientDTO.getCodeClient() != null) {
                xQrCode.setCodeClient(clientDTO.getCodeClient());
            }

            if (clientDTO.getPersonneId() != null) {
                PersonneDTO personneDTO = personneRepository.findPersonneById(clientDTO.getPersonneId());
                if(personneDTO!=null){
                    xQrClient.setTelephone(personneDTO.getTelephone());
                }
                if (personneDTO.getPersonnePhysiqueId() != null) {
                    PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                    if (personnePhysiqueDTO.getNomClient() != null) {
                        xQrClient.setNomClient(personnePhysiqueDTO.getNomClient());
                    }
                    if (personnePhysiqueDTO.getPrenomClient() != null) {
                        xQrClient.setPrenomClient(personnePhysiqueDTO.getPrenomClient());
                    }
                }
                if (personneDTO.getCodePaysTelephone() != null) {
                    if (personneDTO.getCodePaysTelephone() != null) {
                        xQrClient.setCodePays(personneDTO.getCodePaysTelephone());
                    }
                }
            }

            try {
                json = mapper.writeValueAsString(xQrClient);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            String secret = null, crypter = null, decrypt = null;
            crypter = cryptageCodeQRService.encrypt(json, secret);
            decrypt = cryptageCodeQRService.decrypt(crypter, secret);
            log.debug("===============================--------crypter------" + crypter);
            log.debug("======================================--------json------" + decrypt);
            xQrCode.setQrCodeData(crypter);


            xQrClient.setNumeroCompte(cpt.getValeurCompte());
        }
    
    return xQrCode ;
}
    public XQrClientCode xQrOrganisationCode(Long ligneChargementFichierId){
        ObjectMapper mapper = new ObjectMapper();
        XQrClientCode xQrCode = new XQrClientCode();
        XQrOrganisation xQrOrganisation = new XQrOrganisation();
        String json = null;
        UniteOrganisation agent=null;
        if(ligneChargementFichierId!=null){
            LigneChargementFichier ligneChargementFichier=ligneChargementFichierRepository.findById(ligneChargementFichierId).orElse(null);
            if(ligneChargementFichier!=null ){
                if(ligneChargementFichier.getIdentifiant()!=null){
                    agent=uniteOrganisationRepository.findByCodeUO(ligneChargementFichier.getIdentifiant());
                } else {
                    TypeUniteOrganisation typeUniteOrganisation=typeUniteOrganisationRepository.findByLibelleTypeUO(ligneChargementFichier.getType());
                    List<UniteOrganisation> Uo=uniteOrganisationRepository.findByLibelleUniteOrganisationAndTypeUOOrderByIdDesc(ligneChargementFichier.getNom(),typeUniteOrganisation);
                    if(Uo!=null&&Uo.size() >0){
                        agent=Uo.get(0);
                    }
                }
                if(agent!=null){
                    xQrOrganisation.setCodeMarchand(agent.getCodeUO());
                    xQrOrganisation.setLibelleUniteOrganisation(agent.getLibelleUniteOrganisation());
                    xQrOrganisation.setTelephone(agent.getTelephone());
                    List<PointVenteDTO> pv=pointVenteRepository.findByUniteOrganisationIdOptimise(agent.getId());
                    if(!pv.isEmpty()){
                        xQrOrganisation.setCodePV(pv.get(0).getCodePointVente());
                    }
                }
            }
        }
        try {
           json = mapper.writeValueAsString(xQrOrganisation);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String secret = null, crypter = null, decrypt = null;
        crypter = cryptageCodeQRService.encrypt(json, secret);
        decrypt = cryptageCodeQRService.decrypt(crypter, secret);
        log.debug("===============================--------crypter------" + crypter);
        log.debug("======================================--------json------" + decrypt);
        xQrCode.setQrCodeData(crypter);

        return xQrCode;
    }

    public List<DetailsLotDTO> findDetailsLotByLotId(Long id) {
        List<DetailsLotDTO> result=new ArrayList<>();
        List<DetailsLotDTO> listeDetails= detailsLotRepository.findDetailsLotByLotIdOptimiser(id);
        listeDetails.forEach(detailsLotDTO -> {
            if(detailsLotDTO.getClientId()!=null) {
                ClientDTO clientDTO = clientRepository.findClientByClientIdOptimise(detailsLotDTO.getClientId());
                if (clientDTO.getId() != null) {
                    detailsLotDTO.setCodeClient(clientDTO.getCodeClient());
                    if (clientDTO.getPersonneId() != null) {
                        PersonneDTO personneDTO = personneRepository.findPersonneById(clientDTO.getPersonneId());
                        if (personneDTO.getPersonnePhysiqueId() != null) {
                            PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                            if (personnePhysiqueDTO != null) {
                                detailsLotDTO.setNomClient(personnePhysiqueDTO.getNomClient());
                                detailsLotDTO.setPrenomClient(personnePhysiqueDTO.getPrenomClient());
                            }
                        }
                    }
                    result.add(detailsLotDTO);
                }
            } else if(detailsLotDTO.getLigneChargementFichierId()!=null){
                LigneChargementFichier ligneChargementFichier=ligneChargementFichierRepository.findById(detailsLotDTO.getLigneChargementFichierId()).orElse(null);
                if(ligneChargementFichier!=null){
                    detailsLotDTO.setType(ligneChargementFichier.getType());
                    if(ligneChargementFichier.getIdentifiant()!=null){
                        detailsLotDTO.setIdentifiant(ligneChargementFichier.getIdentifiant());
                        detailsLotDTO.setLibelle(ligneChargementFichier.getNom());
                    }else {
                        TypeUniteOrganisation typeUniteOrganisation=typeUniteOrganisationRepository.findByLibelleTypeUO(ligneChargementFichier.getType());
                        List<UniteOrganisation> Uo=uniteOrganisationRepository.findByLibelleUniteOrganisationAndTypeUOOrderByIdDesc(ligneChargementFichier.getNom(),typeUniteOrganisation);
                        if(Uo!=null&&Uo.size() >0){
                            detailsLotDTO.setIdentifiant(Uo.get(0).getCodeUO());
                            detailsLotDTO.setLibelle(Uo.get(0).getLibelleUniteOrganisation());

                        }
                    }
                    detailsLotDTO.setTelephone(ligneChargementFichier.getTelephone());
                    result.add(detailsLotDTO);
                }
            }

        });
        return result;
    }
}
