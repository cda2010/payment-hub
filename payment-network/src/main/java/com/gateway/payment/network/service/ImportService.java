package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.api.dto.NetworkDtos.ErreurImport;
import com.gateway.payment.network.api.dto.NetworkDtos.RapportImport;
import com.gateway.payment.network.domain.RegroupementMembre;
import com.gateway.payment.network.domain.StatutUnite;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.domain.UniteOrganisation;
import com.gateway.payment.network.domain.Zone;
import com.gateway.payment.network.repository.RegroupementMembreRepository;
import com.gateway.payment.network.repository.UniteOrganisationRepository;
import com.gateway.payment.network.repository.ZoneRepository;
import jakarta.persistence.EntityManager;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
 * Import en masse des unités et des membres de regroupements depuis un fichier CSV ou Excel.
 *
 * <p>Tout le fichier passe ou rien ne passe : les lignes sont d'abord contrôlées ensemble, en mémoire, avec les
 * règles de l'arbre (types de rattachement, zone, boucles, couvertures) ; s'il n'y a aucune erreur, les écritures se
 * font dans une seule transaction, annulée à la fin en mode essai ou si une écriture est refusée. L'essai donne ainsi
 * exactement le résultat de l'application.
 *
 * <p>Unités : un code inconnu crée l'unité (parent désigné par son code, en base ou plus haut dans le fichier, dans
 * n'importe quel ordre) ; un code connu met à jour le libellé et, si le parent indiqué diffère, déplace l'unité avec
 * sa branche. Le type et la zone d'une unité existante ne changent pas par import.
 */
@Service
public class ImportService {

    public static final int MAX_ERREURS = 500;

    static final List<String> COLONNES_UNITES = List.of("code", "libelle", "type", "code_parent", "zone", "code_fse");
    static final List<String> COLONNES_MEMBRES = List.of("code_regroupement", "code_membre");

    private static final Map<String, String> ALIAS = Map.of(
            "parent", "code_parent",
            "code_unite_parent", "code_parent",
            "nom", "libelle",
            "regroupement", "code_regroupement",
            "membre", "code_membre",
            "code_unite", "code_membre",
            "codefse", "code_fse",
            "fse", "code_fse");

    private static final Pattern CODE = Pattern.compile("[A-Z0-9_-]{1,40}");
    private static final Pattern CODE_PAYS = Pattern.compile("[A-Z]{2}");
    private static final Pattern CODE_FSE = Pattern.compile("[A-Z0-9_-]{1,20}");
    private static final int LOT = 500;

    private static final Map<String, TypeUnite> TYPES = types();
    private static final String TYPES_ATTENDUS = Arrays.stream(TypeUnite.values())
            .map(TypeUnite::name).collect(Collectors.joining(", "));

    private final UniteOrganisationRepository unites;
    private final RegroupementMembreRepository membres;
    private final ZoneRepository zones;
    private final UniteService uniteService;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final EntityManager em;

    public ImportService(UniteOrganisationRepository unites, RegroupementMembreRepository membres, ZoneRepository zones,
                         UniteService uniteService, JdbcTemplate jdbc, PlatformTransactionManager transactions,
                         EntityManager em) {
        this.unites = unites;
        this.membres = membres;
        this.zones = zones;
        this.uniteService = uniteService;
        this.jdbc = jdbc;
        this.tx = new TransactionTemplate(transactions);
        this.tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.em = em;
    }

    // ── Unités ──────────────────────────────────────────────────────────────

    public RapportImport importerUnites(String nomFichier, byte[] contenu, boolean essai, String acteur) {
        long debut = System.nanoTime();
        TableauImport tableau = TableauImport.lire(nomFichier, contenu, List.of("code", "libelle", "type", "code_parent"), ALIAS);
        Bilan bilan = new Bilan("UNITES", tableau.format(), essai, tableau.lignes().size());
        return executer(bilan, debut, () -> traiterUnites(tableau, bilan, acteur));
    }

    private record LigneUnite(int numero, String code, String libelle, TypeUnite type, String codeParent, String zone,
                              String codeFse) {
    }

