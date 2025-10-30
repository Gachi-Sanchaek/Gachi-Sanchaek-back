package glue.Gachi_Sanchaek.stamp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import glue.Gachi_Sanchaek.exception.StampNotFoundException;
import glue.Gachi_Sanchaek.stamp.dto.StampResponseDto;
import glue.Gachi_Sanchaek.stamp.entity.Stamp;
import glue.Gachi_Sanchaek.stamp.repository.StampRepository;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class StampServiceTest {

    @Mock
    private StampRepository stampRepository;

    @Mock
    private UserService userService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private StampService stampService;

    private User testUser;
    private Stamp stampAffordable;
    private Stamp stampUnaffordable;

    @BeforeEach
    void setUp() {

        // 1. 유저 설정 (총 1000 포인트 보유)
        testUser = mock(User.class);

        // 2. 스탬프 설정
        stampAffordable = Stamp.builder()
                .id(1L)
                .name("저렴한 스탬프")
                .price(500L)
                .imageUrl("url1")
                .createdAt(LocalDateTime.now())
                .build();

        stampUnaffordable = Stamp.builder()
                .id(2L)
                .name("비싼 스탬프")
                .price(2000L)
                .imageUrl("url2")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("getAllStampsWithUserStatus: 유저 포인트에 따라 스탬프 활성화(isActive) 여부를 정확히 반환한다")
    void getAllStampsWithUserStatus_shouldReturnStampsWithCorrectActiveStatus() {
        // Given
        Long userId = 1L;
        List<Stamp> allStamps = List.of(stampAffordable, stampUnaffordable);

        when(testUser.getTotalPoints()).thenReturn(1000L);
        when(userService.findById(userId)).thenReturn(testUser);
        when(stampRepository.findAll()).thenReturn(allStamps);

        // When
        List<StampResponseDto> dtos = stampService.getAllStampsWithUserStatus(userId);

        // Then
        assertThat(dtos).hasSize(2);

        assertThat(dtos.get(0).getId()).isEqualTo(stampAffordable.getId());
        assertThat(dtos.get(0).isActive()).isTrue();

        assertThat(dtos.get(1).getId()).isEqualTo(stampUnaffordable.getId());
        assertThat(dtos.get(1).isActive()).isFalse();

        verify(userService).findById(userId);
        verify(stampRepository).findAll();
    }

    @Test
    @DisplayName("findById: ID로 스탬프 조회에 성공한다")
    void findById_shouldReturnStamp_whenFound() {
        // Given
        Long stampId = 1L;
        when(stampRepository.findById(stampId)).thenReturn(Optional.of(stampAffordable));

        // When
        Stamp foundStamp = stampService.findById(stampId);

        // Then
        assertThat(foundStamp).isEqualTo(stampAffordable);
        verify(stampRepository).findById(stampId);
    }

    @Test
    @DisplayName("findById: ID에 해당하는 스탬프가 없으면 StampNotFoundException을 던진다")
    void findById_shouldThrowException_whenNotFound() {
        // Given
        Long nonExistentId = 99L;
        when(stampRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(StampNotFoundException.class, () -> {
            stampService.findById(nonExistentId);
        });

        verify(stampRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("saveAllStamps: 올바른 SQL로 jdbcTemplate의 batchUpdate를 호출한다")
    void saveAllStamps_shouldCallBatchUpdateWithCorrectSql() {
        // Given
        List<Stamp> stampsToSave = List.of(stampAffordable, stampUnaffordable);

        // SQL 캡쳐를 위한 ArgumentCaptor
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);

        // When
        stampService.saveAllStamps(stampsToSave);

        // Then
        String expectedSql = "INSERT INTO stamps (id, name, image_url, price, created_at) VALUES (?,?,?,?,?)";
        verify(jdbcTemplate).batchUpdate(sqlCaptor.capture(), any(BatchPreparedStatementSetter.class));

        assertThat(sqlCaptor.getValue()).isEqualTo(expectedSql);
    }
}