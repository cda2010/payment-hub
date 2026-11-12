package com.m2i.pti.service;

import java.time.Instant;
import java.time.ZoneOffset;

import com.m2i.pti.domain.*;

import com.m2i.pti.repository.*;
import com.m2i.pti.service.dto.LotDTO;
import com.m2i.pti.service.mapper.DetailsLotMapper;
import com.m2i.pti.service.mapper.LotMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LotService {

    private final Logger log = LoggerFactory.getLogger(LotService.class);

    private final LotRepository lotRepository;

    private final LotMapper lotMapper;

    private final DetailsLotService detailsLotService;
    private DetailsLotRepository detailsLotRepository;

    private final DetailsLotMapper detailsLotMapper;

    private final UniteOrganisationService uoService;

    public LotService(LotRepository lotRepository, LotMapper lotMapper, UniteOrganisationService uoService, DetailsLotService detailsLotService,
            DetailsLotMapper detailsLotMapper, DetailsLotRepository detailsLotRepository) {
        this.lotRepository = lotRepository;
        this.lotMapper = lotMapper;
        this.uoService = uoService;
        this.detailsLotService = detailsLotService;
        this.detailsLotMapper = detailsLotMapper;
        this.detailsLotRepository = detailsLotRepository;
    }

    public LotDTO save(LotDTO lotDTO) {
        log.debug("====== Request to save lot : {}", lotDTO);

        //Lot lot = lotMapper.toEntity(lotDTO);
        Lot lot = lotRepository.save(lotMapper.toEntity(lotDTO));

         
        if(lotDTO.getId() != null) {
            if (lotDTO.getClients().size() >= 1) {
                log.debug("not null");
                for (Client c : lotDTO.getClients()) {
                    DetailsLot existeDetail = detailsLotRepository.findDetailsLotByClientIdAndLotId(c.getId(), lot.getId());
                    if (existeDetail == null) {
                        DetailsLot d = new DetailsLot();
                        d.setLot(lot);
                        d.setClient(c);
                        log.debug("client **** { }"+d.getClient());
                        log.debug("lot **** { }"+d.getLot());
                        detailsLotRepository.saveAndFlush(d);
                        log.debug("d **** { }"+d);
                        //detailsLotService.save(detailsLotMapper.toDto(d));
                    }
                }
            } else if(lotDTO.getTypeActeur().equals("ORGANISATION") && lotDTO.getLigneChargementFichiers()!=null){
                for (LigneChargementFichier l:lotDTO.getLigneChargementFichiers()){
                    DetailsLot existeDetail=detailsLotRepository.findDetailsLotByLigneFichierIdAndLotId(l.getId(), lot.getId());
                    if(existeDetail==null){
                        DetailsLot d = new DetailsLot();
                        d.setLot(lot);
                        d.setLigneChargementFichier(l);
                        log.debug("client **** { }"+d.getClient());
                        log.debug("lot **** { }"+d.getLot());
                        detailsLotRepository.saveAndFlush(d);
                        log.debug("d **** { }"+d);
                    }
                }
            }
        }
 

        return lotMapper.toDto(lot);
    }

    public LotDTO setEtat(LotDTO lotDTO) {
        log.debug("Request to update lot : {}", lotDTO);
        lotDTO.setEtat(false);
        lotDTO.setDateFermeture(Instant.now());
        lotDTO.setNumero(lotDTO.getNumero());
        Lot result = lotRepository.save(lotMapper.toEntity(lotDTO));
        return lotMapper.toDto(result);
    }

    @Transactional(readOnly = true)
    public Page<Lot> findAll(Pageable pageable,String typeActeur) {
        log.debug("Request to get all lots");
        if(typeActeur!=null){
            log.debug("Request to get all lots with typeActeur"+typeActeur);
            return lotRepository.findByTypeActeur(typeActeur,pageable);
        } else {
            return lotRepository.findAll(pageable);
        }
        
    }

    @Transactional(readOnly = true)
    public LotDTO findOne(Long id) {
        log.debug("Request to get Lot : {}", id);
        Lot lot = lotRepository.findById(id).orElse(null);
        return lotMapper.toDto(lot);
    }

    public void delete(Long id) {
        log.debug("Request to delete Lot : {}", id);

        List<DetailsLot> listDetailsLots = detailsLotRepository.findDetailsLotByLotId(id);
        if (!listDetailsLots.isEmpty()) {
            for (DetailsLot d : listDetailsLots) {
                detailsLotRepository.deleteById(d.getId());
            }
        }

        lotRepository.deleteById(id);
    }

    public String getAlphaNumericString(int n) {
        byte[] array = new byte[256];
        new Random().nextBytes(array);
        String randomString = new String(array, Charset.forName("UTF-8"));
        StringBuffer r = new StringBuffer();

        for (int k = 0; k < randomString.length(); k++) {
            char ch = randomString.charAt(k);

            if (((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')) && (n > 0)) {
                r.append(ch);
                n--;
            }
        }
        return r.toString();
    }

}
