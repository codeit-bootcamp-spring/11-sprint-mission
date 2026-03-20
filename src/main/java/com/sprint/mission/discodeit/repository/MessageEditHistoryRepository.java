package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.MessageEditHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageEditHistoryRepository {
    void save(MessageEditHistory history);
    Optional<MessageEditHistory> findById(UUID id);
    List<MessageEditHistory> findAll();
    void deleteById(UUID id);
}
