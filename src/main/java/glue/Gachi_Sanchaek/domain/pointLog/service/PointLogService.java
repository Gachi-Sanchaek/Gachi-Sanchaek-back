package glue.Gachi_Sanchaek.domain.pointLog.service;

import glue.Gachi_Sanchaek.domain.pointLog.dto.PointLogResponseDto;
import glue.Gachi_Sanchaek.domain.pointLog.entity.PointLog;
import glue.Gachi_Sanchaek.domain.pointLog.enums.WalkType;
import glue.Gachi_Sanchaek.domain.pointLog.repository.PointLogRepository;
import glue.Gachi_Sanchaek.domain.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointLogService {
    private final PointLogRepository pointLogRepository;

    @Transactional
    public PointLog save(User user, Long reward, WalkType type, String location){
        return pointLogRepository.save(new PointLog(user, reward, type, location));
    }

    @Transactional(readOnly = true)
    public List<PointLogResponseDto> findByUserId(Long userId){
        return pointLogRepository.findAllByUserId(userId);
    }

}
