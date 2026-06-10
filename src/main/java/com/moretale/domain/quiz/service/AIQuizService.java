package com.moretale.domain.quiz.service;

import com.moretale.domain.quiz.entity.QuizDifficulty;
import com.moretale.domain.quiz.entity.QuizQuestion;
import com.moretale.domain.story.entity.Story;

import java.util.List;

// AI 기반 퀴즈 생성 서비스 인터페이스
// LLM을 통해 동화 내용 기반 퀴즈 문제를 생성
public interface AIQuizService {

    /**
     * 동화 내용 기반 퀴즈 문제 생성
     *
     * @param story      대상 동화
     * @param difficulty 난이도
     * @param language   문제 언어 (primaryLanguage)
     * @param count      생성할 문제 수
     * @return 생성된 문제 목록 (저장 전 상태)
     */
    List<QuizQuestion> generateQuestions(
            Story story,
            QuizDifficulty difficulty,
            String language,
            int count
    );
}
