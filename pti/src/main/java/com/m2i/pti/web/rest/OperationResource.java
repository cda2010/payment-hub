package com.m2i.pti.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.m2i.pti.domain.*;
import com.m2i.pti.repository.LigneReglementRepository;
import com.m2i.pti.domain.enumeration.StatutOperation;
import com.m2i.pti.domain.enumeration.TypeServiceLibelle;
import com.m2i.pti.mobile.dto.MOperationDTO;
import com.m2i.pti.mobile.dto.MOperationNumbLastestDTO;
import com.m2i.pti.repository.CommissionRepository;
import com.m2i.pti.repository.OperationRepository;
import com.m2i.pti.repository.UserRepository;
import com.m2i.pti.service.*;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.dto.Evenement;
import com.m2i.pti.service.kafka.MessageProducer;
import com.m2i.pti.service.easyBatch.Read;
import com.m2i.pti.web.rest.errors.BadRequestAlertException;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import com.m2i.pti.web.rest.util.HeaderUtil;
import com.m2i.pti.web.rest.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import jakarta.xml.bind.JAXBException;
import org.easybatch.core.job.Job;
import org.easybatch.core.processor.RecordProcessor;
import org.easybatch.core.record.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

/**
 * REST controller for managing Operation.
 */
@RestController
@RequestMapping("/api")
public class OperationResource {

    private final Logger log = LoggerFactory.getLogger(OperationResource.class);

    private static final String ENTITY_NAME = "operation";

    private final OperationService operationService;
    private final RegulationService regulationService;
    private final CompteService compteService;
    private final TransactionComptePtiService transactionComptePtiService;
    @Autowired
    OperationRepository operationRepository;
    LigneReglementRepository ligneReglementRepository;
    Read readService;
    ReglementService reglementService;
    private final MessageProducer messageProducer;
    private final UserRepository userRepository;
    UserService userService;
    private final TypeServiceService typeService;
    private final CommissionRepository commissionRepository;
    CommissionService commissionService;

    public OperationResource(CommissionService commissionService, CommissionRepository commissionRepository, TypeServiceService typeService, UserService userService, LigneReglementRepository ligneReglementRepository, ReglementService reglementService, MessageProducer messageProducer, Read readService, OperationService operationService, RegulationService regulationService, CompteService compteService, TransactionComptePtiService transactionComptePtiService, UserRepository userRepository) {
        this.operationService = operationService;
        this.regulationService = regulationService;
        this.compteService = compteService;
        this.readService = readService;
        this.messageProducer = messageProducer;
        this.reglementService = reglementService;
        this.transactionComptePtiService = transactionComptePtiService;
        this.ligneReglementRepository = ligneReglementRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.typeService = typeService;
        this.commissionRepository = commissionRepository;
        this.commissionService = commissionService;
    }

    @GetMapping("operations/byhour/{periode}")
    @Timed
    public List<LabelValue> findSumOperationOfDateGroupByHour(@PathVariable(required = true) String periode) {
        log.debug("Started etat---------");
        ZonedDateTime date = ZonedDateTime.now();
        if (periode.equals("yesterday")) {
            date = date.minusDays(1);
        }
        //return operationService.findSumOperationOfDateGroupByHour(date);
        return operationService.findSumOperationOfDateGroupByHourOptimise(date);
    }

    @GetMapping("operations/byday/{periode}")
    @Timed
    public List<LabelValue> findSumOperationGroupByDay(@PathVariable(required = true) String periode) {

        return operationService.findSumOperationGroupByDay(periode);
    }

    @GetMapping("operations/bymonth/{periode}")
    @Timed
    public List<LabelValue> findSumOperationGroupByMonth(@PathVariable(required = true) String periode) {

        return operationService.findSumOperationGroupByMonth(periode);
    }

    @GetMapping("operations/byyear")
    @Timed
    public List<LabelValue> findSumOperationOfLastFiveYearGroupByYear() {

        return operationService.findSumOperationOfLastFiveYearGroupByYear();
    }

    /**
     * POST /operations : Create a new operation.
     *
     * @param operationDTO the operationDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the
     * new operationDTO, or with status 400 (Bad Request) if the operation has
     * already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/operations")
    @Timed
    public ResponseEntity<OperationDTO> createOperation(@RequestBody OperationDTO operationDTO) throws URISyntaxException {
        log.debug("REST request to save Operation : {}", operationDTO);
        if (operationDTO.getId() != null) {
            throw new BadRequestAlertException("A new operation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        OperationDTO result = operationService.save(operationDTO);
        return ResponseEntity.created(new URI("/api/operations/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * @param operationDTO
     * @return
     */
    @PostMapping("/operations/verification-cumul")
    @Timed
    public TableauCumulDTO verificationAvantValidation(@RequestBody OperationAllDTO operationDTO) {
        TableauCumulDTO tableauCumulDTO = operationService.post(operationDTO);
        return tableauCumulDTO;
    }

