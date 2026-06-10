package com.moretale.domain.profile.dto;

import com.moretale.domain.profile.entity.AgeGroup;
import com.moretale.domain.profile.entity.FamilyStructure;
import com.moretale.domain.profile.entity.Language;
import com.moretale.domain.profile.entity.LanguageProficiency;
import com.moretale.domain.profile.entity.StoryPreference;
import com.moretale.domain.profile.entity.UserProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 프로필 응답 DTO")
public class UserProfileResponse {

    @Schema(description = "프로필 ID", example = "1")
    private Long profileId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "사용자 닉네임", example = "홍길동")
    private String nickname;

    @Schema(description = "아이 이름", example = "민준")
    private String childName;

    @Schema(description = "실제 나이", example = "5")
    private Integer childAge;

    @Schema(description = "나이 그룹", example = "AGE_5_6")
    private AgeGroup ageGroup;

    @Schema(description = "첫 번째 언어 Enum", example = "KO")
    private Language firstLanguage;

    @Schema(
            description = "첫 번째 언어 직접 입력값 (OTHER 선택 시에만 반환, 일반 언어 선택 시 null)",
            nullable = true
    )
    private String customFirstLanguage;

    @Schema(description = "첫 번째 언어 표시명", example = "한국어")
    private String firstLanguageDisplay;

    @Schema(description = "두 번째 언어 Enum", example = "VI")
    private Language secondLanguage;

    @Schema(
            description = "두 번째 언어 직접 입력값 (OTHER 선택 시에만 반환, 일반 언어 선택 시 null)",
            nullable = true
    )
    private String customSecondLanguage;

    @Schema(description = "두 번째 언어 표시명", example = "베트남어")
    private String secondLanguageDisplay;

    @Schema(description = "첫 번째 언어 전체 숙련도", example = "BEE")
    private LanguageProficiency firstLanguageProficiency;

    @Schema(description = "두 번째 언어 전체 숙련도", example = "LARVA")
    private LanguageProficiency secondLanguageProficiency;

    @Schema(description = "첫 번째 언어 듣기 능력", example = "BEE")
    private LanguageProficiency firstLanguageListening;

    @Schema(description = "첫 번째 언어 말하기 능력", example = "PUPA")
    private LanguageProficiency firstLanguageSpeaking;

    @Schema(description = "두 번째 언어 듣기 능력", example = "LARVA")
    private LanguageProficiency secondLanguageListening;

    @Schema(description = "두 번째 언어 말하기 능력", example = "EGG")
    private LanguageProficiency secondLanguageSpeaking;

    @Schema(description = "가족 구조", example = "TWO_PARENTS")
    private FamilyStructure familyStructure;

    @Schema(
            description = "가족 구조 직접 입력값 (CUSTOM 선택 시에만 반환, 일반 선택 시 null)",
            nullable = true
    )
    private String customFamilyStructure;

    @Schema(description = "이야기 선호도", example = "FUN_ADVENTURE")
    private StoryPreference storyPreference;

    @Schema(
            description = "이야기 선호도 직접 입력값 (CUSTOM 선택 시에만 반환, 일반 선택 시 null)",
            nullable = true
    )
    private String customStoryPreference;

    @Schema(description = "아이 국적", example = "KR")
    private String childNationality;

    @Schema(description = "부모 거주 국가", example = "VN")
    private String parentCountry;

    @Schema(description = "생성일시 (ISO 8601)", example = "2024-01-15T09:30:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시 (ISO 8601)", example = "2024-01-15T10:00:00Z")
    private LocalDateTime updatedAt;

    public static UserProfileResponse fromEntity(UserProfile profile) {
        if (profile == null || profile.getUser() == null) {
            throw new IllegalArgumentException("Profile or User cannot be null");
        }

        return UserProfileResponse.builder()
                .profileId(profile.getProfileId())
                .userId(profile.getUser().getUserId())
                .nickname(profile.getUser().getNickname())
                .childName(profile.getChildName())
                .childAge(profile.getChildAge())
                .ageGroup(profile.getAgeGroup())
                .firstLanguage(profile.getFirstLanguage())
                .customFirstLanguage(profile.getCustomFirstLanguage())
                .firstLanguageDisplay(profile.getFirstLanguageDisplay())
                .secondLanguage(profile.getSecondLanguage())
                .customSecondLanguage(profile.getCustomSecondLanguage())
                .secondLanguageDisplay(profile.getSecondLanguageDisplay())
                .firstLanguageProficiency(profile.getFirstLanguageProficiency())
                .secondLanguageProficiency(profile.getSecondLanguageProficiency())
                .firstLanguageListening(profile.getFirstLanguageListening())
                .firstLanguageSpeaking(profile.getFirstLanguageSpeaking())
                .secondLanguageListening(profile.getSecondLanguageListening())
                .secondLanguageSpeaking(profile.getSecondLanguageSpeaking())
                .familyStructure(profile.getFamilyStructure())
                .customFamilyStructure(profile.getCustomFamilyStructure())
                .storyPreference(profile.getStoryPreference())
                .customStoryPreference(profile.getCustomStoryPreference())
                .childNationality(profile.getChildNationality())
                .parentCountry(profile.getParentCountry())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
