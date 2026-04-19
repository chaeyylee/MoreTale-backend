package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.Story;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// 도서관 화면 카드 응답 DTO
// UI 정렬: 최신순(createdAt DESC), 오래된순(createdAt ASC), 가나다순(title ASC)
// thumbnail: 첫 번째 슬라이드 imageUrl 사용
@Schema(description = "도서관 동화 카드 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryLibraryCardResponse {

    @Schema(description = "동화 ID", example = "1")
    private Long storyId;

    @Schema(description = "동화 제목", example = "흥부와 놀부")
    private String title;

    @Schema(description = "썸네일 이미지 URL (첫 번째 슬라이드)", example = "https://storage.example.com/images/slide1.png")
    private String thumbnail;

    @Schema(description = "제1언어 코드", example = "KO")
    private String primaryLanguage;

    @Schema(description = "제2언어 코드", example = "VI")
    private String secondaryLanguage;

    @Schema(description = "공개 여부", example = "false")
    private Boolean isPublic;

    @Schema(description = "생성일시 (ISO 8601)", example = "2024-01-15T09:30:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "슬라이드 수", example = "5")
    private Integer slideCount;

    public static StoryLibraryCardResponse from(Story story) {
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
