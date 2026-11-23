package com.delver.shorturl.api.dto;

import com.delver.shorturl.api.validation.HttpUrl;
import jakarta.validation.constraints.NotBlank;

public record ModifyShortenUrlRequest(
        @NotBlank(message = "원본 URL을 입력해 주세요.") @HttpUrl String longUrl
) {
}
