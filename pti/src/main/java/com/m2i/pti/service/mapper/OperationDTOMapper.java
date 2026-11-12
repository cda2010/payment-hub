package com.m2i.pti.service.mapper;

import com.m2i.pti.domain.*;
import com.m2i.pti.mobile.dto.MOperationDTO;

import org.mapstruct.*;

/**
 * Mapper for the entity Operation and its DTO OperationDTO.
 */
@Mapper(componentModel = "spring",uses = {TypeServiceMapper.class, BeneficiaireMapper.class, EmetteurMapper.class,
        PointVenteMapper.class, PaysMapper.class, CanalMapper.class, UniteOrganisationMapper.class,
        CompteMapper.class,UserMapper.class,MotifOperationMapper.class,DeviseMapper.class,PieceMapper.class})
public interface OperationDTOMapper extends EntityMapper<MOperationDTO, Operation> {

    @Mapping(source = "pieceBeneficiaire.id", target = "pieceBeneficiaireId")
    @Mapping(source = "pieceEmetteur.id", target = "pieceEmetteurId")
    @Mapping(source = "devisePaiement.id",target = "devisePaiementId")
    @Mapping(source = "devisePaiement.libelle", target = "devisePaiementLibelle")
    @Mapping(source = "operationTraite.id", target = "operationTraiteId")
    @Mapping(source = "typeService.id", target = "typeServiceId")
    @Mapping(source = "motifOperation.id", target = "motifOperationId")
    @Mapping(source = "typeService.code", target = "code")
    @Mapping(source = "beneficaire.id", target = "beneficaireId")
    @Mapping(source = "beneficaire.personne.telephone", target = "beneficiaireNum")
    @Mapping(source = "emetteur.id", target = "emetteurId")
    @Mapping(source = "pointVente.id", target = "pointVenteId")
    @Mapping(source = "pays.id", target = "paysId")
    @Mapping(source = "pays.libellePays", target = "paysLibellePays")
    @Mapping(source = "canal.id", target = "canalId")
    @Mapping(source = "canal.libelleCanal", target = "canalLibelleCanal")
    @Mapping(source = "uoAcquereur.id", target = "uoAcquereurId")
    @Mapping(source = "uoAcquereur.libelleUniteOrganisation", target = "uoAcquereurLibelleUniteOrganisation")
    @Mapping(source = "compteSrc.id", target = "compteSrcId")
    @Mapping(source = "compteSrc.valeurCompte", target = "compteSrcValeurCompte")
    @Mapping(source = "compteDest.id", target="compteDestId")
    @Mapping(source = "compteDest.valeurCompte", target = "compteDestValeurCompte")
    @Mapping(source = "uoEmetteur.id", target = "uoEmetteurId")
    @Mapping(source = "uoBeneficiaire.id", target = "uoBeneficiaireId")
    @Mapping(source = "uoEmetteur.libelleUniteOrganisation", target = "uoEmetteurLibelleUniteOrganisation")
      @Mapping(source = "uoBeneficiaire.libelleUniteOrganisation", target = "uoBeneficiaireLibelleUniteOrganisation")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.login", target = "login")
    @Mapping(source = "user.phoneNumber", target = "telephone")
 @Mapping(source = "createdDate", target = "createdDate")
    MOperationDTO toDto(Operation operation);
    @Mapping(source = "pieceBeneficiaireId", target = "pieceBeneficiaire")
    @Mapping(source = "pieceEmetteurId", target = "pieceEmetteur")
  
    @Mapping(source = "typeServiceId", target = "typeService")
    @Mapping(source = "motifOperationId", target = "motifOperation")
    @Mapping(source = "beneficaireId", target = "beneficaire")
    @Mapping(source = "emetteurId", target = "emetteur")
    @Mapping(source = "pointVenteId", target = "pointVente")
    @Mapping(source = "paysId", target = "pays")
    @Mapping(source = "canalId", target = "canal")
    @Mapping(source = "userId", target = "user")
    @Mapping(source = "uoAcquereurId", target = "uoAcquereur")
    @Mapping(source = "uoEmetteurId", target = "uoEmetteur")
        @Mapping(source = "uoBeneficiaireId", target = "uoBeneficiaire")
    @Mapping(source = "compteSrcId", target = "compteSrc")
    @Mapping(source = "compteDestId", target = "compteDest")
    @Mapping(source = "operationTraiteId", target = "operationTraite")
    @Mapping(source = "devisePaiementId", target = "devisePaiement")
    Operation toEntity(MOperationDTO operationDTO);

    default Operation fromId(Long id) {
        if (id == null) {
            return null;
        }
        Operation operation = new Operation();
        operation.setId(id);
        return operation;
    }
}
