package com.moretale.domain.vocabulary.exception;

public class VocabularyNotFoundException extends RuntimeException {
    public VocabularyNotFoundException(Long vocabularyId) {
        super("단어장 항목을 찾을 수 없습니다. vocabularyId=" + vocabularyId);
    }
}
