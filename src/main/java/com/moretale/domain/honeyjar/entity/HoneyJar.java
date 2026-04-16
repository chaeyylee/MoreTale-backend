package com.moretale.domain.honeyjar.entity;

import com.moretale.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 꿀단지 누적 자산
 * - 사용자당 1개 레코드 (누적 관리)
 * - 동화 완독 시 +1, 퀴즈 100점 시 +1 (동화 1권당 최대 2개)
 * - 20개 달성 시 동화 1권 무료 생성에 자동 차감
 */
@Entity
@Table(name = "honey_jars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoneyJar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "honey_jar_id")
    private Long honeyJarId;

    // 사용자당 1개 레코드
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // 현재 보유 꿀단지 수
    @Column(name = "count", nullable = false)
    @Builder.Default
    private Integer count = 0;

    // 누적 획득 꿀단지 수 (통계용)
    @Column(name = "total_earned", nullable = false)
    @Builder.Default
    private Integer totalEarned = 0;

    // 누적 사용 꿀단지 수 (통계용)
    @Column(name = "total_used", nullable = false)
    @Builder.Default
    private Integer totalUsed = 0;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 꿀단지 추가
    public void add(int amount) {
        this.count += amount;
        this.totalEarned += amount;
    }

    // 꿀단지 차감 (20개 → 동화 1권 무료)
    public boolean use(int amount) {
        if (this.count < amount) return false;
        this.count -= amount;
        this.totalUsed += amount;
        return true;
    }

    // 무료 생성 가능 여부 (20개 이상)
    public boolean canGenerateFree() {
        return this.count >= 20;
    }
}
