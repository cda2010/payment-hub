package com.m2i.pti.service;

import java.time.ZonedDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.m2i.pti.domain.BackupData;
import com.m2i.pti.repository.BackupDataRepository;
import com.m2i.pti.service.dto.BackupDataDTO;
import com.m2i.pti.service.mapper.BackupDataMapper;

@Service
@Transactional
public class BackupDataService {
    private final Logger log = LoggerFactory.getLogger(BackupDataService.class);

    private final BackupDataRepository backupDataRepository;
  
    private final BackupDataMapper backupDataMapper;
  
    @Autowired
    public BackupDataService(
        BackupDataRepository backupDataRepository,
        BackupDataMapper backupDataMapper
    ) {
      this.backupDataRepository = backupDataRepository;
      this.backupDataMapper = backupDataMapper;
    }

     /**
   * Save a etapeService.
   *
   * @param typeServiceDTO the entity to save
   * @return the persisted entity
   */
  public BackupDataDTO save(BackupDataDTO backupDataDTO) {
    log.debug("Request to save BackupData : {}", backupDataDTO);
      BackupData etapeService = backupDataMapper.toEntity(backupDataDTO);
      etapeService = backupDataRepository.save(etapeService);
      return backupDataMapper.toDto(etapeService);
    }

    @Transactional(readOnly = true)
    public Page<BackupDataDTO> findAllBackupByDate(ZonedDateTime dateUpdate, Pageable pageable) {
        log.debug("Request to get all Notifications"+dateUpdate);
        List<BackupDataDTO> result;
        if(dateUpdate !=null){
          result= backupDataRepository.findAllBackupSpeed(dateUpdate,dateUpdate.plusHours(23));
        }else{
          result= backupDataRepository.findAllBackupSpeed(null,null);
        }
          int start= (int) pageable.getOffset();
          int end=(start+pageable.getPageSize())>result.size()?result.size():(start+pageable.getPageSize());
          return new PageImpl<>(result.subList(start,end),pageable,result.size());
      }
  }
