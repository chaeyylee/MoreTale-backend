package com.moretale.domain.profile.util;

import com.moretale.domain.profile.entity.Language;
import com.moretale.domain.profile.entity.UserProfile;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Language Enum ↔ TTS locale 변환 유틸
 *
 * StoryService, TTSService 등에서 locale 문자열 조합 대신 이 클래스를 통해 항상 올바른 BCP-47 locale 코드를 얻도록 한다.
 *
 * 사용 예시:
 *   LanguageLocaleMapper.resolveTtsLocale(profile.getFirstLanguage(), null) → "ko-KR"
 *   LanguageLocaleMapper.resolveTtsLocale(Language.VI, null) → "vi-VN"
 *   LanguageLocaleMapper.resolveTtsLocale(Language.OTHER, "태국어") → null (TTS 미지원)
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LanguageLocaleMapper {

    /**
     * Language Enum → TTS locale 코드 반환
     *
     * @param language    Language Enum 값
     * @param customValue OTHER 선택 시 사용자 직접 입력값 (그 외엔 무시됨)
     * @return TTS locale 코드 (ex. "ko-KR", "vi-VN") 또는 null (OTHER / 미지원)
     */
    public static String resolveTtsLocale(Language language, String customValue) {
        if (language == null || language == Language.OTHER) {
            log.debug("TTS locale 변환 불가 - language={}, customValue={}", language, customValue);
            return null;
        }
        return language.getTtsLocale();
    }

    // UserProfile의 첫 번째 언어 → TTS locale 반환
    public static String resolvePrimaryTtsLocale(UserProfile profile) {
        return resolveTtsLocale(profile.getFirstLanguage(), profile.getCustomFirstLanguage());
    }

    // UserProfile의 두 번째 언어 → TTS locale 반환
    public static String resolveSecondaryTtsLocale(UserProfile profile) {
        return resolveTtsLocale(profile.getSecondLanguage(), profile.getCustomSecondLanguage());
    }

    // TTS locale → Language Enum 역변환
    // TTSService 검증 등에서 사용
    public static Language fromTtsLocale(String ttsLocale) {
        return Language.fromTtsLocale(ttsLocale);
    }

    // TTS 지원 여부 확인
    public static boolean isTtsSupported(Language language) {
        return language != null && language.isTtsSupported();
    }
}
