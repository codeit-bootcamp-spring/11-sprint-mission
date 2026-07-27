package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.SseMessage;
import java.util.List;
import java.util.UUID;

public interface SseMessageRepository {

  void save(UUID eventId, SseMessage message);

  List<SseMessage> findEventsAfter(UUID lastEventId);
}