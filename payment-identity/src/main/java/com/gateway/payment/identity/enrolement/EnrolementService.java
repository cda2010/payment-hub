package com.gateway.payment.identity.enrolement;

import com.gateway.payment.identity.domain.IdentityParty;
import com.gateway.payment.identity.domain.PartyKind;
import com.gateway.payment.identity.repository.IdentityPartyRepository;
import com.gateway.payment.identity.service.IdentityService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Produit d'enrôlement. Un code confirme la ligne, puis la personne entre
 * sur un compte existant ou ouvre un portefeuille Coris Money.
 */
@Service
public class EnrolementService {

    static final String MONEY = "MONEY";
    static final String BANK = "BANK";
    static final String MESO = "MESO";
    static final String TELEPHONE = "TELEPHONE";
    static final String CODE = "CODE";
    static final String ENTRE = "ENTRE";
    static final String PROPOSER = "PROPOSER";
    static final String SUITE = "SUITE";
    static final String PRET = "PRET";
    static final String DOSSIER = "DOSSIER";
    static final String ADRESSE = "ADRESSE";
    static final String PIECE = "PIECE";
    static final String OUVERT = "OUVERT";
    static final String TERMINE = "TERMINE";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ESSAIS = 5;
    private static final Set<String> CHAMPS_ADRESSE = Set.of(
            "countryCode", "regionCode", "provinceCode", "communeCode", "city",
            "arrondissement", "sector", "district", "street", "buildingNumber",
            "building", "floor", "unit", "postalCode", "landmark", "additionalInfo",
            "latitude", "longitude", "ville", "adresse");
    private static final Map<String, String> ADRESSE_REQUISE = Map.of(
            "countryCode", "Pays",
            "regionCode", "Région",
            "provinceCode", "Province",
            "communeCode", "Commune",
            "city", "Ville / localité",
            "arrondissement", "Arrondissement",
            "sector", "Secteur",
            "district", "Quartier",
            "street", "Rue");

    private final EnrolementCompteRepository comptes;
    private final EnrolementRepository dossiers;
    private final EnrolementPassageRepository passages;
    private final IdentityPartyRepository parties;
    private final CoffreDocuments coffre;
    private final CatalogueEnrolement catalogue;
    private final PiecesEnrolement pieces;
    private final EnrollmentProperties properties;
    private final AnnuaireFse annuaire;
    private final InfosOmnicanal infos;
    private final LimiteEnrolement limites;

    public EnrolementService(
            EnrolementCompteRepository comptes,
            EnrolementRepository dossiers,
            EnrolementPassageRepository passages,
            IdentityPartyRepository parties,
            CoffreDocuments coffre,
            CatalogueEnrolement catalogue,
            PiecesEnrolement pieces,
            EnrollmentProperties properties,
            AnnuaireFse annuaire,
            InfosOmnicanal infos,
            LimiteEnrolement limites
    ) {
        this.comptes = comptes;
        this.dossiers = dossiers;
        this.passages = passages;
        this.parties = parties;
        this.coffre = coffre;
        this.catalogue = catalogue;
        this.pieces = pieces;
        this.properties = properties;
        this.annuaire = annuaire;
        this.infos = infos;
        this.limites = limites;
    }

    public List<ParcoursPays> pays() {
        return catalogue.pays();
    }

    public List<AnnuaireFse.Operateur> operateurs(String famille, String pays) {
        return annuaire.operateurs(famille, pays);
    }

    public List<AnnuaireFse.Indicatif> indicatifs() {
        return annuaire.indicatifs();
    }

