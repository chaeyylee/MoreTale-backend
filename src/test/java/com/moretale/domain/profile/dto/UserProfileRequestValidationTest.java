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

@DisplayName("UserProfileRequest Validation 테스트")
class UserProfileRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("정상 입력 - 검증 통과")
    void valid_normalInput() {
        UserProfileRequest request = validBuilder().build();
        Set<ConstraintViolation<UserProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("OTHER 언어 + custom 값 정상 입력 - 검증 통과")
    void valid_otherLanguageWithCustom() {
        UserProfileRequest request = validBuilder()
                .firstLanguage(Language.OTHER)
                .customFirstLanguage("태국어")
                .build();
        Set<ConstraintViolation<UserProfileRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("childName null - 검증 실패")
    void invalid_childNameNull() {
        UserProfileRequest request = validBuilder().childName(null).build();
        Set<ConstraintViolation<UserProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("childName"));
    }

    @Test
    @DisplayName("firstLanguage null - 검증 실패")
    void invalid_firstLanguageNull() {
        UserProfileRequest request = validBuilder().firstLanguage(null).build();
        Set<ConstraintViolation<UserProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstLanguage"));
    }

    @Test
    @DisplayName("secondLanguage null - 검증 실패")
    void invalid_secondLanguageNull() {
        UserProfileRequest request = validBuilder().secondLanguage(null).build();
        Set<ConstraintViolation<UserProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("secondLanguage"));
    }

    @Test
    @DisplayName("firstLanguageProficiency null - 검증 실패")
    void invalid_firstLanguageProficiencyNull() {
        UserProfileRequest request = validBuilder().firstLanguageProficiency(null).build();
        Set<ConstraintViolation<UserProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("firstLanguageProficiency"));
    }

    @Test
    @DisplayName("ageGroup null - 검증 실패")
    void invalid_ageGroupNull() {
        UserProfileRequest request = validBuilder().ageGroup(null).build();
        Set<ConstraintViolation<UserProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("ageGroup"));
    }

    @Test
    @DisplayName("firstLanguage=OTHER + customFirstLanguage null - 검증 실패")
    void invalid_otherLanguageWithoutCustom() {
        UserProfileRequest request = validBuilder()
                .firstLanguage(Language.OTHER)
                .customFirstLanguage(null)
                .build();
        Set<ConstraintViolation<UserProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("customFirstLanguage"));
    }

    @Test
    @DisplayName("familyStructure=CUSTOM + customFamilyStructure null - 검증 실패")
    void invalid_customFamilyStructureNull() {
        UserProfileRequest request = validBuilder()
                .familyStructure(FamilyStructure.CUSTOM)
                .customFamilyStructure(null)
                .build();
        Set<ConstraintViolation<UserProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("customFamilyStructure"));
    }

    @Test
    @DisplayName("storyPreference=CUSTOM + customStoryPreference null - 검증 실패")
    void invalid_customStoryPreferenceNull() {
        UserProfileRequest request = validBuilder()
                .storyPreference(StoryPreference.CUSTOM)
                .customStoryPreference(null)
                .build();
        Set<ConstraintViolation<UserProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("customStoryPreference"));
    }

    @Test
    @DisplayName("customFirstLanguage 100자 초과 - 검증 실패")
    void invalid_customFirstLanguageTooLong() {
        UserProfileRequest request = validBuilder()
                .firstLanguage(Language.OTHER)
                .customFirstLanguage("a".repeat(101))
                .build();
        Set<ConstraintViolation<UserProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("customFirstLanguage"));
    }

    @Test
    @DisplayName("familyStructure null - 검증 실패")
    void invalid_familyStructureNull() {
        UserProfileRequest request = validBuilder().familyStructure(null).build();
        Set<ConstraintViolation<UserProfileRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("familyStructure"));
    }

    private UserProfileRequest.UserProfileRequestBuilder validBuilder() {
        return UserProfileRequest.builder()
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
