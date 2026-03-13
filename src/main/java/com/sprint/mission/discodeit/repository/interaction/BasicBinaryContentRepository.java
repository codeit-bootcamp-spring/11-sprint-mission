package com.sprint.mission.discodeit.repository.interaction;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BasicBinaryContentRepository implements BinaryContentRepository {
    private final String CONTENT_FILE = "binaryContents.ser";
    private final Map<UUID, BinaryContent> binaryContentMap;

    public BasicBinaryContentRepository() {
        this.binaryContentMap = loadContents();
    }

    private Map<UUID, BinaryContent> loadContents() {
        File file = new File(CONTENT_FILE);

        if(!file.exists()) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, BinaryContent>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("콘텐츠 로드실패", e);
        }
    }

    private void saveContents() {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CONTENT_FILE))) {
            oos.writeObject(binaryContentMap);
        } catch (IOException e) {
            throw new RuntimeException("유저 저장실패", e);
        }
    }

    @Override
    public BinaryContent save(BinaryContent content) {
        binaryContentMap.put(content.getId(), content);
        saveContents();
        return content;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return binaryContentMap.values().stream()
                .filter(content -> content.getId().equals(id))
                .findFirst();
    }

    @Override
    public void delete(UUID id) {
        if(binaryContentMap.remove(id) != null) {
            saveContents();
        } else {
            throw new IllegalArgumentException("존재하지 않는 콘텐츠입니다.");
        }
    }
}
