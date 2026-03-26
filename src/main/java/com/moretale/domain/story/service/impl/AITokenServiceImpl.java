package com.moretale.domain.story.service.impl;

import com.moretale.domain.story.dto.TokenEnrichRequest;
import com.moretale.domain.story.dto.TokenEnrichResponse;
import com.moretale.domain.story.service.AITokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AITokenServiceImpl implements AITokenService {

    // TODO: 실제 AI API 연동 (Gemini 등)
    // 프롬프트 예시:
    // "다음 한국어 단어들에 대해 {targetLanguage}로 번역어와 한국어 뜻 설명을 JSON으로 반환하세요.
    //  단어: {words}, 문맥: {context}
    //  형식: [{"word":"사자","translation":"sư tử","definition":"갈기가 있는 큰 고양이과 동물"}]"

    @Override
    public List<TokenEnrichResponse> enrichTokens(
            List<TokenEnrichRequest> tokens,
            String sourceLanguage,
            String targetLanguage
    ) {
        log.info("토큰 enrichment 요청 - {}개 단어, {}→{}",
                tokens.size(), sourceLanguage, targetLanguage);

        // TODO: 실제 AI API 호출하여 번역 + definition 생성
        // 현재는 더미 데이터 반환
        return tokens.stream()
                .map(req -> TokenEnrichResponse.builder()
                        .word(req.getWord())
                        .translation(req.getWord() + "_" + targetLanguage) // 더미
                        .definition(req.getWord() + "에 대한 설명입니다.")    // 더미
                        .build())
                .collect(Collectors.toList());
    }
}
