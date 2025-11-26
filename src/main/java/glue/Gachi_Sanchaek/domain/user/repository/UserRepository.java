package glue.Gachi_Sanchaek.domain.user.repository;

import glue.Gachi_Sanchaek.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoId(Long kakaoId);
    Optional<User> findByIdAndDeletedFalse(Long id);
    boolean existsByNickname(String nickname);
}