package com.sprint.mission.discodeit.sse;

import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Repository
public class SseMessageRespository {

    private static final int MAX_SIZE = 1000;

    private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

    public void save(SseMessage message) {
        eventIdQueue.addLast(message.id());
        messages.put(message.id(), message);
        while (eventIdQueue.size() > MAX_SIZE) {
            UUID oldestId = eventIdQueue.pollFirst();
            if (oldestId != null) {
                messages.remove(oldestId);
            }
        }
    }

    public List<SseMessage> findAllAfter(UUID lastEventId, UUID receiverId) {
        List<UUID> ids = eventIdQueue.stream().toList();
        int lastIndex = lastEventId == null ? -1 : ids.indexOf(lastEventId);
        int startIndex = Math.max(lastIndex + 1, 0);

        return ids.subList(startIndex, ids.size()).stream()
                .map(messages::get)
                .filter(Objects::nonNull)
                .filter(message -> isReceivable(message, receiverId))
                .toList();
    }

    private boolean isReceivable(SseMessage message, UUID receiverId) {
        Set<UUID> receiverIds = message.receiverIds();
        return receiverIds == null || receiverIds.isEmpty() || receiverIds.contains(receiverId);
    }
}
