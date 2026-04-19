package com.moretale.domain.tts.controller;

import com.moretale.domain.tts.dto.TTSRequest;
import com.moretale.domain.tts.dto.TTSResponse;
import com.moretale.domain.tts.service.TTSGenerationService;
import com.moretale.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Tag(name = "TTS", description = "음성 생성(TTS) API")
@Slf4j
@RestController
@RequestMapping("/api/tts")
@RequiredArgsConstructor
public class TTSController {

    private final TTSGenerationService ttsGenerationService;

    @Operation(
            summary = "TTS 단일 생성",
            description = """
                    텍스트를 음성으로 변환하여 오디오 URL을 반환합니다.

                    **지원 언어 코드 (온보딩 언어 기준)**
                    - `ko-KR` : 한국어
                    - `en-US` : 영어
                    - `ja-JP` : 일본어
                    - `zh-CN` : 중국어
                    - `es-ES` : 스페인어
                    - `vi-VN` : 베트남어

                    ※ `OTHER`는 TTS 미지원 또는 fallback 처리 대상입니다.

                    **응답 필드**
                    - `audioUrl` : 생성된 오디오 파일 URL
                    - `language` : 적용된 TTS locale 코드
                    - `duration` : 음성 길이 (초)
                    """
    )
    @PostMapping("/generate")
    public ApiResponse<TTSResponse> generateTTS(
            @Valid @RequestBody TTSRequest request
    ) {
        log.info("TTS 생성 요청 - language={}, textLength={}",
                request.getLanguage(), request.getText().length());

        TTSResponse response = ttsGenerationService.generateSingleTTS(request);
        return ApiResponse.success(response, "TTS 생성 완료");
    }

    @Operation(
            summary = "동화 전체 슬라이드 TTS 재생성",
            description = """
                    특정 동화의 모든 슬라이드에 대해 TTS를 재생성합니다.

                    - 기존 오디오 URL 덮어쓰기
                    - 한국어(textKr) + 제2언어(textNative) 모두 재생성
                    - TTS locale 코드는 온보딩 언어 기준 매핑값을 사용합니다.
                    """
    )
    @PostMapping("/regenerate/story/{storyId}")
    public ApiResponse<String> regenerateStoryTTS(
            @Parameter(description = "동화 ID")
            @PathVariable(name = "storyId") Long storyId
    ) {
        log.info("동화 전체 TTS 재생성 요청 - storyId={}", storyId);
        ttsGenerationService.generateTTSForStory(storyId);
        return ApiResponse.success("완료", "동화 TTS 재생성 완료");
    }

    @Operation(
            summary = "슬라이드 단일 TTS 재생성",
            description = """
                    특정 슬라이드의 TTS를 재생성합니다.

                    - `primaryLanguage` : 한국어 또는 제1언어 텍스트 음성 생성에 사용할 TTS locale 코드 (예: `ko-KR`)
                    - `secondaryLanguage` : 제2언어 텍스트 음성 생성에 사용할 TTS locale 코드 (예: `vi-VN`)
                    - 지원 예시: `ko-KR`, `en-US`, `ja-JP`, `zh-CN`, `es-ES`, `vi-VN`
                    """
    )
    @PostMapping("/regenerate/slide/{slideId}")
    public ApiResponse<String> regenerateSlideTTS(
            @Parameter(description = "슬라이드 ID")
            @PathVariable(name = "slideId") Long slideId,

            @Parameter(description = "제1언어 TTS locale 코드 (예: ko-KR)")
            @RequestParam(name = "primaryLanguage") String primaryLanguage,

            @Parameter(description = "제2언어 TTS locale 코드 (예: vi-VN)")
            @RequestParam(name = "secondaryLanguage") String secondaryLanguage
    ) {
        log.info("슬라이드 TTS 재생성 요청 - slideId={}, primary={}, secondary={}",
                slideId, primaryLanguage, secondaryLanguage);

        ttsGenerationService.generateTTSForSlide(slideId, primaryLanguage, secondaryLanguage);
        return ApiResponse.success("완료", "슬라이드 TTS 재생성 완료");
    }

    @Operation(
            summary = "누락된 TTS 재생성",
            description = """
                    특정 동화에서 오디오 URL이 없는 슬라이드만 선별하여 TTS를 생성합니다.

                    - 이미 오디오 URL이 있는 슬라이드는 건너뜀
                    - 부분적으로 TTS가 실패한 동화 복구에 활용
                    - TTS locale 코드는 동화의 언어 설정에 맞춰 적용됩니다.
                    """
    )
    @PostMapping("/regenerate/missing/{storyId}")
    public ApiResponse<String> regenerateMissingTTS(
            @Parameter(description = "동화 ID")
            @PathVariable(name = "storyId") Long storyId
    ) {
        log.info("누락 TTS 재생성 요청 - storyId={}", storyId);
        ttsGenerationService.regenerateMissingTTS(storyId);
        return ApiResponse.success("완료", "누락 TTS 재생성 완료");
    }
}
