package com.moretale.domain.story.service.impl;

import com.moretale.domain.story.service.TokenizationService;
import org.openkoreantext.processor.KoreanTokenJava;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;
import org.springframework.stereotype.Service;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TokenizationServiceImpl implements TokenizationService {

    @Override
    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return new ArrayList<>();

        // 1. 텍스트 정규화
        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);

        // 2. 토큰화 (Seq 타입 반환)
        Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

        // 3. 자바 리스트로 변환 (디컴파일된 소스에서 확인한 메서드명: tokensToJavaKoreanTokenList)
        List<KoreanTokenJava> javaTokens = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens);

        return javaTokens.stream()
                .filter(token -> isTargetPos(token.getPos().toString()))
                .map(token -> token.getText())
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public String normalize(String token) {
        if (token == null || token.isBlank()) return null;

        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(token);
        Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

        List<KoreanTokenJava> javaTokens = OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens);

        return javaTokens.stream()
                .filter(t -> isTargetPos(t.getPos().toString()))
                .map(t -> t.getText())
                .findFirst()
                .orElse(token.trim());
    }

    @Override
    public List<String> selectHighlightWords(List<String> normalizedTokens, int maxCount) {
        if (normalizedTokens == null || normalizedTokens.isEmpty()) {
            return new ArrayList<>();
        }

        return normalizedTokens.stream()
                .filter(token -> token.length() >= 1)
                .distinct()
                .limit(maxCount)
                .collect(Collectors.toList());
    }

    private boolean isTargetPos(String pos) {
        // Noun: 명사, ProperNoun: 고유명사, Verb: 동사, Adjective: 형용사
        List<String> targets = Arrays.asList("Noun", "ProperNoun", "Verb", "Adjective");
        return targets.contains(pos);
    }
}
