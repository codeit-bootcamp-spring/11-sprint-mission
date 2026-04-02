package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "jcf")
public class JCFBinaryContentRepository implements BinaryContentRepository {

    private final Map<UUID, BinaryContent> data;

    public JCFBinaryContentRepository (){

        data = new HashMap<>();

    }

    @Override
    public BinaryContent saveBinaryContent(BinaryContent binaryContent) {
        if(binaryContent == null)
            return null;
        data.put(binaryContent.getId(),binaryContent);
        return binaryContent;
    }

    @Override
    public Optional<BinaryContent> getBinaryContent(UUID binaryContentId) {
        return Optional.ofNullable(data.get(binaryContentId));
    }

    @Override
    public Optional<BinaryContent> getProfileContentByUserId(UUID userId) {
        return data.values().stream()
                .filter(binaryContent -> binaryContent.getUserID().equals(userId))
                .filter(binaryContent -> binaryContent.getMessageId() == null)
                .findFirst();
    }

    @Override
    public List<BinaryContent> getAllBinaryContent() {
        return data.values().stream().toList();
    }

    @Override
    public List<BinaryContent> getAllByUserId(UUID userId) {
        return data.values().stream()
                .filter(binaryContent -> binaryContent.getUserID().equals(userId))
                .toList();
    }

    @Override
    public List<BinaryContent> getAllByMessageId(UUID messageId) {
        return data.values().stream()
                .filter(binaryContent -> binaryContent.getMessageId()!=null&&binaryContent.getMessageId().equals(messageId))
                .toList();
    }

    @Override
    public boolean deleteBinaryContent(UUID binaryContentId) {
        return data.remove(binaryContentId) != null;
    }

    @Override
    public boolean deleteBinaryContentByMessageId(UUID messageId) {
        return data.values().removeIf(binaryContent -> binaryContent.getMessageId()!= null&&binaryContent.getMessageId().equals(messageId));
    }

    @Override
    public boolean isExistBinaryContent(UUID binaryContentId) {
        return data.containsKey(binaryContentId);
    }


    record profileInfo(UUID userId, UUID messageId){}

}
