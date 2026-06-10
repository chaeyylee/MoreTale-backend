package com.moretale.global.validation;

import com.moretale.domain.profile.entity.FamilyStructure;
import com.moretale.domain.profile.entity.Language;
import com.moretale.domain.profile.entity.StoryPreference;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * @ValidLanguageInput 실제 검증 로직
 *
 * ConstraintValidator<어노테이션, 검증 대상 타입>
 * → DTO 클래스 레벨에 적용
 */
public class LanguageInputValidator implements ConstraintValidator<ValidLanguageInput, Object> {

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;

        // Reflection 없이 인터페이스로 접근하기 위해 LanguageValidatable 인터페이스 사용
        if (!(value instanceof LanguageValidatable target)) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        // 1. firstLanguage == OTHER → customFirstLanguage 필수
        if (Language.OTHER.equals(target.getFirstLanguage())) {
            if (isBlank(target.getCustomFirstLanguage())) {
                context.buildConstraintViolationWithTemplate(
                        "'기타' 선택 시 첫 번째 언어를 직접 입력해주세요."
                ).addPropertyNode("customFirstLanguage").addConstraintViolation();
                valid = false;
            }
        }

        // 2. secondLanguage == OTHER → customSecondLanguage 필수
        if (Language.OTHER.equals(target.getSecondLanguage())) {
            if (isBlank(target.getCustomSecondLanguage())) {
                context.buildConstraintViolationWithTemplate(
                        "'기타' 선택 시 두 번째 언어를 직접 입력해주세요."
                ).addPropertyNode("customSecondLanguage").addConstraintViolation();
                valid = false;
            }
        }

        // 3. familyStructure == CUSTOM → customFamilyStructure 필수
        if (FamilyStructure.CUSTOM.equals(target.getFamilyStructure())) {
            if (isBlank(target.getCustomFamilyStructure())) {
                context.buildConstraintViolationWithTemplate(
                        "'직접 작성' 선택 시 가족 구조를 입력해주세요."
                ).addPropertyNode("customFamilyStructure").addConstraintViolation();
                valid = false;
            }
        }

        // 4. storyPreference == CUSTOM → customStoryPreference 필수
        if (StoryPreference.CUSTOM.equals(target.getStoryPreference())) {
            if (isBlank(target.getCustomStoryPreference())) {
                context.buildConstraintViolationWithTemplate(
                        "'직접 적을래요' 선택 시 이야기 선호도를 입력해주세요."
                ).addPropertyNode("customStoryPreference").addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