    @Transactional
    public Vue getInfos(String codeFse, String indicatif, String telephone, String pays) {
        String local = chiffres(telephone, "Le numéro de téléphone est requis");
        String code = indicatifChiffres(indicatif);
        exigerFse(codeFse, "MOBILE_MONEY", pays);
        Optional<InfosOmnicanal.Reconnaissance> distant = infos.getInfos(codeFse.trim(), code, local);
        if (distant.isPresent()) {
            return reconnaitre(distant.get(), MONEY, local);
        }
        List<EnrolementCompte> trouves = parMaison(local, MONEY);
        if (trouves.isEmpty()) {
            trouves = parMaison(code + local, MONEY);
        }
        if (trouves.isEmpty()) {
            throw new NoSuchElementException("Aucun compte Mobile Money pour ce numéro");
        }
        return emettre(trouves.getFirst(), local);
    }

    @Transactional
    public Vue getInfosCompte(String codeFse, String numero) {
        String saisi = chiffres(numero, "Le numéro de compte est requis");
        exigerFse(codeFse, "BANQUE", null);
        Optional<InfosOmnicanal.Reconnaissance> distant = infos.getInfosCompte(codeFse.trim(), saisi);
        if (distant.isPresent()) {
            return reconnaitre(distant.get(), BANK, saisi);
        }
        return chercherCompte(saisi, null);
    }

    public ParcoursPays parcours(String pays) {
        return catalogue.pour(pays);
    }

    @Transactional
    public Vue chercherPortefeuille(String numero) {
        String saisi = chiffres(numero, "Le numéro Coris Money est requis");
        List<EnrolementCompte> trouves = comptes.findByTenantIdAndNumero(IdentityService.DEFAULT_TENANT, saisi).stream()
                .filter(compte -> MONEY.equals(compte.getMaison()))
                .toList();
        if (trouves.isEmpty()) {
            throw new NoSuchElementException("Aucun compte Coris Money pour ce numéro");
        }
        return emettre(trouves.getFirst(), saisi);
    }

    @Transactional
    public Vue chercherCompte(String numero, String maison) {
        String saisi = chiffres(numero, "Le numéro de compte est requis");
        String filtre = maison == null || maison.isBlank() ? null : maison.trim().toUpperCase();
        List<EnrolementCompte> trouves = comptes.findByTenantIdAndNumero(IdentityService.DEFAULT_TENANT, saisi).stream()
                .filter(compte -> BANK.equals(compte.getMaison()) || MESO.equals(compte.getMaison()))
                .filter(compte -> filtre == null || filtre.equals(compte.getMaison()))
                .toList();
        if (trouves.isEmpty()) {
            throw new NoSuchElementException("Aucun compte pour ce numéro");
        }
        if (trouves.size() > 1) {
            return new Vue(null, "CHOIX", null, null, null, null, trouves.stream()
                    .map(compte -> new Vue.Choix(compte.getMaison(), libelle(compte.getMaison())))
                    .toList(), EnrolementWorkflow.NOM);
        }
        return emettre(trouves.getFirst(), saisi);
    }

    @Transactional
    public Vue commencer(String numero) {
        String saisi = chiffres(numero, "Le numéro de téléphone est requis");
        boolean dejaMoney = comptes.findByTenantIdAndNumero(IdentityService.DEFAULT_TENANT, saisi).stream()
                .anyMatch(compte -> MONEY.equals(compte.getMaison()));
        if (dejaMoney) {
            throw new IllegalArgumentException("Ce numéro est déjà un compte Coris Money");
        }
        Enrolement dossier = nouveau(TELEPHONE, null, saisi, saisi);
        return vue(dossiers.save(dossier), codeEnClair(dossier));
    }

    @Transactional
    public Vue confirmer(UUID id, String code) {
        Enrolement dossier = charger(id);
        if (!CODE.equals(dossier.getStatut())) {
            throw new IllegalArgumentException("Ce code n'est plus attendu");
        }
        if (dossier.getCodeExpireAt() == null || Instant.now().isAfter(dossier.getCodeExpireAt())) {
            throw new IllegalArgumentException("Le code a expiré");
        }
        if (dossier.getEssais() >= ESSAIS) {
            throw new IllegalArgumentException("Trop de tentatives");
        }
        if (code == null || !hash(dossier.getId(), code.trim()).equals(dossier.getCodeHash())) {
            dossier.setEssais(dossier.getEssais() + 1);
            dossier.setUpdatedAt(Instant.now());
            throw new IllegalArgumentException("Code incorrect");
        }
        dossier.setCodeHash(null);
        dossier.setCodeExpireAt(null);
        passer(dossier, MONEY.equals(dossier.getOrigine()) ? ENTRE : DOSSIER);
        return vue(dossier, null);
    }

