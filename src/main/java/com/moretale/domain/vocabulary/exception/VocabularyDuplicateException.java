package com.moretale.domain.vocabulary.exception;

public class VocabularyDuplicateException extends RuntimeException {
    public VocabularyDuplicateException(String normalizedWord) {
        super("이미 저장된 단어입니다. word=" + normalizedWord);
    }
}
