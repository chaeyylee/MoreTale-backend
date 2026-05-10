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
import com.moretale.domain.vocabulary.exception.TokenNotFoundException;
import com.moretale.domain.vocabulary.exception.VocabularyAccessDeniedException;
import com.moretale.domain.vocabulary.exception.VocabularyDuplicateException;
import com.moretale.domain.vocabulary.exception.VocabularyNotFoundException;
import com.moretale.domain.vocabulary.repository.VocabularyEntryRepository;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Story story = storyRepository.findById(request.getStoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        Slide slide = slideRepository.findById(request.getSlideId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SLIDE_NOT_FOUND));

        StoryToken token = storyTokenRepository.findById(request.getTokenId())
                .orElseThrow(() -> new TokenNotFoundException(request.getTokenId()));

        // 2. highlight 단어인지 검증 (highlight=false인 토큰은 단어장 저장 불가)
        if (!token.getHighlight()) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "하이라이트 단어만 저장할 수 있습니다. tokenId=" + request.getTokenId()
            );
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
        log.info("단어장 저장 완료 - userId={}, word={}, storyId={}",
                userId, saved.getWord(), request.getStoryId());

        return VocabularyResponse.from(saved);
    }

    // 전체 단어장 조회
    public Page<VocabularyResponse> getAll(Long userId, Pageable pageable) {
        // buildSafeVocabularyPageable 내부에서 isFavorite DESC가 맨 앞에 삽입됨
        Pageable safePageable = buildSafeVocabularyPageable(pageable);
        return vocabularyEntryRepository
                .findByUser_UserId(userId, safePageable)
                .map(VocabularyResponse::from);
    }

    // 특정 동화 기준 단어장 조회
    public Page<VocabularyResponse> getByStory(Long userId, Long storyId, Pageable pageable) {
        // buildSafeVocabularyPageable 내부에서 isFavorite DESC가 맨 앞에 삽입됨
        Pageable safePageable = buildSafeVocabularyPageable(pageable);
        return vocabularyEntryRepository
                .findByUser_UserIdAndStory_StoryId(userId, storyId, safePageable)
                .map(VocabularyResponse::from);
    }

    /**
     * 단어장 통합 필터 조회
     *
     * ─ 정렬 정책 (isFavorite 우선) ──────────────────────────────────────────
     *   buildSafeVocabularyPageable() 에서 isFavorite DESC 를 맨 앞에 고정하고
     *   사용자가 선택한 정렬(createdAt / word)을 그 뒤에 붙인다.
     *
     *   결과 ORDER BY 예시:
     *     최신순   →  isFavorite DESC, createdAt DESC
     *     오래된순 →  isFavorite DESC, createdAt ASC
     *     가나다순 →  isFavorite DESC, word ASC
     * ─────────────────────────────────────────────────────────────────────────
     *
     * 단, favorite=true 필터가 걸린 경우에는 결과가 모두 즐겨찾기이므로
     * isFavorite DESC 선행 정렬이 의미 없어 생략한다. (불필요한 ORDER BY 제거)
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
        // null이면 null 유지, 값이 있으면 "%keyword%" 패턴으로 변환
        // Repository의 :keywordPattern 파라미터에 전달
        String keywordPattern = StringUtils.hasText(condition.getKeyword())
                ? "%" + condition.getKeyword().trim() + "%"
                : null;

        boolean skipFavoriteSort = Boolean.TRUE.equals(condition.getFavorite());
        Pageable safePageable = buildSafeVocabularyPageable(pageable, skipFavoriteSort);

        log.info("단어장 필터 조회 - userId={}, storyId={}, favorite={}, keyword={}, sort={}",
                userId, condition.getStoryId(), condition.getFavorite(),
                keywordPattern, safePageable.getSort());

        return vocabularyEntryRepository
                .findWithFilters(
                        userId,
                        condition.getStoryId(),
                        condition.getFavorite(),
                        keywordPattern,
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

    // 단어장 항목 수정 (즐겨찾기 / 학습상태 / 메모)
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

    // 소유권 확인 후 엔티티 반환 (없으면 404, 권한 없으면 403)
    private VocabularyEntry findOwnedEntry(Long userId, Long vocabularyId) {
        if (!vocabularyEntryRepository.existsById(vocabularyId)) {
            throw new VocabularyNotFoundException(vocabularyId);
        }
        return vocabularyEntryRepository
                .findByVocabularyIdAndUser_UserId(vocabularyId, userId)
                .orElseThrow(VocabularyAccessDeniedException::new);
    }

    /**
     * 단어장 정렬 필드 화이트리스트 검증 + isFavorite DESC 선행 삽입
     *
     * ─ 정렬 규칙 ────────────────────────────────────────────────────────────
     *   1. 허용 필드: isFavorite, createdAt, word  (그 외는 무시)
     *   2. skipFavoriteSort = false 인 경우 (기본):
     *        → isFavorite DESC 를 정렬 맨 앞에 고정
     *        → 이후 사용자 요청 정렬(createdAt / word)을 추가
     *        → 사용자 정렬이 없으면 createdAt DESC 를 기본값으로 사용
     *      결과 예:
     *        sort=createdAt,desc  →  [isFavorite DESC, createdAt DESC]  (기본)
     *        sort=createdAt,asc   →  [isFavorite DESC, createdAt ASC]
     *        sort=word,asc        →  [isFavorite DESC, word ASC]
     *        sort 없음            →  [isFavorite DESC, createdAt DESC]
     *
     *   3. skipFavoriteSort = true 인 경우 (favorite=true 필터 사용 시):
     *        → isFavorite DESC 삽입 생략 (결과가 이미 전부 즐겨찾기)
     *        → 사용자 요청 정렬만 적용
     * ─────────────────────────────────────────────────────────────────────────
     *
     * @param pageable          원본 Pageable (Controller에서 전달)
     * @param skipFavoriteSort  true이면 isFavorite DESC 선행 삽입 생략
     */
    private Pageable buildSafeVocabularyPageable(Pageable pageable, boolean skipFavoriteSort) {
        Sort sort = pageable.getSort();

        // 사용자 요청 정렬에서 허용 필드(createdAt, word)만 추출
        // (isFavorite은 사용자가 직접 정렬 파라미터로 넘기는 필드가 아니므로 화이트리스트에서 제외)
        List<Sort.Order> userOrders = new ArrayList<>();
        if (sort.isSorted()) {
            sort.stream()
                    .filter(order -> isAllowedVocabSortField(order.getProperty()))
                    .map(order -> order.isAscending()
                            ? Sort.Order.asc(order.getProperty())
                            : Sort.Order.desc(order.getProperty()))
                    .forEach(userOrders::add);
        }

        // 사용자 정렬이 비어있으면 기본값 createdAt DESC 사용
        if (userOrders.isEmpty()) {
            userOrders.add(Sort.Order.desc("createdAt"));
        }

        // 최종 정렬 조합
        List<Sort.Order> finalOrders = new ArrayList<>();

        if (!skipFavoriteSort) {
            // isFavorite DESC 를 맨 앞에 고정
            finalOrders.add(Sort.Order.desc("isFavorite"));
        }

        // 사용자 정렬을 그 뒤에 추가
        finalOrders.addAll(userOrders);

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(finalOrders)
        );
    }

    /**
     * skipFavoriteSort = false 로 고정하는 편의 오버로드
     * (기존 getAll, getByStory 등에서 사용)
     */
    private Pageable buildSafeVocabularyPageable(Pageable pageable) {
        return buildSafeVocabularyPageable(pageable, false);
    }

    /**
     * 사용자가 sort 파라미터로 직접 지정할 수 있는 허용 필드
     * - createdAt : 최신순 / 오래된순
     * - word      : 가나다순
     * (isFavorite은 Service 내부에서 자동 삽입하므로 사용자 입력 허용 안 함)
     */
    private boolean isAllowedVocabSortField(String field) {
        return "createdAt".equals(field) || "word".equals(field);
    }
}
