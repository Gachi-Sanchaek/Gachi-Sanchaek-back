package glue.Gachi_Sanchaek.userStamp.service;

import glue.Gachi_Sanchaek.stamp.entity.Stamp;
import glue.Gachi_Sanchaek.stamp.repository.StampRepository;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.userStamp.entity.UserStamp;
import glue.Gachi_Sanchaek.userStamp.repository.UserStampRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStampService {
    private final UserStampRepository userStampRepository;

    private final UserService userService;


    public List<UserStamp> findAllByUserId(Long userId) {
        return userStampRepository.findByUserId(userId);
    }

    public void addStamp(User user, Stamp stamp) {
        userStampRepository.save(new UserStamp(user, stamp));
    }

}
