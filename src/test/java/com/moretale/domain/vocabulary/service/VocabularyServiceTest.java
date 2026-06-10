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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("VocabularyService 단위 테스트")
class VocabularyServiceTest {

    @Mock VocabularyEntryRepository vocabularyEntryRepository;
    @Mock UserRepository userRepository;
    @Mock StoryRepository storyRepository;
    @Mock SlideRepository slideRepository;
    @Mock StoryTokenRepository storyTokenRepository;

    @InjectMocks VocabularyService vocabularyService;

    private User testUser;
    private Story testStory;
    private Slide testSlide;
    private StoryToken highlightToken;
    private StoryToken nonHighlightToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder().userId(1L).email("test@example.com").build();

        testStory = Story.builder()
                .storyId(5L).title("흥부와 놀부")
                .user(testUser).primaryLanguage("ko").secondaryLanguage("vi").build();

        testSlide = Slide.builder().slideId(10L).story(testStory).order(1).build();

        highlightToken = StoryToken.builder()
                .tokenId(42L).slide(testSlide)
                .text("우주복").tokenOrder(0).highlight(true)
                .translation("bộ đồ phi hành gia")
                .definition("우주에서 입는 특별한 옷")
                .secondaryDefinition("áo đặc biệt")
                .sourceLanguage("ko").targetLanguage("vi")
                .build();

