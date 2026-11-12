package com.m2i.pti.service.mapper;


import com.m2i.pti.domain.Historique;
import com.m2i.pti.service.dto.HistoriqueDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {})

public interface HistoriqueMapper extends EntityMapper<HistoriqueDTO, Historique> {
    default Historique formId(Long id) {
        if (id == null) {
            return null;
        }
        Historique historique= new Historique();
        historique.setId(id);
        return historique;
    }
}