    @Transactional
    public Vue renvoyer(UUID id) {
        Enrolement dossier = charger(id);
        if (!CODE.equals(dossier.getStatut())) {
            throw new IllegalArgumentException("Aucun code n'est en attente");
        }
        poserCode(dossier);
        return vue(dossier, codeEnClair(dossier));
    }

    @Transactional
    public Vue dossier(
            UUID id,
            String pays,
            String nom,
            String prenom,
            LocalDate dateNaissance,
            Map<String, String> champs
    ) {
        Enrolement dossier = charger(id);
        if (!DOSSIER.equals(dossier.getStatut()) && !PROPOSER.equals(dossier.getStatut())
                && !SUITE.equals(dossier.getStatut()) && !ADRESSE.equals(dossier.getStatut())
                && !PIECE.equals(dossier.getStatut())) {
            throw new IllegalArgumentException("Les informations ne sont pas encore demandées");
        }
        ParcoursPays parcours = catalogue.pour(pays);
        Map<String, String> valeurs = new HashMap<>();
        if (nom != null) {
            valeurs.put("nom", nom.trim());
        }
        if (prenom != null) {
            valeurs.put("prenom", prenom.trim());
        }
        if (dateNaissance != null) {
            valeurs.put("dateNaissance", dateNaissance.toString());
        }
        if (champs != null) {
            champs.forEach((cle, valeur) -> {
                if (cle != null && valeur != null && !valeur.isBlank()) {
                    valeurs.put(cle, valeur.trim());
                }
            });
        }
        for (ParcoursPays.Champ champ : parcours.champs()) {
            if (CHAMPS_ADRESSE.contains(champ.code())) {
                continue;
            }
            if (champ.requis() && (valeurs.get(champ.code()) == null || valeurs.get(champ.code()).isBlank())) {
                throw new IllegalArgumentException(champ.libelle() + " est requis");
            }
        }
        dossier.setPays(parcours.code());
        dossier.setNom(valeurs.get("nom"));
        dossier.setPrenom(valeurs.get("prenom"));
        String naissance = valeurs.get("dateNaissance");
        dossier.setDateNaissance(naissance == null ? null : LocalDate.parse(naissance));
        Map<String, String> complements = new LinkedHashMap<>();
        if (dossier.getComplements() != null) {
            dossier.getComplements().forEach((cle, valeur) -> {
                if (CHAMPS_ADRESSE.contains(cle) || "choix".equals(cle) || "typeDocument".equals(cle)) {
                    complements.put(cle, valeur);
                }
            });
        }
        for (ParcoursPays.Champ champ : parcours.champs()) {
            if ("nom".equals(champ.code()) || "prenom".equals(champ.code()) || "dateNaissance".equals(champ.code())
                    || CHAMPS_ADRESSE.contains(champ.code())) {
                continue;
            }
            String valeur = valeurs.get(champ.code());
            if (valeur != null) {
                complements.put(champ.code(), valeur);
            }
        }
        dossier.setComplements(complements.isEmpty() ? null : complements);
        if (DOSSIER.equals(dossier.getStatut())) {
            passer(dossier, PROPOSER);
        } else {
            dossier.setUpdatedAt(Instant.now());
        }
        return vue(dossier, null);
    }

