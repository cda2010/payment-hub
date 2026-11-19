package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.alias.AliasLecture;
import com.gateway.payment.network.alias.AliasLecture.AliasVu;
import com.gateway.payment.network.api.dto.GuichetDtos.CaisseDto;
import com.gateway.payment.network.api.dto.GuichetDtos.CreationPointVenteRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.AffectationDto;
import com.gateway.payment.network.api.dto.GuichetDtos.AffectationRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.CreationUtilisateurRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.DesaffectationRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.GuichetMoiDto;
import com.gateway.payment.network.api.dto.GuichetDtos.RattachementDto;
import com.gateway.payment.network.api.dto.GuichetDtos.DestinationAccesRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.MouvementDto;
import com.gateway.payment.network.api.dto.GuichetDtos.MouvementRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.PointCaisseDto;
import com.gateway.payment.network.api.dto.GuichetDtos.PointVenteDto;
import com.gateway.payment.network.api.dto.GuichetDtos.SituationCaisseDto;
import com.gateway.payment.network.api.dto.GuichetDtos.UtilisateurDto;
import com.gateway.payment.network.connexion.ConnexionGuichet;
import com.gateway.payment.network.envoi.EnvoiAcces;
import com.gateway.payment.network.domain.AffectationCaissier;
import com.gateway.payment.network.domain.Caisse;
import com.gateway.payment.network.domain.MouvementAlias;
import com.gateway.payment.network.domain.OperationCaisse;
import com.gateway.payment.network.domain.PointVente;
import com.gateway.payment.network.domain.PositionGuichet;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.domain.UniteOrganisation;
import com.gateway.payment.network.domain.UniteUtilisateur;
import com.gateway.payment.network.repository.AffectationCaissierRepository;
import com.gateway.payment.network.repository.CaisseRepository;
import com.gateway.payment.network.repository.MouvementAliasRepository;
import com.gateway.payment.network.repository.OperationCaisseRepository;
import com.gateway.payment.network.repository.PointVenteRepository;
import com.gateway.payment.network.repository.PositionGuichetRepository;
import com.gateway.payment.network.repository.UniteOrganisationRepository;
import com.gateway.payment.network.repository.UniteUtilisateurRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/** Utilisateurs, points de vente, séance de caisse et transfert entre alias. */
@Service
public class GuichetService {

    public static final String ADMIN = "ADMIN";
    public static final String CAISSIER = "CAISSIER";
    public static final String CPTEOPERATION = "CPTEOPERATION";
    public static final String APPROVISIONNEMENT = "APPROVISIONNEMENT";
    public static final String REMONTEE = "REMONTEE";
    public static final String DEPOT = "DEPOT";
    public static final String RETRAIT = "RETRAIT";
    private static final List<String> SEANCE_BLOQUANTE = List.of("OUVERTE");

    private static final Pattern ROLE = Pattern.compile("[A-Z][A-Z0-9_]{1,31}");
    private static final Pattern LOGIN = Pattern.compile("[a-z][a-z0-9._-]{2,39}");
    private static final Pattern TELEPHONE = Pattern.compile("\\+?[0-9]{8,15}");
    private static final Pattern EMAIL = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    private static final String MAJUSCULES = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULES = "abcdefghijkmnopqrstuvwxyz";
    private static final String CHIFFRES = "23456789";
    private static final String SYMBOLES = "@#%";
    private static final SecureRandom ALEA = new SecureRandom();

    private final UniteOrganisationRepository unites;
    private final UniteUtilisateurRepository utilisateurs;
    private final PointVenteRepository points;
    private final CaisseRepository caisses;
    private final MouvementAliasRepository mouvements;
    private final PositionGuichetRepository positions;
    private final OperationCaisseRepository operations;
    private final AliasLecture alias;
    private final ConnexionGuichet connexion;
    private final EnvoiAcces envoi;
    private final AffectationCaissierRepository affectations;
    private final ContexteUnite contexte;

    public GuichetService(
            UniteOrganisationRepository unites,
            UniteUtilisateurRepository utilisateurs,
            PointVenteRepository points,
            CaisseRepository caisses,
            MouvementAliasRepository mouvements,
            PositionGuichetRepository positions,
            OperationCaisseRepository operations,
            AliasLecture alias,
            ConnexionGuichet connexion,
            EnvoiAcces envoi,
            AffectationCaissierRepository affectations,
            ContexteUnite contexte
    ) {
        this.unites = unites;
        this.utilisateurs = utilisateurs;
        this.points = points;
        this.caisses = caisses;
        this.mouvements = mouvements;
        this.positions = positions;
        this.operations = operations;
        this.alias = alias;
        this.connexion = connexion;
        this.envoi = envoi;
        this.affectations = affectations;
        this.contexte = contexte;
    }

    @Transactional(readOnly = true)
    public List<UtilisateurDto> utilisateurs(UUID uniteId) {
        chargerUnite(uniteId);
        return utilisateurs.findByUniteIdOrderByNomAsc(uniteId).stream().map(u -> utilisateur(u)).toList();
    }

    public UtilisateurDto creerUtilisateur(UUID uniteId, CreationUtilisateurRequest req, String acteur) {
        UniteOrganisation unite = chargerUnite(uniteId);
        String login = login(req.identifiant());
        String role = role(req.role());
        Contact contact = contact(req.canal(), req.telephone(), req.email());
        List<UniteUtilisateur> deja = utilisateurs.findAllByIdentifiantIgnoreCase(login);
        if (deja.stream().anyMatch(u -> uniteId.equals(u.getUniteId()))) {
            throw NetworkException.conflit("UTILISATEUR_EXISTANT", "Ce login est déjà utilisé dans cette unité.");
        }
        if (ADMIN.equals(role) && utilisateurs.existsByUniteIdAndRoleAndStatut(uniteId, ADMIN, "ACTIF")) {
            throw NetworkException.regle("ADMIN_UNIQUE", "L'unité a déjà un administrateur actif.");
        }
        UUID id = UUID.randomUUID();
        if (!deja.isEmpty()) {
            connexion.rattacher(login, role);
            UniteUtilisateur cree = utilisateurs.save(ligne(id, uniteId, login, req.nom().trim(), role, contact, acteur));
            return utilisateur(cree, false);
        }
        String secret = motDePasseTemporaire();
        connexion.ouvrir(id, uniteId, login, req.nom().trim(), role, audience(unite), secret, contact.email());
        envoi.envoyer(contact.canal(), contact.destinataire(), login, secret);
        UniteUtilisateur cree = utilisateurs.save(ligne(id, uniteId, login, req.nom().trim(), role, contact, acteur));
        return utilisateur(cree, true);
    }

    public UtilisateurDto activer(UUID uniteId, UUID utilisateurId) {
        return changerAcces(uniteId, utilisateurId, true);
    }

