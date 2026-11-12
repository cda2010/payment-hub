package com.m2i.pti.reporting.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.mobile.dto.MParametre;
import com.m2i.pti.reporting.ReportFormat;
import com.m2i.pti.reporting.ReportUtils;
import com.m2i.pti.reporting.dto.*;
import com.m2i.pti.reporting.util.Constants;
import com.m2i.pti.reporting.util.UtilService;
import com.m2i.pti.repository.*;
import com.m2i.pti.service.*;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.mapper.ClientMapper;
import com.m2i.pti.service.mapper.CompteMapper;
import com.m2i.pti.service.mapper.TransactionComptePtiMapper;
import com.m2i.pti.service.mapper.UniteOrganisationMapper;
import org.apache.commons.lang3.SystemUtils;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.jasperreports.AbstractJasperReportsView;
import org.springframework.web.servlet.view.jasperreports.JasperReportsPdfView;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EtatStatsService {

    private final Logger log = LoggerFactory.getLogger(EtatStatsService.class);
    private static final String PARAM_DATASOURCE = "datasource";
    @Value("${m2i.security.pti.qrCode.windows}")
    private String qrCodePathWindows;

    @Value("${m2i.security.pti.qrCode.linux}")
    private String qrCodePathLinux;
    private String pathname;
    private final TransactionComptePtiRepository transactionComptePtiRepository;
    private final TransactionComptePtiMapper transactionComptePtiMapper;
    private final UniteOrganisationMapper uniteOrganisationMapper;
    private final BlackListRepository blackListRepository;
    private final CompteRepository compteRepository;
    public final CompteService compteService;
    private final ClientRepository clientRepository;
    private final LigneChargementTransfertService ligneChargementTransfertService;
    private final LigneChargementCompteService ligneChargementCompteService;
    private final EtiquetteCompteRepository etiquetteCompteRepository;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    private final OperationRepository operationRepository;
    private final ClientService clientService;
    private final UniteOrganisationService uniteOrganisationService;
    private final CryptageCodeQRService cryptageCodeQRService;
    private final PointVenteService pointVenteService;
    private final FacturierService facturierService;
    private final FactureRepository factureRepository;
    private final ChargementTransfertService chargementTransfertService;
    private final ChargementCompteService chargementCompteService;
    private final ReglementService reglementService;
    private final LigneReglementRepository ligneReglementRepository;
    private final CompteAutreRepository compteAutreRepository;
    private final CompteMapper compteMapper;
    private final UserRepository userRepository;
    private final HistoriqueRepository historiqueRepository;
    private final DetailHistoriqueRepository detailHistoriqueRepository;
    private final ClientMapper clientMapper;
    private final EntityAuditEventRepository entityAuditEventRepository;
    private final ProfilRepository profilRepository;
    private final ProfilService profilService;
    private final DetailsLotRepository detailsLotRepository;
    private final PersonneRepository personneRepository;
    private final PersonnePhysiqueRepository personnePhysiqueRepository;
    private final ProfilActeurRepository profilActeurRepository;
    private final GroupeProfilActeurRepository groupeProfilActeurRepository;
    private final NationaliteRepository nationaliteRepository;
    private final PointVenteRepository pointVenteRepository;
    private final BeneficiaireRepository beneficiaireRepository;
    private final DetailsLotService detailsLotService;
    private final ResourceLoader resourceLoader;
    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;
    private final TypeServiceRepository typeServiceRepository;
    private final VilleRepository villeRepository;
    private final CommissionRepository commissionRepository;
    private final PersonneMoraleRepository personneMoraleRepository;
    private final CompteBanqueRepository compteBanqueRepository;
    private final OperationSuivisRepository operationSuivisRepository;
    private final PieceRepository pieceRepository;
    private final LigneChargementTransfertRepository ligneChargementTransfertRepository;
    private final UserControlRepository userControlRepository;
    private final ActionEventRepository actionEventRepository;
    private final ChargementFichierRepository chargementFichierRepository;
    private final LigneChargementFichierRepository ligneChargementFichierRepository;

    public EtatStatsService(TransactionComptePtiRepository transactionComptePtiRepository, TransactionComptePtiMapper transactionComptePtiMapper, UniteOrganisationMapper uniteOrganisationMapper, BlackListRepository blackListRepository, CompteRepository compteRepository, CompteService compteService, ClientRepository clientRepository, LigneChargementTransfertService ligneChargementTransfertService, LigneChargementCompteService ligneChargementCompteService, EtiquetteCompteRepository etiquetteCompteRepository, UniteOrganisationRepository uniteOrganisationRepository, OperationRepository operationRepository, ClientService clientService, UniteOrganisationService uniteOrganisationService, CryptageCodeQRService cryptageCodeQRService, PointVenteService pointVenteService, FacturierService facturierService, FactureRepository factureRepository, ChargementTransfertService chargementTransfertService, ChargementCompteService chargementCompteService, ReglementService reglementService, LigneReglementRepository ligneReglementRepository, CompteAutreRepository compteAutreRepository, CompteMapper compteMapper, UserRepository userRepository, HistoriqueRepository historiqueRepository, DetailHistoriqueRepository detailHistoriqueRepository, ClientMapper clientMapper, EntityAuditEventRepository entityAuditEventRepository, ProfilRepository profilRepository, ProfilService profilService, DetailsLotRepository detailsLotRepository, PersonneRepository personneRepository, PersonnePhysiqueRepository personnePhysiqueRepository, ProfilActeurRepository profilActeurRepository, GroupeProfilActeurRepository groupeProfilActeurRepository, NationaliteRepository nationaliteRepository, PointVenteRepository pointVenteRepository, BeneficiaireRepository beneficiaireRepository, DetailsLotService detailsLotService, ResourceLoader resourceLoader, TypeUniteOrganisationRepository typeUniteOrganisationRepository, TypeServiceRepository typeServiceRepository, VilleRepository villeRepository, CommissionRepository commissionRepository, PersonneMoraleRepository personneMoraleRepository, CompteBanqueRepository compteBanqueRepository, OperationSuivisRepository operationSuivisRepository, PieceRepository pieceRepository, LigneChargementTransfertRepository ligneChargementTransfertRepository, UserControlRepository userControlRepository, ActionEventRepository actionEventRepository, ChargementFichierRepository chargementFichierRepository, LigneChargementFichierRepository ligneChargementFichierRepository) {
        this.transactionComptePtiRepository = transactionComptePtiRepository;
        this.transactionComptePtiMapper = transactionComptePtiMapper;
        this.uniteOrganisationMapper = uniteOrganisationMapper;
        this.blackListRepository = blackListRepository;
        this.compteRepository = compteRepository;
        this.compteService = compteService;
        this.clientRepository = clientRepository;
        this.ligneChargementTransfertService = ligneChargementTransfertService;
        this.ligneChargementCompteService = ligneChargementCompteService;
        this.etiquetteCompteRepository = etiquetteCompteRepository;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.operationRepository = operationRepository;
        this.clientService = clientService;
        this.uniteOrganisationService = uniteOrganisationService;
        this.cryptageCodeQRService = cryptageCodeQRService;
        this.pointVenteService = pointVenteService;
        this.facturierService = facturierService;
        this.factureRepository = factureRepository;
        this.chargementTransfertService = chargementTransfertService;
        this.chargementCompteService = chargementCompteService;
        this.reglementService = reglementService;
        this.ligneReglementRepository = ligneReglementRepository;
        this.compteAutreRepository = compteAutreRepository;
        this.compteMapper = compteMapper;
        this.userRepository = userRepository;
        this.historiqueRepository = historiqueRepository;
        this.detailHistoriqueRepository = detailHistoriqueRepository;
        this.clientMapper = clientMapper;
        this.entityAuditEventRepository = entityAuditEventRepository;
        this.profilRepository = profilRepository;
        this.profilService = profilService;
        this.detailsLotRepository = detailsLotRepository;
        this.personneRepository = personneRepository;
        this.personnePhysiqueRepository = personnePhysiqueRepository;
        this.profilActeurRepository = profilActeurRepository;
        this.groupeProfilActeurRepository = groupeProfilActeurRepository;
        this.nationaliteRepository = nationaliteRepository;
        this.pointVenteRepository = pointVenteRepository;
        this.beneficiaireRepository = beneficiaireRepository;
        this.detailsLotService = detailsLotService;
        this.resourceLoader = resourceLoader;
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.typeServiceRepository = typeServiceRepository;
        this.villeRepository = villeRepository;
        this.commissionRepository = commissionRepository;
        this.personneMoraleRepository = personneMoraleRepository;
        this.compteBanqueRepository = compteBanqueRepository;
        this.operationSuivisRepository = operationSuivisRepository;
        this.pieceRepository = pieceRepository;
        this.ligneChargementTransfertRepository = ligneChargementTransfertRepository;
        this.userControlRepository = userControlRepository;
        this.actionEventRepository = actionEventRepository;
        this.chargementFichierRepository = chargementFichierRepository;
        this.ligneChargementFichierRepository = ligneChargementFichierRepository;
    }

    @Transactional(readOnly = true)
    public List<UniteOrganisationDTO> findByPereUoId(String libelleUniteOrganisation, String codeUO, Integer numeroOrdre, String typeUO, Long pereUoId, Long villeId, Long groupeId, String adresse) {
        List<UniteOrganisationDTO> list = uniteOrganisationRepository.findForViewListOptimise();
        List<UniteOrganisationDTO> fils = new ArrayList<>();
        List<UniteOrganisationDTO> result = new ArrayList<UniteOrganisationDTO>();
        list.forEach(uniteOrganisation -> {

            if (uniteOrganisation.getPereUOId() != null && uniteOrganisation.getPereUOId().equals(pereUoId)) {

                fils.add(uniteOrganisation);
                if (((libelleUniteOrganisation == null || libelleUniteOrganisation.equals("")) || (uniteOrganisation.getLibelleUniteOrganisation() != null && uniteOrganisation.getLibelleUniteOrganisation().toUpperCase().contains(libelleUniteOrganisation.toUpperCase())))
                        && ((typeUO == null || typeUO.equals("")) || (uniteOrganisation.getTypeUOId() != null && uniteOrganisation.getTypeUOCode().equalsIgnoreCase(typeUO)))
                        && ((codeUO == null || codeUO.equals("")) || (uniteOrganisation.getCodeUO() != null && uniteOrganisation.getCodeUO().toUpperCase().contains(codeUO.toUpperCase())))
                        && ((numeroOrdre == null) || (uniteOrganisation.getNumeroOrdre() != null && uniteOrganisation.getNumeroOrdre().equals(numeroOrdre)))
                        && ((groupeId == null) || (uniteOrganisation.getGroupeProfilActeurId() != null && uniteOrganisation.getGroupeProfilActeurId().equals(groupeId)))
                        && ((villeId == null) || (uniteOrganisation.getVilleId() != null && uniteOrganisation.getVilleId().equals(villeId)))
                        && ((adresse == null || adresse.equals("")) || (uniteOrganisation.getAdresse() != null && uniteOrganisation.getAdresse().toUpperCase().contains(adresse.toUpperCase())))) {
                    result.add(uniteOrganisation);
                }
            }
        });
        while (!fils.isEmpty()) {
            List<UniteOrganisationDTO> oldFils = new ArrayList<>(fils);
            fils.clear();
            for (UniteOrganisationDTO pere : oldFils) {
                list.forEach(uo -> {
                    if (uo.getPereUOId() != null && uo.getPereUOId().equals(pere.getId())) {
                        fils.add(uo);
                        if (((libelleUniteOrganisation == null || libelleUniteOrganisation.equals("")) || (uo.getLibelleUniteOrganisation() != null && uo.getLibelleUniteOrganisation().toUpperCase().contains(libelleUniteOrganisation.toUpperCase())))
                                && ((typeUO == null || typeUO.equals("")) || (uo.getTypeUOId() != null && uo.getTypeUOCode().equalsIgnoreCase(typeUO)))
                                && ((codeUO == null || codeUO.equals("")) || (uo.getCodeUO() != null && uo.getCodeUO().toUpperCase().contains(codeUO.toUpperCase())))
                                && ((numeroOrdre == null) || (uo.getNumeroOrdre() != null && uo.getNumeroOrdre().equals(numeroOrdre)))
                                && ((groupeId == null) || (uo.getGroupeProfilActeurId() != null && uo.getGroupeProfilActeurId().equals(groupeId)))
                                && ((villeId == null) || (uo.getVilleId() != null && uo.getVilleId().equals(villeId)))
                                && ((adresse == null || adresse.equals("")) || (uo.getAdresse() != null && uo.getAdresse().toUpperCase().contains(adresse.toUpperCase())))) {
                            result.add(uo);
                        }
                    }
                });
            }
        }
        return result;
    }

    public ModelAndView getTrans(ApplicationContext context, ZonedDateTime debut, ZonedDateTime fin, String idClient, String format) {
        // JasperReportsPdfView view = new JasperReportsPdfView();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        XTransaction xTransaction = new XTransaction();
        ListTransDTO listTransDTO = new ListTransDTO();
        List<ListTransDTO> Xligne = new ArrayList<>();
        Client client = new Client();
        Compte cptClient = null;
        List<Client> idClients = new ArrayList<>();
        List<Client> cli = new ArrayList<>();
        String nom = null, tel = null;
        List<TransactionComptePti> transactionComptePti = new ArrayList<>();
        idClients = blackListRepository.findByTel(idClient);
        Long idClit = null;
        if (idClients.size() > 0) {
            client = idClients.get(0);
            for (Client idC : idClients) {
                idClit = idC.getId();
            }
            if (client.getPersonne() != null) {
                tel = client.getPersonne().getTelephone();
                if (client.getPersonne().getPersonnePhysique() != null) {
                    nom = client.getPersonne().getPersonnePhysique().getNomClient() + " " + client.getPersonne().getPersonnePhysique().getPrenomClient();
                }
                if (client.getPersonne().getPersonneMorale() != null) {
                    nom = client.getPersonne().getPersonneMorale().getRaisonSociale() + " " + client.getPersonne().getPersonneMorale().getNumreoIfu();

                }
            }
            cptClient = compteService.findComptePrincipal(client);
        }
        log.debug("cli***************" + client);
        cli = transactionComptePtiRepository.findTrans(idClit, debut, fin);
        if (cptClient != null) {
            log.debug("*-------------------*-" + cptClient);
            log.debug("*-------------------*-" + cptClient.getCompteAutre());
            transactionComptePti = transactionComptePtiRepository.findByCompteAutreIdAndDateBetweenOrderByIdDesc(cptClient.getCompteAutre().getId(), debut, fin.plusHours(23));
            //transactionComptePti = transactionComptePtiRepository.findByCompteAutreAndDateBetweenOrderByIdDesc(cptClient.getCompteAutre(),debut,fin.plusHours(23));
            listTransDTO.setNumeroCompte(cptClient.getValeurCompte());
        }

        listTransDTO.setIntitulle(nom);
        listTransDTO.setNumeroTel(tel);
        Xligne.add(listTransDTO);
        BigDecimal somme = new BigDecimal("0");
        BigDecimal sommeD = new BigDecimal("0");
        BigDecimal sommeC = new BigDecimal("0");
        for (TransactionComptePti t : transactionComptePti) {
            somme = somme.add(t.getMontant());

            System.out.println("--------------------" + t.getId());
        }

        sommeD = transactionComptePtiRepository.findSommeByCli(idClit, debut, fin, TypeTransactionCompte.DEBIT);
        sommeC = transactionComptePtiRepository.findSommeByCli(idClit, debut, fin, TypeTransactionCompte.CREDIT);
        listTransDTO.setSommeDebit(sommeD);
        listTransDTO.setSommeCredit(sommeC);

        listTransDTO.setNbrTransaction(transactionComptePti.size());
        String titre = getTitreWithDate(debut, fin);
        xTransaction.setTitre(titre);
        xTransaction.setListTransaction(Xligne);
        xTransaction.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl("classpath:/report/Etat_nbr_trans_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/Etat_nbr_trans_pdf.jasper");
        }
        view.setApplicationContext(context);

        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xTransaction), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getBalanceCli(ApplicationContext context) {
        JasperReportsPdfView view = new JasperReportsPdfView();
        Map<String, Object> params = new HashMap<>();
        List<Compte> listCompte = new ArrayList<>();
        List<String> d = new ArrayList<>();
        List<BalanceCptClient> liste = new ArrayList<>();
        XBalanceCompte xBalanceCompte = new XBalanceCompte();
        BalanceCptClient bc = new BalanceCptClient();
        List<TransactionComptePti> listCpt = new ArrayList<>();
        listCompte = transactionComptePtiRepository.findAllCompteClient();
        BigDecimal totalD = new BigDecimal("0");
        BigDecimal totalC = new BigDecimal("0");
        for (Compte cpt : listCompte) {
            bc.setNumeroCpt(cpt.getValeurCompte());
            BigDecimal totalDebit = new BigDecimal("0");
            BigDecimal totalCredit = new BigDecimal("0");
            BigDecimal solde = new BigDecimal("0");
            String lib = null;
            String nom = null, prenom = null;
            totalDebit = transactionComptePtiRepository.findTotal(cpt.getValeurCompte(), TypeTransactionCompte.DEBIT);
            totalCredit = transactionComptePtiRepository.findTotal(cpt.getValeurCompte(), TypeTransactionCompte.CREDIT);

            bc.setTotalDebit(totalDebit);
            bc.setTotalCredit(totalCredit);
            if (cpt.getClient().getPersonne().getPersonnePhysique() != null) {
                bc.setIntitulle(cpt.getClient().getPersonne().getPersonnePhysique().getNomClient() + " " + cpt.getClient().getPersonne().getPersonnePhysique().getPrenomClient());

            } else if (cpt.getClient().getPersonne().getPersonneMorale() != null) {
                bc.setIntitulle(cpt.getClient().getPersonne().getPersonneMorale().getRaisonSociale());

            }
            totalD = totalD.add(totalDebit);
            totalC = totalC.add(totalCredit);
            liste.add(bc);
            bc = new BalanceCptClient();

        }
        xBalanceCompte.setSommeDebit(totalD);
        xBalanceCompte.setSommeCredit(totalC);
        xBalanceCompte.setTitre("CLIENTS");
        xBalanceCompte.setBalanceCptClients(liste);
        view.setUrl("classpath:/report/balance_solde_client.jasper");
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xBalanceCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);

    }
    // solde=totalCredit.subtract(totalDebit);

    public ModelAndView getBalanceAgent(ApplicationContext context, String type, Long fse, String format) {
        //JasperReportsPdfView view = new JasperReportsPdfView();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        BalanceCptClient ba = new BalanceCptClient();
        List<BalanceCptClient> listeA = new ArrayList<>();
        XBalanceCompte xBalanceCompteA = new XBalanceCompte();
        List<TransactionComptePti> listCptAgt = new ArrayList<>();
        List<UniteOrganisationDTO> test = new ArrayList<>();
        List<Compte> agentAll = new ArrayList<>();
        List<Compte> agent = new ArrayList<>();
        Long idFSE = uniteOrganisationRepository.findFSE();
        String nomFSE = uniteOrganisationRepository.findNomFSE(fse);
//            //agent=compteRepository.findCompteAgentAndFse(type,idFSE,fse);
//            test=uniteOrganisationRepository.findUOrganisation();
        test = findByPereUoId(null, null, null, type, fse, null, null, null);
        // agent=compteRepository.findCompteAgentAndFse(type,fse);
        //agent= compteRepository.findCompteAgent(type);
        for (UniteOrganisationDTO uo : test) {
            agent = compteRepository.findByUO(uo.getId());
            agentAll.addAll(agent);
        }
        log.debug("--------agent---" + agentAll);
        BigDecimal totalD = new BigDecimal("0");
        BigDecimal totalC = new BigDecimal("0");
        BigDecimal totalSD = new BigDecimal("0");
        BigDecimal totalSC = new BigDecimal("0");
        ba.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        ba.setFmt(format);
        for (Compte cpt : agentAll) {
            ba.setNumeroCpt(cpt.getValeurCompte());
            BigDecimal totalACredit = new BigDecimal("0");
            BigDecimal totalADebit = new BigDecimal("0");
            String libA = null;
            totalADebit = transactionComptePtiRepository.findTotalA(cpt.getValeurCompte(), TypeTransactionCompte.DEBIT);
            totalACredit = transactionComptePtiRepository.findTotalA(cpt.getValeurCompte(), TypeTransactionCompte.CREDIT);
            totalADebit = totalADebit.setScale(0, RoundingMode.HALF_UP);
            totalACredit = totalACredit.setScale(0, RoundingMode.HALF_UP);
            String etiquette = etiquetteCompteRepository.finByEtiquette(cpt.getEtiquette());
            ba.setTotalDebit(totalADebit);
            ba.setTotalCredit(totalACredit);
            ba.setLibelle(cpt.getUniteOrganisation().getLibelleUniteOrganisation());
            if (type.equals("MASTER")) {
                ba.setIntitulle("AGENTS");
            } else {
                ba.setIntitulle(type);
            }
            ba.setEtiquette(etiquette);
            ba.setFseProprietaire(nomFSE);
            listeA.add(ba);
            ba = new BalanceCptClient();

        }
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl("classpath:/report/soldeUO_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/soldeUO_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listeA);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getListing(ApplicationContext context, String type, String format) {
        //JasperReportsPdfView view = new JasperReportsPdfView();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        List<ClientDTO> clientList = new ArrayList<>();
        XClientDTO xClientDTO = new XClientDTO();
        List<ListeClientDTO> xListe = new ArrayList<>();
        ListeClientDTO listeClientDTO = new ListeClientDTO();

        if (type.equals("Actif")) {
            Boolean b = true;
            clientList = clientRepository.findAllClient1Optimise(b);
            if (!clientList.isEmpty()) {
                xClientDTO.setTitre("Liste des clients actifs");
            } else {
                xClientDTO.setTitre("Liste des clients actifs est vide");
            }
        } else {
            Boolean b = false;
            clientList = clientRepository.findAllClient2Optimise(b);
            if (!clientList.isEmpty()) {
                xClientDTO.setTitre("Liste des clients inactifs");
            } else {
                xClientDTO.setTitre("Liste des clients inactifs est vide");
            }
        }
         Integer i = 0;
        if (clientList != null) {
            for (ClientDTO c : clientList) {
                if (c.getPersonneId() != null) {
                    PersonneDTO personneDTO = personneRepository.findPersonneById(c.getPersonneId());
                    if (personneDTO != null) {
                        listeClientDTO.setNomClient(personneDTO.getPersonnePhysiqueNomClient());
                        listeClientDTO.setPrenomClient(personneDTO.getPersonnePhysiquePrenomClient());
                        listeClientDTO.setDateNaiss(personneDTO.getPersonneDateNaissance());
                        PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                        if (personnePhysiqueDTO != null) {
                            if (personnePhysiqueDTO.getNationaliteId() != null) {
                                listeClientDTO.setNationalite(nationaliteRepository.getLibelleById(personnePhysiqueDTO.getNationaliteId()));
                            }
                            if (personnePhysiqueDTO.getPaysResidenceId() != null) {
                                listeClientDTO.setPaysResidence(nationaliteRepository.getLibelleById(personnePhysiqueDTO.getPaysResidenceId()));
                            }
                            listeClientDTO.setDateNaiss(personnePhysiqueDTO.getDateNaissance());

                        }
                        listeClientDTO.setTelephone(personneDTO.getTelephone());
                    }

                    ZoneId zone = ZoneId.systemDefault();
                    ZonedDateTime zdt = c.getCreatedDate().atZone(zone);
                    listeClientDTO.setDateCreation(formatDateOnlyWithTime(zdt));

                }
                CompteDTO compte = compteRepository.findByClientIdAndClasseCompteToReport(c.getId(), ClasseCompte.PRINCIPAL.name());
                if (compte != null) {
                    if (compte.getCompteAutreId() != null) {
                        listeClientDTO.setSolde(compteAutreRepository.getSoldeById(compte.getCompteAutreId()));
                    }
                }
                i = i + 1;
                if (c.getProfilActeurId() != null) {
                    listeClientDTO.setProfil(profilActeurRepository.getNomProfilActeur(c.getProfilActeurId()));
                } else {
                    System.out.println("Pas de profil");
                }
                if (c.getGroupeProfilActeurId() != null) {
                    listeClientDTO.setGroupeProfilActeur(groupeProfilActeurRepository.getNomGroupeProfilActeur(c.getGroupeProfilActeurId()));
                }

                if (c.getClientValide() != null) {
                    listeClientDTO.setStatut("Actif");
                } else {
                    listeClientDTO.setStatut("Inactif");
                }

                xListe.add(listeClientDTO);
                listeClientDTO = new ListeClientDTO();
            }
        }
        xClientDTO.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        xClientDTO.setListeClient(xListe);
        xClientDTO.setFmt(format);
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl("classpath:/report/liste_client_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/liste_client_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xClientDTO), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getListUO(ApplicationContext context, String libelleUniteOrganisation, String codeUO, Integer numeroOrdre, String typeUO, Long pereUoId, Long groupeId, Long villeId, String adresse, ZonedDateTime dateDebut, ZonedDateTime dateFin, String format) {
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        List<ListeUniteOrg> listeUO = new ArrayList<>();
        List<UniteOrganisation> listUorg = new ArrayList<>();
        XUniteOrg xUniteOrg = new XUniteOrg();
        ListeUniteOrg listeUniteOrg = new ListeUniteOrg();
        String nomFSE = uniteOrganisationRepository.findNomFSE(pereUoId);
        Pageable pageable = new Pageable() {
            @Override
            public int getPageNumber() {
                return 0;
            }

            @Override
            public int getPageSize() {
                return 0;
            }

            @Override
            public long getOffset() {
                return 0;
            }

            @Override
            public Pageable withPage(int pageNumber) {
                return this;
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public Pageable next() {
                return null;
            }

            @Override
            public Pageable previousOrFirst() {
                return null;
            }

            @Override
            public Pageable first() {
                return null;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }
        };
        Pageable pageable1;
        Page<UniteOrganisationDTO> page = uniteOrganisationService.findByPereUoId(libelleUniteOrganisation, codeUO, numeroOrdre, typeUO, pereUoId, groupeId, villeId, adresse, pageable);
        long tail = 0;
        if (page != null) {
            tail = page.getTotalElements();
        }
        if (tail == 0) {
            pageable1 = PageRequest.of(0, Math.toIntExact(tail + 1));
        } else {
            pageable1 = PageRequest.of(0, Math.toIntExact(tail));
        }
        Page<UniteOrganisationDTO> listUorganisation = uniteOrganisationService.findByPereUoId(libelleUniteOrganisation, codeUO, numeroOrdre, typeUO, pereUoId, groupeId, villeId, adresse, pageable1);
        for (UniteOrganisationDTO o : listUorganisation) {

            listeUniteOrg.setNomUO(o.getLibelleUniteOrganisation());
            listeUniteOrg.setCodeUO(o.getCodeUO());
            if (o.getTypeUOId() != null) {
                listeUniteOrg.setTypeUO(o.getTypeUOLibelleTypeUO());
            }
            if (o.getPereUOId() != null) {
                listeUniteOrg.setPereUO(o.getPereUOLibelleUniteOrganisation());
            }
            if (o.getProfilActeurId() != null) {

                listeUniteOrg.setProfil(o.getProfilActeurNom());
            }
            if (o.getEstActif() == null || !o.getEstActif()) {
                listeUniteOrg.setStatut("Inactif");
            } else {
                listeUniteOrg.setStatut("Actif");
            }
            listeUniteOrg.setTelephoneUO(o.getTelephone());
            listeUniteOrg.setAdresse(o.getAdresse());
            listeUO.add(listeUniteOrg);
            listeUniteOrg = new ListeUniteOrg();
        }
        xUniteOrg.setTitle(typeUO + "S" + " " + nomFSE);
        xUniteOrg.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        xUniteOrg.setFmt(format);
        xUniteOrg.setUniteOrgList(listeUO);
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl("classpath:/report/liste_UO_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/liste_UO_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xUniteOrg), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getListOfUODate(ApplicationContext context, ZonedDateTime dateDebut, ZonedDateTime dateFin, String typeUO, String format) {
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        List<ListeUniteOrg> listeUO = new ArrayList<>();
        List<UniteOrganisation> listUorg = new ArrayList<>();
        XUniteOrg xUniteOrg = new XUniteOrg();
        ListeUniteOrg listeUniteOrg = new ListeUniteOrg();
        //String nomFSE= uniteOrganisationRepository.findNomFSE(pereUoId);
        List<UniteOrganisationDTO> listUorganisation = uniteOrganisationService.findByPereUoDate(dateDebut, dateFin, typeUO);

        for (UniteOrganisationDTO o : listUorganisation) {

            listeUniteOrg.setNomUO(o.getLibelleUniteOrganisation());
            listeUniteOrg.setCodeUO(o.getCodeUO());
            if (o.getTypeUOId() != null) {
                listeUniteOrg.setTypeUO(o.getTypeUOLibelleTypeUO());
            }
            if (o.getPereUOId() != null) {
                listeUniteOrg.setPereUO(o.getPereUOLibelleUniteOrganisation());
            }
            if (o.getProfilActeurId() != null) {

                listeUniteOrg.setProfil(o.getProfilActeurNom());
            }
            if (o.getEstActif() == null || !o.getEstActif()) {
                listeUniteOrg.setStatut("Inactif");
            } else {
                listeUniteOrg.setStatut("Actif");
            }
            listeUniteOrg.setTelephoneUO(o.getTelephone());
            listeUniteOrg.setAdresse(o.getAdresse());
            listeUniteOrg.setDateCreation(o.getCreatedDate().toString());
            listeUniteOrg.setCreatedBy(o.getCreatedBy());
            listeUO.add(listeUniteOrg);
            listeUniteOrg = new ListeUniteOrg();
        }

        String date = getTitreWithDate(dateDebut, dateFin);
        xUniteOrg.setDate(date);
        xUniteOrg.setTitle(typeUO + "S");
        xUniteOrg.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        xUniteOrg.setFmt(format);
        xUniteOrg.setUniteOrgList(listeUO);
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl("classpath:/report/liste_UO_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/liste_UO_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xUniteOrg), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getOperation(ApplicationContext context, StatutOperation type, String format) {
        // JasperReportsPdfView view = new JasperReportsPdfView();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        XTransactionOp xOperationDTO = new XTransactionOp();
        List<XTransactionOp> list = new ArrayList<>();
        OperationList operation = new OperationList();
        List<Operation> opeList = new ArrayList<>();
        opeList = operationRepository.findEtatByStatut(type);
        String dateOp = null;
        for (Operation p : opeList) {
            xOperationDTO.setIdTransc(p.getCodeOperation());
            xOperationDTO.setTypeService(p.getTypeService().getLibelle());
            xOperationDTO.setMontantOp(p.getMontantOp());
            xOperationDTO.setMontant(p.getMontantFrais());
            if (p.getEmetteur() != null) {
                if (p.getEmetteur().getPersonne() != null) {
                    if (p.getEmetteur().getPersonne().getPersonnePhysique() != null) {
                        xOperationDTO.setEnvoi(p.getEmetteur().getPersonne().getPersonnePhysique().getNomClient() + " " + p.getEmetteur().getPersonne().getPersonnePhysique().getPrenomClient());
                    }
                    if (p.getEmetteur().getPersonne().getPersonneMorale() != null) {
                        xOperationDTO.setEnvoi(p.getEmetteur().getPersonne().getPersonneMorale().getRaisonSociale());
                    }
                }
            }
            if (p.getBeneficaire() != null) {
                if (p.getBeneficaire().getPersonne() != null) {
                    if (p.getBeneficaire().getPersonne().getPersonnePhysique() != null) {
                        xOperationDTO.setBeneficiaire(p.getBeneficaire().getPersonne().getPersonnePhysique().getNomClient() + " " + p.getBeneficaire().getPersonne().getPersonnePhysique().getPrenomClient());
                    }
                    if (p.getBeneficaire().getPersonne().getPersonneMorale() != null) {
                        xOperationDTO.setBeneficiaire(p.getBeneficaire().getPersonne().getPersonneMorale().getRaisonSociale());
                    }
                }
            }
            dateOp = formatDateOnly(p.getDateOperation());
            xOperationDTO.setDateOperation(dateOp);
            if (p.getCompteSrc() != null) {

                xOperationDTO.setNumeroCpt(p.getCompteSrc().getValeurCompte());
            }
            list.add(xOperationDTO);
            xOperationDTO = new XTransactionOp();
        }
        operation.setFmt(format);
        operation.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        operation.setListOperation(list);
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl("classpath:/report/list_Operation_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/list_Operation_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(operation), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView newReport(ApplicationContext context, String format) {
        // JasperReportsPdfView view = new JasperReportsPdfView();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        List<CompteDTO> listCompte = new ArrayList<>();
        List<String> d = new ArrayList<>();
        List<BalanceCptClient> liste = new ArrayList<>();
        final BalanceCptClient[] bc = {new BalanceCptClient()};
        List<TransactionComptePti> listCpt = new ArrayList<>();
        listCompte = transactionComptePtiRepository.findAllCompteClientOptimise();
        final BigDecimal[] totalDebit = {new BigDecimal("0")};
        final BigDecimal[] totalCredit = {new BigDecimal("0")};
        if (listCompte != null) {
            bc[0].setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
            bc[0].setFmt(format);
            listCompte.parallelStream().forEach(cpt -> {
                bc[0].setNumeroCpt(cpt.getValeurCompte());
                totalDebit[0] = transactionComptePtiRepository.findTotal(cpt.getValeurCompte(), TypeTransactionCompte.DEBIT);
                totalCredit[0] = transactionComptePtiRepository.findTotal(cpt.getValeurCompte(), TypeTransactionCompte.CREDIT);
                totalDebit[0] = totalDebit[0].setScale(0, RoundingMode.HALF_UP);
                totalCredit[0] = totalCredit[0].setScale(0, RoundingMode.HALF_UP);
                log.debug("-------total des debits----" + totalDebit[0] + "et credits" + totalCredit[0]);
                String a = null;
                bc[0].setTotalDebit(totalDebit[0]);
                bc[0].setTotalCredit(totalCredit[0]);
                if (cpt.getClientId() != null) {
                    ClientDTO clientDTO=clientRepository.findClientByClientIdOptimise(cpt.getClientId());
                    if(clientDTO.getPersonneId()!=null){
                        PersonneDTO personneDTO=personneRepository.findPersonneById(clientDTO.getPersonneId());
                        if(personneDTO.getPersonnePhysiqueId()!=null){
                            PersonnePhysiqueDTO personnePhysiqueDTO=personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                            if(personnePhysiqueDTO!=null){
                                bc[0].setIntitulle(personnePhysiqueDTO.getNomClient() + " " + personnePhysiqueDTO.getPrenomClient());
                            }
                        }
                        if (personneDTO.getPersonneMoraleId() != null) {
                            PersonneMorale personneMorale=personneMoraleRepository.findById(personneDTO.getPersonneMoraleId()).orElse(null);
                            bc[0].setIntitulle(personneMorale.getRaisonSociale());
                        }
                    }

                }
                bc[0].setLibelle("CLIENTS");
                liste.add(bc[0]);
                bc[0] = new BalanceCptClient();

            });

            if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
                view.setUrl("classpath:/report/balance_des_comptes_xlsx.jasper");

            } else {
                view.setUrl("classpath:/report/balance_des_comptes_pdf.jasper");
            }
            view.setApplicationContext(context);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(liste, false);
            params.put(PARAM_DATASOURCE, dataSource);
            return new ModelAndView(view, params);
        }
        return null;
    }

    public ModelAndView getChrgmtRapport(ApplicationContext context, Long idChargement, String format) {
        //JasperReportsPdfView view = new JasperReportsPdfView();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        XligneCharger xLigneChTr = new XligneCharger();
        XLigneChrgTransf chrgTransf = new XLigneChrgTransf();
        List<XligneCharger> listeChrg = new ArrayList<>();
        List<XligneCharger> listeChrg2 = new ArrayList<>();
        List<LigneChargementTransfert> ligneChg = new ArrayList<>();
        ChargementTransfert chargement = chargementTransfertService.findOne(idChargement);
        if (chargement != null) {
            chrgTransf.setNomFichier(chargement.getNomFichier());
            chrgTransf.setReference(chargement.getReference());
            chrgTransf.setMontantTotal(chargement.getMontantTotal());
            chrgTransf.setNombreLigne(chargement.getNombreLigne());
            chrgTransf.setNombreLigneCharger(chargement.getNombreLigneCharge());
            chrgTransf.setNombreLigneErreur(chargement.getNombreLigneErreur());
            chrgTransf.setCreateBy(chargement.getCreatedBy());
            chrgTransf.setLastModifiedBy(chargement.getLastModifiedBy());

        }
        ligneChg = ligneChargementTransfertService.queryDetail(idChargement);
        log.debug("------lgne chargement-----" + ligneChg);

        for (LigneChargementTransfert l : ligneChg) {
            if (!l.getStatutLigne().equals(StatutLigneChargement.OC)) {
                xLigneChTr.setNumCpt(l.getNumeroTelephone());
                xLigneChTr.setMontant(l.getMontant());
                xLigneChTr.setErreur(l.getError());
                xLigneChTr.setNom(l.getNom());
                xLigneChTr.setPrenom(l.getPrenom());
                listeChrg.add(xLigneChTr);
                xLigneChTr = new XligneCharger();
            }
            if (l.getStatutLigne().equals(StatutLigneChargement.OC)) {
                xLigneChTr.setNumCpt(l.getNumeroTelephone());
                xLigneChTr.setMontant(l.getMontant());
                xLigneChTr.setErreur(l.getError());
                xLigneChTr.setNom(l.getNom());
                xLigneChTr.setPrenom(l.getPrenom());
                listeChrg2.add(xLigneChTr);
                xLigneChTr = new XligneCharger();
            }
        }
        chrgTransf.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        chrgTransf.setLigneNonCharger(listeChrg);
        chrgTransf.setLigneCharger(listeChrg2);
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl("classpath:/report/reportChargement_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/reportChargement.jasper");
        }

        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(chrgTransf), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getChrgmtRapportCpt(ApplicationContext context, Long idChargement, String format) {
        log.debug("get rapport of compte ");
        //JasperReportsPdfView view = new JasperReportsPdfView();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));

        Map<String, Object> params = new HashMap<>();
        XLigneChrgTransf xLigneChTr = new XLigneChrgTransf();
        List<XligneCharger> ligneCpt = new ArrayList<>();
        List<XligneCharger> ligneCpt2 = new ArrayList<>();
        XligneCharger ligne = new XligneCharger();
        XLigneChrgTransf listeChrg = new XLigneChrgTransf();
        List<LigneChargementCompte> ligneChgC = new ArrayList<>();
        ChargementCompte chargementCompte = chargementCompteService.findOne(idChargement);
        listeChrg.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (chargementCompte != null) {
            listeChrg.setNomFichier(chargementCompte.getNomFichier());
            listeChrg.setReference(chargementCompte.getReference());
            listeChrg.setNombreLigne(chargementCompte.getNombreLigne());
            listeChrg.setNombreLigneCharger(chargementCompte.getNombreLigneCharge());
            listeChrg.setNombreLigneErreur(chargementCompte.getNombreLigneErreur());
            listeChrg.setCreateBy(chargementCompte.getCreatedBy());
            listeChrg.setLastModifiedBy(chargementCompte.getLastModifiedBy());
        }
        ligneChgC = ligneChargementCompteService.queryDetail(idChargement);

        for (LigneChargementCompte lc : ligneChgC) {
            if (!lc.getStatutLigne().equals(StatutLigneChargement.OC)) {
                ligne.setNom(lc.getNom());
                ligne.setPrenom(lc.getPrenom());
                ligne.setNumTel(lc.getTelephone());
                ligne.setTypePersonne(lc.getTypePersonne());
                ligne.setErreur(lc.getError());
                ligneCpt.add(ligne);
                ligne = new XligneCharger();
            }
            if (lc.getStatutLigne().equals(StatutLigneChargement.OC)) {
                ligne.setNom(lc.getNom());
                ligne.setPrenom(lc.getPrenom());
                ligne.setNumTel(lc.getTelephone());
                ligne.setTypePersonne(lc.getTypePersonne());
                ligne.setErreur(lc.getError());
                ligneCpt2.add(ligne);
                ligne = new XligneCharger();
            }
        }

        listeChrg.setLigneNonCharger(ligneCpt);
        listeChrg.setLigneCharger(ligneCpt2);

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl("classpath:/report/reportChrgCompte_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/reportChrgCompte.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(listeChrg), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView qrCodeUo(ApplicationContext context, Long idUO) {
        JasperReportsPdfView view = new JasperReportsPdfView();
        Map<String, Object> params = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        UniteOrganisationDTO uo = new UniteOrganisationDTO();
        PointVenteDTO pointVenteDTO = new PointVenteDTO();
        XQrCode xQrCode = new XQrCode();
        XQrUo xQrUo = new XQrUo();
        List<XQrCode> xQrCodeList = new ArrayList<>();
        String json = null;
        pointVenteDTO = pointVenteService.findOne(idUO);
        if (pointVenteDTO != null) {
            xQrCode.setLibelleUO(pointVenteDTO.getUniteOrganisationLibelleUniteOrganisation());
            uo = uniteOrganisationService.findOne(pointVenteDTO.getUniteOrganisationId());
            if (uo != null) {
                xQrCode.setCodeUO(pointVenteDTO.getCodePointVente());
                if (uo.getTypeUOLibelleTypeUO().equals("MARCHAND")) {
                    xQrUo.setCodeMarchand(uo.getCodeUO());
                    xQrUo.setCodePV(pointVenteDTO.getCodePointVente());
                } else {
                    xQrUo.setCodeMarchand(uo.getCodeUO());
                    xQrUo.setCodePV(pointVenteDTO.getCodePointVente());
                }

            }
        }
        try {
            json = mapper.writeValueAsString(xQrUo);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        String secret = null, crypter = null, decrypt = null;
        crypter = cryptageCodeQRService.encrypt(json, secret);
        decrypt = cryptageCodeQRService.decrypt(crypter, secret);
        log.debug("--------crypter------" + crypter);
        log.debug("--------json------" + decrypt);
        xQrCode.setQrCodeData(crypter);
        xQrCodeList.add(xQrCode);
        view.setUrl("classpath:/report/qRCode.jasper");
        view.setApplicationContext(context);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(xQrCodeList, false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getFacturierListe(ApplicationContext context, XFactureFiltre xFactureFiltre) {
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(xFactureFiltre.getFormat()));
        Map<String, Object> params = new HashMap<>();
        List<Facturier> facturierList = new ArrayList<>();
        XFacturier xFacturier = new XFacturier();
        List<XFacturier> xFacturierList = new ArrayList<>();
        facturierList = facturierService.getListeFacturier(xFactureFiltre.getNomFacturier(), xFactureFiltre.getCodeFacturier(),
                xFactureFiltre.getClassification_id(), xFactureFiltre.getTypeFacturier(), xFactureFiltre.getPossesseur_id());
        xFacturier.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        for (Facturier facturier : facturierList) {
            xFacturier.setNomFacturier(facturier.getNomFacturier());
            xFacturier.setCodeFacturier(facturier.getCodeFacturier());
            if (facturier.getClassification() != null) {
                xFacturier.setClassificationId(facturier.getClassification().getLibelleClassification());
            }
            if (facturier.getTypeFacturier() != null) {
                xFacturier.setTypeFacturier(facturier.getTypeFacturier().name());
            }
            if (facturier.getPossesseur() != null) {
                xFacturier.setPossesseurId(facturier.getPossesseur().getLibelleUniteOrganisation());
            }
            if (facturier.getUniteOrganisation() != null) {
                xFacturier.setMarchand(facturier.getUniteOrganisation().getLibelleUniteOrganisation());
            }
            if (facturier.getSecteurActivite() != null) {
                xFacturier.setSecteurActivite(facturier.getSecteurActivite().name());
            }
            xFacturier.setFse(facturier.getFse());
            xFacturier.setFmt(xFactureFiltre.getFormat());
            xFacturierList.add(xFacturier);
            xFacturier = new XFacturier();
        }
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(xFactureFiltre.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(xFactureFiltre.getFormat()))) {
            view.setUrl("classpath:/report/facturier_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/facturier_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(xFacturierList, false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getFacture(ApplicationContext context, XFactureDTO xFactureDTO) {
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(xFactureDTO.getFormat()));
        Map<String, Object> params = new HashMap<>();
        List<FactureDTO> list = new ArrayList<>();
        XFacture xFacture = new XFacture();
        List<XFacture> xFactureList = new ArrayList<>();
        if (xFactureDTO.getFacturierId() != null) {
            //list = factureRepository.findFacturesOfProduit(xFactureDTO.getFacturierId());
            list = factureRepository.findFactureOfProduitOptimise(xFactureDTO.getFacturierId());
        } else {
            list = factureRepository.findFactureByCredentialOptimise(xFactureDTO.getProduit(), xFactureDTO.getRefFacture(), xFactureDTO.getRefPaiement(), xFactureDTO.getDatePaiement());
        }

        if (list != null) {
            xFacture.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
            for (FactureDTO facture : list) {
                xFacture.setReferenceFacature(facture.getReferenceFacature());
                xFacture.setMontantFacture(facture.getMontantPaiement());
                xFacture.setReferencePaiement(facture.getReferencePaiement());
                xFacture.setDatePaiement(facture.getDatePaiement());
                xFacture.setProduit(facture.getProduit());
                xFacture.setReferenceExt(facture.getReferenceExt());
                xFacture.setTransactionIdExt(facture.getTransactionIdExt());
                xFacture.setExtraData(facture.getStrExtraData());
                if(facture.getOperationId()!=null){
                    OperationDTO opDTO=operationRepository.findOperationById(facture.getOperationId());
                    if(opDTO.getCompteSrcId()!=null) {
                        CompteDTO compteDTO=compteRepository.findByNativeId(opDTO.getCompteSrcId());
                        if(compteDTO!=null&&compteDTO.getId()!=null){
                            if(compteDTO.getClientId()!=null){
                                ClientDTO clientDTO=clientRepository.findClientByClientIdOptimise(compteDTO.getClientId());
                                if(clientDTO!=null&&clientDTO.getPersonneId()!=null){
                                    PersonneDTO personneDTO=personneRepository.findPersonneById(clientDTO.getPersonneId());
                                    if(personneDTO!=null && personneDTO.getPersonnePhysiqueId()!=null){
                                        xFacture.setTelephone(personneDTO.getTelephone());
                                        PersonnePhysiqueDTO personnePhysiqueDTO=personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                                        if(personnePhysiqueDTO!=null){
                                            xFacture.setNom(personnePhysiqueDTO.getNomClient());
                                            xFacture.setPrenom(personnePhysiqueDTO.getPrenomClient());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                xFacture.setFmt(xFactureDTO.getFormat());
                xFactureList.add(xFacture);
                xFacture = new XFacture();
            }
        }
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(xFactureDTO.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(xFactureDTO.getFormat()))) {
            view.setUrl("classpath:/report/facture_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/facture_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(xFactureList, false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getExportSolde(ApplicationContext context, UniteEtat uniteEtat) {
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(uniteEtat.getFormat()));
        Map<String, Object> params = new HashMap<>();
        HashMap tabSolde = new HashMap<>();
        TableauSolde tableauSolde = new TableauSolde();
        List<TableauSolde> tabList = new ArrayList<>();
        tabSolde = uniteOrganisationService.getAllSolde(uniteEtat.getPereUoId());
        if (tabSolde != null) {
            tableauSolde.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
            for (Object o : tabSolde.entrySet()) {
                Map.Entry pair = (Map.Entry) o;
                if (pair.getKey().equals("distribution")) {
                    tableauSolde.setDistribution(Double.parseDouble(pair.getValue().toString()));
                }
                if (pair.getKey().equals("clients")) {
                    tableauSolde.setClient(Double.parseDouble(pair.getValue().toString()));
                }
                if (pair.getKey().equals("marchands")) {
                    tableauSolde.setMarchand(Double.parseDouble(pair.getValue().toString()));
                }
                if (pair.getKey().equals("master")) {
                    tableauSolde.setMaster(Double.parseDouble(pair.getValue().toString()));
                }
                if (pair.getKey().equals("agence")) {
                    tableauSolde.setAgence(Double.parseDouble(pair.getValue().toString()));
                }
                if (pair.getKey().equals("commission")) {
                    tableauSolde.setCommission(Double.parseDouble(pair.getValue().toString()));
                }
                if (pair.getKey().equals("commissionAtt")) {
                    tableauSolde.setCommissionAtt(Double.parseDouble(pair.getValue().toString()));
                }
                if (pair.getKey().equals("transfertAtt")) {
                    tableauSolde.setTransfertAtt(Double.parseDouble(pair.getValue().toString()));
                }
                if (pair.getKey().equals("taxe")) {
                    tableauSolde.setTaxe(Double.parseDouble(pair.getValue().toString()));
                }
                if (pair.getKey().equals("plateforme")) {
                    tableauSolde.setPlateforme(Double.parseDouble(pair.getValue().toString()));
                }
            }
        }
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(uniteEtat.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(uniteEtat.getFormat()))) {
            view.setUrl("classpath:/report/tableauSolde_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/tableauSolde_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(tableauSolde), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getRapportReglement(ApplicationContext context, UniteEtat uniteEtat) {
        log.debug("-------Request to get rapport of-----------" + uniteEtat.getReglementId());
        JasperReportsPdfView view = new JasperReportsPdfView();
        Map<String, Object> params = new HashMap<>();
        List<LigneReglement> listeReglement = new ArrayList<>();
        List<XLigneReglementDTO> ligneReglementValide = new ArrayList<>();
        List<XLigneReglementDTO> ligneReglementNonValide = new ArrayList<>();
        Reglement reglement = new Reglement();
        XReglementDTO reglementDTO = new XReglementDTO();
        Integer nbrLigneCharger = null;
        Double mntTotal = null;
        Double totalCommRegle=0D;
        Double totalCommNonRegle=0D;
        if (uniteEtat.getReglementId() != null) {
            reglement = reglementService.findOne(uniteEtat.getReglementId());
            nbrLigneCharger = ligneReglementRepository.countByReglementId(reglement.getId());
            listeReglement = ligneReglementRepository.findByReglementId(reglement.getId());
            mntTotal = ligneReglementRepository.getMontantTotal(reglement.getId());
            reglementDTO.setMontantTotal(mntTotal);
            reglementDTO.setNbrOrganisation(nbrLigneCharger);

        }
        for (LigneReglement ligne : listeReglement) {
            reglementDTO.setCreateBy(reglement.getCreatedBy());
            reglementDTO.setLastModifiedBy(reglement.getLastModifiedBy());
            if (ligne.getValide()) {

                totalCommRegle=totalCommRegle+ligne.getMontantCommission();
                XLigneReglementDTO xLigneReglementDTO = new XLigneReglementDTO();
                xLigneReglementDTO.setMontantCommission(ligne.getMontantCommission());
                xLigneReglementDTO.setOrganisation(ligne.getOrganisation().getLibelleUniteOrganisation());
                xLigneReglementDTO.setSoldeCompteOp(ligne.getSoldeCompteOp());
                xLigneReglementDTO.setErreur(ligne.getErreur());
                xLigneReglementDTO.setSoldeCompteOpApres(ligne.getSoldeCompteOpApres());
                ligneReglementValide.add(xLigneReglementDTO);
            } else {
                totalCommNonRegle=totalCommNonRegle+ligne.getMontantCommission();
                XLigneReglementDTO xLigneReglementDTO = new XLigneReglementDTO();
                xLigneReglementDTO.setMontantCommission(ligne.getMontantCommission());
                xLigneReglementDTO.setOrganisation(ligne.getOrganisation().getLibelleUniteOrganisation());
                xLigneReglementDTO.setSoldeCptCommission(ligne.getSoldeCompteOp());
                xLigneReglementDTO.setErreur(ligne.getErreur());
                xLigneReglementDTO.setSoldeCompteOp(ligne.getSoldeCompteOp());
                xLigneReglementDTO.setSoldeCompteOpApres(ligne.getSoldeCompteOpApres());
                ligneReglementNonValide.add(xLigneReglementDTO);
            }

        }
        reglementDTO.setLigneReglementValide(ligneReglementValide);
        reglementDTO.setLigneReglementNonValide(ligneReglementNonValide);
        reglementDTO.setTotalRegle(totalCommRegle);
        reglementDTO.setTotalNonRegle(totalCommNonRegle);
        reglementDTO.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        view.setUrl("classpath:/report/reportReglement.jasper");
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(reglementDTO), false);
        params.put(PARAM_DATASOURCE, dataSource);
        params.put("nbrComRegle", ligneReglementValide.size());
        params.put("nbrComNonRegle", ligneReglementNonValide.size());
        return new ModelAndView(view, params);
    }

    public ModelAndView getTransactionOfCompteAutre(ApplicationContext context, Long compteAutreId, String format, String type,ZonedDateTime debut,ZonedDateTime fin) {
        log.debug("Request to Export Transaction of compte autre id  " + compteAutreId + "et" + format + "et" + type);
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        UniteOrganisation uo = new UniteOrganisation();
        Client client = new Client();
        List<TransactionComptePtiDTO> transactionComptePtiDTOS =new ArrayList<>();
        CompteAutre compteAutre = compteAutreRepository.findById(compteAutreId).orElse(null);
        List<XTransactionDTO> xTransactionDTOList = new ArrayList<>();
        XTransactionDTO xTransactionDTO = new XTransactionDTO();
        if(debut!=null&&fin!=null){
            transactionComptePtiDTOS = transactionComptePtiRepository.findTransByCompteAutreDateOptimiser(compteAutre.getId(),debut,fin.plusHours(23).plusMinutes(59));

        } else  if (debut!=null){
            transactionComptePtiDTOS = transactionComptePtiRepository.findTransByCompteAutreDateOptimiser(compteAutre.getId(),debut,debut.plusHours(23).plusMinutes(59));

        }else {
            transactionComptePtiDTOS = transactionComptePtiRepository.findByCompteAutreIdOptimise(compteAutre.getId());

        }
        CompteDTO compteDTO = compteMapper.toDto(compteRepository.findByCompteAutreId(compteAutre.getId()));
        if (type != null) {
            client = clientRepository.getOne(compteDTO.getClientId());
        } else {
            uo = uniteOrganisationRepository.findById(compteDTO.getUniteOrganisationId()).orElse(null);
        }
        xTransactionDTO.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (compteDTO.getId() != null && uo != null) {
            if (uo.getId() != null) {
                xTransactionDTO.setLibelleOrg(compteDTO.getUniteOrganisationLibelleUniteOrganisation());
                xTransactionDTO.setCompteCible(compteDTO.getValeurCompte());
                xTransactionDTO.setEtiquette(compteDTO.getEtiquette());
            }
        }

        if (uo != null) {
            if (uo.getId() != null) {
                xTransactionDTO.setTypeUO(uo.getTypeUO().getLibelleTypeUO());
            }
        }
        if (compteDTO.getId() != null && client != null) {
            if (client.getId() != null) {
                xTransactionDTO.setTypeUO("DU COMPTE CLIENT");
                if (client.getPersonne() != null) {
                    if (client.getPersonne().getPersonnePhysique() != null) {
                        xTransactionDTO.setLibelleOrg(client.getPersonne().getPersonnePhysique().getNomClient() + " " + client.getPersonne().getPersonnePhysique().getPrenomClient());
                    } else {
                        xTransactionDTO.setLibelleOrg(client.getPersonne().getPersonneMorale().getRaisonSociale());
                    }
                }
                xTransactionDTO.setCompteCible(compteDTO.getValeurCompte());
                xTransactionDTO.setEtiquette(compteDTO.getEtiquette());
            }
        }
        for (TransactionComptePtiDTO t : transactionComptePtiDTOS) {
            xTransactionDTO.setMontant(t.getMontant());
            xTransactionDTO.setEtat(t.getEtat().name());
            xTransactionDTO.setType(t.getType().name());
            xTransactionDTO.setSoldeCourant(t.getSoldeCourant());
            xTransactionDTO.setSoldeApres(t.getSoldeApres());
            xTransactionDTO.setStatut(t.getOperationStatut());
            xTransactionDTO.setCodeOperation(t.getCodeOperation());
            xTransactionDTO.setService(t.getTypeService());
            xTransactionDTO.setValeurCompte(t.getOperationCompteSrc());
            xTransactionDTO.setDateTransaction(t.getDate());
            xTransactionDTO.setMotifOp(t.getOperationMotif());
            //t.getOperationId().
            xTransactionDTOList.add(xTransactionDTO);
            xTransactionDTO = new XTransactionDTO();

        }
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl("classpath:/report/transaction_of_compte_autre_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/transaction_of_compte_autre.jasper");
        }
        view.setApplicationContext(context);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(xTransactionDTOList, false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);

    }

    public ModelAndView getHistoriqueSolde(ApplicationContext appContext, XHistoriDTO xHistoriDTO) {
        log.debug("request to generate rapport" + xHistoriDTO.getDateEnregistrement() + xHistoriDTO.getTypeHistorique() + xHistoriDTO.getFormat());
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(xHistoriDTO.getFormat()));
        Map<String, Object> params = new HashMap<>();

        List<DetailHistorique> detailHistoriques;
        ListeHistorique listeHistorique = new ListeHistorique();
        List<ListeHistorique> listeHis = new ArrayList<>();
        Historique historique;
        HistoryType historyType;
        historyType = HistoryType.valueOf(xHistoriDTO.getTypeHistorique());
        LocalDate zonedDateTime = LocalDate.parse(xHistoriDTO.getDateEnregistrement());
        historique = historiqueRepository.findByCriteria(historyType, zonedDateTime);
        detailHistoriques = detailHistoriqueRepository.getListHistorique(historique.getId());
        if(xHistoriDTO.getTypeHistorique().equals(HistoryType.SOLDECLIENT.name())){
            //params.put("type",HistoryType.SOLDECLIENT.name());
        }
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(xHistoriDTO.getFormat()))) {
            listeHistorique.setFormat(0);
        } else {
            listeHistorique.setFormat(2);
        }

        listeHistorique.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        for (DetailHistorique ligne : detailHistoriques) {
            listeHistorique.setLibelle(ligne.getLibelle());
            listeHistorique.setNumeroCpt(ligne.getNumeroCpt());
            listeHistorique.setSolde(ligne.getSolde());
            listeHistorique.setTelephone(ligne.getTelephone());
            listeHistorique.setCodeUo(ligne.getCodeUo());
            listeHistorique.setTypeUo(ligne.getTypeUo());
            listeHistorique.setDescription(ligne.getDescription());

            listeHis.add(listeHistorique);
            listeHistorique = new ListeHistorique();
        }
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(xHistoriDTO.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(xHistoriDTO.getFormat()))) {
            if(xHistoriDTO.getTypeHistorique().equals("SOLDEAGENT")){

                view.setUrl("classpath:/report/historiqueSolde_Agent_xlsx.jasper");
            } else {
                view.setUrl("classpath:/report/historiqueSolde_xlsx.jasper");
            }

        } else {
            if(xHistoriDTO.getTypeHistorique().equals("SOLDEAGENT")){
                view.setUrl("classpath:/report/historiqueSolde_Agent_pdf.jasper");
            }else{
                view.setUrl("classpath:/report/historiqueSolde_pdf.jasper");
            }
        }

        view.setApplicationContext(appContext);
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(listeHis, false);
        params.put("date",zonedDateTime.toString());
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public String formatDateOnly(ZonedDateTime dateTime) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(dateTime);
    }

    public String formatDateOnlyWithTime(ZonedDateTime dateTime) {
        return DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(dateTime);
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

    public List<XCompteDTO> getCompteByEtat(boolean compteCloture, boolean compteBloque) {
        List<Compte> comptes = new ArrayList<>();
        List<XCompteDTO> xCompteDTOS = new ArrayList<>();
        String etat = null;
        if ((compteBloque == true && compteCloture == false) || (compteBloque == false && compteCloture == true)) {
            etat = compteCloture == true ? "Clôturé" : compteBloque == true ? "Bloqué" : null;
        }
        if (compteCloture) {
            comptes = compteRepository.findByEtatCompteAndClientIsNotNull(EtatCompte.FERME);
        } else if (compteBloque) {
            comptes = compteRepository.findByStatutCompteAndClientIsNotNull(StatutCompte.DESACTIVE);
        } else {
            comptes = compteRepository.findByStatutCompteOrEtatCompteAndClientIsNotNull(StatutCompte.DESACTIVE, EtatCompte.FERME);
        }
        String finalEtat = etat;
        comptes.forEach(compte -> {
            XCompteDTO xCompteDTO = new XCompteDTO();
            xCompteDTO.setNomPrenom(UtilService.nameToClient(compte));
            xCompteDTO.setNumeroCompte(compte.getValeurCompte());
            xCompteDTO.setNumeroTel(compte.getClient().getPersonne().getTelephone());
            String etatCompte = compte.getEtatCompte() != null ? "Clôturé" : null;
            String statutCompte = compte.getStatutCompte() != null ? "Bloqué" : null;
            if (finalEtat == null) {
                if (etatCompte != null && statutCompte != null) {
                    xCompteDTO.setEtat(etatCompte + "/" + statutCompte);
                } else if (etatCompte != null && statutCompte == null) {
                    xCompteDTO.setEtat(etatCompte);
                } else {
                    xCompteDTO.setEtat(statutCompte);
                }
            } else {
                xCompteDTO.setEtat(finalEtat);
            }
            xCompteDTOS.add(xCompteDTO);
        });

        return xCompteDTOS;
    }

    public List<XCompteDTO> getCompteByEtatAndStatut() {
        List<Compte> comptes = compteRepository.findAll()
                .stream().filter(
                        compte -> EtatCompte.FERME.equals(compte.getEtatCompte())
                        || StatutCompte.DESACTIVE.equals(compte.getStatutCompte())).collect(Collectors.toList());
        List<XCompteDTO> xCompteDTOS = new ArrayList<>();

        if (!comptes.isEmpty()) {
            comptes.forEach(compte -> {
                XCompteDTO xCompteDTO = new XCompteDTO();
                xCompteDTO.setNomPrenom(UtilService.nameToClient(compte));
                xCompteDTO.setNumeroCompte(compte.getValeurCompte());
                xCompteDTO.setNumeroTel(compte.getClient().getPersonne().getTelephone());
                xCompteDTO.setEtat(UtilService.etatBloqueOuCloture(compte));

                xCompteDTOS.add(xCompteDTO);
            });
        }

        return xCompteDTOS;
    }

    public List<XBeneficiaire> getBeneficiaire(String valeurCompte, ZonedDateTime debut, ZonedDateTime fin) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Optional<Compte> compte = compteRepository.findCompteByValeurCompteAndClientIsNotNull(valeurCompte);

        if (compte.isPresent()) {
            List<Operation> operations = operationRepository
                    .findByCompteSrcValeurCompteAndDateOperationIsBetweenAndCompteSrcClientIsNotNullAndCompteDestIsNotNullAndCompteDestClientIsNotNull(valeurCompte, debut, fin);
            List<XBeneficiaire> xBeneficiaires = new ArrayList<>();
            //   .stream().sorted(Comparator.comparing(Operation::get)).collect(Collectors.toList());
            if (!operations.isEmpty()) {
                operations.forEach(operation -> {
                    XBeneficiaire xBeneficiaire = new XBeneficiaire();
                    xBeneficiaire.setDateOp(dateTimeFormatter.format(operation.getDateOperation()));
                    xBeneficiaire.setMontantOp(operation.getMontantOp());
                    xBeneficiaire.setNumOp(operation.getCodeOperation());
                    xBeneficiaire.setTypeService(operation.getTypeService() != null ? operation.getTypeService().getLibelle() : null);
                    xBeneficiaire.setNumCompteBeneficiaire(operation.getCompteDest().getValeurCompte());
                    xBeneficiaire.setNomPrenom(UtilService.nameToClient(operation.getCompteDest()));
                    if (operation.getCompteDest().getClient() != null) {
                        xBeneficiaire.setNomPrenom(UtilService.nameToClient(operation.getCompteDest().getClient()));
                    } else {
                        xBeneficiaire.setNomPrenom(operation.getCompteDest().getUniteOrganisation().getLibelleUniteOrganisation());
                    }

                    xBeneficiaires.add(xBeneficiaire);
                });
            }
            return xBeneficiaires;
        }
        return null;
    }

    public List<XUser> getUsers(XUserDTO xUserDTO) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<XUser> xUsers = new ArrayList<>();
        List<User> users = new ArrayList<>();
        if (xUserDTO != null) {
            users = userRepository.findUserWithCriteria(xUserDTO.getOrganisationId(), xUserDTO.getProfilId(), xUserDTO.getLogin(), xUserDTO.getTelephone());
        } else {
            users = userRepository.findAll();
        }

        users.forEach(user -> {
            XUser xUser = new XUser();
            xUser.setNomPrenom(user.getFirstName() + ' ' + user.getLastName());
            xUser.setEmail(user.getEmail());
            xUser.setLogin(user.getLogin());
            if (user.getProfil() != null) {
                xUser.setNomProfil(user.getProfil().getNom());
            }
            xUser.setNumTel(user.getPhoneNumber());
            xUser.setDateConnexion(user.getDateDerniereConnexion() != null ? dateTimeFormatter.format(user.getDateDerniereConnexion()) : null);
            xUser.setEtat(user.getActivated() ? "Actif" : "Inactif");
            ZoneId zone = ZoneId.systemDefault();
            ZonedDateTime zdt = user.getCreatedDate().atZone(zone);
            xUser.setDateCreation(formatDateOnlyWithTime(zdt));
            xUsers.add(xUser);
        });
        return xUsers;
    }

    public List<XCompteDTO> getCompteInInstance() {
        List<Compte> comptes = compteRepository.findByClientIsNotNullAndClientClientIdentifieIsFalseAndClientSupprimeIsFalse();
        List<XCompteDTO> xCompteDTOS = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        if (!comptes.isEmpty()) {
            comptes.forEach(compte -> {
                XCompteDTO xCompteDTO = new XCompteDTO();
                xCompteDTO.setNomPrenom(UtilService.nameToClient(compte));
                xCompteDTO.setNumeroCompte(compte.getValeurCompte());
                xCompteDTO.setNumeroTel(compte.getClient().getPersonne().getTelephone());
                xCompteDTO.setDateCreation(dateTimeFormatter.format(compte.getCreatedDate()));

                xCompteDTOS.add(xCompteDTO);
            });
        }
        return xCompteDTOS;
    }

    public ModelAndView getListCompte(ApplicationContext context, InfosFormulaire infosFormulaire) {

        XListCompte xListCompte = new XListCompte();
        xListCompte.setListCompte(getCompteByEtatAndStatut());
        xListCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (xListCompte.getListCompte().size() > 0) {
            xListCompte.setTitre("Liste des comptes bloqués ou clôturés");
        } else {
            xListCompte.setTitre("La liste des comptes bloqués ou clôturés est vide");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/compte_cloture_bloque_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/compte_cloture_bloque_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getListCompteFilter(ApplicationContext context, InfosFormulaire infosFormulaire) {

        XListCompte xListCompte = new XListCompte();
        xListCompte.setListCompte(getCompteByEtat(infosFormulaire.isCompteBloque(), infosFormulaire.isCompteCloture()));
        xListCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (xListCompte.getListCompte().size() > 0) {
            xListCompte.setTitre("Liste des comptes bloqués ou clôturés");
        } else {
            xListCompte.setTitre("La liste des comptes bloqués ou clôturés est vide");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/compte_cloture_bloque_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/compte_cloture_bloque_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getListComptes(ApplicationContext context, InfosFormulaire infosFormulaire) {

        XListCompte xListCompte = new XListCompte();

        log.debug("format : {}", infosFormulaire.getReportFormat());
        if (infosFormulaire.isCompteBloque() && infosFormulaire.isCompteCloture()) {
            xListCompte.setListCompte(getCompteByEtatAndStatut());
        } else {
            xListCompte.setListCompte(getCompteByEtat(infosFormulaire.isCompteCloture(), infosFormulaire.isCompteBloque()));
        }
        xListCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (xListCompte.getListCompte().size() > 0) {
            if (!infosFormulaire.isCompteBloque() && !infosFormulaire.isCompteCloture()) {
                xListCompte.setTitre("Liste des comptes bloqués ou clôturés");
            } else if (infosFormulaire.isCompteBloque() && !infosFormulaire.isCompteCloture()) {
                xListCompte.setTitre("Liste des comptes bloqués");
            } else {
                xListCompte.setTitre("Liste des comptes clôturés");
            }
        } else {
            xListCompte.setTitre("La liste des comptes bloqués ou clôturés est vide");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/compte_cloture_bloque_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/compte_cloture_bloque_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getListBeneficiaire(ApplicationContext context, InfosFormulaire infos) {

        XListBeneficiaire xListBeneficiaire = new XListBeneficiaire();
        List<XBeneficiaire> xBeneficiaires = getBeneficiaire(infos.getNumCompte(), infos.getDebut(), infos.getFin());
        if (xBeneficiaires.size() > 0) {
            xListBeneficiaire.setListBeneficiaire(xBeneficiaires);
            xListBeneficiaire.setNomClient(UtilService.nameToClient(compteRepository.findCompteByValeurCompteAndClientIsNotNull(infos.getNumCompte()).get().getClient()));
            xListBeneficiaire.setMontantTotal(xListBeneficiaire.getListBeneficiaire().stream().map(XBeneficiaire::getMontantOp).reduce(BigDecimal.ZERO, BigDecimal::add));
            xListBeneficiaire.setNumeroClient(infos.getNumTel());
            xListBeneficiaire.setNumeroCompte(infos.getNumCompte());

            xListBeneficiaire.setTitre("Liste des bénéficiaires du client ayant le N° " + xListBeneficiaire.getNumeroClient());
        } else {
            xListBeneficiaire.setTitre("La liste des bénéficiaires est vide");
        }

        xListBeneficiaire.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        /*        if (xListBeneficiaire.getListBeneficiaire().size() > 0){
            xListBeneficiaire.setTitre("Liste des bénéficiaires du client ayant le N° "+xListBeneficiaire.getNumeroClient());
        } else {
            xListBeneficiaire.setTitre("La liste des bénéficiaires est vide");
        }*/
        AbstractJasperReportsView view = ReportUtils.getJasperView(infos.getReportFormat());
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(infos.getReportFormat()) || ReportFormat.CSV.equals(infos.getReportFormat())) {
            view.setUrl("classpath:/report/liste_beneficiaire_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/liste_beneficiaire_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListBeneficiaire), false);
        params.put(PARAM_DATASOURCE, dataSource);
        log.debug("la liste: {}", xListBeneficiaire.getMontantTotal());
        log.info("la liste: {}", xListBeneficiaire.getMontantTotal());
        return new ModelAndView(view, params);

    }

    public ModelAndView getListUser(ApplicationContext context, InfosFormulaire infos) {

        List<XUser> xUsers = getUsers(null);
        XListUser xListUser = new XListUser();

        if (xUsers.size() > 0) {
            xListUser.setListUser(xUsers);
            xListUser.setTitre("Liste des utilisateurs");
        } else {
            xListUser.setTitre("La liste des utilisateurs est vide");
        }

        xListUser.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        /*        if (xListBeneficiaire.getListBeneficiaire().size() > 0){
            xListBeneficiaire.setTitre("Liste des bénéficiaires du client ayant le N° "+xListBeneficiaire.getNumeroClient());
        } else {
            xListBeneficiaire.setTitre("La liste des bénéficiaires est vide");
        }*/
        AbstractJasperReportsView view = ReportUtils.getJasperView(infos.getReportFormat());
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(infos.getReportFormat()) || ReportFormat.CSV.equals(infos.getReportFormat())) {
            view.setUrl("classpath:/report/liste_user_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/liste_user_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListUser), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);

    }

    public ModelAndView getListProfilWithRole(ApplicationContext context, InfosFormulaire infos) {
        infos.setReportFormat(ReportFormat.PDF);
        UserProfilDTO userProfilDTO = new UserProfilDTO();
        List<XProfilDTO> allProfils = new ArrayList<>();
        List<XProfilDTO> allProfilsBefore = new ArrayList<>();
        List<String> allRole = new ArrayList<>();
        XProfilDTO xProfilDTO = new XProfilDTO();
        List<Profil> profilList = profilRepository.findAll();
        for (Profil prof : profilList) {

            for (Authority authority : prof.getRoles()) {
                //allRole.add(authority.getName());
                xProfilDTO.setProfilLibelle(prof.getNom());
                xProfilDTO.setDescriptionRole(prof.getDescription());
                xProfilDTO.setNomRole(authority.getName());
                allProfilsBefore.add(xProfilDTO);
                xProfilDTO = new XProfilDTO();
            }
            allProfils.addAll(allProfilsBefore);
            //xProfilDTO.setRoles(allRole);

            xProfilDTO = new XProfilDTO();
        }

        if (profilList.size() > 0) {
            userProfilDTO.setListProfil(allProfils);
            userProfilDTO.setTitre("Liste des utilisateurs");
        } else {
            userProfilDTO.setTitre("La liste des utilisateurs est vide");
        }

        userProfilDTO.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        AbstractJasperReportsView view = ReportUtils.getJasperView(infos.getReportFormat());
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(infos.getReportFormat()) || ReportFormat.CSV.equals(infos.getReportFormat())) {
            view.setUrl("classpath:/report/liste_user_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/liste_profil_with_role_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(userProfilDTO), false);
        params.put(PARAM_DATASOURCE, dataSource);
        log.debug("*******************------Result----------*********");
        log.debug("*******************----------------*********" + userProfilDTO.toString());
        log.debug("*******************------Fin----------*********");
        log.debug("*******************------***----------*********" + userProfilDTO.getListProfil().toString());
        return new ModelAndView(view, params);
        //return null;

    }

    public ModelAndView getListOfUserNew(ApplicationContext context, XUserDTO xUserDTO) {
        List<XUser> xUsers = getUsers(xUserDTO);
        XListUser xListUser = new XListUser();

        if (xUsers.size() > 0) {
            xListUser.setListUser(xUsers);
            xListUser.setTitre("Liste des utilisateurs");
        } else {
            xListUser.setTitre("La liste des utilisateurs est vide");
        }

        xListUser.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(xUserDTO.getReportFormat()));
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(xUserDTO.getReportFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(xUserDTO.getReportFormat()))) {
            view.setUrl("classpath:/report/liste_user_new_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/liste_user_new_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListUser), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getListCompteInInstance(ApplicationContext context, InfosFormulaire infosFormulaire) {

        XListCompte xListCompte = new XListCompte();
        xListCompte.setListCompte(getCompteInInstance());
        xListCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (xListCompte.getListCompte().size() > 0) {
            xListCompte.setTitre("Liste des comptes en instance de validation");
        } else {
            xListCompte.setTitre("La liste des comptes en instance de validation est vide");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/compte_en_instance_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/compte_en_instance_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getListAuditEventOfProfil(ApplicationContext context, InfosFormulaire infosFormulaire) {

        XListAuditEvent xListAuditEvent = new XListAuditEvent();

        log.debug("format : {}", infosFormulaire.getReportFormat());
        xListAuditEvent.setListAuditEvent(getAuditEventForProfilEntity());
        xListAuditEvent.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        if (xListAuditEvent.getListAuditEvent().size() > 0) {
            xListAuditEvent.setTitre("Liste des modifications et créations des profils utilisateurs");
        } else {
            xListAuditEvent.setTitre("La liste des modifications et créations des profils utilisateurs est vide");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());
        Map<String, Object> params = new HashMap<>();
        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/audit_user_profil_xlsx.jasper");
        } else {
            view.setUrl("classpath:/report/audit_user_profil_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListAuditEvent), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public List<XAuditEvent> getAuditEventForProfilEntity() {

        List<XAuditEvent> xAuditEvents = new ArrayList<>();

        List<EntityAuditEvent> event = entityAuditEventRepository.findAllByEntityType("com.m2i.pti.domain.Profil");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

        event.forEach(ev -> {
            XAuditEvent xAuditEvent = new XAuditEvent();
            xAuditEvent.setAction(ev.getAction());
            xAuditEvent.setEntite(ev.getEntityType());
            xAuditEvent.setValeurEntite(ev.getEntityValue());
            xAuditEvent.setUser(ev.getModifiedBy());
            xAuditEvent.setAdresse(ev.getAdresse());
            xAuditEvent.setDateEnregistrement(ev.getCreatedDate() != null ? dateTimeFormatter.format(ev.getCreatedDate()) : null);
            xAuditEvents.add(xAuditEvent);
        });

        return xAuditEvents;
    }

    public ModelAndView getListCompteInactifInPeriode(ApplicationContext context, InfosFormulaire infosFormulaire) {

        XListCompte xListCompte = new XListCompte();
        xListCompte.setListCompte(getCompteInactifofPeriode());
        xListCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (xListCompte.getListCompte().size() > 0) {
            xListCompte.setTitre("Liste des comptes inactifs depuis 06 mois");
        } else {
            xListCompte.setTitre("La liste des comptes inactifs depuis 06 mois est vide");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/compte_inactif_pendant_une_periode_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/compte_inactif_pendant_une_periode_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public List<XCompteDTO> getCompteInactifofPeriode() {
        List<Compte> comptes = compteRepository.findByClientIsNotNullAndClientClientIdentifieIsTrueAndClientSupprimeIsFalse();
        List<XCompteDTO> xCompteDTOS = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        if (!comptes.isEmpty()) {
            comptes.forEach(compte -> {
                log.debug("compte id id : {}", compte.getId());
                Operation operationLast = operationRepository.findTop1ByCompteSrcValeurCompteOrCompteDestValeurCompteOrderByDateOperationDesc(compte.getValeurCompte(), compte.getValeurCompte());
                if (operationLast != null) {
                    log.debug("operation id : {}", operationLast.getId());
                    log.debug("Date fin : {}", ZonedDateTime.now());
//                  long duree = ChronoUnit.DAYS.between(operationLast.getDateOperation(), ZonedDateTime.now().plusDays(150 + 180)); // 150+180 juste pour le teste
                    long duree = ChronoUnit.DAYS.between(operationLast.getDateOperation(), ZonedDateTime.now());
                    if (duree > 180) {
                        XCompteDTO xCompteDTO = new XCompteDTO();
                        xCompteDTO.setNomPrenom(UtilService.nameToClient(compte));
                        xCompteDTO.setNumeroCompte(compte.getValeurCompte());
                        xCompteDTO.setNumeroTel(compte.getClient().getPersonne().getTelephone());
                        xCompteDTO.setDateDernierOp(dateTimeFormatter.format(operationLast.getDateOperation()));
                        xCompteDTO.setDuree(UtilService.formatDureInYearMoisJour(duree));
                        xCompteDTOS.add(xCompteDTO);
                        log.debug("Taille xCompteDTOS : {}", xCompteDTOS.size());
                    }
                }
            });
        }
        log.debug("Taille xCompteDTOS final : {}", xCompteDTOS.size());
        return xCompteDTOS;
    }

    public ModelAndView getListTransactionPending(ApplicationContext context, InfosFormulaire infosFormulaire) {

        XListTransactionCompte xListTransactionCompte = new XListTransactionCompte();

        log.debug("format : {}", infosFormulaire.getReportFormat());
        xListTransactionCompte.setListTransaction(getTransactionPending());
        xListTransactionCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        if (xListTransactionCompte.getListTransaction().size() > 0) {
            xListTransactionCompte.setTitre("Liste des transactions provisoires");
        } else {
            xListTransactionCompte.setTitre("La liste des transactions provisoires est vide");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());
        Map<String, Object> params = new HashMap<>();
        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/transaction_en_pending_xlsx.jasper");
        } else {
            view.setUrl("classpath:/report/transaction_en_pending_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListTransactionCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public List<XTransactionCompteDTO> getTransactionPending() {
        List<TransactionComptePti> t = transactionComptePtiRepository.findAllByEtat(EtatValidite.PROVISOIRE);
        List<XTransactionCompteDTO> xT = new ArrayList<>();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        if (!t.isEmpty()) {
            t.forEach(trans -> {
                XTransactionCompteDTO xTrans = new XTransactionCompteDTO();

                xTrans.setDate(dateTimeFormatter.format(trans.getDate()));
                xTrans.setCodeOperation(trans.getOperation().getCodeOperation());
                xTrans.setCompteSrc(trans.getOperation().getCompteSrc() != null ? trans.getOperation().getCompteSrc().getValeurCompte() : null);
                xTrans.setCompteDest(trans.getOperation().getCompteDest() != null ? trans.getOperation().getCompteDest().getValeurCompte() : null);
                xTrans.setSoldeCourant(trans.getSoldeCourant());
                xTrans.setMontant(trans.getMontant());
                xTrans.setSoldeApres(trans.getSoldeApres());
                xTrans.setType(trans.getType().toString());

                xT.add(xTrans);
            });
        }
        return xT;
    }

    public ModelAndView getUserWithConnexionDenied(ApplicationContext context, InfosFormulaire infosFormulaire) {

        XListUserHistorique xListUserHistorique = new XListUserHistorique();

        log.debug("format : {}", infosFormulaire.getReportFormat());
        xListUserHistorique.setListUsers(getUserConnexionDenied());
        xListUserHistorique.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        if (xListUserHistorique.getListUsers().size() > 0) {
            xListUserHistorique.setTitre("Liste des tentatives de connexion échouées");
        } else {
            xListUserHistorique.setTitre("La liste des tentatives de connexion échouées est vide");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());
        Map<String, Object> params = new HashMap<>();
        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/tentative_connexion_echouee_xlsx.jasper");
        } else {
            view.setUrl("classpath:/report/tentative_connexion_echouee_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListUserHistorique), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public List<XUserHistorique> getUserConnexionDenied() {

        List<XUserHistorique> xUserHistoriques = new ArrayList<>();

        List<EntityAuditEvent> historic = entityAuditEventRepository.findAllByAction("AUTHORIZATION_FAILURE");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

        historic.forEach(entityAuditEvent -> {
            XUserHistorique xUserHistoric = new XUserHistorique();
            xUserHistoric.setAuteur(entityAuditEvent.getModifiedBy());
            xUserHistoric.setDate(entityAuditEvent.getModifiedDate() != null ? dateTimeFormatter.format(entityAuditEvent.getModifiedDate()) : null);
            xUserHistoric.setAdresseIp(entityAuditEvent.getAdresse());

            xUserHistoriques.add(xUserHistoric);
        });

        return xUserHistoriques;
    }

    public ModelAndView getListUserProfil(ApplicationContext context, InfosFormulaire infosFormulaire) {

        XListUserProfil xListUserProfil = new XListUserProfil();

        log.debug("format : {}", infosFormulaire.getReportFormat());
        xListUserProfil.setListProfils(getUserProfil());
        xListUserProfil.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        if (xListUserProfil.getListProfils().size() > 0) {
            xListUserProfil.setTitre("Liste des profils des utilisateurs");
        } else {
            xListUserProfil.setTitre("La liste des profils des utilisateurs est vide");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());
        Map<String, Object> params = new HashMap<>();
        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/profil_user_xlsx.jasper");
        } else {
            view.setUrl("classpath:/report/profil_user_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListUserProfil), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public List<XUserProfil> getUserProfil() {

        List<XUserProfil> xUserProfils = new ArrayList<>();

        List<Profil> profils = profilRepository.findAll();

        profils.forEach(profil -> {
            XUserProfil xUserProfil = new XUserProfil();
            xUserProfil.setNomProfil(profil.getNom());
            xUserProfil.setDescription(profil.getDescription());
            xUserProfils.add(xUserProfil);
        });

        return xUserProfils;
    }

    public ModelAndView getListAuditEvent(ApplicationContext context, InfosFormulaire infosFormulaire) {

        XListAuditEvent xListAuditEvent = new XListAuditEvent();

        log.debug("format : {}", infosFormulaire.getReportFormat());
        xListAuditEvent.setListAuditEvent(getAuditEvent());
        xListAuditEvent.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        if (xListAuditEvent.getListAuditEvent().size() > 0) {
            xListAuditEvent.setTitre("Rapport d'activité des utilisateurs");
        } else {
            xListAuditEvent.setTitre("Le rapport d'activité des utilisateurs est vide");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());
        Map<String, Object> params = new HashMap<>();
        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/rapport_user_activity_xlsx.jasper");
        } else {
            view.setUrl("classpath:/report/rapport_user_activity_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListAuditEvent), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public List<XAuditEvent> getAuditEvent() {

        List<XAuditEvent> xAuditEvents = new ArrayList<>();

        List<EntityAuditEvent> event = entityAuditEventRepository.findAll();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

        event.forEach(ev -> {
            XAuditEvent xAuditEvent = new XAuditEvent();
            xAuditEvent.setAction(ev.getAction());
            xAuditEvent.setEntite(ev.getEntityType());
            xAuditEvent.setValeurEntite(ev.getEntityValue());
            xAuditEvent.setUser(ev.getModifiedBy());
            xAuditEvent.setAdresse(ev.getAdresse());
            xAuditEvent.setDateEnregistrement(ev.getCreatedDate() != null ? dateTimeFormatter.format(ev.getCreatedDate()) : null);
            xAuditEvents.add(xAuditEvent);
        });

        return xAuditEvents;
    }

    public List<XTransactionCompteDTO> getJournalTransactions(ZonedDateTime date) {
        List<TransactionComptePti> t = transactionComptePtiRepository.findAllByDateBetween(date, date.plusDays(1));
        List<XTransactionCompteDTO> xT = new ArrayList<>();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        if (!t.isEmpty()) {
            t.forEach(trans -> {
                XTransactionCompteDTO xTrans = new XTransactionCompteDTO();

                xTrans.setDate(dateTimeFormatter.format(trans.getDate()));
                xTrans.setCodeOperation(trans.getOperation().getCodeOperation());
                xTrans.setCompteSrc(trans.getOperation().getCompteSrc() != null ? trans.getOperation().getCompteSrc().getValeurCompte() : null);
                xTrans.setCompteDest(trans.getOperation().getCompteDest() != null ? trans.getOperation().getCompteDest().getValeurCompte() : null);
                xTrans.setSoldeCourant(trans.getSoldeCourant());
                xTrans.setMontant(trans.getMontant());
                xTrans.setSoldeApres(trans.getSoldeApres());
                xTrans.setType(trans.getType().toString());

                xT.add(xTrans);
            });
        }
        return xT;
    }

    public ModelAndView getJournalTransaction(ApplicationContext context, InfosFormulaire infosFormulaire) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy").withZone(ZoneId.systemDefault());
        XListTransactionCompte xListTransactionCompte = new XListTransactionCompte();

        log.debug("format : {}", infosFormulaire.getReportFormat());
        xListTransactionCompte.setListTransaction(getJournalTransactions(infosFormulaire.getDebut()));
        xListTransactionCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        if (xListTransactionCompte.getListTransaction().size() > 0) {
            xListTransactionCompte.setTitre("Journal des transactions du " + dateTimeFormatter.format(infosFormulaire.getDebut()));
        } else {
            xListTransactionCompte.setTitre("Le journal des transactions du " + dateTimeFormatter.format(infosFormulaire.getDebut()) + " est vide");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());
        Map<String, Object> params = new HashMap<>();
        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/journal_transaction_xlsx.jasper");
        } else {
            view.setUrl("classpath:/report/journal_transaction_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListTransactionCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getEtatCompte(ApplicationContext applicationContext, EtatParams etatParams) {
        String etatCompte = "ETATCOMPTE";
        return getListingFiltreOptimise(applicationContext, null, null, null, etatParams.getTypeClient(), null, null, null, null, etatParams.getFormat(), etatCompte, etatParams.getDateDebut(), etatParams.getDateFin(), etatParams.getStatut());
    }

    public List<Compte> getCptOfUo(String typeUO) {
        List<Compte> listCompte = null;
        List<UniteOrganisationDTO> listUO = uniteOrganisationService.findAllByTypeUo(TypeUO.MASTER);

        for (UniteOrganisationDTO uoList : listUO) {
            UniteOrganisation uo = uniteOrganisationMapper.toEntity(uoList);
            listCompte = compteService.findByUoId(uo);
        }
        return listCompte;
    }

    @Transactional(readOnly = true)
    public StringBuilder exportLotOld(LotDTO lotDTO) {
        // DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        // Date date = new Date();
        final String keyDirectory = (SystemUtils.IS_OS_LINUX ? qrCodePathLinux : qrCodePathWindows);
        if (lotDTO.getId() != null) {
            // LotDTO lotDTO = lotService.findOne(id);
            if (SystemUtils.IS_OS_WINDOWS) {
                log.debug("=============================== keyDirectory =============== {}", keyDirectory);
                String finalDirectory = keyDirectory.replace("\\", "/");
                log.debug("=============================== finalDirectory ================= {}", finalDirectory);
                //pathname = finalDirectory +"/lot"+lotDTO.getId();
                if(lotDTO.getTypeActeur().equals("ORGANISATION")){
                    pathname = System.getProperty("user.home") + "/organisation/lot" + lotDTO.getId();
                } else {
                    pathname = System.getProperty("user.home") + "/client/lot" + lotDTO.getId();
                }
            } else {
                //pathname = keyDirectory +"/lot"+lotDTO.getId();
                if(lotDTO.getTypeActeur().equals("ORGANISATION")){
                    pathname = System.getProperty("user.home") + "/organisation/lot" + lotDTO.getId();
                } else {
                    pathname = System.getProperty("user.home") + "/client/lot" + lotDTO.getId();
                }
            }

            // pathname = System.getProperty("user.home")+"/client/lot"+lotDTO.getId();
            File createDirectory = new File(pathname);
            if (!createDirectory.exists()) {
                createDirectory.mkdirs();
                log.debug("chemin-------" + createDirectory.getAbsolutePath());
            }

            List<DetailsLotDTO> list = new ArrayList<>();
            list = detailsLotRepository.findDetailsLotByLotIdOptimiser(lotDTO.getId());

            XLot xLot = new XLot();

            if (list != null) {
                List<XDetailsLot> xDetailsLotList = new ArrayList<>();
                for (DetailsLotDTO detailsLot : list) {
                    XDetailsLot xDetailsLot = new XDetailsLot();

                    if (detailsLot.getClientId() != null) {
                        ClientDTO clientDTO = clientRepository.findClientByClientIdOptimise(detailsLot.getClientId());
                        if (clientDTO != null) {
                            xDetailsLot.setCodeClient(clientDTO.getCodeClient());
                            if (clientDTO.getPersonneId() != null) {
                                PersonneDTO personneDTO = personneRepository.findPersonneById(clientDTO.getPersonneId());
                                if (personneDTO != null) {
                                    PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                                    xDetailsLot.setQr(personneDTO.getTelephone() + "_qrcode");
                                    if (personnePhysiqueDTO != null) {
                                        xDetailsLot.setNomClient(personnePhysiqueDTO.getNomClient());
                                        xDetailsLot.setPrenomClient(personnePhysiqueDTO.getPrenomClient());
                                    }
                                }
                            }
                        }
                    }else if(detailsLot.getLigneChargementFichierId()!=null){
                        LigneChargementFichier ligneChargementFichier=ligneChargementFichierRepository.findById(detailsLot.getLigneChargementFichierId()).orElse(null);
                        if(ligneChargementFichier!=null){
                            xDetailsLot.setLibelle(ligneChargementFichier.getNom());
                            xDetailsLot.setIdentifiant(ligneChargementFichier.getIdentifiant());
                            xDetailsLot.setTelephone(ligneChargementFichier.getTelephone());
                            if(ligneChargementFichier.getIdentifiant()!=null){
                                UniteOrganisationDTO agent=uniteOrganisationRepository.findByCodeUOAndSupprimeIsFalse(ligneChargementFichier.getIdentifiant(),false);
                                if(agent!=null){
                                    List<PointVenteDTO> pv=pointVenteRepository.findByUniteOrganisationIdOptimise(agent.getId());
                                    xDetailsLot.setPointDeVente(pv.get(0).getCodePointVente());
                                }
                            }
                        }
                    }
                    xDetailsLotList.add(xDetailsLot);
                }
                xLot.setxDetailsLots(xDetailsLotList);

                if (!list.isEmpty()) {
                    try {
                        JasperReport jasperReport;
                        Resource resource;
                        if(lotDTO.getTypeActeur().equals("ORGANISATION")){
                            resource = resourceLoader.getResource("classpath:/report/liste_DLot_Organisation_xlsx.jasper");
                        } else {
                            resource = resourceLoader.getResource("classpath:/report/liste_DLot_xlsx.jasper");
                        }
                        InputStream dbastream;
                        log.debug("****ok***");
                        //File file= ResourceUtils.getFile("classpath:liste_DLot_xlsx.jasper");
                        log.debug("****ok***");
                        if (resource.exists()) {
                            dbastream = resource.getInputStream();
                            jasperReport = (JasperReport) JRLoader.loadObject(dbastream);
                        } else {
                            log.debug("****ok***");
                            if(lotDTO.getTypeActeur().equals("ORGANISATION")){
                                resource = resourceLoader.getResource("classpath:/report/liste_DLot_Organisation_xlsx.jrxml");
                            }else {
                                resource = resourceLoader.getResource("classpath:/report/liste_DLot_xlsx.jrxml");
                            }
                            dbastream = resource.getInputStream();
                            // File file1= ResourceUtils.getFile("classpath:liste_DLot_xlsx.jrxml");
                            jasperReport = JasperCompileManager.compileReport(dbastream);
                            JRSaver.saveObject(jasperReport, "detailsLot.jasper");
                        }

                        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xLot));
                        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, dataSource);
                        JRCsvExporter exporter = new JRCsvExporter();

                        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                        java.lang.StringBuilder sbuffer = new java.lang.StringBuilder();
                        exporter.setExporterOutput(new SimpleWriterExporterOutput(sbuffer));
                        File desttination=null;
                        if(lotDTO.getTypeActeur().equals("ORGANISATION")){
                            desttination = new File(pathname + "/organisations_lot.csv");
                        }else {
                            desttination = new File(pathname + "/clients_lot.csv");
                        }
                        desttination.createNewFile();
                        exporter.setExporterOutput(new SimpleWriterExporterOutput(desttination));
                        for (DetailsLotDTO dLot : list) {
                            DetailsLot updateLot ;
                            Compte compte=new Compte();
                            Client cli;
                            ClientDTO client=new ClientDTO();
                            //List <CompteDTO> listCpt= compteRepository.findByClientIdOptimise(client.getId());
                            if(dLot.getLigneChargementFichierId()!=null){
                                updateLot=detailsLotRepository.findDetailsLotByLigneFichierIdAndLotId(dLot.getLigneChargementFichierId(), dLot.getLotId());
                            }else {
                                client = clientRepository.findClientByClientIdOptimise(dLot.getClientId());
                                cli = clientMapper.toEntity(client);
                                compte = compteService.findComptePrincipal(cli);
                                updateLot = detailsLotRepository.findDetailsLotByClientIdAndLotId(dLot.getClientId(), dLot.getLotId());
                            }
                            updateLot.getQrCode();
                            byte[] bytes ;
                            if(dLot.getLigneChargementFichierId()!=null){
                                bytes = detailsLotService.getQRCodeImage(detailsLotService.xQrOrganisationCode(dLot.getLigneChargementFichierId()).getQrCodeData(), 300, 300);
                            }else {
                                updateLot.getClient().getPersonne().getTelephone().trim();
                                bytes = detailsLotService.getQRCodeImage(detailsLotService.qrClientlot(client, compte).getQrCodeData(), 300, 300);
                            }
                            log.debug("**************bytes************",bytes);
                            if (bytes != null && bytes.length > 0) {
                                updateLot.setQrCode(bytes);
                                detailsLotRepository.save(updateLot);
                            }
                            transformToPmg(updateLot, updateLot.getQrCode(), "qrCode", pathname);

                        }
                        exporter.exportReport();
                        return sbuffer;
                    } catch (Exception e) {
                        log.debug("impossible de generer le fichier lot", e.getMessage());
                        log.debug("Message error" + e.getLocalizedMessage());
                        log.debug("**************************", e.getCause());
                        log.debug("****--------------*********" + e.toString());
                    }
                }
            }
        }
        return null;
    }
    @Transactional(readOnly = true)
    public StringBuilder exportLot(LotDTO lotDTO) {
        // DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        // Date date = new Date();
        final String keyDirectory = (SystemUtils.IS_OS_LINUX ? qrCodePathLinux : qrCodePathWindows);
        if (lotDTO.getId() != null) {
            // LotDTO lotDTO = lotService.findOne(id);
            if (SystemUtils.IS_OS_WINDOWS) {
                log.debug("=============================== keyDirectory =============== {}", keyDirectory);
                String finalDirectory = keyDirectory.replace("\\", "/");
                log.debug("=============================== finalDirectory ================= {}", finalDirectory);
                //pathname = finalDirectory +"/lot"+lotDTO.getId();
                if(lotDTO.getTypeActeur()!=null && lotDTO.getTypeActeur().equals("ORGANISATION")){
                    pathname = System.getProperty("user.home") + "/organisation/lot" + lotDTO.getId();
                } else {
                    pathname = System.getProperty("user.home") + "/client/lot" + lotDTO.getId();
                }
            } else {
                //pathname = keyDirectory +"/lot"+lotDTO.getId();
                if(lotDTO.getTypeActeur()!=null && lotDTO.getTypeActeur().equals("ORGANISATION")){
                    pathname = System.getProperty("user.home") + "/organisation/lot" + lotDTO.getId();
                } else {
                    pathname = System.getProperty("user.home") + "/client/lot" + lotDTO.getId();
                }
            }

            // pathname = System.getProperty("user.home")+"/client/lot"+lotDTO.getId();
            File createDirectory = new File(pathname);
            if (!createDirectory.exists()) {
                createDirectory.mkdirs();
                log.debug("chemin-------" + createDirectory.getAbsolutePath());
            }

            List<DetailsLotDTO> list = new ArrayList<>();
            list = detailsLotRepository.findDetailsLotByLotIdOptimiser(lotDTO.getId());

            XLot xLot = new XLot();

            if (list != null) {
                List<XDetailsLot> xDetailsLotList = new ArrayList<>();
                for (DetailsLotDTO detailsLot : list) {
                    XDetailsLot xDetailsLot = new XDetailsLot();

                    if (detailsLot.getClientId() != null) {
                        ClientDTO clientDTO = clientRepository.findClientByClientIdOptimise(detailsLot.getClientId());
                        if (clientDTO != null) {
                            xDetailsLot.setCodeClient(clientDTO.getCodeClient());
                            if (clientDTO.getPersonneId() != null) {
                                PersonneDTO personneDTO = personneRepository.findPersonneById(clientDTO.getPersonneId());
                                if (personneDTO != null) {
                                    PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                                    xDetailsLot.setQr(personneDTO.getTelephone() + "_qrcode");
                                    if (personnePhysiqueDTO != null) {
                                        xDetailsLot.setNomClient(personnePhysiqueDTO.getNomClient());
                                        xDetailsLot.setPrenomClient(personnePhysiqueDTO.getPrenomClient());
                                    }
                                }
                            }
                        }
                    }else if(detailsLot.getLigneChargementFichierId()!=null){
                        LigneChargementFichier ligneChargementFichier=ligneChargementFichierRepository.findById(detailsLot.getLigneChargementFichierId()).orElse(null);
                        if(ligneChargementFichier!=null){
                            xDetailsLot.setLibelle(ligneChargementFichier.getNom());
                           xDetailsLot.setIdentifiant(ligneChargementFichier.getIdentifiant());
                            xDetailsLot.setTelephone(ligneChargementFichier.getTelephone());
                            if(ligneChargementFichier.getIdentifiant()!=null){
                                UniteOrganisationDTO agent=uniteOrganisationRepository.findByCodeUOAndSupprimeIsFalse(ligneChargementFichier.getIdentifiant(),false);
                                if(agent!=null){
                                    List<PointVenteDTO> pv=pointVenteRepository.findByUniteOrganisationIdOptimise(agent.getId());
                                    xDetailsLot.setPointDeVente(pv.get(0).getCodePointVente());
                                }
                            }else {
                                TypeUniteOrganisation typeUniteOrganisation=typeUniteOrganisationRepository.findByLibelleTypeUO(ligneChargementFichier.getType());
                                List<UniteOrganisation> Uo=uniteOrganisationRepository.findByLibelleUniteOrganisationAndTypeUOOrderByIdDesc(ligneChargementFichier.getNom(),typeUniteOrganisation);
                                if(Uo!=null&&Uo.size() >0){
                                    xDetailsLot.setIdentifiant(Uo.get(0).getCodeUO());
                                    xDetailsLot.setLibelle(Uo.get(0).getLibelleUniteOrganisation());
                                    List<PointVenteDTO> pv=pointVenteRepository.findByUniteOrganisationIdOptimise(Uo.get(0).getId());
                                    xDetailsLot.setPointDeVente(pv.get(0).getCodePointVente());

                                }
                            }
                        }
                    }
                    xDetailsLotList.add(xDetailsLot);
                }
                xLot.setxDetailsLots(xDetailsLotList);

                if (!list.isEmpty()) {
                    try {
                        JasperReport jasperReport;
                        Resource resource;
                        if(lotDTO.getTypeActeur()!=null && lotDTO.getTypeActeur().equals("ORGANISATION")){
                            resource = resourceLoader.getResource("classpath:/report/liste_DLot_Organisation_xlsx.jasper");
                        } else {
                            resource = resourceLoader.getResource("classpath:/report/liste_DLot_xlsx.jasper");
                        }
                        InputStream dbastream;
                        log.debug("****ok***");
                        //File file= ResourceUtils.getFile("classpath:liste_DLot_xlsx.jasper");
                        log.debug("****ok***");
                        if (resource.exists()) {
                            dbastream = resource.getInputStream();
                            jasperReport = (JasperReport) JRLoader.loadObject(dbastream);
                        } else {
                            log.debug("****ok***");
                            if(lotDTO.getTypeActeur()!=null && lotDTO.getTypeActeur().equals("ORGANISATION")){
                                resource = resourceLoader.getResource("classpath:/report/liste_DLot_Organisation_xlsx.jrxml");
                            }else {
                                resource = resourceLoader.getResource("classpath:/report/liste_DLot_xlsx.jrxml");
                            }
                            dbastream = resource.getInputStream();
                            // File file1= ResourceUtils.getFile("classpath:liste_DLot_xlsx.jrxml");
                            jasperReport = JasperCompileManager.compileReport(dbastream);
                            JRSaver.saveObject(jasperReport, "detailsLot.jasper");
                        }

                        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xLot));
                        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, null, dataSource);
                        JRCsvExporter exporter = new JRCsvExporter();

                        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                        java.lang.StringBuilder sbuffer = new java.lang.StringBuilder();
                        exporter.setExporterOutput(new SimpleWriterExporterOutput(sbuffer));
                        File desttination=null;
                        if(lotDTO.getTypeActeur()!=null && lotDTO.getTypeActeur().equals("ORGANISATION")){
                            desttination = new File(pathname + "/organisations_lot.csv");
                        }else {
                            desttination = new File(pathname + "/clients_lot.csv");
                        }
                        desttination.createNewFile();
                        exporter.setExporterOutput(new SimpleWriterExporterOutput(desttination));
                        for (DetailsLotDTO dLot : list) {
                            DetailsLot updateLot ;
                            Compte compte=new Compte();
                            Client cli;
                            ClientDTO client=new ClientDTO();
                            //List <CompteDTO> listCpt= compteRepository.findByClientIdOptimise(client.getId());
                            if(dLot.getLigneChargementFichierId()!=null){
                                updateLot=detailsLotRepository.findDetailsLotByLigneFichierIdAndLotId(dLot.getLigneChargementFichierId(), dLot.getLotId());
                            }else {
                                client = clientRepository.findClientByClientIdOptimise(dLot.getClientId());
                                cli = clientMapper.toEntity(client);
                                compte = compteService.findComptePrincipal(cli);
                                updateLot = detailsLotRepository.findDetailsLotByClientIdAndLotId(dLot.getClientId(), dLot.getLotId());
                            }
                            updateLot.getQrCode();
                            byte[] bytes ;
                            if(dLot.getLigneChargementFichierId()!=null){
                                bytes = detailsLotService.getQRCodeImage(detailsLotService.xQrOrganisationCode(dLot.getLigneChargementFichierId()).getQrCodeData(), 300, 300);
                            }else {
                                updateLot.getClient().getPersonne().getTelephone().trim();
                                bytes = detailsLotService.getQRCodeImage(detailsLotService.qrClientlot(client, compte).getQrCodeData(), 300, 300);
                            }
                            log.debug("**************bytes************",bytes);
                            if (bytes != null && bytes.length > 0) {
                                updateLot.setQrCode(bytes);
                                detailsLotRepository.save(updateLot);
                            }
                            transformToPmg(updateLot, updateLot.getQrCode(), "qrCode", pathname);

                        }
                        exporter.exportReport();
                        return sbuffer;
                    } catch (Exception e) {
                        log.debug("impossible de generer le fichier lot", e.getMessage());
                        log.debug("Message error" + e.getLocalizedMessage());
                        log.debug("**************************", e.getCause());
                        log.debug("****--------------*********" + e.toString());
                    }
                }
            }
        }
        return null;
    }

    public void transformToPmg(DetailsLot details, byte[] objet, String type, String destination) {
        String ft = "png";
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(objet);
            Iterator<?> readers = ImageIO.getImageReadersByFormatName(ft);
            ImageReader reader = (ImageReader) readers.next();
            Object source = bis;
            ImageInputStream iis = ImageIO.createImageInputStream(source);
            reader.setInput(iis, true);
            ImageReadParam param = reader.getDefaultReadParam();
            BufferedImage image = reader.read(0, param);
            BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = bufferedImage.createGraphics();
            g2.drawImage(image, null, null);
            File imageFile ;
            if(details.getLigneChargementFichier()!=null){
                imageFile = new File(destination + "/" + details.getLigneChargementFichier().getIdentifiant() + "_qrcode.png");
            }else {
                imageFile = new File(destination + "/" + details.getClient().getPersonne().getTelephone() + "_qrcode.png");
            }
            imageFile.createNewFile();
            ImageIO.write(bufferedImage, "png", imageFile);

        } catch (Exception e) {
            log.debug("impossible de generer le fichier PNG", e.getMessage());

        }
    }

    public ModelAndView getListingFiltreOptimise(ApplicationContext context, String telephone, String code, String addresse, String type, String nom, String prenom, String raisonSociale, String numeroPiece, String format, String etatCompte, ZonedDateTime dateDebut, ZonedDateTime dateFin, String statut) {
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        XClientDTO xClientDTO = new XClientDTO();
        List<ListeClientDTO> xListe = new ArrayList<>();
        String titre = null;
        int max;
        int nbrPage;
        if (type.equals("PHYSIQUE")) {

            if (statut.equals("ETAT") && dateDebut != null) {
                max = clientRepository.countClientPhysiqueByCriteriaEtat(code, telephone, addresse, nom, prenom, dateDebut, dateFin);
            } else {
                max = clientRepository.countClientPhysiqueByCriteria(code, telephone, addresse, nom, prenom, dateDebut, dateFin);
            }
            nbrPage = (max / 1000);
            for (int j = 0; j <= nbrPage; j++) {
                Pageable page = PageRequest.of(j, 1000, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
                Long taille = new Long(page.getPageSize());
                Long pag = new Long(page.getPageSize() * page.getPageNumber());
                xListe.addAll(clientService.findByTypePersonneReportOptimise(TypePersonne.PHYSIQUE, code, telephone, addresse, nom, prenom, raisonSociale, numeroPiece, dateDebut, dateFin, taille, pag));
            }
        } else if (type.equals("MORALE")) {
            if (statut.equals("ETAT") && dateDebut != null) {
                max = clientRepository.countClientMoraleByCriteriaEtat(code, telephone, addresse, raisonSociale, dateDebut, dateFin);
            } else {
                max = clientRepository.countClientMoraleByCriteria(code, telephone, addresse, raisonSociale, dateDebut, dateFin);
            }
            nbrPage = (max / 200);
            for (int j = 0; j <= nbrPage; j++) {
                Pageable page = PageRequest.of(j, 200, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
                Long taille = new Long(page.getPageSize());
                Long pag = new Long(page.getPageSize() * page.getPageNumber());
                xListe.addAll(clientService.findByTypePersonneReportOptimise(TypePersonne.MORALE, code, telephone, addresse, nom, prenom, raisonSociale, numeroPiece, dateDebut, dateFin, taille, pag));
            }
        }
        titre = getTitreWithDate(dateDebut, dateFin);
        xClientDTO.setListeClient(xListe);
        xClientDTO.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        xClientDTO.setFmt(format);
        xClientDTO.setTitre(titre);
        if (type.equals("MORALE")) {
            if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
                if (etatCompte != null) {
                    view.setUrl("classpath:/report/etat_compte_clientEntrp_xlsx.jasper");
                } else {
                    view.setUrl("classpath:/report/liste_clientEntrp_xlsx.jasper");
                }

            } else {
                if (etatCompte != null) {
                    view.setUrl("classpath:/report/etat_compte_clientEntrp_pdf.jasper");
                } else {
                    view.setUrl("classpath:/report/liste_clientEntrp_pdf.jasper");
                }
            }
        }
        if (type.equals("PHYSIQUE")) {
            if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
                if (etatCompte != null) {
                    view.setUrl("classpath:/report/etat_compte_client_xlsx.jasper");
                } else {
                    view.setUrl("classpath:/report/liste_client_xlsx.jasper");
                }

            } else {
                if (etatCompte != null) {
                    view.setUrl("classpath:/report/etat_compte_client_pdf.jasper");
                } else {
                    view.setUrl("classpath:/report/liste_client_pdf.jasper");
                }
            }
        }
        log.debug("--------------*****---------" + titre);
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xClientDTO), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getComptesMonnaieElectroniquePeriode(ApplicationContext context, EtatParams cli) {
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(cli.getFormat()));
        Map<String, Object> params = new HashMap<>();
        List<CompteDTO> compteList = new ArrayList<>();
        XClientDTO xClientDTO = new XClientDTO();
        List<XCompteMonnaieElectro> xListe = new ArrayList<>();

        if (cli.getDateDebut() != null && cli.getDateFin() != null) {
            compteList = compteRepository.findValeurCompteAndClientIdPeriode(cli.getDateDebut(), cli.getDateFin());
        } else if (cli.getDateDebut() != null) {
            compteList = compteRepository.findValeurCompteAndClientIdPeriode(cli.getDateDebut(), cli.getDateDebut().plusHours(23));
        } else if (cli.getDateFin() != null) {
            compteList = compteRepository.findValeurCompteAndClientIdPeriode(ZonedDateTime.now().minusMonths(12), cli.getDateFin().plusHours(23));
        }

        String titre = getTitreWithDate(cli.getDateDebut(), cli.getDateFin());

        if (!compteList.isEmpty()) {
            compteList.forEach(cpt->{
                XCompteMonnaieElectro xCompteMonnaieElectro = new XCompteMonnaieElectro();
                ClientDTO c = clientRepository.findClientByClientIdOptimise(cpt.getClientId());
                if (c != null) {
                    if (c.getPersonneId() != null) {
                        PersonneDTO personneDTO = personneRepository.findPersonneById(c.getPersonneId());
                        if (personneDTO.getPersonnePhysiqueId() != null) {
                            PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                            xCompteMonnaieElectro.setNomClient(personnePhysiqueDTO.getNomClient() + " " + personnePhysiqueDTO.getPrenomClient());
                            // xCompteMonnaieElectro.setPrenomClient(c.getPersonne().getPersonnePhysique().getPrenomClient());
                            if (personnePhysiqueDTO.getNationaliteId() != null) {

                                xCompteMonnaieElectro.setNationalite(personnePhysiqueDTO.getNationaliteLibelle());
                            }
                        }
                        xCompteMonnaieElectro.setTelephone(personneDTO.getTelephone());
                        ZoneId zone = ZoneId.systemDefault();
                        if(c.getCreatedDate()!=null){
                            ZonedDateTime zdt = c.getCreatedDate().atZone(zone);
                            xCompteMonnaieElectro.setDateCreation(formatDateOnlyWithTime(zdt));
                        }
                    }
                }
                xCompteMonnaieElectro.setValeurCompte(cpt.getValeurCompte());

                if (cpt.getCategorieCompte() != null) {
                    xCompteMonnaieElectro.setCategorieCompte(cpt.getCategorieCompte().toString());
                }

                if (cpt.getStatutCompte() != null) {
                    xCompteMonnaieElectro.setStatutCompte(cpt.getStatutCompte().toString());
                }

                if (cpt.getEtatCompte() != null) {
                    xCompteMonnaieElectro.setEtatCompte(cpt.getEtatCompte().toString());
                }

                xListe.add(xCompteMonnaieElectro);
                xCompteMonnaieElectro = new XCompteMonnaieElectro();
            });
        }

        xClientDTO.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        xClientDTO.setListeCompteMonnaieElectros(xListe);
        xClientDTO.setFmt(cli.getFormat());
        xClientDTO.setTitre("TOTAL COMPTES DE MONNAIE ELECTRONIQUE" + titre);
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(cli.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(cli.getFormat()))) {
            view.setUrl("classpath:/report/liste_compte_monnaie_electro_xlsx.jrxml");

        } else {
            // view.setUrl("classpath:/report/liste_compte_client_pdf.jasper");
            view.setUrl("classpath:/report/liste_compte_monnaie_electro_pdf.jrxml");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xClientDTO), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getTotalTitulairesComptes(ApplicationContext context, EtatParams cli) {
        XListCompte xListCompte = new XListCompte();
        List<CompteDTO> compteDTOList = new ArrayList<>();
        List<XTitulaireCompteDTO> compteList = new ArrayList<>();

        if (cli.getDateDebut() != null && cli.getDateFin() != null) {
            compteDTOList = compteRepository.findValeurCompteAndClientIdPeriode(cli.getDateDebut(), cli.getDateFin());
        } else if (cli.getDateDebut() != null) {
            compteDTOList = compteRepository.findValeurCompteAndClientIdPeriode(cli.getDateDebut(), cli.getDateDebut().plusHours(23));
        } else if (cli.getDateFin() != null) {
            compteDTOList = compteRepository.findValeurCompteAndClientIdPeriode(ZonedDateTime.now().minusMonths(12), cli.getDateFin().plusHours(23));
        }

        if (!compteDTOList.isEmpty()) {
            compteDTOList.stream().forEach(cpt -> {
                ClientDTO c = clientRepository.findClientByClientIdOptimise(cpt.getClientId());
                if (c != null) {
                    XTitulaireCompteDTO xCompteDTO = new XTitulaireCompteDTO();
                    if (c.getPersonneId() != null) {
                        PersonneDTO personneDTO = personneRepository.findPersonneById(c.getPersonneId());
                        if (personneDTO.getId() != null) {
                            xCompteDTO.setNumeroTel(personneDTO.getTelephone());

                            if (personneDTO.getPersonnePhysiqueId() != null) {
                                PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                                if (personnePhysiqueDTO.getId() != null) {
                                    if (personnePhysiqueDTO.getNomClient() != null && personnePhysiqueDTO.getPrenomClient() != null) {
                                        xCompteDTO.setNomPrenom(personnePhysiqueDTO.getNomClient() + " " + personnePhysiqueDTO.getPrenomClient());
                                    }
                                    if (personnePhysiqueDTO.getDateNaissance() != null) {
                                        xCompteDTO.setDateNaissance(personnePhysiqueDTO.getDateNaissance());
                                    }
                                    if (personnePhysiqueDTO.getSexe() != null) {
                                        xCompteDTO.setSexe(personnePhysiqueDTO.getSexe().toString());
                                    }
                                }

                            }
                        }
                    }
                    xCompteDTO.setNumeroCompte(cpt.getValeurCompte());
                    xCompteDTO.setTypeCompte(cpt.getCategorieCompte().toString());
                    compteList.add(xCompteDTO);
                }
            });
        }

        xListCompte.setListTitulaireCompte(compteList);
        xListCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (xListCompte.getListTitulaireCompte().size() > 0) {
            xListCompte.setTitre("TOTAL DES TITULAIRES DE COMPTES DE MONNAIE ELECTRONIQUE");
        } else {
            xListCompte.setTitre("LA LISTE DES TITULAIRES DE COMPTES DE MONNAIE ELECTRONIQUE EST VIDE");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(cli.getFormat()));
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(cli.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(cli.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/t_compte_periode_xlsx.jasper");

        } else {
            // view.setUrl("classpath:/report/t_compte_periode_pdf.jasper");
            view.setUrl("classpath:/report/t_compte_periode_pdf.jrxml");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);

        return new ModelAndView(view, params);
    }

    public ModelAndView getCompteActifIn3mois(ApplicationContext context, EtatParams cli) {
        XListCompte xListCompte = new XListCompte();
        String debut = null,fin=null;
        Integer max=compteRepository.countValeurCompteAndClientIdActifIn3months(cli.getDateDebut(),cli.getDateFin().plusHours(23).plusMinutes(59).plusSeconds(59));
        List<CompteDTO> compteDTOList=new ArrayList<>();
        Integer nbrPage = (max / 1000);
        for (int j = 0; j <= nbrPage; j++) {
            Pageable page = PageRequest.of(j, 1000, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
            Long taille = new Long(page.getPageSize());
            Long pag = new Long(page.getPageSize() * page.getPageNumber());
            compteDTOList.addAll(compteRepository.findValeurCompteAndClientIdActifIn3months(taille, pag,cli.getDateDebut(),cli.getDateFin().plusHours(23).plusMinutes(59).plusSeconds(59)));
        }
        List<XCompteDTO> compteList = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
        System.out.println("Taille xCompteDTOS : {}"+ compteList.size());
        if (!compteDTOList.isEmpty()) {
            compteDTOList.forEach(cpt -> {
                System.out.println("cpt----------id : {}"+ cpt.getValeurCompte());
                XCompteDTO xCompteDTO = new XCompteDTO();
                xCompteDTO.setNumeroCompte(cpt.getValeurCompte());
                xCompteDTO.setCategorieCompte(cpt.getCategorieCompte().toString());
                ClientDTO c = clientRepository.findClientByClientIdOptimise(cpt.getClientId());
                if (c.getPersonneId() != null) {
                    System.out.println("dddd----------"+c.getPersonneId());
                    PersonneDTO personneDTO = personneRepository.findPersonneById(c.getPersonneId());
                        if (personneDTO!= null&&personneDTO.getPersonnePhysiqueId() !=null) {
                            System.out.println("dddd----------"+personneDTO.getPersonnePhysiqueId());
                            PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                            if(personnePhysiqueDTO!=null){
                                if (personnePhysiqueDTO.getNomClient() != null && personnePhysiqueDTO.getPrenomClient() != null) {
                                    xCompteDTO.setNomPrenom(personnePhysiqueDTO.getNomClient() + " " + personnePhysiqueDTO.getPrenomClient());
                                }
                            }
                            if (personneDTO.getTelephone() != null) {
                                xCompteDTO.setNumeroTel(personneDTO.getTelephone());
                            }
                            System.out.println("dddd1----------"+personnePhysiqueDTO.getNomClient());
                        }
                    }
                    System.out.println("xCompteDTO----------"+cpt.getLastModifiedDate());
                    xCompteDTO.setNumeroCompte(cpt.getValeurCompte());
                    xCompteDTO.setCategorieCompte(cpt.getCategorieCompte().toString());
                    xCompteDTO.setDateDernierOp(dateTimeFormatter.format(cpt.getLastModifiedDate()));
                    //xCompteDTO.setDuree(UtilService.formatDureInYearMoisJour(duree));
                    compteList.add(xCompteDTO);
                    log.debug("Taille xCompteDTOS : {}", compteList.size());

            });
        }

        xListCompte.setListCompte(compteList);
        xListCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (xListCompte.getListCompte().size() > 0) {
            if(cli.getDateDebut()!=null && cli.getDateFin()!=null){
                debut=cli.getDateDebut().format(dateTimeFormatter);
                fin=cli.getDateFin().format(dateTimeFormatter);
            }
            xListCompte.setTitre("LISTE DES COMPTES ACTIFS 03 DERNIERS MOIS DU "+debut+" "+"au"+" "+fin);
        } else {
            xListCompte.setTitre("LA LISTE DES COMPTES ACTIFS 03 DERNIERS MOIS EST VIDE");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(cli.getFormat()));
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(cli.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(cli.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/compte_actif_3_derniers_mois_pdf.jrxml");

        } else {
            view.setUrl("classpath:/report/compte_actif_3_derniers_mois_pdf.jrxml");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getCompteActifAgentIn3mois(ApplicationContext context, EtatParams cli){
        XListCompte xListCompte = new XListCompte();
        String debut = null,fin=null;
        TypeUniteOrganisationDTO typeUO=typeUniteOrganisationRepository.findCodetype(cli.getTypeAgent());
        Integer max=compteRepository.countValeurCompteAndAgentIdActifIn3months(typeUO.getId(),cli.getDateDebut(),cli.getDateFin().plusHours(23).plusMinutes(59).plusSeconds(59));
        List<CompteDTO> compteDTOList=new ArrayList<>();
        Integer nbrPage = (max / 1000);
        for (int j = 0; j <= nbrPage; j++) {
            Pageable page = PageRequest.of(j, 1000, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
            Long taille = new Long(page.getPageSize());
            Long pag = new Long(page.getPageSize() * page.getPageNumber());
            compteDTOList.addAll(compteRepository.findValeurCompteAndAgentIdActifIn3months(typeUO.getId(),taille, pag,cli.getDateDebut(),cli.getDateFin().plusHours(23).plusMinutes(59).plusSeconds(59)));
        }

        List<XCompteDTO> compteList = new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());

        if (!compteDTOList.isEmpty()) {
            compteDTOList.parallelStream().forEach(cpt -> {
                log.debug("cpt----------id : {}", cpt.getValeurCompte());
                //OperationOptimiseDTO lastOp = operationRepository.findByCompteIdSrcAndDest(cpt.getId());
                XCompteDTO xCompteDTO = new XCompteDTO();
                //if(lastOp!=null){
                xCompteDTO.setNumeroCompte(cpt.getValeurCompte());
                xCompteDTO.setCategorieCompte(cpt.getCategorieCompte().toString());
                xCompteDTO.setCategorieCompte(cpt.getCategorieCompte().toString());
                xCompteDTO.setDateDernierOp(dateTimeFormatter.format(cpt.getLastModifiedDate()));

                if(cpt.getUniteOrganisationId()!=null){
                    UniteOrganisationDTO organisationDTO=uniteOrganisationRepository.findDtoById(cpt.getUniteOrganisationId());
                    if (organisationDTO!=null){
                        xCompteDTO.setCodeUo(organisationDTO.getCodeUO());
                        xCompteDTO.setLibelleUniteOrganisation(organisationDTO.getLibelleUniteOrganisation());
                    }
                }
                compteList.add(xCompteDTO);
                log.debug("Taille xCompteDTOS : {}", compteList.size());
                //}
            });
        }

        xListCompte.setListCompte(compteList);
        xListCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (xListCompte.getListCompte().size() > 0) {
            if(cli.getDateDebut()!=null && cli.getDateFin()!=null){
                debut=cli.getDateDebut().format(dateTimeFormatter);
                fin=cli.getDateFin().format(dateTimeFormatter);
            }
            xListCompte.setTitre("LISTE DES COMPTES "+typeUO.getCodeTypeUO()+" 03 DERNIERS MOIS DU "+debut+" "+"au"+" "+fin);
        } else {
            xListCompte.setTitre("LA LISTE DES COMPTES "+typeUO.getCodeTypeUO()+" 03 DERNIERS MOIS EST VIDE");
        }

        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(cli.getFormat()));
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(cli.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(cli.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/compte_actif_3_derniers_mois_agent.jasper");

        } else {
            view.setUrl("classpath:/report/compte_actif_3_derniers_mois_agent.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getEtatCompteAgentValidation(ApplicationContext context, EtatParams cli){
        XListCompte xListCompte = new XListCompte();
        String debut = null,fin=null;
        List<XUserControl> liste=new ArrayList<>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault());
        if(cli.getTypeEvaluation()!=null){
            if(cli.getTypeEvaluation().equals("RESET_PASSWORD")){
               List<UserControlDTO> userControles= userControlRepository.findAllByActionAndCreatedDate(cli.getTypeEvaluation(),cli.getDateDebut(),cli.getDateFin().plusHours(23).plusMinutes(59).plusSeconds(59));
               userControles.forEach(userControl -> {
                   XUserControl xUserControl=new XUserControl();
                   xUserControl.setAction(cli.getTypeEvaluation());
                   xUserControl.setLogin(userControl.getLogin());
                   xUserControl.setTypeActeur(userControl.getTypeActeur());
                   xUserControl.setCreatedBy(userControl.getCreatedBy());
                   LocalDateTime localDateTime = LocalDateTime.ofInstant(userControl.getCreatedDate(), ZoneId.systemDefault());
                   LocalDate date= localDateTime.toLocalDate();
                   String dateAction=DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date);
                   xUserControl.setDateAction(dateAction);
                   liste.add(xUserControl);
               });
            }
            if(cli.getTypeEvaluation().equals("VALIDE")){
                List<ClientDTO> valideComptes=clientRepository.findAllClientIdentifier(cli.getDateDebut(),cli.getDateFin());
                //List<ActionEventDTO> valideComptes=actionEventRepository.findAllByActionAndDateAction(ActionEnum.CLIENT_IDENTIFIE.toString(),cli.getDateDebut(),cli.getDateFin().plusHours(23).plusMinutes(59).plusSeconds(59));
                valideComptes.forEach(valideC->{
                    XUserControl xUserControl=new XUserControl();
                    xUserControl.setAction(ActionEnum.CLIENT_IDENTIFIE.name());
                    xUserControl.setCreatedBy(valideC.getUserValide());
                    xUserControl.setTypeActeur("CLIENT");
                    String dateAction=DateTimeFormatter.ofPattern("dd/MM/yyyy").format(valideC.getDateValidation());
                    xUserControl.setDateAction(dateAction);
                    PersonneDTO personneDTO=personneRepository.findPersonneById(valideC.getPersonneId());
                    if(personneDTO!=null){
                        xUserControl.setLogin(personneDTO.getTelephone());
                    }
                    liste.add(xUserControl);
                });
            }
            if(cli.getTypeEvaluation().equals("DISABLE")){
                List<UserControlDTO> valideComptes= userControlRepository.findAllByActionAndCreatedDate(cli.getTypeEvaluation(),cli.getDateDebut(),cli.getDateFin().plusHours(23).plusMinutes(59).plusSeconds(59));
                //List<ActionEventDTO> valideComptes=actionEventRepository.findAllByActionAndDateAction("CLIENT_DESACTIVE",cli.getDateDebut(),cli.getDateFin().plusHours(23).plusMinutes(59).plusSeconds(59));
                valideComptes.forEach(valideC->{
                    XUserControl xUserControl=new XUserControl();
                    xUserControl.setAction(cli.getTypeEvaluation());
                    xUserControl.setCreatedBy(valideC.getCreatedBy());
                    xUserControl.setLogin(valideC.getLogin());
                    xUserControl.setTypeActeur(valideC.getTypeActeur());
                    LocalDateTime localDateTime = LocalDateTime.ofInstant(valideC.getCreatedDate(), ZoneId.systemDefault());
                    LocalDate date= localDateTime.toLocalDate();
                    String dateAction=DateTimeFormatter.ofPattern("dd/MM/yyyy").format(date);
                    //String dateAction=DateTimeFormatter.ofPattern("dd/MM/yyyy").format(valideC.getCreatedDate());
                    xUserControl.setDateAction(dateAction);
                    liste.add(xUserControl);
                });
            }
        }
        xListCompte.setListeAction(liste);

        xListCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (xListCompte.getListeAction().size() > 0) {
            if(cli.getDateDebut()!=null && cli.getDateFin()!=null){
                debut=cli.getDateDebut().format(dateTimeFormatter);
                fin=cli.getDateFin().format(dateTimeFormatter);
            }
            xListCompte.setTitre("LISTE DES COMPTES "+cli.getTypeEvaluation()+"  DU "+debut+" "+"au"+" "+fin);
        } else {
            xListCompte.setTitre("LA LISTE DES COMPTES "+cli.getTypeEvaluation()+"  EST VIDE");
        }

        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(cli.getFormat()));
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(cli.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(cli.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/action_event.jasper");

        } else {
            view.setUrl("classpath:/report/action_event.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getTotalTitulairesComptesAdultes(ApplicationContext context, EtatParams cli) {
        XListCompte xListCompte = new XListCompte();
        List<CompteDTO> compteDTOList = new ArrayList<>();
        List<XTitulaireCompteDTO> compteList = new ArrayList<>();

        if (cli.getDateDebut() != null && cli.getDateFin() != null) {
            compteDTOList = compteRepository.findValeurCompteAndCategorieCompteAndClientIdPeriode(cli.getDateDebut(), cli.getDateFin());
        } else if (cli.getDateDebut() != null) {
            compteDTOList = compteRepository.findValeurCompteAndCategorieCompteAndClientIdPeriode1(cli.getDateDebut());
        } else if (cli.getDateFin() != null) {
            compteDTOList = compteRepository.findValeurCompteAndCategorieCompteAndClientIdPeriode2(cli.getDateFin());
        }

        if (!compteDTOList.isEmpty()) {
            compteDTOList.forEach(cpt -> {
                ClientDTO c = clientRepository.findClientByClientIdOptimise(cpt.getClientId());
                if (c != null) {
                    if (c.getPersonneId() != null) {
                        PersonneDTO personneDTO = personneRepository.findPersonneById(c.getPersonneId());
                        if (personneDTO.getPersonnePhysiqueId() != null) {
                            PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                            if (personnePhysiqueDTO.getDateNaissance() != null) {
                                long age = ChronoUnit.YEARS.between(personnePhysiqueDTO.getDateNaissance(), ZonedDateTime.now());
                                if (age >= 15) {
                                    XTitulaireCompteDTO xCompteDTO = new XTitulaireCompteDTO();
                                    if (personneDTO.getTelephone() != null) {
                                        xCompteDTO.setNumeroTel(personneDTO.getTelephone());
                                    }
                                    if (personnePhysiqueDTO.getNomClient() != null && personnePhysiqueDTO.getPrenomClient() != null) {
                                        xCompteDTO.setNomPrenom(personnePhysiqueDTO.getNomClient() + " " + personnePhysiqueDTO.getPrenomClient());
                                    }
                                    xCompteDTO.setDateNaissance(personnePhysiqueDTO.getDateNaissance());
                                    if (personnePhysiqueDTO.getSexe() != null) {
                                        xCompteDTO.setSexe(personnePhysiqueDTO.getSexe().toString());
                                    }
                                    xCompteDTO.setNumeroCompte(cpt.getValeurCompte());
                                    xCompteDTO.setTypeCompte(cpt.getCategorieCompte().toString());
                                    compteList.add(xCompteDTO);
                                }
                            }
                        }
                    }
                }
            });
        }

        xListCompte.setListTitulaireCompte(compteList);
        xListCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (xListCompte.getListTitulaireCompte().size() > 0) {
            xListCompte.setTitre("TOTAL DES TITULAIRES DE COMPTES DE 15 ANS ET PLUS");
        } else {
            xListCompte.setTitre("LA LISTE DES TITULAIRES DE COMPTES DE 15 ANS ET PLUS EST VIDE");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(cli.getFormat()));
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(cli.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(cli.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/t_compte_periode_xlsx.jrxml");

        } else {
            view.setUrl("classpath:/report/t_compte_periode_pdf.jrxml");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getTotalTitulairesComptesFemmes(ApplicationContext context, EtatParams cli) {
        String sexePersonne=null;
        String titre=null;
        if(cli.getLibelle()!=null){
            sexePersonne= Sexe.valueOf(cli.getLibelle()).name();
        }
        XListCompte xListCompte = new XListCompte();
        List<CompteDTO> compteDTOList = new ArrayList<>();
        List<XTitulaireCompteDTO> titulaireList = new ArrayList<>();

        if (cli.getDateDebut() != null && cli.getDateFin() != null) {
            compteDTOList = compteRepository.findValeurCompteAndCategorieCompteAndClientIdPeriode(cli.getDateDebut(), cli.getDateFin());
        } else if (cli.getDateDebut() != null) {
            compteDTOList = compteRepository.findValeurCompteAndCategorieCompteAndClientIdPeriode1(cli.getDateDebut());
        } else if (cli.getDateFin() != null) {
            compteDTOList = compteRepository.findValeurCompteAndCategorieCompteAndClientIdPeriode2(cli.getDateFin());
        }

        if (!compteDTOList.isEmpty()) {
            String finalSexePersonne = sexePersonne;
            compteDTOList.forEach(cpt -> {
                ClientDTO c = clientRepository.findClientByClientIdOptimise(cpt.getClientId());
                if (c != null) {
                    if (c.getPersonneId() != null) {
                        PersonneDTO personneDTO = personneRepository.findPersonneById(c.getPersonneId());
                        if (personneDTO.getPersonnePhysiqueId() != null) {
                            PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                            if (personnePhysiqueDTO.getSexe() != null) {
                                if (personnePhysiqueDTO.getSexe().equals(Sexe.valueOf(finalSexePersonne))) {
                                    XTitulaireCompteDTO xTitulaireCompte = new XTitulaireCompteDTO();
                                    if (personneDTO.getTelephone() != null) {
                                        xTitulaireCompte.setNumeroTel(personneDTO.getTelephone());
                                    }
                                    if (personnePhysiqueDTO.getNomClient() != null && personnePhysiqueDTO.getPrenomClient() != null) {
                                        xTitulaireCompte.setNomPrenom(personnePhysiqueDTO.getNomClient() + " " + personnePhysiqueDTO.getPrenomClient());
                                    }
                                    if (personnePhysiqueDTO.getDateNaissance() != null) {
                                        xTitulaireCompte.setDateNaissance(personnePhysiqueDTO.getDateNaissance());
                                    }
                                    xTitulaireCompte.setSexe(personnePhysiqueDTO.getSexe().toString());
                                    xTitulaireCompte.setNumeroCompte(cpt.getValeurCompte());
                                    xTitulaireCompte.setTypeCompte(cpt.getCategorieCompte().toString());
                                    titulaireList.add(xTitulaireCompte);
                                }
                            }
                        }
                    }
                }
            });
        }

        xListCompte.setListTitulaireCompte(titulaireList);
        xListCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (xListCompte.getListTitulaireCompte().size() > 0) {
            if(cli.getLibelle().equals("FEMININ")){
                titre="FEMMES";
            }else {
                titre="HOMMES";
            }
            xListCompte.setTitre("TOTAL DES "+titre+" TITULAIRES DE COMPTES");

        } else {
            if(cli.getLibelle().equals("FEMININ")){
                titre="FEMMES";
            }else {
                titre="HOMMES";
            }
            xListCompte.setTitre("LA LISTE DES "+titre+" TITULAIRES DE COMPTES EST VIDE");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(cli.getFormat()));
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(cli.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(cli.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/t_compte_periode_xlsx.jrxml");

        } else {
            view.setUrl("classpath:/report/t_compte_periode_pdf.jrxml");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getTotalTitulairesComptesFemmesAdultes(ApplicationContext context, EtatParams cli) {
        String sexePersonne=null;
        String titre=null;
        if(cli.getLibelle()!=null){
            sexePersonne= Sexe.valueOf(cli.getLibelle()).name();
        }
        XListCompte xListCompte = new XListCompte();
        List<XTitulaireCompteDTO> compteList = new ArrayList<>();
        List<CompteDTO> compteDTOList = new ArrayList<>();

        if (cli.getDateDebut() != null && cli.getDateFin() != null) {
            compteDTOList = compteRepository.findValeurCompteAndCategorieCompteAndClientIdPeriode(cli.getDateDebut(), cli.getDateFin());
        } else if (cli.getDateDebut() != null) {
            compteDTOList = compteRepository.findValeurCompteAndCategorieCompteAndClientIdPeriode1(cli.getDateDebut());
        } else if (cli.getDateFin() != null) {
            compteDTOList = compteRepository.findValeurCompteAndCategorieCompteAndClientIdPeriode2(cli.getDateFin());
        }

        if (!compteDTOList.isEmpty()) {
            String finalSexePersonne = sexePersonne;
            compteDTOList.forEach(cpt -> {
                ClientDTO c = clientRepository.findClientByClientIdOptimise(cpt.getClientId());
                if (c != null) {
                    if (c.getPersonneId() != null) {
                        PersonneDTO personneDTO = personneRepository.findPersonneById(c.getPersonneId());
                        if (personneDTO.getPersonnePhysiqueId() != null) {
                            PersonnePhysiqueDTO personnePhysiqueDTO = personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                            if (personnePhysiqueDTO.getSexe() != null) {
                                if (personnePhysiqueDTO.getSexe().equals(Sexe.valueOf(finalSexePersonne))) {
                                    if (personnePhysiqueDTO.getDateNaissance() != null) {
                                        long age = ChronoUnit.YEARS.between(personnePhysiqueDTO.getDateNaissance(), ZonedDateTime.now());
                                        if (age >= 15) {
                                            XTitulaireCompteDTO xCompteDTO = new XTitulaireCompteDTO();
                                            if (personneDTO.getTelephone() != null) {
                                                xCompteDTO.setNumeroTel(personneDTO.getTelephone());
                                            }
                                            if (personnePhysiqueDTO.getNomClient() != null && personnePhysiqueDTO.getPrenomClient() != null) {
                                                xCompteDTO.setNomPrenom(personnePhysiqueDTO.getNomClient() + " " + personnePhysiqueDTO.getPrenomClient());
                                            }
                                            xCompteDTO.setDateNaissance(personnePhysiqueDTO.getDateNaissance());
                                            xCompteDTO.setSexe(personnePhysiqueDTO.getSexe().toString());
                                            xCompteDTO.setNumeroCompte(cpt.getValeurCompte());
                                            xCompteDTO.setTypeCompte(cpt.getCategorieCompte().toString());
                                            compteList.add(xCompteDTO);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }

        xListCompte.setListTitulaireCompte(compteList);
        xListCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        if (xListCompte.getListTitulaireCompte().size() > 0) {
            if(cli.getLibelle().equals("FEMININ")){
                titre="FEMMES";
            }else {
                titre="HOMMES";
            }
            xListCompte.setTitre("TOTAL DES "+titre+" DE 15 ANS ET PLUS TITULAIRES DE COMPTES");
        } else {
            if(cli.getLibelle().equals("FEMININ")){
                titre="FEMMES";
            }else {
                titre="HOMMES";
            }
            xListCompte.setTitre("LA LISTE DES "+titre+" DE 15 ANS ET PLUS TITULAIRES DE COMPTES EST VIDE");
        }
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(cli.getFormat()));
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(cli.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(cli.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/t_compte_periode_xlsx.jrxml");

        } else {
            view.setUrl("classpath:/report/t_compte_periode_pdf.jrxml");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getListPointVenteInactifs(ApplicationContext context, InfosFormulaire infosFormulaire) {

        XListPointVente xListPointVente = new XListPointVente();

        xListPointVente.setListPointVente(getPointVenteInactifs(infosFormulaire.getDebut(),infosFormulaire.getFin()));
        xListPointVente.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        if (xListPointVente.getListPointVente().size() > 0) {
            xListPointVente.setTitre("Liste des points de ventes inactifs depuis 30 jours");
        } else {
            xListPointVente.setTitre("La liste des points de ventes inactifs depuis 30 jours est vide");
        }

        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());

        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/point_vente_inactif_sur_30_jrs_xlsx.jasper");
        } else {
            view.setUrl("classpath:/report/point_vente_inactif_sur_30_jrs_pdf.jasper");
        }

        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListPointVente), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);

    }

    public List<XPointVenteDTO> getPointVenteNonActives() {
        Date date = new Date();
        List<XPointVenteDTO> xPointVenteDTOS = new ArrayList<XPointVenteDTO>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        List<PointVenteDTO> pointVenteDTOS = pointVenteRepository.findNonActivePointVenteDTO();
        pointVenteDTOS.forEach(pv -> {
            XPointVenteDTO xPointVenteDTO = new XPointVenteDTO();
            xPointVenteDTO.setCodePv(pv.getCodePointVente());
            xPointVenteDTO.setLibellePv(pv.getLibellePointVente());
            xPointVenteDTO.setLatitude(pv.getLatitude());
            xPointVenteDTO.setLongitude(pv.getLongitude());
            xPointVenteDTO.setUo(uniteOrganisationRepository.getLibelleUniteOrganisation(pv.getUniteOrganisationId()));
            xPointVenteDTOS.add(xPointVenteDTO);
        });

        return xPointVenteDTOS;
    }

    public ModelAndView getListPointVenteNonActives(ApplicationContext context, InfosFormulaire infosFormulaire) {

        XListPointVente xListPointVente = new XListPointVente();

        xListPointVente.setListPointVente(getPointVenteNonActives());
        xListPointVente.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        if (xListPointVente.getListPointVente().size() > 0) {
            xListPointVente.setTitre("Liste des points de ventes non activés");
        } else {
            xListPointVente.setTitre("La liste des points de ventes non activés est vide");
        }

        AbstractJasperReportsView view = ReportUtils.getJasperView(infosFormulaire.getReportFormat());

        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(infosFormulaire.getReportFormat()) || ReportFormat.CSV.equals(infosFormulaire.getReportFormat())) {
            view.setUrl("classpath:/report/point_vente_inactif_non_active_xlsx.jasper");
        } else {
            view.setUrl("classpath:/report/points_ventes_non_actives.jasper");
        }

        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListPointVente), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);

    }

    public List<XPointVenteDTO> getPointVenteInactifs(ZonedDateTime debut,ZonedDateTime fin) {
        Date date = new Date();
        List<XPointVenteDTO> xPointVenteDTOS = new ArrayList<XPointVenteDTO>();
        List<OperationDTO> operations = new ArrayList<OperationDTO>();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());
        List<BigInteger> pvIds = pointVenteRepository.findAllPVId(debut,fin);

        pvIds.forEach(id -> {
            OperationDTO operationDto = operationRepository.findLastestPVOperation(id);
            if (operationDto != null) {

                try {
                    SimpleDateFormat formater1 = new SimpleDateFormat("yyyyMMdd");
                    long difference_In_Time = formater1.parse(new SimpleDateFormat("yyyyMMdd").format(new java.util.Date())).getTime() - formater1.parse(new SimpleDateFormat("yyyyMMdd").format(Date.from(operationDto.getDateOperation().toInstant()))).getTime();
                    long diff_date = (difference_In_Time / (1000 * 60 * 60 * 24)) % 365;

                    //if (diff_date >= 30) {
                        PointVenteDTO pv = pointVenteRepository.findPointVenteDTO(id);
                        XPointVenteDTO xPointVenteDTO = new XPointVenteDTO();
                        xPointVenteDTO.setMontant_op((operationDto.getMontantOp()).toString());
                        xPointVenteDTO.setDerniere_op(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(operationDto.getDateOperation()));
                        xPointVenteDTO.setCodePv(pv.getCodePointVente());
                        xPointVenteDTO.setLibellePv(pv.getLibellePointVente());
                        xPointVenteDTO.setDuree(UtilService.formatDureInYearMoisJour(diff_date));
                        xPointVenteDTO.setUo(uniteOrganisationRepository.getLibelleUniteOrganisation(pv.getUniteOrganisationId()));
                        xPointVenteDTOS.add(xPointVenteDTO);
                    //}
                } catch (Exception e) {

                }
            }

        });

        return xPointVenteDTOS;
    }

    public ModelAndView getListBeneficiairesNonTitulairePME(ApplicationContext context, EtatParams cli) {

        XListeTitulaire xListeTitulaire = new XListeTitulaire();

        xListeTitulaire.setListTitulaire(getListBeneficiaireNonTitulairePME());
        //xListeTitulaire.setListTitulaire(null);
        xListeTitulaire.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        if (xListeTitulaire.getListTitulaire() != null && xListeTitulaire.getListTitulaire().size() > 0) {
            xListeTitulaire.setTitre("Liste des bénéficiaires ne disposant pas de porte-monnaie électronique");
        } else {
            xListeTitulaire.setTitre("La liste des bénéficiaires ne disposant pas de porte-monnaie électronique est vide");
        }

        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(cli.getFormat()));
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(cli.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(cli.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/beneficiaires_non_titulaires_de_porte_monaie_electronique_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/beneficiaires_non_titulaires_de_porte_monaie_electronique_pdf.jrxml");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListeTitulaire), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);

    }

    public List<XTitulaireCompteDTO> getListBeneficiaireNonTitulairePME() {

        List<XTitulaireCompteDTO> xTitulaireCompteDTO = new ArrayList<XTitulaireCompteDTO>();
        List<BigInteger> beneficiaireId = operationRepository.getOperationCompteBeneficiaireCompteBanque();
        for (int i = 0; i < beneficiaireId.size(); i++) {

            XTitulaireCompteDTO xTitulaireCompteDTOs = new XTitulaireCompteDTO();
            BigInteger personneId = beneficiaireRepository.getPersonneId(beneficiaireId.get(i));
            PersonneDTO personneDTO = personneRepository.findPersonneById(personneId.longValue());

            if (personneDTO != null) {
                if (personneDTO.getPersonnePhysiqueId() != null) {
                    PersonnePhysiqueDTO pp = personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                    xTitulaireCompteDTOs.setNomPrenom(pp.getNomClient() + " " + pp.getPrenomClient());

                    try {
                        xTitulaireCompteDTOs.setSexe(pp.getSexe().toString());
                    } catch (Exception err) {

                    }

                    try {
                        xTitulaireCompteDTOs.setDateNaissance(pp.getDateNaissance());
                    } catch (Exception err) {

                    }

                    xTitulaireCompteDTOs.setNumeroTel(personneDTO.getTelephone());

                }

                } else if (personneDTO.getPersonneMoraleId() != null) {
                    log.debug("====================*************************++====================={}", personneDTO);
                }
            xTitulaireCompteDTO.add(xTitulaireCompteDTOs);
            }

           return xTitulaireCompteDTO;
        }




    public ModelAndView modelListSousDistributeurs(ApplicationContext context, EtatParams cli) {

        XListeSousDistributeurDTO xListeSousDistributeurDto = new XListeSousDistributeurDTO();

        xListeSousDistributeurDto.setListDistributeur(getListSousDistributeurs(cli.getDateDebut(),cli.getDateFin()));
        xListeSousDistributeurDto.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        if (xListeSousDistributeurDto.getListDistributeur().size() > 0) {
            xListeSousDistributeurDto.setTitre("Liste des sous distributeurs avec details opérations");
        } else {
            xListeSousDistributeurDto.setTitre("La liste des sous distributeurs avec details opérations est vide");
        }

        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(cli.getFormat()));
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(cli.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(cli.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/liste_sous_distributeurs_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/liste_sous_distributeurs_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListeSousDistributeurDto), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);

    }

    public List<XSousDistributeurDTO> getListSousDistributeurs(ZonedDateTime debut,ZonedDateTime fin) {

        List<XSousDistributeurDTO> xSousDistributeurDTO = new ArrayList<XSousDistributeurDTO>();
        //List<UniteOrganisationDTO> uos=null;
        final BigDecimal[] commissionAgentTotal = {new BigDecimal("0")};
        final BigDecimal[] commissionMasterTotal = {new BigDecimal("0")};
        List<Long> serviceIdsDepot = new ArrayList<>();
        List<Long> serviceIdsretrait = new ArrayList<>();
        //List<UniteOrganisationDTO> listeSousDist = uniteOrganisationRepository.findAllOptimiseByTypeUo(typeUniteOrganisationRepository.findByLibelleTypeUO(TypeUO.SOUS_DISTRIBUTEUR.name()).getId());
        int totalUo = uniteOrganisationRepository.countFindForViewPaginated(null, null, null, TypeUO.SOUS_DISTRIBUTEUR.name(), null, null, null);
        Long taille = (long) totalUo;
        ZonedDateTime debutN=ZonedDateTime.now();
        System.out.println("debutN -----------"+debutN);
        System.out.println("debut -----------"+debut);
        List<UniteOrganisationDTO> listeSousDist = uniteOrganisationRepository.findAllForViewPaginatedOptimise(null, null, null, TypeUO.SOUS_DISTRIBUTEUR.name(), null, null, null, taille, (long) 0);
        //List<UniteOrganisationDTO> listeSousDist = uniteOrganisationRepository.findAllOptimiseForReport(TypeUO.SOUS_DISTRIBUTEUR.name(),debutN, taille, (long) 0);
        TypeService depotService = typeServiceRepository.findTopByCode(TypeServiceLibelle.CASH_ACCOUNT.name());
        List<TypeService> retraitService = typeServiceRepository.getRetraitOrRetraitCompte();
        if (depotService != null && depotService.getId() != null) {
            serviceIdsDepot.add(depotService.getId());
        }

        if (retraitService != null) {
            retraitService.forEach(typeService -> {
                serviceIdsretrait.add(typeService.getId());
            });
        }
        listeSousDist.parallelStream().forEach(sousDist -> {
            XSousDistributeurDTO xSousDistributeurDTOs = new XSousDistributeurDTO();
            xSousDistributeurDTOs.setCodeUO(sousDist.getCodeUO());
            xSousDistributeurDTOs.setLibelleUniteOrganisation(sousDist.getLibelleUniteOrganisation());
            xSousDistributeurDTOs.setDateCreation(sousDist.getDateCreation());
            if (sousDist.getVilleId() != null) {
                xSousDistributeurDTOs.setVilleNom(villeRepository.getNomVille(sousDist.getVilleId()).toString());
            }
            if (sousDist.getPereUOId() != null) {
                UniteOrganisationDTO pereUo = uniteOrganisationRepository.findDtoById(sousDist.getPereUOId());
                if (pereUo != null) {
                    xSousDistributeurDTOs.setPereUO(pereUo.getLibelleUniteOrganisation());
                }
            }
            xSousDistributeurDTOs.setTelephone(sousDist.getTelephone());
            CompteDTO agentCpt = compteRepository.findUniteOrganisationByAndEtiquette(sousDist.getId(), EtiquetteCompteCode.CPTEOPERATIONACTEUR.name());
            if (agentCpt != null && agentCpt.getSoldeCompte() != null) {
                xSousDistributeurDTOs.setSolde(agentCpt.getSoldeCompte());
            }
            //List<CommissionDTO> agentCommission = commissionRepository.findCommissionByUoIdAndEstPayer(sousDist.getId(), true);
            BigDecimal montantTotalAgent=commissionRepository.sommeCommissionByUoIdAndEstPayer(sousDist.getId(), true,debut,fin);
            BigDecimal montantTotalMaster=commissionRepository.sommeCommissionByUoIdAndEstPayer(sousDist.getPereUOId(), true,debut,fin);
            //List<CommissionDTO> masterCommission = commissionRepository.findCommissionByUoIdAndEstPayer(sousDist.getPereUOId(), true);
            /*if (agentCommission != null) {
                agentCommission.parallelStream().forEach(commissionDTO -> {
                    commissionAgentTotal[0] = commissionAgentTotal[0].add(commissionDTO.getMontant());
                });
            }
            if (masterCommission != null) {
                masterCommission.parallelStream().forEach(commissionDTO -> {
                    commissionMasterTotal[0] = commissionMasterTotal[0].add(commissionDTO.getMontant());
                });
            }*/
            xSousDistributeurDTOs.setCommission_sd(montantTotalAgent);
            xSousDistributeurDTOs.setCommission_m(montantTotalMaster);

            List<OperationDTO> depositOperations = operationRepository.getAllOperationsByUoIdAndTypeService(sousDist.getId(), serviceIdsDepot);
            List<OperationDTO> withdrawOperations = operationRepository.getAllOperationsByUoIdAndTypeService(sousDist.getId(), serviceIdsretrait);

            if (depositOperations.size() > 0) {
                //BigDecimal sum = new BigDecimal(0);
                final BigDecimal[] sum = {new BigDecimal("0")};
                List<Double> mts = new ArrayList<Double>();
                depositOperations.forEach(depositOp -> {
                    sum[0] = sum[0].add(depositOp.getMontantOp());
                });

                xSousDistributeurDTOs.setVolume_depot(String.valueOf(depositOperations.size()));
                xSousDistributeurDTOs.setValeur_depot(sum[0]);
            }

            if (withdrawOperations.size() > 0) {
                double sum = 0;
                final BigDecimal[] mts = {new BigDecimal("0")};
                withdrawOperations.forEach(withdrawOp -> {
                    mts[0] = mts[0].add(withdrawOp.getMontantOp());
                });
                xSousDistributeurDTOs.setVolume_retrait(String.valueOf(withdrawOperations.size()));
                xSousDistributeurDTOs.setValeur_retrait(mts[0]);

            }

            xSousDistributeurDTO.add(xSousDistributeurDTOs);
        });

        return xSousDistributeurDTO;
    }

    public ModelAndView printRecu(ApplicationContext context, List<MParametre> mParametres, String codeOperation) {
        Map<String, Object> params = new HashMap<>();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.PDF);
        String nomPayeur;
        Optional<Operation> operation = operationRepository.findByCodeOperation(codeOperation); //operationRepository.findByCodeOperation(xOperation.getCodeOperation());
        if (operation.isPresent() && operation.get().getEmetteur() != null && operation.get().getEmetteur().getPersonne().getPersonnePhysique() != null) {
            nomPayeur = operation.get().getEmetteur().getPersonne().getPersonnePhysique().getNomClient()+" "+operation.get().getEmetteur().getPersonne().getPersonnePhysique().getPrenomClient();
            params.put("expeditaire",nomPayeur);
        }
        String logo=new File(Constants.LOGO_URL).getAbsolutePath();
        String logoTogoCel=new File(Constants.LOGO_COMO_URL).getAbsolutePath();
        params.put("logo",logo);
        params.put("logoTogocel",logoTogoCel);
        mParametres.forEach(mParametre -> {
                    log.debug("mParametre.getValeur()" + mParametre.getCodeParametre());
                    if (mParametre.getCodeParametre().equals("MONTANT")) {
                        log.debug("mParametre22" + mParametre.getCodeParametre());
                        params.put("montant", new BigDecimal(mParametre.getValeur()));
                    }
                    if (mParametre.getCodeParametre().equals("MONTANTENLETTRE")) {
                        params.put("montantLettre", mParametre.getValeur());
                    }
                    if (mParametre.getCodeParametre().equals("REFERENCEOPERATION")) {
                        params.put("numeroRecu", mParametre.getValeur());
                    }
                    if (mParametre.getCodeParametre().equals("TELEPHONE")) {
                        params.put("telephone", mParametre.getValeur());
                    }
                    if (mParametre.getCodeParametre().equals("IDENTIFIANT")) {
                        params.put("paiementId", mParametre.getValeur());
                    }
                    if (mParametre.getCodeParametre().equals("DATEPAIEMENT")) {
                        params.put("datePaiement", mParametre.getValeur());
                    }
                    if (mParametre.getCodeParametre().equals("PARCOURS")) {
                        params.put("parcours", mParametre.getValeur());
                    }
                    if (mParametre.getCodeParametre().equals("NOMETUDIANT")) {
                        params.put("beneficiaire", mParametre.getValeur());
                    }
                });

        view.setUrl("classpath:/report/recu_tg.jasper");
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(null), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }
    public ModelAndView listeBanqueActif(ApplicationContext context, EtatParams cli) {

        XListeCompteBanquaire xListeCompteBanquaire = new XListeCompteBanquaire();
        List<CompteDTO> listeCompte=new ArrayList<>();
        List<XBanqueReportDTO> allCompte = new ArrayList<>();
        if(cli.getStatutCompte()!=null && cli.getStatutCompte().equals("ACTIVE")){
            listeCompte = compteRepository.findAllCompteBanqueActif(cli.getDateDebut(), cli.getDateFin(),cli.getStatutCompte());

        } else if(cli.getStatutCompte()!=null && cli.getStatutCompte().equals("DESACTIVE")) {
            listeCompte = compteRepository.findAllCompteBanqueDesactive(cli.getDateDebut(), cli.getDateFin(),cli.getStatutCompte());
        } else {
            listeCompte = compteRepository.findAllCompteBanque(cli.getDateDebut(), cli.getDateFin());
        }
        //listeCompte = compteRepository.findAllCompteBanqueActif(cli.getDateDebut(), cli.getDateFin(),cli.getStatut());
        listeCompte.forEach(compteDTO -> {
            XBanqueReportDTO xBanqueReportDTO=new XBanqueReportDTO();
            if(compteDTO.getClientId()!=null){
                ClientDTO clientDTO=clientRepository.findClientByClientIdOptimise(compteDTO.getClientId());
                if(clientDTO.getId()!=null){
                    PersonneDTO personneDTO=personneRepository.findPersonneById(clientDTO.getPersonneId());
                    xBanqueReportDTO.setTelephone(personneDTO.getTelephone());
                    if(personneDTO.getId()!=null){
                        PersonnePhysiqueDTO personnePhysiqueDTO=personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                        if(personnePhysiqueDTO!=null){
                            xBanqueReportDTO.setNomClient(personnePhysiqueDTO.getNomClient());
                            xBanqueReportDTO.setPrenomClient(personnePhysiqueDTO.getPrenomClient());
                        }
                    }
                }
                if(compteDTO.getCompteBanqueId()!=null){
                    CompteBanque compteBanque=compteBanqueRepository.findById(compteDTO.getCompteBanqueId()).orElse(null);
                    if(compteBanque!=null){
                        xBanqueReportDTO.setNumeroCompteBanquaire(compteDTO.getValeurCompte());
                        xBanqueReportDTO.setCodeAgence(compteBanque.getCodeAgence());
                        xBanqueReportDTO.setCodeBanque(compteBanque.getCodeBanque());
                        xBanqueReportDTO.setRib(compteBanque.getRib());
                    }
                }
                if(compteDTO.getStatutCompte()!=null&& compteDTO.getStatutCompte().name().equals("ACTIVE")){
                    xBanqueReportDTO.setStatutCompte(compteDTO.getStatutCompte().name());
                } else if(compteDTO.getStatutCompte()!=null&&compteDTO.getStatutCompte().name().equals(StatutCompte.DESACTIVE.name())){
                    xBanqueReportDTO.setStatutCompte(compteDTO.getStatutCompte().name());
                } else {
                    xBanqueReportDTO.setStatutCompte(null);
                }
                xBanqueReportDTO.setStatutCompte(compteDTO.getStatutCompte().name());


                /*if(cli.getStatutCompte().equals("ACTIVE")){
                    xBanqueReportDTO.setStatutCompte(StatutCompte.ACTIVE.name());
                }else {
                    xBanqueReportDTO.setStatutCompte(StatutCompte.DESACTIVE.name());

                }*/
            }
            allCompte.add(xBanqueReportDTO);
        });
        xListeCompteBanquaire.setListeBanque(allCompte);
        xListeCompteBanquaire.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());

        if (xListeCompteBanquaire.getListeBanque().size() > 0) {
            if(cli.getStatut().equals("ACTIVE")){
                xListeCompteBanquaire.setTitre("Liste des comptes bancaire ativer");
            }else if(cli.getStatut().equals("DESACTIVE")) {
                xListeCompteBanquaire.setTitre("Liste des comptes bancaire désactiver");
            }else {
                xListeCompteBanquaire.setTitre("Liste des comptes bancaire");

            }
        } else {
            if(cli.getStatut().equals("ACTIVE")){
                xListeCompteBanquaire.setTitre("Liste des comptes bancaire activer est vide");
            }else if(cli.getStatut().equals("DESACTIVE")){
                xListeCompteBanquaire.setTitre("Liste des comptes bancaire désactiver est vide");
            } else {
                xListeCompteBanquaire.setTitre("Liste des comptes bancaire est vide");

            }
        }

        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(cli.getFormat()));
        Map<String, Object> params = new HashMap<>();

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(cli.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(cli.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/compte_banque_actif_pdf.jasper");

        } else {
            view.setUrl("classpath:/report/compte_banque_actif_pdf.jasper");
        }
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xListeCompteBanquaire), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);

    }

    public ModelAndView listeOperationSuivisClient(ApplicationContext context,ParamEtat paramEtat){

        XSuivisOperation opClient=new XSuivisOperation();
        List<XOperationSuivisDTO> allOp=new ArrayList<>();
        List<OperationSuivisDTO> listeOpSuivisClient=operationSuivisRepository.findAllOperationSuivisByClientId(Long.parseLong(paramEtat.getIdClient()));
        listeOpSuivisClient.forEach(op->{
            XOperationSuivisDTO xOperationSuivisDTO=new XOperationSuivisDTO();
            if(op.getClientId()!=null){
                ClientDTO clientDTO=clientRepository.findClientByClientIdOptimise(op.getClientId());
                if(clientDTO.getId()!=null){
                    PersonneDTO personneDTO=personneRepository.findPersonneById(clientDTO.getPersonneId());
                    if(personneDTO.getId()!=null){
                        PersonnePhysiqueDTO personnePhysiqueDTO=personnePhysiqueRepository.findByIdReport(personneDTO.getPersonnePhysiqueId());
                        if(personnePhysiqueDTO!=null){
                            xOperationSuivisDTO.setNomClient(personnePhysiqueDTO.getNomClient());
                            xOperationSuivisDTO.setPrenomClient(personnePhysiqueDTO.getPrenomClient());
                        }
                    }
                }
            }
            if(op.getOperationId()!=null){
                OperationDTO operation=operationRepository.findOperationById(op.getOperationId());
                xOperationSuivisDTO.setDateOperation(operation.getDateOperation().toString());
                xOperationSuivisDTO.setMontantOperation(operation.getMontantOp());
                xOperationSuivisDTO.setMontantFrais(operation.getMontantFrais());
                xOperationSuivisDTO.setMontantTaxe(operation.getMontantTaxe());
                xOperationSuivisDTO.setStatut(operation.getStatut().name());
                xOperationSuivisDTO.setMotif(operation.getMotif());
                if(operation.getTypeServiceId()!=null){
                    TypeServiceDTO typeServiceDTO=typeServiceRepository.findOneOptimise(operation.getTypeServiceId());
                    xOperationSuivisDTO.setTypeService(typeServiceDTO.getLibelle());
                }
                if(operation.getBeneficaireId()!=null){
                    Beneficiaire ben=beneficiaireRepository.findById(operation.getBeneficaireId()).orElse(null);
                    if(ben!=null){
                        if(ben.getPersonne()!=null){
                            Piece p=pieceRepository.findTopByTitulaireId(ben.getPersonne().getId());
                            if(p!=null) {
                                String info = p.getReferencePiece() + " Date deliv." + p.getDateDelivrance() + " Expire le." + p.getDateExpiration();
                                xOperationSuivisDTO.setPieceBeneficiaire(info);
                            }
                        }
                    }

                }
            }
            allOp.add(xOperationSuivisDTO);
        });

        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(paramEtat.getFormat()));
        Map<String, Object> params = new HashMap<>();
        if (listeOpSuivisClient.size() > 0) {
            opClient.setTitre("Liste des opération suivis");
        } else {
            opClient.setTitre("Liste des opération suivis est vide");
        }
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(paramEtat.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(paramEtat.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/operation_suivis_client_xlsx.jasper");

        } else {
            view.setUrl("classpath:/report/operation_suivis_client_pdf.jasper");
        }
        opClient.setListeOperationSuivis(allOp);
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(opClient), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView listePointVenteActifs(ApplicationContext context,ParamEtat paramEtat){
        List<ListeUniteOrg> allPointVente=new ArrayList<>();
        XUniteOrg xUniteOrg=new XUniteOrg();
        List<Long> pointsVentesIds=uniteOrganisationRepository.pointVenteIdsByDate(paramEtat.getDateDebut(),paramEtat.getDateFin());
        List<UniteOrganisationDTO> list=uniteOrganisationRepository.findAllPointVenteByDate(pointsVentesIds);
        if(list!=null){
            list.stream().forEach(org->{
                ListeUniteOrg uo=new ListeUniteOrg();
                uo.setTelephoneUO(org.getTelephone());
                uo.setNomUO(org.getLibelleUniteOrganisation());
                uo.setCodeUO(org.getCodeUO());
                uo.setTypeUO(org.getTypeUOCode());
                if(org.getEstActif()){
                    uo.setStatut("ACTIVER");
                }else {
                    uo.setStatut("DESACTIVER");
                }
                allPointVente.add(uo);
            });
        }
        xUniteOrg.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(paramEtat.getFormat()));
        Map<String, Object> params = new HashMap<>();
        xUniteOrg.setUniteOrgList(allPointVente);
        if (list.size() > 0) {
            xUniteOrg.setTitle("Liste des points de ventes actifs");
        } else {
            xUniteOrg.setTitle("Liste des points de ventes actifs est vide");
        }
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(paramEtat.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(paramEtat.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/points_ventes_actifs.jasper");

        } else {
            view.setUrl("classpath:/report/points_ventes_actifs.jasper");
        }

        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xUniteOrg), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }


    public ModelAndView historiqueCompte(ApplicationContext context,EtatItems item){
        XTransactionsCompte xTransactionsCompte = new XTransactionsCompte();
        List<XLigneTransaction> xListLigneTransaction = new ArrayList<>();
        List<TransactionComptePtiDTO> listTransactions = new ArrayList<>();
        CompteAutreDTO info = compteAutreRepository.getCompteAutre(item.getNumeroCompte());
        log.debug("==========================={}",item);
        BigDecimal montantCredit = new BigDecimal(0);
        BigDecimal montantDebit = new BigDecimal(0);
        log.debug("------------------------------------------{}",item.getData() );
        item.getData().sort(Comparator.comparing(TransactionComptePtiDTO::getId));
        log.debug("------------------------------------------{}",item.getData() );

        TransactionComptePtiDTO firsLine = item.getData().get(0);
        item.getData().remove(0);
        
        for (TransactionComptePtiDTO ln :  item.getData()) {
            if (ln.getType().equals(TypeTransactionCompte.CREDIT)){
                montantCredit = montantCredit.add(new BigDecimal(ln.getMontant().intValue()));
            }else {
                montantDebit = montantDebit.add(new BigDecimal(ln.getMontant().intValue()));
            }
            
        }
        montantCredit = montantCredit.add(new BigDecimal(firsLine.getSoldeApres().intValue()));
        item.getData().add(0, firsLine);

        item.getData().stream().forEach(trans->{
            log.debug("==========================={}",trans.getMontant());
            XLigneTransaction xligne = new XLigneTransaction();
            xligne.setTransaction(trans.getCodeExterne());
            xligne.setDateTrans(trans.getDate());
            if (trans.getType().equals(TypeTransactionCompte.CREDIT)){
                xligne.setMontantCredit(trans.getMontant());
            }else {
                xligne.setMontantDebit(trans.getMontant());
            }
            xligne.setSoldeAvant(trans.getSoldeCourant());
            xligne.setSoldeCourant(trans.getSoldeApres());
            xligne.setDescription(trans.getOperationMotif());
            xListLigneTransaction.add(xligne);

        });
                
        XLigneTransaction xligne = new XLigneTransaction();
        xligne.setTransaction("TOTAL");
        xligne.setMontantCredit(montantCredit);
        xligne.setMontantDebit(montantDebit);
        xListLigneTransaction.add(xligne);

        XLigneTransaction ligne = new XLigneTransaction();
       
        if (montantCredit.compareTo(montantDebit) == 1 || montantCredit.compareTo(montantDebit) == 0) {
            
            ligne.setTransaction("SOLDE CREDITEUR");
            ligne.setMontantCredit(montantCredit.subtract(montantDebit));
            xListLigneTransaction.add(ligne);
        }else if (montantCredit.compareTo(montantDebit) == -1){
            
            ligne.setTransaction("SOLDE DEBITEUR");
            ligne.setMontantDebit(montantDebit.subtract(montantCredit));
            xListLigneTransaction.add(ligne);
        }
        
        xTransactionsCompte.setNumeroCompte(item.getNumeroCompte());
        if (item.getDateDebut() != null && item.getDateFin() != null){
            xTransactionsCompte.setPeriode(item.getDateDebut()+"-"+item.getDateFin());
        }else if (item.getDateFin() != null && item.getDateDebut() == null){
            xTransactionsCompte.setPeriode("Avant le "+item.getDateFin());
        }else if (item.getDateFin() == null && item.getDateDebut() != null){
            xTransactionsCompte.setPeriode("Depuis le "+item.getDateDebut());
        }else {
            xTransactionsCompte.setPeriode("");
        }

        // if (item.getData().indexOf(ln) == 0) {
        //     XLigneTransaction xligne = new XLigneTransaction();
        //     xligne.setDateTrans(ln.getDate());
        //     if (ln.getType().equals(TypeTransactionCompte.CREDIT)){
        //         xligne.setMontantCredit(ln.getSoldeApres());
        //     }else {
        //         xligne.setMontantDebit(ln.getSoldeApres());
        //     }
        //     xligne.setSoldeAvant(ln.getSoldeCourant());
        //     xligne.setSoldeCourant(ln.getSoldeApres());
        //     xligne.setDescription("Solde au "+ln.getDate());
        //     xListLigneTransaction.add(xligne);
                
        // }
        // xListLigneTransaction.get(0).setDescription("Solde au "+xListLigneTransaction.get(0).getDate());

        // xListLigneTransaction.get(0).setTransaction(null);

        // log.debug(xListLigneTransaction.get(0));
        
        if(info.getLibelle()!=null){
            xTransactionsCompte.setClient(info.getLibelle());
            if (info.getIndicatifPaysOrganisation()!=null){
                xTransactionsCompte.setTelephone(info.getIndicatifPaysOrganisation()+info.getTelephoneOrganisation());
            }else {
                xTransactionsCompte.setTelephone(info.getTelephoneOrganisation());
            }

        }else if (info.getRaisonSociale()!=null){
            xTransactionsCompte.setClient(info.getRaisonSociale());
            if (info.getCodeTel()!=null){
                xTransactionsCompte.setTelephone(info.getCodeTel()+info.getTelephone());
            }else {
                xTransactionsCompte.setTelephone(info.getTelephone());
            }
        }else {
            xTransactionsCompte.setClient(info.getNomClient()+" "+info.getPrenomClient());
            if (info.getCodeTel()!=null){
                xTransactionsCompte.setTelephone(info.getCodeTel()+info.getTelephone());
            }else {
                xTransactionsCompte.setTelephone(info.getTelephone());
            }
        }
        xListLigneTransaction.get(0).setDescription("Solde au "+xListLigneTransaction.get(0).getDateTrans());
        xListLigneTransaction.get(0).setMontantCredit(xListLigneTransaction.get(0).getSoldeCourant());
        xListLigneTransaction.get(0).setMontantDebit(null);
        xListLigneTransaction.get(0).setTransaction(null);
        xTransactionsCompte.setDateGeneration(LocalDate.now());
        xTransactionsCompte.setListLigneTransactions(xListLigneTransaction);
        xTransactionsCompte.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        xTransactionsCompte.setLogoComo(new File(Constants.LOGO_COMO_URL).getAbsolutePath());

       
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(item.getFormat()));
        Map<String, Object> params = new HashMap<>();
        
        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(item.getFormat())) || ReportFormat.CSV.equals(ReportFormat.valueOf(item.getFormat()))) {
            params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);
            view.setUrl("classpath:/report/historiqueCompte_pdf.jrxml");

        } else {
            view.setUrl("classpath:/report/historiqueCompte_pdf.jrxml");
        }
        log.debug("==============MontantCredit============={}",montantCredit);
        log.debug("=============MontantDebit=============={}",montantDebit);
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(xTransactionsCompte), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }


    public ModelAndView getChrgmtRapportByStatut(ApplicationContext context, Long idChargement,String statut, String format) {
        //JasperReportsPdfView view = new JasperReportsPdfView();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        XligneCharger xLigneChTr = new XligneCharger();
        List<XligneCharger> listeChrg = new ArrayList<>();
        XChargementTransfert chrgTransf = new XChargementTransfert();
        List<LigneChargementTransfert> lignes = new ArrayList<>();
        ChargementTransfert chargement = chargementTransfertService.findOne(idChargement);


        if (chargement != null) {
            chrgTransf.setNomFichier(chargement.getNomFichier());
            chrgTransf.setReference(chargement.getReference());
            chrgTransf.setMontantTotal(chargement.getMontantTotal());
            chrgTransf.setNombreLigne(chargement.getNombreLigne());
            chrgTransf.setNombreLigneCharger(chargement.getNombreLigneCharge());
            chrgTransf.setNombreLigneErreur(chargement.getNombreLigne()-(chargement.getNombreLigneCharge()));
            chrgTransf.setCreateBy(chargement.getCreatedBy());
            chrgTransf.setLastModifiedBy(chargement.getLastModifiedBy());
        }

        StatutLigneChargement statutLigne = StatutLigneChargement.NC;
        if (statut.equals("ACCT")){
            statutLigne = StatutLigneChargement.OC;
            lignes = ligneChargementTransfertRepository.queryDetailListByStatut(idChargement, statutLigne);
        }else {
            StatutLigneChargement statutLigne1= StatutLigneChargement.IC;
            lignes = ligneChargementTransfertRepository.queryDetailListByStatut1(idChargement, statutLigne, statutLigne1);
        }
        
        log.debug("------lgne chargement-----" + lignes);

        for (LigneChargementTransfert l : lignes) {
            if (!l.getStatutLigne().equals(StatutLigneChargement.OC)) {
                xLigneChTr.setNumCpt(l.getNumeroTelephone());
                xLigneChTr.setMontant(l.getMontant());
                xLigneChTr.setErreur(l.getError());
                xLigneChTr.setNom(l.getNom());
                xLigneChTr.setPrenom(l.getPrenom());
                xLigneChTr.setCodeOperation(l.getCodeOperation());
                xLigneChTr.setDescription("");
                /*Optional<Operation> operation=operationRepository.findByCodeOperation(l.getCodeOperation());
                if(operation.isPresent()){
                    xLigneChTr.setDescription(operation.get().getDescription());
                }*/
                listeChrg.add(xLigneChTr);
                xLigneChTr = new XligneCharger();
            }
            if (l.getStatutLigne().equals(StatutLigneChargement.OC)) {
                // xLigneChTr = new XligneCharger();
                xLigneChTr.setNumCpt(l.getNumeroTelephone());
                xLigneChTr.setMontant(l.getMontant());
                xLigneChTr.setNom(l.getNom());
                xLigneChTr.setPrenom(l.getPrenom());
                xLigneChTr.setCodeOperation(l.getCodeOperation());
                xLigneChTr.setDescription("");
                /*Optional<Operation> operation=operationRepository.findByCodeOperation(l.getCodeOperation());
                if(operation.isPresent()){
                    xLigneChTr.setDescription(operation.get().getDescription());
                }*/
                listeChrg.add(xLigneChTr);
                xLigneChTr = new XligneCharger();
                
            }
        }
        chrgTransf.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        chrgTransf.setLigneCharger(listeChrg);

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl("classpath:/report/chargementReussiMj.jrxml");
            if (statut.equals("RJCT")){
                view.setUrl("classpath:/report/chargementEchoue_excel.jrxml");
            }else{
                view.setUrl("classpath:/report/chargementReussi_excel.jrxml");
            }
           
        } else {
            
            if (statut.equals("RJCT")){
                view.setUrl("classpath:/report/chargementEchoue.jrxml");
            }else{
                view.setUrl("classpath:/report/chargementReussi.jrxml");
            }
            
        }

        log.debug("==============================================={}",chrgTransf);
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(chrgTransf), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }
    public ModelAndView getChrgmtFichierRapportByStatut(ApplicationContext context, Long idChargement,String statut, String format,String typeChargement) {
        //JasperReportsPdfView view = new JasperReportsPdfView();
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(format));
        Map<String, Object> params = new HashMap<>();
        XLigneChargerFichier xLigneChTr = new XLigneChargerFichier();
        List<XLigneChargerFichier> listeChrg = new ArrayList<>();
        XChargementFichier chrgTransf = new XChargementFichier();
        List<LigneChargementFichier> lignes = new ArrayList<>();
        ChargementFichier chargement = chargementFichierRepository.findById(idChargement).orElse(null);
        if (chargement != null) {
            chrgTransf.setNomFichier(chargement.getNomFichier());
            chrgTransf.setReference(chargement.getReference());
            chrgTransf.setNombreLigne(chargement.getNombreLigne());
            chrgTransf.setNombreLigneCharger(chargement.getNombreLigneCharge());
            chrgTransf.setNombreLigneErreur(chargement.getNombreLigne()-(chargement.getNombreLigneCharge()));
            chrgTransf.setCreateBy(chargement.getCreatedBy());
            chrgTransf.setLastModifiedBy(chargement.getLastModifiedBy());
        }
        StatutLigneChargement statutLigne = StatutLigneChargement.NC;
        if (statut.equals("ACCT")){
            statutLigne = StatutLigneChargement.OC;
            lignes = ligneChargementFichierRepository.queryDetailListByStatut(idChargement, statutLigne);
        }else {
            StatutLigneChargement statutLigne1= StatutLigneChargement.IC;
            lignes = ligneChargementFichierRepository.queryDetailListByStatut1(idChargement, statutLigne, statutLigne1);
        }

        log.debug("------lgne chargement-----" + lignes);

        for (LigneChargementFichier l : lignes) {
            if (!l.getStatutLigne().equals(StatutLigneChargement.OC)) {
                xLigneChTr.setNom(l.getNom());
                xLigneChTr.setTelephone(l.getTelephone());
                xLigneChTr.setErreur(l.getError());
                xLigneChTr.setIdentifiant(l.getIdentifiant());
                xLigneChTr.setInfo(l.getInfo());
                listeChrg.add(xLigneChTr);
                xLigneChTr = new XLigneChargerFichier();
            }
            if (l.getStatutLigne().equals(StatutLigneChargement.OC)) {
                xLigneChTr.setNom(l.getNom());
                xLigneChTr.setTelephone(l.getTelephone());
                xLigneChTr.setErreur(l.getError());
                xLigneChTr.setIdentifiant(l.getIdentifiant());
                xLigneChTr.setInfo(l.getInfo());
                listeChrg.add(xLigneChTr);
                xLigneChTr = new XLigneChargerFichier();

            }
        }
        chrgTransf.setLogo(new File(Constants.LOGO_URL).getAbsolutePath());
        chrgTransf.setLigneCharger(listeChrg);

        if (ReportFormat.XLSX.equals(ReportFormat.valueOf(format)) || ReportFormat.CSV.equals(ReportFormat.valueOf(format))) {
            view.setUrl("classpath:/report/chargementReussiMj.jrxml");
            if (statut.equals("RJCT")){
                view.setUrl("classpath:/report/chargementFichierEchoue_excel.jrxml");
            }else{
                view.setUrl("classpath:/report/chargementFichierReussi_excel.jrxml");
            }

        } else {

            if (statut.equals("RJCT")){
                view.setUrl("classpath:/report/chargementFichierEchoue.jrxml");
            }else{
                view.setUrl("classpath:/report/chargementFichierReussi.jrxml");
            }

        }

        log.debug("==============================================={}",chrgTransf);
        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(chrgTransf), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);
    }

    public ModelAndView getEtatBCEAO(ApplicationContext context,EtatParams cli){
        AbstractJasperReportsView view = ReportUtils.getJasperView(ReportFormat.valueOf(cli.getFormat()));
        Map<String, Object> params = new HashMap<>();
        if (cli.getDateDebut() != null && cli.getDateFin() != null)
        {
            Integer nbrCptOuvert=clientRepository.nombreCompteOuvert(cli.getDateDebut(),cli.getDateFin());

        }

        view.setApplicationContext(context);
        JRDataSource dataSource = new JRBeanCollectionDataSource(Collections.singletonList(null), false);
        params.put(PARAM_DATASOURCE, dataSource);
        return new ModelAndView(view, params);

    }

}
