package glue.Gachi_Sanchaek.user.service;

import glue.Gachi_Sanchaek.user.dto.UserJoinRequestDto;
import glue.Gachi_Sanchaek.user.dto.UserResponseDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not Found. id = " + id));
        return user;
    }

    @Transactional
    public void delete(Long id){
        userRepository.deleteById(id);
    }

    public boolean isAvailableNickname(String nickname){
        return userRepository.existsByNickname(nickname);
    }

    public User update(Long userId, UserJoinRequestDto userJoinRequestDto){
        User user = findById(userId);
        user.setNickname(userJoinRequestDto.getNickname());
        user.setGender(userJoinRequestDto.getGender());
        return userRepository.save(user);
    }
}
