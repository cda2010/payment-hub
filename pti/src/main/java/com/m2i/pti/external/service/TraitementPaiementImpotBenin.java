/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.external.service;

import com.m2i.pti.domain.Facturier;
import com.m2i.pti.domain.UniteOrganisation;
import com.m2i.pti.domain.enumeration.TypePersonne;
import com.m2i.pti.mobile.dto.MCompte;
import com.m2i.pti.mobile.dto.MInfos;
import com.m2i.pti.mobile.dto.MOperationFacture;
import com.m2i.pti.mobile.dto.MFacturier;
import com.m2i.pti.mobile.dto.*;
import com.m2i.pti.mobile.dto.MPersonne;
import com.m2i.pti.mobile.enumeration.MSexe;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.FacturierRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.dto.FactureFournisseur;
import com.m2i.pti.service.dto.PersonneAllDTO;
import com.m2i.pti.service.easyBatch.Read;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import com.m2i.pti.web.rest.errors.CustomSettlementException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
public class TraitementPaiementImpotBenin {

    private final Logger log = LoggerFactory.getLogger(TraitementPaiementImpotBenin.class);
    private final Read readService;
    private final MobileService mobileService;
    ApiService apiService;
    private UniteOrganisationRepository uniteOrganisationRepository;
    private final FacturierRepository facturierRepository;

    public TraitementPaiementImpotBenin(UniteOrganisationRepository uniteOrganisationRepository, MobileService mobileService, Read readService, ApiService apiService, FacturierRepository facturierRepository) {
        this.readService = readService;
        this.mobileService = mobileService;
        this.apiService = apiService;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.facturierRepository = facturierRepository;
    }

    public void traitementOperationTransfert(List<FactureFournisseur> listFactFournisseur, String codeFsePartenaire, String codeFseTeneurCompte, String codeIndicatif, String codeUo1, String codeUo2, String montant, String isAmountCondition, String codePv, String typeCompte, String codebanque, String codeFacturier, String idProduit, String codeparametre) throws IOException {
        Job job = (Job) readService.getBatch(listFactFournisseur, new TraitementPaiementImpotBenin.PaiementReaderProcessor(uniteOrganisationRepository, apiService, mobileService, codeFsePartenaire, codeFseTeneurCompte, codeIndicatif, codeUo1, codeUo2, montant, isAmountCondition, codePv, typeCompte, codebanque, codeFacturier, idProduit, codeparametre));
    }

    public class PaiementReaderProcessor implements RecordProcessor {

        String codeFseTeneurCompte;
        String codeIndicatif;
        String codeFsePartenaire;
        String codeUo1;
        String codeUo2;
        String montant;
        String isAmountCondition;
        String codePv;
        String typeCompte;
        String codebanque;
        String codeFacturier;
        String idProduit;
        String codeparametre;
        private final MobileService mobileService;
        ApiService apiService;
        private UniteOrganisationRepository uniteOrganisationRepository;
        Long idFacturier;

        private String rechercheParam(List<MParametre> listeParametre, String param) {
            String valeur = null;
            Iterator it = listeParametre.iterator();
            MParametre elementParam;
            while (it.hasNext()) {
                elementParam = (MParametre) it.next();
                if (elementParam.getCodeParametre().equals(param)) {
                    return elementParam.getValeur();

                }

            }
            return valeur;
        }

