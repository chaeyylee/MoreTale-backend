package com.moretale.domain.honeyjar.service;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HoneyJarService 단위 테스트")
class HoneyJarServiceTest {

    @Mock HoneyJarRepository honeyJarRepository;
    @Mock HoneyJarHistoryRepository honeyJarHistoryRepository;
    @Mock UserRepository userRepository;

    @InjectMocks HoneyJarService honeyJarService;

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

    // ────────────────── getHoneyJar ──────────────────

    @Test
    @DisplayName("꿀단지 조회 - 기존 레코드 있음")
    void getHoneyJar_existingRecord() {
        HoneyJar honeyJar = HoneyJar.builder()
                .honeyJarId(1L)
                .user(testUser)
                .count(5)
                .totalEarned(7)
                .totalUsed(2)
                .build();

        given(honeyJarRepository.findByUser(testUser)).willReturn(Optional.of(honeyJar));

        HoneyJarResponse response = honeyJarService.getHoneyJar(testUser);

        assertThat(response.getCount()).isEqualTo(5);
        assertThat(response.getTotalEarned()).isEqualTo(7);
        assertThat(response.getTotalUsed()).isEqualTo(2);
        assertThat(response.getCanGenerateFree()).isFalse();
        assertThat(response.getRemainingForFree()).isEqualTo(5);
    }

    @Test
    @DisplayName("꿀단지 조회 - 레코드 없으면 신규 생성")
    void getHoneyJar_noRecord_createNew() {
        HoneyJar newHoneyJar = HoneyJar.builder()
                .honeyJarId(1L)
                .user(testUser)
                .count(0)
                .totalEarned(0)
                .totalUsed(0)
                .build();

        given(honeyJarRepository.findByUser(testUser)).willReturn(Optional.empty());
        given(honeyJarRepository.save(any(HoneyJar.class))).willReturn(newHoneyJar);

        HoneyJarResponse response = honeyJarService.getHoneyJar(testUser);

        assertThat(response.getCount()).isEqualTo(0);
        assertThat(response.getRemainingForFree()).isEqualTo(10);
        verify(honeyJarRepository).save(any(HoneyJar.class));
    }

    @Test
    @DisplayName("꿀단지 10개 이상 - canGenerateFree = true")
    void getHoneyJar_tenOrMore_canGenerateFreeTrue() {
        HoneyJar honeyJar = HoneyJar.builder()
                .user(testUser)
                .count(10)
                .totalEarned(10)
                .totalUsed(0)
                .build();

        given(honeyJarRepository.findByUser(testUser)).willReturn(Optional.of(honeyJar));

        HoneyJarResponse response = honeyJarService.getHoneyJar(testUser);

        assertThat(response.getCanGenerateFree()).isTrue();
        assertThat(response.getRemainingForFree()).isEqualTo(0);
    }

    @Test
    @DisplayName("꿀단지 조회 - userId 기반")
    void getHoneyJarByUserId_success() {
        HoneyJar honeyJar = HoneyJar.builder()
                .user(testUser).count(3).totalEarned(3).totalUsed(0).build();

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(honeyJarRepository.findByUser(testUser)).willReturn(Optional.of(honeyJar));

        HoneyJarResponse response = honeyJarService.getHoneyJarByUserId(1L);

        assertThat(response.getCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("꿀단지 조회 - userId 없음 → USER_NOT_FOUND")
    void getHoneyJarByUserId_userNotFound() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> honeyJarService.getHoneyJarByUserId(99L))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    // ────────────────── addHoneyJarAndCheckAutoUse ──────────────────

    @Test
    @DisplayName("꿀단지 지급 - 9개 → 10개 미달, 자동차감 없음")
    void addHoneyJar_belowThreshold_noAutoUse() {
        HoneyJar honeyJar = HoneyJar.builder()
                .user(testUser).count(8).totalEarned(8).totalUsed(0).build();

        given(honeyJarRepository.findByUserWithLock(testUser)).willReturn(Optional.of(honeyJar));
        given(honeyJarRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(honeyJarHistoryRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        boolean autoUsed = honeyJarService.addHoneyJarAndCheckAutoUse(
                testUser, HoneyJarAction.EARN_STORY_COMPLETE, 1L);

        assertThat(autoUsed).isFalse();
        assertThat(honeyJar.getCount()).isEqualTo(9);
    }

    @Test
    @DisplayName("꿀단지 지급 - 9개 → 10개 달성, 자동차감 발생")
    void addHoneyJar_reachesThreshold_autoUse() {
        HoneyJar honeyJar = HoneyJar.builder()
                .user(testUser).count(9).totalEarned(9).totalUsed(0).build();

        given(honeyJarRepository.findByUserWithLock(testUser)).willReturn(Optional.of(honeyJar));
        given(honeyJarRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(honeyJarHistoryRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        boolean autoUsed = honeyJarService.addHoneyJarAndCheckAutoUse(
                testUser, HoneyJarAction.EARN_STORY_COMPLETE, 1L);

        assertThat(autoUsed).isTrue();
        // 10개 달성 후 10개 차감 → 0개
        assertThat(honeyJar.getCount()).isEqualTo(0);
        // 이력 저장 2번 (지급 + 차감)
        verify(honeyJarHistoryRepository, times(2)).save(any(HoneyJarHistory.class));
    }

    @Test
    @DisplayName("꿀단지 지급 - 레코드 없으면 신규 생성 후 지급")
    void addHoneyJar_noRecord_createAndAdd() {
        HoneyJar newHoneyJar = HoneyJar.builder()
                .user(testUser).count(0).totalEarned(0).totalUsed(0).build();

        given(honeyJarRepository.findByUserWithLock(testUser)).willReturn(Optional.empty());
        given(honeyJarRepository.save(any())).willAnswer(inv -> {
            HoneyJar arg = inv.getArgument(0);
            return arg;
        });
        given(honeyJarHistoryRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        boolean autoUsed = honeyJarService.addHoneyJarAndCheckAutoUse(
                testUser, HoneyJarAction.EARN_QUIZ_PERFECT, 5L);

        assertThat(autoUsed).isFalse();
        verify(honeyJarRepository, atLeast(1)).save(any(HoneyJar.class));
    }

    @Test
    @DisplayName("꿀단지 이력 조회 - 최신순 반환")
    void getHoneyJarHistory_returnsList() {
        HoneyJarHistory h1 = HoneyJarHistory.builder()
                .user(testUser).actionType(HoneyJarAction.EARN_STORY_COMPLETE)
                .amount(1).reason("완독").balanceAfter(1).build();
        HoneyJarHistory h2 = HoneyJarHistory.builder()
                .user(testUser).actionType(HoneyJarAction.EARN_QUIZ_PERFECT)
                .amount(1).reason("퀴즈").balanceAfter(2).build();

        given(honeyJarHistoryRepository.findByUserOrderByCreatedAtDesc(testUser))
                .willReturn(List.of(h2, h1));

        var result = honeyJarService.getHoneyJarHistory(testUser);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getActionType())
                .isEqualTo(HoneyJarAction.EARN_QUIZ_PERFECT.name());
    }
}
