package com.moretale.domain.profile.dto;

import com.moretale.domain.profile.entity.Language;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 언어 설정 단독 수정 요청 DTO
 *
 * 기존 String 패턴 → Language Enum으로 변경
 * Legacy primaryLanguage/secondaryLanguage는 Service에서 자동 동기화
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "언어 설정 수정 요청 DTO")
public class LanguageUpdateRequest {

    @NotNull(message = "첫 번째 언어는 필수입니다.")
    @Schema(description = "첫 번째 언어 (KO/EN/JA/ZH/ES/VI/OTHER)", example = "KO")
    private Language firstLanguage;

    @Size(max = 100, message = "직접 입력 언어명은 100자 이하여야 합니다.")
    @Schema(description = "첫 번째 언어 직접 입력 (OTHER 선택 시 필수)", example = "태국어")
    private String customFirstLanguage;

    @NotNull(message = "두 번째 언어는 필수입니다.")
    @Schema(description = "두 번째 언어 (KO/EN/JA/ZH/ES/VI/OTHER)", example = "VI")
    private Language secondLanguage;

    @Size(max = 100, message = "직접 입력 언어명은 100자 이하여야 합니다.")
    @Schema(description = "두 번째 언어 직접 입력 (OTHER 선택 시 필수)", example = "힌디어")
    private String customSecondLanguage;

    // OTHER 선택 시 custom 값 검증
    public void validate() {
        if (Language.OTHER.equals(firstLanguage) &&
                (customFirstLanguage == null || customFirstLanguage.isBlank())) {
            throw new IllegalArgumentException("'기타' 선택 시 첫 번째 언어를 직접 입력해주세요.");
        }
        if (Language.OTHER.equals(secondLanguage) &&
                (customSecondLanguage == null || customSecondLanguage.isBlank())) {
            throw new IllegalArgumentException("'기타' 선택 시 두 번째 언어를 직접 입력해주세요.");
        }
    }
}
