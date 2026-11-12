/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.application.service.dto;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author kadsuke
 */
public class HighChartResponse {
    String label;
    String type;
    String category;
    HashMap<String, List<Double>> series;
    
    
}
