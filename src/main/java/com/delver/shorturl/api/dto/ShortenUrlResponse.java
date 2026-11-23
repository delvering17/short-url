package com.delver.shorturl.api.dto;

import com.delver.shorturl.domain.ShortUrl;

public record ShortenUrlResponse(
        String originalUrl,
        String shortCode
) {
    public static ShortenUrlResponse from(ShortUrl shortUrl) {
        return new ShortenUrlResponse(shortUrl.getOriginalUrl(), shortUrl.getShortCode());
    }

}
