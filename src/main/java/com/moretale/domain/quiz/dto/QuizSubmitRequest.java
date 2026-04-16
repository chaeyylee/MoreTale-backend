package com.moretale.domain.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

// POST /api/quiz/submit 요청 DTO
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmitRequest {

    @NotNull(message = "퀴즈 ID는 필수입니다.")
    private Long quizId;

    @NotEmpty(message = "답안은 최소 1개 이상이어야 합니다.")
    @Valid
    private List<AnswerDto> answers;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDto {

        @NotNull(message = "문제 ID는 필수입니다.")
        private Long questionId;

        @NotNull(message = "답안은 필수입니다.")
        private String submittedAnswer; // 선다형: "1"~"4", T/F: "TRUE"/"FALSE"
    }
}
