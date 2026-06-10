package com.moretale.domain.quiz.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

// POST /api/quiz/submit 요청 DTO
@Schema(description = "퀴즈 제출 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmitRequest {

    @NotNull(message = "퀴즈 ID는 필수입니다.")
    @Schema(description = "제출할 퀴즈 ID", example = "1")
    private Long quizId;

    @NotEmpty(message = "답안은 최소 1개 이상이어야 합니다.")
    @Valid
    @Schema(description = "문항별 답안 목록")
    private List<AnswerDto> answers;

    @Schema(description = "문항별 답안 DTO")
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerDto {

        @NotNull(message = "문제 ID는 필수입니다.")
        @Schema(description = "문제 ID", example = "101")
        private Long questionId;

        @NotNull(message = "답안은 필수입니다.")
        @Schema(description = "제출 답안 (선다형: 1~4, T/F형: TRUE 또는 FALSE)", example = "1")
        private String submittedAnswer;
    }
}
