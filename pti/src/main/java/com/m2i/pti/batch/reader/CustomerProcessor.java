package com.m2i.pti.batch.reader;

import com.m2i.pti.domain.Chargement;
import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.service.ChargementTransfertService;
import com.m2i.pti.service.easyBatch.CSVTransfert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class CustomerProcessor implements ItemProcessor<CSVTransfert, LigneChargementTransfert> {

    @Autowired
    MobileService mobileService;
    @Value("#{jobParameters['chargementId']}")
    Long chargementId;
   ChargementTransfert chargement;
    @Autowired
    ChargementTransfertService chargementTransfertService;
    private final Logger log = LoggerFactory.getLogger(CustomerProcessor.class);

    public CustomerProcessor() {
        
    }

 

    
    @Override
    public LigneChargementTransfert process(CSVTransfert csvTransfert) throws Exception {
        // if (customer.getCountry().equals("United States")) {
      chargement=  chargementTransfertService.findOne(chargementId);
       log.debug("chargementId: {}", chargementId);
        log.debug("Montant: {}", csvTransfert.getMontant());
        LigneChargementTransfert ligneChargementTransfert = new LigneChargementTransfert();
        ligneChargementTransfert.setChargementTransfert(this.chargement);
        ligneChargementTransfert.setMontant(csvTransfert.getMontant());
        ligneChargementTransfert.setStatutLigne(StatutLigneChargement.NC);
        ligneChargementTransfert.setNom(csvTransfert.getNom());
        ligneChargementTransfert.setPrenom(csvTransfert.getPrenom());
        ligneChargementTransfert.setNumeroCompte(csvTransfert.getNumeroCompte());
        ligneChargementTransfert.setNumeroTelephone(csvTransfert.getNumeroTelephone());
        ligneChargementTransfert.setNarration(csvTransfert.getNarration());
        try {
            if (ligneChargementTransfert.getNumeroTelephone() != null && ligneChargementTransfert.getNumeroTelephone().trim().length() > 0) {
                Compte compte = mobileService.findComptePersonne(ligneChargementTransfert.getNumeroTelephone(), chargement.getCodePays());
                if (compte != null) {
                    ligneChargementTransfert.setNumeroCompte(compte.getValeurCompte());
                    //mLigneChargement.setNumeroCompte(compte.getValeurCompte());
                  //  ligneChargementTransfert.setCompteId(compte.getId());
                } else {
                    ligneChargementTransfert.setError("Compte introuvable");
                    //     mLigneChargement.setError("Compte introuvable");
                }
            } else {
                // mLigneChargement.setError("format de compte incorrect");
            }

        } catch (Exception e) {
            ligneChargementTransfert.setError(e.getMessage());
            //  ligneChargementTransfertService.save(ligneChargementTransfert);
        }

        //saveLigneChargement(csvCarte,result,TypeTransaction.CREDIT);              
        return ligneChargementTransfert;
        //    } else {
        //      return null;
        //  }
    }
}
