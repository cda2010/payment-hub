package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.RepartitionFraisDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity RepartitionFrais and its DTO RepartitionFraisDTO.
 */
@Mapper(componentModel = "spring", uses = {TypeServiceMapper.class,UniteOrganisationMapper.class})
public interface RepartitionFraisMapper extends EntityMapper<RepartitionFraisDTO, RepartitionFrais> {

    @Mapping(source = "typeService.id", target = "typeServiceId")
    @Mapping(source = "typeService.libelle", target = "typeServiceLibelle")

    @Mapping(source = "fseSource.id", target = "fseSourceId")
    @Mapping(source = "fseSource.libelleUniteOrganisation", target = "fseSrcLibelle")

    @Mapping(source = "fseDest.id", target = "fseDestId")
    @Mapping(source = "fseDest.libelleUniteOrganisation", target = "fseAcqLibelle")
    RepartitionFraisDTO toDto(RepartitionFrais repartitionFrais);

    @Mapping(source = "typeServiceId", target = "typeService")
    @Mapping(source = "fseSourceId", target = "fseSource")
    @Mapping(source = "fseDestId", target = "fseDest")
    RepartitionFrais toEntity(RepartitionFraisDTO repartitionFraisDTO);

    default RepartitionFrais fromId(Long id) {
        if (id == null) {
            return null;
        }
        RepartitionFrais repartitionFrais = new RepartitionFrais();
        repartitionFrais.setId(id);
        return repartitionFrais;
    }
}
