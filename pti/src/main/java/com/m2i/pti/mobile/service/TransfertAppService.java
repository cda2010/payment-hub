/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.mobile.service;

import com.m2i.pti.domain.BrancheActivite;
import com.m2i.pti.domain.MotifOperation;
import com.m2i.pti.domain.Nationalite;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.mobile.dto.MBrancheActivite;
import com.m2i.pti.mobile.dto.MNationalite;
import com.m2i.pti.mobile.dto.MOrganisation;
import com.m2i.pti.mobile.enumeration.MTypeOrganisation;
import com.m2i.pti.repository.BrancheActiviteRepository;
import com.m2i.pti.repository.NationaliteRepository;
import com.m2i.pti.repository.ParametreAppRepository;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author cda
 */
@Service
public class TransfertAppService {

    private final ParametreAppRepository parametreAppRepository;
    private final UniteOrganisationService uniteOrganisationService;
    private final BrancheActiviteRepository brancheActiviteRepository;
    private final NationaliteRepository nationaliteRepository;
    PassiveExpiringMap<String, List<BrancheActivite>> listeBranche = new PassiveExpiringMap<>(6400000);
    PassiveExpiringMap<String, List<Nationalite>> listNation = new PassiveExpiringMap<>(6400000);

    public TransfertAppService(NationaliteRepository nationaliteRepository, BrancheActiviteRepository brancheActiviteRepository, ParametreAppRepository parametreAppRepository, UniteOrganisationService uniteOrganisationService) {
        this.parametreAppRepository = parametreAppRepository;
        this.uniteOrganisationService = uniteOrganisationService;
        this.brancheActiviteRepository = brancheActiviteRepository;
        this.nationaliteRepository = nationaliteRepository;
    }

    public List<MOrganisation> getUoAllFilsByType(String codeUo, MTypeOrganisation type) {
        UniteOrganisation uo = uniteOrganisationService.findByCode(codeUo);
        ArrayList<UniteOrganisation> listFils = new ArrayList();
        if (uo != null) {
            List<UniteOrganisationDTO> listUoDto = uniteOrganisationService.findAllUoFilsByTypeFiltre(TypeUO.valueOf(type.name()), uo, listFils);
            //    List<UniteOrganisationDTO> listUoDto = uniteOrganisationService.findUoAllFilsByType(uo.getId(), TypeUO.valueOf(type.name()));
            List<MOrganisation> listMUo = new ArrayList();
            for (UniteOrganisationDTO uoD : listUoDto) {
                MOrganisation morga = new MOrganisation();
                morga.setCodeUo(uoD.getCodeUO());
                morga.setNom(uoD.getLibelleUniteOrganisation());
                morga.setType(MTypeOrganisation.valueOf(type.name()));
                listMUo.add(morga);
            }
            return listMUo;
        }

        return null;

    }

    public List<MBrancheActivite> findBrancheActivite() {
        List<BrancheActivite> listBranche;
        if (listeBranche.containsKey("BRANCHE") && listeBranche.get("BRANCHE") != null) {
            listBranche = listeBranche.get("BRANCHE");
        } else {
            listBranche = brancheActiviteRepository.findAll();
            if (listBranche != null) {
                listeBranche.put("BRANCHE", listBranche);
            }

        }
        ArrayList<MBrancheActivite> listBrAc = new ArrayList();
        for (BrancheActivite bracheAc : listBranche) {
            MBrancheActivite br = new MBrancheActivite();
            br.setCode(bracheAc.getCode());
            br.setLibelle(bracheAc.getLibelle());
            listBrAc.add(br);
        }
        return listBrAc;
    }

    public List<MNationalite> findNationalite() {
        List<Nationalite> listNationalite;
        if (listNation.containsKey("NATIONALITE") && listNation.get("NATIONALITE") != null) {
            listNationalite = listNation.get("NATIONALITE");
        } else {
            listNationalite = nationaliteRepository.findAll();
            if (listNationalite != null) {
                listNation.put("NATIONALITE", listNationalite);
            }

        }

        ArrayList<MNationalite> listNat = new ArrayList();
        for (Nationalite nationalite : listNationalite) {
            MNationalite nat = new MNationalite();
            nat.setCode(nationalite.getCode());
            nat.setLibelle(nationalite.getLibelle());
            listNat.add(nat);
        }
        return listNat;
    }

}
