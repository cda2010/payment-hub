/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.domain;

import com.m2i.omnicanal.model.ParametreUtilisateur;
import java.io.Serializable;
import jakarta.persistence.Entity;

/**
 *
 * @author cda
 */
@Entity
public class ParametreUser extends ParametreUtilisateur implements Serializable{

    public ParametreUser() {
        super();
        
    }
    
}
