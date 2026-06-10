package itey.backend.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "oauth2")
public class OAuth2Properties {
    private Google google;
    private Kakao kakao;

    @Getter
    @Setter
    public static class Google {
        private String clientId;
        /**
         * ID 토큰의 aud(audience) 검증에 허용할 client ID 목록.
         * 웹/데스크톱(Electron) 등 플랫폼별로 발급한 client ID를 모두 등록한다.
         * 비어 있으면 aud 검증을 건너뛴다(하위 호환).
         */
        private List<String> allowedClientIds = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Kakao {
        private String clientId;
    }
}
