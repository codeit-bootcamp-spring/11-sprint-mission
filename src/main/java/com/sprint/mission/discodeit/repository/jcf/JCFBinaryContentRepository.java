package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;

import java.util.*;
import java.util.stream.Collectors;

public class JCFBinaryContentRepository implements BinaryContentRepository {
    private final Map<UUID, BinaryContent> binaryContentMap = new HashMap<>();

    public BinaryContent save(BinaryContent content) {
        binaryContentMap.put(content.getId(), content);
        return content;
    }

    public Optional<BinaryContent> findById(UUID id) {
        return binaryContentMap.values().stream()
                .filter(content -> content.getId().equals(id))
                .findFirst();
    }

    public List<BinaryContent> findAllById(Collection<UUID> ids) {
        return binaryContentMap.values().stream()
                .filter(content -> ids.contains(content.getId()))
                .collect(Collectors.toList());
    }

    public void delete(UUID id) {
        binaryContentMap.remove(id);
    }

    public void deleteAllByMessageId(UUID id) {
        binaryContentMap.values().removeIf(content -> content.getMessageId().equals(id));
    }
}
