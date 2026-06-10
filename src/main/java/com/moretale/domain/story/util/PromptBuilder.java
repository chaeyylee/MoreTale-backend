package com.moretale.domain.story.util;

import com.moretale.domain.profile.entity.AgeGroup;
import com.moretale.domain.profile.entity.LanguageProficiency;
import com.moretale.domain.profile.entity.StoryPreference;
import com.moretale.domain.story.dto.StoryGenerateRequest;
import com.moretale.domain.story.enums.TraditionalTale;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * AI 프롬프트 자동 조립 유틸리티
 * - 온보딩 데이터 + 사용자 입력을 통합하여 최적화된 프롬프트 생성
 */
@Slf4j
@UtilityClass
public class PromptBuilder {

    // 통합 프롬프트 생성
    public static String buildPrompt(
            StoryGenerateRequest request,
            String childName,
            String primaryLang,
            String secondaryLang
    ) {
        StringBuilder prompt = new StringBuilder();

        // 1. 기본 동화 주제
        prompt.append("## 동화 주제\n");
        prompt.append(request.getPrompt()).append("\n\n");

        // 2. 추천 전래동화가 있으면 추가
        if (request.getRecommendedTaleTitle() != null) {
            TraditionalTale tale = TraditionalTale.findByTitle(request.getRecommendedTaleTitle());
            prompt.append("## 전래동화 기반\n");
            prompt.append("- 제목: ").append(tale.getTitle()).append("\n");
            prompt.append("- 설명: ").append(tale.getDescription()).append("\n\n");
        }

        // 3. 아이 정보
        prompt.append("## 주인공 정보\n");
        prompt.append("- 이름: ").append(childName).append("\n");
        if (request.getChildAge() != null) {
            prompt.append("- 나이: ").append(request.getChildAge()).append("세\n");
        }
        prompt.append("\n");

        // 4. 언어 및 난이도 제약 조건
        prompt.append("## 언어 설정\n");
        prompt.append("- 기본 언어: ").append(primaryLang).append("\n");
        prompt.append("- 보조 언어: ").append(secondaryLang).append("\n\n");

        // 5. 난이도 조정 (숙련도 기반)
        prompt.append("## 난이도 가이드\n");
        prompt.append(buildDifficultyGuide(request)).append("\n\n");

        // 6. 이야기 스타일
        if (request.getStoryPreference() != null &&
                request.getStoryPreference() != StoryPreference.CUSTOM) {
            prompt.append("## 이야기 스타일\n");
            prompt.append("- 선호: ").append(request.getStoryPreference().getDescription()).append("\n\n");
        }

        // 7. 출력 형식 지정
        prompt.append("## 출력 요구사항\n");
        prompt.append("- 총 5개의 장면(슬라이드)으로 구성\n");
        prompt.append("- 각 장면마다 ").append(primaryLang).append("와 ").append(secondaryLang)
                .append(" 이중언어로 작성\n");
        prompt.append("- 아이의 나이와 언어 수준에 맞는 단어와 문장 사용\n");
        prompt.append("- 각 장면은 이미지로 표현 가능하도록 시각적 묘사 포함\n");

        String finalPrompt = prompt.toString();
        log.info("생성된 프롬프트:\n{}", finalPrompt);

        return finalPrompt;
    }

    // 언어 숙련도에 따른 난이도 가이드 생성
    private static String buildDifficultyGuide(StoryGenerateRequest request) {
        StringBuilder guide = new StringBuilder();

        LanguageProficiency primaryProf = request.getFirstLanguageProficiency();
        LanguageProficiency secondaryProf = request.getSecondLanguageProficiency();

        // 기본 언어 난이도
        if (primaryProf != null) {
            guide.append("기본 언어 수준: ").append(mapProficiencyToLevel(primaryProf)).append("\n");
        }

        // 보조 언어 난이도
        if (secondaryProf != null) {
            guide.append("보조 언어 수준: ").append(mapProficiencyToLevel(secondaryProf)).append("\n");
        }

        return guide.toString();
    }

    // LanguageProficiency Enum을 구체적인 난이도 설명으로 변환
    private static String mapProficiencyToLevel(LanguageProficiency proficiency) {
        switch (proficiency) {
            case EGG:
                return "초급 (짧은 단어, 1-2개 단어 문장, 반복 많음)";
            case LARVA:
                return "초중급 (간단한 문장 3-5단어, 기본 동사)";
            case PUPA:
                return "중급 (복합 문장 5-8단어, 다양한 어휘)";
            case BEE:
                return "고급 (긴 문장 8-12단어, 풍부한 표현)";
            default:
                return "중급";
        }
    }

    // 나이 그룹에 따른 문장 복잡도 가이드
    public static String getComplexityGuide(AgeGroup ageGroup) {
        if (ageGroup == null) {
            return "적절한 수준";
        }

        switch (ageGroup) {
            case AGE_3_4:
                return "매우 간단한 문장 (3-5단어), 반복적 표현";
            case AGE_5_6:
                return "간단한 문장 (5-7단어), 기본 어휘";
            case AGE_7_8:
                return "보통 문장 (7-10단어), 다양한 어휘";
            case AGE_9_10:
                return "복잡한 문장 (10-15단어), 풍부한 표현";
            default:
                return "적절한 수준";
        }
    }
}
