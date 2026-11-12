package com.gateway.payment.network.api;

import com.gateway.payment.network.NetworkException;
import com.gateway.payment.network.api.dto.NetworkDtos.RapportImport;
import com.gateway.payment.network.service.ImportService;
import com.gateway.payment.network.service.ModeleImport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/network/imports")
@Tag(name = "Import en masse", description = "Unités et membres de regroupements depuis un fichier CSV ou Excel")
@PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER')")
public class ImportController {

    private final ImportService imports;

    public ImportController(ImportService imports) {
        this.imports = imports;
    }

    @PostMapping(value = "/unites", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importer des unités (code ; libelle ; type ; code_parent ; zone ; code_fse). "
            + "code_fse obligatoire si type=FSE. Par défaut essai à blanc : "
            + "rien n'est enregistré ; essai=false applique le fichier s'il ne contient aucune erreur")
    public RapportImport unites(@RequestParam("fichier") MultipartFile fichier,
                                @RequestParam(defaultValue = "true") boolean essai, Authentication auth) {
        return imports.importerUnites(fichier.getOriginalFilename(), contenu(fichier), essai, auth.getName());
    }

    @PostMapping(value = "/membres", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Importer des membres de regroupements (code_regroupement ; code_membre). Par défaut essai à "
            + "blanc ; essai=false applique le fichier s'il ne contient aucune erreur")
    public RapportImport membres(@RequestParam("fichier") MultipartFile fichier,
                                 @RequestParam(defaultValue = "true") boolean essai, Authentication auth) {
        return imports.importerMembres(fichier.getOriginalFilename(), contenu(fichier), essai, auth.getName());
    }

    @GetMapping("/modeles/{objet}")
    @Operation(summary = "Fichier modèle : objet unites ou membres, format csv (défaut) ou xlsx")
    public ResponseEntity<byte[]> modele(@PathVariable String objet, @RequestParam(defaultValue = "csv") String format) {
        ModeleImport m = imports.modele(objet, format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(m.nomFichier()).build().toString())
                .contentType(MediaType.parseMediaType(m.typeContenu()))
                .body(m.contenu());
    }

    private static byte[] contenu(MultipartFile fichier) {
        try {
            return fichier.getBytes();
        } catch (IOException e) {
            throw NetworkException.regle("FICHIER_ILLISIBLE", "Lecture du fichier impossible.");
        }
    }
}
