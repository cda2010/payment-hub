package com.m2i.pti.web.rest;

import com.m2i.pti.domain.CredentialApi;
import com.m2i.pti.domain.Document;
import com.m2i.pti.external.service.ExternalService;
import com.m2i.pti.service.minio.MinioService;
import io.micrometer.core.annotation.Timed;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/verification")
public class VerificationResource {
    private final ExternalService externalService;
    private final MinioService minioService;

    public VerificationResource(ExternalService externalService, MinioService minioService) {
        this.externalService = externalService;
        this.minioService = minioService;
    }

    @GetMapping("/verification-hash-param")
    public CredentialApi verifieHashParam(@RequestParam String params, @RequestParam String hashParam, @RequestParam String client_id) {
       System.out.println("Verification !!!!!!!!!!!!!!");
        return externalService.verifieHashParam(params, hashParam, client_id);
    }
    @PostMapping("/load-data-from-pentaho")
    @Timed
    public Document saveDocumentOfClient(@RequestParam("cheminFichier") String cheminFichier, @RequestParam("dossier") String dossier, @RequestParam("shortFolderName") String shortFolderName) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        return minioService.saveDocumentClient(cheminFichier,dossier,shortFolderName);
    }
}
