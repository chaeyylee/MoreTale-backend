package com.moretale.domain.vocabulary.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VocabularyCreateRequest {

    // 저장할 토큰 ID (StoryToken.tokenId)
    @NotNull(message = "tokenId는 필수입니다.")
    private Long tokenId;

    // 단어가 속한 슬라이드 ID
    @NotNull(message = "slideId는 필수입니다.")
    private Long slideId;

    // 단어가 속한 동화 ID
    @NotNull(message = "storyId는 필수입니다.")
    private Long storyId;

    private String word;
    private String translation;
    private String definition;
    private String sourceLanguage;
    private String targetLanguage;
}
