package com.delver.shorturl.application;

import com.delver.shorturl.domain.ShortUrl;
import com.delver.shorturl.domain.ShortUrlRepository;
import com.delver.shorturl.domain.ShortUrlStats;
import com.delver.shorturl.support.error.exception.NotFoundShortUrlException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShortUrlService {

    private final ShortCodeGenerator shortCodeGenerator;
    private final ShortUrlRepository shortUrlRepository;
    private final ShortUrlCache shortUrlCache;

    @Transactional
    public ShortUrl shorten(String originalUrl) {
        String shortCode = shortCodeGenerator.generate();

        var shortUrl = ShortUrl.create(originalUrl, shortCode);
        return shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public ShortUrlDestination access(String shortCode) {
        LocalDateTime accessedAt = LocalDateTime.now();
        String cachedUrl = shortUrlCache.findOriginalUrl(shortCode);
        if (cachedUrl != null && shortUrlRepository.recordAccess(shortCode, cachedUrl, accessedAt) > 0) {
            return new ShortUrlDestination(cachedUrl, shortCode);
        }

        if (shortUrlRepository.recordAccess(shortCode, null, accessedAt) == 0) {
            throw new NotFoundShortUrlException();
        }

        ShortUrl shortUrl = findShortUrl(shortCode);
        String originalUrl = shortUrl.getOriginalUrl();
        if (originalUrl != null) {
            // The access-count update keeps the row locked while the current URL is cached.
            shortUrlCache.put(shortUrl.getShortCode(), originalUrl);
        }
        return new ShortUrlDestination(originalUrl, shortUrl.getShortCode());
    }

    @Transactional
    public ShortUrl modifyOriginalUrl(String shortCode, String originalUrl) {
        ShortUrl shortUrl = findShortUrl(shortCode);
        shortUrl.modifyOriginalUrl(originalUrl, LocalDateTime.now());
        shortUrlCache.putAfterCommit(shortUrl.getShortCode(), originalUrl);

        return shortUrl;
    }

    @Transactional
    public void delete(String shortCode) {
        ShortUrl shortUrl = findShortUrl(shortCode);

        shortUrlRepository.delete(shortUrl);
        shortUrlCache.evictAfterCommit(shortUrl.getShortCode());
    }

    public ShortUrlStats getStats(String shortCode) {
        ShortUrl shortUrl = findShortUrl(shortCode);
        return new ShortUrlStats(shortUrl.getAccessCount(), shortUrl.getLastAccessedAt());
    }

    private ShortUrl findShortUrl(String shortCode) {
        return shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(NotFoundShortUrlException::new);
    }
}
