/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.m2i.pti.jobScheduler;

import com.m2i.pti.domain.CanalCommunication;
import com.m2i.pti.domain.Facturier;
import com.m2i.pti.domain.Ftp;
import com.m2i.pti.domain.RecepteurEmail;
import com.m2i.pti.mobile.dto.MParametre;
import com.m2i.pti.repository.CanalCommunicationRepository;
import com.m2i.pti.repository.FacturierRepository;
import com.m2i.pti.repository.FtpRepository;
import com.m2i.pti.repository.RecepteurEmailRepository;
import com.m2i.pti.service.ApiService;
import com.m2i.pti.service.dto.CanalCommunicationDTO;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.xml.bind.JAXBException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author cda
 */
public class ExecuteSendFile implements Serializable, Job {
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
    public void excuteSenfileToFacturier() throws IOException, JAXBException {
        List<CanalCommunicationDTO> listCanalFacturier = canalCommunicationRepository.findListCommunication();
        for (CanalCommunicationDTO canal : listCanalFacturier) {
            String codeFse = canal.getFse();
            if (canal.getRecepteurEmailId() != null) {
                RecepteurEmail recepteurEmail = recepteurEmailRepository.findById(canal.getRecepteurEmailId()).orElse(null);
                if (recepteurEmail != null) {
                    if (recepteurEmail.getEmail() != null) {
                        String email = recepteurEmail.getEmail();
                        List<MParametre> listparam = new ArrayList();
                        MParametre mparam = new MParametre();
                        mparam.setCodeParametre("EMAIL");
                        mparam.setValeur(email);
                        listparam.add(mparam);
                        apiService.sendFileMail(listparam, codeFse);
                    }

                }
            }
            if (canal.getFtpId() != null) {
                Ftp ftp = ftpRepository.findById(canal.getFtpId()).orElse(null);
                if (ftp != null) {
                    if (ftp.getId() != null) {
                        String username = ftp.getUsername();
                        String password = ftp.getPassword();
                        String url = ftp.getUrl();
                        String repertoire = ftp.getRepertoire();
                        List<MParametre> listparam = new ArrayList();
                        MParametre mparam = new MParametre();
                        mparam.setCodeParametre("USERNAME");
                        mparam.setValeur(username);
                        listparam.add(mparam);
                        mparam = new MParametre();
                        mparam.setCodeParametre("PASSWORD");
                        mparam.setValeur(password);
                        listparam.add(mparam);
                        mparam = new MParametre();
                        mparam.setCodeParametre("URL");
                        mparam.setValeur(url);
                        listparam.add(mparam);
                        mparam = new MParametre();
                        mparam.setCodeParametre("REPERTOIRE");
                        mparam.setValeur(repertoire);
                        listparam.add(mparam);
                        apiService.sendFileFTP(listparam, codeFse);
                    }

                }
            }

        }
    }

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        try {
            excuteSenfileToFacturier();
        } catch (IOException ex) {
            Logger.getLogger(ExecuteSendFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JAXBException ex) {
            Logger.getLogger(ExecuteSendFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
