package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.DetailHistoriqueDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity DetailHistorique and its DTO DetailHistoriqueDTO.
 */
@Mapper(componentModel = "spring", uses = {HistoriqueMapper.class})
public interface DetailHistoriqueMapper extends EntityMapper<DetailHistoriqueDTO, DetailHistorique> {

    @Mapping(source = "historique.id", target = "historiqueId")
    @Mapping(source="historique.etiquette", target = "etiquette")
    DetailHistoriqueDTO toDto(DetailHistorique detailHistorique); 

    @Mapping(source = "historiqueId", target = "historique")
    DetailHistorique toEntity(DetailHistoriqueDTO detailHistoriqueDTO);

    default DetailHistorique fromId(Long id) {
        if (id == null) {
            return null;
        }
        DetailHistorique detailHistorique = new DetailHistorique();
        detailHistorique.setId(id);
        return detailHistorique;
    }
}