        public PaiementReaderProcessor(UniteOrganisationRepository uniteOrganisationRepository, ApiService apiService, MobileService mobileService, String codeFsePartenaire, String codeFseTeneurCompte, String codeIndicatif, String codeUo1, String codeUo2, String montant, String isAmountCondition, String codePv, String typeCompte, String codebanque, String codeFacturier, String idProduit, String codeparametre) {
            this.codeFsePartenaire = codeFsePartenaire;
            this.codeFseTeneurCompte = codeFseTeneurCompte;
            this.codeIndicatif = codeIndicatif;
            this.codeUo1 = codeUo1;
            this.codeUo2 = codeUo2;
            this.montant = montant;
            this.isAmountCondition = isAmountCondition;
            this.codePv = codePv;
            this.typeCompte = typeCompte;
            this.apiService = apiService;
            this.uniteOrganisationRepository = uniteOrganisationRepository;
            this.mobileService = mobileService;
            this.codebanque = codebanque;
            this.codeFacturier = codeFacturier;
            this.idProduit = idProduit;
            this.codeparametre = codeparametre;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            FactureFournisseur facture = (FactureFournisseur) record.getPayload();
            List<MParametre> listeParamExtra = facture.getExtraParametres();
            String referencePaiement = facture.getReference();
            String referenceContribuable = rechercheParam(listeParamExtra, "REFERENCECONTRIBUABLE");
            String compteDebit = rechercheParam(listeParamExtra, "COMPTEDEBIT");
            log.debug("==referenceContribuable" + referenceContribuable);
            log.debug("==COMPTE DEBIT" + compteDebit);
            String nom = rechercheParam(listeParamExtra, "TIERS");;
            String prenom = "";
            String codeAgence = "0";
            MParametre param;

            String identifiant = "";
            String codeUoEm = "";
            String codeUo = "";

            MOperationFacture mOperation = null;
            List<MParametre> listeParam = new ArrayList();
            try {
                listeParam = getMparamtre(referencePaiement, codeparametre);
                /*A revoir, le FSE TENEUR DE COMPTE */
                if (typeCompte.equals("BANQUE")) {
                    UniteOrganisation fse = uniteOrganisationRepository.findByCodeFseAndSupprimeIsFalse(codeFseTeneurCompte);
                    /* IL S AGIT DU CODE FSE DU COMPTE **/
                    PersonneAllDTO personne = apiService.getClientInfo(compteDebit, "0", "0", fse.getId());
                    if (personne != null && personne.getCompteCBIDTO() != null) {
                        codeAgence = personne.getCompteCBIDTO().getCodeAgence();
                        if (personne.getPersonneDTO() != null && personne.getPersonneDTO().getTypePersonne().equals(TypePersonne.MORALE)) {
                            nom = personne.getPersonneMoraleDTO().getRaisonSociale();
                        } else {
                            nom = personne.getPersonnePhysiqueDTO().getNomClient();
                            prenom = personne.getPersonnePhysiqueDTO().getPrenomClient();
                        }
                    } else {
                        this.codebanque = "0";
                    }
                }
                param = getMparametre("CODEREJET", "0");
                listeParam.add(param);
                param = getMparametre("STATUT", "0");
                listeParam.add(param);
                Double mt = Double.parseDouble(facture.getMontant());
                //String identifiant = codeUo;
                // String codeUoEm = codeUo;
                if (verifCondition(Double.parseDouble(facture.getMontant()), Double.parseDouble(montant), isAmountCondition).equals(Boolean.TRUE)) {
                    log.info("==La condition est vraie==");
                    log.info("" + montant + " " + isAmountCondition + " " + facture.getMontant());
                    Facturier fac = facturierRepository.findByCodeUo(codeUo1);
                    codeUo = codeUo1;
                    identifiant = codeUo1;
                    codeUoEm = codeUo1;
                    idFacturier = fac.getId();
                    log.info("---codeUo--" + codeUo);
                    log.info("---codeUoEm--" + codeUoEm);
                    log.info("---idFacturier--" + fac.getCodeFacturier());

                } else {
                    log.info("==La condition n'est pas vraie==");
                    log.info("" + montant + " " + isAmountCondition + " " + facture.getMontant());
                    Facturier fac = facturierRepository.findByCodeUo(codeUo2);
                    codeUo = codeUo2;
                    identifiant = codeUo2;
                    codeUoEm = codeUo2;
                    idFacturier = fac.getId();
                    log.info("---codeUo--" + codeUo);
                    log.info("---codeUoEm--" + codeUoEm);
                    log.info("---idFacturier--" + fac.getCodeFacturier());
                }

                MInfos em = setPersonne(codeIndicatif, referenceContribuable, nom, prenom, codeAgence, compteDebit, codeFseTeneurCompte, MTypeCompte.BANQUE, this.codebanque);

                mOperation = getoperation(em, "MOBILE", "", "CLIENT", identifiant, codePv, codeUoEm, codePv, codeUo, "PAIEMENT_FACTURE", mt.longValue(), 0L, 0L, referencePaiement, codeFacturier, idProduit, codeparametre, idFacturier, listeParam);
                MResponse msgResponse = mobileService.doOperationPrelevement(mOperation);
                //msgResponse.getTansactionId();
                if(!msgResponse.getCode().equals("0")){
                    log.debug("==ERREUR=====" + msgResponse.getMessage());
                    param = getMparametre("CODEREJET", "999");
                    listeParam.add(param);
                    param = getMparametre("STATUT", "-1");
                    listeParam.add(param);
                    param = getMparametre("REFERENCEOPERATION", "0");
                    listeParam.add(param);
                    SettlementResponse settlementResponse = mobileService.invoiceSettlement(listeParam, codeFsePartenaire, "SETTLEMENT", null, mOperation);
                }
            } catch (CustomSettlementException | CustomParameterizedException e) {
                /**
                 * En cas d'erreur rejeter la transaction*
                 */
                log.debug("==ERREUR=====" + e.getMessage());
                param = getMparametre("CODEREJET", "999");
                listeParam.add(param);
                param = getMparametre("STATUT", "-1");
                listeParam.add(param);
                param = getMparametre("REFERENCEOPERATION", "0");
                listeParam.add(param);
                SettlementResponse settlementResponse = mobileService.invoiceSettlement(listeParam, codeFsePartenaire, "SETTLEMENT", null, mOperation);

            }

            return record;
        }

