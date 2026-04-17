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

    private Long profileId;
    private Long userId;
    private String nickname;
    private String childName;
    private Integer childAge;
    private AgeGroup ageGroup;

    // 언어 (Enum + Custom + Display)
    private Language firstLanguage;
    private String customFirstLanguage;
    private String firstLanguageDisplay;

    private Language secondLanguage;
    private String customSecondLanguage;
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

    private LocalDateTime createdAt;
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

                // 언어
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

                // 부가 정보
                .childNationality(profile.getChildNationality())
                .parentCountry(profile.getParentCountry())

                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
