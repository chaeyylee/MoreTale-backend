package com.moretale.domain.story.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "slides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slide {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slide_id")
    private Long slideId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "order_num", nullable = false)
    private Integer order;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "text_kr", columnDefinition = "TEXT")
    private String textKr;

    @Column(name = "text_native", columnDefinition = "TEXT")
    private String textNative;

    @Column(name = "audio_url_kr", length = 500)
    private String audioUrlKr;

    @Column(name = "audio_url_native", length = 500)
    private String audioUrlNative;

    // 토큰 연관관계
    @OneToMany(mappedBy = "slide", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("tokenOrder ASC")
    @Builder.Default
    private List<StoryToken> tokens = new ArrayList<>();

    // 편의 메서드
    public void addToken(StoryToken token) {
        tokens.add(token);
        token.setSlide(this);
    }
}
