package com.moretale.domain.tts.service;

import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.SlideRepository;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.tts.dto.TTSRequest;
import com.moretale.domain.tts.dto.TTSResponse;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TTSGenerationService {

    private final TTSService ttsService;
    private final StoryRepository storyRepository;
    private final SlideRepository slideRepository;

    // 단일 TTS 생성 (외부 API용)
    public TTSResponse generateSingleTTS(TTSRequest request) {
        return ttsService.generateTTS(request);
    }

    // 슬라이드에 대한 TTS 자동 생성
    public void generateTTSForSlide(Long slideId, String primaryLanguage, String secondaryLanguage) {
        Slide slide = slideRepository.findById(slideId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLIDE_NOT_FOUND));

        try {
            // 한국어 TTS 생성
            if (slide.getTextKr() != null && !slide.getTextKr().isEmpty()) {
                slide.setAudioUrlKr(ttsService.generateAudioUrl(slide.getTextKr(), primaryLanguage));
            }

            // 부모 언어 TTS 생성
            if (slide.getTextNative() != null && !slide.getTextNative().isEmpty()) {
                slide.setAudioUrlNative(ttsService.generateAudioUrl(slide.getTextNative(), secondaryLanguage));
            }

            slideRepository.save(slide);
            log.info("슬라이드 {} TTS 생성 완료", slideId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("슬라이드 {} TTS 생성 실패", slideId, e);
            throw new BusinessException(ErrorCode.TTS_GENERATION_FAILED);
        }
    }

    // 동화 전체 슬라이드에 대한 TTS 생성
    public void generateTTSForStory(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        List<Slide> slides = slideRepository.findByStoryIdOrderByOrder(storyId);

        String primaryLanguage = convertToLanguageCode(story.getPrimaryLanguage());
        String secondaryLanguage = convertToLanguageCode(story.getSecondaryLanguage());

        for (Slide slide : slides) {
            try {
                generateTTSForSlide(slide.getSlideId(), primaryLanguage, secondaryLanguage);
            } catch (Exception e) {
                log.error("슬라이드 {} TTS 생성 중 오류 발생", slide.getSlideId(), e);
                // 하나 실패해도 계속 진행
            }
        }

        log.info("동화 {} 전체 TTS 생성 완료", storyId);
    }

    // TTS가 없는 슬라이드만 재생성
    public void regenerateMissingTTS(Long storyId) {
        Story story = storyRepository.findById(storyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        List<Slide> slidesWithoutTTS = slideRepository.findSlidesWithoutTTS(storyId);

        if (slidesWithoutTTS.isEmpty()) {
            log.info("동화 {} - 재생성할 TTS 없음", storyId);
            return;
        }

        String primaryLanguage = convertToLanguageCode(story.getPrimaryLanguage());
        String secondaryLanguage = convertToLanguageCode(story.getSecondaryLanguage());

        for (Slide slide : slidesWithoutTTS) {
            generateTTSForSlide(slide.getSlideId(), primaryLanguage, secondaryLanguage);
        }

        log.info("동화 {} - 누락된 TTS {} 개 재생성 완료", storyId, slidesWithoutTTS.size());
    }

    // 언어 코드 변환 (ko -> ko-KR, vi -> vi-VN 등)
    private String convertToLanguageCode(String languageShort) {
        return switch (languageShort.toLowerCase()) {
            case "ko" -> "ko-KR";
            case "vi" -> "vi-VN";
            case "en" -> "en-US";
            case "zh" -> "zh-CN";
            case "ja" -> "ja-JP";
            case "tl" -> "fil-PH";
            case "mn" -> "mn-MN";
            default -> "ko-KR";
        };
    }
}
