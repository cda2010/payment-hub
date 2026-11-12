/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.pticlientm;

import com.google.gson.Gson;
import com.pti.emeraude.gateway.PtiSetup;
import com.pti.emeraude.gateway.domain.*;
import com.pti.emeraude.gateway.enumeration.MSexe;
import com.pti.emeraude.gateway.enumeration.MTypeCompte;
import com.pti.emeraude.gateway.enumeration.TypeService;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author cda
 */
public class Pticlient {

    private MOperationFacture paiementFacture() throws IllegalAccessException, IOException, InstantiationException {
        PtiSetup pti = new PtiSetup();
        pti.setPlainClientCredentials("$2a$10$bXAyslP4KRJzwraJCLccpOCWd583.f/U2TGPV6a9WP50FcNNrbaF.");
        pti.setPassword("$2a$10$bXAyslP4KRJzwraJCLccpOCWd583.f/U2TGPV6a9WP50FcNNrbaF.");
        pti.setClientID("ptimobile");
        pti.setUrl("http://localhost:9180");
        pti.setGrantUri("?grant_type=client_credentials&scope=read&client_secret=" + pti.getPlainClientCredentials() + "&client_id=" + pti.getClientID());
        MOperationFacture operation = new MOperationFacture();
        MClient mclient = new MClient();
        MPersonne pers = new MPersonne();
        MPersonne persb = new MPersonne();
        MDocIdentification doc = new MDocIdentification();
        doc.setReference("B5588889");
        pers.setNom("VVVVVVVXXXX");
        pers.setPrenom("wxxxxxx");
        pers.setTelephone("73061292");
        pers.setCodeTelephone("226");
        persb.setNom("VVVVVVVXXXX");
        persb.setPrenom("wxxxxxx");
        persb.setTelephone("73061292");
        persb.setCodeTelephone("226");
        mclient.setDocIdentification(doc);
        mclient.setPersonne(pers);
        MCompte cpt1 = new MCompte();
        cpt1.setCodeAgence("10002");
        cpt1.setCodeBanque("CMFBF");
        cpt1.setNumeroCompte("01069724001");
        cpt1.setRib("0");
        cpt1.setTypeCompte(MTypeCompte.BANQUE);
        cpt1.setTeneurCompte("0056");
        mclient.setCompte(cpt1);
        MInfos ben = new MInfos();
        ben.setTitulaire(persb);

        MInfos emt = new MInfos();
        emt.setTitulaire(pers);
        emt.setCompte(cpt1);
        operation.setEmetteur(emt);
        operation.setBeneficiaire(ben);
        operation.setCanal("MOBILE");

        operation.setUserConnecte("isanfo");
        operation.setActeur("ORGANISATION");
        operation.setCodePv("0001291280");
        operation.setCanal("MOBILE");
        operation.setMontantFrais(BigDecimal.valueOf(200));
        operation.setReferenceFacture("21071619831");
        operation.setMontantTaxe(BigDecimal.valueOf(10));
        operation.setCodeUo("0001291");
        operation.setCodePays("UO_0002");
        operation.setMontant(BigDecimal.valueOf(100));
        operation.setIdentifiant("73061292");
        MFacturier facturier = new MFacturier();
        facturier.setCodeFacturier("FAC-MESO");
        facturier.setId(94L);
        facturier.setCodeUo("0010719");
        MProduit p = new MProduit();
        p.setId(100L);
        operation.setProduit(p);

        MParametre pa = new MParametre();

        pa.setCodeParametre("meterNo");
        pa.setValeur("07041704611");

        List<MParametre> listeParam = new ArrayList();

        pa = new MParametre();
        pa.setCodeParametre("NBRE");
        pa.setValeur("1");
        listeParam.add(pa);

        listeParam.add(pa);
        pa = new MParametre();
        pa.setCodeParametre("MONTANT");
        pa.setValeur("100");
        listeParam.add(pa);

        pa = new MParametre();

        pa.setCodeParametre("CODEFSEFACTURIER");
        pa.setValeur("0011");
        listeParam.add(pa);

        pa = new MParametre();
        pa.setCodeParametre("CODEOPERATEUR");
        pa.setValeur("0059");
        listeParam.add(pa);
        pa = new MParametre();
        pa.setCodeParametre("AGENCE");
        pa.setValeur("226");
        listeParam.add(pa);

        pa = new MParametre();
        pa.setCodeParametre("CODETEL");
        pa.setValeur("00000");
        listeParam.add(pa);

        pa = new MParametre();
        pa.setCodeParametre("PHONENUMBER");
        pa.setValeur("00000");
        listeParam.add(pa);

        pa = new MParametre();
        pa.setCodeParametre("TEL");
        pa.setValeur("00000");
        listeParam.add(pa);

        pa = new MParametre();
        pa.setCodeParametre("NUMEROCOMPTE");
        pa.setValeur("62534836");
        listeParam.add(pa);
        pa = new MParametre();
        pa.setCodeParametre("NUMEROTELEPHONE");
        pa.setValeur("22673061292");
        listeParam.add(pa);

        pa = new MParametre();
        pa.setCodeParametre("REFERENCE");
        pa.setValeur("2022101842.B432");
        listeParam.add(pa);

        pa = new MParametre();
        pa.setCodeParametre("VERIFYDATA");
        pa.setValeur("091817367115789349");
        listeParam.add(pa);

        pa = new MParametre();
        pa.setCodeParametre("SESSION_ID");
        pa.setValeur("091817367115789349");
        listeParam.add(pa);

        pa = new MParametre();
        pa.setCodeParametre("TRANSACTIONID");
        pa.setValeur("091817367115789349");
        listeParam.add(pa);

        pa = new MParametre();
        pa.setCodeParametre("CODETEL");
        pa.setValeur("ORA");
        listeParam.add(pa);

        // operation.setListParam(listParam);
        //     facturier.setCodeUo("0019388");
        operation.setTypeService(TypeService.PAIEMENT_FACTURE.toString());
        operation.setFacturier(facturier);
        operation.setListParam(listeParam);
        MListParametreProduit listpr = new MListParametreProduit();
        //    listpr.setIdProduit(23L);
        listpr.setMparametre(listeParam);
        // List<MParametre> pm=    pti.getRechercheInfos(listpr );
        //   pti.getFacture(listpr);

        //   MResponse r = pti.doOperationPaiementCredit(operation);
        //  MResponse r = pti.doOperationPaiementFactureCASH(operation);
        //MResponse r = pti.doOperationPaiementFacture(operation);
        pti.doExternalOperationPaiementFacture(operation, "0059", "PAIEMENT_FACTURE");
        //   pti.getPrint(listpr);
        return operation;
    }

