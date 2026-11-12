/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.external.service;

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
import com.m2i.pti.service.CompteService;
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
public class TraitementPaiementGuceg {

    private final Logger log = LoggerFactory.getLogger(TraitementPaiementGuceg.class);
    private final Read readService;
    private final MobileService mobileService;
    ApiService apiService;
    private UniteOrganisationRepository uniteOrganisationRepository;
    private final CompteService compteService;

    public TraitementPaiementGuceg(UniteOrganisationRepository uniteOrganisationRepository, MobileService mobileService, Read readService, ApiService apiService, CompteService compteService) {
        this.readService = readService;
        this.mobileService = mobileService;
        this.apiService = apiService;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.compteService = compteService;
    }

    public void traitementOperationTransfert(List<FactureFournisseur> listFactFournisseur, String codeFsePartenaire, String codeFseTeneurCompte, String codeIndicatif, String codeUo, String codePv, String typeCompte, String codebanque, String codeFacturier, String idProduit, String codeparametre, Long facId, String codeValide, String codeRejet) throws IOException {
        Job job = (Job) readService.getBatch(listFactFournisseur, new TraitementPaiementGuceg.PaiementReaderProcessor(uniteOrganisationRepository, apiService, mobileService, compteService, codeFsePartenaire, codeFseTeneurCompte, codeIndicatif, codeUo, codePv, typeCompte, codebanque, codeFacturier, idProduit, codeparametre, facId, codeValide, codeRejet));
    }

    public class PaiementReaderProcessor implements RecordProcessor {

        String codeIndicatif;
        String codeFseTeneurCompte;
        String codeFsePartenaire;
        String codeUo;
        String codePv;
        String typeCompte;
        String codebanque;
        String codeFacturier;
        String idProduit;
        String codeparametre;
        private final MobileService mobileService;
        ApiService apiService;
        CompteService compteService;
        private UniteOrganisationRepository uniteOrganisationRepository;
        Long idFacturier;
        String codeValide;
        String codeRejet;

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

