package com.m2i.pti.service;

import com.m2i.pti.domain.Client;
import com.m2i.pti.domain.OperationSuivis;
import com.m2i.pti.repository.OperationSuivisRepository;
import com.m2i.pti.service.dto.OperationSuivisDTO;
import com.m2i.pti.service.mapper.OperationSuivisMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


/**
 * Service Implementation for managing OperationSuivis.
 */
@Service
@Transactional
public class OperationSuivisService {

    private final Logger log = LoggerFactory.getLogger(OperationSuivisService.class);

    private final OperationSuivisRepository operationSuivisRepository;

    private final OperationSuivisMapper operationSuivisMapper;

    public OperationSuivisService(OperationSuivisRepository operationSuivisRepository, OperationSuivisMapper operationSuivisMapper) {
        this.operationSuivisRepository = operationSuivisRepository;
        this.operationSuivisMapper = operationSuivisMapper;
    }

    /**
     * Save a operationSuivis.
     *
     * @param operationSuivisDTO the entity to save
     * @return the persisted entity
     */
    public OperationSuivisDTO save(OperationSuivisDTO operationSuivisDTO) {
        log.debug("Request to save OperationSuivis : {}", operationSuivisDTO);
        OperationSuivis operationSuivis = operationSuivisMapper.toEntity(operationSuivisDTO);
        operationSuivis = operationSuivisRepository.save(operationSuivis);
        return operationSuivisMapper.toDto(operationSuivis);
    }
 public OperationSuivis save(OperationSuivis operationSuivisDTO) {
        log.debug("Request to save OperationSuivis : {}", operationSuivisDTO);
       
      OperationSuivis  operationSuivis = operationSuivisRepository.save(operationSuivisDTO);
        return operationSuivis;
    }
    /**
     *  Get all the operationSuivis.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<OperationSuivisDTO> findAll(Pageable pageable) {
        log.debug("Request to get all OperationSuivis");
        return operationSuivisRepository.findAll(pageable)
            .map(operationSuivisMapper::toDto);
    }

    /**
     *  Get one operationSuivis by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public OperationSuivisDTO findOne(Long id) {
        log.debug("Request to get OperationSuivis : {}", id);
        OperationSuivis operationSuivis = operationSuivisRepository.findById(id).orElse(null);
        return operationSuivisMapper.toDto(operationSuivis);
    }

    /**
     *  Delete the  operationSuivis by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete OperationSuivis : {}", id);
        operationSuivisRepository.deleteById(id);
    }
    public List<Client> findByNom(String nom){
        return operationSuivisRepository.findByNom(nom);
    }
    public List<Client> findByPrenom(String prenom){
        return operationSuivisRepository.findByPrenom(prenom);
    }
    public List<Client> findByCodeAndNom(String code, String nom){
        return operationSuivisRepository.findByCodeAndNom(code,nom);
    }
    public List<Client> findByTelAndNom(String tel, String nom){
        return operationSuivisRepository.findByTelAndNom(tel,nom);
    }
    public List<Client> findByCodeAndTelAndNom(String code, String tel, String nom){
        return operationSuivisRepository.findByCodeAndTelAndNom(code,tel,nom);
    }
    public Page<OperationSuivis> findOperaSuivisByClient(Long id,Pageable pageable){
        return operationSuivisRepository.findOperaSuivisByClient(id,pageable);
    }
}
