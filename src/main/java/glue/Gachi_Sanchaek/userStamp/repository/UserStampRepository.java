package glue.Gachi_Sanchaek.userStamp.repository;

import glue.Gachi_Sanchaek.userStamp.entity.UserStamp;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStampRepository extends JpaRepository<UserStamp, Long> {
    List<UserStamp> findByUserId(Long userId);
}
