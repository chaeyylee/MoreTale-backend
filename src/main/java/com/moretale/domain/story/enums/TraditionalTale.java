package com.moretale.domain.story.enums;

import com.moretale.domain.profile.entity.StoryPreference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

// 전래동화 매핑 Enum
// 이야기 선호도(StoryPreference)에 따라 추천 전래동화를 자동 선택
@Getter
@RequiredArgsConstructor
public enum TraditionalTale {

    // 포근한 이야기
    HEUNGBU_NOLBU("흥부와 놀부", StoryPreference.WARM_HUG,
            "착한 흥부와 욕심 많은 놀부의 이야기"),

    KONGJWI_PATJWI("콩쥐 팥쥐", StoryPreference.WARM_HUG,
            "착한 콩쥐가 어려움을 이겨내는 이야기"),

    // 신나는 모험 이야기
    GOLD_AXE_SILVER_AXE("금도끼 은도끼", StoryPreference.FUN_ADVENTURE,
            "정직한 나무꾼이 산신령을 만나는 이야기"),

    RABBIT_AND_TURTLE("토끼와 거북이", StoryPreference.FUN_ADVENTURE,
            "느리지만 꾸준한 거북이의 승리 이야기"),

    // 오늘 하루를 담은 이야기
    SUN_AND_MOON("해와 달이 된 오누이", StoryPreference.DAILY_LIFE,
            "오누이가 해와 달이 된 이야기"),

    TIGER_AND_PERSIMMON("호랑이와 곶감", StoryPreference.DAILY_LIFE,
            "호랑이가 곶감을 무서워한 이야기"),

    // 직접 적을래요 (기본값)
    CUSTOM("사용자 맞춤 이야기", StoryPreference.CUSTOM,
            "사용자가 직접 입력한 주제");

    private final String title;
    private final StoryPreference preference;
    private final String description;

    /**
     * StoryPreference에 맞는 전래동화 자동 선택
     *
     * 현재 운영 기준:
     * - 포근한 이야기 → 흥부와 놀부
     * - 신나는 모험 이야기 → 호랑이와 곶감
     * - 오늘 하루를 닮은 이야기 → 호랑이와 곶감
     * - 직접 입력 / null → 흥부와 놀부
     */
    public static TraditionalTale findByPreference(StoryPreference preference) {
        if (preference == null || preference == StoryPreference.CUSTOM) {
            return HEUNGBU_NOLBU;
        }

        return switch (preference) {
            case WARM_HUG -> HEUNGBU_NOLBU;
            case FUN_ADVENTURE, DAILY_LIFE -> TIGER_AND_PERSIMMON;
            case CUSTOM -> HEUNGBU_NOLBU;
        };
    }

    /**
     * customStoryPreference 텍스트 기반 전래동화 추천
     *
     * 현재 운영 기준:
     * - 포근/따뜻/사랑 계열 → 흥부와 놀부
     * - 그 외 직접 입력 → 호랑이와 곶감
     */
    public static TraditionalTale findByCustomText(String customText) {
        if (customText == null || customText.isBlank()) {
            return HEUNGBU_NOLBU;
        }

        String text = customText.toLowerCase();

        if (text.contains("포근") || text.contains("안아") || text.contains("따뜻") ||
                text.contains("사랑")) {
            return HEUNGBU_NOLBU;
        }

        return TIGER_AND_PERSIMMON;
    }

    // 제목으로 전래동화 찾기
    public static TraditionalTale findByTitle(String title) {
        if (title == null || title.isBlank()) {
            return CUSTOM;
        }

        return Arrays.stream(values())
                .filter(tale -> tale.getTitle().equals(title))
                .findFirst()
                .orElse(CUSTOM);
    }
}
