package com.moretale.domain.quiz.dto;

import com.moretale.domain.quiz.entity.QuizAnswerRecord;
import com.moretale.domain.quiz.entity.QuizResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Schema(description = "퀴즈 제출 결과 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultResponse {

    @Schema(description = "결과 ID", example = "1")
    private Long resultId;

    @Schema(description = "획득 점수 (0~100)", example = "85")
    private Integer score;

    @Schema(description = "총 문제 수", example = "7")
    private Integer totalQuestions;

    @Schema(description = "정답 수", example = "6")
    private Integer correctCount;

    @Schema(description = "100점 여부", example = "false")
    private Boolean isPerfect;

    @Schema(description = "꿀단지 보상 정보")
    private HoneyJarRewardInfo honeyJarReward;

    @Schema(description = "문항별 정오 내역")
    private List<AnswerResultDto> answerResults;

    @Schema(description = "결과 메시지", example = "👏 훌륭해요! 거의 다 맞혔어요!")
    private String resultMessage;

    public static QuizResultResponse of(
            QuizResult result,
            HoneyJarRewardInfo rewardInfo,
            List<AnswerResultDto> answerResults
    ) {
        String message = buildResultMessage(result.getScore(), result.getIsPerfect());

        return QuizResultResponse.builder()
                .resultId(result.getResultId())
                .score(result.getScore())
                .totalQuestions(result.getTotalQuestions())
                .correctCount(result.getCorrectCount())
                .isPerfect(result.getIsPerfect())
                .honeyJarReward(rewardInfo)
                .answerResults(answerResults)
                .resultMessage(message)
                .build();
    }

    private static String buildResultMessage(int score, boolean isPerfect) {
        if (isPerfect) return "🎉 완벽해요! 모든 문제를 맞혔어요!";
        if (score >= 80) return "👏 훌륭해요! 거의 다 맞혔어요!";
        if (score >= 60) return "😊 잘했어요! 조금만 더 읽어봐요!";
        return "📚 동화를 다시 읽고 도전해봐요!";
    }

    @Schema(description = "꿀단지 보상 정보 DTO")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HoneyJarRewardInfo {

        @Schema(description = "이번에 획득한 꿀단지 수", example = "1")
        private Integer earnedHoneyJars;

        @Schema(description = "현재 보유 꿀단지 수", example = "8")
        private Integer currentHoneyJarCount;

        @Schema(description = "무료 생성 가능 여부 (10개 이상)", example = "false")
        private Boolean canGenerateFree;

        @Schema(description = "10개 달성 시 자동 차감 여부", example = "false")
        private Boolean autoUsedForFreeGeneration;

        @Schema(description = "보상 관련 메시지", example = "🏆 100점 달성! 꿀단지 1개 획득! 🍯")
        private String rewardMessage;
    }

    @Schema(description = "문항별 정오 결과 DTO")
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnswerResultDto {

        @Schema(description = "문제 ID", example = "101")
        private Long questionId;

        @Schema(description = "문제 순서", example = "1")
        private Integer questionOrder;

        @Schema(description = "문제 텍스트", example = "흥부는 어떤 성격을 가진 인물인가요?")
        private String questionText;

        @Schema(description = "제출한 답안", example = "1")
        private String submittedAnswer;

        @Schema(description = "정답", example = "1")
        private String correctAnswer;

        @Schema(description = "정답 여부", example = "true")
        private Boolean isCorrect;

        @Schema(description = "해설", example = "흥부는 마음씨 착한 인물로 묘사됩니다.")
        private String explanation;

        public static AnswerResultDto from(QuizAnswerRecord record) {
            return AnswerResultDto.builder()
                    .questionId(record.getQuestion().getQuestionId())
                    .questionOrder(record.getQuestion().getQuestionOrder())
                    .questionText(record.getQuestion().getQuestionText())
                    .submittedAnswer(record.getSubmittedAnswer())
                    .correctAnswer(record.getQuestion().getCorrectAnswer())
                    .isCorrect(record.getIsCorrect())
                    .explanation(record.getQuestion().getExplanation())
                    .build();
        }
    }
}
