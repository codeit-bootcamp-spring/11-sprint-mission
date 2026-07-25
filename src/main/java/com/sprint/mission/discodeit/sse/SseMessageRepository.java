package com.sprint.mission.discodeit.sse;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Repository
public class SseMessageRepository {

    private static final int MAX_MESSAGE_COUNT = 1000;

    private final ConcurrentLinkedDeque<UUID> eventIdQueue =
            new ConcurrentLinkedDeque<>();
    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

    public void save(SseMessage message) {
        eventIdQueue.addLast(message.id());
        messages.put(message.id(), message);

        while (eventIdQueue.size() > MAX_MESSAGE_COUNT) {
            UUID oldestEventId = eventIdQueue.pollFirst();
            if (oldestEventId != null) {
                messages.remove(oldestEventId);
            }
        }
    }

    public List<SseMessage> findAllAfter(UUID lastEventId, UUID receiverId) {
        if (lastEventId == null || !messages.containsKey(lastEventId)) {
            return List.of();
        }

        boolean foundLastEvent = false;
        List<SseMessage> result = new ArrayList<>();

        for (UUID eventId : eventIdQueue) {
            if (!foundLastEvent) {
                foundLastEvent = eventId.equals(lastEventId);
                continue;
            }

            SseMessage message = messages.get(eventId);
            if (message != null
                    && (message.receiverIds().isEmpty()
                    || message.receiverIds().contains(receiverId))) {
                result.add(message);
            }
        }

        return result;
    }
}
