package com.m2i.pti.reporting.web;


import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.m2i.pti.mobile.dto.MParametre;
import com.m2i.pti.reporting.ReportFormat;
import com.m2i.pti.reporting.dto.*;
import com.m2i.pti.reporting.service.EtatStatsService;
import com.m2i.pti.service.dto.LotDTO;
import io.swagger.models.Model;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * REST controller for managing .
 */
@RestController
@RequestMapping("api/etat")
public class EtatStatsResource {
    private final Logger log = LoggerFactory.getLogger(OperationStatsResource.class);
    private final ApplicationContext appContext;
    private final EtatStatsService etatStatsService;

    public EtatStatsResource(ApplicationContext appContext, EtatStatsService etatStatsService) {
        this.appContext = appContext;
        this.etatStatsService = etatStatsService;
    }
    @PostMapping("/transaction/")
    @Timed
    public ModelAndView getTrans(@RequestBody ParamEtat paramEtat){
            return     etatStatsService.getTrans(appContext, paramEtat.getDateDebut(), paramEtat.getDateFin(),paramEtat.getIdClient(), paramEtat.getFormat());
    }
//    @PostMapping("balance_cpt/client")
//    @Timed
//        public ModelAndView getBalanceCli(){
//            return etatStatsService.getBalanceCli(appContext);
//    }

    @PostMapping("balance_cpt/etat")
    @Timed
    public ModelAndView getBalanceAgent(@RequestBody BalanceEtat balanceEtat){
        return etatStatsService.getBalanceAgent(appContext,balanceEtat.getType(),balanceEtat.getFse(),balanceEtat.getFormat());
    }

    @PostMapping("listing/client")
    @Timed
    public ModelAndView getListing(@RequestBody BalanceEtat balanceEtat){
        return etatStatsService.getListing(appContext,balanceEtat.getType(), balanceEtat.getFormat());
    }

    @PostMapping("listing/client/filtre")
    @Timed
    public ModelAndView getListingFiltre(@RequestBody ClientFiltre cli){
        return etatStatsService.getListingFiltreOptimise(appContext, cli.getTelephone(), cli.getCode(),cli.getAddresse(),cli.getType(),cli.getNom(),cli.getPrenom(),cli.getRaisonSociale(), cli.getNumeroPiece() ,cli.getFormat(),null,cli.getDateDebut(),cli.getDateFin(),cli.getStatut());
    }

    @PostMapping("listing/unite")
    @Timed
    public ModelAndView getListUO(@RequestBody UniteEtat uniteEtat){
        return etatStatsService.getListUO(appContext,uniteEtat.getLibelleUniteOrganisation(),uniteEtat.getCodeUO(),uniteEtat.getNumeroOrdre(),
                uniteEtat.getTypeUO(),uniteEtat.getPereUoId(),uniteEtat.getGroupeId(),uniteEtat.getVilleId(),uniteEtat.getAdresse(),uniteEtat.getDateDebut(),uniteEtat.getDateFin(), uniteEtat.getFormat());
    }

    @PostMapping("listing/unite-date")
    @Timed
    public ModelAndView getListOfUoIntervalle(@RequestBody UniteEtat uniteEtat){
        return etatStatsService.getListOfUODate(appContext,uniteEtat.getDateDebut(),uniteEtat.getDateFin(),uniteEtat.getTypeUO(), uniteEtat.getFormat());

    }

    @PostMapping("listing/operation")
    @Timed
    public ModelAndView getOperation(@RequestBody EtatOperation etatOperation){
        return etatStatsService.getOperation(appContext,etatOperation.getType(), etatOperation.getFormat());
    }
    @PostMapping("balance_cpt/client")
    @Timed
    public ModelAndView getBal(@RequestBody BalanceEtat balanceEtat){
        return etatStatsService.newReport(appContext,balanceEtat.getFormat());
    }

