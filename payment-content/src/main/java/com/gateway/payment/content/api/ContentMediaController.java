package com.gateway.payment.content.api;

import com.gateway.payment.content.service.ContentMediaService;
import com.gateway.payment.content.service.ContentMediaService.UploadedMedia;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1")
public class ContentMediaController {

    private final ContentMediaService mediaService;

    public ContentMediaController(ContentMediaService mediaService) {
        this.mediaService = mediaService;
    }

    /** Lecture publique (balise {@code <img>} mobile / front sans JWT). */
    @GetMapping("/content/media/{fileName}")
    @Operation(summary = "Télécharger un média contenu")
    public ResponseEntity<Resource> get(@PathVariable String fileName) {
        Resource resource = mediaService.load(fileName);
        MediaType type = mediaService.mediaType(fileName);
        return ResponseEntity.ok()
                .contentType(type)
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic())
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @PostMapping(value = "/admin/content/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','CONFIG_MANAGER','OPERATOR')")
    @Operation(summary = "Uploader une image (retourne l'URL à stocker sur le contenu)")
    public UploadedMedia upload(@RequestPart("file") MultipartFile file) {
        return mediaService.store(file);
    }
}
