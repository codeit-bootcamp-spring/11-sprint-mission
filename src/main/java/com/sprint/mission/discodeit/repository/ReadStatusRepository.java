package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReadStatusRepository {
    public void init();
    public void save(ReadStatus readStatus);
    public Optional<ReadStatus> findById(UUID id);
    public List<ReadStatus> findAll();
    public void delete(ReadStatus readStatus);
}
