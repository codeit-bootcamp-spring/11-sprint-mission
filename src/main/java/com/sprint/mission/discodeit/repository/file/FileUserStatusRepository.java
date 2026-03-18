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

    // userid : userstatusid
    private final Map<UUID, UUID> userIdIndex = new HashMap<>();

    protected FileUserStatusRepository(@Value("${app.data.userstatus-path}") String filePath) {
        super(filePath);
        postLoad();
    }

    @Override
    protected void postLoad() {
        for (UserStatus us : dataMap.values()) {
            userIdIndex.put(us.getUserId(), us.getId());
        }
    }

    @Override
    protected void postSave(UserStatus newEntity, UserStatus oldEntity) {
        if (oldEntity != null) {
            postDelete(oldEntity);
        }

        userIdIndex.put(newEntity.getUserId(), newEntity.getId());
    }

    @Override
    protected void postDelete(UserStatus entity) {
        userIdIndex.remove(entity.getUserId());
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        readLock.lock();
        try {
            UUID targetUserStatusId = userIdIndex.get(userId);
            if (targetUserStatusId != null) {
                return super.findById(targetUserStatusId);
            }
            return Optional.empty();
        } finally {
            readLock.unlock();
        }
    }
}
