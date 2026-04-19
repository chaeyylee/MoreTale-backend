package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.StoryToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    // 토큰 고정 ID (단어 저장 시 사용될 식별자)
    private Long id;

    // 원문 단어 텍스트 (정규화된 형태, 예: "사자")
    private String text;

    // 하이라이트 여부
    private Boolean highlight;

    // 번역어 (highlight=true인 경우에만 존재)
    private String translation;

    // 뜻 설명 (highlight=true인 경우에만 존재)
    private String definition;

    // 단어 발음 오디오 URL (highlight=true인 경우에만 존재)
    private String audioUrl;

    // 원문 언어 코드 (예: "ko")
    private String sourceLanguage;

    // 번역 언어 코드 (예: "ja", "vi" 등 / highlight=true인 경우에만 존재)
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
