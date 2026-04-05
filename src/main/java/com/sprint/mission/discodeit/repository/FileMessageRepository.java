package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.FileMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileMessageRepository {
    Optional<FileMessage> findById(UUID id);

    List<FileMessage> findAllByIds(List<UUID> ids);

    void save(FileMessage fileMessage);

    void deleteById(UUID id);
}
