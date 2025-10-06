package glue.Gachi_Sanchaek.user.service;

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

    @Transactional(readOnly = true)
    public UserResponseDto findById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 사용자가 존재하지 않습니다. id=" + id));
        return new UserResponseDto(user);
    }

    @Transactional
    public void delete(Long id){
        userRepository.deleteById(id);
    }
}
