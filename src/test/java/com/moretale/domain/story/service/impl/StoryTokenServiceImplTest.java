package com.moretale.domain.story.service.impl;

import com.moretale.domain.story.dto.VocabularyItem;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.StoryToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StoryTokenServiceImpl 단위 테스트")
class StoryTokenServiceImplTest {

    private StoryTokenServiceImpl service;
    private Slide testSlide;

    @BeforeEach
    void setUp() {
        service = new StoryTokenServiceImpl();
        testSlide = Slide.builder().slideId(10L).order(1).build();
    }

    @Test
    @DisplayName("정상 vocabulary → highlight=true 토큰 생성")
    void generateTokens_success() {
        List<VocabularyItem> vocabulary = List.of(
                VocabularyItem.builder()
                        .primaryWord("우주복")
                        .secondaryWord("bộ đồ phi hành gia")
                        .primaryDefinition("우주에서 입는 특별한 옷")
                        .secondaryDefinition("áo đặc biệt")
                        .audioUrlPrimary("https://example.com/audio.wav")
                        .build(),
                VocabularyItem.builder()
                        .primaryWord("행성")
                        .secondaryWord("hành tinh")
                        .primaryDefinition("태양 주위를 도는 천체")
                        .secondaryDefinition("thiên thể")
                        .build()
        );

        List<StoryToken> tokens = service.generateTokensForSlide(
                testSlide, vocabulary, "ko", "vi");

        assertThat(tokens).hasSize(2);

        StoryToken first = tokens.get(0);
        assertThat(first.getText()).isEqualTo("우주복");
        assertThat(first.getHighlight()).isTrue();
        assertThat(first.getTranslation()).isEqualTo("bộ đồ phi hành gia");
        assertThat(first.getDefinition()).isEqualTo("우주에서 입는 특별한 옷");
        assertThat(first.getSecondaryDefinition()).isEqualTo("áo đặc biệt");
        assertThat(first.getSourceLanguage()).isEqualTo("ko");
        assertThat(first.getTargetLanguage()).isEqualTo("vi");
        assertThat(first.getTokenOrder()).isEqualTo(0);
        assertThat(first.getAudioUrl()).isEqualTo("https://example.com/audio.wav");
    }

    @Test
    @DisplayName("vocabulary null → 빈 리스트 반환")
    void generateTokens_nullVocabulary_returnsEmpty() {
        List<StoryToken> tokens = service.generateTokensForSlide(
                testSlide, null, "ko", "vi");

        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("vocabulary 빈 리스트 → 빈 리스트 반환")
    void generateTokens_emptyVocabulary_returnsEmpty() {
        List<StoryToken> tokens = service.generateTokensForSlide(
                testSlide, List.of(), "ko", "vi");

        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("primaryWord null 또는 blank 항목은 건너뜀")
    void generateTokens_skipNullOrBlankPrimaryWord() {
        List<VocabularyItem> vocabulary = List.of(
                VocabularyItem.builder().primaryWord(null).secondaryWord("test").build(),
                VocabularyItem.builder().primaryWord("  ").secondaryWord("test").build(),
                VocabularyItem.builder().primaryWord("우주복").secondaryWord("valid").build()
        );

        List<StoryToken> tokens = service.generateTokensForSlide(
                testSlide, vocabulary, "ko", "vi");

        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).getText()).isEqualTo("우주복");
    }

    @Test
    @DisplayName("primaryWord 공백 trim 처리")
    void generateTokens_trimsPrimaryWord() {
        List<VocabularyItem> vocabulary = List.of(
                VocabularyItem.builder().primaryWord("  우주복  ").build()
        );

        List<StoryToken> tokens = service.generateTokensForSlide(
                testSlide, vocabulary, "ko", "vi");

        assertThat(tokens.get(0).getText()).isEqualTo("우주복");
    }

    @Test
    @DisplayName("tokenOrder는 vocabulary 인덱스 순서로 설정")
    void generateTokens_tokenOrderMatchesIndex() {
        List<VocabularyItem> vocabulary = List.of(
                VocabularyItem.builder().primaryWord("a").build(),
                VocabularyItem.builder().primaryWord("b").build(),
                VocabularyItem.builder().primaryWord("c").build()
        );

        List<StoryToken> tokens = service.generateTokensForSlide(
                testSlide, vocabulary, "ko", "vi");

        assertThat(tokens.get(0).getTokenOrder()).isEqualTo(0);
        assertThat(tokens.get(1).getTokenOrder()).isEqualTo(1);
        assertThat(tokens.get(2).getTokenOrder()).isEqualTo(2);
    }

    @Test
    @DisplayName("audioUrlPrimary null이면 token audioUrl도 null")
    void generateTokens_nullAudioUrl() {
        List<VocabularyItem> vocabulary = List.of(
                VocabularyItem.builder()
                        .primaryWord("우주복")
                        .audioUrlPrimary(null)
                        .build()
        );

        List<StoryToken> tokens = service.generateTokensForSlide(
                testSlide, vocabulary, "ko", "vi");

        assertThat(tokens.get(0).getAudioUrl()).isNull();
    }
}
