package com.wardrobe.wardrobe;

import com.wardrobe.wardrobe.ClothingPieceRequests.UpdateClothingPieceRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@Tag(name = "Clothing Pieces", description = "CRUD operations for saved wardrobe pieces")
public class ClothingPieceController {
    private final ClothingPieceService pieceService;

    public ClothingPieceController(ClothingPieceService pieceService) {
        this.pieceService = pieceService;
    }

    @Operation(summary = "Get all clothing pieces for the current user")
    @GetMapping("/pieces")
    public List<ClothingPieceDto> listPieces(Authentication authentication) {
        return pieceService.getCurrentUserPieces(authentication);
    }

    @Operation(summary = "Upload and create a clothing piece for the current user")
    @PostMapping("/pieces")
    @ResponseStatus(HttpStatus.CREATED)
    public ClothingPieceDto uploadPiece(
            Authentication authentication,
            @RequestParam ClothingCategory category,
            @RequestParam("photo") MultipartFile photo) {
        return pieceService.createPiece(authentication, category, photo);
    }

    @Operation(summary = "Get one current-user clothing piece by ID")
    @GetMapping("/pieces/{id}")
    public ClothingPieceDto getPiece(Authentication authentication, @PathVariable Long id) {
        return pieceService.getCurrentUserPiece(authentication, id);
    }

    @Operation(summary = "Update a current-user clothing piece by ID")
    @PutMapping("/pieces/{id}")
    public ClothingPieceDto updatePiece(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody UpdateClothingPieceRequest request) {
        return pieceService.updateCurrentUserPiece(authentication, id, request);
    }

    @Operation(summary = "Delete a current-user clothing piece by ID")
    @DeleteMapping("/pieces/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePiece(Authentication authentication, @PathVariable Long id) {
        pieceService.deleteCurrentUserPiece(authentication, id);
    }

    @Operation(summary = "Get a random outfit from the current user's saved pieces")
    @GetMapping("/outfit/random")
    public Map<ClothingCategory, ClothingPieceDto> randomOutfit(Authentication authentication) {
        return pieceService.randomOutfit(authentication);
    }

    @Operation(summary = "Get all clothing pieces in the database")
    @GetMapping("/clothing-pieces")
    public List<ClothingPieceDto> getAllPieces() {
        return pieceService.getAllPieces();
    }

    @Operation(summary = "Get any clothing piece by ID")
    @GetMapping("/clothing-pieces/{id}")
    public ClothingPieceDto getPieceById(@PathVariable Long id) {
        return pieceService.getPieceById(id);
    }
}
