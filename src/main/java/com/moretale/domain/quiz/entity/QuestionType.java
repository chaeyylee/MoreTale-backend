package com.moretale.domain.quiz.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "퀴즈 문제 유형 (MULTIPLE_CHOICE: 4지 선다형, TRUE_FALSE: 참/거짓형)")
@Getter
@RequiredArgsConstructor
public enum QuestionType {
    MULTIPLE_CHOICE("선다형"),   // 4지 선다형
    TRUE_FALSE("참/거짓");       // T/F형

    private final String description;
}
