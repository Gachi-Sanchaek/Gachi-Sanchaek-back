package glue.Gachi_Sanchaek.pointLog.repository;

import glue.Gachi_Sanchaek.pointLog.dto.PointLogResponseDto;
import glue.Gachi_Sanchaek.pointLog.entity.PointLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PointLogRepository extends JpaRepository<PointLog, Long> {
    @Query("SELECT new glue.Gachi_Sanchaek.pointLog.dto.PointLogResponseDto(p) " +
            "FROM PointLog p " +
            "WHERE p.user.id = :userId " +
            "ORDER BY p.createdAt DESC")
    List<PointLogResponseDto> findAllByUserId(Long userId);
}

