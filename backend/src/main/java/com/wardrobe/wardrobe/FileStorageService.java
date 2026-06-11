package com.wardrobe.wardrobe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);

    private final Path uploadRoot;

    public FileStorageService(StorageProperties properties) {
        this.uploadRoot = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
    }

    public StoredFile store(MultipartFile file, Long userId) {
        if (file.isEmpty()) {
            log.warn("Rejected empty upload for user {}", userId);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Rejected upload for user {} with unsupported content type {}", userId, contentType);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only image uploads are supported");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "photo.jpg" : file.getOriginalFilename());
        String extension = extensionFor(originalName, contentType);
        String filename = UUID.randomUUID() + extension;
        Path userDirectory = uploadRoot.resolve(String.valueOf(userId)).normalize();
        Path target = userDirectory.resolve(filename).normalize();

        try {
            Files.createDirectories(userDirectory);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            log.error("Could not store upload {} for user {}", originalName, userId, exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store image", exception);
        }

        String imageUrl = "/uploads/" + userId + "/" + filename;
        log.info("Stored upload {} for user {} at {}", originalName, userId, target);
        return new StoredFile(imageUrl, target.toString(), originalName);
    }

    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(Path.of(storagePath));
        } catch (IOException ignored) {
            // A missing image file should not block deleting the database row.
            log.warn("Could not delete stored image {}", storagePath, ignored);
        }
    }

    public Path uploadRoot() {
        return uploadRoot;
    }

    private String extensionFor(String originalName, String contentType) {
        if (contentType.equals("image/png")) {
            return ".png";
        }
        if (contentType.equals("image/webp")) {
            return ".webp";
        }
        if (contentType.equals("image/jpeg") || contentType.equals("image/jpg")) {
            return ".jpg";
        }

        String cleaned = originalName.toLowerCase(Locale.ROOT);
        int dot = cleaned.lastIndexOf('.');
        if (dot >= 0 && dot < cleaned.length() - 1) {
            return cleaned.substring(dot);
        }
        return ".jpg";
    }

    public record StoredFile(String imageUrl, String storagePath, String originalFilename) {
    }
}
