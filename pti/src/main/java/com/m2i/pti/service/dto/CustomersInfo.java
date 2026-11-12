/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.service.dto;

import java.util.List;

/**
 *
 * @author cda
 */
public class CustomersInfo {

    public List<Customers> customerAccounts;

    public List<Customers> getCustomerAccounts() {
        return customerAccounts;
    }

    public void setCustomerAccounts(List<Customers> customerAccounts) {
        this.customerAccounts = customerAccounts;
    }

   

    
}
