package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserUpdateParam;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.authority.UserRole;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class BasicUserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    BinaryContentRepository binaryContentRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    BinaryContentStorage binaryContentStorage;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    BasicUserService userService;

    @Test
    void create_success() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "evan",
                "evan@test.com",
                "password123",
                null
        );

        User savedUser = new User(
                request.username(),
                request.email(),
                "encoded-password"
        );

        UserDto expectedDto = new UserDto(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                null,
                true,
                UserRole.USER
        );

        given(userRepository.existsByUsername(request.username())).willReturn(false);
        given(userRepository.existsByEmail(request.email())).willReturn(false);
        given(passwordEncoder.encode(request.password())).willReturn("encoded-password");
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(userMapper.toDto(savedUser)).willReturn(expectedDto);

        // when
        UserDto result = userService.create(request);

        // then
        assertThat(result).isEqualTo(expectedDto);

        then(userRepository).should().existsByUsername(request.username());
        then(userRepository).should().existsByEmail(request.email());
        then(passwordEncoder).should().encode(request.password());
        then(userRepository).should().save(any(User.class));
        then(userMapper).should().toDto(savedUser);
    }

    @Test
    void create_fail_whenUsernameAlreadyExists() {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "evan",
                "evan@test.com",
                "password123",
                null
        );

        given(userRepository.existsByUsername(request.username())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        then(userRepository).should().existsByUsername(request.username());
        then(userRepository).should(never()).save(any(User.class));
        verifyNoInteractions(userMapper);
    }

    @Test
    void update_success() {
        // given
        User user = new User("evan", "evan@test.com", "old-password");

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "newEvan",
                "new@test.com",
                "new-password",
                null
        );

        UserUpdateParam param = new UserUpdateParam(user.getId(), updateRequest);

        UserDto expectedDto = new UserDto(
                user.getId(),
                "newEvan",
                "new@test.com",
                null,
                false,
                UserRole.USER
        );

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userRepository.findByUsername("newEvan")).willReturn(Optional.empty());
        given(userRepository.findByEmail("new@test.com")).willReturn(Optional.empty());
        given(userMapper.toDto(user)).willReturn(expectedDto);

        // when
        UserDto result = userService.update(param);

        // then
        assertThat(result).isEqualTo(expectedDto);
        assertThat(user.getUsername()).isEqualTo("newEvan");
        assertThat(user.getEmail()).isEqualTo("new@test.com");
        assertThat(user.getPassword()).isEqualTo("new-password");

        then(userRepository).should().findById(user.getId());
        then(userRepository).should().findByUsername("newEvan");
        then(userRepository).should().findByEmail("new@test.com");
        then(userMapper).should().toDto(user);
    }

    @Test
    void update_fail_whenUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "newEvan",
                "new@test.com",
                "new-password",
                null
        );

        UserUpdateParam param = new UserUpdateParam(userId, updateRequest);

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.update(param))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepository).should().findById(userId);
        verifyNoInteractions(userMapper);
    }

    @Test
    void delete_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        // when
        userService.delete(user.getId());

        // then
        then(userRepository).should().findById(user.getId());
        then(userRepository).should().deleteById(user.getId());
    }

    @Test
    void delete_fail_whenUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepository).should().findById(userId);
        then(userRepository).should(never()).deleteById(any(UUID.class));
    }
}
