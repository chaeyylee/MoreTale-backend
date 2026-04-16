package com.moretale.domain.honeyjar.dto;

import com.moretale.domain.honeyjar.entity.HoneyJarHistory;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class HoneyJarHistoryResponse {

    private Long historyId;
    private String actionType;
    private Integer amount;
    private String reason;
    private Integer balanceAfter;
    private Long storyId;
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
