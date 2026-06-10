package com.moretale.domain.story.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

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

    /**
     * 토큰 연관관계
     *
     * CascadeType.ALL: Slide 저장 시 연결된 StoryToken도 함께 저장됨.
     * orphanRemoval = true: 컬렉션에서 제거된 Token은 자동 삭제됨.
     * @BatchSize(size = 50): tokens 조회 시 IN 절 배치 조회로 N+1 완화
     */
    @BatchSize(size = 50)
    @OneToMany(mappedBy = "slide", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("tokenOrder ASC")
    @Builder.Default
    private List<StoryToken> tokens = new ArrayList<>();

    // 토큰 연관관계 편의 메서드
    // token.slide 설정을 통해 Slide와 연결되며, CascadeType.ALL에 의해 자동 저장됨.
    public void addToken(StoryToken token) {
        tokens.add(token);
        token.setSlide(this);
    }

    // 토큰 연관관계 해제 편의 메서드
    // orphanRemoval = true 이므로 컬렉션에서 제거 시 자동 DELETE
    public void removeToken(StoryToken token) {
        tokens.remove(token);
        token.setSlide(null);
    }
}
