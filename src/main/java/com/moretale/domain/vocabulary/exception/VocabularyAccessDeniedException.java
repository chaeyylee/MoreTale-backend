package com.moretale.domain.vocabulary.exception;

import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;

public class VocabularyAccessDeniedException extends BusinessException {

    public VocabularyAccessDeniedException() {
        super(ErrorCode.FORBIDDEN, "해당 단어장 항목에 접근 권한이 없습니다.");
    }
}
