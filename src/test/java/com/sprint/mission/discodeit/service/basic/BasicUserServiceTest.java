package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.DuplicateEmailException;
import com.sprint.mission.discodeit.exception.user.DuplicateUsernameException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    UserStatusRepository userStatusRepository;
    @Mock
    UserMapper userMapper;
    @Mock
    BinaryContentRepository binaryContentRepository;
    @Mock
    BinaryContentStorage binaryContentStorage;
    @InjectMocks
    BasicUserService userService;

    @Nested
    @DisplayName("create() 메서드는")
    class Describe_create {

        UserCreateRequest userCreateRequest = new UserCreateRequest(
                "testUser", "test@email.com", "password123");
        UserDto expectedDto = new UserDto(
                UUID.randomUUID(), "testUser", "test@email.com", null, true);

        @Nested
        @DisplayName("이미 존재하는 이메일이 주어지면")
        class Context_with_duplicate_email {
            @Test
            @DisplayName("DuplicateEmailException 예외를 던진다.")
            void it_throws_DuplicateEmailException() {
                // given
                given(userRepository.existsByEmail(userCreateRequest.email())).willReturn(true);

                // when & then
                assertThatThrownBy(() -> userService.create(userCreateRequest, Optional.empty()))
                        .isInstanceOf(DuplicateEmailException.class);
            }
        }

        @Nested
        @DisplayName("이미 존재하는 유저네임이 주어지면")
        class Context_with_duplicate_username {
            @Test
            @DisplayName("DuplicateUsernameException 예외를 던진다.")
            void it_throws_DuplicateUsernameException() {
                // given
                given(userRepository.existsByEmail(userCreateRequest.email())).willReturn(false);
                given(userRepository.existsByUsername(userCreateRequest.username())).willReturn(true);

                // when & then
                assertThatThrownBy(() -> userService.create(userCreateRequest, Optional.empty()))
                        .isInstanceOf(DuplicateUsernameException.class);
            }
        }

        @Nested
        @DisplayName("프로필 이미지가 없는 유효한 요청이 주어지면")
        class Context_with_valid_request_no_profile {
            @Test
            @DisplayName("프로필 관련 로직을 건너뛰고 유저를 성공적으로 생성한다.")
            void it_creates_user_without_profile() {
                // given
                given(userRepository.existsByEmail(userCreateRequest.email())).willReturn(false);
                given(userRepository.existsByUsername(userCreateRequest.username())).willReturn(false);
                given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

                // when
                UserDto result = userService.create(userCreateRequest, Optional.empty());

                // then
                assertThat(result).isNotNull();

                // 프로필 이미지가 없으므로 프로필 관련 로직을 건너뛰는지 검증
                verify(binaryContentRepository, never()).save(any());
                verify(binaryContentStorage, never()).put(any(), any());

                // userRepository.save()가 딱 한 번 호출됨을 검증
                verify(userRepository, times(1)).save(any(User.class));
            }
        }

        @Nested
        @DisplayName("프로필 이미지가 포함된 유효한 요청이 주어지면")
        class Context_with_valid_request_with_profile {
            @Test
            @DisplayName("이미지를 스토리지에 저장하고 유저를 성공적으로 생성한다.")
            void it_creates_user_with_profile() {
                // given
                byte[] mockBytes = new byte[] {1, 2, 3};
                BinaryContentCreateRequest profileRequest =
                        new BinaryContentCreateRequest("profile.png", "image/png", mockBytes);

                given(userRepository.existsByEmail(userCreateRequest.email())).willReturn(false);
                given(userRepository.existsByUsername(userCreateRequest.username())).willReturn(false);

                UUID generatedId = UUID.randomUUID();
                willAnswer(invocation -> {
                    BinaryContent entity = invocation.getArgument(0);
                    ReflectionTestUtils.setField(entity, "id", generatedId);
                    return entity;
                }).given(binaryContentRepository).save(any(BinaryContent.class));

                given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

                // when
                UserDto result = userService.create(userCreateRequest, Optional.of(profileRequest));

                // then
                assertThat(result).isNotNull();

                // binaryContentStorage의 저장이 호출되는지 검증
                verify(binaryContentStorage).put(eq(generatedId), eq(mockBytes));

                // 유저를 한 번만 저장하는지 검증
                verify(userRepository, times(1)).save(any(User.class));
            }
        }
    }

    @Captor
    private ArgumentCaptor<User> userCaptor; // 엔티티 상태를 가로채기 위한 캡처 객체

    @Nested
    @DisplayName("update() 메서드는")
    class Describe_update {

        UUID userId = UUID.randomUUID();
        User existingUser = new User("oldUser", "old@email.com", "oldPass", null);
        UserDto expectedDto = new UserDto(userId, "newUser", "new@email.com", null, false);

        @Nested
        @DisplayName("존재하지 않는 유저 ID가 주어지면")
        class Context_with_not_found_user {
            @Test
            @DisplayName("UserNotFoundException 예외를 던진다.")
            void it_throws_UserNotFoundException() {
                // given
                UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
                        "newUser", "new@email.com", "newPassword");
                given(userRepository.findById(userId)).willReturn(Optional.empty());

                // when & then
                assertThatThrownBy(() -> userService.update(userId, userUpdateRequest, Optional.empty()))
                        .isInstanceOf(UserNotFoundException.class);
            }
        }

        @Nested
        @DisplayName("기존과 동일한 이메일과 유저네임이 주어지면")
        class Context_with_same_email_and_username {
            @Test
            @DisplayName("DB 중복 검사를 생략하고 정삭적으로 업데이트한다.")
            void it_skips_validation_and_updates() {
                // given
                UserUpdateRequest sameRequest = new UserUpdateRequest("oldUser", "old@email.com", "newPass");

                given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
                given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

                // when
                userService.update(userId, sameRequest, Optional.empty());

                // then
                // 중복 검사를 실행하지 않아야함
                verify(userRepository, never()).existsByEmail(any());
                verify(userRepository, never()).existsByUsername(any());
            }
        }

        @Nested
        @DisplayName("변경하려는 이메일이 이미 DB에 존재하면")
        class Context_with_duplicated_new_email {
            @Test
            @DisplayName("DuplicateEmailException 예외를 던진다.")
            void it_throws_DuplicateEmailException() {
                // given
                UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
                        "oldUser", "duplicate@email.com", "newPass");
                given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
                given(userRepository.existsByEmail(userUpdateRequest.newEmail())).willReturn(true);

                // when & then
                assertThatThrownBy(() -> userService.update(userId, userUpdateRequest, Optional.empty()))
                        .isInstanceOf(DuplicateEmailException.class);
            }
        }

        @Nested
        @DisplayName("유효한 새로운 정보가 주어지면 (더티 체이킹 검증)")
        class Context_with_valid_new_info {
            @Test
            @DisplayName("엔티티의 상태를 변경하고 매퍼에 전달한다.")
            void it_updates_entity_state() {
                // given
                UserUpdateRequest userUpdateRequest = new UserUpdateRequest(
                        "newUser", "new@email.com", "newPass");

                given(userRepository.findById(userId)).willReturn(Optional.of(existingUser));
                given(userRepository.existsByEmail(userUpdateRequest.newEmail())).willReturn(false);
                given(userRepository.existsByUsername(userUpdateRequest.newUsername())).willReturn(false);
                given(userMapper.toDto(any(User.class))).willReturn(expectedDto);

                // when
                userService.update(userId, userUpdateRequest, Optional.empty());

                // then
                // - save가 없으므로 Mapper로 넘어갈 때의 User 상태를 **캡처**
                verify(userMapper).toDto(userCaptor.capture());
                User updatedUser = userCaptor.getValue();

                // 캡처한 유저 객체 내부에 값이 의도한대로 잘 바뀌었는지 검증
                assertThat(updatedUser.getUsername()).isEqualTo(userUpdateRequest.newUsername());
                assertThat(updatedUser.getEmail()).isEqualTo(userUpdateRequest.newEmail());
                assertThat(updatedUser.getPassword()).isEqualTo(userUpdateRequest.newPassword());
            }
        }
    }
}

// Mockito의 기본 동작 원리
// - given이나 when으로 특정 동작을 Stubbing하지 않는다면?
// - 리턴 타입에 맞는 기본값을 조용히 반환함
//      - boolean: false
//      - 숫자 타입: 0
//      - 객체 타입: null
//      - 컬렉션 타입: 비어있는 컬렉션
//      - Optional 타입: Optional.empty()