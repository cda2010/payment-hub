package com.m2i.pti.service;

import com.m2i.pti.domain.InfoSupp;
import com.m2i.pti.repository.InfosuppRepository;
import com.m2i.pti.service.dto.InfosuppDTO;
import com.m2i.pti.service.mapper.InfosuppMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class InfosuppService {

    private final Logger log = LoggerFactory.getLogger(InfoSupp.class);
    private final InfosuppRepository repository;
    private final InfosuppMapper infosuppMapper;

    public InfosuppService(InfosuppRepository repository,InfosuppMapper infosuppmapper) {
        this.repository = repository;
        this.infosuppMapper = infosuppmapper ;
    }

    public InfosuppDTO save(InfosuppDTO infosuppDto) {
        log.debug("Request to save infosupp : {}", infosuppDto);
        InfoSupp entity = infosuppMapper.toEntity(infosuppDto);
        return infosuppMapper.toDto(repository.save(entity));
    }

    public void deleteById(Long id) {
        log.debug("Request to delete Emetteur : {}", id);
        repository.deleteById(id);
    }

    public InfosuppDTO findById(Long id) {
        log.debug("Request to get infosupp : {}", id);
        InfoSupp infosupp = repository.findById(id).orElse(null);
        return infosuppMapper.toDto(infosupp);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<InfosuppDTO> findAll(Pageable pageable) {
        log.debug("Request to get all infosupp");
        return repository.findAll(pageable)
                .map(infosuppMapper::toDto);
    }

//    public Page<InfosuppDto> findByCondition(InfosuppDto infosuppDto, Pageable pageable) {
//        Page<Infosupp> entityPage = repository.findAll(pageable);
//        List<Infosupp> entities = entityPage.getContent();
//        return new PageImpl<>(infosuppMapper.toDto(entities), pageable, entityPage.getTotalElements());
//    }

    public InfosuppDTO update(InfosuppDTO infosuppDto, Long id) {
        InfosuppDTO data = findById(id);
        InfoSupp entity = infosuppMapper.toEntity(infosuppDto);
        return save(infosuppMapper.toDto(entity));
    }

    public String getInfoLibelle(Long id) {
        return repository.getInfoLibelle(id) ;
    }
}
