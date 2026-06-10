package com.moretale.domain.profile.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "이야기 선호도 (WARM_HUG ~ CUSTOM)")
@Getter
@RequiredArgsConstructor
public enum StoryPreference {
    WARM_HUG("포근포근 안아주는 이야기"),
    FUN_ADVENTURE("신나는 모험 이야기"),
    DAILY_LIFE("오늘 하루를 담은 이야기"),
    CUSTOM("직접 적을래요");

    private final String description;

    // 하위 호환성: 기존 Enum 값 매핑
    public static StoryPreference fromLegacy(String legacy) {
        return switch (legacy.toUpperCase()) {
            case "WARM" -> WARM_HUG;
            case "ADVENTURE" -> FUN_ADVENTURE;
            case "DAILY" -> DAILY_LIFE;
            case "CUSTOM" -> CUSTOM;
            default -> WARM_HUG;
        };
    }
}
