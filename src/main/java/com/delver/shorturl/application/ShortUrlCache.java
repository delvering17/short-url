package com.delver.shorturl.application;

import org.jspecify.annotations.Nullable;

public interface ShortUrlCache {

    @Nullable String findOriginalUrl(String shortCode);

    void put(String shortCode, String originalUrl);

    void putAfterCommit(String shortCode, String originalUrl);

    void evictAfterCommit(String shortCode);
}
