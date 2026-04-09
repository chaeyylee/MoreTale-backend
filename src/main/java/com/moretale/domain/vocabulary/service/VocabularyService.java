package com.moretale.domain.vocabulary.service;

import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.entity.StoryToken;
import com.moretale.domain.story.repository.SlideRepository;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.story.repository.StoryTokenRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.domain.vocabulary.dto.request.VocabularyCreateRequest;
import com.moretale.domain.vocabulary.dto.request.VocabularyPatchRequest;
import com.moretale.domain.vocabulary.dto.request.VocabularySearchCondition;
import com.moretale.domain.vocabulary.dto.response.VocabularyResponse;
import com.moretale.domain.vocabulary.dto.response.VocabularyStoryResponse;
import com.moretale.domain.vocabulary.entity.VocabularyEntry;
import com.moretale.domain.vocabulary.exception.*;
import com.moretale.domain.vocabulary.repository.VocabularyEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VocabularyService {

    private final VocabularyEntryRepository vocabularyEntryRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final SlideRepository slideRepository;
    private final StoryTokenRepository storyTokenRepository;

    // 단어 저장
    @Transactional
    public VocabularyResponse save(Long userId, VocabularyCreateRequest request) {

        // 1. 연관 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId=" + userId));

        Story story = storyRepository.findById(request.getStoryId())
                .orElseThrow(() -> new RuntimeException("동화를 찾을 수 없습니다. storyId=" + request.getStoryId()));

        Slide slide = slideRepository.findById(request.getSlideId())
                .orElseThrow(() -> new RuntimeException("슬라이드를 찾을 수 없습니다. slideId=" + request.getSlideId()));

        StoryToken token = storyTokenRepository.findById(request.getTokenId())
                .orElseThrow(() -> new TokenNotFoundException(request.getTokenId()));

        // 2. highlight 단어인지 검증 (highlight=false인 토큰은 단어장 저장 불가)
        if (!token.getHighlight()) {
            throw new IllegalArgumentException("하이라이트 단어만 저장할 수 있습니다. tokenId=" + request.getTokenId());
        }

        // 3. 중복 저장 방지 (같은 사용자 + 같은 동화 + 같은 정규화 단어)
        String normalizedWord = token.getText().trim().toLowerCase();
        if (vocabularyEntryRepository.existsByUser_UserIdAndStory_StoryIdAndNormalizedWord(
                userId, request.getStoryId(), normalizedWord)) {
            throw new VocabularyDuplicateException(normalizedWord);
        }

        // 4. 저장
        VocabularyEntry entry = VocabularyEntry.builder()
                .user(user)
                .story(story)
                .slide(slide)
                .storyToken(token)
                .word(token.getText())
                .normalizedWord(normalizedWord)
                .translation(token.getTranslation())
                .definition(token.getDefinition())
                .sourceLanguage(token.getSourceLanguage())
                .targetLanguage(token.getTargetLanguage())
                .audioUrl(token.getAudioUrl())
                .build();

        VocabularyEntry saved = vocabularyEntryRepository.save(entry);
        log.info("단어장 저장 완료 - userId={}, word={}, storyId={}", userId, saved.getWord(), request.getStoryId());

        return VocabularyResponse.from(saved);
    }

    // 전체 단어장 조회
    public Page<VocabularyResponse> getAll(Long userId, Pageable pageable) {
        Pageable safePageable = buildSafeVocabularyPageable(pageable);
        return vocabularyEntryRepository
                .findByUser_UserId(userId, safePageable)
                .map(VocabularyResponse::from);
    }

    // 특정 동화 기준 단어장 조회
    public Page<VocabularyResponse> getByStory(Long userId, Long storyId, Pageable pageable) {
        Pageable safePageable = buildSafeVocabularyPageable(pageable);
        return vocabularyEntryRepository
                .findByUser_UserIdAndStory_StoryId(userId, storyId, safePageable)
                .map(VocabularyResponse::from);
    }

    // 통합 필터 조회
    /**
     * 단어장 통합 필터 조회
     * 프론트 정렬 파라미터 매핑:
     *   최신순   → sort=createdAt,desc
     *   오래된순 → sort=createdAt,asc
     *   가나다순 → sort=word,asc
     *
     * @param userId    사용자 ID
     * @param condition 필터 조건 (storyId, favorite, keyword)
     * @param pageable  페이징 + 정렬
     */
    public Page<VocabularyResponse> getWithFilters(
            Long userId,
            VocabularySearchCondition condition,
            Pageable pageable
    ) {
        Pageable safePageable = buildSafeVocabularyPageable(pageable);

        // keyword 정규화: blank이면 null로 처리
        String keyword = StringUtils.hasText(condition.getKeyword())
                ? condition.getKeyword().trim()
                : null;

        log.info("단어장 필터 조회 - userId={}, storyId={}, favorite={}, keyword={}, sort={}",
                userId,
                condition.getStoryId(),
                condition.getFavorite(),
                keyword,
                safePageable.getSort());

        return vocabularyEntryRepository
                .findWithFilters(
                        userId,
                        condition.getStoryId(),
                        condition.getFavorite(),
                        keyword,
                        safePageable
                )
                .map(VocabularyResponse::from);
    }

    // 단어가 저장된 동화 목록 조회
    public List<VocabularyStoryResponse> getStoriesWithVocabulary(Long userId) {
        List<Story> stories = vocabularyEntryRepository.findDistinctStoriesByUserId(userId);

        return stories.stream()
                .map(story -> {
                    long wordCount = vocabularyEntryRepository
                            .countByUser_UserIdAndStory_StoryId(userId, story.getStoryId());
                    return VocabularyStoryResponse.from(story, wordCount);
                })
                .toList();
    }

    // 단어장 항목 수정 (기존 유지)
    @Transactional
    public VocabularyResponse patch(Long userId, Long vocabularyId, VocabularyPatchRequest request) {
        VocabularyEntry entry = findOwnedEntry(userId, vocabularyId);

        if (request.getIsFavorite() != null) {
            entry.updateFavorite(request.getIsFavorite());
        }
        if (request.getLearningStatus() != null) {
            entry.updateLearningStatus(request.getLearningStatus());
        }
        if (request.getMemo() != null) {
            entry.updateMemo(request.getMemo());
        }

        log.info("단어장 수정 완료 - userId={}, vocabularyId={}", userId, vocabularyId);
        return VocabularyResponse.from(entry);
    }

    // 단어 삭제
    @Transactional
    public void delete(Long userId, Long vocabularyId) {
        VocabularyEntry entry = findOwnedEntry(userId, vocabularyId);
        vocabularyEntryRepository.delete(entry);
        log.info("단어장 삭제 완료 - userId={}, vocabularyId={}", userId, vocabularyId);
    }

    // 내부 공통 메서드

    // 소유권 확인 후 엔티티 반환 (없으면 404, 권한 없으면 403)
    private VocabularyEntry findOwnedEntry(Long userId, Long vocabularyId) {
        // 먼저 존재 여부 확인
        if (!vocabularyEntryRepository.existsById(vocabularyId)) {
            throw new VocabularyNotFoundException(vocabularyId);
        }
        // 소유권 확인
        return vocabularyEntryRepository
                .findByVocabularyIdAndUser_UserId(vocabularyId, userId)
                .orElseThrow(VocabularyAccessDeniedException::new);
    }

    /**
     * 단어장 정렬 필드 화이트리스트 검증
     * - 허용 필드: createdAt, word
     * - 허용되지 않은 필드는 기본값(createdAt DESC)으로 대체
     */
    private Pageable buildSafeVocabularyPageable(Pageable pageable) {
        Sort sort = pageable.getSort();

        if (sort.isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
        }

        Sort safeSort = Sort.by(
                sort.stream()
                        .filter(order -> isAllowedVocabSortField(order.getProperty()))
                        .map(order -> order.isAscending()
                                ? Sort.Order.asc(order.getProperty())
                                : Sort.Order.desc(order.getProperty()))
                        .toList()
        );

        if (safeSort.isUnsorted()) {
            safeSort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    }

    private boolean isAllowedVocabSortField(String field) {
        return "createdAt".equals(field) || "word".equals(field);
    }
}
