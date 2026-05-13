package com.sprint.mission.discodeit.service.basic;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.sprint.mission.discodeit.dto.ReadStatusCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.readstatus.ReadStatusAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
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
}