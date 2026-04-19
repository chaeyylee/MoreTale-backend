package com.moretale.domain.quiz.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "퀴즈 평가 유형 (VOCABULARY: 단어 이해, STORY: 줄거리 이해)")
@Getter
@RequiredArgsConstructor
public enum EvaluationType {
    VOCABULARY("단어 이해"),    // 단어 뜻을 알아야 풀 수 있는 문제
    STORY("줄거리 이해");       // 내용을 읽어야 풀 수 있는 문제

    private final String description;
}
