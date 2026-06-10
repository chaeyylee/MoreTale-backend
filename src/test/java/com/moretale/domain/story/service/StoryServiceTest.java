package com.moretale.domain.story.service;

import com.moretale.domain.profile.entity.*;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.story.dto.StoryInitResponse;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.enums.TraditionalTale;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.config.MoreTaleProperties;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoryService 단위 테스트")
class StoryServiceTest {

    @Mock StoryRepository storyRepository;
    @Mock UserRepository userRepository;
    @Mock UserProfileRepository userProfileRepository;
    @Mock AIStoryService aiStoryService;
    @Mock StoryTokenService storyTokenService;
    @Mock MoreTaleProperties properties;
    @Mock EntityManager em;

    @InjectMocks StoryService storyService;

    private User testUser;
    private UserProfile testProfile;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("홍길동")
                .role(User.Role.USER)
                .build();

        testProfile = UserProfile.builder()
                .profileId(10L)
                .user(testUser)
                .childName("민준")
                .ageGroup(AgeGroup.AGE_5_6)
                .childAge(5)
                .firstLanguage(Language.KO)
                .customFirstLanguage(null)
                .firstLanguageProficiency(LanguageProficiency.BEE)
                .secondLanguage(Language.VI)
                .customSecondLanguage(null)
                .secondLanguageProficiency(LanguageProficiency.LARVA)
                .firstLanguageListening(LanguageProficiency.BEE)
                .firstLanguageSpeaking(LanguageProficiency.PUPA)
                .secondLanguageListening(LanguageProficiency.LARVA)
                .secondLanguageSpeaking(LanguageProficiency.EGG)
                .familyStructure(FamilyStructure.TWO_PARENTS)
                .storyPreference(StoryPreference.FUN_ADVENTURE)
                .build();

