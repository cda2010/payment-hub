/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.reporting.service;

import com.m2i.pti.domain.CSVTransfert;


/**
 *
 * @author cda
 */
public interface CsvAssembler <T> {
     CSVTransfert assemble(String[] cols);
}
