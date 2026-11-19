package com.gateway.payment.fidelite.api;

import com.gateway.payment.fidelite.domain.Campagne;
import com.gateway.payment.fidelite.domain.CoalitionMembre;
import com.gateway.payment.fidelite.domain.Mission;
import com.gateway.payment.fidelite.domain.NotificationFidelite;
import com.gateway.payment.fidelite.domain.Programme;
import com.gateway.payment.fidelite.repository.CampagneRepository;
import com.gateway.payment.fidelite.repository.CoalitionMembreRepository;
import com.gateway.payment.fidelite.repository.MissionRepository;
import com.gateway.payment.fidelite.repository.ProgrammeRepository;
import com.gateway.payment.fidelite.service.CommercialActivation;
import com.gateway.payment.fidelite.service.ExpirationService;
import com.gateway.payment.fidelite.service.FideliteService;
import com.gateway.payment.fidelite.service.FideliteService.CatalogueView;
import com.gateway.payment.fidelite.service.FideliteService.RecompenseView;
import com.gateway.payment.fidelite.service.NotificationService;
import com.gateway.payment.fidelite.service.PilotageService;
import com.gateway.payment.fidelite.service.TransfertService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/fidelite")
public class ActivationController {

    private final FideliteService fidelite;
    private final CommercialActivation commercial;
    private final ExpirationService expiration;
    private final TransfertService transferts;
    private final PilotageService pilotage;
    private final NotificationService notifications;
    private final ProgrammeRepository programmes;
    private final CampagneRepository campagnes;
    private final MissionRepository missions;
    private final CoalitionMembreRepository membres;

    public ActivationController(
            FideliteService fidelite,
            CommercialActivation commercial,
            ExpirationService expiration,
            TransfertService transferts,
            PilotageService pilotage,
            NotificationService notifications,
            ProgrammeRepository programmes,
            CampagneRepository campagnes,
            MissionRepository missions,
            CoalitionMembreRepository membres
    ) {
        this.fidelite = fidelite;
        this.commercial = commercial;
        this.expiration = expiration;
        this.transferts = transferts;
        this.pilotage = pilotage;
        this.notifications = notifications;
        this.programmes = programmes;
        this.campagnes = campagnes;
        this.missions = missions;
        this.membres = membres;
    }

    @PutMapping("/programmes/{id}/validite")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validite(@PathVariable UUID id, @RequestParam(required = false) Integer mois) {
        CurrentUser.exigerExploitation();
        fidelite.definirValidite(id, mois);
    }

    @PostMapping("/programmes/{id}/campagnes")
    @ResponseStatus(HttpStatus.CREATED)
    public CampagneView creerCampagne(@PathVariable UUID id, @Valid @RequestBody CampagneRequest request) {
        CurrentUser.exigerExploitation();
        Campagne campagne = new Campagne();
        campagne.setTenantId(tenant());
        campagne.setProgramme(programme(id));
        renseigner(campagne, request);
        campagne.setActif(true);
        return vue(campagnes.save(campagne));
    }

    @PutMapping("/programmes/{id}/campagnes/{campagneId}")
    public CampagneView modifierCampagne(
            @PathVariable UUID id,
            @PathVariable UUID campagneId,
            @Valid @RequestBody CampagneRequest request
    ) {
        CurrentUser.exigerExploitation();
        Campagne campagne = campagnes.findByIdAndProgramme_Id(campagneId, id)
                .orElseThrow(() -> new IllegalArgumentException("Cette campagne n'appartient pas à ce programme"));
        renseigner(campagne, request);
        return vue(campagnes.save(campagne));
    }

    @GetMapping("/programmes/{id}/campagnes")
    public List<CampagneView> campagnes(@PathVariable UUID id) {
        CurrentUser.exigerExploitation();
        return campagnes.findByProgramme_IdOrderByPrioriteDesc(id).stream()
                .map(ActivationController::vue)
                .toList();
    }

    @PostMapping("/programmes/{id}/missions")
    @ResponseStatus(HttpStatus.CREATED)
    public MissionView creerMission(@PathVariable UUID id, @Valid @RequestBody MissionRequest request) {
        CurrentUser.exigerExploitation();
        Mission mission = new Mission();
        mission.setTenantId(tenant());
        mission.setProgramme(programme(id));
        renseigner(mission, request);
        mission.setActif(true);
        return vue(missions.save(mission));
    }

