/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.easyBatch;

import com.m2i.pti.service.easyBatch.CsvData;



/**
 *
 * @author cda
 */
public interface CsvAssembler <T> {
     CsvData assemble(String[] cols);
}
