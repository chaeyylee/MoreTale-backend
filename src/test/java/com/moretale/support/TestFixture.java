package com.moretale.support;

import com.moretale.domain.profile.entity.*;
import com.moretale.domain.story.entity.Slide;
import com.moretale.domain.story.entity.Story;
import com.moretale.domain.user.entity.User;

// 통합 테스트용 공통 픽스처 팩토리
public class TestFixture {

    public static User createUser(String email) {
        return User.builder()
                .email(email)
                .nickname("테스트유저")
                .role(User.Role.USER)
                .provider("google")
                .providerId("google-" + email)
                .build();
    }

    public static UserProfile createProfile(User user) {
        UserProfile profile = UserProfile.builder()
                .user(user)
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
        profile.syncLegacyLanguages();
        return profile;
    }

    public static Story createStory(User user, UserProfile profile, String title) {
        return Story.builder()
                .title(title)
                .prompt("테스트 프롬프트")
                .user(user)
                .profile(profile)
                .childName(profile.getChildName())
                .primaryLanguage("ko")
                .secondaryLanguage("vi")
                .isPublic(false)
                .build();
    }

    public static Slide createCoverSlide(String imageUrl) {
        return Slide.builder()
                .order(0)
                .imageUrl(imageUrl)
                .textKr("")
                .textNative("")
                .build();
    }

    public static Slide createContentSlide(int order, String textKr, String textNative) {
        return Slide.builder()
                .order(order)
                .imageUrl("https://example.com/slide" + order + ".png")
                .textKr(textKr)
                .textNative(textNative)
                .build();
    }
}
