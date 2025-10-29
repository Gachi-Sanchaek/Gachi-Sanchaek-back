package glue.Gachi_Sanchaek.login.service;


import glue.Gachi_Sanchaek.security.jwt.JWTUtil;
import glue.Gachi_Sanchaek.login.dto.UserJoinDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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

    public User findByKakaoId(Long kakaoId){
        return userRepository.findByKakaoId(kakaoId).orElse(null);
    }

    public User joinProcess(UserJoinDto joinDTO) {
        User user = new User(joinDTO);
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
