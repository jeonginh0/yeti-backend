package itey.backend.domain.user.dto;

import itey.backend.domain.user.entity.UserSettings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponse {

    private Integer notifyBeforeMin;
    private boolean notifyOnInvite;
    private boolean notifyOnChat;
    private String theme;
    private String language;

    public static UserSettingsResponse from(UserSettings settings) {
        return new UserSettingsResponse(
                settings.getNotifyBeforeMin(),
                settings.isNotifyOnInvite(),
                settings.isNotifyOnChat(),
                settings.getTheme(),
                settings.getLanguage()
        );
    }
}
