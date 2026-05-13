package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@EnableJpaAuditing
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ChannelRepositoryTest {

    @Autowired
    ChannelRepository channelRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ReadStatusRepository readStatusRepository;

    @Test
    @DisplayName("사용자는 PUBLIC 채널과 자신이 참여한 PRIVATE 채널을 조회할 수 있다")
    void findVisibleChannelsByUserId_success() {
        // given
        User user = userRepository.saveAndFlush(
                new User("evan", "evan@test.com", "password123")
        );

        User otherUser = userRepository.saveAndFlush(
                new User("kim", "kim@test.com", "password123")
        );

        Channel publicChannel = channelRepository.saveAndFlush(
                new Channel("general", "public channel")
        );

        Channel myPrivateChannel = channelRepository.saveAndFlush(
                Channel.createPrivateChannel()
        );

        Channel otherPrivateChannel = channelRepository.saveAndFlush(
                Channel.createPrivateChannel()
        );

        readStatusRepository.saveAndFlush(
                new ReadStatus(user, myPrivateChannel, Instant.now())
        );

        readStatusRepository.saveAndFlush(
                new ReadStatus(otherUser, otherPrivateChannel, Instant.now())
        );

        // when
        List<Channel> result = channelRepository.findVisibleChannelsByUserId(user.getId());

        // then
        assertThat(result)
                .extracting(Channel::getId)
                .contains(publicChannel.getId(), myPrivateChannel.getId())
                .doesNotContain(otherPrivateChannel.getId());
    }

    @Test
    @DisplayName("참여한 PRIVATE 채널이 없어도 PUBLIC 채널은 조회된다")
    void findVisibleChannelsByUserId_onlyPublic() {
        // given
        UUID userId = UUID.randomUUID();

        Channel publicChannel = channelRepository.saveAndFlush(
                new Channel("general", "public channel")
        );

        Channel privateChannel = channelRepository.saveAndFlush(
                Channel.createPrivateChannel()
        );

        // when
        List<Channel> result = channelRepository.findVisibleChannelsByUserId(userId);

        // then
        assertThat(result)
                .extracting(Channel::getId)
                .contains(publicChannel.getId())
                .doesNotContain(privateChannel.getId());
    }

    @Test
    @DisplayName("채널을 저장하면 id와 생성 시간이 생성된다")
    void save_success() {
        // given
        Channel channel = new Channel("notice", "notice channel");

        // when
        Channel savedChannel = channelRepository.saveAndFlush(channel);

        // then
        assertThat(savedChannel.getId()).isNotNull();
        assertThat(savedChannel.getCreatedAt()).isNotNull();
        assertThat(savedChannel.getUpdatedAt()).isNotNull();
        assertThat(savedChannel.getName()).isEqualTo("notice");
    }
}