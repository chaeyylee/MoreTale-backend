package com.moretale.domain.profile.dto;

import com.moretale.domain.profile.entity.Language;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("LanguageUpdateRequest Validation 테스트")
class LanguageUpdateRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("일반 언어 정상 입력 - 검증 통과")
    void valid_normalLanguages() {
        LanguageUpdateRequest request = LanguageUpdateRequest.builder()
                .firstLanguage(Language.KO)
                .customFirstLanguage(null)
                .secondLanguage(Language.VI)
                .customSecondLanguage(null)
                .build();
        Set<ConstraintViolation<LanguageUpdateRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("OTHER + custom 있음 - 검증 통과")
    void valid_otherWithCustom() {
        LanguageUpdateRequest request = LanguageUpdateRequest.builder()
                .firstLanguage(Language.KO)
                .customFirstLanguage(null)
                .secondLanguage(Language.OTHER)
                .customSecondLanguage("태국어")
                .build();
        Set<ConstraintViolation<LanguageUpdateRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("firstLanguage null - 검증 실패")
    void invalid_firstLanguageNull() {
        LanguageUpdateRequest request = LanguageUpdateRequest.builder()
                .firstLanguage(null)
                .secondLanguage(Language.VI)
                .build();
        Set<ConstraintViolation<LanguageUpdateRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("firstLanguage"));
    }

    @Test
    @DisplayName("secondLanguage null - 검증 실패")
    void invalid_secondLanguageNull() {
        LanguageUpdateRequest request = LanguageUpdateRequest.builder()
                .firstLanguage(Language.KO)
                .secondLanguage(null)
                .build();
        Set<ConstraintViolation<LanguageUpdateRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("secondLanguage"));
    }

    @Test
    @DisplayName("OTHER + custom null - validate() 예외 발생")
    void invalid_otherWithoutCustom_validateThrows() {
        LanguageUpdateRequest request = LanguageUpdateRequest.builder()
                .firstLanguage(Language.OTHER)
                .customFirstLanguage(null)
                .secondLanguage(Language.VI)
                .customSecondLanguage(null)
                .build();
        assertThatThrownBy(request::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("기타");
    }

    @Test
    @DisplayName("secondLanguage=OTHER + custom blank - validate() 예외 발생")
    void invalid_secondOtherWithBlankCustom_validateThrows() {
        LanguageUpdateRequest request = LanguageUpdateRequest.builder()
                .firstLanguage(Language.KO)
                .customFirstLanguage(null)
                .secondLanguage(Language.OTHER)
                .customSecondLanguage("   ")
                .build();
        assertThatThrownBy(request::validate)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("기타");
    }

    @Test
    @DisplayName("customSecondLanguage 100자 초과 - 검증 실패")
    void invalid_customSecondLanguageTooLong() {
        LanguageUpdateRequest request = LanguageUpdateRequest.builder()
                .firstLanguage(Language.KO)
                .secondLanguage(Language.OTHER)
                .customSecondLanguage("a".repeat(101))
                .build();
        Set<ConstraintViolation<LanguageUpdateRequest>> violations = validator.validate(request);
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("customSecondLanguage"));
    }
}
