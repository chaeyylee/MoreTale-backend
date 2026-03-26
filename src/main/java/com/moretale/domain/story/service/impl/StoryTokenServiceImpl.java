package com.moretale.domain.story.service.impl;

import com.moretale.domain.story.dto.TokenEnrichRequest;
import com.moretale.domain.story.dto.TokenEnrichResponse;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.StoryToken;
import com.moretale.domain.story.service.AITokenService;
import com.moretale.domain.story.service.StoryTokenService;
import com.moretale.domain.story.service.TTSService;
import com.moretale.domain.story.service.TokenizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StoryTokenServiceImpl implements StoryTokenService {

    private final TokenizationService tokenizationService;
    private final AITokenService aiTokenService;
    private final TTSService ttsService;

    // 슬라이드당 최대 하이라이트 단어 수
    private static final int MAX_HIGHLIGHT_COUNT = 3;

    @Override
    public List<StoryToken> generateTokensForSlide(
            Slide slide,
            String sourceLanguage,
            String targetLanguage
    ) {
        String text = slide.getTextKr();
        if (text == null || text.isBlank()) {
            log.warn("슬라이드 textKr가 비어있음 - slideId={}", slide.getSlideId());
            return Collections.emptyList();
        }

        // 1. 어절 단위 토큰화
        List<String> rawTokens = tokenizationService.tokenize(text);
        log.debug("토큰화 결과 - slideId={}, tokens={}", slide.getSlideId(), rawTokens);

        // 2. 각 어절을 정규화 (조사 제거)
        List<String> normalizedTokens = rawTokens.stream()
                .map(tokenizationService::normalize)
                .collect(Collectors.toList());

        // 3. 하이라이트 대상 단어 선정
        List<String> highlightWords = tokenizationService.selectHighlightWords(
                normalizedTokens, MAX_HIGHLIGHT_COUNT
        );
        Set<String> highlightSet = new HashSet<>(highlightWords);
        log.debug("하이라이트 단어 선정 - slideId={}, words={}", slide.getSlideId(), highlightWords);

        // 4. 하이라이트 단어에 대한 번역 + definition AI 생성 (일괄 요청)
        Map<String, TokenEnrichResponse> enrichMap = new HashMap<>();
        if (!highlightWords.isEmpty()) {
            List<TokenEnrichRequest> enrichRequests = highlightWords.stream()
                    .map(word -> TokenEnrichRequest.builder()
                            .word(word)
                            .context(text) // 문맥 제공
                            .build())
                    .collect(Collectors.toList());

            try {
                List<TokenEnrichResponse> enrichResponses = aiTokenService.enrichTokens(
                        enrichRequests, sourceLanguage, targetLanguage
                );
                enrichResponses.forEach(r -> enrichMap.put(r.getWord(), r));
            } catch (Exception e) {
                log.error("토큰 enrichment 실패 - slideId={}", slide.getSlideId(), e);
                // enrichment 실패 시 하이라이트는 유지하되 번역/definition 없이 진행
            }
        }

        // 5. StoryToken 목록 생성
        List<StoryToken> result = new ArrayList<>();
        for (int i = 0; i < rawTokens.size(); i++) {
            String raw = rawTokens.get(i);
            String normalized = normalizedTokens.get(i);
            boolean isHighlight = highlightSet.contains(normalized);

            StoryToken.StoryTokenBuilder builder = StoryToken.builder()
                    .text(normalized)     // 정규화된 형태 저장
                    .tokenOrder(i)
                    .highlight(isHighlight)
                    .sourceLanguage(sourceLanguage);

            if (isHighlight) {
                TokenEnrichResponse enrich = enrichMap.get(normalized);
                if (enrich != null) {
                    builder
                            .translation(enrich.getTranslation())
                            .definition(enrich.getDefinition())
                            .targetLanguage(targetLanguage);

                    // 6. 하이라이트 단어 TTS 생성
                    try {
                        String audioUrl = ttsService.generateTTS(
                                normalized,
                                sourceLanguage + "-KR"
                        );
                        builder.audioUrl(audioUrl);
                    } catch (Exception e) {
                        log.warn("단어 TTS 생성 실패 - word={}", normalized, e);
                    }
                }
            }

            result.add(builder.build());
        }

        log.info("토큰 생성 완료 - slideId={}, 총{}개, 하이라이트{}개",
                slide.getSlideId(), result.size(), highlightWords.size());
        return result;
    }
}
