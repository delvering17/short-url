package com.delver.shorturl.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.SoftDelete;
import org.hibernate.annotations.SoftDeleteType;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "short_urls",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_short_code", columnNames = "short_code")
        },
        indexes = {
                @Index(name = "idx_short_code", columnList = "short_code")
        }
)
@SoftDelete(
        strategy = SoftDeleteType.TIMESTAMP,
        columnName = "deleted_at"
)
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalUrl;

    private String shortCode;

    private Long accessCount;

    private LocalDateTime lastAccessedAt;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public ShortUrl() {
    }

    public static ShortUrl create(String originalUrl, String shortCode) {
        ShortUrl shortUrl = new ShortUrl();
        shortUrl.originalUrl = originalUrl;
        shortUrl.shortCode = shortCode;
        shortUrl.accessCount = 0L;
        return shortUrl;
    }

    public void updateAccessStats(LocalDateTime lastAccessedAt) {
        this.accessCount++;
        this.lastAccessedAt = lastAccessedAt;
    }

    public void modifyOriginalUrl(String originalUrl, LocalDateTime now) {
        this.originalUrl = originalUrl;
        this.updatedAt = now;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public Long getAccessCount() {
        return accessCount;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
}
