/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.jobScheduler;
import com.m2i.pti.repository.FacturierRepository;
import com.m2i.pti.domain.Facturier;
import com.m2i.pti.external.service.TraitementFactureTransfert;
import com.m2i.pti.external.service.TraitementPaiementImpotBenin;
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
public class ExecuteOperationImpotBenin implements Serializable, Job {

    @Autowired
    ApiService apiService;
    @Autowired
    TraitementPaiementImpotBenin traitementPaiementImpotBenin;
     @Autowired
  FacturierRepository facturierRepository;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        String codeFsePartenaire = data.getString("CODEPARTENAIRE");
        String codeFseTeneurCompte = data.getString("CODETENEURCPTE");
        String codeUo1 = data.getString("CODEUO1");//* le code uo du facturier qui es
        String codeUo2 = data.getString("CODEUO2");
        String codePv = data.getString("CODEPV");
        String typeCompte = data.getString("TYPECOMPTE");
        String codeIndicatif =  data.getString("CODEINDICATIF");
        String codebanque =  data.getString("CODEBANQUE");
        String codeParametre =  data.getString("CODEPARAMETRE"); 
        String codeFacturier =  data.getString("CODEFACTURIER"); 
        String idProduit =  data.getString("IDPRODUIT"); 
        String montant = data.getString("MONTANT");        
        String isAmountCondition = data.getString("MONTANTCONDITION");
        
        System.out.println("-----------CODEUO1---------------");        
        System.out.println(codeUo1);
        System.out.println("-----------isAmountCondition---------------");        
        System.out.println(isAmountCondition);
        System.out.println("-----------codeValide---------------");
        System.out.println("-----------montant---------------");        
        System.out.println(montant);        

        try {
            List<FactureFournisseur> listfacture = getFacture(codeFsePartenaire);
              //Facturier fac = facturierRepository.findByCodeUo(codeUo);
            traitementPaiementImpotBenin.traitementOperationTransfert(listfacture, codeFsePartenaire, codeFseTeneurCompte, codeIndicatif, codeUo1, codeUo2, montant, isAmountCondition, codePv, typeCompte,codebanque,codeFacturier,idProduit,codeParametre);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    public List<FactureFournisseur> getFacture(String codeFse) throws IOException, NoSuchAlgorithmException, JAXBException {
        List<MParametre> listparam = new ArrayList();
        
        CollectionFacture col = apiService.getFacture(listparam, codeFse);
        if (col != null) {
            return col.getListeFacture();
        }
        return Collections.emptyList();
    }
}
