package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.repository.base.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FileUserStatusRepository extends FileRepository<UserStatus> implements UserStatusRepository {

    private final Map<UUID, UUID> userIdIndex = new HashMap<>();

    protected FileUserStatusRepository(@Value("${app.data.userstatus-path}") String filePath) {
        super(filePath);
        postLoad();
    }

    @Override
    protected void postLoad() {
        for (UserStatus us : dataMap.values()) {
            addToIndex(us);
        }
    }

    @Override
    protected void postSave(UserStatus newEntity, UserStatus oldEntity) {
        if (oldEntity != null) {
            postDelete(oldEntity);
        }

        addToIndex(newEntity);
    }

    @Override
    protected void postDelete(UserStatus entity) {
        removeFromIndex(entity);
    }

    private void addToIndex(UserStatus newEntity) {
        userIdIndex.put(newEntity.getUserId(), newEntity.getId());
    }

    private void removeFromIndex(UserStatus entity) {
        userIdIndex.remove(entity.getUserId());
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        readLock.lock();
        try {
            UUID targetUserStatusId = userIdIndex.get(userId);
            if (targetUserStatusId != null) {
                UserStatus userStatus = dataMap.get(targetUserStatusId);
                if (userStatus != null) {
                    return Optional.of ((UserStatus) userStatus.copy());
                }
            }
            return Optional.empty();
        } finally {
            readLock.unlock();
        }
    }
}
