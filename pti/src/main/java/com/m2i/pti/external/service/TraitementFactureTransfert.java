/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.external.service;

import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.mobile.dto.MCompte;
import com.m2i.pti.mobile.dto.MInfos;
import com.m2i.pti.mobile.dto.MOperation;
import com.m2i.pti.mobile.dto.MParametre;
import com.m2i.pti.mobile.dto.MPersonne;
import com.m2i.pti.mobile.dto.MResponseOperation;
import com.m2i.pti.mobile.dto.SettlementResponse;
import com.m2i.pti.mobile.enumeration.MSexe;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.CommissionService;
import com.m2i.pti.service.dto.CommissionDTO;
import com.m2i.pti.service.dto.FactureFournisseur;
import com.m2i.pti.service.dto.PersonneAllDTO;
import com.m2i.pti.service.easyBatch.Read;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.JAXBException;

import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.easybatch.core.job.Job;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author cda
 */
@Service
public class TraitementFactureTransfert {

    private final Read readService;
    private final MobileService mobileService;
    ApiService apiService;
    private UniteOrganisationRepository uniteOrganisationRepository;

    public TraitementFactureTransfert(UniteOrganisationRepository uniteOrganisationRepository, MobileService mobileService, Read readService, ApiService apiService) {
        this.readService = readService;
        this.mobileService = mobileService;
        this.apiService = apiService;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
    }

    public void traitementOperationTransfert(List<FactureFournisseur> listFactFournisseur, String codeFsePartenaire, String codeFseTeneurCompte, String codeIndicatif, String codeUo, String codePv, String typeCompte, String codebanque) throws IOException {
        Job job = (Job) readService.getBatch(listFactFournisseur, new TraitementFactureTransfert.FactureReaderProcessor(uniteOrganisationRepository, apiService, mobileService, codeFsePartenaire, codeFseTeneurCompte, codeIndicatif, codeUo, codePv, typeCompte, codebanque));
    }

    public class FactureReaderProcessor implements RecordProcessor {

        String codeFseTeneurCompte;
        String codeIndicatif;
        String codeFsePartenaire;
        String codeUo;
        String codePv;
        String typeCompte;
        String codebanque;
        private final MobileService mobileService;
        ApiService apiService;
        private UniteOrganisationRepository uniteOrganisationRepository;

        public FactureReaderProcessor(UniteOrganisationRepository uniteOrganisationRepository, ApiService apiService, MobileService mobileService, String codeFsePartenaire, String codeFseTeneurCompte, String codeIndicatif, String codeUo, String codePv, String typeCompte, String codebanque) {
            this.codeFsePartenaire = codeFsePartenaire;
            this.codeFseTeneurCompte = codeFseTeneurCompte;
            this.codeIndicatif = codeIndicatif;
            this.codeUo = codeUo;
            this.codePv = codePv;
            this.typeCompte = typeCompte;
            this.apiService = apiService;
            this.uniteOrganisationRepository = uniteOrganisationRepository;
            this.mobileService = mobileService;
            this.codebanque = codebanque;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            FactureFournisseur facture = (FactureFournisseur) record.getPayload();
            String[] tableDonnee = facture.getReference().split(";");
            String extReference = tableDonnee[0];
            String dateOperation = tableDonnee[2];
            String accountNumber = tableDonnee[3];
            String nombeneficiaire = tableDonnee[6];
            String nomEmetteur = tableDonnee[7];
            String rib = tableDonnee[9];
            String nom = "";
            String prenom = "";
            String codeAgence = "0";
            List<MParametre> listeParam = new ArrayList();

            // listeParam = getMparamtre(referencePaiement, codeparametre);
            String[] tabnom = nombeneficiaire.split(" ");
            if (tabnom.length > 0) {
                nom = tabnom[0];
                prenom = tabnom[1];
            }
            /*A revoir, le FSE TENEUR DE COMPTE */
            if (typeCompte.equals("BANQUE")) {
                UniteOrganisation fse = uniteOrganisationRepository.findByCodeFseAndSupprimeIsFalse(codeFseTeneurCompte);
                /* IL S AGIT DU CODE FSE DU COMPTE **/
                try {
                    PersonneAllDTO personne = apiService.getClientInfo(accountNumber, "0", "0", fse.getId());

                    if (personne != null && personne.getCompteCBIDTO() != null) {
                        codeAgence = personne.getCompteCBIDTO().getCodeAgence();
                        Double mt = Double.parseDouble(facture.getMontant());
                        String identifiant = codeUo;
                        String codeUoEm = codeUo;
                        MInfos ben = setPersonne(codeIndicatif, extReference, nom, prenom, codeAgence, accountNumber, codeFseTeneurCompte, MTypeCompte.BANQUE, this.codebanque, rib);
                        MOperation mOperation = getoperation(ben, "MOBILE", "", "ORGANISATION", identifiant, codePv, codeUoEm, codePv, codeUo, "CASH_ACCOUNT", mt.longValue(), 0L, 0L);
                        MResponseOperation rep = mobileService.doOperation(mOperation);

                        if (rep != null && rep.getCode().equals("0")) {
                            sendSettlement("0", listeParam, codeFsePartenaire, extReference);
                            // param = getMparametre("REFERENCEOPERATION",rep.getOperation().getCodeOperation());
                            //  listeParam.add(param);
                            //SettlementResponse settlementResponse = mobileService.invoiceSettlement(listeParam, codeFsePartenaire, "SETTLEMENT", null, rep.getOperation());
 
                        } else {
                            sendSettlement("-1", listeParam, codeFsePartenaire, extReference);
                        }
                    } else {
                        codeAgence = "0";
                        sendSettlement("-2", listeParam, codeFsePartenaire, extReference);
                    }
                }catch (CustomParameterizedException e){
                    if (e.getMessage().equals("Le compte est introuvable!")){
                        sendSettlement("-2", listeParam, codeFsePartenaire, extReference);
                    }else {
                        sendSettlement("-1", listeParam, codeFsePartenaire, extReference);
                    }
                }
            }
            /* Double mt = Double.parseDouble(facture.getMontant());
            String identifiant = codeUo;
            String codeUoEm = codeUo;
            MInfos ben = setPersonne(codeIndicatif, extReference, nom, prenom, codeAgence, accountNumber, codeFseTeneurCompte, MTypeCompte.BANQUE, this.codebanque);
            MOperation mOperation = getoperation(ben, "MOBILE", "", "ORGANISATION", identifiant, codePv, codeUoEm, codePv, codeUo, "CASH_ACCOUNT", mt.longValue(), 0L, 0L);
            MResponseOperation rep = mobileService.doOperation(mOperation);
            
            if (rep != null && rep.getCode().equals("0")) {
                sendSettlement("0", listeParam, codeFsePartenaire, extReference);
                // param = getMparametre("REFERENCEOPERATION",rep.getOperation().getCodeOperation());
                //  listeParam.add(param);
                //SettlementResponse settlementResponse = mobileService.invoiceSettlement(listeParam, codeFsePartenaire, "SETTLEMENT", null, rep.getOperation());

            } else {
                sendSettlement("-1", listeParam, codeFsePartenaire, extReference);
            }*/
            return record;
        }