    /*
        Service d'envoi de cash
     */
    @PostMapping("/operations/envoie-cash")
    @Timed
    public OperationDTO effectuerOperationEnvoieCash(@RequestBody OperationAllDTO operationAllDTO) throws URISyntaxException, IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", operationAllDTO.getOperationDTO());
        if (operationAllDTO.getOperationDTO().getId() != null) {
            throw new BadRequestAlertException("A new operation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return operationService.doOperationEnvoiCash(operationAllDTO);
    }

    /**
     * Service permettant de faire une operation de mise à disposition
     *
     * @param operationAllDTO
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @PostMapping("/operations/mad")
    @Timed
    public OperationDTO effectuerOperation(@RequestBody OperationAllDTO operationAllDTO) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", operationAllDTO.getOperationDTO());
        if (operationAllDTO.getOperationDTO().getId() != null) {
            throw new BadRequestAlertException("A new operation cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return operationService.doOperationAccount(operationAllDTO);
    }

    /**
     * Service permettant de faire une operation de redressement
     *
     * @param operationDTO
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @PutMapping("/operations/redressement")
    @Timed
    public OperationDTO effectuerOperationRedressement(@RequestBody OperationDTO operationDTO) throws IOException, NoSuchAlgorithmException, JAXBException {
        return operationService.doOperationRedressement(operationDTO);
    }

    /**
     * Service permettant de faire une operation de retrait
     *
     * @param operationAllDTO
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    @PutMapping("/operations/retrait-cash")
    @Timed
    public OperationDTO effectuerOperationRetrait(@RequestBody OperationAllDTO operationAllDTO) throws URISyntaxException, IOException, JAXBException {
        log.debug("REST request to save Operation : {}", operationAllDTO.getOperationDTO());
        return operationService.doOperationRetrait(operationAllDTO);
    }

    @PostMapping("/operations/creation-monnaie")
    @Timed
    public OperationDTO effectuerOperationCreationMonnaie(@RequestBody OperationDTO operationDTO) throws URISyntaxException, IOException {
        log.debug("REST request to save Operation : {}", operationDTO);
        return operationService.doOperationCreationMonnaie(operationDTO);
    }

    /**
     * Service permettant de faire une operation d'appovisionnement de compte
     *
     * @param operationDTO
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    @PostMapping("/operations/approvisionnement-compte")
    @Timed
    public OperationDTO effectuerOperationApprovisionnementCompte(@RequestBody OperationDTO operationDTO) throws URISyntaxException, IOException, JAXBException {
        log.debug("REST request to save Operation : {}", operationDTO);
        return operationService.doOperationApprovisionnement(operationDTO);
    }

    @PostMapping("/operations/reglement-compte")
    @Timed
    public OperationDTO effectuerOperationReglementCompte(@RequestBody OperationReglementDTO operationDTO) throws URISyntaxException, IOException {
        log.debug("REST request to save Operation : {}", operationDTO.getDateReglement());
        List<Long> listeOperation = operationDTO.getUoId();
        Reglement reglement = new Reglement();
        reglement.setDateReglement(operationDTO.getDateReglement());
        reglement.setStatut(false);
        reglement.setEstAnnuler(false);
        TypeService tpeService = typeService.findByCodeService(String.valueOf(operationDTO.getOperationDto().getCode()));//
        User userConnecte = userService.getUserConnecteOptimise(operationDTO.getOperationDto().getLogin());
        operationService.userCanDoOperations(userConnecte);
        reglementService.save(reglement);
        readService.getBatch(listeOperation, new OperationResource.ReglementProcessor(commissionRepository, commissionService, tpeService, reglement, operationService, operationDTO.getOperationDto(), operationDTO.getDateReglement(), userConnecte));
        return operationDTO.getOperationDto();
    }

    @PostMapping("/operations/valider-reglement-compte")
    @Timed
    public OperationDTO effectuerValideOperationReglementCompte(@RequestBody OperationDTO reglement) throws URISyntaxException, IOException {
        log.debug("--------Request to valide reglement----------" + reglement.getId());
        Reglement reg = reglementService.findOne(reglement.getId());
        List<LigneReglement> listeReglement = ligneReglementRepository.findByReglementIdAndValideIsFalse(reglement.getId());
        /**
         * * Ajouter un cha
         */
        try {
            if (listeReglement.size() > 0) {
                if (reglement.getLogin() != null) {
                    User userConnecte = userService.getUserConnecteOptimise(reglement.getLogin());
                    reg.setEnTraitement(Boolean.TRUE);
                    reglementService.save(reg);
                    readService.getBatch(listeReglement, new OperationResource.ReglementValideProcessor(operationService, reglement.getLogin(), userConnecte));
                    reg.setValiderPar(userConnecte.getLogin());
                    reg.setEstAnnuler(false);
                    reg.setStatut(true);
                    reg.setEnTraitement(Boolean.FALSE);
                    reglementService.save(reg);
                }
            } else {
                throw new CustomParameterizedException("Le règlement est annulé ");
            }

        } catch (Exception e) {
            //   reg.setEstAnnuler(reg);
            reg.setStatut(false);
            reglementService.save(reg);
        }

        return reglement;
    }

