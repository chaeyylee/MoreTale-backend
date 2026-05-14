package com.moretale.domain.story.service;

import com.moretale.domain.profile.entity.Language;
import com.moretale.domain.profile.entity.StoryPreference;
import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.profile.util.LanguageLocaleMapper;
import com.moretale.domain.story.dto.StoryGenerateRequest;
import com.moretale.domain.story.dto.StoryGenerateResponse;
import com.moretale.domain.story.dto.StoryInitResponse;
import com.moretale.domain.story.dto.StoryListResponse;
import com.moretale.domain.story.dto.StoryResponse;
import com.moretale.domain.story.dto.StorySaveRequest;
import com.moretale.domain.story.dto.StoryShareRequest;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.entity.StoryToken;
import com.moretale.domain.story.enums.TraditionalTale;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.story.util.PromptBuilder;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final AIStoryService aiStoryService;
    private final TTSService ttsService;
    private final StoryTokenService storyTokenService;
    private final EntityManager em;

    // 온보딩 데이터 기반 동화 생성 초기값 조회
    // GET /api/stories/init
    public StoryInitResponse getStoryInitData(Long userId, Long profileId) {
        User user = getUserById(userId);
        UserProfile profile = getUserProfile(user, profileId);

        // 이야기 선호도에 맞는 전래동화 자동 매핑
        TraditionalTale recommendedTale;

        if (profile.getStoryPreference() == StoryPreference.CUSTOM
                && profile.getCustomStoryPreference() != null) {
            recommendedTale = TraditionalTale.findByCustomText(profile.getCustomStoryPreference());
        } else {
            recommendedTale = TraditionalTale.findByPreference(profile.getStoryPreference());
        }

        log.info("동화 초기값 조회 - userId={}, profileId={}, 추천 전래동화={}",
                userId, profile.getProfileId(), recommendedTale.getTitle());

        return StoryInitResponse.from(profile, recommendedTale.getTitle());
    }

    // 온보딩 직후 자동 동화 생성 (추천 동화)
    // POST /api/stories/auto-generate
    @Transactional
    public StoryGenerateResponse autoGenerateStory(Long userId, Long profileId) {
        User user = getUserById(userId);
        UserProfile profile = getUserProfile(user, profileId);

        // 추천 전래동화 선택
        TraditionalTale tale;

        if (profile.getStoryPreference() == StoryPreference.CUSTOM
                && profile.getCustomStoryPreference() != null) {
            tale = TraditionalTale.findByCustomText(profile.getCustomStoryPreference());

            // CUSTOM이면 전래동화 대신 사용자 입력 텍스트 사용
            if (tale == TraditionalTale.CUSTOM) {
                log.info("사용자 맞춤 동화 생성 - customStoryPreference 사용");
            }
        } else {
            tale = TraditionalTale.findByPreference(profile.getStoryPreference());
        }

        log.info("자동 동화 생성 시작 - userId={}, profileId={}, 전래동화={}",
                userId, profile.getProfileId(), tale.getTitle());

        // 프롬프트 결정: CUSTOM이면 사용자 입력, 아니면 전래동화 설명
        String basePrompt = (tale == TraditionalTale.CUSTOM && profile.getCustomStoryPreference() != null)
                ? profile.getCustomStoryPreference()
                : tale.getDescription();

        // 자동 생성 요청 구성
        StoryGenerateRequest autoRequest = StoryGenerateRequest.builder()
                .prompt(basePrompt)
                .profileId(profileId)
                .childName(profile.getChildName())
                .primaryLanguage(resolvePrimaryLanguage(profile))
                .secondaryLanguage(resolveSecondaryLanguage(profile))
                .ageGroup(profile.getAgeGroup())
                .childAge(profile.getChildAge())
                .firstLanguageProficiency(profile.getFirstLanguageProficiency())
                .secondLanguageProficiency(profile.getSecondLanguageProficiency())
                .firstLanguageListening(profile.getFirstLanguageListening())
                .firstLanguageSpeaking(profile.getFirstLanguageSpeaking())
                .secondLanguageListening(profile.getSecondLanguageListening())
                .secondLanguageSpeaking(profile.getSecondLanguageSpeaking())
                .storyPreference(profile.getStoryPreference())
                .customStoryPreference(profile.getCustomStoryPreference())
                .autoGenerated(true)
                .recommendedTaleTitle(tale.getTitle())
                .build();

        return generateStory(userId, autoRequest);
    }

    // 동화 생성 (AI 연동)
    @Transactional
    public StoryGenerateResponse generateStory(Long userId, StoryGenerateRequest request) {
        User user = getUserById(userId);
        UserProfile profile = getUserProfile(user, request.getProfileId());

        // 값 병합: 요청값이 없으면 프로필 데이터 사용
        String childName = request.getChildName() != null
                ? request.getChildName()
                : profile.getChildName();

        String primaryLang = request.getPrimaryLanguage() != null
                ? request.getPrimaryLanguage()
                : resolvePrimaryLanguage(profile);

        String secondaryLang = request.getSecondaryLanguage() != null
                ? request.getSecondaryLanguage()
                : resolveSecondaryLanguage(profile);

        // 온보딩 데이터가 요청에 없으면 프로필에서 가져옴
        if (request.getAgeGroup() == null) {
            request.setAgeGroup(profile.getAgeGroup());
            request.setChildAge(profile.getChildAge());
            request.setFirstLanguageProficiency(profile.getFirstLanguageProficiency());
            request.setSecondLanguageProficiency(profile.getSecondLanguageProficiency());
            request.setFirstLanguageListening(profile.getFirstLanguageListening());
            request.setFirstLanguageSpeaking(profile.getFirstLanguageSpeaking());
            request.setSecondLanguageListening(profile.getSecondLanguageListening());
            request.setSecondLanguageSpeaking(profile.getSecondLanguageSpeaking());
            request.setStoryPreference(profile.getStoryPreference());
            request.setCustomStoryPreference(profile.getCustomStoryPreference());
        }

        // 지능형 프롬프트 조립
        String enhancedPrompt = PromptBuilder.buildPrompt(
                request, childName, primaryLang, secondaryLang
        );

        // AI 동화 생성
        StoryGenerateResponse response = aiStoryService.generateStory(
                enhancedPrompt,
                childName,
                primaryLang,
                secondaryLang,
                request
        );

        // TTS locale 생성 로직 개선
        String primaryLocale = LanguageLocaleMapper.resolvePrimaryTtsLocale(profile);
        String secondaryLocale = LanguageLocaleMapper.resolveSecondaryTtsLocale(profile);
        response.getSlides().forEach(slide -> {
            try {
                if (slide.getTextKr() != null && primaryLocale != null) {
                    slide.setAudioUrlKr(
                            ttsService.generateTTS(slide.getTextKr(), primaryLocale)
                    );
                } else if (slide.getTextKr() != null) {
                    log.info("첫 번째 언어 TTS 미지원 - slideOrder={}", slide.getOrder());
                }

                if (slide.getTextNative() != null && secondaryLocale != null) {
                    slide.setAudioUrlNative(
                            ttsService.generateTTS(slide.getTextNative(), secondaryLocale)                    );
                } else if (slide.getTextNative() != null) {
                    log.info("두 번째 언어 TTS 미지원 - slideOrder={}", slide.getOrder());
                }
            } catch (Exception e) {
                log.error("TTS 생성 중 오류 발생 (건너뜀) - slideOrder={}",
                        slide.getOrder(), e);
            }
        });

        log.info("동화 생성 완료 - userId={}, 자동생성={}, 제목={}",
                userId, request.getAutoGenerated(), response.getTitle());

        return response;
    }

    // 동화 저장
    @Transactional
    public StoryResponse saveStory(Long userId, StorySaveRequest request) {
        User user = getUserById(userId);

        UserProfile profile = userProfileRepository.findById(request.getProfileId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        // 엔티티 equals() 비교 제거 → userId 기반 비교
        if (!profile.getUser().getUserId().equals(userId)) {
            log.warn("보안 위반 시도 - 요청 userId={}가 userId={}의 프로필 {}을 사용하려고 함",
                    userId, profile.getUser().getUserId(), profile.getProfileId());
            throw new BusinessException(ErrorCode.STORY_ACCESS_DENIED);
        }

        // Story 엔티티 생성
        Story story = Story.builder()
                .title(request.getTitle())
                .prompt(request.getPrompt())
                .user(user)
                .childName(profile.getChildName())
                .primaryLanguage(resolvePrimaryLanguage(profile))
                .secondaryLanguage(resolveSecondaryLanguage(profile))
                .isPublic(false)
                .build();

        // Slide 엔티티 생성
        if (request.getSlides() != null) {
            request.getSlides().forEach(slideReq -> {
                Slide slide = Slide.builder()
                        .order(slideReq.getOrder())
                        .imageUrl(slideReq.getImageUrl())
                        .textKr(slideReq.getTextKr())
                        .textNative(slideReq.getTextNative())
                        .audioUrlKr(slideReq.getAudioUrlKr())
                        .audioUrlNative(slideReq.getAudioUrlNative())
                        .build();
                story.addSlide(slide);
            });
        }

        // Story + Slide 먼저 저장 (slide_id 획득 필요)
        Story savedStory = storyRepository.save(story);

        // 1차 flush: Story + Slide INSERT 실행, slideId 확보
        em.flush();

        String sourceLanguage = resolvePrimaryLanguage(profile);
        String targetLanguage = resolveSecondaryLanguage(profile);

        savedStory.getSlides().forEach(slide -> {
            try {
                List<StoryToken> tokens = storyTokenService.generateTokensForSlide(
                        slide, sourceLanguage, targetLanguage
                );
                tokens.forEach(slide::addToken);
            } catch (Exception e) {
                log.error("토큰 생성 실패 (건너뜀) - slideId={}", slide.getSlideId(), e);
            }
        });

        // 2차 flush: StoryToken INSERT 실행, tokenId 확보
        em.flush();

        log.info("동화 저장 완료 - storyId={}, userId={}",
                savedStory.getStoryId(), userId);

        return StoryResponse.from(savedStory);
    }

    // 특정 동화 상세 조회 (슬라이드 포함)
    public StoryResponse getStoryDetail(Long userId, Long storyId) {
        Story story = storyRepository.findByIdWithSlides(storyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        // story.getUser().equals(user) → userId 기반 비교
        boolean isOwner = story.getUser().getUserId().equals(userId);
        if (!isOwner && !story.getIsPublic()) {
            throw new BusinessException(ErrorCode.STORY_ACCESS_DENIED);
        }

        return StoryResponse.from(story);
    }

    // 내 동화 목록 조회
    public List<StoryListResponse> getMyStories(Long userId) {
        // User 엔티티 조회 없이 userId 기반 Repository 쿼리 직접 사용
        return storyRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(StoryListResponse::from)
                .collect(Collectors.toList());
    }

    // 공개 동화 목록 조회
    public List<StoryListResponse> getPublicStories() {
        return storyRepository.findByIsPublicTrueOrderByCreatedAtDesc()
                .stream()
                .map(StoryListResponse::from)
                .collect(Collectors.toList());
    }

    // 동화 공유 설정 변경
    @Transactional
    public void updateStoryShareStatus(Long userId, Long storyId, StoryShareRequest request) {
        // User 엔티티 조회 없이 userId + storyId 기반으로 직접 조회
        Story story = storyRepository.findByStoryIdAndUserId(storyId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        story.setIsPublic(request.getIsPublic());
    }

    // 동화 삭제
    @Transactional
    public void deleteStory(Long userId, Long storyId) {
        // User 엔티티 조회 없이 userId + storyId 기반으로 직접 조회
        Story story = storyRepository.findByStoryIdAndUserId(storyId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        storyRepository.delete(story);
    }

    // userId로 사용자 조회
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private UserProfile getUserProfile(User user, Long profileId) {
        if (profileId != null) {
            return userProfileRepository
                    .findByProfileIdAndUser_UserId(profileId, user.getUserId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
        }

        return userProfileRepository
                .findFirstByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));
    }

    /**
     * Story/TTS/토큰 생성 등 기존 문자열 기반 로직에서 사용할 첫 번째 언어 문자열 반환
     * - 일반 언어: ISO 코드 반환 (ko, en, ja ...)
     * - OTHER: 사용자가 직접 입력한 문자열 반환
     * - null: 기본값 ko
     */
    private String resolvePrimaryLanguage(UserProfile profile) {
        if (profile.getFirstLanguage() == null) {
            return "ko";
        }

        if (profile.getFirstLanguage() == Language.OTHER) {
            return profile.getCustomFirstLanguage() != null && !profile.getCustomFirstLanguage().isBlank()
                    ? profile.getCustomFirstLanguage()
                    : "other";
        }

        return profile.getFirstLanguage().getIsoCode();
    }

    /**
     * Story/TTS/토큰 생성 등 기존 문자열 기반 로직에서 사용할 두 번째 언어 문자열 반환
     * - 일반 언어: ISO 코드 반환 (ko, en, ja ...)
     * - OTHER: 사용자가 직접 입력한 문자열 반환
     * - null: 기본값 en
     */
    private String resolveSecondaryLanguage(UserProfile profile) {
        if (profile.getSecondLanguage() == null) {
            return "en";
        }

        if (profile.getSecondLanguage() == Language.OTHER) {
            return profile.getCustomSecondLanguage() != null && !profile.getCustomSecondLanguage().isBlank()
                    ? profile.getCustomSecondLanguage()
                    : "other";
        }

        return profile.getSecondLanguage().getIsoCode();
    }
}