        testProfile.syncLegacyLanguages();
    }

    @Test
    @DisplayName("getStoryInitData - 프로필 기반 초기값 반환")
    void getStoryInitData_success() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findFirstByUserOrderByCreatedAtDesc(testUser))
                .willReturn(Optional.of(testProfile));
        given(storyRepository.findLatestStoryIdByProfileAndTitle(
                eq(1L), eq(10L), anyString(), any(Pageable.class)))
                .willReturn(List.of());
        given(storyRepository.findLatestStoryIdByUserAndTitle(
                eq(1L), anyString(), any(Pageable.class)))
                .willReturn(List.of());

        StoryInitResponse response = storyService.getStoryInitData(1L, null);

        assertThat(response.getProfileId()).isEqualTo(10L);
        assertThat(response.getChildName()).isEqualTo("민준");
        assertThat(response.getFirstLanguageDisplay()).isEqualTo("한국어");
        assertThat(response.getSecondLanguageDisplay()).isEqualTo("베트남어");
        assertThat(response.getStoryId()).isNull();
        assertThat(response.getCoverImageUrl()).isNull();
    }

    @Test
    @DisplayName("getStoryInitData - FUN_ADVENTURE → 금도끼 은도끼 또는 토끼와 거북이 추천")
    void getStoryInitData_recommendedTale_funAdventure() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findFirstByUserOrderByCreatedAtDesc(testUser))
                .willReturn(Optional.of(testProfile));
        given(storyRepository.findLatestStoryIdByProfileAndTitle(
                anyLong(), anyLong(), anyString(), any(Pageable.class)))
                .willReturn(List.of());
        given(storyRepository.findLatestStoryIdByUserAndTitle(
                anyLong(), anyString(), any(Pageable.class)))
                .willReturn(List.of());

        StoryInitResponse response = storyService.getStoryInitData(1L, null);

        assertThat(response.getRecommendedTaleTitle())
                .isIn(
                        TraditionalTale.GOLD_AXE_SILVER_AXE.getTitle(),
                        TraditionalTale.RABBIT_AND_TURTLE.getTitle()
                );
    }

    @Test
    @DisplayName("getStoryInitData - 이미 생성된 추천 동화 존재 시 storyId + coverImageUrl 반환")
    void getStoryInitData_withExistingStory() {
        String recommendedTitle = TraditionalTale.findByPreference(StoryPreference.FUN_ADVENTURE).getTitle();

        Slide coverSlide = Slide.builder()
                .slideId(1L)
                .order(0)
                .imageUrl("https://example.com/cover.png")
                .textKr("")
                .textNative("")
                .build();

        Story existingStory = Story.builder()
                .storyId(42L)
                .title(recommendedTitle)
                .user(testUser)
                .profile(testProfile)
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .build();
        existingStory.addSlide(coverSlide);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findFirstByUserOrderByCreatedAtDesc(testUser))
                .willReturn(Optional.of(testProfile));
        given(storyRepository.findLatestStoryIdByProfileAndTitle(
                eq(1L), eq(10L), eq(recommendedTitle), any(Pageable.class)))
                .willReturn(List.of(42L));
        given(storyRepository.fetchByIdsWithSlides(List.of(42L)))
                .willReturn(List.of(existingStory));

        StoryInitResponse response = storyService.getStoryInitData(1L, null);

        assertThat(response.getStoryId()).isEqualTo(42L);
        assertThat(response.getCoverImageUrl()).isEqualTo("https://example.com/cover.png");
    }

    @Test
    @DisplayName("getStoryInitData - profileId 직접 지정 시 해당 프로필 사용")
    void getStoryInitData_withSpecificProfileId() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findByProfileIdAndUser_UserId(10L, 1L))
                .willReturn(Optional.of(testProfile));
        given(storyRepository.findLatestStoryIdByProfileAndTitle(
                anyLong(), anyLong(), anyString(), any(Pageable.class)))
                .willReturn(List.of());
        given(storyRepository.findLatestStoryIdByUserAndTitle(
                anyLong(), anyString(), any(Pageable.class)))
                .willReturn(List.of());

        StoryInitResponse response = storyService.getStoryInitData(1L, 10L);

        assertThat(response.getProfileId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getStoryInitData - 사용자 없음 → USER_NOT_FOUND")
    void getStoryInitData_userNotFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> storyService.getStoryInitData(99L, null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("getStoryInitData - 프로필 없음 → PROFILE_NOT_FOUND")
    void getStoryInitData_profileNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findFirstByUserOrderByCreatedAtDesc(testUser))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> storyService.getStoryInitData(1L, null))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROFILE_NOT_FOUND);
    }

    @Test
    @DisplayName("getStoryDetail - 본인 동화 조회 성공")
    void getStoryDetail_ownStory_success() {
        Story story = Story.builder()
                .storyId(1L)
                .title("흥부와 놀부")
                .user(testUser)
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .isPublic(false)
                .build();

        given(storyRepository.findByIdWithSlides(1L)).willReturn(Optional.of(story));

        var response = storyService.getStoryDetail(1L, 1L);

        assertThat(response.getStoryId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("흥부와 놀부");
    }

    @Test
    @DisplayName("getStoryDetail - 공개 동화는 타인도 조회 가능")
    void getStoryDetail_publicStory_accessibleByOthers() {
        User owner = User.builder().userId(2L).build();
        Story story = Story.builder()
                .storyId(1L)
                .title("공개 동화")
                .user(owner)
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .isPublic(true)
                .build();

        given(storyRepository.findByIdWithSlides(1L)).willReturn(Optional.of(story));

        var response = storyService.getStoryDetail(1L, 1L);

        assertThat(response.getStoryId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getStoryDetail - 비공개 동화 타인 접근 → STORY_ACCESS_DENIED")
    void getStoryDetail_privateStory_forbiddenForOthers() {
        User owner = User.builder().userId(2L).build();
        Story story = Story.builder()
                .storyId(1L)
                .title("비공개 동화")
                .user(owner)
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .isPublic(false)
                .build();

        given(storyRepository.findByIdWithSlides(1L)).willReturn(Optional.of(story));

        assertThatThrownBy(() -> storyService.getStoryDetail(1L, 1L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.STORY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("OTHER 언어 프로필 - enqueueAutoStoryGeneration 시 custom 언어 반영")
    void enqueueAutoStoryGeneration_otherLanguage() {
        UserProfile otherLangProfile = UserProfile.builder()
                .profileId(20L)
                .user(testUser)
                .childName("민준")
                .ageGroup(AgeGroup.AGE_5_6)
                .childAge(5)
                .firstLanguage(Language.OTHER)
                .customFirstLanguage("태국어")
                .firstLanguageProficiency(LanguageProficiency.BEE)
                .secondLanguage(Language.OTHER)
                .customSecondLanguage("힌디어")
                .secondLanguageProficiency(LanguageProficiency.LARVA)
                .firstLanguageListening(LanguageProficiency.BEE)
                .firstLanguageSpeaking(LanguageProficiency.PUPA)
                .secondLanguageListening(LanguageProficiency.LARVA)
                .secondLanguageSpeaking(LanguageProficiency.EGG)
                .familyStructure(FamilyStructure.TWO_PARENTS)
                .storyPreference(StoryPreference.FUN_ADVENTURE)
                .build();

        otherLangProfile.syncLegacyLanguages();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findByProfileIdAndUser_UserId(20L, 1L))
                .willReturn(Optional.of(otherLangProfile));

        MoreTaleProperties.Ai ai = new MoreTaleProperties.Ai();
        ai.setCallbackBaseUrl("http://localhost:8080");
        given(properties.getAi()).willReturn(ai);

        given(aiStoryService.enqueueStoryJob(any(), any(), any()))
                .willAnswer(inv -> {
                    var req = inv.getArgument(
                            0,
                            com.moretale.domain.story.dto.StoryGenerateRequest.class
                    );

                    assertThat(req.getPrimaryLanguage()).isEqualTo("태국어");
                    assertThat(req.getSecondaryLanguage()).isEqualTo("힌디어");

                    return com.moretale.domain.story.dto.StoryGenerationJobResponse.builder()
                            .jobId("test-job")
                            .status("queued")
                            .build();
                });

        storyService.enqueueAutoStoryGeneration(1L, 20L);
    }
}