    @PutMapping("/programmes/{id}/missions/{missionId}")
    public MissionView modifierMission(
            @PathVariable UUID id,
            @PathVariable UUID missionId,
            @Valid @RequestBody MissionRequest request
    ) {
        CurrentUser.exigerExploitation();
        Mission mission = missions.findByIdAndProgramme_Id(missionId, id)
                .orElseThrow(() -> new IllegalArgumentException("Cette mission n'appartient pas à ce programme"));
        renseigner(mission, request);
        return vue(missions.save(mission));
    }

    @GetMapping("/programmes/{id}/missions")
    public List<MissionView> missions(@PathVariable UUID id) {
        CurrentUser.exigerExploitation();
        return missions.findByProgramme_IdAndActifTrueOrderByCodeAsc(id).stream()
                .map(ActivationController::vue)
                .toList();
    }

    @GetMapping("/catalogue-recompenses")
    public List<CatalogueView> catalogue() {
        CurrentUser.exigerExploitation();
        return fidelite.catalogue(CurrentUser.tenantId());
    }

    @PostMapping("/catalogue-recompenses")
    @ResponseStatus(HttpStatus.CREATED)
    public CatalogueView creerCatalogue(@Valid @RequestBody CatalogueRequest request) {
        CurrentUser.exigerExploitation();
        return fidelite.creerCatalogue(CurrentUser.tenantId(), saisieCatalogue(request));
    }

    @PutMapping("/catalogue-recompenses/{code}")
    public CatalogueView modifierCatalogue(
            @PathVariable String code,
            @Valid @RequestBody CatalogueRequest request
    ) {
        CurrentUser.exigerExploitation();
        return fidelite.modifierCatalogue(CurrentUser.tenantId(), code, saisieCatalogue(request));
    }

    @PostMapping("/programmes/{id}/recompenses")
    @ResponseStatus(HttpStatus.CREATED)
    public RecompenseView recompense(@PathVariable UUID id, @Valid @RequestBody RecompenseRequest request) {
        CurrentUser.exigerExploitation();
        return fidelite.ajouterRecompense(id, dynamique(request));
    }

    @PutMapping("/programmes/{id}/recompenses/{code}")
    public RecompenseView modifierRecompense(
            @PathVariable UUID id,
            @PathVariable String code,
            @Valid @RequestBody RecompenseRequest request
    ) {
        CurrentUser.exigerExploitation();
        return fidelite.modifierRecompense(id, code, dynamique(request));
    }

    @PostMapping("/programmes/{id}/transferts")
    public TransfertService.Resultat transferer(@PathVariable UUID id, @Valid @RequestBody TransfertRequest request) {
        return transferts.transferer(tenant(), id, party(), request.destinataireId(), request.points());
    }

    @PostMapping("/conversions")
    public TransfertService.Resultat convertir(@Valid @RequestBody ConversionRequest request) {
        return transferts.convertir(tenant(), request.sourceProgrammeId(), request.cibleProgrammeId(), party(), request.points());
    }

    @PostMapping("/programmes/{id}/monetiser")
    public TransfertService.Resultat monetiser(@PathVariable UUID id, @Valid @RequestBody PointsRequest request) {
        return transferts.monetiser(tenant(), id, party(), request.points());
    }

    @PostMapping("/taux")
    @ResponseStatus(HttpStatus.CREATED)
    public void taux(@Valid @RequestBody TauxRequest request) {
        CurrentUser.exigerExploitation();
        transferts.publierTaux(tenant(), request.sourceProgrammeId(), request.cibleProgrammeId(),
                request.pointsSource(), request.pointsCible(), request.plafondMois());
    }

    @PostMapping("/programmes/{id}/marchands")
    @ResponseStatus(HttpStatus.CREATED)
    public MarchandView marchand(@PathVariable UUID id, @Valid @RequestBody MarchandRequest request) {
        CurrentUser.exigerExploitation();
        if (membres.existsByProgramme_IdAndMarchandId(id, request.marchandId())) {
            throw new IllegalArgumentException("Ce marchand participe déjà");
        }
        CoalitionMembre membre = new CoalitionMembre();
        membre.setProgramme(programme(id));
        membre.setMarchandId(request.marchandId());
        membre.setPartPct(request.partPct());
        CoalitionMembre saved = membres.save(membre);
        return new MarchandView(saved.getMarchandId(), saved.getPartPct());
    }

