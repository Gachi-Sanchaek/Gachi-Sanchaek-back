package glue.Gachi_Sanchaek.pointLog.repository;

import glue.Gachi_Sanchaek.pointLog.entity.PointLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {

    List<PointLog> findAllByUserId(Long userId);
}
