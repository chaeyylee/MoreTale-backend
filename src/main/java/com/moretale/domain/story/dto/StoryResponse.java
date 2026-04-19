package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.Story;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "동화 상세 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryResponse {

    @Schema(description = "동화 ID", example = "1")
    private Long storyId;

    @Schema(description = "동화 제목", example = "흥부와 놀부")
    private String title;

    @Schema(description = "사용자 입력 프롬프트", example = "우주를 탐험하는 이야기")
    private String prompt;

    @Schema(description = "주인공 아이 이름", example = "민준")
    private String childName;

    @Schema(description = "제1언어 코드", example = "KO")
    private String primaryLanguage;

    @Schema(description = "제2언어 코드", example = "VI")
    private String secondaryLanguage;

    @Schema(description = "공개 여부", example = "false")
    private Boolean isPublic;

    @Schema(description = "생성일시 (ISO 8601)", example = "2024-01-15T09:30:00Z")
    private LocalDateTime createdAt;

    @Schema(description = "슬라이드 목록 (순서 포함)")
    private List<SlideResponse> slides;

    public static StoryResponse from(Story story) {
        return StoryResponse.builder()
                .storyId(story.getStoryId())
                .title(story.getTitle())
                .prompt(story.getPrompt())
                .childName(story.getChildName())
                .primaryLanguage(story.getPrimaryLanguage())
                .secondaryLanguage(story.getSecondaryLanguage())
                .isPublic(story.getIsPublic())
                .createdAt(story.getCreatedAt())
                .slides(story.getSlides().stream()
                        .map(SlideResponse::from)
                        .collect(Collectors.toList()))
                .build();
    }
}
