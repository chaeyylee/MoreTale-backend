package com.moretale.domain.quiz.service;

import com.moretale.domain.honeyjar.entity.HoneyJarAction;
import com.moretale.domain.honeyjar.service.HoneyJarService;
import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.quiz.dto.*;
import com.moretale.domain.quiz.entity.*;
import com.moretale.domain.quiz.repository.*;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 퀴즈 & 꿀단지 핵심 서비스
 *
 * 퀴즈 흐름:
 *  1. GET /api/quiz?storyId=N  → 퀴즈 없으면 자동 생성, 있으면 조회
 *  2. POST /api/quiz/submit    → 채점 → 꿀단지 보상 처리
 *
 * 꿀단지 흐름:
 *  - 동화 완독: POST /api/quiz/story-complete → +1
 *  - 퀴즈 100점: submit 채점 후 자동 → +1
 *  - 20개 달성 시 → 자동 -20 (무료 생성 1회)
 *  - 동화 1권당 최대 2개 (완독 1 + 퀴즈 1) 제한
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final StoryReadStatusRepository storyReadStatusRepository;
    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AIQuizService aiQuizService;
    private final HoneyJarService honeyJarService;

    // 퀴즈 조회 (없으면 자동 생성)
    // GET /api/quiz?storyId={storyId}
    @Transactional
    public QuizResponse getOrGenerateQuiz(String email, Long storyId) {
        User user = getUserByEmail(email);
        Story story = getStoryWithAccess(user, storyId);

        // 기존 퀴즈가 있으면 바로 반환
        // QuizResponse.from() 내부에서 correctAnswer, explanation 자동 포함
        // 서비스 레이어 변경 없음 - DTO 레이어에서만 처리
        Quiz existingQuiz = quizRepository.findByStoryWithQuestions(story).orElse(null);
        if (existingQuiz != null) {
            log.info("기존 퀴즈 반환 - storyId={}, quizId={}", storyId, existingQuiz.getQuizId());
            return QuizResponse.from(existingQuiz);
        }

        // 없으면 새로 생성
        log.info("퀴즈 신규 생성 시작 - storyId={}", storyId);
        Quiz newQuiz = generateQuiz(user, story);
        return QuizResponse.from(newQuiz);
    }

    // 퀴즈 채점 및 꿀단지 보상
    // POST /api/quiz/submit
    @Transactional
    public QuizResultResponse submitQuiz(String email, QuizSubmitRequest request) {
        User user = getUserByEmail(email);

        // 퀴즈 조회 (문제 + 보기 포함)
        Quiz quiz = quizRepository.findByIdWithQuestions(request.getQuizId())
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 문제 ID → QuizQuestion 맵 구성
        Map<Long, QuizQuestion> questionMap = quiz.getQuestions().stream()
                .collect(Collectors.toMap(QuizQuestion::getQuestionId, q -> q));

        // 채점 처리
        int correctCount = 0;
        List<QuizAnswerRecord> answerRecords = new ArrayList<>();
        List<QuizResultResponse.AnswerResultDto> answerResults = new ArrayList<>();

        for (QuizSubmitRequest.AnswerDto answer : request.getAnswers()) {
            QuizQuestion question = questionMap.get(answer.getQuestionId());
            if (question == null) {
                log.warn("존재하지 않는 문제 ID - questionId={}", answer.getQuestionId());
                continue;
            }

            boolean isCorrect = question.isCorrect(answer.getSubmittedAnswer());
            if (isCorrect) correctCount++;

            QuizAnswerRecord record = QuizAnswerRecord.builder()
                    .question(question)
                    .submittedAnswer(answer.getSubmittedAnswer())
                    .isCorrect(isCorrect)
                    .build();

            answerRecords.add(record);
            answerResults.add(QuizResultResponse.AnswerResultDto.from(record));
        }

        int totalQuestions = quiz.getQuestions().size();
        int score = QuizResult.calculateScore(correctCount, totalQuestions);
        boolean isPerfect = (score == 100);

        // QuizResult 저장
        QuizResult quizResult = QuizResult.builder()
                .user(user)
                .quiz(quiz)
                .score(score)
                .totalQuestions(totalQuestions)
                .correctCount(correctCount)
                .isPerfect(isPerfect)
                .honeyJarRewarded(false)
                .build();

        answerRecords.forEach(record -> {
            record.setQuizResult(quizResult);
            quizResult.getAnswerRecords().add(record);
        });

        quizResultRepository.save(quizResult);

        log.info("퀴즈 채점 완료 - userId={}, quizId={}, score={}, isPerfect={}",
                user.getUserId(), quiz.getQuizId(), score, isPerfect);

        // 꿀단지 보상 처리
        QuizResultResponse.HoneyJarRewardInfo rewardInfo = processHoneyJarReward(
                user, quiz, quizResult, isPerfect
        );

        return QuizResultResponse.of(quizResult, rewardInfo, answerResults);
    }

    // 동화 완독 처리 + 꿀단지 지급
    // POST /api/quiz/story-complete
    @Transactional
    public QuizResultResponse.HoneyJarRewardInfo completeStory(String email, Long storyId) {
        User user = getUserByEmail(email);
        Story story = getStoryWithAccess(user, storyId);

        // 완독 상태 조회 또는 생성
        StoryReadStatus readStatus = storyReadStatusRepository
                .findByUserAndStory(user, story)
                .orElseGet(() -> StoryReadStatus.builder()
                        .user(user)
                        .story(story)
                        .build());

        // 이미 완독 보상을 받은 경우 중복 지급 방지
        if (Boolean.TRUE.equals(readStatus.getReadHoneyJarRewarded())) {
            log.info("이미 완독 보상 지급됨 - userId={}, storyId={}", user.getUserId(), storyId);

            var honeyJar = honeyJarService.getOrCreateHoneyJar(user);
            return buildRewardInfo(0, honeyJar.getCount(), false, "이미 완독 보상을 받으셨어요! 🍯");
        }

        // 완독 처리
        readStatus.setIsCompleted(true);
        readStatus.setCompletedAt(LocalDateTime.now());
        readStatus.setReadHoneyJarRewarded(true);
        storyReadStatusRepository.save(readStatus);

        // 꿀단지 +1 지급 및 자동 차감 확인
        boolean autoUsed = honeyJarService.addHoneyJarAndCheckAutoUse(
                user, HoneyJarAction.EARN_STORY_COMPLETE, storyId
        );

        var honeyJar = honeyJarService.getOrCreateHoneyJar(user);
        String message = autoUsed
                ? "🎉 꿀단지 20개 달성! 동화 1권을 무료로 만들 수 있어요!"
                : "📖 동화를 다 읽었어요! 꿀단지 1개 획득! 🍯";

        log.info("동화 완독 처리 완료 - userId={}, storyId={}, 꿀단지자동차감={}",
                user.getUserId(), storyId, autoUsed);

        return buildRewardInfo(1, honeyJar.getCount(), autoUsed, message);
    }

    // 내부 메서드: 퀴즈 생성
    private Quiz generateQuiz(User user, Story story) {
        // 사용자 프로필에서 난이도 결정
        UserProfile profile = userProfileRepository
                .findFirstByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        QuizDifficulty difficulty = QuizDifficulty.from(
                profile.getAgeGroup(),
                profile.getFirstLanguageProficiency()
        );

        int questionCount = difficulty.getQuestionCount();
        String language = story.getPrimaryLanguage() != null ? story.getPrimaryLanguage() : "ko";

        // AI 퀴즈 문제 생성
        List<QuizQuestion> questions = aiQuizService.generateQuestions(
                story, difficulty, language, questionCount
        );

        // Quiz 엔티티 조립
        Quiz quiz = Quiz.builder()
                .story(story)
                .language(language)
                .difficulty(difficulty)
                .totalQuestions(questions.size())
                .build();

        questions.forEach(quiz::addQuestion);
        Quiz savedQuiz = quizRepository.save(quiz);

        log.info("퀴즈 생성 완료 - storyId={}, quizId={}, 문제수={}, 난이도={}",
                story.getStoryId(), savedQuiz.getQuizId(), questions.size(), difficulty);

        return savedQuiz;
    }

    // 내부 메서드: 퀴즈 100점 꿀단지 보상 처리
    private QuizResultResponse.HoneyJarRewardInfo processHoneyJarReward(
            User user, Quiz quiz, QuizResult quizResult, boolean isPerfect
    ) {
        Story story = quiz.getStory();

        // 완독 상태 조회 (없으면 초기화)
        StoryReadStatus readStatus = storyReadStatusRepository
                .findByUserAndStory(user, story)
                .orElseGet(() -> storyReadStatusRepository.save(
                        StoryReadStatus.builder().user(user).story(story).build()
                ));

        int earnedHoneyJars = 0;
        boolean autoUsed = false;

        // 100점 + 아직 퀴즈 보상을 받지 않은 경우만 지급
        if (isPerfect && !Boolean.TRUE.equals(readStatus.getQuizHoneyJarRewarded())) {
            readStatus.setQuizHoneyJarRewarded(true);
            storyReadStatusRepository.save(readStatus);

            quizResult.setHoneyJarRewarded(true);
            quizResultRepository.save(quizResult);

            autoUsed = honeyJarService.addHoneyJarAndCheckAutoUse(
                    user, HoneyJarAction.EARN_QUIZ_PERFECT, story.getStoryId()
            );
            earnedHoneyJars = 1;

            log.info("퀴즈 100점 꿀단지 지급 - userId={}, storyId={}",
                    user.getUserId(), story.getStoryId());
        }

        var honeyJar = honeyJarService.getOrCreateHoneyJar(user);
        String message = buildRewardMessage(isPerfect, earnedHoneyJars, autoUsed,
                readStatus.getQuizHoneyJarRewarded());

        return buildRewardInfo(earnedHoneyJars, honeyJar.getCount(), autoUsed, message);
    }

    private String buildRewardMessage(boolean isPerfect, int earned, boolean autoUsed, boolean alreadyRewarded) {
        if (!isPerfect) return "퀴즈 100점 달성 시 꿀단지를 받을 수 있어요! 🍯";
        if (alreadyRewarded && earned == 0) return "이미 이 동화에서 퀴즈 보상을 받으셨어요!";
        if (autoUsed) return "🎉 꿀단지 20개 달성! 동화 1권을 무료로 만들 수 있어요!";
        return "🏆 100점 달성! 꿀단지 1개 획득! 🍯";
    }

    private QuizResultResponse.HoneyJarRewardInfo buildRewardInfo(
            int earned, int currentCount, boolean autoUsed, String message
    ) {
        return QuizResultResponse.HoneyJarRewardInfo.builder()
                .earnedHoneyJars(earned)
                .currentHoneyJarCount(currentCount)
                .canGenerateFree(currentCount >= 20)
                .autoUsedForFreeGeneration(autoUsed)
                .rewardMessage(message)
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private Story getStoryWithAccess(User user, Long storyId) {
        Story story = storyRepository.findByIdWithSlides(storyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        // 본인 동화이거나 공개 동화만 퀴즈 가능
        if (!story.getUser().getUserId().equals(user.getUserId()) && !story.getIsPublic()) {
            throw new BusinessException(ErrorCode.STORY_ACCESS_DENIED);
        }
        return story;
    }
}
