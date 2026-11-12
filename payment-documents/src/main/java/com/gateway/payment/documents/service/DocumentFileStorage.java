package com.gateway.payment.documents.service;

import com.gateway.payment.documents.config.DocumentStorageProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Component
public class DocumentFileStorage {

    private static final Set<String> ALLOWED = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private final DocumentStorageProperties properties;
    private final Path root;

    public DocumentFileStorage(DocumentStorageProperties properties) throws IOException {
        this.properties = properties;
        this.root = Path.of(properties.dir()).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    public record StoredFile(String storageKey, String contentType, long sizeBytes, String checksumSha256) {
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fichier manquant");
        }
        if (file.getSize() > properties.maxSizeBytes()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Fichier trop volumineux (max " + properties.maxSizeBytes() + " octets)");
        }
        String contentType = normalizeContentType(file.getContentType());
        if (!ALLOWED.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Type non supporté (pdf, jpeg, png, webp, gif)");
        }
        String ext = extensionFor(contentType, file.getOriginalFilename());
        String storageKey = UUID.randomUUID() + "." + ext;
        Path target = root.resolve(storageKey).normalize();
        if (!target.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé de stockage invalide");
        }
        try {
            String checksum = writeWithChecksum(file.getInputStream(), target);
            return new StoredFile(storageKey, contentType, Files.size(target), checksum);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Échec enregistrement document");
        }
    }

    public Resource load(String storageKey) {
        Path path = resolveSafe(storageKey);
        if (!Files.isRegularFile(path)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Fichier introuvable");
        }
        return new FileSystemResource(path);
    }

    public void deleteQuietly(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }
        try {
            Files.deleteIfExists(resolveSafe(storageKey));
        } catch (Exception ignored) {
            // soft-delete DB reste la source de vérité
        }
    }

    public String downloadUrl(UUID documentId) {
        String base = properties.publicBaseUrl().replaceAll("/+$", "");
        return base + "/" + documentId + "/file";
    }

    private Path resolveSafe(String storageKey) {
        if (storageKey == null || storageKey.isBlank()
                || storageKey.contains("..") || storageKey.contains("/") || storageKey.contains("\\")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé de stockage invalide");
        }
        Path path = root.resolve(storageKey).normalize();
        if (!path.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Clé de stockage invalide");
        }
        return path;
    }

    private static String writeWithChecksum(InputStream in, Path target) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (DigestInputStream din = new DigestInputStream(in, digest)) {
                Files.copy(din, target);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 indisponible", e);
        }
    }

    private static String normalizeContentType(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String lower = raw.toLowerCase(Locale.ROOT).trim();
        int sep = lower.indexOf(';');
        return sep > 0 ? lower.substring(0, sep).trim() : lower;
    }

    private static String extensionFor(String contentType, String originalName) {
        return switch (contentType) {
            case "application/pdf" -> "pdf";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            case "image/gif" -> "gif";
            case "image/jpeg" -> "jpg";
            default -> {
                if (originalName != null && originalName.contains(".")) {
                    yield originalName.substring(originalName.lastIndexOf('.') + 1)
                            .toLowerCase(Locale.ROOT)
                            .replaceAll("[^a-z0-9]", "bin");
                }
                yield "bin";
            }
        };
    }
}
