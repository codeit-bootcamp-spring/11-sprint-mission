package com.sprint.mission.discodeit.channel;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.config.JpaAuditingConfig;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaAuditingConfig.class)
public class ChannelRepositoryTest {

  @Autowired
  private ChannelRepository channelRepository;

  @Test
  @DisplayName("타입 또는 channelId 목록으로 채널 조회 성공")
  void findByTypeOrIdIn_success() {
    //given
    Channel publicChannel = new Channel("채팅방1", "채팅방", ChannelType.PUBLIC);
    Channel privateChannel = new Channel("채팅방2", "채팅방", ChannelType.PRIVATE);
    channelRepository.saveAll(List.of(publicChannel, privateChannel));

    List<UUID> uuidList = List.of(privateChannel.getId());

    //when
    List<Channel> result = channelRepository.findByTypeOrIdIn(ChannelType.PUBLIC, uuidList);

    //then
    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
  }
}
