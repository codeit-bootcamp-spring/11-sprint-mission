package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        name = "discodeit.repository.type",
        havingValue = "jcf",
        matchIfMissing = true
)
public class JCFMessageRepository extends CommonJCFRepository<Message> implements MessageRepository {
    public JCFMessageRepository() {
        super();
    }

    @Override
    public Optional<Instant> findLastMessageAtByChannelId(UUID id) {
        return findAll().stream()
                .filter(p -> (p.getChannelId().equals(id)))
                .map(Message::getCreatedAt)
                .max(Comparator.naturalOrder());
    }
}
