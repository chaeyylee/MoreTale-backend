package com.moretale.domain.profile.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(description = "지원 언어 (KO ~ OTHER). OTHER 선택 시 customLanguage 필드에 직접 입력 필요")
@Getter
@RequiredArgsConstructor
public enum Language {

    KO("한국어", "ko"),
    EN("영어", "en"),
    JA("일본어", "ja"),
    ZH("중국어", "zh"),
    ES("스페인어", "es"),
    VI("베트남어", "vi"),
    OTHER("기타", null);

    private final String description;
    private final String isoCode;

    /**
     * 하위 호환성: 기존 String 코드 → Language Enum 변환
     * ex) "ko" → KO, "vi" → VI, 매핑 불가 시 OTHER 반환
     */
    public static Language fromIsoCode(String isoCode) {
        if (isoCode == null) return OTHER;
        for (Language lang : values()) {
            if (isoCode.equalsIgnoreCase(lang.isoCode)) {
                return lang;
            }
        }
        return OTHER;
    }

    /**
     * primary/secondary 동기화용: 실제 저장할 언어 코드 문자열 반환
     * - OTHER면 customValue(직접 입력값)를 반환
     * - 그 외에는 name() 반환 (ex. "KO", "EN")
     */
    public String resolveCode(String customValue) {
        if (this == OTHER) {
            return customValue != null ? customValue : "OTHER";
        }
        return this.name();
    }
}
