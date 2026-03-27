package com.moretale.domain.vocabulary.controller;

import com.moretale.domain.vocabulary.dto.request.VocabularyCreateRequest;
import com.moretale.domain.vocabulary.dto.request.VocabularyPatchRequest;
import com.moretale.domain.vocabulary.dto.response.VocabularyResponse;
import com.moretale.domain.vocabulary.dto.response.VocabularyStoryResponse;
import com.moretale.domain.vocabulary.service.VocabularyService;
import com.moretale.global.common.ApiResponse;
import com.moretale.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    /**
     * POST /api/vocabulary
     * 단어 저장
     */
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
     * 내 전체 단어장 조회 (페이징)
     * - storyId 쿼리 파라미터가 있으면 특정 동화 기준 조회
     */
    @GetMapping
    public ApiResponse<Page<VocabularyResponse>> getVocabulary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(name = "storyId", required = false) Long storyId, // name 명시로 에러 해결
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<VocabularyResponse> result = (storyId != null)
                ? vocabularyService.getByStory(principal.getUserId(), storyId, pageable)
                : vocabularyService.getAll(principal.getUserId(), pageable);

        return ApiResponse.success(result);
    }

    /**
     * GET /api/vocabulary/stories
     * 단어가 저장된 동화 목록 조회
     */
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
    @PatchMapping("/{vocabularyId}")
    public ApiResponse<VocabularyResponse> patch(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable(name = "vocabularyId") Long vocabularyId, // name 명시
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
    @DeleteMapping("/{vocabularyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable(name = "vocabularyId") Long vocabularyId // name 명시
    ) {
        vocabularyService.delete(principal.getUserId(), vocabularyId);
    }
}
