package com.moretale.domain.story.dto;

import com.moretale.domain.profile.entity.*;
import com.moretale.domain.profile.entity.UserProfile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 온보딩 데이터 기반 동화 생성 초기값 응답 DTO
// GET /api/stories/init 엔드포인트에서 사용
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryInitResponse {

    private Long profileId;
    private String childName;
    private String firstLanguage;
    private String secondLanguage;
    private AgeGroup ageGroup;
    private Integer childAge;

    // 언어 숙련도
    private LanguageProficiency firstLanguageProficiency;
    private LanguageProficiency secondLanguageProficiency;

    // 세분화된 언어 능력
    private LanguageProficiency firstLanguageListening;
    private LanguageProficiency firstLanguageSpeaking;
    private LanguageProficiency secondLanguageListening;
    private LanguageProficiency secondLanguageSpeaking;

    // 이야기 선호도
    private StoryPreference storyPreference;
    private String customStoryPreference;

    // 가족 구조
    private FamilyStructure familyStructure;
    private String customFamilyStructure;

    // 추천 전래동화 제목 (자동 매핑)
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