        public PaiementReaderProcessor(UniteOrganisationRepository uniteOrganisationRepository, ApiService apiService, MobileService mobileService, CompteService compteService, String codeFsePartenaire, String codeFseTeneurCompte, String codeIndicatif, String codeUo, String codePv, String typeCompte, String codebanque, String codeFacturier, String idProduit, String codeparametre, Long idFacturier, String codeValide, String codeRejet) {
            this.codeFsePartenaire = codeFsePartenaire;
            this.codeFseTeneurCompte = codeFseTeneurCompte;
            this.codeIndicatif = codeIndicatif;
            this.codeUo = codeUo;
            this.codePv = codePv;
            this.typeCompte = typeCompte;
            this.apiService = apiService;
            this.compteService = compteService;
            this.uniteOrganisationRepository = uniteOrganisationRepository;
            this.mobileService = mobileService;
            this.codebanque = codebanque;
            this.codeFacturier = codeFacturier;
            this.idProduit = idProduit;
            this.codeparametre = codeparametre;
            this.idFacturier = idFacturier;
            this.codeValide = codeValide;
            this.codeRejet = codeRejet;
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
            MOperationFacture mOperation = null;
            List<MParametre> listeParam = new ArrayList();
            listeParam = getMparamtre(referencePaiement, codeparametre);
            param = getMparametre(codeparametre, referencePaiement);
            listeParam.add(param);
            if (listeParamExtra != null) {
                listeParam = listeParamExtra;
            }

            try {
                System.out.println("-----------Enttrer du try---------------");


                /*A revoir, le FSE TENEUR DE COMPTE */
                //if (typeCompte.equals("BANQUE")) {
                UniteOrganisation fse = uniteOrganisationRepository.findByCodeFseAndSupprimeIsFalse(codeFseTeneurCompte);
                /* IL S AGIT DU CODE FSE DU COMPTE **/
                //PersonneAllDTO personne = apiService.getClientInfo(compteDebit, "0", "0", fse.getId());
                MInfos infoPersonne = compteService.findByalias(referenceContribuable, fse.getCodeFse());

                if (infoPersonne != null) {
                    //codeAgence = personne.getCompteCBIDTO().getCodeAgence();
                    if (infoPersonne.getCompte() != null) {
                        codeAgence = infoPersonne.getCompte().getCodeAgence();
                        System.out.println("--codeAgence---");
                        System.out.println(codeAgence);
                        System.out.println("--codeAgence---");

                    }
                    if (infoPersonne.getTitulaire() != null) {
                        if (infoPersonne.getTitulaire().getNom() != null) {
                            nom = infoPersonne.getTitulaire().getNom();
                        } else {
                            nom = "Guceg";
                        }
                        System.out.println("--nom---");
                        System.out.println(nom);
                        System.out.println("--nom---");

                        if (infoPersonne.getTitulaire().getPrenom() != null) {
                            prenom = infoPersonne.getTitulaire().getPrenom();
                        } else {
                            prenom = "Guceg";
                        }
                        System.out.println("--prenom---");
                        System.out.println(prenom);
                        System.out.println("--prenom---");

                        //log.info("---nom--"+nom);
                        // log.info("---prenom--"+prenom);
                        /*if (infoPersonne.getPersonneDTO() != null && personne.getPersonneDTO().getTypePersonne().equals(TypePersonne.MORALE)) {
                                nom = personne.getPersonneMoraleDTO().getRaisonSociale();
                            } else {
                                nom = personne.getPersonnePhysiqueDTO().getNomClient();
                                prenom = personne.getPersonnePhysiqueDTO().getPrenomClient();
                            }*/
                    }
                    /* if (personne.getPersonneDTO() != null && personne.getPersonneDTO().getTypePersonne().equals(TypePersonne.MORALE)) {
                            nom = personne.getPersonneMoraleDTO().getRaisonSociale();
                        } else {
                            nom = personne.getPersonnePhysiqueDTO().getNomClient();
                            prenom = personne.getPersonnePhysiqueDTO().getPrenomClient();
                        }*/

                } else {
                    this.codebanque = "0";
                }
                //}

                //if (typeCompte.equals("BANQUE")) {
                //UniteOrganisation fse = uniteOrganisationRepository.findByCodeFseAndSupprimeIsFalse(codeFseTeneurCompte);
                /* IL S AGIT DU CODE FSE DU COMPTE **/
 /*PersonneAllDTO personne = apiService.getClientInfo(compteDebit, "0", "0", fse.getId());
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
                }*/
                System.out.println("--codeRejet---");
                System.out.println(codeRejet);
                System.out.println("--codeRejet---");
                System.out.println("--codeValide---");
                System.out.println(codeValide);
                System.out.println("--codeValide---");
                if (codeRejet != null && codeRejet != "") {
                    // param = getMparametre("STATUT", codeRejet);
                } else {
                    param = getMparametre("CODEREJET", "0");
                }
                listeParam.add(param);
                if (codeValide != null && codeValide != "") {
                    param = getMparametre("STATUS", codeValide);
                } else {
                    param = getMparametre("STATUS", "PD");
                }
                listeParam.add(param);

                System.out.println("--listeParam---");
                System.out.println(listeParam.toString());
                System.out.println("--listeParam---");

                Double mt = Double.parseDouble(facture.getMontant());
                String identifiant = codeUo;
                String codeUoEm = codeUo;
                MInfos em = setPersonne(codeIndicatif, referenceContribuable, nom, prenom, codeAgence, compteDebit, codeFseTeneurCompte, MTypeCompte.BANQUE, this.codebanque);
                mOperation = getoperation(em, "MOBILE", "", "CLIENT", identifiant, codePv, codeUoEm, codePv, codeUo, "PAIEMENT_FACTURE", mt.longValue(), 0L, 0L, referencePaiement, codeFacturier, idProduit, codeparametre, idFacturier, listeParam);
                mobileService.doOperationPrelevement(mOperation);
            } catch (CustomSettlementException | CustomParameterizedException e) {
                System.out.println("-----------Entrer du catch---------------");
                System.out.println(e);
                System.out.println("-----------Fin du catch---------------");

                /**
                 * En cas d'erreur rejeter la transaction*
                 */
                log.debug("==ERREUR=====" + e.getMessage());

                //code ajouté
                for (MParametre mparam : listeParam) {
                    //System.out.println(couleur);
                    if (mparam.getCodeParametre().equals("STATUS")){
                        mparam.setValeur(codeRejet);
                    }
                    if (mparam.getCodeParametre().equals("CODEREJET")){
                        mparam.setValeur("999");
                    }  
                }

                /*param = getMparametre("CODEREJET", "999");
                listeParam.add(param);

                if (codeRejet != null && codeRejet != "") {
                    // param = getMparametre("STATUT", codeRejet);
                    param = getMparametre("STATUS", codeRejet);
                } else {
                    param = getMparametre("STATUS", "RJ");
                }

                //param = getMparametre("STATUS", "RJ");
                listeParam.add(param);*/
                param = getMparametre("REFERENCEOPERATION", "0");
                listeParam.add(param);

                System.out.println("-----------param---------------");
                System.out.println(listeParam);
                System.out.println("-----------param---------------");
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

    }

}
