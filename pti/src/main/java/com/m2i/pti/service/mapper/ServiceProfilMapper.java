package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.ServiceProfilDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity ServiceProfil and its DTO ServiceProfilDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeServiceMapper.class})
public interface ServiceProfilMapper extends EntityMapper<ServiceProfilDTO, ServiceProfil> {

    

    

    default ServiceProfil fromId(Long id) {
        if (id == null) {
            return null;
        }
        ServiceProfil serviceProfil = new ServiceProfil();
        serviceProfil.setId(id);
        return serviceProfil;
    }
}
