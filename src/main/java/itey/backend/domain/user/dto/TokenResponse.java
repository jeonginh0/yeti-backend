package itey.backend.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private boolean newUser;
    // username 미설정(주로 OAuth 가입 후 온보딩 미완료) 시 true. 프론트는 이 값으로 온보딩 유도.
    private boolean requiresOnboarding;
}
