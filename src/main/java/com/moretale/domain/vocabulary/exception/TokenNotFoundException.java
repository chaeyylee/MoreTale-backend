package com.moretale.domain.vocabulary.exception;

public class TokenNotFoundException extends RuntimeException {
    public TokenNotFoundException(Long tokenId) {
        super("토큰을 찾을 수 없습니다. tokenId=" + tokenId);
    }
}
