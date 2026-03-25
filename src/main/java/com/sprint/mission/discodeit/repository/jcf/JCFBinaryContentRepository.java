package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class JCFBinaryContentRepository implements BinaryContentRepository {

    private final Map<UUID, BinaryContent> data = new HashMap<>();

    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        data.put(binaryContent.getId(), binaryContent);
        return binaryContent;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<BinaryContent> findAll() {
        return data.values().stream().toList();
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}