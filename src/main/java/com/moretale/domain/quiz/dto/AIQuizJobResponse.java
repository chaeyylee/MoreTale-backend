package com.moretale.domain.quiz.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

// AI 서버 POST /internal/ai/quiz/jobs 응답 DTO
// Python InternalJobCreateResponse 구조에 맞춤
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AIQuizJobResponse {

    // AI job ID
    @JsonProperty("jobId")
    @JsonAlias("job_id")
    private String jobId;

    // job 타입
    @JsonProperty("type")
    private String type;

    // job 상태
    @JsonProperty("status")
    private String status;

    // 상태 조회 URL
    @JsonProperty("statusUrl")
    @JsonAlias("status_url")
    private String statusUrl;

    // 결과 조회 URL
    @JsonProperty("resultUrl")
    @JsonAlias("result_url")
    private String resultUrl;

    // callback URL
    @JsonProperty("callbackUrl")
    @JsonAlias("callback_url")
    private String callbackUrl;
}
