package com.moretale.domain.vocabulary.dto.response;

import com.moretale.domain.vocabulary.entity.VocabularyEntry;
import com.moretale.domain.vocabulary.entity.VocabularyEntry.LearningStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class VocabularyResponse {

    private Long vocabularyId;

    // 단어 정보
    private String word;
    private String normalizedWord;
    private String translation;
    private String definition;
    private String sourceLanguage;
    private String targetLanguage;
    private String audioUrl;

    // 출처 정보
    private Long storyId;
    private String storyTitle;
    private Long slideId;
    private Integer slideOrder;   // 몇 번째 슬라이드인지 (UX 편의)
    private Long tokenId;

    // 상태 정보
    private Boolean isFavorite;
    private LearningStatus learningStatus;
    private String memo;
    private LocalDateTime lastReviewedAt;

    private LocalDateTime createdAt;

    public static VocabularyResponse from(VocabularyEntry entry) {
        return VocabularyResponse.builder()
                .vocabularyId(entry.getVocabularyId())
                .word(entry.getWord())
                .normalizedWord(entry.getNormalizedWord())
                .translation(entry.getTranslation())
                .definition(entry.getDefinition())
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
