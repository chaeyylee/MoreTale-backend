package com.moretale.domain.story.service;

import com.moretale.domain.story.dto.StoryLibraryCardResponse;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 도서관 전용 서비스
 * - vocabulary_entries는 @OnDelete(CASCADE)로 DB 레벨에서 자동 삭제되므로
 *   별도 vocabularyEntryRepository.deleteAllByStory() 호출 불필요
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryService {

    private final StoryRepository storyRepository;

    /**
     * 도서관 목록 조회 (페이징 + 정렬) — N+1 및 메모리 페이징 개선 버전
     *
     * ─ 개선 전 흐름 ───────────────────────────────────────────────────────────
     *   findByUserIdWithSlides(): LEFT JOIN FETCH + Pageable 조합
     *   → HHH90003004: 전체 결과를 메모리에 올린 후 페이징 처리
     *   → 동화가 많을수록 OOM 위험 증가
     *
     * ─ 개선 후 흐름 ───────────────────────────────────────────────────────────
     *   1단계: story_id만 OFFSET/LIMIT으로 정확히 페이징 (메모리 페이징 없음)
     *   2단계: 해당 ID 목록으로 slides까지 JOIN FETCH 일괄 조회 (IN 절, 최대 20개)
     *   3단계: 원래 ID 순서대로 재정렬 후 Page 재구성
     *   → 합계: 최대 3번 (count 포함)
     *
     * ─ 프론트 정렬 파라미터 매핑 ──────────────────────────────────────────────
     *   최신순   → GET /api/library?sort=createdAt,desc
     *   오래된순 → GET /api/library?sort=createdAt,asc
     *   가나다순 → GET /api/library?sort=title,asc
     *
     * @param userId   인증된 사용자 ID
     * @param pageable Spring Pageable (sort 파라미터 포함)
     * @return 도서관 카드 Page
     */
    public Page<StoryLibraryCardResponse> getLibrary(Long userId, Pageable pageable) {
        Pageable safePageable = buildSafePageable(pageable);

        // 1단계: story_id만 페이징 조회
        Page<Long> idPage = storyRepository.findIdsByUserId(userId, safePageable);
        List<Long> ids = idPage.getContent();

        log.info("도서관 조회 - userId={}, page={}, sort={}, totalElements={}",
                userId,
                pageable.getPageNumber(),
                safePageable.getSort(),
                idPage.getTotalElements());

        // 결과가 없으면 빈 페이지 즉시 반환
        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), safePageable, 0);
        }

        // 2단계: ID 목록으로 slides까지 JOIN FETCH 일괄 조회
        List<Story> stories = storyRepository.fetchByIdsWithSlides(ids);

        // 3단계: IN 절 조회는 DB 반환 순서가 보장되지 않으므로 원래 순서로 재정렬
        Map<Long, Story> storyMap = stories.stream()
                .collect(Collectors.toMap(Story::getStoryId, Function.identity()));

        List<StoryLibraryCardResponse> content = ids.stream()
                .filter(storyMap::containsKey)
                .map(id -> StoryLibraryCardResponse.from(storyMap.get(id)))
                .collect(Collectors.toList());

        // 4단계: 1단계의 totalElements를 재사용하여 Page 재구성
        return new PageImpl<>(content, safePageable, idPage.getTotalElements());
    }

    /**
     * 도서관에서 동화 삭제
     * - vocabulary_entries: @OnDelete(CASCADE)로 DB 레벨 자동 삭제
     * - slides, story_tokens: JPA CascadeType.ALL + orphanRemoval로 자동 삭제
     */
    @Transactional
    public void deleteFromLibrary(Long userId, Long storyId) {
        Story story = storyRepository.findByStoryIdAndUserId(storyId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        storyRepository.delete(story);

        log.info("도서관 동화 삭제 완료 - userId={}, storyId={}, title={}",
                userId, storyId, story.getTitle());
    }

    private Pageable buildSafePageable(Pageable pageable) {
        Sort sort = pageable.getSort();

        // Sort가 없거나 비어있으면 기본값 적용
        if (sort.isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
        }

        // 허용 필드 외 정렬 필드 제거 후 재구성
        Sort safeSort = Sort.by(
                sort.stream()
                        .filter(order -> isAllowedSortField(order.getProperty()))
                        .map(order -> order.isAscending()
                                ? Sort.Order.asc(order.getProperty())
                                : Sort.Order.desc(order.getProperty()))
                        .toList()
        );

        // 모든 정렬 필드가 거부된 경우 기본값 사용
        if (safeSort.isUnsorted()) {
            safeSort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    }

    private boolean isAllowedSortField(String field) {
        return "createdAt".equals(field) || "title".equals(field);
    }
}
