package itey.backend.global.oauth;

import itey.backend.domain.user.entity.enums.Provider;
import itey.backend.global.config.OAuth2Properties;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class GoogleOAuthClient implements OAuthClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuthClient.class);

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
    public OAuthUserInfo verify(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Google 토큰이 비어 있습니다.");
        }
        // ID 토큰(JWT, eyJ... 3등분)과 access token(ya29...)을 구분해 검증
        return isJwt(token) ? verifyIdToken(token) : verifyAccessToken(token);
    }

    private boolean isJwt(String token) {
        return token.startsWith("eyJ") && token.split("\\.").length == 3;
    }

    private OAuthUserInfo verifyIdToken(String idToken) {
        GoogleTokenInfo info = restClient.get()
                .uri(uri -> uri.scheme("https").host("oauth2.googleapis.com")
                        .path("/tokeninfo").queryParam("id_token", idToken).build())
                .retrieve()
                .body(GoogleTokenInfo.class);

        if (info == null || info.getSub() == null) {
            throw new IllegalArgumentException("유효하지 않은 Google ID 토큰입니다.");
        }
        verifyAudience(info.getAud());

        return new OAuthUserInfo(info.getSub(), info.getEmail(), info.getName(), info.getPicture());
    }

    private OAuthUserInfo verifyAccessToken(String accessToken) {
        // 1) access token 유효성 + aud 검증
        GoogleTokenInfo info = restClient.get()
                .uri(uri -> uri.scheme("https").host("oauth2.googleapis.com")
                        .path("/tokeninfo").queryParam("access_token", accessToken).build())
                .retrieve()
                .body(GoogleTokenInfo.class);

        if (info == null || info.getSub() == null) {
            throw new IllegalArgumentException("유효하지 않은 Google access token입니다.");
        }
        verifyAudience(info.getAud());

        // 2) 프로필(name, picture) 조회 - tokeninfo에는 없는 정보 보강
        GoogleUserInfo profile = restClient.get()
                .uri(uri -> uri.scheme("https").host("www.googleapis.com")
                        .path("/oauth2/v3/userinfo").build())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(GoogleUserInfo.class);

        String email = profile != null && profile.getEmail() != null ? profile.getEmail() : info.getEmail();
        String name = profile != null ? profile.getName() : null;
        String picture = profile != null ? profile.getPicture() : null;

        return new OAuthUserInfo(info.getSub(), email, name, picture);
    }

    private void verifyAudience(String aud) {
        // 허용 client ID가 설정된 경우, 토큰이 우리 앱(웹/데스크톱)을 대상으로 발급됐는지 확인
        if (!allowedAudiences.isEmpty() && !allowedAudiences.contains(aud)) {
            log.warn("Google aud 불일치 - 토큰 aud=[{}], 허용 목록={}. " +
                    "이 aud를 GOOGLE_ALLOWED_CLIENT_IDS에 추가하거나 GOOGLE_CLIENT_ID와 일치시키세요.",
                    aud, allowedAudiences);
            throw new IllegalArgumentException("허용되지 않은 Google client ID로 발급된 토큰입니다.");
        }
    }

    @Getter
    static class GoogleTokenInfo {
        private String sub;
        private String aud;
        private String email;
        private String name;
        private String picture;
    }

    @Getter
    static class GoogleUserInfo {
        private String sub;
        private String email;
        private String name;
        private String picture;
    }
}
