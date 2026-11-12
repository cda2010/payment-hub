package com.m2i.pti.service;


import com.m2i.pti.domain.Canal;
import com.m2i.pti.domain.Facturier;
import com.m2i.pti.domain.Produit;
import com.m2i.pti.domain.ProduitCanal;
import com.m2i.pti.repository.CanalRepository;
import com.m2i.pti.repository.FacturierRepository;
import com.m2i.pti.repository.ProduitCanalRepository;
import com.m2i.pti.repository.ProduitRepository;
import com.m2i.pti.service.dto.ProduitDTO;
import com.m2i.pti.service.mapper.ProduitMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProduitService {
    private final Logger log = LoggerFactory.getLogger(ProduitService.class);

    private final FacturierRepository facturierRepository;

    private final ProduitRepository produitRepository;

    private final ProduitMapper produitMapper;

    private final CanalRepository canalRepository;

    private final ProduitCanalRepository produitCanalRepository;


    public ProduitService(FacturierRepository facturierRepository, ProduitRepository produitRepository, ProduitMapper produitMapper, CanalRepository canalRepository, ProduitCanalRepository produitCanalRepository) {
        this.facturierRepository = facturierRepository;
        this.produitRepository = produitRepository;
        this.produitMapper = produitMapper;
        this.canalRepository = canalRepository;
        this.produitCanalRepository = produitCanalRepository;
    }


    /**
     * Save a produitCanal.
     *
     * @param produitDTO the entity to save
     * @return the persisted entity
     */
    @Transactional
    public Produit save(ProduitDTO produitDTO) {
        log.debug("Request to save Produit-- : {}", produitDTO);
        Produit produit=produitMapper.toEntity(produitDTO);
        ProduitCanal produitCanal = new ProduitCanal();
        Facturier facturier=facturierRepository.getOne(produitDTO.getFacturierId());
        if(facturier.getId()!=null){
            produit.setFacturier(facturier);
        }
        produit = produitRepository.save(produit);
        if (produitDTO.getCanalIds() != null) {
                for (Long canalId : produitDTO.getCanalIds()) {
                    if (canalId != null) {
                        Canal canal = canalRepository.getOne(canalId);
                        if (canal.getId() != null) {
                            produitCanal.setCanal(canal);
                            produitCanal.setProduit(produit);
                            produitCanalRepository.save(produitCanal);
                            produitCanal = new ProduitCanal();
                        }
                    }
                }
        } else {
                for (Canal canal : canalRepository.findAll()) {
                    produitCanal.setCanal(canal);
                    produitCanal.setProduit(produit);
                    produitCanalRepository.save(produitCanal);
                    produitCanal = new ProduitCanal();
                }
        }

        return produit;
    }

    @Transactional
    public Produit upDate(ProduitDTO produitDTO) {
        log.debug("Request to update Produit------------ : {}", produitDTO);
        Produit produit=produitMapper.toEntity(produitDTO);
        ProduitCanal produitCanal = new ProduitCanal();
        Facturier facturier=facturierRepository.getOne(produitDTO.getFacturierId());
        if(facturier.getId()!=null){
            produit.setFacturier(facturier);
        }

        produit = produitRepository.save(produit);
        if (produitDTO.getCanalIds() != null) {
            List<Long> canalList=produitCanalRepository.findIdByProduitId(produitDTO.getId());
            log.debug("-----------*-*****----------------"+produitDTO.getCanalIds());
            int sizeFromWeb=produitDTO.getCanalIds().size();
            int sizeFromTable=canalList.size();
            if(sizeFromTable>=sizeFromWeb){
                for(Long ids: canalList){
                    if(produitDTO.getCanalIds().contains(ids)) {
                        log.debug("*******Y********"+ids);
                    } else{
                        log.debug("*********N******"+ids);
                        ProduitCanal canalProduit=produitCanalRepository.findByCanalIdAndProduitId(ids,produit.getId());
                        if(canalProduit!=null){
                            produitCanalRepository.delete(canalProduit);
                        } else {
                            log.debug("****************************Saved New element*****************");
                            ProduitCanal newCanalProduit=new ProduitCanal();
                            newCanalProduit.setProduit(produit);
                            newCanalProduit.setCanal(canalRepository.getOne(ids));
                            produitCanalRepository.save(newCanalProduit);
                        }

                    }
                }
            } else {
                for(Long ids: produitDTO.getCanalIds()){
                    if(canalList.contains(ids)) {
                        log.debug("*******Y********"+ids);
                    } else{
                        log.debug("*********N******"+ids);
                        ProduitCanal canalProduit=produitCanalRepository.findByCanalIdAndProduitId(ids,produit.getId());
                        if(canalProduit!=null){
                            produitCanalRepository.delete(canalProduit);
                        } else {
                            log.debug("****************************Saved*****************");
                            ProduitCanal newCanalProduit=new ProduitCanal();
                            newCanalProduit.setProduit(produit);
                            newCanalProduit.setCanal(canalRepository.getOne(ids));
                            produitCanalRepository.save(newCanalProduit);
                        }

                    }
                }
            }


        } else {
                for (Canal canal : canalRepository.findAll()) {
                    produitCanal.setCanal(canal);
                    produitCanal.setProduit(produit);
                    produitCanalRepository.save(produitCanal);
                    produitCanal = new ProduitCanal();
                }
            }

        return produit;
    }

    public ProduitDTO findOne(Long id) {
        Produit produit = produitRepository.findById(id).orElse(null);
        if(produit.getId()!=null) {
            List<Long> produitCanalIds = new ArrayList<>();
            ProduitDTO produitDTO = produitMapper.toDto(produit);
            if(produit.getServiceAssocie()!=null)
                produitDTO.setServiceAssocieId(produit.getServiceAssocie().getId());
            if (produit.getFacturier() != null) {
                produitDTO.setFacturierId(produit.getFacturier().getId());
            }
            List<ProduitCanal> produitCanals = produitCanalRepository.findByProduitId(produit.getId());
            if (produitCanals.size() > 0) {
                for (ProduitCanal produitCanal : produitCanals) {
                    produitCanalIds.add(produitCanal.getId());
                }
            }
            produitDTO.setCanalIds(produitCanalIds);
            return produitDTO;
        }
        return null;
    }
}
