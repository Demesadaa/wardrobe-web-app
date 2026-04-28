package com.wardrobe.wardrobe;

import com.wardrobe.user.AppUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothingPieceRepository extends JpaRepository<ClothingPiece, Long> {
    List<ClothingPiece> findByOwnerOrderByCreatedAtDesc(AppUser owner);

    List<ClothingPiece> findByOwnerAndCategory(AppUser owner, ClothingCategory category);
}
