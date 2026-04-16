package com.moretale.domain.quiz.dto;

import com.moretale.domain.quiz.entity.*;
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
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResponse {

    private Long quizId;
    private Long storyId;
    private String language;
    private String difficulty;
    private Integer totalQuestions;
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

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionDto {
        private Long questionId;
        private Integer questionOrder;
        private String questionType;
        private String evaluationType;
        private String questionText;
        private List<OptionDto> options; // T/F는 빈 리스트

        /**
         * 선택 즉시 정답/오답 피드백을 위해 correctAnswer 포함
         * - 선다형: "1" ~ "4" (보기 번호)
         * - T/F형:  "TRUE" | "FALSE"
         */
        private String correctAnswer;

        /**
         * 오답 선택 시 정답 강조 표시를 위해 explanation 포함
         * submit 이후 결과 화면에서도 동일하게 활용 가능
         */
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

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptionDto {
        private Integer optionOrder;
        private String optionText;

        public static OptionDto from(QuizOption option) {
            return OptionDto.builder()
                    .optionOrder(option.getOptionOrder())
                    .optionText(option.getOptionText())
                    .build();
        }
    }
}
