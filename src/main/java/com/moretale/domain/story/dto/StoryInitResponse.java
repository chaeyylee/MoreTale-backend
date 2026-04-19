package com.moretale.domain.story.dto;

import com.moretale.domain.profile.entity.AgeGroup;
import com.moretale.domain.profile.entity.FamilyStructure;
import com.moretale.domain.profile.entity.LanguageProficiency;
import com.moretale.domain.profile.entity.StoryPreference;
import com.moretale.domain.profile.entity.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Schema(description = "제1언어 표시명", example = "한국어")
    private String firstLanguage;

    @Schema(description = "제2언어 표시명", example = "베트남어")
    private String secondLanguage;

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

    // UserProfile 엔티티로부터 초기값 생성
    public static StoryInitResponse from(UserProfile profile, String recommendedTale) {
        return StoryInitResponse.builder()
                .profileId(profile.getProfileId())
                .childName(profile.getChildName())
                .firstLanguage(profile.getFirstLanguageDisplay())
                .secondLanguage(profile.getSecondLanguageDisplay())
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
                .build();
    }
}
