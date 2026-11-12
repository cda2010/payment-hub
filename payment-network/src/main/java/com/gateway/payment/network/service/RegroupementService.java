package com.gateway.payment.network.service;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.api.dto.NetworkDtos.CouvertureDto;
import com.gateway.payment.network.api.dto.NetworkDtos.MembreDto;
import com.gateway.payment.network.api.dto.NetworkDtos.PageDto;
import com.gateway.payment.network.api.dto.NetworkDtos.UniteResume;
import com.gateway.payment.network.domain.RegroupementMembre;
import com.gateway.payment.network.domain.TypeUnite;
import com.gateway.payment.network.domain.UniteOrganisation;
import com.gateway.payment.network.repository.RegroupementMembreRepository;
import com.gateway.payment.network.repository.UniteOrganisationRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Membres des regroupements. Un regroupement est rattaché à une unité de l'arbre ; il regroupe des unités de
 * n'importe quel niveau prises dans la branche de ce parent (le parent compris, niveaux mélangés si besoin) et couvre
 * chaque membre avec toute sa descendance. Une unité peut appartenir à plusieurs regroupements.
 */
@Service
@Transactional
public class RegroupementService {

    private static final Comparator<UniteOrganisation> ORDRE =
            Comparator.comparingInt(UniteOrganisation::getNiveau).thenComparing(UniteOrganisation::getCode);

    private final UniteOrganisationRepository unites;
    private final RegroupementMembreRepository membres;

    public RegroupementService(UniteOrganisationRepository unites, RegroupementMembreRepository membres) {
        this.unites = unites;
        this.membres = membres;
    }

    @Transactional(readOnly = true)
    public List<MembreDto> membres(UUID regroupementId) {
        chargerRegroupement(regroupementId);
        List<RegroupementMembre> liste = membres.findByRegroupementId(regroupementId);
        Map<UUID, UniteOrganisation> parId = parId(liste.stream().map(RegroupementMembre::getUniteId).toList());
        return liste.stream()
                .filter(m -> parId.containsKey(m.getUniteId()))
                .sorted(Comparator.comparing(m -> parId.get(m.getUniteId()), ORDRE))
                .map(m -> new MembreDto(UniteResume.from(parId.get(m.getUniteId())), m.getAjouteLe(), m.getAjoutePar()))
                .toList();
    }

    public List<MembreDto> ajouterMembre(UUID regroupementId, UUID uniteId, String acteur) {
        UniteOrganisation regroupement = chargerRegroupement(regroupementId);
        UniteOrganisation unite = unites.findById(uniteId).orElseThrow(() ->
                NetworkException.introuvable("UNITE_INEXISTANTE", "Unité " + uniteId + " introuvable."));
        if (unite.getType().estRegroupement()) {
            throw NetworkException.regle("MEMBRE_REGROUPEMENT_INTERDIT",
                    "Un regroupement ne peut pas être membre d'un autre regroupement.");
        }
        if (!unite.getZone().equals(regroupement.getZone())) {
            throw NetworkException.regle("ZONE_INCOHERENTE", unite.getCode() + " est en zone " + unite.getZone()
                    + ", le regroupement " + regroupement.getCode() + " en zone " + regroupement.getZone() + ".");
        }
        if (!unite.estActive()) {
            throw NetworkException.regle("UNITE_INACTIVE", unite.libelleCourt() + " est inactif : réactivez-le d'abord.");
        }
        if (!unite.getChemin().startsWith(regroupement.cheminParent())) {
            UniteOrganisation parent = charger(regroupement.getParentId());
            throw NetworkException.regle("MEMBRE_HORS_BRANCHE", "Le regroupement " + regroupement.getCode()
                    + " est rattaché à " + parent.getCode() + " : " + unite.getCode() + " n'est pas dans sa branche.");
        }
        if (membres.findByRegroupementIdAndUniteId(regroupementId, uniteId).isPresent()) {
            throw NetworkException.conflit("DEJA_MEMBRE",
                    unite.getCode() + " est déjà membre du regroupement " + regroupement.getCode() + ".");
        }
        List<UniteOrganisation> actuels = new ArrayList<>(parId(membres.findByRegroupementId(regroupementId).stream()
                .map(RegroupementMembre::getUniteId).toList()).values());
        for (UniteOrganisation m : actuels) {
            if (m.estAscendantDe(unite)) {
                throw NetworkException.regle("DOUBLON_COUVERTURE", unite.getCode() + " est déjà couvert par le "
                        + "regroupement " + regroupement.getCode() + " via " + m.getCode() + ", qui en est membre.");
            }
            if (unite.estAscendantDe(m)) {
                throw NetworkException.regle("DOUBLON_COUVERTURE", unite.getCode() + " couvrirait " + m.getCode()
                        + ", déjà membre du regroupement " + regroupement.getCode() + " : retirez-le d'abord.");
            }
        }
        membres.save(RegroupementMembre.builder()
                .regroupementId(regroupementId)
                .uniteId(uniteId)
                .ajoutePar(acteur)
                .build());
        membres.flush();
        return membres(regroupementId);
    }

