package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.util.FileIOUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
public class FileReadStatusRepository implements ReadStatusRepository {
    private final FileIOUtil<ReadStatus> fileIOUtil;

    public FileReadStatusRepository() {
        this.fileIOUtil = new FileIOUtil<>(ReadStatus.class);
    }

    @Override
    public void save(ReadStatus readStatus) {
        this.fileIOUtil.save(readStatus);
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        return Optional.ofNullable(this.fileIOUtil.findById(id));
    }

    @Override
    public List<ReadStatus> findAll() {
        return this.fileIOUtil.findAll();
    }

    @Override
    public List<ReadStatus> findAllByUserId(UUID userId) {
        return this.fileIOUtil.findAll().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByChannelId(UUID channelId) {
        return this.fileIOUtil.findAll().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public List<ReadStatus> findAllByChannelIdIn(List<UUID> channelIds) {
        Set<UUID> idSet = new HashSet<>(channelIds);
        return this.fileIOUtil.findAll().stream()
                .filter(readStatus -> idSet.contains(readStatus.getChannelId()))
                .toList();
    }

    @Override
    public boolean existByUserIdAndChannelId(UUID userId, UUID channelId) {
        return this.fileIOUtil.findAll().stream()
                .anyMatch(readStatus -> readStatus.getUserId().equals(userId) && readStatus.getChannelId().equals(channelId));
    }

    @Override
    public void delete(ReadStatus readStatus) {
        this.fileIOUtil.delete(readStatus);
    }

    @Override
    public void deleteAllByUserId(UUID userId) {
        this.findAllByUserId(userId)
                .forEach(this::delete);
    }

    @Override
    public void deleteAllByChannelId(UUID channelId) {
        this.findAllByChannelId(channelId)
                .forEach(this::delete);
    }
}
