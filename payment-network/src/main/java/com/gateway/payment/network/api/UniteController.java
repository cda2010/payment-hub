package com.gateway.payment.network.api;

import com.gateway.payment.network.api.dto.NetworkDtos.CouvertureDto;
import com.gateway.payment.network.api.dto.NetworkDtos.CreationUniteRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.DeplacementRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.ModificationUniteRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.NoeudArbre;
import com.gateway.payment.network.api.dto.NetworkDtos.PageDto;
import com.gateway.payment.network.api.dto.NetworkDtos.PositionDto;
import com.gateway.payment.network.api.dto.NetworkDtos.Referentiel;
import com.gateway.payment.network.api.dto.NetworkDtos.TypeDto;
import com.gateway.payment.network.api.dto.NetworkDtos.UniteDetail;
import com.gateway.payment.network.api.dto.NetworkDtos.UniteResume;
import com.gateway.payment.network.domain.FamilleFse;
import com.gateway.payment.network.domain.StatutUnite;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.service.PositionService;
import com.gateway.payment.network.service.RegleRattachement;
import com.gateway.payment.network.service.RegroupementService;
import com.gateway.payment.network.service.UniteService;
import com.gateway.payment.network.service.ZoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/network")
@Tag(name = "Unités d'organisation", description = "Arbre du réseau de distribution par zone, et regroupements")
@PreAuthorize("isAuthenticated()")
public class UniteController {

    private static final String ECRITURE = "hasAnyRole('ADMIN','CONFIG_MANAGER')";

    private final UniteService unites;
    private final RegroupementService regroupements;
    private final ZoneService zones;
    private final PositionService positions;

    public UniteController(UniteService unites, RegroupementService regroupements, ZoneService zones,
                           PositionService positions) {
        this.unites = unites;
        this.regroupements = regroupements;
        this.zones = zones;
        this.positions = positions;
    }

    @GetMapping("/fse")
    @PreAuthorize("permitAll()")
    @Operation(summary = "FSE actifs d'une famille (mobile money ou banque)")
    public List<UniteResume> fse(@RequestParam FamilleFse famille) {
        return unites.listerFse(famille);
    }

    @GetMapping("/referentiel")
    @Operation(summary = "Zones et types d'unité, avec les rattachements autorisés")
    public Referentiel referentiel() {
        return new Referentiel(
                zones.lister(),
                Arrays.stream(TypeUnite.values()).map(t -> new TypeDto(t, t.libelle(), RegleRattachement.estRacine(t),
                        t.estRegroupement(), RegleRattachement.parentsAutorises(t), RegleRattachement.enfantsAutorises(t),
                        RegleRattachement.description(t))).toList());
    }

    @GetMapping("/unites")
    @Operation(summary = "Rechercher des unités, page par page (zone, types, statut, parent, branche, texte sur code "
            + "ou libellé ; tri par code, libelle, type, zone, statut, niveau ou maj)")
    public PageDto<UniteResume> rechercher(
            @RequestParam(required = false) String zone,
            @RequestParam(name = "type", required = false) Set<TypeUnite> types,
            @RequestParam(required = false) StatutUnite statut,
            @RequestParam(required = false) UUID parentId,
            @RequestParam(required = false) UUID branche,
            @RequestParam(defaultValue = "false") boolean sansRegroupements,
            @RequestParam(required = false) String texte,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer taille,
            @RequestParam(required = false) String tri,
            @RequestParam(required = false) String sens
    ) {
        return unites.rechercher(
                new UniteService.Filtre(zone, types, statut, parentId, branche, sansRegroupements, texte),
                Pagination.de(page, taille, tri, sens));
    }

    @GetMapping("/unites/{id}")
    @Operation(summary = "Consulter une unité (parent, ascendants, nombre d'enfants ou de membres)")
    public UniteDetail consulter(@PathVariable UUID id) {
        return unites.consulter(id);
    }

    @GetMapping("/unites/{id}/enfants")
    @Operation(summary = "Unités rattachées directement, page par page, avec leur nombre d'enfants et de membres")
    public PageDto<NoeudArbre> enfants(@PathVariable UUID id,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer taille) {
        return unites.enfants(id, Pagination.de(page, taille, "code", "asc"));
    }

    @GetMapping("/unites/{id}/descendants")
    @Operation(summary = "Toute la branche sous l'unité, page par page")
    public PageDto<UniteResume> descendants(@PathVariable UUID id,
                                            @RequestParam(required = false) Integer page,
                                            @RequestParam(required = false) Integer taille) {
        return unites.descendants(id, Pagination.de(page, taille, null, null));
    }

    @GetMapping("/unites/{id}/ascendants")
    @Operation(summary = "Ascendants, du pays au parent direct")
    public List<UniteResume> ascendants(@PathVariable UUID id) {
        return unites.ascendants(id);
    }

    @GetMapping("/unites/{id}/regroupements")
    @Operation(summary = "Regroupements qui couvrent l'unité (directement ou par un ascendant membre)")
    public List<CouvertureDto> couvertures(@PathVariable UUID id) {
        return regroupements.couvertures(id);
    }

    @GetMapping("/positions")
    @Operation(summary = "Position d'une unité (id ou code), d'un pays (code ISO) ou d'une zone : zone, devise, "
            + "lignée jusqu'au pays et regroupements qui la couvrent (un seul critère)")
    public PositionDto position(
            @RequestParam(required = false) UUID unite,
            @RequestParam(required = false) String pays,
            @RequestParam(required = false) String zone,
            @RequestParam(required = false) String code
    ) {
        return positions.position(unite, pays, zone, code);
    }

    @GetMapping("/arbre")
    @Operation(summary = "Premier niveau de l'arbre d'une zone (ses pays) ; la suite se lit par /unites/{id}/enfants")
    public List<NoeudArbre> arbre(@RequestParam String zone) {
        return unites.racines(zone);
    }

    @PostMapping("/unites")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Créer une unité : pays (code ISO 2 lettres, dans une zone active), FSE, agence, master, "
            + "marchand, sous-distributeur ou regroupement")
    public UniteDetail creer(@Valid @RequestBody CreationUniteRequest body, Authentication auth) {
        return unites.creer(body, auth.getName());
    }

    @PutMapping("/unites/{id}")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Modifier le libellé ou le type")
    public UniteDetail modifier(@PathVariable UUID id, @Valid @RequestBody ModificationUniteRequest body,
                                Authentication auth) {
        return unites.modifier(id, body, auth.getName());
    }

    @PostMapping("/unites/{id}/deplacer")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Rattacher l'unité et toute sa branche à un autre parent (ni pays ni FSE ; les membres "
            + "d'un regroupement doivent rester dans la branche de son nouveau parent)")
    public UniteDetail deplacer(@PathVariable UUID id, @Valid @RequestBody DeplacementRequest body, Authentication auth) {
        return unites.deplacer(id, body.parentId(), auth.getName());
    }

    @PostMapping("/unites/{id}/activer")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Réactiver une unité (son parent doit être actif)")
    public UniteDetail activer(@PathVariable UUID id, Authentication auth) {
        return unites.activer(id, auth.getName());
    }

    @PostMapping("/unites/{id}/desactiver")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Désactiver une unité (refusé s'il reste des unités actives rattachées)")
    public UniteDetail desactiver(@PathVariable UUID id, Authentication auth) {
        return unites.desactiver(id, auth.getName());
    }
}
