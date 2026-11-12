package com.m2i.pti.service;

import com.m2i.pti.domain.*;
import com.m2i.pti.domain.enumeration.*;
import com.m2i.pti.domain.util.CodeErreur;
import com.m2i.pti.external.domain.ExternalResponse;
import com.m2i.pti.repository.*;
import com.m2i.pti.security.AuthoritiesConstants;
import com.m2i.pti.security.SecurityUtils;
import com.m2i.pti.service.dto.*;
import com.m2i.pti.service.dto.Evenement;
import com.m2i.pti.service.mapper.*;
import com.m2i.pti.service.util.RandomUtil;
import com.m2i.pti.service.util.Util;
import com.m2i.pti.web.rest.errors.CustomParameterizedException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.m2i.pti.mobile.dto.*;
import com.m2i.pti.mobile.enumeration.MTypeCompte;
import com.m2i.pti.service.easyBatch.Read;
import com.m2i.pti.web.rest.errors.CustomSettlementException;
import jakarta.validation.constraints.NotNull;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import jakarta.xml.bind.JAXBException;
import org.apache.commons.collections4.map.PassiveExpiringMap;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StopWatch;

/**
 * Service Implementation for managing Operation.
 */
@Service
public class OperationService {

    private final Logger log = LoggerFactory.getLogger(OperationService.class);
    private final RoleValidationService roleValidationService;
    private final OperationRepository operationRepository;

    private final OperationMapper operationMapper;

    private final PersonneMapper personneMapper;

    private final PersonneMoraleMapper personneMoraleMapper;

    private final PersonnePhysiqueMapper personnePhysiqueMapper;

    private final PersonneRepository personneRepository;

    private final PersonneMoraleRepository personneMoraleRepository;

    private final PersonnePhysiqueRepository personnePhysiqueRepository;
    private final ParametreAppRepository parametreAppRepository;
    private final PieceMapper pieceMapper;

    private final PieceRepository pieceRepository;

    private final BeneficiaireRepository beneficiaireRepository;

    private final EmetteurRepository emetteurRepository;

    private final CanalRepository canalRepository;

    private final TypeServiceRepository typeServiceRepository;

    private final UserService userService;

    private final TableauFraisService tableauFraisService;

    private final TableauCumulService tableauCumulService;

    private final BilanOperationService bilanOperationService;

    private final LigneFraisRepository ligneFraisRepository;

    private final LigneFraisMapper ligneFraisMapper;

    private final PersonneService personneService;

    private final TableauRepartitionCommissionRepository tableauRepartitionCommissionRepository;

    private final LigneRepartitionCommissionRepository ligneRepartitionCommissionRepository;

    private final UniteOrganisationService uniteOrganisationService;

    private final CommissionRepository commissionRepository;

    private final RepartionFraisEmAcqRepository repartionFraisEmAcqRepository;

    private final CanalService canalService;

    private final TypeServiceService typeServiceService;

    private final BilanOperationRepository bilanOperationRepository;

    private final PointVenteService pointVenteService;

    private final OperationAuditService operationAuditService;

    private final AutorisationService autorisationService;

    private final ApiService apiService;

    private final InstructionComptableService instructionComptableService;

    private final CompteService compteService;

    private final UniteOrganisationRepository uniteOrganisationRepository;

    private final ClientMapper clientMapper;

    private final ClientRepository clientRepository;

    private final CompteBanqueRepository compteBanqueRepository;

    private final CompteRepository compteRepository;

    private final CompteAutreRepository compteAutreRepository;

    private final PasswordEncoder passwordEncoder;

    private final MailService mailService;

    private final BilanTaxeService bilanTaxeService;

    private final RepartitionFraisService repartitionFraisService;

    private final RepartitionOpService repartitionOpService;

    private final RegleCommissionService regleCommissionService;

    private final ProfilActeurRepository profilActeurRepository;

    private final TransactionComptePtiService transactionComptePtiService;

    private final CommissionService commissionServicel;

    private final EvenementOperationRepository evenementOperationRepository;

    private final EvenementOperationMapper evenementOperationMapper;

    private final TypeUniteOrganisationRepository typeUniteOrganisationRepository;
    private final TypeUniteOrganisationService typeUniteOrganisationService;
    private final FileGeneratorService fileGeneratorService;
    OperationAuditEventService operationAuditEventService;
    private final ReglementService reglementService;
    private final OperationDTOMapper operationDTOMapper;
    private final LogOperationRepository logOperationRepository;
    private final Read readService;
    private String pathname;
    private TemplateMessageService templateMessageService;
    RecepteurAlarmeService recepteurAlarmeService;
    LigneReglementRepository ligneReglementRepository;
    TaxeOperationService taxeOperationService;
    EvenementOperationService evenementOperationService;
    private final UniteOrganisationMapper uniteOrganisationMapper;
    private final MotifOperationRepository motifOperationRepository;
    private final NationaliteRepository nationaliteRepository;
    private final GroupeProfilActeurRepository groupeProfilActeurRepository;
    private final CompteMapper compteMapper;
    PassiveExpiringMap<String, UniteOrganisation> mapPaysDTO = new PassiveExpiringMap<>(6400000);
    InstructionComptableRegistreRepository instructionComptableRegistreRepository;
    UserControlService userControlService;
    CommissionService commissionService;

    public OperationService(CommissionService commissionService, OperationAuditEventService operationAuditEventService, UserControlService userControlService, InstructionComptableRegistreRepository instructionComptableRegistreRepository, EvenementOperationRepository evenementOperationRepository, EvenementOperationService evenementOperationService, TypeUniteOrganisationService typeUniteOrganisationService, OperationDTOMapper operationDTOMapper, TaxeOperationService taxeOperationService, ReglementService reglementService, LigneReglementRepository ligneReglementRepository, Read readService, ParametreAppRepository parametreAppRepository, RecepteurAlarmeService recepteurAlarmeService, TemplateMessageService templateMessageService, RoleValidationService roleValidationService, TableauCumulService tableauCumulService, BilanOperationRepository bilanOperationRepository, PasswordEncoder passwordEncoder, OperationRepository operationRepository, OperationMapper operationMapper, PersonneMapper personneMapper, PersonneMoraleMapper personneMoraleMapper, PersonnePhysiqueMapper personnePhysiqueMapper, PersonneRepository personneRepository, PersonneMoraleRepository personneMoraleRepository, PersonnePhysiqueRepository personnePhysiqueRepository, PieceMapper pieceMapper, PieceRepository pieceRepository, BeneficiaireRepository beneficiaireRepository, EmetteurRepository emetteurRepository, CanalRepository canalRepository, TypeServiceRepository typeServiceRepository, UserService userService, TableauFraisService tableauFraisService, BilanOperationService bilanOperationService, LigneFraisRepository ligneFraisRepository, LigneFraisMapper ligneFraisMapper, PersonneService personneService, TableauRepartitionCommissionRepository tableauRepartitionCommissionRepository, LigneRepartitionCommissionRepository ligneRepartitionCommissionRepository, UniteOrganisationService uniteOrganisationService,
            CommissionRepository commissionRepository, RepartionFraisEmAcqRepository repartionFraisEmAcqRepository,
            CanalService canalService, TypeServiceService typeServiceService, PointVenteService pointVenteService, OperationAuditService operationAuditService, AutorisationService autorisationService, ApiService apiService, InstructionComptableService instructionComptableService, CompteService compteService, UniteOrganisationRepository uniteOrganisationRepository, ClientMapper clientMapper, ClientRepository clientRepository, CompteBanqueRepository compteBanqueRepository, CompteRepository compteRepository, CompteAutreRepository compteAutreRepository, MailService mailService, BilanTaxeService bilanTaxeService, RepartitionFraisService repartitionFraisService, RepartitionOpService repartitionOpService, RegleCommissionService regleCommissionService, ProfilActeurRepository profilActeurRepository, TransactionComptePtiService transactionComptePtiService, CommissionService commissionServicel, EvenementOperationMapper evenementOperationMapper, TypeUniteOrganisationRepository typeUniteOrganisationRepository, FileGeneratorService fileGeneratorService, LogOperationRepository logOperationRepository, UniteOrganisationMapper uniteOrganisationMapper, MotifOperationRepository motifOperationRepository, NationaliteRepository nationaliteRepository, GroupeProfilActeurRepository groupeProfilActeurRepository, CompteMapper compteMapper) {
        this.userControlService = userControlService;
        this.evenementOperationRepository = evenementOperationRepository;
        this.reglementService = reglementService;
        this.operationDTOMapper = operationDTOMapper;
        this.commissionService = commissionService;
        this.readService = readService;
        this.tableauCumulService = tableauCumulService;
        this.bilanOperationRepository = bilanOperationRepository;
        this.roleValidationService = roleValidationService;
        this.bilanOperationService = bilanOperationService;
        this.operationRepository = operationRepository;
        this.operationMapper = operationMapper;
        this.personneMapper = personneMapper;
        this.personneMoraleMapper = personneMoraleMapper;
        this.personnePhysiqueMapper = personnePhysiqueMapper;
        this.personneRepository = personneRepository;
        this.templateMessageService = templateMessageService;
        this.ligneReglementRepository = ligneReglementRepository;
        this.personneMoraleRepository = personneMoraleRepository;
        this.personnePhysiqueRepository = personnePhysiqueRepository;
        this.pieceMapper = pieceMapper;
        this.pieceRepository = pieceRepository;
        this.beneficiaireRepository = beneficiaireRepository;
        this.emetteurRepository = emetteurRepository;
        this.canalRepository = canalRepository;
        this.typeServiceRepository = typeServiceRepository;
        this.userService = userService;
        this.tableauFraisService = tableauFraisService;
        this.ligneFraisRepository = ligneFraisRepository;
        this.ligneFraisMapper = ligneFraisMapper;
        this.personneService = personneService;
        this.tableauRepartitionCommissionRepository = tableauRepartitionCommissionRepository;
        this.ligneRepartitionCommissionRepository = ligneRepartitionCommissionRepository;
        this.uniteOrganisationService = uniteOrganisationService;
        this.commissionRepository = commissionRepository;
        this.repartionFraisEmAcqRepository = repartionFraisEmAcqRepository;
        this.canalService = canalService;
        this.typeServiceService = typeServiceService;
        this.pointVenteService = pointVenteService;
        this.operationAuditService = operationAuditService;
        this.autorisationService = autorisationService;
        this.apiService = apiService;
        this.instructionComptableService = instructionComptableService;
        this.compteService = compteService;
        this.uniteOrganisationRepository = uniteOrganisationRepository;
        this.clientMapper = clientMapper;
        this.clientRepository = clientRepository;
        this.compteBanqueRepository = compteBanqueRepository;
        this.compteRepository = compteRepository;
        this.compteAutreRepository = compteAutreRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.bilanTaxeService = bilanTaxeService;
        this.repartitionFraisService = repartitionFraisService;
        this.repartitionOpService = repartitionOpService;
        this.regleCommissionService = regleCommissionService;
        this.profilActeurRepository = profilActeurRepository;
        this.transactionComptePtiService = transactionComptePtiService;
        this.commissionServicel = commissionServicel;
        this.evenementOperationMapper = evenementOperationMapper;
        this.typeUniteOrganisationRepository = typeUniteOrganisationRepository;
        this.typeUniteOrganisationService = typeUniteOrganisationService;
        this.fileGeneratorService = fileGeneratorService;
        this.logOperationRepository = logOperationRepository;
        this.recepteurAlarmeService = recepteurAlarmeService;
        this.parametreAppRepository = parametreAppRepository;
        this.taxeOperationService = taxeOperationService;
        this.evenementOperationService = evenementOperationService;
        this.uniteOrganisationMapper = uniteOrganisationMapper;
        this.motifOperationRepository = motifOperationRepository;
        this.nationaliteRepository = nationaliteRepository;
        this.groupeProfilActeurRepository = groupeProfilActeurRepository;
        this.compteMapper = compteMapper;
        this.instructionComptableRegistreRepository = instructionComptableRegistreRepository;
        this.operationAuditEventService = operationAuditEventService;
    }

    @Transactional(readOnly = true)
    public List<LabelValue> findSumOperationOfDateGroupByHour(ZonedDateTime date) {
        List<LabelValue> result = new ArrayList<LabelValue>();
        Page<Operation> all = null;
        for (int i = 0; i < 24; i++) {
            result.add(i, new LabelValue(i + " h", 0));
        }
        Integer alls = operationRepository.countOpeation();
        int nbreLigne = alls;
        int nbrPage = nbreLigne / 100;
        for (int i = 0; i <= nbrPage; i++) {
            Pageable page = PageRequest.of(i, 100, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
            all = operationRepository.findAll(page);

            if (all != null) {
                all.forEach(item -> {
                    if (date.toLocalDate().isEqual(item.getDateOperation().toLocalDate()) && item.getTypeService() != null) {
                        result.get(item.getDateOperation().getHour()).setValue(result.get(item.getDateOperation().getHour()).getValue() + 1);
                    }
                });
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<LabelValue> findSumOperationOfDateGroupByHourOptimise(ZonedDateTime date) {

        Long taille;
        Long npage;

        List<LabelValue> result = new ArrayList<LabelValue>();
        // Page<Operation> all = null;
        for (int i = 0; i < 24; i++) {
            result.add(i, new LabelValue(i + " h", 0));
        }
        Integer alls = operationRepository.countOpeation();
        /*Integer alls = operationRepository.countOpeationOptimise(
                convertStringToLocalDate(date.toLocalDate().toString(), 00, 00, 00),
                convertStringToLocalDate(date.toLocalDate().toString(), 23, 59, 59));
         */ int nbreLigne = alls;
        if (nbreLigne > 0) {
            int nbrPage = nbreLigne / 100;
            for (int i = 0; i <= nbrPage; i++) {
                Pageable page = PageRequest.of(i, 100, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
                taille = new Long(page.getPageSize());
                npage = new Long(page.getPageSize() * page.getPageNumber());
                //  all = operationRepository.findAll(page);
                List<OperationOptimiseDTO> all = operationRepository.findAllOperationsByPeriode(taille, npage);
                log.debug("size--" + all.size());
                if (all != null) {
                    log.debug("size--" + all.size());
                    /*  for (OperationOptimiseDTO item: all){
                        log.debug("ok--"+item);
                        if (date.toLocalDate().isEqual(item.getDateOperation().toLocalDate()) && item.getCodeService() != null) {
                            result.get(item.getDateOperation().getHour()).setValue(result.get(item.getDateOperation().getHour()).getValue() + 1);
                        }
                    }*/
                    all.forEach(item -> {
                        log.debug("size" + item);
                        if (date.toLocalDate().isEqual(item.getDateOperation().toLocalDate()) && item.getCodeService() != null) {
                            result.get(item.getDateOperation().getHour()).setValue(result.get(item.getDateOperation().getHour()).getValue() + 1);
                        }
                    });
                }
            }
        }
        return result;
    }

    private ZonedDateTime convertStringToLocalDate(String dateTobeConverted, int hour, int minute, int seconde) {
        java.time.LocalDate dateLoc = java.time.LocalDate.parse(dateTobeConverted);
        ZonedDateTime zonedDateTimeOf = ZonedDateTime.of(dateLoc.getYear(), dateLoc.getMonthValue(), dateLoc.getDayOfMonth(), hour, minute, seconde, 0, ZoneId.of("UTC"));
        return zonedDateTimeOf;
    }

    @Transactional(readOnly = true)
    public List<LabelValue> findSumOperationGroupByDay(String periode) {
        int nbdays = 0;
        //Page<Operation> all = null;
        List<OperationOptimiseDTO> all = null;
        Long taille;
        Long npage;

        if (periode.indexOf("days") > 0) {
            try {
                nbdays = Integer.parseInt(periode.substring(0, periode.indexOf("days")));
            } catch (NumberFormatException e) {
                nbdays = 0;
            }

        }
        List<LabelValue> result = new ArrayList<LabelValue>();
        Integer alls = operationRepository.countOpeation();
        int nbreLigne = alls;
        int nbrPage = nbreLigne / 100;
        for (int j = 0; j <= nbrPage; j++) {
            Pageable page = PageRequest.of(j, 100, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
            //all = operationRepository.findAll(page);
            taille = new Long(page.getPageSize());
            npage = new Long(page.getPageSize() * page.getPageNumber());

            all = operationRepository.findAllOperationsByPeriode(taille, npage);

            if (nbdays > 0) {
                for (int i = 1; i <= nbdays; i++) {
                    ZonedDateTime tempDate = ZonedDateTime.now().minusDays(i);
                    result.add(new LabelValue("" + tempDate.getMonthValue() + "/" + tempDate.getDayOfMonth(), 0));
                    int finalI = i - 1;
                    if (all != null) {
                        all.forEach(item -> {
                            if (tempDate.toLocalDate().isEqual(item.getDateOperation().toLocalDate()) && item.getCodeService() != null) {
                                result.get(finalI).setValue(result.get(finalI).getValue() + 1);
                            }
                        });
                    }
                }
            } else if (periode.equals("thisweek")) {
                DayOfWeek dayOfWeek = ZonedDateTime.now().getDayOfWeek();
                for (int i = 0; i < dayOfWeek.getValue(); i++) {
                    ZonedDateTime tempDate = ZonedDateTime.now().minusDays(i);
                    result.add(new LabelValue("" + tempDate.getMonthValue() + "/" + tempDate.getDayOfMonth(), 0));
                    int finalI = i;
                    if (all != null) {
                        all.forEach(item -> {
                            if (tempDate.toLocalDate().isEqual(item.getDateOperation().toLocalDate()) && item.getCodeService() != null) {
                                result.get(finalI).setValue(result.get(finalI).getValue() + 1);
                            }
                        });
                    }
                }
            } else if (periode.equals("lastweek")) {
                DayOfWeek dayOfWeek = ZonedDateTime.now().minusWeeks(1).getDayOfWeek();
                for (int i = 0; i < 7; i++) {
                    ZonedDateTime tempDate = ZonedDateTime.now().minusDays(i + dayOfWeek.getValue());
                    result.add(new LabelValue("" + tempDate.getMonthValue() + "/" + tempDate.getDayOfMonth(), 0));
                    int finalI = i;
                    if (all != null) {
                        all.forEach(item -> {
                            if (tempDate.toLocalDate().isEqual(item.getDateOperation().toLocalDate()) && item.getCodeService() != null) {
                                result.get(finalI).setValue(result.get(finalI).getValue() + 1);
                            }
                        });
                    }
                }
            } else if (periode.equals("thismonth")) {
                int dayOfMonth = ZonedDateTime.now().getDayOfMonth();
                for (int i = 0; i < dayOfMonth; i++) {
                    ZonedDateTime tempDate = ZonedDateTime.now().minusDays(i);
                    if (tempDate.getMonth().equals(ZonedDateTime.now().getMonth())) {
                        result.add(new LabelValue("" + tempDate.getMonthValue() + "/" + tempDate.getDayOfMonth(), 0));
                        int finalI = i;
                        if (all != null) {
                            all.forEach(item -> {
                                if (tempDate.toLocalDate().isEqual(item.getDateOperation().toLocalDate()) && item.getCodeService() != null) {
                                    result.get(finalI).setValue(result.get(finalI).getValue() + 1);
                                }
                            });
                        }
                    }
                }
            } else {
                int dayOfMonth = ZonedDateTime.now().minusMonths(1).getDayOfMonth();
                for (int i = 0; i < 31; i++) {
                    ZonedDateTime tempDate = ZonedDateTime.now().minusDays(i + dayOfMonth);
                    if (tempDate.getMonth().equals(ZonedDateTime.now().minusMonths(1).getMonth())) {
                        result.add(new LabelValue("" + tempDate.getMonthValue() + "/" + tempDate.getDayOfMonth(), 0));
                        int finalI = i;
                        if (all != null) {
                            all.forEach(item -> {
                                if (tempDate.toLocalDate().isEqual(item.getDateOperation().toLocalDate()) && item.getCodeService() != null) {
                                    result.get(finalI).setValue(result.get(finalI).getValue() + 1);
                                }
                            });
                        }
                    }

                }
            }
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<LabelValue> findSumOperationGroupByMonth(String periode) {
        List<LabelValue> result = new ArrayList<LabelValue>();
        List<OperationOptimiseDTO> all = null;
        //Page<Operation> all = null;
        Long taille;
        Long npage;
        Integer alls = operationRepository.countOpeation();
        int nbreLigne = alls;
        int nbrPage = nbreLigne / 100;
        for (int j = 0; j <= nbrPage; j++) {
            Pageable page = PageRequest.of(j, 100, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));

            taille = new Long(page.getPageSize());
            npage = new Long(page.getPageSize() * page.getPageNumber());

            all = operationRepository.findAllOperationsByPeriode(taille, npage);

            //all = operationRepository.findAll(page);
            if (periode.equals("12month")) {
                for (int i = 1; i <= 12; i++) {
                    ZonedDateTime tempDate = ZonedDateTime.now().minusMonths(i);
                    result.add(new LabelValue(tempDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRANCE) + " " + tempDate.getYear(), 0));
                    int finalI = i - 1;
                    all.forEach(item -> {
                        if (tempDate.getYear() == item.getDateOperation().getYear() && tempDate.getMonth().equals(item.getDateOperation().getMonth())) {
                            result.get(finalI).setValue(result.get(finalI).getValue() + 1);
                        }
                    });
                }
            } else if (periode.equals("thisyear")) {
                for (int i = 0; i <= 12; i++) {
                    ZonedDateTime tempDate = ZonedDateTime.now().minusMonths(i);
                    if (tempDate.getYear() == ZonedDateTime.now().getYear()) {
                        result.add(new LabelValue(tempDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRANCE) + " " + tempDate.getYear(), 0));
                        int finalI = i;
                        all.forEach(item -> {
                            if (tempDate.getYear() == item.getDateOperation().getYear() && tempDate.getMonth().equals(item.getDateOperation().getMonth())) {
                                result.get(finalI).setValue(result.get(finalI).getValue() + 1);
                            }
                        });
                    } else {
                        break;
                    }
                }

            } else {
                for (int i = 0; i <= 12; i++) {
                    ZonedDateTime tempDate = ZonedDateTime.now().minusMonths(i).minusDays(ZonedDateTime.now().getDayOfYear());
                    if (tempDate.getYear() == ZonedDateTime.now().minusYears(1).getYear()) {
                        result.add(new LabelValue(tempDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRANCE) + " " + tempDate.getYear(), 0));
                        int finalI = i;
                        all.forEach(item -> {
                            if (tempDate.getYear() == item.getDateOperation().getYear() && tempDate.getMonth().equals(item.getDateOperation().getMonth())) {
                                result.get(finalI).setValue(result.get(finalI).getValue() + 1);
                            }
                        });
                    }
                }
            }
        }
        return result;
    }

    public String getExtraData(Long id) {
        String str = operationRepository.getExtraData(id);
        return str;
    }

    @Transactional(readOnly = true)
    public List<LabelValue> findSumOperationOfLastFiveYearGroupByYear() {
        List<LabelValue> result = new ArrayList<LabelValue>();
        List<OperationOptimiseDTO> all = null;
        //Page<Operation> all = null;
        Long taille;
        Long npage;
        Integer alls = operationRepository.countOpeation();
        int nbreLigne = alls;
        int nbrPage = nbreLigne / 100;
        for (int j = 0; j <= nbrPage; j++) {
            Pageable page = PageRequest.of(j, 100, Sort.by(new Sort.Order(Sort.Direction.ASC, "id")));
            taille = new Long(page.getPageSize());
            npage = new Long(page.getPageSize() * page.getPageNumber());

            all = operationRepository.findAllOperationsByPeriode(taille, npage);

            //all = operationRepository.findAll(page);
            for (int i = 0; i < 5; i++) {
                ZonedDateTime tempDate = ZonedDateTime.now().minusDays(ZonedDateTime.now().getDayOfYear()).minusYears(i);
                result.add(new LabelValue("" + tempDate.getYear(), 0));
                ZonedDateTime finalTempDate = tempDate;
                int finalI = i;
                all.forEach(item -> {
                    if (finalTempDate.getYear() == item.getDateOperation().getYear()) {
                        result.get(finalI).setValue(result.get(finalI).getValue() + 1);
                    }
                });
            }
        }
        return result;
    }

    public void reversionOperation(Long operationId) {
        instructionComptableService.reversionOperation(operationId);
    }

    /**
     * Consultation de toutes les informations d'une operation
     *
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public OperationAllDTO findOne(Long id) {
        Operation operation = operationRepository.findById(id).orElse(null);
        PersonneAllDTO beneficiaire = null;
        PersonneAllDTO emetteur = null;
        if (operation != null) {
            UniteOrganisation uoAcq = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());
            OperationAllDTO operationAllDTO = new OperationAllDTO();
            if (operation.getBeneficaire() != null) {
                beneficiaire = personneService.finAllInfoOfPersonne(operation.getBeneficaire().getPersonne().getId());
                if (operation.getPieceBeneficiaire() != null) {
                    beneficiaire.setPieceDTO(pieceMapper.toDto(operation.getPieceBeneficiaire()));
                }
                if (operation.getBeneficaire() != null && operation.getBeneficaire().getPersonne() != null && operation.getEmetteur() != null && operation.getEmetteur().getPersonne() != null && operation.getBeneficaire().getPersonne().equals(operation.getEmetteur().getPersonne())) {
                    operationAllDTO.setTypeBeneficiaire("Titulaire");
                    if (operation.getBeneficaire() != null) {
                        if (operation.getBeneficaire().getPersonne().equals(operation.getEmetteur().getPersonne())) {
                            operationAllDTO.setTypeBeneficiaire("Titulaire");
                        }
                    } else {
                        operationAllDTO.setTypeBeneficiaire("Autre");
                    }
                }
            }
            if (operation.getEmetteur() != null) {
                emetteur = personneService.finAllInfoOfPersonne(operation.getEmetteur().getPersonne().getId());
                if (operation.getPieceEmetteur() != null) {
                    emetteur.setPieceDTO(pieceMapper.toDto(operation.getPieceEmetteur()));
                }
            }

            if (operation.getCompteSrc() != null) {
                Compte compteSrc = operation.getCompteSrc();
                Client clientSrc = compteSrc.getClient();
                if (emetteur != null) {
                    emetteur.setClientDTO(clientMapper.toDto(clientSrc));
                    getCompteCbis(emetteur, compteSrc);
                }

            }
            if (operation.getCompteDest() != null) {
                Compte compteDest = operation.getCompteDest();
                Client clientDest = compteDest.getClient();
                if (beneficiaire != null) {
                    beneficiaire.setClientDTO(clientMapper.toDto(clientDest));
                    getCompteCbis(beneficiaire, compteDest);
                }

            }

            TypeService typeService = operation.getTypeService();
            OperationDTO operationDTO = operationMapper.toDto(operation);
            if (typeService.isOperationClient() != null && typeService.isOperationClient()) {
                UniteOrganisation fseAcq = operation.getUoAcquereur();
                UniteOrganisation fseEm = operation.getUoEmetteur();
                //UniteOrganisation uoPayAcq = uniteOrganisationService.findUoParentByType(fseAcq, TypeUO.PAYS);
                UniteOrganisation uoPayAcq = returnPays(fseAcq); //uniteOrganisationService.findOneById(fseAcq.getUoPaysId());
                //      UniteOrganisation uoPayEm = uniteOrganisationService.findUoParentByType(fseEm, TypeUO.PAYS);
                UniteOrganisation uoPayEm = returnPays(fseEm); // uniteOrganisationService.findOneById(fseEm.getUoPaysId());
                operationDTO.setUoAcquereurId(uoPayAcq.getId());
                operationDTO.setUoEmetteurId(uoPayEm.getId());
                operationDTO.setFseSrc(fseEm);
                operationDTO.setFseDest(fseAcq);
            } else if (uoAcq != null && !uoAcq.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.toString())) {

                UniteOrganisation uoPays = returnPays(uoAcq); //uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                // UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);
                operationDTO.setUoAcquereurId(uoPays.getId());
            }

            operationAllDTO.setBeneficiaire(beneficiaire);
            operationAllDTO.setEmetteur(emetteur);
            operationAllDTO.setOperationDTO(operationDTO);
            return operationAllDTO;
        }
        return null;
    }

    /**
     * Service de transfert d'argent (_ENVOI_CASH) Verifier la disponibilite du
     * montant Op du CPTEOPERATIONACTEURPTI de Verifier la validitÃ© du
     * bordereau de versment si on est en agene Generation du code retrait
     * Repartion des frais entre acteur Comptabilisation sur OMNICANAL
     * Comptabilisation su PTI
     */
    @Transactional
    public OperationDTO doOperationEnvoiCash(@NotNull OperationAllDTO operationAllDTO) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("Operation d'envoi cash en cours ...");
        User userConnecte = userService.getUserWithAuthorities();
        userCanDoOperations(userConnecte);

        OperationDTO operationDTO = operationAllDTO.getOperationDTO();
        PointVente pointVente = userConnecte.getPointVente();
        Compte compteOperationActeur = compteService.getCompteOfOrganisationConnecte(EtiquetteCompteCode.CPTEOPERATIONACTEURPTI);
        List<Operation> ops = operationRepository.findByPointVenteIdAndStatut(pointVente.getId(), StatutOperation.ENATTENTE_VALIDATION);
        BigDecimal mt = BigDecimal.ZERO;
        if (!ops.isEmpty()) {
            mt = BigDecimal.valueOf(ops.stream().mapToDouble(op -> op.getMontantOp().doubleValue()).sum());
        }
        //Verification de la disponibilitÃ© du montant Ã  debiter chez un sous distributeur
        Boolean debitEstPossible = compteService.peutDebiterCompte(compteOperationActeur, operationDTO.getMontantFrais().add(operationAllDTO.getOperationDTO().getMontantOp()).add(mt));
        if (debitEstPossible || pointVente.getUniteOrganisation().getTypeUO().getLibelleTypeUO().equals(TypeUO.AGENCE.name())) {
            TypeService typeService = typeServiceService.findByCodeService(String.valueOf(TypeServiceLibelle.ENVOI_CASH));
            if (verifAutorisationOfTypeService(typeService)) {
                UniteOrganisation uniteOrganisationSource = userConnecte.getUniteOrganisation();
                //Verifie la validitÃ© du code externe provenant du bordereau de versement en agence
                if (!operationRepository.existsByCodeExterne(operationDTO.getCodeExterne())
                        || uniteOrganisationSource.getTypeUO().getLibelleTypeUO().equals("MASTER")
                        || uniteOrganisationSource.getTypeUO().getLibelleTypeUO().equals("SOUS_DISTRIBUTEUR")) {
                    Operation operation = operationMapper.toEntity(operationDTO);
                    saveInfosPersonne(operationAllDTO.getBeneficiaire(), "BENEFICIAIRE", operation, true);
                    saveInfosPersonne(operationAllDTO.getEmetteur(), "EMETTEUR", operation, false);
                    Canal canal = getCanal(operationDTO);
                    String otps[] = RandomUtil.generateOtp();
                    String codeRetrait = otps[1];
                    if (operationDTO.getLieuPaiementId() != null) {
                        operation.uoAcquereur(uniteOrganisationService.findOneById(operationDTO.getLieuPaiementId()));
                    }

                    if (operationDTO.getStatut() == null) {
                        operation.statut(StatutOperation.ENATTENTE);
                    }
                    operation.dateOperation(ZonedDateTime.now())
                            .commentaire(codeRetrait)
                            .codeRetrait(RandomUtil.getMd5Code(codeRetrait))
                            .commentaire(Base64.encodeBase64String(codeRetrait.getBytes()))
                            .secret(otps[0])
                            .canal(canal)
                            .pointVente(pointVente)
                            .typeService(typeService)
                            .dateExpiration(LocalDate.now().plusDays(RandomUtil.NBR_JOURS))
                            .etape(Etape.PROCEED)
                            .uoEmetteur(uniteOrganisationSource)
                            .user(userConnecte)
                            .codeExterne(operation.getCodeExterne())
                            .codeOperation(RandomUtil.getCodeOperation(operation.getDateOperation()));
                    UniteOrganisation uoPaysSrc = returnPays(uniteOrganisationSource); //uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());
                    // UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.PAYS);
                    UniteOrganisation uniteOrganisationDest = uniteOrganisationService.findOneById(operationDTO.getUoAcquereurId());
                    UniteOrganisation fseDest = uniteOrganisationService.getUoFseFilialeWithUoPays(uniteOrganisationDest);
                    UniteOrganisation fseSrc = uniteOrganisationService.getUoFseFilialeWithUoPays(uoPaysSrc);
                    UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(fseDest);
                    operation = operationRepository.save(operation);
                    operation = calculRepartitionFrais(operation, fseSrc.getId(), fseDest.getId());
                    operationAuditService.saveOpAudit(operation, null, operation.getStatut());
                    bilanTaxeService.saveBilanTaxes(operation, operationDTO.getTaxes());
                    if (operation.getStatut().equals(StatutOperation.ENATTENTE)) {
                        Result response = apiService.sendRequestForComptabilisationEnvoiCash(uoPaysSrc, uniteOrganisationDest, operation, fseSrc, fseDest, fseDestAgenceSiege, Boolean.TRUE);
                        verifResultOfOmnicanal(response, operation);
                        log.info("Debut de repartition des commissions et bilan des operations ...");
                        //UniteOrganisation uoPv =  uniteOrganisationService.findById(operation.getPointVente().getUniteOrganisation().getId());
                        TypeUniteOrganisation typeUniteOrganisation = uniteOrganisationSource.getTypeUO();
                        List<RegleCommission> regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), uniteOrganisationDest.getId(), null, null, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
                        RegleCommission regleCommission;
                        if (regleCommissionExist.isPresent()) {
                            regleCommission = regleCommissionExist.get();
                        } else {
                            regleCommission = null;
                        }
                        //instructionComptableService.comptabiliserOperationWithRegle(operation, fseDestAgenceSiege, uniteOrganisationSource, regleCommission);
                        repartitionCommission(operation, pointVente.getUniteOrganisation(), MontantsAComptabiliser.FRAISEMT);
                        sendMessage(operation, Boolean.TRUE, codeRetrait);
                        bilanOperationService.saveCumulJournalierActeur(uniteOrganisationSource.getId(), typeService.getId(), canal.getId(), null, null, uoPaysSrc.getId(), operationAllDTO.getOperationDTO().getUoAcquereurId(),
                                operationDTO.getMontantOp());

                        bilanOperationService.saveCumulJournalierClient(null, typeService.getId(), canal.getId(), operation.getBeneficaire().getId(), operation.getEmetteur().getId(), uoPaysSrc.getId(), operation);
                    }
                    return operationMapper.toDto(operation);
                } else {
                    throw new CustomParameterizedException("Le numero de versement est introuvable.");
                }
            } else {
                throw new CustomParameterizedException("Vous n'Ãªtes pas autorisÃ© Ã  utiliser ce service. ");
            }
        } else {
            throw new CustomParameterizedException("Fonds insuffisants");
        }
    }

    /**
     * Faire le redressement d'une operation payÃ© Ã  tord (On se base sur
     * l'operation de retrait)
     *
     * @return
     */
    @Transactional
    public OperationDTO doOperationRedressement(OperationDTO operationDTO) throws IOException, NoSuchAlgorithmException, JAXBException {
        Long operationId = operationDTO.getId();
        Operation opRetrait = operationRepository.findById(operationId).orElse(null);
        Operation opInit = opRetrait.getOperationTraite();
        if (opRetrait != null && opInit != null) {
            log.info(" Operation de redressement en cours ...");
            if (opRetrait.getStatut().equals(StatutOperation.PAYE)) {

                Operation operation = new Operation(opInit);
                operation.uoEmetteur(opRetrait.getUoAcquereur());
                operation.uoAcquereur(opInit.getUoAcquereur());
                BigDecimal parAcq = repartitionOpService.getMontantByActeurAndOp(Acteur.ACQ, opInit.getId());
                operation.montantFrais(parAcq);
                String otps[] = RandomUtil.generateOtp();
                String codeRetrait = otps[1];

                Beneficiaire beneficiaire = operation.getBeneficaire();
                Personne personne = beneficiaire.getPersonne();
                PersonnePhysique personnePhysique = personne.getPersonnePhysique();

                Personne p = new Personne();
                p.setTypePersonne(TypePersonne.PHYSIQUE);
                PersonnePhysique physique = new PersonnePhysique();
                physique.nomClient(personnePhysique.getNomClient())
                        .prenomClient(personnePhysique.getPrenomClient())
                        .sexe(personnePhysique.getSexe());
                physique = personnePhysiqueRepository.save(physique);
                p.setPersonnePhysique(physique);
                p = personneRepository.save(p);
                Beneficiaire b = new Beneficiaire();
                b.setPersonne(p);
                b = beneficiaireRepository.save(b);

                operation.dateOperation(ZonedDateTime.now())
                        .codeRetrait(RandomUtil.getMd5Code(codeRetrait))
                        .secret(otps[0])
                        .dateExpiration(LocalDate.now().plusDays(RandomUtil.NBR_JOURS))
                        .etape(Etape.PROCEED)
                        .montantTaxe(BigDecimal.ZERO)
                        .statut(StatutOperation.ENATTENTE)
                        .pointVente(opRetrait.getPointVente())
                        .montantFrais(parAcq)
                        .user(userService.getUserWithAuthorities())
                        .codeOperation(RandomUtil.getCodeOperation(operation.getDateOperation()))
                        .beneficaire(b)
                        .user(userService.getUserWithAuthorities());

                operation = operationRepository.save(operation);
                opRetrait.statut(StatutOperation.ANNULLE);
                operationRepository.save(opRetrait);

                UniteOrganisation uniteOrganisationSource = operation.getPointVente().getUniteOrganisation();
                verifFonds(operation.getMontantOp(), uniteOrganisationSource, EtiquetteCompteCode.CPTEOPERATIONACTEURPTI);
                //  UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.PAYS);
                UniteOrganisation uoPaysSrc = returnPays(uniteOrganisationSource); //uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());
                UniteOrganisation uniteOrganisationDest = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());
                UniteOrganisation fseDest = uniteOrganisationService.getUoFseFilialeWithUoPays(uniteOrganisationDest);//uo agence,master,sous distributeur
                UniteOrganisation fseSrc = uniteOrganisationService.getUoFseFilialeWithUoPays(uoPaysSrc);
                UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(fseDest);

                Result response = apiService.sendRequestForComptabilisationEnvoiCash(uoPaysSrc, uniteOrganisationDest, operation, fseSrc, fseDest, fseDestAgenceSiege, Boolean.TRUE);
                verifResultOfOmnicanal(response, operation);
                log.info(" :Bilan de l'operation en cours ...");
                //UniteOrganisation uoPv =  uniteOrganisationService.findById(operation.getPointVente().getUniteOrganisation().getId());
                TypeUniteOrganisation typeUniteOrganisation = uniteOrganisationSource.getTypeUO();
                List<RegleCommission> regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), uniteOrganisationDest.getId(), null, null, operation.getTypeService().getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
                RegleCommission regleCommission;
                if (regleCommissionExist.isPresent()) {
                    regleCommission = regleCommissionExist.get();
                } else {
                    regleCommission = null;
                }
                instructionComptableService.comptabiliserOperationWithRegle(operation, fseDestAgenceSiege, operation.getUoEmetteur(), regleCommission);
                sendMessage(operation, Boolean.TRUE, codeRetrait);

                bilanOperationService.saveCumulJournalierActeur(uniteOrganisationSource.getId(), operation.getTypeService().getId(), operation.getCanal().getId(), null, null, uoPaysSrc.getId(), operation.getUoAcquereur().getId(),
                        operation.getMontantOp());
                return operationMapper.toDto(operation);
            } else {
                throw new CustomParameterizedException("Impossible de realiser le redressement de cette opÃ©ration.");
            }

        }
        return null;
    }

    public ExternalResponse getOperationStatus(String codeOperation, String option) {
        if (option.equals("EXTERNE")) {
            Operation oper = operationRepository.findByCodeOperationExterne(codeOperation);
            /**
             * Pour l'option externe on retour le code opération et le statut
             */
            if (oper != null) {
                return new ExternalResponse("0", "SUCCES", oper.getCodeOperation(), oper.getMontantOp() + "");

            } else {
                return new ExternalResponse("-1", "OPERATION INTROUVABLE", null, null);

            }

        } else {
            Optional<Operation> op = operationRepository.findByCodeOperation(codeOperation);
            if (op.isPresent()) {
                return new ExternalResponse("0", "SUCCES", op.get().getCodeOperation(), op.get().getMontantOp() + "");

            } else {
                return new ExternalResponse("-1", "OPERATION INTROUVABLE", null, null);
            }
        }

    }

    /**
     * Verification de la disponibilitÃ© du montant Ã  retirer dans le compte
     * CPTEATTENTERETRAITPTI Repartition des commissions
     *
     * @param operationAllDTO
     * @return
     * @throws IOException
     */
    @Transactional
    public OperationDTO doOperationRetrait(@NotNull OperationAllDTO operationAllDTO) throws IOException, JAXBException {
        User userConnecte = userService.getUserWithAuthorities();
        userCanDoOperations(userConnecte);
        UniteOrganisation uoAcq = uniteOrganisationService.getUoOfUserConnecte();
        verifFonds(operationAllDTO.getOperationDTO().getMontantOp(), uoAcq, EtiquetteCompteCode.CPTEATTENTERETRAITPTI);
        TypeService typeService = typeServiceService.findByCodeService(String.valueOf(TypeServiceLibelle.RETRAIT));
        Canal canal = canalService.findByCodeCanal(operationAllDTO.getOperationDTO().getCanalRetrait());
        log.info(" : Operation de retrait en cours ...");
        if (verifAutorisationOfTypeService(typeService)) {
            Operation operation = operationMapper.toEntity(operationAllDTO.getOperationDTO());
            operation.statut(StatutOperation.PAYE);
            operation = operationRepository.save(operation);
            operationAuditService.saveOpAudit(operation, operationAllDTO.getOperationDTO().getStatut(), StatutOperation.PAYE);

            Personne personne = personneMapper.toEntity(operationAllDTO.getBeneficiaire().getPersonneDTO());
            Piece pieceB = pieceMapper.toEntity(operationAllDTO.getBeneficiaire().getPieceDTO());
            String telephone = personne.getTelephone();
            String code = personne.getCodePaysTelephone();

            if (telephone != null && pieceB != null) {
                Optional<Personne> searchPersonne = personneRepository.findTop1ByTelephoneAndCodePaysTelephone(telephone, code);
                if (searchPersonne.isPresent()) {
                    Personne p = searchPersonne.get();
                    List<Piece> pieceExistantList = pieceRepository.findByTitulaireId(p.getId());
                    if (pieceExistantList != null) {
                        for (Piece pieceExistant : pieceExistantList) {
                            if (pieceExistant.getReferencePiece().equals(pieceB.getReferencePiece())
                                    && pieceExistant.getTypePiece().getId().equals(pieceB.getTypePiece().getId())) {
                                pieceB.setId(pieceExistant.getId());
                            }
                        }
                    }
                    personne.setId(p.getId());
                    Beneficiaire beneficiaire = operation.getBeneficaire();
                    Beneficiaire beneficiaireExistant = beneficiaireRepository.findByPersonneId(personne.getId());
                    if (beneficiaireExistant == null) { //La personne est deja emetteur
                        beneficiaire.setPersonne(personne);
                        beneficiaire = beneficiaireRepository.save(beneficiaire);
                        operation.setBeneficaire(beneficiaire);
                    } else {
                        operation.setBeneficaire(beneficiaireExistant);
                    }
                } else {
                    personne = personneRepository.save(personne);
                }
                pieceB.setTitulaire(personne);
                pieceRepository.save(pieceB);
            } else {
                throw new CustomParameterizedException("Les informations du beneficiaires sont invalides.");
            }

            Operation operationRetrait = new Operation();
            operationRetrait.statut(StatutOperation.PAYE)
                    .beneficaire(operation.getBeneficaire())
                    .canal(canal)
                    .emetteur(operation.getEmetteur())
                    .beneficaire(operation.getBeneficaire())
                    .typeService(typeService)
                    .pointVente(userConnecte.getPointVente())
                    .uoAcquereur(uoAcq)
                    .montantOp(operation.getMontantOp())
                    .montantOpEnDevise(operation.getMontantOpEnDevise())
                    .dateOperation(ZonedDateTime.now())
                    .etape(Etape.PROCEED)
                    .uoEmetteur(uoAcq)
                    .devisePaiement(operation.getDevisePaiement())
                    .operationTraite(operation)
                    .user(userConnecte)
                    .codeOperation(RandomUtil.getCodeOperation(operation.getDateOperation()));
            operationRetrait.setPieceBeneficiaire(pieceB);
            operationRetrait = operationRepository.save(operationRetrait);

            operationAuditService.saveOpAudit(operationRetrait, null, StatutOperation.PAYE);

            Result result = apiService.sendRequestForComptabilisationRetrait(operation, operation.getPointVente().getUniteOrganisation());
            verifResultOfOmnicanal(result, operation);
            instructionComptableService.comptabiliserOperation(operationRetrait, null, uoAcq);

            repartitionCommission(operation, uniteOrganisationService.getUoOfUserConnecte(), MontantsAComptabiliser.FRAISACQ);

            sendMessageConfirmationRetrait(operation);
            return operationMapper.toDto(operationRetrait);
        }
        return null;
    }

    @Transactional
    public void comptabilisationRetrait(Operation op, UniteOrganisation uo, Operation opRetrait) throws IOException, JAXBException {
        Result response = apiService.sendRequestForComptabilisationRetrait(opRetrait, opRetrait.getPointVente().getUniteOrganisation());
        verifResultOfOmnicanal(response, opRetrait);
        instructionComptableService.comptabiliserOperation(opRetrait, null, uo);
        if (opRetrait.getTypeService().getCode().equals(TypeServiceLibelle.RETRAIT.toString())) {
            op = opRetrait.getOperationTraite();
        }

        repartitionCommission(opRetrait, uo, MontantsAComptabiliser.FRAISACQ);
        //     repartitionCommission(op, uo, MontantsAComptabiliser.FRAISACQ);
        sendMessageConfirmationRetrait(op);

    }

    /**
     * Operations de type { ACCOUNT_ACCOUNT, ACCOUNT_CASH , CASH_ACCOUNT}
     */
    @Transactional
    public OperationDTO doOperationAccount(@NotNull OperationAllDTO operationAllDTO) throws IOException, NoSuchAlgorithmException, JAXBException {
        OperationDTO operationDTO = operationAllDTO.getOperationDTO();
        User userConnecte = userService.getUserWithAuthorities();
        userCanDoOperations(userConnecte);
        UniteOrganisation uniteOrganisationSource;
        uniteOrganisationSource = userConnecte.getUniteOrganisation();
        String typeServiceLibelle = operationAllDTO.getTypeService();
        TypeService typeService = typeServiceService.findByCodeService(String.valueOf(typeServiceLibelle));
        log.info("Operation de virement ou d'appel de fonds en cours ...");

        if (verifAutorisationOfTypeService(typeService)) {
            Operation operation = operationMapper.toEntity(operationDTO);
            PersonneAllDTO beneficiaire;
            PersonneAllDTO emetteur;
            if (typeServiceLibelle.equals(String.valueOf(TypeServiceLibelle.CASH_ACCOUNT))) {
                verifFonds(Util.getZeroIfNull(operationDTO.getMontantFrais()).add(Util.getZeroIfNull(operationDTO.getMontantOp())), uniteOrganisationSource, EtiquetteCompteCode.CPTEOPERATIONACTEURPTI);
            }
            if (operationRepository.existsByCodeExterne(operationDTO.getCodeExterne())
                    && uniteOrganisationSource.getTypeUO().getLibelleTypeUO().equals("AGENCE")
                    && typeServiceLibelle.equals(String.valueOf(TypeServiceLibelle.CASH_ACCOUNT))) {
                throw new CustomParameterizedException("Le numero de versement est dÃ©ja utilisÃ©.");
            }
            Canal canal = getCanal(operationAllDTO.getOperationDTO());
            PointVente pointVente = userConnecte.getPointVente();

            // Le beneficiaire peut etre l'emetteur
            if (typeServiceLibelle.equals(String.valueOf(TypeServiceLibelle.ACCOUNT_CASH))) {
                //Si le beneficiaire de la ACCOUNT_CASH est le beneficiaire du CASH
                if (operationAllDTO.getTypeBeneficiaire() != null && operationAllDTO.getTypeBeneficiaire().equals("Titulaire")) {
                    emetteur = operationAllDTO.getEmetteur();
                    saveInfosPersonne(emetteur, "EMETTEUR", operation, true);
                } else { //Si le beneficiaire du cash est une autre personne
                    beneficiaire = operationAllDTO.getBeneficiaire();
                    emetteur = operationAllDTO.getEmetteur();
                    saveInfosPersonne(beneficiaire, "BENEFICIAIRE", operation, false);
                    saveInfosPersonne(emetteur, "EMETTEUR", operation, false);
                }
                if (operationDTO.getStatut() == null) {
                    operation.statut(StatutOperation.ENATTENTE)
                            .etape(Etape.PROCEED);
                }
            } else { //Pour un autre type d'operation
                beneficiaire = operationAllDTO.getBeneficiaire();
                emetteur = operationAllDTO.getEmetteur();
                saveInfosPersonne(beneficiaire, "BENEFICIAIRE", operation, false);
                saveInfosPersonne(emetteur, "EMETTEUR", operation, false);
                if (operationDTO.getStatut() == null) {
                    operation.statut(StatutOperation.PAYE)
                            .etape(Etape.PROCEED);
                }
            }

            if (operationDTO.getLieuPaiementId() != null) {
                operation.uoAcquereur(uniteOrganisationService.findOneById(operationDTO.getLieuPaiementId()));
            }

            operation.dateOperation(ZonedDateTime.now())
                    .canal(canal)
                    .pointVente(pointVente)
                    .typeService(typeService)
                    .user(userConnecte)
                    .dateExpiration(LocalDate.now().plusDays(RandomUtil.NBR_JOURS))
                    .codeOperation(RandomUtil.getCodeOperation(operation.getDateOperation()));

            Long fseDestId = operationDTO.getUoAcquereurId();
            Long fseSrcId = operationDTO.getUoEmetteurId();
            UniteOrganisation fseDest = uniteOrganisationService.findOneById(fseDestId);
            UniteOrganisation fseSrc = uniteOrganisationService.findOneById(fseSrcId);
            operation = operationRepository.save(operation);
            operation = calculRepartitionFrais(operation, fseSrcId, fseDestId);
            operationAuditService.saveOpAudit(operation, null, StatutOperation.ENATTENTE);
            bilanTaxeService.saveBilanTaxes(operation, operationDTO.getTaxes());

            //Comptabilisation et repartition des frais pour une operation de type CASH ACCOUNT
            if (operation.getStatut().equals(StatutOperation.ENATTENTE) || operation.getStatut().equals(StatutOperation.PAYE)) {
                if (typeServiceLibelle.equals(String.valueOf(TypeServiceLibelle.CASH_ACCOUNT))) {

                    UniteOrganisation uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(fseDest);
                    UniteOrganisation uoPaysSrc = returnPays(uniteOrganisationSource);// uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());
                    // UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.PAYS);
                    //    UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq
                    UniteOrganisation uoPaysDest = returnPays(fseDest);  //uniteOrganisationService.findOneById(fseDest.getUoPaysId());
                    Compte compte = operation.getCompteDest();

                    Result response = apiService.sendRequestComptabilisationOfCashAccountToOmnicanal(uoPaysSrc, uoPaysDest, operation, fseSrc, fseDest, compte, Boolean.TRUE, Boolean.TRUE);
                    verifResultOfOmnicanal(response, operation);
                    //UniteOrganisation uoPv =  uniteOrganisationService.findById(operation.getPointVente().getUniteOrganisation().getId());
                    TypeUniteOrganisation typeUniteOrganisation = uniteOrganisationSource.getTypeUO();

                    List<RegleCommission> regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), uoPaysDest.getId(), fseSrc.getId(), fseDest.getId(), typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                    Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
                    RegleCommission regleCommission;
                    if (regleCommissionExist.isPresent()) {
                        regleCommission = regleCommissionExist.get();
                    } else {
                        regleCommission = null;
                    }

                    instructionComptableService.comptabiliserOperationWithRegle(operation, uoDestAgenceSiege, uniteOrganisationSource, regleCommission);
                    repartitionCommission(operation, pointVente.getUniteOrganisation(), MontantsAComptabiliser.FRAISEMT);
                    repartitionCommissionCompensateur(operation);

                    bilanOperationService.saveCumulJournalierActeur(uniteOrganisationSource.getId(), typeService.getId(), canal.getId(), null, null, uoPaysSrc.getId(), uoPaysDest.getId(),
                            operationDTO.getMontantOp());

                    bilanOperationService.saveCumulJournalierClient(null, typeService.getId(), canal.getId(), operation.getBeneficaire().getId(), operation.getEmetteur().getId(), uoPaysSrc.getId(), operation);
                    /*Appel au message*/
                    return operationMapper.toDto(operation);
                } else {
                    return operationMapper.toDto(comptabilisationOperationMad(operation, pointVente, null));
                }
            } else {
                return operationMapper.toDto(operation);
            }

        } else {
            throw new CustomParameterizedException("Autorisation ! ");
        }
    }

    @Transactional
    public OperationDTO doOperationRegulation(OperationDTO operationDTO) throws IOException {
        if (operationDTO != null) {
            log.info(" : Operation de crÃ©ation de monnaie en cours ...");
            User userConnecte = userService.getUserWithAuthorities();
            // userCanDoOperations(userConnecte);
            UniteOrganisation userOrganisation = userConnecte.getUniteOrganisation();
            //Si c'est un master on verifie la disponibilitÃ© du montant necessaire Ã  l'approvisionnement
            TypeService typeService = typeServiceService.findByCodeService(String.valueOf(operationDTO.getCode()));//
            // if (verifAutorisationOfTypeService(typeService)) {

            //verification de la validitÃ© du bordereau en agence
            UniteOrganisation organisation = uniteOrganisationService.findOneById(operationDTO.getUoAcquereurId());
            Operation operationCreationMonnaie = operationMapper.toEntity(operationDTO);
            Canal canal = getCanal(operationDTO);
            //init operation
            operationCreationMonnaie.statut(StatutOperation.ENATTENTE_VALIDATION)
                    .beneficaire(null)
                    .canal(canal)
                    .emetteur(null)
                    .description("REGULATION")
                    .beneficaire(null)
                    .typeService(typeService)
                    .pointVente(userConnecte.getPointVente())
                    .montantFrais(BigDecimal.ZERO)
                    .montantFrais(BigDecimal.ZERO)
                    .montantFraisAcq(BigDecimal.ZERO)
                    .dateOperation(ZonedDateTime.now())
                    .etape(Etape.INIT)
                    .uoEmetteur(organisation)
                    .uoAcquereur(organisation)
                    .user(userConnecte)
                    .codeOperation(RandomUtil.getCodeOperation(operationCreationMonnaie.getDateOperation()));
            operationCreationMonnaie = operationRepository.save(operationCreationMonnaie);//Enregistrement d'une operation
            operationAuditService.saveOpAudit(operationCreationMonnaie, null, StatutOperation.PAYE);
            /*  if (!userOrganisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.MASTER))
                        && !organisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.AGENCE))) {
                    Result response = apiService.sendRequestComptabilisationOfApprovisionnementToOmnicanal(operationCreationMonnaie, operationCreationMonnaie.getPointVente().getUniteOrganisation(), organisation);
                    verifResultOfOmnicanal(response, operationCreationMonnaie);
                }*/
            ///  instructionComptableService.comptabiliserOperation(operationCreationMonnaie, userOrganisation, userOrganisation);
            return operationMapper.toDto(operationCreationMonnaie);
            //   }
        }
        throw new CustomParameterizedException("Operation invalide .");
    }

    @Transactional
    public OperationDTO doOperationCreationMonnaie(OperationDTO operationDTO) throws IOException {
        if (operationDTO != null) {
            log.info(" : Operation de crÃ©ation de monnaie en cours ...");
            User userConnecte = userService.getUserWithAuthorities();
            if (userConnecte.getPointVente() != null) {
                // userCanDoOperations(userConnecte);
                UniteOrganisation userOrganisation = userConnecte.getUniteOrganisation();
                //Si c'est un master on verifie la disponibilitÃ© du montant necessaire Ã  l'approvisionnement
                TypeService typeService = typeServiceService.findByCodeService(String.valueOf(operationDTO.getCode()));//
                // if (verifAutorisationOfTypeService(typeService)) {

                //verification de la validitÃ© du bordereau en agence
                UniteOrganisation organisation = uniteOrganisationService.findOneById(operationDTO.getUoAcquereurId());

                Operation operationCreationMonnaie = operationMapper.toEntity(operationDTO);
                Canal canal = getCanal(operationDTO);

                //init operation
                operationCreationMonnaie.statut(StatutOperation.ENATTENTE_VALIDATION)
                        .beneficaire(null)
                        .canal(canal)
                        .emetteur(null)
                        .beneficaire(null)
                        .typeService(typeService)
                        .pointVente(userConnecte.getPointVente())
                        .montantFrais(BigDecimal.ZERO)
                        .montantFrais(BigDecimal.ZERO)
                        .montantFraisAcq(BigDecimal.ZERO)
                        .dateOperation(ZonedDateTime.now())
                        .etape(Etape.INIT)
                        .uoEmetteur(organisation)
                        .uoAcquereur(organisation)
                        .user(userConnecte)
                        .codeOperation(RandomUtil.getCodeOperation(operationCreationMonnaie.getDateOperation()));
                operationCreationMonnaie = operationRepository.save(operationCreationMonnaie);//Enregistrement d'une operation
                operationAuditService.saveOpAudit(operationCreationMonnaie, null, StatutOperation.PAYE);
                /*  if (!userOrganisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.MASTER))
                        && !organisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.AGENCE))) {
                    Result response = apiService.sendRequestComptabilisationOfApprovisionnementToOmnicanal(operationCreationMonnaie, operationCreationMonnaie.getPointVente().getUniteOrganisation(), organisation);
                    verifResultOfOmnicanal(response, operationCreationMonnaie);
                }*/
                ///  instructionComptableService.comptabiliserOperation(operationCreationMonnaie, userOrganisation, userOrganisation);
                return operationMapper.toDto(operationCreationMonnaie);
            } else {
                throw new CustomParameterizedException("Aucun point de vente trouvÃ©");
            }

            //   }
        }
        throw new CustomParameterizedException("Operation invalide .");
    }

    @Transactional
    public OperationDTO doOperationRegelement(OperationDTO operationDTO, Long uoAcq, Reglement regl, ZonedDateTime dateCommission, User userConnecte, TypeService typeService) throws IOException {
        log.debug("----------Begin---------");

        // User userConnecte = userService.findeByLogin(operationDTO.getLogin());
        //userCanDoOperations(userConnecte);
        Double montantTotalReglement = 0D;
        //reglementService.save(regl);
        System.out.println("udrr---------{ }" + userConnecte);
        UniteOrganisation userOrganisation = userConnecte.getUniteOrganisation();

        //   if (verifAutorisationOfTypeService(typeService)) {
        UniteOrganisation organisation = uniteOrganisationService.findOneById(uoAcq);
        //recuperer le solde du compte EtiquetteCompteCode.CPTECOMACTEURPTI

        Compte compte = compteService.getCompteOfOrganisation(organisation, EtiquetteCompteCode.CPTECOMACTEUR);

        CompteAutre cptautre = new CompteAutre();

        //    List<Commission> listeCommission = commissionRepository.findListCommissionOfUoBeforeDateReglement(compte.getUniteOrganisation().getId(), dateCommission);
        //   List<Long> listeIdCommission = commissionRepository.findListIdCommissionOfUoBeforeDateReglement(compte.getUniteOrganisation().getId(), dateCommission);
        List<CommissionDTO> listeCommission = commissionRepository.findListCommissionOfUoBeforeDateReglementOptimiser(compte.getUniteOrganisation().getId(), dateCommission);
//
        BigDecimal montant = compte.getCompteAutre().getSolde();
        //commissionRepository.getMontantTotalCommission(compte.getUniteOrganisation().getId(), dateCommission);
        if (listeCommission.size() > 0) {
            // if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
            // BigDecimal montant = compte.getCompteAutre().getSolde();
            Operation operationApprovisionnement = new Operation();
            Canal canal = getCanal(operationDTO);
            LigneReglement ligneReglement = new LigneReglement();
            //init operation
            operationApprovisionnement.statut(StatutOperation.ENATTENTE_VALIDATION)
                    .beneficaire(null)
                    .canal(canal)
                    .emetteur(null)
                    .beneficaire(null)
                    .typeService(typeService)
                    .montantOp(montant)
                    .pointVente(userConnecte.getPointVente())
                    .montantFrais(BigDecimal.ZERO)
                    .montantFrais(BigDecimal.ZERO)
                    .montantFraisAcq(BigDecimal.ZERO)
                    .dateOperation(ZonedDateTime.now())
                    .etape(Etape.PROCEED)
                    .uoAcquereur(organisation)
                    .uoBeneficiaire(organisation)
                    .uoEmetteur(userOrganisation)
                    .user(userConnecte)
                    .codeOperation(RandomUtil.getCodeOperation(operationApprovisionnement.getDateOperation(), organisation.getCodeUO()));
            operationApprovisionnement = operationRepository.saveAndFlush(operationApprovisionnement);//Enregistrement d'une operation
            //  operationAuditService.saveOpAudit(operationApprovisionnement, null, StatutOperation.ENATTENTE_VALIDATION);
            //   Compte compteSrc = compteService.getCompteOfOrganisation(organisation, EtiquetteCompteCode.CPTECOMACTEUR);
            Compte compteDest = compteService.getCompteOfOrganisation(organisation, EtiquetteCompteCode.CPTEOPERATIONACTEUR);
            montant = montant.setScale(0, RoundingMode.HALF_UP);
            ligneReglement.setMontantCommission(montant.doubleValue());
            ligneReglement.setSoldeCompteOp(compteDest.getCompteAutre().getSolde().doubleValue());
            ligneReglement.setSoldeCompteComm(compte.getCompteAutre().getSolde().doubleValue());
            ligneReglement.setSoldeCompteOpApres(0D);
            ligneReglement.setOrganisation(organisation);
            ligneReglement.setReglement(regl);
            ligneReglement.setValide(Boolean.FALSE);
            ligneReglement.setOperation(operationApprovisionnement);
            LigneReglement ligneReglementSave = ligneReglementRepository.saveAndFlush(ligneReglement);
            BigDecimal resteApayer = montant;
            BigDecimal montantRestePayer = BigDecimal.ZERO;

            /**
             * **Faire une boucle for ici avec le montant de à revoir avec
             */
            if (ligneReglementSave != null) {

                for (CommissionDTO lc : listeCommission) {

                    if (Util.isBiggerThan(resteApayer, BigDecimal.ZERO)) {
                        if (Util.isBiggerOrEqualThan(resteApayer, lc.getMontant())) {
                            montantRestePayer = BigDecimal.ZERO;
                        } else {
                            montantRestePayer = lc.getMontant().subtract(resteApayer);

                        }
                        commissionRepository.updateReglementCommissionById(ligneReglementSave.getId(), lc.getId(), montantRestePayer);
                        resteApayer = resteApayer.subtract(lc.getMontant());

                    }

                }

            }

            /**
             * **Faire une boucle for ici avec le montant de à revoir avec
             */
            regl.setStatut(false);

            if (regl.getMontantTotal() != null) {
                montantTotalReglement = regl.getMontantTotal();
            }
            regl.setMontantTotal(montantTotalReglement + ligneReglementSave.getMontantCommission());
            reglementService.save(regl);
            /*
            Result response = apiService.virementSimple(compteSrc, compteDest, montant, "REGLEMENT du " + ZonedDateTime.now());
            verifResultOfOmnicanal(response, operationApprovisionnement);
            instructionComptableService.comptabiliserOperation(operationApprovisionnement, organisation, organisation);
             */
            return operationMapper.toDto(operationApprovisionnement);

        }
        return null;

    }

    @Transactional
    public OperationDTO doOperationValideReglement(LigneReglement ligneReglement, User userConnecte) throws IOException, JAXBException {
        String error = "";
        try {
            System.out.println("BEGIN--------------");
            Compte compteSrc = compteService.getCompteOfOrganisation(ligneReglement.getOrganisation(), EtiquetteCompteCode.CPTECOMACTEUR);
            // User userConnecte = userService.getUserConnecteOptimise(login);
            //User userConnecte = userService.findeByLogin(login);
            log.debug("Test si commission sup à zero" + ligneReglement.getMontantCommission());
            if (ligneReglement.getMontantCommission() > 0D) {
                //Operation operationApprovisionnement = ligneReglement.getOperation();
                Operation operationApprovisionnement = operationRepository.findByOperationId(ligneReglement.getOperation().getId());
                System.out.println("Verif Autorisation--------------");
                if (verifIfValidateur(userConnecte, operationApprovisionnement.getTypeService().getId(), operationApprovisionnement.getMontantOp())) {
                    Reglement reglement = ligneReglement.getReglement();
                    log.debug("---------------------------Accès au role");
                    System.out.println("Comptabilisation--------------");
                    Compte compteDest = compteService.getCompteOfOrganisation(ligneReglement.getOrganisation(), EtiquetteCompteCode.CPTEOPERATIONACTEUR);
                    UniteOrganisation organisation = ligneReglement.getOrganisation();
                    BigDecimal montant = new BigDecimal(ligneReglement.getMontantCommission());
                    //* si le montant à débiter est supérieur au solde du compte commission, on recupère le solde du compte commission
                    if (compteSrc.getCompteAutre() != null) {
                        BigDecimal solde = compteSrc.getCompteAutre().getSolde();
                        if ((Util.isBiggerThan(solde, BigDecimal.ZERO)) && (Util.isBiggerThan(montant, solde))) {
                            montant = solde;
                            error = "PAIEMENT PARTIEL MONTANT DE LA COMMISSION " + montant + " SOLDE DISPONIBLE " + solde;
                        }
                    }
                    Result response = apiService.virementSimple(compteSrc, compteDest, montant, "REGLEMENT du " + ZonedDateTime.now(), operationApprovisionnement.getCodeOperation(), operationApprovisionnement);
                    Boolean verif = verifResultOfOmnicanal(response, operationApprovisionnement);
                    Long ifExiste = transactionComptePtiService.findExisteOperation(operationApprovisionnement.getCodeOperation(), compteDest.getCompteAutre().getId());

                    // TransactionComptePti trans = transactionComptePtiService.findByOperationAndCompteAutre(operationApprovisionnement.getCodeOperation(), compteDest.getCompteAutre().getId());
                    //   Compte compteOperation = compteService.getCompteOfOrganisation(ligneReglement.getOrganisation(), EtiquetteCompteCode.CPTEOPERATIONACTEUR);
                    if (verif && ifExiste > 0) {
                        error = error + " REGLEMENT DE LA COMMISSION POUR L'AGENT " + organisation.getCodeUO() + " " + organisation.getLibelleUniteOrganisation();
                        //   instructionComptableService.comptabiliserOperation(operationApprovisionnement, organisation, organisation);
                        //*MAJ des commission à partir de ID LIGNE REGLEMENT*/
                        List<CommissionDTO> listeCommission = commissionRepository.findListCommissionOfLigneReglementOptimise(ligneReglement.getId());
                        if (ligneReglement != null) {
                            listeCommission.forEach(c -> {
                                commissionRepository.updateReglementCommissionEstRegle(c.getId());
                            });
                        }
                        //  System.out.println("MAJ--------------{ }" + listeCommission);
                        ligneReglement.setErreur(error);
                        ligneReglement.setValide(Boolean.TRUE);
                        ligneReglementRepository.saveAndFlush(ligneReglement);
                        operationApprovisionnement.statut(StatutOperation.PAYE);
                        // operationApprovisionnement.setCommentaire(commentaire);
                        operationApprovisionnement = operationRepository.saveAndFlush(operationApprovisionnement);//Enregistrement d'une operation
                        envoiMsgOperation(operationApprovisionnement);
                    } else {
                        error = "ERROR DE PAIEMENT";
                        ligneReglement.setErreur(error);
                        ligneReglement.setValide(Boolean.TRUE);

                    }
                    ligneReglementRepository.saveAndFlush(ligneReglement);
                    //operationAuditService.saveOpAudit(operationApprovisionnement, StatutOperation.ENATTENTE_VALIDATION, StatutOperation.PAYE);
                    Integer statut = ligneReglementRepository.countByReglementIdAndValideIsFalse(reglement.getId());
                    // reglement.setValiderPar(login);
                    //   reglement.setEstAnnuler(false);
                    if (statut > 0) {
                        reglement.setStatut(false);
                    } else {

                        reglement.setStatut(true);
                    }
                    reglementService.save(reglement);
                } else {
                    throw new CustomParameterizedException("Pas de privilège ");
                }
                return operationMapper.toDto(operationApprovisionnement);
            }

        } catch (Exception e) {
            error = error + e.getMessage();
            ligneReglement.setErreur(error);
            ligneReglementRepository.save(ligneReglement);
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        // }
        return null;

    }

    @Transactional
    public OperationDTO doOperationValideNewReglement(LigneReglement ligneReglement, String login) throws IOException, JAXBException {
        User userConnecte = userService.findeByLogin(login);
        log.debug("Test si commission sup à zero" + ligneReglement.getMontantCommission());
        if (ligneReglement.getMontantCommission() > 0) {
            Operation operationApprovisionnement = ligneReglement.getOperation();

            if (verifIfValidateur(userConnecte, operationApprovisionnement.getTypeService().getId(), operationApprovisionnement.getMontantOp())) {
                Reglement reglement = ligneReglement.getReglement();
                log.debug("---------------------------Accès au role");
                Compte compteSrc = compteService.getCompteOfOrganisation(ligneReglement.getOrganisation(), EtiquetteCompteCode.CPTECOMACTEUR);
                Compte compteDest = compteService.getCompteOfOrganisation(ligneReglement.getOrganisation(), EtiquetteCompteCode.CPTEOPERATIONACTEUR);
                BigDecimal montant = new BigDecimal(ligneReglement.getMontantCommission());

                Result response = apiService.virementSimple(compteSrc, compteDest, montant, "REGLEMENT du " + ZonedDateTime.now(), operationApprovisionnement.getCodeOperation(), operationApprovisionnement);
                Boolean verif = verifResultOfOmnicanal(response, operationApprovisionnement);
                //*MAJ des commission à partir de ID LIGNE REGLEMENT*/
                List<Commission> listeCommission = commissionRepository.findListCommissionOfLigneReglement(ligneReglement.getId());

                for (Commission c : listeCommission) {
                    c.setEstRegle(Boolean.TRUE);
                    commissionRepository.save(c);

                }

                operationApprovisionnement.statut(StatutOperation.PAYE);
                // operationApprovisionnement.setCommentaire(commentaire);
                operationApprovisionnement = operationRepository.save(operationApprovisionnement);//Enregistrement d'une operation
                envoiMsgOperation(operationApprovisionnement);
                ligneReglement.setValide(Boolean.TRUE);
                operationAuditService.saveOpAudit(operationApprovisionnement, StatutOperation.ENATTENTE_VALIDATION, StatutOperation.PAYE);

                ligneReglementRepository.save(ligneReglement);
                Integer statut = ligneReglementRepository.countByReglementIdAndValideIsFalse(reglement.getId());
                reglement.setValiderPar(login);
                reglement.setEstAnnuler(false);
                if (statut > 0) {
                    reglement.setStatut(false);
                } else {

                    reglement.setStatut(true);
                }
                reglementService.save(reglement);
            } else {
                throw new CustomParameterizedException("Pas de privilège ");
            }
            return operationMapper.toDto(operationApprovisionnement);
        }
        return null;
    }

    /**
     * Enregistrement d'une operation d'approvisionnement de compte
     * (APPROV_COMPTE) Les Masters approvissionnement uniquement les SOUS
     * DISTRIBUTEURS
     */
    @Transactional
    public OperationDTO doOperationValideNewReglement(List<LigneReglement> ligneReglements, String login) throws IOException, JAXBException {
        if (login != null) {

            for (LigneReglement ligne : ligneReglements) {

                return doOperationValideNewReglement(ligne, login);
            }
        } else {
            throw new CustomParameterizedException(" Echec de l\'opération utilisateur connecter non identifier");
        }
        return null;
    }

    @Transactional
    public OperationDTO doOperationApprovisionnement(OperationDTO operationDTO) throws IOException, JAXBException {
        if (operationDTO != null) {
            log.info(" : Operation d'approvissionnement en cours ...");
            User userConnecte = userService.getUserWithAuthorities();
            userCanDoOperations(userConnecte);
            UniteOrganisation userOrganisation = userConnecte.getUniteOrganisation();
            //Si c'est un master on verifie la disponibilitÃ© du montant necessaire Ã  l'approvisionnement
            TypeService typeService = typeServiceService.findByCodeService(String.valueOf(operationDTO.getCode()));//
            if (verifAutorisationOfTypeService(typeService)) {
                if (userOrganisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.MASTER))
                        || userOrganisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.AGENCE))) {
                    Compte cpt = null;
                    cpt = verifFonds(operationDTO.getMontantOp(), userOrganisation, EtiquetteCompteCode.CPTEOPERATIONACTEURPTI);
                    if (cpt == null) {
                        if (operationRepository.existsByCodeExterne(operationDTO.getCodeExterne()) && userOrganisation.getTypeUO().getLibelleTypeUO().equals("AGENCE")) {
                            throw new CustomParameterizedException("Le numero de versement est introuvable.");
                            //verification de la validitÃ© du bordereau en agence

                        }
                    }
                }

                UniteOrganisation organisation = uniteOrganisationService.findOneById(operationDTO.getUoAcquereurId());
                if (operationDTO.getCode().equals(TypeServiceLibelle.APPROVISIONNEMENT_AVEC_COMM.toString())) {
                    verifFonds(operationDTO.getMontantOp(), organisation, EtiquetteCompteCode.CPTECOMACTEURPTI);
                }
                Operation operationApprovisionnement = operationMapper.toEntity(operationDTO);
                Canal canal = getCanal(operationDTO);
                //init operation
                operationApprovisionnement.statut(StatutOperation.ENATTENTE_VALIDATION)
                        .beneficaire(null)
                        .canal(canal)
                        .emetteur(null)
                        .beneficaire(null)
                        .typeService(typeService)
                        .pointVente(userConnecte.getPointVente())
                        .montantFrais(BigDecimal.ZERO)
                        .montantFrais(BigDecimal.ZERO)
                        .montantFraisAcq(BigDecimal.ZERO)
                        .dateOperation(ZonedDateTime.now())
                        .etape(Etape.PROCEED)
                        .uoEmetteur(userOrganisation)
                        .user(userConnecte)
                        .codeOperation(RandomUtil.getCodeOperation(operationApprovisionnement.getDateOperation()));
                List<RoleValidation> roleValide = rechercherRoleValidateur(typeService.getId(), operationDTO.getMontantOp());

                operationApprovisionnement = operationRepository.save(operationApprovisionnement);//Enregistrement d'une operation
                if (roleValide.isEmpty()) {
                    operationAuditService.saveOpAudit(operationApprovisionnement, null, StatutOperation.PAYE);
                    if (!userOrganisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.MASTER)) && !organisation.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.AGENCE))) {
                        Result response = apiService.sendRequestComptabilisationOfApprovisionnementToOmnicanal(operationApprovisionnement, operationApprovisionnement.getUoEmetteur(), organisation);
                        verifResultOfOmnicanal(response, operationApprovisionnement);
                    }
                    instructionComptableService.comptabiliserOperation(operationApprovisionnement, organisation, userOrganisation);
                }

                return operationMapper.toDto(operationApprovisionnement);
            }
        }
        throw new CustomParameterizedException("Operation invalide .");
    }

    /**
     * Annulation d'une operation de type appel de fond qui a Ã©tÃ© validÃ© et
     * dont le retrait de fonds n'a pas encore Ã©tÃ© effectuÃ©
     *
     * @param operationInitId
     * @return
     * @throws IOException
     */
    @Transactional
    public OperationDTO annulationOperationAppelFond(Long operationInitId) throws IOException, JAXBException {

        Operation operation = operationRepository.findById(operationInitId).orElse(null);
        if (operation.getStatut().equals(StatutOperation.PAYE)) {
            //Comptabilisation pour l'annulation de l'appel de fonds
            annulationOperationEnvoiCash(operation.getId());
            Compte compteDest = operation.getCompteSrc();//Compte source de l'operation initiale
            if (compteDest != null) {
                UniteOrganisation organisationConnecte = uniteOrganisationService.getUoOfUserConnecte();
                Compte compteSrc = compteService.getCompteOfOrganisation(organisationConnecte, EtiquetteCompteCode.CPTEOPERATIONACTEUR);
                String montantOp = String.valueOf(operation.getMontantOp());
                String montantFraisAcq = "0";
                String montantFraisEm = "0";
                String codeFseSrc = uniteOrganisationService.getCodeUoFse(organisationConnecte);
                String codeFseDest = uniteOrganisationService.getCodeUoFse(operation.getUoEmetteur());
                String codeAgenceSrc = compteSrc.getCompteBanque().getCodeAgence();
                String codeAgenceDest = compteDest.getCompteBanque().getCodeAgence();
                String ettiqueSrc = compteSrc.getEtiquette();
                String ettiqueteDest = compteDest.getEtiquette();
                String codeDebit = compteSrc.getValeurCompte() + "#" + compteSrc.getCompteBanque().getCodeAgence() + "#" + compteSrc.getCompteBanque().getCodeBanque();
                String codeCredit = compteDest.getValeurCompte() + "#" + compteDest.getCompteBanque().getCodeAgence() + "#" + compteDest.getCompteBanque().getCodeBanque();
                String codeService = ApiService.CODE_SERVICE_ACCOUNT_ACCOUNT;
                Result result = apiService.sendRequestComptabilisation(montantFraisAcq, montantFraisEm, montantOp, codeFseDest, codeFseSrc, codeAgenceDest, codeAgenceSrc, ettiqueSrc, codeDebit, ettiqueteDest, codeCredit, codeService, operation.getCodeOperation());
                verifResultOfOmnicanal(result, operation);

                TypeService typeService = typeServiceRepository.findTopByCode(String.valueOf(TypeServiceLibelle.ACCOUNT_ACCOUNT));
                UniteOrganisation uoFse = uniteOrganisationService.getUoFse(operation.getUoEmetteur());
                UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(uoFse);
                //Comptabilisation en local
                instructionComptableService.comptabiliserOperation(operation.getMontantOp(), BigDecimal.ZERO, BigDecimal.ZERO, fseDestAgenceSiege, uniteOrganisationService.getUoOfUserConnecte(), typeService);
                changeStatut(operation, StatutOperation.ANNULLE);
                return operationMapper.toDto(operation);
            } else {
                throw new CustomParameterizedException("Compte introuvable.");
            }
        } else {
            throw new CustomParameterizedException("Impossible de mettre Ã  jour cette opereration");
        }

    }

    /**
     * Rechereche d'une operation en fonction du code de l'operation
     *
     * @param codeOperation
     * @return
     */
    public OperationDTO findByCodeOperation(String codeOperation) {
        Optional<Operation> operationExist = operationRepository.findByCodeOperation(codeOperation);
        if (operationExist.isPresent()) {
            return operationMapper.toDto(operationExist.get());
        } else {
            return null;
        }
    }

    /**
     * Reactivation ou bloquage d'une operation
     *
     * @return
     */
    public OperationDTO miseJourOperation(OperationDTO operationDTO) {
        Operation operation = operationMapper.toEntity(operationDTO);
        if (operationDTO.getStatut().equals(StatutOperation.ENATTENTE)) {//Supendre l'operation
            changeStatut(operation, StatutOperation.SUSPENDU);
        } else if (operationDTO.getStatut().equals(StatutOperation.SUSPENDU)) {//Reactive l'operation
            changeStatut(operation, StatutOperation.ENATTENTE);
        } else {
            throw new CustomParameterizedException("Erreur statut");
        }
        return operationDTO;
    }

    /**
     * Mise Ã  jour du statut d'une opÃ©ration
     *
     * @param operation
     * @param statutOperation
     * @return
     */
    public OperationDTO changeStatut(Operation operation, StatutOperation statutOperation) {
        operation.statut(statutOperation);
        operation = operationRepository.save(operation);
        operationAuditService.saveOpAudit(operation, operation.getStatut(), statutOperation);
        return operationMapper.toDto(operation);
    }

    /**
     * Save a operation.
     *
     * @param operationDTO the entity to save
     * @return the persisted entity
     */
    @Transactional
    public OperationDTO save(OperationDTO operationDTO) {
        Operation operation = operationMapper.toEntity(operationDTO);
        operation = operationRepository.save(operation);
        return operationMapper.toDto(operation);
    }

    @Transactional
    public Operation save(Operation operation) {
        operation = operationRepository.save(operation);
        return operation;
    }

    /**
     * Filtrer les opÃ©rations en attente en fonction de plusieurs critÃ¨res
     *
     * @param pageable
     * @param typeService
     * @param canalId
     * @param uoAcquereurId
     * @param montantOp
     * @param statutOperation
     * @param dateOperation
     * @return
     */
    public Page<OperationDTO> findOperationEnAttenteByMultiParams(Pageable pageable, String typeService, Long canalId, Long uoAcquereurId, BigDecimal montantOp, StatutOperation statutOperation, ZonedDateTime dateOperation) {

        UniteOrganisation uoPays;
        if (uoAcquereurId == null) {
            uoPays = returnPays(uniteOrganisationService.getUoOfUserConnecte());
            //  uoPays = uniteOrganisationService.findUoParentByType(uniteOrganisationService.getUoOfUserConnecte(), TypeUO.PAYS);
        } else {
            uoPays = uniteOrganisationService.findOneById(uoAcquereurId);
        }

        List<UniteOrganisation> uoFils = uniteOrganisationRepository.findByPereUOAndSupprimeIsFalse(uoPays);
        if (uoFils != null && !uoFils.isEmpty()) {
            List<Long> uoFilsId = uoFils.stream().map(uo -> uo.getId()).collect(Collectors.toList());
            if (dateOperation != null) {
                if (typeService.equals(TypeServiceLibelle.ACCOUNT_CASH.toString())) {
                    return operationRepository.findOperationMadByMultiParams(uoFilsId, montantOp, dateOperation, dateOperation.plusHours(24), pageable).map(operationMapper::toDto);
                } else {//ACCOUNT_ACCOUNT
                    return operationRepository.findOperationAccountAccountByMultiParams(uoFilsId, montantOp, dateOperation, dateOperation.plusHours(24), pageable).map(operationMapper::toDto);
                }
            } else if (typeService.equals(TypeServiceLibelle.ACCOUNT_CASH.toString())) {
                return operationRepository.findOperationMadByMultiParams(uoFilsId, montantOp, dateOperation, dateOperation, pageable).map(operationMapper::toDto);
            } else {//ACCOUNT_ACCOUNT
                return operationRepository.findOperationAccountAccountByMultiParams(uoFilsId, montantOp, dateOperation, dateOperation, pageable).map(operationMapper::toDto);
            }
        }
        return new PageImpl<>(Collections.emptyList());

    }

    /**
     * Recherche des operations expirÃ©s effectuÃ© dans un point de vente
     *
     * @param pageable
     * @return
     */
    public Page<OperationDTO> findOperationsExpire(Pageable pageable) {
        PointVente pv = pointVenteService.getPointVenteOfUserConnecte();
        return operationRepository.findByPointVenteAndDateExpirationBefore(pv, LocalDate.now(), pageable).map(operationMapper::toDto);
    }

    /**
     * Pour l'annulation on recherche -Le compte operation acteur de
     * l'organisation (CREDITE) -Le compte attente retrait de la filiale de
     * l'organisation (DEBITE) -On change le statut de l'operation (ANNULE)
     *
     * @param operationId
     */
    @Transactional
    public OperationDTO annulationOperationEnvoiCash(Long operationId) throws IOException, JAXBException {
        Operation operation = operationRepository.findById(operationId).orElse(null);
        BigDecimal montantOp = operation.getMontantOp();
        //Appel du service de retrait de l'omnicanal
        Result result = apiService.sendRequestForComptabilisationRetrait(operation, operation.getPointVente().getUniteOrganisation());
        verifResultOfOmnicanal(result, operation);
        TypeService typeService = typeServiceRepository.findTopByCode(String.valueOf(TypeServiceLibelle.RETRAIT));
        instructionComptableService.comptabiliserOperation(montantOp, BigDecimal.ZERO, BigDecimal.ZERO, null, operation.getPointVente().getUniteOrganisation(), typeService);
        operation.statut(StatutOperation.REMBOURSE);
        operationRepository.save(operation);
        return operationMapper.toDto(operation);
    }

    public OperationAllDTO verificationOperationWithoutAction(String code, PointVente pointVente) throws NoSuchAlgorithmException {
        Operation operation = operationRepository.findTopByCodeRetrait(RandomUtil.getMd5Code(code));
        if (operation != null) {
            if (operation.getStatut().equals(StatutOperation.ENCOURS) || operation.getStatut().equals(StatutOperation.ENATTENTE) || operation.getStatut().equals(StatutOperation.ENATTENTE_VALIDATION)) {
                UniteOrganisation uoPointVent = pointVente.getUniteOrganisation();
                if (uoPointVent != null) {//Verification de l'UO acq de l'operation
                    ArrayList<Long> ids = new ArrayList<>();
                    uoPointVent = uniteOrganisationService.findOneById(uoPointVent.getId());
                    uniteOrganisationService.findAllUoParentsIds(uoPointVent, ids);
                    //Verification du lieu de paiement
                    if (!operation.getUoAcquereur().getTypeUO().getCodeTypeUO().equals(String.valueOf(TypeUO.PAYS))
                            && !operation.getUoAcquereur().getTypeUO().getCodeTypeUO().equals(String.valueOf(TypeUO.FSE))) {
                        if (!uoPointVent.getId().equals(operation.getUoAcquereur().getId())) {
                            throw new CustomParameterizedException("L'opÃ©ration est introuvable 1.");
                        }
                    }
                    if (ids.contains(operation.getUoAcquereur().getId())) {
                        OperationAllDTO operationAllDTO = new OperationAllDTO();
                        PersonneAllDTO beneficiaire = personneService.finAllInfoOfPersonne(operation.getBeneficaire().getPersonne().getId());
                        if (operation.getPieceBeneficiaire() != null) {
                            beneficiaire.setPieceDTO(pieceMapper.toDto(operation.getPieceBeneficiaire()));
                        }
                        operationAllDTO.setBeneficiaire(beneficiaire);
                        operationAllDTO.setOperationDTO(operationMapper.toDto(operation));
                        return operationAllDTO;
                    } else {
                        throw new CustomParameterizedException("L'opÃ©ration est introuvable.");
                    }
                } else {
                    throw new CustomParameterizedException("le point de vente de l'utilisateur connecte est invalide .");
                }
            } else {
                throw new CustomParameterizedException("L'opÃ©ration est introuvable.");
            }

        } else {
            throw new CustomParameterizedException("L'opÃ©ration est introuvable.");
        }

    }

    public MRetraitResponse getDetailOperation(String codeOperation) {
        OperationDTO operationDTO = findByCodeOperation(codeOperation);
        MPersonne mbenef = new MPersonne();
        MResponse mresponse = null;
        MRetraitResponse mretrait = new MRetraitResponse();
        if (operationDTO != null) {
            Operation operation = operationRepository.findById(operationDTO.getId()).orElse(null);
            if (operation.getBeneficaire() != null) {
                PersonneAllDTO beneficiaire = personneService.finAllInfoOfPersonne(operation.getBeneficaire().getPersonne().getId());
                if (beneficiaire != null) {

                    PersonnePhysiqueDTO benefPhysique = beneficiaire.getPersonnePhysiqueDTO();
                    mbenef.setTelephone(beneficiaire.getPersonneDTO().getTelephone());
                    mbenef.setNom(benefPhysique.getNomClient());
                    mbenef.setPrenom(benefPhysique.getPrenomClient());
                    mresponse = new MResponse("0", "opÃ©ration possible");
                    mretrait.setMontant(operation.getMontantOp());
                    mretrait.setMpersonne(mbenef);
                    mretrait.setReponse(mresponse);
                }
            }

        } else {
            mresponse = new MResponse("-1", "Erreur");
        }
        return mretrait;
    }

    public OperationAllDTO verificationOperationMobile(String code, PointVente pointVente) throws NoSuchAlgorithmException {

        Operation operation = operationRepository.findTopByCodeRetrait(RandomUtil.getMd5Code(code));
        log.debug("Code retrait %%" + RandomUtil.getMd5Code(code));
        if (operation != null) {
            if (RandomUtil.verifyOtp(operation.getSecret(), code)) {//Verification de la dateExpiration
                if (operation.getStatut().equals(StatutOperation.ENATTENTE) || operation.getStatut().equals(StatutOperation.ENATTENTE_VALIDATION)) {
                    log.debug("STATUT%%" + operation.getStatut().toString());
                    UniteOrganisation uoPointVent = pointVente.getUniteOrganisation();

                    uoPointVent = uniteOrganisationService.findOneById(uoPointVent.getId());
                    if (uoPointVent != null) {//Verification de l'UO acq de l'operation
                        ArrayList<Long> ids = new ArrayList<>();
                        uniteOrganisationService.findAllUoParentsIds(uoPointVent, ids);
                        //Verification du lieu de paiement
                        if (!operation.getUoAcquereur().getTypeUO().getCodeTypeUO().equals(String.valueOf(TypeUO.PAYS))
                                && !operation.getUoAcquereur().getTypeUO().getCodeTypeUO().equals(String.valueOf(TypeUO.FSE))) {
                            if (!uoPointVent.getId().equals(operation.getUoAcquereur().getId())) {
                                throw new CustomParameterizedException("L'opération est introuvable 1.");
                            }
                        }

                        if (ids.contains(operation.getUoAcquereur().getId())) {
                            operation.setStatut(StatutOperation.ENCOURS);
                            OperationAllDTO operationAllDTO = new OperationAllDTO();
                            operation = operationRepository.save(operation);
                            operationAuditService.saveOpAudit(operation, StatutOperation.ENATTENTE, StatutOperation.ENCOURS);
                            PersonneAllDTO beneficiaire = personneService.finAllInfoOfPersonne(operation.getBeneficaire().getPersonne().getId());
                            if (operation.getPieceBeneficiaire() != null) {
                                beneficiaire.setPieceDTO(pieceMapper.toDto(operation.getPieceBeneficiaire()));
                            }
                            operationAllDTO.setBeneficiaire(beneficiaire);
                            operationAllDTO.setOperationDTO(operationMapper.toDto(operation));
                            return operationAllDTO;
                        } else {
                            throw new CustomParameterizedException("L'opération est introuvable.");
                        }
                    } else {
                        throw new CustomParameterizedException("le point de vente de l'utilisateur connecte est invalide .");
                    }
                } else {
                    if (operation.getStatut().equals(StatutOperation.ENCOURS)) {
                        throw new CustomParameterizedException("L'opération est en cours de traitement.");
                    }

                    if (operation.getStatut().equals(StatutOperation.PAYE)) {
                        throw new CustomParameterizedException("Le retrait a été effectuée.");
                    }

                    if (operation.getStatut().equals(StatutOperation.ANNULLE)) {
                        throw new CustomParameterizedException("Opération est actuellement annulÃ©e .");
                    }

                    if (operation.getStatut().equals(StatutOperation.SUSPENDU)) {
                        throw new CustomParameterizedException("L'Opération est actuellement suspendue .");
                    }
                }
            } else {
                throw new CustomParameterizedException("Le code de l'opération est  expirer .");
            }
        } else {
            throw new CustomParameterizedException("L'opération est introuvable.");
        }
        return null;
    }

    /**
     * Verification de l'existence d'une operation dans le cadre d'un retrait
     * <p>
     * l'UO acquereur doit correspondre Le code doit est Ãªtre valide
     * (_VERIFICATION_OPERATION_RETRAIT)
     */
    public OperationAllDTO verificationOperation(String code) throws NoSuchAlgorithmException {
        Operation operation = operationRepository.findTopByCodeRetrait(RandomUtil.getMd5Code(code));
        if (operation != null) {
            if (RandomUtil.verifyOtp(operation.getSecret(), code)) {//Verification de la dateExpiration
                if (operation.getStatut().equals(StatutOperation.ENATTENTE)) {

                    UniteOrganisation uoPointVent = uniteOrganisationService.getUoOfUserConnecte();
                    if (uoPointVent != null) {//Verification de l'UO acq de l'operation
                        ArrayList<Long> ids = new ArrayList<>();
                        uniteOrganisationService.findAllUoParentsIds(uoPointVent, ids);
                        //Verification du lieu de paiement
                        if (!operation.getUoAcquereur().getTypeUO().getCodeTypeUO().equals(String.valueOf(TypeUO.PAYS))
                                && !operation.getUoAcquereur().getTypeUO().getCodeTypeUO().equals(String.valueOf(TypeUO.FSE))) {
                            if (uoPointVent.getId() != operation.getUoAcquereur().getId()) {
                                throw new CustomParameterizedException("L'opÃ©ration est introuvable 1.");
                            }
                        }

                        if (ids.contains(operation.getUoAcquereur().getId())) {
                            operation.setStatut(StatutOperation.ENCOURS);
                            OperationAllDTO operationAllDTO = new OperationAllDTO();
                            operation = operationRepository.save(operation);
                            operationAuditService.saveOpAudit(operation, StatutOperation.ENATTENTE, StatutOperation.ENCOURS);
                            PersonneAllDTO beneficiaire = personneService.finAllInfoOfPersonne(operation.getBeneficaire().getPersonne().getId());
                            if (operation.getPieceBeneficiaire() != null) {
                                beneficiaire.setPieceDTO(pieceMapper.toDto(operation.getPieceBeneficiaire()));
                            }
                            operationAllDTO.setBeneficiaire(beneficiaire);
                            operationAllDTO.setOperationDTO(operationMapper.toDto(operation));
                            return operationAllDTO;
                        } else {
                            throw new CustomParameterizedException("L'opÃ©ration est introuvable.");
                        }
                    } else {
                        throw new CustomParameterizedException("le point de vente de l'utilisateur connecte est invalide .");
                    }
                } else {
                    if (operation.getStatut().equals(StatutOperation.ENCOURS)) {
                        throw new CustomParameterizedException("L'opÃ©ration est en cours de traitement.");
                    }

                    if (operation.getStatut().equals(StatutOperation.PAYE)) {
                        throw new CustomParameterizedException("Le retrait a Ã©tÃ© effectuÃ©e.");
                    }

                    if (operation.getStatut().equals(StatutOperation.ANNULLE)) {
                        throw new CustomParameterizedException("OpÃ©ration est actuellement annulÃ©e .");
                    }

                    if (operation.getStatut().equals(StatutOperation.SUSPENDU)) {
                        throw new CustomParameterizedException("L'OpÃ©ration est actuellement suspendue .");
                    }
                }
            } else {
                throw new CustomParameterizedException("Le code de l'opÃ©ration Ã  expirer .");
            }
        } else {
            throw new CustomParameterizedException("L'opÃ©ration est introuvable.");
        }
        return null;
    }

    public OperationAllDTO verificationOperation(String code, PointVente pointVente) throws NoSuchAlgorithmException {
        Operation operation = operationRepository.findTopByCodeRetrait(RandomUtil.getMd5Code(code));
        if (operation != null) {
            if (RandomUtil.verifyOtp(operation.getSecret(), code)) {//Verification de la dateExpiration
                if (operation.getStatut().equals(StatutOperation.ENATTENTE)) {
                    UniteOrganisation uoPointVent = pointVente.getUniteOrganisation();
                    if (uoPointVent != null) {//Verification de l'UO acq de l'operation
                        ArrayList<Long> ids = new ArrayList<>();
                        ids.add(uoPointVent.getId());
                        uniteOrganisationService.findAllUoParentsIds(uoPointVent, ids);
                        log.debug("ORG ACQ %%" + operation.getUoAcquereur().getLibelleUniteOrganisation() + "   " + uoPointVent.getLibelleUniteOrganisation());
                        if (pointVente.getId() == operation.getPointVente().getId()) {
                            operation.setStatut(StatutOperation.ENCOURS);
                            OperationAllDTO operationAllDTO = new OperationAllDTO();
                            operation = operationRepository.save(operation);
                            operationAuditService.saveOpAudit(operation, StatutOperation.ENATTENTE, StatutOperation.ENCOURS);
                            PersonneAllDTO beneficiaire = personneService.finAllInfoOfPersonne(operation.getBeneficaire().getPersonne().getId());
                            if (operation.getPieceBeneficiaire() != null) {
                                beneficiaire.setPieceDTO(pieceMapper.toDto(operation.getPieceBeneficiaire()));
                            }
                            operationAllDTO.setBeneficiaire(beneficiaire);
                            operationAllDTO.setOperationDTO(operationMapper.toDto(operation));
                            return operationAllDTO;
                        } else {
                            throw new CustomParameterizedException("L'opÃ©ration est introuvable 2.");
                        }
                    } else {
                        throw new CustomParameterizedException("le point de vente de l'utilisateur connecte est invalide .");
                    }
                } else {
                    if (operation.getStatut().equals(StatutOperation.ENCOURS)) {
                        throw new CustomParameterizedException("L'opÃ©ration est en cours de traitement.");
                    }

                    if (operation.getStatut().equals(StatutOperation.PAYE)) {
                        throw new CustomParameterizedException("Le retrait a Ã©tÃ© effectuÃ©e.");
                    }

                    if (operation.getStatut().equals(StatutOperation.ANNULLE)) {
                        throw new CustomParameterizedException("OpÃ©ration est actuellement annulÃ©e .");
                    }

                    if (operation.getStatut().equals(StatutOperation.SUSPENDU)) {
                        throw new CustomParameterizedException("L'OpÃ©ration est actuellement suspendue .");
                    }
                }
            } else {
                throw new CustomParameterizedException("Le code de l'opÃ©ration Ã  expirer .");
            }
        } else {
            throw new CustomParameterizedException("L'opÃ©ration est introuvable.");
        }
        return null;
    }

    /**
     * Verification d'une operation expire (_VERIFICATION_OP_EXPIRE)
     *
     * @param code
     * @return
     * @throws NoSuchAlgorithmException
     */
    public OperationDTO verificationOperationExpire(String code) throws NoSuchAlgorithmException {
        Operation operation = operationRepository.findTopByCodeRetrait(RandomUtil.getMd5Code(code));
        if (operation != null) {
            if (operation.getStatut().equals(StatutOperation.ENATTENTE)) {
                if (operation.getDateExpiration().isBefore(LocalDate.now())) {
                    return operationMapper.toDto(operation);
                } else {
                    throw new CustomParameterizedException("L'operation n'est pas expirÃ© .");
                }
            } else {
                throw new CustomParameterizedException("L'operation est dÃ©jÃ  traitÃ©e .");
            }
        } else {
            throw new CustomParameterizedException("L'operation est introuvable .");
        }
    }

    /**
     * * Calcul de frais d'une operation -Est ce que des frais sont applicable
     * pour @typeService -Recuperation des lignes frais en fonction des
     * paramÃ¨tres (_CALCUL_FRAIS)
     *
     * @param canalLibelle
     * @param typeServiceLibelle
     * @param uoAcqId
     * @param montant
     * @return
     */
    public LigneFraisDTO calculFrais(String canalLibelle, TypeServiceLibelle typeServiceLibelle,
            Long uoAcqId, Double montant, Long uoEmId,
            PointVente pointVente, Optional<Personne> pers, Long seuil) {

        Client client;
        Long personneId = null;
        ClientDTO cli;

        log.info("Calcul des frais de l'operation  en cours ...");
        Canal canal = canalService.findByCodeCanal(canalLibelle);

        if (canal == null) {
            throw new CustomParameterizedException("Le canal utilisÃ© n'est pas valide");
        }
        List<TableauFraisDTO> listFrais = new ArrayList<>();
        LigneFraisDTO ligneFraisDTO = new LigneFraisDTO();
        if (pointVente == null) {
            User userConnecte = userService.getUserWithAuthorities();
            pointVente = userConnecte.getPointVente();
        }

        if (pointVente != null) {
            // UniteOrganisation uniteOrganisationSource = pointVente.getUniteOrganisation();
            UniteOrganisation uniteOrganisationSource = uniteOrganisationService.findOneById(pointVente.getUniteOrganisation().getId());

            ProfilActeur profilClient;
            ProfilActeur profilActeur;
            ProfilActeur profilAgent = null;
            if (pers.isPresent()) {
                log.info("RECHERCHE PROFIL CLIENT");
                cli = clientRepository.findClientPersonne(pers.get().getId());
                client = clientMapper.toEntity(cli);
                personneId = pers.get().getId();
                if (client != null) {
                    profilActeur = client.getProfilActeur();
                    log.info("PROFIL CLIENT TROUVER {}", profilActeur);
                } else {
                    profilAgent = uniteOrganisationSource.getProfilActeur();
                    profilActeur = profilAgent;
                    log.info("PROFIL AGENT TROUVER {}", profilActeur);
                    if (profilAgent == null) {
                        throw new CustomParameterizedException("Profil de l'organisation introuvable. Contactez votre administrateur pour l'activer.");
                    }
                }

            } else {
                log.info("RECHERCHE PROFIL AGENT PERSONNE NOT EXISTE");
                profilAgent = uniteOrganisationSource.getProfilActeur();
                profilActeur = profilAgent;
                if (profilAgent == null) {
                    throw new CustomParameterizedException("Profil de l'organisation introuvable. Contactez votre administrateur pour l'activer.");
                }
            }

            UniteOrganisation uoEm;//Fse source
            UniteOrganisation uoAcq;//Fse source
            if (uoEmId != null) {
                uoEm = uniteOrganisationService.findOneById(uoEmId);
            } else {
                uoEm = uniteOrganisationSource;
            }

            if (uoAcqId != null) {
                uoAcq = uniteOrganisationService.findOneById(uoAcqId);
                //  uoAcq = uniteOrganisationRepository.findById(uoAcqId).orElse(null);
            } else {
                uoAcq = pointVente.getUniteOrganisation();
                uoAcq = uniteOrganisationService.findOneById(uoAcq.getId());
            }
            //Verification Ã  travers le tableau de cumul
            TypeService typeService = typeServiceService.findService(String.valueOf(typeServiceLibelle));
            //    TypeService typeService = typeServiceRepository.findTopByCode(String.valueOf(typeServiceLibelle));
            //    UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(uoEm, TypeUO.PAYS);
            UniteOrganisation uoPays = returnPays(uoEm);//uniteOrganisationService.findOneById(uoEm.getUoPaysId());
            List<Action> actions = null;
            if (profilAgent != null) {
                actions = verifSeuil(typeService.getId(), canal.getId(), null, null, uoPays.getId(), uoAcqId, TypeRegle.ACTEUR, BigDecimal.valueOf(montant), profilAgent.getId());
                if (actions != null && !actions.isEmpty()) {
                    if (actions.get(0).equals(Action.BLOQUE)) {
                        throw new CustomParameterizedException("Vous ne pouvez pas envoyer " + montant + ".");
                    } else {
                        ligneFraisDTO.setAction(actions.get(0));
                    }
                }
            }
            log.info("PROFIL ACTEUR {}", profilActeur);
            log.info("TYPESERVICE {}", typeService);
            log.info("CANAL {}", canal);
            log.info("UOEMT {}", uoEm);
            log.info("UOACQ {}", uoAcqId);

            UniteOrganisation acqPaysDest;
            //Verification Ã  travers la table de repartition de frais
            if (uoEm != null && typeService != null && canal != null) {
                List<RegleCommission> regleCommissions = new ArrayList<>();
                UniteOrganisation uoPaysSrc = returnPays(uniteOrganisationSource); //uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());
                //    UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.PAYS);
                //uniteOrganisationSource =  uniteOrganisationService.findOneById(uniteOrganisationSource.getId());
                UniteOrganisation cashAccountPaysDest;
                TypeUniteOrganisation typeUniteOrganisation = uniteOrganisationSource.getTypeUO();

                switch (typeServiceLibelle) {
                    case ENVOI_CASH:
                        UniteOrganisation uoPaysDest = returnPays(uoAcq);// uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        //   UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//type Pays
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), uoPaysDest.getId(), null, null, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case CARD_TO_WALLET:
                    case WALLET_TO_CARD:
                        acqPaysDest = returnPays(uoAcq);// uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        //  acqPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), acqPaysDest.getId(), uoEmId, uoAcqId, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case ACCOUNT_ACCOUNT:
                        acqPaysDest = returnPays(uoAcq);// uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        //  acqPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), acqPaysDest.getId(), uoEmId, uoAcqId, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case PAIEMENT_FACTURE_DEPUIS_CASH:
                        acqPaysDest = returnPays(uoAcq);// uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        //  acqPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), acqPaysDest.getId(), uoEmId, uoAcqId, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case PAIEMENT_FACTURE:
                        acqPaysDest = returnPays(uoAcq);// uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        //  acqPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), acqPaysDest.getId(), uoEmId, uoAcqId, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case CASH_ACCOUNT:

                        cashAccountPaysDest = returnPays(uoAcq);//= uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        //  UniteOrganisation cashAccountPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), cashAccountPaysDest.getId(), null, uoAcqId, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case CASH_ACCOUNT_AGENT:
                        cashAccountPaysDest = returnPays(uoAcq);//= uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        //  UniteOrganisation cashAccountPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), cashAccountPaysDest.getId(), uoEmId, uoEmId, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case ACCOUNT_CASH:
                        UniteOrganisation appelFondPaysDest = returnPays(uoAcq); // uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), appelFondPaysDest.getId(), uoAcqId, null, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case TRANSFERT_MASSE_CASH:
                        UniteOrganisation transFondPaysDest = returnPays(uoAcq);// uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        //  UniteOrganisation transFondPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), transFondPaysDest.getId(), uoAcqId, null, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case RETRAIT_COMPTE:
                        acqPaysDest = returnPays(uoAcq);// uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        //  UniteOrganisation transFondPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        //  acqPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), acqPaysDest.getId(), uoEmId, uoAcqId, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case PAIEMENT_BIEN:
                        acqPaysDest = returnPays(uoAcq); //uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        //    acqPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), acqPaysDest.getId(), uoEmId, uoAcqId, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                }
                if (!Util.isNullOrFalse(typeService.isFraisApplicable())) {
                    log.info("TyPeService----", typeService.getId());
                    log.info("TyPeService Frais Appliquer----", typeService.isFraisApplicable());
                    if (!typeService.getCode().equals(String.valueOf(TypeServiceLibelle.ACCOUNT_CASH))) {//ACCOUNT_ACCOUNT, CASH_ACCOUNT
                        listFrais = tableauFraisService.deduireFrais(typeService.getId(), canal.getId(), null, null, uoEm.getId(), uoAcqId, profilActeur.getId());
                    } else { //Pour l ACCOUNT_CASH l'uoAcq est l'uo de l'utilisateur connecte
                        listFrais = tableauFraisService.deduireFrais(typeService.getId(), canal.getId(), null, null, uoEm.getId(), uoAcqId, profilActeur.getId());
                    }

                    if (listFrais != null && !listFrais.isEmpty()) {
                        //On utilise le frais ayant le plus grand poids
                        log.info("LISTE DES FRAIS {}", listFrais);
                        String sourceCalcul = listFrais.get(0).getSourceCalcul() == null ? SourceCalcul.MONTANT.name() : listFrais.get(0).getSourceCalcul();
                        if (SourceCalcul.CUMUL_JOURNALIER.name().equals(sourceCalcul)) {
                            BigDecimal montantBilan = bilanOperationService.findTotalBilanJour(typeService.getId(), personneId);
                            Double montantOp = montant;
                            if (Util.isBiggerThan(montantBilan, BigDecimal.ZERO)) {
                                montant = (montantOp + montantBilan.doubleValue()) - seuil;
                                System.out.println("MONTANT DE L'OPERATION" + (montantOp + montantBilan.doubleValue()));
                            } else {
                                montant = montantOp - seuil;
                            }
                        }
                        LigneFrais ligneFrais = ligneFraisRepository.findByTableauFraisIdAndMinimunLessThanEqualAndMaximunGreaterThanEqual(listFrais.get(0).getId(), montant, montant);
                        if (ligneFrais != null) {
                            ligneFraisDTO = ligneFraisMapper.toDto(ligneFrais);
                            log.info("LIGNE DES FRAIS {}", ligneFraisDTO);
                            if (actions != null && !actions.isEmpty()) {
                                ligneFraisDTO.setAction(actions.get(0));
                            }
                            if (regleCommissions.size() > 0) {
                                ligneFraisDTO.setMessage("Les frais ne sont pas Ã  la charge du client.");
                            }
                            ligneFraisDTO.setBaseCalcul(montant);
                            return ligneFraisDTO;
                        }
                    }
                }

            } else {
                throw new CustomParameterizedException("uo ou typeService ou canal introuvable .");
            }
        } else {
            throw new CustomParameterizedException("Vous n'Ãªtes pas chargÃ© de la gestion d'un point de vente ! ");
        }
        return ligneFraisDTO;
    }

    /**
     * @param canalLibelle
     * @param typeServiceLibelle
     * @param uoAcqId
     * @param montant
     * @return
     */
    public Operation calculFraisSimulation(String canalLibelle, TypeServiceLibelle typeServiceLibelle, Long uoAcqId, Integer montant, Long uoEmId) {
        User userConnecte = userService.getUserWithAuthorities();
        List<TableauFraisDTO> listFrais = new ArrayList<>();
        PointVente pointVente = userConnecte.getPointVente();
        Operation operation = new Operation();
        UniteOrganisation uniteOrganisationSource = pointVente.getUniteOrganisation();
        ProfilActeur profilActeur = uniteOrganisationSource.getProfilActeur();

        if (pointVente != null && profilActeur != null) {

            UniteOrganisation uoEm;//Fse source
            UniteOrganisation uoAcq;//Fse source
            if (uoEmId != null) {
                uoEm = uniteOrganisationService.findOneById(uoEmId);
            } else {
                uoEm = uniteOrganisationSource;
            }

            if (uoAcqId != null) {
                uoAcq = uniteOrganisationService.findOneById(uoAcqId);
            } else {
                uoAcq = pointVente.getUniteOrganisation();
            }
            Canal canal = canalRepository.findByLibelleCanal(canalLibelle);
            TypeService typeService = typeServiceRepository.findTopByCode(String.valueOf(typeServiceLibelle));
            if (uoEm != null && typeService != null && canal != null) {
                List<RegleCommission> regleCommissions = new ArrayList<>();
                TypeUniteOrganisation typeUniteOrganisation = uniteOrganisationSource.getTypeUO();
                UniteOrganisation uoPaysSrc = returnPays(uniteOrganisationSource);  //uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());
                //   UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.PAYS);
                switch (typeServiceLibelle) {
                    case ENVOI_CASH:
                        UniteOrganisation uoPaysDest = returnPays(uoAcq); //uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        //  UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//type Pays
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), uoPaysDest.getId(), null, null, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case ACCOUNT_ACCOUNT:
                        UniteOrganisation acqPaysDest = returnPays(uoAcq);//uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        // UniteOrganisation acqPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), acqPaysDest.getId(), uoEmId, uoAcqId, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case CASH_ACCOUNT:
                        // UniteOrganisation cashAccountPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        UniteOrganisation cashAccountPaysDest = returnPays(uoAcq);//uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), cashAccountPaysDest.getId(), null, uoAcqId, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                    case ACCOUNT_CASH:

                        UniteOrganisation appelFondPaysDest = returnPays(uoAcq); //uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                        //  UniteOrganisation appelFondPaysDest = uniteOrganisationService.findUoParentByType(uoAcq, TypeUO.PAYS);//Pays acq
                        regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), appelFondPaysDest.getId(), uoAcqId, null, typeService.getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
                        break;
                }
                if (!Util.isNullOrFalse(typeService.isFraisApplicable())) {
                    if (!typeService.getCode().equals(String.valueOf(TypeServiceLibelle.ACCOUNT_CASH))) {
                        listFrais = tableauFraisService.deduireFrais(typeService.getId(), canal.getId(), null, null, uoEm.getId(), uoAcqId, profilActeur.getId());
                    } else { //Pour l ACCOUNT_CASH l'uoAcq est l'uo de l'utilisateur connecte
                        listFrais = tableauFraisService.deduireFrais(typeService.getId(), canal.getId(), null, null, uoAcqId, uoEm.getId(), profilActeur.getId());
                    }

                    if (listFrais != null && !listFrais.isEmpty()) {
                        //On utilise le frais ayant le plus grand poids
                        List<LigneFrais> allLigneFrais = ligneFraisRepository.findByTableauFraisId(listFrais.get(0).getId());
                        if (allLigneFrais != null && !allLigneFrais.isEmpty()) {
                            for (LigneFrais l : allLigneFrais) {
                                Double value = getFrais(l, montant);
                                Double m1 = montant - value;
                                if (m1 >= l.getMinimun() && m1 <= l.getMaximun()) {
                                    operation.setMontantOp(BigDecimal.valueOf(m1));
                                    operation.setMontantFrais(BigDecimal.valueOf(value));
                                    if (regleCommissions.size() > 0) {
                                        operation.setCommentaire("Les frais ne sont pas Ã  la charge du client.");
                                    }
                                    return operation;
                                }
                            }
                        }
                    }
                }
            } else {
                throw new CustomParameterizedException("UO ou typeService ou canal introuvable .");
            }
        } else {
            throw new CustomParameterizedException("Vous n'Ãªtes pas chargÃ© de la gestion d'un point de vente ! ");
        }
        return null;
    }

    /**
     * @param l
     * @param montant
     * @return
     */
    private Double getFrais(LigneFrais l, Integer montant) {
        switch (l.getTypeFrais()) {
            case FIXE:
                return l.getValeur();
            case POURCENTAGE:
                return (l.getValeur() * montant) / 100;
        }
        return 0.0;
    }

    public OperationDTO comptabilisationServiceExterieur(Operation operation, Compte compteSrc, Compte compteDest, BigDecimal montantOp, String reference, String description, String donneurOrdre, String beneficiaire) throws IOException, NoSuchAlgorithmException, JAXBException {
        Result response = apiService.virementSimpleEx(compteSrc, compteDest, montantOp, reference, description, donneurOrdre, beneficiaire, operation);
        verifResultOfOmnicanal(response, operation);

        return operationMapper.toDto(operation);

    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    public OperationDTO comptablisationApprovisionnement(@NotNull Long operationId) throws IOException, NoSuchAlgorithmException, JAXBException {
        Operation operation = operationRepository.findById(operationId).orElse(null);

        operation.statut(StatutOperation.PAYE);
        UniteOrganisation uniteOrganisationSrc = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());

        //   UniteOrganisation uniteOrganisationSrc = operation.getPointVente().getUniteOrganisation();
        User userConnecte = userService.getUserWithAuthorities();
        // UniteOrganisation userOrganisation = userConnecte.getUniteOrganisation();
        //    UniteOrganisation uniteOrganisationSrc = uniteOrganisationRepository.findById(operation.getUoEmetteur().getId()).orElse(null);
        UniteOrganisation uniteOrganisationDest = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());

        // if (!uniteOrganisationSrc.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.MASTER)) && !uniteOrganisationDest.getTypeUO().getLibelleTypeUO().equals(String.valueOf(TypeUO.AGENCE))) {
        Result response = apiService.sendRequestComptabilisationOfApprovisionnementToOmnicanal(operation, uniteOrganisationSrc, uniteOrganisationDest);
        verifResultOfOmnicanal(response, operation);
        //}
        repartitionCommission(operation, uniteOrganisationSrc, MontantsAComptabiliser.FRAISEMT);
        repartitionCommission(operation, uniteOrganisationDest, MontantsAComptabiliser.FRAISACQ);
        instructionComptableService.comptabiliserOperation(operation, uniteOrganisationDest, uniteOrganisationSrc);
        Operation ope = operationRepository.save(operation);
        operationAuditService.saveOpAudit(ope, null, StatutOperation.PAYE);
        return operationMapper.toDto(ope);

    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    public OperationDTO comptabilisationMobileEnvoiCash(@NotNull Long operationId) throws IOException, NoSuchAlgorithmException, JAXBException {
        Operation operation = operationRepository.findById(operationId).orElse(null);
        if (operation.getStatut().equals(StatutOperation.ENATTENTE)) {
            UniteOrganisation uoPaysDest;
            UniteOrganisation uniteOrganisationSource = operation.getPointVente().getUniteOrganisation();
            uniteOrganisationSource = uniteOrganisationService.findById(uniteOrganisationSource.getId());
            UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.FSE);
            UniteOrganisation uniteOrganisationDest = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());
            if (uniteOrganisationDest.getTypeUO().getLibelleTypeUO().equals(String.valueOf("PAYS"))) {
                uoPaysDest = uniteOrganisationDest;
            } else {
                uoPaysDest = uniteOrganisationService.findUoParentByType(uniteOrganisationDest, TypeUO.FSE);
            }

            UniteOrganisation fseDest = uniteOrganisationService.getUoFseFilialeWithUoPays(uoPaysDest);//uo agence,master,sous distributeur
            UniteOrganisation fseSrc = uniteOrganisationService.getUoFseFilialeWithUoPays(uoPaysSrc);
            UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(fseDest);

            Result response = apiService.sendRequestForComptabilisationEnvoiCash(uoPaysSrc, uniteOrganisationDest, operation, fseSrc, fseDest, fseDestAgenceSiege, Boolean.TRUE);
            verifResultOfOmnicanal(response, operation);
            TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationService.findByOne(uniteOrganisationSource.getTypeUO().getId());
            List<RegleCommission> regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), uniteOrganisationDest.getId(), null, null, operation.getTypeService().getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));

            Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
            RegleCommission regleCommission;
            if (regleCommissionExist.isPresent()) {
                regleCommission = regleCommissionExist.get();
            } else {
                regleCommission = null;
            }
            // instructionComptableService.comptabiliserOperationWithRegle(operation, fseDestAgenceSiege, uniteOrganisationSource, regleCommission);

            repartitionCommission(operation, uniteOrganisationSource, MontantsAComptabiliser.FRAISEMT);

            sendNewCodeRetrait(operationMapper.toDto(operation));
            bilanOperationService.saveCumulJournalierActeur(uniteOrganisationSource.getId(), operation.getTypeService().getId(), operation.getCanal().getId(), null, null, uoPaysSrc.getId(), operation.getUoAcquereur().getId(),
                    operation.getMontantOp());
            bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), operation.getBeneficaire().getId(), operation.getEmetteur().getId(), uoPaysSrc.getId(), operation);

            operation = operationRepository.save(operation);
            return operationMapper.toDto(operation);
        } else {
            operation.statut(StatutOperation.ANNULLE)
                    .commentaire("");
            operation.dateTraitement(ZonedDateTime.now());
            operationRepository.save(operation);
            return operationMapper.toDto(operation);
        }
    }

    @Transactional
    public OperationDTO comptablisationCreationMonnaie(@NotNull Long operationId, StatutOperation statutOperation, String commentaire) throws IOException, NoSuchAlgorithmException, JAXBException {
        Operation operation = operationRepository.findById(operationId).orElse(null);
        boolean sens; // designe destruction ou création
        if (operation.getStatut().equals(StatutOperation.ENATTENTE_VALIDATION) && statutOperation.equals(StatutOperation.VALIDE)) {
            operation.statut(StatutOperation.PAYE);
            User userConnecte = userService.getUserWithAuthorities();
            UniteOrganisation userOrganisation = userConnecte.getUniteOrganisation();

            /*vÃ©rifier si le compte de distribution Ã  un solde */
            if (operation.getTypeService().getLibelle().equals((TypeServiceLibelle.DESTRUCTION_MONNAIE.toString()))) {
                verifFonds(operation.getMontantOp(), userOrganisation, EtiquetteCompteCode.CPTDISTRIBUTEUR);
                sens = false;
            } else {
                sens = true;
            }
            instructionComptableService.comptabiliserOperation(operation, userOrganisation, userOrganisation);
            UniteOrganisation uo = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
            UniteOrganisation uoAgenceSiege = uniteOrganisationService.getFseAgenceSiege(uo);
            if (uoAgenceSiege != null) {
                UniteOrganisation uoFse = uniteOrganisationService.findOneById(uoAgenceSiege.getUoFseId());
                ParametreApp paramcodeFse = parametreAppRepository.findByTypeAndNomAndCodeFse("BANQUE_FSE", uoFse.getCodeFse());
                if (paramcodeFse != null) {
                    Result response = apiService.sendRequestComptabilisationDestruction(operation, sens);
                    verifResultOfOmnicanal(response, operation);
                }
            }

            operation.setCommentaire(commentaire);
            operation = operationRepository.save(operation);
            operationAuditService.saveOpAudit(operation, null, StatutOperation.PAYE);
            return operationMapper.toDto(operation);
        } else {
            operation.statut(StatutOperation.ANNULLE)
                    .commentaire(commentaire);
            operation.dateTraitement(ZonedDateTime.now());
            operationAuditService.saveOpAudit(operation, null, StatutOperation.ANNULLE);
            operationRepository.save(operation);
            if (operation.getTypeService().equals(TypeServiceLibelle.DESTRUCTION_MONNAIE.toString())) {
                this.recepteurAlarmeService.sendAlarme("DETRUCTION DE MONNAIE " + operation.getMontantOp() + " CODE  " + operation.getCodeOperation(), "DESTRUCTION_MONNAIE");
            } else {
                this.recepteurAlarmeService.sendAlarme("CREATION DE MONNAIE  " + operation.getMontantOp() + " CODE  " + operation.getCodeOperation(), "CREATION_MONNAIE");
            }

            return operationMapper.toDto(operation);
        }
    }

    /**
     * Validation d'une operation d'envoi de cash On effectue la
     * comptabilisation sur PTI et sur l'omnicanal
     *
     * @return
     */
    @Transactional
    public OperationDTO comptabilisationEnvoiCash(@NotNull Long operationId, StatutOperation statutOperation, String commentaire) throws IOException, NoSuchAlgorithmException, JAXBException {
        Operation operation = operationRepository.findById(operationId).orElse(null);
        if (operation.getStatut().equals(StatutOperation.ENATTENTE_VALIDATION) && statutOperation.equals(StatutOperation.VALIDE)) {
            operation.statut(StatutOperation.ENATTENTE);
            UniteOrganisation uniteOrganisationSource = operation.getPointVente().getUniteOrganisation();
            verifFonds(operation.getMontantOp(), uniteOrganisationSource, EtiquetteCompteCode.CPTEOPERATIONACTEURPTI);

            // UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.PAYS);
            //  UniteOrganisation uoPaysSrc = uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());
            UniteOrganisation uoPaysSrc;
            if (!uniteOrganisationSource.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
                uoPaysSrc = uniteOrganisationService.findOneById(uniteOrganisationSource.getUoPaysId());
            } else {
                uoPaysSrc = uniteOrganisationSource;
            }

            UniteOrganisation uniteOrganisationDest = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());
            UniteOrganisation fseDest = uniteOrganisationService.getUoFseFilialeWithUoPays(uniteOrganisationDest);//uo agence,master,sous distributeur
            UniteOrganisation fseSrc = uniteOrganisationService.getUoFseFilialeWithUoPays(uoPaysSrc);
            UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(fseDest);

            Result response = apiService.sendRequestForComptabilisationEnvoiCash(uoPaysSrc, uniteOrganisationDest, operation, fseSrc, fseDest, fseDestAgenceSiege, Boolean.TRUE);
            verifResultOfOmnicanal(response, operation);
            TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationService.findByOne(uniteOrganisationSource.getTypeUO().getId());
            List<RegleCommission> regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), uniteOrganisationDest.getId(), null, null, operation.getTypeService().getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));

            Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
            RegleCommission regleCommission;
            if (regleCommissionExist.isPresent()) {
                regleCommission = regleCommissionExist.get();
            } else {
                regleCommission = null;
            }
            //   instructionComptableService.comptabiliserOperationWithRegle(operation, fseDestAgenceSiege, uniteOrganisationSource, regleCommission);

            repartitionCommission(operation, uniteOrganisationSource, MontantsAComptabiliser.FRAISEMT);

            sendNewCodeRetrait(operationMapper.toDto(operation));

            bilanOperationService.saveCumulJournalierActeur(uniteOrganisationSource.getId(), operation.getTypeService().getId(), operation.getCanal().getId(), null, null, uoPaysSrc.getId(), operation.getUoAcquereur().getId(),
                    operation.getMontantOp());
            bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), operation.getBeneficaire().getId(), operation.getEmetteur().getId(), uoPaysSrc.getId(), operation);

            operation = operationRepository.save(operation);
            return operationMapper.toDto(operation);
        } else {
            operation.statut(StatutOperation.ANNULLE)
                    .commentaire(commentaire);
            operation.dateTraitement(ZonedDateTime.now());
            operationRepository.save(operation);
            return operationMapper.toDto(operation);
        }
    }

    public OperationDTO comptabilisationReceptionCash(Long operationId) throws IOException, JAXBException {
        Operation operation = operationRepository.findById(operationId).orElse(null);
        if (operation.getStatut().equals(StatutOperation.ENATTENTE)) {

            UniteOrganisation fseDest = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());
            UniteOrganisation fseSrc = uniteOrganisationService.findOneById(operation.getUoEmetteur().getId());
            //uo agence,master,sous distributeur
            // UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(fseDest);
            UniteOrganisation fseSource = uniteOrganisationService.findOneById(operation.getUoEmetteur().getId());;
            UniteOrganisation organisationSource = operation.getPointVente().getUniteOrganisation();

            //   UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(fseSource, TypeUO.PAYS);
            UniteOrganisation uoPaysSrc = returnPays(fseSource);//uniteOrganisationService.findOneById(fseSource.getUoPaysId());

            if (!fseSource.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
                uoPaysSrc = uniteOrganisationService.findOneById(fseSource.getUoPaysId());
            } else {
                uoPaysSrc = fseSource;
            }

            UniteOrganisation uoPaysDest = returnPays(fseDest); // uniteOrganisationService.findOneById(fseDest.getUoPaysId());
            //   UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq

            Result response = apiService.sendRequestComptabilisationOfCashTransfertToOmnicanal(uoPaysSrc, uoPaysDest, operation, fseSrc, fseDest, operation.getCompteDest(), Boolean.TRUE, Boolean.TRUE);

            //Result response = apiService.sendRequestComptabilisationOfCashAccountToOmnicanal(uoPaysSrc, uoPaysDest, operation, fseSource, organisationDest, compte, Boolean.TRUE, Boolean.TRUE);
            verifResultOfOmnicanal(response, operation);
            //ComptabilisationPTI
            TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationService.findByOne(organisationSource.getTypeUO().getId());
            List<RegleCommission> regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), uoPaysDest.getId(), null, fseDest.getId(), operation.getTypeService().getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
            Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
            RegleCommission regleCommission;
            if (regleCommissionExist.isPresent()) {
                regleCommission = regleCommissionExist.get();
            } else {
                regleCommission = null;
            }
            UniteOrganisation uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(fseDest);

            //  instructionComptableService.comptabiliserOperationWithRegle(operation, uoDestAgenceSiege, organisationSource, regleCommission);
            repartitionCommission(operation, operation.getPointVente().getUniteOrganisation(), MontantsAComptabiliser.FRAISACQ);
            operation.statut(StatutOperation.PAYE);
            operation = operationRepository.save(operation);

            bilanOperationService.saveCumulJournalierActeur(operation.getPointVente().getUniteOrganisation().getId(),
                    operation.getTypeService().getId(),
                    operation.getCanal().getId(), null, null,
                    uoPaysSrc.getId(), uoPaysDest.getId(),
                    operation.getMontantOp());

            bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), operation.getBeneficaire().getId(), operation.getEmetteur().getId(), uoPaysSrc.getId(), operation);

            return operationMapper.toDto(operation);

        } else {
            throw new CustomParameterizedException("Impossible d'effectuer la transaction.");
        }
    }

    /**
     * @param operationId
     * @param statutOperation
     * @param commentaire
     * @return
     * @throws IOException
     */
    @Transactional
    public OperationDTO comptabilisationCashAccount(Long operationId, StatutOperation statutOperation, String commentaire) throws IOException, JAXBException {
        Operation operation = operationRepository.findById(operationId).orElse(null);
        if (operation.getStatut().equals(StatutOperation.ENATTENTE_VALIDATION) && statutOperation.equals(StatutOperation.VALIDE)) {
            UniteOrganisation organisationDest = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());
            UniteOrganisation fseDest = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());
            UniteOrganisation fseSrc = uniteOrganisationService.findOneById(operation.getUoEmetteur().getId());
            if (!fseSrc.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.toString())) {
                fseSrc = uniteOrganisationService.findUoParentByType(fseSrc, TypeUO.FSE);
            }
            //uo agence,master,sous distributeur
            // UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(fseDest);
            UniteOrganisation fseSource = operation.getUoEmetteur();
            UniteOrganisation organisationSource = operation.getPointVente().getUniteOrganisation();
            organisationSource = uniteOrganisationService.findOneById(organisationSource.getId());
            Compte compte = operation.getCompteDest();
            UniteOrganisation uoPaysSrc;
            if (!fseSource.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
                uoPaysSrc = uniteOrganisationService.findOneById(fseSource.getUoPaysId());
            } else {
                uoPaysSrc = fseSource;
            }
            UniteOrganisation uoPaysDest;
            if (!fseDest.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
                uoPaysDest = uniteOrganisationService.findOneById(fseDest.getUoPaysId());
            } else {
                uoPaysDest = fseSource;
            }
            // UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(fseSource, TypeUO.PAYS);
            //  UniteOrganisation uoPaysSrc = uniteOrganisationService.findOneById(fseSource.getUoPaysId());
            //  UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(c, TypeUO.PAYS);//Pays acq
            // UniteOrganisation uoPaysDest = uniteOrganisationService.findOneById(fseDest.getUoPaysId());
            Result response = apiService.sendRequestComptabilisationOfCashAccountToOmnicanal(uoPaysSrc, uoPaysDest, operation, fseSource, organisationDest, compte, Boolean.TRUE, Boolean.TRUE);
            verifResultOfOmnicanal(response, operation);
            //ComptabilisationPTI
            // RegleCommission regleCommission = regleCommissionService.verifRegleCommission(fseDest.getId(), fseDest.getId(), TypeOperateur.valueOf(operation.getPointVente().getUniteOrganisation().getTypeUO().getCodeTypeUO()), operation.getTypeService().getId(), compteOperationActeur, uoSrc.getId(), uoDest.getId());

            TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationService.findByOne(organisationSource.getTypeUO().getId());
            List<RegleCommission> regleCommissions = regleCommissionService.findByCriteria(uoPaysSrc.getId(), uoPaysDest.getId(), fseSrc.getId(), fseDest.getId(), operation.getTypeService().getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()), operation.getMontantOp());
            Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
            RegleCommission regleCommission;
            if (regleCommissionExist.isPresent()) {
                regleCommission = regleCommissionExist.get();
            } else {
                regleCommission = null;
            }
            UniteOrganisation uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(fseDest);

            //    instructionComptableService.comptabiliserOperationWithRegle(operation, uoDestAgenceSiege, organisationSource, regleCommission);
            repartitionCommission(operation, operation.getPointVente().getUniteOrganisation(), MontantsAComptabiliser.FRAISEMT);
            operation.statut(StatutOperation.PAYE);
            operation = operationRepository.save(operation);

            bilanOperationService.saveCumulJournalierActeur(operation.getPointVente().getUniteOrganisation().getId(),
                    operation.getTypeService().getId(),
                    operation.getCanal().getId(), null, null,
                    uoPaysSrc.getId(), uoPaysDest.getId(),
                    operation.getMontantOp());
            //bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), operation.getBeneficaire().getId(), operation.getEmetteur().getId(), uoPaysSrc.getId(), operation);
            bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), operation.getBeneficaire().getId(), null, uoPaysSrc.getId(), operation);
            return operationMapper.toDto(operation);
        } else if (statutOperation.equals(StatutOperation.REJETE)) {
            operation.statut(StatutOperation.ANNULLE)
                    .commentaire(commentaire);
            operation.dateTraitement(ZonedDateTime.now());
            operationRepository.save(operation);
            return operationMapper.toDto(operation);
        } else {
            throw new CustomParameterizedException("Impossible d'effectuer la transaction.");
        }

    }

    private Canal getCanal(OperationDTO operationDTO) {
        String canalLibelle = operationDTO.getCanalLibelleCanal();
        return canalService.findByCodeCanal(canalLibelle);
    }

    /**
     * @param montant
     * @param organisation
     * @param etiquetteCompteCode
     */
    public Compte verifFonds(BigDecimal montant, UniteOrganisation organisation, EtiquetteCompteCode etiquetteCompteCode) {
        log.info("Verification fonds du compte " + etiquetteCompteCode.name() + " de " + organisation.getLibelleUniteOrganisation());

        Compte compte = compteService.getCompteOfOrganisation(organisation, etiquetteCompteCode);
        EtiquetteCompteCode etiquetteCompte = EtiquetteCompteCode.valueOf(compte.getEtiquette());
        if (!organisation.getTypeUO().getCodeTypeUO().equals(TypeUO.AGENCE.name())) {
            Boolean debitEstPossible = false;
            if (compte.getCompteAutre() != null) {

                CompteAutre cpte = compteAutreRepository.findById(compte.getCompteAutre().getId()).orElse(null);

                if (etiquetteCompte.name().endsWith("PTI")) {
                    debitEstPossible = compteService.peutDebiterComptePTI(cpte, montant);
                } else {
                    debitEstPossible = compteService.peutDebiterCompte(compte, montant);
                }
            }
            if (!debitEstPossible) {
                throw new CustomParameterizedException("Fonds insuffisants.");
            }
        }
        return compte;
    }

    /**
     * Service de realisation des operations de retrait dans les differents
     * comptes
     *
     * @param operationDTO
     * @return
     * @throws IOException
     */
    public ResponseEntity saveInforamationOfOperationRetraitCompte(@NotNull OperationDTO operationDTO) throws IOException {
        User userConnecte = userService.getUserWithAuthorities();
        userCanDoOperations(userConnecte);
        UniteOrganisation userOrganisation = userConnecte.getUniteOrganisation();
        TypeService typeService = typeServiceService.findByCodeService(String.valueOf(operationDTO.getCode()));//
        if (verifAutorisationOfTypeService(typeService)) {
            UniteOrganisation organisation = uniteOrganisationService.findOneById(operationDTO.getUoAcquereurId());
            Compte compteDest = compteService.getCompteOfOrganisation(userOrganisation, EtiquetteCompteCode.CPTEOPERATIONACTEUR);
            Compte compteSrc = new Compte();
            if (operationDTO.getCode().equals(TypeServiceLibelle.RETRAIT_OP_ACTEUR.toString())) {
                //  verifFonds(operationDTO.getMontantOp(), organisation, EtiquetteCompteCode.CPTEOPERATIONACTEURPTI);
                compteSrc = compteService.getCompteOfOrganisation(organisation, EtiquetteCompteCode.CPTEOPERATIONACTEUR);
            } else if (operationDTO.getCode().equals(TypeServiceLibelle.RETRAIT_COMMISSION.toString())) {
                verifFonds(operationDTO.getMontantOp(), organisation, EtiquetteCompteCode.CPTECOMACTEURPTI);
                compteSrc = compteService.getCompteOfOrganisation(organisation, EtiquetteCompteCode.CPTECOMACTEUR);
            }

            Operation operationRetrait = operationMapper.toEntity(operationDTO);
            Canal canal = getCanal(operationDTO);
            operationRetrait.statut(StatutOperation.PAYE)
                    .beneficaire(null)
                    .canal(canal)
                    .emetteur(null)
                    .beneficaire(null)
                    .typeService(typeService)
                    .pointVente(userConnecte.getPointVente())
                    .montantFrais(BigDecimal.ZERO)
                    .montantFrais(BigDecimal.ZERO)
                    .montantFraisAcq(BigDecimal.ZERO)
                    .dateOperation(ZonedDateTime.now())
                    .etape(Etape.PROCEED)
                    .uoEmetteur(userOrganisation)
                    .uoAcquereur(organisation)
                    .compteSrc(compteSrc)
                    .compteDest(compteDest)
                    .user(userConnecte)
                    .codeOperation(RandomUtil.getCodeOperation(ZonedDateTime.now()));
            operationRetrait = operationRepository.save(operationRetrait);//Enregistrement d'une operation
            operationAuditService.saveOpAudit(operationRetrait, null, StatutOperation.PAYE);
            instructionComptableService.comptabiliserOperation(operationRetrait, organisation, userOrganisation);
            return apiService.virementSimple(compteSrc, compteDest, operationDTO.getMontantOp(), operationRetrait.getCodeOperation(), operationRetrait);
        } else {
            throw new CustomParameterizedException("");
        }
    }

    private Long getIdUo(Compte cmptClient) {
        if (cmptClient.getEtiquette().equals("CPTECLIENT")) {
            if (cmptClient.getCompteAutre() != null) {
                UniteOrganisation uo = uniteOrganisationService.findByCodeFSE(cmptClient.getCompteAutre().getCodeFse());
                return uo.getId();
            }

        } else if (cmptClient.getUniteOrganisation() != null) {
            return cmptClient.getUniteOrganisation().getId();
        }
        return null;
    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    public Operation comptabilisationOperationRegulation(Operation operation) throws IOException, NoSuchAlgorithmException, JAXBException {

        try {
            log.info("Comptabilsation de l'operaiton paiement de facture en cours ");
            Compte cmptClientSrc = operation.getCompteSrc();
            Compte cmpteClientDest = operation.getCompteDest();

            Long fseSrcId = getIdUo(cmptClientSrc);
            Long fseDestId = getIdUo(cmpteClientDest);

            UniteOrganisation fseDest = uniteOrganisationService.findOneById(fseDestId);//Fse Dest
            UniteOrganisation fseSrc = uniteOrganisationService.findOneById(fseSrcId); //Fse Acq
            TypeUniteOrganisation typeUniteOrganisationSrc = typeUniteOrganisationService.findByOne(fseSrc.getTypeUO().getId());
            TypeUniteOrganisation typeUniteOrganisationDest = typeUniteOrganisationService.findByOne(fseDest.getTypeUO().getId());
            fseDest.setTypeUO(typeUniteOrganisationDest);
            fseSrc.setTypeUO(typeUniteOrganisationSrc);
            operation.statut(StatutOperation.PAYE)
                    .uoEmetteur(fseSrc)
                    .uoAcquereur(fseDest);

            if (!typeUniteOrganisationSrc.getCodeTypeUO().equals(TypeUO.FSE.name())) {
                fseSrc = uniteOrganisationService.findUoParentByType(fseSrc, TypeUO.FSE); //Fse Acq
            }
            TransactionComptePti trans = transactionComptePtiService.findByOperationAndCompteAutre(operation.getCodeOperation(), cmpteClientDest.getCompteAutre().getId());
            if (trans == null) {
                Result response = apiService.virementSimple(cmptClientSrc, cmpteClientDest, operation.getMontantOp(), "REGULATION", operation.getCodeOperation(), operation);
                verifResultOfOmnicanal(response, operation);
                Compte cmptPtiSrc = new Compte();
                cmptPtiSrc.setEtiquette(cmptClientSrc.getEtiquette() + "PTI");
                Compte cmptPtiDest = new Compte();
                cmptPtiDest.setEtiquette(cmpteClientDest.getEtiquette() + "PTI");
                UniteOrganisation uoAcquereur = operation.getUoAcquereur();
                UniteOrganisation uoEm = operation.getUoEmetteur();
                if (uoEm.getTypeUO().getCodeTypeUO().equals(TypeUO.FSE.name())) {
                    UniteOrganisation uoEmAgenceSiege = uniteOrganisationService.getFseAgenceSiege(uoEm);
                    uoEm = uoEmAgenceSiege;
                }
                if (uoAcquereur.getTypeUO().getCodeTypeUO().equals(TypeUO.FSE.name())) {
                    UniteOrganisation uoAcqAgenceSiege = uniteOrganisationService.getFseAgenceSiege(uoAcquereur);
                    uoAcquereur = uoAcqAgenceSiege;
                }

                instructionComptableService.comptabiliserOperationSimple(operation, cmptPtiSrc, cmptPtiDest, uoEm, uoAcquereur);
                operation = operationRepository.save(operation);
            } else {
                operation = operationRepository.save(operation);
                throw new CustomSettlementException("Cette transaction est déjà validée .");
            }
            return operation;
        } catch (Exception e) {
            throw new CustomParameterizedException(e.getMessage());
        }

        // envoie sms
        // envoiMsgOperation(operation, "0");
    }

    @Transactional
    public OperationDTO rejetOperation(OperationDTO operationDTO) throws IOException, NoSuchAlgorithmException, JAXBException {
        User userConnecte = userService.getUserWithAuthorities();
        Operation operation = operationRepository.findById(operationDTO.getOperationTraiteId()).orElse(null);
        if (verifIfValidateur(userConnecte, operation.getTypeService().getId(), operation.getMontantOp())) {

            if (operation.getStatut().equals(StatutOperation.ENATTENTE_VALIDATION)) {
                operation.statut(StatutOperation.ANNULLE)
                        .commentaire(operationDTO.getCommentaire());
                operation.dateTraitement(ZonedDateTime.now());
                operationAuditService.saveOpAudit(operation, null, StatutOperation.ANNULLE);
                operationRepository.save(operation);

                return operationMapper.toDto(operation);
            } else {
                return operationDTO;
            }

        } else {
            throw new CustomParameterizedException("Privilèges insuffisants.");
        }

    }

    /**
     * Validation des operations en attentes de validation
     *
     * @param operationDTO
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @Transactional
    public Operation validationOperationDeuxPhase(Operation operation) throws IOException, NoSuchAlgorithmException, JAXBException {
        if (operation.getStatut().equals(StatutOperation.PAYE)) {
            Operation oper = comptabilisationOperationMad(operation, operation.getPointVente(), null);
            return operation;
        } else {
            throw new CustomParameterizedException("Impossible de validation l'opération.");
        }

    }

    /**
     * Validation des operations en attentes de validation
     *
     * @param operationDTO
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @Transactional
    public OperationDTO validationOperation(OperationDTO operationDTO) throws IOException, NoSuchAlgorithmException, JAXBException {
        try {
            User userConnecte = userService.getUserWithAuthorities();
            Operation operation = operationRepository.findById(operationDTO.getOperationTraiteId()).orElse(null);
            if (verifIfValidateur(userConnecte, operation.getTypeService().getId(), operation.getMontantOp())) {
                userCanValideOperations(operation, userConnecte);
                if (operation.getTypeService().getCode().equals(String.valueOf(TypeServiceLibelle.ENVOI_CASH))) {
                    return comptabilisationEnvoiCash(operation.getId(), operationDTO.getStatut(), operationDTO.getCommentaire());
                } else if (operation.getTypeService().getCode().equals(String.valueOf(TypeServiceLibelle.CASH_ACCOUNT))) {
                    return comptabilisationCashAccount(operation.getId(), operationDTO.getStatut(), operationDTO.getCommentaire());
                } else if (operation.getTypeService().getCode().equals(String.valueOf(TypeServiceLibelle.REGULATION))) {
                    comptabilisationOperationRegulation(operation);

                } else if (operation.getTypeService().getCode().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT_INTERNE)) || operation.getTypeService().getCode().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT)) || operation.getTypeService().getCode().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT_AVEC_COMM))) {
                    return comptablisationApprovisionnement(operation.getId());
                } else if (operation.getTypeService().getCode().equals(String.valueOf(TypeServiceLibelle.CREATION_MONNAIE)) || operation.getTypeService().getCode().equals(String.valueOf(TypeServiceLibelle.DESTRUCTION_MONNAIE))) {
                    return comptablisationCreationMonnaie(operation.getId(), operationDTO.getStatut(), operationDTO.getCommentaire());
                } else if (operation.getStatut().equals(StatutOperation.ENATTENTE_VALIDATION) && operationDTO.getStatut().equals(StatutOperation.VALIDE)) {
                    operation.setStatut(StatutOperation.ENATTENTE);
                    operation = operationRepository.save(operation);
                    comptabilisationOperationMad(operation, operation.getPointVente(), null);
                    return operationMapper.toDto(operation);
                }
                return operationMapper.toDto(operation);
            } else {
                throw new CustomParameterizedException("Privilèges insuffisants.");
            }
        } catch (Exception e) {
            throw new CustomParameterizedException(e.getMessage());
        }

    }

    @Transactional(noRollbackFor = CustomSettlementException.class)

    public Operation comptabilisationOperationPaiementCredit(Operation operation, PointVente pointVente, MOperationFacture mOperation, String codeFse) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("Comptabilsation de l'operaiton paiement de facture en cours ");
        Long fseSrcId = operation.getUoEmetteur().getId();
        Long fseDestId = operation.getUoAcquereur().getId();
        UniteOrganisation fseDest = uniteOrganisationService.findOneById(fseDestId);//Fse Dest
        UniteOrganisation fseSrc = uniteOrganisationService.findOneById(fseSrcId); //Fse Acq
        UniteOrganisation fseDestCompte = null;
        if (!fseSrc.getTypeUO().equals(TypeUO.FSE)) {
            fseSrc = uniteOrganisationService.findUoParentByType(fseSrc, TypeUO.FSE); //Fse Acq
        }
        UniteOrganisation organisationDest = operation.getPointVente().getUniteOrganisation();
        //    UniteOrganisation uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(fseDest);
        // UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(fseSrc, TypeUO.PAYS);

        //   UniteOrganisation uoPaysSrc = uniteOrganisationService.findOneById(fseSrc.getUoPaysId());
        // UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq
        //UniteOrganisation uoPaysDest = uniteOrganisationService.findOneById(fseDest.getUoPaysId());
        UniteOrganisation uoPaysSrc;
        if (!fseSrc.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
            uoPaysSrc = uniteOrganisationService.findOneById(fseSrc.getUoPaysId());
        } else {
            uoPaysSrc = fseSrc;
        }

        UniteOrganisation uoPaysDest;
        if (!fseDest.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
            uoPaysDest = uniteOrganisationService.findOneById(fseDest.getUoPaysId());
        } else {
            uoPaysDest = fseDest;
        }
        UniteOrganisation uoP = uniteOrganisationService.findOneById(pointVente.getUniteOrganisation().getId());
        UniteOrganisation uoPays = returnPays(uoP);// uniteOrganisationService.findUoParentByType(uoP, TypeUO.PAYS);
        Compte cmptClientSrc = operation.getCompteSrc();
        Compte cmpteClientDest = operation.getCompteDest();
        if (cmpteClientDest != null && cmpteClientDest.getCompteAutre() != null && cmpteClientDest.getCompteAutre().getCodeFse() != null) {
            fseDestCompte = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(cmpteClientDest.getCompteAutre().getCodeFse());
        } else if (cmpteClientDest != null && cmpteClientDest.getCompteBanque() != null) {
            fseDestCompte = uniteOrganisationService.findByCodeFseAndSupprimeIsFalse(cmpteClientDest.getCompteBanque().getCodeFse());
        }
        if (fseDestCompte == null) {
            fseDestCompte = fseSrc;
        }
        Result response = apiService.sendRequestComptabilisationOfAccountAccountToOmnicanal(uoPays, uoPaysDest, operation, fseSrc, fseDestCompte, cmptClientSrc, cmpteClientDest, Boolean.TRUE, Boolean.TRUE, null);

        verifResultOfOmnicanal(response, operation);
        operation.statut(StatutOperation.PAYE);
        repartitionCommission(operation, pointVente.getUniteOrganisation(), MontantsAComptabiliser.FRAISEMT);
        repartitionCommission(operation, organisationDest, MontantsAComptabiliser.FRAISACQ);
        repartitionCommissionCompensateur(operation);
        operation = operationRepository.save(operation);
        UniteOrganisation uoPv = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
        TypeUniteOrganisation typeUniteOrganisation = typeUniteOrganisationService.findByOne(uoPv.getTypeUO().getId());
        List<RegleCommission> regleCommissions = regleCommissionService.findByCriteria(uoPays.getId(), uoPaysDest.getId(), fseSrc.getId(), fseDest.getId(), operation.getTypeService().getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
        Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
        RegleCommission regleCommission;
        if (regleCommissionExist.isPresent()) {
            regleCommission = regleCommissionExist.get();
        } else {
            regleCommission = null;
        }
        //  instructionComptableService.comptabiliserOperationWithRegle(operation, operation.getUoBeneficiaire(), pointVente.getUniteOrganisation(), regleCommission);

        bilanOperationService.saveCumulJournalierActeur(operation.getPointVente().getUniteOrganisation().getId(), operation.getTypeService().getId(), operation.getCanal().getId(), null, null, uoPaysSrc.getId(), uoPaysDest.getId(),
                operation.getMontantOp());
        Long beneficiaire;
        if (operation.getBeneficaire() != null) {

        }

        if (operation.getBeneficaire() != null) {
            beneficiaire = operation.getBeneficaire().getId();
        } else {
            beneficiaire = null;
        }
        bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), beneficiaire, operation.getEmetteur().getId(), uoPaysSrc.getId(), operation);
        return operation;
    }

    private UniteOrganisation returnPays(UniteOrganisation organisation) {
        UniteOrganisation uoPays;
        log.info("Recherche PAYS " + organisation.getLibelleUniteOrganisation());
        if (!organisation.getTypeUO().getCodeTypeUO().equals(TypeUO.PAYS.name())) {
            if (mapPaysDTO.containsKey(organisation.getUoPaysId().toString()) && mapPaysDTO.get(organisation.getUoPaysId().toString()) != null) {
                uoPays = mapPaysDTO.get(organisation.getUoPaysId().toString());
            } else {
                uoPays = uniteOrganisationService.findOneById(organisation.getUoPaysId());
                if (uoPays != null) {
                    mapPaysDTO.put(organisation.getUoPaysId().toString(), uoPays);
                }

            }
        } else {
            uoPays = organisation;
        }
        return uoPays;
    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    public Operation comptabilisationOperationPaiementFacture(Operation operation, PointVente pointVente) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("Comptabilsation de l'operaiton paiement de facture en cours ");
        Long fseSrcId = operation.getUoEmetteur().getId();
        Long fseDestId = operation.getUoAcquereur().getId();
        Compte compteComm = null;
        UniteOrganisation fseDest = uniteOrganisationService.findOneById(fseDestId);//Fse Dest
        UniteOrganisation fseSrc = uniteOrganisationService.findOneById(fseSrcId); //Fse Acq

        UniteOrganisation fseDestCompta = fseDest;
        UniteOrganisation uoDestAgenceSiege;
        if (!fseSrc.getTypeUO().equals(TypeUO.FSE)) {

            fseSrc = uniteOrganisationService.findUoParentByType(fseSrc, TypeUO.FSE); //Fse Acq
        }
        if (!fseDest.getTypeUO().equals(TypeUO.FSE)) {
            // compteComm = compteService.getCompteOfOrganisationUser(fseDestCompta, EtiquetteCompteCode.CPTECOMACTEUR);
            fseDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.FSE);
        } else {

        }
        uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(fseDest);
        compteComm = compteService.getCompteOfOrganisationUser(uoDestAgenceSiege, EtiquetteCompteCode.CPTEATTENTECOMM);
        UniteOrganisation organisationDest = operation.getPointVente().getUniteOrganisation();
        UniteOrganisation uoPaysSrc = returnPays(fseSrc);// uniteOrganisationService.findOneById(fseSrc.getUoPaysId());
//returnPays(   organisation);
        //  UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq
        UniteOrganisation uoPaysDest = returnPays(fseDest);// uniteOrganisationService.findOneById(fseDest.getUoPaysId());
        UniteOrganisation uoP = uniteOrganisationService.findOneById(pointVente.getUniteOrganisation().getId());
        UniteOrganisation uoPays = returnPays(uoP);
        Compte cmptClientSrc = operation.getCompteSrc();
        Compte cmpteClientDest = operation.getCompteDest();
        if (compteComm != null) {
            log.info("RECHERCHE DU COMPTE DE COMMISSION ");
        }
        log.info("DEBUT DU VIREMENT ");
        Result response = apiService.sendRequestComptabilisationOfAccountAccountToOmnicanal(uoPays, uoPaysDest, operation, fseSrc, fseDest, cmptClientSrc, cmpteClientDest, Boolean.TRUE, Boolean.TRUE, compteComm);
        verifResultOfOmnicanal(response, operation);
        log.info("FIN DU VIREMENT ");
        log.info("DEBUT DU RPARTITION DES COMMISSIONS ");
        repartitionCommission(operation, pointVente.getUniteOrganisation(), MontantsAComptabiliser.FRAISEMT);
        repartitionCommission(operation, organisationDest, MontantsAComptabiliser.FRAISACQ);
        repartitionCommissionCompensateur(operation);
        log.info("FIN DU RPARTITION DES COMMISSIONS ");
        operation = operationRepository.save(operation);
        UniteOrganisation uoPv = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
        TypeUniteOrganisation typeUniteOrganisation = uoPv.getTypeUO();
        List<RegleCommission> regleCommissions = regleCommissionService.findByCriteria(uoPays.getId(), uoPaysDest.getId(), fseSrc.getId(), fseDest.getId(), operation.getTypeService().getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
        Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
        RegleCommission regleCommission;
        if (regleCommissionExist.isPresent()) {
            regleCommission = regleCommissionExist.get();
        } else {
            regleCommission = null;
        }
        // instructionComptableService.comptabiliserOperationWithRegle(operation, operation.getUoBeneficiaire(), pointVente.getUniteOrganisation(), regleCommission);
        bilanOperationService.saveCumulJournalierActeur(operation.getPointVente().getUniteOrganisation().getId(), operation.getTypeService().getId(), operation.getCanal().getId(), null, null, uoPaysSrc.getId(), uoPaysDest.getId(),
                operation.getMontantOp());
        Long beneficiaire;
        if (operation.getBeneficaire() != null) {
            beneficiaire = operation.getBeneficaire().getId();
        } else {
            beneficiaire = null;
        }
        bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), beneficiaire, operation.getEmetteur().getId(), uoPaysSrc.getId(), operation);
        return operation;
    }

    public void sendMessage(Operation operation) {
        if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.ACCOUNT_ACCOUNT))) {
            sendMessageAccountToAccount(operation);

        }

    }

    @Transactional
    public Operation comptabilisationOperationBWWB(Operation operation, PointVente pointVente) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("Comptabilsation de l'operaiton en cours ");
        Long fseDestId = operation.getUoAcquereur().getId();
        DecimalFormat df = new DecimalFormat("###");
        Long fseSrcId = operation.getUoEmetteur().getId();
        UniteOrganisation fseDest = uniteOrganisationService.findById(fseDestId);//Fse Dest
        if (!fseDest.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.toString())) {
            if (fseDest.getTypeUO().getLibelleTypeUO().equals(TypeUO.PAYS.toString())) {
                fseDest = uniteOrganisationService.getFilialeOfUniteOrganisation(fseDest);
            } else {
                fseDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.FSE);
            }
        }
        UniteOrganisation fseSrc = uniteOrganisationService.findById(fseSrcId); //Fse EM
        if (!fseSrc.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.toString())) {
            fseSrc = uniteOrganisationService.findUoParentByType(fseSrc, TypeUO.FSE);
        }
        UniteOrganisation uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(fseDest);
        UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);
        UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq
        UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(pointVente.getUniteOrganisation(), TypeUO.PAYS);
        if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.BANK_TO_WALLET)) || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.WALLET_TO_BANK))) {
            UniteOrganisation organisationDest = operation.getPointVente().getUniteOrganisation();
            Compte cmptClientSrc = operation.getCompteSrc();
            Compte cmpteClientDest = operation.getCompteDest();
            Result response = apiService.sendRequestComptabilisationOfAccountAccountToOmnicanal(uoPays, uoPaysDest, operation, fseSrc, fseDest, cmptClientSrc, cmpteClientDest, Boolean.TRUE, Boolean.TRUE, null);
            verifResultOfOmnicanal(response, operation);
            repartitionCommission(operation, pointVente.getUniteOrganisation(), MontantsAComptabiliser.FRAISEMT);
            repartitionCommission(operation, organisationDest, MontantsAComptabiliser.FRAISACQ);
            repartitionCommissionCompensateur(operation);
            operation = operationRepository.save(operation);

        }

        UniteOrganisation uoPv = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
        TypeUniteOrganisation typeUniteOrganisation = uoPv.getTypeUO();
        List<RegleCommission> regleCommissions = regleCommissionService.findByCriteria(uoPays.getId(), uoPaysDest.getId(), fseSrc.getId(), fseDest.getId(), operation.getTypeService().getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
        Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
        RegleCommission regleCommission;
        if (regleCommissionExist.isPresent()) {
            regleCommission = regleCommissionExist.get();
        } else {
            regleCommission = null;
        }
        instructionComptableService.comptabiliserOperationWithRegle(operation, uoDestAgenceSiege, uoDestAgenceSiege, regleCommission);
        bilanOperationService.saveCumulJournalierActeur(operation.getPointVente().getUniteOrganisation().getId(), operation.getTypeService().getId(), operation.getCanal().getId(), null, null, uoPaysSrc.getId(), uoPaysDest.getId(),
                operation.getMontantOp());
        Long beneficiaire;
        if (operation.getBeneficaire() != null) {
            beneficiaire = operation.getBeneficaire().getId();
        } else {
            beneficiaire = null;
        }
        bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), beneficiaire, null, uoPaysSrc.getId(), operation);
        //  bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), beneficiaire, operation.getEmetteur().getId(), uoPaysSrc.getId(), operation);

        return operation;
    }

    @Transactional(noRollbackFor = CustomSettlementException.class)
    public synchronized Operation comptabilisationMasse(Operation operation) throws IOException, JAXBException {
        log.info("APPEL LA FONCTION comptabilisationMasse  DE LA COMPTABILISATION " + operation.getCodeOperation());
        try {
            log.info("DEBUT DE LA COMPTABILISATION " + operation.getCodeOperation());
            Result result = apiService.virementSimple(operation.getCompteSrc(), operation.getCompteDest(), operation.getMontantOp(), operation.getDescription(), operation.getCodeOperation(), operation);
            verifResultOfOmnicanal(result, operation);
            log.info("FIN DE LA COMPTABILISATION " + operation.getCodeOperation());
            Long beneficiaire;
            if (operation.getBeneficaire() != null) {
                beneficiaire = operation.getBeneficaire().getId();
            } else {
                beneficiaire = null;
            }
            //   instructionComptableService.comptabiliserOperationWithRegle(operation, operation.getUoBeneficiaire(), operation.getPointVente().getUniteOrganisation(), null);
            log.info("DEBUT MISE A JOUR DU CUMUL " + operation.getCodeOperation());
            bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), beneficiaire, null, operation.getUoEmetteur().getId(), operation);
            log.info("FIN MISE A JOUR DU CUMUL " + operation.getCodeOperation());
            log.info("FIN LA FONCTION comptabilisationMasse  DE LA COMPTABILISATION " + operation.getCodeOperation());
            return operation;
        } catch (Exception e) {
            log.info("FIN LA FONCTION comptabilisationMasse  AVEC ERREUR " + e);
            log.info("Une erreur est survenue lors de l'opération" + operation.getCodeOperation());
            throw new CustomSettlementException("Une erreur est survenue lors de l'opération :" + e.getMessage());
        }

    }

    private Compte convertToAccount(MCompte mCompte) {
        Compte compte = new Compte();
        String codeBanque = "0";
        compte.valeurCompte(mCompte.getNumeroCompte())
                .etiquette(EtiquetteCompteCode.CPTECLIENT.toString());
        if (mCompte.getTeneurCompte() != null) {

        }
        UniteOrganisation fseTeneur = uniteOrganisationService.findByCodeFSE(mCompte.getTeneurCompte());
        if (fseTeneur != null) {
            if (mCompte.getTypeCompte().equals(MTypeCompte.BANQUE)) {
                codeBanque = fseTeneur.getCodeBanque();
            }
        }
        if (mCompte.getTypeCompte().equals(MTypeCompte.BANQUE)) {
            compte.categorieCompte(CategorieCompte.BANQUE);
            CompteBanque compteBanque = new CompteBanque();
            compteBanque.codeBanque(codeBanque)
                    .codeFse(mCompte.getTeneurCompte())
                    .rib(mCompte.getRib())
                    .codeAgence(mCompte.getCodeAgence());
            compte.setCompteBanque(compteBanque);
        } else if (mCompte.getTypeCompte().equals(MTypeCompte.AUTRE)) { //Compte autre
            compte.setEtatCompte(EtatCompte.OUVERT);
            compte.setStatutCompte(StatutCompte.ACTIVE);
            compte.categorieCompte(CategorieCompte.AUTRE);
            compte.setClasseCompte(ClasseCompte.PRINCIPAL);
            CompteAutre compteAutre = new CompteAutre();
            compteAutre.setCodeFse(mCompte.getTeneurCompte());
            compteAutre.setSolde(BigDecimal.ZERO);
            compte.compteAutre(compteAutre);
        } else if (mCompte.getTypeCompte().equals(MTypeCompte.CARTE)) {
            compte.categorieCompte(CategorieCompte.CARTE);
            compte.setStatutCompte(StatutCompte.DESACTIVE);
            Carte carte = new Carte();
            carte.setCodeFse(mCompte.getTeneurCompte());
            carte.setSolde(BigDecimal.ZERO);
            carte.setLast4Digit(mCompte.getLas4Digit());
            compte.setCarte(carte);

        }

        return compte;

    }

    public Compte getCompteExterne(Operation operation, boolean compteDest) throws JAXBException {
        if (compteDest) {
            if (operation.getCompteExterneDest() != null) {
                MCompte mCompte = Util.unmarshall(operation.getCompteExterneDest(), MCompte.class);
                Compte cpt = convertToAccount(mCompte);
                return cpt;
            } else {
                return operation.getCompteDest();
            }
        } else {
            if (operation.getCompteExterneSrc() != null) {
                MCompte mCompte = Util.unmarshall(operation.getCompteExterneSrc(), MCompte.class);
                Compte cpt = convertToAccount(mCompte);
                return cpt;
            } else {
                return operation.getCompteSrc();
            }

        }

    }

    /**
     * @param operation
     * @param pointVente
     * @param cptCommission
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @Transactional(noRollbackFor = CustomSettlementException.class)
    public Operation comptabilisationOperationMad(Operation operation, PointVente pointVente, Compte cptCommission) throws IOException, NoSuchAlgorithmException, JAXBException {
        StopWatch stopWatch = new StopWatch("Measure Code Execution Compta");
        try {
            log.info("Comptabilsation de l'operaiton en cours ");
            Long fseDestId = operation.getUoAcquereur().getId();
            DecimalFormat df = new DecimalFormat("###");
            Long fseSrcId = operation.getUoEmetteur().getId();
            String codeOperation = operation.getCodeOperation();
            stopWatch.start("1. Recherche UO");

            log.info("Recherche du l'UO dest ");
            UniteOrganisation fseDest = uniteOrganisationService.findOneById(fseDestId);//Fse Dest
            //  TypeUniteOrganisation typeUniteOrganisa = typeUniteOrganisationService.findByOne(fseDest.getTypeUO().getId());
            if (!fseDest.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.toString())) {
                if (fseDest.getTypeUO().getLibelleTypeUO().equals(TypeUO.PAYS.toString())) {
                    fseDest = uniteOrganisationService.getFilialeOfUniteOrganisation(fseDest);
                } else {
                    fseDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.FSE);
                }
            }
            log.info("Recherche du l'UO src ");
            //  UniteOrganisation fseSrc = uniteOrganisationRepository.findById(fseSrcId).orElse(null); //Fse EM
            UniteOrganisation fseSrc = uniteOrganisationService.findOneById(fseSrcId); //Fse EM
            //TypeUniteOrganisation typeUniteOrganisaSrc = typeUniteOrganisationService.findByOne(fseSrc.getTypeUO().getId());
            if (!fseSrc.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.toString())) {
                if (fseSrc.getTypeUO().getLibelleTypeUO().equals(TypeUO.PAYS.toString())) {
                    fseSrc = uniteOrganisationService.getFilialeOfUniteOrganisation(fseSrc);
                } else {
                    fseSrc = uniteOrganisationService.findUoParentByType(fseSrc, TypeUO.FSE);
                }
            }
            stopWatch.stop();
            log.info("Recherche du l'UO agence siege ");
            UniteOrganisation uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(fseDest);
            //  UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);

            UniteOrganisation uoPaysSrc = returnPays(fseSrc); //uniteOrganisationService.findOneById(fseSrc.getUoPaysId());

            // UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq
            UniteOrganisation uoPaysDest = returnPays(fseDest); //uniteOrganisationService.findOneById(fseDest.getUoPaysId());

            UniteOrganisation uoPdest = uniteOrganisationService.findOneById(pointVente.getUniteOrganisation().getId());
            UniteOrganisation uoPays = returnPays(uoPdest);
            //uniteOrganisationService.findOneById(uoPdest.getUoPaysId());
            // UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(pointVente.getUniteOrganisation(), TypeUO.PAYS);

            if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.TRANSFERT_MASSE))
                    || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.ACCOUNT_ACCOUNT))
                    || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.BANK_TO_WALLET))
                    || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.WALLET_TO_BANK))
                    || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.PI_TO_ACCOUNT))
                    || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.ACCOUNT_TO_PI))
                    || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.CARD_TO_WALLET))
                    || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.WALLET_TO_CARD))
                    || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.SWEEP_DEPOSIT_TO_BANK))
                    || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.AUTO_ALLOCATION))
                    || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.VIREMENT_SERVICE_EXTERIEUR))) {
                UniteOrganisation organisationDest = operation.getPointVente().getUniteOrganisation();
                // Compte cmptClientSrc = operation.getCompteSrc();
                //Compte cmpteClientDest = operation.getCompteDest();

                Compte cmptClientSrc = getCompteExterne(operation, false);
                Compte cmpteClientDest = getCompteExterne(operation, true);
                stopWatch.start("1. Compta OMNICANAL COMPTA A COMPTE");
                Result response;
                /**
                 * *Verification si compte bancaire *
                 */
                if (cmptClientSrc.getCompteBanque() != null || cmpteClientDest.getCompteBanque() != null) {
                    String fseCode = null;
                    if (cmptClientSrc.getCompteBanque() != null) {
                        fseCode = fseSrc.getCodeFse();
                    } else if (cmpteClientDest.getCompteBanque() != null) {
                        fseCode = fseDest.getCodeFse();
                    }

                    Message msg = apiService.checkPing(fseCode);
                    if (msg != null) {
                        if (msg.getCodeErr() == null || (msg.getCodeErr() != null && msg.getCodeErr().equals("-1"))) {
                            throw new CustomParameterizedException(msg.getMsg());
                        }
                    } else {
                        throw new CustomParameterizedException("le système distant est inaccessible!!");
                    }

                }
                stopWatch.stop();
//|| cmptClientSrc.getCarte() != null || cmpteClientDest.getCarte() != null
                log.info("Entrer de la fonction de comptabilisation");
                log.info("ENVOI VERS COMPTE A COMPTE " + codeOperation);
                stopWatch.start("1. Compta sendRequestComptabilisationOfAccountAccountToOmnicanal ");
                response = apiService.sendRequestComptabilisationOfAccountAccountToOmnicanal(uoPays, uoPaysDest, operation, fseSrc, fseDest, cmptClientSrc, cmpteClientDest, Boolean.TRUE, Boolean.TRUE, cptCommission);
                stopWatch.stop();
                stopWatch.start("1. Compta VERIF ");

                verifResultOfOmnicanal(response, operation);
                operation.setStatut(StatutOperation.PAYE);
                stopWatch.stop();
                log.info("FIN DE LA COMPTABILITE " + codeOperation);
                operation = operationRepository.save(operation);
                //}
                stopWatch.start("2. Repartition commission");
                repartitionCommission(operation, pointVente.getUniteOrganisation(), MontantsAComptabiliser.FRAISEMT);
                repartitionCommission(operation, organisationDest, MontantsAComptabiliser.FRAISACQ);
                // repartitionCommissionCompensateur(operation);
                operation = operationRepository.save(operation);

                stopWatch.stop();
                // A envoyer uniquement de la cadre du canal WEB
                if (operation.getCanal().getCodeCanal().equals("WEB")) {
                    sendMessageAccountToAccount(operation);
                }

            } else {
                Compte cmptClientSrc = operation.getCompteSrc();
                Result response;
                if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.PAIEMENT_INTERNET))
                        || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.RETRAIT_CARDLESS)) || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.RETRAIT_COMPTE))
                        || operation.getTypeService().getLibelle().equals(TypeServiceLibelle.PAIEMENT_BIEN.toString())) {
                    log.info("ENVOI VERS PAIEMENT INTERNET, PAIEMENT BIEN " + codeOperation);
                    stopWatch.start("1. Compta OMNICANAL sendRequestComptabilisationOfRetraitAccountCashToOmnicanal");
                    response = apiService.sendRequestComptabilisationOfRetraitAccountCashToOmnicanal(operation, fseSrc, fseDest, cmptClientSrc, Boolean.TRUE, Boolean.TRUE, TypeServiceLibelle.PAIEMENT_BIEN.toString());
                    verifResultOfOmnicanal(response, operation);
                    stopWatch.stop();

                    log.info("FIN PAIEMENT INTERNET, PAIEMENT BIEN " + codeOperation);
                    // if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.RETRAIT_CARDLESS))) {
                    operation.statut(StatutOperation.PAYE);
                    operation = operationRepository.save(operation);
                    //   }

                    repartitionCommission(operation, pointVente.getUniteOrganisation(), MontantsAComptabiliser.FRAISACQ);
                    if (operation.getUoEmetteur() != null) {
                        //si l'emetteur est different d'un FSE
                        UniteOrganisation uoEm = uniteOrganisationService.findOneById(operation.getUoEmetteur().getId());
                        //    TypeUniteOrganisation typeUniteOrganisaEmeteur = typeUniteOrganisationService.findByOne(uoEm.getTypeUO().getId());
                        if (!uoEm.getTypeUO().getCodeTypeUO().equals(TypeUO.FSE.name())) {
                            repartitionCommission(operation, operation.getUoEmetteur(), MontantsAComptabiliser.FRAISEMT);
                        }
                    }

                    /**
                     * *SI CARDLESS ****
                     */
                } else {
                    stopWatch.start("1. Compta OMNICANAL sendRequestComptabilisationOfAccountCashToOmnicanal");
                    log.info("ENVOI VERS ACCOUNT CASH " + codeOperation);
                    response = apiService.sendRequestComptabilisationOfAccountCashToOmnicanal(operation, fseSrc, fseDest, cmptClientSrc, Boolean.TRUE, Boolean.TRUE);
                    verifResultOfOmnicanal(response, operation);
                    stopWatch.stop();
                    log.info("FIN VERS ACCOUNT CASH  " + codeOperation);

                    stopWatch.start("1. repartitionCommission");
                    repartitionCommission(operation, pointVente.getUniteOrganisation(), MontantsAComptabiliser.FRAISEMT);
                    stopWatch.stop();

                    stopWatch.start("1. FINALISATION");
                    if (!operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.PAIEMENT_INTERNET))) {
                        String otps[] = RandomUtil.generateOtp();
                        String codeRetrait = otps[1];
                        log.info("Generation du code retrait " + codeRetrait);
                        int dureeExpire = getDureeExpire();

                        operation.commentaire(Base64.encodeBase64String(codeRetrait.getBytes()))
                                .codeRetrait(RandomUtil.getMd5Code(codeRetrait))
                                .dateExpiration(LocalDate.now().plusDays(dureeExpire))
                                .duree(dureeExpire)
                                .secret(otps[0]);
                        operation.setCodeSecret(codeRetrait);
                        operation = operationRepository.saveAndFlush(operation);
                    }
                    stopWatch.stop();

                    //     sendMessage(operation, Boolean.TRUE, codeRetrait);
                }

                // sendEmailWithCodeRetrait(operation, codeRetrait);
            }
            stopWatch.start("1. Recherche des règles ");
            UniteOrganisation uoPv = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
            TypeUniteOrganisation typeUniteOrganisation = uoPv.getTypeUO();
            List<RegleCommission> regleCommissions = regleCommissionService.findByCriteria(uoPays.getId(), uoPaysDest.getId(), fseSrc.getId(), fseDest.getId(), operation.getTypeService().getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));
            Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
            stopWatch.stop();
            RegleCommission regleCommission;
            if (regleCommissionExist.isPresent()) {
                regleCommission = regleCommissionExist.get();
            } else {
                regleCommission = null;
            }
            stopWatch.start("1. Comptabilisation INTERNE");
            log.info("COMPTABILISATION INTERNE" + codeOperation);
            if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.BANK_TO_WALLET)) || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.WALLET_TO_BANK)) || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.SWEEP_DEPOSIT_TO_BANK)) || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.AUTO_ALLOCATION))) {
                if (uoDestAgenceSiege == null) {
                    uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(uniteOrganisationService.findOneById(pointVente.getUniteOrganisation().getId()));
                }
                //   instructionComptableService.enregistreInstructionComptableRegistre(operation, uoDestAgenceSiege, uoDestAgenceSiege, regleCommission);
                instructionComptableService.comptabiliserOperationWithRegle(operation, uoDestAgenceSiege, uoDestAgenceSiege, regleCommission);
            } else {
                //instructionComptableService.enregistreInstructionComptableRegistre(operation, uoDestAgenceSiege, pointVente.getUniteOrganisation(), regleCommission);
            }
            log.info("FIN COMPTABILISATION INTERNE" + codeOperation);
            stopWatch.stop();
            stopWatch.start("1. Bilan journalier acteur");
            log.info("BILAN DES OPERATIONS" + codeOperation);
            bilanOperationService.saveCumulJournalierActeur(operation.getPointVente().getUniteOrganisation().getId(), operation.getTypeService().getId(), operation.getCanal().getId(), null, null, uoPaysSrc.getId(), uoPaysDest.getId(),
                    operation.getMontantOp());
            stopWatch.stop();
            Long beneficiaire;
            log.info("FIN DU BILAN DES OPERATIONS" + codeOperation);
            if (operation.getBeneficaire() != null) {
                beneficiaire = operation.getBeneficaire().getId();
            } else {
                beneficiaire = null;
            }

            stopWatch.start("1. Bilan journalier client");
            bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), beneficiaire, null, uoPaysSrc.getId(), operation);
            //  bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), beneficiaire, operation.getEmetteur().getId(), uoPaysSrc.getId(), operation);
            stopWatch.stop();
            System.out.println(stopWatch.prettyPrint());
            return operation;
        } catch (Exception e) {
            throw new CustomSettlementException("Une erreur est survenue lors de l'opération" + e.getMessage());
        }

    }

    /**
     * @param operation
     * @param pointVente
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @Transactional(noRollbackFor = CustomSettlementException.class)
    public Operation comptabilisationOperationCardless(Operation operation, PointVente pointVente) throws IOException, NoSuchAlgorithmException, JAXBException {
        log.info("Comptabilsation de l'operaiton en cours ");
        Long fseDestId = operation.getUoAcquereur().getId();
        DecimalFormat df = new DecimalFormat("###");
        Long fseSrcId = operation.getUoEmetteur().getId();
        StopWatch stopWatch = new StopWatch("Measure Code Execution");
        stopWatch.start("1. Recherche UO");
        UniteOrganisation fseDest = uniteOrganisationService.findOneById(fseDestId);//Fse Dest
        //  TypeUniteOrganisation typeUniteOrganisa = typeUniteOrganisationService.findByOne(fseDest.getTypeUO().getId());
        if (!fseDest.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.toString())) {
            if (fseDest.getTypeUO().getLibelleTypeUO().equals(TypeUO.PAYS.toString())) {
                fseDest = returnPays(fseDest);
                fseDest = uniteOrganisationService.getFilialeOfUniteOrganisation(fseDest);
            } else {
                fseDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.FSE);
            }
        }
        //  UniteOrganisation fseSrc = uniteOrganisationRepository.findById(fseSrcId).orElse(null); //Fse EM
        UniteOrganisation fseSrc = uniteOrganisationService.findOneById(fseSrcId); //Fse EM
        //TypeUniteOrganisation typeUniteOrganisaSrc = typeUniteOrganisationService.findByOne(fseSrc.getTypeUO().getId());
        if (!fseSrc.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.toString())) {
            if (fseSrc.getTypeUO().getLibelleTypeUO().equals(TypeUO.PAYS.toString())) {
                fseSrc = uniteOrganisationService.getFilialeOfUniteOrganisation(fseSrc);
            } else {
                fseSrc = uniteOrganisationService.findUoParentByType(fseSrc, TypeUO.FSE);
            }
        }
        //    stopWatch.stop();

        UniteOrganisation uoDestAgenceSiege = uniteOrganisationService.getFseAgenceSiege(fseDest);
        //  UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);

        UniteOrganisation uoPaysSrc = returnPays(fseSrc); //uniteOrganisationService.findOneById(fseSrc.getUoPaysId());

        // UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq
        UniteOrganisation uoPaysDest = returnPays(fseDest); //uniteOrganisationService.findOneById(fseDest.getUoPaysId());

        UniteOrganisation uoPdest = uniteOrganisationService.findOneById(pointVente.getUniteOrganisation().getId());
        UniteOrganisation uoPays = returnPays(uoPdest);
        //uniteOrganisationService.findOneById(uoPdest.getUoPaysId());
        // UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(pointVente.getUniteOrganisation(), TypeUO.PAYS);

        Compte cmptClientSrc = operation.getCompteSrc();
        Result response;
        if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.RETRAIT_CARDLESS)) || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.RETRAIT_COMPTE)) || operation.getTypeService().getLibelle().equals(TypeServiceLibelle.PAIEMENT_BIEN.toString())) {

            response = apiService.sendRequestComptabilisationOfRetraitAccountCashToOmnicanal(operation, fseSrc, fseDest, cmptClientSrc, Boolean.TRUE, Boolean.TRUE, TypeServiceLibelle.PAIEMENT_BIEN.toString());

            if (verifResultOfOmnicanal2(response, operation)) {
                if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.RETRAIT_CARDLESS))) {
                    operation
                            .statut(StatutOperation.PAYE);
                    operation = operationRepository.saveAndFlush(operation);
                }

                repartitionCommission(operation, pointVente.getUniteOrganisation(), MontantsAComptabiliser.FRAISACQ);
                if (operation.getUoEmetteur() != null) {
                    //si l'emetteur est different d'un FSE
                    UniteOrganisation uoEm = uniteOrganisationService.findOneById(operation.getUoEmetteur().getId());
                    //    TypeUniteOrganisation typeUniteOrganisaEmeteur = typeUniteOrganisationService.findByOne(uoEm.getTypeUO().getId());
                    if (!uoEm.getTypeUO().getCodeTypeUO().equals(TypeUO.FSE.name())) {
                        repartitionCommission(operation, operation.getUoEmetteur(), MontantsAComptabiliser.FRAISEMT);
                    }
                }
            }
            UniteOrganisation uoPv = uniteOrganisationService.findOneById(operation.getPointVente().getUniteOrganisation().getId());
            TypeUniteOrganisation typeUniteOrganisation = uoPv.getTypeUO();
            List<RegleCommission> regleCommissions = regleCommissionService.findByCriteria(uoPays.getId(), uoPaysDest.getId(), fseSrc.getId(), fseDest.getId(), operation.getTypeService().getId(), TypeOperateur.valueOf(typeUniteOrganisation.getLibelleTypeUO()));

            Optional<RegleCommission> regleCommissionExist = regleCommissions.stream().findFirst();
            RegleCommission regleCommission;
            if (regleCommissionExist.isPresent()) {
                regleCommission = regleCommissionExist.get();
            } else {
                regleCommission = null;
            }

            //     instructionComptableService.comptabiliserOperationWithRegle(operation, uoDestAgenceSiege, pointVente.getUniteOrganisation(), regleCommission);
            // bilanOperationService.saveCumulJournalierActeur(operation.getPointVente().getUniteOrganisation().getId(), operation.getTypeService().getId(), operation.getCanal().getId(), null, null, uoPaysSrc.getId(), uoPaysDest.getId(),
            //operation.getMontantOp());
            Long beneficiaire;
            if (operation.getBeneficaire() != null) {
                beneficiaire = operation.getBeneficaire().getId();
            } else {
                beneficiaire = null;
            }
            bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), beneficiaire, null, uoPaysSrc.getId(), operation);
            //  bilanOperationService.saveCumulJournalierClient(null, operation.getTypeService().getId(), operation.getCanal().getId(), beneficiaire, operation.getEmetteur().getId(), uoPaysSrc.getId(), operation);

            stopWatch.stop();

            /**
             * *SI CARDLESS ****
             */
        }

        return operation;
    }

    public OperationDTO setOperationDTO(OperationAmbigueDTO operationDto) {
        OperationDTO operationAmbigueDTO = new OperationDTO();
        operationAmbigueDTO.setBeneficaireId(operationDto.getBeneficaireId());
        operationAmbigueDTO.setBeneficiaireNum(operationDto.getBeneficiaireNum());
        operationAmbigueDTO.setCanalId(operationDto.getCanalId());
        operationAmbigueDTO.setCode(operationDto.getCode());
        operationAmbigueDTO.setCodeExterne(operationDto.getCodeExterne());
        operationAmbigueDTO.setCompteDestId(operationDto.getCompteDestId());
        operationAmbigueDTO.setCompteDestValeurCompte(operationDto.getCompteDestValeurCompte());
        operationAmbigueDTO.setCompteSrcId(operationDto.getCompteSrcId());
        operationAmbigueDTO.setMontantOp(operationDto.getMontantOp());
        operationAmbigueDTO.setTypeServiceId(operationDto.getTypeServiceId());
        operationAmbigueDTO.setUoAcquereurId(operationDto.getUoAcquereurId());
        operationAmbigueDTO.setPointVenteId(operationDto.getPointVenteId());
        operationAmbigueDTO.setMontantFrais(operationDto.getMontantFrais());
        operationAmbigueDTO.setMontantTaxe(operationDto.getMontantTaxe());
        operationAmbigueDTO.setLieuPaiementId(operationDto.getLieuPaiementId());
        operationAmbigueDTO.setTelephone(operationDto.getTelephone());
        operationAmbigueDTO.setCreatedBy(operationDto.getCreatedBy());
        operationAmbigueDTO.setFseDest(operationDto.getFseDest());
        operationAmbigueDTO.setFseSrc(operationDto.getFseSrc());
        operationAmbigueDTO.setEmetteurId(operationDto.getEmetteurId());
        operationAmbigueDTO.setMotifOperationId(operationDto.getMotifOperationId());
        operationAmbigueDTO.setPaysId(operationDto.getPaysId());
        operationAmbigueDTO.setSecret(operationDto.getSecret());
        operationAmbigueDTO.setCodeOperation(operationDto.getCodeOperation());
        // DateTimeFormatter formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        // ZonedDateTime dateTime = ZonedDateTime.parse(operationDto.getDateTraitement(), formatter);
        // operationAmbigueDTO.setDateOperation(dateTime);
        operationAmbigueDTO.setDevisePaiementId(operationDto.getDevisePaiementId());
        operationAmbigueDTO.setEtape(operationDto.getEtape());
        operationAmbigueDTO.setCommentaire(operationDto.getCommentaire());
        operationAmbigueDTO.setUoBeneficiaireId(operationDto.getUoBeneficiaireId());
        operationAmbigueDTO.setUoEmetteurId(operationDto.getUoEmetteurId());
        operationAmbigueDTO.setMotif(operationDto.getMotif());
        return operationAmbigueDTO;
    }

    public OperationAmbigueDTO setOperationAmbigueDTO(OperationDTO operationDto) {
        OperationAmbigueDTO operationAmbigueDTO = new OperationAmbigueDTO();
        operationAmbigueDTO.setBeneficaireId(operationDto.getBeneficaireId());
        operationAmbigueDTO.setBeneficiaireNum(operationDto.getBeneficiaireNum());
        operationAmbigueDTO.setCanalId(operationDto.getCanalId());
        operationAmbigueDTO.setCode(operationDto.getCode());
        operationAmbigueDTO.setCodeExterne(operationDto.getCodeExterne());
        operationAmbigueDTO.setCompteDestId(operationDto.getCompteDestId());
        operationAmbigueDTO.setCompteDestValeurCompte(operationDto.getCompteDestValeurCompte());
        operationAmbigueDTO.setCompteSrcId(operationDto.getCompteSrcId());
        operationAmbigueDTO.setMontantOp(operationDto.getMontantOp());
        operationAmbigueDTO.setTypeServiceId(operationDto.getTypeServiceId());
        operationAmbigueDTO.setUoAcquereurId(operationDto.getUoAcquereurId());
        operationAmbigueDTO.setPointVenteId(operationDto.getPointVenteId());
        operationAmbigueDTO.setMontantFrais(operationDto.getMontantFrais());
        operationAmbigueDTO.setMontantTaxe(operationDto.getMontantTaxe());
        operationAmbigueDTO.setLieuPaiementId(operationDto.getLieuPaiementId());
        operationAmbigueDTO.setTelephone(operationDto.getTelephone());
        operationAmbigueDTO.setCreatedBy(operationDto.getCreatedBy());
        operationAmbigueDTO.setFseDest(operationDto.getFseDest());
        operationAmbigueDTO.setFseSrc(operationDto.getFseSrc());
        operationAmbigueDTO.setEmetteurId(operationDto.getEmetteurId());
        operationAmbigueDTO.setMotifOperationId(operationDto.getMotifOperationId());
        operationAmbigueDTO.setPaysId(operationDto.getPaysId());
        operationAmbigueDTO.setSecret(operationDto.getSecret());
        operationAmbigueDTO.setCodeOperation(operationDto.getCodeOperation());
        operationAmbigueDTO.setDateOperation(operationDto.getDateOperation().format(DateTimeFormatter.ISO_DATE_TIME));
        operationAmbigueDTO.setDevisePaiementId(operationDto.getDevisePaiementId());
        operationAmbigueDTO.setEtape(operationDto.getEtape());
        operationAmbigueDTO.setCommentaire(operationDto.getCommentaire());
        operationAmbigueDTO.setUoBeneficiaireId(operationDto.getUoBeneficiaireId());
        operationAmbigueDTO.setUoEmetteurId(operationDto.getUoEmetteurId());
        operationAmbigueDTO.setMotif(operationDto.getMotif());
        return operationAmbigueDTO;
    }

    public LogOperation getLogOperation(String codeOperation) {
        LogOperation logOperation = logOperationRepository.findByCodeOperation(codeOperation);
        return logOperation;
    }

    @Async("taskExecutor")
    public void sendAckToOmnicanal(Operation operation, String error, String codeFse, List<MParametre> extraData) throws JAXBException, IOException {
        if (operation != null && operation.getCodeOperation() != null) {
            operation.setStatut(StatutOperation.AMBIGUE);
            operation.setError(error);
            OperationDTO operationDto = operationMapper.toDto(operation);
            operationDto.setCreatedDate(null);
            OperationAmbigueDTO operationAmbigueDTO = new OperationAmbigueDTO();
            operationAmbigueDTO = setOperationAmbigueDTO(operationDto);
            apiService.sendOperationAmbigue(operationAmbigueDTO, codeFse, extraData);
            //   reversionOperation(operation.getId());
            //   operationRepository.save(operation);
            /* LogOperation logOperation = logOperationRepository.findByCodeOperation(operation.getCodeOperation());
            if (logOperation != null && logOperation.getCodeOperationEx() != null) {
                apiService.sendAck(logOperation.getCodeOperationEx().toString());
            }
             */
        }

    }

    public CodeErreur verifMessageRetourOmnicanal(Operation operation) throws JAXBException, IOException {
        CodeErreur codeErreur = null;
        if (operation != null && operation.getCodeOperation() != null) {
            codeErreur = apiService.verifMessageRetourOmnicanal(operation.getCodeOperation());

        }
        return codeErreur;
    }

    @Async("taskExecutor")
    public void sendAckToOmnicanal(Operation operation, String error) throws JAXBException, IOException {
        if (operation != null && operation.getCodeOperation() != null) {
            operation.setStatut(StatutOperation.AMBIGUE);
            operation.setError(error);
            OperationDTO operationDto = operationMapper.toDto(operation);
            operationDto.setCreatedDate(null);
            OperationAmbigueDTO operationAmbigueDTO = new OperationAmbigueDTO();
            operationAmbigueDTO = setOperationAmbigueDTO(operationDto);
            apiService.sendOperationAmbigue(operationAmbigueDTO);
            //   reversionOperation(operation.getId());
            //   operationRepository.save(operation);

        }

    }

    /*@Async("taskExecutor")
    public void sendAckToOmnicanal(Operation operation, String error) throws JAXBException, IOException {
        if (operation != null && operation.getCodeOperation() != null) {
            operation.setStatut(StatutOperation.AMBIGUE);
            operation.setError(error);
            OperationDTO operationDto = operationMapper.toDto(operation);
            operationDto.setCreatedDate(null);
            OperationAmbigueDTO operationAmbigueDTO = new OperationAmbigueDTO();
            operationAmbigueDTO = setOperationAmbigueDTO(operationDto);
            apiService.sendOperationAmbigue(operationAmbigueDTO);
            //   reversionOperation(operation.getId());
            //   operationRepository.save(operation);
          //  LogOperation logOperation = logOperationRepository.findByCodeOperation(operation.getCodeOperation());
            if (logOperation != null && logOperation.getCodeOperationEx() != null) {
                apiService.sendAck(logOperation.getCodeOperationEx().toString());
            }

        }

    }*/
    public void sendAck(Operation operation, String error) throws JAXBException, IOException {
        if (operation != null && operation.getCodeOperation() != null) {
            operation.setStatut(StatutOperation.REJETE);
            operation.setError(error);
            OperationDTO operationDto = operationMapper.toDto(operation);
            operationDto.setCreatedDate(null);
            OperationAmbigueDTO operationAmbigueDTO = new OperationAmbigueDTO();
            operationAmbigueDTO = setOperationAmbigueDTO(operationDto);
            apiService.sendOperationAmbigue(operationAmbigueDTO);
            reversionOperation(operation.getId());
            //   operationRepository.save(operation);
            LogOperation logOperation = logOperationRepository.findByCodeOperation(operation.getCodeOperation());
            if (logOperation != null && logOperation.getCodeOperationEx() != null) {
                apiService.sendAck(logOperation.getCodeOperationEx().toString());
            }

        }

    }

    public String getOperationIdOmniResult(Result response) throws JAXBException {
        if (response != null) {
            String str = response.getText();
            OperationID operid = Util.unmarshall(str, OperationID.class);
            if (operid != null && operid.getCodeOperation() != null) {
                return operid.getCodeOperation().toString();
            }

        }
        return null;
    }

    public String getOperationIdOmni(Result response) throws JAXBException {
        if (response != null) {
            String str = response.getText();
            OperationID operid = Util.unmarshall(str, OperationID.class);
            if (operid != null && operid.getCodeOperation() != null) {
                return operid.getCodeOperation().toString();
            }

        }
        return null;
    }

    /**
     * Traitement de la reponse de comptabilisation sur omnicanal
     *
     * @param response
     * @return
     */
    public Boolean verifResultOfOmnicanal(Result response, Operation operation) throws JAXBException {
        log.info("----------Entrer dans l\'omnicanal-------");
        String codeOperation = "";
        Result res = new Result();
        try {
            if (response == null) {
                log.info("----------Entrer dans l\'omnicanal-------" + response);
                log.info("----------Entrer dans l\'omnicanal-------");
                res.setCodeErr("-1");
                res.setMsg("Impossible d'effectuer la transaction.");
                throw new CustomSettlementException("Impossible d'effectuer la transaction.");
            } else {
                log.info("Reponse---------------" + response.getCodeErr() + "----" + response.getText());
                System.out.println("res--Op------------{ }" + operation);
                if (operation != null) {
                    codeOperation = operation.getCodeOperation();
                    LogOperation logOperation = new LogOperation();
                    logOperation.setCodeOperation(operation.getCodeOperation());
                    logOperation.setDateOperation(ZonedDateTime.now());
                    logOperation.setDescription(response.getDesc());
                    logOperation.setCodeOperationEx(getOperationIdOmni(response));
                    if (operation.getUser() != null) {
                        logOperation.setLogin(operation.getUser().getLogin());
                    }

                    logOperation.setResult(response.getMsg());

                    //operation = operationRepository.save(operation);
                    logOperationRepository.save(logOperation);
                } else {
                    log.info("OPERATION-----EST NULL----------");
                }
                if (response.getCodeErr() != null && response.getCodeErr().equals("0")) {
                    res.setCodeErr(response.getCodeErr());
                    res.setMsg(response.getMsg());
                    log.info("FIN VERFICATION OK " + codeOperation);
                    return true;
                } else {
                    if (response.getMsg() != null) {
                        log.info("FIN VERFICATION NOK " + codeOperation + " " + response.getMsg() + " " + response.getCodeErr() + " " + response.getText());
                        res.setCodeErr(response.getCodeErr());
                        res.setMsg(response.getMsg());
                        throw new CustomSettlementException(" " + response.getMsg());
                    }
                    log.info("FIN VERFICATION NOK " + codeOperation + " ");
                    log.info("Erreur de lors de comptabilisation " + codeOperation);
                    res.setMsg("Erreur de lors de comptabilisation");
                    throw new CustomSettlementException("Erreur de lors de comptabilisation");

                }

            }
        } catch (Exception e) {
            res.setCodeErr("-1");
            res.setMsg(e.getMessage());
            log.info("FIN VERFICATION NOK " + codeOperation + " " + e.getMessage());
            throw new CustomSettlementException("Erreur de lors de comptabilisation ");
        } finally {
            operationAuditEventService.sendOperationEvent(ActionService.ACT_COMPTA_CLIENT, operation.getCodeOperation(), res.getCodeErr(), res.getMsg(), "", "");
        }

    }

    public Boolean verifResultOfOmnicanal2(Result response, Operation operation) throws JAXBException {
        log.debug("----------Entrer dans l\'omnicanal-------");
        if (response == null) {
            throw new CustomSettlementException("Impossible d'effectuer la transaction.");
        } else if (response != null) {
            log.debug("Reponse---------------" + response.getCodeErr() + "----" + response.getText());
            if (operation != null) {
                LogOperation logOperation = new LogOperation();
                logOperation.setCodeOperation(operation.getCodeOperation());
                logOperation.setDateOperation(ZonedDateTime.now());
                logOperation.setDescription(response.getDesc());
                logOperation.setCodeOperationEx(getOperationIdOmni(response));
                if (operation.getUser() != null) {
                    logOperation.setLogin(operation.getUser().getLogin());
                }

                logOperation.setResult(response.getMsg());
                operation.statut(StatutOperation.PAYE);
                operation = operationRepository.save(operation);
                logOperationRepository.save(logOperation);
            }
            if (response.getCodeErr() != null && response.getCodeErr().equals("0")) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Redaction du message Ã  envoyer lors de l'envoi cash
     */
    public void sendMessage(Operation operation, Boolean init, String codeRetrait) {
        log.info("Envoi du code retrait de l'operaiton");
        String message = getContenu(operation, codeRetrait);
        String telephone;
        if (init) {
            telephone = "+" + operation.getEmetteur().getPersonne().getCodePaysTelephone() + operation.getEmetteur().getPersonne().getTelephone();
        } else {
            telephone = "+" + operation.getBeneficaire().getPersonne().getCodePaysTelephone() + operation.getBeneficaire().getPersonne().getTelephone();
        }
        envoiMsgOperation(operation, codeRetrait);
        /* if (telephone != null) {
            apiService.sendSms(telephone, message);
        }*/
    }

    /**
     * Mise en forme et envoi d'un sms de confirmation de retrait
     *
     * @param operation
     */
    @Async
    public void sendMessageConfirmationRetrait(Operation operation) {
        log.info("Envoi de l'sms de confirmation de retrait.");
        String message = "Suite a votre Transfert Numero : ";
        message += operation.getCodeOperation() + ", le beneficiaire a procede au retrait avec succes.";
        message += "Montant:" + operation.getMontantOp();
        String telephone = operation.getEmetteur().getPersonne().getTelephone();
        sendmsg(telephone, message, operation.getEmetteur().getPersonne().getCodePaysTelephone());
    }

    public String getContenuAccSource(Operation operation, BigDecimal solde) {
        String contenu = "Transfert d'argent reussi" + "  ";

        BigDecimal total = Util.getZeroIfNull(operation.getMontantFrais()).add(Util.getZeroIfNull(operation.getMontantOp())).add(Util.getZeroIfNull(operation.getMontantTaxe()));
        contenu += "Montant:" + operation.getMontantOp() + " ";
        contenu += "Frais:" + Util.getZeroIfNull(operation.getMontantFrais()).add(Util.getZeroIfNull(operation.getMontantTaxe())) + " Total:" + total + " Date:" + Util.format(operation.getDateOperation(), "dd-MM-yyyy  HH:mm" + " ");
        contenu += "TID: " + operation.getCodeOperation();

        if (operation.getCompteSrc().getCompteAutre() != null) {
            Compte cptSrc = operation.getCompteSrc();
            Compte cpt = compteService.findCompteOne(cptSrc.getId());
            solde = cpt.getCompteAutre().getSolde();

        }
        contenu += " " + "Votre solde est de " + solde;

        return contenu;
    }

    public String getContenuAccDest(Operation operation, BigDecimal solde) {
        String contenu = null;

        if (operation.getEmetteur().getPersonne().getTypePersonne().equals(TypePersonne.PHYSIQUE)) {
            contenu = "Vous avez recu " + operation.getMontantOp() + " de " + operation.getEmetteur().getPersonne().getPersonnePhysique().getNomClient() + " " + operation.getEmetteur().getPersonne().getTelephone() + "\n";
        } else {
            contenu = "Vous avez recu " + operation.getMontantOp() + " de " + operation.getEmetteur().getPersonne().getPersonneMorale().getRaisonSociale() + " " + operation.getEmetteur().getPersonne().getTelephone() + "\n";
        }

        contenu += "TID: " + operation.getCodeOperation();

        if (operation.getCompteDest().getCompteAutre() != null) {
            Compte cptSrc = operation.getCompteDest();
            Compte cpt = compteService.findCompteOne(cptSrc.getId());
            solde = cpt.getCompteAutre().getSolde();

        }

        // if (cptSrc.getCompteAutre() != null) {
        //     solde = cptSrc.getCompteAutre().getSolde();
        contenu += "  " + "Votre solde est de " + solde;
        //}
        return contenu;
    }

    public void sendmsg(String tel, String msg, String codeIndicatif) {
        if (!"".equals(tel) && tel != null) {
            apiService.sendMessage(tel, msg, codeIndicatif);
        }
    }

    public void sendmsg(String tel, String msg, String codeIndicatif, Operation oper) {
        if (!"".equals(tel) && tel != null) {
            if (oper != null) {
                apiService.sendMessage(tel, msg, codeIndicatif, oper.getId());

            } else {
                apiService.sendMessage(tel, msg, codeIndicatif);
            }

        }
    }

    public void sendMail(String email, String msg) {
        if (!"".equals(email) && email != null) {
            apiService.sendMail(email, msg);
        }
    }

    private void repartirCommission(OperationCommissionDTO opCommission) {
        apiService.repartirCommission(opCommission);
    }

    public String getContenuFactureDest(Operation operation, BigDecimal solde) {
        String contenu = "Vous avez pay la facture montant " + operation.getMontantOp() + "\n";
        contenu += "TID: " + operation.getCodeOperation();
        if (operation.getCompteSrc().getCompteAutre() != null) {
            Compte cptSrc = operation.getCompteSrc();
            Compte cpt = compteService.findCompteOne(cptSrc.getId());
            solde = cpt.getCompteAutre().getSolde();
            contenu += "  " + "Votre solde est de " + solde;
        }

        // if (cptSrc.getCompteAutre() != null) {
        //     solde = cptSrc.getCompteAutre().getSolde();
        //}
        return contenu;
    }

    public String getContenuPaiementBien(Operation operation, BigDecimal solde) {
        String contenu = "Vous avez effectuÃ© un paiement d'un montant de " + operation.getMontantOp() + "\n";
        contenu += " Ã  " + operation.getUoAcquereur().getCodeUO() + " TID: " + operation.getCodeOperation();
        if (operation.getCompteSrc().getCompteAutre() != null) {
            Compte cptSrc = operation.getCompteSrc();
            Compte cpt = compteService.findCompteOne(cptSrc.getId());
            solde = cpt.getCompteAutre().getSolde();

        }

        // if (cptSrc.getCompteAutre() != null) {
        //     solde = cptSrc.getCompteAutre().getSolde();
        contenu += "  " + "Votre solde est de " + solde;
        //}
        return contenu;
    }

    public String getContenuReception(Operation operation, BigDecimal solde) {
        String contenu = "Vous avez transfÃ©rÃ©  " + operation.getMontantOp() + "\n";
        contenu += " vers votre compte,  " + " TID: " + operation.getCodeOperation();

        // if (cptSrc.getCompteAutre() != null) {
        //     solde = cptSrc.getCompteAutre().getSolde();
        contenu += "  " + "Votre solde est de " + solde;
        //}
        return contenu;
    }

    public void sendMessagePaiementFacture(Operation operation) {
        String telephone = null;
        Compte cpt;
        log.info("Envoi de l'sms de l'envoi account to account.");
        String message = "";
        BigDecimal soldeDestinataire = BigDecimal.ZERO;
        BigDecimal soldeEmetteur = BigDecimal.ZERO;
        String codeIndicatif = operation.getEmetteur().getPersonne().getCodePaysTelephone();

        telephone = "+" + operation.getEmetteur().getPersonne().getCodePaysTelephone() + operation.getEmetteur().getPersonne().getTelephone();
        if (operation.getCompteSrc().getCompteAutre() != null && operation.getCompteSrc().getEtiquette().equals("CPTECLIENT")) {
            cpt = compteService.findCompteOne(operation.getCompteSrc().getId());
            soldeDestinataire = cpt.getCompteAutre().getSolde();
        }
        message = getContenuFactureDest(operation, soldeDestinataire);
        sendmsg(telephone, message, codeIndicatif);

    }

    private static VelocityContext populateMessage(Operation operation, BigDecimal solde, String codeRetrait) {
        VelocityContext context = new VelocityContext();

        DateTimeFormatter formatDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter formatHeure = DateTimeFormatter.ofPattern("HH:mm:ss");
        String dateTimeRequest = operation.getDateOperation().format(formatDate);
        String timeRequest = operation.getDateOperation().format(formatHeure);
        context.put("SOLDE", solde);
        context.put("TRANSID", operation.getCodeOperation());
        context.put("MONTANT", operation.getMontantOp().doubleValue());
        context.put("NARRATION", operation.getNarration());
        context.put("NBREJRS", operation.getDuree());
        context.put("DATEOPERATION", dateTimeRequest);
        context.put("HEUREOPERATION", timeRequest);
        context.put("CODERETRAIT", (operation.getCodeSecret() != null) ? operation.getCodeSecret() : "");
        context.put("CODEOTP", (operation.getCodeSecret() != null) ? operation.getCodeSecret() : "");
        if (Util.isBiggerThan(Util.getZeroIfNull(operation.getMontantFraisClient()), BigDecimal.ZERO)) {
            context.put("FRAIS", Util.getZeroIfNull(operation.getMontantFraisClient()).add(Util.getZeroIfNull(operation.getMontantTaxe())).doubleValue());
            context.put("TOTAL", Util.getZeroIfNull(operation.getMontantFrais()).add(Util.getZeroIfNull(operation.getMontantOp())).add(Util.getZeroIfNull(operation.getMontantTaxe())).doubleValue());
        } else {
            context.put("FRAIS", BigDecimal.ZERO.doubleValue());
            context.put("TOTAL", Util.getZeroIfNull(operation.getMontantOp()));
        }

        if (operation.getEmetteur() != null) {
            if (operation.getEmetteur().getPersonne() != null) {
                if (operation.getEmetteur().getPersonne().getPersonnePhysique() != null) {
                    context.put("EMETTEUR", operation.getEmetteur().getPersonne().getPersonnePhysique().getNomClient());
                }
                if (operation.getEmetteur().getPersonne().getPersonneMorale() != null) {
                    context.put("EMETTEUR", operation.getEmetteur().getPersonne().getPersonneMorale().getRaisonSociale());
                }
                context.put("TELEPHONEEME", operation.getEmetteur().getPersonne().getTelephone());

            }
        }
        if (operation.getUoEmetteur() != null) {
            context.put("UOEMETTEUR", operation.getUoEmetteur().getCodeUO() + " " + operation.getUoEmetteur().getLibelleUniteOrganisation());
        }

        if (operation.getUoAcquereur() != null) {
            context.put("UOACQUEREUR", operation.getUoAcquereur().getCodeUO() + " " + operation.getUoAcquereur().getLibelleUniteOrganisation());
        }
        if (operation.getBeneficaire() != null) {
            if (operation.getBeneficaire().getPersonne().getPersonnePhysique() != null) {
                context.put("BENEFICIAIRE", operation.getBeneficaire().getPersonne().getPersonnePhysique().getNomClient());
            }
            if (operation.getBeneficaire().getPersonne().getPersonneMorale() != null) {
                context.put("BENEFICIAIRE", operation.getBeneficaire().getPersonne().getPersonneMorale().getRaisonSociale());
            }
            context.put("TELEPHONEACQ", operation.getBeneficaire().getPersonne().getTelephone());
        }
        if (operation.getDonneurOrdre() != null) {
            context.put("DONNEURORDRE", operation.getDonneurOrdre());
        }

        return context;
    }

    public void envoiRepartition(OperationCommissionDTO op) {
        apiService.repartirCommission(op);
    }

    public void sendCashBack(String opId) {
        apiService.sendCashBack(opId);
    }

    public void sendMessagePaiementBien(Operation operation) {
        String telephone = null;
        Compte cpt;
        log.info("Envoi de l'sms de paiement.");
        String message = "";
        BigDecimal soldeDestinataire = BigDecimal.ZERO;
        BigDecimal soldeEmetteur = BigDecimal.ZERO;
        String codeIndicatif = "";
        if (operation.getCompteSrc().getCompteAutre() != null) {
            codeIndicatif = operation.getEmetteur().getPersonne().getCodePaysTelephone();
            telephone = "+" + operation.getEmetteur().getPersonne().getCodePaysTelephone() + operation.getEmetteur().getPersonne().getTelephone();
            cpt = compteService.findCompteOne(operation.getCompteSrc().getId());
            soldeDestinataire = cpt.getCompteAutre().getSolde();
        }
        message = getContenuPaiementBien(operation, soldeDestinataire);
        sendmsg(telephone, message, codeIndicatif);

    }

    public void envoiMsgOperation(TypeService typeservice, Personne pers) {
        log.info("Envoi de l'sms .");

        String telephoneEmeteur = "";

        List<TemplateMessage> listTemplateEmt = findMessageTemplate(typeservice, "EMETTEUR");
        List<TemplateMessage> listTemplateBen = findMessageTemplate(typeservice, "BENEFICIAIRE");
        String msgEmt;
        //
        Iterator itEmt = listTemplateEmt.iterator();
        while (itEmt.hasNext()) {
            TemplateMessage tplEmt;
            tplEmt = (TemplateMessage) itEmt.next();
            msgEmt = tplEmt.getContenu();
            if (msgEmt != null) {
                telephoneEmeteur = "+" + pers.getCodePaysTelephone() + pers.getTelephone();
                /* Envoi du message a l'emetteur*/
                log.info("Envoi de l'sms a l'emetteur.");

                if (!"".equals(telephoneEmeteur)) {
                    sendmsg(telephoneEmeteur, msgEmt, pers.getCodePaysTelephone());
                }

                log.info("Fin de l'envoi du message emeteur.{}", telephoneEmeteur);
                /**
                 * *fin envoi emetteur
                 */
            }

        }

    }

    public void envoiMailOperation(String email, String etiquette, HashMap<String, String> keyVal) {
        String tplEmt = findContentMail(etiquette, "EMETTEUR");
        String message = velocityWithStringTemplate(tplEmt, keyVal);
        sendMail(email, message);
    }

    public void envoiSmsOperation(String codePaysTelephone, String telephone, String etiquette, HashMap<String, String> keyVal) {
        String tplEmt = findContentMail(etiquette, "EMETTEUR");
        String message = velocityWithStringTemplate(tplEmt, keyVal);
        sendmsg(telephone, message, codePaysTelephone);
    }

    @Async("taskExecutor")
    public void envoiMsgOperation(Operation operation, String codeRetrait) {
        log.info("Envoi de l'sms .");
        BigDecimal soldeDestinataire = BigDecimal.ZERO;
        BigDecimal soldeEmetteur = BigDecimal.ZERO;
        String telephoneEmeteur = "";
        String telephoneBenef = "";
        String message = "";
        String codeIndicatif = "";
        BigDecimal somEve = BigDecimal.ZERO;
        List<TemplateMessage> listTemplateEmt = findMessageTemplate(operation.getTypeService(), "EMETTEUR");
        List<TemplateMessage> listTemplateBen = findMessageTemplate(operation.getTypeService(), "BENEFICIAIRE");
        String msgEmt;
        //
//               = findMessageContent(operation.getTypeService(), "EMETTEUR");
        String msgBen;
        //= findMessageContent(operation.getTypeService(), "BENEFICIAIRE");
        Compte cptSrc;
        Compte cptDest;
        Iterator itEmt = listTemplateEmt.iterator();
        Iterator itBen = listTemplateBen.iterator();
        while (itEmt.hasNext()) {
            TemplateMessage tplEmt;
            tplEmt = (TemplateMessage) itEmt.next();
            msgEmt = tplEmt.getContenu();
            if (msgEmt != null) {
                if (operation.getEmetteur() != null) {
                    codeIndicatif = operation.getEmetteur().getPersonne().getCodePaysTelephone();
                    telephoneEmeteur = "+" + operation.getEmetteur().getPersonne().getCodePaysTelephone() + operation.getEmetteur().getPersonne().getTelephone();
                }
                /* Envoi du message a l'emetteur*/
                log.info("Envoi de l'sms a l'emetteur.");
                if (operation.getCompteSrc() != null) {
                    if (operation.getCompteSrc().getCompteAutre() != null) {
                        if (operation.getTypeService().getCode().equals(TypeServiceLibelle.PAIEMENT_FACTURE_DEPUIS_CASH.toString()) || operation.getTypeService().getCode().equals(TypeServiceLibelle.PAIEMENT_FACTURE.toString())) {
                            if (operation.getCompteSrc().getCompteAutre() != null && operation.getCompteSrc().getEtiquette().equals("CPTECLIENT")) {
                                cptSrc = compteService.findCompteOne(operation.getCompteSrc().getId());
                                somEve = evenementOperationService.getEve(cptSrc);
                                soldeEmetteur = cptSrc.getCompteAutre().getSolde();

                            }
                        } else {
                            cptSrc = compteService.findCompteOne(operation.getCompteSrc().getId());
                            somEve = evenementOperationService.getEve(cptSrc);
                            soldeEmetteur = cptSrc.getCompteAutre().getSolde();
                        }

                    }
                }
                soldeEmetteur = soldeEmetteur.add(somEve);
                message = velocityWithStringTemplate(msgEmt, operation, soldeEmetteur, codeRetrait);
                if (!"".equals(telephoneEmeteur)) {
                    sendmsg(telephoneEmeteur, message, codeIndicatif, operation);
                }

                log.info("Fin de l'envoi du message emeteur.{}", telephoneEmeteur);
                /**
                 * *fin envoi emetteur
                 */
            }

        }
        while (itBen.hasNext()) {
            TemplateMessage tplBen;
            tplBen = (TemplateMessage) itBen.next();
            msgBen = tplBen.getContenu();
            if (msgBen != null) {
                /* Envoi du message au beneficiaire*/
                log.info("Envoi de l'sms au beneficiaire.");

                if (operation.getTypeService().getCode().equals(TypeServiceLibelle.RETRAIT_COMPTE.toString()) || operation.getTypeService().getCode().equals(TypeServiceLibelle.PAIEMENT_BIEN.toString())) {
                    User user = userService.findByPointVente(operation.getPointVente());
                    if (user != null) {
                        codeIndicatif = user.getCodeIndicatif();
                        telephoneBenef = "+" + codeIndicatif + user.getPhoneNumber();
                        Compte cptOp = compteService.getCompteOfOrganisationUser(operation.getPointVente().getUniteOrganisation(), EtiquetteCompteCode.CPTEOPERATIONACTEUR);
                        if (cptOp != null) {
                            soldeDestinataire = cptOp.getCompteAutre().getSolde();
                        }

                    }
                } else {
                    if (operation.getBeneficaire() != null) {
                        codeIndicatif = operation.getBeneficaire().getPersonne().getCodePaysTelephone();
                        telephoneBenef = "+" + operation.getBeneficaire().getPersonne().getCodePaysTelephone() + operation.getBeneficaire().getPersonne().getTelephone();
                    }
                    if (operation.getCompteDest() != null) {
                        if (operation.getCompteDest().getCompteAutre() != null) {
                            cptDest = compteService.findCompteOne(operation.getCompteDest().getId());
                            soldeDestinataire = cptDest.getCompteAutre().getSolde();
                        }
                    }
                    if (operation.getTypeService().getCode().equals(TypeServiceLibelle.PI_PAIEMENT_MARCHAND.toString()) && operation.getUoAcquereur() != null) {
                        UniteOrganisation acq = operation.getUoAcquereur();
                        if (acq != null) {
                            log.info("Envoi de l'sms au beneficiaire.226");
                            codeIndicatif = acq.getCodePaysTelephone();
                            telephoneBenef = "+" + codeIndicatif + acq.getTelephone();
                        }

                    }
                }

                message = velocityWithStringTemplate(msgBen, operation, soldeDestinataire, codeRetrait);
                if (!"".equals(telephoneBenef)) {
                    log.info("Envoi beneficiaire.OK");
                    sendmsg(telephoneBenef, message, codeIndicatif, operation);
                }

            }
        }

    }

    /*En cas d'approvisisonnement*/
    @Async("taskExecutor")
    public void envoiMsgOperation(Operation operation) {
        log.info("Envoi de l'sms .");
        BigDecimal soldeDestinataire = BigDecimal.ZERO;
        BigDecimal soldeEmetteur = BigDecimal.ZERO;
        String telephoneEmeteur = "";
        String telephoneBenef = "";
        String codeIndicatif = "";
        String message = "";
        BigDecimal somEve = BigDecimal.ZERO;
        List<TemplateMessage> listTemplateEmt = findMessageTemplate(operation.getTypeService(), "EMETTEUR");
        List<TemplateMessage> listTemplateBen = findMessageTemplate(operation.getTypeService(), "BENEFICIAIRE");
        String msgEmt;
        //
//               = findMessageContent(operation.getTypeService(), "EMETTEUR");
        String msgBen;
        //= findMessageContent(operation.getTypeService(), "BENEFICIAIRE");
        Compte cptSrc;
        Compte cptDest;
        Iterator itEmt = listTemplateEmt.iterator();
        Iterator itBen = listTemplateBen.iterator();
        while (itEmt.hasNext()) {
            TemplateMessage tplEmt;
            tplEmt = (TemplateMessage) itEmt.next();
            msgEmt = tplEmt.getContenu();
            if (msgEmt != null) {
                if (operation.getUoEmetteur() != null) {
                    /**
                     * ici il s'agit de l'organisation emettrice
                     */

                    UniteOrganisation uoEmt = uniteOrganisationService.findOneById(operation.getUoEmetteur().getId());
                    codeIndicatif = uoEmt.getCodePaysTelephone();
                    telephoneEmeteur = "+" + uoEmt.getCodePaysTelephone() + uoEmt.getTelephone();
                }
                /* Envoi du message a l'emetteur*/
                log.info("Envoi de l'sms a l'emetteur.");
                if (operation.getCompteSrc() != null) {
                    if (operation.getCompteSrc().getCompteAutre() != null) {
                        cptSrc = compteService.findCompteOne(operation.getCompteSrc().getId());
                        somEve = evenementOperationService.getEve(cptSrc);
                        soldeEmetteur = cptSrc.getCompteAutre().getSolde();

                    }
                }
                soldeEmetteur = soldeEmetteur.add(somEve);
                message = velocityWithStringTemplate(msgEmt, operation, soldeEmetteur, "0");
                if (!"".equals(telephoneEmeteur)) {
                    sendmsg(telephoneEmeteur, message, codeIndicatif);
                }

                log.info("Fin de l'envoi du message emeteur.{}", telephoneEmeteur);
                /**
                 * *fin envoi emetteur
                 */
            }
        }
        while (itBen.hasNext()) {
            TemplateMessage tplBen;
            tplBen = (TemplateMessage) itBen.next();
            msgBen = tplBen.getContenu();

            if (msgBen != null) {
                /* Envoi du message au beneficiaire*/
                log.info("Envoi de l'sms au bÃ©nÃ©ficiaire.");

                if (operation.getUoAcquereur() != null) {
                    UniteOrganisation uoBen = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());
                    codeIndicatif = uoBen.getCodePaysTelephone();
                    telephoneBenef = "+" + codeIndicatif + uoBen.getTelephone();
                }
                if (operation.getCompteDest() != null) {
                    if (operation.getCompteDest().getCompteAutre() != null) {
                        cptDest = compteService.findCompteOne(operation.getCompteDest().getId());
                        soldeDestinataire = cptDest.getCompteAutre().getSolde();

                    }

                }
                message = velocityWithStringTemplate(msgBen, operation, soldeDestinataire, "0");
                if (!"".equals(telephoneBenef)) {
                    sendmsg(telephoneBenef, message, codeIndicatif);
                }

            }
        }

    }

    public void sendMessageReception(Operation operation) {
        String telephone = null;
        Compte cpt;
        log.info("Envoi de l'sms de paiement.");
        String message = "";
        BigDecimal soldeDestinataire = BigDecimal.ZERO;
        BigDecimal soldeEmetteur = BigDecimal.ZERO;
        String codeIndicatif = "";
        if (operation.getCompteDest().getCompteAutre() != null) {
            telephone = "+" + operation.getEmetteur().getPersonne().getCodePaysTelephone() + operation.getEmetteur().getPersonne().getTelephone();
            cpt = compteService.findCompteOne(operation.getCompteDest().getId());
            soldeDestinataire = cpt.getCompteAutre().getSolde();
            codeIndicatif = operation.getEmetteur().getPersonne().getCodePaysTelephone();
        }
        message = getContenuReception(operation, soldeDestinataire);
        sendmsg(telephone, message, codeIndicatif);

    }

    public void sendMessageCashToAccount(Operation operation) {
        String telephone = null;
        Compte cpt;
        log.info("Envoi de l'sms de l'envoi account to account.");
        String message = "";
        BigDecimal soldeDestinataire = BigDecimal.ZERO;
        BigDecimal soldeEmetteur = BigDecimal.ZERO;
        String codeIndicatif = "";
        if (operation.getCompteDest().getCompteAutre() != null) {
            cpt = compteService.findCompteOne(operation.getCompteDest().getId());
            soldeDestinataire = cpt.getCompteAutre().getSolde();
            telephone = operation.getBeneficaire().getPersonne().getCodePaysTelephone() + operation.getBeneficaire().getPersonne().getTelephone();
            message = getContenuAccDest(operation, soldeDestinataire);
            codeIndicatif = operation.getBeneficaire().getPersonne().getCodePaysTelephone();
            sendmsg(telephone, message, codeIndicatif);
        }

    }

    private List<TemplateMessage> findMessageTemplate(TypeService typeService, String destinataire) {
        List<TemplateMessage> listtemplateMessage = templateMessageService.findByTypeServiceList(TypeServiceLibelle.valueOf(typeService.getCode()), Destination.valueOf(destinataire));

        return listtemplateMessage;

    }

    private String findContentMail(String etiquette, String destinataire) {
        TemplateMessage templateMessage = templateMessageService.findByEtiquette(etiquette, Destination.valueOf(destinataire));
        if (templateMessage != null) {
            return templateMessage.getContenu();
        }

        return null;
    }

    private String findMessageContent(TypeService typeService, String destinataire) {
        TemplateMessage templateMessage = templateMessageService.findByTypeService(TypeServiceLibelle.valueOf(typeService.getCode()), Destination.valueOf(destinataire));
        if (templateMessage != null) {
            return templateMessage.getContenu();
        }

        return null;
    }

    private String velocityWithStringTemplate(String contenuMessage, HashMap<String, String> keyVal) {
        // Initialize the engine.
        VelocityEngine engine = new VelocityEngine();
        //Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());
        engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");
        engine.setProperty(Velocity.RESOURCE_LOADER, "string");
        engine
                .addProperty("string.resource.loader.class", StringResourceLoader.class
                        .getName());
        engine.addProperty("string.resource.loader.repository.static", "false");
        //  engine.addProperty("string.resource.loader.modificationCheckInterval", "1");
        engine.init();
        // Initialize my template repository. You can replace the "Hello $w" with your String.
        StringResourceRepository repo = (StringResourceRepository) engine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
        repo.putStringResource("message", contenuMessage);
        // Set parameters for my template.
        VelocityContext context = new VelocityContext();
        //VelocityContext context = new VelocityContext();
        for (Map.Entry m : keyVal.entrySet()) {

            context.put("" + m.getKey(), m.getValue());
        }
        // Get and merge the template with my parameters.
        Template template = engine.getTemplate("message");
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        // Show the result.
        return writer.toString();
    }

    private String velocityWithStringTemplate(String contenuMessage, Operation operation, BigDecimal solde, String codeRetrait) {
        // Initialize the engine.
        VelocityEngine engine = new VelocityEngine();
        //Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new NullLogChute());

        engine.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, "org.apache.velocity.runtime.log.Log4JLogChute");

        engine.setProperty(Velocity.RESOURCE_LOADER, "string");
        engine
                .addProperty("string.resource.loader.class", StringResourceLoader.class
                        .getName());
        engine.addProperty("string.resource.loader.repository.static", "false");
        //  engine.addProperty("string.resource.loader.modificationCheckInterval", "1");
        engine.init();
        // Initialize my template repository. You can replace the "Hello $w" with your String.
        StringResourceRepository repo = (StringResourceRepository) engine.getApplicationAttribute(StringResourceLoader.REPOSITORY_NAME_DEFAULT);
        repo.putStringResource("message", contenuMessage);
        // Set parameters for my template.
        VelocityContext context;
        context = populateMessage(operation, solde, codeRetrait);
        // Get and merge the template with my parameters.
        Template template = engine.getTemplate("message");
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        // Show the result.
        return writer.toString();
    }

    public void sendMessageAccountToAccount(Operation operation) {
        String telephone = null;
        Compte cpt;
        log.info("Envoi de l'sms de l'envoi account to account.");
        String message = "";
        BigDecimal soldeDestinataire = BigDecimal.ZERO;
        BigDecimal soldeEmetteur = BigDecimal.ZERO;
        String codeIndicatif;
        /**
         * ***************************destinataire************************************
         */
        if (operation.getCompteDest() != null) {
            if (operation.getCompteDest().getCompteAutre() != null) {
                cpt = compteService.findCompteOne(operation.getCompteDest().getId());
                soldeDestinataire = cpt.getCompteAutre().getSolde();
            }
        }

        /**
         * ***************************emetteur********************************
         */
        if (operation.getCompteSrc().getCompteAutre() != null) {
            cpt = compteService.findCompteOne(operation.getCompteSrc().getId());
            soldeEmetteur = cpt.getCompteAutre().getSolde();;
        }
        if (operation.getTypeService().getLibelle().equals(TypeServiceLibelle.ACCOUNT_ACCOUNT.name())) {
            telephone = "+" + operation.getEmetteur().getPersonne().getCodePaysTelephone() + operation.getEmetteur().getPersonne().getTelephone();
            message = getContenuAccSource(operation, soldeEmetteur);
            codeIndicatif = operation.getEmetteur().getPersonne().getCodePaysTelephone();
            sendmsg(telephone, message, codeIndicatif);
            message = getContenuAccDest(operation, soldeDestinataire);
            telephone = "+" + operation.getBeneficaire().getPersonne().getCodePaysTelephone() + operation.getBeneficaire().getPersonne().getTelephone();
            codeIndicatif = operation.getBeneficaire().getPersonne().getCodePaysTelephone();
            sendmsg(telephone, message, codeIndicatif);
        } else if (operation.getTypeService().getLibelle().equals(TypeServiceLibelle.ACCOUNT_CASH.name())) {
            telephone = "+" + operation.getEmetteur().getPersonne().getCodePaysTelephone() + operation.getEmetteur().getPersonne().getTelephone();
            codeIndicatif = operation.getEmetteur().getPersonne().getCodePaysTelephone();
            message = getContenuAccSource(operation, soldeEmetteur);
            sendmsg(telephone, message, codeIndicatif);

        }

    }

    /**
     * Envoi de l'email contenant les informations du transfert
     *
     * @param operation
     * @param codeRetrait
     */
    public void sendEmailWithCodeRetrait(Operation operation, String codeRetrait) {
        log.info("Send EMail");
        if (operation.getEmetteur().getPersonne().getEmail() != null) {
            String contenu = getContenu(operation, codeRetrait);
            String subject = "[PTI] Code retrait";
            //  mailService.sendEmail("da.santouro@gmail.com", subject, contenu, false, false);
            if (operation.getEmetteur() != null) {
                apiService.sendMail(operation.getEmetteur().getPersonne().getEmail(), contenu);

            }

        }
    }

    /**
     * Formatage du message Ã  envoyer
     *
     * @param operation
     * @param codeRetrait
     * @return
     */
    public String getContenu(Operation operation, String codeRetrait) {
        String contenu = "Transfert d'argent reussi" + "\n";
        BigDecimal total = Util.getZeroIfNull(operation.getMontantFrais()).add(Util.getZeroIfNull(operation.getMontantOp())).add(Util.getZeroIfNull(operation.getMontantTaxe()));
        contenu += "Montant:" + operation.getMontantOp() + "\n";
        contenu += "Frais:" + Util.getZeroIfNull(operation.getMontantFrais()).add(Util.getZeroIfNull(operation.getMontantTaxe())) + " Total:" + total + " Date:" + Util.format(operation.getDateOperation(), "dd-MM-yyyy  HH:mm" + "\n");
        contenu += "TID: " + operation.getCodeOperation();

        contenu += "\n" + "Code de transfert " + codeRetrait + " valide pour " + operation.getDuree() + "jours." + "Communiquez ce code au beneficiaire.";
        return contenu;
    }

    public int getDureeExpire() {
        ParametreApp nombreJours = parametreAppRepository.findByTypeAndNom(TypeParametre.AUTRE, "DELAI_EXPIRED_CODE");

        int nbreJours = 0;
        if (nombreJours != null) {
            nbreJours = Integer.parseInt(nombreJours.getValeur());
        }
        return nbreJours;
    }

    /**
     * Permet de renvoyer le code de retrait d'une operation
     *
     * @param operationDTO
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Async("taskExecutor")
    public OperationDTO sendNewCodeRetrait(OperationDTO operationDTO) throws NoSuchAlgorithmException {

        if (operationDTO != null) {
            Operation operation = operationRepository.findById(operationDTO.getId()).orElse(null);
            if (operation.getStatut().equals(StatutOperation.ENATTENTE)) {
                String otps[] = RandomUtil.generateOtp();

                operation.secret(otps[0])
                        .commentaire(Base64.encodeBase64String(otps[1].getBytes()))
                        .codeRetrait(RandomUtil.getMd5Code(otps[1]))
                        .dateExpiration(LocalDate.now().plusDays(getDureeExpire()));
                operation.setCodeSecret(otps[1]);
                operationRepository.save(operation);
                //  sendMessage(operation, Boolean.TRUE, otps[1]);
                sendEmailWithCodeRetrait(operation, otps[1]);
                log.info("Code de retrait final" + otps[1]);
                return operationMapper.toDto(operation);
            } else {
                throw new CustomParameterizedException("Operation dejÃ  traitÃ© .");
            }
        }
        throw new CustomParameterizedException("Operation invalide .");
    }

    /**
     * Verifie que l'organisation de l'utilisateur connecte peut faire un type
     * de service
     *
     * @param typeService
     * @return
     */
    public Boolean verifAutorisationOfTypeService(TypeService typeService) {
        log.info("Verification de l'autorisation par service ..");
        if (typeService != null) {
            UniteOrganisation uniteOrganisation = uniteOrganisationService.getUoOfUserConnecte();
            if (autorisationService.hasAutorisation(uniteOrganisation, typeService)) {
                return true;
            } else {
                throw new CustomParameterizedException("Vous ne pouvez pas effectuer ce type de service.");
            }
        } else {
            throw new CustomParameterizedException("Type de service introuvable.");
        }
    }

    public Boolean verifAutorisationOfTypeService(TypeService typeService, UniteOrganisation uniteOrganisation) {
        UniteOrganisation uo = uniteOrganisationService.findOneById(uniteOrganisation.getId());
        //UniteOrganisation uo = uniteOrganisationService.findByCode(uniteOrganisation.getCodeUO());
        if (uo != null) {
            if (typeService != null) {
                if (autorisationService.hasAutorisation(uniteOrganisation, typeService)) {
                    return true;
                } else {
                    throw new CustomParameterizedException("Vous ne pouvez pas effectuer ce type de service.");
                }
            } else {
                throw new CustomParameterizedException("Type de service introuvable.");
            }
        } else {
            throw new CustomParameterizedException("impossible d'effectuer cette opération.");
        }

    }

    public List<Autorisation> getListAutorisation(String codeUo) {

        return autorisationService.findByCodeUO(codeUo);

    }

    /**
     * Annulation d'une operation de retrait
     *
     * @param operationDTO
     * @return
     */
    public OperationDTO annulationOperationRetrait(OperationDTO operationDTO) {
        if (operationDTO != null) {
            Operation operation = operationRepository.findById(operationDTO.getId()).orElse(null);
            if (operation != null) {
                if (operation.getStatut().equals(StatutOperation.ENCOURS)) {
                    operation.setStatut(StatutOperation.ENATTENTE);
                    operation = operationRepository.save(operation);
                    operationAuditService.saveOpAudit(operation, StatutOperation.ENCOURS, StatutOperation.ENATTENTE);
                    return operationMapper.toDto(operation);
                }
                return operationDTO;
            }
        }
        return operationDTO;
    }

    /**
     * @param operation
     */
    public void repartitionCommissionCompensateur(Operation operation) throws IOException {
        log.info("Repartition des commissions du compensateur");
        BigDecimal montant = repartitionOpService.getMontantByActeurAndOp(Acteur.COMP, operation.getId());
        if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
            UniteOrganisation uoEm = uniteOrganisationService.findOneById(operation.getUoEmetteur().getId());
            FseDTO fseDTO = apiService.getFseCompense(uoEm.getCodeFse());
            if (fseDTO != null) {
                UniteOrganisation fseCompense = uniteOrganisationService.findFseByCode(fseDTO.getCodeFse());
                UniteOrganisation agenceSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(fseCompense);
                Commission commission = new Commission();
                commission.estPaye(Boolean.FALSE)
                        .estComptabilise(Boolean.FALSE)
                        .montant(montant)
                        .organisation(agenceSiege)
                        .operation(operation)
                        .dateEnregistrement(ZonedDateTime.now())
                        .etat(EtatValidite.VALIDE)
                        .acteur(Acteur.COMP);
                commissionRepository.save(commission);
            } else {
                throw new CustomParameterizedException("Le Compensateur de " + uoEm.getLibelleUniteOrganisation() + "est introuvable.");
            }
        }
    }

    /**
     * Repartition des frais entre les organisations emetteur de l'operation
     * d'ENVOI_CASH -Recuperation de la partEm (en fonction du type de service
     * de l'operation -Pour chaque UO de la chaine de l'emetteur on calcul sa
     * commission
     */
    private LigneRepartitionCommission getLigneRepartition(Operation operation, List<LigneRepartitionCommission> lignesComm, UniteOrganisation uo) {
        BigDecimal baseCalcul;

        for (LigneRepartitionCommission lc : lignesComm) {
            if (lc.getSurFrais().equals(Boolean.TRUE)) {
                if (lc.getTypeIntervalle() != null & lc.getTypeIntervalle().equals(TypeIntervalle.FRAIS)) {
                    baseCalcul = Util.getZeroIfNull(operation.getMontantFrais());
                } else {
                    baseCalcul = Util.getZeroIfNull(operation.getMontantOp());
                }

            } else {
                baseCalcul = Util.getZeroIfNull(operation.getMontantOp());
            }

            if (Util.isBiggerOrEqualThan(baseCalcul, lc.getMin()) && Util.isBiggerOrEqualThan(lc.getMax(), baseCalcul) && (lc.getNiveau().equals(uo.getTypeUO().getNiveau()))) {
                return lc;
            }

        }
        return null;

    }

    private List<TaxeOperationDTO> calculTaxe(Long organisationId, String typeServiceLibelle, BigDecimal montantFrais, BigDecimal montant, UniteOrganisation uoSource) {
        return taxeOperationService.searchTaxeOperation(organisationId, typeServiceLibelle, montantFrais, montant, uoSource);
    }

    public void repartitionCommission(Operation operation, UniteOrganisation uoBeneficiaire, MontantsAComptabiliser typeFrais) {
        Operation op = operation;
        //*pour le cas des du retrait recupérer l'opération d'envoi
        if (operation.getTypeService().getCode().equals(TypeServiceLibelle.RETRAIT.toString())) {
            op = operation.getOperationTraite();
        }
        if (op.getMontantFrais() != null) {
            if (Util.isBiggerThan(Util.getZeroIfNull(op.getMontantFrais()), BigDecimal.ZERO)) {
                log.info("Repartition des commissions entre les acteurs");
                ArrayList<Long> acteurs = new ArrayList<>();
                List<TableauRepartitionCommission> tableauRepartitionCommission = new ArrayList<>();
                uoBeneficiaire = uniteOrganisationService.findOneById(uoBeneficiaire.getId());
                TypeUniteOrganisation typeUniteOrganisa = typeUniteOrganisationService.findByOne(uoBeneficiaire.getTypeUO().getId());
                tableauRepartitionCommission = tableauRepartitionCommissionRepository.findTableauRepartitionCommissionByMultiParams(operation.getTypeService().getId(), typeUniteOrganisa.getNiveau(), uoBeneficiaire.getProfilActeur().getId());
                if (tableauRepartitionCommission.isEmpty()) {

                    UniteOrganisation uoPere = uniteOrganisationService.findOneById(uoBeneficiaire.getPereUO().getId());
                    uniteOrganisationService.findAllUoParentsIdsCanDoOperation(uoPere, acteurs);
                    for (Long acteurId : acteurs) {
                        UniteOrganisation org = uniteOrganisationService.findOneById(acteurId);
                        tableauRepartitionCommission = tableauRepartitionCommissionRepository.findTableauRepartitionCommissionByMultiParams(operation.getTypeService().getId(), typeUniteOrganisa.getNiveau(), org.getProfilActeur().getId());
                        if (tableauRepartitionCommission != null && !tableauRepartitionCommission.isEmpty()) {
                            break;
                        }
                    }
                }
                if (!tableauRepartitionCommission.isEmpty()) {
                    TableauRepartitionCommission tableau = tableauRepartitionCommission.get(0);
                    List<LigneRepartitionCommission> lignes = ligneRepartitionCommissionRepository.findByTableauRepartitionCommissionId(tableau.getId());
                    if (!lignes.isEmpty()) {
                        BigDecimal montant;
                        BigDecimal montantTaxe;
                        BigDecimal montantARepartirRep;
                        ArrayList<UniteOrganisation> uos = new ArrayList<>();
                        uniteOrganisationService.findAllUoParents(uoBeneficiaire, uos);
                        if (!uos.isEmpty()) {
                            for (UniteOrganisation uo : uos) {
                                montantTaxe = BigDecimal.ZERO;

                                List<LigneRepartitionCommission> ligneOfUo = lignes.stream().filter(ligneRepartitionCommission -> ligneRepartitionCommission.getNiveau().equals(uo.getTypeUO().getNiveau())).collect(Collectors.toCollection(ArrayList::new));
                                /*ligne correspondante Ã  la repartition */
                                LigneRepartitionCommission lignecomOfUo = getLigneRepartition(operation, ligneOfUo, uo);

                                if (lignecomOfUo != null) {
                                    Commission commission = new Commission();

                                    if (typeFrais.equals(MontantsAComptabiliser.FRAISEMT)) {
                                        montantARepartirRep = repartitionOpService.getMontantByActeurAndOp(Acteur.EM, op.getId());
                                        commission.acteur(Acteur.EM);
                                    } else {
                                        /*Pour le cas du retrait rechercher l'opération utilisé est l'opération de retrait*/
                                        montantARepartirRep = repartitionOpService.getMontantByActeurAndOp(Acteur.ACQ, op.getId());
                                        commission.acteur(Acteur.ACQ);
                                    }
                                    if (Util.isBiggerThan(montantARepartirRep, BigDecimal.ZERO)) {// si part Em ou acq
                                        if (lignecomOfUo.getSurFrais().equals(Boolean.TRUE)) {
                                            montant = Util.getZeroIfNull(op.getMontantFrais());
                                        } else {
                                            montant = Util.getZeroIfNull(op.getMontantOp());
                                        }
                                    } else {
                                        montant = BigDecimal.ZERO;
                                    }

//
                                    if (Util.isBiggerThan(montant, BigDecimal.ZERO)) {
                                        BigDecimal valeurCommission = BigDecimal.ZERO;
                                        if (lignecomOfUo.getTypeTarif().equals(TypeTarif.FIXE)) {
                                            valeurCommission = lignecomOfUo.getSomme();
                                        } else if (lignecomOfUo.getTypeTarif().equals(TypeTarif.POURCENTAGE)) {

                                            valeurCommission = Util.pourcentage(montant.setScale(0, RoundingMode.HALF_UP), BigDecimal.valueOf(lignecomOfUo.getValeur()));
                                        }
                                        List<TaxeOperationDTO> taxeOperations = calculTaxe(operation.getUoAcquereur().getId(),
                                                operation.getTypeService().getLibelle(),
                                                valeurCommission, operation.getMontantOp(), operation.getPointVente().getUniteOrganisation());
                                        if (taxeOperations != null) {
                                            for (TaxeOperationDTO taxeOperation : taxeOperations) {
                                                if (!taxeOperation.getAppliqueSur().equals(MontantsAComptabilise.LIBRE)) {
                                                    montantTaxe = montantTaxe.add(Util.getZeroIfNull(taxeOperation.getMontant()));
                                                }

                                            }
                                            /**
                                             * Lorsque le calcul est sur la base
                                             * des frais plus besoin de
                                             * retrancher les taxes
                                             */
                                            if (lignecomOfUo.getSurFrais().equals(Boolean.FALSE)) {
                                                valeurCommission = valeurCommission.subtract(montantTaxe);
                                            }

                                        }
                                        if (Util.isBiggerThan(valeurCommission, BigDecimal.ZERO)) {
                                            commission.estPaye(Boolean.FALSE)
                                                    .estComptabilise(Boolean.FALSE)
                                                    .montant(valeurCommission)
                                                    .taxe(montantTaxe)
                                                    .organisation(uo)
                                                    .operation(operation)
                                                    .etat(EtatValidite.VALIDE)
                                                    .dateEnregistrement(ZonedDateTime.now());

                                            commission.setEnTraitement(Boolean.FALSE);
                                            commissionRepository.save(commission);
                                        }

                                    }
                                }
                            }
                        } else {
                            throw new CustomParameterizedException("ParamÃ©trage incorrecte, Contactez l'administrateur.");
                        }
                    } else {
                        throw new CustomParameterizedException("ParamÃ©trage incorrecte, Contactez l'administrateur.");
                    }
                }

            }

        }

    }

    /**
     * Calcul commission en fonction des acteurs
     */
    @Transactional
    public Operation calculRepartitionFrais(Operation operation, Long fseSrceId, Long fseDestId) {
        BigDecimal frais = operation.getMontantFrais();
        if (Util.isBiggerThan(frais, BigDecimal.ZERO)) {
            TypeService typeService = operation.getTypeService();

            Acteur[] acteurs = Acteur.class
                    .getEnumConstants();
            for (Acteur acteur : acteurs) {
                RepartitionFrais repartionFrais = null;

                if (typeService.isOperationClient() == null || !typeService.isOperationClient()) {
                    repartionFrais = repartitionFraisService.findByCriteria(operation.getTypeService().getId(), acteur, operation.getMontantFrais(), operation.getMontantOp());
                } else if (fseDestId != null && fseSrceId != null) {
                    repartionFrais = repartitionFraisService.findByCriteriaAndFse(operation.getTypeService().getId(), acteur, operation.getMontantFrais(), operation.getMontantOp(), fseSrceId, fseDestId);
                }
                if (repartionFrais != null) {
                    BigDecimal baseCalcul;

                    BigDecimal montantPart;
                    if (repartionFrais.getTypeTarif().equals(TypeTarif.POURCENTAGE)) {
                        /*determination de la base de calcul en fonction du param_tre*/
                        if (repartionFrais.getSurFrais()) {

                            baseCalcul = operation.getMontantFrais().setScale(0, RoundingMode.HALF_UP);
                        } else {
                            baseCalcul = operation.getMontantOp();
                        }
                        montantPart = Util.pourcentage(baseCalcul, BigDecimal.valueOf(repartionFrais.getValeur()));
                    } else {
                        montantPart = BigDecimal.valueOf(repartionFrais.getValeur());
                    }

                    if (Util.isBiggerThan(montantPart, BigDecimal.ZERO)) {
                        repartitionOpService.save(acteur, montantPart, operation);
                    }
                }
            }
        }
        return operation;
    }

    /**
     * Enregistrement ou mise Ã  jour des informations d'un beneficiaure ou d'un
     * emetteur
     * Personne,PersonnePhysique,PersonneMorale,Piece,Client,Compte,CompteBanque
     */
    @Transactional
    private Operation saveInfosPersonne(PersonneAllDTO personneAllDTO, String type, Operation operation, boolean estBeneficiare) {
        Personne personne = personneMapper.toEntity(personneAllDTO.getPersonneDTO());
        Piece piece;
        Boolean newPersonne = true;
        //Traitement du document d'identification
        if (personneAllDTO.getPieceDTO() != null
                && personneAllDTO.getPieceDTO().getReferencePiece() != null) {
            piece = pieceMapper.toEntity(personneAllDTO.getPieceDTO());
            Optional<Piece> pieceExist = pieceRepository.findTopByTypePieceIdAndReferencePiece(personneAllDTO.getPieceDTO().getTypePieceId(), personneAllDTO.getPieceDTO().getReferencePiece());
            if (pieceExist.isPresent()) {
                piece.setId(pieceExist.get().getId());
            }
        } else {
            piece = null;
        }

        PersonnePhysique personnePhysique = personnePhysiqueMapper.toEntity(personneAllDTO.getPersonnePhysiqueDTO());
        PersonneMorale personneMorale = personneMoraleMapper.toEntity(personneAllDTO.getPersonneMoraleDTO());

        if (personne.getId() != null) { //Mise Ã  jour des infos existants de la personne
            personne = personneRepository.save(personne);
            if (personne.getTypePersonne().equals(TypePersonne.PHYSIQUE)) {
                personnePhysiqueRepository.save(personnePhysique);
            } else {
                personneMoraleRepository.save(personneMorale);
            }
            if (piece != null) {
                piece.setTitulaire(personne);
                piece = pieceRepository.save(piece);
            }
            newPersonne = false;
        } else {
            String telephone = personne.getTelephone();
            String code = personne.getCodePaysTelephone();
            if (telephone != null) {//Recherche d'une personne Ã  partir de son telephone
                Optional<Personne> searchPersonne = personneRepository.findTop1ByTelephoneAndCodePaysTelephone(telephone, code);//Inclure le code
                if (searchPersonne.isPresent()) {
                    Personne p = searchPersonne.get();
                    PersonnePhysique physique = p.getPersonnePhysique();
                    PersonneMorale morale = p.getPersonneMorale();
                    /*List<Piece> pieceExistantList = pieceRepository.findByTitulaireId(p.getId());
                    if (pieceExistantList != null && piece != null) {
                        for (Piece pieceExistant : pieceExistantList) {
                            //USE TYPE for verification
                            if (pieceExistant.getReferencePiece().equals(piece.getReferencePiece())
                                    && pieceExistant.getTypePiece().getId().equals(piece.getTypePiece().getId())) {
                                piece.setId(pieceExistant.getId());
                            }
                        }
                    }*/
                    personne.setId(p.getId());
                    if (morale != null) {
                        personneMorale.setId(morale.getId());
                    } else {
                        personnePhysique.setId(physique.getId());
                    }
                    newPersonne = false;
                }
            }

            if (personne.getTypePersonne().equals(TypePersonne.PHYSIQUE)) { //PersonnePhysiqye
                personnePhysique = personnePhysiqueRepository.save(personnePhysique);
                personne.setPersonnePhysique(personnePhysique);
                personne = personneRepository.save(personne);

                if (piece != null) {
                    piece.setTitulaire(personne);
                    piece = pieceRepository.save(piece);
                } else {
                }
            } else { //PersonneMorale
                personneMorale = personneMoraleRepository.save(personneMorale);
                personne.setPersonneMorale(personneMorale);
                personne = personneRepository.save(personne);
                if (piece != null) {
                    piece.setTitulaire(personne);
                    piece = pieceRepository.save(piece);
                }
            }
        }
        if (type.equals("EMETTEUR")) {
            Emetteur emetteurExistant = emetteurRepository.findByPersonneId(personne.getId());
            Emetteur emetteur = new Emetteur();
            // verifPersonneAutorisation(personne, newPersonne);
            personne.setActivated(Boolean.TRUE);
            personne = personneRepository.save(personne);
            if (emetteurExistant == null) { //Emetteur de l'operation pas encore enregistre
                emetteur.setPersonne(personne);
                emetteur = emetteurRepository.save(emetteur);
                operation.setEmetteur(emetteur);
                if (estBeneficiare) {//l'emetteur est le beneficiaire
                    setOpBeneficiaire(operation, personne);
                }
            } else {
                operation.setEmetteur(emetteurExistant);
                if (estBeneficiare) {
                    setOpBeneficiaire(operation, personne);
                }
            }
            if (piece != null) {
                operation.setPieceEmetteur(piece);
            }
        } else {
            setOpBeneficiaire(operation, personne);
            if (piece != null) {
                operation.setPieceBeneficiaire(piece);
            }
        }
        //Enregistrement des informations du client et des comtpes
        enregistrementInfoComptes(personneAllDTO, type, operation, piece, personne);
        return operation;
    }

    /**
     * @param personne
     * @return
     */
    private void verifPersonneAutorisation(Personne personne, Boolean newPersonne) {
        if (!personne.isActivated() && !newPersonne) {
            throw new CustomParameterizedException("Le client n'est pas autorisÃ© Ã  faire des opÃ©rations." + personne.isActivated());
        }
    }

    /**
     * *
     * Association du beneficiaire de l'operation
     *
     * @param operation
     * @param personne
     */
    private void setOpBeneficiaire(Operation operation, Personne personne) {
        Beneficiaire beneficiaire = new Beneficiaire();
        Beneficiaire beneficiaireExistant = beneficiaireRepository.findByPersonneId(personne.getId());
        if (beneficiaireExistant == null) { //La personne est deja emetteur
            beneficiaire.setPersonne(personne);
            beneficiaire = beneficiaireRepository.save(beneficiaire);
            operation.setBeneficaire(beneficiaire);
        } else {
            operation.setBeneficaire(beneficiaireExistant);
        }
    }

    /**
     * *
     * Enregistrement des informations du client et de ses comptes
     *
     * @param personneAllDTO
     * @param type
     * @param operation
     */
    @Transactional
    private void enregistrementInfoComptes(PersonneAllDTO personneAllDTO, String type, Operation operation, Piece piece, Personne personne) {
        Client client;

        Compte compte = new Compte();
        CompteCBI compteCBI = personneAllDTO.getCompteCBIDTO();
        if (compteCBI != null) {
            //Informations d'un compte de client souscrit
            if (compteCBI.getCompteDTO() != null && compteCBI.getCompteDTO().getId() != null) {
                compte = compteCBI.getCompteDTO();
            } else if (compteCBI.getType() != null) {
                if (compteCBI.getType().equals("Banque")) {
                    Client clientDTO = compteCBI.getClientDTO();
                    Compte compte1 = compteService.getCompteExistant(compteCBI.getNumeroCompte(), personne);

                    if (compte1 != null) {
                        compte = compte1;
                    } else {
                        clientDTO.setPersonne(personne);
                        client = clientRepository.save(clientDTO);
                        CompteBanque compteBanque = compteCBI.getCompteBanqueDTO();
                        compteBanque = compteBanqueRepository.save(compteBanque);

                        compte = compteCBI.getCompteDTO();
                        compte.setClient(client);
                        compte.setCompteBanque(compteBanque);
                        compteRepository.save(compte);
                    }
                } else {//Compte virtuel
                    List<Compte> compteExistant = compteRepository.findByValeurCompte(compteCBI.getNumeroCompte());
                    if (!compteExistant.isEmpty()) {
                        compte = compteExistant.get(0);
                    } else {
                        CompteAutre compteAutre = new CompteAutre();
                        compteAutre.solde(BigDecimal.ZERO);
                        if (compteCBI.getCodeFse() != null) {
                            UniteOrganisation teneurCompte = uniteOrganisationService.findOneById(compteCBI.getCodeFse());
                            if (teneurCompte != null) {
                                compteAutre.setCodeFse(teneurCompte.getCodeFse());
                            }
                        }
                        compteAutre = compteAutreRepository.save(compteAutre);
                        Client clientAutre = new Client();
                        clientAutre.setCodeClient(RandomUtil.generateAlphaNumeric(5));
                        clientAutre.setPersonne(personne);
                        clientAutre = clientRepository.save(clientAutre);

                        //Enregistrement d'un nouveau compte
                        compte.valeurCompte(compteCBI.getNumeroCompte())
                                .categorieCompte(CategorieCompte.AUTRE)
                                .etiquette(String.valueOf(EtiquetteCompteCode.CPTECLIENT))
                                .compteAutre(compteAutre)
                                .client(clientAutre);
                        compte = compteRepository.save(compte);
                    }
                }
            }
            if (type.equals("EMETTEUR")) {
                operation.compteSrc(compte);
            } else {
                operation.compteDest(compte);
            }
        }
    }

    /**
     * Get all the operations.
     *
     * @param pageable the pagination information
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<OperationDTO> findAll(Pageable pageable) {
        return operationRepository.findByPointVente(pointVenteService.getPointVenteOfUserConnecte(), pageable)
                .map(operationMapper::toDto);
    }

    private void getCompteCbis(PersonneAllDTO personne, Compte compte) {
        CompteCBI compteCBI = new CompteCBI();
        List<CompteCBI> compteCBIS = new ArrayList<>();
        if (compte.getCompteBanque() != null) {
            CompteBanque compteBanque = compte.getCompteBanque();
            compteCBI.setCodeBanque(compteBanque.getCodeBanque());
            compteCBI.setCodeAgence(compteBanque.getCodeAgence());
            compteCBIS.add(compteCBI);
            compteCBI.setNumeroCompte(compte.getValeurCompte());
            compteCBI.setType("Banque");
        } else {
            compteCBI.setType("Autre");
            compteCBI.setNumeroCompte(compte.getValeurCompte());
        }
        personne.setCompteCBIDTO(compteCBI);
    }

    /**
     * Delete the operation by id.
     *
     * @param id the id of the entity
     */
    @Transactional
    public void delete(Long id) {
        operationRepository.deleteById(id);
    }

    /**
     * Verification des autorisations de l'utilisateur
     *
     * @param user
     */
    public void userCanDoOperations(User user) {
        if (user != null && user.getPointVente() == null && user.getPointVente().getId() == null) {
            throw new CustomParameterizedException("Vous n'êtes pas chargé de la gestion d'un point de vente .");
        } else {

        }
    }

    public void userCanValideOperations(Operation operation, User user) {
        if (operation.getUser() != null) {
            if (user != null && user.getId().equals(operation.getUser().getId())) {
                throw new CustomParameterizedException("Vous ne pouvez pas valider une opération que vous avez initié.");
            }
        }

    }

    /**
     * *************************************************************VERIFICATION
     * CUMUL DES
     * OPERATIONS********************************************************************
     */
    /**
     * Get dates of week
     */
    private List<LocalDate> getDatesOfWeek() {
        LocalDate today = LocalDate.now();
        int day = today.getDayOfWeek().getValue();
        LocalDate firstDayOfWeek = today.minusDays(day - 1L);
        LocalDate endDayOfWeek = firstDayOfWeek.plusDays(day);
        return new ArrayList<>(Arrays.asList(firstDayOfWeek, endDayOfWeek));
    }

    /**
     * @return Get dates of month
     */
    private List<LocalDate> getDatesOfMonth() {
        LocalDate today = LocalDate.now();
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate endDayOfMonth = firstDayOfMonth.withDayOfMonth(today.lengthOfMonth());
        return new ArrayList<>(Arrays.asList(firstDayOfMonth, endDayOfMonth));
    }

    /**
     * Verification avant la validation de l'opÃ©ration
     *
     * @param operationAllDTO
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public TableauCumulDTO post(OperationAllDTO operationAllDTO) {
        User userConnecte = userService.getUserWithAuthorities();
        userCanDoOperations(userConnecte);
        UniteOrganisation uniteOrganisationSource = userConnecte.getUniteOrganisation();
        Canal canal = getCanal(operationAllDTO.getOperationDTO());
        UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.PAYS);
        TypeService typeService = typeServiceService.findByCodeService(operationAllDTO.getTypeService());
        Personne emetteur = new Personne();
        //Recherche de la  personne effectuant l'operaiton
        if (operationAllDTO.getEmetteur() != null) {
            String telephone = operationAllDTO.getEmetteur().getPersonneDTO().getTelephone();
            String code = operationAllDTO.getEmetteur().getPersonneDTO().getCodePaysTelephone();
            if (telephone != null && code != null) {
                emetteur = personneRepository.findTopByTelephoneAndCodePaysTelephoneAndActivatedIsTrue(telephone, code);
            }
        }

        TableauCumulDTO tab;
        OperationDTO operationDTO = operationAllDTO.getOperationDTO();
        ProfilActeur profilActeur = uniteOrganisationSource.getProfilActeur();
        Optional<ProfilActeur> profilClient = profilActeurRepository.findByTypeAndActiveTrue(TypeProfilActeur.CLIENT);
        if (!typeService.isOperationClient()) {
            tab = getTableauCumulDTO(uniteOrganisationSource.getId(),
                    typeService.getId(),
                    canal.getId(),
                    null,
                    null,
                    uoPays.getId(),
                    operationDTO.getUoAcquereurId(), TypeRegle.ACTEUR,
                    operationDTO.getMontantOp(), operationDTO.getMontantOp(), profilActeur.getId(), null, null);
        } else {
            Long fseDestId = operationDTO.getUoAcquereurId();
            UniteOrganisation fseDest = uniteOrganisationService.findOneById(fseDestId);//Fse Dest
            UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq
            tab = getTableauCumulDTO(uniteOrganisationSource.getId(),
                    typeService.getId(),
                    canal.getId(),
                    null,
                    null,
                    uoPays.getId(),
                    uoPaysDest.getId(), TypeRegle.ACTEUR,
                    operationDTO.getMontantOp(), operationDTO.getMontantOp(), profilActeur.getId(), null, null);
        }

        if (tab == null && profilClient.isPresent()) {
            if (emetteur != null) {
                Client client = clientRepository.findTop1ByPersonneIdAndSupprimeIsFalse(emetteur.getId());
                if (client != null) {
                    return getTableauCumulDTO(null, typeService.getId(), canal.getId(), null, client.getId(), uoPays.getId(),
                            operationDTO.getUoAcquereurId(), TypeRegle.CLIENT,
                            operationDTO.getMontantOp(), operationDTO.getMontantOp(), profilClient.get().getId(), null, emetteur.getId());
                } else {
                    return getTableauCumulDTO(null, typeService.getId(), canal.getId(), null, null, uoPays.getId(),
                            operationDTO.getUoAcquereurId(), TypeRegle.CLIENT,
                            operationDTO.getMontantOp(), operationDTO.getMontantOp(), profilClient.get().getId(), null, emetteur.getId());
                }
            } else {
                return getTableauCumulDTO(null, typeService.getId(), canal.getId(), null, null, uoPays.getId(),
                        operationDTO.getUoAcquereurId(), TypeRegle.CLIENT,
                        operationDTO.getMontantOp(), operationDTO.getMontantOp(), profilClient.get().getId(), null, null);
            }
        } else {
            return tab;
        }
    }

    public boolean verifIfValidateur(User user, Long typeServiceId, BigDecimal montant) {

        List<RoleValidation> roleValide = rechercherRoleValidateur(typeServiceId, montant);
        Set<Authority> role = user.getAuthorities();
        if (!roleValide.isEmpty()) {//BLOQUANT est prioritaire
            for (RoleValidation v : roleValide) {
                if (role.contains(v.getRole())) {
                    return true;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    private List<RoleValidation> rechercherRoleValidateur(Long typeServiceId, BigDecimal montant) {
        List<RoleValidation> taleauRoleValidation = roleValidationService.findByTypeService(typeServiceId);
        List<RoleValidation> taleauRoleValidationRet = new ArrayList();
        if (!taleauRoleValidation.isEmpty()) {//BLOQUANT est prioritaire
            for (RoleValidation t : taleauRoleValidation) {
                switch (t.getOperateur()) {
                    case EGALE:
                        if (t.getMontant().equals(montant)) {
                            taleauRoleValidationRet.add(t);
                        } else {
                            break;
                        }
                    case INFERIEUR:
                        if (Util.isBiggerThan(t.getMontant(), montant)) {
                            taleauRoleValidationRet.add(t);
                        } else {
                            break;
                        }
                    case SUPERIEUR:
                        if (Util.isBiggerThan(montant, t.getMontant())) {
                            taleauRoleValidationRet.add(t);
                        } else {
                            break;
                        }
                    case INFERIEUR_EGALE:
                        if (Util.isBiggerOrEqualThan(t.getMontant(), montant)) {
                            taleauRoleValidationRet.add(t);
                        } else {
                            break;
                        }
                    case SUPERIEUR_EGALE:
                        if (Util.isBiggerOrEqualThan(montant, t.getMontant())) {
                            taleauRoleValidationRet.add(t);
                        } else {
                            break;
                        }
                }
            }

        }
        return taleauRoleValidationRet;
    }

    /**
     * Verification des regles de validations des operations en fonction du
     * cumul et du seuil
     *
     * @param acteurId
     * @param typeServiceId
     * @param canalId
     * @param clientBeneficiaireId
     * @param clientEmetteurId
     * @param uoEmetteurId
     * @param uoAcqId
     * @param typeRegle
     * @param montant
     * @param soldeApresOp
     * @param profilId
     * @param cBeneficiaire
     * @param cEmeteurId
     * @return
     */
    public TableauCumulDTO getTableauCumulDTO(Long acteurId, Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
            Long uoEmetteurId, Long uoAcqId, TypeRegle typeRegle, BigDecimal montant, BigDecimal soldeApresOp, Long profilId, Long cBeneficiaire, Long cEmeteurId) {
        TableauCumulDTO tab;
        log.info("VERIFICATION DU MONTANT");
        tab = verifCumulPeriodique(acteurId, typeServiceId, canalId, cBeneficiaire, cEmeteurId, uoEmetteurId, uoAcqId,
                typeRegle, montant, Temps.MONTANT_OP, profilId);

        if (tab == null) {

            log.info("VERIFICATION DU SOLDE {}", soldeApresOp);
            tab = verifCumulPeriodique(acteurId, typeServiceId, canalId, cBeneficiaire, cEmeteurId, uoEmetteurId, uoAcqId,
                    typeRegle, soldeApresOp, Temps.SOLDE, profilId);
            if (tab == null) {
                log.info("VERIFICATION DU CUMUL JOURNALIER {}", montant);
                tab = verifCumulPeriodique(acteurId, typeServiceId, canalId, cBeneficiaire, cEmeteurId, uoEmetteurId, uoAcqId,
                        typeRegle, montant, Temps.JOURNALIER, profilId);
                if (tab == null) {//Verification HEBDOMADAIRE
                    log.info("VERIFICATION DU CUMUL HEBDOMADAIRE {}", montant);
                    tab = verifCumulPeriodique(acteurId, typeServiceId, canalId, cBeneficiaire, cEmeteurId, uoEmetteurId, uoAcqId,
                            typeRegle, montant, Temps.HEBDOMADAIRE, profilId);
                    if (tab == null) {//Verfication MENSUEL
                        log.info("VERIFICATION DU CUMUL MENSUEL {}", montant);
                        tab = verifCumulPeriodique(acteurId, typeServiceId, canalId, cBeneficiaire, cEmeteurId, uoEmetteurId, uoAcqId,
                                typeRegle, montant, Temps.MENSUEL, profilId);

                        return tab;
                    } else {
                        return tab;
                    }
                } else {
                    return tab;
                }
            }

        }
        return tab;
    }

    public TableauCumulDTO getTableauCumulVerifMontant(Long acteurId, Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
            Long uoEmetteurId, Long uoAcqId, TypeRegle typeRegle, BigDecimal montant, BigDecimal soldeApresOp, Long profilId, Long cBeneficiaire, Long cEmeteurId) {
        TableauCumulDTO tab;
        log.info("VERIFICATION DU MONTANT");
        tab = verifCumulPeriodique(acteurId, typeServiceId, canalId, cBeneficiaire, cEmeteurId, uoEmetteurId, uoAcqId,
                typeRegle, montant, Temps.MONTANT_OP, profilId);

        return tab;
    }

    /**
     * Verification du seuil pour le montant des transactions
     *
     * @param typeServiceId
     * @param canalId
     * @param clientBeneficiaireId
     * @param clientEmetteurId
     * @param uoEmetteurId
     * @param uoAcqId
     * @param typeRegle
     * @param montant
     */
    private TableauCumulDTO verifSeuil(TableauCumulDTO taleauCumulConfig, BigDecimal montant) {
        String message = "";
        boolean actionApplique = false;
        if (taleauCumulConfig != null) {//BLOQUANT est prioritaire
            TableauCumulDTO tcumul = new TableauCumulDTO();

            Operateur op;
            if (taleauCumulConfig.getOperateur() != null) {
                op = taleauCumulConfig.getOperateur();
            } else {
                op = Operateur.EGALE;
            }
            switch (op) {
                case EGALE:
                    if (taleauCumulConfig.getMontant().equals(montant)) {
                        message = "Montant ne doit pas Ãªtre Ã©gal Ã " + montant;
                        actionApplique = true;
                    }
                    break;
                case INFERIEUR:
                    if (Util.isBiggerThan(taleauCumulConfig.getMontant(), montant)) {
                        message = "Montant ne doit pas être inferieur  à " + taleauCumulConfig.getMontant();
                        actionApplique = true;
                    }
                    break;

                case SUPERIEUR:
                    if (Util.isBiggerThan(montant, taleauCumulConfig.getMontant())) {
                        message = "Montant ne doit pas être superieur  à  " + taleauCumulConfig.getMontant();
                        actionApplique = true;
                    }
                    break;

                case INFERIEUR_EGALE:
                    if (Util.isBiggerOrEqualThan(taleauCumulConfig.getMontant(), montant)) {
                        message = "Montant ne doit pas être inferieur  à " + montant;
                        actionApplique = true;

                    }
                    break;

                case SUPERIEUR_EGALE:
                    if (Util.isBiggerOrEqualThan(montant, taleauCumulConfig.getMontant())) {
                        message = "Montant ne doit pas être superieur ou égal  à " + montant;
                        actionApplique = true;

                    }
                    break;
            }
            if (actionApplique) {
                tcumul.setAction(taleauCumulConfig.getAction());
                tcumul.setMessage(message);
            }

            if (tcumul.getAction() != null) {
                return tcumul;
            } else {
                return null;
            }

        }
        return null;
    }

    private List<Action> verifSeuil(Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
            Long uoEmetteurId, Long uoAcqId, TypeRegle typeRegle, BigDecimal montant, Long profilId) {
        List<TableauCumulDTO> taleauCumulConfig = tableauCumulService.findByCriteria(typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmetteurId, uoAcqId, typeRegle, Temps.MONTANT_OP, profilId);
        if (!taleauCumulConfig.isEmpty()) {//BLOQUANT est prioritaire
            List<Action> actions = new ArrayList<>();
            for (TableauCumulDTO t : taleauCumulConfig) {
                switch (t.getOperateur()) {
                    case EGALE:
                        if (t.getMontant().equals(montant)) {
                            actions.add(t.getAction());
                        }
                        break;
                    case INFERIEUR:
                        if (Util.isBiggerThan(t.getMontant(), montant)) {
                            actions.add(t.getAction());
                        }
                        break;

                    case SUPERIEUR:
                        if (Util.isBiggerThan(montant, t.getMontant())) {
                            actions.add(t.getAction());
                        }
                        break;

                    case INFERIEUR_EGALE:
                        if (Util.isBiggerOrEqualThan(t.getMontant(), montant)) {
                            actions.add(t.getAction());
                        }
                        break;

                    case SUPERIEUR_EGALE:
                        if (Util.isBiggerOrEqualThan(montant, t.getMontant())) {
                            actions.add(t.getAction());
                        }
                        break;

                }
            }
            return actions.stream().sorted().collect(Collectors.toList());
        }
        return null;
    }

    private MResponse verifCumulPeriodiqueMobile(Long acteurId, Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
            Long uoEmetteurId, Long uoAcqId, TypeRegle typeRegle, BigDecimal montant, Temps temps, Long profilId) {
        List<LocalDate> dates = new ArrayList<>();
        List<TableauCumulDTO> taleauCumulConfig;
        MResponse mResponse = new MResponse();
        String messageMontant = "";
        String messageNombre = "";
        switch (temps) {//Verification en fonction de la periode
            case MENSUEL:
                dates = getDatesOfMonth();
                if (typeRegle.equals(TypeRegle.ACTEUR)) {
                    messageMontant = "Le montant max mensuel de l'opÃ©rateur est atteint!";
                    messageNombre = "Le nombre d'operation max mensuel de l'opÃ©rateur est atteint";
                } else {
                    messageMontant = "Le montant max mensuel du client est atteint!";
                    messageNombre = "Le nombre d'operation max mensuel du client  est atteint!";
                }

                break;
            case HEBDOMADAIRE:
                dates = getDatesOfWeek();
                if (typeRegle.equals(TypeRegle.ACTEUR)) {
                    messageMontant = "Le montant max hebdomadaire de l'opÃ©rateur est atteint!";
                    messageNombre = "Le nombre d'operation max hebdomadaire de l'opÃ©rateur est atteint!";
                } else {
                    messageMontant = "Le montant max hebdomadaire du client est atteint!";
                    messageNombre = "Le nombre d'operation max hebdomadaire  du client  est atteint!";
                }
                break;
            case JOURNALIER:
                dates.add(LocalDate.now());
                dates.add(LocalDate.now());
                if (typeRegle.equals(TypeRegle.ACTEUR)) {
                    messageMontant = "Le montant max journalier de l'opÃ©rateur est atteint!";
                    messageNombre = "Le nombre d'operation max journalier de l'opÃ©rateur est atteint!";
                } else {
                    messageMontant = "Le montant max journalier du client est atteint!";
                    messageNombre = "Le nombre d'operation max journalier  du client  est atteint!";
                }
                break;
        }
        if (!dates.isEmpty()) {
            taleauCumulConfig = tableauCumulService.findByCriteria(typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmetteurId, uoAcqId, typeRegle, temps, profilId);
            if (!taleauCumulConfig.isEmpty()) {
                for (TableauCumulDTO t : taleauCumulConfig) {
                    List<BilanOperation> bilanOperations = null;
                    Long typeSerId = null;
                    if (t.getTypeServiceId() != null) {
                        typeSerId = typeServiceId;
                    }
                    //Calcul du bilan des operations
                    if (!temps.equals(Temps.MONTANT_OP)) {
                        bilanOperations = null;

                    } else if (t.getOrganisationAcqId() == null && t.getOrganisationEmId() == null) {
                        bilanOperations = bilanOperationRepository.findByMultiParamsAndActeurBetweenDateDebutAndDateFinAll(acteurId, typeSerId, canalId, clientBeneficiaireId, uoEmetteurId, uoAcqId, dates.get(0), dates.get(1));
                    } else {
                        bilanOperations = bilanOperationRepository.findByMultiParamsAndActeurBetweenDateDebutAndDateFin(acteurId, typeSerId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmetteurId, uoAcqId, dates.get(0), dates.get(1));
                    }

                    if (!bilanOperations.isEmpty()) {
                        BigDecimal montantTotal = BigDecimal.valueOf(bilanOperations.stream().mapToDouble(b -> b.getMontant().doubleValue()).sum());
                        Integer nombreTotal = bilanOperations.stream().mapToInt(b -> b.getNombre().intValue()).sum();
                        if (Util.isBiggerThan(montantTotal.add(Util.getZeroIfNull(montant)), t.getMontant())) {
                            mResponse.setCode("0");
                            mResponse.setMessage(messageMontant);

                        }
                        if (t.getNombre() != null && (nombreTotal + 1 > t.getNombre())) {
                            mResponse.setCode("0");
                            mResponse.setMessage(messageNombre);

                        }
                    } else {
                        if (Util.isBiggerThan(montant, t.getMontant())) {
                            mResponse.setCode("0");
                            mResponse.setMessage(messageMontant);
                            //   throw new CustomParameterizedException(messageMontant);
                        }
                        if (t.getNombre() != null && t.getNombre() < 1) {
                            mResponse.setCode("0");
                            mResponse.setMessage(messageNombre);
                        }
                    }
                }
            }
        }
        return mResponse;
    }

    /**
     * Verification periodique
     *
     * @param acteurId
     * @param typeServiceId
     * @param canalId
     * @param clientBeneficiaireId
     * @param clientEmetteurId
     * @param uoEmetteurId
     * @param uoAcqId
     * @param typeRegle
     * @param montant
     * @param temps
     */
    private TableauCumulDTO verifCumulPeriodique(Long acteurId, Long typeServiceId, Long canalId, Long clientBeneficiaireId, Long clientEmetteurId,
            Long uoEmetteurId, Long uoAcqId, TypeRegle typeRegle, BigDecimal montant, Temps temps, Long profilId) {
        List<LocalDate> dates = new ArrayList<>();
        List<TableauCumulDTO> taleauCumulConfig;
        String messageMontant = "";
        String messageNombre = "";
        switch (temps) {//Verification en fonction de la periode
            case MENSUEL:
                dates = getDatesOfMonth();
                if (typeRegle.equals(TypeRegle.ACTEUR)) {
                    messageMontant = "Le montant maximun mensuel de l'opérateur est atteint!";
                    messageNombre = "Le nombre d'operation maximun mensuel de l'opÃ©rateur est atteint";
                } else {
                    messageMontant = "Le montant maximun mensuel du client est atteint!";
                    messageNombre = "Le nombre d'operation max mensuel du client  est atteint!";
                }
                break;
            case MONTANT_OP:
                dates.add(LocalDate.now());
                dates.add(LocalDate.now());
                if (typeRegle.equals(TypeRegle.ACTEUR)) {
                    messageMontant = "Le montant limite de l'opÃ©rateur est atteint!";
                    messageNombre = "Le nombre d'operation max mensuel de l'opérateur est atteint";
                } else {
                    messageMontant = "Le montant  limite du client est atteint!";
                    messageNombre = "Le nombre d'operation max mensuel du client  est atteint!";
                }
                break;
            case SOLDE:
                dates.add(LocalDate.now());
                dates.add(LocalDate.now());
                if (typeRegle.equals(TypeRegle.ACTEUR)) {
                    messageMontant = "Le solde maximun  de l'opérateur est atteint!";
                    messageNombre = "Le nombre d'operation max mensuel de l'opÃ©rateur est atteint";
                } else {
                    messageMontant = "Le solde maximun  du client est atteint!";
                    messageNombre = "Le nombre d'operation max mensuel du client  est atteint!";
                }
                break;
            case HEBDOMADAIRE:
                dates = getDatesOfWeek();
                if (typeRegle.equals(TypeRegle.ACTEUR)) {
                    messageMontant = "Le montant maximun hebdomadaire de l'opÃ©rateur est atteint!";
                    messageNombre = "Le nombre d'operation max hebdomadaire de l'opÃ©rateur est atteint!";
                } else {
                    messageMontant = "Le montant maximun hebdomadaire du client est atteint!";
                    messageNombre = "Le nombre d'operation max hebdomadaire  du client  est atteint!";
                }
                break;
            case JOURNALIER:
                dates.add(LocalDate.now());
                dates.add(LocalDate.now());
                if (typeRegle.equals(TypeRegle.ACTEUR)) {
                    messageMontant = "Le montant maximun journalier de l'opÃ©rateur est atteint!";
                    messageNombre = "Le nombre d'operation max journalier de l'opÃ©rateur est atteint!";
                } else {
                    messageMontant = "Le montant maximun journalier du client est atteint!";
                    messageNombre = "Le nombre d'operation max journalier  du client  est atteint!";
                }
                break;
        }

        if (!dates.isEmpty()) {
            taleauCumulConfig = tableauCumulService.findByCriteria(typeServiceId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmetteurId, uoAcqId, typeRegle, temps, profilId);
            if (!taleauCumulConfig.isEmpty()) {
                for (TableauCumulDTO t : taleauCumulConfig) {
                    List<BilanOperation> bilanOperations = new ArrayList();
                    Long typeSerId = null;
                    if (t.getTypeServiceId() != null) {
                        typeSerId = typeServiceId;
                    }
                    //Calcul du bilan des operations
                    if (temps.equals(Temps.SOLDE)) {
                        if (Util.isBiggerThan(montant, t.getMontant())) {
                            t.setMessage(messageMontant);
                            return t;
                        }
                    } else if (!temps.equals(Temps.MONTANT_OP)) {
                        if (t.getOrganisationAcqId() == null && t.getOrganisationEmId() == null) {
                            bilanOperations = bilanOperationRepository.findByMultiParamsAndActeurBetweenDateDebutAndDateFinAll(acteurId, typeSerId, canalId, clientBeneficiaireId, null, null, dates.get(0), dates.get(1));
                        } else {
                            bilanOperations = bilanOperationRepository.findByMultiParamsAndActeurBetweenDateDebutAndDateFin(acteurId, typeSerId, canalId, clientBeneficiaireId, clientEmetteurId, uoEmetteurId, uoAcqId, dates.get(0), dates.get(1));
                        }

                    }

                    if (!bilanOperations.isEmpty()) {
                        BigDecimal montantTotal = BigDecimal.valueOf(bilanOperations.stream().mapToDouble(b -> b.getMontant().doubleValue()).sum());
                        Integer nombreTotal = bilanOperations.stream().mapToInt(b -> b.getNombre().intValue()).sum();
                        if (Util.isBiggerThan(montantTotal.add(Util.getZeroIfNull(montant)), t.getMontant())) {

                            t.setMessage(messageMontant);
                            return t;
                        }
                        if (t.getNombre() != null && (nombreTotal + 1 > t.getNombre())) {
                            t.setMessage(messageNombre);
                            return t;
                        }
                    } else {
                        if (t.getMontant() != null) {
                            return verifSeuil(t, montant);
                        }

                        if (t.getNombre() != null && t.getNombre() < 1) {
                            t.setMessage(messageNombre);
                            return t;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Bilan du montant total des operations par type de service -Journalier En
     * fonction du point de vente Si l'utilisateur est associÃ© Ã  un pv on
     * liste les operations du pv Si non on recherche les pv de l'organisation
     * de l'utilisateur connectÃ© puis les operations de chaque PV
     */
    public StatsBilanJour stats() throws IOException {
        List<TypeService> typeServices = typeServiceRepository.findAll();
        List<String> labels = typeServices.stream().map(typeService -> typeService.getLibelle()).collect(Collectors.toList());
        List<Double> datas = new ArrayList<>();
        List<Operation> operations = new ArrayList<>();
        User user = userService.getUserConnecte();
        PointVente pointVente = pointVenteService.getPointVenteOfUserConnecte();
        for (TypeService typeService : typeServices) {
            LocalDate localDate = LocalDate.now();
            operations.clear();
            ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
            if (pointVente != null) {
                operations = operationRepository.findAllOperationByAndTypseServiceAndPointVente(typeService.getId(), pointVente.getId(), zonedDateTime, zonedDateTime.plusHours(23), Etape.PROCEED);
            } else {
                UniteOrganisation organisation = uniteOrganisationService.getUoOfUserConnecte();
                List<PointVente> pointVentes = new ArrayList<>();
                if (user.getAuthorities().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AuthoritiesConstants.ADMIN))) {
                    if (organisation != null) {
                        List<PointVente> litepointVentes = pointVenteService.findPointVenteByOrganisation(organisation.getId());
                        pointVentes = litepointVentes;
                    }
                } else {
                    pointVentes = pointVenteService.findAllPv();
                }
                for (PointVente pv : pointVentes) {
                    operations.addAll(operationRepository.findAllOperationByAndTypseServiceAndPointVente(typeService.getId(), pv.getId(), zonedDateTime, zonedDateTime.plusHours(24), Etape.PROCEED));
                }
            }
            datas.add(operations.stream().mapToDouble(operation -> operation.getMontantOp().doubleValue()).sum());
        }
        StatsBilanJour statsBilanJour = new StatsBilanJour();
        statsBilanJour.setDatas(datas);
        statsBilanJour.setLabels(labels);
        return statsBilanJour;
    }

    public StatsBilanJour statsOptimise() throws IOException {
        List<TypeServiceDTO> typeServices = typeServiceRepository.findAllTypeService();
        List<String> labels = typeServices.stream().map(typeService -> typeService.getLibelle()).collect(Collectors.toList());
        // List<Long> tsIds = typeServices.stream().map(typeService -> typeService.getId()).collect(Collectors.toList());
        List<Double> datas = new ArrayList<>();
        List<OperationOptimiseDTO> operations = new ArrayList<>();
        User user = userService.getUserWithAuthorities();
        PointVente pointVente = pointVenteService.getPointVenteOfUserConnecteOptimise(user);
        //log.debug("Etape.PROCEED.name()"+Etape.PROCEED.name(),"et point de vent"+Collections.singletonList(pointVente.getId()));
        for (TypeServiceDTO typeService : typeServices) {
            LocalDate localDate = LocalDate.now();
            operations.clear();
            ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());

            if (pointVente != null) {
                operations = operationRepository.findAllOperationByAndTypseServiceAndPointVenteOptimise(typeService.getId(), Collections.singletonList(pointVente.getId()), zonedDateTime, zonedDateTime.plusHours(23), StatutOperation.PAYE.name());
            } else {
                UniteOrganisation organisation = user.getOrganisation();
                List<Long> pointVentes = new ArrayList<>();
                if (user.getAuthorities().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AuthoritiesConstants.ADMIN))) {

                    if (organisation != null) {
                        List<PointVente> litepointVentes = pointVenteService.findPointVenteByOrganisation(organisation.getId());
                        pointVentes = litepointVentes.stream().map(value -> value.getId()).collect(Collectors.toList());
                    }
                } else {
                    pointVentes = pointVenteService.findAllIdOptimise();
                }
                operations.addAll(operationRepository.findAllOperationByAndTypseServiceAndPointVenteOptimise(typeService.getId(), pointVentes, zonedDateTime, zonedDateTime.plusHours(24), StatutOperation.PAYE.name()));
            }
            datas.add(operations.stream().mapToDouble(operation -> operation.getMontantOp().doubleValue()).sum());
        }
        StatsBilanJour statsBilanJour = new StatsBilanJour();
        statsBilanJour.setDatas(datas);
        statsBilanJour.setLabels(labels);
        return statsBilanJour;
    }

    /**
     * Stats des operations realisÃ© durant l'annÃ©e en cours En fonction du
     * point de vente de l'utilisateur connectÃ© ou des points de vente de
     * l'organisation Pour l'adminisatrateur on traites toutes les informations
     * des points de vente
     *
     * @return
     */
    public StatsBilanMensuel getDatasByMonth() {
        List<Integer> months = new ArrayList<>();
        Integer month = LocalDate.now().getMonthValue();
        for (int i = 1; i <= month; i++) {
            months.add(i);
        }
        List<TypeService> typeServices = typeServiceRepository.findAll();
        List<String> labels = typeServices.stream().map(typeService -> typeService.getLibelle()).collect(Collectors.toList());
        List<Object> datas = new ArrayList<>();
        PointVente pointVente = pointVenteService.getPointVenteOfUserConnecte();
        if (pointVente != null) {
            typeServices.forEach(typeService -> {//Bilan pour chaque type de service
                List<Object> datasTypeService = new ArrayList<>();
                for (int i = 0; i < months.size(); i++) {
                    Object montant = operationRepository.getSumMontantByMonthAndTypeServiceAndEtape(months.get(i), typeService.getId(), pointVente.getId(), String.valueOf(Etape.PROCEED)).get(0);
                    if (montant == null) {
                        montant = BigDecimal.ZERO;
                    }
                    if (montant != null) {
                        datasTypeService.add(montant);
                    } else {
                        datasTypeService.add(BigDecimal.ZERO);
                    }
                }
                datas.add(datasTypeService);
            });
        } else {
            UniteOrganisation organisation = uniteOrganisationService.getUoOfUserConnecte();
            typeServices.forEach(typeService -> {//Bilan pour chaque service
                List<Object> datasTypeService = new ArrayList<>();
                for (int i = 0; i < months.size(); i++) {
                    BigDecimal montant = BigDecimal.ZERO;
                    User user = userService.getUserWithAuthorities();
                    List<PointVente> pointVentes = new ArrayList<>();
                    if (user.getAuthorities().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AuthoritiesConstants.ADMIN))) {
                        if (organisation != null) {

                            List<PointVente> litepointVentes = pointVenteService.findPointVenteByOrganisation(organisation.getId());
                            pointVentes = litepointVentes;
                        }
                    } else {
                        pointVentes = pointVenteService.findAllPv();
                    }
                    for (PointVente pv : pointVentes) {
                        List<Object> mnt = operationRepository.getSumMontantByMonthAndTypeServiceAndEtape(months.get(i), typeService.getId(), pv.getId(), String.valueOf(Etape.PROCEED));
                        if (mnt == null || mnt.get(0) == null) {
                            montant = montant.add(BigDecimal.ZERO);
                        } else {
                            montant = montant.add((BigDecimal) mnt.get(0));
                        }
                    }
                    if (montant != null) {
                        datasTypeService.add(Util.getZeroIfNull(montant));
                    } else {
                        datasTypeService.add(BigDecimal.ZERO);
                    }
                }
                datas.add(datasTypeService);
            });
        }
        StatsBilanMensuel statsBilanMensuel = new StatsBilanMensuel();
        statsBilanMensuel.setLabels(labels);
        statsBilanMensuel.setDatas(datas);
        statsBilanMensuel.setMonths(months);
        return statsBilanMensuel;
    }

    /**
     * Stats des operations en direction d'un pays donnÃ© En fonction du point
     * de vente de l'utilisateur connectÃ© ou des points de vente de
     * l'organisation de l'utilsateur connectÃ©
     */
    public StatsBilanJour statsByCountryForAllOperations(Long uoPaysId) {
        List<TypeService> typeServices = typeServiceRepository.findAll();
        List<String> labels = typeServices.stream().map(typeService -> typeService.getLibelle()).collect(Collectors.toList());
        List<Double> datas = new ArrayList<>();
        PointVente pointVente = pointVenteService.getPointVenteOfUserConnecte();
        List<UniteOrganisation> uniteOrganisationList = new ArrayList<>();
        ArrayList<UniteOrganisation> listAll = new ArrayList<>();
        ArrayList<UniteOrganisation> listPeres = new ArrayList<>();
        ArrayList<UniteOrganisation> listFils = new ArrayList<>();
        UniteOrganisation uo = uniteOrganisationService.findOneById(uoPaysId);
        uniteOrganisationList = uniteOrganisationService.findUoFilsOfAnUO(uo, listAll, listPeres, listFils, 0);
        List<Long> uoIds = uniteOrganisationList.stream().map(uniteOrganisationDTO -> uniteOrganisationDTO.getId()).collect(Collectors.toList());
        if (pointVente != null) {//En fonction du point de vente de l'utilisateur
            for (TypeService typeService : typeServices) {
                List<Operation> operations = operationRepository.findByTypeServiceAndUoAcquereurIdInAndPointVenteAndEtape(typeService, uoIds, pointVente, Etape.PROCEED);
                datas.add(operations.stream().mapToDouble(operation -> operation.getMontantOp().doubleValue()).sum());
            }
        } else { //En fonction de tous les points de vente de l'utilisateur connectÃ©
            UniteOrganisation organisation = uniteOrganisationService.getUoOfUserConnecte();
            //Les informations des points de vente de l'organisation
            for (TypeService typeService : typeServices) {
                List<Operation> operations = new ArrayList<>();
                User user = userService.getUserWithAuthorities();
                List<PointVente> pointVentes = new ArrayList<>();
                if (user.getAuthorities().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AuthoritiesConstants.ADMIN))) {
                    if (organisation != null) {
                        List<PointVente> litepointVentes = pointVenteService.findPointVenteByOrganisation(organisation.getId());
                        pointVentes = litepointVentes;
                    }
                } else {
                    pointVentes = pointVenteService.findAllPv();
                }
                for (PointVente pv : pointVentes) {
                    operations.addAll(operationRepository.findByTypeServiceAndUoAcquereurIdInAndPointVenteAndEtape(typeService, uoIds, pv, Etape.PROCEED));
                }
                datas.add(operations.stream().mapToDouble(operation -> operation.getMontantOp().doubleValue()).sum());
            }
        }
        StatsBilanJour statsBilanJour = new StatsBilanJour();
        statsBilanJour.setLabels(labels);
        statsBilanJour.setDatas(datas);
        return statsBilanJour;
    }

    /**
     * Filtrer les opÃ©rations en fonction de plusieurs critÃ¨res
     *
     * @param pageable
     * @param typeServiceId
     * @param canalId
     * @param uoAcquereurId
     * @param montantOp
     * @param statutOperation
     * @param dateOperation
     * @param dateFin
     * @return
     */
    public Page<MOperationDTO> findByMultiParamsMobile(Pageable pageable, Long typeServiceId, Long canalId, Long uoAcquereurId, BigDecimal montantOp,
            StatutOperation statutOperation, ZonedDateTime dateOperation, ZonedDateTime dateFin,
            String codeOperation, Long organisationId) {
        List<StatutOperation> statut = null;
        List<Long> pointVenteIds = getMobilePointVenteIdsOfUserConnecte(organisationId);
        StatutOperation statutAnnuler = null;
        if (statutOperation != null) {
            statut = new ArrayList();
            if (statutOperation.equals(StatutOperation.ENATTENTE_VALIDATION)) {

                statut.add(StatutOperation.ENATTENTE_VALIDATION);
                statut.add(StatutOperation.ENATTENTE_ANNULATION);
            } else {
                statut.add(statutOperation);
            }
        } else {
            StatutOperation[] allStatut = StatutOperation.values();
            statut = new ArrayList();
            for (StatutOperation statutAll : allStatut) {
                if (!statutAll.equals(StatutOperation.ENATTENTE_ANNULATION)) {
                    statut.add(statutAll);
                }
            }
        }
        if (!pointVenteIds.isEmpty()) {
            if (dateOperation != null && dateFin != null) {
                return operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, dateOperation, dateFin.plusHours(23), codeOperation, pageable).map(operationDTOMapper::toDto);
            } else if (dateOperation != null) {
                return operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, dateOperation, dateOperation.plusHours(23), codeOperation, pageable).map(operationDTOMapper::toDto);
            } else if (dateFin != null) {
                return operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, ZonedDateTime.now().minusMonths(12), dateFin.plusHours(23), codeOperation, pageable).map(operationDTOMapper::toDto);
            }
            return operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, dateOperation, dateFin, codeOperation, pageable).map(operationDTOMapper::toDto);
        }
        return new PageImpl<MOperationDTO>(Collections.emptyList());
    }

    /**
     * Filtrer les opÃ©rations en fonction de plusieurs critÃ¨res
     *
     * @param pageable
     * @param typeServiceId
     * @param canalId
     * @param uoAcquereurId
     * @param montantOp
     * @param statutOperation
     * @param dateOperation
     * @param dateFin
     * @return
     */
    public Page<OperationDTO> findByMultiParams(Pageable pageable, Long typeServiceId, Long canalId, Long uoAcquereurId, BigDecimal montantOp,
            StatutOperation statutOperation, ZonedDateTime dateOperation, ZonedDateTime dateFin,
            String codeOperation, Long organisationId) {
        List<StatutOperation> statut = null;
        List<Long> pointVenteIds = getPointVenteIdsOfUserConnecte(organisationId);
        StatutOperation statutAnnuler = null;
        if (statutOperation != null) {
            statut = new ArrayList();
            if (statutOperation.equals(StatutOperation.ENATTENTE_VALIDATION)) {

                statut.add(StatutOperation.ENATTENTE_VALIDATION);
                statut.add(StatutOperation.ENATTENTE_ANNULATION);
            } else {
                statut.add(statutOperation);
            }
        } else {
            StatutOperation[] allStatut = StatutOperation.values();
            statut = new ArrayList();
            for (StatutOperation statutAll : allStatut) {
                if (!statutAll.equals(StatutOperation.ENATTENTE_ANNULATION)) {
                    statut.add(statutAll);
                }
            }
        }
        if (!pointVenteIds.isEmpty()) {
            if (dateOperation != null && dateFin != null) {
                return operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, dateOperation, dateFin.plusHours(23), codeOperation, pageable).map(operationMapper::toDto);
            } else if (dateOperation != null) {
                return operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, dateOperation, dateOperation.plusHours(23), codeOperation, pageable).map(operationMapper::toDto);
            } else if (dateFin != null) {
                return operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, ZonedDateTime.now().minusMonths(12), dateFin.plusHours(23), codeOperation, pageable).map(operationMapper::toDto);
            }
            return operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, dateOperation, dateFin, codeOperation, pageable).map(operationMapper::toDto);
        }
        return new PageImpl<OperationDTO>(Collections.emptyList());
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
     * @return
     */
    public Page<OperationDTO> findByMultiParamsEnAttenteValidation(Pageable pageable, Long typeServiceId, Long canalId, Long uoAcquereurId, BigDecimal montantOp, StatutOperation statutOperation, ZonedDateTime dateOperation, ZonedDateTime dateFin, String codeOperation) {
        User user = userService.getUserWithAuthorities();
        List<Long> pointVenteIds = new ArrayList<>();
        List<StatutOperation> statut = null;
        if (statutOperation != null) {
            statut = new ArrayList();
            statut.add(statutOperation);
        }

        if (user.getAuthorities().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AuthoritiesConstants.DO_VALIDATION))) {
            ArrayList<UniteOrganisation> listAll = new ArrayList<>();
            ArrayList<UniteOrganisation> listPeres = new ArrayList<>();
            ArrayList<UniteOrganisation> listFils = new ArrayList<>();

            List<UniteOrganisation> uniteOrganisationList = uniteOrganisationService.findUoFilsOfAnUO(user.getUniteOrganisation(), listAll, listPeres, listFils, 0);
            uniteOrganisationList.forEach(organisation -> {

                List<PointVente> litepointVentes = pointVenteService.findPointVenteByOrganisation(organisation.getId());
                Set<PointVente> pointVentes = litepointVentes.stream().collect(Collectors.toSet());
                if (pointVentes != null) {
                    pointVentes.forEach(pv -> {
                        pointVenteIds.add(pv.getId());
                    });
                }
            });
        }

        if (!pointVenteIds.isEmpty()) {
            if (dateOperation != null && dateFin != null) {
                return operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, dateOperation, dateFin.plusHours(23), codeOperation, pageable).map(operationMapper::toDto);
            } else if (dateOperation != null) {
                return operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, dateOperation, dateOperation.plusHours(23), codeOperation, pageable).map(operationMapper::toDto);
            }
            return operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, dateOperation, dateFin, codeOperation, pageable).map(operationMapper::toDto);
        } else {

        }
        return new PageImpl<OperationDTO>(Collections.emptyList());
    }

    /**
     * Recherche les identifiants des points de vente de l'utilisateur connectÃ©
     *
     * @return
     */
    public List<Long> getPointVenteIdsOfUserConnecte(Long organisationId) {
        User user = userService.getUserConnecte();
        PointVente pointVente = pointVenteService.getPointVenteOfUserConnecte();
        List<Long> pointVenteIds = new ArrayList<>();
        if (!user.getAuthorities().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AuthoritiesConstants.ADMIN))) {
            if (organisationId != null) {
                pointVenteService.findAllByUO(organisationId).forEach(pv -> {
                    pointVenteIds.add(pv.getId());
                });
            } else {
                pointVenteService.findAllPv().forEach(pv -> {
                    pointVenteIds.add(pv.getId());
                });
            }
        } else if (pointVente != null) {
            pointVenteIds.add(pointVente.getId());
        } else {
            UniteOrganisation organisation = uniteOrganisationService.getUoOfUserConnecte();
            List<PointVente> litepointVentes = pointVenteService.findPointVenteByOrganisation(organisation.getId());

            Set<PointVente> pointVentes = litepointVentes.stream().collect(Collectors.toSet());
            if (pointVentes != null) {
                pointVentes.forEach(pv -> {
                    pointVenteIds.add(pv.getId());
                });
            }
        }
        return pointVenteIds;
    }

    public List<Long> getMobilePointVenteIdsOfUserConnecte(Long organisationId) {
        // User user = userService.getUserConnecte();

        //**** Recuperer les points de ventes de l'organisation
        /**
         * l'utilisateur connecté
         */
        PointVente pointVente = null;//pointVenteService.getPointVenteOfUserConnecte();
        List<Long> pointVenteIds = new ArrayList<>();
        //    if (!user.getAuthorities().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AuthoritiesConstants.ADMIN))) {
        /*  if (organisationId != null) {
                pointVenteService.findAllByUO(organisationId).forEach(pv -> {
                    pointVenteIds.add(pv.getId());
                });
            } else {
                pointVenteService.findAllPv().forEach(pv -> {
                    pointVenteIds.add(pv.getId());
                });
            }*/
        //   } else if (pointVente != null) {
        //         pointVenteIds.add(pointVente.getId());
        //      } else {
        //    UniteOrganisation organisation = uniteOrganisationService.getUoOfUserConnecte();
        List<PointVente> litepointVentes = pointVenteService.findPointVenteByOrganisation(organisationId);

        Set<PointVente> pointVentes = litepointVentes.stream().collect(Collectors.toSet());
        if (pointVentes != null) {
            pointVentes.forEach(pv -> {
                pointVenteIds.add(pv.getId());
            });
        }
        // }
        return pointVenteIds;
    }

    public List<Operation> findByCompteSourceAndEtat(Long typeServiceId, Compte compteSrc, StatutOperation statutOperation) {
        List<Operation> operations = new ArrayList<>();
        operations.addAll(operationRepository.findByCompteSourceAndEtat(typeServiceId, compteSrc, statutOperation));
        return operations;
    }

    /**
     * Retrouver une personne
     *
     * @param personneId
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
    public List<OperationDTO> findByPersonne(Long personneId, Long typeServiceId, Long canalId, Long uoAcquereurId, BigDecimal montantOp, StatutOperation statutOperation, Boolean statutAnnuler, ZonedDateTime dateOperation, ZonedDateTime dateFin, String codeOperation) {
        Personne personne = personneRepository.findById(personneId).orElse(null);
        StatutOperation annulationStatut = null;
        if (statutAnnuler) {

            annulationStatut = StatutOperation.ENATTENTE_ANNULATION;
        }
        if (personne != null) {
            Beneficiaire beneficiaire = beneficiaireRepository.findTop1ByPersonneId(personneId);
            Emetteur emetteur = emetteurRepository.findTop1ByPersonneId(personneId);

            Long emetteurId = emetteur != null ? emetteur.getId() : null;
            Long beneficiareId = beneficiaire != null ? beneficiaire.getId() : null;

            List<Operation> operations = new ArrayList<>();

            if (dateOperation != null && dateFin != null) {
                if (beneficiaire != null) {
                    operations.addAll(operationRepository.findOperationByPersonneBeneficiaire(typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, annulationStatut, dateOperation, dateFin.plusHours(23), codeOperation, beneficiareId));
                }
                if (emetteur != null) {
                    operations.addAll(operationRepository.findOperationByPersonneEmetteur(typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, annulationStatut, dateOperation, dateFin.plusHours(23), codeOperation, emetteurId));
                }
                return operationMapper.toDto(operations.stream().filter(distinctByKey(p -> p.getId())).collect(Collectors.toList()));
            } else if (dateOperation != null) {
                if (beneficiaire != null) {
                    operations.addAll(operationRepository.findOperationByPersonneBeneficiaire(typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, annulationStatut, dateOperation, dateOperation.plusHours(23), codeOperation, beneficiareId));
                }
                if (emetteur != null) {
                    operations.addAll(operationRepository.findOperationByPersonneEmetteur(typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, annulationStatut, dateOperation, dateOperation.plusHours(23), codeOperation, emetteurId));
                }
                return operationMapper.toDto(operations.stream().filter(distinctByKey(p -> p.getId())).collect(Collectors.toList()));
            } else {
                if (beneficiaire != null) {
                    operations.addAll(operationRepository.findOperationByPersonneBeneficiaire(typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, annulationStatut, dateOperation, dateFin, codeOperation, beneficiareId));
                }
                if (emetteur != null) {
                    operations.addAll(operationRepository.findOperationByPersonneEmetteur(typeServiceId, canalId, uoAcquereurId, montantOp, statutOperation, annulationStatut, dateOperation, dateFin, codeOperation, emetteurId));
                }
                return operationMapper.toDto(operations.stream().filter(distinctByKey(p -> p.getId())).collect(Collectors.toList()));
            }
        }
        return Collections.emptyList();
    }

    public static <T> Predicate<T> distinctByKey(
            Function<? super T, ?> keyExtractor) {

        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    /**
     * Retourne la liste des evenements en fonction de l'utilisateur,du montant
     * et du compte d'operation acteur
     *
     * @param montant
     * @return
     * @throws IOException
     */
    public List<Evenement> getEvenements(String montant) throws IOException {
        User user = userService.getUserWithAuthorities();
        UniteOrganisation organisation = uniteOrganisationService.getUoOfUserConnecte();
        Compte compte = compteService.getCompteOperationActeur(organisation);
        String sens = "C";
        List<Evenement> evenements = apiService.getListEvenement(user.getLogin(), compte.getValeurCompte(), sens, montant, compte.getCompteBanque().getCodeFse(), compte.getCompteBanque().getCodeAgence());
        List<Evenement> evenementsFilter = new ArrayList<>();
        if (evenements != null) {
            for (Evenement ev : evenements) {
                if (!operationRepository.existsByCodeExterne(ev.getEve())) {
                    evenementsFilter.add(ev);
                }
            }
            return evenementsFilter;
        } else {
            throw new CustomParameterizedException("Evenenments non disponible.");
        }
    }

    @Transactional
    public OperationDTO annulationOpEnvoiCash(@NotNull Long operationId) throws IOException, JAXBException {
        Operation operation = operationRepository.findById(operationId).orElse(null);
        //Verification de la pÃ©riode d'annulation
        if (!canUpdateOperation(0, operation)) {
            throw new CustomParameterizedException("La pÃ©riode d'annulation autorisÃ© est depassÃ©e.");
        }
        if (operation.getStatut().equals(StatutOperation.ENATTENTE)) {
            log.info("Annulation de l'operation avec remboursement des frais");
            operation.statut(StatutOperation.ANNULLE);
            operationAuditService.saveOpAudit(operation, operation.getStatut(), StatutOperation.ANNULLE);
            operation.commentaire("Annulation de l'operation avec remboursement des frais");
            UniteOrganisation uniteOrganisationSource = operation.getPointVente().getUniteOrganisation();
            UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.PAYS);
            UniteOrganisation uniteOrganisationDest = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());
            UniteOrganisation fseDest = uniteOrganisationService.getUoFseFilialeWithUoPays(uniteOrganisationDest);
            UniteOrganisation fseSrc = uniteOrganisationService.getUoFseFilialeWithUoPays(uoPaysSrc);
            UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(fseDest);
            Result response = apiService.sendRequestForComptabilisationEnvoiCash(uoPaysSrc, uniteOrganisationDest, operation, fseSrc, fseDest, fseDestAgenceSiege, Boolean.FALSE);
            verifResultOfOmnicanal(response, operation);

            transactionComptePtiService.findByOperationId(operationId);
            // commissionServicel.annulationRepartitionCommission(operationId);
            operation = operationRepository.save(operation);
            return operationMapper.toDto(operation);
        } else {
            throw new CustomParameterizedException("Impossible d'annuler cette opÃ©ration!");
        }
    }

    /**
     * ******************************************************ANNULATION
     * OPERATION ENVOI
     * CASH************************************************************************************************
     */
    /**
     * Service de comptabilisation de l'annulation des operations
     *
     * @param operationId
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @Transactional
    public OperationDTO annulationOpEnvoiCashAvecFrais(@NotNull Long operationId) throws IOException, JAXBException {
        Operation operation = operationRepository.findById(operationId).orElse(null);
        //Verification de la pÃ©riode d'annulation
        if (!canUpdateOperation(0, operation)) {
            throw new CustomParameterizedException("La pÃ©riode d'annulation autorisÃ© est depassÃ©e.");
        }
        if (operation.getStatut().equals(StatutOperation.ENATTENTE)) {
            log.info("Annulation de l'operation avec remboursement des frais");
            operation.statut(StatutOperation.ANNULLE);
            operationAuditService.saveOpAudit(operation, operation.getStatut(), StatutOperation.ANNULLE);
            operation.commentaire("Annulation de l'operation avec remboursement des frais");
            UniteOrganisation uniteOrganisationSource = operation.getPointVente().getUniteOrganisation();
            UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(uniteOrganisationSource, TypeUO.PAYS);
            UniteOrganisation uniteOrganisationDest = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());
            UniteOrganisation fseDest = uniteOrganisationService.getUoFseFilialeWithUoPays(uniteOrganisationDest);
            UniteOrganisation fseSrc = uniteOrganisationService.getUoFseFilialeWithUoPays(uoPaysSrc);
            UniteOrganisation fseDestAgenceSiege = uniteOrganisationService.getUoAgenceSiegeWithUoFse(fseDest);
            Result response = apiService.sendRequestForComptabilisationEnvoiCash(uoPaysSrc, uniteOrganisationDest, operation, fseSrc, fseDest, fseDestAgenceSiege, Boolean.FALSE);
            verifResultOfOmnicanal(response, operation);
            transactionComptePtiService.findByOperationId(operationId);
            commissionServicel.annulationRepartitionCommission(operationId);
            operation = operationRepository.save(operation);
            return operationMapper.toDto(operation);
        } else {
            throw new CustomParameterizedException("Impossible d'annuler cette opération!");
        }
    }

    public MResponse annulationOperation(String codeOperation) throws IOException, JAXBException {
        Long idOperation = Long.parseLong(codeOperation);
        Operation operation = operationRepository.findById(idOperation).orElse(null);
        int dureeExpire = getDureeExpire();
        if (operation.getStatut().equals(StatutOperation.ENATTENTE)) {
            if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.PAIEMENT_INTERNET)) || operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.ACCOUNT_CASH))) {
                if (canUpdateOperation(dureeExpire, operation)) {
                    OperationDTO optFDto;
                    if (idOperation != null) {
                        optFDto = annulationOpAccountAvecFrais(idOperation, Boolean.TRUE);
                        if (optFDto != null) {
                            if (optFDto.getStatut().equals(StatutOperation.ANNULLE)) {
                                return new MResponse("0", "Opération effectuée avec succès");
                            } else {
                                return new MResponse("-1", "Erreur lors de l'opération ");
                            }
                        } else {
                            return new MResponse("-1", "Erreur lors de l'opération");
                        }

                    } else {
                        return new MResponse("-1", "Erreur lors de l'opération");
                    }
                } else {
                    return new MResponse("-1", "Impossible d'annuler l'opération");
                }
            }

        }
        return new MResponse("-1", "Erreur lors de l'opération");

        //
    }

    public MResponseOperation doannulationOperation(String codeOperation, String codePv) throws IOException, JAXBException {
        Operation operation = operationRepository.findByCodeOperation(codeOperation).orElse(null);
        PointVente opt = pointVenteService.findPointByCode(codePv);
        try {
            if (operation != null && opt != null) {
                Optional<PointVente> pv = Optional.of(opt);
                if (pv.isPresent() && pv.get().equals(operation.getPointVente())) {
                    if (operation != null && operation.getStatut().equals(StatutOperation.PAYE)) {
                        operation.setEtape(Etape.INIT);
                        operation.setStatut(StatutOperation.ENATTENTE_ANNULATION);
                        operationAuditService.saveOpAudit(operation, operation.getStatut(), StatutOperation.ENATTENTE_ANNULATION);
                        operation = operationRepository.save(operation);
                        Compte cmptClientSrc = operation.getCompteSrc();
                        transactionComptePtiService.findByOperationId(operation.getId());
                        Long fseDestId = operation.getUoAcquereur().getId();
                        Long fseSrcId = operation.getUoEmetteur().getId();
                        UniteOrganisation fseDest = uniteOrganisationService.findOneById(fseDestId);//Fse Dest
                        if (!fseDest.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.toString())) {
                            fseDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.FSE);
                        }
                        UniteOrganisation fseSrc = uniteOrganisationService.findOneById(fseSrcId); //Fse Acq
                        // UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq
                        UniteOrganisation uoPaysDest = returnPays(fseDest);//uniteOrganisationService.findOneById(fseDest.getUoPaysId());
                        UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(operation.getPointVente().getUniteOrganisation(), TypeUO.PAYS);
                        operation.commentaire("Annulation de l'operation avec remboursement des frais");
                        Result response = null;
                        if (!operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.ACCOUNT_CASH))) {
                            Compte cmpteClientDest = operation.getCompteDest();
                            response = apiService.sendRequestComptabilisationOfAccountAccountToOmnicanal(uoPays, uoPaysDest, operation, fseSrc, fseDest, cmptClientSrc, cmpteClientDest, Boolean.FALSE, true, null);

                        } else {
                            response = apiService.sendRequestComptabilisationOfAccountCashToOmnicanal(operation, fseSrc, fseDest, cmptClientSrc, Boolean.FALSE, Boolean.FALSE);

                        }
                        verifResultOfOmnicanal(response, operation);

                        operation.setEtape(Etape.PROCEED);
                        operation.setStatut(StatutOperation.ANNULLE);
                        operationAuditService.saveOpAudit(operation, operation.getStatut(), StatutOperation.ANNULLE);
                        operation = operationRepository.save(operation);

                        if (operation.getStatut().equals(StatutOperation.ANNULLE)) {
                            return new MResponseOperation("0", "Opération effectuée avec succès", operation);
                        } else {
                            return new MResponseOperation("-1", "Erreur lors de l'opération ", null);
                        }

                    } else {
                        return new MResponseOperation("-1", "Impossible d'annuler cette operation ", null);
                    }
                }
            }
        } catch (CustomSettlementException ex) {
            return new MResponseOperation("-1", "Erreur lors de l'opération ", null);
        }

        return new MResponseOperation("-1", "Erreur lors de l'opération ", null);

        //
    }

    public MResponse annulationOperation(Long idOperation, Boolean avecfrais) throws IOException, JAXBException {
        OperationDTO optFDto;
        if (idOperation != null) {
            optFDto = annulationOpAccountAvecFrais(idOperation, avecfrais);
            if (optFDto != null) {
                if (optFDto.getStatut().equals(StatutOperation.ANNULLE)) {
                    return new MResponse("0", "Opération effectuée avec succès");
                } else {
                    return new MResponse("-1", "Erreur lors de l'opération ");
                }
            } else {
                return new MResponse("-1", "Erreur lors de l'opération");
            }
        } else {
            return new MResponse("-1", "Erreur lors de l'opération");
        }

        //
    }

    /**
     * @param operationId
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @Transactional
    public OperationDTO annulationOpAccountAvecFrais(long operationId, Boolean avecFrais) throws IOException, JAXBException {
        Operation operation = operationRepository.findById(operationId).orElse(null);

        if (operation.getEtape().equals(Etape.PROCEED)) {
            operation.setEtape(Etape.INIT);
            operation = operationRepository.save(operation);
            if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.CASH_ACCOUNT))) {
                return annulationCashAccount(operationId, avecFrais);
            }
            Long fseDestId = operation.getUoAcquereur().getId();
            Long fseSrcId = operation.getUoEmetteur().getId();
            UniteOrganisation fseDest = uniteOrganisationService.findOneById(fseDestId);//Fse Dest
            if (!fseDest.getTypeUO().getLibelleTypeUO().equals(TypeUO.FSE.toString())) {
                fseDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.FSE);
            }
            UniteOrganisation fseSrc = uniteOrganisationService.findOneById(fseSrcId); //Fse Acq
            // UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq
            UniteOrganisation uoPaysDest = returnPays(fseDest);//uniteOrganisationService.findOneById(fseDest.getUoPaysId());
            UniteOrganisation uoPays = uniteOrganisationService.findUoParentByType(operation.getPointVente().getUniteOrganisation(), TypeUO.PAYS);
            operation.commentaire("Annulation de l'operation avec remboursement des frais");

            if (operation.getStatut().equals(StatutOperation.ENATTENTE_ANNULATION) || operation.getStatut().equals(StatutOperation.ENATTENTE)) {
                operation.setStatut(StatutOperation.ANNULLE);
                operationAuditService.saveOpAudit(operation, operation.getStatut(), StatutOperation.ANNULLE);
                if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.PAIEMENT_BIEN))) {
                    transactionComptePtiService.findByOperationId(operationId);
                    Compte cmptClientSrc = operation.getCompteSrc();
                    Compte cmpteClientDest = operation.getCompteDest();
                    Result response = apiService.sendRequestComptabilisationOfAccountAccountToOmnicanal(uoPays, uoPaysDest, operation, fseSrc, fseDest, cmptClientSrc, cmpteClientDest, Boolean.FALSE, avecFrais, null);
                    verifResultOfOmnicanal(response, operation);
                    if (avecFrais.equals(Boolean.TRUE)) {
                        commissionServicel.annulationRepartitionCommission(operationId);
                    }
                    operation.setEtape(Etape.PROCEED);
                    operation = operationRepository.save(operation);
                } else if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.ACCOUNT_CASH))) {
                    Compte cmptClientSrc = operation.getCompteSrc();
                    transactionComptePtiService.findByOperationId(operationId);
                    Result response = apiService.sendRequestComptabilisationOfAccountCashToOmnicanal(operation, fseSrc, fseDest, cmptClientSrc, Boolean.FALSE, avecFrais);
                    verifResultOfOmnicanal(response, operation);
                    if (avecFrais.equals(Boolean.TRUE)) {
                        commissionServicel.annulationRepartitionCommission(operationId);
                    }
                    operation.setEtape(Etape.PROCEED);
                    operation = operationRepository.save(operation);

                } else if (operation.getTypeService().getLibelle().equals(String.valueOf(TypeServiceLibelle.ENVOI_CASH))) {
                    if (avecFrais.equals(Boolean.TRUE)) {
                        annulationOpEnvoiCashAvecFrais(operationId);
                    } else {
                        annulationOpEnvoiCash(operationId);
                    }
                }
            } else {
                log.debug("statut --------------" + operation.getStatut().name());
            }
        }

        log.debug("resultat --------------" + operation);

        return operationMapper.toDto(operation);
    }

    /**
     * @param operationId
     * @param avecFrais
     * @return
     * @throws IOException
     */
    @Transactional
    public OperationDTO annulationCashAccount(Long operationId, Boolean avecFrais) throws IOException, JAXBException {
        log.info("Init annulation de l'operation Cash Account ......");
        Operation operation = operationRepository.findById(operationId).orElse(null);

        if (operation.getStatut().equals(StatutOperation.PAYE)) {

            if (canUpdateOperation(3, operation)) {
                UniteOrganisation organisationDest = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());
                UniteOrganisation fseDest = uniteOrganisationService.findOneById(operation.getUoAcquereur().getId());
                UniteOrganisation fseSource = operation.getUoEmetteur();
                Compte compte = operation.getCompteDest();
                UniteOrganisation uoPaysSrc = returnPays(fseSource); // uniteOrganisationService.findOneById(fseSource.getUoPaysId());
                //   UniteOrganisation uoPaysSrc = uniteOrganisationService.findUoParentByType(fseSource, TypeUO.PAYS);
                UniteOrganisation uoPaysDest = returnPays(fseDest); //uniteOrganisationService.findOneById(fseDest.getUoPaysId());
                // UniteOrganisation uoPaysDest = uniteOrganisationService.findUoParentByType(fseDest, TypeUO.PAYS);//Pays acq
                operationAuditService.saveOpAudit(operation, operation.getStatut(), StatutOperation.ANNULLE);
                transactionComptePtiService.findByOperationId(operationId);
                Result response = apiService.sendRequestComptabilisationOfCashAccountToOmnicanal(uoPaysSrc, uoPaysDest, operation, fseSource, organisationDest, compte, Boolean.FALSE, avecFrais);
                verifResultOfOmnicanal(response, operation);
                operation.setStatut(StatutOperation.ANNULLE);
                if (avecFrais) {
                    commissionServicel.annulationRepartitionCommission(operationId);
                }
                operation = operationRepository.save(operation);
                return operationMapper.toDto(operation);
            } else {
                throw new CustomParameterizedException("La pÃ©riode d'annulation autorisÃ© est depassÃ©e.");
            }
        } else {
            throw new CustomParameterizedException("Impossible d'effectuer l'annulation de cette opÃ©ration.");
        }
    }

    //codeFse & etiquette
    private void SoldeCompte() {
        Optional<TypeUniteOrganisation> typeUniteOrganisation = typeUniteOrganisationRepository.findByCodeTypeUO(TypeUO.PAYS.name());
        if (typeUniteOrganisation.isPresent()) {
            List<UniteOrganisation> paysList = uniteOrganisationService.findByType(typeUniteOrganisation.get().getId());

            for (UniteOrganisation pays : paysList) {//Liste des fournisseurs de service
                UniteOrganisation filiale = uniteOrganisationService.getFilialeOfUniteOrganisation(pays);
                //On recupere le compte de la filiale
                if (filiale != null) {
                    List<UniteOrganisationDTO> fseList = uniteOrganisationService.findUoAllFilsByType(filiale.getId(), TypeUO.FSE);

                }
            }
        }
    }

    /**
     * @param days
     * @param operation
     * @return
     */
    public Boolean canUpdateOperation(int days, Operation operation) {
        LocalDate dateOperation = operation.getDateOperation().toLocalDate();
        log.debug("-----dateOp" + dateOperation.plusDays(days) + "------" + LocalDate.now());
        if (LocalDate.now().isAfter(dateOperation.plusDays(days))) {
            return false;
        }

        return true;
    }

    /**
     * @param days
     * @param operation
     * @return
     */
    public Boolean canSendCodeRetraitOperation(int days, Operation operation) {
        LocalDate dateOperation = operation.getDateOperation().toLocalDate();
        if (LocalDate.now().isAfter(dateOperation.plusDays(days))) {
            return false;
        }
        return true;
    }

    /**
     * Fonction de generation de fichier de suivi des operations et des
     * commissions
     *
     * @return
     * @throws FileNotFoundException
     */
    public InputStream genererCsv(Pageable pageable, Long typeServiceId, Long canalId, Long uoAcquereurId, BigDecimal montantOp,
            StatutOperation statutOperation, ZonedDateTime dateOperation, ZonedDateTime dateFin,
            String codeOperation, Long organisationId) throws FileNotFoundException {
        List<StatutOperation> statut = null;
        if (statutOperation != null) {
            statut = new ArrayList();
            statut.add(statutOperation);
        }

        List<Long> pointVenteIds = getPointVenteIdsOfUserConnecte(organisationId);
        List<Operation> operations = new ArrayList<>();
        if (!pointVenteIds.isEmpty()) {
            if (dateOperation != null && dateFin != null) {
                operations.addAll(operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, dateOperation, dateFin.plusHours(23), codeOperation, pageable).getContent());
            } else if (dateOperation != null) {
                operations.addAll(operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, dateOperation, dateOperation.plusHours(23), codeOperation, pageable).getContent());
            } else if (dateFin != null) {
                operations.addAll(operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, ZonedDateTime.now().minusMonths(12), dateFin.plusHours(23), codeOperation, pageable).getContent());
            }
            operations.addAll(operationRepository.findOperationByMultiParams(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, dateOperation, dateFin, codeOperation, pageable).getContent());
        }
        new PageImpl<OperationDTO>(Collections.emptyList());
        pathname = System.getProperty("user.home") + File.separator + "operations" + RandomUtil.generateAlphaNumeric(2) + ".csv";
        final File csvFileName = new File(pathname);
        if (fileGeneratorService.generateCSV(csvFileName, getJasperOp(operations).toArray())) {
            return new FileInputStream(csvFileName);
        } else {
            throw new CustomParameterizedException("La gÃ©nÃ©ration du fichier a Ã©chouÃ©");
        }
    }

    /**
     * On supprime le fichier temporaire, gÃ©nÃ©rÃ© lors de l'exportation des
     * fichiers excel
     */
    public boolean deleteCurrentFile() {
        return new File(pathname).delete();
    }

    public String getPathname() {
        return pathname;
    }

    public void setPathname(String pathname) {
        this.pathname = pathname;
    }

    /**
     * @param operations
     * @return
     */
    public List<XOperationDTO> getJasperOp(List<Operation> operations) {
        List<XOperationDTO> operationDTOS = new ArrayList<>();
        for (Operation operation : operations) {
            TypeService typeService = operation.getTypeService();
            XOperationDTO xItem = new XOperationDTO();
            if (operation.getMotifOperation() != null) {
                xItem.setMotif(operation.getMotifOperation().getLibelle());
            }
            String ctrlCanal;
            if (operation.getCanal() != null) {
                ctrlCanal = operation.getCanal().getLibelleCanal();
            } else {
                ctrlCanal = "";
            }

            xItem.canal(ctrlCanal)
                    .dateOp(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").format(operation.getDateOperation()))
                    .montant(operation.getMontantOp())
                    .typeService(operation.getTypeService().getLibelle())
                    .montantFrais(operation.getMontantFrais())
                    .setNumero(operation.getCodeOperation());

            //Informations de l'emetteur de l'operation
            if (operation.getEmetteur() != null) {
                Personne personne = operation.getEmetteur().getPersonne();
                if (personne != null && personne.getPersonnePhysique() != null) {
                    PersonnePhysique personnePhysique = operation.getEmetteur().getPersonne().getPersonnePhysique();
                    if (personnePhysique.getPaysResidence() != null) {
                        xItem.setPaysOrigine(personnePhysique.getPaysResidence().getLibelle());
                    }
                    xItem.emetteur(personnePhysique.getNomClient().trim() + " " + personnePhysique.getPrenomClient().trim());
                } else if (operation.getEmetteur().getPersonne() != null && operation.getEmetteur().getPersonne().getPersonneMorale() != null) {
                    xItem.emetteur(operation.getEmetteur().getPersonne().getPersonneMorale().getRaisonSociale());
                }
            }

            //Informations du beneficiaire de l'operation
            if (operation.getBeneficaire() != null) {
                if (operation.getBeneficaire().getPersonne() != null && operation.getBeneficaire().getPersonne().getPersonnePhysique() != null) {
                    PersonnePhysique personnePhysique = operation.getBeneficaire().getPersonne().getPersonnePhysique();
                    xItem.beneficiaire(personnePhysique.getNomClient().trim() + " " + personnePhysique.getPrenomClient().trim());
                    if (personnePhysique.getPaysResidence() != null) {
                        xItem.setPaysBeneficiaire(personnePhysique.getPaysResidence().getLibelle());
                    }
                } else if (operation.getBeneficaire().getPersonne() != null && operation.getBeneficaire().getPersonne().getPersonneMorale() != null) {
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

            if (typeService != null && typeService.getLibelle() != null) {
                if (typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT))
                        || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT_INTERNE))) {
                    if (operation.getUoAcquereur() != null) {
                        xItem.beneficiaire(operation.getUoAcquereur().getLibelleUniteOrganisation());
                    }
                }
            }
            operationDTOS.add(xItem);
        }
        return operationDTOS.stream().sorted(Comparator.comparing(XOperationDTO::getTypeService)).collect(Collectors.toList());
    }

    public OperationOptimiseDTO findByCodeOperationEx(String codeExterne) {
        OperationOptimiseDTO op = operationRepository.findByCodeOperationEx(codeExterne);
        return op;
    }

    Operation findById(Long operId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Transactional
    public OperationDTO changeOperationStatut(@NotNull Long operationId) {
        Operation operation = operationRepository.findById(operationId).orElse(null);
        if (operation != null) {
            if (operation.getStatut().equals(StatutOperation.ENCOURS)) {
                operation.setStatut(StatutOperation.ENATTENTE);
                operationRepository.save(operation);
                operationAuditService.saveOpAudit(operation, StatutOperation.ENCOURS, operation.getStatut());
            }
        }
        return operationMapper.toDto(operation);
    }

    @Transactional
    public void annulationReglement(Long reglementId) {
        log.debug("annulation du règlement" + reglementId);
        List<LigneReglement> liste;
        List<CommissionDTO> listeComm;
        Reglement reglement = reglementService.findOne(reglementId);
        liste = ligneReglementRepository.findByReglementIdAndValideIsFalse(reglementId);
        if (liste.size() > 0) {
            for (LigneReglement ligR : liste) {

                if (ligR.getValide() == false) {
                    listeComm = commissionRepository.findListCommissionOfLigneReglementOptimise(ligR.getId());

                    listeComm.forEach(c -> {

                        commissionRepository.updateDetacherReglementCommissionById(c.getId());
                    });

                } else {
                    throw new CustomParameterizedException("Echec le Règlement contient une ligne valide");
                }
            }
            Integer nombreValide = ligneReglementRepository.countByReglementIdAndValideIsTrue(reglement.getId());
            if (nombreValide <= 0) {
                reglement.setEstAnnuler(true);
            }

            reglementService.save(reglement);
        } else {
            reglement.setEstAnnuler(true);
            reglementService.save(reglement);
        }

    }

    @Transactional
    public LigneReglement detacherReglement(Long ligneReglementId) {
        LigneReglement ligneReglement = ligneReglementRepository.findById(ligneReglementId).orElse(null);
        if (ligneReglement != null && ligneReglement.getOrganisation() != null && ligneReglement.getOperation() != null) {
            UniteOrganisation organisation = ligneReglement.getOrganisation();
            Operation operation = ligneReglement.getOperation();
            Compte compteDest = compteService.getCompteOfOrganisation(organisation, EtiquetteCompteCode.CPTEOPERATIONACTEUR);
            TransactionComptePti transp = transactionComptePtiService.findByOperationAndCompteAutre(operation.getCodeOperation(), compteDest.getCompteAutre().getId());
            if (transp != null) {
                throw new CustomParameterizedException("Erreur règlement déja comptabilisé");
            } else {
                operation.setStatut(StatutOperation.REJETE);
                ligneReglement.setEstDetacher(true);
                List<Commission> listeComm = commissionRepository.findListCommissionOfLigneReglement(ligneReglementId);
                listeComm.forEach(commission -> {
                    commission.setLigneReglement(null);
                    commission.setEstRegle(false);
                    commissionRepository.save(commission);
                });
                operationRepository.save(operation);
                ligneReglementRepository.save(ligneReglement);
            }
        }
        return ligneReglement;
    }

    public List<Operation> findOperationByCompteAutre(Long id, ZonedDateTime dateDebut, ZonedDateTime dateFin) {
        List<Operation> operationIds = new ArrayList<>();
        List<TransactionComptePti> transactionComptePtis = new ArrayList<>();
        System.out.println("----------");
        System.out.println("----------" + id);
        if (dateDebut != null && dateFin != null) {
            transactionComptePtis = transactionComptePtiService.findByCompteAutre(id, dateDebut, dateFin);
        } else if (dateDebut != null) {
            transactionComptePtis = transactionComptePtiService.findByCompteAutre(id, dateDebut, dateDebut);
        } else if (dateFin != null) {
            transactionComptePtis = transactionComptePtiService.findByCompteAutreId(id, dateFin);
        } else {
            transactionComptePtis = transactionComptePtiService.findByCompteAutreId(id, null);
        }
        for (TransactionComptePti t : transactionComptePtis) {
            operationIds.add(t.getOperation());
        }
        return operationIds;
    }

    public List<Operation> findOperationByPointVenteIds(List<Long> pointVentes, ZonedDateTime dateDebut, ZonedDateTime dateFin) {
        if (!pointVentes.isEmpty()) {
            if (dateDebut != null && dateFin != null) {
                return operationRepository.findAllOperationByMultiParams(null, null, null, pointVentes, null, null, dateDebut, dateFin.plusHours(23));
            } else if (dateDebut != null) {
                return operationRepository.findAllOperationByMultiParams(null, null, null, pointVentes, null, null, dateDebut, dateFin.plusHours(23));
            } else if (dateFin != null) {
                return operationRepository.findAllOperationByMultiParams(null, null, null, pointVentes, null, null, dateDebut, dateFin.plusHours(23));
            }
            return operationRepository.findAllOperationByMultiParams(null, null, null, pointVentes, null, null, null, null);
        }
        return null;
    }

    @Transactional
    public OperationDTO annulationDesOperation(Long operationId) {
        log.debug("Request to cancel opération" + operationId);
        Operation operation = operationRepository.getOne(operationId);
        if (operation != null) {
            if (operation.getId() != null && (operation.getStatut().equals(StatutOperation.ENATTENTE) || operation.getStatut().equals(StatutOperation.SUSPENDU))) {
                operation.setStatut(StatutOperation.ENATTENTE_ANNULATION);
                operationRepository.save(operation);
                return operationMapper.toDto(operation);
            } else {
                throw new CustomParameterizedException("Opération introuvable ou statut non conforme");
            }
        } else {
            throw new CustomParameterizedException("Opération introuvable");
        }

    }

    public EvenementOperationDTO traitementEvenment(EvenementOperationDTO evenementOperationDTO) {
        User userConnecte = userService.getUserWithAuthorities();
        EvenementOperation evenementOperation = evenementOperationRepository.findById(evenementOperationDTO.getId()).orElse(null);
        if (userConnecte != null) {
            if (evenementOperationDTO.getEstLevee() == 0) {
                throw new CustomParameterizedException("Echec de l'opération evènement déja levé");
            } else if (userConnecte.getLogin().equals(evenementOperation.getLogin())) {
                throw new CustomParameterizedException("Echec de l'opération! vous ne pouvez pas levé l'evènement que vous avez créez");
            } else {
                evenementOperation.setEstLevee(evenementOperationDTO.getEstLevee());
                evenementOperation.setLogin(userConnecte.getLogin());
                evenementOperation.setMotif(evenementOperationDTO.getMotif());
                evenementOperationRepository.save(evenementOperation);
            }
            return evenementOperationMapper.toDto(evenementOperation);
        } else {
            throw new CustomParameterizedException("Utilisateur introuvable!");
        }
    }

    public EvenementOperationDTO saveEvenementOfClient(OperationAllDTO operationAllDTO) {
        User userConnecte = userService.getUserWithAuthorities();
        Client client = clientRepository.findByPersonneIdAndSupprimeIsFalse(operationAllDTO.getEmetteur().getPersonneDTO().getId());
        EvenementOperation evenementOperation = new EvenementOperation();
        Operation operation = new Operation();
        evenementOperation.setLogin(userConnecte.getLogin());
        TypeService typeService = typeServiceService.findByCodeService(String.valueOf(TypeServiceLibelle.BLOCAGE_MONTANT));
        if (client != null) {
            BigDecimal moinsUn = BigDecimal.valueOf(-1L);
            Compte cpt = compteService.findComptePrincipal(client);
            operation.setMontantOp(operationAllDTO.getOperationDTO().getMontantOp());
            operation.description(operationAllDTO.getOperationDTO().getMotif());
            operation.compteSrc(cpt);
            operation.setDateOperation(ZonedDateTime.now());
            operation.setEmetteur(operation.getEmetteur());
            operation.setEtape(Etape.PROCEED);
            operation.statut(StatutOperation.ENATTENTE);
            operation.setTypeService(typeService);
            operationRepository.save(operation);
            evenementOperation.setOperation(operation);
            evenementOperation.setMontant(operationAllDTO.getOperationDTO().getMontantOp().multiply(moinsUn));
            evenementOperation.setCompte(cpt);
            evenementOperation.setMotif(operationAllDTO.getOperationDTO().getMotif());
            evenementOperation.setEstLevee(0);
            evenementOperationRepository.save(evenementOperation);

        }
        return evenementOperationMapper.toDto(evenementOperation);

    }

    public List<OperationOptimiseDTO> getTop10Operations(StatutOperation statutOperation, Long uoId) {
        List<String> statut = null;
        List<Long> pointVenteIds = getPointVenteIdsOfUserConnecteOptimise(uoId);
        if (statutOperation != null) {
            statut = new ArrayList();
            if (statutOperation.equals(StatutOperation.ENATTENTE_VALIDATION)) {
                statut.add(StatutOperation.ENATTENTE_VALIDATION.name());
                statut.add(StatutOperation.ENATTENTE_ANNULATION.name());
            } else {
                statut.add(statutOperation.name());
            }
        } else {
            StatutOperation[] allStatut = StatutOperation.values();
            statut = new ArrayList();
            for (StatutOperation statutAll : allStatut) {
                if (!statutAll.equals(StatutOperation.ENATTENTE_ANNULATION)) {
                    statut.add(statutAll.name());
                }
            }
        }
        if (!pointVenteIds.isEmpty()) {
            return operationRepository.findTop10LastOperation(statut, pointVenteIds);
        }
        return Collections.emptyList();
    }

    public List<Long> getPointVenteIdsOfUserConnecteOptimise(Long organisationId) {
        User user = userService.getUserWithAuthorities();
        PointVente pointVente = pointVenteService.getPointVenteOfUserConnecteOptimise(user);
        List<Long> pointVenteIds = new ArrayList<>();
        if (!user.getAuthorities().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AuthoritiesConstants.ADMIN) || a.getName().equalsIgnoreCase(AuthoritiesConstants.VIEW_ALL_OPERATION))) {
            if (organisationId != null) {
                pointVenteService.findAllByUOOptimise(organisationId).forEach(pv -> {
                    pointVenteIds.add(pv.getId());
                });
            } else {
                pointVenteService.findAllIdOptimise().forEach(pv -> {
                    pointVenteIds.add(pv);
                });
            }
        } else if (pointVente != null) {
            pointVenteIds.add(pointVente.getId());
        } else {
            UniteOrganisation organisation = user.getOrganisation();
            log.debug("user" + user.getUniteOrganisation().getId());
            log.debug("organisation { }" + user.getOrganisation().getId());
            List<PointVente> litepointVentes = pointVenteService.findPointVenteByOrganisation(organisation.getId());
            Set<PointVente> pointVentes = litepointVentes.stream().collect(Collectors.toSet());
            //organisation.getPointVentes();
            if (pointVentes != null) {
                pointVentes.forEach(pv -> {
                    pointVenteIds.add(pv.getId());
                });
            }
        }
        return pointVenteIds;
    }

    public Page<OperationDTO> findByMultiParamsOptimise(Pageable pageable, Long typeServiceId, Long canalId, Long uoAcquereurId, BigDecimal montantOp,
            StatutOperation statutOperation, ZonedDateTime dateOperation, ZonedDateTime dateFin,
            String codeOperation, Long organisationId, String description) {
        Long taille = new Long(pageable.getPageSize());
        Long page = new Long(pageable.getPageSize() * pageable.getPageNumber());
        List<String> statut = null;
        List<StatutOperation> statutCount = null;

        List<Long> pointVenteIds = getPointVenteIdsOfUserConnecteOptimise(organisationId);
        StatutOperation statutAnnuler = null;
        // Pageable pageable1;
        Pageable pageable1 = new Pageable() {
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
        List<OperationDTO> operationDTOS;
        int totalOp = 0;
        if (statutOperation != null) {
            statut = new ArrayList();
            statutCount = new ArrayList();
            if (statutOperation.equals(StatutOperation.ENATTENTE_VALIDATION)) {
                statut.add(StatutOperation.ENATTENTE_VALIDATION.name());
                statut.add(StatutOperation.ENATTENTE_ANNULATION.name());
                statutCount.add(StatutOperation.ENATTENTE_VALIDATION);
            } else {
                statut.add(statutOperation.name());
                statutCount.add(statutOperation);
            }
        } else {
            StatutOperation[] allStatut = StatutOperation.values();
            statut = new ArrayList();
            statutCount = new ArrayList();
            for (StatutOperation statutAll : allStatut) {
                if (!statutAll.equals(StatutOperation.ENATTENTE_ANNULATION)) {
                    statutCount.add(statutAll);
                    statut.add(statutAll.name());
                }
            }
        }
        if (!pointVenteIds.isEmpty()) {
            if (dateOperation != null && dateFin != null) {
                totalOp = operationRepository.countByMultiParamsOptimise(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statutCount, dateOperation, dateFin.plusHours(23), codeOperation, description);
                operationDTOS = operationRepository.findOperationByMultiParamsOptimise(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, codeOperation, description, dateOperation, dateFin.plusHours(23), taille, page);
            } else if (dateOperation != null) {
                totalOp = operationRepository.countByMultiParamsOptimise(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statutCount, dateOperation, dateOperation.plusHours(23), codeOperation, description);
                operationDTOS = operationRepository.findOperationByMultiParamsOptimise(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, codeOperation, description, dateOperation, dateOperation.plusHours(23), taille, page);
            } else if (dateFin != null) {
                totalOp = operationRepository.countByMultiParamsOptimise(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statutCount, ZonedDateTime.now().minusMonths(12), dateFin.plusHours(23), codeOperation, description);
                operationDTOS = operationRepository.findOperationByMultiParamsOptimise(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, codeOperation, description, ZonedDateTime.now().minusMonths(12), dateFin.plusHours(23), taille, page);
            } else {
                totalOp = operationRepository.countByMultiParamsOptimise(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statutCount, dateOperation, dateFin, codeOperation, description);
                operationDTOS = operationRepository.findOperationByMultiParamsOptimise(typeServiceId, canalId, uoAcquereurId, pointVenteIds, montantOp, statut, codeOperation, description, dateOperation, dateFin, taille, page);
            }
            int start = pageable1.getPageNumber();
            int end = (start + pageable.getPageSize()) > operationDTOS.size() ? operationDTOS.size() : (start + pageable1.getPageSize());
            Page<OperationDTO> retour = new PageImpl<>(operationDTOS, pageable1, totalOp);
            return retour;
        }
        return new PageImpl<OperationDTO>(Collections.emptyList());
    }

    @Transactional(readOnly = true)
    public OperationAllDTO findOneOptimise(Long id) {
        OperationDTO operation = operationRepository.findOperationById(id);
        PersonneAllDTO beneficiaire = null;
        PersonneAllDTO emetteur = null;
        CompteDTO compteDTOEmetteur = null;
        CompteDTO compteDTOBeneficiaire = null;
        //CompteDTO compteDTOEmetteur=new CompteDTO();
        CompteDTO compteSrc = new CompteDTO();
        CompteDTO compteDest = new CompteDTO();
        if (operation != null) {
            UniteOrganisationDTO uoAcq = uniteOrganisationRepository.findDtoById(operation.getUoAcquereurId());
            OperationAllDTO operationAllDTO = new OperationAllDTO();
            if (operation.getBeneficaireId() != null) {
                beneficiaire = personneService.finAllInfoOfPersonneOptimise(beneficiaireRepository.getPersonneIdByBeneficiaireId(operation.getBeneficaireId()));
                if (operation.getPieceBeneficiaireId() != null) {
                    beneficiaire.setPieceDTO(pieceMapper.toDto(pieceRepository.findById(operation.getPieceBeneficiaireId()).orElse(null)));
                }
                if (operation.getBeneficaireId().equals(operation.getEmetteurId())) {
                    operationAllDTO.setTypeBeneficiaire("Titulaire");
                    if (operation.getBeneficaireId() != null) {
                        if (operation.getBeneficaireId().equals(operation.getEmetteurId())) {
                            operationAllDTO.setTypeBeneficiaire("Titulaire");
                        }
                    } else {
                        operationAllDTO.setTypeBeneficiaire("Autre");
                    }
                }
            }
            if (operation.getEmetteurId() != null) {
                emetteur = personneService.finAllInfoOfPersonneOptimise(emetteurRepository.getPersonneIdByEmetteurId(operation.getEmetteurId()));
                if (operation.getPieceEmetteurId() != null) {
                    emetteur.setPieceDTO(pieceMapper.toDto(pieceRepository.findById(operation.getPieceEmetteurId()).orElse(null)));
                }
            }

            if (operation.getCompteSrcId() != null) {
                compteSrc = compteRepository.findCompteByIdReport(operation.getCompteSrcId());
                if (compteSrc.getClientId() != null) {
                    ClientDTO clientSrc = clientRepository.findClientByClientIdOptimise(compteSrc.getClientId());
                    if (emetteur != null) {
                        emetteur.setClientDTO(clientSrc);
                        getCompteCbisOptimise(emetteur, compteSrc);
                    }
                }

                if (compteSrc.getUniteOrganisationId() != null) {
                    compteDTOEmetteur = compteRepository.findByNativeId(compteSrc.getId());
                    operation.setUoEmetteurLibelleUniteOrganisation(uniteOrganisationRepository.getLibelleUniteOrganisation(compteSrc.getUniteOrganisationId()));
                    System.out.println("commmmmmppppp");
                    System.out.println("commmmmmppppp" + uniteOrganisationRepository.getLibelleUniteOrganisation(compteSrc.getUniteOrganisationId()));
                    if (compteDTOEmetteur != null) {
                        compteDTOEmetteur.setUniteOrganisationLibelleUniteOrganisation(uniteOrganisationRepository.getLibelleUniteOrganisation(compteSrc.getUniteOrganisationId()));
                    }

                }

            }
            if (operation.getCompteDestId() != null) {
                compteDest = compteRepository.findCompteByIdReport(operation.getCompteDestId());
                if (compteDest.getClientId() != null) {
                    ClientDTO clientDest = clientRepository.findClientByClientIdOptimise(compteDest.getClientId());
                    if (beneficiaire != null) {
                        beneficiaire.setClientDTO(clientDest);
                        getCompteCbisOptimise(beneficiaire, compteDest);
                    }
                }

                if (compteDest.getUniteOrganisationId() != null) {
                    compteDTOBeneficiaire = compteRepository.findByNativeId(compteDest.getId());
                    operation.setUoAcquereurLibelleUniteOrganisation(uniteOrganisationRepository.getLibelleUniteOrganisation(compteDest.getUniteOrganisationId()));
                    if (compteDTOBeneficiaire != null) {
                        compteDTOBeneficiaire.setUniteOrganisationLibelleUniteOrganisation(uniteOrganisationRepository.getLibelleUniteOrganisation(compteDest.getUniteOrganisationId()));
                    }
                }

            }

            TypeServiceDTO typeService = typeServiceRepository.findOneOptimise(operation.getTypeServiceId());
            operation.setCompteSrcValeurCompte(compteSrc.getValeurCompte());
            operation.setCompteDestValeurCompte(compteDest.getValeurCompte());
            OperationDTO operationDTO = operation;
            if (typeService.getCode().equals(TypeServiceLibelle.REGULATION.name()) || (typeService.isOperationClient() != null && typeService.isOperationClient())) {
                UniteOrganisationDTO fseAcq = uniteOrganisationRepository.findDtoById(operation.getUoAcquereurId());
                UniteOrganisationDTO fseEm = uniteOrganisationRepository.findDtoById(operation.getUoEmetteurId());
                UniteOrganisationDTO uoPayAcq = returnPaysOptimise(fseAcq);
                UniteOrganisationDTO uoPayEm = returnPaysOptimise(fseEm);
                operationDTO.setUoAcquereurId(uoPayAcq != null ? uoPayAcq.getId() : null);
                operationDTO.setUoEmetteurId(uoPayEm != null ? uoPayEm.getId() : null);
                operationDTO.setFseSrc(uniteOrganisationMapper.toEntity(fseEm));
                operationDTO.setFseDest(uniteOrganisationMapper.toEntity(fseAcq));
            } else if (uoAcq != null && !uoAcq.getTypeUOCode().equals(TypeUO.PAYS.toString())) {
                UniteOrganisationDTO uoPays = returnPaysOptimise(uoAcq); //uniteOrganisationService.findOneById(uoAcq.getUoPaysId());
                operationDTO.setUoAcquereurId(uoPays != null ? uoPays.getId() : null);
            }
            operationAllDTO.setBeneficiaire(beneficiaire != null ? beneficiaire : beneficiaire);
            operationAllDTO.setEmetteur(emetteur != null ? emetteur : emetteur);
            operationAllDTO.setCompteDTOEmetteur(compteDTOEmetteur != null ? compteDTOEmetteur : compteDTOEmetteur);
            operationAllDTO.setCompteDTOBeneficiaire(compteDTOBeneficiaire != null ? compteDTOBeneficiaire : compteDTOBeneficiaire);
            operationAllDTO.setOperationDTO(operationDTO);
            return operationAllDTO;
        }
        return null;
    }

    private void getCompteCbisOptimise(PersonneAllDTO personne, CompteDTO compte) {
        CompteCBI compteCBI = new CompteCBI();
        List<CompteCBI> compteCBIS = new ArrayList<>();
        if (compte.getCompteBanqueId() != null) {
            CompteBanque compteBanque = compteBanqueRepository.findById(compte.getCompteBanqueId()).orElse(null);
            compteCBI.setCodeBanque(compteBanque.getCodeBanque());
            compteCBI.setCodeAgence(compteBanque.getCodeAgence());
            compteCBIS.add(compteCBI);
            compteCBI.setNumeroCompte(compte.getValeurCompte());
            compteCBI.setType("Banque");
        } else {
            compteCBI.setType("Autre");
            compteCBI.setNumeroCompte(compte.getValeurCompte());
        }
        personne.setCompteCBIDTO(compteCBI);
    }

    private UniteOrganisationDTO returnPaysOptimise(UniteOrganisationDTO organisation) {
        UniteOrganisationDTO uoPays;
        if (organisation != null && !organisation.getTypeUOCode().equals(TypeUO.PAYS.name())) {
            uoPays = uniteOrganisationRepository.findDtoById(organisation.getUoPaysId());
        } else {
            uoPays = organisation;
        }
        return uoPays;
    }

    public List<XOperationDTO> getJasperOpOptimise(List<OperationDTO> operations) {
        List<XOperationDTO> operationDTOS = new ArrayList<>();
        for (OperationDTO operation : operations) {
            TypeServiceDTO typeService = typeServiceRepository.findOneOptimise(operation.getTypeServiceId());
            XOperationDTO xItem = new XOperationDTO();
            if (operation.getMotifOperationId() != null) {
                xItem.setMotif(motifOperationRepository.findById(operation.getMotifOperationId()).orElse(null).getLibelle());
            }
            String ctrlCanal;
            if (operation.getCanalId() != null) {
                ctrlCanal = canalRepository.getLibelleCanalById(operation.getCanalId());
            } else {
                ctrlCanal = "";
            }
            xItem.setCodeExterne(operation.getCodeExterne());
            xItem.canal(ctrlCanal)
                    .dateOp(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(operation.getDateOperation()))
                    .montant(operation.getMontantOp())
                    .typeService(typeService.getLibelle())
                    .montantFrais(operation.getMontantFrais())
                    .setNumero(operation.getCodeOperation());

            //Informations de l'emetteur de l'operation
            if (operation.getEmetteurId() != null) {
                log.debug("-------------------");
                log.debug("-------------------" + operation.getEmetteurId());
                Long emetteurId = emetteurRepository.getPersonneIdByEmetteurId(operation.getEmetteurId());
                if (emetteurId != null) {
                    PersonneDTO personne = personneRepository.findPersonneById(emetteurId);
                    if (personne != null && personne.getPersonnePhysiqueId() != null) {
                        xItem.setTelephoneEmet(personne.getTelephone());
                        PersonnePhysiqueDTO personnePhysique = personnePhysiqueRepository.findByIdReport(personne.getPersonnePhysiqueId());
                        if (personnePhysique.getPaysResidenceId() != null) {
                            xItem.setPaysOrigine(nationaliteRepository.getLibelleById(personnePhysique.getPaysResidenceId()));
                        }
                        xItem.emetteur(personnePhysique.getNomClient().trim() + " " + personnePhysique.getPrenomClient().trim());
                    } else if (personne != null && personne.getPersonneMoraleId() != null) {
                        xItem.emetteur(personneMoraleRepository.findById(personne.getPersonneMoraleId()).orElse(null).getRaisonSociale());
                    }
                }

            }

            //Informations du beneficiaire de l'operation
            if (operation.getBeneficaireId() != null) {
                Long benefId = beneficiaireRepository.getPersonneIdByBeneficiaireId(operation.getBeneficaireId());
                if (benefId != null) {
                    PersonneDTO personne = personneRepository.findPersonneById(benefId);
                    if (personne != null && personne.getPersonnePhysiqueId() != null) {
                        xItem.setTelephoneBenef(personne.getTelephone());
                        PersonnePhysiqueDTO personnePhysique = personnePhysiqueRepository.findByIdReport(personne.getPersonnePhysiqueId());
                        xItem.beneficiaire(personnePhysique.getNomClient().trim() + " " + personnePhysique.getPrenomClient().trim());
                        if (personnePhysique.getPaysResidenceId() != null) {
                            xItem.setPaysBeneficiaire(nationaliteRepository.getLibelleById(personnePhysique.getPaysResidenceId()));
                        }
                    } else if (personne != null && personne.getPersonneMoraleId() != null) {
                        xItem.beneficiaire(personneMoraleRepository.findById(personne.getPersonneMoraleId()).orElse(null).getRaisonSociale());
                    }
                }
            }

            /*Compte compteDest = operation.getCompteDest();*/
            if (operation.getCompteDestId() != null) {
                CompteDTO compteDest = compteRepository.findCompteByIdReport(operation.getCompteDestId());
                xItem.setNumeroCompteBeneficiaire(compteDest.getValeurCompte());
                if (compteDest.getCompteBanqueId() != null) {
                    String cfse = compteBanqueRepository.getCodeFseById(compteDest.getCompteBanqueId());
                    if (cfse != null) {
                        xItem.setNomBanqueBeneficiaire(uniteOrganisationRepository.getLibelleUniteOrganisationByCodeFse(cfse));
                    }
                } else if (compteDest.getCompteAutreId() != null) {
                    String cfseAutre = compteAutreRepository.getCodeFseById(compteDest.getCompteAutreId());
                    xItem.setNomBanqueBeneficiaire(uniteOrganisationRepository.getLibelleUniteOrganisationByCodeFse(cfseAutre));
                }
                if (compteDest.getClientId() != null) {
                    ClientDTO clientDTO = clientRepository.findClientByClientIdOptimise(compteDest.getClientId());
                    if (clientDTO != null && clientDTO.getGroupeProfilActeurId() != null) {
                        String nomGroupe = groupeProfilActeurRepository.getNomGroupeProfilActeur(clientDTO.getGroupeProfilActeurId());
                        xItem.setProfilBeneficiaire(nomGroupe);
                    }
                }
            }
            if (operation.getCompteSrcId() != null) {
                CompteDTO compteSrc = compteRepository.findCompteByIdReport(operation.getCompteSrcId());
                if (compteSrc.getClientId() != null) {
                    ClientDTO clientDTO = clientRepository.findClientByClientIdOptimise(compteSrc.getClientId());
                    if (clientDTO != null && clientDTO.getGroupeProfilActeurId() != null) {
                        String nomGroupe = groupeProfilActeurRepository.getNomGroupeProfilActeur(clientDTO.getGroupeProfilActeurId());
                        xItem.setProfilEmetteur(nomGroupe);
                    }
                }
            }

            if (operation.getUoEmetteurId() != null) {
                xItem.uoEm(uniteOrganisationRepository.getLibelleUniteOrganisation(operation.getUoEmetteurId()));
            }

            if (typeService != null && typeService.getLibelle() != null) {
                if (typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT))
                        || typeService.getLibelle().equals(String.valueOf(TypeServiceLibelle.APPROVISIONNEMENT_INTERNE))) {
                    if (operation.getUoAcquereurId() != null) {
                        xItem.beneficiaire(uniteOrganisationRepository.getLibelleUniteOrganisation(operation.getUoAcquereurId()));
                    }
                }
            }
            operationDTOS.add(xItem);
        }
        return operationDTOS.stream().sorted(Comparator.comparing(XOperationDTO::getTypeService)).collect(Collectors.toList());
    }

    @Transactional
    public CompteDTO changerStatutCompteBanque(Long id) {

        User curentUser = userService.findeByLogin(SecurityUtils.getCurrentUserLogin().toLowerCase());

        Compte compte = compteRepository.findById(id).orElse(null);

        if (compte != null && (compte.getCategorieCompte().equals(CategorieCompte.BANQUE) || compte.getCategorieCompte().equals(CategorieCompte.CARTE))) {
            if (compte.getStatutCompte() != null && compte.getStatutCompte().equals(StatutCompte.ACTIVE)) {
                compte.setStatutCompte(StatutCompte.DESACTIVE);
                compte = compteRepository.save(compte);
                if (compte.getClient() != null) {
                    Client clientDTO = clientRepository.findById(compte.getClient().getId()).orElse(null);
                    String telephone = clientDTO.getPersonne().getTelephone();
                    String codeTelephone = clientDTO.getPersonne().getCodePaysTelephone();
                    String typePersonne = clientDTO.getPersonne().getTypePersonne().name();
                    if (compte != null) {
                        userControlService.addAccount(telephone, codeTelephone, typePersonne);
                    }
                }

            } else {
                log.debug("***********----------OK-------***********");
                if (curentUser.getId() != null) {
                    if (compte.getCreatedBy() != null && compte.getCreatedBy().equals(curentUser.getLogin())) {
                        throw new CustomParameterizedException("Impossible de valider ce compte !!!");
                    }
                }
                compte.setStatutCompte(StatutCompte.ACTIVE);
                compte.setEtatCompte(EtatCompte.OUVERT);
                TypeService etiquette = typeServiceRepository.findTopByCode(TypeServiceLibelle.ACTIVE_COMPTE_BANK.name());
                compte = compteRepository.save(compte);
                if (etiquette != null && compte != null && compte.getClient() != null) {
                    envoiMsgOperation(etiquette, compte.getClient().getPersonne());
                }
            }

            log.debug("***********-----------------***********" + compte.getStatutCompte());

        }

        return compteMapper.toDto(compte);
    }

    @Transactional(readOnly = true)
    public List<MOperationNumbLastestDTO> findLastThreeOperation(String code, String telephone) {
        log.debug("Request to get 3 last operation for a client");
        PersonneDTO personne = personneRepository.findPersonneByPhoneAndCodeIndicatif(telephone, code);
        return operationRepository.findLast3Operation(personne.getId());

    }

    //Ajout liste statut
    @Transactional(readOnly = true)
    public List<OperationStatutDTO> operationStatut(@NotNull Long operationId) {
        return operationRepository.getOperationStatut(operationId);
    }

    public List<OperationDetailsDTO> getOperationDetails(String date, Integer nombre) {
        List<OperationDetailsDTO> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        ZonedDateTime dateDebut = localDate.atStartOfDay(ZoneId.of("UTC"));
        //ZonedDateTime dateDebut=ZonedDateTime.now();
        ZonedDateTime dateFin = dateDebut.plusHours(23);
        System.out.println("Date debut {}" + dateDebut);
        System.out.println("Date fin {}" + dateFin);
        List<Operation> list = operationRepository.findByDateLimite(dateDebut, dateFin, nombre.longValue());
        if (list.size() > 0) {
            list.forEach(operationDTO -> {
                OperationDetailsDTO operationDetailsDTO = new OperationDetailsDTO();
                operationDetailsDTO.setCodeOperation(operationDTO.getCodeOperation());
                operationDetailsDTO.setDateOperation(operationDTO.getDateOperation());
                operationDetailsDTO.setCodeExterne(operationDTO.getCodeExterne());
                operationDetailsDTO.setMontantOp(operationDTO.getMontantOp());
                operationDetailsDTO.setMontantFrais(operationDTO.getMontantFrais());
                operationDetailsDTO.setMontantFraisAcq(operationDTO.getMontantFraisAcq());
                operationDetailsDTO.setMontantFraisClient(operationDTO.getMontantFraisClient());
                operationDetailsDTO.setMontantTaxe(operationDTO.getMontantTaxe());
                operationDetailsDTO.setMontantFraisEm(operationDTO.getMontantFraisEm());
                operationDetailsDTO.setDescription(operationDTO.getDescription());
                if (operationDTO.getStatut() != null) {
                    operationDetailsDTO.setStatut(operationDTO.getStatut().name());
                }
                if (operationDTO.getTypeService() != null) {
                    operationDetailsDTO.setCodeService(operationDTO.getTypeService().getCode());
                }
                operationDetailsDTO.setMotif(operationDTO.getMotif());
                List<TableauFraisDTO> tableau = tableauFraisService.findAllByMultiC(operationDTO.getTypeService() != null ? operationDTO.getTypeService().getId() : null, operationDTO.getCanal() != null ? operationDTO.getCanal().getId() : null, operationDTO.getBeneficaire() != null ? operationDTO.getBeneficaire().getId() : null, operationDTO.getEmetteur() != null ? operationDTO.getEmetteur().getId() : null, null, null);
                if (tableau.size() > 0) {
                    operationDetailsDTO.setFraisAppliquer(true);
                } else {
                    operationDetailsDTO.setFraisAppliquer(false);
                }
                if (operationDTO.getMontantFrais() != null && operationDTO.getMontantFrais().compareTo(BigDecimal.ZERO) > 0) {
                    operationDetailsDTO.setRaisonFrais("Frais appliquer avec le montant {} " + operationDTO.getMontantFrais());
                }
                List<Commission> listCom = commissionRepository.findByOperationIdAndEstComptabiliseAndEtat(operationDTO.getId(), Boolean.TRUE, EtatValidite.VALIDE);
                if (listCom.size() > 0) {
                    operationDetailsDTO.setCommissionGenerer(true);
                    operationDetailsDTO.setListeCommission(listCom);
                }
                result.add(operationDetailsDTO);
            });
        }
        return result;
    }

}
