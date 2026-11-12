package com.m2i.pti.service.mapper;


import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.service.ChargementTransfertDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {})
public interface ChargementTransfertMapper extends EntityMapper<ChargementTransfertDTO, ChargementTransfert> {

    @Mapping(source = "client.id", target = "clientId")
    ChargementTransfertDTO toDto(ChargementTransfert chargement);

    ChargementTransfert toEntity(ChargementTransfertDTO chargementTransfertDTO);

    default ChargementTransfert formId(Long id){
        if (id == null) {
            return null;
        }
        ChargementTransfert chargementTransfert=new ChargementTransfert();
        chargementTransfert.setId(id);
        return  chargementTransfert;
    }
}
