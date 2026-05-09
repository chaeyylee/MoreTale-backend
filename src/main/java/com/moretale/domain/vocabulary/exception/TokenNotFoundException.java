package com.moretale.domain.vocabulary.exception;

import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;

public class TokenNotFoundException extends BusinessException {

    public TokenNotFoundException(Long tokenId) {
        super(ErrorCode.RESOURCE_NOT_FOUND,
                "토큰을 찾을 수 없습니다. tokenId=" + tokenId);
    }
}
