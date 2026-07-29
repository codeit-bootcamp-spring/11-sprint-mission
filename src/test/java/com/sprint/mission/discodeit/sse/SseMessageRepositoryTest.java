package com.sprint.mission.discodeit.sse;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SseMessageRepositoryTest {

    private final SseMessageRepository messageRepository =
            new SseMessageRepository();

    @Test
    void findAllAfter_returnsMissedMessagesForReceiver() {
        UUID receiverId = UUID.randomUUID();
        SseMessage lastReceivedMessage = new SseMessage(
                UUID.randomUUID(),
                Set.of(receiverId),
                "notifications.created",
                "first"
        );
        SseMessage otherUserMessage = new SseMessage(
                UUID.randomUUID(),
                Set.of(UUID.randomUUID()),
                "notifications.created",
                "other"
        );
        SseMessage broadcastMessage = new SseMessage(
                UUID.randomUUID(),
                Set.of(),
                "users.updated",
                "broadcast"
        );

        messageRepository.save(lastReceivedMessage);
        messageRepository.save(otherUserMessage);
        messageRepository.save(broadcastMessage);

        List<SseMessage> result = messageRepository.findAllAfter(
                lastReceivedMessage.id(),
                receiverId
        );

        assertThat(result).containsExactly(broadcastMessage);
    }
}
