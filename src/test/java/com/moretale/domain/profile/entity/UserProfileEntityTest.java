package com.moretale.domain.profile.entity;

import com.moretale.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserProfile 엔티티 비즈니스 로직 테스트")
class UserProfileEntityTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().userId(1L).email("test@example.com").build();
    }

    @Test
    @DisplayName("calculateChildAge - ageGroup으로 childAge 자동 계산")
    void calculateChildAge_setsRepresentativeAge() {
        UserProfile profile = buildProfile(AgeGroup.AGE_5_6);
        profile.setChildAge(null);
        profile.calculateChildAge();

        assertThat(profile.getChildAge()).isEqualTo(5);
    }

    @Test
    @DisplayName("calculateChildAge - AGE_0_2 → childAge = 1")
    void calculateChildAge_age0to2() {
        UserProfile profile = buildProfile(AgeGroup.AGE_0_2);
        profile.setChildAge(null);
        profile.calculateChildAge();

        assertThat(profile.getChildAge()).isEqualTo(1);
    }

    @Test
    @DisplayName("calculateChildAge - 각 ageGroup 대표값 매핑 전체 검증")
    void calculateChildAge_allAgeGroups() {
        assertThat(AgeGroup.AGE_0_2.getRepresentativeAge()).isEqualTo(1);
        assertThat(AgeGroup.AGE_3_4.getRepresentativeAge()).isEqualTo(3);
        assertThat(AgeGroup.AGE_5_6.getRepresentativeAge()).isEqualTo(5);
        assertThat(AgeGroup.AGE_7_8.getRepresentativeAge()).isEqualTo(7);
        assertThat(AgeGroup.AGE_9_10.getRepresentativeAge()).isEqualTo(9);
        assertThat(AgeGroup.AGE_10_PLUS.getRepresentativeAge()).isEqualTo(10);
    }

    @Test
    @DisplayName("syncLegacyLanguages - KO → primaryLanguage = KO")
    void sync_normalEnum_setsName() {
        UserProfile profile = buildProfile(AgeGroup.AGE_5_6);
        profile.setFirstLanguage(Language.KO);
        profile.setCustomFirstLanguage(null);
        profile.setSecondLanguage(Language.VI);
        profile.setCustomSecondLanguage(null);
        profile.syncLegacyLanguages();

        assertThat(profile.getPrimaryLanguage()).isEqualTo("KO");
        assertThat(profile.getSecondaryLanguage()).isEqualTo("VI");
    }

    @Test
    @DisplayName("syncLegacyLanguages - OTHER + custom → primaryLanguage = custom 값")
    void sync_other_setsCustomValue() {
        UserProfile profile = buildProfile(AgeGroup.AGE_5_6);
        profile.setFirstLanguage(Language.OTHER);
        profile.setCustomFirstLanguage("태국어");
        profile.setSecondLanguage(Language.OTHER);
        profile.setCustomSecondLanguage("힌디어");
        profile.syncLegacyLanguages();

        assertThat(profile.getPrimaryLanguage()).isEqualTo("태국어");
        assertThat(profile.getSecondaryLanguage()).isEqualTo("힌디어");
    }

    @Test
    @DisplayName("syncLegacyLanguages - OTHER + null custom → OTHER 문자열 저장")
    void sync_other_nullCustom_storesOtherString() {
        UserProfile profile = buildProfile(AgeGroup.AGE_5_6);
        profile.setFirstLanguage(Language.OTHER);
        profile.setCustomFirstLanguage(null);
        profile.syncLegacyLanguages();

        assertThat(profile.getPrimaryLanguage()).isEqualTo("OTHER");
    }

    @Test
    @DisplayName("getFirstLanguageDisplay - 각 Enum 값 표시명 검증")
    void getFirstLanguageDisplay_allLanguages() {
        UserProfile profile = buildProfile(AgeGroup.AGE_5_6);

        profile.setFirstLanguage(Language.KO);
        assertThat(profile.getFirstLanguageDisplay()).isEqualTo("한국어");

        profile.setFirstLanguage(Language.EN);
        assertThat(profile.getFirstLanguageDisplay()).isEqualTo("영어");

        profile.setFirstLanguage(Language.VI);
        assertThat(profile.getFirstLanguageDisplay()).isEqualTo("베트남어");

        profile.setFirstLanguage(Language.JA);
        assertThat(profile.getFirstLanguageDisplay()).isEqualTo("일본어");

        profile.setFirstLanguage(Language.ZH);
        assertThat(profile.getFirstLanguageDisplay()).isEqualTo("중국어");

        profile.setFirstLanguage(Language.ES);
        assertThat(profile.getFirstLanguageDisplay()).isEqualTo("스페인어");
    }

    @Test
    @DisplayName("getFirstLanguageDisplay - OTHER + custom → custom 값 반환")
    void getFirstLanguageDisplay_other_returnsCustom() {
        UserProfile profile = buildProfile(AgeGroup.AGE_5_6);
        profile.setFirstLanguage(Language.OTHER);
        profile.setCustomFirstLanguage("태국어");

        assertThat(profile.getFirstLanguageDisplay()).isEqualTo("태국어");
    }

    @Test
    @DisplayName("getFirstLanguageDisplay - OTHER + null → 기타")
    void getFirstLanguageDisplay_other_nullCustom_returnsDefault() {
        UserProfile profile = buildProfile(AgeGroup.AGE_5_6);
        profile.setFirstLanguage(Language.OTHER);
        profile.setCustomFirstLanguage(null);

        assertThat(profile.getFirstLanguageDisplay()).isEqualTo("기타");
    }

    @Test
    @DisplayName("updateProfile - OTHER → KO 변경 시 customFirstLanguage null 처리")
    void updateProfile_otherToKo_clearsCustomLanguage() {
        UserProfile profile = buildProfile(AgeGroup.AGE_5_6);
        profile.setFirstLanguage(Language.OTHER);
        profile.setCustomFirstLanguage("태국어");

        profile.updateProfile(
                "민준", AgeGroup.AGE_7_8, 7,
                Language.KO, "이 값은 무시돼야 함", LanguageProficiency.BEE,
                Language.VI, null, LanguageProficiency.LARVA,
                LanguageProficiency.BEE, LanguageProficiency.PUPA,
                LanguageProficiency.LARVA, LanguageProficiency.EGG,
                FamilyStructure.TWO_PARENTS, null,
                StoryPreference.FUN_ADVENTURE, null,
                "KR", "VN"
        );

        assertThat(profile.getFirstLanguage()).isEqualTo(Language.KO);
        assertThat(profile.getCustomFirstLanguage()).isNull();
        assertThat(profile.getChildAge()).isEqualTo(7);
    }

    @Test
    @DisplayName("updateProfile - CUSTOM → TWO_PARENTS 변경 시 customFamilyStructure null 처리")
    void updateProfile_customToTwoParents_clearsFamilyStructure() {
        UserProfile profile = buildProfile(AgeGroup.AGE_5_6);
        profile.setFamilyStructure(FamilyStructure.CUSTOM);
        profile.setCustomFamilyStructure("조부모님과 살아요");

        profile.updateProfile(
                "민준", AgeGroup.AGE_5_6, 6,
                Language.KO, null, LanguageProficiency.BEE,
                Language.VI, null, LanguageProficiency.LARVA,
                LanguageProficiency.BEE, LanguageProficiency.PUPA,
                LanguageProficiency.LARVA, LanguageProficiency.EGG,
                FamilyStructure.TWO_PARENTS, "이 값 무시",
                StoryPreference.FUN_ADVENTURE, null,
                null, null
        );

        assertThat(profile.getFamilyStructure()).isEqualTo(FamilyStructure.TWO_PARENTS);
        assertThat(profile.getCustomFamilyStructure()).isNull();
        assertThat(profile.getChildAge()).isEqualTo(6);
    }

    private UserProfile buildProfile(AgeGroup ageGroup) {
        return UserProfile.builder()
                .user(testUser)
                .childName("민준")
                .ageGroup(ageGroup)
                .childAge(ageGroup.getRepresentativeAge())
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
}
