package com.moretale.domain.profile.entity;

import com.moretale.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 자녀 프로필 정보 엔티티
 * 한 명의 사용자(부모)가 여러 명의 자녀를 가질 수 있다.
 *
 * [언어 필드 구조]
 * - firstLanguage / secondLanguage       : Enum (Source of Truth)
 * - customFirstLanguage / customSecond.. : OTHER 선택 시 직접 입력값
 * - primaryLanguage / secondaryLanguage  : @Deprecated Legacy 호환용 파생값
 *   → syncLegacyLanguages() 호출 시 자동 동기화
 */
@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id")
    private Long profileId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "child_name", nullable = false, length = 50)
    private String childName;

    // 나이
    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false)
    private AgeGroup ageGroup;

    // 실제 나이 (대표값, 자동 계산)
    @Column(name = "child_age", nullable = false)
    private Integer childAge;

    // 언어 (Enum + Custom)
    // 첫 번째 언어 Enum (Source of Truth)
    // OTHER 선택 시 customFirstLanguage에 실제 언어명 저장
    @Enumerated(EnumType.STRING)
    @Column(name = "first_language_enum", nullable = false, length = 10)
    private Language firstLanguage;

    @Column(name = "custom_first_language", length = 100)
    private String customFirstLanguage;

    // 두 번째 언어 Enum (Source of Truth)
    // OTHER 선택 시 customSecondLanguage에 실제 언어명 저장
    @Enumerated(EnumType.STRING)
    @Column(name = "second_language_enum", nullable = false, length = 10)
    private Language secondLanguage;

    @Column(name = "custom_second_language", length = 100)
    private String customSecondLanguage;

    // 언어 숙련도
    @Enumerated(EnumType.STRING)
    @Column(name = "first_language_proficiency", nullable = false)
    private LanguageProficiency firstLanguageProficiency;

    @Enumerated(EnumType.STRING)
    @Column(name = "second_language_proficiency", nullable = false)
    private LanguageProficiency secondLanguageProficiency;

    // 언어 능력 (듣기/말하기)
    @Enumerated(EnumType.STRING)
    @Column(name = "first_language_listening", nullable = false)
    private LanguageProficiency firstLanguageListening;

    @Enumerated(EnumType.STRING)
    @Column(name = "first_language_speaking", nullable = false)
    private LanguageProficiency firstLanguageSpeaking;

    @Enumerated(EnumType.STRING)
    @Column(name = "second_language_listening", nullable = false)
    private LanguageProficiency secondLanguageListening;

    @Enumerated(EnumType.STRING)
    @Column(name = "second_language_speaking", nullable = false)
    private LanguageProficiency secondLanguageSpeaking;

    // 가족 구조
    @Enumerated(EnumType.STRING)
    @Column(name = "family_structure", nullable = false)
    private FamilyStructure familyStructure;

    @Column(name = "custom_family_structure", length = 200)
    private String customFamilyStructure;

    // 이야기 선호도
    @Enumerated(EnumType.STRING)
    @Column(name = "story_preference", nullable = false)
    private StoryPreference storyPreference;

    @Column(name = "custom_story_preference", length = 200)
    private String customStoryPreference;

    // 부가 정보
    @Column(name = "child_nationality", length = 50)
    private String childNationality;

    @Column(name = "parent_country", length = 50)
    private String parentCountry;

    // Legacy 호환 필드 (@Deprecated)
    /**
     * @deprecated firstLanguage(Enum) 기반으로 syncLegacyLanguages() 통해 자동 동기화.
     *             StoryService, TTS, Quiz 등 기존 참조 로직 유지용.
     */
    @Deprecated
    @Column(name = "primary_language", length = 100)
    private String primaryLanguage;

    /**
     * @deprecated secondLanguage(Enum) 기반으로 syncLegacyLanguages() 통해 자동 동기화.
     */
    @Deprecated
    @Column(name = "secondary_language", length = 100)
    private String secondaryLanguage;

    // 타임스탬프
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 비즈니스 로직

    // 나이 그룹에서 실제 나이 자동 계산
    @PrePersist
    @PreUpdate
    public void calculateChildAge() {
        if (this.childAge == null && this.ageGroup != null) {
            this.childAge = this.ageGroup.getRepresentativeAge();
        }
        // 저장/수정 시 항상 Legacy 필드 동기화
        syncLegacyLanguages();
    }

    /**
     * Legacy 호환: primaryLanguage / secondaryLanguage 자동 동기화
     *
     * - OTHER인 경우: customXxxLanguage 값으로 동기화
     * - 일반 Enum:   Enum name() 값으로 동기화 (ex. "KO", "EN")
     *
     * StoryService, TTS, Quiz 등 기존 서비스는 primary/secondary를 그대로 참조하면 됨.
     */
    public void syncLegacyLanguages() {
        if (this.firstLanguage != null) {
            this.primaryLanguage = this.firstLanguage.resolveCode(this.customFirstLanguage);
        }
        if (this.secondLanguage != null) {
            this.secondaryLanguage = this.secondLanguage.resolveCode(this.customSecondLanguage);
        }
    }

    // 첫 번째 언어 표시명 반환 (UI/로그용)
    // ex) KO → "한국어", OTHER + customValue → "태국어"
    public String getFirstLanguageDisplay() {
        if (this.firstLanguage == Language.OTHER) {
            return this.customFirstLanguage != null ? this.customFirstLanguage : "기타";
        }
        return this.firstLanguage != null ? this.firstLanguage.getDescription() : null;
    }

    // 두 번째 언어 표시명 반환 (UI/로그용)
    public String getSecondLanguageDisplay() {
        if (this.secondLanguage == Language.OTHER) {
            return this.customSecondLanguage != null ? this.customSecondLanguage : "기타";
        }
        return this.secondLanguage != null ? this.secondLanguage.getDescription() : null;
    }

    // 프로필 전체 업데이트
    public void updateProfile(
            String childName,
            AgeGroup ageGroup,
            Integer childAge,
            Language firstLanguage,
            String customFirstLanguage,
            LanguageProficiency firstLanguageProficiency,
            Language secondLanguage,
            String customSecondLanguage,
            LanguageProficiency secondLanguageProficiency,
            LanguageProficiency firstLanguageListening,
            LanguageProficiency firstLanguageSpeaking,
            LanguageProficiency secondLanguageListening,
            LanguageProficiency secondLanguageSpeaking,
            FamilyStructure familyStructure,
            String customFamilyStructure,
            StoryPreference storyPreference,
            String customStoryPreference,
            String childNationality,
            String parentCountry
    ) {
        this.childName = childName;
        this.ageGroup = ageGroup;
        this.childAge = childAge;

        this.firstLanguage = firstLanguage;
        // OTHER가 아닌 경우 customFirstLanguage는 null 처리
        this.customFirstLanguage = (firstLanguage == Language.OTHER) ? customFirstLanguage : null;

        this.firstLanguageProficiency = firstLanguageProficiency;

        this.secondLanguage = secondLanguage;
        this.customSecondLanguage = (secondLanguage == Language.OTHER) ? customSecondLanguage : null;
        this.secondLanguageProficiency = secondLanguageProficiency;

        this.firstLanguageListening = firstLanguageListening;
        this.firstLanguageSpeaking = firstLanguageSpeaking;
        this.secondLanguageListening = secondLanguageListening;
        this.secondLanguageSpeaking = secondLanguageSpeaking;

        this.familyStructure = familyStructure;
        // CUSTOM이 아닌 경우 null 처리
        this.customFamilyStructure = (familyStructure == FamilyStructure.CUSTOM) ? customFamilyStructure : null;

        this.storyPreference = storyPreference;
        this.customStoryPreference = (storyPreference == StoryPreference.CUSTOM) ? customStoryPreference : null;

        this.childNationality = childNationality;
        this.parentCountry = parentCountry;

        // 하위 호환성 필드 동기화
        syncLegacyLanguages();

        // updatedAt 명시적 갱신 (Hibernate가 처리하지만 명확성을 위해 유지)
        this.updatedAt = LocalDateTime.now();
    }
}