        private SettlementResponse sendSettlement(String status, List<MParametre> listeParam, String codeFsePartenaire, String extReference) throws IOException, JAXBException {
            MParametre param = getMparametre("NOORDER", extReference);
            listeParam.add(param);
            param = getMparametre("STATUT", status);
            listeParam.add(param);
            
            SettlementResponse settlementResponse = apiService.settlement(listeParam, codeFsePartenaire);
            return settlementResponse;
        }

        private MParametre getMparametre(String codeParam, String valeur) {
            MParametre param = new MParametre();
            param.setCodeParametre(codeParam);
            param.setValeur(valeur);
            return param;
        }

        private MOperation getoperation(MInfos ben, String canal, String user, String acteur, String identifiant, String codePv,
                String codeUoEm, String codePvEm, String codeUo, String typeService, Long montant, Long montantfrais, Long montanttaxe) {
            MOperation operation = new MOperation();
            operation.setCanal(canal);
            operation.setUserConnecte(user);
            operation.setActeur(acteur);
            operation.setIdentifiant(identifiant);
            operation.setCodePv(codePv);
            operation.setBeneficiaire(ben);
            operation.setEmetteur(ben);
            operation.setCodeUoEm(codeUoEm);
            operation.setCodeUoAcq(codeUo);
            operation.setCodePvEm(codePvEm);
            operation.setMotif("CASH TO ACCOUNT");
            //   operation.setMontant(mt);
            operation.setApplication("PTI");
            operation.setMontantFrais(BigDecimal.valueOf(montantfrais));
            operation.setMontantTaxe(BigDecimal.valueOf(montanttaxe));
            operation.setTypeService(typeService);
            operation.setMontant(BigDecimal.valueOf(montant));
            operation.setMotif("M2");
            operation.setCodeUo(codeUo);
            /**
             * A PARAMETRE
             */
            operation.setCodePays("UO_0002");
            return operation;
        }

        private MInfos setPersonne(String codetel, String telephone, String nom, String prenom, String codeagence, String numCpt, String teneur, MTypeCompte typeCompte, String codebanque, String rib) {

            MInfos ben = new MInfos();
            MPersonne pers = new MPersonne();
            pers.setTelephone(telephone);
            pers.setNom(nom);
            pers.setCodeTelephone(codetel);
            pers.setPrenom(prenom);
            pers.setSexe(MSexe.MASCULIN);
            ben.setTitulaire(pers);
            MCompte cpt1 = new MCompte();
            cpt1.setCodeAgence(codeagence);
            cpt1.setCodeBanque(codebanque);
            cpt1.setNumeroCompte(numCpt);
            //cpt1.setRib("0");
            cpt1.setRib(rib);
            cpt1.setTypeCompte(typeCompte);
            cpt1.setTeneurCompte(teneur);
            ben.setCompte(cpt1);
            return ben;

        }

    }

}
