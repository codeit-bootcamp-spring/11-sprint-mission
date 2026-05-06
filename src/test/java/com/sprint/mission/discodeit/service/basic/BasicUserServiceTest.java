package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.dto.request.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.EmailAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.exception.user.UsernameAlreadyExistsException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.BinaryContentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class BasicUserServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BinaryContentService binaryContentService;

    @Mock
    private MultipartFile profile;

    @InjectMocks
    private BasicUserService userService;

    @Test
    void create_success() {

        UserCreateRequest request = new UserCreateRequest(
                "taehk23",
                "taehk23@test.com",
                "password123"
        );

        BinaryContent binaryContent = new BinaryContent(
                "profile.png",
                "image/png",
                1000L
        );

        UserDto expected = new UserDto(
                UUID.randomUUID(),
                "taehk23",
                "taehk23@test.com",
                null,
                false
        );

        given(userRepo.findByUsername("taehk23")).willReturn(Optional.empty());
        given(userRepo.findByEmail("taehk23@test.com")).willReturn(Optional.empty());
        given(binaryContentService.create(profile)).willReturn(binaryContent);
        given(userMapper.toDto(any(User.class))).willReturn(expected);

        UserDto result = userService.create(request, profile);

        assertThat(result).isEqualTo(expected);

        then(userRepo).should().findByUsername("taehk23");
        then(userRepo).should().findByEmail("taehk23@test.com");
        then(binaryContentService).should().create(profile);
        then(userRepo).should().save(any(User.class));
        then(userMapper).should().toDto(any(User.class));
    }

    @Test
    void create_fail_whenUsernameAlreadyExists() {

        UserCreateRequest request = new UserCreateRequest(
                "taehk23",
                "taehk23@test.com",
                "password123"
        );

        User existingUser = new User(
                "taehk23",
                "other@test.com",
                "password",
                null
        );

        given(userRepo.findByUsername("taehk23")).willReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.create(request, profile))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        then(userRepo).should().findByUsername("taehk23");
        then(userRepo).should(never()).findByEmail(any());
        then(binaryContentService).should(never()).create(profile);
        then(userRepo).should(never()).save(any());
        then(userMapper).should(never()).toDto(any());

    }

    @Test
    void create_fail_whenEmailAlreadyExists() {

        UserCreateRequest request = new UserCreateRequest(
                "taehk23",
                "taehk23@test.com",
                "password123"
        );

        User existingUser = new User(
                "other",
                "taehk23@test.com",
                "password",
                null
        );

        given(userRepo.findByUsername("taehk23")).willReturn(Optional.empty());
        given(userRepo.findByEmail("taehk23@test.com")).willReturn(Optional.of(existingUser));

        assertThatThrownBy(() -> userService.create(request, profile))
                .isInstanceOf(EmailAlreadyExistsException.class);

        then(userRepo).should().findByUsername(any());
        then(userRepo).should().findByEmail("taehk23@test.com");
        then(binaryContentService).should(never()).create(profile);
        then(userRepo).should(never()).save(any());
        then(userMapper).should(never()).toDto(any());

    }

    @Test
    void update_success() {

        UUID userId = UUID.randomUUID();

        User user = new User(
                "taehk23",
                "taehk23@test.com",
                "oldPassword",
                null
        );

        UserUpdateRequest request = new UserUpdateRequest(
                "other",
                "other@test.com",
                "newPassword"
        );

        given(userRepo.findById(userId)).willReturn(Optional.of(user));
        given(userRepo.findByUsername("other")).willReturn(Optional.empty());
        given(userRepo.findByEmail("other@test.com")).willReturn(Optional.empty());
        given(binaryContentService.create(profile)).willReturn(null);

        userService.update(userId, request, profile);

        assertThat(user.getUsername()).isEqualTo("other");
        assertThat(user.getEmail()).isEqualTo("other@test.com");
        assertThat(user.getPassword()).isEqualTo("newPassword");

        then(userRepo).should().findById(userId);
        then(userRepo).should().findByUsername("other");
        then(userRepo).should().findByEmail("other@test.com");
        then(binaryContentService).should().create(profile);
    }

    @Test
    void update_fail_whenUserNotFound() {
        UUID userId = UUID.randomUUID();

        UserUpdateRequest request = new UserUpdateRequest(
                "other",
                "other@test.com",
                "newPassword"
        );

        given(userRepo.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(userId, request, profile))
            .isInstanceOf(UserNotFoundException.class);

        then(userRepo).should().findById(userId);
        then(userRepo).should(never()).findByUsername(any());
        then(userRepo).should(never()).findByEmail(any());
        then(binaryContentService).should(never()).create(any());

    }

    @Test
    void delete_success() {

        UUID userId = UUID.randomUUID();

        BinaryContent binaryContent = new BinaryContent(
                "profile.png",
                "image/png",
                1000L
        );

        User user = new User(
                "taehk23",
                "taehk23@test.com",
                "password",
                binaryContent
        );

        given(userRepo.findById(userId)).willReturn(Optional.of(user));

        userService.delete(userId);

        then(userRepo).should().findById(userId);
        then(binaryContentService).should().delete(binaryContent);
        then(userRepo).should().delete(user);
    }

    @Test
    void delete_success_withoutProfile() {

        UUID userId = UUID.randomUUID();

        User user = new User(
                "taehk23",
                "taehk23@test.com",
                "password",
                null
        );

        given(userRepo.findById(userId)).willReturn(Optional.of(user));

        userService.delete(userId);

        then(userRepo).should().findById(userId);
        then(binaryContentService).should(never()).delete(any());
        then(userRepo).should().delete(user);
    }

    @Test
    void delete_fail_whenUserNotFound() {
        UUID userId = UUID.randomUUID();

        given(userRepo.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.delete(userId))
                .isInstanceOf(UserNotFoundException.class);

        then(userRepo).should().findById(userId);
        then(binaryContentService).should(never()).delete(any());
        then(userRepo).should(never()).delete(any());
    }

}