    private void paiementFacture2() throws IllegalAccessException, IOException, InstantiationException {
        PtiSetup pti = new PtiSetup();
        pti.setPlainClientCredentials("$2a$10$bXAyslP4KRJzwraJCLccpOCWd583.f/U2TGPV6a9WP50FcNNrbaF.");
        pti.setPassword("$2a$10$bXAyslP4KRJzwraJCLccpOCWd583.f/U2TGPV6a9WP50FcNNrbaF.");
        pti.setClientID("ptimobile");
        pti.setUrl("http://localhost:9002");

        pti.setGrantUri("?grant_type=client_credentials&scope=read&client_secret=" + pti.getPlainClientCredentials() + "&client_id=" + pti.getClientID());
        MListParametreProduit listpr = new MListParametreProduit();
        listpr.setIdProduit(21L);
        List<MParametre> listeParam = new ArrayList();
        MParametre pa = new MParametre();
        pa.setCodeParametre("DATA");
        pa.setValeur("OPTION");
        listeParam.add(pa);
        listpr.setMparametre(listeParam);
        pti.getDataInfos(listpr);

    }

    private MOperation getoperation(String canal, String user, String acteur, String identifiant, String codePv,
            String codeUoEm, String codePvEm, String codeUo, String typeService, Long montant, Long montantfrais, Long montanttaxe) {
        MOperation operation = new MOperation();
        operation.setCanal(canal);
        // operation.setApplication("PTI");
        //   MInfos emet = setPersonne("CMFBF", "226", "73061292", "DA", "PATRICK", "10000", "01038524001", "0056", MTypeCompte.BANQUE, null);

        //      MInfos ben = setPersonne("CMFBF", "226", "57478955x", "DA", "PATRICK", "10002", "00365724001", "0056", MTypeCompte.BANQUE,null);
        //   MInfos emet =setPersonne("BJ212", "228","90223338","DA","PATRICK","01005", "01165424001","0012",MTypeCompte.BANQUE);
        //   MInfos   emet =setPersonne("BJ212","229","90243477","Mawulolo","Mawulolo","01001", "01570224001","0012",MTypeCompte.BANQUE); 
       //    MInfos ben   = setPersonne("0", "226", "73061292", "DA", "PATRICK", "4490", "12742369", "0054", MTypeCompte.CARTE,"4490");
        // MInfos emet =setPersonne("BF148","226","73061292","DA","PATRICK","01001", "30415124001","0004",MTypeCompte.BANQUE,null);
        //  MInfos   ben =setPersonne("BJ212","229","0124744","Mawulolo","Mawulolo","03005", "01085924001","0012",MTypeCompte.BANQUE); 
        //   MInfos   ben =setPersonne("TG182","228","90223338","Mawulolo","Mawulolo","01005", "01165424001","0010",MTypeCompte.BANQUE); 
        //  MInfos ben = setPersonne("0", "226", "73061292", "DA", "PATRICK", "01001", "0011000000015", "0011", MTypeCompte.AUTRE, null);
   //  MInfos ben = setPersonne("BF148", "226", "73061292", "DA", "PATRICK", "01001", "30415124001", "0004", MTypeCompte.BANQUE, null);
    // MInfos emet = setPersonne("BF148", "226", "70531413", "DA", "PATRICK", "01001", "00950224001", "0004", MTypeCompte.BANQUE, null);

        //   MInfos emet = setPersonne("0", "226", "73061292", "DA", "PATRICK", "10000", "01038524001", "0056", MTypeCompte.BANQUE, null);
     //   MInfos emet = setPersonne("0", "226", "73061292", "DA", "PATRICK", "10000", "0011000003090", "0011", MTypeCompte.AUTRE, null);
         MInfos emet = setPersonne("0", "226", "73061292", "DA", "PATRICK", "10000", "0011000003090", "0011", MTypeCompte.AUTRE, null);
        MInfos ben = setPersonne("0", "226", "55532895", "DA", "PATRICK", "10000", "0840101306422132700", "0011", MTypeCompte.AUTRE, null);

        //  MInfos ben = setPersonne("0", "226", "74503682xxx", "DA", "PATRICK", "10000", "01389024001", "0056", MTypeCompte.BANQUE, null);
        //    MInfos emet = setPersonne("BF148", "226", "73061292", "DA", "PATRICK", "01001", "30415124001", "0004", MTypeCompte.BANQUE,null);
        //   MInfos emet = setPersonne("0", "228", "90243622", "DA", "PATRICK", "01001", "0021000000013", "0011", MTypeCompte.AUTRE, null);
        // MInfos ben = setPersonne("0", "226", "75230014", "DA", "PATRICK", "01001", "001100523696901", "0011", MTypeCompte.AUTRE,null);
        //  MInfos   ben =setPersonne("0","228","90243622","DA","PATRICK","01001", "0021000000013","0021",MTypeCompte.AUTRE);
        ////   MInfos ben = setPersonne("0", "228", "90492389", "DA", "PATRICK", "01001", "0021000745668", "0021", MTypeCompte.AUTRE, null);
        //  MInfos ben  = setPersonne("0", "226", "73061292", "DA", "PATRICK", "0", "12704045", "0054", MTypeCompte.CARTE,"7204");
        //       MInfos   ben =setPersonne("BF148","226","73061292","Mawulolo","Mawulolo","01001", "01570224001","0004",MTypeCompte.BANQUE,null); 
        //   MInfos  ben   =setPersonne("BF148","226","70531413","DA","PATRICK","01011", "42395224001","0004",MTypeCompte.BANQUE,null);
        operation.setUserConnecte(user);
        //    operation.setCodePv("PV003");
        operation.setActeur(acteur);
        //   operation.setActeur("CLIENT");
        operation.setIdentifiant("70531413");
        //operation.setIdentifiant("0003410");
        operation.setIdentifiant(identifiant);
        operation.setPreautorisaton(1);

        // operation.setCodePv("PV003");
        operation.setCodePv(codePv);
        operation.setBeneficiaire(ben);
        operation.setEmetteur(emet);

        //MOBILE   operation.setCodePv("0001291280");
        //MOBILEoperation.setCodeUoAcq("0001291");
        operation.setCodeUoEm(codeUoEm);
        operation.setCodeUoAcq(codeUo);
        operation.setCodeUo(codeUo);
        //  operation.setCodeUo("0026733");
        operation.setCodePvEm(codePvEm);
        //      operation.setCodeUo(codeUo);
        //   operation.setCodeUo("0000014089");
        //TG  operation.setCodeUo("0004453");

//   operation.setCodeUo("0005022");
        operation.setMotif("");
        //   operation.setMontant(mt);
        operation.setMontantFrais(BigDecimal.valueOf(montantfrais));
        operation.setMontantTaxe(BigDecimal.valueOf(montanttaxe));
        operation.setTypeService(typeService);
        operation.setMontant(BigDecimal.valueOf(montant));
        operation.setMotif("M2");

        //TG               operation.setCodePays("0010935");
        //    operation.setCodePays("P0000005U");
        operation.setCodePays("UO_0002");
        // operation.setCodeRetrait("760636341");
        operation.setCodeRetrait("548718468");
        //309249312
        //883181635
        return operation;
    }

