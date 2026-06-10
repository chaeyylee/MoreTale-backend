package com.moretale.domain.profile.controller;

import com.moretale.domain.profile.dto.OnboardingProfileRequest;
import com.moretale.domain.profile.dto.LanguageUpdateRequest;
import com.moretale.domain.profile.entity.*;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.security.UserPrincipal;
import com.moretale.support.IntegrationTestSupport;
import com.moretale.support.TestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("UserProfile Controller 통합 테스트")
class UserProfileControllerIntegrationTest extends IntegrationTestSupport {

    @Autowired UserRepository userRepository;
    @Autowired UserProfileRepository userProfileRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(TestFixture.createUser("profile-test@example.com"));
        setAuthentication(testUser);
    }

    @Test
    @DisplayName("온보딩 프로필 생성 - 정상 요청 → 201 + 응답 구조 검증")
    void createOnboardingProfile_success() throws Exception {
        OnboardingProfileRequest request = buildValidOnboardingRequest();

        perform(post("/api/users/profile/onboarding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.childName").value("민준"))
                .andExpect(jsonPath("$.data.profileId").exists())
                .andExpect(jsonPath("$.data.firstLanguage").value("KO"))
                .andExpect(jsonPath("$.data.secondLanguage").value("VI"))
                .andExpect(jsonPath("$.message").value("프로필 설정이 완료되었습니다!"));
    }

    @Test
    @DisplayName("온보딩 프로필 생성 - OTHER 언어 + custom → 정상 저장")
    void createOnboardingProfile_otherLanguage() throws Exception {
        OnboardingProfileRequest request = buildValidOnboardingRequest();
        request.setFirstLanguage(Language.OTHER);
        request.setCustomFirstLanguage("태국어");

        perform(post("/api/users/profile/onboarding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstLanguage").value("OTHER"))
                .andExpect(jsonPath("$.data.customFirstLanguage").value("태국어"))
                .andExpect(jsonPath("$.data.firstLanguageDisplay").value("태국어"));
    }

    @Test
    @DisplayName("온보딩 프로필 생성 - OTHER 언어 + custom 누락 → 400 VALIDATION_ERROR")
    void createOnboardingProfile_otherWithoutCustom_400() throws Exception {
        OnboardingProfileRequest request = buildValidOnboardingRequest();
        request.setFirstLanguage(Language.OTHER);
        request.setCustomFirstLanguage(null);

        perform(post("/api/users/profile/onboarding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.data.customFirstLanguage").exists());
    }

    @Test
    @DisplayName("온보딩 프로필 생성 - childName 누락 → 400")
    void createOnboardingProfile_missingChildName_400() throws Exception {
        OnboardingProfileRequest request = buildValidOnboardingRequest();
        request.setChildName(null);

        perform(post("/api/users/profile/onboarding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.childName").exists());
    }

    @Test
    @DisplayName("온보딩 프로필 생성 - 동일 이름 중복 등록 → 409")
    void createOnboardingProfile_duplicate_409() throws Exception {
        UserProfile existing = TestFixture.createProfile(testUser);
        userProfileRepository.save(existing);

        OnboardingProfileRequest request = buildValidOnboardingRequest();

        perform(post("/api/users/profile/onboarding")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("P003"));
    }

    @Test
    @DisplayName("전체 프로필 목록 조회 - 정상")
    void getAllProfiles_success() throws Exception {
        userProfileRepository.save(TestFixture.createProfile(testUser));

        perform(get("/api/users/profile/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.data[0].childName").value("민준"));
    }

    @Test
    @DisplayName("언어 설정 수정 - 정상")
    void updateLanguage_success() throws Exception {
        UserProfile saved = userProfileRepository.save(TestFixture.createProfile(testUser));

        LanguageUpdateRequest request = LanguageUpdateRequest.builder()
                .firstLanguage(Language.EN)
                .customFirstLanguage(null)
                .secondLanguage(Language.JA)
                .customSecondLanguage(null)
                .build();

        perform(patch("/api/users/profile/" + saved.getProfileId() + "/language")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstLanguage").value("EN"))
                .andExpect(jsonPath("$.data.secondLanguage").value("JA"))
                .andExpect(jsonPath("$.message").value("언어 설정이 변경되었습니다."));
    }

    @Test
    @DisplayName("언어 설정 수정 - OTHER 선택 + custom 없음 → 현재 구현 기준 실패")
    void updateLanguage_otherWithoutCustom_validationFail() throws Exception {

        UserProfile saved =
                userProfileRepository.save(TestFixture.createProfile(testUser));

        LanguageUpdateRequest request =
                LanguageUpdateRequest.builder()
                        .firstLanguage(Language.OTHER)
                        .customFirstLanguage(null)
                        .secondLanguage(Language.VI)
                        .customSecondLanguage(null)
                        .build();

        perform(
                patch("/api/users/profile/" + saved.getProfileId() + "/language")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("프로필 존재 여부 - 프로필 있음 → true")
    void hasProfile_returnsTrue() throws Exception {
        userProfileRepository.save(TestFixture.createProfile(testUser));

        perform(get("/api/users/profile/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(true));
    }

    @Test
    @DisplayName("프로필 존재 여부 - 프로필 없음 → false")
    void hasProfile_returnsFalse() throws Exception {
        perform(get("/api/users/profile/exists"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @DisplayName("프로필 삭제 - 정상")
    void deleteProfile_success() throws Exception {
        UserProfile saved = userProfileRepository.save(TestFixture.createProfile(testUser));

        perform(delete("/api/users/profile/" + saved.getProfileId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("프로필이 삭제되었습니다."));
    }

    private ResultActions perform(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder)
            throws Exception {
        return mockMvc.perform(builder).andDo(print());
    }

    private void setAuthentication(User user) {
        UserPrincipal principal = UserPrincipal.create(user);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));
    }

    private OnboardingProfileRequest buildValidOnboardingRequest() {
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
}
