package com.sprint.mission.discodeit.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@DataJpaTest
class ReadStatusRepositoryTest {

  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private ReadStatusRepository readStatusRepository;

  private User user;
  private User otherUser;
  private Channel channel;
  private Channel otherChannel;
  private ReadStatus readStatus;

  @BeforeEach
  void setUp() {
    user = new User("tester", "tester@example.io", "qwerty", null);
    entityManager.persist(user);

    otherUser = new User("other", "other@example.io", "qwerty", null);
    entityManager.persist(otherUser);

    channel = new Channel();
    entityManager.persist(channel);

    otherChannel = new Channel();
    entityManager.persist(otherChannel);

    readStatus = new ReadStatus(user, channel, Instant.now());
    entityManager.persistAndFlush(readStatus);
  }

  @Nested
  @DisplayName("find all by user id")
  class FindAllByUserId {

    @Test
    @DisplayName("return read statuses with existing user id")
    void findAllByUserId_ExistingUserId_ReturnsReadStatuses() {
      // when
      List<ReadStatus> foundStatuses = readStatusRepository.findAllByUserId(user.getId());

      // then
      assertThat(foundStatuses).hasSize(1);
      assertThat(foundStatuses.get(0).getId()).isEqualTo(readStatus.getId());
    }

    @Test
    @DisplayName("return empty list with non existing user id")
    void findAllByUserId_NonExistingUserId_ReturnsEmpty() {
      // when
      List<ReadStatus> foundStatuses = readStatusRepository.findAllByUserId(otherUser.getId());

      // then
      assertThat(foundStatuses).isEmpty();
    }
  }

  @Nested
  @DisplayName("find all by channel")
  class FindAllByChannel {

    @Test
    @DisplayName("return read statuses with existing channel")
    void findAllByChannel_ExistingChannel_ReturnsReadStatuses() {
      // when
      List<ReadStatus> foundStatuses = readStatusRepository.findAllByChannel(channel);

      // then
      assertThat(foundStatuses).hasSize(1);
      assertThat(foundStatuses.get(0).getId()).isEqualTo(readStatus.getId());
    }

    @Test
    @DisplayName("return empty list with non existing channel")
    void findAllByChannel_NonExistingChannel_ReturnsEmpty() {
      // when
      List<ReadStatus> foundStatuses = readStatusRepository.findAllByChannel(otherChannel);

      // then
      assertThat(foundStatuses).isEmpty();
    }
  }

  @Nested
  @DisplayName("find all by channel in")
  class FindAllByChannelIn {

    @Test
    @DisplayName("return read statuses for multiple channels")
    void findAllByChannelIn_MultipleChannels_ReturnsReadStatuses() {
      // given
      entityManager.persistAndFlush(new ReadStatus(otherUser, otherChannel, Instant.now()));

      // when
      List<ReadStatus> foundStatuses = readStatusRepository.findAllByChannelIn(
          List.of(channel, otherChannel));

      // then
      assertThat(foundStatuses).hasSize(2);
    }

    @Test
    @DisplayName("return empty list with empty channel list")
    void findAllByChannelIn_EmptyChannels_ReturnsEmpty() {
      // when
      List<ReadStatus> foundStatuses = readStatusRepository.findAllByChannelIn(List.of());

      // then
      assertThat(foundStatuses).isEmpty();
    }
  }

  @Nested
  @DisplayName("exists by user and channel")
  class ExistsByUserAndChannel {

    @Test
    @DisplayName("return true with existing user and channel")
    void existsByUserAndChannel_ExistingUserAndChannel_ReturnsTrue() {
      // when
      boolean exists = readStatusRepository.existsByUserAndChannel(user, channel);

      // then
      assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("return false with non existing user and channel")
    void existsByUserAndChannel_NonExistingUserAndChannel_ReturnsFalse() {
      // when
      boolean exists = readStatusRepository.existsByUserAndChannel(otherUser, channel);

      // then
      assertThat(exists).isFalse();
    }
  }

  @Nested
  @DisplayName("delete all by channel")
  class DeleteAllByChannel {

    @Test
    @DisplayName("delete all read statuses in channel")
    void deleteAllByChannel_DeletesAllReadStatusesInChannel() {
      // when
      readStatusRepository.deleteAllByChannel(channel);
      entityManager.flush();
      entityManager.clear();

      // then
      assertThat(readStatusRepository.findAllByChannel(channel)).isEmpty();
    }
  }

  @Nested
  @DisplayName("delete all by user")
  class DeleteAllByUser {

    @Test
    @DisplayName("delete all read statuses for user")
    void deleteAllByUser_DeletesAllReadStatusesForUser() {
      // when
      readStatusRepository.deleteAllByUser(user);
      entityManager.flush();
      entityManager.clear();

      // then
      assertThat(readStatusRepository.findAllByUserId(user.getId())).isEmpty();
    }
  }
}