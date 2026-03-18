package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;

import java.util.*;

public class JCFUserStatusRepository implements UserStatusRepository {
    private final Map<UUID, UserStatus> data = new HashMap<>();

    @Override
    public void save(UserStatus status) {
        data.put(status.getId(), status);
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return data.values().stream()
                .filter(status -> status.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public List<UserStatus> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void deleteByUserId(UUID userId) {
        data.entrySet()
                .removeIf(entry -> entry.getValue().getUserId().equals(userId));
    }
}
