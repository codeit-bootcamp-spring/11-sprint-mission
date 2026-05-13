package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateParam;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
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
class BasicReadStatusServiceTest {

    @Mock
    ReadStatusRepository readStatusRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ChannelRepository channelRepository;

    @Mock
    ReadStatusMapper readStatusMapper;

    @InjectMocks
    BasicReadStatusService readStatusService;

    @Test
    void create_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        Channel channel = new Channel("general", "general channel");
        Instant lastReadAt = Instant.parse("2026-05-09T10:00:00Z");

        ReadStatusCreateRequest request = new ReadStatusCreateRequest(
                user.getId(),
                channel.getId(),
                lastReadAt
        );

        ReadStatus savedReadStatus = new ReadStatus(user, channel, lastReadAt);

        ReadStatusDto expectedDto = new ReadStatusDto(
                savedReadStatus.getId(),
                user.getId(),
                channel.getId(),
                lastReadAt
        );

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(channelRepository.findById(channel.getId())).willReturn(Optional.of(channel));
        given(readStatusRepository.existsByUser_IdAndChannel_Id(user.getId(), channel.getId()))
                .willReturn(false);
        given(readStatusRepository.save(any(ReadStatus.class))).willReturn(savedReadStatus);
        given(readStatusMapper.toDto(savedReadStatus)).willReturn(expectedDto);

        // when
        ReadStatusDto result = readStatusService.create(request);

        // then
        assertThat(result).isEqualTo(expectedDto);

