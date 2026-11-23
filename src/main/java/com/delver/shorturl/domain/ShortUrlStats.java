package com.delver.shorturl.domain;

import java.time.LocalDateTime;

public record ShortUrlStats(
        Long accessCount,
        LocalDateTime lastAccessedAt
) {
}
