package com.m2i.pti.batch.reader.validation;

import com.m2i.pti.batch.reader.*;
import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.service.ChargementTransfertService;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.LigneChargementTransfertService;
import com.m2i.pti.service.TransactionComptePtiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class ChargementTransfertProcessor implements ItemProcessor<LigneChargementTransDTO, LigneChargementTransDTO> {

    @Autowired
    CompteService compteService;

    @Autowired
    LigneChargementTransfertService ligneChargementTransfertService;

    @Autowired
    OperationRepository operationRepository;

    @Autowired
    MobileService mobileService;
    @Value("#{jobParameters['chargementId']}")
    Long chargementId;
    ChargementTransfert chargement;
    @Autowired
    ChargementTransfertService chargementTransfertService;
    @Autowired
    TransactionComptePtiService transactionComptePtiService;
    private final Logger log = LoggerFactory.getLogger(CustomerProcessor.class);

    @Override
    public LigneChargementTransDTO process(LigneChargementTransDTO ligneChargementTransDTO) throws Exception {

        return ligneChargementTransDTO;

    }
}
