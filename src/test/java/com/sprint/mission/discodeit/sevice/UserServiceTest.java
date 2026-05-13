package com.sprint.mission.discodeit.sevice;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.dto.userdto.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userdto.UserDto;
import com.sprint.mission.discodeit.dto.userdto.request.UserCreateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.service.user.DupEmailException;
import com.sprint.mission.discodeit.exception.service.user.NonExistUserException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.JPABinaryContentRepository;
import com.sprint.mission.discodeit.repository.JPAChannelRepository;
import com.sprint.mission.discodeit.repository.JPAReadStatusRepository;
import com.sprint.mission.discodeit.repository.JPAUserRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock
  JPAUserRepository userRepository;
  @Mock
  JPABinaryContentRepository binaryContentRepository;
  @Mock
  BinaryContentStorage binaryContentStorage;
  @Mock
  JPAReadStatusRepository readStatusRepository;
  @Mock
  JPAChannelRepository channelRepository;
  @Mock
  UserMapper userMapper;

  @InjectMocks
  private BasicUserService userService;


  @Test
  @DisplayName("유저 생성 성공 테스트")
  void createUser() {
    //given
    UserCreateRequest userCreateRequest = new UserCreateRequest(
        "테스트",
        "test1@test.com",
        "testpassword"
    );

    given(userRepository.existsByEmail(userCreateRequest.email())).willReturn(false);
    given(userRepository.existsByUsername(userCreateRequest.username())).willReturn(false);

    given(userRepository.save(any(User.class)))
        .willAnswer(invocation -> {
          return invocation.getArgument(0); // 첫 번째 인자를 꺼냄
        });

    given(userMapper.toDto(any(User.class)))
        .willReturn(new UserDto(
            UUID.randomUUID(),
            userCreateRequest.username(),
            userCreateRequest.email(),
            null,
            true
        ));

    //when

    UserDto result = userService.create(userCreateRequest, null);

    //then

    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo(userCreateRequest.username());
    assertThat(result.email()).isEqualTo(userCreateRequest.email());
    then(userRepository).should().save(any(User.class));
  }

  @Test
  @DisplayName("이메일 중복으로 인한 유저 생성 실패 테스트")
  void createUserFailByDuplicateEmail() {
    //given
    UserCreateRequest userCreateRequest = new UserCreateRequest(
        "테스트",
        "test1@test.com",
        "testpassword"
    );

    given(userRepository.existsByEmail(userCreateRequest.email())).willReturn(true);
    given(userRepository.existsByUsername(userCreateRequest.username())).willReturn(false);

    //when & then

    assertThatThrownBy(() -> userService.create(userCreateRequest, null))
        .isInstanceOf(DupEmailException.class);
  }

  @Test
  void updateUser() {

    UserUpdateRequest userUpdateRequest = new UserUpdateRequest(

        "업데이트테스트",
        "updatetest@test.com",
        "updatetestpassword"

    );

    given(userRepository.findById(any(UUID.class))).willReturn(java.util.Optional.of(new User(
        "이전유저",
        "useruser@test.com",
        "password!!",
        null,
        null

    )));
    given(userRepository.existsByEmail(userUpdateRequest.newEmail())).willReturn(false);
    given(userRepository.existsByUsername(userUpdateRequest.newUsername())).willReturn(false);

    given(userMapper.toDto(any(User.class)))
        .willReturn(new UserDto(
            UUID.randomUUID(),
            userUpdateRequest.newUsername(),
            userUpdateRequest.newEmail(),
            null,
            true
        ));

    //when
    UserDto userDto = userService.updateUser(UUID.randomUUID(), userUpdateRequest, null);

    //then
    assertThat(userDto).isNotNull();
    assertThat(userDto.username()).isEqualTo(userUpdateRequest.newUsername());
    assertThat(userDto.email()).isEqualTo(userUpdateRequest.newEmail());
  }

  @Test
  void updateUserFailByNoneUserId() {
    UserUpdateRequest userUpdateRequest = new UserUpdateRequest(

        "업데이트테스트",
        "updatetest@test.com",
        "updatetestpassword"

    );

    given(userRepository.findById(any(UUID.class))).willReturn(Optional.empty());

    //when & then
    assertThatThrownBy(
        () -> userService.updateUser(UUID.randomUUID(), userUpdateRequest, null)).isInstanceOf(
        NonExistUserException.class);


  }

  @Test
  void delete() {
    //given
    UUID userId = UUID.randomUUID();

    given(userRepository.existsById(userId)).willReturn(true);

    //when & then
    userService.delete(userId);
    then(userRepository).should().deleteById(userId);
  }

  @Test
  void deleteFailByNoneUserId() {

    //given
    UUID userId = UUID.randomUUID();

    given(userRepository.existsById(userId)).willReturn(false);

    //when & then
    assertThatThrownBy(() -> userService.delete(userId)).isInstanceOf(NonExistUserException.class);


  }
}