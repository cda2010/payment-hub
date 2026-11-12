package com.m2i.pti.batch.reader.validation;

import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.pti.batch.reader.*;
import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.TransactionComptePti;
import com.m2i.pti.domain.enumeration.EtatCompte;
import com.m2i.pti.domain.enumeration.StatutCompte;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.mobile.dto.MCompte;
import com.m2i.pti.mobile.dto.MInfos;
import com.m2i.pti.mobile.dto.MOperation;
import com.m2i.pti.mobile.dto.MPersonne;
import com.m2i.pti.mobile.dto.MResponseOperation;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.service.ChargementTransfertService;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.LigneChargementTransfertService;
import com.m2i.pti.service.TransactionComptePtiService;
import com.m2i.pti.web.rest.errors.ChargementException;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.StopWatch;

public class LigneChargementWriter implements ItemWriter<Operation> {

    private final Logger log = LoggerFactory.getLogger(CustomerWriter.class);
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

    @Override
    public void write(Chunk<? extends Operation> list) throws Exception {
        log.debug("REST list : {}", list);
        System.out.println("Thread Name : -" + Thread.currentThread().getName());
        for (Operation operation : list) {
        //    mobileService.envoiDesMessage(operation);
        }
//        list.parallelStream()
//                .forEach(
//                        operation -> {
//                            try {
//                               
//                            } catch (Exception ex) {
//
//                            }
//                        });

    }
}
