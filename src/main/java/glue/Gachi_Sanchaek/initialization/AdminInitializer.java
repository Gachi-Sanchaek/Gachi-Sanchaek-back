package glue.Gachi_Sanchaek.initialization;

import glue.Gachi_Sanchaek.login.dto.UserJoinDto;
import glue.Gachi_Sanchaek.login.service.TokenService;
import glue.Gachi_Sanchaek.pointLog.enums.WalkType;
import glue.Gachi_Sanchaek.pointLog.service.PointLogService;
import glue.Gachi_Sanchaek.ranking.service.RankingService;
import glue.Gachi_Sanchaek.security.jwt.JWTUtil;
import glue.Gachi_Sanchaek.user.dto.UserJoinRequestDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer {
    private final UserService userService;
    private final JWTUtil jWTUtil;
    private final TokenService tokenService;

    private final PointLogService pointLogService;
    private final RankingService rankingService;


    public void init(){
        User user = userService.registerInitialUser(new UserJoinDto(123L, "즈에"));
        userService.completeRegistration(user.getId(),new UserJoinRequestDto("즈에","FEMALE"));

        String accessToken = jWTUtil.createJwt(user.getId(), "ADMIN", 86400 * 7L);
        String refreshToken = tokenService.createRefreshToken(user.getId());
        log.info("AccessToken Created: {}", accessToken);
        log.info("RefreshToken created: {}", refreshToken);

        //즈에 포인트 로그 생성
        for(int i=0;i<10;i++){
            userService.recordWalkingResult(user.getId(), 500L);
            pointLogService.save(user, 500L, WalkType.DOG, "부천 유기견 센터");
            rankingService.updateRanking(user.getId(), 500L);
        }

        //랭킹용 더미 유저 생성
        createDummyUserRanking("기뮤딘", 9000L);
        createDummyUserRanking("시윤팍", 8000L);
        createDummyUserRanking("치삼이", 7000L);
        createDummyUserRanking("큰뚱이", 6500L);
        createDummyUserRanking("작은뚱이", 5500L);
        createDummyUserRanking("저우승좀", 5000L);


    }

    private void createDummyUserRanking(String userName, Long rankPoint){
        User user = userService.registerInitialUser(new UserJoinDto(123L, userName));
        userService.completeRegistration(user.getId(),new UserJoinRequestDto(userName,"FEMALE"));
        rankingService.updateRanking(user.getId(), rankPoint);
    }
}
