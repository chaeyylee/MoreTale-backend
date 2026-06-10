package com.moretale.domain.profile.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Language Enum 단위 테스트")
class LanguageEntityTest {

    // ────────────────── resolveCode ──────────────────

    @ParameterizedTest(name = "language={0} → resolveCode={1}")
    @CsvSource({
            "KO,  KO",
            "EN,  EN",
            "VI,  VI",
            "JA,  JA",
            "ZH,  ZH",
            "ES,  ES",
    })
    @DisplayName("resolveCode - 일반 언어는 name() 반환")
    void resolveCode_normalLanguage(String langStr, String expected) {
        Language lang = Language.valueOf(langStr);
        assertThat(lang.resolveCode(null)).isEqualTo(expected);
    }

    @Test
    @DisplayName("resolveCode - OTHER + custom 값 있으면 custom 반환")
    void resolveCode_other_withCustom() {
        assertThat(Language.OTHER.resolveCode("태국어")).isEqualTo("태국어");
    }

    @Test
    @DisplayName("resolveCode - OTHER + null → OTHER 문자열 반환")
    void resolveCode_other_nullCustom() {
        assertThat(Language.OTHER.resolveCode(null)).isEqualTo("OTHER");
    }

    // ────────────────── fromIsoCode ──────────────────

    @ParameterizedTest(name = "isoCode={0} → {1}")
    @CsvSource({
            "ko, KO",
            "en, EN",
            "vi, VI",
            "ja, JA",
            "zh, ZH",
            "es, ES",
            "KO, KO",  // 대소문자 무관
    })
    @DisplayName("fromIsoCode - ISO 코드로 Enum 변환")
    void fromIsoCode_success(String isoCode, String expected) {
        assertThat(Language.fromIsoCode(isoCode)).isEqualTo(Language.valueOf(expected));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"unknown", "th", "xx"})
    @DisplayName("fromIsoCode - 매핑 안 되면 OTHER 반환")
    void fromIsoCode_unknown_returnsOther(String isoCode) {
        assertThat(Language.fromIsoCode(isoCode)).isEqualTo(Language.OTHER);
    }

    // ────────────────── isTtsSupported ──────────────────

    @ParameterizedTest(name = "{0} → ttsSupported={1}")
    @CsvSource({
            "KO, true",
            "EN, true",
            "VI, true",
            "JA, true",
            "ZH, true",
            "ES, true",
            "OTHER, false",
    })
    @DisplayName("isTtsSupported - OTHER만 false")
    void isTtsSupported(String langStr, boolean expected) {
        Language lang = Language.valueOf(langStr);
        assertThat(lang.isTtsSupported()).isEqualTo(expected);
    }

    // ────────────────── ttsLocale ──────────────────

    @ParameterizedTest(name = "{0} → ttsLocale={1}")
    @CsvSource({
            "KO, ko-KR",
            "EN, en-US",
            "VI, vi-VN",
            "JA, ja-JP",
            "ZH, zh-CN",
            "ES, es-ES",
    })
    @DisplayName("ttsLocale - 각 언어별 BCP-47 코드 검증")
    void ttsLocale_perLanguage(String langStr, String expectedLocale) {
        Language lang = Language.valueOf(langStr);
        assertThat(lang.getTtsLocale()).isEqualTo(expectedLocale);
    }

    @Test
    @DisplayName("ttsLocale - OTHER는 null")
    void ttsLocale_other_isNull() {
        assertThat(Language.OTHER.getTtsLocale()).isNull();
    }
}
