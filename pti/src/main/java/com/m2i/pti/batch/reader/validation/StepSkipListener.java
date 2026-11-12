/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.batch.reader.validation;

import com.m2i.pti.batch.reader.LigneChargementTransDTO;
import com.m2i.pti.domain.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;

/**
 *
 * @author cda
 */
public class StepSkipListener implements SkipListener<LigneChargementTransDTO, Operation> {

    private final Logger log = LoggerFactory.getLogger(StepSkipListener.class);
  

    @Override // item reader
    public void onSkipInRead(Throwable throwable) {

    }

    @Override // item writter
    public void onSkipInWrite(Operation item, Throwable throwable) {
        log.debug(throwable.getMessage());
    }

    @Override // item processor
    public void onSkipInProcess(LigneChargementTransDTO ligne, Throwable throwable) {
      
    }
}
