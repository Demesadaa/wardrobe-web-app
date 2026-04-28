package com.wardrobe.wardrobe;

import com.wardrobe.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "clothing_pieces")
public class ClothingPiece {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private AppUser owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClothingCategory category;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String storagePath;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private Instant createdAt;

    protected ClothingPiece() {
    }

    public ClothingPiece(
            AppUser owner,
            ClothingCategory category,
            String imageUrl,
            String storagePath,
            String originalFilename) {
        this.owner = owner;
        this.category = category;
        this.imageUrl = imageUrl;
        this.storagePath = storagePath;
        this.originalFilename = originalFilename;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getOwner() {
        return owner;
    }

    public ClothingCategory getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
