package com.m2i.pti.service.mapper;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Named;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.m2i.pti.domain.EtapeService;
import com.m2i.pti.domain.enumeration.ActionService;
import com.m2i.pti.service.dto.EtapeServiceDTO;

@Mapper(componentModel = "spring", uses = {TypeServiceMapper.class}) 
public interface EtapeServiceMapper
{
   // @Mapping(target = "actionService", expression = "java(etapeService.getActionServicesAsString())")
   // EtapeServiceDTO toDto(EtapeService etapeService);

   // @Mapping(source = "actionService", target = "actionService", qualifiedByName = "mapStringListToEnumList")
  //  EtapeService toEntity(EtapeServiceDTO etapeServiceDTO);

  /* @Named("mapToStringList")
    default List<String> mapToStringList(List<ActionService> actionServices) {
        return actionServices.stream()
                             .map(ActionService::name)
                             .collect(Collectors.toList());
    }
    */
/*     default List<ActionService> mapStringListToEnumList(List<String> strings) {
        return strings.stream()
                      .map(ActionService::valueOf)
                      .collect(Collectors.toList());
    } */
}  
