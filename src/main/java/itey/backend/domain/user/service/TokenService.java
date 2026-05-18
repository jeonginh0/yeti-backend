package itey.backend.domain.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private static final String REFRESH_PREFIX = "refresh:";

    private final StringRedisTemplate redisTemplate;
    private final long refreshExpiration;

    public TokenService(StringRedisTemplate redisTemplate,
                        @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.redisTemplate = redisTemplate;
        this.refreshExpiration = refreshExpiration;
    }

    public String createRefreshToken(UUID userId) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + token,
                userId.toString(),
                Duration.ofSeconds(refreshExpiration)
        );
        return token;
    }

    public Optional<UUID> getUserId(String refreshToken) {
        String value = redisTemplate.opsForValue().get(REFRESH_PREFIX + refreshToken);
        return Optional.ofNullable(value).map(UUID::fromString);
    }

    public void delete(String refreshToken) {
        redisTemplate.delete(REFRESH_PREFIX + refreshToken);
    }
}
