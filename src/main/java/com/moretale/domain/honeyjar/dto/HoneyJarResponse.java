package com.moretale.domain.honeyjar.dto;

import com.moretale.domain.honeyjar.entity.HoneyJar;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "꿀단지 현황 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoneyJarResponse {

    @Schema(description = "현재 보유 꿀단지 수", example = "7")
    private Integer count;

    @Schema(description = "누적 획득 꿀단지 수", example = "12")
    private Integer totalEarned;

    @Schema(description = "누적 사용 꿀단지 수", example = "5")
    private Integer totalUsed;

    @Schema(description = "동화 무료 생성 가능 여부 (20개 이상 보유 시 true)", example = "false")
    private Boolean canGenerateFree;

    @Schema(description = "무료 생성까지 남은 꿀단지 수", example = "13")
    private Integer remainingForFree;

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
