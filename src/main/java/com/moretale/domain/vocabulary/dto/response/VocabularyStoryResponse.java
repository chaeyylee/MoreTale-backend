package com.moretale.domain.vocabulary.dto.response;

import com.moretale.domain.story.entity.Story;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "단어가 저장된 동화 목록 응답 DTO")
@Getter
@Builder
public class VocabularyStoryResponse {

    @Schema(description = "동화 ID", example = "5")
    private Long storyId;

    @Schema(description = "동화 제목", example = "흥부와 놀부")
    private String title;

    @Schema(description = "제1언어 코드", example = "KO")
    private String primaryLanguage;

    @Schema(description = "제2언어 코드", example = "VI")
    private String secondaryLanguage;

    @Schema(description = "동화 생성일시 (ISO 8601)", example = "2024-01-15T09:30:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "해당 동화에서 저장한 단어 수", example = "3")
    private long wordCount;

    public static VocabularyStoryResponse from(Story story, long wordCount) {
        return VocabularyStoryResponse.builder()
                .storyId(story.getStoryId())
                .title(story.getTitle())
                .primaryLanguage(story.getPrimaryLanguage())
                .secondaryLanguage(story.getSecondaryLanguage())
                .createdAt(story.getCreatedAt())
                .wordCount(wordCount)
                .build();
    }
}
