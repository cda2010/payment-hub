package com.m2i.pti.batch.reader.validation;

import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.pti.batch.reader.*;
import com.m2i.pti.domain.ChargementTransfert;
import com.m2i.pti.domain.Compte;
import com.m2i.pti.domain.LigneChargementTransfert;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.Personne;
import com.m2i.pti.domain.enumeration.Etape;
import com.m2i.pti.domain.enumeration.EtatCompte;
import com.m2i.pti.domain.enumeration.StatutCompte;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.domain.enumeration.TypeEvenementOperation;
import com.m2i.pti.domain.enumeration.TypePersonne;
import com.m2i.pti.mobile.dto.MCompte;
import com.m2i.pti.mobile.dto.MInfos;
import com.m2i.pti.mobile.dto.MOperation;
import com.m2i.pti.mobile.dto.MPersonne;
import com.m2i.pti.mobile.dto.MResponseOperation;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.service.ChargementTransfertService;
import com.m2i.pti.service.CompteService;
import com.m2i.pti.service.EvenementOperationService;
import com.m2i.pti.service.LigneChargementTransfertService;
import com.m2i.pti.service.TransactionComptePtiService;
import com.m2i.pti.web.rest.errors.ChargementException;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.List;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;

public class ValidationLigneChargementTransfertWriter implements ItemWriter<LigneChargementTransDTO> {

    @Autowired
    CompteService compteService;

    @Autowired
    LigneChargementTransfertService ligneChargementTransfertService;

    @Autowired
    OperationRepository operationRepository;
    @Autowired
    MobileService mobileService;

    @Value("#{jobParameters['chargementId']}")
    Long chargementId;
    ChargementTransfert chargement;
    @Autowired
    ChargementTransfertService chargementTransfertService;
    @Autowired
    TransactionComptePtiService transactionComptePtiService;
    private final Logger log = LoggerFactory.getLogger(CustomerProcessor.class);
    @Autowired
    EvenementOperationService evenementOperationService;
    PassiveExpiringMap<String, Compte> compteDebitMapDto = new PassiveExpiringMap<>(600000);
    PassiveExpiringMap<String, Operation> operationMapDto = new PassiveExpiringMap<>(600000);

    private static MInfos getEmeteur(Compte compteDebit) {
        Personne pers = compteDebit.getClient().getPersonne();
        MInfos emetteur = new MInfos();
        if (pers.getTypePersonne().equals(TypePersonne.MORALE)) {
            MPersonne persE = new MPersonne();
            MCompte cptE = new MCompte();
            // MPersonne Emetteur
            persE.setNom(pers.getPersonneMorale().getRaisonSociale());
            persE.setPrenom(pers.getPersonneMorale().getRaisonSociale());
            persE.setTelephone(pers.getTelephone());
            persE.setCodeTelephone(pers.getCodePaysTelephone());
            cptE.setCodeAgence("");
            cptE.setCodeBanque("");
            cptE.setNumeroCompte(compteDebit.getValeurCompte());
            cptE.setTeneurCompte(compteDebit.getCompteAutre().getCodeFse());
            // if (compteDebit.getTypeCompte().equals(CategorieCompte.BANQUE)) {
            //   cptE.setTypeCompte(MTypeCompte.BANQUE);
            // } else {
            cptE.setTypeCompte(MTypeCompte.AUTRE);
            //}
            emetteur.setTitulaire(persE);
            emetteur.setCompte(cptE);
        }
        return emetteur;
    }

    private static MInfos getBeneficiaire(LigneChargementTransfert lgCharge, Compte compteCredit) {

        MInfos benef = new MInfos();
        //Compte compteCredit = compteService.findByCompteFse(ligneChargementTransfert.getNumeroTelephone(), this.fournisseurMonnaie);

        MPersonne persE = new MPersonne();

        // MPersonne Emetteur
        persE.setNom(lgCharge.getNom());
        persE.setPrenom(lgCharge.getPrenom());
        persE.setTelephone(lgCharge.getNumeroTelephone());
        persE.setCodeTelephone(lgCharge.getChargementTransfert().getCodePays());

        benef.setTitulaire(persE);
        if (compteCredit != null) {
            MCompte cptE = new MCompte();
            cptE.setCodeAgence("");
            cptE.setCodeBanque("");
            cptE.setNumeroCompte(compteCredit.getValeurCompte());
            cptE.setTeneurCompte(compteCredit.getCompteAutre().getCodeFse());
            cptE.setTypeCompte(MTypeCompte.AUTRE);
            benef.setCompte(cptE);
        }

        return benef;
    }

