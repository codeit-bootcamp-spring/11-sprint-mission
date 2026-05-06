package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.projection.ChannelLastMessageAtProjection;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepo;

    @Autowired
    private ChannelRepository channelRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BinaryContentRepository binaryContentRepo;

    @Test
    void findLastMessageAtByChannel_success() {
        Channel channel = savePublicChannel();
        User author = saveUser("taehk23", "taehk23@test.com");

        Message message = messageRepo.save(new Message(
                "hello",
                channel,
                author,
                List.of()
        ));

        Optional<Instant> result = messageRepo.findLastMessageAtByChannel(channel);

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(message.getCreatedAt());
    }

    @Test
    void findLastMessageAtByChannel_fail_whenMessageNotFound() {
        Channel channel = savePublicChannel();

        Optional<Instant> result = messageRepo.findLastMessageAtByChannel(channel);

        assertThat(result).isEmpty();
    }

    @Test
    void findLastMessageAtByChannels_success() {
        Channel channel1 = savePublicChannel();
        Channel channel2 = channelRepo.save(new Channel(
                ChannelType.PUBLIC,
                "random",
                "random channel"
        ));
        User author = saveUser("taehk23", "taehk23@test.com");

        Message message1 = messageRepo.save(new Message(
                "channel1 message",
                channel1,
                author,
                List.of()
        ));
        Message message2 = messageRepo.save(new Message(
                "channel2 message",
                channel2,
                author,
                List.of()
        ));

        List<ChannelLastMessageAtProjection> result =
                messageRepo.findLastMessageAtByChannels(List.of(channel1, channel2));

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(ChannelLastMessageAtProjection::channelId)
                .containsExactlyInAnyOrder(channel1.getId(), channel2.getId());
        assertThat(result)
                .extracting(ChannelLastMessageAtProjection::lastMessageAt)
                .contains(message1.getCreatedAt(), message2.getCreatedAt());
    }

    @Test
    void findLastMessageAtByChannels_fail_whenMessagesNotFound() {
        Channel channel = savePublicChannel();

        List<ChannelLastMessageAtProjection> result =
                messageRepo.findLastMessageAtByChannels(List.of(channel));

        assertThat(result).isEmpty();
    }

    @Test
    void findAllWithAttachmentsByChannel_success() {
        Channel channel = savePublicChannel();
        User author = saveUser("taehk23", "taehk23@test.com");

        BinaryContent attachment = binaryContentRepo.save(new BinaryContent(
                "image.png",
                "image/png",
                1000L
        ));

        Message message = messageRepo.save(new Message(
                "hello",
                channel,
                author,
                List.of(attachment)
        ));

        List<Message> result = messageRepo.findAllWithAttachmentsByChannel(channel);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(message.getId());
        assertThat(result.get(0).getAttachments()).hasSize(1);
        assertThat(result.get(0).getAttachments().get(0).getFileName()).isEqualTo("image.png");
    }

    @Test
    void findAllWithAttachmentsByChannel_fail_whenChannelHasNoMessage() {
        Channel channel = savePublicChannel();

        List<Message> result = messageRepo.findAllWithAttachmentsByChannel(channel);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllByChannelWithCursor_success_withoutCursor() {
        Channel channel = savePublicChannel();
        User author = saveUser("taehk23", "taehk23@test.com");

        Message message1 = messageRepo.save(new Message(
                "first",
                channel,
                author,
                List.of()
        ));
        Message message2 = messageRepo.save(new Message(
                "second",
                channel,
                author,
                List.of()
        ));

        List<Message> result = messageRepo.findAllByChannelWithCursor(
                channel,
                null,
                50
        );

        assertThat(result).hasSize(2);
        assertThat(result)
                .extracting(Message::getId)
                .containsExactly(message2.getId(), message1.getId());
    }

    @Test
    void findAllByChannelWithCursor_success_withCursor() {
        Channel channel = savePublicChannel();
        User author = saveUser("taehk23", "taehk23@test.com");

        Message oldMessage = messageRepo.save(new Message(
                "old",
                channel,
                author,
                List.of()
        ));
        Message cursorMessage = messageRepo.save(new Message(
                "cursor",
                channel,
                author,
                List.of()
        ));
        Message newMessage = messageRepo.save(new Message(
                "new",
                channel,
                author,
                List.of()
        ));

        List<Message> result = messageRepo.findAllByChannelWithCursor(
                channel,
                cursorMessage.getCreatedAt(),
                50
        );

        assertThat(result)
                .extracting(Message::getId)
                .contains(oldMessage.getId());
        assertThat(result)
                .extracting(Message::getId)
                .doesNotContain(cursorMessage.getId(), newMessage.getId());
    }

    @Test
    void findAllByChannelWithCursor_fail_whenChannelHasNoMessage() {
        Channel channel = savePublicChannel();

        List<Message> result = messageRepo.findAllByChannelWithCursor(
                channel,
                null,
                50
        );

        assertThat(result).isEmpty();
    }

    @Test
    void findWithDetailsById_success() {
        Channel channel = savePublicChannel();
        User author = saveUser("taehk23", "taehk23@test.com");

        BinaryContent attachment = binaryContentRepo.save(new BinaryContent(
                "image.png",
                "image/png",
                1000L
        ));

        Message message = messageRepo.save(new Message(
                "hello",
                channel,
                author,
                List.of(attachment)
        ));

        Optional<Message> result = messageRepo.findWithDetailsById(message.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("hello");
        assertThat(result.get().getChannel().getId()).isEqualTo(channel.getId());
        assertThat(result.get().getAuthor().getId()).isEqualTo(author.getId());
        assertThat(result.get().getAttachments()).hasSize(1);
    }

    @Test
    void findWithDetailsById_fail_whenMessageNotFound() {
        UUID unknownMessageId = UUID.randomUUID();

        Optional<Message> result = messageRepo.findWithDetailsById(unknownMessageId);

        assertThat(result).isEmpty();
    }

    private Channel savePublicChannel() {
        return channelRepo.save(new Channel(
                ChannelType.PUBLIC,
                "general",
                "general channel"
        ));
    }

    private User saveUser(String username, String email) {
        return userRepo.save(new User(
                username,
                email,
                "password",
                null
        ));
    }
}