        private MParametre getMparametre(String codeParam, String valeur) {
            MParametre param = new MParametre();
            param.setCodeParametre(codeParam);
            param.setValeur(valeur);
            return param;
        }

        private List<MParametre> getMparamtre(String referencePaiement, String codeParametre) {
            MParametre param = new MParametre();
            param.setCodeParametre(codeParametre);
            param.setValeur(referencePaiement);
            List<MParametre> listeParam = new ArrayList();
            listeParam.add(param);
            return listeParam;
        }

        private MOperationFacture getoperation(MInfos ben, String canal, String user, String acteur, String identifiant, String codePv,
                String codeUoEm, String codePvEm, String codeUo, String typeService, Long montant, Long montantfrais, Long montanttaxe, String referencePaiement, String codeFacturier, String idProduit, String codeParametre, Long idFacturier, List<MParametre> listeParam) {
            MOperationFacture operation = new MOperationFacture();
            operation.setCanal(canal);
            operation.setUserConnecte(user);
            operation.setActeur(acteur);
            operation.setIdentifiant(identifiant);
            operation.setCodePv(codePv);
            operation.setBeneficiaire(ben);
            operation.setEmetteur(ben);
            operation.setApplication("PTI");
            operation.setReferenceFacture(referencePaiement);
            operation.setMotif("PAIEMENT DES IMPOTS");
            MFacturier facturier = new MFacturier();
            facturier.setCodeFacturier(codeFacturier);
            facturier.setId(idFacturier);
            MProduit prod = new MProduit();
            prod.setId(Long.parseLong(idProduit));
            operation.setProduit(prod);

            operation.setListParam(listeParam);
            operation.setFacturier(facturier);
            //   operation.setMontant(mt);
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

        private MInfos setPersonne(String codetel, String telephone, String nom, String prenom, String codeagence, String numCpt, String teneur, MTypeCompte typeCompte, String codebanque) {
            MInfos em = new MInfos();
            MPersonne pers = new MPersonne();
            log.debug("==codetel" + codetel);
            log.debug("==telephone " + telephone);
            log.debug("==numCpt" + numCpt);
            log.debug("==codeagence " + codeagence);
            log.debug("==codebanque " + codebanque);
            pers.setTelephone(telephone);
            pers.setNom(nom);
            pers.setCodeTelephone(codetel);
            pers.setPrenom(prenom);
            pers.setSexe(MSexe.MASCULIN);
            em.setTitulaire(pers);
            MCompte cpt1 = new MCompte();
            cpt1.setCodeAgence(codeagence);
            cpt1.setCodeBanque(codebanque);
            cpt1.setNumeroCompte(numCpt);
            cpt1.setRib("0");
            cpt1.setTypeCompte(typeCompte);
            cpt1.setTeneurCompte(teneur);
            em.setCompte(cpt1);
            return em;

        }

        public Boolean verifCondition(Double montantFac, Double montantCondition, String condition) {
            Boolean bool = false;
            log.info("==Debut vérifCondition=====");
            log.info("==montantFac=====" + montantFac);
            log.info("==montantCondition=====" + montantCondition);
            log.info("==condition=====" + condition);

            if ("SUPERIEUR".equals(condition)) {
                if (montantFac > montantCondition) {
                    bool = true;
                }
            } else if ("SUPERIEUR_OU_EGALE".equals(condition)) {
                if (montantFac >= montantCondition) {
                    bool = true;
                }
            } else if ("INFERIEUR_OU_EGALE".equals(condition)) {
                if (montantFac <= montantCondition) {
                    bool = true;
                }
            } else if ("INFERIEUR".equals(condition)) {
                if (montantFac < montantCondition) {
                    bool = true;
                }
            }
            log.info("==bool=====" + bool);
            return bool;
        }

    }

}