    @PostMapping("chargement-transfert/etat/rapportChargement")
    @Timed
    public ModelAndView getChargement(@RequestBody EtatOperation etatOperation ) {
            return etatStatsService.getChrgmtRapport(appContext,etatOperation.getIdChargement(),etatOperation.getFormat());
    }

    
    @PostMapping("chargement-transfert/etat/rapportChargementSelonStatut")
    @Timed
    public ModelAndView getChargementSelonStatut(@RequestBody EtatOperation etatOperation ) {
        log.debug("ChargementSelonStatutIdChargement=========================={}",etatOperation.getIdChargement());
        log.debug("ChargementSelonStatutStatut=========================={}",etatOperation.getStatut());
            return etatStatsService.getChrgmtRapportByStatut(appContext,etatOperation.getIdChargement(),etatOperation.getStatut(),etatOperation.getFormat());
    }
    @PostMapping("chargement-fichier/etat/rapportChargementSelonStatut")
    @Timed
    public ModelAndView getChargementFichierSelonStatut(@RequestBody EtatOperation etatOperation ) {
        log.debug("ChargementSelonStatutIdChargement=========================={}",etatOperation.getIdChargement());
        log.debug("ChargementSelonStatutStatut=========================={}",etatOperation.getStatut());
        return etatStatsService.getChrgmtFichierRapportByStatut(appContext,etatOperation.getIdChargement(),etatOperation.getStatut(),etatOperation.getFormat(),etatOperation.getTypeChargement());
    }



    @PostMapping("chargement-compte/etat/rapportChargement")
    @Timed
    public ModelAndView getChargementCpt(@RequestBody EtatOperation etatOperation ) {
        return etatStatsService.getChrgmtRapportCpt(appContext,etatOperation.getIdChargement(),etatOperation.getFormat());
    }
    @PostMapping("qrCode/UO")
    @Timed
    public ModelAndView getQrCodeUO(@RequestBody EtatOperation etatOperation ) throws JsonProcessingException {
        return etatStatsService.qrCodeUo(appContext,etatOperation.getIdChargement());
    }
    @PostMapping("listing/facturier")
    @Timed
    public ModelAndView getFacturiersList(@RequestBody XFactureFiltre xFactureFiltre) {
        return etatStatsService.getFacturierListe(appContext,xFactureFiltre);
    }
    @PostMapping("factures/listing")
    @Timed
    public ModelAndView getFactures(@RequestBody XFactureDTO xFactureDTO ) {
        return etatStatsService.getFacture(appContext,xFactureDTO);
    }
    @PostMapping("export/solde")
    @Timed
    public ModelAndView getAllsolde(@RequestBody UniteEtat uniteEtat ) {
        return etatStatsService.getExportSolde(appContext,uniteEtat);
    }
    @PostMapping("export/rapportReglement")
    @Timed
    public ModelAndView getRapportReglement(@RequestBody UniteEtat uniteEtat ) {
        return etatStatsService.getRapportReglement(appContext,uniteEtat);
    }

    @PostMapping("export/transactionOfCompteAutre")
    @Timed
    public ModelAndView getTransactionOfCompteAutre(@RequestBody XDataUnique xDataUnique) {
        return etatStatsService.getTransactionOfCompteAutre(appContext,xDataUnique.getCompteAutreId(),xDataUnique.getFormat(),xDataUnique.getType(),xDataUnique.getDateDebut(),xDataUnique.getDateFin());
    }

    @PostMapping("export/historiqueSolde")
    @Timed
    public ModelAndView getHistoriqueSolde(@RequestBody XHistoriDTO xHistoriDTO) {
        return etatStatsService.getHistoriqueSolde(appContext,xHistoriDTO);
    }

    @GetMapping("export/listCompte")
    @Timed
    public ModelAndView getlistUser() {
        InfosFormulaire infosFormulaire = new InfosFormulaire();
        infosFormulaire.setReportFormat(ReportFormat.XLSX);
        return etatStatsService.getListAuditEventOfProfil(appContext,infosFormulaire);
    }

    @GetMapping("export/listComptee")
    @Timed
    public ModelAndView getlistUserr() {
        InfosFormulaire infosFormulaire = new InfosFormulaire();
        infosFormulaire.setReportFormat(ReportFormat.PDF);
        return etatStatsService.getListAuditEventOfProfil(appContext,infosFormulaire);
    }

    @PostMapping("export/list-compte-bloque-cloture")
    @Timed
    public ModelAndView getlistCompteBloqueOrCloture(@RequestBody InfosFormulaire infosFormulaire) {
        return etatStatsService.getListComptes(appContext,infosFormulaire);
    }

    @PostMapping("export/list-beneficiaire")
    @Timed
    public ModelAndView getlistBeneficiaire(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getListBeneficiaire(appContext,infos);
    }