    public List<MembreDto> retirerMembre(UUID regroupementId, UUID uniteId) {
        UniteOrganisation regroupement = chargerRegroupement(regroupementId);
        RegroupementMembre membre = membres.findByRegroupementIdAndUniteId(regroupementId, uniteId).orElseThrow(() ->
                NetworkException.introuvable("MEMBRE_INEXISTANT",
                        "Cette unité n'est pas membre du regroupement " + regroupement.getCode() + "."));
        membres.delete(membre);
        membres.flush();
        return membres(regroupementId);
    }

    /** Toutes les unités couvertes : les membres et toute leur descendance, hors regroupements. */
    @Transactional(readOnly = true)
    public PageDto<UniteResume> perimetre(UUID regroupementId, Pageable pageable) {
        chargerRegroupement(regroupementId);
        List<String> chemins = parId(membres.findByRegroupementId(regroupementId).stream()
                .map(RegroupementMembre::getUniteId).toList()).values().stream()
                .map(UniteOrganisation::getChemin)
                .toList();
        if (chemins.isEmpty()) {
            return new PageDto<>(List.of(), 0, pageable.getPageNumber(), pageable.getPageSize());
        }
        Specification<UniteOrganisation> spec = (root, q, cb) -> cb.and(
                cb.notEqual(root.get("type"), TypeUnite.REGROUPEMENT),
                cb.or(chemins.stream().map(c -> cb.like(root.<String>get("chemin"), c + "%")).toArray(Predicate[]::new)));
        return PageDto.of(unites.findAll(spec, pageable).map(UniteResume::from));
    }

    /** Regroupements qui couvrent l'unité : elle-même membre, ou l'un de ses ascendants. */
    @Transactional(readOnly = true)
    public List<CouvertureDto> couvertures(UUID uniteId) {
        UniteOrganisation unite = charger(uniteId);
        if (unite.getType().estRegroupement()) {
            return List.of();
        }
        List<UUID> lignee = new ArrayList<>(unite.idsAscendants());
        lignee.add(unite.getId());
        List<RegroupementMembre> appartenances = membres.findByUniteIdIn(lignee);
        Map<UUID, UniteOrganisation> parId = parId(appartenances.stream()
                .flatMap(m -> Stream.of(m.getRegroupementId(), m.getUniteId()))
                .distinct()
                .toList());
        return appartenances.stream()
                .filter(m -> parId.containsKey(m.getRegroupementId()) && parId.containsKey(m.getUniteId()))
                .map(m -> new CouvertureDto(UniteResume.from(parId.get(m.getRegroupementId())),
                        UniteResume.from(parId.get(m.getUniteId())), m.getUniteId().equals(uniteId)))
                .sorted(Comparator.comparing(c -> c.regroupement().code()))
                .toList();
    }

    private UniteOrganisation charger(UUID id) {
        return unites.findById(id).orElseThrow(() ->
                NetworkException.introuvable("UNITE_INEXISTANTE", "Unité " + id + " introuvable."));
    }

    private UniteOrganisation chargerRegroupement(UUID id) {
        UniteOrganisation u = charger(id);
        if (!u.getType().estRegroupement()) {
            throw NetworkException.regle("PAS_UN_REGROUPEMENT", u.libelleCourt() + " n'est pas un regroupement.");
        }
        return u;
    }

    private Map<UUID, UniteOrganisation> parId(List<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return unites.findAllById(ids).stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UniteOrganisation::getId, Function.identity()));
    }
}
