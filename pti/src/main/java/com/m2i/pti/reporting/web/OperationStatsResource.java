package com.m2i.pti.reporting.web;


import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.reporting.dto.EtatParams;
import com.m2i.pti.reporting.dto.ListeUo;
import com.m2i.pti.reporting.dto.XOperation;
import com.m2i.pti.reporting.service.OperationStatsService;
import javax.script.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.net.URISyntaxException;


/**
 * REST controller for managing .
 */
@RestController
@RequestMapping("/api/reporting")
public class OperationStatsResource {
    private final Logger log = LoggerFactory.getLogger(OperationStatsResource.class);

    private final ApplicationContext appContext;

    private final OperationStatsService operationStatsService;

    public OperationStatsResource(ApplicationContext appContext, OperationStatsService operationStatsService) {
        this.appContext = appContext;
        this.operationStatsService = operationStatsService;
    }

    /**
     * GET  /operation/EtatEnvoiCashPDF : get all the lnfoCommande.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of ligneBilans in body
     * @throws groovy.util.ScriptException
     * @throws java.net.URISyntaxException
     */
    @PostMapping("/operations/envoiCash")
    @Timed
    public ModelAndView getStatsEnvoiCash(@RequestBody EtatParams params) throws ScriptException, URISyntaxException {
        return this.operationStatsService.genererPdfEtatEnvoiCash(appContext, params.getUoConnecteId(), params.getDateDebut(), params.getDateFin());
    }

    @PostMapping("/operations/bordereau")
    @Timed
    public ModelAndView getBordereau(@RequestBody EtatParams params) throws ScriptException, URISyntaxException {
        return operationStatsService.getBordereauView(appContext, params.getCodeOperation());
    }

    @PostMapping("/operations/test_operaiton")
    @Timed
    public ModelAndView getReportOperation(@RequestBody EtatParams params) throws ScriptException, URISyntaxException {
        return operationStatsService.getBordereauView(appContext, params.getCodeOperation());
    }



    /*@GetMapping("/operations/stats")
    @Timed
    public ModelAndView BonCommandePDF(@RequestParam(required = false) Long uoConnecteId, @RequestParam Long uoPaysId, @RequestParam(required = false) Long typeServiceId) throws ScriptException, URISyntaxException {
        return this.operationStatsService.genererPdfEtatOperations(appContext, uoPaysId, typeServiceId, uoConnecteId);
    }*/


    @PostMapping("/operations/all")
    @Timed
    public ModelAndView getAllOperations(@RequestBody EtatParams params) {
        return operationStatsService.etatOfAllOperations(appContext, params.getUoSourceId(), params.getTypeServiceId(), params.getCanalId(), params.getUoAcquereurId(), params.getMontatOp(), params.getStatutOperation(), params.getDateDebut(), params.getDateFin());
    }


    @PostMapping("/operations/service-details")
    @Timed
    public ModelAndView getStatsByService(@RequestBody EtatParams params) throws ScriptException, URISyntaxException {
        return this.operationStatsService.genererPdfEtatOperations(appContext, params.getUoSourceId(), params.getUoPaysId(), params.getTypeServiceId(), params.getUoConnecteId(), params.getDateDebut(), params.getDateFin(), params.getFormat());
    }

    @PostMapping("/operations/service-details-by-uo")
    @Timed
    public ModelAndView getStatsByUo(@RequestBody ListeUo params) throws ScriptException, URISyntaxException {
        return this.operationStatsService.getOperationOfUoFils(appContext, params);
    }



    @PostMapping("/operations/service-bilan")
    @Timed
    public ModelAndView getStats(@RequestBody EtatParams params) {
        return operationStatsService.statsBilanOperations(appContext, params.getUoSourceId(), params.getTypeServiceId(), params.getCanalId(), params.getUoAcquereurId(), params.getMontatOp(), params.getStatutOperation(), params.getDateDebut(), params.getDateFin(), params.getFormat());

    }

    @PostMapping("/commissions/all")
    @Timed
    public ModelAndView getAllCommissions(@RequestBody EtatParams params) {
        return operationStatsService.etatOfAllCommissions(appContext, params.getUoSourceId(), params.getDateDebut(), params.getDateFin(), params.getEspPaye());
    }

    @PostMapping("/operations/cumuls")
    @Timed
    public ModelAndView getCumulOperationsStats(@RequestBody EtatParams params) {
        return operationStatsService.bilanEnvoisAndReceptions(appContext, params.getDateDebut(), params.getDateFin(), null, params.getFormat());
    }

    @PostMapping("/operations/cumuls_periode")
    @Timed
    public ModelAndView getCumulOperationsStatsPeriode(@RequestBody EtatParams params) {
        return operationStatsService.statsBilanOperationByActeur2(appContext, null);
    }

    @PostMapping("/operations/listing")
    @Timed
    public ModelAndView getOperationListing(@RequestBody XOperation xOperation) {
            return operationStatsService.getOperationListOptimise(appContext,xOperation);
    }



}
