package com.moretale.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 선다형 문제의 보기 항목
 * - optionOrder: 1~4번 보기 순서
 * - optionText: 보기 내용
 */
@Entity
@Table(name = "quiz_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_id")
    private Long optionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    // 보기 순서 (1~4)
    @Column(name = "option_order", nullable = false)
    private Integer optionOrder;

    // 보기 내용
    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;
}