    private MInfos setPersonne(String codebanque, String codetel, String telephone, String nom, String prenom, String codeagence, String numCpt, String teneur, MTypeCompte typeCompte, String las4Digit) {

        MInfos ben = new MInfos();
        MPersonne pers = new MPersonne();
        pers.setTelephone(telephone);
        pers.setNom(nom);
        pers.setCodeTelephone(codetel);
        pers.setPrenom(prenom);
        pers.setSexe(MSexe.FEMININ);
        ben.setTitulaire(pers);
        MCompte cpt1 = new MCompte();
        cpt1.setLas4Digit(las4Digit);
        cpt1.setCodeAgence(codeagence);
        cpt1.setCodeBanque(codebanque);
        cpt1.setNumeroCompte(numCpt);
        cpt1.setRib("0");
        cpt1.setTypeCompte(typeCompte);
        cpt1.setTeneurCompte(teneur);
        ben.setCompte(cpt1);
        return ben;

    }

    public void executer2() throws IOException, IllegalAccessException, InstantiationException {
        PtiSetup pti = new PtiSetup();
        pti.setLogin(URLEncoder.encode("{\"login\":\"shubert\", \"hostId\": \"1111555\", \"hostName\":\"5555\", \"currentUser\":\"5555\"}", "UTF-8"));
        pti.setPassword(URLEncoder.encode("753951", "UTF-8"));
        String scope = URLEncoder.encode("read write", "UTF-8");
        System.out.println(pti.getPassword());
        System.out.println(scope);
        System.out.println(pti.getLogin());
        pti.setClientID("ptiapp");
        pti.setPlainClientCredentials("my-secret-token-to-change-in-production");
        pti.setPassword("my-secret-token-to-change-in-production");
        pti.setUrl("http://localhost:9002");
        pti.setGrantUri("?grant_type=client_credentials&scope=read&client_secret=" + pti.getPlainClientCredentials() + "&client_id=" + pti.getClientID());
        //  pti.setGrantUri("?grant_type=password&scope=read&client_secret=" + pti.getPlainClientCredentials() + "&client_id=" + pti.getClientID());
        //   pti.setGrantUri("?grant_type=password&scope="+scope+"&username="+pti.getLogin()+"&password="+pti.getPassword()+"&client_secret="+URLEncoder.encode(pti.getPlainClientCredentials(),  "UTF-8")+"&client_id="+URLEncoder.encode(pti.getClientID(),  "UTF-8"));
        //   System.out.println(pti.getComptesOfClient("76477115", "221"));
        System.out.println(pti.getGrantUri());
//pti.getComptesOfClient("71274660", "226");
        MClient mclient = new MClient();
        //         String identifiant="90243622";
        //String identifiant="93888219";
        //  String identifiant="68626815";
        //   String identifiant="90243622";
        //String identifiant = "70531413";

        String identifiant = "73061292";
        //   String identifiant="0001291";

        //  String identifiant = "0042702";
        //   String identifiant="0001291";
        //  String codePv="0003410881";
        //        String codeUoEm="0009227";
        //     String identifiant="90243622";
        // String codePv=  "0002119945";
        String codePv = "0001291280";
        String codeUo = "0001291";
        //  String codePv=    "0009227556";
        String codeUoEm = "0001291";
        //String codeUoEm="0002119";
        // String codeUoEm=    "0009227";
        //          String codePvEm="0009227556";
        String codePvEm = "0001291280";
        // String codePvEm="0002119945";
        //      MOperation operation1= getoperation("MOBILE","shubert", "ORGANISATION", identifiant, codePv,   codeUoEm, codePvEm, "0042702", "CASH_ACCOUNT", 500L, 100L, 100L);
        //MOperation operation = getoperation("MOBILE", "shubert", "CLIENT", identifiant, codePv, codeUoEm, codePvEm, "0001291", "BANK_TO_WALLET", 500L, 100L, 100L);
        // MOperation operation= getoperation("MOBILE","asimza", "CLIENT", identifiant, codePv,   codeUoEm, codePvEm, "0001610", "RETRAIT", 500L, 100L, 100L);
         MOperation operation = getoperation("MOBILE", "mbaye", "CLIENT", identifiant, codePv, codeUoEm, codePvEm, "0001291", "ACCOUNT_ACCOUNT", 27L, 50L, 10L);

    //     MOperation operation = getoperation("MOBILE", "mbaye", "CLIENT", identifiant, codePv, codeUoEm, codePvEm, "0001291", "WALLET_TO_CARD", 200L, 1L, 1L);
        //   MOperation operation = getoperation("MOBILE", "mbaye", "CLIENT", identifiant, codePv, codeUoEm, codePvEm, "0001291", "CARD_TO_WALLET", 20L, 1L, 1L);
        //    MOperation operation = getoperation("MOBILE", "mbaye", "ORGANISATION", identifiant, codePv, codeUoEm, codePvEm, "0001291", "RETRAIT_COMPTE", 1000L, 10L, 10L);
        //  MOperation operation = getoperation("MOBILE", "mbaye", "CLIENT", identifiant, codePv, codeUoEm, codePvEm, "0001291",ss "BANK_TO_WALLET", 50L, 1L, 1L);
        //      MOperation operation= getoperation("MOBILE","mbaye", "CLIENT", identifiant, codePv,   codeUoEm, codePvEm, "0001291", "WALLET_TO_BANK", 500L, 1L, 1L);
        //  MOperation operation = getoperation("MOBILE", "mbaye", "ORGANISATION", identifiant, codePv, codeUoEm, codePvEm, "0001291", "ACCOUNT_CASH", 126L, 10L, 10L);
        //     MOperation operation= getoperation("MOBILE","mbaye", "CLIENT", identifiant, codePv,   codeUoEm, codePvEm, "0001291", "RETRAIT_CARDLESS", 15000L, 10L, 10L);
        //     MOperation operation= getoperation("MOBILE","mbaye", "CLIENT", identifiant, codePv,   codeUoEm, codePvEm, "0002444", "PAIEMENT_INTERNET", 10L, 10L, 10L);
        //   MOperation operation= getoperation("MOBILE","cda", "ORGANISATION", identifiant, codePv,   codeUoEm, codePvEm, "0002119", "SWEEP_DEPOSIT_TO_BANK", 2000L, 100L, 100L);
        //        MOperation operation= getoperation("MOBILE","isanfo", "ORGANISATION", identifiant, codePv,   codeUoEm, codePvEm, "0002119", "AUTO_ALLOCATION", 2000L, 100L, 100L);
        //  MOperation operation= getoperation("MOBILE","mbaye", "ORGANISATION", identifiant, codePv,   codeUoEm, codePvEm, "0001610", "BANK_TO_WALLET", 2000L, 100L, 100L);
        // MOperation operation= getoperation("MOBILE","shubert", "ORGANISATION", identifiant, codePv,   codeUoEm, codePvEm, "F0000004E", "RETRAIT", 15000L, 100L, 100L);
        // MResponse r=pti.doOperationRetraitCash(operation);
        //       MOperation operation = getoperation("MOBILE", "mbaye", "CLIENT", identifiant, codePv, codeUoEm, codePvEm, "0001291", "WALLET_TO_BANK", 200L, 100L, 100L);
        //  MOperation operation = getoperation("MOBILE", "mbaye", "CLIENT", identifiant, codePv, codeUoEm, codePvEm, "0001291", "ACCOUNT_ACCOUNT", 200L, 100L, 100L);
//   //     for (MListeReseauServiceFse listeReseauServiceFse :pti.getDataParameter("226").getmListReseauFse()){
        //System.out.println(  listeReseauServiceFse.getListMServiceReseau());
        //   }
//     MResponse r=pti.doPaiementInternet(operation);
        //     MResponse r=pti.doOperationEnvoiCash(operation);
        //    MResponse r=pti.doOperationRetraitCardLess(operation);
        //MResponse r=pti.doOperationRetraitCompte(operation);
        //    operation.setApplication("PTI");
        long tempsDebut, tempsFin;
        //   tempsDebut = System.currentTimeMillis();
        long duree;
      //  Balance b=  pti.getSoldeCompte("0011", "0011000003090", "0011", "CLIENT", "73061292", "226");
       // tempsDebut = System.currentTimeMillis();
       // b=  pti.getSoldeCompte("0011", "0011000003090", "0011", "CLIENT", "73061292", "226");
      
  //      System.out.println("LE RETOUR EST " + b.getIdEvent());

        // b=  pti.getSoldeCompte("0011", "0011000003090", "0011", "CLIENT", "73061292", "226");
        /// MResponse r=pti.doOperationAccountCash(operation);
        //tempsFin = System.currentTimeMillis();
      //  duree = tempsFin - tempsDebut;
      //  System.out.println("duree du solde "+ duree);
        // System.out.println("Le solde "+ b.getSolde());
        tempsDebut = System.currentTimeMillis();
      //      b=  pti.getSoldeCompte("0011", "0011000003090", "0011", "CLIENT", "73061292", "226");
      //   System.out.println("solde du compte est"+ b.getSolde());
        tempsFin = System.currentTimeMillis();
        duree = tempsFin - tempsDebut;
        System.out.println("La duree du solde est de" + duree);
        
        
         MResponse r = pti.doOperationAccountToAccount(operation);
      //     b=  pti.getSoldeCompte("0011", "0011000003090", "0011", "CLIENT", "73061292", "226");
           
           //  System.out.println("solde du compte APRES est"+ b.getSolde());
//MResponse r = pti.doOperationWalleToCard(operation);
         //  MResponse r=pti.doOperationAccountToAccount(operation);
        //pti.getSoldeCompte()
      //  Message msg=     pti.getGenereOTP("10000", "01389024001", "0109", "0118",101L) ;
        
                //      System.out.println(msg.getText());
        //  System.out.println(msg.getMsg());
       // Message msg2=     pti.verificationOtp(msg.getMsg(), "01389024001",  "0109") ;
              //ystem.out.println(msg2.getText());
         // System.out.println(msg2.getMsg());
        //      pti.doExternalContexteOperation(operation, "0059", "ACCOUNT_ACCOUNT");
        
    
      //  MDataTransaction m= pti.getDataTransactionInterOp("92252913","228","MOBILE", "TIG_ACCOUNT_ACCOUNT", "0001291280", "F0000004E", "0005022", BigDecimal.valueOf(5000),"AUTRE","92252913","228","0021", Optional.empty());
        //GrData g= pti.doExternalGetFacturierInfos("00105719", "WALLET");
//System.out.println(m.getNomUo());
        // System.out.println( g.getExfacturier().get(0).getCodeFacturier());
        // System.out.println(  m.getBeneficiaire().getCodeFse());
        //       MResponse r=pti.doOperationCashAccount(operation1);
        //  MResponse r=pti.doPaiementInternet(operation);
        //  MResponse r=pti.doOperationAuToAllocation(operation);
        //        MResponse r=pti.doOperationSweepToAccount(operation);
        // System.out.println(   pti.doExternalGetListeFacturier( "0011",  "GENERAL")  );
        // System.out.println(   pti.getInfosClientCompte("xxxxxx", "0",  "0056")  );
        // pti.generateOtp( "226",  "19995550122", "CORISMONF","12713969" );
        // verificationOtp(String codeTelephone, String telephone, String otp, String codeFacturier, String identifiant)
        //       System.out.println(   pti.verificationOtp("226","19995550122","60483",  "CORISMONF","12713969").getMsg());
        // MResponse r = pti.doOperationWalleToCard(operation);
        //          System.out.println( pti.getHistoriqueCompte("01001", "30415124001", "0004", "01-01-2019", "30-09-2019", "CLIENT", "73061292", "226","0","0"));
        //     CustomerInformation c = pti.getInfosClientCompteEx("01001", "01165424001", "0011", "", "");
        // //   System.out.println(c.getPrenom());
        // System.out.println(c.getListCompte().get(0).getNumeroCompte());
        //    operation= getoperation("MOBILE","mbaye", "CLIENT", identifiant, codePv,   codeUoEm, codePvEm, "0001291", "ACCOUNT_CASH", 200L, 100L, 100L);
        //  pti.doOperationAccountCash(operation);
        //  MResponse r=pti.(operation);
        // MResponse r = pti.doOperationWalletToBank(operation);
        //MResponse r=null;
        //    System.out.println(pti.getHistoriqueCompte("01001", "12704045", "0054", "01-01-2024", "30-06-2024","CLIENT","73061292", "226","",""));
        //      System.out.println(pti.getMiniReleve("0011", "0011000003090", "0011","CLIENT","73061292","226","0","0"));
        //  GrData grD = pti.getFacturiersByCodeUO("F0000004E", "AUTRE", null, null);
        //  System.out.println(grD.getExfacturier().get(0).getNomFacturier());
        ///////     pti.doExternalContexteOperation(operation, "0011", "ACCOUNT_ACCOUNT");
        //  MDataParameter mpx = pti.getDataParameter("226");
     //   System.out.println(pti.getPublicite("226"));

        List<MParametre> listeParam = new ArrayList();
        MParametre pa = new MParametre();
        pa.setCodeParametre("IDENTIFIANT");
        pa.setValeur("0011000003090");

        listeParam.add(pa);
        pa = new MParametre();

        pa.setCodeParametre("OTP");
        pa.setValeur("85568");

        listeParam.add(pa);

        pa = new MParametre();

        pa.setCodeParametre("TELEPHONE");
        pa.setValeur("73061292");
        listeParam.add(pa);

        pa = new MParametre();
        pa.setCodeParametre("CODETELEPHONE");
        pa.setValeur("226");

        listeParam.add(pa);

        // System.out.println("LE CODE EST : "+pti.traiteOtp(listeParam, "0011", "GENEREROTP").getCodeErr());
        //   System.out.println("LE CODE EST : "+pti.traiteOtp(listeParam, "0011", "VERIFOTP").getCodeErr());
        //   System.out.println(pti.getListeActivite("226", "73061292"));
        //  System.out.println(mpx);
        // / String tre = pti.convertToJsonX(mpx.getListeCategorieFse());
        //   System.out.println(tre);
        // MDataTransaction m = pti.getExternalContexteDataTransaction("70531413","226","MOBILE", "ACCOUNT_ACCOUNT", "6892646910", "F0000004E", "F0000004E", BigDecimal.valueOf(5000),"AUTRE","0011000000044","0","0011", "0011",Optional.empty());
        //   System.out.println(  pti.doVerifPointVenteIsAffected("M0000006M","PV026").getMessage());
        //  System.out.println(  pti.getPointVente("M0000006M"));
        // System.out.println(  pti.getTypeReclamation());
        //  System.out.println(m.getMfrais().getFraisOperation());
        // System.out.println(m.getMfrais().getTaxeOPeration());
        //  System.out.println(m.getMfrais().getResponsable());
        MListParametreProduit mparametre = new MListParametreProduit();
        mparametre.setIdProduit(40L);

        List<MParametre> listP = new ArrayList();
        MParametre p = new MParametre();
        p.setCodeParametre("NBRE");
        p.setValeur("1");
        listP.add(p);
        p = new MParametre();
        p.setCodeParametre("REFERENCE");
        p.setValeur("123456");
        listP.add(p);
        p = new MParametre();
        p.setCodeParametre("MONTANT");
        p.setValeur("100");
        listP.add(p);
        mparametre.setMparametre(listP);
        String parma = pti.convertToJsonX(mparametre);
        System.out.println(parma);
        ///    pti.getFactureExternal(mparametre, null, null);
        // System.out.println(pti.sendOperationMsg("2024661.VU0011511.926").getMessage());
        //  System.out.println( pti.doPaiementFacture("226", "73061292", "0011511723", BigDecimal.valueOf(500), "CORISASSVIE", 28L, "MOBILE", null, "PAIEMENT_FACTURE", "100", "73061292", mparametre, null, null).getCode());
        //   System.out.println(pti.getLastOperationAttente("226", "73061292"));

        //    System.out.println(pti.getClientExist("70531413","226"));
        //System.out.println("ddd"+r.getTansactionId());
        //  EtatParams e=new EtatParams();
        //   pti.updateUserFailure("cda1");
        //@RequestParam String agence, @RequestParam String numeroCompte, @RequestParam String codeFse, @RequestParam String acteur, @RequestParam(required = false) String codeIndicatif, @RequestParam String identifiant
        //  System.out.println(   pti.getInfosClientCompte( "226getDataParameter", "76477115","0011"));
        // e.setOperationId(Long.parseLong("14317"));
        //   System.out.println(   pti.getAutotisationClient("226", "73061292"));
        //    System.out.println (  pti.getAllOperation(1, null, null, null, null, "PAYE", null, null, null, 243L));
        // System.out.println (pti.getTokenInfo());
        //     System.out.println (pti.getTempPass("SFAtNUNHNjI3MlBROS1CRkVCRkJGRjAwMDQwNkUzLTAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwNDYyZTMyMzA\\u003d"));
        //    Files.write(Paths.get("G:\\rpt\\op.pdf"), pti.getPrintBordereau(e), StandardOpenOption.CREATE);
        //System.out.println(pti.getListOrganisationFils("F0000004E","SOUS_DISTRIBUTEUR"));
        //  pti.doAnnulation("148706");
        //    System.out.println("ddd"+pti.getOperationStatus("202311950.T772","","").getCode());
  //     MFrais frais=     pti.calculFrais("1", "ACCOUNT_TO_PI", codePv, codeUoEm, codeUoEm,  BigDecimal.valueOf(500));
       
         //System.out.println(frais.getFraisOperation());
         
               //StoppageData b=  pti.getStoppageCompte("01001", "02089024002", "0004", "1", "D", "0", "1");

    }

