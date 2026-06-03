package com.moretale.domain.story.service.impl;

import com.moretale.domain.story.dto.VocabularyItem;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.StoryToken;
import com.moretale.domain.story.service.StoryTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * StoryToken 생성 서비스 구현체
 *
 * ── 변경 이력 ─────────────────────────────────────────────────────────────
 *   이전: 형태소 분석기(open-korean-text) + AITokenService(더미 enrichment)로
 *         텍스트에서 토큰을 추출하고 번역/설명을 별도 AI 호출로 처리.
 *         → AI vocabulary가 백엔드에 전달되지 않아 더미 데이터가 저장되는 버그.
 *
 *   현재: AI 파트가 동화 생성 시점에 슬라이드별 vocabulary를 함께 반환.
 *         백엔드는 해당 vocabulary를 그대로 StoryToken으로 변환하여 저장.
 *         형태소 분석기 및 AITokenService(더미 enrichment) 제거.
 * ─────────────────────────────────────────────────────────────────────────
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StoryTokenServiceImpl implements StoryTokenService {

    @Override
    public List<StoryToken> generateTokensForSlide(
            Slide slide,
            List<VocabularyItem> vocabulary,
            String sourceLanguage,
            String targetLanguage
    ) {
        if (CollectionUtils.isEmpty(vocabulary)) {
            log.warn("슬라이드 vocabulary가 비어있음 - slideId={}, 토큰 생성 건너뜀",
                    slide.getSlideId());
            return List.of();
        }

        log.info("토큰 생성 시작 - slideId={}, vocabularySize={}, {}→{}",
                slide.getSlideId(), vocabulary.size(), sourceLanguage, targetLanguage);

        List<StoryToken> tokens = new ArrayList<>();

        for (int i = 0; i < vocabulary.size(); i++) {
            VocabularyItem item = vocabulary.get(i);

            if (item.getPrimaryWord() == null || item.getPrimaryWord().isBlank()) {
                log.warn("vocabulary primaryWord가 비어있음 - slideId={}, entryId={}",
                        slide.getSlideId(), item.getEntryId());
                continue;
            }

            StoryToken token = StoryToken.builder()
                    .text(item.getPrimaryWord().trim())
                    .tokenOrder(i)
                    .highlight(true)
                    .translation(item.getSecondaryWord())
                    .definition(item.getPrimaryDefinition())
                    .secondaryDefinition(item.getSecondaryDefinition())
                    .audioUrl(item.getAudioUrlPrimary())
                    .sourceLanguage(sourceLanguage)
                    .targetLanguage(targetLanguage)
                    .build();

            tokens.add(token);

            log.debug("토큰 생성 - slideId={}, order={}, word={}, translation={}, definitionNull={}, secondaryDefinitionNull={}",
                    slide.getSlideId(), i,
                    item.getPrimaryWord(),
                    item.getSecondaryWord(),
                    item.getPrimaryDefinition() == null,
                    item.getSecondaryDefinition() == null);
        }

        log.info("토큰 생성 완료 - slideId={}, 총 {}개 (전체 하이라이트)",
                slide.getSlideId(), tokens.size());

        return tokens;
    }
}
