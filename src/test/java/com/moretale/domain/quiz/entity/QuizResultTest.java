package com.moretale.domain.quiz.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("QuizResult 점수 계산 테스트")
class QuizResultTest {

    @ParameterizedTest(name = "correct={0}, total={1} → score={2}")
    @CsvSource({
            "0,  0,   0",   // 0문제 → 0점
            "0,  5,   0",   // 전부 오답
            "5,  5, 100",   // 전부 정답
            "3,  5,  60",   // 60점
            "6,  7,  86",   // 반올림: 6/7 * 100 = 85.7 → 86
            "1,  7,  14",   // 반올림: 1/7 * 100 = 14.28 → 14
            "1,  3,  33",   // 반올림: 1/3 * 100 = 33.33 → 33
    })
    @DisplayName("calculateScore - 정답 수 / 총 문제 수 × 100, 반올림")
    void calculateScore(int correct, int total, int expected) {
        int result = QuizResult.calculateScore(correct, total);
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest(name = "correct={0}, total={1}")
    @CsvSource({"7, 7", "10, 10"})
    @DisplayName("calculateScore - 100점이면 isPerfect 로직과 일치")
    void calculateScore_perfectShouldBeHundred(int correct, int total) {
        int score = QuizResult.calculateScore(correct, total);
        assertThat(score).isEqualTo(100);
    }
}