    /**
     * Service de retrait de cash dans un compte
     *
     */
    @GetMapping("/operations/valide-reglement-compte-new")
    @Timed
    public OperationDTO effectuerValideOpReglementCompte(@RequestParam Long reglementId, @RequestParam String login) throws IOException, JAXBException {
        log.debug("Request to get" + reglementId + "and" + login);
        Reglement reg = reglementService.findOne(reglementId);
        List<LigneReglement> listeReglement = ligneReglementRepository.findByReglementIdAndValideIsFalse(reglementId);
        return operationService.doOperationValideNewReglement(listeReglement, login);
    }

    @PostMapping("/operations/retrait-compte")
    @Timed
    public ResponseEntity effectuerOperationARetraitCompte(@RequestBody OperationDTO operationDTO) throws URISyntaxException, IOException {
        log.debug("REST request to save Operation : {}", operationDTO);
        return operationService.saveInforamationOfOperationRetraitCompte(operationDTO);
    }

    /**
     * Service de traitement d'une oepration en attente de validation
     *
     * @param operationDTO
     * @return
     * @throws URISyntaxException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @PostMapping("/operations/traitement-operation")
    @Timed
    public OperationDTO traitementOperationMad(@RequestBody OperationDTO operationDTO) throws URISyntaxException, IOException, NoSuchAlgorithmException, JAXBException {
        log.debug("REST request to save Operation : {}", operationDTO);
        if (operationDTO.getStatut().equals(StatutOperation.VALIDE)) {
            OperationDTO operDTO = operationService.validationOperation(operationDTO);
            if (operDTO != null) {

                Operation operation = operationRepository.findById(operationDTO.getOperationTraiteId()).orElse(null);

                if (operation.getTypeService().getCode().equals(String.valueOf(TypeServiceLibelle.REGULATION))) {
                    operationService.envoiMsgOperation(operation, "0");
                }

            }
            return operDTO;
        } else {
            return operationService.rejetOperation(operationDTO);
        }

    }

    @PostMapping("/operations/traitement-operation-evenement")
    @Timed
    public EvenementOperationDTO traitementOperationEvenement(@RequestBody EvenementOperationDTO evenementOperationDTO) {
        log.debug("Request to save event");
        return operationService.traitementEvenment(evenementOperationDTO);
    }

    @PostMapping("operations/save-evenement-client")
    @Timed
    public EvenementOperationDTO saveEvenementOfClient(@RequestBody OperationAllDTO operationAllDTO) {
        log.debug("Request to save evenement of client ");
        return operationService.saveEvenementOfClient(operationAllDTO);
    }

    /**
     * Service de calcul des frais en fonction des differents critères
     *
     * @param canalLibelle
     * @param uoAcq
     * @param typeServiceLibelle
     * @param montant
     * @param uoEmId
     * @return
     */
    @GetMapping("/operations/calcul-frais")
    @Timed
    public LigneFraisDTO calculFraisOperations(@RequestParam String canalLibelle, @RequestParam Long uoAcq, @RequestParam TypeServiceLibelle typeServiceLibelle, @RequestParam Double montant, @RequestParam(required = false) Long uoEmId) {
        log.debug("REST request to get a page of Operations");
        return operationService.calculFrais(canalLibelle, typeServiceLibelle, uoAcq, montant, uoEmId, null, Optional.empty(), 0L);
    }

    /**
     * Service de simulation du calcul des frais
     *
     * @param canalLibelle
     * @param uoAcq
     * @param typeServiceLibelle
     * @param montant
     * @param uoEmId
     * @return
     */
    @GetMapping("/operations/simulation-calcul-frais")
    @Timed
    public Operation simulation(@RequestParam(required = true) String canalLibelle, @RequestParam(required = true) Long uoAcq, @RequestParam(required = true) TypeServiceLibelle typeServiceLibelle, @RequestParam(required = true) Integer montant, @RequestParam(required = false) Long uoEmId) {
        log.debug("REST request to get a page of Operations");
        return operationService.calculFraisSimulation(canalLibelle, typeServiceLibelle, uoAcq, montant, uoEmId);
    }

