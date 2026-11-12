/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.easyBatch;

import org.easybatch.core.filter.RecordFilter;
import org.easybatch.core.record.Record;

/**
 *
 * @author cda
 */
public class RecordFilterCustom implements RecordFilter<Record>{
 private long number;

    public RecordFilterCustom(long number) {
        this.number = number;
    }
 
    @Override
    public Record processRecord(Record r) {
     if (r.getHeader().getNumber()%2 == number) {
            return null;
        }
        return r;
    }
    
}
