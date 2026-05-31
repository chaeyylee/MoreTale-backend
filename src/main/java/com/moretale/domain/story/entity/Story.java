package com.moretale.domain.story.entity;

import com.moretale.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 동화 엔티티
 *
 * ── 저장 cascade 흐름 ────────────────────────────────────────────────────────
 *   storyRepository.save(story) 호출 시:
 *     Story INSERT → Slide INSERT (CascadeType.ALL)
 *   em.flush() 후 slide.addToken(token) 호출 시:
 *     트랜잭션 커밋 → StoryToken INSERT (Slide.tokens CascadeType.ALL)
 *
 * ── 삭제 cascade 흐름 ────────────────────────────────────────────────────────
 *   storyRepository.delete(story) 호출 시:
 *
 *   [JPA 레벨]
 *     Story
 *      └── Slide           (CascadeType.ALL + orphanRemoval)
 *            └── StoryToken (CascadeType.ALL + orphanRemoval)
 *
 *   [DB 레벨 - @OnDelete(CASCADE)]
 *     Story 삭제 → VocabularyEntry 자동 삭제
 *     Slide 삭제 → VocabularyEntry 자동 삭제 (slide_id FK)
 *     StoryToken 삭제 → VocabularyEntry 자동 삭제 (token_id FK)
 * ─────────────────────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "stories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_id")
    private Long storyId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "profile_id")
    private Long profileId;

    @Column(name = "child_name", length = 100)
    private String childName;

    @Column(name = "primary_language", length = 10)
    private String primaryLanguage;

    @Column(name = "secondary_language", length = 10)
    private String secondaryLanguage;

    @Column(name = "is_public", nullable = false)
    @Builder.Default
    private Boolean isPublic = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 슬라이드 목록
     *
     * CascadeType.ALL:
     *   storyRepository.save(story) 만으로 모든 Slide도 함께 저장됨.
     *   slideRepository.saveAll() 별도 호출 불필요.
     *
     * orphanRemoval = true:
     *   slides 컬렉션에서 제거된 Slide는 자동으로 DELETE됨.
     */
    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("order ASC")
    @Builder.Default
    private List<Slide> slides = new ArrayList<>();

    // 연관관계 편의 메서드
    public void addSlide(Slide slide) {
        slides.add(slide);
        slide.setStory(this);
    }

    public void removeSlide(Slide slide) {
        slides.remove(slide);
        slide.setStory(null);
    }
}