    private MResponse executer() throws IOException, IllegalAccessException, InstantiationException {
        PtiSetup pti = new PtiSetup();
        pti.setPlainClientCredentials("$2a$10$bXAyslP4KRJzwraJCLccpOCWd583.f/U2TGPV6a9WP50FcNNrbaF.");
        pti.setPassword("$2a$10$bXAyslP4KRJzwraJCLccpOCWd583.f/U2TGPV6a9WP50FcNNrbaF.");
        pti.setClientID("ptimobile");
        pti.setUrl("http://localhost:8080/");

        pti.setGrantUri("?grant_type=client_credentials&scope=read&client_secret=" + pti.getPlainClientCredentials() + "&client_id=" + pti.getClientID());
        //   System.out.println(pti.getComptesOfClient("76477115", "221"));
//pti.getComptesOfClient("71274660", "226");
        MClient mclient = new MClient();

        //  System.out.println(pti.getDataParameter("226"));
        MPersonne pers = new MPersonne();
        MPersonne pers1 = new MPersonne();
        MDocIdentification doc = new MDocIdentification();
        doc.setReference("B5588889");
        pers.setNom("VVVVVVVXXXX");
        pers.setPrenom("wxxxxxx");
        pers.setTelephone("68626815");
        pers.setCodeTelephone("226");
        mclient.setDocIdentification(doc);
        //    MDataParameter d=  pti.getDataParameter();

        mclient.setPersonne(pers);

        //MPersonne p= pti.doSouscriptionTemporaire(mclient);
//         System.out.println(p.getNom());
        MInfos minfo = new MInfos();

        minfo.setTitulaire(pers);

        //  operation.setBeneficiaire(minfo);
        MCompte cpt = new MCompte();

        pers1.setCodeTelephone("226");
        pers1.setTelephone("68626815");

        //System.out.println(  pti.getSoldeCompte("0011","77441100", "0011").getSolde());
        //     System.out.println(pti.verificationClient("226", "70531413", "cccdddd"));
//   
        List<MParametre> tran = new ArrayList();
        MParametre mp = new MParametre();
        mp.setCodeParametre("MONTANT");
        mp.setValeur("500");
        tran.add(mp);
        mp = new MParametre();
        mp.setCodeParametre("NBRE");
        mp.setValeur("2");
        tran.add(mp);
        //   System.out.println(pti.getMontantFacture(tran));
        // System.out.println(pti.getMontantFacture(tran));

        // System.out.println(pti.verifPersonneInscrit("60606060","226","esof@esof.com","MORALE"));
        //  System.out.println(pti.getRegle("XXXXEEEEEEEEE", "5000", "5"));
        //   System.out.println(pti.getFacturier(7L));
        // System.out.println(pti.getCodeAgentGab("226"));
        // getExternalContexteDataTransaction(String telephone, String code, String canal, String typeService, String codePv,String codeUosrc, String codeUoDest, BigDecimal montant, String typeCompte, String valeurCompte, String codeAgence, String fseTeneur, Optional<String> codeacteur, String codeFse)
        // pti.getExternalContexteDataTransaction("70531413", "226", "MOBILE", "ENVOI_CASH", "6892646910", "F0000004E", "F0000004E", BigDecimal.valueOf(5000), "AUTRE", "0011000003090", "0", "0011", "0011", Optional.empty());
        pti.getPublicite("226");

        //System.out.println(m.getBeneficiaire().getPersonne().getNom());
//   System.out.println(  pti.doVerifPointVenteIsAffected("M0000006M","PV026").getMessage());
        //  System.out.println(  pti.getPointVente("M0000006M"));
        // System.out.println(  pti.getTypeReclamation());
        /*
           System.out.println(m.getMfrais().getFraisOperation());
             System.out.println(m.getMfrais().getTaxeOPeration());
          System.out.println(m.getMfrais().getResponsable());*/
        //     BigDecimal mt = BigDecimal.valueOf(50000);
        // BigDecimal taxe = m.getTaxeOPeration(); //BigDecimal.valueOf(m.getTaxeOPeration()); 
        //  BigDecimal frais = m.getFraisOperation();
        //System.out.println( m.getMfrais().getTaxeOPeration());
        //       System.out.println( m.getMfrais().getFraisOperation());
        //  System.out.println(pti.getDetailOperation("202010133.A262").getMontant());
        // System.out.println(pti.getOrganisationTelephonie("0011"));
        //      System.out.println(pti.getChargement("76477115","226","V"));
        //   System.out.println(pti.valideChargement(2L, "admin", "WEB"));
        //MDataParameter mpx=     pti.getDataParameter("226");
        //   System.out.println(pti.getTypeDOcument());
        //    pti.getPays();
        MOperationFacture operation = new MOperationFacture();

        //   operation.setTypeService(TypeService.ENVOI_CASH.toString());
//operation.setTypeService(TypeService.RETRAIT_COMPTE.toString());
        operation.setTypeService(TypeService.PAIEMENT_FACTURE.toString());
        //operation.setTypeService(TypeService.ACCOUNT_CASH.toString());
        //   operation.setTypeService(TypeService.RETRAIT_COMPTE.toString());
        MInfos emet = new MInfos();
        operation.setCodeUo("0001291");

        cpt.setCodeAgence("");
        cpt.setCodeBanque("");
        cpt.setNumeroCompte("0011000000030");
        cpt.setRib("14");

        cpt.setTypeCompte(MTypeCompte.AUTRE);
        cpt.setTeneurCompte("0011");
        pers.setNom("OUEDRAOGO");
        pers.setPrenom("SOUFIANE");
        pers.setSexe(MSexe.FEMININ);
        pers.setTelephone("68626815");
        pers.setCodeTelephone("226");
        emet.setCompte(cpt);
        emet.setTitulaire(pers);
        MProduit produit = new MProduit();
        operation.setProduit(produit);
        MFacturier fac = new MFacturier();
        fac.setId(15L);
        operation.setFacturier(fac);
        operation.setIdentifiant("68626815");
        operation.setCanal("MOBILE");
        operation.setActeur("CLIENT");
        // operation.setCodePv("PV003");
        operation.setCodePv("0001291280");
        operation.setEmetteur(emet);
        //pti.doOperationPaiementFacture(operation);
        //  System.out.println( operation.getBeneficiaire());
        // System.out.println(pti.doOperationRetraitCash(operation));
        // System.out.println(pti.getComptesOfAgent("0000001180").getNomOrganisation());
        //System.out.println(pti.getAutotisationClient("226","9010504"));    
        //   System.out.println(pti.doOperationPaiementCredit(operation)); 
        //   System.out.println(pti.getCodeAgentGab("226").getCodePv());
        // MResponse r=    pti.doOperationRetraitCardLess(operation);
        //  MResponse r=    pti.doOperationAccountToAccount(operation);
        //   System.out.println(r.getCode()+""+r.getMessage());
        //    System.out.println(m.getTaxeOPeration());
        //  operation.se
//doOperationAccountCash
        //    System.out.println(pti.getPays());
        //  List <MOrganisation>    Organisation=   pti.getPays();
        //     System.out.println(pti.getComptesOfAgent("M0000006M"));
        // System.out.println( pti.getInfosClient( "71274660" ,"226"));
        //   List<MPersonne> personnes = pti.getInfosClient( "71274660" ,"226");
        //      System.out.println(personnes.get(0));

        //  System.out.println( pti.getHistoriqueCompte("01001", "30", "0004", "01-01-2024", "30-09-2024", "CLIENT", "73061292", "226",null,null));
        //  System.out.println(pti.getHistoriqueCompte("01001", "76477115", "0011", "01-01-2019", "30-09-2019"));
//Balance bal= pti.getSoldeCompte(null, "76477115", "0011") ;
        //System.out.println(bal.getSolde());
        //   System.out.println(pti.getFseOfPays("UO_0002"));
        System.out.println(pti.getListeActivite("226", "73061292"));
        System.out.println("Solde---" + pti.getSoldeCompte("0054", "73061292", "0054", "CLIENT", "73061292", "226").getSolde());
        //    MResponse r=pti.doOperationRetraitCompte(operation);
        //    MResponse r=pti.doOperationCashAccount(operation);
        //MResponse r=pti.doOperationRetrait(operation);
        //  MResponse r=pti.doOperationAccountCash(operation);
        //  MResponse r=pti.doOperationAccountCash(operation);
        //   System.out.println(pti.getFseOfPays("UO_0002"));
        // TODO code application logic here
        // System.out.println(pti.getOrganisationByCode("0000015599").getTypeDuPere()); 
        //System.out.println(pti.getOrganisationByCodePV("PV033").getCodeUo()); 
        //System.out.println(   pti.getInfosClientCompte( "01001", "30415124001","0004"));
        //   System.out.println(   pti.getInfosClientCompte( "226", "73061292","0011"));
        //  System.out.println (pti.getTempPass(("SFAtQ1pDOTEyOTlNSi1CRkVCRkJGRjAwMDkwNkVBLTAwMDAwMDUxMzAzMzIwNTY2NTcyMmUyMDMwMzIyZTMwMzYyZTMwMzA=")).getDonnee());
        //System.out.println( pti.verifToken("123456", "SFAtQ1pDOTEyOTlNSi1CRkVCRkJGRjAwMDkwNkVBLTAwMDAwMDUxMzAzMzIwNTY2NTcyMmUyMDMwMzIyZTMwMzYyZTMwMzA=").getDonnee());
        // System.out.println(     pti.getTempPass("SFAtQ1pDOTEyOTlNSi1CRkVCRkJGRjAwMDkwNkVBLTAwMDAwMDUxMzAzMzIwNTY2NTcyMmUyMDMwMzIyZTMwMzYyZTMwMzA="));
        // System.out.println(pti.getFacturierOfOrganisation("0022973"));

        //  System.out.println( pti.getInfosActeur("0003410").getNom());
        MPersonne pers3 = new MPersonne();

        pers3.setCodeTelephone("226");
        pers3.setTelephone("76477115");
        return null;
    }

