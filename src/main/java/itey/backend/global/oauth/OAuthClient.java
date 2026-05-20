package itey.backend.global.oauth;

import itey.backend.domain.user.entity.enums.Provider;

public interface OAuthClient {
    Provider getProvider();
    OAuthUserInfo verify(String token);
}
