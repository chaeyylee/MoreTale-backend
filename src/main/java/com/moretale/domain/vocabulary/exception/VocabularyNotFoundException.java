package com.moretale.domain.vocabulary.exception;

import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;

public class VocabularyNotFoundException extends BusinessException {

    public VocabularyNotFoundException(Long vocabularyId) {
        super(ErrorCode.RESOURCE_NOT_FOUND,
                "단어장 항목을 찾을 수 없습니다. vocabularyId=" + vocabularyId);
    }
}
