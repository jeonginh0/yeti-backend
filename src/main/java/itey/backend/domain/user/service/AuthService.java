package itey.backend.domain.user.service;

import itey.backend.domain.user.dto.PlanStatusResponse;
import itey.backend.domain.user.dto.TokenResponse;
import itey.backend.domain.user.entity.User;
import itey.backend.domain.user.entity.enums.PlanStatus;
import itey.backend.domain.user.entity.enums.Provider;
import itey.backend.domain.user.repository.UserRepository;
import itey.backend.global.oauth.OAuthClient;
import itey.backend.global.oauth.OAuthUserInfo;
import itey.backend.global.security.JwtProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final Map<Provider, OAuthClient> oauthClients;

    public AuthService(UserRepository userRepository,
                       JwtProvider jwtProvider,
                       TokenService tokenService,
                       PasswordEncoder passwordEncoder,
                       List<OAuthClient> oauthClientList) {
        this.userRepository = userRepository;
        this.jwtProvider = jwtProvider;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.oauthClients = oauthClientList.stream()
                .collect(Collectors.toMap(OAuthClient::getProvider, c -> c));
    }

    public TokenResponse oauthLogin(Provider provider, String token) {
        OAuthClient client = oauthClients.get(provider);
        if (client == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth provider입니다.");
        }
        OAuthUserInfo userInfo = client.verify(token);

        AtomicBoolean isNew = new AtomicBoolean(false);
        User user = userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
                .orElseGet(() -> {
                    isNew.set(true);
                    return createUser(userInfo, provider);
                });

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.isAdmin());
        String refreshToken = tokenService.createRefreshToken(user.getId());
        return new TokenResponse(accessToken, refreshToken, "Bearer", isNew.get());
    }

    public TokenResponse emailSignup(String email, String password, String username, String nickname) {
        if (!username.matches("^[a-zA-Z0-9_]{2,20}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "사용자명은 2~20자의 영문, 숫자, 언더스코어만 사용 가능합니다.");
        }
        LocalDateTime now = LocalDateTime.now();
        String resolvedNickname = (nickname != null && !nickname.isBlank())
                ? nickname
                : email.split("@")[0];

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .username(username)
                .nickname(resolvedNickname)
                .provider(Provider.EMAIL)
                .providerId(email)
                .planStatus(PlanStatus.TRIAL)
                .trialStartedAt(now)
                .trialEndsAt(now.plusDays(7))
                .build();

        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            String message = e.getMessage() != null ? e.getMessage() : "";
            if (message.contains("users_username_key")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 사용자명입니다.");
            }
            if (message.contains("users_email_key")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 정보입니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.isAdmin());
        String refreshToken = tokenService.createRefreshToken(user.getId());
        return new TokenResponse(accessToken, refreshToken, "Bearer", false);
    }

    public TokenResponse emailLogin(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.isAdmin());
        String refreshToken = tokenService.createRefreshToken(user.getId());
        return new TokenResponse(accessToken, refreshToken, "Bearer", false);
    }

    public void completeOnboarding(UUID userId, String username, String nickname) {
        if (!username.matches("^[a-zA-Z0-9_]{2,20}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "사용자명은 2~20자의 영문, 숫자, 언더스코어만 사용 가능합니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        user.updateUsername(username);
        if (nickname != null && !nickname.isBlank()) {
            user.updateNickname(nickname);
        }
        try {
            userRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 사용자명입니다.");
        }
    }

    public TokenResponse refresh(String refreshToken) {
        UUID userId = tokenService.getUserId(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다."));

        tokenService.delete(refreshToken);
        String newRefreshToken = tokenService.createRefreshToken(userId);
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.isAdmin());
        return new TokenResponse(accessToken, newRefreshToken, "Bearer", false);
    }

    public void logout(String refreshToken) {
        tokenService.delete(refreshToken);
    }

    @Transactional(readOnly = true)
    public PlanStatusResponse getPlanStatus(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        return new PlanStatusResponse(
                user.getPlanStatus().name().toLowerCase(),
                user.getTrialEndsAt(),
                user.getSubscriptionExpiresAt()
        );
    }

    private User createUser(OAuthUserInfo userInfo, Provider provider) {
        LocalDateTime now = LocalDateTime.now();
        String nickname = userInfo.getNickname() != null
                ? userInfo.getNickname()
                : userInfo.getEmail() != null ? userInfo.getEmail().split("@")[0] : "사용자";

        User user = User.builder()
                .email(userInfo.getEmail())
                .nickname(nickname)
                .profileImageUrl(userInfo.getProfileImageUrl())
                .provider(provider)
                .providerId(userInfo.getProviderId())
                .planStatus(PlanStatus.TRIAL)
                .trialStartedAt(now)
                .trialEndsAt(now.plusDays(7))
                .build();

        return userRepository.save(user);
    }
}
