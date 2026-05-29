package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.DuplicateEmailException;
import com.sprint.mission.discodeit.exception.DuplicateUsernameException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.dto.user.CreateUserRequest;
import com.sprint.mission.discodeit.service.dto.user.UpdateUserRequest;
import com.sprint.mission.discodeit.service.dto.user.UserDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private BinaryContentStorage binaryContentStorage;

    @InjectMocks private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password1234")
                .profile(null)
                .build();

        userDto = UserDto.builder()
                .id(user.getId())
                .username("testuser")
                .email("test@example.com")
                .online(false)
                .build();
    }

    @Test
    void 유저_생성시_정상적으로_repository의_save를_호출한다() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password1234")
                .build();

        given(userRepository.existsByUsername("testuser")).willReturn(false);
        given(userRepository.existsByEmail("test@example.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(user);
        given(userMapper.toDto(user)).willReturn(userDto);

        UserDto result = userService.create(request);

        assertThat(result).isEqualTo(userDto);
        then(userRepository).should().save(any(User.class));
    }

    @Test
    void 이미_존재하는_username으로_유저_생성시_DuplicateUsernameException이_발생한다() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password1234")
                .build();

        given(userRepository.existsByUsername("testuser")).willReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateUsernameException.class);
    }

    @Test
    void 이미_존재하는_email로_유저_생성시_DuplicateEmailException이_발생한다() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password1234")
                .build();

        given(userRepository.existsByUsername("testuser")).willReturn(false);
        given(userRepository.existsByEmail("test@example.com")).willReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void 유저_정보_수정시_변경된_정보가_반환된다() {
        UUID userId = user.getId();
        UpdateUserRequest request = UpdateUserRequest.builder()
                .userId(userId)
                .username("updateduser")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.findByUsername("updateduser")).willReturn(Optional.empty());
        given(userMapper.toDto(user)).willReturn(userDto);

        UserDto result = userService.update(request);

        assertThat(result).isEqualTo(userDto);
    }

    @Test
    void 존재하지_않는_유저_수정시_UserNotFoundException이_발생한다() {
        UUID userId = UUID.randomUUID();
        UpdateUserRequest request = UpdateUserRequest.builder()
                .userId(userId)
                .username("updateduser")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void 유저_삭제시_repository의_delete를_호출한다() {
        UUID userId = user.getId();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        userService.delete(userId);

        then(userRepository).should().delete(user);
    }

    @Test
    void 존재하지_않는_유저_삭제시_UserNotFoundException이_발생한다() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void 유저_단건_조회시_해당_유저_정보가_반환된다() {
        UUID userId = user.getId();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userMapper.toDto(user)).willReturn(userDto);

        UserDto result = userService.find(userId);

        assertThat(result).isEqualTo(userDto);
    }

    @Test
    void 존재하지_않는_유저_조회시_UserNotFoundException이_발생한다() {
        UUID userId = UUID.randomUUID();
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.find(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void 전체_유저_조회시_모든_유저_목록이_반환된다() {
        given(userRepository.findAll()).willReturn(List.of(user));
        given(userMapper.toDto(user)).willReturn(userDto);

        List<UserDto> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(userDto);
    }

    @Test
    void null_요청으로_유저_생성시_DiscodeitException이_발생한다() {
        assertThatThrownBy(() -> userService.create(null))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void 빈_username으로_유저_생성시_DiscodeitException이_발생한다() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("")
                .email("test@example.com")
                .password("password1234")
                .build();

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void 빈_email로_유저_생성시_DiscodeitException이_발생한다() {
        CreateUserRequest request = CreateUserRequest.builder()
                .username("testuser")
                .email("")
                .password("password1234")
                .build();

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void 이미_존재하는_username으로_유저_수정시_DuplicateUsernameException이_발생한다() {
        UUID userId = user.getId();
        UpdateUserRequest request = UpdateUserRequest.builder()
                .userId(userId)
                .username("duplicate")
                .build();
        User other = User.builder()
                .username("duplicate")
                .email("other@example.com")
                .password("pass")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.findByUsername("duplicate")).willReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.update(request))
                .isInstanceOf(DuplicateUsernameException.class);
    }

    @Test
    void null_요청으로_유저_수정시_DiscodeitException이_발생한다() {
        assertThatThrownBy(() -> userService.update(null))
                .isInstanceOf(DiscodeitException.class);
    }
}
