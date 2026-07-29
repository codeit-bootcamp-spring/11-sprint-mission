package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.NotificationCreator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class NotificationRequiredTopicListenerTest {

    private final NotificationCreator notificationCreator =
            mock(NotificationCreator.class);
    private final NotificationRequiredTopicListener topicListener =
            new NotificationRequiredTopicListener(new ObjectMapper(), notificationCreator);

    @Test
    void onMessageCreatedEvent_skipsInvalidPayload() {
        assertThatCode(() -> topicListener.onMessageCreatedEvent("invalid-json"))
                .doesNotThrowAnyException();
        verifyNoInteractions(notificationCreator);
    }
}