    @GetMapping("/programmes/{id}/compensations")
    public List<PilotageService.Compensation> compensations(@PathVariable UUID id) {
        CurrentUser.exigerExploitation();
        return pilotage.compensations(id);
    }

    @GetMapping("/pilotage/passif")
    public List<PilotageService.Echeance> passif(@RequestParam UUID programmeId) {
        CurrentUser.exigerExploitation();
        return pilotage.passif(programmeId);
    }

    @PutMapping("/consentements")
    public void consentement(@Valid @RequestBody ConsentementRequest request) {
        notifications.enregistrerConsentement(tenant(), party(), request.push(), request.sms(), request.email(), request.whatsapp());
    }

    @GetMapping("/notifications")
    public List<NotificationFidelite> notifications(@RequestParam(required = false) UUID partyId) {
        if (partyId != null) {
            CurrentUser.exigerExploitation();
            return notifications.duClient(tenant(), partyId);
        }
        UUID self = CurrentUser.partyId();
        if (self != null) {
            return notifications.duClient(tenant(), self);
        }
        CurrentUser.exigerExploitation();
        return notifications.duTenant(tenant());
    }

    @GetMapping("/segments")
    public Set<String> segments(@RequestParam(required = false) UUID partyId) {
        UUID party = partyId == null ? party() : partyId;
        if (partyId != null) {
            CurrentUser.exigerExploitation();
        }
        return commercial.codes(tenant(), party);
    }

    @PostMapping("/annulations")
    public long annuler(@RequestBody AnnulationRequest request) {
        CurrentUser.exigerExploitation();
        return expiration.annuler(tenant(), request.reference(), request.evenementId());
    }

    @PostMapping("/expiration")
    public int expiration() {
        CurrentUser.exigerExploitation();
        Instant maintenant = Instant.now();
        int perimes = expiration.expirer(maintenant);
        expiration.prevenir(maintenant);
        return perimes;
    }

    private Programme programme(UUID id) {
        Programme programme = programmes.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Programme introuvable"));
        if (!tenant().equals(programme.getTenantId())) {
            throw new IllegalArgumentException("Le programme n'appartient pas à ce tenant");
        }
        return programme;
    }

    private static String tenant() {
        String tenant = CurrentUser.tenantId();
        return tenant == null || tenant.isBlank() ? "CORIS_DEMO" : tenant;
    }

