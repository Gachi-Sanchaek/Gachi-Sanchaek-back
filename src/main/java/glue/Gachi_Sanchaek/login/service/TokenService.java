package glue.Gachi_Sanchaek.login.service;


import glue.Gachi_Sanchaek.security.jwt.JWTUtil;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.service.UserService;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JWTUtil jwtUtil;
    private final RedisTemplate<String,String> redisTemplate;
    private final UserService userService;

    @Value("${jwt.access-expiration-ms:86400000}")
    private Long accessExpirationMs;

    @Value("${jwt.refresh-expiration-ms:2592000000}")
    private Long refreshExpirationMs;

    private final String REFRESH_TOKEN_PREFIX = "refresh:";

    public String createAccessToken(Long userId, String role) {
        return createAccessToken(userId, role,  accessExpirationMs);
    }

    public String createAccessToken(Long userId, String role, Long milliseconds) {
        return jwtUtil.createJwt(userId, role, milliseconds);
    }

    public String createRefreshToken(Long userId) {
        return createRefreshToken(userId, refreshExpirationMs);
    }

    public String createRefreshToken(Long userId, Long milliseconds) {
        String refreshToken = UUID.randomUUID().toString();
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, String.valueOf(userId), Duration.ofMillis(milliseconds));
        return refreshToken;
    }

    public Optional<String> validateRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        String userId = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(userId);
    }

    public String reissueAccessToken(String refreshToken) {
        String userId = validateRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is invalid or expired."));

        User user = userService.findById(Long.valueOf(userId));

        return createAccessToken(user.getId(), user.getRole());
    }
}
