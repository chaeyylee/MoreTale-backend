package com.moretale.domain.profile.dto;

import com.moretale.domain.profile.entity.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OnboardingProfileRequest Validation 테스트")
class OnboardingProfileRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("정상 입력 (일반 언어) - 검증 통과")
    void valid_normalLanguage() {
        OnboardingProfileRequest request = validBuilder().build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("정상 입력 (OTHER 언어 + custom 값) - 검증 통과")
    void valid_otherLanguageWithCustom() {
        OnboardingProfileRequest request = validBuilder()
                .firstLanguage(Language.OTHER)
                .customFirstLanguage("태국어")
                .secondLanguage(Language.OTHER)
                .customSecondLanguage("힌디어")
                .build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CUSTOM familyStructure + customFamilyStructure 있음 - 검증 통과")
    void valid_customFamilyStructure() {
        OnboardingProfileRequest request = validBuilder()
                .familyStructure(FamilyStructure.CUSTOM)
                .customFamilyStructure("조부모님과 살아요")
                .build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("CUSTOM storyPreference + customStoryPreference 있음 - 검증 통과")
    void valid_customStoryPreference() {
        OnboardingProfileRequest request = validBuilder()
                .storyPreference(StoryPreference.CUSTOM)
                .customStoryPreference("우주 탐험 이야기")
                .build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("childName null - 검증 실패")
    void invalid_childNameNull() {
        OnboardingProfileRequest request = validBuilder().childName(null).build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("childName"));
    }

    @Test
    @DisplayName("childName blank - 검증 실패")
    void invalid_childNameBlank() {
        OnboardingProfileRequest request = validBuilder().childName("   ").build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("childName"));
    }

    @Test
    @DisplayName("ageGroup null - 검증 실패")
    void invalid_ageGroupNull() {
        OnboardingProfileRequest request = validBuilder().ageGroup(null).build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("ageGroup"));
    }

    @Test
    @DisplayName("firstLanguage null - 검증 실패")
    void invalid_firstLanguageNull() {
        OnboardingProfileRequest request = validBuilder().firstLanguage(null).build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstLanguage"));
    }

    @Test
    @DisplayName("secondLanguage null - 검증 실패")
    void invalid_secondLanguageNull() {
        OnboardingProfileRequest request = validBuilder().secondLanguage(null).build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("secondLanguage"));
    }

    @Test
    @DisplayName("firstLanguageProficiency null - 검증 실패")
    void invalid_firstLanguageProficiencyNull() {
        OnboardingProfileRequest request = validBuilder().firstLanguageProficiency(null).build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("firstLanguageProficiency"));
    }

    @Test
    @DisplayName("familyStructure null - 검증 실패")
    void invalid_familyStructureNull() {
        OnboardingProfileRequest request = validBuilder().familyStructure(null).build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("familyStructure"));
    }

    @Test
    @DisplayName("storyPreference null - 검증 실패")
    void invalid_storyPreferenceNull() {
        OnboardingProfileRequest request = validBuilder().storyPreference(null).build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("storyPreference"));
    }

    @Test
    @DisplayName("firstLanguage=OTHER + customFirstLanguage null - 검증 실패")
    void invalid_otherFirstLanguageWithoutCustom() {
        OnboardingProfileRequest request = validBuilder()
                .firstLanguage(Language.OTHER)
                .customFirstLanguage(null)
                .build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("customFirstLanguage"));
    }

    @Test
    @DisplayName("firstLanguage=OTHER + customFirstLanguage blank - 검증 실패")
    void invalid_otherFirstLanguageWithBlankCustom() {
        OnboardingProfileRequest request = validBuilder()
                .firstLanguage(Language.OTHER)
                .customFirstLanguage("   ")
                .build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("customFirstLanguage"));
    }

    @Test
    @DisplayName("secondLanguage=OTHER + customSecondLanguage null - 검증 실패")
    void invalid_otherSecondLanguageWithoutCustom() {
        OnboardingProfileRequest request = validBuilder()
                .secondLanguage(Language.OTHER)
                .customSecondLanguage(null)
                .build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("customSecondLanguage"));
    }

    @Test
    @DisplayName("familyStructure=CUSTOM + customFamilyStructure null - 검증 실패")
    void invalid_customFamilyStructureWithoutValue() {
        OnboardingProfileRequest request = validBuilder()
                .familyStructure(FamilyStructure.CUSTOM)
                .customFamilyStructure(null)
                .build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("customFamilyStructure"));
    }

    @Test
    @DisplayName("storyPreference=CUSTOM + customStoryPreference null - 검증 실패")
    void invalid_customStoryPreferenceWithoutValue() {
        OnboardingProfileRequest request = validBuilder()
                .storyPreference(StoryPreference.CUSTOM)
                .customStoryPreference(null)
                .build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("customStoryPreference"));
    }

    @Test
    @DisplayName("childName null - 에러 메시지 확인")
    void errorMessage_childNameNull() {
        OnboardingProfileRequest request = validBuilder().childName(null).build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("childName")
                        && v.getMessage().equals("아이 이름은 필수입니다."));
    }

    @Test
    @DisplayName("OTHER + custom 누락 - 에러 메시지 확인")
    void errorMessage_otherLanguageWithoutCustom() {
        OnboardingProfileRequest request = validBuilder()
                .firstLanguage(Language.OTHER)
                .customFirstLanguage(null)
                .build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("customFirstLanguage")
                        && v.getMessage().contains("기타"));
    }

    @Test
    @DisplayName("childName 50자 초과 - 검증 실패")
    void invalid_childNameTooLong() {
        OnboardingProfileRequest request = validBuilder()
                .childName("a".repeat(51))
                .build();
        Set<ConstraintViolation<OnboardingProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("childName"));
    }

    private OnboardingProfileRequest.OnboardingProfileRequestBuilder validBuilder() {
        return OnboardingProfileRequest.builder()
                .childName("민준")
                .ageGroup(AgeGroup.AGE_5_6)
                .firstLanguage(Language.KO)
                .customFirstLanguage(null)
                .secondLanguage(Language.VI)
                .customSecondLanguage(null)
                .firstLanguageProficiency(LanguageProficiency.BEE)
                .secondLanguageProficiency(LanguageProficiency.LARVA)
                .firstLanguageListening(LanguageProficiency.BEE)
                .firstLanguageSpeaking(LanguageProficiency.PUPA)
                .secondLanguageListening(LanguageProficiency.LARVA)
                .secondLanguageSpeaking(LanguageProficiency.EGG)
                .familyStructure(FamilyStructure.TWO_PARENTS)
                .customFamilyStructure(null)
                .storyPreference(StoryPreference.FUN_ADVENTURE)
                .customStoryPreference(null);
    }
}
