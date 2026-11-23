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

    @Transactional
    public ShortUrl shorten(String originalUrl) {
        String shortCode = shortCodeGenerator.generate();

        var shortUrl = ShortUrl.create(originalUrl, shortCode);
        return shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public ShortUrl access(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(NotFoundShortUrlException::new);
        shortUrl.updateAccessStats(LocalDateTime.now());

        return shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public ShortUrl modifyOriginalUrl(String shortCode, String originalUrl) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(NotFoundShortUrlException::new);
        shortUrl.modifyOriginalUrl(originalUrl, LocalDateTime.now());

        return shortUrlRepository.save(shortUrl);
    }

    @Transactional
    public void delete(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(NotFoundShortUrlException::new);

        shortUrlRepository.delete(shortUrl);
    }

    public ShortUrlStats getStats(String shortCode) {
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(NotFoundShortUrlException::new);
        return new ShortUrlStats(shortUrl.getAccessCount(), shortUrl.getLastAccessedAt());
    }
}