    private static UUID party() {
        UUID partyId = CurrentUser.partyId();
        if (partyId == null) {
            throw new IllegalArgumentException("partyId requis");
        }
        return partyId;
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void renseigner(Campagne campagne, CampagneRequest request) {
        campagne.setCode(request.code().trim().toUpperCase());
        campagne.setNom(request.nom().trim());
        campagne.setDebut(request.debut());
        campagne.setFin(request.fin());
        campagne.setMultiplicateur(request.multiplicateur() == null ? BigDecimal.ONE : request.multiplicateur());
        campagne.setBonusFixe(Math.max(0, request.bonusFixe()));
        campagne.setBudgetPoints(request.budgetPoints());
        campagne.setPriorite(request.priorite());
        campagne.setGroupe(blank(request.groupe()));
        campagne.setWeekendSeulement(request.weekendSeulement());
        campagne.setProfils(blank(request.profils()));
        campagne.setRegions(blank(request.regions()));
        campagne.setUniteId(request.uniteId());
        campagne.setProduits(blank(request.produits()));
        campagne.setServices(blank(request.services()));
        campagne.setCanaux(blank(request.canaux()));
        campagne.setFinanceurId(request.financeurId());
        campagne.setSegmentRequis(blank(request.segmentRequis()));
    }

    private static CampagneView vue(Campagne saved) {
        return new CampagneView(saved.getId(), saved.getCode(), saved.getNom(), saved.getDebut(), saved.getFin(),
                saved.getMultiplicateur(), saved.getBonusFixe(), saved.getBudgetPoints(), saved.getPointsAttribues(),
                saved.getPriorite(), saved.isWeekendSeulement(), saved.getSegmentRequis(), saved.getCanaux(),
                saved.getGroupe(), saved.getProfils(), saved.getRegions(), saved.getUniteId(),
                saved.getProduits(), saved.getServices(), saved.getFinanceurId());
    }

    public record CampagneRequest(
            @NotBlank String code,
            @NotBlank String nom,
            @NotNull Instant debut,
            @NotNull Instant fin,
            BigDecimal multiplicateur,
            long bonusFixe,
            Long budgetPoints,
            int priorite,
            String groupe,
            boolean weekendSeulement,
            String profils,
            String regions,
            UUID uniteId,
            String produits,
            String services,
            String canaux,
            UUID financeurId,
            String segmentRequis
    ) {
    }

    public record MissionRequest(
            @NotBlank String code,
            @NotBlank String nom,
            @NotBlank String metrique,
            @NotNull BigDecimal cible,
            int fenetreJours,
            String filtreProduit,
            String filtreService,
            String filtreCanal,
            long points
    ) {
    }

    public record CatalogueRequest(
            @NotBlank String code,
            @NotBlank String titre,
            @NotBlank String description,
            int cout,
            @NotBlank String type
    ) {
    }

    public record RecompenseRequest(
            @NotBlank String code,
            @NotBlank String titre,
            @NotBlank String description,
            int cout,
            @NotBlank String type,
            Integer stock,
            Instant valideDu,
            Instant valideAu,
            UUID marchandId,
            BigDecimal tauxCashback,
            UUID financeurId
    ) {
    }

    public record TransfertRequest(@NotNull UUID destinataireId, long points) {
    }

    public record ConversionRequest(@NotNull UUID sourceProgrammeId, @NotNull UUID cibleProgrammeId, long points) {
    }

    public record PointsRequest(long points) {
    }

    public record TauxRequest(
            @NotNull UUID sourceProgrammeId,
            UUID cibleProgrammeId,
            int pointsSource,
            int pointsCible,
            long plafondMois
    ) {
    }

    public record MarchandRequest(@NotNull UUID marchandId, @NotNull BigDecimal partPct) {
    }

    public record ConsentementRequest(boolean push, boolean sms, boolean email, boolean whatsapp) {
    }

    public record AnnulationRequest(String reference, UUID evenementId) {
    }

    public record CampagneView(
            UUID id, String code, String nom, Instant debut, Instant fin,
            BigDecimal multiplicateur, long bonusFixe, Long budgetPoints, long pointsAttribues,
            int priorite, boolean weekendSeulement, String segmentRequis, String canaux,
            String groupe, String profils, String regions, UUID uniteId,
            String produits, String services, UUID financeurId
    ) {
    }

    public record MissionView(
            UUID id, String code, String nom, String metrique, BigDecimal cible, int fenetreJours, long points,
            String produit, String service, String canal
    ) {
    }

    private static FideliteService.CatalogueSaisie saisieCatalogue(CatalogueRequest request) {
        return new FideliteService.CatalogueSaisie(
                request.code(), request.titre(), request.description(), request.cout(), request.type());
    }

    private static FideliteService.RecompenseDynamique dynamique(RecompenseRequest request) {
        return new FideliteService.RecompenseDynamique(
                request.code(), request.titre(), request.description(), request.cout(), request.type(),
                request.stock(), request.valideDu(), request.valideAu(), request.marchandId(),
                request.tauxCashback(), request.financeurId());
    }

    private static void renseigner(Mission mission, MissionRequest request) {
        mission.setCode(request.code().trim().toUpperCase());
        mission.setNom(request.nom().trim());
        mission.setMetrique(request.metrique().trim().toUpperCase());
        mission.setCible(request.cible());
        mission.setFenetreJours(Math.max(0, request.fenetreJours()));
        mission.setFiltreProduit(blank(request.filtreProduit()));
        mission.setFiltreService(blank(request.filtreService()));
        mission.setFiltreCanal(blank(request.filtreCanal()));
        mission.setPoints(request.points());
    }

    private static MissionView vue(Mission mission) {
        return new MissionView(mission.getId(), mission.getCode(), mission.getNom(), mission.getMetrique(),
                mission.getCible(), mission.getFenetreJours(), mission.getPoints(),
                mission.getFiltreProduit(), mission.getFiltreService(), mission.getFiltreCanal());
    }

    public record MarchandView(UUID marchandId, BigDecimal partPct) {
    }
}
