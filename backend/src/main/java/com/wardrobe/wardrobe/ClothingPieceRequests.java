package com.wardrobe.wardrobe;

import jakarta.validation.constraints.NotNull;

public final class ClothingPieceRequests {
    private ClothingPieceRequests() {
    }

    public record UpdateClothingPieceRequest(@NotNull ClothingCategory category) {
    }
}
