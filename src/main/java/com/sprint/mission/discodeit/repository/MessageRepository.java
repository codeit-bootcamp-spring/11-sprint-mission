package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Domain.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository {
    UUID create(Message message);

    // 단건 조회
    Message read(UUID id);

    // 전체 조회
    List<Message> readAll();

    // 삭제
    void delete(UUID id);

    // 복구
    void restore(UUID id);
}
