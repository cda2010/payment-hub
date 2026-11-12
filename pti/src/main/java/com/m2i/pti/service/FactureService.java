package com.m2i.pti.service;


import com.m2i.pti.domain.Facture;
import com.m2i.pti.reporting.dto.XFacture;
import com.m2i.pti.service.dto.FactureDTO;
import com.m2i.pti.repository.FactureRepository;
import com.m2i.pti.service.util.RandomUtil;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.time.ZonedDateTime;
import java.text.SimpleDateFormat;
/**
 * Service Implementation for managing Facture.
 */
@Service
@Transactional
public class FactureService {
    private final Logger log = LoggerFactory.getLogger(FactureService.class);
    private final FactureRepository factureRepository;
    private String pathname;
    private final ResourceLoader resourceLoader;
    public FactureService(FactureRepository factureRepository, ResourceLoader resourceLoader) {
        this.factureRepository = factureRepository;
        this.resourceLoader = resourceLoader;
    }

    @Transactional(readOnly = true)
    public Page<Facture> findAll(String refFacture, String refPaiement, Long produit, String datePaiement, Pageable pageable) {
        log.debug("Request to get all Factures");
        List<Facture> list=new ArrayList<>();

        list= factureRepository.findFactureByCredential(refFacture,refPaiement,produit,datePaiement);
        int start= (int) pageable.getOffset();
        int end=(start+pageable.getPageSize())>list.size()?list.size():(start+pageable.getPageSize());
        Page<Facture> retour= new PageImpl<>(list.subList(start, end), pageable, list.size());
        return retour;
    }

    @Transactional(readOnly = true)
    public Page<FactureDTO> findAllOptimise(String refFacture, String refPaiement, Long produit, String datePaiement, Pageable pageable) {
        log.debug("Request to get all Factures");
        List<FactureDTO> list=new ArrayList<>();
        log.debug("----------------------------------------------");
        log.debug("{}",refFacture);
        log.debug("{}",refPaiement);
        log.debug("{}",produit);
        log.debug("{}",datePaiement);
        log.debug("----------------------------------------------");
        list = factureRepository.findFactureByCredentialOptimise(produit,refFacture,refPaiement,datePaiement);
        // ,refFacture,refPaiement, datePaiement
        log.debug("List of Factures========================================={}", list);
        int start= (int) pageable.getOffset();
        int end=(start+pageable.getPageSize())>list.size()?list.size():(start+pageable.getPageSize());
        Page<FactureDTO> retour= new PageImpl<>(list.subList(start, end), pageable, list.size());
        return retour;
    }

    @Transactional(readOnly = true)
    public StringBuilder getCSVFacture(String refFacture, String refPaiement, Long produit, String datePaiement, Long facturierId) throws FileNotFoundException,JRException{
        List<Facture> list=new ArrayList<>();
        XFacture xFacture=new XFacture();
        List<XFacture> xFactureList=new ArrayList<>();
        if (facturierId != null) {
            list= factureRepository.findFacturesOfProduit(facturierId);
        } else {
            list= factureRepository.findFactureByCredential(refFacture,refPaiement,produit,datePaiement);
        }
        if(list!=null){
            for (Facture facture:list){
                xFacture.setReferenceFacature(facture.getReferenceFacature());
                xFacture.setMontantFacture(facture.getMontantFacture());
                xFacture.setReferencePaiement(facture.getReferencePaiement());
                xFacture.setDatePaiement(facture.getDatePaiement());
                xFacture.setProduit(facture.getProduit().getNomProduit());
                xFactureList.add(xFacture);
                xFacture=new XFacture();
            }
            try {
                JasperReport jasperReport;
                Resource resource = resourceLoader.getResource("classpath:/report/facture.jasper");
                InputStream dbastream;

                if (resource.exists()) {

                    dbastream = resource.getInputStream();
                    jasperReport = (JasperReport) JRLoader.loadObject(dbastream);
                } else {
                    resource = resourceLoader.getResource("classpath:/report/facture.jrxml");
                    dbastream = resource.getInputStream();
                    jasperReport = JasperCompileManager.compileReport(dbastream);
                    JRSaver.saveObject(jasperReport, "classpath:/report/facture.jasper");
                }
                JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(xFactureList);
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, dataSource);
                JRCsvExporter exporter = new JRCsvExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                java.lang.StringBuilder sbuffer = new java.lang.StringBuilder();
                exporter.setExporterOutput(new SimpleWriterExporterOutput(sbuffer));
                pathname = System.getProperty("user.home") + File.separator + "facture" + RandomUtil.generateAlphaNumeric(2) + ".csv";
                final File csvFileName = new File(pathname);
                exporter.setExporterOutput(new SimpleWriterExporterOutput(csvFileName));
                exporter.exportReport();
                return sbuffer;
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
