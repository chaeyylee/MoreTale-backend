package com.moretale.domain.tts.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncTTSService {

    private final TTSGenerationService ttsGenerationService;

    // 비동기로 동화 TTS 생성
    @Async("ttsExecutor")
    public void generateTTSAsync(Long storyId) {
        try {
            log.info("비동기 TTS 생성 시작 - 동화 ID: {}", storyId);
            ttsGenerationService.generateTTSForStory(storyId);
            log.info("비동기 TTS 생성 완료 - 동화 ID: {}", storyId);
        } catch (Exception e) {
            log.error("비동기 TTS 생성 실패 - 동화 ID: {}", storyId, e);
        }
    }
}
