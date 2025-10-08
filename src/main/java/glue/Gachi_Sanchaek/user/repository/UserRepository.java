package glue.Gachi_Sanchaek.user.repository;

import glue.Gachi_Sanchaek.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKakaoId(Long kakaoId);

    boolean existsByNickname(String nickname);
}