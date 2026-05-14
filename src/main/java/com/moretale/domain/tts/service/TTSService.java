package com.moretale.domain.tts.service;

import com.moretale.domain.profile.entity.Language;
import com.moretale.domain.tts.dto.TTSRequest;
import com.moretale.domain.tts.dto.TTSResponse;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public interface TTSService {

    // TTS 생성
    TTSResponse generateTTS(TTSRequest request);

    // 텍스트 + locale 코드로 오디오 URL 생성 (StoryService 내부 사용)
    String generateAudioUrl(String text, String language);

    /**
     * TTS locale 유효성 검증
     *
     * Language Enum의 ttsLocale 값을 기반으로 지원 여부 판단.
     * Swagger 문서의 지원 예시와 실제 검증 로직을 Language Enum 단일 소스로 일치시킴.
     *
     * 지원: ko-KR, en-US, ja-JP, zh-CN, es-ES, vi-VN
     * 미지원: OTHER, null, 알 수 없는 코드
     */
    default void validateLanguage(String language) {
        Set<String> supportedLocales = Arrays.stream(Language.values())
                .filter(Language::isTtsSupported)
                .map(Language::getTtsLocale)
                .collect(Collectors.toSet());

        if (language == null || !supportedLocales.contains(language)) {
            throw new BusinessException(ErrorCode.TTS_INVALID_LANGUAGE);
        }
    }
}
