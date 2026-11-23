package com.delver.shorturl.support.error.exception;

import org.springframework.http.HttpStatus;

public class NotFoundShortUrlException extends DomainException {

    public NotFoundShortUrlException() {
        super(HttpStatus.NOT_FOUND, "NOT_FOUND_SHORT_URL", "조회한 ShortUrl이 없습니다.");
    }
}
