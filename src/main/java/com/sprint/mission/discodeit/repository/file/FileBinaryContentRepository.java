package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.util.FileIOUtil;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public class FileBinaryContentRepository implements BinaryContentRepository {
    private final FileIOUtil<BinaryContent> fileIOUtil;

    public FileBinaryContentRepository() {
        this.fileIOUtil = new FileIOUtil<>(BinaryContent.class);
    }

    @Override
    public void save(BinaryContent binaryContent) {
        this.fileIOUtil.save(binaryContent);
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return Optional.ofNullable(this.fileIOUtil.findById(id));
    }

    @Override
    public List<BinaryContent> findAll() {
        return this.fileIOUtil.findAll();
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        Set<UUID> idSet = new HashSet<>(ids);
        return this.fileIOUtil.findAll().stream()
                .filter(binaryContent -> idSet.contains(binaryContent.getId()))
                .toList();
    }

    @Override
    public void delete(BinaryContent binaryContent) {
        this.fileIOUtil.delete(binaryContent);
    }

    @Override
    public void deleteAllByIdIn(List<UUID> ids) {
        this.findAllByIdIn(ids)
                .forEach(this.fileIOUtil::delete);
    }
}
