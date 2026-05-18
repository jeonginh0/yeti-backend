package itey.backend.global.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import itey.backend.domain.user.entity.enums.Provider;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KakaoOAuthClient implements OAuthClient {

    private final RestClient restClient;

    public KakaoOAuthClient(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    @Override
    public Provider getProvider() {
        return Provider.KAKAO;
    }

    @Override
    public OAuthUserInfo verify(String accessToken) {
        KakaoUserInfo info = restClient.get()
                .uri("https://kapi.kakao.com/v2/user/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(KakaoUserInfo.class);

        if (info == null || info.getId() == null) {
            throw new IllegalArgumentException("유효하지 않은 Kakao 액세스 토큰입니다.");
        }

        KakaoAccount account = info.getKakaoAccount();
        String email = account != null ? account.getEmail() : null;
        String nickname = account != null && account.getProfile() != null
                ? account.getProfile().getNickname() : null;
        String profileImageUrl = account != null && account.getProfile() != null
                ? account.getProfile().getProfileImageUrl() : null;

        return new OAuthUserInfo(String.valueOf(info.getId()), email, nickname, profileImageUrl);
    }

    @Getter
    static class KakaoProfile {
        private String nickname;
        @JsonProperty("profile_image_url")
        private String profileImageUrl;
    }

    @Getter
    static class KakaoAccount {
        private String email;
        private KakaoProfile profile;
    }

    @Getter
    static class KakaoUserInfo {
        private Long id;
        @JsonProperty("kakao_account")
        private KakaoAccount kakaoAccount;
    }
}