    @Transactional
    public Vue adresse(UUID id, Map<String, String> champs) {
        Enrolement dossier = charger(id);
        if (!ADRESSE.equals(dossier.getStatut()) && !PIECE.equals(dossier.getStatut())) {
            throw new IllegalArgumentException("L'adresse n'est pas encore demandée");
        }
        Map<String, String> saisis = new LinkedHashMap<>();
        if (champs != null) {
            champs.forEach((cle, valeur) -> {
                if (cle != null && valeur != null && !valeur.isBlank() && CHAMPS_ADRESSE.contains(cle)
                        && !"ville".equals(cle) && !"adresse".equals(cle)) {
                    saisis.put(cle, valeur.trim());
                }
            });
        }
        for (Map.Entry<String, String> requis : ADRESSE_REQUISE.entrySet()) {
            if (saisis.get(requis.getKey()) == null) {
                throw new IllegalArgumentException(requis.getValue() + " est requis");
            }
        }
        String pays = saisis.get("countryCode").toUpperCase();
        if (!pays.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("Le pays est requis");
        }
        saisis.put("countryCode", pays);
        controlerCoordonnee(saisis, "latitude", "La latitude");
        controlerCoordonnee(saisis, "longitude", "La longitude");
        Map<String, String> complements = dossier.getComplements() == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(dossier.getComplements());
        CHAMPS_ADRESSE.forEach(complements::remove);
        complements.putAll(saisis);
        dossier.setComplements(complements);
        if (ADRESSE.equals(dossier.getStatut())) {
            passer(dossier, PIECE);
        } else {
            dossier.setUpdatedAt(Instant.now());
        }
        return vue(dossier, null);
    }

    @Transactional
    public Vue proposer(UUID id) {
        Enrolement dossier = charger(id);
        if (SUITE.equals(dossier.getStatut())) {
            return vue(dossier, null);
        }
        if (!PROPOSER.equals(dossier.getStatut())) {
            throw new IllegalArgumentException("La question n'est pas encore posée");
        }
        passer(dossier, SUITE);
        return vue(dossier, null);
    }

    @Transactional
    public Vue suite(UUID id, String choix) {
        Enrolement dossier = charger(id);
        String decision = choix == null ? "" : choix.trim().toUpperCase();
        if (!"CREATION".equals(decision) && !"IDENTIFICATION".equals(decision)) {
            throw new IllegalArgumentException("Le choix doit être la création ou l'identification");
        }
        boolean dejaIdentifie = "IDENTIFICATION".equals(decision)
                && (ADRESSE.equals(dossier.getStatut()) || PIECE.equals(dossier.getStatut()));
        if (!SUITE.equals(dossier.getStatut()) && !dejaIdentifie) {
            throw new IllegalArgumentException("Le choix n'est pas encore demandé");
        }
        Map<String, String> complements = dossier.getComplements() == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(dossier.getComplements());
        complements.put("choix", decision);
        dossier.setComplements(complements);
        if (dejaIdentifie) {
            dossier.setUpdatedAt(Instant.now());
            return vue(dossier, null);
        }
        passer(dossier, "CREATION".equals(decision) ? PRET : ADRESSE);
        return vue(dossier, null);
    }

    public List<ParcoursPays.Document> pieces(UUID id, String typeDocument) {
        Enrolement dossier = charger(id);
        if (!PIECE.equals(dossier.getStatut())) {
            throw new IllegalArgumentException("La pièce n'est pas encore demandée");
        }
        return pieces.demander(dossier.getPays(), typeDeDocument(typeDocument));
    }

