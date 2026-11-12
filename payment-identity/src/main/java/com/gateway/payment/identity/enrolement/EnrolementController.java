package com.gateway.payment.identity.enrolement;

import com.gateway.payment.identity.enrolement.EnrolementService.Vue;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/identity/enrolement")
public class EnrolementController {

    private final EnrolementService enrolement;
    private final FormulaireCatalogue formulaires;

    public EnrolementController(EnrolementService enrolement, FormulaireCatalogue formulaires) {
        this.enrolement = enrolement;
        this.formulaires = formulaires;
    }

    @GetMapping("/workflow")
    public WorkflowEnrolement workflow() {
        return wrap(formulaires::publie);
    }

    @GetMapping("/produit")
    public List<ParcoursPays> produit() {
        return wrap(enrolement::pays);
    }

    @GetMapping("/produit/{pays}")
    public ParcoursPays produitPays(@PathVariable String pays) {
        return wrap(() -> enrolement.parcours(pays));
    }

    @GetMapping("/operateurs")
    public List<AnnuaireFse.Operateur> operateurs(
            @RequestParam String famille,
            @RequestParam(required = false) String pays
    ) {
        return wrap(() -> enrolement.operateurs(famille, pays));
    }

    @GetMapping("/indicatifs")
    public List<AnnuaireFse.Indicatif> indicatifs() {
        return wrap(enrolement::indicatifs);
    }

    @PostMapping("/infos")
    public Vue infos(@RequestBody InfosRequest body) {
        return wrap(() -> enrolement.getInfos(body.codeFse(), body.indicatif(), body.telephone(), body.pays()));
    }

    @PostMapping("/portefeuille")
    public Vue portefeuille(@RequestBody NumeroRequest body) {
        return wrap(() -> enrolement.chercherPortefeuille(body.numero()));
    }

    @PostMapping("/compte")
    public Vue compte(@RequestBody CompteRequest body) {
        if (body.codeFse() != null && !body.codeFse().isBlank()) {
            return wrap(() -> enrolement.getInfosCompte(body.codeFse(), body.numero()));
        }
        return wrap(() -> enrolement.chercherCompte(body.numero(), body.maison()));
    }

    @PostMapping("/telephone")
    public Vue telephone(@RequestBody NumeroRequest body) {
        return wrap(() -> enrolement.commencer(body.numero()));
    }

    @PostMapping("/{id}/code")
    public Vue code(@PathVariable UUID id, @RequestBody CodeRequest body) {
        return wrap(() -> enrolement.confirmer(id, body.code()));
    }

    @PostMapping("/{id}/renvoyer")
    public Vue renvoyer(@PathVariable UUID id) {
        return wrap(() -> enrolement.renvoyer(id));
    }

    @PostMapping("/{id}/dossier")
    public Vue dossier(@PathVariable UUID id, @RequestBody DossierRequest body) {
        return wrap(() -> enrolement.dossier(
                id, body.pays(), body.nom(), body.prenom(), body.dateNaissance(), body.champs()));
    }

    @GetMapping("/{id}/pieces")
    public List<ParcoursPays.Document> pieces(@PathVariable UUID id, @RequestParam String typeDocument) {
        return wrap(() -> enrolement.pieces(id, typeDocument));
    }

    @PostMapping("/{id}/adresse")
    public Vue adresse(@PathVariable UUID id, @RequestBody Map<String, String> champs) {
        return wrap(() -> enrolement.adresse(id, champs));
    }

    @PostMapping("/{id}/proposer")
    public Vue proposer(@PathVariable UUID id) {
        return wrap(() -> enrolement.proposer(id));
    }

    @PostMapping("/{id}/suite")
    public Vue suite(@PathVariable UUID id, @RequestBody SuiteRequest body) {
        return wrap(() -> enrolement.suite(id, body.choix()));
    }

    @PostMapping("/{id}/piece")
    public Vue piece(@PathVariable UUID id, @RequestBody PieceRequest body) {
        return wrap(() -> enrolement.piece(id, body.face(), body.type(), body.contenu(), body.typeDocument()));
    }

    @PostMapping("/{id}/ouvrir")
    public Vue ouvrir(@PathVariable UUID id) {
        return wrap(() -> enrolement.ouvrir(id));
    }

    @PostMapping("/{id}/terminer")
    @ResponseStatus(HttpStatus.OK)
    public Vue terminer(@PathVariable UUID id) {
        return wrap(() -> enrolement.terminer(id));
    }

    private <T> T wrap(java.util.concurrent.Callable<T> call) {
        try {
            return call.call();
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public record NumeroRequest(String numero) {
    }

    public record CompteRequest(String numero, String maison, String codeFse) {
    }

    public record InfosRequest(String codeFse, String indicatif, String telephone, String pays) {
    }

    public record CodeRequest(String code) {
    }

    public record DossierRequest(
            String pays,
            String nom,
            String prenom,
            LocalDate dateNaissance,
            Map<String, String> champs
    ) {
    }

    public record SuiteRequest(String choix) {
    }

    public record PieceRequest(String face, String type, String contenu, String typeDocument) {
    }
}
