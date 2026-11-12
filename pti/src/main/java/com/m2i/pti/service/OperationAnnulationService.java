/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service;

import com.m2i.pti.domain.Operation;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;

/**
 *
 * @author cda
 */
public class OperationAnnulationService {
    
    
      public static class OperationProcessor implements RecordProcessor {
        OperationService operationService;
        public OperationProcessor(OperationService operationService) {
            this.operationService = operationService;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            Operation operation = (Operation) record.getPayload();
            try {
                operationService.annulationOperation(operation.getId(), Boolean.FALSE);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return record;
        }
    }
}
