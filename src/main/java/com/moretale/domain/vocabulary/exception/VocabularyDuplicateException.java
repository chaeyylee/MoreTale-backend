package com.moretale.domain.vocabulary.exception;

import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;

public class VocabularyDuplicateException extends BusinessException {

    public VocabularyDuplicateException(String normalizedWord) {
        super(ErrorCode.INVALID_INPUT_VALUE,
                "이미 저장된 단어입니다. word=" + normalizedWord);
    }
}
