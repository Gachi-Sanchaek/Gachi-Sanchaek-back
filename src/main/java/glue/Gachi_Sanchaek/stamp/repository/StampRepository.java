package glue.Gachi_Sanchaek.stamp.repository;

import glue.Gachi_Sanchaek.stamp.entity.Stamp;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StampRepository extends JpaRepository<Stamp, Long> {
    List<Stamp> findByUserId(Long userId);
}
