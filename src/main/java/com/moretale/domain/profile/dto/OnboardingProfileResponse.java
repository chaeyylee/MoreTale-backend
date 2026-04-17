package com.moretale.domain.profile.dto;

import com.moretale.domain.profile.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "온보딩 프로필 생성 응답 DTO")
public class OnboardingProfileResponse {

    private Long profileId;
    private Long userId;
    private String nickname;
    private String childName;
    private AgeGroup ageGroup;
    private Integer childAge;

    // 언어 (Enum + Custom + Display)
    @Schema(description = "첫 번째 언어 Enum", example = "KO")
    private Language firstLanguage;

    @Schema(description = "첫 번째 언어 직접 입력값 (OTHER 시)", example = "태국어")
    private String customFirstLanguage;

    @Schema(description = "첫 번째 언어 표시명", example = "한국어")
    private String firstLanguageDisplay;

    @Schema(description = "두 번째 언어 Enum", example = "VI")
    private Language secondLanguage;

    @Schema(description = "두 번째 언어 직접 입력값 (OTHER 시)")
    private String customSecondLanguage;

    @Schema(description = "두 번째 언어 표시명", example = "베트남어")
    private String secondLanguageDisplay;

    // 숙련도
    private LanguageProficiency firstLanguageProficiency;
    private LanguageProficiency secondLanguageProficiency;
    private LanguageProficiency firstLanguageListening;
    private LanguageProficiency firstLanguageSpeaking;
    private LanguageProficiency secondLanguageListening;
    private LanguageProficiency secondLanguageSpeaking;

    // 가족 구조
    private FamilyStructure familyStructure;
    private String customFamilyStructure;

    // 이야기 선호도
    private StoryPreference storyPreference;
    private String customStoryPreference;

    // 부가 정보
    private String childNationality;
    private String parentCountry;

    // 생성/수정 시각
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static OnboardingProfileResponse fromEntity(UserProfile profile) {
        if (profile == null || profile.getUser() == null) {
            throw new IllegalArgumentException("Profile or User cannot be null");
        }

        return OnboardingProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUser().getUserId())
                .nickname(profile.getUser().getNickname())
                .childName(profile.getChildName())
                .ageGroup(profile.getAgeGroup())
                .childAge(profile.getChildAge())
                // 언어 Enum + Custom + Display
                .firstLanguage(profile.getFirstLanguage())
                .customFirstLanguage(profile.getCustomFirstLanguage())
                .firstLanguageDisplay(profile.getFirstLanguageDisplay())
                .secondLanguage(profile.getSecondLanguage())
                .customSecondLanguage(profile.getCustomSecondLanguage())
                .secondLanguageDisplay(profile.getSecondLanguageDisplay())
                // 숙련도
                .firstLanguageProficiency(profile.getFirstLanguageProficiency())
                .secondLanguageProficiency(profile.getSecondLanguageProficiency())
                .firstLanguageListening(profile.getFirstLanguageListening())
                .firstLanguageSpeaking(profile.getFirstLanguageSpeaking())
                .secondLanguageListening(profile.getSecondLanguageListening())
                .secondLanguageSpeaking(profile.getSecondLanguageSpeaking())
                // 가족/이야기
                .familyStructure(profile.getFamilyStructure())
                .customFamilyStructure(profile.getCustomFamilyStructure())
                .storyPreference(profile.getStoryPreference())
                .customStoryPreference(profile.getCustomStoryPreference())
                // 부가
                .childNationality(profile.getChildNationality())
                .parentCountry(profile.getParentCountry())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
