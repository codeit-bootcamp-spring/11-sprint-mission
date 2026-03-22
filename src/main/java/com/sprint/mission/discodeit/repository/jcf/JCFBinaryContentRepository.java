package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class JCFBinaryContentRepository implements BinaryContentRepository {
    private final Map<UUID, BinaryContent> data;

    public JCFBinaryContentRepository() {
        this.data = new HashMap<>();
    }

    @Override
    public void save(BinaryContent binaryContent) {
        this.data.put(binaryContent.getId(), binaryContent);
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return Optional.ofNullable(this.data.get(id));
    }

    @Override
    public List<BinaryContent> findAll() {
        return new ArrayList<>(this.data.values());
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        Set<UUID> idSet = new HashSet<>(ids);
        return this.data.values().stream()
                .filter(binaryContent -> idSet.contains(binaryContent.getId()))
                .toList();
    }

    @Override
    public void delete(BinaryContent binaryContent) {
        this.data.remove(binaryContent.getId());
    }

    @Override
    public void deleteAllByIdIn(List<UUID> ids) {
        ids.forEach(this.data::remove);
    }
}
