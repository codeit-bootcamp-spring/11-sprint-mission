package com.sprint.mission.discodeit.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ReadStatusTest {

  private final User user = new User("testUser", "test@example.com", "password", null);

  @Test
  @DisplayName("PRIVATE 채널의 읽음 상태는 알림이 기본으로 활성화된다")
  void notificationEnabled_DefaultsToTrue_ForPrivateChannel() {
    // given
    Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);

    // when
    ReadStatus readStatus = new ReadStatus(user, privateChannel, Instant.now());

    // then
    assertThat(readStatus.isNotificationEnabled()).isTrue();
  }

  @Test
  @DisplayName("PUBLIC 채널의 읽음 상태는 알림이 기본으로 비활성화된다")
  void notificationEnabled_DefaultsToFalse_ForPublicChannel() {
    // given
    Channel publicChannel = new Channel(ChannelType.PUBLIC, "공지", "설명");

    // when
    ReadStatus readStatus = new ReadStatus(user, publicChannel, Instant.now());

    // then
    assertThat(readStatus.isNotificationEnabled()).isFalse();
  }

  @Test
  @DisplayName("updateNotificationEnabled으로 알림 여부를 변경할 수 있다")
  void updateNotificationEnabled_ChangesValue() {
    // given
    Channel publicChannel = new Channel(ChannelType.PUBLIC, "공지", "설명");
    ReadStatus readStatus = new ReadStatus(user, publicChannel, Instant.now());

    // when
    readStatus.updateNotificationEnabled(true);

    // then
    assertThat(readStatus.isNotificationEnabled()).isTrue();
  }

  @Test
  @DisplayName("updateNotificationEnabled에 null을 전달하면 변경되지 않는다")
  void updateNotificationEnabled_WithNull_DoesNotChange() {
    // given
    Channel privateChannel = new Channel(ChannelType.PRIVATE, null, null);
    ReadStatus readStatus = new ReadStatus(user, privateChannel, Instant.now());

    // when
    readStatus.updateNotificationEnabled(null);

    // then
    assertThat(readStatus.isNotificationEnabled()).isTrue();
  }
}
