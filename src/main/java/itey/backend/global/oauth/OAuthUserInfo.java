package itey.backend.global.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class OAuthUserInfo {
    private String providerId;
    private String email;
    private String nickname;
    private String profileImageUrl;
}
