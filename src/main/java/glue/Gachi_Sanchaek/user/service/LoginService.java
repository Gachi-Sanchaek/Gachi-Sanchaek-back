package glue.Gachi_Sanchaek.user.service;


import glue.Gachi_Sanchaek.security.jwt.JWTUtil;
import glue.Gachi_Sanchaek.user.dto.UserJoinDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
//    private final RedisTemplate<String,String> redisTemplate;

    private final Long ACCESS_TOKEN_EXPIRATION_MS = 24 * 60 * 60 * 1000L;
    private final Long REFRESH_TOKEN_EXPIRATION_MS = 30 * 24 * 60 * 60 * 1000L;

    private final String REFRESH_TOKEN_PREFIX = "refresh:";

    public User joinProcess(UserJoinDto joinDTO, String role) {

        Long kakaoId = joinDTO.getKakaoId();
        Optional<User> OptionalUser = userRepository.findByKakaoId(kakaoId);

        if (OptionalUser.isPresent()) {
            return OptionalUser.get();
        }

        User user = new User(joinDTO);
        user.setRole(role);

        return userRepository.save(user);
    }

    public String createToken(Long userId, String role) {
        return createToken(userId, role,  ACCESS_TOKEN_EXPIRATION_MS);
    }

    public String createToken(Long userId, String role, Long second) {
        return jwtUtil.createJwt(userId, role, second);
    }

    public String createRefreshToken(Long userId) {
        return createRefreshToken(userId, REFRESH_TOKEN_EXPIRATION_MS);
    }

    public String createRefreshToken(Long userId, Long second) {
        String refreshToken = UUID.randomUUID().toString();
//        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + refreshToken, String.valueOf(userId), Duration.ofSeconds(second));
        return refreshToken;
    }

//    public Optional<String> validateRefreshToken(String refreshToken) {
//        String userId = redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + refreshToken);
//        return Optional.ofNullable(userId);
//    }

}
