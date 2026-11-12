package com.gateway.payment.pricing.tarif;

import com.gateway.payment.pricing.tarif.domain.CibleReseau;
import com.gateway.payment.pricing.tarif.reseau.PositionReseau;
import com.gateway.payment.pricing.tarif.reseau.ReseauClient;
import com.gateway.payment.pricing.tarif.reseau.ReseauException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Réseau de démonstration de payment-network, en mémoire :
 * <pre>
 * UEMOA (XOF) : BF ── FSE-UEMOA ─┬─ AG-OUAHIGOUYA ── MS-SAHEL ─┬─ SD-SAHEL-01
 *                                │                            └─ RG-EQUIPE-SAHEL : SD-SAHEL-01
 *                                ├─ AG-KAYA
 *                                └─ RG-AGENCES-NORD : AG-OUAHIGOUYA, AG-KAYA
 *               ML
 * CEMAC (XAF) : CM
 * </pre>
 */
public class ReseauDeTest implements ReseauClient {

    public static final Map<String, String> DEVISES = Map.of("UEMOA", "XOF", "CEMAC", "XAF");

    private final Map<String, Noeud> parCode = new LinkedHashMap<>();
    private final Map<String, List<String>> membres = new LinkedHashMap<>();
    public final AtomicInteger appels = new AtomicInteger();
    public boolean indisponible;

    private record Noeud(UUID id, String code, String type, String zone, String parent, String statut) {
    }

    public ReseauDeTest() {
        ajouter("BF", "PAYS", "UEMOA", null);
        ajouter("ML", "PAYS", "UEMOA", null);
        ajouter("CM", "PAYS", "CEMAC", null);
        ajouter("FSE-UEMOA", "FSE", "UEMOA", "BF");
        ajouter("AG-OUAHIGOUYA", "AGENCE", "UEMOA", "FSE-UEMOA");
        ajouter("AG-KAYA", "AGENCE", "UEMOA", "FSE-UEMOA");
        ajouter("MS-SAHEL", "MASTER", "UEMOA", "AG-OUAHIGOUYA");
        ajouter("SD-SAHEL-01", "SOUS_DISTRIBUTEUR", "UEMOA", "MS-SAHEL");
        ajouter("RG-AGENCES-NORD", "REGROUPEMENT", "UEMOA", "FSE-UEMOA");
        ajouter("RG-EQUIPE-SAHEL", "REGROUPEMENT", "UEMOA", "MS-SAHEL");
        membres.put("RG-AGENCES-NORD", List.of("AG-OUAHIGOUYA", "AG-KAYA"));
        membres.put("RG-EQUIPE-SAHEL", List.of("SD-SAHEL-01"));
    }

    public UUID id(String code) {
        return parCode.get(code).id();
    }

    public void desactiver(String code) {
        Noeud n = parCode.get(code);
        parCode.put(code, new Noeud(n.id(), n.code(), n.type(), n.zone(), n.parent(), "INACTIF"));
    }

    /** Cible d'une grille sur cette unité, comme l'enregistre le service. */
    public CibleReseau cible(String code) {
        Noeud n = parCode.get(code);
        UUID parent = n.parent() == null ? null : parCode.get(n.parent()).id();
        return new CibleReseau(n.zone(), n.id(), n.code(), n.code(), n.type(), parent);
    }

    @Override
    public PositionReseau unite(UUID id) {
        appels.incrementAndGet();
        verifierDisponible();
        return parCode.values().stream().filter(n -> n.id().equals(id)).findFirst()
                .map(this::position)
                .orElseThrow(() -> new ReseauException(true, "Unité " + id + " introuvable."));
    }

    @Override
    public PositionReseau pays(String code) {
        appels.incrementAndGet();
        verifierDisponible();
        Noeud n = parCode.get(code);
        if (n == null || !"PAYS".equals(n.type())) {
            throw new ReseauException(true, "Pays " + code + " introuvable.");
        }
        return position(n);
    }

    @Override
    public PositionReseau zone(String code) {
        appels.incrementAndGet();
        verifierDisponible();
        if (!DEVISES.containsKey(code)) {
            throw new ReseauException(true, "Zone " + code + " introuvable.");
        }
        return new PositionReseau(code, DEVISES.get(code), null, List.of(), List.of());
    }

    private void verifierDisponible() {
        if (indisponible) {
            throw new ReseauException(false, "Le réseau de distribution est injoignable.");
        }
    }

    private void ajouter(String code, String type, String zone, String parent) {
        parCode.put(code, new Noeud(UUID.randomUUID(), code, type, zone, parent, "ACTIF"));
    }

    private PositionReseau position(Noeud n) {
        List<PositionReseau.Unite> lignee = new ArrayList<>();
        for (Noeud c = n; c != null; c = c.parent() == null ? null : parCode.get(c.parent())) {
            lignee.add(unite(c));
        }
        List<PositionReseau.Couverture> couvertures = new ArrayList<>();
        membres.forEach((rg, ms) -> lignee.stream()
                .filter(u -> ms.contains(u.code()))
                .forEach(u -> couvertures.add(new PositionReseau.Couverture(unite(parCode.get(rg)), u,
                        u.code().equals(n.code())))));
        return new PositionReseau(n.zone(), DEVISES.get(n.zone()), unite(n), lignee, couvertures);
    }

    private PositionReseau.Unite unite(Noeud n) {
        int niveau = 0;
        for (Noeud c = n; c != null; c = c.parent() == null ? null : parCode.get(c.parent())) {
            niveau++;
        }
        return new PositionReseau.Unite(n.id(), n.code(), n.code(), n.type(), n.zone(), n.statut(), niveau);
    }
}
