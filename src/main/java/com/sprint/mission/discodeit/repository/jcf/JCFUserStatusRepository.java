package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;
@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFUserStatusRepository implements UserStatusRepository {

    private final Map<UUID, UserStatus> data;
    public JCFUserStatusRepository(){
        data = new HashMap<>();
    }

    @Override
    public boolean saveUserStatus(UserStatus userStatus) {

        data.put(userStatus.getUserId(), userStatus);
        return true;

    }

    @Override
    public Optional<UserStatus> getUserStatus(UUID userId) {
        return Optional.ofNullable(data.get(userId));
    }

    @Override
    public List<UserStatus> getAllUserStatus() {
        return data.values().stream().toList();
    }

    @Override
    public boolean deleteUserStatus(UUID userId) {
        return data.remove(userId) != null;
    }

    @Override
    public boolean isExistUserStatus(UUID userId) {
        return data.containsKey(userId);
    }
}
