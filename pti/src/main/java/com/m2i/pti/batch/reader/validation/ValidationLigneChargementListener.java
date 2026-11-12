/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.m2i.pti.batch.reader.validation;

import com.m2i.pti.batch.reader.LigneChargementTransDTO;
import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.service.LigneChargementTransfertService;
import com.m2i.pti.web.rest.FtpResource;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author USER_M2I
 */
@Component
public class ValidationLigneChargementListener implements ItemWriteListener<LigneChargementTransDTO> {

    private final Logger log = LoggerFactory.getLogger(ValidationLigneChargementListener.class);
    @Autowired
    LigneChargementTransfertService lignechargementTransfertService;

    @Override
    public void beforeWrite(Chunk<? extends LigneChargementTransDTO> list) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void afterWrite(Chunk<? extends LigneChargementTransDTO> list) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onWriteError(Exception excptn, Chunk<? extends LigneChargementTransDTO> list) {
        for (LigneChargementTransDTO ligneChargementTransDTO : list) {
            LigneChargementTransfert lignechargementTransfert = lignechargementTransfertService.findOne(ligneChargementTransDTO.getIdLigneChargement());
       //     lignechargementTransfert.setStatutLigne(StatutLigneChargement.RJ);
            lignechargementTransfert.setError(excptn.getMessage());
            lignechargementTransfertService.save(lignechargementTransfert);
        }

    }

}