    public Operation functionchargementLigne(Long idLigne, Long compteDebit, Long operationId) throws IOException, NoSuchAlgorithmException, JAXBException {
        MResponseOperation response = null;
        Compte cptDebit;

        if (compteDebitMapDto.containsKey(compteDebit.toString()) && compteDebitMapDto.get(compteDebit.toString()) != null) {
            cptDebit = compteDebitMapDto.get(compteDebit.toString());
        } else {
            cptDebit = compteService.findCompteOne(compteDebit);
            if (cptDebit != null) {
                compteDebitMapDto.put(compteDebit.toString(), cptDebit);
            }

        }

        Operation operation;
        if (operationMapDto.containsKey(operationId.toString()) && operationMapDto.get(operationId.toString()) != null) {
            operation = operationMapDto.get(operationId.toString());
        } else {
            operation = operationRepository.findByOperationId(operationId);
            if (operation != null) {
                operationMapDto.put(operationId.toString(), operation);
            }

        }

        LigneChargementTransfert ligneChargementTransfert = ligneChargementTransfertService.findOne(idLigne);

        StopWatch stopWatch = new StopWatch("Measure Code Execution");

        try {
            if (ligneChargementTransfert != null && !ligneChargementTransfert.getStatutLigne().equals(StatutLigneChargement.OC)) {
                ResultMessage resMessage;  //  (ResultMessage)apiService.convert(result.getBody(),ResultMessage.class);
                ResultMessage resMessageEnvoiMsg;
                String typeService = "";
                MOperation mOperation = new MOperation();
                DecimalFormat df = new DecimalFormat("###");
                // new BigDecimal(ligneChargementTransfert.getMontant())
                //   ligneChargementTransfert.setStatutLigne(StatutLigneChargement.IC);
                ligneChargementTransfert.setSoldeAvant(0D);
                Compte compteCredit = null;
                //il s'agit d'un transfert de carte à carte

                //               le débit est constaté et la carte désactivé
                BigDecimal montantApres = BigDecimal.valueOf(ligneChargementTransfert.getMontant());
                if (ligneChargementTransfert.getChargementTransfert().getTypeTransfert() != 2) {// cas des reglations en masse
                    montantApres = cptDebit.getCompteAutre().getSolde().subtract(BigDecimal.valueOf(Double.parseDouble(ligneChargementTransfert.getMontant().toString())));

                }
                //tra

                if (ligneChargementTransfert.getChargementTransfert().getTypeTransfert().equals(0)) { //transert vers compte
                    typeService = "TRANSFERT_MASSE";
                    if (ligneChargementTransfert.getCompteId() != null) {
                        compteCredit = compteService.findCompteOne(ligneChargementTransfert.getCompteId());
                    } else {
                        ligneChargementTransfert.setError("Compte introuvable");
                        throw new ChargementException(ligneChargementTransfert.getError());
                    }
                    mOperation.setBeneficiaire(getBeneficiaire(ligneChargementTransfert, compteCredit));
                    // compteCredit = mobileService.findComptePersonne(ligneChargementTransfert.getNumeroTelephone(), ligneChargementTransfert.getChargementTransfert().getCodePays());
                    if (compteCredit != null) {
                        if (compteCredit.getEtatCompte().equals(EtatCompte.FERME) || compteCredit.getStatutCompte().equals(StatutCompte.DESACTIVE)) {
                            ligneChargementTransfert.setError("Compte bloqué");
                            throw new ChargementException(ligneChargementTransfert.getError());
                        }
                    } else {
                        ligneChargementTransfert.setError("Compte introuvable");
                        throw new ChargementException(ligneChargementTransfert.getError());
                    }
                } else if (ligneChargementTransfert.getChargementTransfert().getTypeTransfert().equals(1)) {
                    typeService = "TRANSFERT_MASSE_CASH";
                } else if (ligneChargementTransfert.getChargementTransfert().getTypeTransfert().equals(2)) {
                    typeService = "TRANSFERT_MASSE_DEBIT";
                    // compteCredit = compteService.findCompteOne(ligneChargementTransfert.getCompteId());;
                    if (ligneChargementTransfert.getCompteId() != null) {
                        compteCredit = compteService.findCompteOne(ligneChargementTransfert.getCompteId());
                    } else {
                        ligneChargementTransfert.setError("Compte introuvable");
                        throw new ChargementException(ligneChargementTransfert.getError());
                    }
                    mOperation.setEmetteur(getBeneficiaire(ligneChargementTransfert, compteCredit));
                    mOperation.setBeneficiaire(getEmeteur(cptDebit));
                    if (compteCredit != null) {

                    } else {
                        ligneChargementTransfert.setError("Compte introuvable");
                        throw new ChargementException(ligneChargementTransfert.getError());
                    }
                }
                if (montantApres.doubleValue() >= 0) {
                    String narration = "PAIEMENT EN MASSE";
                    mOperation.setMontant(BigDecimal.valueOf(ligneChargementTransfert.getMontant()));
                    mOperation.setMotif(ligneChargementTransfert.getChargementTransfert().getMotifOperation());
                    mOperation.setCodePv(operation.getPointVente().getCodePointVente());
                    log.debug("POINT VENTE:" + operation.getPointVente().getCodePointVente());
                    mOperation.setTypeService(typeService);
                    mOperation.setCanal("MOBILE");
                    mOperation.setDescription(typeService + " REF: " + ligneChargementTransfert.getChargementTransfert().getReference());
                    mOperation.setCodeUo(operation.getUoEmetteur().getCodeUO());
                    mOperation.setCodePays(operation.getUoAcquereur().getCodeUO());
                    if (ligneChargementTransfert.getNarration() != null) {
                        narration = ligneChargementTransfert.getNarration();
                    }
                    Integer nombreLigneCharge = 1;

                    response = mobileService.doOperationChargement(mOperation, operation, narration, nombreLigneCharge, compteCredit, ligneChargementTransfert.getId());
                    log.debug("====XXXXXXXXXX=============TRANSACTION EXISTE=============");
                    if (response != null && response.getCode() != null && response.getCode().equals("0")) {

                        ligneChargementTransfert.setSoldeApres(0D);
                        ligneChargementTransfert.setStatutLigne(StatutLigneChargement.OC);
                        ligneChargementTransfert.setDescription(mOperation.getDescription()+" "+narration);
                        operation.setMontantFrais(BigDecimal.ZERO);
                        operation.setMontantTaxe(BigDecimal.ZERO);
                        if (response.getOperation() != null) {
                            /** SI CHARGEMENT RETRAIT GRATUIT********** */
                            if (ligneChargementTransfert.getChargementTransfert().getRetraitGratuit()) {
                                evenementOperationService.saveEvt(response.getOperation(), compteCredit, TypeEvenementOperation.TRANSFERT_GRATUIT.toString(), mOperation.getMontant(), narration, "");
                            }

                            log.debug("==============TRANSACTION EXISTE=============");
                            ligneChargementTransfert.setCodeOperation(response.getOperation().getCodeOperation());
                            long compte = transactionComptePtiService.findExisteOperation(response.getOperation().getCodeOperation(), compteCredit.getCompteAutre().getId());
                            if (compte > 0) {
                                ligneChargementTransfert.setSoldeApres(0D);
                                ligneChargementTransfert.setStatutLigne(StatutLigneChargement.OC);
                                log.debug("TRANSACTION EXISTE");
                            } else {
                                ligneChargementTransfert.setError(response.getOperation().getError());
                                ligneChargementTransfert.setStatutLigne(StatutLigneChargement.NC);
                            }

                            //  mobileService.envoiDesMessage(response.getOperation(), null);
                            //  } else {
                            //  ligneChargementTransfert.setError("ERREUR LORS DU CHARGEMENT");
                            //ligneChargementTransfert.setStatutLigne(StatutLigneChargement.NC);
                            //log.debug("ERROR=== DE CHARGEMENT");
                            /// }
                            ligneChargementTransfertService.save(ligneChargementTransfert);
                            return response.getOperation();
                        }

                    }else{
                        if(response.getCode().equals("-1")){
                        
                            ligneChargementTransfert.setError(response.getMessage());
                            ligneChargementTransfert.setDescription(mOperation.getDescription()+" "+narration);
                            ligneChargementTransfertService.save(ligneChargementTransfert);
                        }
                            
                    }

                } else {

                    ligneChargementTransfert.setError("Solde insuffisant");
                }

            } else {

                ligneChargementTransfert.setError("Compte déjà crédité");
            }

            ligneChargementTransfertService.save(ligneChargementTransfert);

        } catch (Exception e) {
            ligneChargementTransfert.setError(e.getMessage());
            ligneChargementTransfertService.save(ligneChargementTransfert);
        }

        return null;
    }

//    @Override
//    public Operation process(LigneChargementTransDTO ligneChargementTransDTO) throws Exception {
//        // if (customer.getCountry().equals("United States")) {
//        Operation oper = functionchargementLigne(ligneChargementTransDTO.getIdLigneChargement(), ligneChargementTransDTO.getCompteId(), ligneChargementTransDTO.getIdOperation());
//        //saveLigneChargement(csvCarte,result,TypeTransaction.CREDIT);              
//        return oper;
//        //    } else {
//        //      return null;
//        //  }
//    }
    @Override
    public void write(Chunk<? extends LigneChargementTransDTO> list) throws Exception {
        log.debug("REST list : {}", list);
        System.out.println("Thread Name : -" + Thread.currentThread().getName());
        for (LigneChargementTransDTO ligneChargementTransDTO : list) {
            Operation oper = functionchargementLigne(ligneChargementTransDTO.getIdLigneChargement(), ligneChargementTransDTO.getCompteId(), ligneChargementTransDTO.getIdOperation());
            if (oper != null) {
                mobileService.envoiDesMessage(oper, null);

            }

        }
//        list.parallelStream()
//                .forEach(
//                        operation -> {
//                            try {
//                               
//                            } catch (Exception ex) {
//
//                            }
//                        });

    }
}
