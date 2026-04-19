package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.Story;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "마이페이지 최근 동화 응답 DTO")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentStoryResponse {

    @Schema(description = "동화 ID", example = "1")
    private Long storyId;

    @Schema(description = "동화 제목", example = "흥부와 놀부")
    private String title;

    @Schema(description = "주인공 아이 이름", example = "민준")
    private String childName;

    @Schema(
            description = "제1언어 코드 또는 표시값 (예: ko, en, ja, 태국어)",
            example = "ko"
    )
    private String primaryLanguage;

    @Schema(
            description = "제2언어 코드 또는 표시값 (예: en, vi, ja, 힌디어)",
            example = "en"
    )
    private String secondaryLanguage;

    @Schema(description = "썸네일 이미지 URL (첫 번째 슬라이드)", example = "https://storage.example.com/images/slide1.png")
    private String thumbnailUrl;

    @Schema(description = "공개 여부", example = "false")
    private Boolean isPublic;

    @Schema(description = "생성일시 (ISO 8601)", example = "2024-01-15T09:30:00Z")
    private LocalDateTime createdAt;

    public static RecentStoryResponse fromEntity(Story story) {
        String thumbnail = story.getSlides().isEmpty()
                ? null
                : story.getSlides().get(0).getImageUrl();

        return RecentStoryResponse.builder()
                .storyId(story.getStoryId())
                .title(story.getTitle())
                .childName(story.getChildName())
                .primaryLanguage(story.getPrimaryLanguage())
                .secondaryLanguage(story.getSecondaryLanguage())
                .thumbnailUrl(thumbnail)
                .isPublic(story.getIsPublic())
                .createdAt(story.getCreatedAt())
                .build();
    }
}
