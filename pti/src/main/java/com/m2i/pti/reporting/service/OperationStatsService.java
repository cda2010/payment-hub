package com.m2i.pti.reporting.service;

import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.domain.enumeration.TypeParametre;
import com.m2i.pti.domain.enumeration.TypeServiceLibelle;
import com.m2i.pti.domain.enumeration.TypeUO;
import com.m2i.pti.reporting.ReportFormat;
import com.m2i.pti.reporting.ReportUtils;
import com.m2i.pti.reporting.dto.*;
import com.m2i.pti.reporting.util.Constants;
import com.m2i.pti.repository.*;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.service.OperationService;
import com.m2i.pti.service.TypeServiceService;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.service.UserService;
import com.m2i.pti.service.dto.OperationDTO;
import com.m2i.pti.service.dto.PointVenteDTO;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import com.m2i.pti.service.mapper.OperationMapper;
import com.m2i.pti.service.mapper.UniteOrganisationMapper;
import com.m2i.pti.service.util.Util;
import javax.script.ScriptException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsPdfView;

import java.io.File;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 */
@Service
@Transactional
public class OperationStatsService {

    private final Logger log = LoggerFactory.getLogger(OperationStatsService.class);
    private static final String PARAM_DATASOURCE = "datasource";
    private final UniteOrganisationService uniteOrganisationService;
    private final TypeServiceService typeServiceService;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    private final OperationRepository operationRepository;
    private final TypeServiceRepository typeServiceRepository;
    private final OperationMapper operationMapper;
    private final PointVenteRepository pointVenteRepository;
    private final CommissionRepository commissionRepository;
    private final UniteOrganisationMapper uniteOrganisationMapper;
    private final PieceRepository pieceRepository;
    private final OperationService operationService;
    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;
    private final UserService userService;
    private final CompteRepository compteRepository;
    private final ParametreAppRepository parametreAppRepository;

    public OperationStatsService(UniteOrganisationService uniteOrganisationService, TypeServiceService typeServiceService, UniteOrganisationRepository uniteOrganisationRepository, OperationRepository operationRepository, TypeServiceRepository typeServiceRepository, OperationMapper operationMapper, PointVenteRepository pointVenteRepository, CommissionRepository commissionRepository, UniteOrganisationMapper uniteOrganisationMapper, PieceRepository pieceRepository, OperationService operationService, TypeUniteOrganisationRepository typeUniteOrganisationRepository, UserService userService, CompteRepository compteRepository,ParametreAppRepository parametreAppRepository) {
        this.uniteOrganisationService = uniteOrganisationService;
        this.typeServiceService = typeServiceService;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.operationRepository = operationRepository;
        this.typeServiceRepository = typeServiceRepository;
        this.operationMapper = operationMapper;
        this.pointVenteRepository = pointVenteRepository;
        this.commissionRepository = commissionRepository;
        this.uniteOrganisationMapper = uniteOrganisationMapper;
        this.pieceRepository = pieceRepository;
        this.operationService = operationService;
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.userService = userService;
        this.compteRepository = compteRepository;
        this.parametreAppRepository=parametreAppRepository;
    }

    private final static String FICHE_OP = Constants.JASPER_REPORT_DIRECTORY + "/etats_envoi_cash.jasper";
    private final static String FICHE_OP_RETRAIT = Constants.JASPER_REPORT_DIRECTORY + "/etats_retraitNew.jasper";
    private final static String FICHE_OP_AGENT = Constants.JASPER_REPORT_DIRECTORY + "/etats_uo_fils_new.jasper";
    private final static String FICHE_OP_AGENT_SD = Constants.JASPER_REPORT_DIRECTORY + "/etat_uo_once_op.jasper";
    private final static String FICHE_OP_APPROVISSIONNEMENT = Constants.JASPER_REPORT_DIRECTORY + "/etats_approvisionnement.jasper";
    private final static String FICHE_ALL_OP = Constants.JASPER_REPORT_DIRECTORY + "/etats_all_operations.jasper";
    private final static String FICHE_ALL_COM = Constants.JASPER_REPORT_DIRECTORY + "/etats_all_commissions.jasper";
    private final static String FICHE_ALL_COM_UO = Constants.JASPER_REPORT_DIRECTORY + "/etats_all_commissions_uo.jasper";
    private final static String OPERAION_LISTE_PDF = Constants.JASPER_REPORT_DIRECTORY + "/operations_pdf.jasper";
    private final static String OPERAION_LISTE_XLSX = Constants.JASPER_REPORT_DIRECTORY + "/operations_xlsx.jasper";
    private final static String BORDEREAU = Constants.JASPER_REPORT_DIRECTORY + "/reçu_operation.jasper";

    private final static String BORDEREAU_RETRAIT = Constants.JASPER_REPORT_DIRECTORY + "/reçu_op_retrait.jasper";
    private final static String BORDEREAU_APPROVISSIONNEMENT = Constants.JASPER_REPORT_DIRECTORY + "/reçu_op_approvisionnement.jasper";

    private final static String BILAN_OP = Constants.JASPER_REPORT_DIRECTORY + "/DailyReport.jasper";
    private final static String BILAN_OP_XLSX = Constants.JASPER_REPORT_DIRECTORY + "/DailyReport.jasper";
    private final static String BILAN_CUMUL_OP_PDF = Constants.JASPER_REPORT_DIRECTORY + "/VolumeStructureStatistiqueReport2_pdf.jasper";
    private final static String BILAN_CUMUL_OP_XLSX = Constants.JASPER_REPORT_DIRECTORY + "/VolumeStructureStatistiqueReport2_xlsx.jasper";
    private final static String BILAN_CUMUL_OP_PERIODE = Constants.JASPER_REPORT_DIRECTORY + "/VolumeStructureReport.jasper";

