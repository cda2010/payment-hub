package com.m2i.pti.service;

import com.m2i.pti.domain.TaxeOperation;
import com.m2i.pti.domain.TypeService;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.repository.TaxeOperationRepository;
import com.m2i.pti.repository.TypeServiceRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.dto.TaxeOperationDTO;
import com.m2i.pti.service.mapper.TaxeOperationMapper;
import com.m2i.pti.service.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing Taxe.
 */
@Service
@Transactional
public class TaxeOperationService {

    private final Logger log = LoggerFactory.getLogger(TaxeService.class);

    private final TaxeOperationRepository taxeOperationRepository;

    private final TaxeOperationMapper taxeOperationMapper;

    private final UniteOrganisationService uniteOrganisationService;

    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final TypeServiceRepository typeServiceRepository;
TypeServiceService typeServiceService;
    public TaxeOperationService(TypeServiceService typeServiceService,TaxeOperationRepository taxeOperationRepository, TaxeOperationMapper taxeOperationMapper, UniteOrganisationService uniteOrganisationService, UniteOrganisationRepository uniteOrganisationRepository, TypeServiceRepository typeServiceRepository) {
        this.taxeOperationRepository = taxeOperationRepository;
        this.taxeOperationMapper = taxeOperationMapper;
        this.uniteOrganisationService = uniteOrganisationService;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.typeServiceRepository = typeServiceRepository;
        this.typeServiceService=typeServiceService;
    }

    /**
     * Save a taxe.
     *
     * @param taxeDTO the entity to save
     * @return the persisted entity
     */
    public TaxeOperationDTO save(TaxeOperationDTO taxeDTO) {
        log.debug("Request to save Taxe : {}", taxeDTO);
        log.debug(taxeDTO + "            ");
        TaxeOperation taxeOperation = taxeOperationMapper.toEntity(taxeDTO);
        taxeOperation = taxeOperationRepository.save(taxeOperation);
        return taxeOperationMapper.toDto(taxeOperation);
    }

    /**
     * Get all the taxes.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<TaxeOperationDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Taxes");
        return taxeOperationRepository.findAll(pageable)
                .map(taxeOperationMapper::toDto);
    }

    /**
     * Recherche des taxes en fonction d'un type de service
     *
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public List<TaxeOperationDTO> findAllByTypeServiceId(Long id) {
        log.debug("Request to get all Taxes");
        return taxeOperationMapper.toDto(taxeOperationRepository.findByTypeServices_Id(id));
    }


    /**
     * @param organisationId
     * @param typeServiceLibelle
     * @param montantFrais
     * @param montant
     * @param uoSource
     * @return
     */
    public List<TaxeOperationDTO> searchTaxeOperation(Long organisationId, String typeServiceLibelle,
                                                      BigDecimal montantFrais, BigDecimal montant,
                                                      UniteOrganisation uoSource) {
         TypeService typeService = typeServiceService.findService(String.valueOf(typeServiceLibelle));
        //TypeService typeService = typeServiceRepository.findTopByCode(String.valueOf(typeServiceLibelle));
        List<TaxeOperation> taxeOperationsOfService = taxeOperationRepository.findByTypeServices_Id(typeService.getId());
        if (uoSource == null)
            uoSource = uniteOrganisationService.getUoOfUserConnecte();
        if (uoSource!=null){
            
        }
        UniteOrganisation organisationDest =  uniteOrganisationService.findOneById(organisationId);
       // UniteOrganisation organisationDest = uniteOrganisationRepository.findById(organisationId).orElse(null);//Pays de destination
        List<TaxeOperation> taxeOperationsFind = new ArrayList<>();
        if (organisationDest != null) {
            UniteOrganisation zone = uniteOrganisationService.findUoParentByType(organisationDest, TypeUO.ZONE);
            ArrayList<UniteOrganisation> uos = new ArrayList<>();
            uniteOrganisationService.findAllUoParents(uoSource, uos);
            uos.add(uoSource);
            List<Long> taxeIds = taxeOperationsOfService.stream().map(taxeOperation ->
                    taxeOperation.getTaxe().getId()).distinct().collect(Collectors.toList());
            for (Long taxeId : taxeIds) {
                for (UniteOrganisation uo : uos) {
                    List<TaxeOperation> taxeOperations = taxeOperationRepository.findByUniteOrganisationIdAndTaxeIdAndTypeServices_IdAndZoneIdAndActivatedIsTrue(uo.getId(), taxeId, typeService.getId(), zone.getId());
                    if (!taxeOperations.isEmpty()) {
                        taxeOperationsFind.add(taxeOperations.get(0));
                        break;
                    }
                }
            }
        }
        List<TaxeOperationDTO> taxeOperationDTOS = taxeOperationMapper.toDto(taxeOperationsFind);
        taxeOperationDTOS = calculTaxes(taxeOperationDTOS, montant, montantFrais);
        return taxeOperationDTOS;
    }

    
    
    private BigDecimal getTaxeFormule(BigDecimal montant , BigDecimal valTax){
        
      valTax=  valTax.add(BigDecimal.valueOf(100));
      valTax=valTax.divide(BigDecimal.valueOf(100));
      
       System.out.println(valTax);
       BigDecimal valeurFrais=  montant.divide(valTax,2,RoundingMode.HALF_DOWN);
        return valeurFrais ;
        
    }
    /**
     * Calcul du montant des taxes
     *
     * @param taxeOperations
     * @param montantOp
     * @param montantFrais
     * @return
     */
    List<TaxeOperationDTO> calculTaxes(List<TaxeOperationDTO> taxeOperations, BigDecimal montantOp, BigDecimal montantFrais) {

        for (TaxeOperationDTO taxeOperation : taxeOperations) {
            if (montantOp != null && Util.isBiggerThan(montantFrais, BigDecimal.ZERO)) {
                BigDecimal montant = BigDecimal.ZERO;
                switch (taxeOperation.getAppliqueSur()) {
                    case FRAIS:
                        montant = montantFrais;
                        break;
                    case MONTANT:
                        montant = montantOp;
                        break;
                }
                BigDecimal resultat = BigDecimal.ZERO;
                 BigDecimal resul = BigDecimal.ZERO;
                switch (taxeOperation.getModeCalcul()) {
                    case FIXE:
                        resultat = taxeOperation.getValeur();
                        break;
                    case POURCENTAGE:
                        resul=    getTaxeFormule( montant ,  taxeOperation.getValeur());
                        resultat = (resul.multiply(taxeOperation.getValeur())).divide(BigDecimal.valueOf(100));
                  
                        break;
                }
                taxeOperation.setMontant(resultat);
            } else {
                taxeOperation.setMontant(BigDecimal.ZERO);
            }
        }
        return taxeOperations;
    }


    /**
     * Get all the Taxe with eager load of many-to-many relationships.
     *
     * @return the list of entities
     */
    public Page<TaxeOperationDTO> findAllWithEagerRelationships(Pageable pageable) {
        return taxeOperationRepository.findAllWithEagerRelationships(pageable).map(taxeOperationMapper::toDto);
    }


    /**
     * Get one taxe by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public Optional<TaxeOperationDTO> findOne(Long id) {
        log.debug("Request to get Taxe : {}", id);
        return taxeOperationRepository.findOneWithEagerRelationships(id)
                .map(taxeOperationMapper::toDto);
    }

    /**
     * Delete the taxe by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Taxe : {}", id);
        taxeOperationRepository.deleteById(id);
    }
}

