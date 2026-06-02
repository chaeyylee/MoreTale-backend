package com.moretale.domain.tts.service.impl;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import com.moretale.domain.tts.dto.TTSRequest;
import com.moretale.domain.tts.dto.TTSResponse;
import com.moretale.domain.tts.service.TTSService;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import com.moretale.global.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleTTSServiceImpl implements TTSService {

    @Value("${google.cloud.credentials.location}")
    private String credentialsLocation;

    @Value("${tts.storage.path}")
    private String storagePath;

    private final FileStorageService fileStorageService;
    private final ResourceLoader resourceLoader;

    @Override
    public TTSResponse generateTTS(TTSRequest request) {
        // 언어 검증 추가
        validateLanguage(request.getLanguage());

        log.info("TTS 생성 요청 시작 - 언어: {}, 텍스트 길이: {}",
                request.getLanguage(), request.getText().length());

        try {
            Resource resource = resourceLoader.getResource(credentialsLocation);
            GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream());

            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();

            try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {
                SynthesisInput input = SynthesisInput.newBuilder()
                        .setText(request.getText())
                        .build();

                VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                        .setLanguageCode(request.getLanguage())
                        .setSsmlGender(SsmlVoiceGender.NEUTRAL)
                        .build();

                AudioConfig audioConfig = AudioConfig.newBuilder()
                        .setAudioEncoding(AudioEncoding.LINEAR16)
                        .setSpeakingRate(1.0)
                        .setPitch(0.0)
                        .build();

                SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);
                ByteString audioContents = response.getAudioContent();

                String fileName = generateFileName(request.getLanguage());
                String audioUrl = saveAudioFile(audioContents.toByteArray(), fileName);

                log.info("TTS 생성 및 업로드 완료 - URL: {}", audioUrl);

                return TTSResponse.builder()
                        .audioUrl(audioUrl)
                        .language(request.getLanguage())
                        .message("TTS 생성 성공")
                        .build();
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google TTS API 호출 중 상세 에러 발생: ", e);
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

    private String generateFileName(String language) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("tts_%s_%s_%s.wav", language, timestamp, uuid);
    }

    private String saveAudioFile(byte[] audioData, String fileName) throws IOException {
        Path tempDir = Files.createTempDirectory("tts_temp");
        Path tempFile = tempDir.resolve(fileName);

        try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
            fos.write(audioData);
        }

        String subPath = storagePath + "/" + fileName;
        String audioUrl = fileStorageService.uploadFile(tempFile.toFile(), subPath);

        Files.deleteIfExists(tempFile);
        Files.deleteIfExists(tempDir);

        return audioUrl;
    }
}