        nonHighlightToken = StoryToken.builder()
                .tokenId(43L).slide(testSlide)
                .text("은").tokenOrder(1).highlight(false)
                .sourceLanguage("ko").targetLanguage("vi")
                .build();
    }

    // ────────────────── save ──────────────────

    @Test
    @DisplayName("단어 저장 - 정상")
    void save_success() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(storyRepository.findById(5L)).willReturn(Optional.of(testStory));
        given(slideRepository.findById(10L)).willReturn(Optional.of(testSlide));
        given(storyTokenRepository.findById(42L)).willReturn(Optional.of(highlightToken));
        given(vocabularyEntryRepository.existsByUser_UserIdAndStory_StoryIdAndNormalizedWord(
                1L, 5L, "우주복")).willReturn(false);
        given(vocabularyEntryRepository.save(any(VocabularyEntry.class)))
                .willAnswer(inv -> {
                    VocabularyEntry e = inv.getArgument(0);
                    return VocabularyEntry.builder()
                            .vocabularyId(100L)
                            .user(e.getUser()).story(e.getStory())
                            .slide(e.getSlide()).storyToken(e.getStoryToken())
                            .word(e.getWord()).normalizedWord(e.getNormalizedWord())
                            .translation(e.getTranslation()).definition(e.getDefinition())
                            .secondaryDefinition(e.getSecondaryDefinition())
                            .sourceLanguage(e.getSourceLanguage())
                            .targetLanguage(e.getTargetLanguage())
                            .isFavorite(false)
                            .learningStatus(VocabularyEntry.LearningStatus.UNSEEN)
                            .build();
                });

        VocabularyCreateRequest request = new VocabularyCreateRequest();
        setField(request, "tokenId", 42L);
        setField(request, "slideId", 10L);
        setField(request, "storyId", 5L);

        VocabularyResponse response = vocabularyService.save(1L, request);

        assertThat(response.getVocabularyId()).isEqualTo(100L);
        assertThat(response.getWord()).isEqualTo("우주복");
        assertThat(response.getTranslation()).isEqualTo("bộ đồ phi hành gia");
        assertThat(response.getNormalizedWord()).isEqualTo("우주복");
    }

    @Test
    @DisplayName("단어 저장 - highlight=false 토큰 → INVALID_INPUT_VALUE")
    void save_nonHighlightToken_throwsException() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(storyRepository.findById(5L)).willReturn(Optional.of(testStory));
        given(slideRepository.findById(10L)).willReturn(Optional.of(testSlide));
        given(storyTokenRepository.findById(43L)).willReturn(Optional.of(nonHighlightToken));

        VocabularyCreateRequest request = new VocabularyCreateRequest();
        setField(request, "tokenId", 43L);
        setField(request, "slideId", 10L);
        setField(request, "storyId", 5L);

        assertThatThrownBy(() -> vocabularyService.save(1L, request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("단어 저장 - 중복 단어 → VocabularyDuplicateException")
    void save_duplicate_throws() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(storyRepository.findById(5L)).willReturn(Optional.of(testStory));
        given(slideRepository.findById(10L)).willReturn(Optional.of(testSlide));
        given(storyTokenRepository.findById(42L)).willReturn(Optional.of(highlightToken));
        given(vocabularyEntryRepository.existsByUser_UserIdAndStory_StoryIdAndNormalizedWord(
                1L, 5L, "우주복")).willReturn(true);

        VocabularyCreateRequest request = new VocabularyCreateRequest();
        setField(request, "tokenId", 42L);
        setField(request, "slideId", 10L);
        setField(request, "storyId", 5L);

        assertThatThrownBy(() -> vocabularyService.save(1L, request))
                .isInstanceOf(VocabularyDuplicateException.class);
    }

    @Test
    @DisplayName("단어 저장 - 토큰 없음 → TokenNotFoundException")
    void save_tokenNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(storyRepository.findById(5L)).willReturn(Optional.of(testStory));
        given(slideRepository.findById(10L)).willReturn(Optional.of(testSlide));
        given(storyTokenRepository.findById(999L)).willReturn(Optional.empty());

        VocabularyCreateRequest request = new VocabularyCreateRequest();
        setField(request, "tokenId", 999L);
        setField(request, "slideId", 10L);
        setField(request, "storyId", 5L);

        assertThatThrownBy(() -> vocabularyService.save(1L, request))
                .isInstanceOf(TokenNotFoundException.class);
    }

    // ────────────────── patch ──────────────────

    @Test
    @DisplayName("즐겨찾기 수정 - false → true")
    void patch_toggleFavorite() {
        VocabularyEntry entry = buildEntry(100L, false);
        given(vocabularyEntryRepository.existsById(100L)).willReturn(true);
        given(vocabularyEntryRepository.findByVocabularyIdAndUser_UserId(100L, 1L))
                .willReturn(Optional.of(entry));

        VocabularyPatchRequest request = new VocabularyPatchRequest();
        setField(request, "isFavorite", true);

        VocabularyResponse response = vocabularyService.patch(1L, 100L, request);

        assertThat(response.getIsFavorite()).isTrue();
    }

    @Test
    @DisplayName("학습 상태 수정 - UNSEEN → MASTERED")
    void patch_learningStatus() {
        VocabularyEntry entry = buildEntry(100L, false);
        given(vocabularyEntryRepository.existsById(100L)).willReturn(true);
        given(vocabularyEntryRepository.findByVocabularyIdAndUser_UserId(100L, 1L))
                .willReturn(Optional.of(entry));

        VocabularyPatchRequest request = new VocabularyPatchRequest();
        setField(request, "learningStatus", VocabularyEntry.LearningStatus.MASTERED);

        VocabularyResponse response = vocabularyService.patch(1L, 100L, request);

        assertThat(response.getLearningStatus())
                .isEqualTo(VocabularyEntry.LearningStatus.MASTERED);
    }

    @Test
    @DisplayName("patch - null 필드는 변경 없음")
    void patch_nullFields_noChange() {
        VocabularyEntry entry = buildEntry(100L, true);
        given(vocabularyEntryRepository.existsById(100L)).willReturn(true);
        given(vocabularyEntryRepository.findByVocabularyIdAndUser_UserId(100L, 1L))
                .willReturn(Optional.of(entry));

        // 모든 필드 null → 변경 없음
        VocabularyPatchRequest request = new VocabularyPatchRequest();

        VocabularyResponse response = vocabularyService.patch(1L, 100L, request);

        assertThat(response.getIsFavorite()).isTrue(); // 기존값 유지
    }

    @Test
    @DisplayName("patch - 항목 없음 → VocabularyNotFoundException")
    void patch_notFound() {
        given(vocabularyEntryRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() ->
                vocabularyService.patch(1L, 999L, new VocabularyPatchRequest()))
                .isInstanceOf(VocabularyNotFoundException.class);
    }

    @Test
    @DisplayName("patch - 타인 소유 항목 → VocabularyAccessDeniedException")
    void patch_accessDenied() {
        given(vocabularyEntryRepository.existsById(100L)).willReturn(true);
        given(vocabularyEntryRepository.findByVocabularyIdAndUser_UserId(100L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                vocabularyService.patch(1L, 100L, new VocabularyPatchRequest()))
                .isInstanceOf(VocabularyAccessDeniedException.class);
    }

    // ────────────────── delete ──────────────────

    @Test
    @DisplayName("단어 삭제 - 정상")
    void delete_success() {
        VocabularyEntry entry = buildEntry(100L, false);
        given(vocabularyEntryRepository.existsById(100L)).willReturn(true);
        given(vocabularyEntryRepository.findByVocabularyIdAndUser_UserId(100L, 1L))
                .willReturn(Optional.of(entry));

        vocabularyService.delete(1L, 100L);

        verify(vocabularyEntryRepository).delete(entry);
    }

    // ────────────────── getStoriesWithVocabulary ──────────────────

    @Test
    @DisplayName("단어 저장 동화 목록 - 단어 수 포함 반환")
    void getStoriesWithVocabulary_withWordCount() {
        given(vocabularyEntryRepository.findDistinctStoriesByUserId(1L))
                .willReturn(List.of(testStory));
        given(vocabularyEntryRepository.countByUser_UserIdAndStory_StoryId(1L, 5L))
                .willReturn(3L);

        List<VocabularyStoryResponse> result =
                vocabularyService.getStoriesWithVocabulary(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStoryId()).isEqualTo(5L);
        assertThat(result.get(0).getWordCount()).isEqualTo(3L);
    }

    // ────────────────── getWithFilters - 즐겨찾기 정렬 선행 정책 ──────────────────

    @Test
    @DisplayName("getWithFilters - favorite 필터 없으면 isFavorite DESC 선행 정렬 적용")
    void getWithFilters_noFavoriteFilter_prependsFavoriteSort() {
        VocabularySearchCondition condition = new VocabularySearchCondition();
        condition.setFavorite(null);

        Pageable pageable = PageRequest.of(0, 20,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        given(vocabularyEntryRepository.findIdsByFilters(
                eq(1L), isNull(), isNull(), isNull(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        vocabularyService.getWithFilters(1L, condition, pageable);

        verify(vocabularyEntryRepository).findIdsByFilters(
                eq(1L), isNull(), isNull(), isNull(),
                argThat(p -> {
                    Sort sort = p.getSort();
                    List<Sort.Order> orders = sort.toList();
                    // isFavorite DESC가 첫 번째여야 함
                    return orders.size() >= 2
                            && orders.get(0).getProperty().equals("isFavorite")
                            && orders.get(0).getDirection() == Sort.Direction.DESC
                            && orders.get(1).getProperty().equals("createdAt");
                })
        );
    }

    @Test
    @DisplayName("getWithFilters - favorite=true 필터 시 isFavorite 선행 정렬 생략")
    void getWithFilters_favoriteFilter_skipsFavoriteSort() {
        VocabularySearchCondition condition = new VocabularySearchCondition();
        condition.setFavorite(true);

        Pageable pageable = PageRequest.of(0, 20,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        given(vocabularyEntryRepository.findIdsByFilters(
                eq(1L), isNull(), eq(true), isNull(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        vocabularyService.getWithFilters(1L, condition, pageable);

        verify(vocabularyEntryRepository).findIdsByFilters(
                eq(1L), isNull(), eq(true), isNull(),
                argThat(p -> {
                    Sort sort = p.getSort();
                    List<Sort.Order> orders = sort.toList();
                    // isFavorite 정렬이 없어야 함
                    return orders.stream()
                            .noneMatch(o -> o.getProperty().equals("isFavorite"));
                })
        );
    }

    @Test
    @DisplayName("getWithFilters - keyword 있으면 LIKE 패턴으로 변환")
    void getWithFilters_withKeyword_convertsToLikePattern() {
        VocabularySearchCondition condition = new VocabularySearchCondition();
        condition.setKeyword("사자");

        Pageable pageable = PageRequest.of(0, 20);

        given(vocabularyEntryRepository.findIdsByFilters(
                eq(1L), isNull(), isNull(), eq("%사자%"), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        vocabularyService.getWithFilters(1L, condition, pageable);

        verify(vocabularyEntryRepository).findIdsByFilters(
                eq(1L), isNull(), isNull(), eq("%사자%"), any(Pageable.class));
    }

    @Test
    @DisplayName("getWithFilters - keyword null이면 null 그대로 전달")
    void getWithFilters_nullKeyword_passesNull() {
        VocabularySearchCondition condition = new VocabularySearchCondition();
        condition.setKeyword(null);

        Pageable pageable = PageRequest.of(0, 20);

        given(vocabularyEntryRepository.findIdsByFilters(
                eq(1L), isNull(), isNull(), isNull(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        vocabularyService.getWithFilters(1L, condition, pageable);

        verify(vocabularyEntryRepository).findIdsByFilters(
                eq(1L), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    @DisplayName("getWithFilters - 2단계 조회 후 원래 ID 순서 유지")
    void getWithFilters_preservesOrder() {
        VocabularySearchCondition condition = new VocabularySearchCondition();
        Pageable pageable = PageRequest.of(0, 20);

        Page<Long> idPage = new PageImpl<>(List.of(1L, 2L, 3L), pageable, 3);

        VocabularyEntry e1 = buildEntry(1L, false);
        VocabularyEntry e2 = buildEntry(2L, true);
        VocabularyEntry e3 = buildEntry(3L, false);

        given(vocabularyEntryRepository.findIdsByFilters(
                eq(1L), isNull(), isNull(), isNull(), any(Pageable.class)))
                .willReturn(idPage);
        // DB에서 역순 반환
        given(vocabularyEntryRepository.fetchByIds(List.of(1L, 2L, 3L)))
                .willReturn(List.of(e3, e1, e2));

        Page<VocabularyResponse> result =
                vocabularyService.getWithFilters(1L, condition, pageable);

        assertThat(result.getContent().get(0).getVocabularyId()).isEqualTo(1L);
        assertThat(result.getContent().get(1).getVocabularyId()).isEqualTo(2L);
        assertThat(result.getContent().get(2).getVocabularyId()).isEqualTo(3L);
    }

    // ────────────────── 헬퍼 ──────────────────

    private VocabularyEntry buildEntry(Long id, boolean isFavorite) {
        return VocabularyEntry.builder()
                .vocabularyId(id)
                .user(testUser)
                .story(testStory)
                .slide(testSlide)
                .storyToken(highlightToken)
                .word("우주복")
                .normalizedWord("우주복")
                .translation("bộ đồ phi hành gia")
                .definition("우주에서 입는 특별한 옷")
                .secondaryDefinition("áo đặc biệt")
                .sourceLanguage("ko")
                .targetLanguage("vi")
                .isFavorite(isFavorite)
                .learningStatus(VocabularyEntry.LearningStatus.UNSEEN)
                .build();
    }

    @SuppressWarnings("all")
    private void setField(Object target, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