    private Balance executerS() throws IOException, IllegalAccessException, InstantiationException {
        PtiSetup pti = new PtiSetup();
        pti.setPlainClientCredentials("$2a$10$bXAyslP4KRJzwraJCLccpOCWd583.f/U2TGPV6a9WP50FcNNrbaF.");
        pti.setPassword("$2a$10$bXAyslP4KRJzwraJCLccpOCWd583.f/U2TGPV6a9WP50FcNNrbaF.");
        pti.setClientID("ptimobile");
        pti.setUrl("http://localhost:9002");
        pti.setGrantUri("?grant_type=client_credentials&scope=read&client_secret=" + pti.getPlainClientCredentials() + "&client_id=" + pti.getClientID());
        //   System.out.println(pti.getComptesOfClient("76477115", "221"));
//pti.getComptesOfClient("71274660", "226");
        Balance bal = pti.getSoldeCompte("0011", "76477115", "0011", "CLIENT", "76477115", "226");

        return bal;
    }

    public static long get64MostSignificantBitsForVersion1() {
        final long currentTimeMillis = System.currentTimeMillis();
        final long time_low = (currentTimeMillis & 0x0000_0000_FFFF_FFFFL) << 32;
        final long time_mid = ((currentTimeMillis >> 32) & 0xFFFF) << 16;
        final long version = 1 << 12;
        final long time_hi = ((currentTimeMillis >> 48) & 0x0FFF);
        return time_low | time_mid | version | time_hi;
    }

