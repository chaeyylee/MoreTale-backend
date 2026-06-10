package com.moretale.domain.honeyjar.entity;

import com.moretale.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HoneyJar 엔티티 단위 테스트")
class HoneyJarTest {

    private HoneyJar buildHoneyJar(int count) {
        return HoneyJar.builder()
                .user(User.builder().userId(1L).build())
                .count(count)
                .totalEarned(count)
                .totalUsed(0)
                .build();
    }

    @Test
    @DisplayName("add() - count, totalEarned 증가")
    void add_increasesCountAndTotalEarned() {
        HoneyJar honeyJar = buildHoneyJar(3);
        honeyJar.add(2);

        assertThat(honeyJar.getCount()).isEqualTo(5);
        assertThat(honeyJar.getTotalEarned()).isEqualTo(5);
        assertThat(honeyJar.getTotalUsed()).isEqualTo(0);
    }

    @Test
    @DisplayName("use() - 잔액 충분 시 차감 성공 → true")
    void use_sufficientBalance_returnsTrue() {
        HoneyJar honeyJar = buildHoneyJar(10);
        boolean result = honeyJar.use(10);

        assertThat(result).isTrue();
        assertThat(honeyJar.getCount()).isEqualTo(0);
        assertThat(honeyJar.getTotalUsed()).isEqualTo(10);
    }

    @Test
    @DisplayName("use() - 잔액 부족 시 차감 실패 → false")
    void use_insufficientBalance_returnsFalse() {
        HoneyJar honeyJar = buildHoneyJar(5);
        boolean result = honeyJar.use(10);

        assertThat(result).isFalse();
        assertThat(honeyJar.getCount()).isEqualTo(5); // 변화 없음
        assertThat(honeyJar.getTotalUsed()).isEqualTo(0);
    }

    @Test
    @DisplayName("canGenerateFree() - 10개 이상 시 true")
    void canGenerateFree_tenOrMore_returnsTrue() {
        assertThat(buildHoneyJar(10).canGenerateFree()).isTrue();
        assertThat(buildHoneyJar(11).canGenerateFree()).isTrue();
    }

    @Test
    @DisplayName("canGenerateFree() - 9개 이하 시 false")
    void canGenerateFree_belowTen_returnsFalse() {
        assertThat(buildHoneyJar(9).canGenerateFree()).isFalse();
        assertThat(buildHoneyJar(0).canGenerateFree()).isFalse();
    }

    @Test
    @DisplayName("9개 → +1 → 10개 달성 → use(10) → 0개")
    void scenario_add9_add1_reach10_use10() {
        HoneyJar honeyJar = buildHoneyJar(9);
        honeyJar.add(1);

        assertThat(honeyJar.canGenerateFree()).isTrue();

        boolean used = honeyJar.use(10);
        assertThat(used).isTrue();
        assertThat(honeyJar.getCount()).isEqualTo(0);
        assertThat(honeyJar.getTotalEarned()).isEqualTo(10);
        assertThat(honeyJar.getTotalUsed()).isEqualTo(10);
    }
}
