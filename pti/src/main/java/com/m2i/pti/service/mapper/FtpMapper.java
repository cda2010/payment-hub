package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.Ftp;
import com.m2i.pti.service.dto.FtpDTO;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {})
public interface FtpMapper extends EntityMapper<FtpDTO, Ftp>{

    FtpDTO toDto(Ftp ftp);


    Ftp toEntity(FtpDTO ftpDTO);

    default Ftp fromId(Long id) {
        if (id == null) {
            return null;
        }
        Ftp ftp = new Ftp();
        ftp.setId(id);
        return ftp;
    }
    
}
