package com.moretale.global.validation;

import com.moretale.domain.profile.entity.FamilyStructure;
import com.moretale.domain.profile.entity.Language;
import com.moretale.domain.profile.entity.StoryPreference;

// @ValidLanguageInput 검증에 필요한 필드 접근 인터페이스
// OnboardingProfileRequest / UserProfileRequest 양쪽에서 구현
public interface LanguageValidatable {
    Language getFirstLanguage();
    String getCustomFirstLanguage();
    Language getSecondLanguage();
    String getCustomSecondLanguage();
    FamilyStructure getFamilyStructure();
    String getCustomFamilyStructure();
    StoryPreference getStoryPreference();
    String getCustomStoryPreference();
}
