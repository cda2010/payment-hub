/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.model;

import com.m2i.pti.domain.Canal;
import com.m2i.pti.mobile.dto.MParametre;
import com.m2i.pti.service.CanalService;
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

/**
 *
 * @author cda
 */
@Service
public class ApplicationGlobalDataService {

    GlobalData globalData;
    CanalService canalService;
    List<Object> listeData;
    List<GlobalData> listglobalData = new ArrayList();

    public ApplicationGlobalDataService(CanalService canalService) {
        this.canalService = canalService;
    }

  

    public List<GlobalData> getListglobalData() {
        return listglobalData;
    }

    @PostConstruct
    public void setGlobalData() {

        List<GlobalData> listglobalData = new ArrayList();
        List<Canal> listCanal = canalService.findAll();
           GlobalData globalData=new GlobalData();
        globalData.setTableName("CANAL");
        List<MParametre> listeData = new ArrayList();

        listCanal.forEach(canal -> {
            Canal param = new Canal();
            param.codeCanal(canal.getCodeCanal());
            param.setLibelleCanal(canal.getLibelleCanal());
            // you can implement some business logic here..
        });
        globalData.setListeData(listeData);
        listglobalData.add(globalData);
    }

}
