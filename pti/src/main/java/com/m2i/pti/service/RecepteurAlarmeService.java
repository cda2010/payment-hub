package com.m2i.pti.service;

import com.m2i.pti.domain.RecepteurAlarme;
import com.m2i.pti.domain.RecepteurAlarmeList;
import com.m2i.pti.repository.RecepteurAlarmeRepository;
import com.m2i.pti.service.dto.RecepteurAlarmeDTO;
import com.m2i.pti.service.mapper.RecepteurAlarmeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import org.springframework.scheduling.annotation.Async;


/**
 * Service Implementation for managing RecepteurAlarme.
 */
@Service
@Transactional
public class RecepteurAlarmeService {

    private final Logger log = LoggerFactory.getLogger(RecepteurAlarmeService.class);

    private final RecepteurAlarmeRepository recepteurAlarmeRepository;

    private final RecepteurAlarmeMapper recepteurAlarmeMapper;
ApiService apiService;
    public RecepteurAlarmeService(ApiService apiService,RecepteurAlarmeRepository recepteurAlarmeRepository, RecepteurAlarmeMapper recepteurAlarmeMapper) {
        this.recepteurAlarmeRepository = recepteurAlarmeRepository;
        this.recepteurAlarmeMapper = recepteurAlarmeMapper;
        this.apiService=apiService;
    }

    /**,
     * Save a recepteurAlarme.
     *
     * @param recepteurAlarmeDTO the entity to save
     * @return the persisted entity
     */
    public RecepteurAlarmeDTO save(RecepteurAlarmeDTO recepteurAlarmeDTO) {
        log.debug("Request to save RecepteurAlarme : {}", recepteurAlarmeDTO);
        RecepteurAlarme recepteurAlarme = recepteurAlarmeMapper.toEntity(recepteurAlarmeDTO);
        recepteurAlarme = recepteurAlarmeRepository.save(recepteurAlarme);
        return recepteurAlarmeMapper.toDto(recepteurAlarme);
    }

    /**
     *  Get all the recepteurAlarmes.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<RecepteurAlarmeDTO> findAll(String etiquette ,String sms ,Pageable pageable) {
        log.debug("Request to get all RecepteurAlarmes");
        List<RecepteurAlarmeDTO> alarmes= new ArrayList<>();
        alarmes = recepteurAlarmeMapper.toDto(recepteurAlarmeRepository.findAlarmeByCriterial(etiquette,sms));
        int start= (int) pageable.getOffset();
        int end=(start+pageable.getPageSize())>alarmes.size()?alarmes.size():(start+pageable.getPageSize());
        Page<RecepteurAlarmeDTO> retour = new PageImpl<>(alarmes.subList(start,end),pageable,alarmes.size());
        return retour;
        /*return recepteurAlarmeRepository.findAll(pageable)
            .map(recepteurAlarmeMapper::toDto);*/
    }

    /**
     *  Get one recepteurAlarme by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public RecepteurAlarmeDTO findOne(Long id) {
        log.debug("Request to get RecepteurAlarme : {}", id);
        RecepteurAlarme recepteurAlarme = recepteurAlarmeRepository.findById(id).orElse(null);
        return recepteurAlarmeMapper.toDto(recepteurAlarme);
    }

    /**
     *  Delete the  recepteurAlarme by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete RecepteurAlarme : {}", id);
        recepteurAlarmeRepository.deleteById(id);
    }

    
    @Async("taskExecutor")
    public void sendAlarme(String message,String etiquette){
      RecepteurAlarmeList recepteurAlarmeList =findListByEtiquette(etiquette);
      if (recepteurAlarmeList!=null){
          if (recepteurAlarmeList.getEmails()!=null){
              for (String email :recepteurAlarmeList.getEmails()){
                  apiService.sendMail(email, message);
                }
        }
       if (recepteurAlarmeList.getSms()!=null){
              for (String numero :recepteurAlarmeList.getSms()){
                  apiService.sendMessage(numero, message,"");
                }
        }
      }
        
}
    public RecepteurAlarmeList findListByEtiquette(String etiquette) {
        RecepteurAlarmeList recepteurAlarmeList=new RecepteurAlarmeList();
        RecepteurAlarme recepteurAlarme=recepteurAlarmeRepository.findByEtiquette(etiquette);
        if(recepteurAlarme!=null){
            List<String> emails=new ArrayList<>();
            List<String> sms=new ArrayList<>();
            if(recepteurAlarme.getEmails()!=null){
                String[] tableauEmails= recepteurAlarme.getEmails().split(",");
                for (String email :tableauEmails){
                    emails.add(email);
                }
            }
            if(recepteurAlarme.getSms()!=null){
                String[] tableauEmails= recepteurAlarme.getSms().split(",");
                for (String sm :tableauEmails){
                    sms.add(sm);
                }
            }
            recepteurAlarmeList.setEmails(emails);
            recepteurAlarmeList.setSms(sms);
            return recepteurAlarmeList;
        };
        return null;
    }
}
