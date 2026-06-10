package com.moretale.domain.story.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "동화 공유 설정 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoryShareRequest {

    @NotNull(message = "공유 여부를 설정해주세요.")
    @Schema(description = "공개 여부 (true: 공개, false: 비공개)", example = "true")
    private Boolean isPublic;
}