    public UtilisateurDto desactiver(UUID uniteId, UUID utilisateurId) {
        return changerAcces(uniteId, utilisateurId, false);
    }

    public void reinitialiserMotDePasse(UUID uniteId, UUID utilisateurId, DestinationAccesRequest req) {
        chargerUnite(uniteId);
        UniteUtilisateur utilisateur = utilisateurDeLunite(uniteId, utilisateurId);
        String canal = utilisateur.getCanal();
        String telephone = utilisateur.getTelephone();
        String email = utilisateur.getEmail();
        if (req != null && req.canal() != null && !req.canal().isBlank()) {
            Contact saisi = contact(req.canal(), req.telephone(), req.email());
            canal = saisi.canal();
            telephone = saisi.telephone();
            email = saisi.email();
        }
        if (canal == null || ("SMS".equals(canal) && (telephone == null || telephone.isBlank()))
                || ("EMAIL".equals(canal) && (email == null || email.isBlank()))) {
            throw NetworkException.regle("CONTACT_REQUIS",
                    "Indiquez un téléphone pour le SMS, ou une adresse pour l'e-mail.");
        }
        String secret = motDePasseTemporaire();
        String destinataire = "SMS".equals(canal) ? telephone : email;
        connexion.reinitialiserMotDePasse(utilisateur.getIdentifiant(), secret);
        envoi.envoyer(canal, destinataire, utilisateur.getIdentifiant(), secret);
        utilisateur.setCanal(canal);
        utilisateur.setTelephone(telephone);
        utilisateur.setEmail(email);
        utilisateurs.save(utilisateur);
    }

    private UtilisateurDto changerAcces(UUID uniteId, UUID utilisateurId, boolean actif) {
        chargerUnite(uniteId);
        UniteUtilisateur utilisateur = utilisateurDeLunite(uniteId, utilisateurId);
        if (actif && ADMIN.equals(utilisateur.getRole())
                && !utilisateur.estActif()
                && utilisateurs.existsByUniteIdAndRoleAndStatut(uniteId, ADMIN, "ACTIF")) {
            throw NetworkException.regle("ADMIN_UNIQUE", "L'unité a déjà un administrateur actif.");
        }
        if (!actif && caisses.existsByUtilisateurIdAndStatut(utilisateur.getId(), "OUVERTE")) {
            throw NetworkException.regle("CAISSE_OUVERTE", "Ferme la caisse avant de désactiver ce caissier.");
        }
        utilisateur.setStatut(actif ? "ACTIF" : "INACTIF");
        utilisateurs.save(utilisateur);
        boolean encore = utilisateurs.findAllByIdentifiantIgnoreCase(utilisateur.getIdentifiant()).stream()
                .anyMatch(UniteUtilisateur::estActif);
        connexion.definirAcces(utilisateur.getIdentifiant(), encore);
        return utilisateur(utilisateur);
    }

    @Transactional(readOnly = true)
    public List<PointVenteDto> pointsDeVente(UUID uniteId) {
        chargerUnite(uniteId);
        return points.findByUniteIdOrderByCodeAsc(uniteId).stream().map(GuichetService::point).toList();
    }

    public PointVenteDto creerPointDeVente(UUID uniteId, CreationPointVenteRequest req, String acteur) {
        UniteOrganisation unite = chargerUnite(uniteId);
        if (unite.getType().estRegroupement()) {
            throw NetworkException.regle("POINT_DE_VENTE_INTERDIT", "Un regroupement n'a pas de point de vente.");
        }
        String code = req.code().trim().toUpperCase(Locale.ROOT);
        if (points.existsByCode(code)) {
            throw NetworkException.conflit("CODE_EXISTANT", "Un point de vente " + code + " existe déjà.");
        }
        PointVente cree = points.save(PointVente.builder()
                .id(UUID.randomUUID())
                .uniteId(uniteId)
                .code(code)
                .libelle(req.libelle().trim())
                .statut("ACTIF")
                .createdBy(acteur)
                .updatedBy(acteur)
                .build());
        return point(cree);
    }

