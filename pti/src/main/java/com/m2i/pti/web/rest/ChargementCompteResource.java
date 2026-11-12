package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.omnicanal.outils.restclient.AuthTokenInfo;
import com.m2i.omnicanal.outils.restclient.model.MessageInt;
import com.m2i.omnicanal.outils.restclient.model.ResultMessage;
import com.m2i.pti.domain.ChargementCompte;
import com.m2i.pti.domain.Compte;

import com.m2i.pti.domain.LigneChargementCompte;
import com.m2i.pti.domain.Operation;
import com.m2i.pti.domain.ParametreApp;
import com.m2i.pti.domain.enumeration.StatutLigneChargement;
import com.m2i.pti.domain.enumeration.TypeParametre;
import com.m2i.pti.mobile.dto.MClient;
import com.m2i.pti.mobile.dto.MDocIdentification;
import com.m2i.pti.mobile.dto.MPersonne;
import com.m2i.pti.mobile.dto.MResponse;
import com.m2i.pti.mobile.dto.MTypePiece;
import com.m2i.pti.mobile.enumeration.MSexe;
import com.m2i.pti.mobile.service.MobileService;
import com.m2i.pti.repository.ChargementCompteRepository;
import com.m2i.pti.repository.CompteRepository;
import com.m2i.pti.repository.PersonneRepository;
import com.m2i.pti.service.ChargementCompteService;
import com.m2i.pti.service.LigneChargementCompteService;

import com.m2i.pti.service.ParametreAppService;
import com.m2i.pti.service.dto.PersonneDTO;
import com.m2i.pti.service.easyBatch.*;
import com.m2i.pti.service.easyBatch.Read;
import com.m2i.pti.service.easyBatch.ReaderCompteService;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.easybatch.core.job.Job;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.time.LocalDate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * REST controller for managing ChargementCompte.
 */
@RestController
@RequestMapping("/api")
public class ChargementCompteResource {

    private final Logger log = LoggerFactory.getLogger(ChargementCompteResource.class);

    private static final String ENTITY_NAME = "chargementCompte";

    private final ChargementCompteService chargementCompteService;

    private final LigneChargementCompteService ligneChargementCompteService;

    private final ChargementCompteRepository chargementCompteRepository;

    private final ParametreAppService parametreAppService;
    private final PersonneRepository personneRepository;

    final MobileService mobileService;
    ReaderCompteService readerCompteService;

    private static final String CHARGEMENT_PATTERN = "^(?:[a-zA-Z]+;[a-z A-Z]+;[0-9]+;[0-3]?[0-9]-[0-3]?[0-9]-(?:[0-9]{2})?[0-9]{2};[a-z A-Z]+;[a-z A-Z]+;[a-z A-Z 0-9]+;(.+)@(.+)+;[a-z A-Z 0-9]+;[A-Z]+$)";
//    private static final String email ="^(.+)@(.+)$";
//    private static final String date ="^[0-3]?[0-9]/[0-3]?[0-9]/(?:[0-9]{2})?[0-9]{2}$";
//    private static final String usecase ="^[a-z A-Z 0-9]+$";
//    private static final String adresse ="^[a-z A-Z 0-9]+$";
    private final Read readService;

    public ChargementCompteResource(MobileService mobileService, ChargementCompteService chargementCompteService, LigneChargementCompteService ligneChargementCompteService, ReaderChargementService readerChargementService, ChargementCompteRepository chargementCompteRepository, ParametreAppService parametreAppService, ReaderCompteService readerCompteService, Read readService,PersonneRepository personneRepository) {
        this.chargementCompteService = chargementCompteService;
        this.ligneChargementCompteService = ligneChargementCompteService;
        this.chargementCompteRepository = chargementCompteRepository;
        this.parametreAppService = parametreAppService;
        this.readerCompteService = readerCompteService;
        this.readService = readService;
        this.mobileService = mobileService;
        this.personneRepository=personneRepository;
    }

