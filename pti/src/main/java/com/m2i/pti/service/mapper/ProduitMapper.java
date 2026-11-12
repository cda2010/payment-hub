package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.Facturier;
import com.m2i.pti.domain.Produit;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.service.dto.ProduitDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {TypeService.class, Facturier.class})
public interface ProduitMapper extends EntityMapper<ProduitDTO, Produit> {
    
    
    @Mapping(source = "serviceAssocie.id", target = "serviceAssocieId")
    ProduitDTO toDto(Produit produit);

    @Mapping(source = "facturierId", target = "facturier.id")
    @Mapping(source = "serviceAssocieId", target = "serviceAssocie.id")
    Produit toEntity(ProduitDTO produitDTO);

    default Produit fromId(Long id) {
        if (id == null) {
            return null;
        }
        Produit produit = new Produit();
        produit.setId(id);
        return produit;
    }

}
