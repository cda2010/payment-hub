/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.jobScheduler;

import com.m2i.pti.repository.FacturierRepository;
import com.m2i.pti.domain.Facturier;
import com.m2i.pti.external.service.TraitementFactureTransfert;
import com.m2i.pti.external.service.TraitementPaiementGuceg;
import com.m2i.pti.mobile.dto.MParametre;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.dto.CollectionFacture;
import com.m2i.pti.service.dto.FactureFournisseur;
import java.io.IOException;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import jakarta.xml.bind.JAXBException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author cda
 */
public class ExecuteOperationGuceg implements Serializable, Job {

    @Autowired
    ApiService apiService;
    @Autowired
    TraitementPaiementGuceg traitementPaiementGuceg;
    @Autowired
    FacturierRepository facturierRepository;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        String codeFsePartenaire = data.getString("CODEPARTENAIRE");
        String codeFseTeneurCompte = data.getString("CODETENEURCPTE");
        String codeUo = data.getString("CODEUO");//* le code uo du facturier qui es
        String codePv = data.getString("CODEPV");
        String typeCompte = data.getString("TYPECOMPTE");
        String codeIndicatif = data.getString("CODEINDICATIF");
        String codebanque = data.getString("CODEBANQUE");
        String codeParametre = data.getString("CODEPARAMETRE");
        String codeFacturier = data.getString("CODEFACTURIER");
        String idProduit = data.getString("IDPRODUIT");
        String codeValide = data.getString("CODEVALIDE");
        String codeRejet = data.getString("CODEREJET");
        System.out.println("-----------codeRejet---------------");        
        System.out.println(codeRejet);
        System.out.println("-----------codeRejet---------------");
        System.out.println("-----------codeValide---------------");        
        System.out.println(codeValide);
        System.out.println("-----------codeValide---------------");
        System.out.println("-----------CODEVALIDE---------------");        
        System.out.println(data.getString("CODEVALIDE"));        
        System.out.println(data.getString("CODEREJET"));
        System.out.println("-----------codeValide---------------");


        try {
            List<FactureFournisseur> listfacture = getFacture(codeFsePartenaire);
            Facturier fac = facturierRepository.findByCodeUo(codeUo);
            traitementPaiementGuceg.traitementOperationTransfert(listfacture, codeFsePartenaire, codeFseTeneurCompte, codeIndicatif, codeUo, codePv, typeCompte, codebanque, codeFacturier, idProduit, codeParametre, fac.getId(), codeValide, codeRejet);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    public List<FactureFournisseur> getFacture(String codeFse) throws IOException, NoSuchAlgorithmException, JAXBException {
        List<MParametre> listparam = new ArrayList();
        System.out.println(codeFse);
        CollectionFacture col = apiService.getFacture(listparam, codeFse);
        if (col != null) {
            return col.getListeFacture();
        }
        return Collections.emptyList();
    }
}
