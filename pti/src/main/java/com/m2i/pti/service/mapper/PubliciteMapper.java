package com.m2i.pti.service.mapper;

import org.mapstruct.Mapper;

import com.m2i.pti.domain.Publicite;
import com.m2i.pti.service.dto.PubliciteDTO;

/**
 * Mapper for the entity Pays and its DTO PaysDTO.
 */
@Mapper(componentModel = "spring", uses = {DeviseMapper.class})
public interface PubliciteMapper extends EntityMapper<PubliciteDTO, Publicite> {

    Publicite toEntity(PubliciteDTO publiciteDTO);

    default Publicite fromId(Long id) {
        if (id == null) {
            return null;
        }
        Publicite publicite = new Publicite();
        publicite.setId(id);
        return publicite;
    }
}
