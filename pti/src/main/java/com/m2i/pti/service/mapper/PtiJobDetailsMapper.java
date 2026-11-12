package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.PtiJobDetails;
import com.m2i.pti.service.dto.PtiJobDetailsDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity PtiJobDetails and its DTO PtiJobDetailsDTO.
 */
@Mapper(componentModel = "spring", uses = {})
public interface PtiJobDetailsMapper extends EntityMapper<PtiJobDetailsDTO, PtiJobDetails> {

    

    //@Mapping(target = "histExecutionJobs", ignore = true)
   // @Mapping(source = "fournisseurServiceId", target = "fournisseurService.id")
   // PtiJobDetails toEntity(PtiJobDetailsDTO ptiJobDetailsDTO);

    @Mapping(source = "fournisseurService.id", target = "fournisseurServiceId")
    @Mapping(source = "fournisseurService.libelleFse", target = "libelleFse")
    PtiJobDetailsDTO toDto(PtiJobDetails ptiJobDetails);

    default PtiJobDetails fromId(Long id) {
        if (id == null) {
            return null;
        }
        PtiJobDetails ptiJobDetails = new PtiJobDetails();
        ptiJobDetails.setId(id);
        return ptiJobDetails;
    }
}
