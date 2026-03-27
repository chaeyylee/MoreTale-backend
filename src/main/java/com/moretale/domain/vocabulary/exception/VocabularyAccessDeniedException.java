package com.moretale.domain.vocabulary.exception;

public class VocabularyAccessDeniedException extends RuntimeException {
    public VocabularyAccessDeniedException() {
        super("해당 단어장 항목에 접근 권한이 없습니다.");
    }
}
