package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Override
    public Optional<ReadStatus> findByUserIdAndChannelId(UUID userId, UUID channelId) {
        return findAll().stream()
                .filter(p -> p.getUser().getId().equals(userId))
                .filter(p -> p.getChannel().getId().equals(channelId))
                .findFirst();
    }

    @Override
    public List<UUID> findUserIdsByChannelId(UUID channelId) {
        return findAll().stream()
                .filter(p -> (p.getChannel().getId().equals(channelId)))
                .map(ReadStatus::getUser)
                .map(User::getId)
                .toList();
    }

}
