package com.moretale.domain.honeyjar.entity;

import com.moretale.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 꿀단지 획득/사용 이력
 * - 획득/사용 내역을 모두 기록 (감사 로그 + 향후 통계용)
 */
@Entity
@Table(name = "honey_jar_histories", indexes = {
        @Index(name = "idx_honey_jar_history_user_id", columnList = "user_id")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoneyJarHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 변동 유형 (획득/사용)
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false)
    private HoneyJarAction actionType;

    // 변동 수량 (양수: 획득, 음수: 사용)
    @Column(name = "amount", nullable = false)
    private Integer amount;

    // 변동 사유
    @Column(name = "reason", nullable = false, length = 200)
    private String reason;

    // 변동 후 잔액
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    // 관련 동화 ID (선택)
    @Column(name = "story_id")
    private Long storyId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
