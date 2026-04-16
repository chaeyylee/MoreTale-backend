package com.moretale.domain.honeyjar.dto;

import com.moretale.domain.honeyjar.entity.HoneyJar;
import lombok.*;

// 꿀단지 상태 응답 DTO
// GET /api/honey-jar 응답
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoneyJarResponse {

    private Integer count;              // 현재 보유 꿀단지 수
    private Integer totalEarned;        // 누적 획득 수
    private Integer totalUsed;          // 누적 사용 수
    private Boolean canGenerateFree;    // 무료 생성 가능 여부
    private Integer remainingForFree;   // 무료 생성까지 남은 꿀단지 수

    private static final int FREE_GENERATION_THRESHOLD = 20;

    public static HoneyJarResponse from(HoneyJar honeyJar) {
        int remaining = Math.max(0, FREE_GENERATION_THRESHOLD - honeyJar.getCount());
        return HoneyJarResponse.builder()
                .count(honeyJar.getCount())
                .totalEarned(honeyJar.getTotalEarned())
                .totalUsed(honeyJar.getTotalUsed())
                .canGenerateFree(honeyJar.canGenerateFree())
                .remainingForFree(remaining)
                .build();
    }
}
