package com.moretale.domain.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "동화 생성 비동기 작업 응답")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryGenerationJobResponse {

    @Schema(description = "AI story job ID", example = "20260522_153000_story_mina")
    private String jobId;

    @Schema(description = "작업 상태", example = "queued")
    private String status;

    @Schema(description = "백엔드 상태 조회 URL", example = "/api/stories/generation-jobs/20260522_153000_story_mina")
    private String statusUrl;

    @Schema(description = "백엔드 결과 조회 URL", example = "/api/stories/generation-jobs/20260522_153000_story_mina/result")
    private String resultUrl;

    @Schema(description = "AI가 호출할 백엔드 callback URL")
    private String callbackUrl;
}
