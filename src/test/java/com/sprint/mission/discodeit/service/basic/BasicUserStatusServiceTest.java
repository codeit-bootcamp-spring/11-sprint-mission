package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserStatusCreateRequest;
import com.sprint.mission.discodeit.dto.UserStatusDto;
import com.sprint.mission.discodeit.dto.UserStatusUpdateParam;
import com.sprint.mission.discodeit.dto.UserStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.mapper.UserStatusMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class BasicUserStatusServiceTest {

    @Mock
    UserStatusRepository userStatusRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    UserStatusMapper userStatusMapper;

    @InjectMocks
    BasicUserStatusService userStatusService;

    @Test
    void create_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        Instant lastActiveAt = Instant.parse("2026-05-09T10:00:00Z");

        UserStatusCreateRequest request = new UserStatusCreateRequest(user.getId(), lastActiveAt);
        UserStatus savedStatus = new UserStatus(user, lastActiveAt);

        UserStatusDto expectedDto = new UserStatusDto(
                savedStatus.getId(),
                user.getId(),
                lastActiveAt
        );

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userStatusRepository.existsByUser_Id(user.getId())).willReturn(false);
        given(userStatusRepository.save(any(UserStatus.class))).willReturn(savedStatus);
        given(userStatusMapper.toDto(savedStatus)).willReturn(expectedDto);

        // when
        UserStatusDto result = userStatusService.create(request);

        // then
        assertThat(result).isEqualTo(expectedDto);

        then(userRepository).should().findById(user.getId());
        then(userStatusRepository).should().existsByUser_Id(user.getId());
        then(userStatusRepository).should().save(any(UserStatus.class));
        then(userStatusMapper).should().toDto(savedStatus);
    }

    @Test
    void create_fail_whenUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UserStatusCreateRequest request = new UserStatusCreateRequest(
                userId,
                Instant.parse("2026-05-09T10:00:00Z")
        );

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userStatusService.create(request))
                .isInstanceOf(RuntimeException.class);

        then(userRepository).should().findById(userId);
        then(userStatusRepository).should(never()).save(any(UserStatus.class));
    }

    @Test
    void create_fail_whenAlreadyExists() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        UserStatusCreateRequest request = new UserStatusCreateRequest(
                user.getId(),
                Instant.parse("2026-05-09T10:00:00Z")
        );

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(userStatusRepository.existsByUser_Id(user.getId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> userStatusService.create(request))
                .isInstanceOf(RuntimeException.class);

        then(userRepository).should().findById(user.getId());
        then(userStatusRepository).should().existsByUser_Id(user.getId());
        then(userStatusRepository).should(never()).save(any(UserStatus.class));
    }

    @Test
    void find_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        UserStatus status = new UserStatus(user, Instant.parse("2026-05-09T10:00:00Z"));

        UserStatusDto dto = new UserStatusDto(
                status.getId(),
                user.getId(),
                status.getLastActiveAt()
        );

        given(userStatusRepository.findById(status.getId())).willReturn(Optional.of(status));
        given(userStatusMapper.toDto(status)).willReturn(dto);

        // when
        Optional<UserStatusDto> result = userStatusService.find(status.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(dto);
    }

    @Test
    void findAll_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        UserStatus status = new UserStatus(user, Instant.parse("2026-05-09T10:00:00Z"));

        UserStatusDto dto = new UserStatusDto(
                status.getId(),
                user.getId(),
                status.getLastActiveAt()
        );

        given(userStatusRepository.findAll()).willReturn(List.of(status));
        given(userStatusMapper.toDto(status)).willReturn(dto);

        // when
        List<UserStatusDto> result = userStatusService.findAll();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);
    }

    @Test
    void update_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        UserStatus status = new UserStatus(user, Instant.parse("2026-05-09T10:00:00Z"));

        Instant newLastActiveAt = Instant.parse("2026-05-09T11:00:00Z");
        UserStatusUpdateRequest request = new UserStatusUpdateRequest(newLastActiveAt);
        UserStatusUpdateParam param = new UserStatusUpdateParam(status.getId(), request);

        UserStatusDto expectedDto = new UserStatusDto(
                status.getId(),
                user.getId(),
                newLastActiveAt
        );

        given(userStatusRepository.findById(status.getId())).willReturn(Optional.of(status));
        given(userStatusMapper.toDto(status)).willReturn(expectedDto);

        // when
        UserStatusDto result = userStatusService.update(param);

        // then
        assertThat(result).isEqualTo(expectedDto);
        assertThat(status.getLastActiveAt()).isEqualTo(newLastActiveAt);

        then(userStatusRepository).should().findById(status.getId());
        then(userStatusMapper).should().toDto(status);
    }

    @Test
    void update_fail_whenNotFound() {
        // given
        UUID statusId = UUID.randomUUID();
        UserStatusUpdateRequest request = new UserStatusUpdateRequest(
                Instant.parse("2026-05-09T11:00:00Z")
        );
        UserStatusUpdateParam param = new UserStatusUpdateParam(statusId, request);

        given(userStatusRepository.findById(statusId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userStatusService.update(param))
                .isInstanceOf(RuntimeException.class);

        then(userStatusRepository).should().findById(statusId);
    }

    @Test
    void updateByUserId_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        UserStatus status = new UserStatus(user, Instant.parse("2026-05-09T10:00:00Z"));

        Instant newLastActiveAt = Instant.parse("2026-05-09T11:00:00Z");
        UserStatusUpdateRequest request = new UserStatusUpdateRequest(newLastActiveAt);

        UserStatusDto expectedDto = new UserStatusDto(
                status.getId(),
                user.getId(),
                newLastActiveAt
        );

        given(userStatusRepository.findByUser_Id(user.getId())).willReturn(Optional.of(status));
        given(userStatusMapper.toDto(status)).willReturn(expectedDto);

        // when
        UserStatusDto result = userStatusService.updateByUserId(user.getId(), request);

        // then
        assertThat(result).isEqualTo(expectedDto);
        assertThat(status.getLastActiveAt()).isEqualTo(newLastActiveAt);

        then(userStatusRepository).should().findByUser_Id(user.getId());
        then(userStatusMapper).should().toDto(status);
    }

    @Test
    void updateByUserId_fail_whenNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UserStatusUpdateRequest request = new UserStatusUpdateRequest(
                Instant.parse("2026-05-09T11:00:00Z")
        );

        given(userStatusRepository.findByUser_Id(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userStatusService.updateByUserId(userId, request))
                .isInstanceOf(RuntimeException.class);

        then(userStatusRepository).should().findByUser_Id(userId);
    }

    @Test
    void delete_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        UserStatus status = new UserStatus(user, Instant.parse("2026-05-09T10:00:00Z"));

        given(userStatusRepository.findById(status.getId())).willReturn(Optional.of(status));

        // when
        userStatusService.delete(status.getId());

        // then
        then(userStatusRepository).should().findById(status.getId());
        then(userStatusRepository).should().deleteById(status.getId());
    }

    @Test
    void delete_fail_whenNotFound() {
        // given
        UUID statusId = UUID.randomUUID();

        given(userStatusRepository.findById(statusId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userStatusService.delete(statusId))
                .isInstanceOf(RuntimeException.class);

        then(userStatusRepository).should().findById(statusId);
        then(userStatusRepository).should(never()).deleteById(statusId);
    }
}