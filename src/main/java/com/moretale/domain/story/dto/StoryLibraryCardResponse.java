package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.Story;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 도서관 화면 카드 응답 DTO
// UI 정렬: 최신순(createdAt DESC), 오래된순(createdAt ASC), 가나다순(title ASC)
// thumbnail: 첫 번째 슬라이드 imageUrl 사용
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryLibraryCardResponse {

    private Long storyId;
    private String title;
    private String thumbnail;           // 첫 번째 슬라이드 imageUrl
    private String primaryLanguage;
    private String secondaryLanguage;
    private Boolean isPublic;
    private LocalDateTime createdAt;    // Story.createdAt 기준 (UI 날짜 표시용)
    private Integer slideCount;

    public static StoryLibraryCardResponse from(Story story) {
        // 첫 번째 슬라이드 imageUrl을 thumbnail로 사용
        String thumbnail = story.getSlides().isEmpty()
                ? null
                : story.getSlides().get(0).getImageUrl();

        return StoryLibraryCardResponse.builder()
                .storyId(story.getStoryId())
                .title(story.getTitle())
                .thumbnail(thumbnail)
                .primaryLanguage(story.getPrimaryLanguage())
                .secondaryLanguage(story.getSecondaryLanguage())
                .isPublic(story.getIsPublic())
                .createdAt(story.getCreatedAt())
                .slideCount(story.getSlides().size())
                .build();
    }
}
