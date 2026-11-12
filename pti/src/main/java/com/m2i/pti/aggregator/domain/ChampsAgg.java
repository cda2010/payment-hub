/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.aggregator.domain;

import com.m2i.pti.domain.enumeration.TypeComposant;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 *
 * @author cda
 */
public class ChampsAgg {

    private String code;

    @Enumerated(EnumType.STRING)
    private TypeComposant typeComposant;
}
