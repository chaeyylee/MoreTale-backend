package com.moretale.domain.story.controller;

import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.security.UserPrincipal;
import com.moretale.support.IntegrationTestSupport;
import com.moretale.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Library Controller 통합 테스트")
class LibraryControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired UserRepository userRepository;
    @Autowired UserProfileRepository userProfileRepository;
    @Autowired StoryRepository storyRepository;

    private User testUser;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(TestFixture.createUser("library-test@example.com"));
        testProfile = userProfileRepository.save(TestFixture.createProfile(testUser));
        setAuthentication(testUser);
    }

    // ────────────────── GET /api/library ──────────────────

    @Test
    @DisplayName("도서관 조회 - 기본값 (최신순) 정상 응답")
    void getLibrary_defaultSort_success() throws Exception {
        storyRepository.save(TestFixture.createStory(testUser, testProfile, "동화1"));
        storyRepository.save(TestFixture.createStory(testUser, testProfile, "동화2"));

        mockMvc.perform(get("/api/library"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(2)))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @DisplayName("도서관 조회 - 동화 없음 → 빈 페이지")
    void getLibrary_empty_returnsEmptyPage() throws Exception {
        mockMvc.perform(get("/api/library"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content").isEmpty())
                .andExpect(jsonPath("$.data.totalElements").value(0));
    }

    @Test
    @DisplayName("도서관 조회 - 가나다순 정렬 (title ASC)")
    void getLibrary_titleSort_success() throws Exception {
        storyRepository.save(TestFixture.createStory(testUser, testProfile, "흥부와 놀부"));
        storyRepository.save(TestFixture.createStory(testUser, testProfile, "가나다 순 동화"));

        mockMvc.perform(get("/api/library?sort=title,asc"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("가나다 순 동화"))
                .andExpect(jsonPath("$.data.content[1].title").value("흥부와 놀부"));
    }

    @Test
    @DisplayName("도서관 조회 - 썸네일은 첫 번째 슬라이드 imageUrl")
    void getLibrary_thumbnailFromFirstSlide() throws Exception {
        Story story = TestFixture.createStory(testUser, testProfile, "썸네일 테스트");
        Slide cover = TestFixture.createCoverSlide("https://example.com/cover.png");
        story.addSlide(cover);
        storyRepository.save(story);

        mockMvc.perform(get("/api/library"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].thumbnail")
                        .value("https://example.com/cover.png"));
    }

    @Test
    @DisplayName("도서관 조회 - 타인의 동화는 포함되지 않음")
    void getLibrary_onlyMyStories() throws Exception {
        User otherUser = userRepository.save(TestFixture.createUser("other@example.com"));
        UserProfile otherProfile = userProfileRepository.save(TestFixture.createProfile(otherUser));
        storyRepository.save(TestFixture.createStory(otherUser, otherProfile, "타인 동화"));
        storyRepository.save(TestFixture.createStory(testUser, testProfile, "내 동화"));

        mockMvc.perform(get("/api/library"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].title").value("내 동화"));
    }

    @Test
    @DisplayName("도서관 조회 - 페이징 동작 확인")
    void getLibrary_pagination_works() throws Exception {
        for (int i = 1; i <= 5; i++) {
            storyRepository.save(TestFixture.createStory(testUser, testProfile, "동화" + i));
        }

        mockMvc.perform(get("/api/library?page=0&size=3"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content", hasSize(3)))
                .andExpect(jsonPath("$.data.totalElements").value(5))
                .andExpect(jsonPath("$.data.totalPages").value(2));
    }

    // ────────────────── DELETE /api/library/{storyId} ──────────────────

    @Test
    @DisplayName("도서관 동화 삭제 - 정상")
    void deleteFromLibrary_success() throws Exception {
        Story story = storyRepository.save(
                TestFixture.createStory(testUser, testProfile, "삭제할 동화"));

        mockMvc.perform(delete("/api/library/" + story.getStoryId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("동화가 삭제되었습니다."));
    }

    @Test
    @DisplayName("도서관 동화 삭제 - 타인 소유 → 404")
    void deleteFromLibrary_otherUserStory_404() throws Exception {
        User otherUser = userRepository.save(TestFixture.createUser("owner@example.com"));
        UserProfile otherProfile = userProfileRepository.save(TestFixture.createProfile(otherUser));
        Story otherStory = storyRepository.save(
                TestFixture.createStory(otherUser, otherProfile, "타인 동화"));

        mockMvc.perform(delete("/api/library/" + otherStory.getStoryId()))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("S001"));
    }

    // ────────────────── 헬퍼 ──────────────────

    private void setAuthentication(User user) {
        UserPrincipal principal = UserPrincipal.create(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
    }
}
