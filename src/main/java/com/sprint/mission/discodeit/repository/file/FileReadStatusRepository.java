package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FileReadStatusRepository extends CommonFileRepository<ReadStatus> implements ReadStatusRepository {
    public FileReadStatusRepository() {
        super("readstatuses", ReadStatus.class);
    }
}
