package com.delver.shorturl.api;

import com.delver.shorturl.api.validation.HttpUrl;
import jakarta.validation.constraints.NotBlank;

public record ShortenUrlRequest(
        @NotBlank(message = "원본 URL을 입력해 주세요.") @HttpUrl String longUrl
) {
}
