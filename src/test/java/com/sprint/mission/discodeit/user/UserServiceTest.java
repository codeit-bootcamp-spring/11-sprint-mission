package com.sprint.mission.discodeit.user;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class) // 빠른 처리를 위해 Spring 컨텍스트를 로딩하지 않음
public class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private UserMapper userMapper;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private BasicUserService userService;

  @Test
  @DisplayName("사용자 생성 성공")
  void create_success() {
    //given
    UserCreateRequest request = new UserCreateRequest("이마크", "mark@test.com", "pass1234");
    UserDto dto = new UserDto(UUID.randomUUID(), "이마크", "mark@test.com", null, true, null);

    given(userRepository.existsByUsernameOrEmail("이마크", "mark@test.com")).willReturn(
        false); // 중복 검사 통과 (false 반환)

    given(passwordEncoder.encode(any(String.class))).willReturn("encodedPassword");

    given(userRepository.save(any(User.class))).will(
        returnsFirstArg()); // save 시 첫 번째 파라미터(들어온 User)를 그대로 반환해라! (실무 패턴)

    given(userMapper.toDto(any(User.class))).willReturn(
        dto); // Mapper는 만들어둔 mockDto를 반환해라! given() 안에서만 any() 사용 가능.

    //when
    UserDto result = userService.create(request, empty());

    //then
    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo("이마크");

    then(userRepository).should().save(any(User.class)); // 검증: User에 save 되었는지 확인
  }

  @Test
  @DisplayName("이름 또는 이메일 중복 시 UserAlreadyExistsException 발생")
  void create_fail_duplicateEmail() {
    //given
    UserCreateRequest request = new UserCreateRequest("이마크", "mark@test.com", "pass1234");
    given(userRepository.existsByUsernameOrEmail(request.username(), request.email())).willReturn(
        true);

    //when&then
    assertThatThrownBy(() -> userService.create(request, empty()))
        .isInstanceOf(
            UserAlreadyExistsException.class); // 해당 메서드를 실행했을 때 UserAlreadyExistsException이 터지는지 검증

    // 예외가 터졌을 떄 아래 save 로직들이 호출되지 않았는지 검증
    then(userRepository).should(never())
        .save(any(User.class)); // userRepository mock 객체의 특정 메서드(save())가 호출되지 않았는지 검증
  }

  @Test
  @DisplayName("사용자 정보 수정 성공")
  void update_success() {
    //given
    UUID userId = UUID.randomUUID(); // UUID는 update했다고 바뀌지 않으므로 미리 정의해도 괜찮음

    UserUpdateRequest request = new UserUpdateRequest("이마크", "mark@test.com", "pass1234");
    User oldUser = new User("이민형", "mh@test.com", "pass1234", null);

    given(userRepository.findById(userId)).willReturn(Optional.of(oldUser)); // 실제 객체 리턴 필수!
    given(userRepository.existsByUsernameAndIdNot(request.newUsername(), userId)).willReturn(
        false);
    given(userRepository.existsByEmailAndIdNot(request.newEmail(), userId)).willReturn(
        false);
    given(passwordEncoder.encode(any(String.class))).willReturn("encodedPassword");

    UserDto updatedDto = new UserDto(userId, "이마크", "mark@test.com", null, true, null);
    given(userMapper.toDto(any(User.class))).willReturn(updatedDto);

    //when
    UserDto result = userService.update(userId, request, empty());

    //then
    assertThat(result).isNotNull();
    assertThat(result.username()).isEqualTo("이마크");
    assertThat(updatedDto.username()).isEqualTo("이마크");
  }

  @Test
  @DisplayName("이미 존재하는 아이디/이메일로 수정할 경우 UserAlreadyExistsException 발생")
  void update_fail_user_already_exists() {
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("이마크", "old@test.com", "pass1234");
    User user = new User("이마크", "old@test.com", "pass1234", null);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(userRepository.existsByUsernameAndIdNot(request.newUsername(), userId)).willReturn(true);

    assertThatThrownBy(() -> userService.update(userId, request, empty()))
        .isInstanceOf(UserAlreadyExistsException.class);
  }

  @Test
  @DisplayName("존재하지 않는 사용자 ID로 수정 시 UserNotFoundException 발생")
  void update_fail_user_not_found() {
    //given
    UUID userId = UUID.randomUUID();
    UserUpdateRequest request = new UserUpdateRequest("이마크", "mark@test.com", "pass1234");

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    //when&then
    assertThatThrownBy(() -> userService.update(userId, request, empty()))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  @DisplayName("사용자 삭제 성공")
  void delete_success() {
    //given
    UUID userId = UUID.randomUUID();
    User user = new User("이마크", "mark@test.com", "pass1234", null);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));

    //when
    userService.delete(userId);

    //then
    then(userRepository).should()
        .delete(user); // userRepository의 delete 메서드가 위에서 찾은 user 객체를 넣고 실행되었는지 검증
  }

  @Test
  @DisplayName("존재하지 않는 사용자 Id로 삭제 시 UserNotFoundException 발생")
  void delete_fail_user_not_found() {
    UUID userId = UUID.randomUUID();

    given(userRepository.findById(userId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> userService.delete(userId))
        .isInstanceOf(UserNotFoundException.class);
  }
}
