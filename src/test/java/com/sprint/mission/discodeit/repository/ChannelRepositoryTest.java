package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Channel.ChannelType;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class ChannelRepositoryTest {

  @Autowired
  private ChannelRepository channelRepository;

  // ───── findAllByType ─────

  @Test
  void findAllByType_PUBLIC_반환() {
    // given
    channelRepository.save(new Channel(ChannelType.PUBLIC, "general", "공용 채널"));
    channelRepository.save(new Channel(ChannelType.PUBLIC, "random", "랜덤 채널"));
    channelRepository.save(new Channel(ChannelType.PRIVATE, null, null));

    // when
    List<Channel> result = channelRepository.findAllByType(ChannelType.PUBLIC);

    // then
    assertThat(result).hasSize(2);
    assertThat(result).allMatch(c -> c.getType() == ChannelType.PUBLIC);
  }

  @Test
  void findAllByType_PRIVATE_반환() {
    // given
    channelRepository.save(new Channel(ChannelType.PUBLIC, "general", "공용 채널"));
    channelRepository.save(new Channel(ChannelType.PRIVATE, null, null));
    channelRepository.save(new Channel(ChannelType.PRIVATE, null, null));

    // when
    List<Channel> result = channelRepository.findAllByType(ChannelType.PRIVATE);

    // then
    assertThat(result).hasSize(2);
    assertThat(result).allMatch(c -> c.getType() == ChannelType.PRIVATE);
  }

  @Test
  void findAllByType_없는경우_빈목록() {
    // given
    channelRepository.save(new Channel(ChannelType.PUBLIC, "general", "공용 채널"));

    // when
    List<Channel> result = channelRepository.findAllByType(ChannelType.PRIVATE);

    // then
    assertThat(result).isEmpty();
  }

  // ───── 페이징 ─────

  @Test
  void findAll_페이징() {
    // given
    channelRepository.save(new Channel(ChannelType.PUBLIC, "aaa", "채널1"));
    channelRepository.save(new Channel(ChannelType.PUBLIC, "bbb", "채널2"));
    channelRepository.save(new Channel(ChannelType.PUBLIC, "ccc", "채널3"));

    // when
    List<Channel> result = channelRepository.findAll(
        org.springframework.data.domain.PageRequest.of(0, 2)
    ).getContent();

    // then
    assertThat(result).hasSize(2);
  }

  @Test
  void findAll_페이징_두번째페이지() {
    // given
    channelRepository.save(new Channel(ChannelType.PUBLIC, "aaa", "채널1"));
    channelRepository.save(new Channel(ChannelType.PUBLIC, "bbb", "채널2"));
    channelRepository.save(new Channel(ChannelType.PUBLIC, "ccc", "채널3"));

    // when
    List<Channel> result = channelRepository.findAll(
        org.springframework.data.domain.PageRequest.of(1, 2)
    ).getContent();

    // then
    assertThat(result).hasSize(1);
  }
}