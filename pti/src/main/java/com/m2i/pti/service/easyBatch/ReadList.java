/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.easyBatch;

import org.easybatch.core.reader.RecordReader;
import org.easybatch.core.record.Record;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.easybatch.core.record.GenericRecord;
import org.easybatch.core.record.Header;

/**
 *
 * @author cda
 */
public class ReadList implements RecordReader{

   
 private long currentRecordNumber;
  private Iterator iterator;
    public ReadList( ) {
   
      
    }
    
    @Override
    public void open() throws Exception {
         
        currentRecordNumber = 0;
    }

    @Override
    public Record readRecord() throws Exception {
           currentRecordNumber++;
        Header header = new Header(currentRecordNumber, getDataSourceName(), new Date());
        if (iterator.hasNext()) {
            return new GenericRecord<>(header, iterator.next());
        } else {
            return null;
}
    }

    @Override
    public void close() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String getDataSourceName() {
         return "In-Memory Iterable";
    }
    
}
