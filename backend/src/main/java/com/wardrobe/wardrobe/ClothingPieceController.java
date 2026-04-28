package com.wardrobe.wardrobe;

import com.wardrobe.user.AppUser;
import com.wardrobe.user.AuthController;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
public class ClothingPieceController {
    private final AuthController authController;
    private final ClothingPieceRepository pieceRepository;
    private final FileStorageService fileStorageService;

    public ClothingPieceController(
            AuthController authController,
            ClothingPieceRepository pieceRepository,
            FileStorageService fileStorageService) {
        this.authController = authController;
        this.pieceRepository = pieceRepository;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/pieces")
    public List<ClothingPieceDto> listPieces(Authentication authentication) {
        AppUser user = authController.currentUser(authentication);
        return pieceRepository.findByOwnerOrderByCreatedAtDesc(user).stream()
                .map(ClothingPieceDto::from)
                .toList();
    }

    @PostMapping("/pieces")
    public ClothingPieceDto uploadPiece(
            Authentication authentication,
            @RequestParam ClothingCategory category,
            @RequestParam("photo") MultipartFile photo) {
        AppUser user = authController.currentUser(authentication);
        FileStorageService.StoredFile storedFile = fileStorageService.store(photo, user.getId());
        ClothingPiece piece = new ClothingPiece(
                user,
                category,
                storedFile.imageUrl(),
                storedFile.storagePath(),
                storedFile.originalFilename());
        return ClothingPieceDto.from(pieceRepository.save(piece));
    }

    @DeleteMapping("/pieces/{id}")
    public void deletePiece(Authentication authentication, @PathVariable Long id) {
        AppUser user = authController.currentUser(authentication);
        ClothingPiece piece = pieceRepository.findById(id)
                .filter(found -> found.getOwner().getId().equals(user.getId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Piece not found"));

        pieceRepository.delete(piece);
        fileStorageService.delete(piece.getStoragePath());
    }

    @GetMapping("/outfit/random")
    public Map<ClothingCategory, ClothingPieceDto> randomOutfit(Authentication authentication) {
        AppUser user = authController.currentUser(authentication);
        Map<ClothingCategory, ClothingPieceDto> outfit = new EnumMap<>(ClothingCategory.class);
        Arrays.stream(ClothingCategory.values()).forEach(category ->
                outfit.put(category, randomPiece(user, category).map(ClothingPieceDto::from).orElse(null)));
        return outfit;
    }

    private Optional<ClothingPiece> randomPiece(AppUser user, ClothingCategory category) {
        List<ClothingPiece> pieces = pieceRepository.findByOwnerAndCategory(user, category);
        if (pieces.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(pieces.get(ThreadLocalRandom.current().nextInt(pieces.size())));
    }
}