    /**
     * Service de mise à jour d'une operation (non utilise)
     *
     * @param operationDTO
     * @return
     * @throws URISyntaxException
     */
    @PutMapping("/operations")
    @Timed
    public ResponseEntity<OperationDTO> updateOperation(@RequestBody OperationDTO operationDTO) throws URISyntaxException {
        log.debug("REST request to update Operation : {}", operationDTO);
        if (operationDTO.getId() == null) {
            return createOperation(operationDTO);
        }
        OperationDTO result = operationService.save(operationDTO);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, operationDTO.getId().toString()))
                .body(result);
    }

    /**
     * Service d'annulation d'un operation de retrait de cash
     *
     * @param operationDTO
     * @return
     * @throws URISyntaxException
     */
    @PutMapping("/operations/annulation-retrait-cash")
    @Timed
    public ResponseEntity<OperationDTO> annulerOperationRetraitCash(@RequestBody OperationDTO operationDTO) throws URISyntaxException {
        log.debug("REST request to update Operation : {}", operationDTO);
        if (operationDTO.getId() == null) {
            return createOperation(operationDTO);
        }
        OperationDTO result = operationService.annulationOperationRetrait(operationDTO);

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, operationDTO.getId().toString()))
                .body(result);
    }

    /**
     * Service de recherche d'une operation en fonction de différents critères
     *
     * @param pageable
     * @param typeServiceId
     * @param canalId
     * @param uoAcquereurId
     * @param montantOp
     * @param statutOperation
     * @param dateOperation
     * @param dateFin
     * @param codeOperation
     * @param organisationId
     * @return
     */
    @GetMapping("/operations")
    @Timed
    public ResponseEntity<List<OperationDTO>> getAllOperations(Pageable pageable, @RequestParam(required = false) Long typeServiceId,
            @RequestParam(required = false) Long canalId,
            @RequestParam(required = false) Long uoAcquereurId,
            @RequestParam(required = false) BigDecimal montantOp,
            @RequestParam(required = false) StatutOperation statutOperation,
            @RequestParam(required = false) ZonedDateTime dateOperation,
            @RequestParam(required = false) ZonedDateTime dateFin,
            @RequestParam(required = false) String codeOperation,
            @RequestParam(required = false) Long organisationId) {
        log.debug("REST request to get a page of Operations%" + uoAcquereurId + "%" + "statutOperation" + statutOperation);
        Page<OperationDTO> page = operationService.findByMultiParams(pageable, typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, dateOperation, dateFin, codeOperation, organisationId);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/operations-optimise")
    @Timed
    public ResponseEntity<List<OperationDTO>> getAllOperationsOptimise(Pageable pageable, @RequestParam(required = false) Long typeServiceId,
            @RequestParam(required = false) Long canalId,
            @RequestParam(required = false) Long uoAcquereurId,
            @RequestParam(required = false) BigDecimal montantOp,
            @RequestParam(required = false) StatutOperation statutOperation,
            @RequestParam(required = false) ZonedDateTime dateOperation,
            @RequestParam(required = false) ZonedDateTime dateFin,
            @RequestParam(required = false) String codeOperation,
            @RequestParam(required = false) Long organisationId, @RequestParam(required = false) String description) {
        log.debug("REST request to get a page of Operations%" + uoAcquereurId + "%" + "statutOperation" + statutOperation);
        // return  operationService.findByMultiParamsOptimise(pageable, typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, dateOperation, dateFin, codeOperation, organisationId);

        Page<OperationDTO> page = operationService.findByMultiParamsOptimise(pageable, typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, dateOperation, dateFin, codeOperation, organisationId, description);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Service de recherche d'une operation en fonction de différents critères
     *
     * @param pageNo
     * @param pageable
     * @param typeServiceId
     * @param canalId
     * @param uoAcquereurId
     * @param montantOp
     * @param statutOperation
     * @param dateOperation
     * @param dateFin
     * @param codeOperation
     * @param organisationId
     * @return
     */
    @GetMapping("/operations/mobile/all")
    @Timed
    public ResponseEntity<List<MOperationDTO>> getAllOperationAll(@RequestParam int pageNo, @RequestParam(required = false) Optional<Long> typeServiceId,
            @RequestParam(required = false) Optional<Long> canalId,
            @RequestParam(required = false) Optional<Long> uoAcquereurId,
            @RequestParam(required = false) Optional<BigDecimal> montantOp,
            @RequestParam(required = false) Optional<String> statutOperation,
            @RequestParam(required = false) Optional<ZonedDateTime> dateOperation,
            @RequestParam(required = false) Optional<ZonedDateTime> dateFin,
            @RequestParam(required = false) Optional<String> codeOperation,
            @RequestParam(required = false) Optional<Long> organisationId) {
        log.debug("REST request to get a page of Operations%" + uoAcquereurId + "%" + "statutOperation" + statutOperation);
        int pageSize = 20;
        Pageable pageReq = PageRequest.of(pageNo, pageSize, Sort.by(new Sort.Order(Sort.Direction.DESC, "dateOperation")));
        Long tServiceId = null;
        Long tAcquereurId = null;
        Long tcanalId = null;
        String tstatutOperation = null;
        String tcodeOperation = null;
        Long torganisationId = null;
        BigDecimal tmontantOp = null;
        ZonedDateTime tdateOperation = null;
        ZonedDateTime tdateFin = null;
        StatutOperation statutOp = null;
        if (dateOperation.isPresent()) {
            tdateOperation = dateOperation.get();
        }
        if (dateFin.isPresent()) {
            tdateFin = dateFin.get();
        }
        if (canalId.isPresent()) {
            tcanalId = canalId.get();
        }
        if (statutOperation.isPresent()) {

            tstatutOperation = statutOperation.get();
            if (tstatutOperation != null) {
                statutOp = StatutOperation.valueOf(tstatutOperation);
            }

        }
        if (montantOp.isPresent()) {
            tmontantOp = montantOp.get();
        }
        if (codeOperation.isPresent()) {
            tcodeOperation = codeOperation.get();
        }
        if (organisationId.isPresent()) {
            torganisationId = organisationId.get();
        }

        if (uoAcquereurId.isPresent()) {
            tAcquereurId = uoAcquereurId.get();
        }
        if (typeServiceId.isPresent()) {
            tServiceId = typeServiceId.get();
        }
        Page<MOperationDTO> page = operationService.findByMultiParamsMobile(pageReq, tServiceId, tcanalId, tAcquereurId, tmontantOp, statutOp, tdateOperation, tdateFin, tcodeOperation, torganisationId);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Service de recherche des operations en attente de validation
     *
     * @param pageable
     * @param typeServiceId
     * @param canalId
     * @param uoAcquereurId
     * @param montantOp
     * @param statutOperation
     * @param dateOperation
     * @param dateFin
     * @param codeOperation
     * @return
     */
    @GetMapping("/operations/search_validation")
    @Timed
    public ResponseEntity<List<OperationDTO>> getAllOperationsPourValidation(Pageable pageable, @RequestParam(required = false) Long typeServiceId, @RequestParam(required = false) Long canalId, @RequestParam(required = false) Long uoAcquereurId, @RequestParam(required = false) BigDecimal montantOp, @RequestParam(required = false) StatutOperation statutOperation, @RequestParam(required = false) ZonedDateTime dateOperation, @RequestParam(required = false) ZonedDateTime dateFin, @RequestParam(required = false) String codeOperation) {
        log.debug("REST request to get a page of %%Operations%%");
        Page<OperationDTO> page = operationService.findByMultiParamsEnAttenteValidation(pageable, typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, dateOperation, dateFin, codeOperation);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Service de recherche des operations en attent de retrait dont le code de
     * retrait à expirer
     *
     * @param pageable
     * @return
     */
    @GetMapping("/operations/expires")
    @Timed
    public ResponseEntity<List<OperationDTO>> getAllOperationsExpires(Pageable pageable) {
        log.debug("REST request to get a page of Operations");
        Page<OperationDTO> page = operationService.findOperationsExpire(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * Service d'annulation d'une opération d'envoi de cash avec la possibilité
     * de retourner les frais
     *
     * @param operationId
     * @return
     * @throws IOException
     */
    @GetMapping("operations/annulation_envoi_cash_avec_retour_frais")
    public OperationDTO annulationOperation(@RequestParam Long operationId) throws IOException, JAXBException {
        return operationService.annulationOpEnvoiCashAvecFrais(operationId);
    }

    /**
     * Service d'annulation des operations de frais
     *
     * @param operationId
     * @param avecFrais
     * @return
     * @throws IOException
     */
    @GetMapping("operations/annulation_account_avec_retour_frais")
    public OperationDTO annulationOperationAccount(@RequestParam Long operationId, @RequestParam Boolean avecFrais) throws IOException, JAXBException {
        return operationService.annulationOpAccountAvecFrais(operationId, avecFrais);
    }

    @GetMapping("operations/annulation_envoi_cash_sans_retour_frais")
    public OperationDTO annulationOperationSansFrais(@RequestParam Long operationId) throws IOException, JAXBException {
        return operationService.annulationOperationEnvoiCash(operationId);
    }

    /**
     * Service de verification des operations expirés
     *
     * @param codeRetrait
     * @return
     * @throws NoSuchAlgorithmException
     */
    @GetMapping("operations/verif-expiration")
    public OperationDTO verificationOperationExpiree(@RequestParam String codeRetrait) throws NoSuchAlgorithmException {
        return operationService.verificationOperationExpire(codeRetrait);
    }

    /**
     * GET /operations : get all the operations.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of
     * operations in body
     */
    @GetMapping("/operationsEnAttente")
    @Timed
    public ResponseEntity<List<OperationDTO>> getSomeOperationsEnAttente(Pageable pageable, @RequestParam(required = false) String typeService, @RequestParam(required = false) Long canalId, @RequestParam(required = false) Long uoAcquereurId, @RequestParam(required = false) BigDecimal montantOp, @RequestParam(required = false) StatutOperation statutOperation, @RequestParam(required = false) ZonedDateTime dateOperation) {
        log.debug("REST request to get a page of Operations");
        Page<OperationDTO> page = operationService.findOperationEnAttenteByMultiParams(pageable, typeService, canalId, uoAcquereurId, montantOp, statutOperation, dateOperation);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/operations");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/operations/somme_montant")
    @Timed
    public StatsBilanJour getSommeJournalier(Pageable pageable, @RequestParam(required = false) Long typeServiceId, @RequestParam(required = false) Long canalId, @RequestParam(required = false) Long uoAcquereurId, @RequestParam(required = false) BigDecimal montantOp, @RequestParam(required = false) StatutOperation statutOperation, @RequestParam(required = false) ZonedDateTime dateOperation) throws IOException {
        return operationService.stats();
    }

    @GetMapping("/operations/somme_montant_optimise")
    @Timed
    public StatsBilanJour getSommeJournalierOptimise() throws IOException {
        return operationService.statsOptimise();
    }

    @GetMapping("/operations/bilan_mensuel")
    @Timed
    public StatsBilanMensuel getBilanMensuelOperations() throws IOException {
        return operationService.getDatasByMonth();
    }

    @GetMapping("/operations/bilan_operations_agence")
    @Timed
    public StatsBilanJour getBilanOperationsByCountry(@RequestParam Long uoId) throws IOException {
        return operationService.statsByCountryForAllOperations(uoId);
    }

    @PutMapping("/operations/send_code_retrait")
    @Timed
    public OperationDTO sendCodeRetrait(@RequestBody OperationDTO operationDTO) throws IOException, NoSuchAlgorithmException {
        return operationService.sendNewCodeRetrait(operationDTO);
    }

    /**
     * GET /operations/:id : get the "id" operation.
     *
     * @param id the id of the operationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the
     * operationDTO, or with status 404 (Not Found)
     */
    @GetMapping("/operations/{id}")
    @Timed
    public ResponseEntity<OperationAllDTO> getOperation(@PathVariable Long id) {
        log.debug("REST request to get Operation : {}", id);
        OperationAllDTO operationDTO = operationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(operationDTO));
    }

    @GetMapping("/operations/verification-avant-retrait")
    @Timed
    public ResponseEntity<OperationAllDTO> verificationOperation(@RequestParam String codeRetrait) throws NoSuchAlgorithmException {
        OperationAllDTO operationDTO = operationService.verificationOperation(codeRetrait);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(operationDTO));
    }

    @GetMapping("/operations/search_codeOperation")
    @Timed
    public OperationDTO getOperationByCode(@RequestParam String codeOperation) {
        return operationService.findByCodeOperation(codeOperation);
    }

    @GetMapping("/operations/search_by_personne")
    @Timed
    public List<OperationDTO> getAllOperations(@RequestParam(required = false) Long personneId, @RequestParam(required = false) Long typeServiceId, @RequestParam(required = false) Long canalId, @RequestParam(required = false) Long uoAcquereurId, @RequestParam(required = false) BigDecimal montantOp, @RequestParam(required = false) StatutOperation statutOperation, @RequestParam(required = false) Boolean statutAnnuler, @RequestParam(required = false) ZonedDateTime dateOperation, @RequestParam(required = false) ZonedDateTime dateFin, @RequestParam(required = false) String codeOperation) {
        log.debug("Request to get operation" + personneId + "et acq" + uoAcquereurId + "statutOperation" + statutOperation.name());
        return operationService.findByPersonne(personneId, typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, statutAnnuler, dateOperation, dateFin, codeOperation);
    }

    @PutMapping("/operations/mise_jour_statut")
    @Timed
    public OperationDTO miseAJourOperation(@RequestBody OperationDTO operationDTO) {
        return operationService.miseJourOperation(operationDTO);
    }

    @GetMapping("/operations/annulation_appel_fond")
    @Timed
    public OperationDTO annulationAppelFonds(@RequestParam Long operationId) throws IOException, JAXBException {
        return operationService.annulationOperationAppelFond(operationId);
    }

    @GetMapping("/operations/list_evenement")
    @Timed
    public List<Evenement> getListEvenement(@RequestParam String montant) throws IOException {
        return operationService.getEvenements(montant);
    }

    @GetMapping("/operation/change-statut-operation")
    @Timed
    public OperationDTO changeStatutOperation(@RequestParam Long operationId) {
        return operationService.changeOperationStatut(operationId);

    }

    /**
     * DELETE /operations/:id : delete the "id" operation.
     *
     * @param id the id of the operationDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/operations/{id}")
    @Timed
    public ResponseEntity<Void> deleteOperation(@PathVariable Long id) {
        log.debug("REST request to delete Operation : {}", id);
        operationService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * @param pageable
     * @param typeServiceId
     * @param canalId
     * @param uoAcquereurId
     * @param montantOp
     * @param statutOperation
     * @param dateOperation
     * @param dateFin
     * @param codeOperation
     * @param organisationId
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/operations/download",
            method = RequestMethod.GET,
            produces = MediaType.ALL_VALUE)
    @Timed
    public void getAllOperations(Pageable pageable, @RequestParam(required = false) Long typeServiceId,
            @RequestParam(required = false) Long canalId,
            @RequestParam(required = false) Long uoAcquereurId,
            @RequestParam(required = false) BigDecimal montantOp,
            @RequestParam(required = false) StatutOperation statutOperation,
            @RequestParam(required = false) ZonedDateTime dateOperation,
            @RequestParam(required = false) ZonedDateTime dateFin,
            @RequestParam(required = false) String codeOperation,
            @RequestParam(required = false) Long organisationId, HttpServletResponse response) throws IOException {

        InputStream stream = operationService.genererCsv(pageable, typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, dateOperation, dateFin, codeOperation, organisationId);
        response.setContentType("text/csv");
        response.setHeader("Content-disposition", "attachment;filename=\"" + this.operationService.getPathname() + "\"");
        response.setCharacterEncoding("UTF-8");
        IOUtils.copy(stream, response.getOutputStream());
        stream.close();
    }

    @GetMapping("/operations/regulation")
    @Timed
    public OperationDTO doOperationCompteaCompte(@RequestParam Long compteSrc, @RequestParam Long compteDest, @RequestParam(required = false) BigDecimal montant, @RequestParam String typeService, @RequestParam(required = false) String commentaire, @RequestParam String motif) throws IOException {
        OperationAllDTO operationAllDTO = new OperationAllDTO();
        OperationDTO operationDTO = new OperationDTO();
        CompteDTO cptD = new CompteDTO();
        CompteDTO cptS = new CompteDTO();
        cptS = compteService.findOne(compteSrc);
        cptD = compteService.findOne(compteDest);
        if (cptS != null) {
            if (cptD != null) {
                operationDTO.setCanalLibelleCanal("WEB");
                operationDTO.setCompteDestId(compteDest);
                operationDTO.setCompteSrcId(compteSrc);
                operationDTO.setCommentaire("Maker: " + commentaire);
                operationDTO.setMotif(motif);
                if (typeService.equals("Transfert")) {
                    operationDTO.setMontantOp(montant);
                } else {
                    operationDTO.setMontantOp(cptS.getSoldeCompte());
                }
                operationAllDTO.setOperationDTO(operationDTO);
                return regulationService.doOperationCompteACompte(operationAllDTO, typeService);
            }
        }
        return null;
    }

    @GetMapping("/operation/annulationReglement")
    @Timed
    public void annulationReglement(@RequestParam Long reglementId) {
        operationService.annulationReglement(reglementId);
    }

    @GetMapping("/operation/detacherReglement")
    @Timed
    public LigneReglement detacherReglement(@RequestParam Long ligneReglementId) {
        return operationService.detacherReglement(ligneReglementId);
    }

    @GetMapping("operations/annulation_operation")
    @Timed
    public OperationDTO annulerOperation(@RequestParam Long operationId) {
        return operationService.annulationDesOperation(operationId);
    }

    @GetMapping("/operations/expiration")
    @Timed
    public void test() throws IOException {
        annulationOperationExpire();
        // operationProcessor.annulationOperationExpire();
    }

    // LES RESOURCES POUR L'OPTIMISATION
    @GetMapping("/operations/opt/dashboard")
    @Timed
    public List<OperationOptimiseDTO> getTop10Operations(Pageable pageable, @RequestParam(required = false) StatutOperation statutOperation) {
        log.debug("REST request to get a page of Operations%" + pageable + "%" + "statutOperation" + statutOperation);
        return operationService.getTop10Operations(statutOperation, null);
    }

    @GetMapping("/operations-optimise/{id}")
    @Timed
    public ResponseEntity<OperationAllDTO> getOperationOptimise(@PathVariable Long id) {
        log.debug("REST request to get Operation : {}", id);
        OperationAllDTO operationDTO = operationService.findOneOptimise(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(operationDTO));
    }

    @Transactional
    public void annulationOperationExpire() {
        List<String> typeService = new ArrayList();
        typeService.add("RETRAIT_COMPTE");
        typeService.add("ACCOUNT_CASH");
        typeService.add("PAIEMENT_INTERNET");
        typeService.add("RETRAIT_CARDLESS");
        List<Operation> listeOperation = operationRepository.findOperationExpire(StatutOperation.ENATTENTE, LocalDate.now(), typeService);
        Job job = (Job) readService.getBatch(listeOperation, new OperationResource.OperationProcessor(operationService));
    }

    public static class ReglementProcessor implements RecordProcessor {

        OperationService operationService;
        OperationDTO operationDTO;
        Reglement reglement;
        ZonedDateTime dateCommission;
        User userConnecte;
        TypeService tpeService;
        CommissionService commissionService;
        CommissionRepository commissionRepository;

        public ReglementProcessor(CommissionRepository commissionRepository, CommissionService commissionService, TypeService tpeService, Reglement reglement, OperationService operationService, OperationDTO operationDTO, ZonedDateTime dateCommission, User userConnecte) {
            this.operationService = operationService;
            this.operationDTO = operationDTO;

            this.reglement = reglement;
            this.dateCommission = dateCommission;
            this.userConnecte = userConnecte;
            this.tpeService = tpeService;
            this.commissionService = commissionService;
            this.commissionRepository = commissionRepository;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            Long uoId = (Long) record.getPayload();
            try {
                List<CommissionDTO> listeCommissionResiduel = commissionRepository.findByCommissionOrganisationPasPayeAndEstComptabilise(uoId);
                commissionService.repartitionCommissionBatchWithId(listeCommissionResiduel);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            try {

                OperationReglDto op = new OperationReglDto();
                op.setCanal(operationDTO.getCanalLibelleCanal());
                op.setTypeService(operationDTO.getCode());
                op.setUoAcq(uoId);
                op.setLogin(operationDTO.getLogin());

                //     messageProducer.sendMessageReglementOperation( op) ;
                operationService.doOperationRegelement(operationDTO, uoId, reglement, dateCommission, userConnecte, tpeService);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return record;
        }
    }

    public static class ReglementValideProcessor implements RecordProcessor {

        User userConnecte;
        OperationService operationService;
        String login;
        //String commentaire;

        public ReglementValideProcessor(OperationService operationService, String login, User userConnecte) {
            this.operationService = operationService;
            this.login = login;
            this.userConnecte = userConnecte;
            //this.commentaire=commentaire;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            LigneReglement listeReglement = (LigneReglement) record.getPayload();
            try {
                System.out.println("Entrer--------------" + listeReglement);
                operationService.doOperationValideReglement(listeReglement, userConnecte);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            System.out.println("Entrer--------------" + (LigneReglement) record.getPayload());
            return record;
        }
    }

    private static class OperationProcessor implements RecordProcessor {

        OperationService operationService;

        public OperationProcessor(OperationService operationService) {
            this.operationService = operationService;
        }

        @Override
        public Record processRecord(Record record) throws Exception {
            Operation operation = (Operation) record.getPayload();
            try {
                operationService.annulationOperation(operation.getId(), Boolean.FALSE);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return record;
        }
    }

    @GetMapping("/comptes-banque/changeStatut/{id}")
    @Timed
    public ResponseEntity<CompteDTO> changeStatutCompteBanque(@PathVariable Long id) {
        CompteDTO compteDTO = operationService.changerStatutCompteBanque(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(compteDTO));
    }

    @GetMapping("/operation/LastThreeOperation-optimise")
    @Timed
    public List<MOperationNumbLastestDTO> getLastThreeOperationByClient(@RequestParam String code, @RequestParam String telephone) {
        log.debug("REST request to get a page of Operation");
        return operationService.findLastThreeOperation(code, telephone);
    }

    //Ajout statut
    @GetMapping("/operation/statut-operation")
    @Timed
    public List<OperationStatutDTO> statutOperation(@RequestParam Long operationId) {
        return operationService.operationStatut(operationId);
    }
   @GetMapping("/reporting/istq/last-operation")
    @Timed
    public List<OperationDetailsDTO> getOperationDetails(@RequestParam String date,@RequestParam Integer nombre){
        return operationService.getOperationDetails(date,nombre);
    }
}
