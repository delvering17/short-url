package com.delver.shorturl.api;

import com.delver.shorturl.api.dto.ModifyShortenUrlRequest;
import com.delver.shorturl.api.dto.ShortUrlStatsResponse;
import com.delver.shorturl.api.dto.ShortenUrlResponse;
import com.delver.shorturl.application.ShortUrlDestination;
import com.delver.shorturl.application.ShortUrlService;
import com.delver.shorturl.domain.ShortUrl;
import com.delver.shorturl.domain.ShortUrlStats;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shorten")
@RequiredArgsConstructor
public class ShortUrlController {

    private final ShortUrlService shortUrlService;

    @PostMapping
    public ResponseEntity<ShortenUrlResponse> shorten(@Valid @RequestBody ShortenUrlRequest request) {
        ShortUrl shortUrl = shortUrlService.shorten(request.longUrl());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ShortenUrlResponse.from(shortUrl));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<ShortenUrlResponse> accessShortUrl(@PathVariable String shortCode) {
        ShortUrlDestination destination = shortUrlService.access(shortCode);

        return ResponseEntity.ok(new ShortenUrlResponse(destination.originalUrl(), destination.shortCode()));
    }

    @GetMapping("/{shortCode}/stats")
    public ResponseEntity<ShortUrlStatsResponse> getShortUrlStats(@PathVariable String shortCode) {
        ShortUrlStats shortUrlStats = shortUrlService.getStats(shortCode);

        return ResponseEntity.ok(ShortUrlStatsResponse.from(shortUrlStats));
    }

    @PutMapping("/{shortCode}")
    public ResponseEntity<ShortenUrlResponse> modifyOriginalUrl(
            @PathVariable String shortCode, @Valid @RequestBody ModifyShortenUrlRequest request) {
        ShortUrl shortUrl = shortUrlService.modifyOriginalUrl(shortCode, request.longUrl());

        return ResponseEntity.ok(ShortenUrlResponse.from(shortUrl));
    }

    @DeleteMapping("/{shortCode}")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String shortCode) {
        shortUrlService.delete(shortCode);

        return ResponseEntity.noContent().build();
    }

}