    @Transactional
    public CaisseDto ouvrir(UUID pointVenteId, BigDecimal fondsDeclare, String acteur, String authorization) {
        BigDecimal declare = argent(fondsDeclare);
        PointVente point = chargerPoint(pointVenteId);
        exigerUnite(acteur, point.getUniteId());
        UniteUtilisateur caissier = caissierConnecte(point.getUniteId(), acteur);
        if (!affecteAujourdhui(point.getId(), caissier.getId())) {
            throw NetworkException.regle("AFFECTATION_REQUISE", "Ce caissier n'est pas affecté à ce point de vente.");
        }
        if (seanceBloquante(point.getId())) {
            throw NetworkException.conflit("CAISSE_OCCUPEE", "Une caisse est déjà ouverte sur ce point de vente.");
        }
        List<UUID> ids = utilisateurs.findAllByIdentifiantIgnoreCase(caissier.getIdentifiant()).stream()
                .map(UniteUtilisateur::getId)
                .toList();
        if (caisses.existsByUtilisateurIdInAndStatut(ids, "OUVERTE")) {
            throw NetworkException.conflit("CAISSIER_OCCUPE", "Ce caissier a déjà une caisse en cours.");
        }
        AliasVu compte = alias.compteOperation(point.getId(), authorization);
        if (compte == null || !compte.active() || !CPTEOPERATION.equals(compte.etiquette())) {
            throw NetworkException.regle("COMPTE_OPERATION_ABSENT",
                    "Le point de vente n'a pas de compte d'opération actif.");
        }
        PositionGuichet position = positionDe(point.getId());
        BigDecimal ecart = declare.subtract(position.getFondsAttendu());
        Caisse cree = caisses.save(Caisse.builder()
                .id(UUID.randomUUID())
                .pointVenteId(point.getId())
                .utilisateurId(caissier.getId())
                .numero(caisses.maxNumero(point.getId()) + 1)
                .statut("OUVERTE")
                .fondsAttendu(position.getFondsAttendu())
                .fondsDeclare(declare)
                .ecartOuverture(ecart)
                .soldeCompteOuverture(position.getSoldeCompte())
                .depots(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .retraits(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .ouverteLe(Instant.now())
                .ouvertePar(acteur)
                .build());
        return vueCaisse(cree, point, caissier, position);
    }

    @Transactional
    public CaisseDto fermer(UUID caisseId, BigDecimal fondsCompte, String acteur) {
        BigDecimal compte = argent(fondsCompte);
        Caisse caisse = caisses.findById(caisseId)
                .orElseThrow(() -> NetworkException.introuvable("CAISSE_INEXISTANTE", "Caisse introuvable."));
        if (!caisse.estOuverte()) {
            throw NetworkException.regle("CAISSE_FERMEE", "La caisse n'est pas ouverte.");
        }
        PointVente point = chargerPoint(caisse.getPointVenteId());
        exigerUnite(acteur, point.getUniteId());
        UniteUtilisateur acteurMetier = membre(point.getUniteId(), acteur)
                .filter(UniteUtilisateur::estActif)
                .orElseThrow(() -> NetworkException.regle("FERMETURE_INTERDITE",
                        "Le caissier ou l'administrateur ferme la caisse."));
        boolean titulaire = caisse.getUtilisateurId().equals(acteurMetier.getId());
        if (!titulaire && !ADMIN.equals(acteurMetier.getRole())) {
            throw NetworkException.regle("FERMETURE_INTERDITE", "Le caissier ou l'administrateur ferme la caisse.");
        }
        PositionGuichet position = positionDe(point.getId());
        BigDecimal attendu = physique(caisse);
        BigDecimal ecart = compte.subtract(attendu);
        caisse.setFondsPhysiqueCloture(compte);
        caisse.setSoldeCompteFermeture(position.getSoldeCompte());
        caisse.setEcartFermeture(ecart);
        caisse.setFermeeLe(Instant.now());
        caisse.setFermeePar(acteur);
        caisse.setStatut("FERMEE");
        position.setFondsAttendu(compte);
        positions.save(position);
        UniteUtilisateur titulaireMetier = utilisateurs.findById(caisse.getUtilisateurId()).orElse(acteurMetier);
        return vueCaisse(caisses.save(caisse), point, titulaireMetier, position);
    }

    @Transactional
    public CaisseDto regulariser(UUID caisseId, String motif, Authentication auth) {
        Caisse caisse = chargerSeance(caisseId);
        PointVente point = chargerPoint(caisse.getPointVenteId());
        autoriser(point.getUniteId(), auth);
        String raison = motifCourt(motif);
        PositionGuichet position = positionDe(point.getId());
        if ("EN_VALIDATION".equals(caisse.getStatut())) {
            if (caisses.existsByPointVenteIdAndStatut(point.getId(), "OUVERTE")) {
                throw NetworkException.conflit("CAISSE_DEJA_OUVERTE", "Une caisse est déjà ouverte sur ce point de vente.");
            }
            caisse.setStatut("OUVERTE");
            caisse.setSoldeCompteOuverture(position.getSoldeCompte());
            caisse.setOuverteLe(Instant.now());
        } else if ("A_REGULARISER".equals(caisse.getStatut())) {
            caisse.setStatut("REGULARISEE");
            position.setFondsAttendu(nz(caisse.getFondsPhysiqueCloture()));
            positions.save(position);
        } else {
            throw NetworkException.regle("RIEN_A_REGULARISER", "Cette séance n'attend pas de validation.");
        }
        caisse.setMotif(raison);
        caisse.setRegulariseeLe(Instant.now());
        caisse.setRegulariseePar(auth.getName());
        return vueCaisse(caisses.save(caisse), point, utilisateurs.findById(caisse.getUtilisateurId()).orElse(null), position);
    }

    @Transactional
    public CaisseDto refuser(UUID caisseId, String motif, Authentication auth) {
        Caisse caisse = chargerSeance(caisseId);
        PointVente point = chargerPoint(caisse.getPointVenteId());
        autoriser(point.getUniteId(), auth);
        String raison = motifCourt(motif);
        if ("EN_VALIDATION".equals(caisse.getStatut())) {
            caisse.setStatut("ANNULEE");
            caisse.setFermeeLe(Instant.now());
            caisse.setFermeePar(auth.getName());
        } else if ("A_REGULARISER".equals(caisse.getStatut())) {
            caisse.setStatut("OUVERTE");
            caisse.setFondsPhysiqueCloture(null);
            caisse.setSoldeCompteFermeture(null);
            caisse.setEcartFermeture(null);
            caisse.setFermeeLe(null);
            caisse.setFermeePar(null);
        } else {
            throw NetworkException.regle("RIEN_A_REGULARISER", "Cette séance n'attend pas de validation.");
        }
        caisse.setMotif(raison);
        caisse.setRegulariseeLe(Instant.now());
        caisse.setRegulariseePar(auth.getName());
        return vueCaisse(caisses.save(caisse), point,
                utilisateurs.findById(caisse.getUtilisateurId()).orElse(null), positionDe(point.getId()));
    }

    @Transactional
    public CaisseDto enregistrerOperation(UUID caisseId, String sensBrut, BigDecimal montant, String reference, String acteur) {
        String sens = sensOperation(sensBrut);
        BigDecimal valeur = argentStrict(montant);
        Caisse caisse = caisses.findById(caisseId)
                .orElseThrow(() -> NetworkException.introuvable("CAISSE_INEXISTANTE", "Caisse introuvable."));
        if (!caisse.estOuverte()) {
            throw NetworkException.regle("CAISSE_FERMEE", "Les opérations exigent une caisse ouverte.");
        }
        PointVente point = chargerPoint(caisse.getPointVenteId());
        exigerUnite(acteur, point.getUniteId());
        UniteUtilisateur caissier = caissierConnecte(point.getUniteId(), acteur);
        if (!caisse.getUtilisateurId().equals(caissier.getId())) {
            throw NetworkException.regle("CAISSE_AUTRE_CAISSIER", "La caisse est ouverte à un autre identifiant.");
        }
        PositionGuichet position = positionDe(point.getId());
        if (DEPOT.equals(sens)) {
            if (position.getSoldeCompte().compareTo(valeur) < 0) {
                throw NetworkException.regle("SOLDE_COMPTE_INSUFFISANT",
                        "Le compte d'opération ne couvre pas ce dépôt.");
            }
            position.setSoldeCompte(position.getSoldeCompte().subtract(valeur));
            caisse.setDepots(nz(caisse.getDepots()).add(valeur));
        } else {
            if (physique(caisse).compareTo(valeur) < 0) {
                throw NetworkException.regle("ESPECES_INSUFFISANTES",
                        "Les espèces en caisse ne couvrent pas ce retrait.");
            }
            position.setSoldeCompte(position.getSoldeCompte().add(valeur));
            caisse.setRetraits(nz(caisse.getRetraits()).add(valeur));
        }
        positions.save(position);
        operations.save(OperationCaisse.builder()
                .id(UUID.randomUUID())
                .caisseId(caisse.getId())
                .sens(sens)
                .montant(valeur)
                .reference(reference == null || reference.isBlank() ? null : reference.trim())
                .creeLe(Instant.now())
                .creePar(acteur)
                .build());
        return vueCaisse(caisses.save(caisse), point, caissier, position);
    }

    public SituationCaisseDto situation(String identifiant) {
        UniteUtilisateur personne = personneConnectee(identifiant);
        UniteOrganisation unite = chargerUnite(personne.getUniteId());
        boolean caissier = CAISSIER.equals(personne.getRole());
        List<PointVente> comptoirs = points.findByUniteIdOrderByCodeAsc(unite.getId()).stream()
                .filter(PointVente::estActif)
                .filter(p -> !caissier || affecteAujourdhui(p.getId(), personne.getId()))
                .toList();
        java.util.Map<UUID, PositionGuichet> parPoint = comptoirs.isEmpty()
                ? java.util.Map.of()
                : positions.findAllById(comptoirs.stream().map(PointVente::getId).toList()).stream()
                .collect(java.util.stream.Collectors.toMap(PositionGuichet::getPointVenteId, p -> p));
        java.util.Map<UUID, List<Caisse>> bloquees = seancesBloquantes(comptoirs);
        List<PointCaisseDto> vues = comptoirs.stream().map(p -> {
            PositionGuichet position = parPoint.get(p.getId());
            List<Caisse> enCours = bloquees.getOrDefault(p.getId(), List.of());
            String message = messagePoint(enCours);
            return new PointCaisseDto(p.getId(), p.getCode(), p.getLibelle(),
                    position == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : nz(position.getFondsAttendu()),
                    position == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : nz(position.getSoldeCompte()),
                    message == null, message);
        }).toList();
        Caisse ouverte = caisses.findFirstByUtilisateurIdAndStatut(personne.getId(), "OUVERTE").orElse(null);
        java.util.Map<UUID, PointVente> pointsParId = points.findByUniteIdOrderByCodeAsc(unite.getId()).stream()
                .collect(java.util.stream.Collectors.toMap(PointVente::getId, p -> p));
        java.util.Map<UUID, UniteUtilisateur> gens = utilisateurs.findByUniteIdOrderByNomAsc(unite.getId()).stream()
                .collect(java.util.stream.Collectors.toMap(UniteUtilisateur::getId, u -> u));
        return new SituationCaisseDto(
                caissier,
                !caissier || ouverte != null,
                personne.getId(),
                personne.getIdentifiant(),
                personne.getNom(),
                personne.getRole(),
                unite.getCode(),
                unite.getLibelle(),
                ouverte == null ? null : vueCaisse(ouverte, pointsParId.get(ouverte.getPointVenteId()),
                        gens.get(ouverte.getUtilisateurId()), positionDeLecture(ouverte.getPointVenteId(), parPoint)),
                List.of(),
                vues);
    }

    /** Séances du connecté, ou d'un caissier désigné par son login quand l'appelant est l'admin de l'unité. */
    @Transactional(readOnly = true)
    public List<CaisseDto> seances(String identifiant, String loginDemande, String etat) {
        UniteUtilisateur personne = personneConnectee(identifiant);
        chargerUnite(personne.getUniteId());
        boolean archives = "archives".equalsIgnoreCase(etat == null ? "" : etat.trim());
        String login = loginDemande == null ? "" : loginDemande.trim();
        List<Caisse> lignes;
        if (login.isEmpty() && ADMIN.equals(personne.getRole())) {
            if (archives) {
                throw NetworkException.regle("LOGIN_REQUIS", "Indiquez le login du caissier pour consulter les archives.");
            }
            List<UUID> ids = points.findByUniteIdOrderByCodeAsc(personne.getUniteId()).stream()
                    .map(PointVente::getId)
                    .toList();
            lignes = ids.isEmpty()
                    ? List.of()
                    : caisses.findByPointVenteIdInAndStatutIn(ids, List.of("OUVERTE")).stream()
                    .sorted(Comparator.comparing(Caisse::getOuverteLe, Comparator.nullsLast(Comparator.reverseOrder())))
                    .toList();
        } else {
            UniteUtilisateur cible = cibleDesSeances(personne, login);
            lignes = archives
                    ? caisses.findTop40ByUtilisateurIdAndStatutNotOrderByOuverteLeDesc(cible.getId(), "OUVERTE")
                    : caisses.findByUtilisateurIdAndStatutOrderByOuverteLeDesc(cible.getId(), "OUVERTE");
        }
        return vuesSeances(personne.getUniteId(), lignes);
    }

    @Transactional
    public MouvementDto transferer(UUID pointVenteId, MouvementRequest req, String authorization, String acteur) {
        PointVente point = chargerPoint(pointVenteId);
        UniteUtilisateur admin = chargerUtilisateur(point.getUniteId(), req.utilisateurId());
        if (!ADMIN.equals(admin.getRole())) {
            throw NetworkException.regle("ADMIN_REQUIS", "L'administrateur de l'unité fait le transfert.");
        }
        String sens = sens(req.sens());
        AliasVu source = alias.lire(req.aliasSourceId(), authorization);
        AliasVu cible = alias.lire(req.aliasCibleId(), authorization);
        if (APPROVISIONNEMENT.equals(sens)) {
            exiger(source, "UNITE", point.getUniteId(), null);
            exiger(cible, "POINT_VENTE", point.getId(), CPTEOPERATION);
        } else {
            exiger(source, "POINT_VENTE", point.getId(), CPTEOPERATION);
            exiger(cible, "UNITE", point.getUniteId(), null);
        }
        if (source.currency() != null && cible.currency() != null && !source.currency().equals(cible.currency())) {
            throw NetworkException.regle("DEVISE_DISTINCTE", "Les deux alias n'ont pas la même devise.");
        }
        String devise = req.devise() == null || req.devise().isBlank()
                ? (source.currency() == null ? "XOF" : source.currency())
                : req.devise().trim().toUpperCase(Locale.ROOT);
        PositionGuichet position = positionDe(point.getId());
        if (APPROVISIONNEMENT.equals(sens)) {
            position.setSoldeCompte(nz(position.getSoldeCompte()).add(req.montant().setScale(2, RoundingMode.HALF_UP)));
        } else if (nz(position.getSoldeCompte()).compareTo(req.montant()) < 0) {
            throw NetworkException.regle("SOLDE_COMPTE_INSUFFISANT",
                    "Le compte d'opération ne couvre pas cette remontée.");
        } else {
            position.setSoldeCompte(nz(position.getSoldeCompte()).subtract(req.montant().setScale(2, RoundingMode.HALF_UP)));
        }
        positions.save(position);
        MouvementAlias cree = mouvements.save(MouvementAlias.builder()
                .id(UUID.randomUUID())
                .uniteId(point.getUniteId())
                .pointVenteId(point.getId())
                .utilisateurId(admin.getId())
                .sens(sens)
                .aliasSourceId(source.id())
                .aliasCibleId(cible.id())
                .etiquetteSource(etiquette(source))
                .etiquetteCible(etiquette(cible))
                .montant(req.montant())
                .devise(devise)
                .creeLe(Instant.now())
                .creePar(acteur)
                .build());
        return mouvement(cree);
    }

    private static void exiger(AliasVu alias, String ownerKind, UUID ownerId, String etiquette) {
        if (!ownerKind.equals(alias.ownerKind()) || !ownerId.equals(alias.ownerId())) {
            throw NetworkException.regle("ALIAS_HORS_PERIMETRE", "L'alias n'appartient pas au compte attendu.");
        }
        if (etiquette != null && !etiquette.equals(alias.etiquette())) {
            throw NetworkException.regle("ETIQUETTE_ATTENDUE", "L'alias doit porter l'étiquette " + etiquette + ".");
        }
    }

    private static String etiquette(AliasVu alias) {
        return alias.etiquette() == null ? "" : alias.etiquette();
    }

    private UniteUtilisateur personneConnectee(String identifiant) {
        Resolution resolution = resoudre(lignesDe(identifiant));
        if (resolution.personne() == null) {
            throw NetworkException.regle("UNITE_ACTIVE_REQUISE", "Choisissez l'unité d'organisation.");
        }
        return resolution.personne();
    }

    private UniteUtilisateur cibleDesSeances(UniteUtilisateur personne, String login) {
        if (login.isEmpty() || login.equalsIgnoreCase(personne.getIdentifiant())) {
            return personne;
        }
        if (!ADMIN.equals(personne.getRole())) {
            throw new NetworkException(HttpStatus.FORBIDDEN, "ACCES_REFUSE",
                    "Un caissier consulte ses propres séances.");
        }
        return membre(personne.getUniteId(), login)
                .orElseThrow(() -> NetworkException.introuvable(
                        "UTILISATEUR_INEXISTANT", "Ce login n'existe pas dans l'unité."));
    }

    private List<CaisseDto> vuesSeances(UUID uniteId, List<Caisse> lignes) {
        if (lignes.isEmpty()) {
            return List.of();
        }
        java.util.Map<UUID, PointVente> pointsParId = points.findByUniteIdOrderByCodeAsc(uniteId).stream()
                .collect(java.util.stream.Collectors.toMap(PointVente::getId, p -> p));
        java.util.Map<UUID, UniteUtilisateur> gens = utilisateurs.findByUniteIdOrderByNomAsc(uniteId).stream()
                .collect(java.util.stream.Collectors.toMap(UniteUtilisateur::getId, u -> u));
        java.util.Map<UUID, PositionGuichet> parPoint = positions.findAllById(
                lignes.stream().map(Caisse::getPointVenteId).distinct().toList()).stream()
                .collect(java.util.stream.Collectors.toMap(PositionGuichet::getPointVenteId, p -> p));
        return lignes.stream()
                .map(c -> vueCaisse(c, pointsParId.get(c.getPointVenteId()), gens.get(c.getUtilisateurId()),
                        positionDeLecture(c.getPointVenteId(), parPoint)))
                .toList();
    }

    private UniteOrganisation chargerUnite(UUID id) {
        UniteOrganisation unite = unites.findById(id)
                .orElseThrow(() -> NetworkException.introuvable("UNITE_INEXISTANTE", "Unité " + id + " introuvable."));
        if (!unite.estActive()) {
            throw NetworkException.regle("UNITE_INACTIVE", "L'unité n'est pas active.");
        }
        return unite;
    }

    private PointVente chargerPoint(UUID id) {
        PointVente point = points.findById(id)
                .orElseThrow(() -> NetworkException.introuvable("POINT_DE_VENTE_INEXISTANT", "Point de vente introuvable."));
        if (!point.estActif()) {
            throw NetworkException.regle("POINT_DE_VENTE_INACTIF", "Le point de vente n'est pas actif.");
        }
        chargerUnite(point.getUniteId());
        return point;
    }

    private UniteUtilisateur chargerUtilisateur(UUID uniteId, UUID utilisateurId) {
        UniteUtilisateur utilisateur = utilisateurDeLunite(uniteId, utilisateurId);
        if (!utilisateur.estActif()) {
            throw NetworkException.regle("UTILISATEUR_INACTIF", "L'utilisateur n'est pas actif.");
        }
        return utilisateur;
    }

    private UniteUtilisateur utilisateurDeLunite(UUID uniteId, UUID utilisateurId) {
        return utilisateurs.findByIdAndUniteId(utilisateurId, uniteId)
                .orElseThrow(() -> NetworkException.introuvable(
                        "UTILISATEUR_INEXISTANT", "Cet utilisateur n'appartient pas à l'unité."));
    }

    private static String audience(UniteOrganisation unite) {
        return unite.getType() == TypeUnite.MARCHAND ? "MERCHANT" : "AGENT";
    }

    @Transactional(readOnly = true)
    public GuichetMoiDto moi(String identifiant) {
        List<UniteUtilisateur> lignes = lignesDe(identifiant);
        List<RattachementDto> rattachements = rattachementsDe(lignes);
        Resolution resolution = resoudre(lignes);
        UniteUtilisateur personne = resolution.personne();
        if (personne == null) {
            return new GuichetMoiDto(utilisateur(lignes.get(0)), null, null, null,
                    List.of(), List.of(), List.of(), rattachements, null, null);
        }
        UniteOrganisation unite = chargerUnite(personne.getUniteId());
        List<PointVente> comptoirs = points.findByUniteIdOrderByCodeAsc(unite.getId());
        List<AffectationDto> affectes = affectationsDe(comptoirs);
        List<UtilisateurDto> caissiers = utilisateurs.findByUniteIdOrderByNomAsc(unite.getId()).stream()
                .filter(u -> CAISSIER.equals(u.getRole()))
                .map(u -> utilisateur(u))
                .toList();
        return new GuichetMoiDto(utilisateur(personne), unite.getId(), unite.getCode(), unite.getLibelle(),
                comptoirs.stream().map(GuichetService::point).toList(), caissiers, affectes,
                rattachements, personne.getUniteId(), resolution.avertissement());
    }

    public List<AffectationDto> affectationsDuPoint(UUID pointVenteId) {
        PointVente point = chargerPoint(pointVenteId);
        return affectationsDe(List.of(point));
    }

    public List<AffectationDto> affectationsDeLunite(UUID uniteId) {
        chargerUnite(uniteId);
        return affectationsDe(points.findByUniteIdOrderByCodeAsc(uniteId));
    }

    public AffectationDto affecter(UUID pointVenteId, AffectationRequest req, Authentication auth) {
        PointVente point = chargerPoint(pointVenteId);
        autoriser(point.getUniteId(), auth);
        if (!"ACTIF".equals(point.getStatut())) {
            throw NetworkException.regle("POINT_INACTIF", "Le point de vente n'est pas actif.");
        }
        UniteUtilisateur caissier = utilisateurDeLunite(point.getUniteId(), req.utilisateurId());
        if (!CAISSIER.equals(caissier.getRole()) || !caissier.estActif()) {
            throw NetworkException.regle("CAISSIER_REQUIS", "Seul un caissier actif est affecté à un point de vente.");
        }
        LocalDate debut = req.debut();
        LocalDate fin = req.fin();
        if (fin != null && fin.isBefore(debut)) {
            throw NetworkException.regle("PERIODE_INVALIDE", "La date de fin n'est pas avant la date de début.");
        }
        cloturerEcheance(affectations.findByPointVenteIdAndUtilisateurIdAndStatut(
                point.getId(), caissier.getId(), "ACTIF").orElse(null));
        if (affectations.findByPointVenteIdAndUtilisateurIdAndStatut(point.getId(), caissier.getId(), "ACTIF").isPresent()) {
            throw NetworkException.conflit("AFFECTATION_EXISTANTE", "Ce caissier est déjà affecté à ce point de vente.");
        }
        AffectationCaissier cree = affectations.save(AffectationCaissier.builder()
                .id(UUID.randomUUID())
                .pointVenteId(point.getId())
                .utilisateurId(caissier.getId())
                .debut(debut)
                .fin(fin)
                .statut("ACTIF")
                .creePar(auth.getName())
                .build());
        return vue(cree, point, caissier);
    }

    public AffectationDto desaffecter(UUID affectationId, DesaffectationRequest req, Authentication auth) {
        AffectationCaissier ligne = affectations.findById(affectationId)
                .orElseThrow(() -> NetworkException.introuvable("AFFECTATION_INEXISTANTE", "Affectation introuvable."));
        PointVente point = chargerPoint(ligne.getPointVenteId());
        autoriser(point.getUniteId(), auth);
        String motif = req.motif() == null ? "" : req.motif().trim();
        if (motif.length() < 3) {
            throw NetworkException.regle("MOTIF_REQUIS", "La désaffectation indique un motif.");
        }
        if (!ligne.estActive()) {
            throw NetworkException.conflit("AFFECTATION_CLOTUREE", "Cette affectation est déjà close.");
        }
        if (caisses.existsByPointVenteIdAndUtilisateurIdAndStatut(
                point.getId(), ligne.getUtilisateurId(), "OUVERTE")) {
            throw NetworkException.regle("CAISSE_OUVERTE", "Ferme la caisse avant de désaffecter ce caissier.");
        }
        LocalDate aujourd = LocalDate.now();
        if (ligne.getFin() == null || ligne.getFin().isAfter(aujourd)) {
            ligne.setFin(aujourd.isBefore(ligne.getDebut()) ? ligne.getDebut() : aujourd);
        }
        ligne.setMotif(motif);
        ligne.setStatut("CLOTUREE");
        ligne.setClotureeLe(Instant.now());
        ligne.setClotureePar(auth.getName());
        UniteUtilisateur caissier = utilisateurDeLunite(point.getUniteId(), ligne.getUtilisateurId());
        return vue(affectations.save(ligne), point, caissier);
    }

    private List<AffectationDto> affectationsDe(List<PointVente> comptoirs) {
        if (comptoirs.isEmpty()) {
            return List.of();
        }
        java.util.Map<UUID, PointVente> parId = comptoirs.stream()
                .collect(java.util.stream.Collectors.toMap(PointVente::getId, p -> p));
        List<AffectationCaissier> lignes = affectations.findByPointVenteIdInOrderByDebutDesc(parId.keySet());
        lignes.forEach(this::cloturerEcheance);
        return lignes.stream().map(ligne -> {
            PointVente point = parId.get(ligne.getPointVenteId());
            UniteUtilisateur personne = utilisateurs.findById(ligne.getUtilisateurId()).orElse(null);
            return vue(ligne, point, personne);
        }).toList();
    }

    private boolean affecteAujourdhui(UUID pointVenteId, UUID utilisateurId) {
        AffectationCaissier ligne = affectations
                .findByPointVenteIdAndUtilisateurIdAndStatut(pointVenteId, utilisateurId, "ACTIF")
                .orElse(null);
        if (ligne == null || cloturerEcheance(ligne)) {
            return false;
        }
        return !ligne.getDebut().isAfter(LocalDate.now());
    }

    /** Ferme l'affectation dont la date de fin est passée. Retourne vrai si elle vient d'être close. */
    private boolean cloturerEcheance(AffectationCaissier ligne) {
        if (ligne == null || !ligne.estActive() || ligne.getFin() == null || !ligne.getFin().isBefore(LocalDate.now())) {
            return false;
        }
        ligne.setStatut("CLOTUREE");
        ligne.setMotif("Échéance");
        ligne.setClotureeLe(Instant.now());
        ligne.setClotureePar("echeance");
        affectations.save(ligne);
        return true;
    }

    private void autoriser(UUID uniteId, Authentication auth) {
        if (auth != null && auth.getAuthorities().stream().anyMatch(droit ->
                "ROLE_ADMIN".equals(droit.getAuthority()) || "ROLE_CONFIG_MANAGER".equals(droit.getAuthority()))) {
            return;
        }
        String nom = auth == null ? "" : auth.getName();
        boolean adminDeLunite = membre(uniteId, nom)
                .filter(u -> u.estActif() && ADMIN.equals(u.getRole()))
                .isPresent();
        if (!adminDeLunite) {
            throw new NetworkException(HttpStatus.FORBIDDEN, "ACCES_REFUSE",
                    "L'administrateur de l'unité affecte les caissiers.");
        }
    }

    private static AffectationDto vue(AffectationCaissier ligne, PointVente point, UniteUtilisateur personne) {
        LocalDate aujourd = LocalDate.now();
        boolean effective = ligne.estActive()
                && !ligne.getDebut().isAfter(aujourd)
                && (ligne.getFin() == null || !ligne.getFin().isBefore(aujourd));
        String statut = ligne.estActive()
                ? (ligne.getDebut().isAfter(aujourd) ? "A_VENIR" : "ACTIF")
                : ("Échéance".equals(ligne.getMotif()) ? "ECHUE" : "CLOTUREE");
        return new AffectationDto(
                ligne.getId(),
                ligne.getPointVenteId(),
                point == null ? "" : point.getCode(),
                point == null ? "" : point.getLibelle(),
                ligne.getUtilisateurId(),
                personne == null ? "" : personne.getIdentifiant(),
                personne == null ? "" : personne.getNom(),
                ligne.getDebut(),
                ligne.getFin(),
                ligne.getMotif(),
                statut,
                effective);
    }

    private static String login(String brut) {
        String login = brut == null ? "" : brut.trim().toLowerCase(Locale.ROOT);
        if (!LOGIN.matcher(login).matches()) {
            throw NetworkException.regle("LOGIN_INVALIDE",
                    "Le login est un identifiant dédié, pas un numéro de téléphone.");
        }
        return login;
    }

    private static Contact contact(String canalBrut, String telephoneBrut, String emailBrut) {
        String canal = canalBrut == null ? "" : canalBrut.trim().toUpperCase(Locale.ROOT);
        String telephone = normaliserTelephone(telephoneBrut);
        String email = normaliserEmail(emailBrut);
        if ("SMS".equals(canal)) {
            if (telephone == null) {
                throw NetworkException.regle("CONTACT_REQUIS", "Le numéro de téléphone reçoit le SMS.");
            }
            return new Contact(canal, telephone, email, telephone);
        }
        if ("EMAIL".equals(canal)) {
            if (email == null) {
                throw NetworkException.regle("CONTACT_REQUIS", "L'adresse e-mail reçoit le mot de passe.");
            }
            return new Contact(canal, telephone, email, email);
        }
        throw NetworkException.regle("CANAL_INVALIDE", "Le canal est SMS ou EMAIL.");
    }

    private static String normaliserTelephone(String brut) {
        if (brut == null || brut.isBlank()) {
            return null;
        }
        String telephone = brut.replace(" ", "").replace(".", "");
        if (!TELEPHONE.matcher(telephone).matches()) {
            throw NetworkException.regle("TELEPHONE_INVALIDE", "Le téléphone compte 8 à 15 chiffres.");
        }
        return telephone;
    }

    private static String normaliserEmail(String brut) {
        if (brut == null || brut.isBlank()) {
            return null;
        }
        String email = brut.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(email).matches()) {
            throw NetworkException.regle("EMAIL_INVALIDE", "L'adresse e-mail n'est pas valide.");
        }
        return email;
    }

    static String motDePasseTemporaire() {
        List<Character> signes = new ArrayList<>();
        signes.add(tirer(MAJUSCULES));
        signes.add(tirer(MINUSCULES));
        signes.add(tirer(CHIFFRES));
        signes.add(tirer(SYMBOLES));
        String tout = MAJUSCULES + MINUSCULES + CHIFFRES + SYMBOLES;
        while (signes.size() < 12) {
            signes.add(tirer(tout));
        }
        Collections.shuffle(signes, ALEA);
        StringBuilder secret = new StringBuilder(signes.size());
        signes.forEach(secret::append);
        return secret.toString();
    }

    private static char tirer(String alphabet) {
        return alphabet.charAt(ALEA.nextInt(alphabet.length()));
    }

    private record Contact(String canal, String telephone, String email, String destinataire) {
    }

    private static String role(String brut) {
        String role = brut.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
        if (!ROLE.matcher(role).matches()) {
            throw NetworkException.regle("ROLE_INVALIDE", "Le rôle est un code : ADMIN, CAISSIER, ou un autre à définir.");
        }
        return role;
    }

    private static String sens(String brut) {
        String sens = brut.trim().toUpperCase(Locale.ROOT);
        if (!APPROVISIONNEMENT.equals(sens) && !REMONTEE.equals(sens)) {
            throw NetworkException.regle("SENS_INVALIDE", "Le sens est APPROVISIONNEMENT ou REMONTEE.");
        }
        return sens;
    }

    private static UtilisateurDto utilisateur(UniteUtilisateur u) {
        return utilisateur(u, false);
    }

    private static UtilisateurDto utilisateur(UniteUtilisateur u, boolean nouveauCompte) {
        return new UtilisateurDto(u.getId(), u.getUniteId(), u.getIdentifiant(), u.getNom(), u.getRole(), u.getStatut(),
                u.getTelephone(), u.getEmail(), u.getCanal(), nouveauCompte);
    }

    private static UniteUtilisateur ligne(
            UUID id, UUID uniteId, String login, String nom, String role, Contact contact, String acteur) {
        return UniteUtilisateur.builder()
                .id(id)
                .uniteId(uniteId)
                .identifiant(login)
                .nom(nom)
                .role(role)
                .telephone(contact.telephone())
                .email(contact.email())
                .canal(contact.canal())
                .statut("ACTIF")
                .createdBy(acteur)
                .build();
    }

    private static PointVenteDto point(PointVente p) {
        return new PointVenteDto(p.getId(), p.getUniteId(), p.getCode(), p.getLibelle(), p.getStatut());
    }

    private UniteUtilisateur caissierConnecte(UUID uniteId, String acteur) {
        return membre(uniteId, acteur)
                .filter(u -> u.estActif() && CAISSIER.equals(u.getRole()))
                .orElseThrow(() -> NetworkException.regle("CAISSIER_REQUIS", "Seul un caissier ouvre la caisse."));
    }

    private Optional<UniteUtilisateur> membre(UUID uniteId, String identifiant) {
        return utilisateurs.findAllByIdentifiantIgnoreCase(identifiant).stream()
                .filter(u -> uniteId.equals(u.getUniteId()))
                .findFirst();
    }

    private List<UniteUtilisateur> lignesDe(String identifiant) {
        List<UniteUtilisateur> lignes = utilisateurs.findAllByIdentifiantIgnoreCase(identifiant);
        if (lignes.isEmpty()) {
            throw NetworkException.introuvable(
                    "UTILISATEUR_INEXISTANT", "Ce compte n'est pas rattaché à une unité.");
        }
        return lignes;
    }

    private Resolution resoudre(List<UniteUtilisateur> lignes) {
        UUID demandee = contexte.demandee();
        String avertissement = null;
        Optional<Caisse> ouverte = caisseOuverte(lignes);
        if (ouverte.isPresent()) {
            UUID uniteCaisse = points.findById(ouverte.get().getPointVenteId())
                    .map(PointVente::getUniteId)
                    .orElse(null);
            if (uniteCaisse != null) {
                if (demandee != null && !demandee.equals(uniteCaisse)) {
                    avertissement = "Ferme la caisse avant de changer d'unité.";
                }
                demandee = uniteCaisse;
            }
        }
        if (demandee != null) {
            UUID choisie = demandee;
            UniteUtilisateur personne = lignes.stream()
                    .filter(u -> choisie.equals(u.getUniteId()))
                    .findFirst()
                    .orElseThrow(() -> new NetworkException(HttpStatus.FORBIDDEN, "ACCES_REFUSE",
                            "Ce compte n'est pas rattaché à cette unité."));
            return new Resolution(personne, avertissement);
        }
        List<UniteUtilisateur> actives = lignes.stream().filter(UniteUtilisateur::estActif).toList();
        List<UniteUtilisateur> candidats = actives.isEmpty() ? lignes : actives;
        if (candidats.size() == 1) {
            return new Resolution(candidats.get(0), null);
        }
        return new Resolution(null, null);
    }

    private Optional<Caisse> caisseOuverte(List<UniteUtilisateur> lignes) {
        List<UUID> ids = lignes.stream().map(UniteUtilisateur::getId).toList();
        if (ids.isEmpty()) {
            return Optional.empty();
        }
        return caisses.findFirstByUtilisateurIdInAndStatut(ids, "OUVERTE");
    }

    private void exigerUnite(String acteur, UUID uniteId) {
        Resolution resolution = resoudre(lignesDe(acteur));
        if (resolution.personne() == null) {
            throw NetworkException.regle("UNITE_ACTIVE_REQUISE", "Choisissez l'unité d'organisation.");
        }
        if (uniteId.equals(resolution.personne().getUniteId())) {
            return;
        }
        if (resolution.avertissement() != null) {
            throw NetworkException.regle("CAISSE_OUVERTE", resolution.avertissement());
        }
        throw NetworkException.regle("UNITE_ACTIVE", "Cette opération concerne une autre unité.");
    }

    private List<RattachementDto> rattachementsDe(List<UniteUtilisateur> lignes) {
        return lignes.stream().map(u -> {
            UniteOrganisation unite = unites.findById(u.getUniteId()).orElse(null);
            return new RattachementDto(
                    u.getId(),
                    u.getUniteId(),
                    unite == null ? "" : unite.getCode(),
                    unite == null ? "" : unite.getLibelle(),
                    u.getRole(),
                    u.getStatut(),
                    unite == null ? "AGENT" : audience(unite));
        }).toList();
    }

    private record Resolution(UniteUtilisateur personne, String avertissement) {
    }

    private boolean seanceBloquante(UUID pointVenteId) {
        return SEANCE_BLOQUANTE.stream().anyMatch(statut -> caisses.existsByPointVenteIdAndStatut(pointVenteId, statut));
    }

    private java.util.Map<UUID, List<Caisse>> seancesBloquantes(List<PointVente> comptoirs) {
        if (comptoirs.isEmpty()) {
            return java.util.Map.of();
        }
        return caisses.findByPointVenteIdInAndStatutIn(
                        comptoirs.stream().map(PointVente::getId).toList(), SEANCE_BLOQUANTE).stream()
                .collect(java.util.stream.Collectors.groupingBy(Caisse::getPointVenteId));
    }

    private static String messagePoint(List<Caisse> enCours) {
        if (enCours.stream().anyMatch(c -> "OUVERTE".equals(c.getStatut()))) {
            return "Une séance est déjà ouverte sur ce point.";
        }
        return null;
    }

    private Caisse chargerSeance(UUID caisseId) {
        return caisses.findById(caisseId)
                .orElseThrow(() -> NetworkException.introuvable("CAISSE_INEXISTANTE", "Caisse introuvable."));
    }

    private PositionGuichet positionDe(UUID pointVenteId) {
        return positions.findById(pointVenteId).orElseGet(() -> positions.save(PositionGuichet.builder()
                .pointVenteId(pointVenteId)
                .soldeCompte(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .fondsAttendu(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .devise("XOF")
                .build()));
    }

    private static PositionGuichet positionDeLecture(UUID pointVenteId, java.util.Map<UUID, PositionGuichet> parPoint) {
        PositionGuichet position = parPoint.get(pointVenteId);
        if (position != null) {
            return position;
        }
        return PositionGuichet.builder()
                .pointVenteId(pointVenteId)
                .soldeCompte(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .fondsAttendu(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                .devise("XOF")
                .build();
    }

    private static BigDecimal physique(Caisse caisse) {
        return nz(caisse.getFondsDeclare()).add(nz(caisse.getDepots())).subtract(nz(caisse.getRetraits()));
    }

    private static BigDecimal nz(BigDecimal montant) {
        return montant == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : montant;
    }

    private static BigDecimal argent(BigDecimal montant) {
        if (montant == null || montant.signum() < 0) {
            throw NetworkException.regle("MONTANT_INVALIDE", "Le montant compté est un nombre positif ou nul.");
        }
        return montant.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal argentStrict(BigDecimal montant) {
        BigDecimal valeur = argent(montant);
        if (valeur.signum() <= 0) {
            throw NetworkException.regle("MONTANT_INVALIDE", "Le montant de l'opération est supérieur à zéro.");
        }
        return valeur;
    }

    private static String sensOperation(String brut) {
        String sens = brut == null ? "" : brut.trim().toUpperCase(Locale.ROOT);
        if (!DEPOT.equals(sens) && !RETRAIT.equals(sens)) {
            throw NetworkException.regle("SENS_INVALIDE", "L'opération est un DEPOT ou un RETRAIT.");
        }
        return sens;
    }

    private static String motifCourt(String brut) {
        String motif = brut == null ? "" : brut.trim();
        if (motif.length() < 3) {
            throw NetworkException.regle("MOTIF_REQUIS", "La validation indique un motif.");
        }
        return motif.length() > 200 ? motif.substring(0, 200) : motif;
    }

    private static CaisseDto vueCaisse(Caisse c, PointVente point, UniteUtilisateur personne, PositionGuichet position) {
        boolean ouverte = c.estOuverte() || "EN_VALIDATION".equals(c.getStatut());
        BigDecimal solde = ouverte || c.getSoldeCompteFermeture() == null
                ? (position == null ? nz(c.getSoldeCompteOuverture()) : nz(position.getSoldeCompte()))
                : nz(c.getSoldeCompteFermeture());
        return new CaisseDto(
                c.getId(),
                c.getPointVenteId(),
                point == null ? "" : point.getCode(),
                point == null ? "" : point.getLibelle(),
                c.getUtilisateurId(),
                personne == null ? "" : personne.getIdentifiant(),
                personne == null ? "" : personne.getNom(),
                c.getStatut(),
                c.getNumero() == null ? 0 : c.getNumero(),
                c.getOuverteLe(),
                c.getFermeeLe(),
                nz(c.getFondsAttendu()),
                nz(c.getFondsDeclare()),
                nz(c.getEcartOuverture()),
                nz(c.getSoldeCompteOuverture()),
                nz(c.getDepots()),
                nz(c.getRetraits()),
                physique(c),
                solde,
                c.getFondsPhysiqueCloture(),
                c.getEcartFermeture(),
                c.getSoldeCompteFermeture(),
                c.getMotif());
    }

    private static MouvementDto mouvement(MouvementAlias m) {
        return new MouvementDto(m.getId(), m.getUniteId(), m.getPointVenteId(), m.getUtilisateurId(), m.getSens(),
                m.getAliasSourceId(), m.getAliasCibleId(), m.getEtiquetteSource(), m.getEtiquetteCible(),
                m.getMontant(), m.getDevise());
    }
}
