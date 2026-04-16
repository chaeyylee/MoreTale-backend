package com.moretale.domain.user.dto;

import com.moretale.domain.honeyjar.entity.HoneyJar;
import lombok.*;

/**
 * 사용 현황 응답 DTO
 * - 꿀단지 보유 개수
 * - 무료 동화 잔여 생성 가능 횟수
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsageStatusResponse {

    //현재 보유 꿀단지 수
    private Integer honeyJarCount;

    // 무료 동화 생성 가능 여부 (20개 이상)
    private Boolean canGenerateFreeStory;

    // 무료 생성까지 남은 꿀단지 수
    private Integer remainingHoneyJarForFree;

    // 누적 생성 동화 수
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
