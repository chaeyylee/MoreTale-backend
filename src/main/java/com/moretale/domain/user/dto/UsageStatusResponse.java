package com.moretale.domain.user.dto;

import com.moretale.domain.honeyjar.entity.HoneyJar;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "마이페이지 사용 현황 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageStatusResponse {

    @Schema(description = "현재 보유 꿀단지 수", example = "7")
    private Integer honeyJarCount;

    @Schema(description = "무료 동화 생성 가능 여부 (20개 이상)", example = "false")
    private Boolean canGenerateFreeStory;

    @Schema(description = "무료 생성까지 남은 꿀단지 수", example = "13")
    private Integer remainingHoneyJarForFree;

    @Schema(description = "누적 생성 동화 수", example = "12")
    private Long totalStoriesCreated;

    private static final int FREE_GENERATION_THRESHOLD = 20;

    public static UsageStatusResponse of(HoneyJar honeyJar, long totalStoriesCreated) {
        int count = honeyJar != null ? honeyJar.getCount() : 0;
        int remaining = Math.max(0, FREE_GENERATION_THRESHOLD - count);

        return UsageStatusResponse.builder()
                .honeyJarCount(count)
                .canGenerateFreeStory(count >= FREE_GENERATION_THRESHOLD)
                .remainingHoneyJarForFree(remaining)
                .totalStoriesCreated(totalStoriesCreated)
                .build();
    }

    // 꿀단지 레코드가 아직 없는 사용자용 (신규 가입자)
    public static UsageStatusResponse empty(long totalStoriesCreated) {
        return UsageStatusResponse.builder()
                .honeyJarCount(0)
                .canGenerateFreeStory(false)
                .remainingHoneyJarForFree(FREE_GENERATION_THRESHOLD)
                .totalStoriesCreated(totalStoriesCreated)
                .build();
    }
}
