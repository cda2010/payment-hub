/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.omnicanal.presentation.service;

import com.m2i.omnicanal.model.CasExecution;
import com.m2i.omnicanal.model.FournisseurService;
import com.m2i.omnicanal.model.InterfaceUtilisateur;
import com.m2i.omnicanal.model.LigneSchemaEcriture;
import com.m2i.omnicanal.model.LigneTransaction;
import com.m2i.omnicanal.model.ParametreUtilisateur;
import com.m2i.omnicanal.presentation.domain.AgenceFse;
import com.m2i.omnicanal.presentation.domain.FournisseurFSE;
import com.m2i.omnicanal.presentation.domain.InterfaceUser;
import java.util.List;

import java.util.Set;

/**
 *
 * @author cda
 */
public interface PresentationDetailService {
    public Set<LigneTransaction> getTransactionInterne(AgenceFse agenceSource,AgenceFse agenceDest,String codeUo,FournisseurFSE fseSource, FournisseurFSE fseDest, List<ParametreUtilisateur> listparamUser, Set<LigneSchemaEcriture> listLigneShEcriture,CasExecution cas) throws Exception;
    public Set<ParametreUtilisateur> getParametresInterfaceValue(List<ParametreUtilisateur> parametreUser, Set<ParametreUtilisateur> paramUserInterface);
  
}
