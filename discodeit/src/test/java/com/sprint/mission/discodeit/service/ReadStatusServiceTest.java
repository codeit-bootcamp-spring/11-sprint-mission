package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.exception.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.dto.readstatus.CreateReadStatusRequest;
import com.sprint.mission.discodeit.service.dto.readstatus.ReadStatusDto;
import com.sprint.mission.discodeit.service.dto.readstatus.UpdateReadStatusRequest;
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
class ReadStatusServiceTest {

    @Mock private ReadStatusRepository readStatusRepository;
    @Mock private UserRepository userRepository;
    @Mock private ChannelRepository channelRepository;
    @Mock private ReadStatusMapper readStatusMapper;

    @InjectMocks private ReadStatusService readStatusService;

    private User user;
    private Channel channel;
    private ReadStatus readStatus;
    private ReadStatusDto readStatusDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password1234")
                .build();
        channel = Channel.publicChannel("일반", "일반 채널");
        readStatus = new ReadStatus(user, channel, Instant.now());
        readStatusDto = ReadStatusDto.builder()
                .id(readStatus.getId())
                .userId(user.getId())
                .channelId(channel.getId())
                .lastReadAt(Instant.now())
                .build();
    }

    @Test
    void create_성공() {
        CreateReadStatusRequest request = new CreateReadStatusRequest(user.getId(), channel.getId(), Instant.now());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(channelRepository.findById(channel.getId())).willReturn(Optional.of(channel));
        given(readStatusRepository.existsByUserIdAndChannelId(user.getId(), channel.getId())).willReturn(false);
        given(readStatusRepository.save(any(ReadStatus.class))).willReturn(readStatus);
        given(readStatusMapper.toDto(readStatus)).willReturn(readStatusDto);

        ReadStatusDto result = readStatusService.create(request);

        assertThat(result).isEqualTo(readStatusDto);
    }

    @Test
    void create_중복_읽음상태_예외() {
        CreateReadStatusRequest request = new CreateReadStatusRequest(user.getId(), channel.getId(), Instant.now());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(channelRepository.findById(channel.getId())).willReturn(Optional.of(channel));
        given(readStatusRepository.existsByUserIdAndChannelId(user.getId(), channel.getId())).willReturn(true);

        assertThatThrownBy(() -> readStatusService.create(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void create_사용자_없음_예외() {
        UUID unknownUserId = UUID.randomUUID();
        CreateReadStatusRequest request = new CreateReadStatusRequest(unknownUserId, channel.getId(), Instant.now());

        given(userRepository.findById(unknownUserId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> readStatusService.create(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void create_채널_없음_예외() {
        UUID unknownChannelId = UUID.randomUUID();
        CreateReadStatusRequest request = new CreateReadStatusRequest(user.getId(), unknownChannelId, Instant.now());

        given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
        given(channelRepository.findById(unknownChannelId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> readStatusService.create(request))
                .isInstanceOf(ChannelNotFoundException.class);
    }

    @Test
    void create_요청_null_예외() {
        assertThatThrownBy(() -> readStatusService.create(null))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void create_userId_null_예외() {
        CreateReadStatusRequest request = new CreateReadStatusRequest(null, channel.getId(), Instant.now());

        assertThatThrownBy(() -> readStatusService.create(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void create_channelId_null_예외() {
        CreateReadStatusRequest request = new CreateReadStatusRequest(user.getId(), null, Instant.now());

        assertThatThrownBy(() -> readStatusService.create(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void find_성공() {
        UUID id = readStatus.getId();
        given(readStatusRepository.findById(id)).willReturn(Optional.of(readStatus));
        given(readStatusMapper.toDto(readStatus)).willReturn(readStatusDto);

        ReadStatusDto result = readStatusService.find(id);

        assertThat(result).isEqualTo(readStatusDto);
    }

    @Test
    void find_없음_예외() {
        UUID id = UUID.randomUUID();
        given(readStatusRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> readStatusService.find(id))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void find_id_null_예외() {
        assertThatThrownBy(() -> readStatusService.find(null))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void findAllByUserId_성공() {
        UUID userId = user.getId();
        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(readStatusRepository.findAllByUserId(userId)).willReturn(List.of(readStatus));
        given(readStatusMapper.toDto(readStatus)).willReturn(readStatusDto);

        List<ReadStatusDto> result = readStatusService.findAllByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(readStatusDto);
    }

    @Test
    void findAllByUserId_userId_null_예외() {
        assertThatThrownBy(() -> readStatusService.findAllByUserId(null))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void findAllByUserId_사용자_없음_예외() {
        UUID unknownId = UUID.randomUUID();
        given(userRepository.findById(unknownId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> readStatusService.findAllByUserId(unknownId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void update_성공() {
        UUID id = readStatus.getId();
        Instant newTime = Instant.now();
        UpdateReadStatusRequest request = new UpdateReadStatusRequest(id, newTime);

        given(readStatusRepository.findById(id)).willReturn(Optional.of(readStatus));
        given(readStatusMapper.toDto(readStatus)).willReturn(readStatusDto);

        ReadStatusDto result = readStatusService.update(request);

        assertThat(result).isEqualTo(readStatusDto);
    }

    @Test
    void update_요청_null_예외() {
        assertThatThrownBy(() -> readStatusService.update(null))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void update_id_null_예외() {
        UpdateReadStatusRequest request = new UpdateReadStatusRequest(null, Instant.now());

        assertThatThrownBy(() -> readStatusService.update(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void update_lastReadAt_null_예외() {
        UpdateReadStatusRequest request = new UpdateReadStatusRequest(UUID.randomUUID(), null);

        assertThatThrownBy(() -> readStatusService.update(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void update_없음_예외() {
        UUID unknownId = UUID.randomUUID();
        UpdateReadStatusRequest request = new UpdateReadStatusRequest(unknownId, Instant.now());

        given(readStatusRepository.findById(unknownId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> readStatusService.update(request))
                .isInstanceOf(DiscodeitException.class);
    }

    @Test
    void delete_성공() {
        UUID id = readStatus.getId();
        given(readStatusRepository.findById(id)).willReturn(Optional.of(readStatus));

        readStatusService.delete(id);

        then(readStatusRepository).should().delete(readStatus);
    }

    @Test
    void delete_없음_예외() {
        UUID id = UUID.randomUUID();
        given(readStatusRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> readStatusService.delete(id))
                .isInstanceOf(DiscodeitException.class);
    }
}
