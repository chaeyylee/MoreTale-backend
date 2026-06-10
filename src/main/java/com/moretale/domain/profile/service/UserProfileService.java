package com.moretale.domain.profile.service;

import com.moretale.domain.profile.dto.*;
import com.moretale.domain.profile.entity.Language;
import com.moretale.domain.profile.entity.UserProfile;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// 사용자 프로필 서비스
// CustomException -> BusinessException으로 통일
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    // 온보딩용 프로필 생성
    @Transactional
    public OnboardingProfileResponse createOnboardingProfile(Long userId, OnboardingProfileRequest request) {
        log.info("온보딩 프로필 생성 시작 - userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 동일한 이름의 아이가 이미 등록되어 있는지 체크
        if (userProfileRepository.existsByUser_UserIdAndChildName(userId, request.getChildName())) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        // OTHER가 아닌 경우 customLanguage는 null 처리 (Entity에서도 처리하지만 명시적으로)
        String customFirst = (request.getFirstLanguage() == Language.OTHER)
                ? request.getCustomFirstLanguage() : null;
        String customSecond = (request.getSecondLanguage() == Language.OTHER)
                ? request.getCustomSecondLanguage() : null;

        UserProfile profile = UserProfile.builder()
                .user(user)
                .childName(request.getChildName())
                .ageGroup(request.getAgeGroup())
                // 언어 (Enum + Custom)
                .firstLanguage(request.getFirstLanguage())
                .customFirstLanguage(customFirst)
                .firstLanguageProficiency(request.getFirstLanguageProficiency())
                .secondLanguage(request.getSecondLanguage())
                .customSecondLanguage(customSecond)
                .secondLanguageProficiency(request.getSecondLanguageProficiency())
                // 능력
                .firstLanguageListening(request.getFirstLanguageListening())
                .firstLanguageSpeaking(request.getFirstLanguageSpeaking())
                .secondLanguageListening(request.getSecondLanguageListening())
                .secondLanguageSpeaking(request.getSecondLanguageSpeaking())
                // 가족/이야기
                .familyStructure(request.getFamilyStructure())
                .customFamilyStructure(request.getCustomFamilyStructure())
                .storyPreference(request.getStoryPreference())
                .customStoryPreference(request.getCustomStoryPreference())
                // 부가
                .childNationality(request.getChildNationality())
                .parentCountry(request.getParentCountry())
                .build();

        // Legacy primaryLanguage / secondaryLanguage 동기화
        profile.syncLegacyLanguages();

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("온보딩 프로필 생성 완료 - profileId: {}, firstLanguage: {}, secondLanguage: {}",
                savedProfile.getProfileId(),
                savedProfile.getFirstLanguageDisplay(),
                savedProfile.getSecondLanguageDisplay());

        return OnboardingProfileResponse.fromEntity(savedProfile);
    }

    // 기본 프로필 생성
    @Transactional
    public UserProfileResponse createProfile(Long userId, UserProfileRequest request) {
        log.info("프로필 생성 시작 - userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 동일한 이름의 아이가 이미 등록되어 있는지 체크
        if (userProfileRepository.existsByUser_UserIdAndChildName(userId, request.getChildName())) {
            throw new BusinessException(ErrorCode.PROFILE_ALREADY_EXISTS);
        }

        String customFirst = (request.getFirstLanguage() == Language.OTHER)
                ? request.getCustomFirstLanguage() : null;
        String customSecond = (request.getSecondLanguage() == Language.OTHER)
                ? request.getCustomSecondLanguage() : null;

        UserProfile profile = UserProfile.builder()
                .user(user)
                .childName(request.getChildName())
                .ageGroup(request.getAgeGroup())
                .firstLanguage(request.getFirstLanguage())
                .customFirstLanguage(customFirst)
                .firstLanguageProficiency(request.getFirstLanguageProficiency())
                .secondLanguage(request.getSecondLanguage())
                .customSecondLanguage(customSecond)
                .secondLanguageProficiency(request.getSecondLanguageProficiency())
                .firstLanguageListening(request.getFirstLanguageListening())
                .firstLanguageSpeaking(request.getFirstLanguageSpeaking())
                .secondLanguageListening(request.getSecondLanguageListening())
                .secondLanguageSpeaking(request.getSecondLanguageSpeaking())
                .familyStructure(request.getFamilyStructure())
                .customFamilyStructure(request.getCustomFamilyStructure())
                .storyPreference(request.getStoryPreference())
                .customStoryPreference(request.getCustomStoryPreference())
                .childNationality(request.getChildNationality())
                .parentCountry(request.getParentCountry())
                .build();

        profile.syncLegacyLanguages();

        UserProfile savedProfile = userProfileRepository.save(profile);
        log.info("프로필 생성 완료 - profileId: {}", savedProfile.getProfileId());

        return UserProfileResponse.fromEntity(savedProfile);
    }

    // 특정 사용자의 모든 자녀 프로필 목록 조회
    public List<UserProfileResponse> getAllProfiles(Long userId) {
        log.info("전체 프로필 조회 - userId: {}", userId);

        return userProfileRepository.findAllByUser_UserId(userId).stream()
                .map(UserProfileResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 프로필 상세 조회
    public UserProfileResponse getProfile(Long profileId) {
        log.info("프로필 상세 조회 - profileId: {}", profileId);

        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        return UserProfileResponse.fromEntity(profile);
    }

    // 프로필 정보 수정
    @Transactional
    public UserProfileResponse updateProfile(Long userId, Long profileId, UserProfileRequest request) {
        log.info("프로필 수정 시작 - userId: {}, profileId: {}", userId, profileId);

        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 프로필 존재 확인
        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        // 본인의 프로필인지 권한 확인
        if (!profile.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 프로필 업데이트 메서드 사용
        profile.updateProfile(
                request.getChildName(),
                request.getAgeGroup(),
                request.getFirstLanguage(),
                request.getCustomFirstLanguage(),
                request.getFirstLanguageProficiency(),
                request.getSecondLanguage(),
                request.getCustomSecondLanguage(),
                request.getSecondLanguageProficiency(),
                request.getFirstLanguageListening(),
                request.getFirstLanguageSpeaking(),
                request.getSecondLanguageListening(),
                request.getSecondLanguageSpeaking(),
                request.getFamilyStructure(),
                request.getCustomFamilyStructure(),
                request.getStoryPreference(),
                request.getCustomStoryPreference(),
                request.getChildNationality(),
                request.getParentCountry()
        );

        log.info("프로필 수정 완료 - profileId: {}", profile.getProfileId());
        return UserProfileResponse.fromEntity(profile);
    }

    // 언어 설정만 수정
    @Transactional
    public UserProfileResponse updateLanguage(Long profileId, LanguageUpdateRequest request) {
        log.info("언어 설정 수정 - profileId: {}", profileId);

        // LanguageUpdateRequest 자체 검증 (OTHER + custom 누락 체크)
        request.validate();

        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        // Enum 기반으로 언어 업데이트
        profile.setFirstLanguage(request.getFirstLanguage());
        profile.setCustomFirstLanguage(
                request.getFirstLanguage() == Language.OTHER ? request.getCustomFirstLanguage() : null
        );
        profile.setSecondLanguage(request.getSecondLanguage());
        profile.setCustomSecondLanguage(
                request.getSecondLanguage() == Language.OTHER ? request.getCustomSecondLanguage() : null
        );

        // Legacy 동기화
        profile.syncLegacyLanguages();

        log.info("언어 설정 수정 완료 - profileId: {}, first: {}, second: {}",
                profileId,
                profile.getFirstLanguageDisplay(),
                profile.getSecondLanguageDisplay());

        return UserProfileResponse.fromEntity(profile);
    }

    // 프로필 삭제
    @Transactional
    public void deleteProfile(Long profileId) {
        log.info("프로필 삭제 - profileId: {}", profileId);

        UserProfile profile = userProfileRepository.findById(profileId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROFILE_NOT_FOUND));

        userProfileRepository.delete(profile);
    }

    // 프로필 존재 여부 확인
    public boolean hasProfile(Long userId) {
        return userProfileRepository.existsByUser_UserId(userId);
    }
}
