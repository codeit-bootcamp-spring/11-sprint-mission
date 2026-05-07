package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.request.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
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
    private ChannelRepository channelRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ReadStatusRepository readStatusRepo;

    @Mock
    private ReadStatusMapper readStatusMapper;

    @InjectMocks
    private BasicReadStatusService readStatusService;

    @Test
    void create_success() {
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        Instant lastReadAt = Instant.now();
        ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, lastReadAt);
        User user = new User("taehk23", "taehk23@test.com", "password", null);
        Channel channel = new Channel(ChannelType.PUBLIC, "general", "general channel");
        ReadStatusDto expected = new ReadStatusDto(UUID.randomUUID(), userId, channelId, lastReadAt);

        given(channelRepo.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepo.findById(userId)).willReturn(Optional.of(user));
        given(readStatusRepo.findByUserAndChannel(user, channel)).willReturn(Optional.empty());
        given(readStatusMapper.toDto(any(ReadStatus.class))).willReturn(expected);

        ReadStatusDto result = readStatusService.create(request);

        assertThat(result).isEqualTo(expected);

        then(readStatusRepo).should().save(any(ReadStatus.class));
        then(readStatusMapper).should().toDto(any(ReadStatus.class));
    }

    @Test
    void create_fail_whenChannelNotFound() {
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, Instant.now());

        given(channelRepo.findById(channelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> readStatusService.create(request))
                .isInstanceOf(ChannelNotFoundException.class);

        then(userRepo).should(never()).findById(any());
        then(readStatusRepo).should(never()).save(any());
    }

    @Test
    void create_fail_whenUserNotFound() {
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, Instant.now());
        Channel channel = new Channel(ChannelType.PUBLIC, "general", "general channel");

        given(channelRepo.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepo.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> readStatusService.create(request))
                .isInstanceOf(UserNotFoundException.class);

        then(readStatusRepo).should(never()).save(any());
    }

    @Test
    void create_fail_whenReadStatusAlreadyExists() {
        UUID userId = UUID.randomUUID();
        UUID channelId = UUID.randomUUID();
        ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, Instant.now());
        User user = new User("taehk23", "taehk23@test.com", "password", null);
        Channel channel = new Channel(ChannelType.PUBLIC, "general", "general channel");
        ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());

        given(channelRepo.findById(channelId)).willReturn(Optional.of(channel));
        given(userRepo.findById(userId)).willReturn(Optional.of(user));
        given(readStatusRepo.findByUserAndChannel(user, channel)).willReturn(Optional.of(readStatus));

        assertThatThrownBy(() -> readStatusService.create(request))
                .isInstanceOf(ReadStatusAlreadyExistsException.class);

        then(readStatusRepo).should(never()).save(any());
    }

    @Test
    void find_success() {
        UUID readStatusId = UUID.randomUUID();
        User user = new User("taehk23", "taehk23@test.com", "password", null);
        Channel channel = new Channel(ChannelType.PUBLIC, "general", "general channel");
        ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());
        ReadStatusDto expected = new ReadStatusDto(readStatusId, user.getId(), channel.getId(), readStatus.getLastReadAt());

        given(readStatusRepo.findById(readStatusId)).willReturn(Optional.of(readStatus));
        given(readStatusMapper.toDto(readStatus)).willReturn(expected);

        ReadStatusDto result = readStatusService.find(readStatusId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void find_fail_whenReadStatusNotFound() {
        UUID readStatusId = UUID.randomUUID();

        given(readStatusRepo.findById(readStatusId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> readStatusService.find(readStatusId))
                .isInstanceOf(ReadStatusNotFoundException.class);

        then(readStatusMapper).should(never()).toDto(any());
    }

    @Test
    void findAllByUserId_success() {
        UUID userId = UUID.randomUUID();
        User user = new User("taehk23", "taehk23@test.com", "password", null);
        Channel channel = new Channel(ChannelType.PUBLIC, "general", "general channel");
        ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());
        ReadStatusDto dto = new ReadStatusDto(readStatus.getId(), user.getId(), channel.getId(), readStatus.getLastReadAt());

        given(userRepo.findById(userId)).willReturn(Optional.of(user));
        given(readStatusRepo.findAllWithUserAndChannelByUser(user)).willReturn(List.of(readStatus));
        given(readStatusMapper.toDto(readStatus)).willReturn(dto);

        List<ReadStatusDto> result = readStatusService.findAllByUserId(userId);

        assertThat(result).containsExactly(dto);
    }

    @Test
    void findAllByUserId_fail_whenUserNotFound() {
        UUID userId = UUID.randomUUID();

        given(userRepo.findById(userId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> readStatusService.findAllByUserId(userId))
                .isInstanceOf(UserNotFoundException.class);

        then(readStatusRepo).should(never()).findAllWithUserAndChannelByUser(any());
    }

    @Test
    void update_success() {
        UUID readStatusId = UUID.randomUUID();
        Instant newLastReadAt = Instant.now();
        User user = new User("taehk23", "taehk23@test.com", "password", null);
        Channel channel = new Channel(ChannelType.PUBLIC, "general", "general channel");
        ReadStatus readStatus = new ReadStatus(user, channel, Instant.now().minusSeconds(60));
        ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(newLastReadAt);

        given(readStatusRepo.findById(readStatusId)).willReturn(Optional.of(readStatus));

        readStatusService.update(readStatusId, request);

        assertThat(readStatus.getLastReadAt()).isEqualTo(newLastReadAt);
    }

    @Test
    void update_fail_whenReadStatusNotFound() {
        UUID readStatusId = UUID.randomUUID();
        ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(Instant.now());

        given(readStatusRepo.findById(readStatusId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> readStatusService.update(readStatusId, request))
                .isInstanceOf(ReadStatusNotFoundException.class);
    }

    @Test
    void delete_success() {
        UUID readStatusId = UUID.randomUUID();
        User user = new User("taehk23", "taehk23@test.com", "password", null);
        Channel channel = new Channel(ChannelType.PUBLIC, "general", "general channel");
        ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());

        given(readStatusRepo.findById(readStatusId)).willReturn(Optional.of(readStatus));

        readStatusService.delete(readStatusId);

        then(readStatusRepo).should().delete(readStatus);
    }

    @Test
    void delete_fail_whenReadStatusNotFound() {
        UUID readStatusId = UUID.randomUUID();

        given(readStatusRepo.findById(readStatusId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> readStatusService.delete(readStatusId))
                .isInstanceOf(ReadStatusNotFoundException.class);

        then(readStatusRepo).should(never()).delete(any());
    }
}
