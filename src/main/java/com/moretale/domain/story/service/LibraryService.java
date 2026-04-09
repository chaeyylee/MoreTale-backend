package com.moretale.domain.story.service;

import com.moretale.domain.story.dto.StoryLibraryCardResponse;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.domain.vocabulary.repository.VocabularyEntryRepository; // 단어장 리포지토리 주입 [cite: 70]
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

/**
 * 도서관 전용 서비스
 * - 도서관 = 사용자가 생성한 동화 자체를 관리하는 영역 [cite: 253]
 * - 삭제 = 동화 자체 삭제 (숨김 아님) [cite: 253, 260]
 * - 삭제 시 Story → Slide → StoryToken → VocabularyEntry 순으로 연쇄 삭제 로직 포함 [cite: 260, 361]
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryService {

    private final StoryRepository storyRepository;
    private final UserRepository userRepository;
    private final VocabularyEntryRepository vocabularyEntryRepository; // 명시적 삭제를 위해 주입 [cite: 70, 254]

    /**
     * 도서관 목록 조회 (페이징 + 정렬)
     *
     * 프론트 정렬 파라미터 매핑: [cite: 254, 319]
     * 최신순   → GET /api/library?sort=createdAt,desc
     * 오래된순 → GET /api/library?sort=createdAt,asc
     * 가나다순 → GET /api/library?sort=title,asc
     *
     * @param email    인증된 사용자 이메일
     * @param pageable Spring Pageable (sort 파라미터 포함)
     * @return 도서관 카드 Page
     */
    public Page<StoryLibraryCardResponse> getLibrary(String email, Pageable pageable) {
        User user = getUserByEmail(email);

        // 정렬 필드 화이트리스트 검증 (허용: createdAt, title) [cite: 256, 382]
        Pageable safePageable = buildSafePageable(pageable);

        Page<Story> stories = storyRepository.findByUserIdWithSlides(
                user.getUserId(), safePageable
        );

        log.info("도서관 조회 - userId={}, page={}, sort={}, totalElements={}",
                user.getUserId(),
                pageable.getPageNumber(),
                pageable.getSort(),
                stories.getTotalElements());

        return stories.map(StoryLibraryCardResponse::from);
    }

    /**
     * 도서관에서 동화 삭제
     * - Story 삭제 시 외래 키 제약 조건(VocabularyEntry 참조)을 해결하기 위해 자식 데이터를 먼저 삭제합니다. [cite: 386, 390]
     *
     * @param email   인증된 사용자 이메일
     * @param storyId 삭제할 동화 ID
     */
    @Transactional
    public void deleteFromLibrary(String email, Long storyId) {
        User user = getUserByEmail(email);

        Story story = storyRepository.findByStoryIdAndUser(storyId, user)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));

        // 1. 외래 키 제약 조건 위반 방지를 위해 관련 단어장 데이터를 명시적으로 먼저 삭제 [cite: 76, 240, 386]
        // DB의 @OnDelete(CASCADE)가 설정되어 있더라도 JPA의 삭제 순서 최적화 과정에서 에러가 발생할 수 있으므로 안전하게 처리합니다.
        vocabularyEntryRepository.deleteAllByStory(story);

        // 2. 이후 동화 삭제 (JPA Cascade에 의해 Slide, StoryToken이 함께 삭제됨) [cite: 29, 262, 361]
        storyRepository.delete(story);

        log.info("도서관 동화 및 관련 단어장 데이터 완전 삭제 완료 - userId={}, storyId={}, title={}",
                user.getUserId(), storyId, story.getTitle());
    }

    /**
     * 정렬 필드 화이트리스트 검증 [cite: 264]
     * - 허용 필드: createdAt, title [cite: 271, 382]
     * - 허용되지 않은 필드가 오면 기본값(createdAt DESC)으로 대체하여 SQL Injection 방지 [cite: 268, 382]
     */
    private Pageable buildSafePageable(Pageable pageable) {
        Sort sort = pageable.getSort();

        // Sort가 없거나 비어있으면 기본값 적용 [cite: 265]
        if (sort.isUnsorted()) {
            return PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by(Sort.Direction.DESC, "createdAt")
            );
        }

        // 허용 필드 외 정렬 필드 제거 후 재구성 [cite: 266, 267]
        Sort safeSort = Sort.by(
                sort.stream()
                        .filter(order -> isAllowedSortField(order.getProperty()))
                        .map(order -> order.isAscending()
                                ? Sort.Order.asc(order.getProperty())
                                : Sort.Order.desc(order.getProperty()))
                        .toList()
        );

        // 모든 정렬 필드가 거부된 경우 기본값 사용 [cite: 268, 269]
        if (safeSort.isUnsorted()) {
            safeSort = Sort.by(Sort.Direction.DESC, "createdAt");
        }

        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), safeSort);
    }

    private boolean isAllowedSortField(String field) {
        return "createdAt".equals(field) || "title".equals(field);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
