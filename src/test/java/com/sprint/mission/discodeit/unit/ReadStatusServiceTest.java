package com.sprint.mission.discodeit.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sprint.mission.discodeit.dto.request.ReadStatusUpdateRequest;
import com.sprint.mission.discodeit.dto.response.ReadStatusDto;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.ReadStatusMapper;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.basic.BasicReadStatusService;
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
public class ReadStatusServiceTest {

  @Mock
  private ReadStatusRepository readStatusRepository;

  @Mock
  private ReadStatusMapper readStatusMapper;

  @InjectMocks
  private BasicReadStatusService readStatusService;

  @Test
  @DisplayName("ReadStatus 수정 시 요청 lastReadAt과 notificationEnabled가 둘 다 null일 경우 기존 값으로 유지")
  void update_null_lastReadAt_and_notificationEnabled() {
    // given
    UUID id = UUID.randomUUID();
    User user = mock(User.class);
    Channel channel = mock(Channel.class);

    ReadStatus readStatus = new ReadStatus(user, channel, Instant.now());

    Instant beforeLastReadAt = readStatus.getLastReadAt();
    boolean beforeNotificationEnabled = readStatus.isNotificationEnabled();

    // 두 필드 다 null로 설정
    ReadStatusUpdateRequest request = new ReadStatusUpdateRequest(null, null);

    // 간접적으로 가짜 ReadStatus id 설정(id 조회시 readStatus가 나오도록)
    when(readStatusRepository.findById(id)).thenReturn(Optional.of(readStatus));

    ReadStatusDto dto = mock(ReadStatusDto.class);
    when(readStatusMapper.toDto(readStatus)).thenReturn(dto);

    // when
    ReadStatusDto result = readStatusService.update(id, request);

    // then
    // lastRead, notificationEnabled 갱신되지 않았는지 확인
    assertThat(readStatus.getLastReadAt()).isEqualTo(beforeLastReadAt);
    assertThat(readStatus.isNotificationEnabled()).isEqualTo(beforeNotificationEnabled);
  }

}
