package com.moretale.domain.honeyjar.service;

import com.moretale.domain.honeyjar.dto.HoneyJarHistoryResponse;
import com.moretale.domain.honeyjar.dto.HoneyJarResponse;
import com.moretale.domain.honeyjar.entity.HoneyJar;
import com.moretale.domain.honeyjar.entity.HoneyJarAction;
import com.moretale.domain.honeyjar.entity.HoneyJarHistory;
import com.moretale.domain.honeyjar.repository.HoneyJarHistoryRepository;
import com.moretale.domain.honeyjar.repository.HoneyJarRepository;
import com.moretale.domain.user.entity.User;
import com.moretale.domain.user.repository.UserRepository;
import com.moretale.global.exception.BusinessException;
import com.moretale.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 꿀단지 보상 관리 서비스
 *
 * userId 기반 API 추가
 *  - getHoneyJar(Long userId)        : Controller에서 User 조회 없이 직접 호출 가능
 *  - getHoneyJarHistory(Long userId) : Controller에서 User 조회 없이 직접 호출 가능
 *
 *  기존 User 엔티티 기반 메서드(getHoneyJar(User), getHoneyJarHistory(User))는
 *  QuizService 등 내부에서 이미 User 객체를 가지고 있는 경우를 위해 유지합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HoneyJarService {

    private final HoneyJarRepository honeyJarRepository;
    private final HoneyJarHistoryRepository honeyJarHistoryRepository;
    private final UserRepository userRepository;

    private static final int FREE_GENERATION_THRESHOLD = 20;


    // 꿀단지 현황 조회
    @Transactional
    public HoneyJarResponse getHoneyJarByUserId(Long userId) {
        User user = getUserById(userId);
        return getHoneyJar(user);
    }

    // 꿀단지 이력 조회
    @Transactional(readOnly = true)
    public List<HoneyJarHistoryResponse> getHoneyJarHistoryByUserId(Long userId) {
        User user = getUserById(userId);
        return getHoneyJarHistory(user);
    }


    //  꿀단지 현황 조회
    // 최초 조회 시 레코드가 없으면 생성
    @Transactional
    public HoneyJarResponse getHoneyJar(User user) {
        HoneyJar honeyJar = getOrCreateHoneyJar(user);
        return HoneyJarResponse.from(honeyJar);
    }

    // 꿀단지 이력 조회 (최신순)
    @Transactional(readOnly = true)
    public List<HoneyJarHistoryResponse> getHoneyJarHistory(User user) {
        return honeyJarHistoryRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(HoneyJarHistoryResponse::from)
                .toList();
    }

    /**
     * 꿀단지 지급 및 20개 달성 시 자동 차감 처리
     *
     * @param user    사용자 엔티티
     * @param action  지급 사유
     * @param storyId 관련 동화 ID
     * @return 자동 차감 발생 여부
     */
    @Transactional
    public boolean addHoneyJarAndCheckAutoUse(User user, HoneyJarAction action, Long storyId) {
        // 동시성 제어를 위해 비관적 락 사용
        HoneyJar honeyJar = honeyJarRepository.findByUserWithLock(user)
                .orElseGet(() -> createHoneyJar(user));

        // 꿀단지 1개 지급
        honeyJar.add(1);
        honeyJarRepository.save(honeyJar);

        // 지급 이력 저장
        saveHistory(user, action, 1, honeyJar.getCount(), storyId);

        log.info("꿀단지 지급 완료 - userId={}, action={}, 현재 잔액={}",
                user.getUserId(), action, honeyJar.getCount());

        // 20개 달성 시 자동 차감
        if (honeyJar.canGenerateFree()) {
            boolean used = honeyJar.use(FREE_GENERATION_THRESHOLD);

            if (used) {
                honeyJarRepository.save(honeyJar);

                // 사용 이력 저장
                saveHistory(
                        user,
                        HoneyJarAction.USE_FREE_GENERATION,
                        -FREE_GENERATION_THRESHOLD,
                        honeyJar.getCount(),
                        storyId
                );

                log.info("꿀단지 20개 달성 및 자동 차감 완료 - userId={}, 남은 잔액={}",
                        user.getUserId(), honeyJar.getCount());

                return true;
            }
        }

        return false;
    }

    // 꿀단지 조회 또는 신규 생성
    @Transactional
    public HoneyJar getOrCreateHoneyJar(User user) {
        return honeyJarRepository.findByUser(user)
                .orElseGet(() -> createHoneyJar(user));
    }

    private void saveHistory(User user, HoneyJarAction action, int amount,
                             int balanceAfter, Long storyId) {
        HoneyJarHistory history = HoneyJarHistory.builder()
                .user(user)
                .actionType(action)
                .amount(amount)
                .reason(action.getDescription())
                .balanceAfter(balanceAfter)
                .storyId(storyId)
                .build();

        honeyJarHistoryRepository.save(history);
    }

    // 꿀단지 엔티티 신규 생성
    private HoneyJar createHoneyJar(User user) {
        HoneyJar newHoneyJar = HoneyJar.builder()
                .user(user)
                .count(0)
                .totalEarned(0)
                .totalUsed(0)
                .build();

        return honeyJarRepository.save(newHoneyJar);
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
