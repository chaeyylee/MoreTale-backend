package com.moretale.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 언어 Enum + Custom 입력 복합 검증 어노테이션
 *
 * 적용 규칙:
 * - firstLanguage == OTHER  → customFirstLanguage 필수 (null/blank 불가)
 * - firstLanguage != OTHER  → customFirstLanguage 무시 (자동 null 처리)
 * - secondLanguage 동일하게 적용
 * - familyStructure == CUSTOM  → customFamilyStructure 필수
 * - storyPreference == CUSTOM  → customStoryPreference 필수
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = LanguageInputValidator.class)
@Documented
public @interface ValidLanguageInput {
    String message() default "언어 입력이 올바르지 않습니다.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
