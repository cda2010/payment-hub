package com.m2i.pti.web.rest;

import com.m2i.pti.domain.Document;
import com.m2i.pti.service.minio.MinioService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api")
public class MinioResource {
    private final Logger log = LoggerFactory.getLogger(MinioResource.class);
    private final MinioService minioService;

    public MinioResource(MinioService minioService) {
        this.minioService = minioService;
    }


    @PostMapping("/load-data-from-pentaho")
    @Timed
    public Document saveDocumentOfClient(@RequestParam("cheminFichier") String cheminFichier, @RequestParam("dossier") String dossier, @RequestParam("shortFolderName") String shortFolderName) throws NoSuchAlgorithmException, InvalidKeyException, IOException {
        //return minioService.saveDocumentClient(cheminFichier,dossier,shortFolderName);
        return new Document();
    }
    
    @PostMapping("/migrate-image-frompti")
    @Timed
    public void migrateImageFromPti(@RequestParam("fin") Long fin) throws IOException, NoSuchAlgorithmException, InvalidKeyException {
         minioService.migrateImageFromPti(fin);
    }

}