    public ModelAndView getBordereauView(ApplicationContext appContext, String codeOperation) {
        Operation operation = null;
        Optional<Operation> oper = operationRepository.findByCodeOperation(codeOperation);
        if (oper.isPresent()) {
            operation = oper.get();

            Bordereau bordereau = getOpBordereau(operation);
            JasperReportsPdfView view = new JasperReportsPdfView();
            if (operation != null) {
                if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.RETRAIT))) {
                    view.setUrl(BORDEREAU_RETRAIT);
                } else if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT))
                        || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT_INTERNE)) || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.CREATION_MONNAIE))) {
                    view.setUrl(BORDEREAU_APPROVISSIONNEMENT);
                } else {
                    view.setUrl(BORDEREAU);
                }
            }
            Map<String, Object> params = new HashMap<>();
            view.setApplicationContext(appContext);
            JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singleton(bordereau));
            params.put(PARAM_DATASOURCE, dataSource);
            return new ModelAndView(view, params);
        } else {
            return null;
        }
    }

    /**
     * Stats des operations ENVOI_CASH par destination
     *
     * @param appContext
     * @param uoEmeteurId
     * @param dateDepart
     * @param dateArrive
     * @return
     * @throws ScriptException
     * @throws URISyntaxException
     */
    public ModelAndView genererPdfEtatEnvoiCash(ApplicationContext appContext, Long uoEmeteurId, ZonedDateTime dateDepart, ZonedDateTime dateArrive) throws ScriptException, URISyntaxException {
        JasperReportsPdfView view = new JasperReportsPdfView();
        Map<String, Object> params = new HashMap<>();
        view.setUrl("classpath:/report/cashToCashByDest.jrxml");
        view.setApplicationContext(appContext);
        params.put("datasource", this.findByCTCByMultiParams(uoEmeteurId, dateDepart, dateArrive));
        return new ModelAndView(view, params);
    }

    /**
     * @param appContext
     * @param typeServiceId
     * @param uoConnecteId
     * @return
     * @throws ScriptException
     * @throws URISyntaxException
     */
    public ModelAndView genererPdfEtatOperations(ApplicationContext appContext, Long uoSourceId, Long uoPaysId, Long typeServiceId, Long uoConnecteId, ZonedDateTime dateDebut, ZonedDateTime dateFin, String format) throws ScriptException, URISyntaxException {
        // JasperReportsPdfView view = new JasperReportsPdfView();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        List<XOperationDTO> operations = new ArrayList<>();
        List<UniteOrganisationDTO> uos;
        if (uoPaysId != null) {//Filtre en fonction de la destination
            uos = new ArrayList<>(Arrays.asList(uniteOrganisationRepository.findDtoById(uoPaysId)));
        } else {//Filtre en fonction des organisations de type Pays
            uos = uniteOrganisationService.findAllByTypeUo(TypeUO.PAYS);
        }
        for (UniteOrganisationDTO uo : uos) {
            operations.addAll(statsOfOperationsByCountry(uoSourceId, uoConnecteId, uo.getId(), typeServiceId, dateDebut, dateFin));
        }
        String titre = getTitreWithDate(dateDebut, dateFin);
        Double total = operations.stream().mapToDouble(operation -> operation.getMontant().doubleValue()).sum();
        FicheOperations ficheOperations = new FicheOperations();
        ficheOperations.setInfosOperations(operations);
        ficheOperations.setTotal(total);
        ficheOperations.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (uoSourceId != null) {
            ficheOperations.setOrganisation("Organisation : " + uniteOrganisationRepository.findById(uoSourceId).orElse(null).getLibelleUniteOrganisation());
        } else {
            ficheOperations.setOrganisation("Organisation : " + uniteOrganisationService.getUoOfUserConnecte().getLibelleUniteOrganisation());
        }

        if (typeServiceId != null) {
            TypeService typeService = typeServiceRepository.findById(typeServiceId).orElse(null);
            if (typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.RETRAIT))) {
                if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
                    view.setUrl(FICHE_OP_RETRAIT);
                } else {
                    view.setUrl(FICHE_OP_RETRAIT);
                }
            } else if (typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT))
                    || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT_INTERNE))) {
                if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
                    view.setUrl(FICHE_OP_APPROVISSIONNEMENT);
                } else {
                    view.setUrl(FICHE_OP_APPROVISSIONNEMENT);
                }
            } else if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
                view.setUrl(FICHE_OP);
            } else {
                view.setUrl(FICHE_OP);
            }
            ficheOperations.setTitre("BILAN " + typeService.getLibelle().toUpperCase() + titre);
        } else {
            ficheOperations.setTitre("BILAN DE TOUTES LES OPERATIONS PAR PAYS" + titre);

            if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
                view.setUrl(FICHE_OP_RETRAIT);
            } else {
                view.setUrl(FICHE_OP_RETRAIT);
            }
        }
        view.setApplicationContext(appContext);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singleton(ficheOperations));
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getOperationOfUoFils(ApplicationContext appContext, ListeUo param) {
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(param.getFormat()));
        Map<String, Object> params = new HashMap<>();
        List<XOperationDTO> operations = new ArrayList<>();
        List<XOperationAgent> infoOp = new ArrayList<>();
        XOperationAgent xOperationAgent = new XOperationAgent();
        List<UniteOrganisationDTO> uos = new ArrayList<>();
        UniteOrganisation pereUo = uniteOrganisationRepository.findById(param.getPereId()).orElse(null);
        if (param.getPereId() != null) {
            uos = uniteOrganisationMapper.toDto(uniteOrganisationRepository.findAllInList(param.getAgentIds()));
        }

        for (UniteOrganisationDTO uo : uos) {
            Compte compteOperationPTI = compteRepository.findCopmteOperationPTi(uo.getId());
            List<Long> pvIds = pointVenteRepository.findPvIdsOfUniteOrganisation(uo.getId());
            //List<Operation> operationsF=operationService.findOperationByCompteAutre(compteOperationPTI.getCompteAutre().getId(),param.getDateDebut(),param.getDateFin());
            List<Operation> operationsF = operationService.findOperationByPointVenteIds(pvIds, param.getDateDebut(), param.getDateFin());
            xOperationAgent.setUoEmetteur(uo.getLibelleUniteOrganisation());
            Double mnt = operationsF.stream().mapToDouble(operation -> operation.getMontantOp().doubleValue()).sum();

            xOperationAgent.setTotal(mnt);
            infoOp.add(xOperationAgent);
            xOperationAgent = new XOperationAgent();
            operations.addAll(getJasperOp(operationsF, uniteOrganisationMapper.toEntity(uo), new BigDecimal(mnt)));
        }
        Double total = operations.stream().mapToDouble(operation -> operation.getMontant().doubleValue()).sum();
        FicheOperations ficheOperations = new FicheOperations();

        ficheOperations.setOrganisation(pereUo.getLibelleUniteOrganisation());
        ficheOperations.setTotal(total);
        if (param.getDateDebut() != null || param.getDateFin() != null) {
            String dateDebut = param.getDateDebut().format(DateTimeFormatter.ofPattern("yyyy-MM-dd KK:mm:ss a"));
            String dateFin = param.getDateFin().format(DateTimeFormatter.ofPattern("yyyy-MM-dd KK:mm:ss a"));
            ficheOperations.setTitre("BILAN DES OPERATIONS DE L'AGENT DU" + " " + dateDebut + " " + "au" + " " + dateFin);
        } else {
            ficheOperations.setTitre("BILAN DES OPERATIONS DE L'AGENT");
        }
        ficheOperations.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        if (param.getType().equals("OSD")) {
            view.setUrl(FICHE_OP_AGENT_SD);
            ficheOperations.setInfos(infoOp);
        } else {
            view.setUrl(FICHE_OP_AGENT);
            ficheOperations.setInfosOperations(operations);
        }
        view.setApplicationContext(appContext);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singleton(ficheOperations));
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
        //return null;
    }

    public ModelAndView statsBilanOperations(ApplicationContext appContext, Long uoSourceId, Long typeServiceId,
            Long canalId, Long uoAcquereurId, BigDecimal montantOp,
            StatutOperation statutOperation, ZonedDateTime dateOperation,
            ZonedDateTime dateFin, String format) {
        //JasperReportsPdfView view = new JasperReportsPdfView();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        List<Operation> operations = getAllOperations(uoSourceId, typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, dateOperation, dateFin);
        Double total = operations.stream().mapToDouble(operation -> operation.getMontantOp().doubleValue()).sum();

        BigDecimal totalOp = BigDecimal.valueOf(total);
        List<XOperationDTO> items = getJasperOp(operations, null, totalOp);

        String titre = getTitreWithDate(dateOperation, dateFin);

        FicheOperations ficheOperations = new FicheOperations();
        ficheOperations.setInfosOperations(items);
        ficheOperations.setTotalMontant(totalOp);
        ficheOperations.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl(BILAN_OP);
        } else {
            view.setUrl(BILAN_OP);
        }

        ficheOperations.setTitre("BILAN DES OPERATIONS" + titre);
        view.setApplicationContext(appContext);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singleton(ficheOperations));
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);

    }

    /**
     * Bilan envoi & reception
     *
     * @param appContext
     * @param dateDebut
     * @param dateFin
     * @return
     */
    public ModelAndView bilanEnvoisAndReceptions(ApplicationContext appContext, ZonedDateTime dateDebut, ZonedDateTime dateFin, UniteOrganisation uoSource, String format) {
        //JasperReportsPdfView view = new JasperReportsPdfView();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();

        FicheOperations ficheOperations = new FicheOperations();

        List<XCumulOperation> cumulOperations = new ArrayList<>();

        ArrayList<UniteOrganisation> uos = new ArrayList<>();
        findOrganisations(uoSource, uos);

        for (UniteOrganisation u : uos) {
            List<PointVente> pointVentes = pointVenteRepository.findByUniteOrganisation(u);
            List<Long> pvIds = pointVentes.stream().map(pointVente -> pointVente.getId()).collect(Collectors.toList());
            TypeService typeService = typeServiceRepository.findTopByCode(TypeServiceLibelle.ENVOI_CASH.name());

            List<Operation> opsEnvoye = new ArrayList<>();
            bilanOperationOfPvs(dateDebut, dateFin, pvIds, typeService, opsEnvoye);
            XCumulOperation xcumul = new XCumulOperation();
            xcumul.setAgence(u.getLibelleUniteOrganisation());
            xcumul.setMontantEnvoi(BigDecimal.valueOf(opsEnvoye.stream().mapToDouble(operation -> operation.getMontantOp().doubleValue()).sum()));
            xcumul.setNombreEnvoi(opsEnvoye.size());

            TypeService typeServiceReception = typeServiceRepository.findTopByCode(TypeServiceLibelle.RETRAIT.name());
            List<Operation> opsReception = new ArrayList<>();
            bilanOperationOfPvs(dateDebut, dateFin, pvIds, typeServiceReception, opsReception);

            xcumul.setMontantReception(BigDecimal.valueOf(opsReception.stream().mapToDouble(operation -> operation.getMontantOp().doubleValue()).sum()));
            xcumul.setNombreReception(opsReception.size());

            xcumul.setMontantEnvoiTotal(BigDecimal.valueOf(cumulOperations.stream().mapToDouble(operation -> operation.getMontantEnvoi().doubleValue()).sum()).add(xcumul.getMontantEnvoi()));
            xcumul.setNombreEnvoiTotal(cumulOperations.stream().mapToInt(op -> op.getNombreEnvoi()).sum() + xcumul.getNombreEnvoi());

            xcumul.setNombreReceptionTotal(cumulOperations.stream().mapToInt(op -> op.getNombreReception()).sum() + xcumul.getNombreReception());
            xcumul.setMontantReceptionTotal(BigDecimal.valueOf(cumulOperations.stream().mapToDouble(operation -> operation.getMontantReception().doubleValue()).sum()).add(xcumul.getMontantReception()));

            cumulOperations.add(xcumul);
        }

        String periode = "Volume par structure " + getTitreWithDate(dateDebut, dateFin);
        ficheOperations.setPeriode(periode);
        ficheOperations.setFse(periode.toUpperCase());
        ficheOperations.setInfosStatistiques(cumulOperations);
        ficheOperations.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl(BILAN_CUMUL_OP_XLSX);
        } else {
            view.setUrl(BILAN_CUMUL_OP_PDF);
        }
        view.setApplicationContext(appContext);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singleton(ficheOperations));
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    /**
     * @param dateDebut
     * @param dateFin
     * @param pvIds
     * @param typeService
     * @param ops
     */
    private void bilanOperationOfPvs(ZonedDateTime dateDebut, ZonedDateTime dateFin, List<Long> pvIds, TypeService typeService, List<Operation> ops) {
        if (!pvIds.isEmpty()) {//Bilan des points de vente de l'agence
            for (int j = 0; j < pvIds.size(); j++) {
                if (dateDebut != null && dateFin != null) {
                    ops.addAll(operationRepository.findOperationByPointVenteAndPeriodeAndService(typeService.getId(), pvIds.get(j), dateDebut, dateFin.plusHours(23)));
                } else if (dateDebut != null) {
                    ops.addAll(operationRepository.findOperationByPointVenteAndPeriodeAndService(typeService.getId(), pvIds.get(j), dateDebut, dateDebut.plusHours(23)));
                } else if (dateFin != null) {
                    ops.addAll(operationRepository.findOperationByPointVenteAndPeriodeAndService(typeService.getId(), pvIds.get(j), ZonedDateTime.now().minusMonths(6), dateFin.plusHours(23)));
                } else {
                    ops.addAll(operationRepository.findOperationByPointVenteAndPeriodeAndService(typeService.getId(), pvIds.get(j), dateDebut, dateFin));
                }
            }
        }
    }

    /**
     * @param appContext
     * @return
     */
    public ModelAndView statsBilanOperationByActeur2(ApplicationContext appContext, UniteOrganisation uoSource) {
        JasperReportsPdfView view = new JasperReportsPdfView();
        Map<String, Object> params = new HashMap<>();
        ArrayList<UniteOrganisation> uos = new ArrayList<>();
        List<Integer> months = new ArrayList<>();
        months.add(8);
        months.add(9);
        findOrganisations(uoSource, uos);
        TypeService typeServiceReception = typeServiceRepository.findTopByCode(TypeServiceLibelle.RETRAIT.name());
        TypeService typeService = typeServiceRepository.findTopByCode(TypeServiceLibelle.ENVOI_CASH.name());

        List<Long> pvIds = new ArrayList<>();
        for (UniteOrganisation u : uos) {
            List<PointVente> pointVentes = pointVenteRepository.findByUniteOrganisation(u);
            pvIds.addAll(pointVentes.stream().map(pointVente -> pointVente.getId()).collect(Collectors.toList()));
        }
        FicheOperations ficheOperations = new FicheOperations();
        List<XCumulOperation> infoAnalyseMontant = new ArrayList<>();
        List<XCumulOperation> infoAnalyseNombre = new ArrayList<>();
        List<XCumulOperation> graphicMontant = new ArrayList<>();
        List<XCumulOperation> graphicNombre = new ArrayList<>();

        for (Integer month : months) {
            List<Operation> opsEnvois = new ArrayList<>();
            List<Operation> opsReceptions = new ArrayList<>();
            for (Long pvId : pvIds) {
                opsReceptions.addAll(operationRepository.findOperationByPointVenteAndMonth(typeServiceReception.getId(), pvId));
                opsEnvois.addAll(operationRepository.findOperationByPointVenteAndMonth(typeService.getId(), pvId));
            }
            BigDecimal montantEnvoi = BigDecimal.valueOf(opsEnvois.stream().mapToDouble(operation -> operation.getMontantOp().doubleValue()).sum());
            BigDecimal montantReception = BigDecimal.valueOf(opsReceptions.stream().mapToDouble(operation -> operation.getMontantOp().doubleValue()).sum());

            XCumulOperation d1 = new XCumulOperation();
            d1.setMontantReception(Util.getZeroIfNull(montantReception));
            d1.setMontantEnvoi(Util.getZeroIfNull(montantEnvoi));
            d1.setPeriode("M " + month);
            d1.setMontantEnvoiTotal(BigDecimal.valueOf(infoAnalyseMontant.stream().mapToDouble(operation -> operation.getMontantEnvoiTotal().doubleValue()).sum()).add(Util.getZeroIfNull(montantEnvoi)));
            d1.setMontantReceptionTotal(BigDecimal.valueOf(infoAnalyseMontant.stream().mapToDouble(operation -> operation.getMontantReceptionTotal().doubleValue()).sum()).add(Util.getZeroIfNull(montantReception)));

            infoAnalyseMontant.add(d1);

            XCumulOperation d2 = new XCumulOperation();
            d2.setNombreReceptionTotal(infoAnalyseMontant.stream().mapToInt(operation -> Util.getZeroIfNull(operation.getNombreReception())).sum() + opsReceptions.size());
            d2.setNombreEnvoiTotal(infoAnalyseMontant.stream().mapToInt(operation -> Util.getZeroIfNull(operation.getNombreEnvoi())).sum() + opsEnvois.size());
            d2.setNombreReception(opsReceptions.size());
            d2.setNombreEnvoi(opsEnvois.size());
            d2.setPeriode("M " + month);
            infoAnalyseNombre.add(d2);

            /*   XCumulOperation d3 = new XCumulOperation();
            d3.setMontant(BigDecimal.TEN);
            d3.setPeriode("M " + month);
            d3.setTypeTransaction("ENVOI");
            graphicMontant.add(d3);

            XCumulOperation d4 = new XCumulOperation();
            d4.setMontant(BigDecimal.ONE);
            d3.setPeriode("M " + month);
            d4.setTypeTransaction("RECEPTION");
            graphicMontant.add(d4);

/*
            XCumulOperation d5 = new XCumulOperation();
            d3.setNombreEnvoi(opsEnvois.size());
            d3.setPeriode("M " + month);
            d3.setTypeTransaction("ENVOI");
            graphicNombre.add(d5);

            XCumulOperation d6 = new XCumulOperation();
            d4.setNombreEnvoi(opsReceptions.size());
            d3.setPeriode("M " + month);
            d4.setTypeTransaction("RECEPTION");
            graphicNombre.add(d6);*/
        }

        ficheOperations.setReseau("Rapidex");
        ficheOperations.setInfoAnalyseMontant(infoAnalyseMontant);
        ficheOperations.setInfoAnalyseNombre(infoAnalyseNombre);
        ficheOperations.setGraphicMontant(graphicMontant);
        ficheOperations.setGraphicNombre(graphicMontant);

        view.setUrl(BILAN_CUMUL_OP_PERIODE);
        ficheOperations.setTitre("BILAN DE TOUTES LES OPERATIONS PAR PAYS");
        view.setApplicationContext(appContext);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singleton(ficheOperations));
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    private void findOrganisations(UniteOrganisation uoSource, ArrayList<UniteOrganisation> uos) {
        if (uoSource != null) {
            uos.add(uoSource);
        } else {
            User userConnecte = userService.getUserWithAuthorities();
            UniteOrganisation uo = userConnecte.getUniteOrganisation();
            if (userConnecte.getAuthorities().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AuthoritiesConstants.ADMIN))) {
                ArrayList<UniteOrganisation> listFils = new ArrayList<>();
                uos.add(userConnecte.getUniteOrganisation());
                uos.addAll(uniteOrganisationService.findAllUoFils(userConnecte.getUniteOrganisation(), uos, listFils));
            } else {
                List<TypeUniteOrganisation> typeUniteOrganisations = typeUniteOrganisationRepository.findByNiveauActeur();
                for (TypeUniteOrganisation t : typeUniteOrganisations) {
                    ArrayList<UniteOrganisation> listFils = new ArrayList<>();
                    uos.addAll(uniteOrganisationMapper.toEntity(uniteOrganisationService.findAllUoFilsByType(TypeUO.valueOf(t.getCodeTypeUO()), uo, listFils)));
                }
            }
        }
    }

    private String getTitreWithDate(ZonedDateTime dateDebut, ZonedDateTime dateFin) {
        String titre = "";
        if (dateDebut != null) {
            titre = " : " + formatDateOnly(dateDebut);
        }
        if (dateFin != null) {
            titre += " au " + formatDateOnly(dateFin);
        }
        return titre;
    }

    /**
     * Etats de toutes les commissions
     *
     * @param appContext
     * @param uoId
     * @param dateDebut
     * @param dateFin
     * @param espPaye
     * @return
     */
    public ModelAndView etatOfAllCommissions(ApplicationContext appContext, Long uoId, ZonedDateTime dateDebut, ZonedDateTime dateFin, Boolean espPaye) {
        FicheCommissions ficheCommissions = new FicheCommissions();
        if (uoId == null) {
            UniteOrganisation uo = uniteOrganisationService.getUoOfUserConnecte();
            uoId = uo.getId();
            ficheCommissions.setOrganisation("Organisation : " + uo.getLibelleUniteOrganisation());
        } else {
            UniteOrganisation uo = uniteOrganisationRepository.findById(uoId).orElse(null);
            ficheCommissions.setOrganisation("Organisation : " + uo.getLibelleUniteOrganisation());
        }
        List<XCommissionDTO> operations = getAllCommisssion(uoId, dateDebut, dateFin, espPaye);
        ficheCommissions.setInfosCommissions(operations);
        String titre = getTitreWithDate(dateDebut, dateFin);
        ficheCommissions.setTitre("Bilan des commissions".toUpperCase() + " " + titre);
        Double total = operations.stream().mapToDouble(operation -> operation.getMontant().doubleValue()).sum();
        ficheCommissions.setTotal(total);
        JasperReportsPdfView view = new JasperReportsPdfView();
        Map<String, Object> params = new HashMap<>();
        if (uoId == null) {
            view.setUrl(FICHE_ALL_COM);
        } else {
            view.setUrl(FICHE_ALL_COM_UO);
        }
        view.setApplicationContext(appContext);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singleton(ficheCommissions));
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    /**
     * Liste des operations sous forme de fichier pdf
     *
     * @param appContext
     * @param typeServiceId
     * @param canalId
     * @param uoAcquereurId
     * @param montantOp
     * @param statutOperation
     * @param dateOperation
     * @param dateFin
     * @return
     */
    public ModelAndView etatOfAllOperations(ApplicationContext appContext, Long uoSourceId, Long typeServiceId,
            Long canalId, Long uoAcquereurId, BigDecimal montantOp,
            StatutOperation statutOperation, ZonedDateTime dateOperation,
            ZonedDateTime dateFin) {
        FicheOperations ficheOperations = new FicheOperations();
        List<Operation> operations = getAllOperations(uoSourceId, typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, dateOperation, dateFin);
        if (uoSourceId == null) {
            UniteOrganisation uo = uniteOrganisationService.getUoOfUserConnecte();
            ficheOperations.setOrganisation("Organisation : " + uo.getLibelleUniteOrganisation());
        } else {
            UniteOrganisation uo = uniteOrganisationRepository.findById(uoSourceId).orElse(null);
            ficheOperations.setOrganisation("Organisation : " + uo.getLibelleUniteOrganisation());
        }
        ficheOperations.setInfosOperations(getJasperOp(operations, null, BigDecimal.ZERO));
        String titre = getTitreWithDate(dateOperation, dateFin);
        ficheOperations.setTitre("BILAN DES OPERATIONS " + titre);
        Double total = operations.stream().mapToDouble(operation -> operation.getMontantOp().doubleValue()).sum();
        ficheOperations.setTotal(total);
        getFicheInformations(ficheOperations, typeServiceId, uoAcquereurId, dateOperation, dateFin);
        JasperReportsPdfView view = new JasperReportsPdfView();
        Map<String, Object> params = new HashMap<>();
        view.setUrl(FICHE_ALL_OP);
        view.setApplicationContext(appContext);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singleton(ficheOperations));
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    FicheOperations getFicheInformations(FicheOperations ficheOperations, Long typeServiceId, Long uoAcquereurId, ZonedDateTime dateOperation, ZonedDateTime dateFin) {
        if (typeServiceId != null) {
            ficheOperations.setService(typeServiceRepository.findById(typeServiceId).orElse(null).getLibelle());
        }
        if (uoAcquereurId != null) {
            ficheOperations.setUoAcquereur(uniteOrganisationRepository.findById(uoAcquereurId).orElse(null).getLibelleUniteOrganisation());
        }
        if (dateFin != null && dateOperation != null) {
            ficheOperations.setPeriode(formatDate(dateOperation) + " " + formatDate(dateFin));
        }
        return ficheOperations;
    }

    /**
     * Filtre des opérations
     *
     * @param typeServiceId
     * @param canalId
     * @param uoAcquereurId
     * @param montantOp
     * @param statutOperation
     * @param dateOperation
     * @param dateFin
     * @return
     */
    public List<Operation> getAllOperations(Long uoSourceId, Long typeServiceId, Long canalId, Long uoAcquereurId, BigDecimal montantOp, StatutOperation statutOperation, ZonedDateTime dateOperation, ZonedDateTime dateFin) {
        List<Long> pvIds;
        if (uoSourceId != null) {
            UniteOrganisation uoSource = uniteOrganisationRepository.findById(uoSourceId).orElse(null);

            List<PointVente> listepointVentes = pointVenteRepository.findByUniteOrganisationId(uoSource.getId());
            pvIds = listepointVentes.stream().map(pointVente -> pointVente.getId()).collect(Collectors.toList());
            // pvIds =  .stream().map(pointVente -> pointVente.getId()).collect(Collectors.toList());
        } else {
            pvIds = operationService.getPointVenteIdsOfUserConnecte(null);
        }
        if (!pvIds.isEmpty()) {
            if (dateOperation != null && dateFin != null) {
                return operationRepository.findAllOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pvIds, montantOp, statutOperation, dateOperation, dateFin.plusHours(24));

            } else if (dateOperation != null) {
                return operationRepository.findAllOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pvIds, montantOp, statutOperation, dateOperation, dateOperation.plusHours(23));

            }
            log.debug("point de vente" + pvIds);
            return operationRepository.findAllOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pvIds, montantOp, statutOperation, dateOperation, dateFin);
            //return  operationRepository.findAll();
        }
        return Collections.emptyList();
    }

    public List<OperationDTO> getAllOperationsOptimiser(Long uoSourceId, Long typeServiceId, Long canalId, Long uoAcquereurId, BigDecimal montantOp, StatutOperation statutOperation, ZonedDateTime dateOperation, ZonedDateTime dateFin) {
        List<Long> pvIds;
        if (uoSourceId != null) {
            UniteOrganisationDTO uoSource = uniteOrganisationRepository.findDtoById(uoSourceId);

            List<PointVenteDTO> listepointVentes = pointVenteRepository.findByUniteOrganisationIdOptimise(uoSource.getId());
            pvIds = listepointVentes.stream().map(pointVente -> pointVente.getId()).collect(Collectors.toList());
            // pvIds =  .stream().map(pointVente -> pointVente.getId()).collect(Collectors.toList());
        } else {
            pvIds = operationService.getPointVenteIdsOfUserConnecteOptimise(null);
        }
        if (!pvIds.isEmpty()) {
            if (dateOperation != null && dateFin != null) {
                return operationRepository.findAllOperationByMultiParamsOptimiser(typeServiceId, canalId, uoAcquereurId, pvIds, montantOp, statutOperation, dateOperation, dateFin.plusHours(24));

            } else if (dateOperation != null) {
                return operationRepository.findAllOperationByMultiParamsOptimiser(typeServiceId, canalId, uoAcquereurId, pvIds, montantOp, statutOperation, dateOperation, dateOperation.plusHours(23));

            }
            log.debug("point de vente" + pvIds);
            return operationRepository.findAllOperationByMultiParamsOptimiser(typeServiceId, canalId, uoAcquereurId, pvIds, montantOp, statutOperation, dateOperation, dateFin);
            //return  operationRepository.findAll();
        }
        return Collections.emptyList();
    }

    /**
     *
     */
    public List<OperationDTO> findByCTCByMultiParams(Long uoEmeteurId, ZonedDateTime dateOperation1, ZonedDateTime dateOperation2) {
        List<Operation> operations = new ArrayList<>();
        TypeService typeService = typeServiceService.findByCodeService(String.valueOf(TypeServiceLibelle.ENVOI_CASH));
        UniteOrganisation organisation = new UniteOrganisation();
        BigDecimal montantTotal = BigDecimal.ZERO;

        if (uoEmeteurId == null) {
            organisation = uniteOrganisationService.getUoOfUserConnecte();
        } else {
            organisation = uniteOrganisationRepository.findById(uoEmeteurId).orElse(null);
        }
        List<PointVente> listepointVentes = pointVenteRepository.findByUniteOrganisationId(organisation.getId());
        for (PointVente pv : listepointVentes) {
            operations.addAll(operationRepository.findOperationCTCByMultiParams(typeService.getId(), pv.getId(), dateOperation1, dateOperation2));
        }

        List<OperationDTO> operationDTOS = operationMapper.toDto(operations);
        for (OperationDTO operationDTO : operationDTOS) {
            montantTotal = montantTotal.add(operationDTO.getMontantOp());
        }
        for (OperationDTO operationDTO : operationDTOS) {
            operationDTO.setUoEmetteurLibelleUniteOrganisation(organisation.getLibelleUniteOrganisation());
            operationDTO.setTotal(montantTotal);
            if (dateOperation1 != null) {
                operationDTO.setDateDepart(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(dateOperation1.toLocalDate()));
            }
            if (dateOperation2 == null) {
                dateOperation2 = ZonedDateTime.now();
            }
            operationDTO.setDateArrive(DateTimeFormatter.ofPattern("dd/MM/yyyy").format(dateOperation2.toLocalDate()));
        }
        return operationDTOS;
    }

    public String formatDate(ZonedDateTime dateTime) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(dateTime);
    }

    public String formatDateOnly(ZonedDateTime dateTime) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(dateTime);
    }

    /**
     * Recherche des operations en fonction de la destination
     *
     * @param uoPaysId
     * @param typeServiceId
     * @return
     */
    public List<XOperationDTO> statsOfOperationsByCountry(Long uoSourceId, Long uoConnecteId, Long uoPaysId, Long typeServiceId, ZonedDateTime dateDebut, ZonedDateTime dateFin) {
        List<TypeService> typeServices = new ArrayList<>();
        if (typeServiceId == null) {
            typeServices = typeServiceRepository.findAll();
        } else {
            typeServices.add(typeServiceRepository.findById(typeServiceId).orElse(null));
        }
        List<XOperationDTO> operationDTOS = new ArrayList<>();
        UniteOrganisation uo = uniteOrganisationRepository.findById(uoPaysId).orElse(null);
        ArrayList<UniteOrganisation> listAll = new ArrayList<>();
        ArrayList<UniteOrganisation> listFils = new ArrayList<>();
        listAll.addAll(uniteOrganisationService.findAllUoFils(uo, listAll, listFils));
        listAll.add(uo);
        List<Long> uoIds = listAll.stream().map(uniteOrganisationDTO -> uniteOrganisationDTO.getId()).collect(Collectors.toList());
        //En fonction de tous les points de vente de l'utilisateur connecté
        //Les informations des points de vente de l'organisation
        for (TypeService typeService : typeServices) {
            List<Operation> operations = new ArrayList<>();
            for (Long uoId : uoIds) {
                operations.addAll(getAllOperations(uoSourceId, typeService.getId(), null, uoId, null, null, dateDebut, dateFin));
            }
            operationDTOS.addAll(getJasperOp(operations, uo, BigDecimal.ZERO));
        }
        return operationDTOS;
    }

    public List<XOperationDTO> statsOfOperationsByAgent(Long agentId, Long typeServiceId, ZonedDateTime dateDebut, ZonedDateTime dateFin) {
        List<TypeService> typeServices = new ArrayList<>();
        List<XOperationDTO> operationDTOS = new ArrayList<>();
        List<Operation> operations = new ArrayList<>();
        UniteOrganisation uo = uniteOrganisationRepository.findById(agentId).orElse(null);
        if (typeServiceId == null) {
            typeServices = typeServiceRepository.findAll();
        } else {
            typeServices.add(typeServiceRepository.findById(typeServiceId).orElse(null));
        }

        for (TypeService typeService : typeServices) {
            operations = operationRepository.findAllOperationByAgent(typeService.getId(), agentId, dateDebut, dateFin);
            operationDTOS.addAll(getJasperOp(operations, uo, BigDecimal.ZERO));
        }

        return operationDTOS;
    }

    /**
     * @param operations
     * @param uo
     * @return
     */
    public List<XOperationDTO> getJasperOp(List<Operation> operations, UniteOrganisation uo, BigDecimal totalOp) {
        String canal;
        List<XOperationDTO> operationDTOS = new ArrayList<>();
        for (Operation operation : operations) {
            TypeService typeService = operation.getTypeService();
            XOperationDTO xItem = new XOperationDTO();
            if (operation.getMotifOperation() != null) {
                xItem.setMotif(operation.getMotifOperation().getLibelle());
            }

            if (uo != null) {
                xItem.uoEmetteur(uo.getLibelleUniteOrganisation());
            }
            if (operation.getCanal() != null) {
                canal = operation.getCanal().getLibelleCanal();
            } else {
                canal = "WEB";
            }
            xItem.canal(canal)
                    .dateOp(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(operation.getDateOperation()))
                    .montant(operation.getMontantOp())
                    .typeService(operation.getTypeService().getLibelle())
                    .montantFrais(operation.getMontantFrais())
                    .setNumero(operation.getCodeOperation());
            xItem.setTotal(totalOp);

            if (operation.getOperationTraite() != null) {
                xItem.typeServiceTraite(operation.getOperationTraite().getTypeService().getLibelle());
            }

            //Informations de l'emetteur de l'operation
            if (operation.getEmetteur() != null) {
                Personne personne = operation.getEmetteur().getPersonne();
                if (personne.getPersonnePhysique() != null) {
                    PersonnePhysique personnePhysique = operation.getEmetteur().getPersonne().getPersonnePhysique();
                    if (personnePhysique.getPaysResidence() != null) {
                        xItem.setPaysOrigine(personnePhysique.getPaysResidence().getLibelle());
                    }
                    xItem.emetteur(personnePhysique.getNomClient().trim() + " " + personnePhysique.getPrenomClient().trim());
                } else if (operation.getEmetteur().getPersonne().getPersonneMorale() != null) {
                    xItem.emetteur(operation.getEmetteur().getPersonne().getPersonneMorale().getRaisonSociale());
                }
            }

            //Informations du beneficiaire de l'operation
            if (operation.getBeneficaire() != null) {
                if (operation.getBeneficaire().getPersonne().getPersonnePhysique() != null) {
                    PersonnePhysique personnePhysique = operation.getBeneficaire().getPersonne().getPersonnePhysique();
                    xItem.beneficiaire(personnePhysique.getNomClient().trim() + " " + personnePhysique.getPrenomClient().trim());
                    if (personnePhysique.getPaysResidence() != null) {
                        xItem.setPaysBeneficiaire(personnePhysique.getPaysResidence().getLibelle());
                    }
                } else if (operation.getBeneficaire().getPersonne().getPersonneMorale() != null) {
                    xItem.beneficiaire(operation.getBeneficaire().getPersonne().getPersonneMorale().getRaisonSociale());
                }
            }

            Compte compteDest = operation.getCompteDest();
            if (compteDest != null) {
                xItem.setNumeroCompteBeneficiaire(compteDest.getValeurCompte());
                if (compteDest.getCompteBanque() != null) {
                    if (compteDest.getCompteBanque().getCodeFse() != null) {
                        UniteOrganisation fse = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteDest.getCompteBanque().getCodeFse());
                        xItem.setNomBanqueBeneficiaire(fse.getLibelleUniteOrganisation());
                    }
                } else if (compteDest.getCompteAutre().getCodeFse() != null) {
                    UniteOrganisation fse = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteDest.getCompteAutre().getCodeFse());
                    xItem.setNomBanqueBeneficiaire(fse.getLibelleUniteOrganisation());
                }
            }

            if (operation.getUoEmetteur() != null) {
                xItem.uoEm(operation.getUoEmetteur().getLibelleUniteOrganisation());
            }
            if (operation.getUoAcquereur() != null) {
                xItem.uoAcq(operation.getUoAcquereur().getLibelleUniteOrganisation());
            }

            if (typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT))
                    || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT_INTERNE))) {
                if (operation.getUoAcquereur() != null) {
                    xItem.beneficiaire(operation.getUoAcquereur().getLibelleUniteOrganisation());
                }
            }

            operationDTOS.add(xItem);
        }
        return operationDTOS.stream().sorted(Comparator.comparing(XOperationDTO::getTypeService)).collect(Collectors.toList());
    }

    /**
     * Liste des commisssions en fonction de plusieurs critères
     *
     * @param uoId
     * @param dateDebut
     * @param dateFin
     * @param estPaye
     * @return
     */
    public List<XCommissionDTO> getAllCommisssion(Long uoId, ZonedDateTime dateDebut, ZonedDateTime dateFin, Boolean estPaye) {
        List<Commission> commissions = commissionRepository.findAllCommissionByMultiCriteria(estPaye, uoId, dateDebut, dateFin);
        List<XCommissionDTO> jasperCommissions = new ArrayList<>();
        for (Commission commission : commissions) {
            XCommissionDTO xItem = new XCommissionDTO();
            xItem.setDateOp(formatDate(commission.getDateEnregistrement()));
            xItem.setMontant(commission.getMontant());
            xItem.setOrganisation(commission.getOrganisation().getLibelleUniteOrganisation());
            xItem.setTypeService(commission.getOperation().getTypeService().getLibelle());
            jasperCommissions.add(xItem);
        }
        return jasperCommissions.stream().sorted(Comparator.comparing(XCommissionDTO::getTypeService)).collect(Collectors.toList());
    }

    /**
     * Traitement des informations du bordereau
     *
     * @param operation
     * @return
     */
    public Bordereau getOpBordereau(Operation operation) {
        Personne personneE=null;
        if (operation != null) {
            TypeService typeService = operation.getTypeService();
            Bordereau bordereau = new Bordereau();
            ParametreApp devise=parametreAppRepository.findByTypeAndNom(TypeParametre.SYSTEM,"DEVISE");
            if (!typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT)) && !typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT_INTERNE)) && !typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT_EXTERNE))) {
                //Beneficiaire
                       Personne personneB =null;
                if (operation.getBeneficaire()!=null){
                       personneB = operation.getBeneficaire().getPersonne();
                }
              
                if (personneB != null) {
                    if (personneB.getPersonnePhysique() != null) {
                        bordereau.beneficiaire(personneB.getPersonnePhysique().getNomClient().toUpperCase().trim() + " " + personneB.getPersonnePhysique().getPrenomClient().toUpperCase().trim());
                    } else if (personneB.getPersonneMorale() != null) {
                        bordereau.beneficiaire(personneB.getPersonneMorale().getRaisonSociale().trim());
                    }
                    if (personneB.getTelephone() != null) {
                        bordereau.telephoneB("Téléphone : " + personneB.getTelephone());
                    } else {
                        bordereau.telephoneB(null);
                    }

                    Piece piece = operation.getPieceBeneficiaire();
                    if (piece != null && piece.getReferencePiece() != null && piece.getTypePiece() != null) {
                        if (piece.getReferencePiece().length() > 3) {
                            String valeur = piece.getReferencePiece().
                                    replace(piece.getReferencePiece().
                                            substring(piece.getReferencePiece().length() - 3), "****");
                            bordereau.noPieceB("Pièce : " + piece.getTypePiece().getLibelleTypePiece() + "(" + valeur + ")");
                        } else {
                            bordereau.noPieceB("Pièce : " + piece.getTypePiece().getLibelleTypePiece() + "(********)");
                        }
                    }
                }

                //Expediteur
                if(operation.getEmetteur()!=null){
                    personneE = operation.getEmetteur().getPersonne();
                }
                if (personneE != null) {
                    if (personneE.getPersonnePhysique() != null) {
                        bordereau.expediteur(personneE.getPersonnePhysique().getNomClient().toUpperCase().trim() + " " + personneE.getPersonnePhysique().getPrenomClient().toUpperCase().trim());
                    }
                    if (personneE.getPersonneMorale() != null) {
                        bordereau.expediteur(personneE.getPersonneMorale().getRaisonSociale().trim());
                    }
                    bordereau.telephoneE("Téléphone : " + personneE.getTelephone());
                    List<Piece> pieces = pieceRepository.findByTitulaireId(personneE.getId());

                    Piece piece = operation.getPieceEmetteur();
                    if (piece != null && piece.getReferencePiece() != null && piece.getTypePiece() != null) {
                        bordereau.noPieceE("Pièce : " + piece.getTypePiece().getLibelleTypePiece() + "(" + piece.getReferencePiece() + ")");
                        if (piece.getReferencePiece().length() > 3) {
                            String valeur = piece.getReferencePiece().
                                    replace(piece.getReferencePiece().
                                            substring(piece.getReferencePiece().length() - 3), "****");
                            bordereau.noPieceE("Pièce : " + piece.getTypePiece().getLibelleTypePiece() + "(" + valeur + ")");
                        } else {
                            bordereau.noPieceE("Pièce : " + piece.getTypePiece().getLibelleTypePiece() + "(********)");
                        }
                    }
                }
            }
            // bordereau.setPointVente("POINT DE VENTE : " + operation.getPointVente().getLibellePointVente().toUpperCase());
            bordereau.setOrganisation(operation.getPointVente().getUniteOrganisation().getLibelleUniteOrganisation().toUpperCase() + "," + operation.getPointVente().getLibellePointVente().toUpperCase());
            bordereau.date("Date et heure : " + formatDate(operation.getDateOperation()));
            bordereau.setMontant(String.format("%,.0f", Util.getZeroIfNull(operation.getMontantOp()).doubleValue()));
            BigDecimal total = Util.getZeroIfNull(operation.getMontantFrais()).add(Util.getZeroIfNull(operation.getMontantTaxe()).add(Util.getZeroIfNull(operation.getMontantOp())));
            if(devise!=null && devise.getValeur()!=null){
                bordereau.setTotal("Franc "+devise.getValeur()+" " + String.format("%,.0f", total.doubleValue()));
            } else {
                bordereau.setTotal("Franc CFA " + String.format("%,.0f", total.doubleValue()));
            }
            bordereau.setFrais(String.format("%,.0f", Util.getZeroIfNull(operation.getMontantFrais()).doubleValue()));
            bordereau.setTaxe(String.format("%,.0f", Util.getZeroIfNull(operation.getMontantTaxe()).doubleValue()));
            bordereau.setCodeOp("Code opération : " + operation.getCodeOperation());
            if (operation.getCommentaire() != null) {
                bordereau.setCodeRetrait("Code retrait : " + new String(Base64.decodeBase64(operation.getCommentaire())));
            }
            UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(operation.getPointVente().getUniteOrganisation(), TypeUO.PAYS);
            UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(operation.getUoAcquereur(), TypeUO.PAYS);

            bordereau.pays("Pays d'origine : " + uoPays.getLibelleUniteOrganisation());
            bordereau.setPaysDestination("Pays de destination : " + uoPaysDest.getLibelleUniteOrganisation());
            bordereau.logo(new File(Constants.LOGO_URL).getAbsolutePath());
            if (operation.getCompteSrc() != null) {
                Compte compte = operation.getCompteSrc();
                if (compte.getCompteBanque() != null) {
                    CompteBanque compteBanque = compte.getCompteBanque();
                    UniteOrganisation fse = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteBanque.getCodeFse());
                    bordereau.setNoCompteE("No compte : " + operation.getCompteSrc().getValeurCompte() + "(" + fse.getLibelleUniteOrganisation() + ")");
                } else {
                    CompteAutre compteAutre = compte.getCompteAutre();
                    UniteOrganisation fse = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteAutre.getCodeFse());
                    bordereau.setNoCompteE("No compte : " + operation.getCompteSrc().getValeurCompte() + "(" + fse.getLibelleUniteOrganisation() + ")");
                }
            }

            if (operation.getCompteDest() != null) {
                Compte compte = operation.getCompteDest();
                if (compte.getCompteBanque() != null) {
                    CompteBanque compteBanque = compte.getCompteBanque();
                    UniteOrganisation fse = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteBanque.getCodeFse());
                    bordereau.setNoCompteB("No compte : " + operation.getCompteDest().getValeurCompte() + "(" + fse.getLibelleUniteOrganisation() + ")");
                } else {
                    CompteAutre compteAutre = compte.getCompteAutre();
                    UniteOrganisation fse = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(compteAutre.getCodeFse());
                    bordereau.setNoCompteB("No compte : " + operation.getCompteDest().getValeurCompte() + "(" + fse.getLibelleUniteOrganisation() + ")");
                }
            }

            switch (TypeServiceLibelle.valueOf(typeService.getLibelle())) {
                case ENVOI_CASH:
                    bordereau.setTitre("Bordereau d'envoi de cash " + getCopie(operation));
                    break;
                case RETRAIT:
                    bordereau.setTitre("Bordereau de retrait " + getCopie(operation));
                    break;
                case ACCOUNT_CASH:
                    bordereau.setTitre("Bordereau d'appel de fond" + getCopie(operation));
                    break;
                case APPROVISIONNEMENT:
                    bordereau.setTitre("Bordereau d'approvisionnement " + getCopie(operation));
                    bordereau.setBeneficiaire(operation.getUoAcquereur().getLibelleUniteOrganisation());
                    UniteOrganisation uoPaysAcq = uniteOrganisationService.findUoParentByType(operation.getUoAcquereur(), TypeUO.PAYS);
                    bordereau.setPaysOrganisationB("Pays: " + uoPaysAcq.getLibelleUniteOrganisation());
                    break;
                case CASH_ACCOUNT:
                    bordereau.setTitre("Bordereau de depôt" + getCopie(operation));
                    break;
                case RETRAIT_COMPTE:
                    bordereau.setTitre("Retrait depuis le compte" + getCopie(operation));
                    break;
                  case PAIEMENT_FACTURE:
                    bordereau.setTitre("Paiement de facture " + getCopie(operation));
                    break;
                  case PAIEMENT_FACTURE_DEPUIS_CASH:
                    bordereau.setTitre("Paiement de facture depuis cash" + getCopie(operation));
                    break;
                case ACCOUNT_ACCOUNT:
                    bordereau.setTitre("Bordereau de virement" + getCopie(operation));
                    break;
                case APPROVISIONNEMENT_INTERNE:
                case   APPROVISIONNEMENT_EXTERNE:
                    bordereau.setTitre("Bordereau d'approvisionnement " + getCopie(operation));
                    bordereau.setBeneficiaire(operation.getUoAcquereur().getLibelleUniteOrganisation());
                    UniteOrganisation uoApp = uniteOrganisationService.findUoParentByType(operation.getUoAcquereur(), TypeUO.PAYS);
                    bordereau.setPaysOrganisationB("Pays: " + uoApp.getLibelleUniteOrganisation());
                    break;
                default:
            }
            return bordereau;
        }
        return null;
    }

    public ModelAndView getOperationList(ApplicationContext appContext, XOperation xOperation) {
        //JasperReportsPdfView view = new JasperReportsPdfView();
        String typeService = null, dateDebut = null, dateFin = null, statutOperation = null, organisation = null, acquereur = null;
        if (xOperation.getTypeServiceId() != null) {
            typeService = "TYPE DE SERVICE " + typeServiceRepository.getOne(xOperation.getTypeServiceId()).getCode();
        }
        if (xOperation.getDateFin() != null) {
            dateFin = "DATE FIN " + formatDateOnly(xOperation.getDateFin());
        }
        if (xOperation.getDateOperation() != null) {
            dateDebut = "DATE DEBUT " + formatDateOnly(xOperation.getDateOperation());
        }
        if (xOperation.getStatutOperation() != null) {
            statutOperation = "STATUT " + xOperation.getStatutOperation().toString();
        }
        if (xOperation.getOrganisationId() != null) {
            UniteOrganisation org = uniteOrganisationRepository.findById(xOperation.getOrganisationId()).orElse(null);
            if (org != null) {
                organisation = "ORGANISATION " + org.getLibelleUniteOrganisation();
            }
        }
        if (xOperation.getUoAcquereurId() != null) {
            UniteOrganisation org = uniteOrganisationRepository.findById(xOperation.getUoAcquereurId()).orElse(null);
            if (org != null) {
                acquereur = "ACQUEREUR " + org.getLibelleUniteOrganisation();
            }
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(xOperation.getFormat()));
        Map<String, Object> params = new HashMap<>();
        List<StatutOperation> statut = null;
        List<com.m2i.pti.service.dto.XOperationDTO> xOperations = new ArrayList<>();
        List<Long> pointVenteIds = operationService.getPointVenteIdsOfUserConnecte(xOperation.getOrganisationId());
        List<Operation> operations = new ArrayList<>();
        if (!pointVenteIds.isEmpty()) {

            if (xOperation.getStatutOperation() != null) {
                statut = new ArrayList();
                statut.add(xOperation.getStatutOperation());

            }
            if (xOperation.getDateOperation() != null && xOperation.getDateFin() != null) {

                operations.addAll(operationRepository.findOperationByMultiParams(xOperation.getTypeServiceId(), xOperation.getCanalId(), xOperation.getUoAcquereurId(), pointVenteIds, xOperation.getMontantOp(), statut, xOperation.getDateOperation(), xOperation.getDateFin().plusHours(23), xOperation.getCodeOperation(), null).getContent());
            } else if (xOperation.getDateOperation() != null) {
                operations.addAll(operationRepository.findOperationByMultiParams(xOperation.getTypeServiceId(), xOperation.getCanalId(), xOperation.getUoAcquereurId(), pointVenteIds, xOperation.getMontantOp(), statut, xOperation.getDateOperation(), xOperation.getDateOperation().plusHours(23), xOperation.getCodeOperation(), null).getContent());
            } else if (xOperation.getDateFin() != null) {
                operations.addAll(operationRepository.findOperationByMultiParams(xOperation.getTypeServiceId(), xOperation.getCanalId(), xOperation.getUoAcquereurId(), pointVenteIds, xOperation.getMontantOp(), statut, ZonedDateTime.now().minusMonths(12), xOperation.getDateFin().plusHours(23), xOperation.getCodeOperation(), null).getContent());
            }
            operations.addAll(operationRepository.findOperationByMultiParams(xOperation.getTypeServiceId(), xOperation.getCanalId(), xOperation.getUoAcquereurId(), pointVenteIds, xOperation.getMontantOp(), statut, xOperation.getDateOperation(), xOperation.getDateFin(), xOperation.getCodeOperation(), null).getContent());
        }
        xOperations = operationService.getJasperOp(operations);
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(xOperation.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(xOperation.getFormat()))) {
            view.setUrl(OPERAION_LISTE_XLSX);

        } else {
            view.setUrl(OPERAION_LISTE_PDF);
        }
        view.setApplicationContext(appContext);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(xOperations, false);
        String logo = new File(Constants.LOGO_URL).getAbsolutePath();
        params.put("logo", logo);
        params.put("pStatutOperation", statutOperation);
        params.put("pTypeService", typeService);
        params.put("pDateDebut", dateDebut);
        params.put("pDateFin", dateFin);
        params.put("pOrganisation", organisation);
        params.put("pAcquereur", acquereur);
        params.put(PARAM_DATASOURCE, dataSource);
        log.debug("org------------" + organisation + "oooooooo" + xOperation.getOrganisationId());
        log.debug("org------------" + acquereur + "acquerr" + xOperation.getUoAcquereurId());
        return new ModelAndView(view, params);
    }

    private String getCopie(Operation op) {
        if (op != null) {
            log.info(op.getDateOperation() + "    " + ZonedDateTime.now());
            if (ZonedDateTime.now().isAfter(op.getDateOperation().plusMinutes(10))) {
                return " (copie)";
            }
        }
        return "";
    }

    public ModelAndView getOperationListOptimise(ApplicationContext appContext, XOperation xOperation) {
        String typeService = null, dateDebut = null, dateFin = null, statutOperation = null, organisation = null, acquereur = null;

        if (xOperation.getTypeServiceId() != null) {
            typeService = "TYPE DE SERVICE " + typeServiceRepository.findOneOptimise(xOperation.getTypeServiceId()).getCode();
        }
        if (xOperation.getDateFin() != null) {
            dateFin = "DATE FIN " + formatDateOnly(xOperation.getDateFin());
        }
        if (xOperation.getDateOperation() != null) {
            dateDebut = "DATE DEBUT " + formatDateOnly(xOperation.getDateOperation());
        }
        if (xOperation.getStatutOperation() != null) {
            statutOperation = "STATUT " + xOperation.getStatutOperation().toString();
        }
        if (xOperation.getOrganisationId() != null) {
            String org = uniteOrganisationRepository.getLibelleUniteOrganisation(xOperation.getOrganisationId());
            if (org != null) {
                organisation = "ORGANISATION " + org;
            }
        }
        if (xOperation.getUoAcquereurId() != null) {
            String org = uniteOrganisationRepository.getLibelleUniteOrganisation(xOperation.getUoAcquereurId());
            if (org != null) {
                acquereur = "ACQUEREUR " + org;
            }
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(xOperation.getFormat()));
        Map<String, Object> params = new HashMap<>();
        List<String> statut = null;
        List<StatutOperation> statutCount = null;
        List<com.m2i.pti.service.dto.XOperationDTO> xOperations = new ArrayList<>();
        List<Long> pointVenteIds = operationService.getPointVenteIdsOfUserConnecteOptimise(xOperation.getOrganisationId());
        List<OperationDTO> operations = new ArrayList<>();
        if (!pointVenteIds.isEmpty()) {
            int max;
            int nbrPage;
            if (xOperation.getStatutOperation() != null) {
                statut = new ArrayList();
                statutCount = new ArrayList();
                statut.add(xOperation.getStatutOperation().name());
                statutCount.add(xOperation.getStatutOperation());
            } else {
                StatutOperation[] allStatut = StatutOperation.values();
                statut = new ArrayList();
                statutCount = new ArrayList();
                for (StatutOperation statutAll : allStatut) {
                    statutCount.add(statutAll);
                    statut.add(statutAll.name());
                }
            }

            if (xOperation.getDateOperation() != null && xOperation.getDateFin() != null) {
                max = operationRepository.countByMultiParamsOptimise(xOperation.getTypeServiceId(), xOperation.getCanalId(), xOperation.getUoAcquereurId(), pointVenteIds, xOperation.getMontantOp(), statutCount, xOperation.getDateOperation(), xOperation.getDateFin().plusHours(23), xOperation.getCodeOperation(),xOperation.getDescription());
            } else if (xOperation.getDateOperation() != null) {
                max = operationRepository.countByMultiParamsOptimise(xOperation.getTypeServiceId(), xOperation.getCanalId(), xOperation.getUoAcquereurId(), pointVenteIds, xOperation.getMontantOp(), statutCount, xOperation.getDateOperation(), xOperation.getDateOperation().plusHours(23), xOperation.getCodeOperation(),xOperation.getDescription());
            } else if (xOperation.getDateFin() != null) {
                max = operationRepository.countByMultiParamsOptimise(xOperation.getTypeServiceId(), xOperation.getCanalId(), xOperation.getUoAcquereurId(), pointVenteIds, xOperation.getMontantOp(), statutCount, ZonedDateTime.now().minusMonths(12), xOperation.getDateFin().plusHours(23), xOperation.getCodeOperation(),xOperation.getDescription());
            } else {
                max = operationRepository.countByMultiParamsOptimise(xOperation.getTypeServiceId(), xOperation.getCanalId(), xOperation.getUoAcquereurId(), pointVenteIds, xOperation.getMontantOp(), statutCount, xOperation.getDateOperation(), xOperation.getDateFin(), xOperation.getCodeOperation(),xOperation.getDescription());
            }
            nbrPage = (max / 200);
            for (int i = 0; i <= nbrPage; i++) {
                Pageable page = PageRequest.of(i, 200, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
                Long taille = new Long(page.getPageSize());
                Long pag = new Long(page.getPageSize() * page.getPageNumber());

                if (xOperation.getDateOperation() != null && xOperation.getDateFin() != null) {
                    operations.addAll(operationRepository.findOperationByMultiParamsOptimise(xOperation.getTypeServiceId(), xOperation.getCanalId(), xOperation.getUoAcquereurId(), pointVenteIds, xOperation.getMontantOp(), statut, xOperation.getCodeOperation(),xOperation.getDescription(), xOperation.getDateOperation(), xOperation.getDateFin().plusHours(23), taille, pag));
                } else if (xOperation.getDateOperation() != null) {
                    operations.addAll(operationRepository.findOperationByMultiParamsOptimise(xOperation.getTypeServiceId(), xOperation.getCanalId(), xOperation.getUoAcquereurId(), pointVenteIds, xOperation.getMontantOp(), statut, xOperation.getCodeOperation(),xOperation.getDescription(), xOperation.getDateOperation(), xOperation.getDateOperation().plusHours(23), taille, pag));
                } else if (xOperation.getDateFin() != null) {
                    operations.addAll(operationRepository.findOperationByMultiParamsOptimise(xOperation.getTypeServiceId(), xOperation.getCanalId(), xOperation.getUoAcquereurId(), pointVenteIds, xOperation.getMontantOp(), statut, xOperation.getCodeOperation(),xOperation.getDescription(), ZonedDateTime.now().minusMonths(12), xOperation.getDateFin().plusHours(23), taille, pag));
                } else {
                    operations.addAll(operationRepository.findOperationByMultiParamsOptimise(xOperation.getTypeServiceId(), xOperation.getCanalId(), xOperation.getUoAcquereurId(), pointVenteIds, xOperation.getMontantOp(), statut, xOperation.getCodeOperation(),xOperation.getDescription(), xOperation.getDateOperation(), xOperation.getDateFin(), taille, pag));

                }

            }
        }
        xOperations = operationService.getJasperOpOptimise(operations);
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(xOperation.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(xOperation.getFormat()))) {
            view.setUrl(OPERAION_LISTE_XLSX);
        } else {
            view.setUrl(OPERAION_LISTE_PDF);
        }
        view.setApplicationContext(appContext);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(xOperations, false);
        String logo = new File(Constants.LOGO_URL).getAbsolutePath();
        params.put("logo", logo);
        params.put("pStatutOperation", statutOperation);
        params.put("pTypeService", typeService);
        params.put("pDateDebut", dateDebut);
        params.put("pDateFin", dateFin);
        params.put("pOrganisation", organisation);
        params.put("pAcquereur", acquereur);
        params.put(PARAM_DATASOURCE, dataSource);
        log.debug("org------------" + organisation + "oooooooo" + xOperation.getOrganisationId());
        log.debug("org------------" + acquereur + "acquerr" + xOperation.getUoAcquereurId());
        return new ModelAndView(view, params);
    }

}
