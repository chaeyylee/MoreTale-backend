package com.moretale.domain.profile.dto;

import com.moretale.domain.profile.entity.*;
import com.moretale.global.validation.LanguageValidatable;
import com.moretale.global.validation.ValidLanguageInput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

// 사용자 프로필 생성/수정 요청 DTO (일반 API용)
// OnboardingProfileRequest와 동일한 언어 입력 구조 적용
@ValidLanguageInput
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사용자 프로필 요청 DTO (생성/수정)")
public class UserProfileRequest implements LanguageValidatable {

    @NotBlank(message = "아이 이름은 필수입니다.")
    @Size(max = 50, message = "아이 이름은 50자 이하로 입력해주세요.")
    @Schema(description = "아이 이름", example = "민준")
    private String childName;

    @NotNull(message = "나이 그룹은 필수입니다.")
    @Schema(description = "나이 그룹", example = "AGE_5_6")
    private AgeGroup ageGroup;

    @NotNull(message = "아이 실제 나이는 필수입니다.")
    @Min(value = 0, message = "아이 나이는 0세 이상이어야 합니다.")
    @Max(value = 20, message = "아이 나이는 15세 이하여야 합니다.")
    @Schema(description = "아이 실제 나이", example = "5")
    private Integer childAge;

    @NotNull(message = "제1언어는 필수입니다.")
    @Schema(description = "제1언어 (KO/EN/JA/ZH/ES/VI/OTHER)", example = "KO")
    private Language firstLanguage;

    @Size(max = 100, message = "직접 입력 언어명은 100자 이하여야 합니다.")
    @Schema(description = "제1언어 직접 입력 (OTHER 선택 시 필수)", example = "태국어")
    private String customFirstLanguage;

    @NotNull(message = "제2언어는 필수입니다.")
    @Schema(description = "제2언어 (KO/EN/JA/ZH/ES/VI/OTHER)", example = "VI")
    private Language secondLanguage;

    @Size(max = 100, message = "직접 입력 언어명은 100자 이하여야 합니다.")
    @Schema(description = "제2언어 직접 입력 (OTHER 선택 시 필수)", example = "힌디어")
    private String customSecondLanguage;

    @NotNull(message = "제1언어 숙련도는 필수입니다.")
    @Schema(description = "제1언어 전체 숙련도 (EGG/LARVA/PUPA/BEE)", example = "BEE")
    private LanguageProficiency firstLanguageProficiency;

    @NotNull(message = "제2언어 숙련도는 필수입니다.")
    @Schema(description = "제2언어 전체 숙련도 (EGG/LARVA/PUPA/BEE)", example = "LARVA")
    private LanguageProficiency secondLanguageProficiency;

    @NotNull(message = "제1언어 듣기 능력은 필수입니다.")
    @Schema(description = "제1언어 듣기 능력 (EGG/LARVA/PUPA/BEE)", example = "BEE")
    private LanguageProficiency firstLanguageListening;

    @NotNull(message = "제1언어 말하기 능력은 필수입니다.")
    @Schema(description = "제1언어 말하기 능력 (EGG/LARVA/PUPA/BEE)", example = "BEE")
    private LanguageProficiency firstLanguageSpeaking;

    @NotNull(message = "제2언어 듣기 능력은 필수입니다.")
    @Schema(description = "제2언어 듣기 능력 (EGG/LARVA/PUPA/BEE)", example = "PUPA")
    private LanguageProficiency secondLanguageListening;

    @NotNull(message = "제2언어 말하기 능력은 필수입니다.")
    @Schema(description = "제2언어 말하기 능력 (EGG/LARVA/PUPA/BEE)", example = "LARVA")
    private LanguageProficiency secondLanguageSpeaking;

    @NotNull(message = "가족 구조는 필수입니다.")
    @Schema(
            description = "가족 구조 (ONE_PARENT/TWO_PARENTS/EXTENDED_FAMILY/SECRET/CUSTOM)",
            example = "TWO_PARENTS"
    )
    private FamilyStructure familyStructure;

    @Size(max = 200, message = "커스텀 가족 구조는 200자 이하로 입력해주세요.")
    @Schema(description = "커스텀 가족 구조 (CUSTOM 선택 시 필수)", example = "조부모님과 살아요")
    private String customFamilyStructure;

    @NotNull(message = "이야기 선호도는 필수입니다.")
    @Schema(
            description = "이야기 선호도 (WARM_HUG/FUN_ADVENTURE/DAILY_LIFE/CUSTOM)",
            example = "FUN_ADVENTURE"
    )
    private StoryPreference storyPreference;

    @Size(max = 200, message = "커스텀 이야기 선호도는 200자 이하로 입력해주세요.")
    @Schema(description = "커스텀 이야기 선호도 (CUSTOM 선택 시 필수)", example = "우주 탐험 이야기")
    private String customStoryPreference;

    @Size(max = 50)
    @Schema(description = "아이 국적", example = "KR")
    private String childNationality;

    @Size(max = 50)
    @Schema(description = "부모 거주 국가", example = "VN")
    private String parentCountry;

    @Deprecated
    @Schema(
            description = "[Deprecated] 사용 안 함 - firstLanguage로 대체",
            deprecated = true,
            example = "KO"
    )
    private String primaryLanguage;

    @Deprecated
    @Schema(
            description = "[Deprecated] 사용 안 함 - secondLanguage로 대체",
            deprecated = true,
            example = "VI"
    )
    private String secondaryLanguage;
}