    /** Unité telle qu'elle sera une fois l'import appliqué (avant les déplacements). */
    private record Position(UUID id, String code, TypeUnite type, String zone, int niveau, String chemin,
                            boolean active) {
        static Position de(UniteOrganisation u) {
            return new Position(u.getId(), u.getCode(), u.getType(), u.getZone(), u.getNiveau(), u.getChemin(),
                    u.estActive());
        }
    }

    private record Nouvelle(LigneUnite ligne, UUID id, UUID parentId, String zone, int niveau, String chemin) {
    }

    private record Renommage(UUID id, String libelle) {
    }

    private record Deplacement(int ligne, String code, UUID id, UUID parentId) {
    }

    private void traiterUnites(TableauImport tableau, Bilan bilan, String acteur) {
        Map<String, LigneUnite> lignes = new LinkedHashMap<>();
        Map<String, Integer> lignesInvalides = new HashMap<>();
        for (TableauImport.Ligne l : tableau.lignes()) {
            int n = l.numero();
            String code = l.valeur("code").toUpperCase(Locale.ROOT);
            String libelle = l.valeur("libelle");
            String typeSaisi = l.valeur("type");
            int erreursAvant = bilan.nbErreurs;
            if (code.isEmpty()) {
                bilan.erreur(n, null, "code", "CODE_OBLIGATOIRE", "Le code est obligatoire.");
            } else if (!CODE.matcher(code).matches()) {
                bilan.erreur(n, code, "code", "CODE_INVALIDE",
                        "Code " + code + " : lettres, chiffres, _ ou - (40 caractères au plus).");
            }
            Integer premiere = lignes.containsKey(code) ? Integer.valueOf(lignes.get(code).numero())
                    : lignesInvalides.get(code);
            if (!code.isEmpty() && premiere != null) {
                bilan.erreur(n, code, "code", "DOUBLON_FICHIER", "Le code " + code + " figure déjà ligne " + premiere + ".");
                continue;
            }
            if (libelle.isEmpty()) {
                bilan.erreur(n, code, "libelle", "LIBELLE_OBLIGATOIRE", "Le libellé est obligatoire.");
            } else if (libelle.length() > 150) {
                bilan.erreur(n, code, "libelle", "LIBELLE_TROP_LONG", "Le libellé dépasse 150 caractères.");
            }
            TypeUnite type = TYPES.get(normaliser(typeSaisi));
            if (type == null) {
                bilan.erreur(n, code, "type", "TYPE_INCONNU",
                        "Type « " + typeSaisi + " » inconnu. Attendu : " + TYPES_ATTENDUS + ".");
            }
            String codeFse = l.valeur("code_fse").toUpperCase(Locale.ROOT);
            if (type == TypeUnite.FSE) {
                if (codeFse.isEmpty()) {
                    bilan.erreur(n, code, "code_fse", "CODE_FSE_OBLIGATOIRE",
                            "Le code FSE Omnicanal (code_fse) est obligatoire pour une FSE (ex. 0011).");
                } else if (!CODE_FSE.matcher(codeFse).matches()) {
                    bilan.erreur(n, code, "code_fse", "CODE_FSE_INVALIDE",
                            "Code FSE « " + codeFse + " » : lettres, chiffres, _ ou - (20 max).");
                }
            } else if (!codeFse.isEmpty()) {
                bilan.erreur(n, code, "code_fse", "CODE_FSE_INTERDIT",
                        "Le code FSE (code_fse) ne s'applique qu'aux unités de type FSE.");
            }
            if (bilan.nbErreurs > erreursAvant) {
                if (!code.isEmpty()) {
                    lignesInvalides.put(code, n);
                }
                continue;
            }
            lignes.put(code, new LigneUnite(n, code, libelle, type,
                    l.valeur("code_parent").toUpperCase(Locale.ROOT), l.valeur("zone").toUpperCase(Locale.ROOT),
                    type == TypeUnite.FSE ? codeFse : null));
        }

        Set<String> codes = new HashSet<>(lignes.keySet());
        lignes.values().stream().map(LigneUnite::codeParent).filter(c -> !c.isEmpty()).forEach(codes::add);
        Map<String, UniteOrganisation> existantes = parCode(codes);
        Set<UUID> idsParents = lignes.keySet().stream().map(existantes::get).filter(Objects::nonNull)
                .map(UniteOrganisation::getParentId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<UUID, UniteOrganisation> parentsActuels = parId(idsParents);
        Map<String, Zone> referentiel = zones.findAll().stream().collect(Collectors.toMap(Zone::getCode, Function.identity()));

        Resolution r = new Resolution(lignes, lignesInvalides, existantes, parentsActuels, referentiel, bilan);
        lignes.keySet().forEach(code -> r.resoudre(code, new HashSet<>()));
        if (bilan.nbErreurs > 0) {
            return;
        }

        Timestamp maintenant = Timestamp.from(Instant.now());
        List<Nouvelle> nouvelles = r.nouvelles.stream().sorted(Comparator.comparingInt(Nouvelle::niveau)).toList();
        // Doublons code_fse dans le fichier (FSE uniquement).
        Map<String, Integer> fseVus = new HashMap<>();
        for (Nouvelle n : nouvelles) {
            String cf = n.ligne().codeFse();
            if (cf == null || cf.isEmpty()) {
                continue;
            }
            Integer premiere = fseVus.putIfAbsent(cf, n.ligne().numero());
            if (premiere != null) {
                bilan.erreur(n.ligne().numero(), n.ligne().code(), "code_fse", "DOUBLON_FICHIER",
                        "Le code FSE " + cf + " figure déjà ligne " + premiere + ".");
            } else if (unites.existsByCodeFse(cf)) {
                bilan.erreur(n.ligne().numero(), n.ligne().code(), "code_fse", "CODE_FSE_EXISTANT",
                        "Le code FSE " + cf + " est déjà attribué à une autre unité.");
            }
        }
        if (bilan.nbErreurs > 0) {
            return;
        }

        jdbc.batchUpdate("""
                insert into unite_organisation (id, code, libelle, type, zone, code_fse, parent_id, statut, niveau, chemin,
                                                version, created_at, created_by, updated_at, updated_by)
                values (?, ?, ?, ?, ?, ?, ?, 'ACTIF', ?, ?, 0, ?, ?, ?, ?)
                """, nouvelles, LOT, (ps, u) -> {
            ps.setObject(1, u.id());
            ps.setString(2, u.ligne().code());
            ps.setString(3, u.ligne().libelle());
            ps.setString(4, u.ligne().type().name());
            ps.setString(5, u.zone());
            ps.setString(6, u.ligne().codeFse());
            ps.setObject(7, u.parentId());
            ps.setInt(8, u.niveau());
            ps.setString(9, u.chemin());
            ps.setTimestamp(10, maintenant);
            ps.setString(11, acteur);
            ps.setTimestamp(12, maintenant);
            ps.setString(13, acteur);
        });
        jdbc.batchUpdate("""
                update unite_organisation set libelle = ?, version = version + 1, updated_at = ?, updated_by = ?
                 where id = ?
                """, r.renommages, LOT, (ps, u) -> {
            ps.setString(1, u.libelle());
            ps.setTimestamp(2, maintenant);
            ps.setString(3, acteur);
            ps.setObject(4, u.id());
        });
        em.clear();
        for (Deplacement d : r.deplacements) {
            try {
                uniteService.deplacer(d.id(), d.parentId(), acteur);
            } catch (NetworkException e) {
                bilan.erreur(d.ligne(), d.code(), "code_parent", e.getCode(), e.getMessage());
            }
        }
    }

    /** Résolution des lignes, chaque parent avant ses enfants quel que soit l'ordre du fichier. */
    private final class Resolution {

        private final Map<String, LigneUnite> lignes;
        private final Map<String, Integer> lignesInvalides;
        private final Map<String, UniteOrganisation> existantes;
        private final Map<UUID, UniteOrganisation> parentsActuels;
        private final Map<String, Zone> referentiel;
        private final Bilan bilan;
        private final Map<String, Position> positions = new HashMap<>();
        private final Set<String> echecs = new HashSet<>();
        final List<Nouvelle> nouvelles = new ArrayList<>();
        final List<Renommage> renommages = new ArrayList<>();
        final List<Deplacement> deplacements = new ArrayList<>();

        Resolution(Map<String, LigneUnite> lignes, Map<String, Integer> lignesInvalides,
                   Map<String, UniteOrganisation> existantes, Map<UUID, UniteOrganisation> parentsActuels,
                   Map<String, Zone> referentiel, Bilan bilan) {
            this.lignes = lignes;
            this.lignesInvalides = lignesInvalides;
            this.existantes = existantes;
            this.parentsActuels = parentsActuels;
            this.referentiel = referentiel;
            this.bilan = bilan;
        }

        /** Null si l'unité n'existe ni en base ni dans le fichier, ou si sa ligne est en erreur. */
        Position resoudre(String code, Set<String> enCours) {
            Position connue = positions.get(code);
            if (connue != null || echecs.contains(code)) {
                return connue;
            }
            LigneUnite ligne = lignes.get(code);
            UniteOrganisation existante = existantes.get(code);
            if (ligne == null) {
                return existante == null ? null : retenir(Position.de(existante));
            }
            return existante != null ? mettreAJour(ligne, existante, enCours) : creer(ligne, enCours);
        }

        private Position creer(LigneUnite l, Set<String> enCours) {
            UUID id = UUID.randomUUID();
            if (l.type() == TypeUnite.PAYS) {
                return creerPays(l, id);
            }
            if (l.codeParent().isEmpty()) {
                return echec(l, "code_parent", "PARENT_OBLIGATOIRE", RegleRattachement.description(l.type()));
            }
            if (l.codeParent().equals(l.code()) || enCours.contains(l.codeParent())) {
                return echec(l, "code_parent", "BOUCLE", l.code() + " et " + l.codeParent()
                        + " dépendent l'un de l'autre dans le fichier.");
            }
            Position parent = parent(l, enCours);
            if (parent == null || !rattachable(l, parent)) {
                return echec(l);
            }
            if (!l.zone().isEmpty() && !l.zone().equals(parent.zone())) {
                return echec(l, "zone", "ZONE_INCOHERENTE", l.codeParent() + " est en zone " + parent.zone()
                        + " : " + l.code() + " ne peut pas être en zone " + l.zone() + ".");
            }
            Position p = new Position(id, l.code(), l.type(), parent.zone(), parent.niveau() + 1,
                    parent.chemin() + id + "/", true);
            nouvelles.add(new Nouvelle(l, id, parent.id(), p.zone(), p.niveau(), p.chemin()));
            bilan.creations++;
            return retenir(p);
        }

        private Position creerPays(LigneUnite l, UUID id) {
            if (!CODE_PAYS.matcher(l.code()).matches()) {
                return echec(l, "code", "CODE_PAYS_INVALIDE", "Le code d'un pays est son code ISO 3166 sur 2 lettres "
                        + "(ex. BF, CM) : " + l.code() + " ne convient pas.");
            }
            if (!l.codeParent().isEmpty()) {
                return echec(l, "code_parent", "RATTACHEMENT_INTERDIT", RegleRattachement.description(TypeUnite.PAYS)
                        + " Laissez code_parent vide.");
            }
            if (l.zone().isEmpty()) {
                return echec(l, "zone", "ZONE_OBLIGATOIRE", "La zone d'un pays est obligatoire.");
            }
            Zone zone = referentiel.get(l.zone());
            if (zone == null) {
                return echec(l, "zone", "ZONE_INEXISTANTE", "Zone " + l.zone() + " inconnue.");
            }
            if (zone.getStatut() != StatutUnite.ACTIF) {
                return echec(l, "zone", "ZONE_INACTIVE", "La zone " + l.zone() + " est inactive.");
            }
            Position p = new Position(id, l.code(), TypeUnite.PAYS, zone.getCode(), 1, "/" + id + "/", true);
            nouvelles.add(new Nouvelle(l, id, null, p.zone(), 1, p.chemin()));
            bilan.creations++;
            return retenir(p);
        }

        /** Unité déjà en base : libellé mis à jour, déplacement si le parent indiqué a changé. */
        private Position mettreAJour(LigneUnite l, UniteOrganisation u, Set<String> enCours) {
            Position p = retenir(Position.de(u));
            int erreursAvant = bilan.nbErreurs;
            if (l.type() != u.getType()) {
                bilan.erreur(l.numero(), l.code(), "type", "TYPE_DIFFERENT", u.getCode() + " existe déjà comme "
                        + u.getType().libelle() + " : le type ne change pas par import.");
            }
            if (!l.zone().isEmpty() && !l.zone().equals(u.getZone())) {
                bilan.erreur(l.numero(), l.code(), "zone", "ZONE_INCOHERENTE", u.getCode() + " est en zone "
                        + u.getZone() + " : la zone d'une unité ne change pas.");
            }
            boolean deplacee = false;
            if (u.getType() == TypeUnite.PAYS) {
                if (!l.codeParent().isEmpty()) {
                    bilan.erreur(l.numero(), l.code(), "code_parent", "RATTACHEMENT_INTERDIT",
                            RegleRattachement.description(TypeUnite.PAYS) + " Laissez code_parent vide.");
                }
            } else if (l.codeParent().isEmpty()) {
                bilan.erreur(l.numero(), l.code(), "code_parent", "PARENT_OBLIGATOIRE",
                        RegleRattachement.description(u.getType()));
            } else {
                UniteOrganisation actuel = parentsActuels.get(u.getParentId());
                if (actuel == null || !actuel.getCode().equals(l.codeParent())) {
                    deplacee = preparerDeplacement(l, u, enCours);
                }
            }
            if (bilan.nbErreurs > erreursAvant) {
                return p;
            }
            boolean renommee = !l.libelle().equals(u.getLibelle());
            if (renommee) {
                renommages.add(new Renommage(u.getId(), l.libelle()));
                bilan.misesAJour++;
            }
            if (deplacee) {
                bilan.deplacements++;
            }
            if (!renommee && !deplacee) {
                bilan.inchangees++;
            }
            return p;
        }

        private boolean preparerDeplacement(LigneUnite l, UniteOrganisation u, Set<String> enCours) {
            if (!RegleRattachement.estDeplacable(u.getType())) {
                bilan.erreur(l.numero(), l.code(), "code_parent", "FSE_NON_DEPLACABLE",
                        u.getCode() + " reste rattaché à son pays : un FSE ne change pas de pays.");
                return false;
            }
            Position parent = parent(l, enCours);
            if (parent == null || !rattachable(l, parent)) {
                return false;
            }
            if (!parent.zone().equals(u.getZone())) {
                bilan.erreur(l.numero(), l.code(), "code_parent", "ZONE_INCOHERENTE", u.getCode() + " est en zone "
                        + u.getZone() + " et " + parent.code() + " en zone " + parent.zone()
                        + " : une branche ne change pas de zone.");
                return false;
            }
            deplacements.add(new Deplacement(l.numero(), l.code(), u.getId(), parent.id()));
            return true;
        }

        private Position parent(LigneUnite l, Set<String> enCours) {
            enCours.add(l.code());
            Position parent = resoudre(l.codeParent(), enCours);
            enCours.remove(l.code());
            if (parent == null) {
                LigneUnite ligneParent = lignes.get(l.codeParent());
                Integer numero = ligneParent != null ? Integer.valueOf(ligneParent.numero()) : lignesInvalides.get(l.codeParent());
                if (numero != null) {
                    bilan.erreur(l.numero(), l.code(), "code_parent", "PARENT_EN_ERREUR",
                            "Le parent " + l.codeParent() + " (ligne " + numero + ") est en erreur.");
                } else {
                    bilan.erreur(l.numero(), l.code(), "code_parent", "PARENT_INEXISTANT",
                            "Le parent " + l.codeParent() + " n'existe ni en base ni dans le fichier.");
                }
            }
            return parent;
        }

        private boolean rattachable(LigneUnite l, Position parent) {
            String erreur = null;
            String code = "RATTACHEMENT_INTERDIT";
            if (parent.type().estRegroupement()) {
                erreur = "Un regroupement n'accueille aucune unité : il a des membres, pas d'enfants ("
                        + parent.code() + ").";
            } else if (!RegleRattachement.peutRattacher(parent.type(), l.type())) {
                erreur = "Impossible de rattacher un(e) " + l.type().libelle() + " à " + parent.type().libelle() + " "
                        + parent.code() + ". " + RegleRattachement.description(l.type());
            } else if (!parent.active()) {
                code = "PARENT_INACTIF";
                erreur = parent.type().libelle() + " " + parent.code() + " est inactif : on ne peut rien y rattacher.";
            }
            if (erreur != null) {
                bilan.erreur(l.numero(), l.code(), "code_parent", code, erreur);
            }
            return erreur == null;
        }

        private Position retenir(Position p) {
            positions.put(p.code(), p);
            return p;
        }

        private Position echec(LigneUnite l, String champ, String codeErreur, String message) {
            bilan.erreur(l.numero(), l.code(), champ, codeErreur, message);
            return echec(l);
        }

        private Position echec(LigneUnite l) {
            echecs.add(l.code());
            return null;
        }
    }

    // ── Membres de regroupements ────────────────────────────────────────────

    public RapportImport importerMembres(String nomFichier, byte[] contenu, boolean essai, String acteur) {
        long debut = System.nanoTime();
        TableauImport tableau = TableauImport.lire(nomFichier, contenu, COLONNES_MEMBRES, ALIAS);
        Bilan bilan = new Bilan("MEMBRES", tableau.format(), essai, tableau.lignes().size());
        return executer(bilan, debut, () -> traiterMembres(tableau, bilan, acteur));
    }

    private record LigneMembre(int numero, String regroupement, String membre) {
    }

    private record Ajout(int ligne, UniteOrganisation regroupement, UniteOrganisation membre) {
    }

    private void traiterMembres(TableauImport tableau, Bilan bilan, String acteur) {
        List<LigneMembre> lignes = new ArrayList<>();
        Map<String, Integer> paires = new HashMap<>();
        for (TableauImport.Ligne l : tableau.lignes()) {
            String regroupement = l.valeur("code_regroupement").toUpperCase(Locale.ROOT);
            String membre = l.valeur("code_membre").toUpperCase(Locale.ROOT);
            if (regroupement.isEmpty()) {
                bilan.erreur(l.numero(), null, "code_regroupement", "CODE_OBLIGATOIRE", "Le code du regroupement est obligatoire.");
                continue;
            }
            if (membre.isEmpty()) {
                bilan.erreur(l.numero(), regroupement, "code_membre", "CODE_OBLIGATOIRE", "Le code du membre est obligatoire.");
                continue;
            }
            Integer premiere = paires.putIfAbsent(regroupement + "\u0000" + membre, l.numero());
            if (premiere != null) {
                bilan.erreur(l.numero(), membre, "code_membre", "DOUBLON_FICHIER",
                        membre + " dans " + regroupement + " figure déjà ligne " + premiere + ".");
                continue;
            }
            lignes.add(new LigneMembre(l.numero(), regroupement, membre));
        }

        Set<String> codes = new HashSet<>();
        lignes.forEach(l -> {
            codes.add(l.regroupement());
            codes.add(l.membre());
        });
        Map<String, UniteOrganisation> existantes = parCode(codes);
        List<Ajout> candidats = new ArrayList<>();
        for (LigneMembre l : lignes) {
            UniteOrganisation r = existantes.get(l.regroupement());
            UniteOrganisation m = existantes.get(l.membre());
            String erreur = null;
            String code = null;
            String champ = "code_membre";
            if (r == null) {
                champ = "code_regroupement";
                code = "REGROUPEMENT_INEXISTANT";
                erreur = "Regroupement " + l.regroupement() + " introuvable.";
            } else if (!r.getType().estRegroupement()) {
                champ = "code_regroupement";
                code = "PAS_UN_REGROUPEMENT";
                erreur = r.libelleCourt() + " n'est pas un regroupement.";
            } else if (m == null) {
                code = "UNITE_INEXISTANTE";
                erreur = "Unité " + l.membre() + " introuvable.";
            } else if (m.getType().estRegroupement()) {
                code = "MEMBRE_REGROUPEMENT_INTERDIT";
                erreur = "Un regroupement ne peut pas être membre d'un autre regroupement.";
            } else if (!m.getZone().equals(r.getZone())) {
                code = "ZONE_INCOHERENTE";
                erreur = m.getCode() + " est en zone " + m.getZone() + ", le regroupement " + r.getCode()
                        + " en zone " + r.getZone() + ".";
            } else if (!m.estActive()) {
                code = "UNITE_INACTIVE";
                erreur = m.libelleCourt() + " est inactif : réactivez-le d'abord.";
            } else if (!m.getChemin().startsWith(r.cheminParent())) {
                code = "MEMBRE_HORS_BRANCHE";
                erreur = m.getCode() + " n'est pas dans la branche du parent du regroupement " + r.getCode() + ".";
            }
            if (erreur != null) {
                bilan.erreur(l.numero(), l.membre(), champ, code, erreur);
            } else {
                candidats.add(new Ajout(l.numero(), r, m));
            }
        }

        Set<UUID> idsRegroupements = candidats.stream().map(a -> a.regroupement().getId()).collect(Collectors.toSet());
        Map<UUID, Set<UUID>> actuels = new HashMap<>();
        if (!idsRegroupements.isEmpty()) {
            for (RegroupementMembre m : membres.findByRegroupementIdIn(idsRegroupements)) {
                actuels.computeIfAbsent(m.getRegroupementId(), k -> new HashSet<>()).add(m.getUniteId());
            }
        }
        Map<UUID, UniteOrganisation> unitesActuelles = parId(actuels.values().stream()
                .flatMap(Set::stream).collect(Collectors.toSet()));
        List<Ajout> ajouts = new ArrayList<>();
        Map<UUID, List<Ajout>> parRegroupement = new LinkedHashMap<>();
        for (Ajout a : candidats) {
            if (actuels.getOrDefault(a.regroupement().getId(), Set.of()).contains(a.membre().getId())) {
                bilan.inchangees++;
            } else {
                parRegroupement.computeIfAbsent(a.regroupement().getId(), k -> new ArrayList<>()).add(a);
            }
        }
        for (List<Ajout> nouveaux : parRegroupement.values()) {
            UniteOrganisation r = nouveaux.get(0).regroupement();
            List<UniteOrganisation> deja = actuels.getOrDefault(r.getId(), Set.of()).stream()
                    .map(unitesActuelles::get).filter(Objects::nonNull).toList();
            for (Ajout a : nouveaux) {
                String conflit = chevauchement(a, deja, nouveaux);
                if (conflit != null) {
                    bilan.erreur(a.ligne(), a.membre().getCode(), "code_membre", "DOUBLON_COUVERTURE", conflit);
                } else {
                    ajouts.add(a);
                }
            }
        }
        if (bilan.nbErreurs > 0) {
            return;
        }
        bilan.creations = ajouts.size();
        Timestamp maintenant = Timestamp.from(Instant.now());
        jdbc.batchUpdate("""
                insert into regroupement_membre (id, regroupement_id, unite_id, ajoute_le, ajoute_par)
                values (?, ?, ?, ?, ?)
                """, ajouts, LOT, (ps, a) -> {
            ps.setObject(1, UUID.randomUUID());
            ps.setObject(2, a.regroupement().getId());
            ps.setObject(3, a.membre().getId());
            ps.setTimestamp(4, maintenant);
            ps.setString(5, acteur);
        });
    }

    /** Un regroupement couvre chaque membre et sa descendance : deux membres sur la même branche feraient doublon. */
    private static String chevauchement(Ajout a, List<UniteOrganisation> deja, List<Ajout> nouveaux) {
        UniteOrganisation m = a.membre();
        String r = a.regroupement().getCode();
        for (UniteOrganisation autre : deja) {
            if (autre.estAscendantDe(m)) {
                return m.getCode() + " est déjà couvert par " + r + " via " + autre.getCode() + ", qui en est membre.";
            }
            if (m.estAscendantDe(autre)) {
                return m.getCode() + " couvrirait " + autre.getCode() + ", déjà membre de " + r + ".";
            }
        }
        for (Ajout b : nouveaux) {
            if (b != a && (b.membre().estAscendantDe(m) || m.estAscendantDe(b.membre()))) {
                return m.getCode() + " et " + b.membre().getCode() + " (ligne " + b.ligne() + ") sont sur la même "
                        + "branche : " + r + " couvrirait deux fois les mêmes unités.";
            }
        }
        return null;
    }

    // ── Modèles ─────────────────────────────────────────────────────────────

    public ModeleImport modele(String objet, String format) {
        return ModeleImport.de(objet, format);
    }

    // ── Commun ──────────────────────────────────────────────────────────────

    private RapportImport executer(Bilan bilan, long debut, Runnable traitement) {
        try {
            tx.executeWithoutResult(status -> {
                traitement.run();
                if (bilan.essai || bilan.nbErreurs > 0) {
                    status.setRollbackOnly();
                }
            });
        } catch (DataAccessException e) {
            String cause = String.valueOf(NestedExceptionUtils.getMostSpecificCause(e).getMessage());
            bilan.erreur(0, null, null, "ERREUR_BASE", "Écriture refusée par la base : "
                    + (cause.length() > 300 ? cause.substring(0, 300) + "…" : cause));
        }
        return bilan.rapport(!bilan.essai && bilan.nbErreurs == 0, debut);
    }

    private Map<String, UniteOrganisation> parCode(Collection<String> codes) {
        Map<String, UniteOrganisation> parCode = new HashMap<>();
        List<String> liste = new ArrayList<>(codes);
        for (int i = 0; i < liste.size(); i += 1000) {
            unites.findByCodeIn(liste.subList(i, Math.min(i + 1000, liste.size())))
                    .forEach(u -> parCode.put(u.getCode(), u));
        }
        return parCode;
    }

    private Map<UUID, UniteOrganisation> parId(Collection<UUID> ids) {
        Map<UUID, UniteOrganisation> parId = new HashMap<>();
        List<UUID> liste = new ArrayList<>(ids);
        for (int i = 0; i < liste.size(); i += 1000) {
            unites.findAllById(liste.subList(i, Math.min(i + 1000, liste.size())))
                    .forEach(u -> parId.put(u.getId(), u));
        }
        return parId;
    }

    /** « Sous-distributeur », « sous distributeur », « SOUS_DISTRIBUTEUR » → SOUS_DISTRIBUTEUR. */
    static String normaliser(String s) {
        return TableauImport.sansAccents(s == null ? "" : s).trim().toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private static Map<String, TypeUnite> types() {
        Map<String, TypeUnite> types = new HashMap<>();
        for (TypeUnite t : TypeUnite.values()) {
            types.put(normaliser(t.name()), t);
            types.put(normaliser(t.libelle()), t);
        }
        return types;
    }

    private static final class Bilan {

        private final String objet;
        private final String format;
        private final boolean essai;
        private final int nbLignes;
        private final List<ErreurImport> erreurs = new ArrayList<>();
        private int nbErreurs;
        private int creations;
        private int misesAJour;
        private int deplacements;
        private int inchangees;

        Bilan(String objet, String format, boolean essai, int nbLignes) {
            this.objet = objet;
            this.format = format;
            this.essai = essai;
            this.nbLignes = nbLignes;
        }

        void erreur(int ligne, String code, String champ, String codeErreur, String message) {
            nbErreurs++;
            if (erreurs.size() < MAX_ERREURS) {
                erreurs.add(new ErreurImport(ligne, code, champ, codeErreur, message));
            }
        }

        RapportImport rapport(boolean applique, long debut) {
            List<ErreurImport> triees = erreurs.stream().sorted(Comparator.comparingInt(ErreurImport::ligne)).toList();
            return new RapportImport(objet, format, essai, applique, nbLignes, creations, misesAJour, deplacements,
                    inchangees, nbErreurs, triees, (System.nanoTime() - debut) / 1_000_000);
        }
    }
}
