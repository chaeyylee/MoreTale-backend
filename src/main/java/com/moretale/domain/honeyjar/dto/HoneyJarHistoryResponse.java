package com.moretale.domain.honeyjar.dto;

import com.moretale.domain.honeyjar.entity.HoneyJarHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "꿀단지 이력 응답 DTO")
@Getter
@Builder
public class HoneyJarHistoryResponse {

    @Schema(description = "이력 ID", example = "42")
    private Long historyId;

    @Schema(
            description = "변동 유형 (EARN_STORY_COMPLETE: 동화 완독 보상, EARN_QUIZ_PERFECT: 퀴즈 100점 보상, USE_FREE_GENERATION: 무료 동화 생성 사용)",
            example = "EARN_STORY_COMPLETE"
    )
    private String actionType;

    @Schema(description = "변동 수량 (획득: 양수, 사용: 음수)", example = "1")
    private Integer amount;

    @Schema(description = "변동 사유", example = "동화 완독 보상")
    private String reason;

    @Schema(description = "변동 후 잔액", example = "8")
    private Integer balanceAfter;

    @Schema(description = "관련 동화 ID", example = "5")
    private Long storyId;

    @Schema(description = "이력 생성 시각 (ISO 8601)", example = "2024-01-15T09:30:00Z")
    private LocalDateTime createdAt;

    public static HoneyJarHistoryResponse from(HoneyJarHistory history) {
        return HoneyJarHistoryResponse.builder()
                .historyId(history.getHistoryId())
                .actionType(history.getActionType().name())
                .amount(history.getAmount())
                .reason(history.getReason())
                .balanceAfter(history.getBalanceAfter())
                .storyId(history.getStoryId())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
