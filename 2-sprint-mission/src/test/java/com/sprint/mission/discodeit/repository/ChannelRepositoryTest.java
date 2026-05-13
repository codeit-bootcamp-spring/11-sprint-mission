package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
class ChannelRepositoryTest {

  @Autowired
  private ChannelRepository channelRepository;

  @Autowired
  private TestEntityManager entityManager;

  private Channel createTestChannel(ChannelType type, String name) {
    Channel channel = Channel.builder()
        .type(type)
        .name(name)
        .description("설명")
        .build();
    entityManager.persist(channel);
    return channel;
  }

  // findAllByTypeOrIdIn 테스트
  @Test
  @DisplayName("지정된 타입 혹은 내 채널 ID 목록에 속한 채널 조회 - 성공")
  void findAllByTypeOrIdIn_publicOrJoinedPrivate_returnsChannels() {
    // Given
    Channel publicChannel1 = createTestChannel(ChannelType.PUBLIC, "공개채널1");
    Channel publicChannel2 = createTestChannel(ChannelType.PUBLIC, "공개채널2");
    Channel privateChannel1 = createTestChannel(ChannelType.PRIVATE, "비공개채널1");
    Channel privateChannel2 = createTestChannel(ChannelType.PRIVATE, "비공개채널2");

    entityManager.flush();
    entityManager.clear();

    List<UUID> selectedPrivateIds = List.of(privateChannel1.getId());

    // When
    List<Channel> foundChannels = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC,
        selectedPrivateIds);

    // Then
    assertThat(foundChannels).hasSize(3);

    assertThat(
        foundChannels.stream().filter(c -> c.getType() == ChannelType.PUBLIC).count()).isEqualTo(2);

    List<Channel> privateChannels = foundChannels.stream()
        .filter(c -> c.getType() == ChannelType.PRIVATE)
        .toList();
    assertThat(privateChannels).hasSize(1);
    assertThat(privateChannels.get(0).getId()).isEqualTo(privateChannel1.getId());
  }

  @Test
  @DisplayName("조건에 부합하는 채널이 없을 때 - 빈 리스트 반환")
  void findAllByTypeOrIdIn_noMatchingChannels_returnsEmpty() {
    // Given
    createTestChannel(ChannelType.PRIVATE, "비공개채널1");
    createTestChannel(ChannelType.PRIVATE, "비공개채널2");

    entityManager.flush();
    entityManager.clear();

    // When
    List<Channel> foundChannels = channelRepository.findAllByTypeOrIdIn(ChannelType.PUBLIC,
        List.of());

    // Then
    assertThat(foundChannels).isEmpty();
  }
}