package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.event.BinaryContentCreatedEvent;
import com.sprint.mission.discodeit.exception.user.DuplicateUserException;
import com.sprint.mission.discodeit.exception.user.EmailAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

  @Mock
  private UserRepository userRepository;
  @Mock
  private BinaryContentRepository binaryContentRepository;
  @Mock
  private BinaryContentStorage binaryContentStorage;
  @Mock
  private ApplicationEventPublisher eventPublisher;
  @Mock
  private UserMapper userMapper;
  @Mock
  private JwtRegistry jwtRegistry;
  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private BasicUserService userService;

  private UUID userId;
  private User mockUser;
  private UserDto mockUserDto;

  @BeforeEach
  void setUp() {
    userId = UUID.randomUUID();
    mockUser = new User("testUser", "test@test.com", "password123", null);

    mockUserDto = new UserDto(
        userId,
        "tester",
        "test@test.com",
        null,
        true,
        Role.USER
    );
  }

  @Nested
  @DisplayName("createUser()")
  class CreateUser {

    private UserCreateRequest request;

    @BeforeEach
    void setUpRequest() {
      request = new UserCreateRequest("tester", "test@test.com", "password123");
    }

    // --- 성공 ---
    @Test
    @DisplayName("유저 생성 성공 - 프로필 이미지가 없이 유저가 생성됨")
    void createUser_success_withoutProfile() {
      // given
      given(userRepository.findByUsername("tester")).willReturn(Optional.empty());
      given(userRepository.findByEmail("test@test.com")).willReturn(Optional.empty());
      given(userRepository.save(any(User.class))).willReturn(mockUser);
      given(userMapper.toDto(any(User.class), anyBoolean())).willReturn(mockUserDto);

      // when
      UserDto result = userService.createUser(request, null);

      // then
      assertThat(result).isNotNull();
      assertThat(result.username()).isEqualTo("tester");

      then(userRepository).should().save(any(User.class));
      then(binaryContentRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("유저 생성 성공 - 프로필 이미지와 함께 유저가 생성됨")
    void createUser_success_withProfile() {
      MockMultipartFile profileFile = new MockMultipartFile(
          "profile", "profile.png", "image/png", "fake-image-bytes".getBytes()
      );
      BinaryContent savedBinary = new BinaryContent("image.png", "image/png", 15L);
      UUID binaryId = UUID.randomUUID();
      ReflectionTestUtils.setField(savedBinary, "id", binaryId);

      // given
      given(userRepository.findByUsername("tester")).willReturn(Optional.empty());
      given(userRepository.findByEmail("test@test.com")).willReturn(Optional.empty());
      given(binaryContentRepository.save(any(BinaryContent.class))).willReturn(savedBinary);
      given(userRepository.save(any(User.class))).willReturn(mockUser);
      given(userMapper.toDto(any(User.class), anyBoolean())).willReturn(mockUserDto);
      given(passwordEncoder.encode(any())).willReturn("encodedPassword");

      // when
      UserDto result = userService.createUser(request, profileFile);

      // then
      assertThat(result).isNotNull();
      then(binaryContentRepository).should().save(any(BinaryContent.class));
      then(eventPublisher).should().publishEvent(any(BinaryContentCreatedEvent.class));
    }

    // --- 실패 ---
    @Test
    @DisplayName("유저 생성 실패 - 이미 존재하는 유저")
    void createUser_duplicateUsername_duplicateUserException() {
      // given
      given(userRepository.findByUsername("tester")).willReturn(Optional.of(mockUser));

      // when & then
      assertThatThrownBy(() -> userService.createUser(request, null))
          .isInstanceOf(DuplicateUserException.class);

      then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("유저 생성 실패 - 이미 존재하는 이메일")
    void createUser_duplicateEmail_EmailAlreadyExistsException() {
      // given
      given(userRepository.findByUsername("tester")).willReturn(Optional.empty());
      given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(mockUser));

      // when & then
      assertThatThrownBy(() -> userService.createUser(request, null))
          .isInstanceOf(EmailAlreadyExistsException.class);

      then(userRepository).should(never()).save(any());
    }
  }

  @Nested
  @DisplayName("deleteUser()")
  class DeleteUser {

    // --- 성공 ---
    @Test
    @DisplayName("프로필 이미지가 있는 사용자를 삭제하는 경우 BinaryContent도 함께 삭제됨")
    void deleteUser_withProfile_deletesProfileAndUser() {
      // given
      BinaryContent profile = new BinaryContent("pic.png", "image/png", 20L);
      User userWithProfile = new User("tester", "test@test.com", "pw", profile);

      given(userRepository.findById(userId)).willReturn(Optional.of(userWithProfile));
      given(userRepository.save(any(User.class))).willReturn(userWithProfile);

      // when
      userService.deleteUser(userId);

      // then
      then(binaryContentRepository).should().delete(profile);
      then(userRepository).should().delete(userWithProfile);
    }

    @Test
    @DisplayName("프로필 이미지가 없는 사용자를 삭제하는 경우")
    void deleteUser_withoutProfile_deletesUser() {
      // given
      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(userRepository.save(any(User.class))).willReturn(mockUser);

      // when
      userService.deleteUser(userId);

      // then
      then(binaryContentRepository).should(never()).delete(any());
      then(userRepository).should().delete(mockUser);
    }

    // --- 실패 ---
    @Test
    @DisplayName("존재하지 않는 userId일 경우 UserNotFoundException 발생")
    void deleteUser_userNotFound_throwsUserNotFoundException() {
      // given
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.deleteUser(userId))
          .isInstanceOf(UserNotFoundException.class);

      then(userRepository).should(never()).delete(any(User.class));
    }
  }

  @Nested
  @DisplayName("updateUser()")
  class UpdateUser {

    // --- 성공 ---
    @Test
    @DisplayName("username, email, password를 정상적으로 변경")
    void updateUser_allField_success() {
      // given
      UserUpdateRequest updateRequest = new UserUpdateRequest("newName", "new@test.com",
          "newPassword123");
      UserDto updatedDto = new UserDto(userId, "newName", "new@test.com", null, true, Role.USER);

      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(userRepository.findByUsername("newName")).willReturn(Optional.empty());
      given(userRepository.findByEmail("new@test.com")).willReturn(Optional.empty());
      given(userMapper.toDto(any(User.class), anyBoolean())).willReturn(updatedDto);
      given(jwtRegistry.getActiveUserIds()).willReturn(java.util.Collections.emptySet());

      // when
      UserDto result = userService.updateUser(userId, updateRequest, null);

      // then
      assertThat(result.username()).isEqualTo("newName");
      assertThat(result.email()).isEqualTo("new@test.com");
    }

    @Test
    @DisplayName("프로필 이미지 변경")
    void updateUser_profileImage_success() {
      //given
      BinaryContent existingProfile = new BinaryContent("last.png", "image.png", 10L);
      User userWithProfile = new User("tester", "test@test.com", "password123", existingProfile);

      MockMultipartFile newProfileFile = new MockMultipartFile(
          "profile", "new.png", "image/png", "new-image".getBytes()
      );
      BinaryContent newBinary = new BinaryContent("new.png", "image/png", 9L);
      UUID newBinaryId = UUID.randomUUID();
      ReflectionTestUtils.setField(newBinary, "id", newBinaryId);

      given(userRepository.findById(userId)).willReturn(Optional.of(userWithProfile));
      given(binaryContentRepository.save(any(BinaryContent.class))).willReturn(newBinary);
      given(userMapper.toDto(any(User.class), anyBoolean())).willReturn(mockUserDto);
      given(jwtRegistry.getActiveUserIds()).willReturn(java.util.Collections.emptySet());

      // when
      userService.updateUser(userId, new UserUpdateRequest(null, null, null), newProfileFile);

      // then
      then(binaryContentRepository).should().delete(existingProfile);
      then(binaryContentRepository).should().save(any(BinaryContent.class));
      then(eventPublisher).should().publishEvent(any(BinaryContentCreatedEvent.class));
    }

    // --- 실패 ---
    @Test
    @DisplayName("존재하지 않는 userId일 경우 UserNotFoundException이 발생")
    void updateUser_userNotFound_throwsUserNotFoundException() {
      // given
      given(userRepository.findById(userId)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> userService.updateUser(userId,
          new UserUpdateRequest("name", "ex@ex.com", "password"), null))
          .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("변경하려는 username이 이미 사용중이면 DuplicateUserException이 발생")
    void updateUser_duplicateUsername_throwsDuplicateUserException() {
      // given
      User otherUser = new User("otherName", "other@test.com", "password", null);
      UserUpdateRequest updateRequest = new UserUpdateRequest("otherName", null, null);

      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(userRepository.findByUsername("otherName")).willReturn(Optional.of(otherUser));

      // when & then
      assertThatThrownBy(() -> userService.updateUser(userId, updateRequest, null))
          .isInstanceOf(DuplicateUserException.class);
    }

    @Test
    @DisplayName("변경하려는 email이 이미 사용중이라면 EmailAlreadyExistsException이 발생")
    void updateUser_duplicateEmail_throwsEmailAlreadyExistsException() {
      // given
      User otherUser = new User("otherName", "other@test.com", "password", null);
      UserUpdateRequest updateRequest = new UserUpdateRequest(null, "other@test.com", null);

      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(userRepository.findByEmail("other@test.com")).willReturn(Optional.of(otherUser));

      // when & then
      assertThatThrownBy(() -> userService.updateUser(userId, updateRequest, null))
          .isInstanceOf(EmailAlreadyExistsException.class);
    }
  }
}