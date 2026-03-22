package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// @Repository
public class JCFUserStatusRepository implements UserStatusRepository {

    private final Map<UUID, UserStatus> userStatusData = new ConcurrentHashMap<>();

    @Override
    public void save(UserStatus userStatus) {
        userStatusData.put(userStatus.getId(), userStatus);
    }

    @Override
    public UserStatus findById(UUID id) {
        UserStatus userStatus = userStatusData.get(id);
        if (userStatus != null && userStatus.isDeleted()) {
            return null;
        }
        return userStatus;
    }

    @Override
    public List<UserStatus> findAll() {
        if (userStatusData.isEmpty()) {
            return new ArrayList<>();
        }
        return userStatusData.values().stream()
                .filter(userStatus -> !userStatus.isDeleted())
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        UserStatus userStatus = findById(id);
        if (userStatus != null) {
            userStatus.softDelete();
            save(userStatus);
        }
    }
}