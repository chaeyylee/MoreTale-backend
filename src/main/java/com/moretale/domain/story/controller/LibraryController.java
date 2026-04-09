package com.moretale.domain.story.controller;

import com.moretale.domain.story.dto.StoryLibraryCardResponse;
import com.moretale.domain.story.service.LibraryService;
import com.moretale.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 도서관 API Controller
 *
 * ┌──────────────────────────────────────────────────────────────────┐
 * │  API 명세 (프론트 정렬 UI 연동)                                    │
 * ├──────────────┬───────────────────────────────────────────────────┤
 * │  정렬 UI     │  요청 파라미터                                      │
 * ├──────────────┼───────────────────────────────────────────────────┤
 * │  최신순      │  GET /api/library?sort=createdAt,desc              │
 * │  오래된순    │  GET /api/library?sort=createdAt,asc               │
 * │  가나다순    │  GET /api/library?sort=title,asc                   │
 * ├──────────────┼───────────────────────────────────────────────────┤
 * │  삭제        │  DELETE /api/library/{storyId}                     │
 * │              │  → Story + Slide + StoryToken + VocabularyEntry    │
 * │              │    전체 삭제 (숨김 아님)                             │
 * └──────────────┴───────────────────────────────────────────────────┘
 */
@Tag(name = "Library", description = "도서관 API - 내 동화 목록 관리")
@Slf4j
@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    /**
     * 도서관 목록 조회
     * GET /api/library
     * GET /api/library?sort=createdAt,desc  (최신순 - 기본값)
     * GET /api/library?sort=createdAt,asc   (오래된순)
     * GET /api/library?sort=title,asc       (가나다순)
     * GET /api/library?page=0&size=10&sort=createdAt,desc
     */
    @Operation(
            summary = "도서관 목록 조회",
            description = """
                    내가 만든 동화 목록을 페이징으로 조회합니다.
                    
                    **정렬 파라미터 (sort)**
                    - `sort=createdAt,desc` : 최신순 (기본값)
                    - `sort=createdAt,asc`  : 오래된순
                    - `sort=title,asc`      : 가나다순
                    
                    **응답 필드**
                    - `storyId` : 동화 ID
                    - `title` : 동화 제목
                    - `thumbnail` : 첫 번째 슬라이드 이미지 URL
                    - `primaryLanguage` / `secondaryLanguage` : 언어쌍
                    - `createdAt` : 생성일시 (ISO 8601)
                    - `slideCount` : 슬라이드 수
                    """
    )
    @GetMapping
    public ApiResponse<Page<StoryLibraryCardResponse>> getLibrary(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "페이징 및 정렬 (sort=createdAt,desc | sort=title,asc)")
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        log.info("도서관 조회 - email={}, sort={}", userDetails.getUsername(), pageable.getSort());

        Page<StoryLibraryCardResponse> result = libraryService.getLibrary(
                userDetails.getUsername(), pageable
        );

        return ApiResponse.success(result, "도서관 조회 성공");
    }

    /**
     * 도서관에서 동화 삭제
     * DELETE /api/library/{storyId}
     *
     * Story 삭제 = 동화 자체 삭제 (숨김 아님)
     * cascade 삭제: Story → Slide → StoryToken → VocabularyEntry
     */
    @Operation(
            summary = "도서관 동화 삭제",
            description = """
                    동화를 완전히 삭제합니다. (숨김이 아닌 영구 삭제)
                    
                    **삭제 범위 (연쇄 삭제)**
                    - Story (동화)
                    - Slide (슬라이드, JPA cascade)
                    - StoryToken (단어 토큰, JPA cascade)
                    - VocabularyEntry (단어장 데이터, DB OnDelete CASCADE)
                    
                    본인 소유의 동화만 삭제할 수 있습니다.
                    """
    )
    @DeleteMapping("/{storyId}")
    public ApiResponse<Void> deleteFromLibrary(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable(name = "storyId") Long storyId
    ) {
        log.info("도서관 동화 삭제 요청 - email={}, storyId={}",
                userDetails.getUsername(), storyId);

        libraryService.deleteFromLibrary(userDetails.getUsername(), storyId);

        return ApiResponse.success(null, "동화가 삭제되었습니다.");
    }
}
