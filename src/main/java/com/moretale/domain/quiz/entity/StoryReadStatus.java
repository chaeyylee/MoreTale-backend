package com.moretale.domain.quiz.entity;

import com.moretale.domain.story.entity.Story;
import com.moretale.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 동화 완독 상태
 * - 완독 여부와 꿀단지 지급 여부를 별도 추적
 * - 동화 1권당 최대 2개 꿀단지 제한 관리 (완독 1개 + 퀴즈 1개)
 */
@Entity
@Table(name = "story_read_statuses",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_story_read_status_user_story",
                columnNames = {"user_id", "story_id"}
        ))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Long statusId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    // 완독 여부
    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    // 완독 보상 꿀단지 지급 여부
    @Column(name = "read_honey_jar_rewarded", nullable = false)
    @Builder.Default
    private Boolean readHoneyJarRewarded = false;

    // 퀴즈 보상 꿀단지 지급 여부
    @Column(name = "quiz_honey_jar_rewarded", nullable = false)
    @Builder.Default
    private Boolean quizHoneyJarRewarded = false;

    // 완독 일시
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 이 동화에서 받을 수 있는 꿀단지를 모두 받았는지 확인 (최대 2개 제한)
    public boolean isFullyRewarded() {
        return readHoneyJarRewarded && quizHoneyJarRewarded;
    }
}
