package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.util.FileIOUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
public class FileUserStatusRepository implements UserStatusRepository {
    private final FileIOUtil<UserStatus> fileIOUtil;

    public FileUserStatusRepository() {
        this.fileIOUtil = new FileIOUtil<>(UserStatus.class);
    }

    @Override
    public void save(UserStatus userStatus) {
        this.fileIOUtil.save(userStatus);
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        return Optional.ofNullable(this.fileIOUtil.findById(id));
    }

    @Override
    public Optional<UserStatus> findByUserId(UUID userId) {
        return this.fileIOUtil.findAll().stream()
                .filter(userStatus -> userStatus.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public List<UserStatus> findAll() {
        return this.fileIOUtil.findAll();
    }

    @Override
    public boolean existByUserId(UUID userId) {
        return this.fileIOUtil.findAll().stream()
                .anyMatch(userStatus -> userStatus.getUserId().equals(userId));
    }

    @Override
    public void delete(UserStatus userStatus) {
        this.fileIOUtil.delete(userStatus);
    }
}
