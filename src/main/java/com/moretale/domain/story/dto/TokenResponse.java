package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.StoryToken;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

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

    // 번역 언어 코드 (예: "vi")
    private String targetLanguage;

    public static TokenResponse from(StoryToken token) {
        return TokenResponse.builder()
                .text(token.getText())
                .highlight(token.getHighlight())
                // highlight=false이면 null 반환 (응답 경량화)
                .translation(token.getHighlight() ? token.getTranslation() : null)
                .definition(token.getHighlight() ? token.getDefinition() : null)
                .audioUrl(token.getHighlight() ? token.getAudioUrl() : null)
                .sourceLanguage(token.getSourceLanguage())
                .targetLanguage(token.getHighlight() ? token.getTargetLanguage() : null)
                .build();
    }
}
