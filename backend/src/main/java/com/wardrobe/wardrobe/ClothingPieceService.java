package com.wardrobe.wardrobe;

import com.wardrobe.user.AppUser;
import com.wardrobe.user.UserService;
import com.wardrobe.wardrobe.ClothingPieceRequests.UpdateClothingPieceRequest;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ClothingPieceService {
    private static final Logger log = LoggerFactory.getLogger(ClothingPieceService.class);

    private final UserService userService;
    private final ClothingPieceRepository pieceRepository;
    private final FileStorageService fileStorageService;

    public ClothingPieceService(
            UserService userService,
            ClothingPieceRepository pieceRepository,
            FileStorageService fileStorageService) {
        this.userService = userService;
        this.pieceRepository = pieceRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ClothingPieceDto createPiece(Authentication authentication, ClothingCategory category, MultipartFile photo) {
        AppUser user = userService.currentUser(authentication);
        FileStorageService.StoredFile storedFile = fileStorageService.store(photo, user.getId());
        ClothingPiece piece = new ClothingPiece(
                user,
                category,
                storedFile.imageUrl(),
                storedFile.storagePath(),
                storedFile.originalFilename());
        ClothingPiece savedPiece = pieceRepository.save(piece);
        log.info("User {} created clothing piece {} in category {}", user.getUsername(), savedPiece.getId(), category);
        return ClothingPieceDto.from(savedPiece);
    }

    @Transactional(readOnly = true)
    public List<ClothingPieceDto> getAllPieces() {
        log.debug("Loading all clothing pieces for admin request");
        return pieceRepository.findAll().stream()
                .map(ClothingPieceDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ClothingPieceDto> getCurrentUserPieces(Authentication authentication) {
        AppUser user = userService.currentUser(authentication);
        log.debug("Loading clothing pieces for user {}", user.getUsername());
        return pieceRepository.findByOwnerOrderByCreatedAtDesc(user).stream()
                .map(ClothingPieceDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClothingPieceDto getCurrentUserPiece(Authentication authentication, Long id) {
        AppUser user = userService.currentUser(authentication);
        return ClothingPieceDto.from(findOwnedPiece(user, id));
    }

    @Transactional(readOnly = true)
    public ClothingPieceDto getPieceById(Long id) {
        return ClothingPieceDto.from(findPieceById(id));
    }

    @Transactional
    public ClothingPieceDto updateCurrentUserPiece(
            Authentication authentication,
            Long id,
            UpdateClothingPieceRequest request) {
        AppUser user = userService.currentUser(authentication);
        ClothingPiece piece = findOwnedPiece(user, id);
        piece.setCategory(request.category());
        ClothingPiece savedPiece = pieceRepository.save(piece);
        log.info("User {} updated clothing piece {} to category {}", user.getUsername(), id, request.category());
        return ClothingPieceDto.from(savedPiece);
    }

    @Transactional
    public void deleteCurrentUserPiece(Authentication authentication, Long id) {
        AppUser user = userService.currentUser(authentication);
        ClothingPiece piece = findOwnedPiece(user, id);
        pieceRepository.delete(piece);
        fileStorageService.delete(piece.getStoragePath());
        log.warn("User {} deleted clothing piece {}", user.getUsername(), id);
    }

    @Transactional(readOnly = true)
    public Map<ClothingCategory, ClothingPieceDto> randomOutfit(Authentication authentication) {
        AppUser user = userService.currentUser(authentication);
        Map<ClothingCategory, ClothingPieceDto> outfit = new EnumMap<>(ClothingCategory.class);
        Arrays.stream(ClothingCategory.values()).forEach(category ->
                outfit.put(category, randomPiece(user, category).map(ClothingPieceDto::from).orElse(null)));
        log.debug("Generated random outfit for user {}", user.getUsername());
        return outfit;
    }

    private ClothingPiece findPieceById(Long id) {
        return pieceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Piece not found"));
    }

    private ClothingPiece findOwnedPiece(AppUser user, Long id) {
        return pieceRepository.findByOwnerAndId(user, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Piece not found"));
    }

    private Optional<ClothingPiece> randomPiece(AppUser user, ClothingCategory category) {
        List<ClothingPiece> pieces = pieceRepository.findByOwnerAndCategory(user, category);
        if (pieces.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(pieces.get(ThreadLocalRandom.current().nextInt(pieces.size())));
    }
}