    /**
     * POST /chargement-comptes : Create a new chargementCompte.
     *
     * @param chargementCompte the chargementCompte to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new chargementCompte, or with status 400 (Bad Request) if the
     * chargementCompte has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/chargement-comptes")
    @Timed
    public ResponseEntity<ChargementCompte> createChargementCompte(@RequestBody ChargementCompte chargementCompte) throws URISyntaxException {
        log.debug("REST request to save ChargementCompte : {}", chargementCompte);
        if (chargementCompte.getId() != null) {
            throw new BadRequestAlertException("A new chargementCompte cannot already have an ID", ENTITY_NAME, "idexists");
        }
        ChargementCompte result = chargementCompteService.save(chargementCompte);
        return ResponseEntity.created(new URI("/api/chargement-comptes/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT /chargement-comptes : Updates an existing chargementCompte.
     *
     * @param chargementCompte the chargementCompte to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated
     * chargementCompte, or with status 400 (Bad Request) if the
     * chargementCompte is not valid, or with status 500 (Internal Server Error)
     * if the chargementCompte couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/chargement-comptes")
    @Timed
    public ResponseEntity<ChargementCompte> updateChargementCompte(@RequestBody ChargementCompte chargementCompte) throws URISyntaxException {
        log.debug("REST request to update ChargementCompte : {}", chargementCompte);
        if (chargementCompte.getId() == null) {
            return createChargementCompte(chargementCompte);
        }
        ChargementCompte result = chargementCompteService.save(chargementCompte);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, chargementCompte.getId().toString()))
                .body(result);
    }

    /**
     * GET /chargement-comptes : get all the chargementComptes.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of
     * chargementComptes in body
     */
    @GetMapping("/chargement-comptes")
    @Timed
    public ResponseEntity<List<ChargementCompte>> getAllChargementComptes(Pageable pageable) {
        log.debug("REST request to get all ChargementComptes");
        Page<ChargementCompte> page=chargementCompteRepository.findAllOrderByDateChargementPage(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/chargement-compte");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET /chargement-comptes/:id : get the "id" chargementCompte.
     *
     * @param id the id of the chargementCompte to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the
     * chargementCompte, or with status 404 (Not Found)
     */
    @GetMapping("/chargement-comptes/{id}")
    @Timed
    public ResponseEntity<ChargementCompte> getChargementCompte(@PathVariable Long id) {
        log.debug("REST request to get ChargementCompte : {}", id);
        ChargementCompte chargementCompte = chargementCompteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(chargementCompte));
    }

    /**
     * DELETE /chargement-comptes/:id : delete the "id" chargementCompte.
     *
     * @param id the id of the chargementCompte to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/chargement-comptes/{id}")
    @Timed
    public ResponseEntity<Void> deleteChargementCompte(@PathVariable Long id) {
        log.debug("REST request to delete ChargementCompte : {}", id);
        chargementCompteService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    // @Transactional
    @PostMapping("/upload/chargement_compte_client/fichier")
    public ResponseEntity<Void> singleFileUpload5(@RequestParam("file") MultipartFile file,@RequestParam Long profilId) throws IOException, Exception {
        Map<String, byte[]> mapSign = new HashMap();
        List<CsvCompte> listCompte = new ArrayList();
        CompteAssembleur assembleur;
        try {
            /**
             * rechercher dans la table des chargements si le nom existe déjà si
             * oui retourner les informations procéder à vérification de la
             * signature du fichier
             */
            String hash = readerCompteService.hashFile(file.getInputStream());
            Integer nbreHash = chargementCompteService.findByHashFichier(hash);
            Integer nbreHashNomFile = chargementCompteService.findByNomFichier(file.getOriginalFilename().toUpperCase());
            /**
             * Enregistrement sur disque du fichier*
             */
            File fichier = readerCompteService.convert(file);
            if ((nbreHash > 0) && (nbreHashNomFile > 0)) {
                log.debug("Fichier existe");
                return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "Le fichier a ete deja charge")).body(null);
            } else {
                log.debug("debut traitement");
                ChargementCompte chargementCompte = new ChargementCompte();
                chargementCompte.setDateChargement(ZonedDateTime.now().toLocalDate());
                chargementCompte.setKeyPublic("");
                assembleur = new CompteAssembleur();
                listCompte = readerCompteService.read(file.getInputStream(), assembleur, CHARGEMENT_PATTERN);
                chargementCompte.setNombreLigne(listCompte.size());
                chargementCompte.setNombreLigneErreur(0);
                chargementCompte.setStatut("N");
                chargementCompte.setNomFichier(file.getOriginalFilename().toUpperCase());
                chargementCompte.setNombreLigneCharge(0);
                chargementCompte.setHashFichier(hash);
                chargementCompte.setProfilId(profilId);
                Iterator it = listCompte.iterator();
                ChargementCompte result = chargementCompteService.save(chargementCompte);
                if (result.getId() > 0) {

                    // ReportBatch rpBatch = new ReportBatch(0D);
                    Job job = (Job) readService.getBatch(listCompte, new ChargementCompteResource.ReadFileProcessor(result, ligneChargementCompteService,personneRepository,parametreAppService));
                    result.setReference(ChargementCompteResource.padLeftZeros(result.getId().toString(), 10));
                    chargementCompteService.save(result);
                }
                return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "OK")).build();
            }
        } catch (IOException e) {
            log.debug(e.getMessage());
            return ResponseEntity.ok().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", e.getMessage())).body(null);
        }
    }

    private class ReportBatch {

        Integer nombreLigneCharge;
        Integer nombreLigneErreur;
        // Double montant;

        public ReportBatch(Integer nombreLigneCharge, Integer nombreLigneErreur) {
            this.nombreLigneCharge = nombreLigneCharge;
            this.nombreLigneErreur = nombreLigneErreur;
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

    public static class ReadFileProcessor implements RecordProcessor {

        //private Double montant = (0D);
        AuthTokenInfo tokeinfo;
        //ReportBatch rpBatch;

        private final LigneChargementCompteService ligneChargementCompteService;
        private final PersonneRepository personneRepository;
        private final ParametreAppService parametreAppService;
        ChargementCompte chargement;

        public ReadFileProcessor(ChargementCompte chargement, LigneChargementCompteService ligneChargementCompteService,PersonneRepository personneRepository,ParametreAppService parametreAppService) {
            // this.rpBatch = rpBatch;

            this.ligneChargementCompteService = ligneChargementCompteService;
            //this.chargement = chargement;
            this.chargement = new ChargementCompte();
            this.chargement = chargement;
            this.personneRepository=personneRepository;
            this.parametreAppService=parametreAppService;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            CsvCompte csvCompte = (CsvCompte) record.getPayload();

            LigneChargementCompte ligneChargementCompte = new LigneChargementCompte();
            String numeroCarte = "";
            Boolean numeroCorrect=true;
            int ctrlNumero=0;

            /**
             * Il s'agit d'un chargement classic
             */
            int inputNumeroSize=csvCompte.getTelephone().length();
            Integer nbr =personneRepository.countClientByTelephone(csvCompte.getTelephone());
            ParametreApp parametreApp = parametreAppService.findByTypeAndNom(TypeParametre.SYSTEM, "LONGUEUR_NUM_TELEPHONE");
            ligneChargementCompte.setStatutLigne(StatutLigneChargement.IC);
            if (parametreApp != null) {
                String valeur = parametreApp.getValeur();
                int result=Integer.parseInt(valeur);
                ctrlNumero=result;

               if(ctrlNumero!=inputNumeroSize){
                    ligneChargementCompte.setStatutLigne(StatutLigneChargement.NC);
                    ligneChargementCompte.setError("Numéro incorrect");
                }
            }
            if(nbr>=1){
                ligneChargementCompte.setStatutLigne(StatutLigneChargement.NC);
                ligneChargementCompte.setError("Client existant");
            }

            ligneChargementCompte.setCodePays(csvCompte.getCodePays());
            ligneChargementCompte.setChargementCompte(chargement);
            ligneChargementCompte.setNom(csvCompte.getNom());
            ligneChargementCompte.setPrenom(csvCompte.getPrenom());
            ligneChargementCompte.setTelephone(csvCompte.getTelephone());
            ligneChargementCompte.setDateNaissance(csvCompte.getDateNaissance());
            ligneChargementCompte.setLieuNaissance(csvCompte.getLieuNaissance());
            ligneChargementCompte.setNomMere(csvCompte.getNomMere());
            ligneChargementCompte.setPersonnePrevenir(csvCompte.getPersonnePrevenir());
            ligneChargementCompte.setEmail(csvCompte.getEmail());
            ligneChargementCompte.setAdresse(csvCompte.getAdresse());
            ligneChargementCompte.setTypePersonne(csvCompte.getTypePersonne().toUpperCase());
            ligneChargementCompte.setCodeTypePiece(csvCompte.getCodeTypePiece());
            ligneChargementCompte.setReference(csvCompte.getReference());
            ligneChargementCompte.setLieuDelivrance(csvCompte.getLieuDelivrance());
            ligneChargementCompte.setDateDelivrance(convertStringToLocal(csvCompte.getDateDelivrance()));
            ligneChargementCompte.setDateExpiration(convertStringToLocal(csvCompte.getDateExpiration()));
            ligneChargementCompte.setSexe(csvCompte.getSexe());
            LigneChargementCompte lignC = ligneChargementCompteService.save(ligneChargementCompte);

            lignC.setRrn(padLeftZeros("C" + lignC.getId().toString(), 10));
            ligneChargementCompteService.save(lignC);

            //l'appel de la methode avec transactionnal
//            saveLigneChargement(() ->
//                            System.out.println(""),
//                    1000,
//                    csvCompte,
//                    this.chargement,
//                    ligneChargementCompteService
//                   // rpBatch
//            );
            /**
             * Mise à jour du rrn
             */
            return record;
        }
    }
private static LocalDate convertStringToLocal(String date){
     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
     LocalDate localDate = LocalDate.parse(date, formatter);
     return localDate;
}
        
       


//convert String to LocalDate




    private static String padLeftZeros(String str, int n) {
        return String.format("%1$" + n + "s", str).replace(' ', '0');
    }
// implementation de la methode avec transactional
//    public static void saveLigneChargement(
//            Runnable runnable,
//            int delay,
//            CsvCompte csvCompte,
//            ChargementCompte chargement,
//            LigneChargementCompteService ligneChargementCompteService
//            //ReportBatch rpBatch
//    ){
//
//        LigneChargementCompte ligneChargementCompte = new LigneChargementCompte();
//        ligneChargementCompte.setStatutLigne(StatutLigneChargement.NC);
//        ligneChargementCompte.setChargementCompte(chargement);
//        ligneChargementCompte.setNom(csvCompte.getNom());
//        ligneChargementCompte.setPrenom(csvCompte.getPrenom());
//        ligneChargementCompte.setTelephone(csvCompte.getTelephone());
//        ligneChargementCompte.setDateNaissance(csvCompte.getDateNaissance());
//        ligneChargementCompte.setLieuNaissance(csvCompte.getLieuNaissance());
//        ligneChargementCompte.setNomMere(csvCompte.getNomMere());
//        ligneChargementCompte.setPersonnePrevenir(csvCompte.getPersonnePrevenir());
//        ligneChargementCompte.setEmail(csvCompte.getEmail());
//        ligneChargementCompte.setAdresse(csvCompte.getAdresse());
//        ligneChargementCompte.setTypePersonne(csvCompte.getTypePersonne().toUpperCase());
//
//
//
//        new Thread(() -> {
//            try {
//                Thread.sleep(delay);
//                runnable.run();
//                LigneChargementCompte lignC = ligneChargementCompteService.save(ligneChargementCompte);
//
//                lignC.setRrn(padLeftZeros("C" + lignC.getId().toString(), 10));
//                ligneChargementCompteService.save(lignC);
////                final Double nouveauMontant = csvCompte.getMontant() + montant;
////                rpBatch.setMontant(nouveauMontant);
//            }
//            catch (Exception e){
//                System.err.println(e);
//            }
//        }).start();
//    }

    @GetMapping("/chargement-compte/valider/{id}/{user}/{canal}")
    @Timed
    public ResponseEntity<Void> setValidateChargement(@PathVariable Long id, @PathVariable String user, @PathVariable String canal) {

        ChargementCompte chargement = chargementCompteService.findOne(id);

        chargement.setStatut("I");
        chargementCompteService.save(chargement);

        ChargementCompteResource.ReportBatch rpBatch = new ChargementCompteResource.ReportBatch(chargement.getNombreLigneCharge(), chargement.getNombreLigneErreur());

        List<LigneChargementCompte> listligneChargementCompte = null;

        listligneChargementCompte = ligneChargementCompteService.findByChargementCompteIdAndStatutLigneChargement(id,StatutLigneChargement.IC);
        //      MInfos emetteur= new MInfos();
        //mOperation.setEmetteur(emetteur);

        // List<LigneChargementCarte> listligneChargementCarte      = ligneChargementCarteService.decryptLigneChargement(listligneChargementCarteND);
        Job job = (Job) readService.getBatch(listligneChargementCompte, new ChargementCompteResource.ChargementProcessor(mobileService, ligneChargementCompteService, rpBatch,chargement.getProfilId()));
        chargement.setNombreLigneCharge(rpBatch.getNombreLigneCharge());
        chargement.setNombreLigneErreur(rpBatch.getNombreLigneErreur());

        //    if (chargement.getNombreLigne()-chargement.getNombreLigneCharge()==0){
        chargement.setStatut("V");
        //            }
        chargementCompteService.save(chargement);

        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, "Validation effectuée")).build();
    }

    // @Transactional
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
        Compte cptDebit;
        MobileService mobileService;
        Operation operation;
        ChargementCompteResource.ReportBatch rpBatch;
        CompteRepository compteRepository;
        private final LigneChargementCompteService ligneChargementCompteService;

        public ChargementProcessor(MobileService mobileService, LigneChargementCompteService ligneChargementCompteService, ChargementCompteResource.ReportBatch rpBatch,Long profil) {
       
            this.mobileService = mobileService;
         
            this.ligneChargementCompteService = ligneChargementCompteService;
            this.rpBatch = rpBatch;
            this.profil = profil;
            nombreLigneCharge = rpBatch.getNombreLigneCharge();
            nombreLigneErreur = rpBatch.getNombreLigneErreur();
            resultatC = null;
            result = null;
        }

        @Override
        public Record processRecord(Record record) throws Exception {

            LigneChargementCompte ligneChargementCompte = (LigneChargementCompte) record.getPayload();

            Pattern p = Pattern.compile(CHARGEMENT_PATTERN);

            LigneChargementCompte lgCharge = ligneChargementCompte;
            if (ligneChargementCompte.getStatutLigne().equals(StatutLigneChargement.NC)) {
                //ligneChargementCompte.setStatutLigne(StatutLigneChargement.IC);
            }
            error = false;
            ligneChargementCompteService.save(lgCharge);
            /**
             * vérification du numéro de la carte après décryptage
             */
            Matcher m;
            boolean bool = false;


            try {

                if (ligneChargementCompte.getStatutLigne().equals(StatutLigneChargement.IC)) {
                    ResultMessage resMessage;  //  (ResultMessage)apiService.convert(result.getBody(),ResultMessage.class);
                    ResultMessage resMessageEnvoiMsg;

                    DecimalFormat df = new DecimalFormat("###");
                    // new BigDecimal(ligneChargementTransfert.getMontant())

                    //il s'agit d'un transfert de carte à carte
                    /*
                                                         le débit est constaté et la carte désactivé
                     */
                    // BigDecimal montantApres = cptDebit.getCompteAutre().getSolde().subtract(BigDecimal.valueOf(Double.parseDouble(ligneChargementTransfert.getMontant().toString())));
                    ligneChargementCompte.setStatutLigne(StatutLigneChargement.OC);
                    MClient client = new MClient();
                    MDocIdentification mDocIdentification =new MDocIdentification();
                    MTypePiece  typePiece= new  MTypePiece();
                    typePiece.setCodeTypePiece(ligneChargementCompte.getCodeTypePiece());
                    mDocIdentification.setTypePiece(typePiece);
                    MPersonne personne = new MPersonne();
                    personne.setCodeTelephone(ligneChargementCompte.getCodePays().toString());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                   // mobileService.convertStringToLocal(ligneChargementCompte.getDateNaissance())
                    LocalDate localDate = LocalDate.parse(ligneChargementCompte.getDateNaissance(), formatter);
                    personne.setDateNaissance(localDate);
                    personne.setNom(ligneChargementCompte.getNom());
                    personne.setSexe(MSexe.valueOf(ligneChargementCompte.getSexe()));
                    personne.setPrenom(ligneChargementCompte.getPrenom());
                    personne.setEmail(ligneChargementCompte.getEmail());
                    personne.setLieuNaissance(ligneChargementCompte.getLieuNaissance());
                    personne.setTelephone(ligneChargementCompte.getTelephone());
                    personne.setNomDelaMere(ligneChargementCompte.getNomMere());
                    personne.setPersonneAprevenir(ligneChargementCompte.getPersonnePrevenir());
                    personne.setAdresse(ligneChargementCompte.getAdresse());
                    mDocIdentification.setDateDelivrance(ligneChargementCompte.getDateDelivrance());
                    mDocIdentification.setDateExpiration(ligneChargementCompte.getDateExpiration());
                    mDocIdentification.setReference(ligneChargementCompte.getReference());
                    mDocIdentification.setLieuDelivrance(ligneChargementCompte.getLieuDelivrance());
                    client.setDocIdentification(mDocIdentification);
                    String fseDef = mobileService.getFournisseurMonnaieDef(ligneChargementCompte.getCodePays().toString());
                    client.setCodeFse(fseDef);

                    client.setPersonne(personne);
                    client.setProfil(this.profil.toString());
                    MResponse rep= mobileService.doSouscription(client);
                    nombreLigneCharge = nombreLigneCharge + 1;
                    //     ligneChargementCarteService.save(ligneChargementCarte);

                    error = false;
                    if (rep.getCode().equals("0")){
                       
                   }else {
                        error = true;
                        ligneChargementCompte.setError(rep.getMessage());
                    }
                    

                    /**
                     *
                     * Faire le chargement
                     */
                } else {
                    error = true;
                    ligneChargementCompte.setError("Client  dejà cree");
                }

            } catch (Exception e) {
                error = true;
                System.out.println("Echec-------"+e.getMessage());
                ligneChargementCompte.setError(e.getMessage());

            }

            if (error) {
                nombreLigneErreur = nombreLigneErreur + 1;
            }

            rpBatch.setNombreLigneCharge(nombreLigneCharge);
            rpBatch.setNombreLigneErreur(nombreLigneErreur);
            ligneChargementCompteService.save(ligneChargementCompte);

            return record;
        }
    }
}
