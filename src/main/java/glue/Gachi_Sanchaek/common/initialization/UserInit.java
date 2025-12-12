package glue.Gachi_Sanchaek.common.initialization;

import glue.Gachi_Sanchaek.domain.login.dto.UserJoinDto;
import glue.Gachi_Sanchaek.domain.pointLog.enums.WalkType;
import glue.Gachi_Sanchaek.domain.user.entity.User;
import glue.Gachi_Sanchaek.domain.user.repository.UserRepository;
import glue.Gachi_Sanchaek.domain.user.service.UserService;
import glue.Gachi_Sanchaek.domain.walk.dto.WalkEndRequest;
import glue.Gachi_Sanchaek.domain.walk.dto.WalkResponse;
import glue.Gachi_Sanchaek.domain.walk.dto.WalkStartRequest;
import glue.Gachi_Sanchaek.domain.walk.service.WalkRecordService;
import glue.Gachi_Sanchaek.domain.walk.service.WalkService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInit {
    private final UserRepository userRepository;
    private final WalkService walkService;
    private final UserService userService;

    public void init(){

        for(int i=0;i<100000;i++){
            User user = userRepository.save(new User(new UserJoinDto((long) i, "user" + i)));
            WalkStartRequest walkStartRequest = new WalkStartRequest(null, WalkType.NORMAL);
            WalkResponse walkResponse = walkService.startWalk(walkStartRequest, user.getId());
            WalkEndRequest walkEndRequest
                    = new WalkEndRequest(walkResponse.getWalkId(), Math.random()*1000, (int) (Math.random()*1000));
            walkService.endWalk(user.getId(),walkEndRequest);
            if(i%1000==0){
                System.out.println("user"+i+" complete");
            }
        }



    }


}
