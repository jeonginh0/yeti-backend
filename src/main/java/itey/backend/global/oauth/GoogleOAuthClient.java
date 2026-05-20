package itey.backend.global.oauth;

import itey.backend.domain.user.entity.enums.Provider;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GoogleOAuthClient implements OAuthClient {

    private final RestClient restClient;

    public GoogleOAuthClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public Provider getProvider() {
        return Provider.GOOGLE;
    }

    @Override
    public OAuthUserInfo verify(String idToken) {
        GoogleTokenInfo info = restClient.get()
                .uri("https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken)
                .retrieve()
                .body(GoogleTokenInfo.class);

        if (info == null || info.getSub() == null) {
            throw new IllegalArgumentException("유효하지 않은 Google ID 토큰입니다.");
        }

        return new OAuthUserInfo(info.getSub(), info.getEmail(), info.getName(), info.getPicture());
    }

    @Getter
    static class GoogleTokenInfo {
        private String sub;
        private String email;
        private String name;
        private String picture;
    }
}
