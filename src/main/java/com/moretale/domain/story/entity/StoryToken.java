package com.moretale.domain.story.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "story_tokens", indexes = {
        @Index(name = "idx_story_token_slide_id", columnList = "slide_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    // 어느 슬라이드에 속한 토큰인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slide_id", nullable = false)
    private Slide slide;

    // 원문 텍스트 (정규화된 형태, 예: "우주복")
    @Column(name = "text", nullable = false, length = 100)
    private String text;

    // 원문에서의 순서 (0부터 시작)
    @Column(name = "token_order", nullable = false)
    private Integer tokenOrder;

    // 하이라이트 여부 (AI vocabulary에 포함된 핵심 단어인 경우 true)
    @Column(name = "highlight", nullable = false)
    @Builder.Default
    private Boolean highlight = false;

    // 번역어 / 제2언어 단어 (highlight=true인 경우에만 존재)
    @Column(name = "translation", length = 200)
    private String translation;

    // 제1언어 뜻 설명 (highlight=true인 경우에만 존재)
    @Column(name = "definition", columnDefinition = "TEXT")
    private String definition;

    // 제2언어 뜻 설명 (highlight=true인 경우에만 존재)
    @Column(name = "secondary_definition", columnDefinition = "TEXT")
    private String secondaryDefinition;

    // 제1언어 단어 발음 오디오 URL (highlight=true인 경우에만 존재)
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    // 제1언어 코드 (예: "ko", "vi", "en")
    @Column(name = "source_language", length = 10)
    private String sourceLanguage;

    // 제2언어 코드 (예: "vi", "en", "ja")
    @Column(name = "target_language", length = 10)
    private String targetLanguage;
}