    public static long get64LeastSignificantBitsForVersion1() {
        Random random = new Random();
        long random63BitLong = random.nextLong() & 0x3FFFFFFFFFFFFFFFL;
        long variant3BitFlag = 0x8000000000000000L;
        return random63BitLong | variant3BitFlag;
    }

    public static String generateType1UUID() {
        long most64SigBits = get64MostSignificantBitsForVersion1();
        long least64SigBits = get64LeastSignificantBitsForVersion1();
        UUID uiid = new UUID(most64SigBits, least64SigBits);
        // System.out.println(UUID.randomUUID().toString().toUpperCase());
        RandomStringUtils.randomAlphabetic(2).toUpperCase();
        System.out.println(RandomStringUtils.randomAlphabetic(3).toUpperCase());
        return uiid.toString();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, IllegalAccessException, InstantiationException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        System.out.println(LocalDateTime.now().format(formatter));
        Pticlient t1 = new Pticlient();
        Pticlient t2 = new Pticlient();
        Pticlient t3 = new Pticlient();
        //   t1.paiementFacture();

        String json = "{\n"
                + "  \"deviseSrc\" : null,\n"
                + "  \"deviseDest\" : null,\n"
                + "  \"codePv\" : \"0001291280\",\n"
                + "  \"montant\" : 200,\n"
                + "  \"montantEnDevise\" : null,\n"
                + "  \"montantFrais\" : 1,\n"
                + "  \"montantTaxe\" : 1,\n"
                + "  \"responsableFrais\" : null,\n"
                + "  \"montantFraisClient\" : null,\n"
                + "  \"canal\" : \"MOBILE\",\n"
                + "  \"typeService\" : \"ACCOUNT_ACCOUNT\",\n"
                + "  \"motif\" : \"M2\",\n"
                + "  \"codeUo\" : \"0001291\",\n"
                + "  \"codeUoEm\" : \"0001291\",\n"
                + "  \"codeUoAcq\" : \"0001291\",\n"
                + "  \"codePvEm\" : \"0001291280\",\n"
                + "  \"emetteur\" : {\n"
                + "    \"titulaire\" : {\n"
                + "      \"responseStatus\" : null,\n"
                + "      \"codeNationalite\" : null,\n"
                + "      \"paysResidence\" : null,\n"
                + "      \"nom\" : \"DA\",\n"
                + "      \"prenom\" : \"PATRICK\",\n"
                + "      \"telephone\" : \"73061292\",\n"
                + "      \"codeTelephone\" : \"226\",\n"
                + "      \"email\" : null,\n"
                + "      \"adresse\" : null,\n"
                + "      \"nomDelaMere\" : null,\n"
                + "      \"personneAprevenir\" : null,\n"
                + "      \"sexe\" : \"FEMININ\",\n"
                + "      \"lieuNaissance\" : null,\n"
                + "      \"dateNaissance\" : null,\n"
                + "      \"profession\" : null,\n"
                + "      \"docIdentifications\" : null,\n"
                + "      \"imageDocumentIdentification\" : null,\n"
                + "      \"brancheActivite\" : null\n"
                + "    },\n"
                + "    \"compte\" : {\n"
                + "      \"las4Digit\" : null,\n"
                + "      \"teneurCompte\" : \"0011\",\n"
                + "      \"codeOperateur\" : null,\n"
                + "      \"numeroCompte\" : \"0011000003090\",\n"
                + "      \"codeAgence\" : \"01001\",\n"
                + "      \"codeBanque\" : \"0\",\n"
                + "      \"rib\" : \"0\",\n"
                + "      \"typeCompte\" : \"AUTRE\",\n"
                + "      \"alias\" : null\n"
                + "    }\n"
                + "  },\n"
                + "  \"codePays\" : \"UO_0002\",\n"
                + "  \"codeRetrait\" : \"548718468\",\n"
                + "  \"beneficiaire\" : {\n"
                + "    \"titulaire\" : {\n"
                + "      \"responseStatus\" : null,\n"
                + "      \"codeNationalite\" : null,\n"
                + "      \"paysResidence\" : null,\n"
                + "      \"nom\" : \"DA\",\n"
                + "      \"prenom\" : \"PATRICK\",\n"
                + "      \"telephone\" : \"75007474\",\n"
                + "      \"codeTelephone\" : \"226\",\n"
                + "      \"email\" : null,\n"
                + "      \"adresse\" : null,\n"
                + "      \"nomDelaMere\" : null,\n"
                + "      \"personneAprevenir\" : null,\n"
                + "      \"sexe\" : \"FEMININ\",\n"
                + "      \"lieuNaissance\" : null,\n"
                + "      \"dateNaissance\" : null,\n"
                + "      \"profession\" : null,\n"
                + "      \"docIdentifications\" : null,\n"
                + "      \"imageDocumentIdentification\" : null,\n"
                + "      \"brancheActivite\" : null\n"
                + "    },\n"
                + "    \"compte\" : {\n"
                + "      \"las4Digit\" : null,\n"
                + "      \"teneurCompte\" : \"0011\",\n"
                + "      \"codeOperateur\" : null,\n"
                + "      \"numeroCompte\" : \"0011000000134\",\n"
                + "      \"codeAgence\" : \"01001\",\n"
                + "      \"codeBanque\" : \"0\",\n"
                + "      \"rib\" : \"0\",\n"
                + "      \"typeCompte\" : \"AUTRE\",\n"
                + "      \"alias\" : null\n"
                + "    }\n"
                + "  },\n"
                + "  \"cardLess\" : null,\n"
                + "  \"userConnecte\" : \"mbaye\",\n"
                + "  \"identifiant\" : \"73061292\",\n"
                + "  \"acteur\" : \"CLIENT\",\n"
                + "  \"description\" : null,\n"
                + "  \"application\" : null\n"
                + "}";
        Gson gson = new Gson();

        MOperation user = gson.fromJson(json, MOperation.class);

        t1.executer2();

        for (int i = 0; i < 1; i++) {

            //t1.executer2();
            new Thread(() -> {
                try {

                  //  t1.executer2();
                    //     // t1.generateType1UUID();
                    Thread.sleep(1000);

                } catch (Exception e) {

                } finally {

                }
            }).start();
        }

    }

}
