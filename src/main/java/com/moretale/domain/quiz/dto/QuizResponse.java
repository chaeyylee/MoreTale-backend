package com.moretale.domain.quiz.dto;

import com.moretale.domain.quiz.entity.*;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

// GET /api/quiz?storyId={storyId} 응답 DTO
// 정답은 포함하지 않음 (채점은 서버에서만 처리)
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
