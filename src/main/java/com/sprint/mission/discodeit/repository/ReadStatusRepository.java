package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.ReadStatus;

import java.util.List;

public interface ReadStatusRepository {
    void save(ReadStatus readStatus);
    ReadStatus findById(Long id);
    List<ReadStatus> findAll();
    void delete(ReadStatus readStatus);
}
