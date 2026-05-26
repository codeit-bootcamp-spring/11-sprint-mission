package com.sprint.mission.discodeit.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.mission.discodeit.exception.channel.PrivateChannelUpdateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Channel 도메인 단위 테스트")
class ChannelTest {

  @Test
  @DisplayName("퍼블릭 채널은 정보를 수정할 수 있다")
  void updatePublicInfo_success() {
    // Given
    Channel publicChannel = Channel.createPublic("옛날 이름", "옛날 설명");

    // When
    publicChannel.updatePublicInfo("새 이름", "새 설명");

    // Then
    assertThat(publicChannel.getName()).isEqualTo("새 이름");
    assertThat(publicChannel.getDescription()).isEqualTo("새 설명");
  }

  @Test
  @DisplayName("프라이빗 채널 수정 시도 시 PrivateChannelUpdateException 발생")
  void updatePublicInfo_fail_privateChannel() {
    // Given
    Channel privateChannel = Channel.createPrivate();

    // When & Then
    assertThatThrownBy(() -> privateChannel.updatePublicInfo("새 이름", "새 설명"))
        .isInstanceOf(PrivateChannelUpdateException.class);
  }
}