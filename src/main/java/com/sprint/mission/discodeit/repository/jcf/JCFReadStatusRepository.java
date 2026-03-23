package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "jcf",
        matchIfMissing = true
)
public class JCFReadStatusRepository extends CommonJCFRepository<ReadStatus> implements ReadStatusRepository {
    public JCFReadStatusRepository() {
        super();
    }

    public Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID channelId) {
        return findAll().stream()
                .filter(p -> p.getUserId().equals(userId))
                .filter(p -> p.getChannelId().equals(channelId))
                .findFirst();
    }
}
