package com.moretale.domain.user.service;

import com.moretale.domain.honeyjar.entity.HoneyJar;
import com.moretale.domain.honeyjar.repository.HoneyJarHistoryRepository;
import com.moretale.domain.honeyjar.repository.HoneyJarRepository;
import com.moretale.domain.profile.entity.*;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.story.dto.RecentStoryResponse;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.user.dto.MyPageResponse;
import com.moretale.domain.user.dto.UserResponse;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock UserProfileRepository userProfileRepository;
    @Mock HoneyJarRepository honeyJarRepository;
    @Mock HoneyJarHistoryRepository honeyJarHistoryRepository;
    @Mock StoryRepository storyRepository;

    @InjectMocks UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L).email("test@example.com")
                .nickname("홍길동").role(User.Role.USER)
                .build();
    }

    // ────────────────── getUserInfo ──────────────────

    @Test
    @DisplayName("사용자 정보 조회 - 정상")
    void getUserInfo_success() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        UserResponse response = userService.getUserInfo(1L);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getEmail()).isEqualTo("test@example.com");
        assertThat(response.getNickname()).isEqualTo("홍길동");
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    @DisplayName("사용자 정보 조회 - 없음 → USER_NOT_FOUND")
    void getUserInfo_notFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserInfo(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    // ────────────────── getMyPage ──────────────────

    @Test
    @DisplayName("마이페이지 조회 - 꿀단지 있음")
    void getMyPage_withHoneyJar() {
        UserProfile profile = buildProfile();
        HoneyJar honeyJar = HoneyJar.builder()
                .user(testUser).count(7).totalEarned(10).totalUsed(3).build();

        Story story = buildStory();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findAllByUser_UserId(1L)).willReturn(List.of(profile));
        given(honeyJarRepository.findByUser(testUser)).willReturn(Optional.of(honeyJar));
        given(storyRepository.countByUser(testUser)).willReturn(5L);
        given(storyRepository.findRecentStoriesWithSlides(eq(testUser), any(Pageable.class)))
                .willReturn(List.of(story));

        MyPageResponse response = userService.getMyPage(1L);

        assertThat(response.getAccountInfo().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getProfiles()).hasSize(1);
        assertThat(response.getUsageStatus().getHoneyJarCount()).isEqualTo(7);
        assertThat(response.getUsageStatus().getCanGenerateFreeStory()).isFalse();
        assertThat(response.getUsageStatus().getRemainingHoneyJarForFree()).isEqualTo(13);
        assertThat(response.getUsageStatus().getTotalStoriesCreated()).isEqualTo(5L);
        assertThat(response.getRecentStories()).hasSize(1);
    }

    @Test
    @DisplayName("마이페이지 조회 - 신규 사용자 (꿀단지 레코드 없음) → 0 반환")
    void getMyPage_newUser_noHoneyJar_returnsEmpty() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findAllByUser_UserId(1L)).willReturn(List.of());
        given(honeyJarRepository.findByUser(testUser)).willReturn(Optional.empty());
        given(storyRepository.countByUser(testUser)).willReturn(0L);
        given(storyRepository.findRecentStoriesWithSlides(eq(testUser), any(Pageable.class)))
                .willReturn(List.of());

        MyPageResponse response = userService.getMyPage(1L);

        assertThat(response.getUsageStatus().getHoneyJarCount()).isEqualTo(0);
        assertThat(response.getUsageStatus().getCanGenerateFreeStory()).isFalse();
        assertThat(response.getUsageStatus().getRemainingHoneyJarForFree()).isEqualTo(20);
        assertThat(response.getRecentStories()).isEmpty();
    }

    @Test
    @DisplayName("마이페이지 조회 - 꿀단지 20개 이상 → canGenerateFreeStory = true")
    void getMyPage_twentyHoneyJars_canGenerateFreeStory() {
        HoneyJar honeyJar = HoneyJar.builder()
                .user(testUser).count(20).totalEarned(20).totalUsed(0).build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findAllByUser_UserId(1L)).willReturn(List.of());
        given(honeyJarRepository.findByUser(testUser)).willReturn(Optional.of(honeyJar));
        given(storyRepository.countByUser(testUser)).willReturn(0L);
        given(storyRepository.findRecentStoriesWithSlides(eq(testUser), any(Pageable.class)))
                .willReturn(List.of());

        MyPageResponse response = userService.getMyPage(1L);

        assertThat(response.getUsageStatus().getCanGenerateFreeStory()).isTrue();
        assertThat(response.getUsageStatus().getRemainingHoneyJarForFree()).isEqualTo(0);
    }

    // ────────────────── deleteUser ──────────────────

    @Test
    @DisplayName("회원 탈퇴 - 정상, 순서대로 삭제")
    void deleteUser_success_correctOrder() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        userService.deleteUser(1L);

        // 순서 검증: 이력 → 꿀단지 → 동화 → 사용자
        var inOrder = inOrder(
                honeyJarHistoryRepository, honeyJarRepository,
                storyRepository, userRepository);
        inOrder.verify(honeyJarHistoryRepository).deleteAllByUser(testUser);
        inOrder.verify(honeyJarRepository).deleteByUser(testUser);
        inOrder.verify(storyRepository).deleteAllByUser(testUser);
        inOrder.verify(userRepository).delete(testUser);
    }

    @Test
    @DisplayName("회원 탈퇴 - 사용자 없음 → USER_NOT_FOUND")
    void deleteUser_notFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);

        verify(honeyJarHistoryRepository, never()).deleteAllByUser(any());
        verify(userRepository, never()).delete(any());
    }

    // ────────────────── 헬퍼 ──────────────────

    private UserProfile buildProfile() {
        UserProfile p = UserProfile.builder()
                .profileId(10L).user(testUser).childName("민준")
                .ageGroup(AgeGroup.AGE_5_6).childAge(5)
                .firstLanguage(Language.KO).firstLanguageProficiency(LanguageProficiency.BEE)
                .secondLanguage(Language.VI).secondLanguageProficiency(LanguageProficiency.LARVA)
                .firstLanguageListening(LanguageProficiency.BEE)
                .firstLanguageSpeaking(LanguageProficiency.PUPA)
                .secondLanguageListening(LanguageProficiency.LARVA)
                .secondLanguageSpeaking(LanguageProficiency.EGG)
                .familyStructure(FamilyStructure.TWO_PARENTS)
                .storyPreference(StoryPreference.FUN_ADVENTURE)
                .build();
        p.syncLegacyLanguages();
        return p;
    }

    private Story buildStory() {
        Story story = Story.builder()
                .storyId(1L).title("흥부와 놀부").user(testUser)
                .primaryLanguage("ko").secondaryLanguage("vi").isPublic(false).build();
        story.addSlide(Slide.builder().order(0)
                .imageUrl("https://example.com/thumb.png").build());
        return story;
    }
}
