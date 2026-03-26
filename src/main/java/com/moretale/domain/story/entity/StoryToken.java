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

    // 원문 텍스트 (예: "사자를" → 정규화 후 "사자")
    @Column(name = "text", nullable = false, length = 100)
    private String text;

    // 원문에서의 순서 (0부터 시작)
    @Column(name = "token_order", nullable = false)
    private Integer tokenOrder;

    // 하이라이트 여부 (핵심 단어인 경우 true)
    @Column(name = "highlight", nullable = false)
    @Builder.Default
    private Boolean highlight = false;

    // 번역어 (highlight=true인 경우에만 존재)
    @Column(name = "translation", length = 200)
    private String translation;

    // 뜻 설명 (한국어 기준, highlight=true인 경우에만 존재)
    @Column(name = "definition", columnDefinition = "TEXT")
    private String definition;

    // 단어 발음 오디오 URL (highlight=true인 경우에만 존재)
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    // 원문 언어 (예: "ko")
    @Column(name = "source_language", length = 10)
    private String sourceLanguage;

    // 번역 대상 언어 (예: "vi")
    @Column(name = "target_language", length = 10)
    private String targetLanguage;
}
