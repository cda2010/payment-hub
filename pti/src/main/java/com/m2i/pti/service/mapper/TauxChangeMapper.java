package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.TauxChangeDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity TauxChange and its DTO TauxChangeDTO.
 */
@Mapper(componentModel = "spring", uses = {DeviseMapper.class})
public interface TauxChangeMapper extends EntityMapper<TauxChangeDTO, TauxChange> {

    @Mapping(source = "deviseDepart.id", target = "deviseDepartId")
    @Mapping(source = "deviseDepart.libelle", target = "deviseDepartLibelle")
    @Mapping(source = "deviseArrivee.id", target = "deviseArriveeId")
    @Mapping(source = "deviseArrivee.libelle", target = "deviseArriveeLibelle")
    TauxChangeDTO toDto(TauxChange tauxChange); 

    @Mapping(source = "deviseDepartId", target = "deviseDepart")
    @Mapping(source = "deviseArriveeId", target = "deviseArrivee")
    TauxChange toEntity(TauxChangeDTO tauxChangeDTO);

    default TauxChange fromId(Long id) {
        if (id == null) {
            return null;
        }
        TauxChange tauxChange = new TauxChange();
        tauxChange.setId(id);
        return tauxChange;
    }
}
