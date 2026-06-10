package com.moretale.domain.story.dto;

import com.moretale.domain.profile.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

// 온보딩 데이터 기반 동화 생성 초기값 응답 DTO
// GET /api/stories/init 엔드포인트에서 사용
@Schema(description = "동화 생성 초기값 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryInitResponse {

    @Schema(description = "프로필 ID", example = "1")
    private Long profileId;

    @Schema(description = "주인공 아이 이름", example = "민준")
    private String childName;

    /**
     * 제1언어 표시명 (UI 표시 전용)
     * 필드명을 firstLanguageDisplay로 변경하여 display 값임을 명확히 함.
     * 기존 firstLanguage 필드명은 value처럼 보여 혼동을 유발했음.
     */
    @Schema(
            description = "제1언어 표시명 (UI 표시 전용, ex. 한국어, 베트남어, 태국어)",
            example = "한국어"
    )
    private String firstLanguageDisplay;

    @Schema(
            description = "제2언어 표시명 (UI 표시 전용, ex. 베트남어, 영어)",
            example = "베트남어"
    )
    private String secondLanguageDisplay;

    @Schema(description = "나이 그룹", example = "AGE_5_6")
    private AgeGroup ageGroup;

    @Schema(description = "실제 나이", example = "5")
    private Integer childAge;

    @Schema(description = "제1언어 전체 숙련도", example = "BEE")
    private LanguageProficiency firstLanguageProficiency;

    @Schema(description = "제2언어 전체 숙련도", example = "LARVA")
    private LanguageProficiency secondLanguageProficiency;

    @Schema(description = "제1언어 듣기 능력", example = "BEE")
    private LanguageProficiency firstLanguageListening;

    @Schema(description = "제1언어 말하기 능력", example = "PUPA")
    private LanguageProficiency firstLanguageSpeaking;

    @Schema(description = "제2언어 듣기 능력", example = "LARVA")
    private LanguageProficiency secondLanguageListening;

    @Schema(description = "제2언어 말하기 능력", example = "EGG")
    private LanguageProficiency secondLanguageSpeaking;

    @Schema(description = "이야기 선호도", example = "FUN_ADVENTURE")
    private StoryPreference storyPreference;

    @Schema(description = "이야기 선호도 직접 입력 (CUSTOM 시)", example = "우주 탐험 이야기")
    private String customStoryPreference;

    @Schema(description = "가족 구조", example = "TWO_PARENTS")
    private FamilyStructure familyStructure;

    @Schema(description = "가족 구조 직접 입력 (CUSTOM 시)", example = "조부모님과 살아요")
    private String customFamilyStructure;

    @Schema(description = "추천 전래동화 제목", example = "흥부와 놀부")
    private String recommendedTaleTitle;

    /**
     * 추천 전래동화와 일치하는 가장 최근 생성된 동화 ID
     *
     * - recommendedTaleTitle과 같은 title을 가진 동화 중 가장 최근 것
     * - profile_id 컬럼 추가 이전 데이터는 userId + title 기반 fallback으로 조회
     * - 해당 추천 동화를 아직 생성한 적이 없으면 null
     */
    @Schema(
            description = "추천 전래동화와 일치하는 가장 최근 생성된 동화 ID (없으면 null)",
            nullable = true,
            example = "42"
    )
    private Long storyId;

    /**
     * 추천 전래동화와 일치하는 가장 최근 생성된 동화의 표지 이미지 URL
     *
     * - recommendedTaleTitle과 같은 title을 가진 동화 기준
     * - 첫 번째 슬라이드(order=0)의 imageUrl 기준
     * - 해당 추천 동화를 아직 생성한 적이 없거나 슬라이드가 없으면 null
     */
    @Schema(
            description = "추천 전래동화와 일치하는 가장 최근 생성된 동화의 표지 이미지 URL (첫 번째 슬라이드 기준, 없으면 null)",
            nullable = true,
            example = "https://storage.example.com/images/slide1.png"
    )
    private String coverImageUrl;

    // 기존 from() - 하위 호환 유지 (storyId, coverImageUrl = null)
    public static StoryInitResponse from(UserProfile profile, String recommendedTale) {
        return from(profile, recommendedTale, null, null);
    }

    // storyId, coverImageUrl 포함
    public static StoryInitResponse from(
            UserProfile profile,
            String recommendedTale,
            Long storyId,
            String coverImageUrl
    ) {
        return StoryInitResponse.builder()
                .profileId(profile.getProfileId())
                .childName(profile.getChildName())
                .firstLanguageDisplay(profile.getFirstLanguageDisplay())
                .secondLanguageDisplay(profile.getSecondLanguageDisplay())
                .ageGroup(profile.getAgeGroup())
                .childAge(profile.getChildAge())
                .firstLanguageProficiency(profile.getFirstLanguageProficiency())
                .secondLanguageProficiency(profile.getSecondLanguageProficiency())
                .firstLanguageListening(profile.getFirstLanguageListening())
                .firstLanguageSpeaking(profile.getFirstLanguageSpeaking())
                .secondLanguageListening(profile.getSecondLanguageListening())
                .secondLanguageSpeaking(profile.getSecondLanguageSpeaking())
                .storyPreference(profile.getStoryPreference())
                .customStoryPreference(profile.getCustomStoryPreference())
                .familyStructure(profile.getFamilyStructure())
                .customFamilyStructure(profile.getCustomFamilyStructure())
                .recommendedTaleTitle(recommendedTale)
                .storyId(storyId)
                .coverImageUrl(coverImageUrl)
                .build();
    }
}
