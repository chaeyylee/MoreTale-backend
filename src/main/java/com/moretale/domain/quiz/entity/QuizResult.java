package com.moretale.domain.quiz.entity;

import com.moretale.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 응시 결과
 * - 사용자가 퀴즈를 제출할 때마다 생성
 * - 점수, 정답 여부, 꿀단지 지급 여부 저장
 */
@Entity
@Table(name = "quiz_results", indexes = {
        @Index(name = "idx_quiz_result_user_id", columnList = "user_id"),
        @Index(name = "idx_quiz_result_quiz_id", columnList = "quiz_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long resultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // 획득 점수 (0~100)
    @Column(name = "score", nullable = false)
    private Integer score;

    // 총 문제 수
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    // 맞은 문제 수
    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    // 만점(100점) 여부
    @Column(name = "is_perfect", nullable = false)
    @Builder.Default
    private Boolean isPerfect = false;

    // 퀴즈 보상 꿀단지 지급 여부 (100점 시 1개 추가)
    @Column(name = "honey_jar_rewarded", nullable = false)
    @Builder.Default
    private Boolean honeyJarRewarded = false;

    // 응시 일시
    @CreationTimestamp
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    // 개별 문항 응답 내역
    @OneToMany(mappedBy = "quizResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuizAnswerRecord> answerRecords = new ArrayList<>();

    // 편의 메서드: 점수 계산
    public static int calculateScore(int correctCount, int totalQuestions) {
        if (totalQuestions == 0) return 0;
        return (int) Math.round((double) correctCount / totalQuestions * 100);
    }
}