    @PostMapping("export/list-compte-en-instance")
    @Timed
    public ModelAndView getListCompteInstance(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getListCompteInInstance(appContext,infos);
    }

    @PostMapping("export/list-user")
    @Timed
    public ModelAndView getListUser(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getListUser(appContext,infos);
    }
    @PostMapping("export/list-profil-with-role")
    @Timed
    public ModelAndView getListProfilWithRole(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getListProfilWithRole(appContext,infos);
    }

    @PostMapping("export/listing/users")
    @Timed
    public ModelAndView getListeOfUsers(@RequestBody XUserDTO xUserDTO){
        return etatStatsService.getListOfUserNew(appContext,xUserDTO);
    }

    @PostMapping("export/list-compte-inactif-pour-une-periode")
    @Timed
    public ModelAndView getListCompteInactidDepuisUnePeriodeDonnee(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getListCompteInactifInPeriode(appContext,infos);
    }

    @PostMapping("export/list-transaction-pending")
    @Timed
    public ModelAndView getlistTransactionPending(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getListTransactionPending(appContext,infos);
    }

    @PostMapping("export/list-connexion-failed")
    @Timed
    public ModelAndView getListConnexionFailed(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getUserWithConnexionDenied(appContext,infos);
    }

    @PostMapping("export/list-user-profil")
    @Timed
    public ModelAndView getListUserProfil(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getListUserProfil(appContext,infos);
    }

    @PostMapping("export/rapport-audit-event-user")
    @Timed
    public ModelAndView getListAuditEvent(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getListAuditEvent(appContext,infos);
    }

    @PostMapping("export/audit-event-profil")
    @Timed
    public ModelAndView getProfilAuditEvent(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getListAuditEventOfProfil(appContext,infos);
    }

    @PostMapping("export/journal-transaction")
    @Timed
    public ModelAndView getJournalTransactionPending(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getJournalTransaction(appContext,infos);
    }

    @PostMapping("etat-compte/client")
    @Timed
    public ModelAndView getEtatCompte(@RequestBody EtatParams cli){
        return etatStatsService.getEtatCompte(appContext, cli);
    }
    @PostMapping("lots/export")
    @Timed
    public StringBuilder exportLot(@RequestBody LotDTO lotDTO ) {
        return etatStatsService.exportLot(lotDTO);
    }
    
