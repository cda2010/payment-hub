package com.m2i.pti.service.mapper;

import org.mapstruct.Mapper;
import com.m2i.pti.domain.BackupData;
import com.m2i.pti.service.dto.BackupDataDTO;

@Mapper(componentModel = "spring", uses = {}) 
public interface BackupDataMapper extends EntityMapper<BackupDataDTO, BackupData>
{
    BackupDataDTO toDto(BackupData backupData);
    
    BackupData toEntity(BackupDataDTO backupDataDTO);
    
}  

