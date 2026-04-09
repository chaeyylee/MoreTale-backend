package com.moretale.domain.vocabulary.controller;

import com.moretale.domain.vocabulary.dto.request.VocabularyCreateRequest;
import com.moretale.domain.vocabulary.dto.request.VocabularyPatchRequest;
import com.moretale.domain.vocabulary.dto.request.VocabularySearchCondition;
import com.moretale.domain.vocabulary.dto.response.VocabularyResponse;
import com.moretale.domain.vocabulary.dto.response.VocabularyStoryResponse;
import com.moretale.domain.vocabulary.service.VocabularyService;
import com.moretale.global.common.ApiResponse;
import com.moretale.global.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 단어장 API Controller
 *
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  API 명세 (프론트 정렬 UI 연동)                                        │
 * ├──────────────┬─────────────────────────────────────────────────────── │
 * │  정렬 UI     │  요청 파라미터                                          │
 * ├──────────────┼───────────────────────────────────────────────────────┤
 * │  최신순      │  GET /api/vocabulary?sort=createdAt,desc               │
 * │  오래된순    │  GET /api/vocabulary?sort=createdAt,asc                │
 * │  가나다순    │  GET /api/vocabulary?sort=word,asc                     │
 * ├──────────────┼───────────────────────────────────────────────────────┤
 * │  즐겨찾기    │  GET /api/vocabulary?favorite=true                     │
 * │  키워드검색  │  GET /api/vocabulary?keyword=사자                      │
 * │  동화필터    │  GET /api/vocabulary?storyId=1                         │
 * │  조합        │  GET /api/vocabulary?storyId=1&favorite=true&sort=word,asc │
 * └──────────────┴───────────────────────────────────────────────────────┘
 */
@Tag(name = "Vocabulary", description = "단어장 API")
@Slf4j
@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    /**
     * POST /api/vocabulary
     * 단어 저장
     */
    @Operation(summary = "단어 저장", description = "하이라이트 단어를 단어장에 저장합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<VocabularyResponse> save(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody VocabularyCreateRequest request
    ) {
        VocabularyResponse response = vocabularyService.save(principal.getUserId(), request);
        return ApiResponse.success(response, "단어가 단어장에 저장되었습니다.");
    }

    /**
     * GET /api/vocabulary
     * 단어장 통합 조회 (필터 + 정렬 + 페이징)
     *
     * Query Parameters:
     *   - storyId  : 특정 동화 필터 (없으면 전체)
     *   - favorite : 즐겨찾기 필터 (true만 가능, 없으면 전체)
     *   - keyword  : 단어/번역어 검색
     *   - sort     : createdAt,desc | createdAt,asc | word,asc
     *   - page     : 페이지 번호 (0부터 시작)
     *   - size     : 페이지 크기 (기본 20)
     */
    @Operation(
            summary = "단어장 조회",
            description = """
                    단어장을 조회합니다. 필터와 정렬을 조합할 수 있습니다.
                    
                    **정렬 파라미터 (sort)**
                    - `sort=createdAt,desc` : 최신순 (기본값)
                    - `sort=createdAt,asc`  : 오래된순
                    - `sort=word,asc`       : 가나다순
                    
                    **필터 파라미터**
                    - `storyId=1`       : 특정 동화 단어만
                    - `favorite=true`   : 즐겨찾기 단어만
                    - `keyword=사자`    : 단어/번역어 검색
                    
                    **조합 예시**
                    - `?storyId=1&sort=word,asc`
                    - `?favorite=true&sort=createdAt,desc`
                    - `?storyId=1&favorite=true&keyword=사자&sort=word,asc`
                    """
    )
    @GetMapping
    public ApiResponse<Page<VocabularyResponse>> getVocabulary(
            @AuthenticationPrincipal UserPrincipal principal,
            @Parameter(description = "동화 ID 필터")
            @RequestParam(name = "storyId", required = false) Long storyId,
            @Parameter(description = "즐겨찾기 필터 (true이면 즐겨찾기만)")
            @RequestParam(name = "favorite", required = false) Boolean favorite,
            @Parameter(description = "단어/번역어 검색 키워드")
            @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "페이징 및 정렬")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("단어장 조회 - userId={}, storyId={}, favorite={}, keyword={}, sort={}",
                principal.getUserId(), storyId, favorite, keyword, pageable.getSort());

        VocabularySearchCondition condition = new VocabularySearchCondition();
        condition.setStoryId(storyId);
        condition.setFavorite(favorite);
        condition.setKeyword(keyword);

        // 필터가 하나도 없으면 기존 단순 조회 사용 (성능 최적화)
        Page<VocabularyResponse> result;
        if (storyId == null && favorite == null && keyword == null) {
            result = vocabularyService.getAll(principal.getUserId(), pageable);
        } else {
            result = vocabularyService.getWithFilters(principal.getUserId(), condition, pageable);
        }

        return ApiResponse.success(result);
    }

    /**
     * GET /api/vocabulary/stories
     * 단어가 저장된 동화 목록 조회
     */
    @Operation(
            summary = "단어가 저장된 동화 목록 조회",
            description = "단어장에 단어가 저장된 동화 목록과 각 동화별 단어 수를 반환합니다."
    )
    @GetMapping("/stories")
    public ApiResponse<List<VocabularyStoryResponse>> getStoriesWithVocabulary(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<VocabularyStoryResponse> result =
                vocabularyService.getStoriesWithVocabulary(principal.getUserId());
        return ApiResponse.success(result);
    }

    /**
     * PATCH /api/vocabulary/{vocabularyId}
     * 단어장 항목 수정 (즐겨찾기 / 학습상태 / 메모)
     */
    @Operation(
            summary = "단어장 항목 수정",
            description = "즐겨찾기, 학습상태, 메모를 수정합니다. null인 필드는 변경되지 않습니다."
    )
    @PatchMapping("/{vocabularyId}")
    public ApiResponse<VocabularyResponse> patch(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable(name = "vocabularyId") Long vocabularyId,
            @RequestBody VocabularyPatchRequest request
    ) {
        VocabularyResponse response =
                vocabularyService.patch(principal.getUserId(), vocabularyId, request);
        return ApiResponse.success(response, "단어장이 수정되었습니다.");
    }

    /**
     * DELETE /api/vocabulary/{vocabularyId}
     * 단어 삭제
     */
    @Operation(summary = "단어 삭제", description = "단어장에서 단어를 삭제합니다.")
    @DeleteMapping("/{vocabularyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable(name = "vocabularyId") Long vocabularyId
    ) {
        vocabularyService.delete(principal.getUserId(), vocabularyId);
    }
}