    @PostMapping("/lots/exportZip")
    @Timed
    public ResponseEntity<byte[]> exportLotZip(@RequestBody LotDTO lotDTO) {
        // 1. Génération des fichiers (CSV + images)
        StringBuilder csvContent = etatStatsService.exportLot(lotDTO); // garde ton service existant
        String pathname=null;
        String nameFile="clients_lot.csv";
        if (csvContent == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Récupérer le CSV généré dans le dossier
            if(lotDTO.getTypeActeur()!=null && lotDTO.getTypeActeur().equals("ORGANISATION")){
                pathname = System.getProperty("user.home") + "/organisation/lot" + lotDTO.getId();
                nameFile="organisations_lot.csv";
            }else {
                 pathname = System.getProperty("user.home") + "/client/lot" + lotDTO.getId();
            }
            File csvFile = new File(pathname + "/"+nameFile);
            if (csvFile.exists()) {
                try (FileInputStream fis = new FileInputStream(csvFile)) {
                    ZipEntry csvEntry = new ZipEntry(nameFile);
                    zos.putNextEntry(csvEntry);
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                }
            } else {
                // Optionnel : fallback si le CSV n'existe pas
                ZipEntry csvEntry = new ZipEntry(nameFile);
                zos.putNextEntry(csvEntry);
                zos.write(csvContent.toString().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            // Ajouter les images générées (QR codes)
            File folder = new File(pathname);
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".png") || name.endsWith(".jpg"));
            if (files != null) {
                for (File file : files) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ZipEntry imageEntry = new ZipEntry(file.getName());
                        zos.putNextEntry(imageEntry);
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, len);
                        }
                        zos.closeEntry();
                    }
                }
            }

            zos.finish();
            byte[] zipBytes = baos.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=lot_" + lotDTO.getId() + ".zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipBytes);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("etat-compte/periode")
    @Timed
    public ModelAndView getComptesMonnaieElectroniquePeriode(@RequestBody EtatParams cli){
        return etatStatsService.getComptesMonnaieElectroniquePeriode(appContext, cli);
    }

    @PostMapping("etat-compte/titulaires")
    @Timed
    public ModelAndView getTitulairesComptesMonnaieElectronique(@RequestBody EtatParams cli){
        return etatStatsService.getTotalTitulairesComptes(appContext, cli);
    }
    @PostMapping("etat-compte/actif-in-3-months")
    @Timed
    public ModelAndView getComptesMonnaieElectroniqueActifsIn3mois(@RequestBody EtatParams cli){
        return etatStatsService.getCompteActifIn3mois(appContext, cli);
    }
    @PostMapping("etat-compte/adultes")
    @Timed
    public ModelAndView getComptesMonnaieElectroniqueAdultes(@RequestBody EtatParams cli){
        return etatStatsService.getTotalTitulairesComptesAdultes(appContext, cli);
    }

    @PostMapping("etat-compte/femmes")
    @Timed
    public ModelAndView getComptesMonnaieElectroniqueFemmes(@RequestBody EtatParams cli){
        return etatStatsService.getTotalTitulairesComptesFemmes(appContext, cli);
    }

    @PostMapping("etat-compte/femmes-adultes")
    @Timed
    public ModelAndView getComptesMonnaieElectroniqueFemmesAdultes(@RequestBody EtatParams cli){
        return etatStatsService.getTotalTitulairesComptesFemmesAdultes(appContext, cli);
    }

    @PostMapping("export/liste-points-vente-inactifs")
    @Timed
    public ModelAndView getListPointVenteInactifs(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getListPointVenteInactifs(appContext,infos);
    }

    @PostMapping("export/liste-points-vente-non-actives")
    @Timed
    public ModelAndView getListPointVenteNonActives(@RequestBody InfosFormulaire infos) {
        return etatStatsService.getListPointVenteNonActives(appContext,infos);
    }

    @PostMapping("export/liste-beneficiaires-transfert-non-titulaires-de-porte-monnaie-électronique")
    @Timed
    public ModelAndView getListBeneficiaireNonTitulairePME(@RequestBody EtatParams cli) {
        return etatStatsService.getListBeneficiairesNonTitulairePME(appContext, cli);
    }
    @PostMapping("export/liste-sous-distribibuteurs")
    @Timed
    public ModelAndView getListSousDistributeurs(@RequestBody EtatParams cli) {
        return etatStatsService.modelListSousDistributeurs(appContext, cli);
    }
    @PostMapping("export/liste-banque-actif")
    @Timed
    public ModelAndView getListBanqueActif(@RequestBody EtatParams cli) {
        return etatStatsService.listeBanqueActif(appContext, cli);
    }
    /*@PostMapping("export/paiement_recu")
    @Timed
    public ModelAndView getRecuPaiement(@RequestBody List<MParametre> mParametres){
        return etatStatsService.printRecu(appContext,mParametres);
    }*/
    @PostMapping("export/opearion-suivis-client")
    @Timed
    public ModelAndView getListeOperationSuivisClient(@RequestBody ParamEtat paramEtat){
        return etatStatsService.listeOperationSuivisClient(appContext,paramEtat);
    }

    @PostMapping("export/liste-points-ventes-actifs")
    @Timed
    public ModelAndView getListePointVenteActif(@RequestBody ParamEtat paramEtat){
        return etatStatsService.listePointVenteActifs(appContext,paramEtat);
    }

    @PostMapping("/compte-historique")
    @Timed
    public ModelAndView getHistoriqueCompte(@RequestBody EtatItems item){
        return etatStatsService.historiqueCompte(appContext,item);
    }

    @PostMapping("etat-compte/actif-agent-in-3-months")
    @Timed
    public ModelAndView getComptesMonnaieElectroniqueActifsAgentIn3mois(@RequestBody EtatParams cli){
        return etatStatsService.getCompteActifAgentIn3mois(appContext, cli);
    }
    @PostMapping("etat-compte/agent-validation")
    @Timed
    public ModelAndView getEtatCompteAgentValidation(@RequestBody EtatParams cli){
        return etatStatsService.getEtatCompteAgentValidation(appContext, cli);
    }
    @PostMapping("etat-compte/etat-bceao")
    @Timed
    public ModelAndView getEtatBCEAO(@RequestBody EtatParams cli){
        return etatStatsService.getEtatBCEAO(appContext, cli);
    }

}
