package com.gateway.payment.content.service;

import com.gateway.payment.content.config.ContentMediaProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ContentMediaService {

    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final Map<String, String> EXT = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif"
    );

    private final ContentMediaProperties properties;
    private final Path root;

    public ContentMediaService(ContentMediaProperties properties) throws IOException {
        this.properties = properties;
        this.root = Path.of(properties.storageDir()).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public record UploadedMedia(String fileName, String url, String contentType, long size) {
    }

    public UploadedMedia store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier manquant");
        }
        if (file.getSize() > properties.maxSizeBytes()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fichier trop volumineux (max " + properties.maxSizeBytes() + " octets)");
        }
        String contentType = file.getContentType() != null
                ? file.getContentType().toLowerCase(Locale.ROOT)
                : "";
        if (!ALLOWED.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Type non supporté (jpeg, png, webp, gif)");
        }
        String ext = EXT.get(contentType);
        String fileName = UUID.randomUUID() + "." + ext;
        Path target = root.resolve(fileName).normalize();
        if (!target.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide");
        }
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Échec enregistrement média");
        }
        String base = properties.publicBaseUrl().replaceAll("/+$", "");
        return new UploadedMedia(fileName, base + "/" + fileName, contentType, file.getSize());
    }

    public Resource load(String fileName) {
        Path path = resolveSafe(fileName);
        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Média introuvable");
        }
        return new FileSystemResource(path);
    }

    public MediaType mediaType(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }

    private Path resolveSafe(String fileName) {
        if (fileName == null || fileName.isBlank() || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide");
        }
        Path path = root.resolve(fileName).normalize();
        if (!path.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nom de fichier invalide");
        }
        return path;
    }
}
