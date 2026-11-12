package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.api.dto.NetworkDtos.CreationUniteRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.ModificationUniteRequest;
import com.gateway.payment.network.api.dto.NetworkDtos.NoeudArbre;
import com.gateway.payment.network.api.dto.NetworkDtos.PageDto;
import com.gateway.payment.network.api.dto.NetworkDtos.UniteDetail;
import com.gateway.payment.network.api.dto.NetworkDtos.UniteResume;
import com.gateway.payment.network.domain.FamilleFse;
import com.gateway.payment.network.domain.RegroupementMembre;
import com.gateway.payment.network.domain.StatutUnite;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.domain.UniteOrganisation;
import com.gateway.payment.network.repository.RegroupementMembreRepository;
import com.gateway.payment.network.repository.UniteOrganisationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Arbre du réseau, regroupements compris : création, modification, déplacement de branche, activation et lectures.
 * Les membres des regroupements sont gérés par {@link RegroupementService} ; un déplacement vérifie toutefois
 * qu'aucun membre ne sort de la branche du parent de son regroupement et qu'il ne crée pas de double couverture.
 */
@Service
@Transactional
public class UniteService {

    private static final Pattern CODE_PAYS = Pattern.compile("[A-Z]{2}");
    private static final Pattern CODE_FSE = Pattern.compile("[A-Z0-9_-]{1,20}");

    private static final Comparator<UniteOrganisation> ORDRE =
            Comparator.comparingInt(UniteOrganisation::getNiveau).thenComparing(UniteOrganisation::getCode);

    private final UniteOrganisationRepository unites;
    private final RegroupementMembreRepository membres;
    private final ZoneService zones;

    public UniteService(UniteOrganisationRepository unites, RegroupementMembreRepository membres, ZoneService zones) {
        this.unites = unites;
        this.membres = membres;
        this.zones = zones;
    }

    /**
     * Critères de recherche, tous facultatifs et cumulés.
     *
     * @param types             un des types donnés
     * @param branche           l'unité donnée et toute sa descendance
     * @param sansRegroupements exclut les regroupements
     * @param texte             contenu dans le code ou le libellé, sans tenir compte de la casse
     */
    public record Filtre(String zone, Set<TypeUnite> types, StatutUnite statut, UUID parentId, UUID branche,
                         boolean sansRegroupements, String texte) {

        public static Filtre vide() {
            return new Filtre(null, Set.of(), null, null, null, false, null);
        }
    }

