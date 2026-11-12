/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.easyBatch;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.service.OperationService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.easybatch.core.job.Job;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.m2i.pti.domain.enumeration.*;
/**
 *
 * @autho
 * r cda
 */
@Service
public  class OperationProcessor {
    
//     private final Logger log = LoggerFactory.getLogger(OperationProcessor.class);
//     OperationRepository operationRepository;
//      OperationService operationService;
//     private Read readService;
//    public OperationProcessor(Read readService,OperationRepository operationRepository,OperationService operationService) {
//        this.operationRepository = operationRepository;
//        this.readService=readService;
//        this.operationService=operationService;
//    }
//     
//    @Override
//        public Record processRecord(Record record) throws Exception {
//
//            Operation operation = (Operation) record.getPayload();
//           
//            try {
//               
//                
//                operationService.annulationOperation(operation.getCodeOperation(), Boolean.FALSE);
//            } catch (Exception e) {
//                System.out.println(e.getMessage());
//            }
//
//            return record;
//        }
//         //codeFse & etiquette
//    @Transactional
//    public void annulationOperationExpire() {
//        List<String> typeService = new ArrayList();
//        typeService.add("ENVOI_CASH");
//        typeService.add("APPEL_FOND");
//        List<Operation> listeOperation = operationRepository.findOperationExpire(StatutOperation.ENATTENTE, LocalDate.now(), typeService);
////        for (Operation op : listeOperation) {
//
//        //    }
//        Job job = (Job) readService.getBatch(listeOperation,  this);
//
//    }

}
