package com.m2i.pti.service;

import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.EtiquetteCompteCode;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.repository.BilanTaxeRepository;
import com.m2i.pti.repository.TypeUniteOrganisationRepository;
import com.m2i.pti.service.dto.BilanTaxeDTO;
import com.m2i.pti.service.dto.TaxeOperationDTO;
import com.m2i.pti.service.mapper.BilanTaxeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing BilanTaxe.
 */
@Service
@Transactional
public class BilanTaxeService {

    private final Logger log = LoggerFactory.getLogger(BilanTaxeService.class);

    private BilanTaxeRepository bilanTaxeRepository;

    private BilanTaxeMapper bilanTaxeMapper;

    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;
    private final  UniteOrganisationService uniteOrganisationService;
    private final CompteService compteService;
    private final ApiService apiService;

    public BilanTaxeService(BilanTaxeRepository bilanTaxeRepository, BilanTaxeMapper bilanTaxeMapper, TypeUniteOrganisationRepository typeUniteOrganisationRepository, UniteOrganisationService uniteOrganisationService, CompteService compteService, CommissionService commissionService, ApiService apiService) {
        this.bilanTaxeRepository = bilanTaxeRepository;
        this.bilanTaxeMapper = bilanTaxeMapper;
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.uniteOrganisationService = uniteOrganisationService;
        this.compteService = compteService;
        this.apiService = apiService;
    }

    /**
     * Save a bilanTaxe.
     *
     * @param bilanTaxeDTO the entity to save
     * @return the persisted entity
     */
    public BilanTaxeDTO save(BilanTaxeDTO bilanTaxeDTO) {
        log.debug("Request to save BilanTaxe : {}", bilanTaxeDTO);

        BilanTaxe bilanTaxe = bilanTaxeMapper.toEntity(bilanTaxeDTO);
        bilanTaxe = bilanTaxeRepository.save(bilanTaxe);
        return bilanTaxeMapper.toDto(bilanTaxe);
    }

    /**
     * Get all the bilanTaxes.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public List<BilanTaxeDTO> findAll() {
        log.debug("Request to get all BilanTaxes");
        return bilanTaxeRepository.findAll().stream()
            .map(bilanTaxeMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one bilanTaxe by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    public BilanTaxeDTO findOne(Long id) {
        log.debug("Request to get BilanTaxe : {}", id);
        return bilanTaxeMapper.toDto(bilanTaxeRepository.findById(id).orElse(null));
    }

    /**
     * Delete the bilanTaxe by id.
     *
     * @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete BilanTaxe : {}", id);
        bilanTaxeRepository.deleteById(id);
    }

    /**
     * Enregistrement des taxes de l'opération en cours
     * @param operation
     * @param taxeOperations
     */
    public void saveBilanTaxes(Operation operation, Set<TaxeOperationDTO> taxeOperations) {
        log.info("sauvegarde du bilan  des taxes de l'operation");
        for (TaxeOperationDTO taxeOperation:taxeOperations){
            log.debug("---||taxe "+taxeOperation.getId());
            BilanTaxeDTO bilanTaxe=new BilanTaxeDTO();
            bilanTaxe.setTaxeOperationId(taxeOperation.getId());
            bilanTaxe.setOperationId(operation.getId());
            bilanTaxe.setEstPaye(Boolean.FALSE);
            bilanTaxe.setOrganisationId(operation.getPointVente().getUniteOrganisation().getId());
            bilanTaxe.setMontant(taxeOperation.getMontant());
            this.save(bilanTaxe);
        }
    }

    /**
     * Comptabilisatin des taxes par agence
     * Chaque agence dispose de son compte CPTEATTENTECOMTAXE
     * @param etiquetteCompteDebit
     * @param etiquettecompteCredit
     * @throws IOException
     */
    public void comptabilisationTaxes(EtiquetteCompteCode etiquetteCompteDebit, EtiquetteCompteCode etiquettecompteCredit) throws IOException {
        Optional<TypeUniteOrganisation> typeUniteOrganisation = typeUniteOrganisationRepository.findByCodeTypeUO(String.valueOf(TypeUO.AGENCE));
        if (typeUniteOrganisation.isPresent()) {
            //Liste de toutes les agences du système
            List<UniteOrganisation> uniteOrganisations = uniteOrganisationService.findByType(typeUniteOrganisation.get().getId());
            for (UniteOrganisation uo : uniteOrganisations) {//Faire le bilan des commissions par agence
                log.debug("Agence ........"+uo.getLibelleUniteOrganisation());
                ArrayList<UniteOrganisation> listAll = new ArrayList<>();
                ArrayList<UniteOrganisation> listFils = new ArrayList<>();
                listAll.addAll(uniteOrganisationService.findAllUoFils(uo, listAll, listFils));
                listAll.add(uo);
                List<Commission> commsActeurs = new ArrayList<>();
                for (UniteOrganisation uoFils : listAll) { //Bilan pour chaque fils du reseau de l'agence
                  List<BilanTaxe> bilanTaxes=bilanTaxeRepository.findByEstPayeAndOrganisationId(Boolean.FALSE,uoFils.getId());
                    BigDecimal montant = BigDecimal.ZERO;
                    for (BilanTaxe bilanTaxe : bilanTaxes) {
                        montant = montant.add(bilanTaxe.getMontant());
                        bilanTaxe.setEstPaye(Boolean.TRUE);
                        bilanTaxeRepository.save(bilanTaxe);
                    }
                    Commission commission = new Commission();
                    commission.organisation(uoFils)
                            .montant(montant);
                    commsActeurs.add(commission);
                }
                Double montantTotal = commsActeurs.stream().mapToDouble(comm -> comm.getMontant().doubleValue()).sum();
                /*  On debite le compte CPTEATTENTECOMTAXE de l'agence
                    On credite le compte commission  de chaque fils du reseau de l'agence
                 */
                Compte compteDebit = compteService.getCompteOfOrganisation(uo,etiquetteCompteDebit);
                Compte compteCredit=compteService.getCompteOfOrganisation(uo,etiquettecompteCredit);
                String montantFraisAcq = "0";
                String montantFraisEm = "0";
                String codeFseSrc = compteDebit.getCompteBanque().getCodeFse();
                String codeFseDest =compteCredit.getCompteBanque().getCodeFse();
                String codeAgenceSrc = compteDebit.getCompteBanque().getCodeAgence();
                String codeAgenceDest = compteCredit.getCompteBanque().getCodeAgence();
                String ettiqueSrc = compteDebit.getEtiquette();
                String ettiqueteDest = compteCredit.getEtiquette();
                String codeDebit = compteDebit.getValeurCompte() + "#" + compteDebit.getCompteBanque().getCodeAgence() + "#" + compteDebit.getCompteBanque().getCodeBanque();
                String codeCredit = compteCredit.getValeurCompte() + "#" + compteCredit.getCompteBanque().getCodeAgence() + "#" + compteCredit.getCompteBanque().getCodeBanque();
                String codeService = ApiService.CODE_SERVICE_ACCOUNT_ACCOUNT;
                //apiService.sendRequestComptabilisation(montantFraisAcq, montantFraisEm, String.valueOf(montantTotal), codeFseDest, codeFseSrc, codeAgenceDest, codeAgenceSrc, ettiqueSrc, codeDebit, ettiqueteDest, codeCredit, codeService);
            }
        }
    }
}
