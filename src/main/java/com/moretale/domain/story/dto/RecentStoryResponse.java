package com.moretale.domain.story.dto;

import com.moretale.domain.story.entity.Story;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

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
            description = """
                    [Legacy] 제1언어 ISO 코드 (ex. ko, vi, en).
                    Story 저장 시 Profile.firstLanguage Enum의 isoCode를 기반으로 저장된 값.
                    OTHER인 경우 사용자 입력 custom 문자열이 저장되어 있을 수 있음.
                    향후 firstLanguage / firstLanguageDisplay / primaryLanguageCode 필드로 분리 예정.
                    """,
            example = "ko"
    )
    private String primaryLanguage;

    @Schema(
            description = """
                    [Legacy] 제2언어 ISO 코드 (ex. vi, en, ja).
                    Story 저장 시 Profile.secondLanguage Enum의 isoCode를 기반으로 저장된 값.
                    OTHER인 경우 사용자 입력 custom 문자열이 저장되어 있을 수 있음.
                    향후 secondLanguage / secondLanguageDisplay / secondaryLanguageCode 필드로 분리 예정.
                    """,
            example = "vi"
    )
    private String secondaryLanguage;

    @Schema(
            description = "썸네일 이미지 URL (첫 번째 슬라이드 기준, 슬라이드가 없으면 null 가능)",
            nullable = true,
            example = "https://storage.example.com/images/slide1.png"
    )
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
