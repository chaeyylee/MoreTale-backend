package com.moretale.domain.tts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "TTS 생성 결과 응답 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TTSResponse {

    @Schema(description = "생성된 오디오 파일 URL", example = "https://storage.example.com/audio/tts_ko-KR_123.mp3")
    private String audioUrl;

    @Schema(description = "적용된 TTS locale 코드", example = "ko-KR")
    private String language;

    @Schema(description = "음성 길이 (초)", example = "5")
    private Integer duration;

    @Schema(description = "처리 결과 메시지", example = "TTS 생성 성공")
    private String message;
}
