package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.service.dto.ProduitCanalDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity ProduitCanal and its DTO ProduitCanalDTO.
 */
@Mapper(componentModel = "spring", uses = {CanalMapper.class,ProduitMapper.class})
public interface ProduitCanalMapper extends EntityMapper<ProduitCanalDTO, ProduitCanal> {


    @Mapping(source = "produit.id", target = "produitId")
    @Mapping(source = "canal.libelleCanal", target = "canalLibelle")
    @Mapping(source = "canal.id", target = "canalId")
    ProduitCanalDTO toDto(ProduitCanal produitCanal);

    @Mapping(source = "produitId", target = "produit")
    @Mapping(source = "canalId", target = "canal")
    ProduitCanal toEntity(ProduitCanalDTO produitCanalDTO);

    default ProduitCanal fromId(Long id) {
        if (id == null) {
            return null;
        }
        ProduitCanal produitCanal = new ProduitCanal();
        produitCanal.setId(id);
        return produitCanal;
    }
}
