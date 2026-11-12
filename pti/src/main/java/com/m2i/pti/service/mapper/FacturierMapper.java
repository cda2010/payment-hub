package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.Facturier;
import com.m2i.pti.service.dto.FacturierDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UniteOrganisationMapper.class, ClassificationFacturierMapper.class, RecepteurEmailMapper.class, FtpMapper.class})
public interface FacturierMapper extends EntityMapper<FacturierDTO, Facturier>{

    @Mapping(source = "uniteOrganisation.id", target = "uniteOrganisationId")

    // @Mapping(source = "classification.id", target = "classificationId")

    // @Mapping(source = "possesseur.id", target = "possesseur")

    //  @Mapping(source = "recepteurEmail.id", target = "recepteurEmailId")

    FacturierDTO toDto(FacturierDTO facturier);


    Facturier toEntity(FacturierDTO facturierDTO);

    default Facturier fromId(Long id) {
        if (id == null) {
            return null;
        }
        Facturier facturier = new Facturier();
        facturier.setId(id);
        return facturier;
    }
    
}
