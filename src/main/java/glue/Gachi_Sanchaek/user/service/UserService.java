package glue.Gachi_Sanchaek.user.service;

import glue.Gachi_Sanchaek.exception.UserNotFoundException;
import glue.Gachi_Sanchaek.login.dto.UserJoinDto;
import glue.Gachi_Sanchaek.user.dto.UserJoinRequestDto;
import glue.Gachi_Sanchaek.user.dto.UserUpdateRequestDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;

    public User findById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not Found. userId = " + userId));
    }

    public Optional<User> findByKakaoId(Long kakaoId){
        return userRepository.findByKakaoId(kakaoId);
    }

    @Transactional
    public void delete(Long userId){
        User user = findById(userId);
        userRepository.delete(user);
    }

    public boolean isAvailableNickname(String nickname){
        return !userRepository.existsByNickname(nickname);
    }


    @Transactional
    public User registerInitialUser(UserJoinDto joinDto) {
        User user = new User(joinDto);
        return userRepository.save(user);
    }

    @Transactional
    public User completeRegistration(Long userId, UserJoinRequestDto userJoinRequestDto){
        User user = findById(userId);
        user.applyJoinInfo(userJoinRequestDto.getNickname(), userJoinRequestDto.getGender());
        return user;
    }

    @Transactional
    public User update(Long userId, UserUpdateRequestDto requestDto) {
        User user = findById(userId);
        user.updateProfile(requestDto.getNickname(), requestDto.getProfileImageUrl());
        return user;
    }

    @Transactional
    public User recordWalkingResult(Long userId, Long reward){
        User user = findById(userId);
        user.addTotalPoints(reward);
        user.incrementWalkingCount();
        return user;
    }
}
