package com.m2i.pti.service;

import com.m2i.pti.domain.CanalCommunication;
import com.m2i.pti.domain.Facturier;
import com.m2i.pti.domain.Ftp;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.RecepteurEmail;
import com.m2i.pti.repository.CanalCommunicationRepository;
import com.m2i.pti.repository.ClassificationFacturierRepository;
import com.m2i.pti.repository.FacturierRepository;
import com.m2i.pti.repository.FtpRepository;
import com.m2i.pti.repository.RecepteurEmailRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.dto.FacturierDTO;
import com.m2i.pti.service.dto.RecepteurEmailDTO;
import com.m2i.pti.service.mapper.FacturierMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class FacturierService {
    private final Logger log= LoggerFactory.getLogger(FacturierService.class);
    private final FacturierRepository facturierRepository;
    private final FacturierMapper facturierMapper;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    private final ClassificationFacturierRepository classificationFacturierRepository;
    private final FtpRepository ftpRepository;
    private final RecepteurEmailRepository recepteurEmailRepository;
    private final CanalCommunicationRepository canalCommunicationrRepository;

    public FacturierService(FacturierRepository facturierRepository,FacturierMapper facturierMapper,UniteOrganisationRepository uniteOrganisationRepository,
        ClassificationFacturierRepository classificationFacturierRepository,FtpRepository ftpRepository,RecepteurEmailRepository recepteurEmailRepository,
        CanalCommunicationRepository canalCommunicationrRepository) {
        this.facturierRepository = facturierRepository;
        this.facturierMapper=facturierMapper;
        this.uniteOrganisationRepository=uniteOrganisationRepository;
        this.classificationFacturierRepository=classificationFacturierRepository;
        this.ftpRepository=ftpRepository;
        this.recepteurEmailRepository=recepteurEmailRepository;
        this.canalCommunicationrRepository=canalCommunicationrRepository;
    }
    @Transactional(readOnly = true)
    public List<Facturier> getListeFacturier(String nomFacturier, String codeFacturier,
                                             Long classification_id, Long typeFacturier,
                                             Long possesseur_id) {
        log.debug("requet to get all facturier"+nomFacturier+codeFacturier+classification_id+typeFacturier+possesseur_id);
        List<Facturier> list = new ArrayList<>();
        list=facturierRepository.findFacturierByCriteria(nomFacturier,codeFacturier,classification_id,typeFacturier,possesseur_id);

        return list;
    }

    @Transactional(readOnly = true)
    public Page<Facturier> findAll(Pageable pageable) {
        log.debug("Request to get all facturier");
        return facturierRepository.findAll(pageable);
    }

    @Transactional
    public Facturier save(FacturierDTO facturierDTO){

        Facturier facturier= new Facturier();
        CanalCommunication canalCommunication=new CanalCommunication();
        // facturier=facturierMapper.toEntity(facturierDTO);
        facturier.setActivated(true);
        facturier.setClassification(classificationFacturierRepository.findById(facturierDTO.getClassification().getId()).orElse(null));
        facturier.setUniteOrganisation(facturierDTO.getUniteOrganisation());
        facturier.setPossesseur(facturierDTO.getPossesseur());
        facturier.setCodeFacturier(facturierDTO.getCodeFacturier());
        facturier.setTypeFacturier(facturierDTO.getTypeFacturier());
        facturier.setSecteurActivite(facturierDTO.getSecteurActivite());
        facturier.setFse(facturierDTO.getFse());
        facturier.setValeur(facturierDTO.getValeur());
        facturier.setValeurContentType(facturierDTO.getValeurContentType());
        facturier.setNomFacturier(facturierDTO.getNomFacturier());

        if(facturier.getUniteOrganisation()!=null){
            facturier.setNomFacturier(uniteOrganisationRepository.findById(facturier.getUniteOrganisation().getId()).orElse(null).getLibelleUniteOrganisation());
            //facturier = facturierRepository.save(facturier);
        }
        if(facturierDTO.getRecepteurEmailDTO()!=null){
            RecepteurEmail recepteurEmail=new RecepteurEmail();
            recepteurEmail.setEmail(facturierDTO.getRecepteurEmailDTO().getEmail());
            recepteurEmailRepository.save(recepteurEmail);
            canalCommunication.setRecepteurEmail(recepteurEmail);
        }
        if(facturierDTO.getFtpDTO()!=null){
            Ftp ftp = new Ftp();
            ftp.setUrl(facturierDTO.getFtpDTO().getUrl());
            ftp.setUsername(facturierDTO.getFtpDTO().getUsername());
            ftp.setPassword(facturierDTO.getFtpDTO().getPassword());
            ftp.setRepertoire(facturierDTO.getFtpDTO().getRepertoire());
            ftpRepository.save(ftp);
            canalCommunication.setFtp(ftp);
        }
        facturier = facturierRepository.save(facturier);
        canalCommunication.setFacturier(facturier);
        canalCommunicationrRepository.save(canalCommunication);

        return facturier;

    }

    @Transactional
    public Facturier update(FacturierDTO facturierDTO){

        CanalCommunication canalCommunication = canalCommunicationrRepository.getCCByIdFacturier(facturierDTO.getId());
        Facturier facturier = facturierRepository.findById(facturierDTO.getId()).orElse(null);

//        facturier.setActivated(facturierDTO.isActivated());
//        System.out.println("--------MMMM-"+facturierDTO.toString());
//        facturier.setClassification(classificationFacturierRepository.findById(facturier.getClassification().getId()).orElse(null));
//        facturier.setUniteOrganisation(facturierDTO.getUniteOrganisation());
//        facturier.setPossesseur(facturierDTO.getPossesseur());
//        facturier.setCodeFacturier(facturierDTO.getCodeFacturier());
//        facturier.setTypeFacturier(facturierDTO.getTypeFacturier());
//        facturier.setSecteurActivite(facturierDTO.getSecteurActivite());
//        facturier.setFse(facturierDTO.getFse());
//        facturier.setValeur(facturierDTO.getValeur());
//        facturier.setValeurContentType(facturierDTO.getValeurContentType());
//        facturier.setNomFacturier(facturierDTO.getNomFacturier());

        if (facturierDTO.getClassificationId() != null) {
            facturier.setClassification(classificationFacturierRepository.findById(facturierDTO.getClassificationId()).orElse(null));
        }
        if (facturierDTO.getClassification()!=null && facturierDTO.getClassification().getId() != null) {
            facturier.setClassification(classificationFacturierRepository.findById(facturierDTO.getClassification().getId()).orElse(null));
        }
        if (facturierDTO.getPossesseurId() != null) {
            UniteOrganisation proc = uniteOrganisationRepository.findById(facturierDTO.getPossesseurId()).orElse(null);
            facturier.setPossesseur(proc);
        }
        if (facturierDTO.getPossesseur()!=null && facturierDTO.getPossesseur().getId() != null) {
            facturier.setPossesseur(uniteOrganisationRepository.findById(facturierDTO.getPossesseur().getId()).orElse(null));
        }
        if (facturierDTO.getUniteOrganisationId() != null) {
            facturier.setUniteOrganisation(uniteOrganisationRepository.findById(facturierDTO.getUniteOrganisationId()).orElse(null));
        }
        if (facturierDTO.getUniteOrganisation()!=null && facturierDTO.getUniteOrganisation().getId() != null) {
            facturier.setUniteOrganisation(uniteOrganisationRepository.findById(facturierDTO.getUniteOrganisation().getId()).orElse(null));
        }
        facturier.setCodeFacturier(facturierDTO.getCodeFacturier());
        facturier.setTypeFacturier(facturierDTO.getTypeFacturier());
        facturier.setSecteurActivite(facturierDTO.getSecteurActivite());
        facturier.setFse(facturierDTO.getFse());
        if (facturierDTO.getValeur() == null) {
            facturier.setValeur(facturier.getValeur());
            facturier.setValeurContentType(facturier.getValeurContentType());
        } else {
            facturier.setValeur(facturierDTO.getValeur());
            facturier.setValeurContentType(facturierDTO.getValeurContentType());
        }
        facturier.setNomFacturier(facturierDTO.getNomFacturier());

        if(facturier.getUniteOrganisation()!=null){
            facturier.setNomFacturier(uniteOrganisationRepository.findById(facturier.getUniteOrganisation().getId()).orElse(null).getLibelleUniteOrganisation());
        }
        facturier.setActivated(facturierDTO.isActivated());

        if(canalCommunication!=null){
            Long idRecepteurEmail = canalCommunicationrRepository.getIdRecepteurEmailByIdFacturier(facturierDTO.getId());
            Long idFtp = canalCommunicationrRepository.getIdFtpByIdFacturier(facturierDTO.getId());

            if(facturierDTO.getRecepteurEmailDTO()!=null) {

                if(idRecepteurEmail!=null){
                    RecepteurEmail email = recepteurEmailRepository.findById(idRecepteurEmail).orElse(null);
                    email.setEmail(facturierDTO.getRecepteurEmailDTO().getEmail());
                    recepteurEmailRepository.save(email);
                    canalCommunication.setRecepteurEmail(email);
                }
                else {
                    RecepteurEmail email = new RecepteurEmail();
                    email.setEmail(facturierDTO.getRecepteurEmailDTO().getEmail());
                    recepteurEmailRepository.save(email);
                    canalCommunication.setRecepteurEmail(email);
                }
            }

            if(facturierDTO.getFtpDTO()!=null) {

                if(idFtp!=null) {
                    Ftp ftp = ftpRepository.findById(idFtp).orElse(null);
                    ftp.setUrl(facturierDTO.getFtpDTO().getUrl());
                    ftp.setUsername(facturierDTO.getFtpDTO().getUsername());
                    ftp.setPassword(facturierDTO.getFtpDTO().getPassword());
                    ftp.setRepertoire(facturierDTO.getFtpDTO().getRepertoire());
                    ftpRepository.save(ftp);
                    canalCommunication.setFtp(ftp);
                }
                else {
                    Ftp ftp = new Ftp();
                    ftp.setUrl(facturierDTO.getFtpDTO().getUrl());
                    ftp.setUsername(facturierDTO.getFtpDTO().getUsername());
                    ftp.setPassword(facturierDTO.getFtpDTO().getPassword());
                    ftp.setRepertoire(facturierDTO.getFtpDTO().getRepertoire());
                    ftpRepository.save(ftp);
                    canalCommunication.setFtp(ftp);
                }
            }
            canalCommunication.setFacturier(facturier);
            canalCommunicationrRepository.save(canalCommunication);
        }
        else {
            CanalCommunication cc = new CanalCommunication();
            if(facturierDTO.getRecepteurEmailDTO()!=null){
                RecepteurEmail email = new RecepteurEmail();
                email.setEmail(facturierDTO.getRecepteurEmailDTO().getEmail());
                recepteurEmailRepository.save(email);
                cc.setRecepteurEmail(email);
            }
            if(facturierDTO.getFtpDTO()!=null){
                Ftp ftp = new Ftp();
                ftp.setUrl(facturierDTO.getFtpDTO().getUrl());
                ftp.setUsername(facturierDTO.getFtpDTO().getUsername());
                ftp.setPassword(facturierDTO.getFtpDTO().getPassword());
                ftp.setRepertoire(facturierDTO.getFtpDTO().getRepertoire());
                ftpRepository.save(ftp);
                cc.setFtp(ftp);
            }
            cc.setFacturier(facturier);
            canalCommunicationrRepository.save(cc);
        }

        facturier = facturierRepository.save(facturier);

        return facturier;
    }
    @Transactional(readOnly = true)
    public Page<FacturierDTO> findAllOptimiser(Pageable pageable) {
        log.debug("Request to get all facturier");
        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        int totalFacturier = 0;
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
        totalFacturier=facturierRepository.countAllFacturier();
        List<FacturierDTO> listeFacturier=facturierRepository.findAllFacturierOptimiser(taille, page);
        Page<FacturierDTO> retour = new PageImpl<>(listeFacturier, pageable1, totalFacturier);
        return retour;
    }
}
