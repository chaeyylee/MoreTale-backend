package com.moretale.domain.story.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "AI가 생성한 슬라이드별 핵심 단어 항목")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabularyItem {

    @Schema(description = "항목 식별자 (AI 내부 ID)", example = "page-01-word-01")
    @JsonProperty("entryId")
    @JsonAlias("entry_id")
    private String entryId;

    @Schema(description = "제1언어 단어", example = "우주복")
    @JsonProperty("primaryWord")
    @JsonAlias("primary_word")
    private String primaryWord;

    @Schema(description = "제2언어 단어 (번역어)", example = "bộ đồ phi hành gia")
    @JsonProperty("secondaryWord")
    @JsonAlias("secondary_word")
    private String secondaryWord;

    @Schema(description = "제1언어 뜻 설명", example = "우주에서 입는 특별한 옷")
    @JsonProperty("primaryDefinition")
    @JsonAlias("primary_definition")
    private String primaryDefinition;

    @Schema(description = "제2언어 뜻 설명", example = "áo đặc biệt mặc khi bay vào không gian")
    @JsonProperty("secondaryDefinition")
    @JsonAlias("secondary_definition")
    private String secondaryDefinition;

    @Schema(
            description = "제1언어 단어 TTS 오디오 URL (wav 형식, null 가능)",
            nullable = true,
            example = "https://storage.googleapis.com/moretale-ai-generated-project-640335ef-3b09-441e-a26/generated/audio/word1_primary.wav"
    )
    @JsonProperty("audioUrlPrimary")
    @JsonAlias("audio_url_primary")
    private String audioUrlPrimary;

    @Schema(
            description = "제2언어 단어 TTS 오디오 URL (wav 형식, null 가능)",
            nullable = true,
            example = "https://storage.googleapis.com/moretale-ai-generated-project-640335ef-3b09-441e-a26/generated/audio/word1_secondary.wav"
    )
    @JsonProperty("audioUrlSecondary")
    @JsonAlias("audio_url_secondary")
    private String audioUrlSecondary;
}
