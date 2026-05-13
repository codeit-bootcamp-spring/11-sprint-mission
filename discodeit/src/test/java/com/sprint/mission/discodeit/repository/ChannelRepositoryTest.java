package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@EnableJpaAuditing
@ActiveProfiles("test")
@DataJpaTest
class ChannelRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private ChannelRepository channelRepository;

  private User user;
  private String channelName;
  private Channel publicChannel;
  private Channel privateChannelWithAccess;
  private Channel privateChannelWithoutAccess;

  @BeforeEach
  void setUp() {
    user = new User("tester", "tester@example.io", "qwerty", null);
    entityManager.persist(user);

    channelName = "general";
    publicChannel = new Channel(channelName, "공개 채널");
    entityManager.persist(publicChannel);

    privateChannelWithAccess = new Channel();
    entityManager.persist(privateChannelWithAccess);
    entityManager.persist(new ReadStatus(user, privateChannelWithAccess, Instant.now()));

    privateChannelWithoutAccess = new Channel();
    entityManager.persist(privateChannelWithoutAccess);

    entityManager.flush();
  }

  @Nested
  @DisplayName("exists by name")
  class ExistsByName {

    @Test
    @DisplayName("return true with existing name")
    void existsByName_ExistingName_ReturnsTrue() {
      // when
      boolean exists = channelRepository.existsByName(channelName);

      // then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("return false with non existing name")
    void existsByName_NonExistingName_ReturnsFalse() {
      // when
      boolean exists = channelRepository.existsByName("unknown");

      // then
      assertThat(exists).isFalse();
    }
  }

  @Nested
  @DisplayName("find all by user id")
  class FindAllByUserId {

    @Test
    @DisplayName("return public and joined private channels")
    void findAllByUserId_ReturnsPublicAndJoinedPrivateChannels() {
      // when
      List<Channel> foundChannels = channelRepository.findAllByUserId(user.getId());

      // then
      assertThat(foundChannels).extracting(Channel::getId)
          .containsExactlyInAnyOrder(publicChannel.getId(), privateChannelWithAccess.getId());
    }

    @Test
    @DisplayName("return public channels for any user")
    void findAllByUserId_ReturnsPublicChannelForAnyUser() {
      // when
      List<Channel> foundChannels = channelRepository.findAllByUserId(UUID.randomUUID());

      // then
      assertThat(foundChannels).extracting(Channel::getId)
          .contains(publicChannel.getId());
    }

    @Test
    @DisplayName("exclude private channels without access")
    void findAllByUserId_ExcludesPrivateChannelWithoutAccess() {
      // when
      List<Channel> foundChannels = channelRepository.findAllByUserId(user.getId());

      // then
      assertThat(foundChannels).extracting(Channel::getId)
          .doesNotContain(privateChannelWithoutAccess.getId());
    }
  }
}