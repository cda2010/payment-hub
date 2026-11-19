package com.gateway.payment.network.api;

import com.gateway.payment.network.api.dto.GuichetDtos.AffectationDto;
import com.gateway.payment.network.api.dto.GuichetDtos.AffectationRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.CaisseDto;
import com.gateway.payment.network.api.dto.GuichetDtos.CreationPointVenteRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.CreationUtilisateurRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.DesaffectationRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.DestinationAccesRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.GuichetMoiDto;
import com.gateway.payment.network.api.dto.GuichetDtos.FermetureCaisseRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.MotifCaisseRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.MouvementDto;
import com.gateway.payment.network.api.dto.GuichetDtos.MouvementRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.OperationCaisseRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.OuvertureCaisseRequest;
import com.gateway.payment.network.api.dto.GuichetDtos.SituationCaisseDto;
import com.gateway.payment.network.api.dto.GuichetDtos.PointVenteDto;
import com.gateway.payment.network.api.dto.GuichetDtos.UtilisateurDto;
import com.gateway.payment.network.service.GuichetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/network")
@Tag(name = "Guichet", description = "Utilisateurs, points de vente, caisse et transfert d'alias")
@PreAuthorize("isAuthenticated()")
public class GuichetController {

    private static final String ECRITURE = "hasAnyRole('ADMIN','CONFIG_MANAGER')";
    private static final String AFFECTATION = "hasAnyRole('ADMIN','CONFIG_MANAGER','OWNER')";
    private static final String CAISSE = "hasAnyRole('ADMIN','CONFIG_MANAGER','OWNER','CASHIER')";
    private static final String SUPERVISEUR = "hasAnyRole('ADMIN','CONFIG_MANAGER','OWNER')";

    private final GuichetService guichet;

    public GuichetController(GuichetService guichet) {
        this.guichet = guichet;
    }

    @GetMapping("/unites/{id}/utilisateurs")
    @Operation(summary = "Utilisateurs de l'unité : un admin, des employés")
    public List<UtilisateurDto> utilisateurs(@PathVariable UUID id) {
        return guichet.utilisateurs(id);
    }

    @PostMapping("/unites/{id}/utilisateurs")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Créer l'administrateur ou un employé. Le login est dédié, le mot de passe temporaire est envoyé")
    public UtilisateurDto creerUtilisateur(@PathVariable UUID id, @Valid @RequestBody CreationUtilisateurRequest body,
                                           Authentication auth) {
        return guichet.creerUtilisateur(id, body, auth.getName());
    }

    @PostMapping("/unites/{uniteId}/utilisateurs/{utilisateurId}/activer")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Réactiver la personne et son compte de connexion")
    public UtilisateurDto activer(@PathVariable UUID uniteId, @PathVariable UUID utilisateurId) {
        return guichet.activer(uniteId, utilisateurId);
    }

    @PostMapping("/unites/{uniteId}/utilisateurs/{utilisateurId}/desactiver")
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Désactiver la personne : elle ne peut plus se connecter")
    public UtilisateurDto desactiver(@PathVariable UUID uniteId, @PathVariable UUID utilisateurId) {
        return guichet.desactiver(uniteId, utilisateurId);
    }

    @PostMapping("/unites/{uniteId}/utilisateurs/{utilisateurId}/mot-de-passe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Envoyer un nouveau mot de passe temporaire, à changer à la première connexion")
    public void motDePasse(@PathVariable UUID uniteId, @PathVariable UUID utilisateurId,
                           @RequestBody(required = false) DestinationAccesRequest body) {
        guichet.reinitialiserMotDePasse(uniteId, utilisateurId, body);
    }

    @GetMapping("/guichet/moi")
    @Operation(summary = "Unité, points de vente et affectations de la personne connectée")
    public GuichetMoiDto moi(Authentication auth) {
        return guichet.moi(auth.getName());
    }

    @GetMapping("/guichet/caisse")
    @Operation(summary = "Séance de caisse, fonds physique et solde du compte d'opération")
    public SituationCaisseDto caisse(Authentication auth) {
        return guichet.situation(auth.getName());
    }

    @GetMapping("/guichet/caisses")
    @Operation(summary = "Séances ouvertes ou archives. Chacun voit les siennes ; l'admin précise le login du caissier")
    public List<CaisseDto> seances(Authentication auth,
                                   @RequestParam(required = false) String login,
                                   @RequestParam(defaultValue = "ouvertes") String etat) {
        return guichet.seances(auth.getName(), login, etat);
    }

