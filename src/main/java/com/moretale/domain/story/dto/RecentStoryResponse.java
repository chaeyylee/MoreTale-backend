package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.Story;
import lombok.*;

import java.time.LocalDateTime;

// 마이페이지용 최근 생성 동화 요약 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentStoryResponse {

    private Long storyId;
    private String title;
    private String childName;
    private String primaryLanguage;
    private String secondaryLanguage;

    // 대표 썸네일 (첫 번째 슬라이드 이미지)
    private String thumbnailUrl;

    private Boolean isPublic;
    private LocalDateTime createdAt;

    public static RecentStoryResponse fromEntity(Story story) {
        // 첫 번째 슬라이드의 이미지를 썸네일로 사용
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
