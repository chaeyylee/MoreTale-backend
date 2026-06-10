package com.moretale.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

// 퀴즈 개별 문항 응답 기록
// 오답노트, 학습 통계, 개인화 추천 기능 확장 가능하도록 설계
@Entity
@Table(name = "quiz_answer_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizAnswerRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private QuizResult quizResult;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    // 사용자가 제출한 답
    @Column(name = "submitted_answer", nullable = false, length = 20)
    private String submittedAnswer;

    // 정답 여부
    @Column(name = "is_correct", nullable = false)
    private Boolean isCorrect;
}
