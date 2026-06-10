package com.moretale.domain.vocabulary.dto.response;

import com.moretale.domain.vocabulary.entity.VocabularyEntry;
import com.moretale.domain.vocabulary.entity.VocabularyEntry.LearningStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "단어장 응답 DTO")
@Getter
@Builder
public class VocabularyResponse {

    @Schema(description = "단어장 항목 ID", example = "1")
    private Long vocabularyId;

    @Schema(description = "단어 원문", example = "우주복")
    private String word;

    @Schema(description = "정규화된 단어", example = "우주복")
    private String normalizedWord;

    @Schema(description = "번역어 / 제2언어 단어", example = "bộ đồ phi hành gia")
    private String translation;

    @Schema(description = "제1언어 뜻 설명", example = "우주에서 입는 특별한 옷")
    private String definition;

    @Schema(description = "제2언어 뜻 설명", example = "áo đặc biệt mặc khi bay vào không gian")
    private String secondaryDefinition;

    @Schema(description = "제1언어 ISO 코드", example = "ko")
    private String sourceLanguage;

    @Schema(description = "제2언어 ISO 코드", example = "vi")
    private String targetLanguage;

    @Schema(description = "단어 발음 오디오 URL (wav 형식)",
            example = "https://storage.googleapis.com/moretale-ai-generated-project-640335ef-3b09-441e-a26/tts/audio/tts_ko-KR_12345678_abcd1234.wav")
    private String audioUrl;

    @Schema(description = "출처 동화 ID", example = "5")
    private Long storyId;

    @Schema(description = "출처 동화 제목", example = "흥부와 놀부")
    private String storyTitle;

    @Schema(description = "출처 슬라이드 ID", example = "10")
    private Long slideId;

    @Schema(description = "출처 슬라이드 순서 (0부터 시작)", example = "2")
    private Integer slideOrder;

    @Schema(description = "출처 토큰 ID", example = "42")
    private Long tokenId;

    @Schema(description = "즐겨찾기 여부", example = "false")
    private Boolean isFavorite;

    @Schema(description = "학습 상태 (UNSEEN / LEARNING / MASTERED)", example = "UNSEEN")
    private LearningStatus learningStatus;

    @Schema(description = "메모", example = "동화에서 중요한 단어")
    private String memo;

    @Schema(description = "마지막 복습 일시 (ISO 8601)", example = "2024-01-15T09:30:00Z")
    private LocalDateTime lastReviewedAt;

    @Schema(description = "저장일시 (ISO 8601)", example = "2024-01-15T09:30:00Z")
    private LocalDateTime createdAt;

    public static VocabularyResponse from(VocabularyEntry entry) {
        return VocabularyResponse.builder()
                .vocabularyId(entry.getVocabularyId())
                .word(entry.getWord())
                .normalizedWord(entry.getNormalizedWord())
                .translation(entry.getTranslation())
                .definition(entry.getDefinition())
                .secondaryDefinition(entry.getSecondaryDefinition())
                .sourceLanguage(entry.getSourceLanguage())
                .targetLanguage(entry.getTargetLanguage())
                .audioUrl(entry.getAudioUrl())
                .storyId(entry.getStory().getStoryId())
                .storyTitle(entry.getStory().getTitle())
                .slideId(entry.getSlide().getSlideId())
                .slideOrder(entry.getSlide().getOrder())
                .tokenId(entry.getStoryToken().getTokenId())
                .isFavorite(entry.getIsFavorite())
                .learningStatus(entry.getLearningStatus())
                .memo(entry.getMemo())
                .lastReviewedAt(entry.getLastReviewedAt())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}
