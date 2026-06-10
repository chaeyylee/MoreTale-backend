package com.moretale.domain.profile.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "지원 언어 (KO ~ OTHER). OTHER 선택 시 customLanguage 필드에 직접 입력 필요")
@Getter
@RequiredArgsConstructor
public enum Language {

    KO("한국어", "ko", "ko-KR"),
    EN("영어",   "en", "en-US"),
    JA("일본어", "ja", "ja-JP"),
    ZH("중국어", "zh", "zh-CN"),
    ES("스페인어","es", "es-ES"),
    VI("베트남어","vi", "vi-VN"),
    OTHER("기타", null, null);

    private final String description;
    private final String isoCode;

    // TTS locale 코드 (Google TTS / Azure 등에서 사용하는 BCP-47 형식)
    // OTHER는 null → TTS 미지원 대상으로 처리
    private final String ttsLocale;

    // isoCode → Language Enum 변환
    // 매핑 불가 시 OTHER 반환
    public static Language fromIsoCode(String isoCode) {
        if (isoCode == null) return OTHER;
        for (Language lang : values()) {
            if (isoCode.equalsIgnoreCase(lang.isoCode)) {
                return lang;
            }
        }
        return OTHER;
    }

    // ttsLocale → Language Enum 변환 (TTSService 검증용)
    // 매핑 불가 시 OTHER 반환
    public static Language fromTtsLocale(String ttsLocale) {
        if (ttsLocale == null) return OTHER;
        for (Language lang : values()) {
            if (ttsLocale.equalsIgnoreCase(lang.ttsLocale)) {
                return lang;
            }
        }
        return OTHER;
    }

    // TTS 지원 여부 확인
    // OTHER는 ttsLocale이 null이므로 false 반환
    public boolean isTtsSupported() {
        return this.ttsLocale != null;
    }

    /**
     * Legacy 동기화용: primaryLanguage / secondaryLanguage 저장값 반환
     * - OTHER면 customValue(직접 입력값) 반환
     * - 그 외에는 name() 반환 (ex. "KO", "EN")
     */
    public String resolveCode(String customValue) {
        if (this == OTHER) {
            return customValue != null ? customValue : "OTHER";
        }
        return this.name();
    }
}
