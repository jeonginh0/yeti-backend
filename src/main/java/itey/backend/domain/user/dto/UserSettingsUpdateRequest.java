package itey.backend.domain.user.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsUpdateRequest {

    // 허용값: 15, 30, 60, 1440(하루 전)
    @Pattern(regexp = "^(15|30|60|1440)$", message = "notifyBeforeMin은 15, 30, 60, 1440 중 하나여야 합니다.")
    private String notifyBeforeMin;

    private Boolean notifyOnInvite;
    private Boolean notifyOnChat;

    @Pattern(regexp = "^(light|dark|system)$", message = "theme은 light, dark, system 중 하나여야 합니다.")
    private String theme;

    @Pattern(regexp = "^(ko|en)$", message = "language는 ko 또는 en이어야 합니다.")
    private String language;

    public Integer getNotifyBeforeMinAsInt() {
        return notifyBeforeMin != null ? Integer.parseInt(notifyBeforeMin) : null;
    }
}
