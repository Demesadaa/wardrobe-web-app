package com.wardrobe.wardrobe;

import java.time.Instant;

public record ClothingPieceDto(
        Long id,
        ClothingCategory category,
        String imageUrl,
        String originalFilename,
        Instant createdAt) {
    public static ClothingPieceDto from(ClothingPiece piece) {
        return new ClothingPieceDto(
                piece.getId(),
                piece.getCategory(),
                piece.getImageUrl(),
                piece.getOriginalFilename(),
                piece.getCreatedAt());
    }
}
