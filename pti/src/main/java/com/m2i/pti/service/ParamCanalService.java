package com.m2i.pti.service;

import com.m2i.pti.domain.ParamCanal;
import com.m2i.pti.repository.*;
import com.m2i.pti.service.dto.ParamCanalDTO;
import com.m2i.pti.service.mapper.ParamCanalMapper;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing Canal.
 */
@Service
@Transactional
public class ParamCanalService {

    private final Logger log = LoggerFactory.getLogger(ParamCanalService.class);

    private final ParamCanalRepository paramCanalRepository;
    private final ParamCanalMapper paramCanalMapper;

    public ParamCanalService(ParamCanalRepository paramCanalRepository,ParamCanalMapper paramCanalMapper) {
        this.paramCanalRepository = paramCanalRepository;
        this.paramCanalMapper = paramCanalMapper;
    }
public ParamCanalDTO save(ParamCanalDTO paramCanalDTO) {
            ParamCanal paramCanal = paramCanalMapper.toEntity(paramCanalDTO);
            paramCanal = paramCanalRepository.save(paramCanal);
            return paramCanalMapper.toDto(paramCanal);
    }

    public Page<ParamCanalDTO> getAllParamCanal(Pageable pageable) {
        List<ParamCanalDTO> result;
        result = paramCanalRepository.getAllParamCanalApplication();
        int start= (int) pageable.getOffset();
        int end=(start+pageable.getPageSize())>result.size()?result.size():(start+pageable.getPageSize());
        Page<ParamCanalDTO> retour=new PageImpl<ParamCanalDTO>(result.subList(start,end),pageable,result.size());
        return retour;
    } 

    public Page<ParamCanalDTO> getAllParamCanalByCanal(String canal,Pageable pageable) {
        List<ParamCanalDTO> result;
        result = paramCanalRepository.getAllParamCanalApplicationStatut(canal);
        int start= (int) pageable.getOffset();
        int end=(start+pageable.getPageSize())>result.size()?result.size():(start+pageable.getPageSize());
        Page<ParamCanalDTO> retour=new PageImpl<ParamCanalDTO>(result.subList(start,end),pageable,result.size());
        return retour;
    }

    @Transactional(readOnly = true)
    public ParamCanalDTO findOne(Long id) {
        log.debug("Request to get TypeService : {}", id);
        ParamCanal paramCanal = paramCanalRepository.findById(id).orElse(null);
        return paramCanalMapper.toDto(paramCanal);
    }
    
    public void delete(Long id) {
        log.debug("Request to delete TypeService : {}", id);
        paramCanalRepository.deleteById(id);
    }
}
