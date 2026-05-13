package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
public class ChannelRepositoryTest {

  @Autowired
  private ChannelRepository channelRepository;

  @Test
  @DisplayName("ID로 채널 조회 - 존재하는 경우")
  void findById_found() {
    Channel channel = new Channel(ChannelType.PUBLIC, "코드잇", "스프린트 미션7");
    Channel savedChannel = channelRepository.save(channel);

    Optional<Channel> foundChannel = channelRepository.findById(savedChannel.getId());

    assertThat(foundChannel).isPresent();
    assertThat(foundChannel.get().getName()).isEqualTo("코드잇");
  }

  @Test
  @DisplayName("ID로 채널 조회 - 존재하지 않는 경우")
  void findById_notFound() {
    UUID randomId = UUID.randomUUID();

    Optional<Channel> foundChannel = channelRepository.findById(randomId);

    assertThat(foundChannel).isEmpty();
  }
}
