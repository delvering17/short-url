package com.delver.shorturl.api.dto;

import com.delver.shorturl.domain.ShortUrl;
import com.delver.shorturl.domain.ShortUrlStats;

import java.time.LocalDateTime;

public record ShortUrlStatsResponse(
        Long accessCount,
        LocalDateTime lastAccessedAt
) {
    public static ShortUrlStatsResponse from(ShortUrlStats shortUrlStats) {
        return new ShortUrlStatsResponse(shortUrlStats.accessCount(), shortUrlStats.lastAccessedAt());
    }
}
