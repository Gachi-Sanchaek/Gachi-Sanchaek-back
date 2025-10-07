package glue.Gachi_Sanchaek.stamp.service;

import glue.Gachi_Sanchaek.stamp.entity.Stamp;
import glue.Gachi_Sanchaek.stamp.repository.StampRepository;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import glue.Gachi_Sanchaek.user.service.UserService;
import glue.Gachi_Sanchaek.userStamp.entity.UserStamp;
import glue.Gachi_Sanchaek.userStamp.repository.UserStampRepository;
import glue.Gachi_Sanchaek.userStamp.service.UserStampService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StampService {
    private final StampRepository stampRepository;

    private final UserService userService;
    private final UserStampService userStampService;

    public List<Stamp> findAll(){
        return (List<Stamp>) stampRepository.findAll();
    }

    public Stamp findById(Long stampId){
        return stampRepository.findById(stampId)
                .orElseThrow(()->new IllegalArgumentException("Stamp not Found. id = "+stampId));
    }

    @Transactional
    public void buyStamp(Long userId, Long stampId){
        Stamp stamp = findById(stampId);
        User user = userService.findById(userId);

        Long userPoint = user.getTotalPoints();
        Long stampPrice = stamp.getPrice();

        if(userPoint < stampPrice){
            throw new RuntimeException("Not enough points to buy this stamp");
        }

        userPoint-=stampPrice;
        user.setTotalPoints(userPoint);
        userStampService.addStamp(user, stamp);

    }

}
