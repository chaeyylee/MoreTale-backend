package com.moretale.domain.tts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "TTS 생성 요청 DTO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TTSRequest {

    @NotBlank(message = "텍스트는 필수입니다")
    @Schema(
            description = "음성으로 변환할 텍스트",
            example = "옛날 옛날에 흥부와 놀부가 살았어요."
    )
    private String text;

    @NotBlank(message = "언어 코드는 필수입니다")
    @Schema(
            description = """
                    TTS locale 코드

                    [지원 값 예시]
                    - ko-KR : 한국어
                    - en-US : 영어
                    - ja-JP : 일본어
                    - zh-CN : 중국어
                    - es-ES : 스페인어
                    - vi-VN : 베트남어

                    ※ 온보딩 언어 설정에 따라 적절한 locale 코드로 전달해야 합니다.
                    ※ OTHER는 TTS 미지원 또는 fallback 처리 대상입니다.
                    """,
            example = "ko-KR"
    )
    private String language; // TTS locale code

    @Schema(
            description = "음성 스타일 (선택)",
            example = "neutral"
    )
    private String style;
}