    @Transactional
    public Vue piece(UUID id, String face, String type, String contenu, String typeDocument) {
        Enrolement dossier = charger(id);
        if (!PIECE.equals(dossier.getStatut())) {
            throw new IllegalArgumentException("La pièce n'est pas encore demandée");
        }
        String cote = face == null ? "" : face.trim().toUpperCase();
        if (!"RECTO".equals(cote) && !"VERSO".equals(cote) && !"SELFIE".equals(cote)) {
            throw new IllegalArgumentException("La face doit être RECTO, VERSO ou SELFIE");
        }
        String typePiece = typeDeDocument(typeDocument);
        boolean demande = pieces.demander(dossier.getPays(), typePiece).stream()
                .anyMatch(document -> cote.equals(document.face()));
        if (!demande) {
            throw new IllegalArgumentException("Ce document n'est pas demandé");
        }
        Map<String, String> complements = dossier.getComplements() == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(dossier.getComplements());
        String deja = complements.get("typeDocument");
        if (deja != null && !deja.equals(typePiece)) {
            throw new IllegalArgumentException("Le type de document est déjà choisi");
        }
        complements.put("typeDocument", typePiece);
        dossier.setComplements(complements);
        byte[] octets = decoder(contenu);
        String typeFichier = type == null || type.isBlank() ? "image/jpeg" : type.trim();
        UUID documentId = coffre.deposer(dossier.getTenantId(), id, cote, typePiece, typeFichier, octets);
        if ("RECTO".equals(cote)) {
            dossier.setRectoDocumentId(documentId);
        } else if ("VERSO".equals(cote)) {
            dossier.setVersoDocumentId(documentId);
        } else {
            dossier.setSelfieDocumentId(documentId);
        }
        dossier.setUpdatedAt(Instant.now());
        return vue(dossier, null);
    }

