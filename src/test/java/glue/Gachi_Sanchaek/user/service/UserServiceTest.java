package glue.Gachi_Sanchaek.user.service;

import glue.Gachi_Sanchaek.exception.UserNotFoundException;
import glue.Gachi_Sanchaek.user.dto.UserJoinRequestDto;
import glue.Gachi_Sanchaek.user.dto.UserUpdateRequestDto;
import glue.Gachi_Sanchaek.user.entity.User;
import glue.Gachi_Sanchaek.user.repository.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    private User testUser;

    private String bonggong1 = "/bonggong/1_default.png";
    private String bonggong2 = "/bonggong/2_hello.png";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .nickname("기존닉네임")
                .kakaoId(12345L)
                .role("USER")
                .gender("NONE")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("findById 메소드 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("성공: 사용자를 ID로 찾는다")
        void findById_Success() {
            // given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // when
            User foundUser = userService.findById(userId);

            // then
            assertThat(foundUser).isEqualTo(testUser);
            assertThat(foundUser.getNickname()).isEqualTo("기존닉네임");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 조회 시 UserNotFoundException 발생")
        void findById_Fail_UserNotFound() {
            // given
            Long nonExistingUserId = 99L;
            when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(UserNotFoundException.class, () -> {
                userService.findById(nonExistingUserId);
            });
        }
    }

    @Nested
    @DisplayName("delete 메소드 테스트")
    class DeleteTest {

        @Test
        @DisplayName("성공: 사용자 삭제")
        void delete_Success() {
            // given
            Long userId = 1L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // when
            userService.delete(userId);

            // then
            verify(userRepository, times(1)).findById(userId);
            verify(userRepository, times(1)).delete(testUser);
        }

        @Test
        @DisplayName("실패: 삭제할 사용자가 존재하지 않으면 UserNotFoundException 발생")
        void delete_Fail_UserNotFound() {
            // given
            Long nonExistingUserId = 99L;
            when(userRepository.findById(nonExistingUserId)).thenReturn(Optional.empty());

            // when & then
            assertThrows(UserNotFoundException.class, () -> {
                userService.delete(nonExistingUserId);
            });

            verify(userRepository, never()).delete(any(User.class));
        }
    }

    @Nested
    @DisplayName("isAvailableNickname 메소드 테스트")
    class IsAvailableNicknameTest {

        @Test
        @DisplayName("성공: 닉네임이 존재하지 않아 '사용 가능' (true) 반환")
        void isAvailableNickname_True() {
            // given
            String newNickname = "새로운닉네임";
            when(userRepository.existsByNickname(newNickname)).thenReturn(false);

            // when
            boolean isAvailable = userService.isAvailableNickname(newNickname);

            // then
            assertThat(isAvailable).isTrue();
        }

        @Test
        @DisplayName("실패: 닉네임이 이미 존재하여 '사용 불가능' (false) 반환")
        void isAvailableNickname_False() {
            // given
            String existingNickname = "이미있는닉네임";
            when(userRepository.existsByNickname(existingNickname)).thenReturn(true);

            // when
            boolean isAvailable = userService.isAvailableNickname(existingNickname);

            // then
            assertThat(isAvailable).isFalse();
        }
    }

    @Nested
    @DisplayName("join 메소드 테스트")
    class JoinTest {

        @Test
        @DisplayName("성공: 사용자 정보(닉네임, 성별) 추가 등록")
        void join_Success() {
            // given
            Long userId = 1L;
            UserJoinRequestDto requestDto = new UserJoinRequestDto("가입닉네임", "MALE");
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // when
            User joinedUser = userService.completeRegistration(userId, requestDto);

            // then
            assertThat(joinedUser.getNickname()).isEqualTo("가입닉네임");
            assertThat(joinedUser.getGender()).isEqualTo("MALE");
        }
    }

    @Nested
    @DisplayName("update 메소드 테스트 (프로필 수정)")
    class UpdateTest {

        @Test
        @DisplayName("성공: 닉네임과 프로필 이미지 모두 변경")
        void update_Success_All() {
            // given
            Long userId = 1L;
            UserUpdateRequestDto requestDto = new UserUpdateRequestDto("수정된닉네임", bonggong2);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // when
            User updatedUser = userService.update(userId, requestDto);

            // then
            assertThat(updatedUser.getNickname()).isEqualTo("수정된닉네임");
            assertThat(updatedUser.getProfileImageUrl()).isEqualTo(bonggong2);
        }

        @Test
        @DisplayName("성공: 닉네임만 변경 (이미지는 null)")
        void update_Success_NicknameOnly() {
            // given
            Long userId = 1L;
            UserUpdateRequestDto requestDto = new UserUpdateRequestDto("닉네임만수정", null);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            String originalImageUrl = testUser.getProfileImageUrl();

            // when
            User updatedUser = userService.update(userId, requestDto);

            // then
            assertThat(updatedUser.getNickname()).isEqualTo("닉네임만수정");
            assertThat(updatedUser.getProfileImageUrl()).isEqualTo(originalImageUrl); // 기존 이미지 유지
        }

        @Test
        @DisplayName("성공: 프로필 이미지만 변경 (닉네임은 null)")
        void update_Success_ImageOnly() {
            // given
            Long userId = 1L;
            UserUpdateRequestDto requestDto = new UserUpdateRequestDto(null, bonggong2);
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            String originalNickname = testUser.getNickname();

            // when
            User updatedUser = userService.update(userId, requestDto);

            // then
            assertThat(updatedUser.getNickname()).isEqualTo(originalNickname); // 기존 닉네임 유지
            assertThat(updatedUser.getProfileImageUrl()).isEqualTo(bonggong2);
        }

        @Test
        @DisplayName("성공: 닉네임이 비어있는(blank) 문자열이면 변경되지 않음")
        void update_Success_BlankNickname() {
            // given
            Long userId = 1L;
            UserUpdateRequestDto requestDto = new UserUpdateRequestDto(" ", bonggong2); // 닉네임이 " "
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            String originalNickname = testUser.getNickname();

            // when
            User updatedUser = userService.update(userId, requestDto);

            // then
            assertThat(updatedUser.getNickname()).isEqualTo(originalNickname); // 기존 닉네임 유지
            assertThat(updatedUser.getProfileImageUrl()).isEqualTo(bonggong2);
        }
    }

    @Nested
    @DisplayName("recordWalkingResult 메소드 테스트")
    class RecordWalkingResultTest {

        @Test
        @DisplayName("성공: 산책 결과(포인트, 횟수)가 누적됨")
        void recordWalkingResult_Success() {
            // given
            Long userId = 1L;
            Long reward = 100L;
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

            // when
            User userAfterFirstWalk = userService.recordWalkingResult(userId, reward);

            // then
            assertThat(userAfterFirstWalk.getTotalPoints()).isEqualTo(100L);
            assertThat(userAfterFirstWalk.getWalkingCount()).isEqualTo(1L);

            // when (두 번째 산책)
            User userAfterSecondWalk = userService.recordWalkingResult(userId, 50L); // 50 포인트 추가

            // then
            assertThat(userAfterSecondWalk.getTotalPoints()).isEqualTo(150L); // 100 + 50
            assertThat(userAfterSecondWalk.getWalkingCount()).isEqualTo(2L); // 1 + 1
        }
    }
}