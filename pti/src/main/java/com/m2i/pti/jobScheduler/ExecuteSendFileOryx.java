/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.jobScheduler;

import com.google.gson.Gson;
import com.m2i.pti.domain.Ftp;
import com.m2i.pti.domain.RecepteurEmail;
import com.m2i.pti.mobile.dto.MParametre;
import com.m2i.pti.repository.*;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.dto.CanalCommunicationDTO;
import com.m2i.pti.service.dto.TransactionComptePtiOryx;
import com.google.gson.JsonObject;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.JSONArray;
import com.google.gson.JsonArray;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author cda
 */
public class ExecuteSendFileOryx implements Serializable, Job {
    @Autowired
    ApiService apiService;
    @Autowired
    FacturierRepository facturierRepository;
    @Autowired
    CanalCommunicationRepository canalCommunicationRepository;
    @Autowired
    RecepteurEmailRepository recepteurEmailRepository;
    @Autowired
    FtpRepository ftpRepository;
    @Autowired
    TransactionComptePtiRepository transactionComptePtiRepository;

    public void excuteSenfileToFacturier(JobExecutionContext jobExecutionContext) throws IOException, JAXBException, JSONException {
        List<MParametre> listparam = new ArrayList();
        MParametre mparam = new MParametre();
        JobDataMap data = jobExecutionContext.getJobDetail().getJobDataMap();
        String codeFse1 = data.getString("PARAMETRE1");
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterdayStart = now.minusDays(1).toLocalDate().atStartOfDay(); // Hier à 00h00
        LocalDateTime yesterdayEnd = yesterdayStart.plusHours(23).plusMinutes(59).plusSeconds(59); // Hier à 23h59

        LocalDateTime startOfSeptember = LocalDateTime.of(2024, 9, 1, 0, 0,00);

        // Créer le 30 septembre 2024 à 23h59
        LocalDateTime endOfSeptember = LocalDateTime.of(2024, 9, 30, 23, 59,59);

        // Formatter pour afficher les dates au format 'yyyy-MM-dd HH:mm:ss'
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Afficher les dates d'hier
        System.out.println("Hier à 00h00: " + yesterdayStart.format(formatter));
        System.out.println("Hier à 23h59: " + yesterdayEnd.format(formatter));
        System.out.println("Datefin: " + endOfSeptember.format(formatter));

        String numCompte = data.getString("PARAMETRE2");
        Gson gson = new Gson();
        String queryResult;
        queryResult = gson.toJsonTree(transactionComptePtiRepository.queryResultAFB120(numCompte, yesterdayStart, yesterdayEnd)).toString();
        System.out.println("-------------Query result" + queryResult);

        if(queryResult.equals("[]")) {
            System.out.println("-------------Query result2");

            queryResult = gson.toJsonTree(transactionComptePtiRepository.queryResultAFB120Null(numCompte, yesterdayStart)).toString();
        }

        System.out.println("-------------Query result" + queryResult);
        String mail = data.getString("PARAMETRE3");
        String filename = data.getString("PARAMETRE4");
        //JSONObject json = new JSONObject();
        mparam.setCodeParametre("queryResult");
        mparam.setValeur(queryResult);
        listparam.add(mparam);
        mparam = new MParametre();

        mparam.setCodeParametre("mail");
        mparam.setValeur(mail);
        listparam.add(mparam);
        mparam = new MParametre();


        mparam.setCodeParametre("numCompte");
        mparam.setValeur(numCompte);
        listparam.add(mparam);
        mparam = new MParametre();


        mparam.setCodeParametre("filename");
        mparam.setValeur(filename);

        listparam.add(mparam);

        apiService.sendFileMail(listparam, codeFse1);

    }

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        try {
            excuteSenfileToFacturier(jec);
        } catch (IOException ex) {
            Logger.getLogger(ExecuteSendFileOryx.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException | JSONException ex) {
            Logger.getLogger(ExecuteSendFileOryx.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
