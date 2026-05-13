package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.dto.userstatus.CreateUserStatusRequest;
import com.sprint.mission.discodeit.service.dto.userstatus.UpdateUserStatusByUserIdRequest;
import com.sprint.mission.discodeit.service.dto.userstatus.UpdateUserStatusRequest;
import com.sprint.mission.discodeit.service.dto.userstatus.UserStatusDto;
import java.time.Instant;
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
class UserStatusServiceTest {

    @Mock private UserStatusRepository userStatusRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserStatusMapper userStatusMapper;

    @InjectMocks private UserStatusService userStatusService;

    private User user;
    private UserStatus userStatus;
    private UserStatusDto userStatusDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password1234")
                .build();
        userStatus = new UserStatus(user);
        userStatusDto = UserStatusDto.builder()
                .id(userStatus.getId())
                .userId(user.getId())
                .lastActiveAt(Instant.now())
                .build();
    }

    @Test
    void create_성공() {
        CreateUserStatusRequest request = new CreateUserStatusRequest(user.getId());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userStatusRepository.findByUserId(user.getId())).willReturn(Optional.empty());
        given(userStatusRepository.save(any(UserStatus.class))).willReturn(userStatus);
        given(userStatusMapper.toDto(userStatus)).willReturn(userStatusDto);

        UserStatusDto result = userStatusService.create(request);

        assertThat(result).isEqualTo(userStatusDto);
    }

    @Test
    void create_중복_예외() {
        CreateUserStatusRequest request = new CreateUserStatusRequest(user.getId());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userStatusRepository.findByUserId(user.getId())).willReturn(Optional.of(userStatus));

        assertThatThrownBy(() -> userStatusService.create(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void create_사용자_없음_예외() {
        UUID unknownId = UUID.randomUUID();
        CreateUserStatusRequest request = new CreateUserStatusRequest(unknownId);

        given(userRepository.findById(unknownId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userStatusService.create(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void create_요청_null_예외() {
        assertThatThrownBy(() -> userStatusService.create(null))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void create_userId_null_예외() {
        CreateUserStatusRequest request = new CreateUserStatusRequest(null);

        assertThatThrownBy(() -> userStatusService.create(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void find_성공() {
        UUID id = userStatus.getId();
        given(userStatusRepository.findById(id)).willReturn(Optional.of(userStatus));
        given(userStatusMapper.toDto(userStatus)).willReturn(userStatusDto);

        UserStatusDto result = userStatusService.find(id);

        assertThat(result).isEqualTo(userStatusDto);
    }

    @Test
    void find_없음_예외() {
        UUID id = UUID.randomUUID();
        given(userStatusRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userStatusService.find(id))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void find_id_null_예외() {
        assertThatThrownBy(() -> userStatusService.find(null))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void findAll_성공() {
        given(userStatusRepository.findAll()).willReturn(List.of(userStatus));
        given(userStatusMapper.toDto(userStatus)).willReturn(userStatusDto);

        List<UserStatusDto> result = userStatusService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(userStatusDto);
    }

    @Test
    void update_성공() {
        UUID id = userStatus.getId();
        Instant newTime = Instant.now();
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(id, newTime);

        given(userStatusRepository.findById(id)).willReturn(Optional.of(userStatus));
        given(userStatusMapper.toDto(userStatus)).willReturn(userStatusDto);

        UserStatusDto result = userStatusService.update(request);

        assertThat(result).isEqualTo(userStatusDto);
    }

    @Test
    void update_요청_null_예외() {
        assertThatThrownBy(() -> userStatusService.update((UpdateUserStatusRequest) null))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void update_id_null_예외() {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(null, Instant.now());

        assertThatThrownBy(() -> userStatusService.update(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void update_lastActiveAt_null_예외() {
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(UUID.randomUUID(), null);

        assertThatThrownBy(() -> userStatusService.update(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void update_없음_예외() {
        UUID unknownId = UUID.randomUUID();
        UpdateUserStatusRequest request = new UpdateUserStatusRequest(unknownId, Instant.now());

        given(userStatusRepository.findById(unknownId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userStatusService.update(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void updateByUserId_성공() {
        UUID userId = user.getId();
        Instant newTime = Instant.now();
        UpdateUserStatusByUserIdRequest request = new UpdateUserStatusByUserIdRequest(userId, newTime);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userStatusRepository.findByUserId(userId)).willReturn(Optional.of(userStatus));
        given(userStatusMapper.toDto(userStatus)).willReturn(userStatusDto);

        UserStatusDto result = userStatusService.updateByUserId(request);

        assertThat(result).isEqualTo(userStatusDto);
    }

    @Test
    void updateByUserId_사용자_없음_예외() {
        UUID unknownId = UUID.randomUUID();
        UpdateUserStatusByUserIdRequest request = new UpdateUserStatusByUserIdRequest(unknownId, Instant.now());

        given(userRepository.findById(unknownId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userStatusService.updateByUserId(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void updateByUserId_상태_없음_예외() {
        UUID userId = user.getId();
        UpdateUserStatusByUserIdRequest request = new UpdateUserStatusByUserIdRequest(userId, Instant.now());

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userStatusRepository.findByUserId(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userStatusService.updateByUserId(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void updateByUserId_userId로_lastActiveAt_null이면_현재시각_사용() {
        UUID userId = user.getId();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userStatusRepository.findByUserId(userId)).willReturn(Optional.of(userStatus));
        given(userStatusMapper.toDto(userStatus)).willReturn(userStatusDto);

        UserStatusDto result = userStatusService.updateByUserId(userId, null);

        assertThat(result).isEqualTo(userStatusDto);
    }

    @Test
    void updateByUserId_요청_null_예외() {
        assertThatThrownBy(() -> userStatusService.updateByUserId((UpdateUserStatusByUserIdRequest) null))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void delete_성공() {
        UUID id = userStatus.getId();
        given(userStatusRepository.findById(id)).willReturn(Optional.of(userStatus));

        userStatusService.delete(id);

        then(userStatusRepository).should().delete(userStatus);
    }

    @Test
    void delete_없음_예외() {
        UUID id = UUID.randomUUID();
        given(userStatusRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userStatusService.delete(id))
                .isInstanceOf(DiscodeitException.class);
    }
}
