package com.moretale.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "지역 설정 요청 DTO")
@Getter
@NoArgsConstructor
public class RegionRequest {

    @Schema(description = "지역 정보", example = "서울")
    private String region;
}
