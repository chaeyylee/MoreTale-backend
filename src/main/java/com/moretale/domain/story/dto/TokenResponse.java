package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.StoryToken;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "동화 단어 토큰 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenResponse {

    @Schema(description = "토큰 고유 ID (단어 저장 시 사용될 식별자)", example = "242")
    private Long id;

    @Schema(description = "원문 단어 텍스트", example = "우주복")
    private String text;

    @Schema(description = "하이라이트 여부", example = "true")
    private Boolean highlight;

    @Schema(
            description = "번역어 / 제2언어 단어 (highlight=true인 경우에만 반환, 아니면 null)",
            nullable = true,
            example = "bộ đồ phi hành gia"
    )
    private String translation;

    @Schema(
            description = "제1언어 뜻 설명 (highlight=true인 경우에만 반환, 아니면 null)",
            nullable = true,
            example = "우주에서 입는 특별한 옷"
    )
    private String definition;

    @Schema(
            description = "제2언어 뜻 설명 (highlight=true인 경우에만 반환, 아니면 null)",
            nullable = true,
            example = "áo đặc biệt mặc khi bay vào không gian"
    )
    private String secondaryDefinition;

    @Schema(
            description = "단어별 TTS URL. 현재 백엔드에서는 생성하지 않으며, 프론트에서 브라우저 TTS(Web Speech API)로 처리합니다.",
            nullable = true,
            example = ""
    )
    private String audioUrl;

    @Schema(description = "제1언어 ISO 코드", example = "ko")
    private String sourceLanguage;

    @Schema(
            description = "제2언어 ISO 코드 (highlight=true인 경우에만 반환, 아니면 null)",
            nullable = true,
            example = "vi"
    )
    private String targetLanguage;

    // Entity -> DTO 변환 메서드
    public static TokenResponse from(StoryToken token) {
        return TokenResponse.builder()
                .id(token.getTokenId())
                .text(token.getText())
                .highlight(token.getHighlight())
                .translation(token.getHighlight() ? token.getTranslation() : null)
                .definition(token.getHighlight() ? token.getDefinition() : null)
                .secondaryDefinition(token.getHighlight() ? token.getSecondaryDefinition() : null)
                .audioUrl(token.getHighlight() ? token.getAudioUrl() : null)
                .sourceLanguage(token.getSourceLanguage())
                .targetLanguage(token.getHighlight() ? token.getTargetLanguage() : null)
                .build();
    }
}
