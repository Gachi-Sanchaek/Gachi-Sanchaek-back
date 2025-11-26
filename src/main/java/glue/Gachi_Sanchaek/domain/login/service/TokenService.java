package glue.Gachi_Sanchaek.domain.login.service;


import glue.Gachi_Sanchaek.common.redis.service.RedisService;
import glue.Gachi_Sanchaek.common.security.jwt.JWTUtil;
import glue.Gachi_Sanchaek.domain.user.entity.User;
import glue.Gachi_Sanchaek.domain.user.service.UserService;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JWTUtil jwtUtil;
    private final RedisService redisService;
    private final UserService userService;

    @Value("${jwt.access-expiration-sec:86400}")
    private Long accessExpirationSecond;

    @Value("${jwt.refresh-expiration-sec:2592000}")
    private Long refreshExpirationSecond;

    private final String REFRESH_TOKEN_PREFIX = "refresh:";

    public String createAccessToken(Long userId, String role) {
        return createAccessToken(userId, role,  accessExpirationSecond);
    }

    public String createAccessToken(Long userId, String role, Long seconds) {
        return jwtUtil.createJwt(userId, role, seconds);
    }

    public String createRefreshToken(Long userId) {
        return createRefreshToken(userId, refreshExpirationSecond);
    }

    public String createRefreshToken(Long userId, Long seconds) {
        String refreshToken = UUID.randomUUID().toString();
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisService.save(key, String.valueOf(userId), seconds);
        return refreshToken;
    }

    public Optional<String> validateRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        String userId = redisService.get(key);
        return Optional.ofNullable(userId);
    }

    public String reissueAccessToken(String refreshToken) {
        String userId = validateRefreshToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token is invalid or expired."));

        User user = userService.findById(Long.valueOf(userId));

        return createAccessToken(user.getId(), user.getRole());
    }
}
