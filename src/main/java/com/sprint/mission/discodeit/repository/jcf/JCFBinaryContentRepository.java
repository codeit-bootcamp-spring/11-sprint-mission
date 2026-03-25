package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
// application.yaml의 설정값에 따라 Bean을 설정 / name : 설정값의 이름, havingValue : type 지정, matchIfMissing : 설정이 안되있으면 jcf
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf", matchIfMissing = true)
public class JCFBinaryContentRepository implements BinaryContentRepository {
    private final Map<UUID, BinaryContent> binaryContents = new HashMap<>();

    @Override
    public void insert(BinaryContent binaryContent) {
        binaryContents.put(binaryContent.getId(), binaryContent);
    }

    @Override
    public BinaryContent findById(UUID id) {
        return binaryContents.get(id);
    }

    @Override
    public void delete(UUID id) {
        binaryContents.remove(id);
    }
}
