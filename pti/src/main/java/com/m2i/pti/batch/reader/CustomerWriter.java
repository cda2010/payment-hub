package com.m2i.pti.batch.reader;

import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.service.LigneChargementTransfertService;
import com.m2i.pti.service.easyBatch.CSVTransfert;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class CustomerWriter implements ItemWriter<LigneChargementTransfert> {

    private final Logger log = LoggerFactory.getLogger(CustomerWriter.class);
    @Autowired
    LigneChargementTransfertService ligneChargementTransfertService;

    @Override
    public void write(Chunk<? extends LigneChargementTransfert> list) throws Exception {
        log.debug("REST list : {}", list);
        System.out.println("Thread Name : -" + Thread.currentThread().getName());
        for (LigneChargementTransfert item : list) {
            log.debug("REST list : {}", item.getMontant());
            ligneChargementTransfertService.save(item);
        }
    }
}
