package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.StoryToken;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "동화 단어 토큰 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    @Schema(description = "토큰 고유 ID (단어 저장 시 사용될 식별자)", example = "242")
    private Long id;

    @Schema(description = "원문 단어 텍스트 (정규화된 형태)", example = "사자")
    private String text;

    @Schema(description = "하이라이트 여부", example = "true")
    private Boolean highlight;

    @Schema(
            description = "번역어 (highlight=true인 경우에만 반환, highlight=false이면 null)",
            nullable = true,
            example = "lion"
    )
    private String translation;

    @Schema(
            description = "뜻 설명 (highlight=true인 경우에만 반환, highlight=false이면 null)",
            nullable = true,
            example = "사자에 대한 설명입니다."
    )
    private String definition;

    @Schema(
            description = "단어 발음 오디오 URL (highlight=true인 경우에만 반환, highlight=false이면 null, wav 형식)",
            nullable = true,
            example = "https://storage.googleapis.com/moretale-ai-generated-project-640335ef-3b09-441e-a26/tts/audio/tts_ko-KR_12345678_abcd1234.wav"
    )
    private String audioUrl;

    @Schema(description = "원문 언어 ISO 코드", example = "ko")
    private String sourceLanguage;

    @Schema(
            description = "번역 언어 ISO 코드 (highlight=true인 경우에만 반환, highlight=false이면 null)",
            nullable = true,
            example = "en"
    )
    private String targetLanguage;

    /**
     * Entity -> DTO 변환 메서드
     */
    public static TokenResponse from(StoryToken token) {
        return TokenResponse.builder()
                .id(token.getTokenId())
                .text(token.getText())
                .highlight(token.getHighlight())
                .translation(token.getHighlight() ? token.getTranslation() : null)
                .definition(token.getHighlight() ? token.getDefinition() : null)
                .audioUrl(token.getHighlight() ? token.getAudioUrl() : null)
                .sourceLanguage(token.getSourceLanguage())
                .targetLanguage(token.getHighlight() ? token.getTargetLanguage() : null)
                .build();
    }
}
