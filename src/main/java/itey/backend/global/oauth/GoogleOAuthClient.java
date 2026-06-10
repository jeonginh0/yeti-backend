package itey.backend.global.oauth;

import itey.backend.domain.user.entity.enums.Provider;
import itey.backend.global.config.OAuth2Properties;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class GoogleOAuthClient implements OAuthClient {

    private final RestClient restClient;
    private final Set<String> allowedAudiences;

    public GoogleOAuthClient(RestClient.Builder builder, OAuth2Properties oAuth2Properties) {
        this.restClient = builder.build();
        this.allowedAudiences = resolveAllowedAudiences(oAuth2Properties);
    }

    private Set<String> resolveAllowedAudiences(OAuth2Properties properties) {
        Set<String> audiences = new LinkedHashSet<>();
        OAuth2Properties.Google google = properties.getGoogle();
        if (google != null) {
            if (google.getClientId() != null && !google.getClientId().isBlank()) {
                audiences.add(google.getClientId());
            }
            if (google.getAllowedClientIds() != null) {
                google.getAllowedClientIds().stream()
                        .filter(id -> id != null && !id.isBlank())
                        .forEach(audiences::add);
            }
        }
        return audiences;
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

        // aud 검증: 허용 client ID가 설정된 경우, 토큰이 우리 앱(웹/데스크톱)을 대상으로 발급됐는지 확인
        if (!allowedAudiences.isEmpty() && !allowedAudiences.contains(info.getAud())) {
            throw new IllegalArgumentException("허용되지 않은 Google client ID로 발급된 토큰입니다.");
        }

        return new OAuthUserInfo(info.getSub(), info.getEmail(), info.getName(), info.getPicture());
    }

    @Getter
    static class GoogleTokenInfo {
        private String sub;
        private String aud;
        private String email;
        private String name;
        private String picture;
    }
}
