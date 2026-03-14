package com.sprint.mission.discodeit.repository.interaction;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class BasicReadStatusRepository implements ReadStatusRepository {
    private final List<ReadStatus> store = new ArrayList<>();

    @Override
    public List<ReadStatus> findAllByChannelId(UUID id) {
        return store.stream()
                .filter(status -> status.getChannelId().equals(id))
                .toList();
    }

    public void deleteAllByChannelId(UUID channelId) {
        store.removeIf(status -> status.getChannelId().equals(channelId));
    }
}
