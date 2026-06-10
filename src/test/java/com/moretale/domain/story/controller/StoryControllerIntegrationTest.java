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

@DisplayName("Story Controller 통합 테스트")
class StoryControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired UserRepository userRepository;
    @Autowired UserProfileRepository userProfileRepository;
    @Autowired StoryRepository storyRepository;

    private User testUser;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(TestFixture.createUser("story-test@example.com"));
        testProfile = userProfileRepository.save(TestFixture.createProfile(testUser));
        setAuthentication(testUser);
    }

    @Test
    @DisplayName("동화 초기값 조회 - 프로필 있음 → 추천 전래동화 반환")
    void getStoryInitData_success() throws Exception {
        mockMvc.perform(get("/api/stories/init"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.profileId").value(testProfile.getProfileId()))
                .andExpect(jsonPath("$.data.childName").value("민준"))
                .andExpect(jsonPath("$.data.recommendedTaleTitle").isNotEmpty())
                .andExpect(jsonPath("$.data.firstLanguageDisplay").value("한국어"))
                .andExpect(jsonPath("$.data.secondLanguageDisplay").value("베트남어"))
                .andExpect(jsonPath("$.data.storyId").doesNotExist());
    }

    @Test
    @DisplayName("동화 초기값 조회 - 추천 동화 이미 생성된 경우 storyId + coverImageUrl 반환")
    void getStoryInitData_withExistingStory() throws Exception {
        // 추천 전래동화 제목 생성
        String recommendedTitle = com.moretale.domain.story.enums.TraditionalTale
                .findByPreference(testProfile.getStoryPreference()).getTitle();

        Story story = TestFixture.createStory(testUser, testProfile, recommendedTitle);
        Slide cover = TestFixture.createCoverSlide("https://example.com/cover.png");
        story.addSlide(cover);
        story.addSlide(TestFixture.createContentSlide(1, "내용", "content"));
        storyRepository.save(story);

        mockMvc.perform(get("/api/stories/init"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storyId").value(story.getStoryId()))
                .andExpect(jsonPath("$.data.coverImageUrl").value("https://example.com/cover.png"));
    }

    @Test
    @DisplayName("동화 초기값 조회 - 프로필 없음 → 404")
    void getStoryInitData_noProfile_404() throws Exception {
        // 프로필 없는 새 사용자
        User userWithoutProfile = userRepository.save(
                TestFixture.createUser("no-profile@example.com"));
        setAuthentication(userWithoutProfile);

        mockMvc.perform(get("/api/stories/init"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("P001"));
    }

    @Test
    @DisplayName("동화 상세 조회 - 본인 동화 → 슬라이드 포함 응답")
    void getStoryDetail_ownStory() throws Exception {
        Story story = TestFixture.createStory(testUser, testProfile, "흥부와 놀부");
        story.addSlide(TestFixture.createCoverSlide("https://example.com/cover.png"));
        story.addSlide(TestFixture.createContentSlide(1, "한국어 내용", "베트남어 내용"));
        storyRepository.save(story);

        mockMvc.perform(get("/api/stories/" + story.getStoryId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storyId").value(story.getStoryId()))
                .andExpect(jsonPath("$.data.title").value("흥부와 놀부"))
                .andExpect(jsonPath("$.data.slides", hasSize(2)));
    }

    @Test
    @DisplayName("동화 상세 조회 - 비공개 타인 동화 → 403")
    void getStoryDetail_privateOtherStory_403() throws Exception {
        User otherUser = userRepository.save(TestFixture.createUser("other@example.com"));
        UserProfile otherProfile = userProfileRepository.save(TestFixture.createProfile(otherUser));
        Story privateStory = TestFixture.createStory(otherUser, otherProfile, "비공개 동화");
        storyRepository.save(privateStory);

        mockMvc.perform(get("/api/stories/" + privateStory.getStoryId()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("S002"));
    }

    @Test
    @DisplayName("동화 상세 조회 - 공개 타인 동화 → 조회 가능")
    void getStoryDetail_publicOtherStory_accessible() throws Exception {
        User otherUser = userRepository.save(TestFixture.createUser("public-owner@example.com"));
        UserProfile otherProfile = userProfileRepository.save(TestFixture.createProfile(otherUser));
        Story publicStory = Story.builder()
                .title("공개 동화")
                .user(otherUser)
                .profile(otherProfile)
                .childName("다른아이")
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .isPublic(true)
                .build();
        storyRepository.save(publicStory);

        mockMvc.perform(get("/api/stories/" + publicStory.getStoryId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("공개 동화"));
    }

    @Test
    @DisplayName("내 동화 목록 조회 - 동화 있음")
    void getMyStories_success() throws Exception {
        storyRepository.save(TestFixture.createStory(testUser, testProfile, "동화1"));
        storyRepository.save(TestFixture.createStory(testUser, testProfile, "동화2"));

        mockMvc.perform(get("/api/stories/my"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    @DisplayName("내 동화 목록 조회 - 동화 없음 → 빈 배열")
    void getMyStories_empty() throws Exception {
        mockMvc.perform(get("/api/stories/my"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("동화 공유 설정 변경 - 비공개 → 공개")
    void updateStoryShareStatus_success() throws Exception {
        Story story = storyRepository.save(
                TestFixture.createStory(testUser, testProfile, "공유 테스트 동화"));

        String body = """
                {"isPublic": true}
                """;

        mockMvc.perform(patch("/api/stories/" + story.getStoryId() + "/share")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("동화 삭제 - 본인 동화 정상 삭제")
    void deleteStory_success() throws Exception {
        Story story = storyRepository.save(
                TestFixture.createStory(testUser, testProfile, "삭제 대상 동화"));

        mockMvc.perform(delete("/api/stories/" + story.getStoryId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("동화 삭제 완료"));
    }

    private void setAuthentication(User user) {
        UserPrincipal principal = UserPrincipal.create(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
    }
}
