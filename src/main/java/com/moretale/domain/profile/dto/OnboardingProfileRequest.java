package com.moretale.domain.profile.dto;

import com.moretale.domain.profile.entity.*;
import com.moretale.global.validation.LanguageValidatable;
import com.moretale.global.validation.ValidLanguageInput;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * 온보딩 프로필 생성 요청 DTO
 *
 * [언어 입력 구조]
 * - firstLanguage  : Language Enum (KO/EN/JA/ZH/ES/VI/OTHER)
 * - customFirst..  : OTHER 선택 시 필수, 그 외 무시
 *
 * [요청 예시 - 일반 언어 선택]
 * {
 *   "firstLanguage": "KO",
 *   "secondLanguage": "VI",
 *   ...
 * }
 *
 * [요청 예시 - 기타 언어 직접 입력]
 * {
 *   "firstLanguage": "OTHER",
 *   "customFirstLanguage": "태국어",
 *   "secondLanguage": "EN",
 *   ...
 * }
 */
@ValidLanguageInput  // Cross-field validation (언어 + custom 조합 검증)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "온보딩 프로필 생성 요청 DTO")
public class OnboardingProfileRequest implements LanguageValidatable {

    // 1단계: 주인공(아이) 소개
    @NotBlank(message = "아이 이름은 필수입니다.")
    @Size(max = 50, message = "아이 이름은 50자 이하여야 합니다.")
    @Schema(description = "아이 이름", example = "민준")
    private String childName;

    @NotNull(message = "아이 나이 그룹은 필수입니다.")
    @Schema(description = "나이 그룹 (AGE_0_2 / AGE_3_4 / AGE_5_6 / AGE_7_8 / AGE_9_10 / AGE_10_PLUS)",
            example = "AGE_5_6")
    private AgeGroup ageGroup;

    // 2단계: 주인공이 쓰는 말 (언어 선택)
    @NotNull(message = "첫 번째 언어는 필수입니다.")
    @Schema(description = "첫 번째 언어 (KO/EN/JA/ZH/ES/VI/OTHER)", example = "KO")
    private Language firstLanguage;

    @Size(max = 100, message = "직접 입력 언어명은 100자 이하여야 합니다.")
    @Schema(description = "첫 번째 언어 직접 입력 (firstLanguage=OTHER 시 필수)", example = "태국어")
    private String customFirstLanguage;

    @NotNull(message = "두 번째 언어는 필수입니다.")
    @Schema(description = "두 번째 언어 (KO/EN/JA/ZH/ES/VI/OTHER)", example = "VI")
    private Language secondLanguage;

    @Size(max = 100, message = "직접 입력 언어명은 100자 이하여야 합니다.")
    @Schema(description = "두 번째 언어 직접 입력 (secondLanguage=OTHER 시 필수)", example = "힌디어")
    private String customSecondLanguage;

    // 3단계: 언어 숙련도
    @NotNull(message = "첫 번째 언어 숙련도는 필수입니다.")
    @Schema(description = "첫 번째 언어 전체 숙련도 (EGG/LARVA/PUPA/BEE)", example = "BEE")
    private LanguageProficiency firstLanguageProficiency;

    @NotNull(message = "두 번째 언어 숙련도는 필수입니다.")
    @Schema(description = "두 번째 언어 전체 숙련도 (EGG/LARVA/PUPA/BEE)", example = "LARVA")
    private LanguageProficiency secondLanguageProficiency;

    @NotNull(message = "첫 번째 언어 듣기 숙련도는 필수입니다.")
    @Schema(description = "첫 번째 언어 듣기 능력 (EGG/LARVA/PUPA/BEE)", example = "BEE")
    private LanguageProficiency firstLanguageListening;

    @NotNull(message = "첫 번째 언어 말하기 숙련도는 필수입니다.")
    @Schema(description = "첫 번째 언어 말하기 능력 (EGG/LARVA/PUPA/BEE)", example = "PUPA")
    private LanguageProficiency firstLanguageSpeaking;

    @NotNull(message = "두 번째 언어 듣기 숙련도는 필수입니다.")
    @Schema(description = "두 번째 언어 듣기 능력 (EGG/LARVA/PUPA/BEE)", example = "LARVA")
    private LanguageProficiency secondLanguageListening;

    @NotNull(message = "두 번째 언어 말하기 숙련도는 필수입니다.")
    @Schema(description = "두 번째 언어 말하기 능력 (EGG/LARVA/PUPA/BEE)", example = "EGG")
    private LanguageProficiency secondLanguageSpeaking;

    // 4단계: 함께 사는 사람들
    @NotNull(message = "함께 사는 사람 정보는 필수입니다.")
    @Schema(description = "가족 구조 (ONE_PARENT/TWO_PARENTS/EXTENDED_FAMILY/SECRET/CUSTOM)",
            example = "TWO_PARENTS")
    private FamilyStructure familyStructure;

    @Size(max = 200, message = "가족 구조 직접 입력은 200자 이하여야 합니다.")
    @Schema(description = "가족 구조 직접 입력 (familyStructure=CUSTOM 시 필수)", example = "조부모님과 살아요")
    private String customFamilyStructure;

    // 5단계: 어떤 이야기가 좋아요
    @NotNull(message = "선호 이야기 유형은 필수입니다.")
    @Schema(description = "이야기 선호도 (WARM_HUG/FUN_ADVENTURE/DAILY_LIFE/CUSTOM)",
            example = "FUN_ADVENTURE")
    private StoryPreference storyPreference;

    @Size(max = 200, message = "이야기 선호도 직접 입력은 200자 이하여야 합니다.")
    @Schema(description = "이야기 선호도 직접 입력 (storyPreference=CUSTOM 시 필수)", example = "우주 탐험 이야기")
    private String customStoryPreference;

    // 부가 정보 (선택)
    @Size(max = 50, message = "아이 국적은 50자 이하여야 합니다.")
    @Schema(description = "아이 국적 (ISO 3166-1 alpha-2)", example = "KR")
    private String childNationality;

    @Size(max = 50, message = "부모 거주 국가는 50자 이하여야 합니다.")
    @Schema(description = "부모 거주 국가 (ISO 3166-1 alpha-2)", example = "VN")
    private String parentCountry;
}
