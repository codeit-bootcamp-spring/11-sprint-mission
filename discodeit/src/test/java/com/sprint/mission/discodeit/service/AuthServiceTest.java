package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.dto.user.UserDto;
import com.sprint.mission.discodeit.service.dto.user.UserLoginRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserStatusRepository userStatusRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks private AuthService authService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password1234")
                .build();

        userDto = UserDto.builder()
                .id(user.getId())
                .username("testuser")
                .email("test@example.com")
                .online(true)
                .build();
    }

    @Test
    void login_성공_유저상태_존재() {
        UserLoginRequest request = new UserLoginRequest("testuser", "password1234");
        UserStatus userStatus = new UserStatus(user);

        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));
        given(userStatusRepository.findByUserId(user.getId())).willReturn(Optional.of(userStatus));
        given(userMapper.toDto(user)).willReturn(userDto);

        UserDto result = authService.login(request);

        assertThat(result).isEqualTo(userDto);
    }

    @Test
    void login_성공_유저상태_없으면_신규_생성() {
        UserLoginRequest request = new UserLoginRequest("testuser", "password1234");
        UserStatus newStatus = new UserStatus(user);

        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));
        given(userStatusRepository.findByUserId(user.getId())).willReturn(Optional.empty());
        given(userStatusRepository.save(any(UserStatus.class))).willReturn(newStatus);
        given(userMapper.toDto(user)).willReturn(userDto);

        UserDto result = authService.login(request);

        assertThat(result).isEqualTo(userDto);
    }

    @Test
    void login_사용자_없음_예외() {
        UserLoginRequest request = new UserLoginRequest("unknown", "password1234");
        given(userRepository.findByUsername("unknown")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void login_비밀번호_불일치_예외() {
        UserLoginRequest request = new UserLoginRequest("testuser", "wrongpassword");
        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void login_요청_null_예외() {
        assertThatThrownBy(() -> authService.login(null))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void login_아이디_비어있음_예외() {
        UserLoginRequest request = new UserLoginRequest("", "password1234");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void login_비밀번호_비어있음_예외() {
        UserLoginRequest request = new UserLoginRequest("testuser", "");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(DiscodeitException.class);
    }
}
