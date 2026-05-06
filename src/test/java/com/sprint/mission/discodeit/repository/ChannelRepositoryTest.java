package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class ChannelRepositoryTest {

    @Autowired
    private ChannelRepository channelRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ReadStatusRepository readStatusRepo;

    @Test
    void findAllByChannelType_success() {

        Channel publicChannel = new Channel(
                ChannelType.PUBLIC,
                "general",
                "general channel"
        );

        Channel privateChannel = new Channel(
                ChannelType.PRIVATE,
                null,
                null
        );

        channelRepo.save(publicChannel);
        channelRepo.save(privateChannel);

        List<Channel> result = channelRepo.findAllByChannelType(ChannelType.PUBLIC);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChannelType()).isEqualTo(ChannelType.PUBLIC);
        assertThat(result.get(0).getName()).isEqualTo("general");
    }

    @Test
    void findAllByChannelType_fail_whenPublicChannelNotFound() {
        Channel privateChannel = new Channel(
                ChannelType.PRIVATE,
                null,
                null
        );
        channelRepo.save(privateChannel);

        List<Channel> result = channelRepo.findAllByChannelType(ChannelType.PUBLIC);

        assertThat(result).isEmpty();
    }

    @Test
    void findPrivateChannelsByUser_success() {

        User user = new User(
                "taehk23",
                "taehk23@test.com",
                "password",
                null
        );
        userRepo.save(user);

        Channel privateChannel = new Channel(
                ChannelType.PRIVATE,
                null,
                null
        );
        channelRepo.save(privateChannel);

        ReadStatus readStatus = new ReadStatus(
                user,
                privateChannel,
                Instant.now()
        );
        readStatusRepo.save(readStatus);

        List<Channel> result = channelRepo.findPrivateChannelsByUser(user);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(privateChannel.getId());
        assertThat(result.get(0).getChannelType()).isEqualTo(ChannelType.PRIVATE);
    }

    @Test
    void findPrivateChannelsByUser_fail_whenUserHasNoChannel() {
        User user = new User(
                "taehk23",
                "taehk23@test.com",
                "password",
                null
        );
        userRepo.save(user);

        Channel privateChannel = new Channel(
                ChannelType.PRIVATE,
                null,
                null
        );
        channelRepo.save(privateChannel);

        List<Channel> result = channelRepo.findPrivateChannelsByUser(user);

        assertThat(result).isEmpty();
    }
}
