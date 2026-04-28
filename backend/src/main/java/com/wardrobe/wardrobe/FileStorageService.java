package com.wardrobe.wardrobe;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FileStorageService {
    private final Path uploadRoot;

    public FileStorageService(StorageProperties properties) {
        this.uploadRoot = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
    }

    public StoredFile store(MultipartFile file, Long userId) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Photo is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
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
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store image", exception);
        }

        String imageUrl = "/uploads/" + userId + "/" + filename;
        return new StoredFile(imageUrl, target.toString(), originalName);
    }

    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(Path.of(storagePath));
        } catch (IOException ignored) {
            // A missing image file should not block deleting the database row.
        }
    }

    public Path uploadRoot() {
        return uploadRoot;
    }

    private String extensionFor(String originalName, String contentType) {
        String cleaned = originalName.toLowerCase(Locale.ROOT);
        int dot = cleaned.lastIndexOf('.');
        if (dot >= 0 && dot < cleaned.length() - 1) {
            return cleaned.substring(dot);
        }
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
    }

    public record StoredFile(String imageUrl, String storagePath, String originalFilename) {
    }
}
