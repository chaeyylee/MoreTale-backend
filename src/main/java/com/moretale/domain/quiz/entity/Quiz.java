package com.moretale.domain.quiz.entity;

import com.moretale.domain.story.entity.Story;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 동화 1권에 연결된 퀴즈 세트
 * - 동화 1권당 퀴즈 1세트 (5~10문제)
 * - primaryLanguage 기준으로 문제 언어 결정
 */
@Entity
@Table(name = "quizzes", indexes = {
        @Index(name = "idx_quiz_story_id", columnList = "story_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id")
    private Long quizId;

    // 연결된 동화 (동화 1권 = 퀴즈 1세트)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false, unique = true)
    private Story story;

    // 문제 언어 (primaryLanguage 기준)
    @Column(name = "language", nullable = false, length = 10)
    private String language;

    // 난이도 (연령 및 언어 수준 기반)
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false)
    private QuizDifficulty difficulty;

    // 총 문제 수 (5~10개)
    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 퀴즈 문제 목록
    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("questionOrder ASC")
    @Builder.Default
    private List<QuizQuestion> questions = new ArrayList<>();

    // 편의 메서드
    public void addQuestion(QuizQuestion question) {
        questions.add(question);
        question.setQuiz(this);
    }
}