    @Transactional
    public Vue ouvrir(UUID id) {
        Enrolement dossier = charger(id);
        boolean immediat = PRET.equals(dossier.getStatut());
        if (!immediat && !PIECE.equals(dossier.getStatut())) {
            throw new IllegalArgumentException("Le dossier n'est pas prêt");
        }
        String typePiece = dossier.getComplements() == null ? null : dossier.getComplements().get("typeDocument");
        if (!immediat) {
        for (ParcoursPays.Document document : pieces.demander(dossier.getPays(), typePiece)) {
            if (!document.requis()) {
                continue;
            }
            boolean present = switch (document.face()) {
                case "RECTO" -> dossier.getRectoDocumentId() != null;
                case "VERSO" -> dossier.getVersoDocumentId() != null;
                case "SELFIE" -> dossier.getSelfieDocumentId() != null;
                default -> false;
            };
            if (!present) {
                throw new IllegalArgumentException(document.libelle() + " est requis");
            }
        }
        }
        UUID partyId = dossier.getPartyId();
        if (partyId == null) {
            IdentityParty party = new IdentityParty();
            party.setTenantId(dossier.getTenantId());
            party.setCode("ENR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            party.setKind(PartyKind.PERSON);
            party.setDisplayName(dossier.getPrenom() + " " + dossier.getNom());
            party.setPhone(dossier.getTelephone());
            party.setKycLevel(immediat ? "LIGHT" : "FULL");
            party.setCustomerStatus("ACTIVE");
            party.setActive(true);
            partyId = parties.save(party).getId();
            dossier.setPartyId(partyId);
        } else {
            parties.findById(partyId).ifPresent(party -> {
                party.setKycLevel(immediat ? "LIGHT" : "FULL");
                if (party.getPhone() == null || party.getPhone().isBlank()) {
                    party.setPhone(dossier.getTelephone());
                }
            });
        }
        String numero = numeroLibre();
        EnrolementCompte compte = new EnrolementCompte();
        compte.setTenantId(dossier.getTenantId());
        compte.setMaison(MONEY);
        compte.setNumero(numero);
        compte.setPartyId(partyId);
        compte.setTelephone(dossier.getTelephone());
        comptes.save(compte);
        dossier.setNumeroOuvert(numero);
        limites.appliquer(partyId, immediat ? "CREATION" : "IDENTIFICATION");
        passer(dossier, OUVERT);
        return vue(dossier, null);
    }

    @Transactional
    public Vue terminer(UUID id) {
        Enrolement dossier = charger(id);
        if (!DOSSIER.equals(dossier.getStatut()) && !PROPOSER.equals(dossier.getStatut())) {
            throw new IllegalArgumentException("Ce parcours ne peut pas s'arrêter ici");
        }
        if (dossier.getPartyId() == null) {
            IdentityParty party = new IdentityParty();
            party.setTenantId(dossier.getTenantId());
            party.setCode("ENR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            party.setKind(PartyKind.PERSON);
            party.setDisplayName(dossier.getPrenom() == null ? dossier.getTelephone() : dossier.getPrenom() + " " + dossier.getNom());
            party.setPhone(dossier.getTelephone());
            party.setKycLevel("BASIC");
            party.setCustomerStatus("ACTIVE");
            party.setActive(true);
            dossier.setPartyId(parties.save(party).getId());
        }
        limites.appliquer(dossier.getPartyId(), "REFUS");
        passer(dossier, TERMINE);
        return vue(dossier, null);
    }

    private static void controlerCoordonnee(Map<String, String> saisis, String cle, String libelle) {
        String valeur = saisis.get(cle);
        if (valeur == null) {
            return;
        }
        try {
            Double.parseDouble(valeur.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(libelle + " doit être un nombre");
        }
        saisis.put(cle, valeur.replace(',', '.'));
    }

    private void exigerFse(String codeFse, String famille, String pays) {
        if (codeFse == null || codeFse.isBlank()) {
            throw new IllegalArgumentException("Le FSE est requis");
        }
        if (!annuaire.connue(codeFse.trim(), famille, pays)) {
            throw new IllegalArgumentException("Ce FSE ne correspond pas à la liste demandée");
        }
    }

    private List<EnrolementCompte> parMaison(String numero, String maison) {
        return comptes.findByTenantIdAndNumero(IdentityService.DEFAULT_TENANT, numero).stream()
                .filter(compte -> maison.equals(compte.getMaison()))
                .toList();
    }

    private Vue reconnaitre(InfosOmnicanal.Reconnaissance reconnaissance, String maison, String saisi) {
        Enrolement dossier = nouveau(maison, null, saisi, saisi);
        dossier.setNom(reconnaissance.nom());
        dossier.setPrenom(reconnaissance.prenom());
        dossiers.save(dossier);
        return vue(dossier, codeEnClair(dossier));
    }

    private Vue emettre(EnrolementCompte compte, String saisi) {
        Enrolement dossier = nouveau(compte.getMaison(), compte.getPartyId(), compte.getTelephone(), saisi);
        return vue(dossiers.save(dossier), codeEnClair(dossier));
    }

    private Enrolement nouveau(String origine, UUID partyId, String telephone, String saisi) {
        Enrolement dossier = new Enrolement();
        dossier.setTenantId(IdentityService.DEFAULT_TENANT);
        dossier.setOrigine(origine);
        dossier.setPartyId(partyId);
        dossier.setTelephone(telephone);
        dossier.setNumeroSaisi(saisi);
        dossier.setWorkflowCode(EnrolementWorkflow.NOM);
        dossier.setStatut(CODE);
        dossier.setEssais(0);
        Instant now = Instant.now();
        dossier.setCreatedAt(now);
        dossier.setUpdatedAt(now);
        dossiers.save(dossier);
        noter(dossier, CODE);
        poserCode(dossier);
        return dossier;
    }

    private void passer(Enrolement dossier, String cible) {
        EnrolementWorkflow.vers(dossier.getStatut(), cible);
        dossier.setStatut(cible);
        dossier.setUpdatedAt(Instant.now());
        noter(dossier, cible);
    }

    private void noter(Enrolement dossier, String etape) {
        EnrolementPassage passage = new EnrolementPassage();
        passage.setEnrolementId(dossier.getId());
        passage.setEtape(etape);
        passage.setCreatedAt(Instant.now());
        passages.save(passage);
    }

    private void poserCode(Enrolement dossier) {
        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        dossier.setCodeHash(hash(dossier.getId(), code));
        dossier.setCodeExpireAt(Instant.now().plus(10, ChronoUnit.MINUTES));
        dossier.setEssais(0);
        dossier.setUpdatedAt(Instant.now());
        dossier.setCodeClair(properties.isExposeCode() ? code : null);
    }

    private String codeEnClair(Enrolement dossier) {
        if (!properties.isExposeCode() || !CODE.equals(dossier.getStatut())) {
            return null;
        }
        return dossier.getCodeClair();
    }

    private Vue vue(Enrolement dossier, String code) {
        if (code != null) {
            return new Vue(dossier.getId(), dossier.getStatut(), dossier.getOrigine(), null, null, code, List.of(),
                    dossier.getWorkflowCode());
        }
        String nom = null;
        if (dossier.getPartyId() != null && dossier.getPrenom() == null) {
            nom = parties.findById(dossier.getPartyId()).map(IdentityParty::getDisplayName).orElse(null);
        }
        String affiche = dossier.getPrenom() == null ? nom : dossier.getPrenom() + " " + dossier.getNom();
        return new Vue(
                dossier.getId(),
                dossier.getStatut(),
                dossier.getOrigine(),
                affiche,
                dossier.getNumeroOuvert(),
                code,
                List.of(),
                dossier.getWorkflowCode());
    }

    private Enrolement charger(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("Le dossier est requis");
        }
        return dossiers.findById(id).orElseThrow(() -> new NoSuchElementException("Dossier introuvable"));
    }

    private String numeroLibre() {
        for (int essai = 0; essai < 8; essai++) {
            String numero = "226" + String.format("%08d", RANDOM.nextInt(100_000_000));
            if (!comptes.existsByTenantIdAndMaisonAndNumero(IdentityService.DEFAULT_TENANT, MONEY, numero)) {
                return numero;
            }
        }
        throw new IllegalStateException("Impossible de réserver un numéro");
    }

    private static String typeDeDocument(String valeur) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException("Le type de document est requis");
        }
        String code = valeur.trim().toUpperCase().replace(' ', '_').replace('-', '_');
        if (!code.matches("[A-Z][A-Z0-9_]{1,31}")) {
            throw new IllegalArgumentException("Le type de document est inconnu");
        }
        return code;
    }

