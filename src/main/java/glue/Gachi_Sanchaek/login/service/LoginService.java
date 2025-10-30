package glue.Gachi_Sanchaek.login.service;


import glue.Gachi_Sanchaek.login.dto.KakaoUserInfoResponseDto;
import glue.Gachi_Sanchaek.login.dto.LoginResponseDto;
import glue.Gachi_Sanchaek.login.dto.UserJoinDto;
import glue.Gachi_Sanchaek.security.jwt.JWTUtil;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import glue.Gachi_Sanchaek.user.service.UserService;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserService userService;

    @Transactional
    public LoginResponseDto kakaoLogin(KakaoUserInfoResponseDto userInfo) {
        Optional<User> userOpt = userService.findByKakaoId(userInfo.getId());

        if (userOpt.isEmpty()) {
            User newUser = userService.registerInitialUser(new UserJoinDto(userInfo));
            return new LoginResponseDto(true, newUser);
        }

        return new LoginResponseDto(false, userOpt.get());
    }

}
