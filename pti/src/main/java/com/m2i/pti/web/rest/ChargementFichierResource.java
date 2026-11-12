package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.outils.restclient.model.MessageInt;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.mobile.dto.MResponse;
import com.m2i.pti.repository.ChargementFichierRepository;
import com.m2i.pti.repository.GroupeProfilActeurRepository;
import com.m2i.pti.repository.ServiceProfilRepository;
import com.m2i.pti.repository.UniteOrganisationRepository;
import com.m2i.pti.service.TypeUniteOrganisationService;
import com.m2i.pti.service.UniteOrganisationService;
import com.m2i.pti.service.dto.UniteOrganisationDTO;
import com.m2i.pti.service.easyBatch.*;
import com.m2i.pti.service.fichier.ChargementOrganisationService;
import com.m2i.pti.service.fichier.LigneFichierService;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import org.easybatch.core.job.Job;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.pentaho.di.core.exception.KettleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class ChargementFichierResource {
    private final Logger log = LoggerFactory.getLogger(ChargementFichierResource.class);

    private static final String ENTITY_NAME = "chargementFichier";
    private final Read readService;
    private final ChargementFichierRepository chargementFichierRepository;
    ReaderOrganisationService readerOrganisationService;
    private static final String CHARGEMENT_PATTERN = "^(?:[0-9]+;?[0-9]+;?(.+)@(.+)+;[a-zA-Z]+$)";
    private final LigneFichierService ligneFichierService;
    private final ChargementOrganisationService chargementOrganisationService;
    private final UniteOrganisationRepository uniteOrganisationRepository;
    final UniteOrganisationService uniteOrganisationService;
    final TypeUniteOrganisationService typeUniteOrganisationService;
    final GroupeProfilActeurRepository groupeProfilActeurRepository;
    final ServiceProfilRepository serviceProfilRepository;

    public ChargementFichierResource(Read readService, ChargementFichierRepository chargementFichierRepository, ReaderOrganisationService readerOrganisationService, LigneFichierService ligneFichierService, ChargementOrganisationService chargementOrganisationService, UniteOrganisationRepository uniteOrganisationRepository, UniteOrganisationService uniteOrganisationService, TypeUniteOrganisationService typeUniteOrganisationService, GroupeProfilActeurRepository groupeProfilActeurRepository, ServiceProfilRepository serviceProfilRepository) {
        this.readService = readService;
        this.chargementFichierRepository = chargementFichierRepository;
        this.readerOrganisationService = readerOrganisationService;
        this.ligneFichierService = ligneFichierService;
        this.chargementOrganisationService = chargementOrganisationService;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.uniteOrganisationService = uniteOrganisationService;
        this.typeUniteOrganisationService = typeUniteOrganisationService;
        this.groupeProfilActeurRepository = groupeProfilActeurRepository;
        this.serviceProfilRepository = serviceProfilRepository;
    }

    @PostMapping("/upload/chargement_fichier/fichier")
    public ResponseEntity<Void> singleFileUpload(@RequestParam("file") MultipartFile file,Long organisationId,String typeUO,Long profilId,Long serviceId) throws IOException {
        OrganisationAssembleur organisationAssembleur=new OrganisationAssembleur();
        List<CsvOrganisation> listeOrg= new ArrayList<>();
        try {
            String type = file.getContentType();
            if (type.equals("text/plain") || type.equals("text/csv") || type.equals("application/vnd.ms-excel")) {

                String hash = readerOrganisationService.hashFile(file.getInputStream());
                Integer nbreHash = chargementOrganisationService.findByHashFichier(hash);
                Integer nbreHashNomFile = chargementOrganisationService.findByNomFichier(file.getOriginalFilename().toUpperCase());
                /*
                Enregistrement sur le disque
                */
                File fichier = readerOrganisationService.convert(file);
                if ((nbreHash > 0) && (nbreHashNomFile > 0)) {
                    log.debug("Fichier existe");

                    return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "Le fichier a ete deja charge")).body(null);
                } else {
                    ChargementFichier chargementFichier = new ChargementFichier();
                    chargementFichier.setDateChargement(ZonedDateTime.now().toLocalDate());
                    chargementFichier.setDateFichier(ZonedDateTime.now());
                    chargementFichier.setTypeFichier("ORGANISATION_MASSE");
                    chargementFichier.setUniteOrganisation(uniteOrganisationRepository.findById(organisationId).orElse(null));
                    organisationAssembleur = new OrganisationAssembleur();
                    listeOrg = readerOrganisationService.read(file.getInputStream(), organisationAssembleur, CHARGEMENT_PATTERN);
                    System.out.println("------------size-----"+listeOrg.size());
                    chargementFichier.setNomFichier(file.getOriginalFilename().toUpperCase());
                    chargementFichier.setHashFichier(hash);
                    chargementFichier.setStatut("N");
                    chargementFichier.setNombreLigne(listeOrg.size());
                    chargementFichier.setNombreLigneCharge(listeOrg.size());
                    chargementFichier.setNombreLigneErreur(0);
                    chargementFichier.setProfilId(profilId);
                    chargementFichier.setServiceId(serviceId);
                    ChargementFichier result = chargementFichierRepository.save(chargementFichier);
                    if (result.getId() > 0) {
                        ChargementFichierResource.ReportBatch rpBatch = new ChargementFichierResource.ReportBatch();
                        Job job = (Job) readService.getBatch(listeOrg, new ChargementFichierResource.ReadFileProcessor(result, ligneFichierService,typeUO));
                        result.setReference(ChargementFichierResource.padLeftZeros(result.getId().toString(), 10));
                        chargementFichierRepository.save(result);
                    }
                    return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "OK")).build();

                }
            }else {
                return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "Format de fichier incorrect")).body(null);
            }
        } catch (IOException e) {
            log.debug(e.getMessage());
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", e.getMessage())).body(null);
        }
    }
    private class ReportBatch {

        Integer nombreLigneCharge;
        Integer nombreLigneErreur;
        public ReportBatch(Integer nombreLigneCharge, Integer nombreLigneErreur) {
            this.nombreLigneCharge = nombreLigneCharge;
            this.nombreLigneErreur = nombreLigneErreur;
        }

        public ReportBatch() {
        }

        public Integer getNombreLigneCharge() {
            return nombreLigneCharge;
        }

        public void setNombreLigneCharge(Integer nombreLigneCharge) {
            this.nombreLigneCharge = nombreLigneCharge;
        }

        public Integer getNombreLigneErreur() {
            return nombreLigneErreur;
        }

        public void setNombreLigneErreur(Integer nombreLigneErreur) {
            this.nombreLigneErreur = nombreLigneErreur;
        }

    }
    @Transactional
    public static class ReadFileProcessor implements RecordProcessor {
        AuthTokenInfo tokeinfo;
        ChargementFichierResource.ReportBatch rpBatch;
        private final LigneFichierService ligneFichierService;
        ChargementFichier chargementFichier;
        String typeUO;


        public ReadFileProcessor(ChargementFichier chargementFichier, LigneFichierService ligneFichierService,String typeUO) {


            this.ligneFichierService = ligneFichierService;
            this.chargementFichier = chargementFichier;
            this.typeUO=typeUO;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            CsvOrganisation csvOrganisation = (CsvOrganisation) record.getPayload();
            LigneChargementFichier ligneChargementFichier = new LigneChargementFichier();

            ligneChargementFichier.setChargementFichier(this.chargementFichier);
            ligneChargementFichier.setStatutLigne(StatutLigneChargement.NC);
            ligneChargementFichier.setNom(csvOrganisation.getNom());
            ligneChargementFichier.setTelephone(csvOrganisation.getTelephone());
            ligneChargementFichier.setType(this.typeUO);
            LigneChargementFichier lignC = ligneFichierService.save(ligneChargementFichier);
            lignC.setRrn(padLeftZeros("C" + lignC.getId().toString(), 10));
            /**
             * Mise à jour du rrn
             */
            // lignC.setRrn(padLeftZeros("C" + lignC.getId().toString(), 10));
            ligneFichierService.save(lignC);

            return record;
        }
    }
    private static String padLeftZeros(String str, int n) {
        return String.format("%1$" + n + "s", str).replace(' ', '0');
    }

    @GetMapping("/chargement-fichiers/valider/{id}/{user}/{canal}")
    @Timed
    public ResponseEntity<Void> setValidateChargement(@PathVariable Long id, @PathVariable String user, @PathVariable String canal) throws IOException, JAXBException, InterruptedException, ExecutionException, KettleException {
        log.debug("debut du tranfert");
        ChargementFichier chargement = chargementOrganisationService.findOne(id);
        Long pereUoId=null;
        if (chargement.getUniteOrganisation()!=null && chargement.getUniteOrganisation().getId()!=null){
            pereUoId=chargement.getUniteOrganisation().getId();
        }

        if (chargement.getStatut().equals("I")) {

            return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "Chargement encours de validation. ")).build();

        }
        chargement.setStatut("I");
        chargementOrganisationService.save(chargement);
        ChargementFichierResource.ReportBatch rpBatch = new ChargementFichierResource.ReportBatch(chargement.getNombreLigneCharge(), chargement.getNombreLigneErreur());
        List<LigneChargementFichier> ligneChargementFichiers = null;
        ligneChargementFichiers = ligneFichierService.findByChargementFichierIdAndStatutLigneChargement(id,StatutLigneChargement.NC);
        Job job = (Job) readService.getBatch(ligneChargementFichiers, new ChargementFichierResource.ChargementProcessor(uniteOrganisationService,typeUniteOrganisationService,groupeProfilActeurRepository,serviceProfilRepository, ligneFichierService, rpBatch,chargement.getProfilId(),pereUoId,chargement.getServiceId()));
        chargement.setStatut("V");

        chargementOrganisationService.save(chargement);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "Validation effectuée")).build();


    }
    private static class ChargementProcessor implements RecordProcessor {

        boolean error = false;
        boolean errorT = false;
        MessageInt resultat = null;
        Integer isValide = null;
        ResponseEntity result;
        ResponseEntity resultatC;
        Integer nombreLigneCharge = 0;
        Integer nombreLigneErreur = 0;
        Long profil;
        Long pereUoId;
        Long serviceId;
        ChargementFichierResource.ReportBatch rpBatch;
        UniteOrganisationService uniteOrganisationService;
        TypeUniteOrganisationService typeUniteOrganisationService;
        GroupeProfilActeurRepository groupeProfilActeurRepository;
        ServiceProfilRepository serviceProfilRepository;
        private final LigneFichierService ligneFichierService;

        public ChargementProcessor(UniteOrganisationService uniteOrganisationService,TypeUniteOrganisationService typeUniteOrganisationService,GroupeProfilActeurRepository groupeProfilActeurRepository,ServiceProfilRepository serviceProfilRepository,LigneFichierService ligneFichierService, ChargementFichierResource.ReportBatch rpBatch, Long profil,Long pereUoId,Long serviceId) {

            this.ligneFichierService = ligneFichierService;
            this.uniteOrganisationService=uniteOrganisationService;
            this.typeUniteOrganisationService=typeUniteOrganisationService;
            this.groupeProfilActeurRepository=groupeProfilActeurRepository;
            this.serviceProfilRepository=serviceProfilRepository;
            this.rpBatch = rpBatch;
            this.profil = profil;
            this.serviceId=serviceId;
            this.pereUoId=pereUoId;
            nombreLigneCharge = rpBatch.getNombreLigneCharge();
            nombreLigneErreur = rpBatch.getNombreLigneErreur();
            resultatC = null;
            result = null;
        }

        @Override
        public Record processRecord(Record record) throws Exception {

            LigneChargementFichier ligneChargementFichier = (LigneChargementFichier) record.getPayload();

            Pattern p = Pattern.compile(CHARGEMENT_PATTERN);
            if(ligneChargementFichier.getStatutLigne().equals(StatutLigneChargement.NC)) {
                ligneChargementFichier.setStatutLigne(StatutLigneChargement.IC);
            }

            LigneChargementFichier lgCharge = ligneChargementFichier;
            error = false;
            ligneFichierService.save(lgCharge);
            /**
             * vérification du numéro de la carte après décryptage
             */
            Matcher m;
            boolean bool = false;

            try {

                if (ligneChargementFichier.getStatutLigne().equals(StatutLigneChargement.IC)) {
                    ResultMessage resMessage;  //  (ResultMessage)apiService.convert(result.getBody(),ResultMessage.class);
                    ResultMessage resMessageEnvoiMsg;
                    DecimalFormat df = new DecimalFormat("###");
                    UniteOrganisationDTO organisation = new UniteOrganisationDTO();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    organisation.setLibelleUniteOrganisation(ligneChargementFichier.getNom());
                    organisation.setTelephone(ligneChargementFichier.getTelephone());
                    organisation.setEstActif(true);
                    organisation.setPereUOId(pereUoId);
                    System.out.println("getType------------"+ligneChargementFichier.getType());
                    if(ligneChargementFichier.getType()!=null){
                        TypeUniteOrganisation typeUniteOrganisation=typeUniteOrganisationService.findByCodeType(ligneChargementFichier.getType());
                        if(typeUniteOrganisation!=null &&typeUniteOrganisation.getId()!=null){
                            organisation.setTypeUOId(typeUniteOrganisation.getId());
                        }
                    }
                    if(profil!=null){
                        GroupeProfilActeur groupeProfilActeur=groupeProfilActeurRepository.findById(profil).orElse(null);
                        if(groupeProfilActeur!=null && groupeProfilActeur.getId()!=null){
                            organisation.setGroupeProfilActeurId(groupeProfilActeur.getId());
                            organisation.setGroupeProfilActeurNom(groupeProfilActeur.getNom());
                        }
                    }
                    if(serviceId!=null){
                        ServiceProfil serviceProfil=serviceProfilRepository.findById(serviceId).orElse(null);
                        if(serviceProfil!=null && serviceProfil.getId()!=null){
                            organisation.setServiceProfilId(serviceProfil.getId());
                            organisation.setServiceProfilNom(serviceProfil.getNom());
                        }
                    }
                    UniteOrganisationDTO org=uniteOrganisationService.saveNew(organisation);
                    MResponse rep = new MResponse() ;
                    System.out.println("---------------");
                    System.out.println("---------------"+org);
                    System.out.println("---------------"+org.getId());
                    if(org!=null && org.getId()!=null){
                        rep.setCode("0");
                        ligneChargementFichier.setStatutLigne(StatutLigneChargement.OC);
                    }else {
                        rep.setCode("-1");
                    }
                    nombreLigneCharge = nombreLigneCharge + 1;
                    error = false;
                    if (rep.getCode().equals("0")) {
                        ligneChargementFichier.setError(null);
                        ligneChargementFichier.setInfo(organisation.getLibelleUniteOrganisation());
                        ligneChargementFichier.setIdentifiant(organisation.getCodeUO());
                    } else {
                        error = true;
                        ligneChargementFichier.setError(rep.getMessage());
                    }
                    /**
                     *
                     * Faire le chargement
                     */
                } else {
                    error = true;
                    ligneChargementFichier.setError("Organisation  dejà cree");
                }

            } catch (Exception e) {
                error = true;
                System.out.println("Echec-------" + e.getMessage());
                ligneChargementFichier.setError(e.getMessage());

            }

            if (error) {
                nombreLigneErreur = nombreLigneErreur + 1;
            }

            rpBatch.setNombreLigneCharge(nombreLigneCharge);
            rpBatch.setNombreLigneErreur(nombreLigneErreur);
            ligneFichierService.save(ligneChargementFichier);

            return record;
        }
    }
    @GetMapping("/chargement-fichiers")
    @Timed
    public List<ChargementFichier> getAllChargementTransferts() {
        log.debug("REST request to get all ChargementFichier");
        return chargementFichierRepository.findAllOrderByDateDesc();
    }

    @GetMapping("/chargement-fichiers/chargementByOrganisation/{id}")
    @Timed
    public ResponseEntity<List<ChargementFichier>> getChargementFichierById(@PathVariable Long id, Pageable pageable) {
        log.debug("REST request to get ChargementFichier : {}", id);
        Page<ChargementFichier> page = chargementOrganisationService.findChargementPage(id, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/chargement-fichier");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