        then(userRepository).should().findById(user.getId());
        then(channelRepository).should().findById(channel.getId());
        then(readStatusRepository).should().existsByUser_IdAndChannel_Id(user.getId(), channel.getId());
        then(readStatusRepository).should().save(any(ReadStatus.class));
        then(readStatusMapper).should().toDto(savedReadStatus);
    }

    @Test
    void create_fail_whenUserNotFound() {
        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();

        ReadStatusCreateRequest request = new ReadStatusCreateRequest(
                userId,
                channelId,
                Instant.parse("2026-05-09T10:00:00Z")
        );

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> readStatusService.create(request))
                .isInstanceOf(RuntimeException.class);

        then(userRepository).should().findById(userId);
        then(channelRepository).should(never()).findById(any(UUID.class));
        then(readStatusRepository).should(never()).save(any(ReadStatus.class));
    }

    @Test
    void create_fail_whenChannelNotFound() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        UUID channelId = UUID.randomUUID();

        ReadStatusCreateRequest request = new ReadStatusCreateRequest(
                user.getId(),
                channelId,
                Instant.parse("2026-05-09T10:00:00Z")
        );

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> readStatusService.create(request))
                .isInstanceOf(RuntimeException.class);

        then(userRepository).should().findById(user.getId());
        then(channelRepository).should().findById(channelId);
        then(readStatusRepository).should(never()).save(any(ReadStatus.class));
    }

    @Test
    void create_fail_whenAlreadyExists() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        Channel channel = new Channel("general", "general channel");

        ReadStatusCreateRequest request = new ReadStatusCreateRequest(
                user.getId(),
                channel.getId(),
                Instant.parse("2026-05-09T10:00:00Z")
        );

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(channelRepository.findById(channel.getId())).willReturn(Optional.of(channel));
        given(readStatusRepository.existsByUser_IdAndChannel_Id(user.getId(), channel.getId()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> readStatusService.create(request))
                .isInstanceOf(RuntimeException.class);

        then(userRepository).should().findById(user.getId());
        then(channelRepository).should().findById(channel.getId());
        then(readStatusRepository).should().existsByUser_IdAndChannel_Id(user.getId(), channel.getId());
        then(readStatusRepository).should(never()).save(any(ReadStatus.class));
    }

    @Test
    void find_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        Channel channel = new Channel("general", "general channel");

        ReadStatus readStatus = new ReadStatus(
                user,
                channel,
                Instant.parse("2026-05-09T10:00:00Z")
        );

        ReadStatusDto dto = new ReadStatusDto(
                readStatus.getId(),
                user.getId(),
                channel.getId(),
                readStatus.getLastReadAt()
        );

        given(readStatusRepository.findById(readStatus.getId()))
                .willReturn(Optional.of(readStatus));
        given(readStatusMapper.toDto(readStatus)).willReturn(dto);

        // when
        Optional<ReadStatusDto> result = readStatusService.find(readStatus.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(dto);
    }

    @Test
    void findAllByUserId_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        Channel channel = new Channel("general", "general channel");
        ReadStatus readStatus = new ReadStatus(
                user,
                channel,
                Instant.parse("2026-05-09T10:00:00Z")
        );

        ReadStatusDto dto = new ReadStatusDto(
                readStatus.getId(),
                user.getId(),
                channel.getId(),
                readStatus.getLastReadAt()
        );

        given(readStatusRepository.findAllByUser_Id(user.getId()))
                .willReturn(List.of(readStatus));
        given(readStatusMapper.toDto(readStatus)).willReturn(dto);

        // when
        List<ReadStatusDto> result = readStatusService.findAllByUserId(user.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(dto);
    }

    @Test
    void update_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        Channel channel = new Channel("general", "general channel");

        ReadStatus readStatus = new ReadStatus(
                user,
                channel,
                Instant.parse("2026-05-09T10:00:00Z")
        );

        Instant newLastReadAt = Instant.parse("2026-05-09T11:00:00Z");

        ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(newLastReadAt);
        ReadStatusUpdateParam param = new ReadStatusUpdateParam(readStatus.getId(), request);

        ReadStatusDto expectedDto = new ReadStatusDto(
                readStatus.getId(),
                user.getId(),
                channel.getId(),
                newLastReadAt
        );

        given(readStatusRepository.findById(readStatus.getId()))
                .willReturn(Optional.of(readStatus));
        given(readStatusMapper.toDto(readStatus)).willReturn(expectedDto);

        // when
        ReadStatusDto result = readStatusService.update(param);

        // then
        assertThat(result).isEqualTo(expectedDto);
        assertThat(readStatus.getLastReadAt()).isEqualTo(newLastReadAt);

        then(readStatusRepository).should().findById(readStatus.getId());
        then(readStatusMapper).should().toDto(readStatus);
    }

    @Test
    void update_fail_whenNotFound() {
        // given
        UUID readStatusId = UUID.randomUUID();

        ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(
                Instant.parse("2026-05-09T11:00:00Z")
        );
        ReadStatusUpdateParam param = new ReadStatusUpdateParam(readStatusId, request);

        given(readStatusRepository.findById(readStatusId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> readStatusService.update(param))
                .isInstanceOf(RuntimeException.class);

        then(readStatusRepository).should().findById(readStatusId);
    }

    @Test
    void delete_success() {
        // given
        User user = new User("evan", "evan@test.com", "password123");
        Channel channel = new Channel("general", "general channel");

        ReadStatus readStatus = new ReadStatus(
                user,
                channel,
                Instant.parse("2026-05-09T10:00:00Z")
        );

        given(readStatusRepository.findById(readStatus.getId()))
                .willReturn(Optional.of(readStatus));

        // when
        readStatusService.delete(readStatus.getId());

        // then
        then(readStatusRepository).should().findById(readStatus.getId());
        then(readStatusRepository).should().deleteById(readStatus.getId());
    }

    @Test
    void delete_fail_whenNotFound() {
        // given
        UUID readStatusId = UUID.randomUUID();

        given(readStatusRepository.findById(readStatusId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> readStatusService.delete(readStatusId))
                .isInstanceOf(RuntimeException.class);

        then(readStatusRepository).should().findById(readStatusId);
        then(readStatusRepository).should(never()).deleteById(readStatusId);
    }
}