    // ── Lectures ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageDto<UniteResume> rechercher(Filtre filtre, Pageable pageable) {
        String cheminBranche = filtre.branche() == null ? null : charger(filtre.branche()).getChemin();
        return PageDto.of(unites.findAll(specification(filtre, cheminBranche), pageable).map(UniteResume::from));
    }

    @Transactional(readOnly = true)
    public List<UniteResume> listerFse(FamilleFse famille) {
        return unites.findByTypeAndFamilleFseAndStatutAndCodeFseIsNotNullOrderByLibelle(
                        TypeUnite.FSE, famille, StatutUnite.ACTIF)
                .stream()
                .map(UniteResume::from)
                .toList();
    }

    public UniteDetail consulter(UUID id) {
        return detail(charger(id));
    }

    /** Unités rattachées directement, regroupements compris, avec de quoi savoir si chacune a des enfants. */
    @Transactional(readOnly = true)
    public PageDto<NoeudArbre> enfants(UUID id, Pageable pageable) {
        charger(id);
        Page<UniteOrganisation> page = unites.findByParentId(id, pageable);
        return new PageDto<>(noeuds(page.getContent()), page.getTotalElements(), page.getNumber(), page.getSize());
    }

    /** Toute la branche sous l'unité, l'unité elle-même exclue. */
    @Transactional(readOnly = true)
    public PageDto<UniteResume> descendants(UUID id, Pageable pageable) {
        UniteOrganisation u = charger(id);
        Specification<UniteOrganisation> spec = (root, q, cb) -> cb.and(
                cb.like(root.get("chemin"), u.getChemin() + "%"),
                cb.notEqual(root.get("id"), id));
        return PageDto.of(unites.findAll(spec, pageable).map(UniteResume::from));
    }

    @Transactional(readOnly = true)
    public List<UniteResume> ascendants(UUID id) {
        return ascendantsDe(charger(id)).stream().map(UniteResume::from).toList();
    }

    /** Premier niveau de l'arbre d'une zone : ses pays. La suite se lit unité par unité ({@link #enfants}). */
    @Transactional(readOnly = true)
    public List<NoeudArbre> racines(String zone) {
        return noeuds(unites.findByZoneAndParentIdIsNullOrderByCode(zones.charger(zone).getCode()));
    }

    /** Nœuds avec leur nombre d'enfants et de membres, comptés en deux requêtes pour toute la liste. */
    private List<NoeudArbre> noeuds(List<UniteOrganisation> liste) {
        if (liste.isEmpty()) {
            return List.of();
        }
        List<UUID> ids = liste.stream().map(UniteOrganisation::getId).toList();
        Map<UUID, Long> nbEnfants = comptes(unites.compterEnfants(ids));
        List<UUID> regroupements = liste.stream().filter(u -> u.getType().estRegroupement())
                .map(UniteOrganisation::getId).toList();
        Map<UUID, Long> nbMembres = regroupements.isEmpty() ? Map.of() : comptes(membres.compterMembres(regroupements));
        return liste.stream()
                .map(u -> NoeudArbre.from(u, nbEnfants.getOrDefault(u.getId(), 0L), nbMembres.getOrDefault(u.getId(), 0L)))
                .toList();
    }

    private static Map<UUID, Long> comptes(List<Object[]> lignes) {
        Map<UUID, Long> parId = new HashMap<>();
        for (Object[] l : lignes) {
            parId.put((UUID) l[0], ((Number) l[1]).longValue());
        }
        return parId;
    }

    // ── Écritures ───────────────────────────────────────────────────────────

    public UniteDetail creer(CreationUniteRequest req, String acteur) {
        String code = req.code().trim().toUpperCase(Locale.ROOT);
        TypeUnite type = req.type();
        if (type == TypeUnite.PAYS && !CODE_PAYS.matcher(code).matches()) {
            throw NetworkException.regle("CODE_PAYS_INVALIDE",
                    "Le code d'un pays est son code ISO 3166 sur 2 lettres (ex. BF, CM) : " + code + " ne convient pas.");
        }
        if (unites.existsByCode(code)) {
            throw NetworkException.conflit("CODE_EXISTANT", "Une unité " + code + " existe déjà.");
        }
        String codeFse = normaliserCodeFse(type, req.codeFse());
        UUID id = UUID.randomUUID();
        UniteOrganisation.UniteOrganisationBuilder unite = UniteOrganisation.builder()
                .id(id)
                .code(code)
                .libelle(req.libelle().trim())
                .type(type)
                .codeFse(codeFse)
                .statut(StatutUnite.ACTIF)
                .createdBy(acteur)
                .updatedBy(acteur);

        if (RegleRattachement.estRacine(type)) {
            if (req.parentId() != null) {
                throw NetworkException.regle("RATTACHEMENT_INTERDIT", RegleRattachement.description(type));
            }
            unite.zone(zonePays(req.zone())).niveau(1).chemin("/" + id + "/");
        } else {
            if (req.parentId() == null) {
                throw NetworkException.regle("PARENT_OBLIGATOIRE", RegleRattachement.description(type));
            }
            UniteOrganisation parent = chargerParent(req.parentId());
            verifierParent(parent, type);
            unite.zone(zoneHeritee(req.zone(), parent))
                    .parentId(parent.getId())
                    .niveau(parent.getNiveau() + 1)
                    .chemin(parent.getChemin() + id + "/");
        }

        UniteOrganisation creee = unite.build();
        unites.save(creee);
        unites.flush();
        return detail(creee);
    }

    public UniteDetail modifier(UUID id, ModificationUniteRequest req, String acteur) {
        UniteOrganisation u = charger(id);
        if (req.type() != u.getType()) {
            changerType(u, req.type());
        }
        u.setLibelle(req.libelle().trim());
        u.setUpdatedBy(acteur);
        unites.flush();
        return detail(u);
    }

    /**
     * Rattache l'unité à un autre parent ; toute sa branche suit (chemins et niveaux recalculés). Un regroupement
     * se déplace seul, à condition que tous ses membres soient dans la branche du nouveau parent.
     */
    public UniteDetail deplacer(UUID id, UUID parentId, String acteur) {
        UniteOrganisation u = charger(id);
        if (RegleRattachement.estRacine(u.getType())) {
            throw NetworkException.regle("RATTACHEMENT_INTERDIT", RegleRattachement.description(u.getType()));
        }
        if (!RegleRattachement.estDeplacable(u.getType())) {
            throw NetworkException.regle("FSE_NON_DEPLACABLE",
                    u.getCode() + " reste rattaché à son pays : un FSE ne change pas de pays.");
        }
        if (parentId.equals(u.getParentId())) {
            return detail(u);
        }
        UniteOrganisation parent = chargerParent(parentId);
        if (parent.getId().equals(u.getId()) || u.estAscendantDe(parent)) {
            throw NetworkException.regle("BOUCLE",
                    "Impossible de déplacer " + u.getCode() + " sous sa propre branche (" + parent.getCode() + ").");
        }
        verifierParent(parent, u.getType());
        if (!parent.getZone().equals(u.getZone())) {
            throw NetworkException.regle("ZONE_INCOHERENTE", u.getCode() + " est en zone " + u.getZone()
                    + " et " + parent.getCode() + " en zone " + parent.getZone() + " : une branche ne change pas de zone.");
        }
        if (u.getType().estRegroupement()) {
            verifierMembresSous(u, parent);
        }

        String ancien = u.getChemin();
        String nouveau = parent.getChemin() + u.getId() + "/";
        verifierRegroupementsApresDeplacement(ancien, nouveau);

        unites.deplacerBranche(ancien, ancien.length(), nouveau, parent.getNiveau() + 1 - u.getNiveau(),
                Instant.now(), acteur);
        UniteOrganisation deplacee = charger(id);
        deplacee.setParentId(parent.getId());
        deplacee.setUpdatedBy(acteur);
        unites.flush();
        return detail(deplacee);
    }

    public UniteDetail desactiver(UUID id, String acteur) {
        UniteOrganisation u = charger(id);
        if (!u.estActive()) {
            return detail(u);
        }
        if (unites.existsByParentIdAndStatut(id, StatutUnite.ACTIF)) {
            throw NetworkException.regle("ENFANTS_ACTIFS", u.libelleCourt()
                    + " a encore des unités actives rattachées : désactivez-les d'abord.");
        }
        u.setStatut(StatutUnite.INACTIF);
        u.setUpdatedBy(acteur);
        unites.flush();
        return detail(u);
    }

    public UniteDetail activer(UUID id, String acteur) {
        UniteOrganisation u = charger(id);
        if (u.estActive()) {
            return detail(u);
        }
        if (u.getParentId() != null) {
            UniteOrganisation parent = charger(u.getParentId());
            if (!parent.estActive()) {
                throw NetworkException.regle("PARENT_INACTIF",
                        parent.libelleCourt() + " est inactif : réactivez-le avant " + u.getCode() + ".");
            }
        } else if (u.getType() == TypeUnite.PAYS) {
            zones.exigerActive(u.getZone());
        }
        u.setStatut(StatutUnite.ACTIF);
        u.setUpdatedBy(acteur);
        unites.flush();
        return detail(u);
    }

    // ── Règles ──────────────────────────────────────────────────────────────

    private void changerType(UniteOrganisation u, TypeUnite nouveau) {
        if (u.getType().estRegroupement() || nouveau.estRegroupement()) {
            throw NetworkException.regle("TYPE_NON_MODIFIABLE",
                    "Un regroupement reste un regroupement, et une unité de l'arbre ne peut pas en devenir un.");
        }
        if (RegleRattachement.estRacine(u.getType()) != RegleRattachement.estRacine(nouveau)) {
            throw NetworkException.regle("TYPE_NON_MODIFIABLE",
                    "Un pays reste un pays, et une unité rattachée ne peut pas devenir un pays.");
        }
        if (u.getType() == TypeUnite.FSE || nouveau == TypeUnite.FSE) {
            throw NetworkException.regle("TYPE_NON_MODIFIABLE",
                    "Le type FSE (et son codeFse) est fixé à la création et ne change pas.");
        }
        if (u.getParentId() != null) {
            UniteOrganisation parent = charger(u.getParentId());
            if (!RegleRattachement.peutRattacher(parent.getType(), nouveau)) {
                throw NetworkException.regle("RATTACHEMENT_INTERDIT", "Un(e) " + nouveau.libelle()
                        + " ne peut pas dépendre de " + parent.libelleCourt() + ". " + RegleRattachement.description(nouveau));
            }
        }
        List<String> incompatibles = unites.findByParentIdOrderByCode(u.getId()).stream()
                .filter(e -> !RegleRattachement.peutRattacher(nouveau, e.getType()))
                .map(UniteOrganisation::getCode)
                .toList();
        if (!incompatibles.isEmpty()) {
            throw NetworkException.regle("TYPE_INCOMPATIBLE_ENFANTS", u.getCode() + " a des unités rattachées qui ne "
                    + "peuvent pas dépendre d'un(e) " + nouveau.libelle() + " : " + String.join(", ", incompatibles) + ".");
        }
        u.setType(nouveau);
    }

    /**
     * Obligatoire et unique pour une FSE ; interdit (ou ignoré s'il est vide) pour les autres types.
     */
    private String normaliserCodeFse(TypeUnite type, String brut) {
        String saisi = brut == null ? "" : brut.trim().toUpperCase(Locale.ROOT);
        if (type == TypeUnite.FSE) {
            if (saisi.isEmpty()) {
                throw NetworkException.regle("CODE_FSE_OBLIGATOIRE",
                        "Le code FSE Omnicanal (codeFse) est obligatoire pour une unité de type FSE (ex. 0011).");
            }
            if (!CODE_FSE.matcher(saisi).matches()) {
                throw NetworkException.regle("CODE_FSE_INVALIDE",
                        "Code FSE « " + saisi + " » : lettres, chiffres, _ ou - (20 caractères au plus).");
            }
            if (unites.existsByCodeFse(saisi)) {
                throw NetworkException.conflit("CODE_FSE_EXISTANT",
                        "Le code FSE " + saisi + " est déjà attribué à une autre unité.");
            }
            return saisi;
        }
        if (!saisi.isEmpty()) {
            throw NetworkException.regle("CODE_FSE_INTERDIT",
                    "Le code FSE (codeFse) ne s'applique qu'aux unités de type FSE.");
        }
        return null;
    }

    /** Un regroupement ne change de parent que si tous ses membres sont dans la branche du nouveau parent. */
    private void verifierMembresSous(UniteOrganisation regroupement, UniteOrganisation parent) {
        List<UUID> ids = membres.findByRegroupementId(regroupement.getId()).stream()
                .map(RegroupementMembre::getUniteId).toList();
        List<String> dehors = unites.findAllById(ids).stream()
                .filter(m -> !m.getChemin().startsWith(parent.getChemin()))
                .map(UniteOrganisation::getCode)
                .sorted()
                .toList();
        if (!dehors.isEmpty()) {
            throw NetworkException.regle("MEMBRE_HORS_BRANCHE", "Ces membres de " + regroupement.getCode()
                    + " ne sont pas dans la branche de " + parent.getCode() + " : " + String.join(", ", dehors)
                    + ". Retirez-les avant de déplacer le regroupement.");
        }
    }

    /**
     * Après le déplacement, chaque membre de regroupement de la branche doit rester dans la branche du parent de
     * son regroupement, et aucun regroupement ne doit couvrir deux fois la même unité.
     */
    private void verifierRegroupementsApresDeplacement(String ancien, String nouveau) {
        List<RegroupementMembre> touches = membres.findDansBranche(ancien);
        if (touches.isEmpty()) {
            return;
        }
        Set<UUID> regroupementIds = touches.stream().map(RegroupementMembre::getRegroupementId).collect(Collectors.toSet());
        List<RegroupementMembre> appartenances = membres.findByRegroupementIdIn(regroupementIds);
        List<UniteOrganisation> regroupements = unites.findAllById(regroupementIds);

        Set<UUID> aCharger = appartenances.stream().map(RegroupementMembre::getUniteId).collect(Collectors.toSet());
        regroupements.stream().map(UniteOrganisation::getParentId).filter(Objects::nonNull).forEach(aCharger::add);
        Map<UUID, UniteOrganisation> parId = unites.findAllById(aCharger).stream()
                .collect(Collectors.toMap(UniteOrganisation::getId, Function.identity()));
        Function<String, String> apres = c -> c.startsWith(ancien) ? nouveau + c.substring(ancien.length()) : c;

        for (UniteOrganisation r : regroupements.stream().sorted(Comparator.comparing(UniteOrganisation::getCode)).toList()) {
            List<Couverture.Position> positions = appartenances.stream()
                    .filter(m -> m.getRegroupementId().equals(r.getId()))
                    .map(m -> parId.get(m.getUniteId()))
                    .filter(Objects::nonNull)
                    .map(m -> new Couverture.Position(m, apres.apply(m.getChemin())))
                    .toList();
            String cheminParent = apres.apply(r.cheminParent());
            UniteOrganisation parent = parId.get(r.getParentId());
            positions.stream()
                    .filter(p -> !p.chemin().startsWith(cheminParent))
                    .findFirst()
                    .ifPresent(p -> {
                        throw NetworkException.regle("SORTIE_BRANCHE_REGROUPEMENT", "Déplacement impossible : "
                                + p.unite().getCode() + " est membre du regroupement " + r.getCode()
                                + ", rattaché à " + (parent == null ? "?" : parent.getCode())
                                + ", et sortirait de sa branche. Retirez-le du regroupement ou déplacez le regroupement.");
                    });
            Couverture.premierChevauchement(positions).ifPresent(c -> {
                throw NetworkException.regle("DOUBLON_COUVERTURE", "Déplacement impossible : dans le regroupement "
                        + r.getCode() + ", " + c.descendant().getCode() + " serait couvert deux fois (il est membre "
                        + "et descendrait de " + c.ascendant().getCode() + ", membre lui aussi).");
            });
        }
    }

    private void verifierParent(UniteOrganisation parent, TypeUnite type) {
        if (!RegleRattachement.peutRattacher(parent.getType(), type)) {
            throw NetworkException.regle("RATTACHEMENT_INTERDIT", "Impossible de rattacher un(e) " + type.libelle()
                    + " à " + parent.libelleCourt() + ". " + RegleRattachement.description(type));
        }
        if (!parent.estActive()) {
            throw NetworkException.regle("PARENT_INACTIF",
                    parent.libelleCourt() + " est inactif : on ne peut rien y rattacher.");
        }
    }

    private static String zoneHeritee(String demandee, UniteOrganisation reference) {
        if (demandee != null && !demandee.isBlank() && !ZoneService.normaliser(demandee).equals(reference.getZone())) {
            throw NetworkException.regle("ZONE_INCOHERENTE", reference.getCode() + " est en zone "
                    + reference.getZone() + " : une unité qui en dépend ne peut pas être en zone "
                    + ZoneService.normaliser(demandee) + ".");
        }
        return reference.getZone();
    }

    /** La zone d'un pays est saisie ; elle doit exister et être active. */
    private String zonePays(String zone) {
        if (zone == null || zone.isBlank()) {
            throw NetworkException.regle("ZONE_OBLIGATOIRE", "La zone d'un pays est obligatoire.");
        }
        return zones.exigerActive(zone).getCode();
    }

    // ── Accès ───────────────────────────────────────────────────────────────

    private UniteOrganisation charger(UUID id) {
        return unites.findById(id).orElseThrow(() ->
                NetworkException.introuvable("UNITE_INEXISTANTE", "Unité " + id + " introuvable."));
    }

    private UniteOrganisation chargerParent(UUID id) {
        UniteOrganisation parent = unites.findById(id).orElseThrow(() ->
                NetworkException.introuvable("PARENT_INEXISTANT", "Unité parente " + id + " introuvable."));
        if (parent.getType().estRegroupement()) {
            throw NetworkException.regle("RATTACHEMENT_INTERDIT", "Un regroupement n'accueille aucune unité : "
                    + "il a des membres, pas d'enfants.");
        }
        return parent;
    }

    private List<UniteOrganisation> ascendantsDe(UniteOrganisation u) {
        return unites.findAllById(u.idsAscendants()).stream().sorted(ORDRE).toList();
    }

    private UniteDetail detail(UniteOrganisation u) {
        UniteOrganisation parent = u.getParentId() == null ? null : unites.findById(u.getParentId()).orElse(null);
        return new UniteDetail(u.getId(), u.getCode(), u.getLibelle(), u.getType(), u.getZone(), u.getCodeFse(),
                zones.charger(u.getZone()).getDevise(),
                u.getStatut(), u.getNiveau(), UniteResume.from(parent),
                ascendantsDe(u).stream().map(UniteResume::from).toList(),
                u.getType().estRegroupement() ? 0 : unites.countByParentId(u.getId()),
                u.getType().estRegroupement() ? membres.countByRegroupementId(u.getId()) : 0,
                RegleRattachement.enfantsAutorises(u.getType()),
                u.getCreatedAt(), u.getCreatedBy(), u.getUpdatedAt(), u.getUpdatedBy());
    }

    private static Specification<UniteOrganisation> specification(Filtre f, String cheminBranche) {
        List<Specification<UniteOrganisation>> specs = new ArrayList<>();
        if (f.zone() != null && !f.zone().isBlank()) {
            String zone = ZoneService.normaliser(f.zone());
            specs.add((root, q, cb) -> cb.equal(root.get("zone"), zone));
        }
        if (f.types() != null && !f.types().isEmpty()) {
            specs.add((root, q, cb) -> root.get("type").in(f.types()));
        }
        if (f.sansRegroupements()) {
            specs.add((root, q, cb) -> cb.notEqual(root.get("type"), TypeUnite.REGROUPEMENT));
        }
        if (cheminBranche != null) {
            specs.add((root, q, cb) -> cb.like(root.get("chemin"), cheminBranche + "%"));
        }
        if (f.statut() != null) {
            specs.add((root, q, cb) -> cb.equal(root.get("statut"), f.statut()));
        }
        if (f.parentId() != null) {
            specs.add((root, q, cb) -> cb.equal(root.get("parentId"), f.parentId()));
        }
        if (f.texte() != null && !f.texte().isBlank()) {
            String motif = "%" + f.texte().trim().toLowerCase(Locale.ROOT) + "%";
            specs.add((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("code")), motif),
                    cb.like(cb.lower(root.get("libelle")), motif)));
        }
        return Specification.allOf(specs);
    }
}
