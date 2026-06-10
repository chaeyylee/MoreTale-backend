package com.moretale.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 퀴즈 개별 문제
 * - 선다형(MULTIPLE_CHOICE) / T/F(TRUE_FALSE) 지원
 * - 단어 이해 + 줄거리 이해 복합 평가
 */
@Entity
@Table(name = "quiz_questions", indexes = {
        @Index(name = "idx_quiz_question_quiz_id", columnList = "quiz_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // 문제 유형 (선다형 / T/F)
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;

    // 평가 유형 (단어 이해 / 줄거리 이해)
    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_type", nullable = false)
    private EvaluationType evaluationType;

    // 문제 번호 (1부터 시작)
    @Column(name = "question_order", nullable = false)
    private Integer questionOrder;

    // 문제 내용
    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    // 정답 (선다형: "1"~"4", T/F: "TRUE"/"FALSE")
    @Column(name = "correct_answer", nullable = false, length = 10)
    private String correctAnswer;

    // 해설
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    // 선다형 보기 목록 (T/F는 비어있음)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("optionOrder ASC")
    @Builder.Default
    private List<QuizOption> options = new ArrayList<>();

    // 편의 메서드
    public void addOption(QuizOption option) {
        options.add(option);
        option.setQuestion(this);
    }

    // 정답 여부 판별
    public boolean isCorrect(String answer) {
        return this.correctAnswer.equalsIgnoreCase(answer != null ? answer.trim() : "");
    }
}
