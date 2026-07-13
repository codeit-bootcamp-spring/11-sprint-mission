package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.dto.ReadStatusDto;
import com.sprint.mission.discodeit.dto.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BasicReadStatusServiceTest {

  @InjectMocks
  private BasicReadStatusService readStatusService;

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private ReadStatusMapper readStatusMapper;

  @Test
  @DisplayName("읽음 상태 생성 성공")
  void create_success() {
    UUID userId = UUID.randomUUID();
    UUID channelId = UUID.randomUUID();
    Instant now = Instant.now();
    ReadStatusCreateRequest request = new ReadStatusCreateRequest(userId, channelId, now);

    User user = new User("user", "email@test.com", "pw");
    Channel channel = mock(Channel.class);
    ReadStatusDto dto = new ReadStatusDto(UUID.randomUUID(), userId, channelId, now, true);

    given(userRepository.findById(userId)).willReturn(Optional.of(user));
    given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
    given(readStatusRepository.existsByUserIdAndChannelId(userId, channelId)).willReturn(false);
    given(readStatusMapper.toDto(any(ReadStatus.class))).willReturn(dto);

    ReadStatusDto result = readStatusService.create(request);

    assertThat(result).isNotNull();
    assertThat(result.userId()).isEqualTo(userId);
    then(readStatusRepository).should().save(any(ReadStatus.class));
  }

  @Test
  @DisplayName("읽음 상태 생성 실패 - 존재하지 않는 유저")
  void create_fail_userNotFound() {
    ReadStatusCreateRequest request = new ReadStatusCreateRequest(UUID.randomUUID(),
        UUID.randomUUID(), Instant.now());

    given(userRepository.findById(request.userId())).willReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> {
      readStatusService.create(request);
    });
  }

  @Test
  @DisplayName("읽음 상태 생성 실패 - 존재하지 않는 채널")
  void create_fail_channelNotFound() {
    ReadStatusCreateRequest request = new ReadStatusCreateRequest(UUID.randomUUID(),
        UUID.randomUUID(), Instant.now());
    User user = new User("user", "email", "pw");

    given(userRepository.findById(request.userId())).willReturn(Optional.of(user));
    given(channelRepository.findById(request.channelId())).willReturn(Optional.empty());

    assertThrows(ChannelNotFoundException.class, () -> {
      readStatusService.create(request);
    });
  }

  @Test
  @DisplayName("읽음 상태 생성 실패 - 이미 상태 존재")
  void create_fail_alreadyExists() {
    ReadStatusCreateRequest request = new ReadStatusCreateRequest(UUID.randomUUID(),
        UUID.randomUUID(), Instant.now());
    User user = new User("user", "email", "pw");
    Channel channel = mock(Channel.class);

    given(userRepository.findById(request.userId())).willReturn(Optional.of(user));
    given(channelRepository.findById(request.channelId())).willReturn(Optional.of(channel));
    given(readStatusRepository.existsByUserIdAndChannelId(request.userId(),
        request.channelId())).willReturn(true);

    assertThrows(ReadStatusAlreadyExistsException.class, () -> {
      readStatusService.create(request);
    });
  }

  @Test
  @DisplayName("읽음 상태 수정 성공")
  void update_success() {
    UUID readStatusId = UUID.randomUUID();
    Instant newTime = Instant.now();
    ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(newTime, false);

    User user = new User("user", "email@test.com", "pw");
    Channel channel = mock(Channel.class);
    ReadStatus readStatus = new ReadStatus(user, channel, Instant.now().minusSeconds(100));

    ReadStatusDto dto = new ReadStatusDto(readStatusId, UUID.randomUUID(), UUID.randomUUID(),
        newTime, false);

    given(readStatusRepository.findById(readStatusId)).willReturn(Optional.of(readStatus));
    given(readStatusMapper.toDto(readStatus)).willReturn(dto);

    ReadStatusDto result = readStatusService.update(readStatusId, request);

    assertThat(result.notificationEnabled()).isFalse();
    assertThat(result.lastReadAt()).isEqualTo(newTime);
  }

  @Test
  @DisplayName("유저 ID로 전체 읽음 상태 조회")
  void findAllByUserId_success() {
    UUID userId = UUID.randomUUID();
    User user = new User("user", "email@test.com", "pw");
    Channel channel = mock(Channel.class);
    ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());

    given(readStatusRepository.findByUserId(userId)).willReturn(List.of(readStatus));
    given(readStatusMapper.toDto(any(ReadStatus.class))).willReturn(mock(ReadStatusDto.class));

    List<ReadStatusDto> results = readStatusService.findAllByUserId(userId);

    assertThat(results).hasSize(1);
  }

  @Test
  @DisplayName("읽음 상태 삭제 성공")
  void delete_success() {
    UUID readStatusId = UUID.randomUUID();

    readStatusService.delete(readStatusId);

    then(readStatusRepository).should().deleteById(readStatusId);
  }
}