    private static byte[] decoder(String contenu) {
        if (contenu == null || contenu.isBlank()) {
            throw new IllegalArgumentException("Le document est requis");
        }
        String brut = contenu.contains(",") ? contenu.substring(contenu.indexOf(',') + 1) : contenu.trim();
        byte[] octets;
        try {
            octets = Base64.getDecoder().decode(brut);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Le document est illisible");
        }
        if (octets.length == 0 || octets.length > 1_500_000) {
            throw new IllegalArgumentException("Le document doit rester sous 1,5 Mo");
        }
        return octets;
    }

    private static String indicatifChiffres(String valeur) {
        String chiffres = valeur == null ? "" : valeur.replaceAll("\\D", "");
        if (chiffres.length() < 1 || chiffres.length() > 4) {
            throw new IllegalArgumentException("L'indicatif pays est requis");
        }
        return chiffres;
    }

    private static String chiffres(String valeur, String message) {
        if (valeur == null) {
            throw new IllegalArgumentException(message);
        }
        String chiffres = valeur.replaceAll("\\D", "");
        if (chiffres.length() < 6) {
            throw new IllegalArgumentException(message);
        }
        return chiffres;
    }

    private static String requis(String valeur, String message) {
        if (valeur == null || valeur.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return valeur.trim();
    }

    private static String libelle(String maison) {
        return switch (maison) {
            case BANK -> "Coris Bank";
            case MESO -> "Coris Meso Finance";
            default -> "Coris Money";
        };
    }

    private static String hash(UUID id, String code) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] octets = digest.digest((id + ":" + code).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(octets);
        } catch (Exception e) {
            throw new IllegalStateException("Code indisponible");
        }
    }

    public record Vue(
            UUID id,
            String etape,
            String maison,
            String nom,
            String numeroCompte,
            String code,
            List<Choix> choix,
            String workflow
    ) {
        public record Choix(String maison, String libelle) {
        }
    }
}
