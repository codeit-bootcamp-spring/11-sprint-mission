package com.sprint.mission.discodeit.service.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.sprint.mission.discodeit.dto.data.ReadStatusDto;
import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class BasicReadStatusServiceTest {

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ChannelRepository channelRepository;

  @Mock
  private ReadStatusMapper readStatusMapper;

  @InjectMocks
  private BasicReadStatusService readStatusService;

  private UUID readStatusId;
  private ReadStatus readStatus;

  @BeforeEach
  void setUp() {
    readStatusId = UUID.randomUUID();
    User user = new User("testUser", "test@example.com", "password", null);
    Channel channel = new Channel(ChannelType.PUBLIC, "채널", "설명");
    readStatus = new ReadStatus(user, channel, Instant.now().minusSeconds(3600));
    ReflectionTestUtils.setField(readStatus, "id", readStatusId);
  }

  @Test
  @DisplayName("lastReadAt만 수정하면 notificationEnabled는 그대로 유지된다")
  void update_OnlyLastReadAt_KeepsNotificationEnabled() {
    // given
    Instant newLastReadAt = Instant.now();
    ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(newLastReadAt, null);
    given(readStatusRepository.findById(eq(readStatusId))).willReturn(Optional.of(readStatus));
    given(readStatusMapper.toDto(eq(readStatus))).willAnswer(invocation -> new ReadStatusDto(
        readStatusId, readStatus.getUser().getId(), readStatus.getChannel().getId(),
        readStatus.getLastReadAt(), readStatus.isNotificationEnabled()));

    // when
    ReadStatusDto result = readStatusService.update(readStatusId, request);

    // then
    assertThat(result.lastReadAt()).isEqualTo(newLastReadAt);
    assertThat(result.notificationEnabled()).isFalse();
  }

  @Test
  @DisplayName("notificationEnabled만 수정하면 lastReadAt은 그대로 유지된다")
  void update_OnlyNotificationEnabled_KeepsLastReadAt() {
    // given
    Instant originalLastReadAt = readStatus.getLastReadAt();
    ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(null, true);
    given(readStatusRepository.findById(eq(readStatusId))).willReturn(Optional.of(readStatus));
    given(readStatusMapper.toDto(eq(readStatus))).willAnswer(invocation -> new ReadStatusDto(
        readStatusId, readStatus.getUser().getId(), readStatus.getChannel().getId(),
        readStatus.getLastReadAt(), readStatus.isNotificationEnabled()));

    // when
    ReadStatusDto result = readStatusService.update(readStatusId, request);

    // then
    assertThat(result.lastReadAt()).isEqualTo(originalLastReadAt);
    assertThat(result.notificationEnabled()).isTrue();
  }
}
