package com.sprint.mission.discodeit.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.User.Role;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

  @Mock
  private final ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);

  @Spy
  PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Mock
  private UserRepository userRepository;

  @Mock
  private BinaryContentRepository binaryContentRepository;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private BasicUserService userService;

  @Test
  @DisplayName("유저 생성 성공(프로필 이미지 있음)")
  void create_success_with_profile() throws IOException {
    // given
    // BCrypt 암호화를 위한 평문 비밀번호
    String password = "12345678";
    // 유저 DTO
    UserCreateRequest request = new UserCreateRequest(
        "테스트", "test@naver.com", passwordEncoder.encode(password));

    // 프로필
    MultipartFile profile = mock(MultipartFile.class);
    given(profile.getOriginalFilename()).willReturn("test.png");
    given(profile.getSize()).willReturn(100L);
    given(profile.getContentType()).willReturn("image/png");
    given(profile.getBytes()).willReturn("data".getBytes());
    given(profile.isEmpty()).willReturn(false); // 프로필 이미지 존재하는 유저 생성하도록

    // 현재 이름이 "테스트"와 "test@naver.com"은 없음
    given(userRepository.existsByUsername("테스트")).willReturn(false);
    given(userRepository.existsByEmail("test@naver.com")).willReturn(false);

    // 각 Repository에 저장 후 willAnswer로 결과를 동적으로 가져옴
    given(userRepository.save(any(User.class))).willAnswer(i -> i.getArgument(0));
    given(binaryContentRepository.save(any(BinaryContent.class))).willAnswer(i -> i.getArgument(0));

    given(userMapper.toDto(any(User.class)))
        .willAnswer(i -> {
          User u = i.getArgument(0);

          return new UserDto(
              u.getId(),
              u.getUsername(),
              u.getEmail(),
              null,
              true,
              Role.USER
          );
        });

    // when
    UserDto result = userService.create(request, profile);

    // then
    assertThat(result).isNotNull();
    then(userRepository).should().save(any(User.class));
    then(binaryContentRepository).should().save(any());
    then(eventPublisher).should().publishEvent(any(BinaryContentCreatedEvent.class));
  }

  @Test
  @DisplayName("유저 생성 성공(프로필 이미지 없음)")
  void create_success_no_profile() {
    // given
    // BCrypt 암호화를 위한 평문 비밀번호
    String password = "12345678";

    // 유저 DTO
    UserCreateRequest request = new UserCreateRequest(
        "테스트", "test@naver.com", passwordEncoder.encode(password));

    given(userRepository.existsByUsername("테스트")).willReturn(false);
    given(userRepository.existsByEmail("test@naver.com")).willReturn(false);

    given(userRepository.save(any(User.class))).willAnswer(i -> i.getArgument(0));

    given(userMapper.toDto(any(User.class)))
        .willAnswer(i -> {
          User u = i.getArgument(0);

          return new UserDto(
              u.getId(),
              u.getUsername(),
              u.getEmail(),
              null,
              true,
              Role.USER
          );
        });

    // when
    UserDto result = userService.create(request, null);

    // then
    assertThat(result).isNotNull();
    then(userRepository).should().save(any(User.class));
    then(binaryContentRepository).should((never())).save(any());
  }

  @Test
  @DisplayName("유저 생성 실패(이름 중복)")
  void create_fail_duplicate_username() {
    // given
    UserCreateRequest request = new UserCreateRequest("테스트", "test@naver.com", "12345678");

    // 이미 "테스트"라는 이름이 있음 -> 실패 유도
    given(userRepository.existsByUsername("테스트")).willReturn(true);

    // when & then
    // 예외 시 비즈니스 예외가 나와야함(UsernameAlreadyExistsException)
    assertThatThrownBy(
        () -> userService.create(request, null)).isInstanceOf(DiscodeitException.class);

    // User 저장되지 않음
    then(userRepository).should(never()).save(any(User.class));
  }

  @Test
  @DisplayName("유저 생성 실패(이메일 중복)")
  void create_fail_duplicate_email() {
    // given
    UserCreateRequest request = new UserCreateRequest("테스트", "test@naver.com", "12345678");

    // 이미 "test@naver.com"이 있음 -> 실패 유도
    given(userRepository.existsByEmail("test@naver.com")).willReturn(true);

    // when & then
    // 예외 시 비즈니스 예외가 나와야함(UserEmailAlreadyExistsException)
    assertThatThrownBy(
        () -> userService.create(request, null)).isInstanceOf(DiscodeitException.class);

    // User 저장되지 않음
    then(userRepository).should(never()).save(any(User.class));
  }

  @Test
  @DisplayName("유저 수정 성공(Username만 수정)")
  void update_username_success() {
    // given
    // 유저 생성
    User user = User.create("이름", "test@naver.com", "12345678");

    // 수정 요청
    UserUpdateRequest request = new UserUpdateRequest("새로운 이름", null, null);

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    given(userMapper.toDto(any(User.class)))
        .willAnswer(i -> {
          User u = i.getArgument(0);

          return new UserDto(
              u.getId(),
              u.getUsername(),
              u.getEmail(),
              null,
              true,
              Role.USER
          );
        });

    // when
    UserDto result = userService.update(user.getId(), request, null);

    // then
    assertEquals("새로운 이름", result.username());
//    assertThat(result.getUsername()).isEqualTo("새로운 이름");

    // userRepository를 대상으로 save 메서드가 User의 아무 필드나 받아서 호출됐는지 확인
    then(userRepository).should().save(any(User.class));
  }

  @Test
  @DisplayName("유저 수정 성공(Email만 수정)")
  void update_email_success() {
    // given
    // 유저 생성
    User user = User.create("이름", "test@naver.com", "12345678");

    // 수정 요청
    UserUpdateRequest request = new UserUpdateRequest(null, "testA@naver.com", null);

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    given(userMapper.toDto(any(User.class)))
        .willAnswer(i -> {
          User u = i.getArgument(0);

          return new UserDto(
              u.getId(),
              u.getUsername(),
              u.getEmail(),
              null,
              true,
              Role.USER
          );
        });

    // when
    UserDto result = userService.update(user.getId(), request, null);

    // then
    assertEquals("testA@naver.com", result.email());

    // userRepository를 대상으로 save 메서드가 User의 아무 필드나 받아서 호출됐는지 확인
    then(userRepository).should().save(any(User.class));
  }

  @Test
  @DisplayName("유저 수정 성공(Password만 수정)")
  void update_password_success() {
    // given
    // BCrypt 암호화를 위한 평문 비밀번호
    String password = "12345678";
    // 유저 생성
    User user = User.create("이름", "test@naver.com", passwordEncoder.encode(password));

    // 수정 요청
    UserUpdateRequest request = new UserUpdateRequest(null, "null", "1234");

    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when
    UserDto result = userService.update(user.getId(), request, null);

    // then
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    then(userRepository).should().save(captor.capture());
    User savedUser = captor.getValue();
    assertTrue(passwordEncoder.matches("1234", savedUser.getPassword()));

    // userRepository를 대상으로 save 메서드가 User의 아무 필드나 받아서 호출됐는지 확인
    then(userRepository).should().save(any(User.class));
  }

  @Test
  @DisplayName("유저 수정 실패(이름 중복)")
  void update_fail_duplicate_username() {
    // given
    // 유저 생성
    User user = User.create("이름", "test@naver.com", "12345678");

    // 수정 요청
    UserUpdateRequest request = new UserUpdateRequest("새로운 이름", null, null);

    // 유저 조회 시 가짜 객체 존재함을 알림
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(userRepository.existsByUsernameAndIdNot("새로운 이름", user.getId())).willReturn(true);

    // when & then
    assertThatThrownBy(() ->
        userService.update(user.getId(), request, null)).isInstanceOf(DiscodeitException.class);

    then(userRepository).should(never()).save(any(User.class));
  }

  @Test
  @DisplayName("유저 수정 실패(이메일 중복)")
  void update_fail_duplicate_email() {
    // given
    // 유저 생성
    User user = User.create("이름", "test@naver.com", "12345678");

    // 수정 요청
    UserUpdateRequest request = new UserUpdateRequest(null, "test@naver.com", null);

    // 유저 조회 시 가짜 객체 존재함을 알림
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
    given(userRepository.existsByEmailAndIdNot("test@naver.com", user.getId())).willReturn(true);

    // when & then
    assertThatThrownBy(() ->
        userService.update(user.getId(), request, null)).isInstanceOf(DiscodeitException.class);

    then(userRepository).should(never()).save(any(User.class));
  }

  @Test
  @DisplayName("유저 수정 실패(유저가 존재하지 않음)")
  void update_fail_notfound_user() {
    // given
    UUID userId = UUID.randomUUID();

    // userId로 조회했을때 빈 값이 나왔을 때
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // 수정 요청
    UserUpdateRequest request = new UserUpdateRequest("새로운 이름", null, null);

    // when & then
    assertThatThrownBy(() ->
        userService.update(userId, request, null)).isInstanceOf(DiscodeitException.class);

    then(userRepository).should(never()).save(any(User.class));
  }

  @Test
  @DisplayName("유저 삭제 성공")
  void delete_success_user() {
    // given
    User user = User.create("삭제될 유저", "test@naver.com", "12345678");
    given(userRepository.findById(user.getId())).willReturn(Optional.of(user));

    // when
    userService.delete(user.getId());

    // then
    then(userRepository).should().delete(user);
  }

  @Test
  @DisplayName("유저 삭제 실패(유저가 존재하지 않음)")
  void delete_fail_notfound_user() {
    // given
    UUID userId = UUID.randomUUID();
    User user = User.create("삭제될 유저", "test@naver.com", "12345678");

    // userId로 조회했을때 빈 값이 나왔을 때
    given(userRepository.findById(userId)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> userService.delete(userId)).isInstanceOf(DiscodeitException.class);
    then(userRepository).should(never()).delete(user);
  }
}
