package itey.backend.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import itey.backend.domain.user.dto.EmailLoginRequest;
import itey.backend.domain.user.dto.EmailSignupRequest;
import itey.backend.domain.user.dto.FcmTokenRequest;
import itey.backend.domain.user.dto.OAuthLoginRequest;
import itey.backend.domain.user.dto.OnboardingRequest;
import itey.backend.domain.user.dto.PlanStatusResponse;
import itey.backend.domain.user.dto.RefreshRequest;
import itey.backend.domain.user.dto.TokenResponse;
import itey.backend.domain.user.entity.enums.Provider;
import itey.backend.domain.user.service.AuthService;
import itey.backend.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "이메일 회원가입",
            description = "이메일과 비밀번호로 회원가입합니다. 가입 후 온보딩(username 설정)이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 이메일")
    })
    @PostMapping("/signup")
    public ResponseEntity<TokenResponse> signup(@RequestBody EmailSignupRequest request) {
        return ResponseEntity.ok(authService.emailSignup(
                request.getEmail(), request.getPassword(), request.getUsername(), request.getNickname()));
    }

    @Operation(
            summary = "이메일 로그인",
            description = "이메일과 비밀번호로 로그인합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호 불일치")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody EmailLoginRequest request) {
        return ResponseEntity.ok(authService.emailLogin(request.getEmail(), request.getPassword()));
    }

    @Operation(
            summary = "소셜 로그인",
            description = "Google 또는 Kakao OAuth 토큰으로 로그인합니다. " +
                    "신규 유저면 `newUser: true`가 반환되며 온보딩이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 provider"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 OAuth 토큰")
    })
    @PostMapping("/oauth2/{provider}")
    public ResponseEntity<TokenResponse> login(
            @Parameter(description = "OAuth 제공자 (google, kakao)", example = "google")
            @PathVariable String provider,
            @RequestBody OAuthLoginRequest request) {
        Provider providerEnum = Provider.valueOf(provider.toUpperCase());
        return ResponseEntity.ok(authService.oauthLogin(providerEnum, request.getToken()));
    }

    @Operation(
            summary = "토큰 갱신",
            description = "Refresh Token으로 새로운 Access Token을 발급합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @Operation(
            summary = "로그아웃",
            description = "Refresh Token을 무효화합니다."
    )
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    @DeleteMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "온보딩 완료",
            description = "신규 유저가 사용자명(username)을 설정합니다. " +
                    "`newUser: true`로 로그인한 경우 반드시 호출해야 합니다.",
            security = @SecurityRequirement(name = "Bearer")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "온보딩 완료"),
            @ApiResponse(responseCode = "400", description = "사용자명 형식 오류 (2~20자, 영문/숫자/언더스코어)"),
            @ApiResponse(responseCode = "409", description = "이미 사용 중인 사용자명")
    })
    @PostMapping("/onboarding")
    public ResponseEntity<Void> completeOnboarding(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody OnboardingRequest request) {
        authService.completeOnboarding(principal.getId(), request.getUsername(), request.getNickname());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "FCM 토큰 등록", security = @SecurityRequirement(name = "Bearer"))
    @PatchMapping("/fcm-token")
    public ResponseEntity<Void> registerFcmToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody FcmTokenRequest request) {
        authService.registerFcmToken(principal.getId(), request.getFcmToken());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "구독 플랜 조회",
            description = "현재 로그인한 사용자의 구독 플랜 상태를 조회합니다.",
            security = @SecurityRequirement(name = "Bearer")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/plan")
    public ResponseEntity<PlanStatusResponse> getPlanStatus(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.getPlanStatus(principal.getId()));
    }
}
