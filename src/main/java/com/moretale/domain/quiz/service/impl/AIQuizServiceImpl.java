package com.moretale.domain.quiz.service.impl;

import com.moretale.domain.quiz.dto.AIQuizJobRequest;
import com.moretale.domain.quiz.dto.AIQuizJobResponse;
import com.moretale.domain.quiz.dto.AIQuizResultResponse;
import com.moretale.domain.quiz.entity.*;
import com.moretale.domain.quiz.service.AIQuizService;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.entity.StoryToken;
import com.moretale.global.config.MoreTaleProperties;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 서버 연동 퀴즈 생성 구현체
 *
 * 흐름:
 *  1. Story 엔티티에서 슬라이드 텍스트와 핵심 단어 수집
 *  2. POST /internal/ai/quiz/jobs → job_id 획득
 *  3. GET  /internal/ai/quiz/jobs/{jobId}/result → 폴링으로 결과 획득
 *  4. AI 응답 → QuizQuestion 엔티티 변환 후 반환
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIQuizServiceImpl implements AIQuizService {

    private static final int POLL_MAX_ATTEMPTS = 20;
    private static final long POLL_INTERVAL_MS = 3000;
    private static final String DEFAULT_AI_MODEL = "gemini-2.5-flash";

    private final MoreTaleProperties properties;
    private final RestClient.Builder restClientBuilder;

    @Override
    public List<QuizQuestion> generateQuestions(
            Story story,
            QuizDifficulty difficulty,
            String language,
            int count
    ) {
        log.info("AI 퀴즈 생성 요청 - storyId={}, difficulty={}, language={}, count={}",
                story.getStoryId(), difficulty, language, count);

        AIQuizJobRequest request = buildJobRequest(story, language, count);
        String jobId = enqueueQuizJob(request);
        AIQuizResultResponse.AIQuizData quizData = pollQuizResult(jobId);
        List<QuizQuestion> questions = convertToQuizQuestions(quizData);

        log.info("AI 퀴즈 생성 완료 - storyId={}, jobId={}, 생성된 문제 수={}",
                story.getStoryId(), jobId, questions.size());

        return questions;
    }

    private AIQuizJobRequest buildJobRequest(Story story, String language, int count) {
        List<AIQuizJobRequest.AIQuizPagePayload> pages = new ArrayList<>();

        log.info("퀴즈 생성용 전체 슬라이드 수 - storyId={}, slideCount={}",
                story.getStoryId(),
                story.getSlides() != null ? story.getSlides().size() : 0
        );

        int pageNumber = 1;

        for (Slide slide : story.getSlides()) {
            log.info(
                    "퀴즈 페이지 확인 - storyId={}, slideId={}, order={}, textKrBlank={}, textNativeBlank={}",
                    story.getStoryId(),
                    slide.getSlideId(),
                    slide.getOrder(),
                    slide.getTextKr() == null || slide.getTextKr().isBlank(),
                    slide.getTextNative() == null || slide.getTextNative().isBlank()
            );

            if (isCoverSlide(slide)) {
                log.info("퀴즈 생성에서 커버 슬라이드 제외 - storyId={}, slideId={}, order={}",
                        story.getStoryId(), slide.getSlideId(), slide.getOrder());
                continue;
            }

            pages.add(
                    AIQuizJobRequest.AIQuizPagePayload.builder()
                            .pageNumber(pageNumber++)
                            .textPrimary(slide.getTextKr() != null ? slide.getTextKr() : "")
                            .textSecondary(slide.getTextNative() != null ? slide.getTextNative() : "")
                            .illustrationPrompt("")
                            .vocabulary(buildVocabulary(slide))
                            .build()
            );
        }

        log.info("AI 퀴즈 요청 페이지 번호 - storyId={}, pageNumbers={}",
                story.getStoryId(),
                pages.stream()
                        .map(AIQuizJobRequest.AIQuizPagePayload::getPageNumber)
                        .toList()
        );

        String primaryLanguage = language != null && !language.isBlank()
                ? language
                : getDefaultPrimaryLanguage(story);

        String secondaryLanguage = story.getSecondaryLanguage() != null
                ? story.getSecondaryLanguage()
                : "en";

        AIQuizJobRequest.AIQuizStoryPayload storyPayload =
                AIQuizJobRequest.AIQuizStoryPayload.builder()
                        .titlePrimary(story.getTitle())
                        .titleSecondary(story.getTitle())
                        .authorName("MoreTale")
                        .primaryLanguage(primaryLanguage)
                        .secondaryLanguage(secondaryLanguage)
                        .imageStyle("storybook")
                        .mainCharacterDesign(
                                story.getChildName() != null && !story.getChildName().isBlank()
                                        ? story.getChildName()
                                        : "child"
                        )
                        .pages(pages)
                        .build();

        return AIQuizJobRequest.builder()
                .callbackUrl(buildCallbackUrl())
                .storyId(String.valueOf(story.getStoryId()))
                .story(storyPayload)
                .questionCount(count)
                .model(DEFAULT_AI_MODEL)
                .build();
    }

    private List<AIQuizJobRequest.AIQuizVocabEntry> buildVocabulary(Slide slide) {
        List<AIQuizJobRequest.AIQuizVocabEntry> vocabulary = new ArrayList<>();

        if (slide.getTokens() == null || slide.getTokens().isEmpty()) {
            return vocabulary;
        }

        for (StoryToken token : slide.getTokens()) {
            if (!Boolean.TRUE.equals(token.getHighlight())) {
                continue;
            }

            vocabulary.add(
                    AIQuizJobRequest.AIQuizVocabEntry.builder()
                            .entryId(token.getTokenId() != null ? "token-" + token.getTokenId() : "")
                            .primaryWord(token.getText() != null ? token.getText() : "")
                            .secondaryWord(token.getTranslation() != null ? token.getTranslation() : "")
                            .primaryDefinition(token.getDefinition() != null ? token.getDefinition() : "")
                            .secondaryDefinition(token.getSecondaryDefinition() != null
                                    ? token.getSecondaryDefinition()
                                    : "")
                            .build()
            );
        }

        return vocabulary;
    }

    private String buildCallbackUrl() {
        MoreTaleProperties.Ai ai = properties.getAi();
        String baseUrl = ai.getCallbackBaseUrl();

        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:8080/internal/ai/callback";
        }

        return baseUrl.replaceAll("/+$", "") + "/internal/ai/callback";
    }

    private String getDefaultPrimaryLanguage(Story story) {
        if (story.getPrimaryLanguage() != null && !story.getPrimaryLanguage().isBlank()) {
            return story.getPrimaryLanguage();
        }
        return "ko";
    }

    private boolean isCoverSlide(Slide slide) {
        if (slide.getOrder() == null || slide.getOrder() != 0) {
            return false;
        }

        boolean textKrEmpty = slide.getTextKr() == null || slide.getTextKr().isBlank();
        boolean textNativeEmpty = slide.getTextNative() == null || slide.getTextNative().isBlank();

        return textKrEmpty && textNativeEmpty;
    }

    private String enqueueQuizJob(AIQuizJobRequest request) {
        try {
            AIQuizJobResponse response = aiClient().post()
                    .uri("/internal/ai/quiz/jobs")
                    .body(request)
                    .retrieve()
                    .body(AIQuizJobResponse.class);

            if (response == null || response.getJobId() == null) {
                throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
            }

            log.info("퀴즈 job 등록 완료 - jobId={}, status={}",
                    response.getJobId(), response.getStatus());

            return response.getJobId();

        } catch (RestClientResponseException e) {
            log.error("퀴즈 job 등록 실패 - status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

    private AIQuizResultResponse.AIQuizData pollQuizResult(String jobId) {
        for (int attempt = 1; attempt <= POLL_MAX_ATTEMPTS; attempt++) {
            try {
                log.info("퀴즈 결과 폴링 - jobId={}, attempt={}/{}",
                        jobId, attempt, POLL_MAX_ATTEMPTS);

                AIQuizResultResponse result = aiClient().get()
                        .uri("/internal/ai/quiz/jobs/{jobId}/result", jobId)
                        .retrieve()
                        .body(AIQuizResultResponse.class);

                if (result == null) {
                    throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
                }

                if ("completed".equalsIgnoreCase(result.getStatus())) {
                    if (result.getData() == null) {
                        log.error("퀴즈 job 완료됐지만 data가 null - jobId={}", jobId);
                        throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
                    }

                    log.info("퀴즈 job 완료 - jobId={}", jobId);
                    return result.getData();
                }

                if ("failed".equalsIgnoreCase(result.getStatus())) {
                    log.error("퀴즈 job 실패 - jobId={}, error={}", jobId, result.getError());
                    throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
                }

                log.debug("퀴즈 job 진행 중 - jobId={}, status={}", jobId, result.getStatus());
                Thread.sleep(POLL_INTERVAL_MS);

            } catch (RestClientResponseException e) {
                if (e.getStatusCode().value() == 409) {
                    log.debug("퀴즈 결과 아직 미준비(409) - jobId={}, attempt={}", jobId, attempt);
                    sleepForNextPoll();
                    continue;
                }

                if (e.getStatusCode().value() == 404) {
                    log.error("퀴즈 job을 찾을 수 없음 - jobId={}", jobId);
                    throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
                }

                log.error("퀴즈 결과 조회 실패 - jobId={}, status={}, body={}",
                        jobId, e.getStatusCode(), e.getResponseBodyAsString(), e);
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
            }
        }

        log.error("퀴즈 job 폴링 타임아웃 - jobId={}, maxAttempts={}",
                jobId, POLL_MAX_ATTEMPTS);
        throw new BusinessException(ErrorCode.AI_SERVICE_TIMEOUT);
    }

    private void sleepForNextPoll() {
        try {
            Thread.sleep(POLL_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR);
        }
    }

    private List<QuizQuestion> convertToQuizQuestions(AIQuizResultResponse.AIQuizData quizData) {
        List<QuizQuestion> questions = new ArrayList<>();

        if (quizData.getQuestions() == null || quizData.getQuestions().isEmpty()) {
            log.warn("AI가 반환한 문제 목록이 비어있음 - storyId={}", quizData.getStoryId());
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }

        for (int i = 0; i < quizData.getQuestions().size(); i++) {
            AIQuizResultResponse.AIQuizQuestionData qData = quizData.getQuestions().get(i);

            EvaluationType evaluationType = mapSkillToEvaluationType(qData.getSkill());

            String correctAnswer = qData.getAnswer() != null
                    ? qData.getAnswer().getChoiceId()
                    : "1";

            QuizQuestion question = QuizQuestion.builder()
                    .questionType(QuestionType.MULTIPLE_CHOICE)
                    .evaluationType(evaluationType)
                    .questionOrder(i + 1)
                    .questionText(qData.getQuestionText())
                    .correctAnswer(correctAnswer)
                    .explanation(qData.getExplanation())
                    .build();

            if (qData.getChoices() != null) {
                for (AIQuizResultResponse.AIQuizChoice choice : qData.getChoices()) {
                    try {
                        int optionOrder = Integer.parseInt(choice.getChoiceId());
                        question.addOption(
                                QuizOption.builder()
                                        .optionOrder(optionOrder)
                                        .optionText(choice.getText())
                                        .build()
                        );
                    } catch (NumberFormatException e) {
                        log.warn("보기 choice_id 파싱 실패 - choiceId={}", choice.getChoiceId());
                    }
                }
            }

            questions.add(question);

            log.debug("문제 변환 완료 - order={}, skill={}, evaluationType={}, correctAnswer={}",
                    i + 1, qData.getSkill(), evaluationType, correctAnswer);
        }

        log.info("AI 응답 변환 완료 - 총 {}개 문제", questions.size());
        return questions;
    }

    private EvaluationType mapSkillToEvaluationType(String skill) {
        if (skill == null) {
            return EvaluationType.STORY;
        }

        return switch (skill.toUpperCase()) {
            case "VOCABULARY" -> EvaluationType.VOCABULARY;
            default -> EvaluationType.STORY;
        };
    }

    private RestClient aiClient() {
        MoreTaleProperties.Ai ai = properties.getAi();
        RestClient.Builder builder = restClientBuilder.baseUrl(ai.resolveBaseUrl());

        if (ai.getApiKey() != null && !ai.getApiKey().isBlank()) {
            builder.defaultHeader("X-API-Key", ai.getApiKey());
        }

        return builder.build();
    }
}
