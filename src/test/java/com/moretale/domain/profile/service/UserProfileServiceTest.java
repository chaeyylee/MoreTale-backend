package com.moretale.domain.profile.service;

import com.moretale.domain.profile.dto.*;
import com.moretale.domain.profile.entity.*;
import com.moretale.domain.profile.repository.UserProfileRepository;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileService 단위 테스트")
class UserProfileServiceTest {

    @Mock UserProfileRepository userProfileRepository;
    @Mock UserRepository userRepository;

    @InjectMocks UserProfileService userProfileService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(1L)
                .email("test@example.com")
                .nickname("홍길동")
                .role(User.Role.USER)
                .build();
    }

    @Test
    @DisplayName("온보딩 프로필 생성 - 정상 (일반 언어)")
    void createOnboardingProfile_success_normalLanguage() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.existsByUser_UserIdAndChildName(1L, "민준")).willReturn(false);
        given(userProfileRepository.save(any(UserProfile.class)))
                .willAnswer(inv -> {
                    UserProfile p = inv.getArgument(0);
                    // 저장 후 id 세팅 흉내
                    return UserProfile.builder()
                            .profileId(10L)
                            .user(testUser)
                            .childName(p.getChildName())
                            .ageGroup(p.getAgeGroup())
                            .childAge(p.getAgeGroup().getRepresentativeAge())
                            .firstLanguage(p.getFirstLanguage())
                            .customFirstLanguage(p.getCustomFirstLanguage())
                            .firstLanguageProficiency(p.getFirstLanguageProficiency())
                            .secondLanguage(p.getSecondLanguage())
                            .customSecondLanguage(p.getCustomSecondLanguage())
                            .secondLanguageProficiency(p.getSecondLanguageProficiency())
                            .firstLanguageListening(p.getFirstLanguageListening())
                            .firstLanguageSpeaking(p.getFirstLanguageSpeaking())
                            .secondLanguageListening(p.getSecondLanguageListening())
                            .secondLanguageSpeaking(p.getSecondLanguageSpeaking())
                            .familyStructure(p.getFamilyStructure())
                            .storyPreference(p.getStoryPreference())
                            .build();
                });

        OnboardingProfileRequest request = validOnboardingRequest();

        // when
        OnboardingProfileResponse response =
                userProfileService.createOnboardingProfile(1L, request);

        // then
        assertThat(response.getProfileId()).isEqualTo(10L);
        assertThat(response.getChildName()).isEqualTo("민준");
        assertThat(response.getFirstLanguage()).isEqualTo(Language.KO);
        assertThat(response.getCustomFirstLanguage()).isNull();
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("온보딩 프로필 생성 - OTHER 언어 + customFirstLanguage 저장")
    void createOnboardingProfile_success_otherLanguage() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.existsByUser_UserIdAndChildName(1L, "민준")).willReturn(false);
        given(userProfileRepository.save(any(UserProfile.class)))
                .willAnswer(inv -> {
                    UserProfile p = inv.getArgument(0);
                    // OTHER인 경우 customFirstLanguage 저장 확인
                    assertThat(p.getCustomFirstLanguage()).isEqualTo("태국어");
                    return buildSavedProfile(p, 10L);
                });

        OnboardingProfileRequest request = validOnboardingRequest();
        request.setFirstLanguage(Language.OTHER);
        request.setCustomFirstLanguage("태국어");

        // when
        userProfileService.createOnboardingProfile(1L, request);

        // then
        verify(userProfileRepository).save(any(UserProfile.class));
    }

    @Test
    @DisplayName("온보딩 프로필 생성 - OTHER가 아닌 언어 선택 시 customFirstLanguage null 처리")
    void createOnboardingProfile_nonOtherLanguage_customLanguageSetNull() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.existsByUser_UserIdAndChildName(1L, "민준")).willReturn(false);
        given(userProfileRepository.save(any(UserProfile.class)))
                .willAnswer(inv -> {
                    UserProfile p = inv.getArgument(0);
                    // KO 선택 시 custom은 null이어야 함
                    assertThat(p.getCustomFirstLanguage()).isNull();
                    return buildSavedProfile(p, 10L);
                });

        OnboardingProfileRequest request = validOnboardingRequest();
        request.setFirstLanguage(Language.KO);
        request.setCustomFirstLanguage("이 값은 무시돼야 함");

        // when
        userProfileService.createOnboardingProfile(1L, request);
    }

    @Test
    @DisplayName("온보딩 프로필 생성 - 사용자 없음 → USER_NOT_FOUND")
    void createOnboardingProfile_userNotFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                userProfileService.createOnboardingProfile(99L, validOnboardingRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("온보딩 프로필 생성 - 동일 이름 자녀 이미 존재 → PROFILE_ALREADY_EXISTS")
    void createOnboardingProfile_alreadyExists() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.existsByUser_UserIdAndChildName(1L, "민준")).willReturn(true);

        assertThatThrownBy(() ->
                userProfileService.createOnboardingProfile(1L, validOnboardingRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROFILE_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("프로필 수정 - 정상")
    void updateProfile_success() {
        UserProfile existingProfile = buildExistingProfile(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findById(10L)).willReturn(Optional.of(existingProfile));

        UserProfileRequest request = validProfileRequest();
        request.setChildName("지우");
        request.setAgeGroup(AgeGroup.AGE_7_8);

        UserProfileResponse response = userProfileService.updateProfile(1L, 10L, request);

        assertThat(response.getChildName()).isEqualTo("지우");
        assertThat(response.getAgeGroup()).isEqualTo(AgeGroup.AGE_7_8);
    }

    @Test
    @DisplayName("프로필 수정 - 본인 소유 아님 → FORBIDDEN")
    void updateProfile_forbiddenForOtherUser() {
        User anotherUser = User.builder().userId(2L).email("other@example.com").build();
        UserProfile profileOwnedByAnother = buildProfileForUser(anotherUser, 10L);

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findById(10L)).willReturn(Optional.of(profileOwnedByAnother));

        assertThatThrownBy(() ->
                userProfileService.updateProfile(1L, 10L, validProfileRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("프로필 수정 - 프로필 없음 → PROFILE_NOT_FOUND")
    void updateProfile_profileNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userProfileRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() ->
                userProfileService.updateProfile(1L, 999L, validProfileRequest()))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROFILE_NOT_FOUND);
    }

    @Test
    @DisplayName("언어 수정 - KO → EN 변경 + Legacy 동기화")
    void updateLanguage_success_enumChange() {
        UserProfile profile = buildExistingProfile(1L);
        given(userProfileRepository.findById(10L)).willReturn(Optional.of(profile));

        LanguageUpdateRequest request = LanguageUpdateRequest.builder()
                .firstLanguage(Language.EN)
                .customFirstLanguage(null)
                .secondLanguage(Language.JA)
                .customSecondLanguage(null)
                .build();

        UserProfileResponse response = userProfileService.updateLanguage(10L, request);

        assertThat(response.getFirstLanguage()).isEqualTo(Language.EN);
        assertThat(response.getSecondLanguage()).isEqualTo(Language.JA);
        assertThat(response.getCustomFirstLanguage()).isNull();
    }

    @Test
    @DisplayName("언어 수정 - OTHER → customSecondLanguage 저장")
    void updateLanguage_success_otherWithCustom() {
        UserProfile profile = buildExistingProfile(1L);
        given(userProfileRepository.findById(10L)).willReturn(Optional.of(profile));

        LanguageUpdateRequest request = LanguageUpdateRequest.builder()
                .firstLanguage(Language.KO)
                .customFirstLanguage(null)
                .secondLanguage(Language.OTHER)
                .customSecondLanguage("태국어")
                .build();

        UserProfileResponse response = userProfileService.updateLanguage(10L, request);

        assertThat(response.getSecondLanguage()).isEqualTo(Language.OTHER);
        assertThat(response.getCustomSecondLanguage()).isEqualTo("태국어");
    }

    @Test
    @DisplayName("언어 수정 - OTHER + custom 누락 → IllegalArgumentException")
    void updateLanguage_otherWithoutCustom_throws() {
        LanguageUpdateRequest request = LanguageUpdateRequest.builder()
                .firstLanguage(Language.OTHER)
                .customFirstLanguage(null)
                .secondLanguage(Language.VI)
                .build();

        assertThatThrownBy(() -> userProfileService.updateLanguage(10L, request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("전체 프로필 목록 조회")
    void getAllProfiles_returnsList() {
        UserProfile p1 = buildExistingProfile(1L);
        UserProfile p2 = buildExistingProfile(2L);
        given(userProfileRepository.findAllByUser_UserId(1L)).willReturn(List.of(p1, p2));

        List<UserProfileResponse> profiles = userProfileService.getAllProfiles(1L);

        assertThat(profiles).hasSize(2);
    }

    @Test
    @DisplayName("hasProfile - 프로필 존재 시 true")
    void hasProfile_returnsTrue() {
        given(userProfileRepository.existsByUser_UserId(1L)).willReturn(true);
        assertThat(userProfileService.hasProfile(1L)).isTrue();
    }

    @Test
    @DisplayName("hasProfile - 프로필 없음 시 false")
    void hasProfile_returnsFalse() {
        given(userProfileRepository.existsByUser_UserId(1L)).willReturn(false);
        assertThat(userProfileService.hasProfile(1L)).isFalse();
    }

    @Test
    @DisplayName("syncLegacyLanguages - KO 선택 시 primaryLanguage = KO")
    void syncLegacyLanguages_normalEnum() {
        UserProfile profile = buildExistingProfile(1L);
        profile.setFirstLanguage(Language.KO);
        profile.setCustomFirstLanguage(null);
        profile.syncLegacyLanguages();

        assertThat(profile.getPrimaryLanguage()).isEqualTo("KO");
    }

    @Test
    @DisplayName("syncLegacyLanguages - OTHER + custom 시 primaryLanguage = custom 값")
    void syncLegacyLanguages_otherWithCustom() {
        UserProfile profile = buildExistingProfile(1L);
        profile.setFirstLanguage(Language.OTHER);
        profile.setCustomFirstLanguage("태국어");
        profile.syncLegacyLanguages();

        assertThat(profile.getPrimaryLanguage()).isEqualTo("태국어");
    }

    @Test
    @DisplayName("getFirstLanguageDisplay - KO → 한국어")
    void getFirstLanguageDisplay_ko() {
        UserProfile profile = buildExistingProfile(1L);
        profile.setFirstLanguage(Language.KO);
        assertThat(profile.getFirstLanguageDisplay()).isEqualTo("한국어");
    }

    @Test
    @DisplayName("getFirstLanguageDisplay - OTHER + custom → custom 값 반환")
    void getFirstLanguageDisplay_otherWithCustom() {
        UserProfile profile = buildExistingProfile(1L);
        profile.setFirstLanguage(Language.OTHER);
        profile.setCustomFirstLanguage("태국어");
        assertThat(profile.getFirstLanguageDisplay()).isEqualTo("태국어");
    }

    @Test
    @DisplayName("getFirstLanguageDisplay - OTHER + null → 기타")
    void getFirstLanguageDisplay_otherWithNull() {
        UserProfile profile = buildExistingProfile(1L);
        profile.setFirstLanguage(Language.OTHER);
        profile.setCustomFirstLanguage(null);
        assertThat(profile.getFirstLanguageDisplay()).isEqualTo("기타");
    }

    private OnboardingProfileRequest validOnboardingRequest() {
        return OnboardingProfileRequest.builder()
                .childName("민준")
                .ageGroup(AgeGroup.AGE_5_6)
                .firstLanguage(Language.KO)
                .customFirstLanguage(null)
                .secondLanguage(Language.VI)
                .customSecondLanguage(null)
                .firstLanguageProficiency(LanguageProficiency.BEE)
                .secondLanguageProficiency(LanguageProficiency.LARVA)
                .firstLanguageListening(LanguageProficiency.BEE)
                .firstLanguageSpeaking(LanguageProficiency.PUPA)
                .secondLanguageListening(LanguageProficiency.LARVA)
                .secondLanguageSpeaking(LanguageProficiency.EGG)
                .familyStructure(FamilyStructure.TWO_PARENTS)
                .customFamilyStructure(null)
                .storyPreference(StoryPreference.FUN_ADVENTURE)
                .customStoryPreference(null)
                .build();
    }

    private UserProfileRequest validProfileRequest() {
        return UserProfileRequest.builder()
                .childName("민준")
                .ageGroup(AgeGroup.AGE_5_6)
                .firstLanguage(Language.KO)
                .secondLanguage(Language.VI)
                .firstLanguageProficiency(LanguageProficiency.BEE)
                .secondLanguageProficiency(LanguageProficiency.LARVA)
                .firstLanguageListening(LanguageProficiency.BEE)
                .firstLanguageSpeaking(LanguageProficiency.PUPA)
                .secondLanguageListening(LanguageProficiency.LARVA)
                .secondLanguageSpeaking(LanguageProficiency.EGG)
                .familyStructure(FamilyStructure.TWO_PARENTS)
                .storyPreference(StoryPreference.FUN_ADVENTURE)
                .build();
    }

    private UserProfile buildExistingProfile(Long profileId) {
        UserProfile p = UserProfile.builder()
                .profileId(profileId)
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
        p.syncLegacyLanguages();
        return p;
    }

    private UserProfile buildProfileForUser(User user, Long profileId) {
        return UserProfile.builder()
                .profileId(profileId)
                .user(user)
                .childName("민준")
                .ageGroup(AgeGroup.AGE_5_6)
                .childAge(5)
                .firstLanguage(Language.KO)
                .firstLanguageProficiency(LanguageProficiency.BEE)
                .secondLanguage(Language.VI)
                .secondLanguageProficiency(LanguageProficiency.LARVA)
                .firstLanguageListening(LanguageProficiency.BEE)
                .firstLanguageSpeaking(LanguageProficiency.PUPA)
                .secondLanguageListening(LanguageProficiency.LARVA)
                .secondLanguageSpeaking(LanguageProficiency.EGG)
                .familyStructure(FamilyStructure.TWO_PARENTS)
                .storyPreference(StoryPreference.FUN_ADVENTURE)
                .build();
    }

    private UserProfile buildSavedProfile(UserProfile p, Long profileId) {
        return UserProfile.builder()
                .profileId(profileId)
                .user(p.getUser())
                .childName(p.getChildName())
                .ageGroup(p.getAgeGroup())
                .childAge(p.getAgeGroup().getRepresentativeAge())
                .firstLanguage(p.getFirstLanguage())
                .customFirstLanguage(p.getCustomFirstLanguage())
                .firstLanguageProficiency(p.getFirstLanguageProficiency())
                .secondLanguage(p.getSecondLanguage())
                .customSecondLanguage(p.getCustomSecondLanguage())
                .secondLanguageProficiency(p.getSecondLanguageProficiency())
                .firstLanguageListening(p.getFirstLanguageListening())
                .firstLanguageSpeaking(p.getFirstLanguageSpeaking())
                .secondLanguageListening(p.getSecondLanguageListening())
                .secondLanguageSpeaking(p.getSecondLanguageSpeaking())
                .familyStructure(p.getFamilyStructure())
                .customFamilyStructure(p.getCustomFamilyStructure())
                .storyPreference(p.getStoryPreference())
                .customStoryPreference(p.getCustomStoryPreference())
                .childNationality(p.getChildNationality())
                .parentCountry(p.getParentCountry())
                .build();
    }
}
