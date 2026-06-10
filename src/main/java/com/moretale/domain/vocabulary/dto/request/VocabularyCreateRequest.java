package com.moretale.domain.vocabulary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "단어 저장 요청 DTO")
@Getter
@NoArgsConstructor
public class VocabularyCreateRequest {

    @NotNull(message = "tokenId는 필수입니다.")
    @Schema(description = "저장할 단어 토큰 ID (StoryToken.tokenId)", example = "42")
    private Long tokenId;

    @NotNull(message = "slideId는 필수입니다.")
    @Schema(description = "단어가 속한 슬라이드 ID", example = "10")
    private Long slideId;

    @NotNull(message = "storyId는 필수입니다.")
    @Schema(description = "단어가 속한 동화 ID", example = "5")
    private Long storyId;

    @Schema(description = "단어 원문", example = "사자")
    private String word;

    @Schema(description = "번역어", example = "sư tử")
    private String translation;

    @Schema(description = "뜻 설명", example = "갈기가 있는 큰 고양이과 동물")
    private String definition;

    @Schema(description = "원문 언어 코드", example = "ko")
    private String sourceLanguage;

    @Schema(description = "번역 대상 언어 코드", example = "vi")
    private String targetLanguage;
}
