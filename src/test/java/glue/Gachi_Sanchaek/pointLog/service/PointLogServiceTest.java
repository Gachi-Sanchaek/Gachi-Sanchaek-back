package glue.Gachi_Sanchaek.pointLog.service;

import glue.Gachi_Sanchaek.domain.pointLog.dto.PointLogResponseDto;
import glue.Gachi_Sanchaek.domain.pointLog.entity.PointLog;
import glue.Gachi_Sanchaek.domain.pointLog.enums.WalkType;
import glue.Gachi_Sanchaek.domain.pointLog.repository.PointLogRepository;
import glue.Gachi_Sanchaek.domain.pointLog.service.PointLogService;
import glue.Gachi_Sanchaek.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils; // ReflectionTestUtils 임포트

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointLogServiceTest {

    @Mock
    private PointLogRepository pointLogRepository;

    @InjectMocks
    private PointLogService pointLogService;

    private User createTestUser(Long id) {
        User user = User.builder().build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private PointLog createTestPointLog(Long logId, User user, Long amount, WalkType type) {
        PointLog log = new PointLog(user, amount, type, "부천 유기견 센터");
        ReflectionTestUtils.setField(log, "id", logId);
        ReflectionTestUtils.setField(log, "createdAt", LocalDateTime.now());
        return log;
    }

    @Test
    @DisplayName("포인트 로그 저장 성공")
    void savePointLog_Success() {
        // given
        User user = createTestUser(1L);
        Long reward = 100L;
        WalkType type = WalkType.DOG;
        String location = "서울숲";

        PointLog savedLog = new PointLog(user, reward, type, location);
        ReflectionTestUtils.setField(savedLog, "id", 1L);
        ArgumentCaptor<PointLog> captor = ArgumentCaptor.forClass(PointLog.class);
        when(pointLogRepository.save(captor.capture())).thenReturn(savedLog);


        // when
        PointLog result = pointLogService.save(user, reward, type, location);


        // then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(reward, result.getAmount());

        verify(pointLogRepository, times(1)).save(any(PointLog.class));

        PointLog capturedLog = captor.getValue();
        assertThat(capturedLog.getId()).isNull();
        assertEquals(user, capturedLog.getUser());
        assertEquals(reward, capturedLog.getAmount());
        assertEquals(type, capturedLog.getType());
    }

    @Test
    @DisplayName("사용자 ID로 포인트 로그 DTO 목록 조회 성공")
    void findByUserId_Success() {
        // given
        Long userId = 1L;
        User user = createTestUser(userId);

        PointLog log1 = createTestPointLog(1L, user, 100L, WalkType.DOG);
        PointLog log2 = createTestPointLog(2L, user, 500L, WalkType.DOG);

        List<PointLogResponseDto> expectedDtoList = List.of(
                new PointLogResponseDto(log1),
                new PointLogResponseDto(log2)
        );

        when(pointLogRepository.findAllByUserId(userId)).thenReturn(expectedDtoList);

        // when
        List<PointLogResponseDto> resultList = pointLogService.findByUserId(userId);

        // then
        assertNotNull(resultList);
        assertEquals(2, resultList.size());
        assertEquals(expectedDtoList.get(0).getType(), resultList.get(0).getType());
        assertEquals(expectedDtoList.get(1).getAmount(), resultList.get(1).getAmount());

        verify(pointLogRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    @DisplayName("사용자 ID로 조회 시 결과가 없으면 빈 리스트 반환")
    void findByUserId_ReturnsEmptyList() {
        // given
        Long userId = 99L;

        when(pointLogRepository.findAllByUserId(userId)).thenReturn(Collections.emptyList());

        // when
        List<PointLogResponseDto> resultList = pointLogService.findByUserId(userId);

        // then
        assertNotNull(resultList);
        assertThat(resultList).isEmpty();
        verify(pointLogRepository, times(1)).findAllByUserId(userId);
    }
}