    @GetMapping("/unites/{id}/affectations")
    @Operation(summary = "Affectations des caissiers sur les points de vente de l'unité")
    public List<AffectationDto> affectationsUnite(@PathVariable UUID id) {
        return guichet.affectationsDeLunite(id);
    }

    @GetMapping("/points-de-vente/{id}/affectations")
    @Operation(summary = "Affectations d'un point de vente")
    public List<AffectationDto> affectations(@PathVariable UUID id) {
        return guichet.affectationsDuPoint(id);
    }

    @PostMapping("/points-de-vente/{id}/affectations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(AFFECTATION)
    @Operation(summary = "Affecter un caissier. La date de fin retire l'affectation à l'échéance")
    public AffectationDto affecter(@PathVariable UUID id, @Valid @RequestBody AffectationRequest body,
                                   Authentication auth) {
        return guichet.affecter(id, body, auth);
    }

    @PostMapping("/affectations/{id}/desaffecter")
    @PreAuthorize(AFFECTATION)
    @Operation(summary = "Désaffecter le caissier. Le motif est obligatoire")
    public AffectationDto desaffecter(@PathVariable UUID id, @Valid @RequestBody DesaffectationRequest body,
                                      Authentication auth) {
        return guichet.desaffecter(id, body, auth);
    }

    @GetMapping("/unites/{id}/points-de-vente")
    @Operation(summary = "Points de vente de l'unité")
    public List<PointVenteDto> points(@PathVariable UUID id) {
        return guichet.pointsDeVente(id);
    }

    @PostMapping("/unites/{id}/points-de-vente")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Ouvrir un point de vente. Son compte est un alias étiqueté, créé dans le service alias")
    public PointVenteDto creerPoint(@PathVariable UUID id, @Valid @RequestBody CreationPointVenteRequest body,
                                    Authentication auth) {
        return guichet.creerPointDeVente(id, body, auth.getName());
    }

    @PostMapping("/points-de-vente/{id}/caisses")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(CAISSE)
    @Operation(summary = "Ouvrir la caisse. Le caissier déclare les espèces ; un écart est enregistré")
    public CaisseDto ouvrir(@PathVariable UUID id, @Valid @RequestBody OuvertureCaisseRequest body,
                            Authentication auth, HttpServletRequest request) {
        return guichet.ouvrir(id, body.fondsDeclare(), auth.getName(), request.getHeader("Authorization"));
    }

    @PostMapping("/caisses/{id}/fermer")
    @PreAuthorize(CAISSE)
    @Operation(summary = "Fermer la caisse. Le comptage des espèces est comparé au fonds attendu")
    public CaisseDto fermer(@PathVariable UUID id, @Valid @RequestBody FermetureCaisseRequest body,
                            Authentication auth) {
        return guichet.fermer(id, body.fondsCompte(), auth.getName());
    }

    @PostMapping("/caisses/{id}/operations")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(CAISSE)
    @Operation(summary = "Rattacher un dépôt ou un retrait à la séance ouverte")
    public CaisseDto operation(@PathVariable UUID id, @Valid @RequestBody OperationCaisseRequest body,
                               Authentication auth) {
        return guichet.enregistrerOperation(id, body.sens(), body.montant(), body.reference(), auth.getName());
    }

    @PostMapping("/caisses/{id}/regulariser")
    @PreAuthorize(SUPERVISEUR)
    @Operation(summary = "Le superviseur valide un écart d'ouverture ou de fermeture")
    public CaisseDto regulariser(@PathVariable UUID id, @Valid @RequestBody MotifCaisseRequest body,
                                 Authentication auth) {
        return guichet.regulariser(id, body.motif(), auth);
    }

    @PostMapping("/caisses/{id}/refuser")
    @PreAuthorize(SUPERVISEUR)
    @Operation(summary = "Le superviseur refuse l'écart : l'ouverture est annulée, ou la caisse reste ouverte pour un nouveau comptage")
    public CaisseDto refuser(@PathVariable UUID id, @Valid @RequestBody MotifCaisseRequest body,
                             Authentication auth) {
        return guichet.refuser(id, body.motif(), auth);
    }

    @PostMapping("/points-de-vente/{id}/mouvements")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ECRITURE)
    @Operation(summary = "Approvisionner l'alias étiqueté CPTEOPERATION, ou ramener le montant vers un alias de l'unité")
    public MouvementDto transferer(@PathVariable UUID id, @Valid @RequestBody MouvementRequest body,
                                   Authentication auth, HttpServletRequest request) {
        return guichet.transferer(id, body, request.getHeader("Authorization"), auth.getName());
    }
}
