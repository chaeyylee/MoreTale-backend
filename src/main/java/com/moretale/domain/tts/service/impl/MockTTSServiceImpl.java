package com.moretale.domain.tts.service.impl;

import com.moretale.domain.tts.dto.TTSRequest;
import com.moretale.domain.tts.dto.TTSResponse;
import com.moretale.domain.tts.service.TTSService;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import com.moretale.global.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@Primary
@Profile({"dev", "local"})
@RequiredArgsConstructor
public class MockTTSServiceImpl implements TTSService {

    private final FileStorageService fileStorageService;

    @Value("${tts.storage.path:tts/audio}")
    private String storagePath;

    @Override
    public TTSResponse generateTTS(TTSRequest request) {
        // 언어 검증 추가
        validateLanguage(request.getLanguage());

        try {
            log.info("[Mock TTS] 생성 시작 - 언어: {}, 텍스트: {}",
                    request.getLanguage(),
                    request.getText().substring(0, Math.min(50, request.getText().length())));

            // 더미 오디오 데이터 생성
            byte[] dummyAudioData = generateDummyAudioData();

            // 파일명 생성
            String fileName = generateFileName(request.getLanguage());
            String filePath = storagePath + "/" + fileName;

            // 파일 저장
            String audioUrl = fileStorageService.uploadFile(dummyAudioData, filePath);

            log.info("[Mock TTS] 생성 완료 - URL: {}", audioUrl);

            return TTSResponse.builder()
                    .audioUrl(audioUrl)
                    .language(request.getLanguage())
                    .duration(5)
                    .message("Mock TTS 생성 완료")
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Mock TTS] 생성 실패", e);
            throw new BusinessException(ErrorCode.TTS_GENERATION_FAILED);
        }
    }

    @Override
    public String generateAudioUrl(String text, String language) {
        TTSRequest request = TTSRequest.builder()
                .text(text)
                .language(language)
                .build();

        TTSResponse response = generateTTS(request);
        return response.getAudioUrl();
    }

    private byte[] generateDummyAudioData() {
        String dummyContent = "MOCK_TTS_AUDIO_DATA_" + UUID.randomUUID();
        return dummyContent.getBytes();
    }

    private String generateFileName(String language) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("tts_%s_%s_%s.wav", language, timestamp, uuid);
    }
}
