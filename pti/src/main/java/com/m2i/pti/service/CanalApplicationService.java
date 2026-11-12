package com.m2i.pti.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m2i.pti.domain.CanalApplication;
import com.m2i.pti.repository.CanalApplicationRepository;
import com.m2i.pti.service.dto.CanalApplicationDTO;
import com.m2i.pti.service.mapper.CanalApplicationMapper;

/**
 * Service Implementation for managing Canal.
 */
@Service
@Transactional
public class CanalApplicationService {

    private final Logger log = LoggerFactory.getLogger(CanalApplicationService.class);

    private final CanalApplicationRepository canalApplicationRepository;
    private final CanalApplicationMapper canalApplicationMapper;
    
    public CanalApplicationService(CanalApplicationRepository canalApplicationRepository, CanalApplicationMapper canalApplicationMapper) {
        this.canalApplicationRepository = canalApplicationRepository;
        this.canalApplicationMapper = canalApplicationMapper;
    }

    public CanalApplicationDTO save(CanalApplicationDTO canalApplicationDTO) {
            CanalApplication canalApplication = canalApplicationMapper.toEntity(canalApplicationDTO);
            canalApplication = canalApplicationRepository.save(canalApplication);
            return canalApplicationMapper.toDto(canalApplication);
          
    }

    public Page<CanalApplicationDTO> getAllCanalApplication(Pageable pageable) {
        List<CanalApplicationDTO> result;
        result = canalApplicationRepository.getAllCanalApplication();
        int start= (int) pageable.getOffset();
        int end=(start+pageable.getPageSize())>result.size()?result.size():(start+pageable.getPageSize());
        Page<CanalApplicationDTO> retour=new PageImpl<CanalApplicationDTO>(result.subList(start,end),pageable,result.size());
        return retour;
    } 

    public Page<CanalApplicationDTO> getAllCanalApplicationByCanal(String canal,Pageable pageable) {
        List<CanalApplicationDTO> result;
        result = canalApplicationRepository.getAllCanalApplicationStatut(canal);
        int start= (int) pageable.getOffset();
        int end=(start+pageable.getPageSize())>result.size()?result.size():(start+pageable.getPageSize());
        Page<CanalApplicationDTO> retour=new PageImpl<CanalApplicationDTO>(result.subList(start,end),pageable,result.size());
        return retour;
    }

    @Transactional(readOnly = true)
    public CanalApplicationDTO findOne(Long id) {
        CanalApplication canalApplication = canalApplicationRepository.findById(id).orElse(null);
        return canalApplicationMapper.toDto(canalApplication);
    }

    public void delete(Long id) {
        canalApplicationRepository.deleteById(id);
    }
}
