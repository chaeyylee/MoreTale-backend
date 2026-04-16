package com.moretale.domain.user.service;

import com.moretale.domain.honeyjar.entity.HoneyJar;
import com.moretale.domain.honeyjar.repository.HoneyJarHistoryRepository;
import com.moretale.domain.honeyjar.repository.HoneyJarRepository;
import com.moretale.domain.profile.dto.UserProfileResponse;
import com.moretale.domain.profile.repository.UserProfileRepository;
import com.moretale.domain.story.dto.RecentStoryResponse;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.story.repository.StoryRepository;
import com.moretale.domain.user.dto.AccountInfoResponse;
import com.moretale.domain.user.dto.MyPageResponse;
import com.moretale.domain.user.dto.UsageStatusResponse;
import com.moretale.domain.user.dto.UserResponse;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.CustomException;
import com.moretale.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final HoneyJarRepository honeyJarRepository;
    private final HoneyJarHistoryRepository honeyJarHistoryRepository;
    private final StoryRepository storyRepository;

    /** 최근 동화 조회 건수 */
    private static final int RECENT_STORY_LIMIT = 5;

    // ────────────────────────────────────────────────────────────────────
    // 기존 API
    // ────────────────────────────────────────────────────────────────────

    /** 사용자 단순 정보 조회 (기존 /me API용) */
    public UserResponse getUserInfo(Long userId) {
        log.info("사용자 정보 조회 - userId: {}", userId);
        User user = findUserById(userId);
        return UserResponse.fromEntity(user);
    }

    /** 사용자 지역 설정 (기존 API) */
    @Transactional
    public UserResponse updateRegion(Long userId, String region) {
        log.info("사용자 지역 설정 - userId: {}, region: {}", userId, region);
        User user = findUserById(userId);
        user.setRegion(region);
        log.info("사용자 지역 설정 완료 - userId: {}", userId);
        return UserResponse.fromEntity(user);
    }

    // ────────────────────────────────────────────────────────────────────
    // 마이페이지 통합 조회
    // ────────────────────────────────────────────────────────────────────

    /**
     * 마이페이지 통합 조회
     * GET /api/users/mypage
     *
     * <p>구성:
     * <ul>
     *   <li>계정 정보 (read-only)</li>
     *   <li>자녀 프로필 목록</li>
     *   <li>사용 현황 (꿀단지 + 무료 동화 잔여 횟수 + 누적 동화 수)</li>
     *   <li>최근 생성 동화 5건</li>
     * </ul>
     */
    public MyPageResponse getMyPage(Long userId) {
        log.info("마이페이지 조회 - userId: {}", userId);

        // 1. 사용자 조회
        User user = findUserById(userId);

        // 2. 계정 정보
        AccountInfoResponse accountInfo = AccountInfoResponse.fromEntity(user);

        // 3. 자녀 프로필 목록
        List<UserProfileResponse> profiles = userProfileRepository
                .findAllByUser_UserId(userId)
                .stream()
                .map(UserProfileResponse::fromEntity)
                .toList();

        // 4. 사용 현황
        UsageStatusResponse usageStatus = buildUsageStatus(user);

        // 5. 최근 동화 5건 (슬라이드 썸네일 포함)
        List<RecentStoryResponse> recentStories = storyRepository
                .findRecentStoriesWithSlides(user, PageRequest.of(0, RECENT_STORY_LIMIT))
                .stream()
                .map(RecentStoryResponse::fromEntity)
                .toList();

        log.info("마이페이지 조회 완료 - userId: {}, 프로필 수: {}, 최근 동화 수: {}",
                userId, profiles.size(), recentStories.size());

        return MyPageResponse.builder()
                .accountInfo(accountInfo)
                .profiles(profiles)
                .usageStatus(usageStatus)
                .recentStories(recentStories)
                .build();
    }

    // ────────────────────────────────────────────────────────────────────
    // 회원 탈퇴
    // ────────────────────────────────────────────────────────────────────

    /**
     * 회원 탈퇴
     * DELETE /api/users/delete
     *
     * <p>연관 데이터 처리 정책:
     * <ul>
     *   <li>HoneyJarHistory → 먼저 삭제 (FK 제약)</li>
     *   <li>HoneyJar → 삭제</li>
     *   <li>Story / Slide / StoryToken → Story 삭제 시 CascadeType.ALL로 자동 처리</li>
     *   <li>UserProfile → User CascadeType.ALL + orphanRemoval로 자동 처리</li>
     *   <li>User → 최종 삭제</li>
     * </ul>
     */
    @Transactional
    public void deleteUser(Long userId) {
        log.info("회원 탈퇴 처리 시작 - userId: {}", userId);

        User user = findUserById(userId);

        // 1. 꿀단지 이력 삭제 (FK: honey_jar_histories.user_id → users.user_id)
        honeyJarHistoryRepository.deleteAllByUser(user);
        log.info("꿀단지 이력 삭제 완료 - userId: {}", userId);

        // 2. 꿀단지 삭제 (FK: honey_jars.user_id → users.user_id)
        honeyJarRepository.deleteByUser(user);
        log.info("꿀단지 삭제 완료 - userId: {}", userId);

        // 3. 동화 삭제
        //    Story → Slide → StoryToken: CascadeType.ALL + orphanRemoval로 자동 처리
        storyRepository.deleteAllByUser(user);
        log.info("동화 데이터 삭제 완료 - userId: {}", userId);

        // 4. 사용자 삭제
        //    UserProfile: User 엔티티의 CascadeType.ALL + orphanRemoval로 자동 처리
        userRepository.delete(user);
        log.info("회원 탈퇴 완료 - userId: {}", userId);
    }

    // ────────────────────────────────────────────────────────────────────
    // private 헬퍼
    // ────────────────────────────────────────────────────────────────────

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 사용 현황 빌드
     * - 꿀단지 레코드가 없는 신규 사용자는 empty() 반환
     */
    private UsageStatusResponse buildUsageStatus(User user) {
        long totalStories = storyRepository.countByUser(user);

        return honeyJarRepository.findByUser(user)
                .map(honeyJar -> UsageStatusResponse.of(honeyJar, totalStories))
                .orElseGet(() -> UsageStatusResponse.empty(totalStories));
    }
}
