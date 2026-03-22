package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// @Repository
public class JCFBinaryContentRepository implements BinaryContentRepository {

    private final Map<UUID, BinaryContent> binaryContentData = new ConcurrentHashMap<>();
    @Override
    public void save(BinaryContent binaryContent) {
        binaryContentData.put(binaryContent.getId(), binaryContent);
    }

    @Override
    public BinaryContent findById(UUID id) {
        BinaryContent binaryContent = binaryContentData.get(id);
        if (binaryContent != null && binaryContent.isDeleted()) {
            return null;
        }
        return binaryContent;
    }

    @Override
    public List<BinaryContent> findAll() {
        if (binaryContentData.isEmpty()) {
            return new ArrayList<>();
        }
        return binaryContentData.values().stream()
                .filter(binaryContent -> !binaryContent.isDeleted())
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        BinaryContent binaryContent = findById(id);
        if (binaryContent != null) {
            binaryContent.softDelete();
            save(binaryContent);
        }
    }
}