package com.moretale.domain.story.service;

import com.moretale.domain.story.dto.StoryLibraryCardResponse;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("LibraryService 단위 테스트")
class LibraryServiceTest {

    @Mock StoryRepository storyRepository;

    @InjectMocks LibraryService libraryService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().userId(1L).email("test@example.com").build();
    }

    // ────────────────── getLibrary ──────────────────

    @Test
    @DisplayName("도서관 조회 - 정상 결과 반환")
    void getLibrary_success() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Long> idPage = new PageImpl<>(List.of(1L, 2L), pageable, 2);

        Story s1 = buildStory(1L, "동화1");
        Story s2 = buildStory(2L, "동화2");

        given(storyRepository.findIdsByUserId(eq(1L), any(Pageable.class)))
                .willReturn(idPage);
        given(storyRepository.fetchByIdsWithSlides(List.of(1L, 2L)))
                .willReturn(List.of(s1, s2));

        Page<StoryLibraryCardResponse> result = libraryService.getLibrary(1L, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getStoryId()).isEqualTo(1L);
        assertThat(result.getContent().get(1).getStoryId()).isEqualTo(2L);
    }

    @Test
    @DisplayName("도서관 조회 - 결과 없으면 빈 페이지 반환")
    void getLibrary_empty_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 20);
        given(storyRepository.findIdsByUserId(eq(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<StoryLibraryCardResponse> result = libraryService.getLibrary(1L, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("도서관 조회 - ID 순서대로 재정렬 보장")
    void getLibrary_preservesIdOrder() {
        // DB가 역순으로 반환해도 원래 순서(1,2,3)를 유지해야 함
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Long> idPage = new PageImpl<>(List.of(1L, 2L, 3L), pageable, 3);

        Story s1 = buildStory(1L, "동화1");
        Story s2 = buildStory(2L, "동화2");
        Story s3 = buildStory(3L, "동화3");

        given(storyRepository.findIdsByUserId(eq(1L), any(Pageable.class)))
                .willReturn(idPage);
        // DB에서 역순으로 반환
        given(storyRepository.fetchByIdsWithSlides(List.of(1L, 2L, 3L)))
                .willReturn(List.of(s3, s1, s2));

        Page<StoryLibraryCardResponse> result = libraryService.getLibrary(1L, pageable);

        assertThat(result.getContent().get(0).getStoryId()).isEqualTo(1L);
        assertThat(result.getContent().get(1).getStoryId()).isEqualTo(2L);
        assertThat(result.getContent().get(2).getStoryId()).isEqualTo(3L);
    }

    @Test
    @DisplayName("도서관 조회 - Sort 없으면 createdAt DESC 기본 적용")
    void getLibrary_unsortedPageable_appliesDefaultSort() {
        Pageable unsorted = PageRequest.of(0, 20);
        given(storyRepository.findIdsByUserId(eq(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), unsorted, 0));

        libraryService.getLibrary(1L, unsorted);

        verify(storyRepository).findIdsByUserId(eq(1L), argThat(p ->
                p.getSort().getOrderFor("createdAt") != null &&
                        p.getSort().getOrderFor("createdAt").getDirection() == Sort.Direction.DESC
        ));
    }

    @Test
    @DisplayName("도서관 조회 - 허용되지 않은 정렬 필드는 제거")
    void getLibrary_illegalSortField_removedFromSort() {
        Pageable maliciousSort = PageRequest.of(0, 20, Sort.by("userId"));
        given(storyRepository.findIdsByUserId(eq(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), maliciousSort, 0));

        libraryService.getLibrary(1L, maliciousSort);

        // userId 정렬은 제거되고 기본값 createdAt DESC 적용 확인
        verify(storyRepository).findIdsByUserId(eq(1L), argThat(p ->
                p.getSort().getOrderFor("userId") == null
        ));
    }

    @Test
    @DisplayName("도서관 조회 - title ASC 정렬 허용")
    void getLibrary_titleSort_allowed() {
        Pageable titleSort = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "title"));
        given(storyRepository.findIdsByUserId(eq(1L), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), titleSort, 0));

        libraryService.getLibrary(1L, titleSort);

        verify(storyRepository).findIdsByUserId(eq(1L), argThat(p ->
                p.getSort().getOrderFor("title") != null
        ));
    }

    @Test
    @DisplayName("도서관 조회 - thumbnail은 첫 번째 슬라이드 imageUrl")
    void getLibrary_thumbnailFromFirstSlide() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Long> idPage = new PageImpl<>(List.of(1L), pageable, 1);

        Slide slide = Slide.builder()
                .order(0).imageUrl("https://example.com/thumb.png").build();
        Story story = buildStory(1L, "동화1");
        story.addSlide(slide);

        given(storyRepository.findIdsByUserId(eq(1L), any(Pageable.class))).willReturn(idPage);
        given(storyRepository.fetchByIdsWithSlides(List.of(1L))).willReturn(List.of(story));

        Page<StoryLibraryCardResponse> result = libraryService.getLibrary(1L, pageable);

        assertThat(result.getContent().get(0).getThumbnail())
                .isEqualTo("https://example.com/thumb.png");
    }

    // ────────────────── deleteFromLibrary ──────────────────

    @Test
    @DisplayName("도서관 동화 삭제 - 정상")
    void deleteFromLibrary_success() {
        Story story = buildStory(1L, "삭제 동화");
        given(storyRepository.findByStoryIdAndUserId(1L, 1L)).willReturn(Optional.of(story));

        libraryService.deleteFromLibrary(1L, 1L);

        verify(storyRepository).delete(story);
    }

    @Test
    @DisplayName("도서관 동화 삭제 - 존재하지 않음 또는 타인 소유 → STORY_NOT_FOUND")
    void deleteFromLibrary_notFoundOrNotOwner() {
        given(storyRepository.findByStoryIdAndUserId(1L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> libraryService.deleteFromLibrary(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.STORY_NOT_FOUND);
    }

    // ────────────────── 헬퍼 ──────────────────

    private Story buildStory(Long id, String title) {
        return Story.builder()
                .storyId(id)
                .title(title)
                .user(testUser)
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .isPublic(false)
                .build();
    }
}
