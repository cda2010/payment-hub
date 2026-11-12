package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.ClassificationFacturier;
import com.m2i.pti.service.dto.ClassificationFacturierDTO;

public interface ClassificationFacturierMapper extends EntityMapper<ClassificationFacturierDTO, ClassificationFacturier>{

    ClassificationFacturierDTO toDto(ClassificationFacturier classificationFacturier);


    ClassificationFacturier toEntity(ClassificationFacturierDTO classificationFacturierDTO);

    default ClassificationFacturier fromId(Long id) {
        if (id == null) {
            return null;
        }
        ClassificationFacturier classificationFacturier = new ClassificationFacturier();
        classificationFacturier.setId(id);
        return classificationFacturier;
    }
    
}
