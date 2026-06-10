package com.moretale.domain.quiz.dto;

import com.moretale.domain.quiz.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * GET /api/quiz?storyId={storyId} 응답 DTO
 *
 * - QuestionDto에 correctAnswer 필드 추가
 * - 프론트에서 선택 즉시 정답/오답 판별 가능하도록 제공
 * - 보안 trade-off: 아동 교육용 앱 특성상 클라이언트 노출 허용
 *   서버 무결성은 submit 시점의 서버 재채점으로 보장 (기존 유지)
 */
@Schema(description = "퀴즈 조회 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {

    @Schema(description = "퀴즈 ID", example = "1")
    private Long quizId;

    @Schema(description = "연결된 동화 ID", example = "5")
    private Long storyId;

    @Schema(description = "문제 출제 언어", example = "KO")
    private String language;

    @Schema(description = "난이도 (쉬움 / 보통 / 어려움)", example = "보통")
    private String difficulty;

    @Schema(description = "총 문제 수", example = "7")
    private Integer totalQuestions;

    @Schema(description = "문제 목록")
    private List<QuestionDto> questions;

    public static QuizResponse from(Quiz quiz) {
        return QuizResponse.builder()
                .quizId(quiz.getQuizId())
                .storyId(quiz.getStory().getStoryId())
                .language(quiz.getLanguage())
                .difficulty(quiz.getDifficulty().getDescription())
                .totalQuestions(quiz.getTotalQuestions())
                .questions(quiz.getQuestions().stream()
                        .map(QuestionDto::from)
                        .collect(Collectors.toList()))
                .build();
    }

    @Schema(description = "퀴즈 문제 DTO")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDto {

        @Schema(description = "문제 ID", example = "101")
        private Long questionId;

        @Schema(description = "문제 순서 (1부터 시작)", example = "1")
        private Integer questionOrder;

        @Schema(description = "문제 유형 (MULTIPLE_CHOICE: 선다형, TRUE_FALSE: T/F형)",
                example = "MULTIPLE_CHOICE")
        private String questionType;

        @Schema(description = "평가 유형 설명", example = "내용 이해")
        private String evaluationType;

        @Schema(description = "문제 텍스트", example = "흥부는 어떤 성격을 가진 인물인가요?")
        private String questionText;

        @Schema(description = "선택지 목록 (TRUE_FALSE형은 빈 리스트)")
        private List<OptionDto> options;

        @Schema(description = "정답 (선다형: 1~4, T/F형: TRUE 또는 FALSE)", example = "1")
        private String correctAnswer;

        @Schema(description = "해설", example = "흥부는 마음씨 착한 인물로 묘사됩니다.")
        private String explanation;

        public static QuestionDto from(QuizQuestion question) {
            return QuestionDto.builder()
                    .questionId(question.getQuestionId())
                    .questionOrder(question.getQuestionOrder())
                    .questionType(question.getQuestionType().name())
                    .evaluationType(question.getEvaluationType().getDescription())
                    .questionText(question.getQuestionText())
                    .options(question.getOptions().stream()
                            .map(OptionDto::from)
                            .collect(Collectors.toList()))
                    .correctAnswer(question.getCorrectAnswer())
                    .explanation(question.getExplanation())
                    .build();
        }
    }

    @Schema(description = "선택지 DTO")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDto {

        @Schema(description = "선택지 번호 (1~4)", example = "1")
        private Integer optionOrder;

        @Schema(description = "선택지 텍스트", example = "마음씨 착한 사람")
        private String optionText;

        public static OptionDto from(QuizOption option) {
            return OptionDto.builder()
                    .optionOrder(option.getOptionOrder())
                    .optionText(option.getOptionText())
                    .build();
        }
    }
}
