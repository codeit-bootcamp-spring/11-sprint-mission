package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "file"
)
public class FileReadStatusRepository extends CommonFileRepository<ReadStatus> implements ReadStatusRepository {
    public FileReadStatusRepository(
            @Value("${discodeit.repository.file.base-dir}") String basedir
    ) {
        super("readstatuses", ReadStatus.class, basedir);
    }

    public Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID channelId) {
        return findAll().stream()
                .filter(p -> p.getUserId().equals(userId))
                .filter(p -> p.getChannelId().equals(channelId))
                .findFirst();
    }
}
