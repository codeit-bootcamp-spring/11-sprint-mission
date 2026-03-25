package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileBinaryContentRepository implements BinaryContentRepository {

    private static final String FILE_PATH = "binaryContent.ser";

    private Map<UUID, BinaryContent> load() {
        if (Files.exists(Path.of(FILE_PATH))) {
            try (
                    FileInputStream fis = new FileInputStream(FILE_PATH);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                return (Map<UUID, BinaryContent>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("파일에서 BinaryContent 불러오기 실패", e);
            }
        }
        return new HashMap<>();
    }

    private void saveMapToFile(Map<UUID, BinaryContent> data) {
        try (
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("파일에 BinaryContent 저장 실패", e);
        }
    }

    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        Map<UUID, BinaryContent> data = load();
        data.put(binaryContent.getId(), binaryContent);
        saveMapToFile(data);
        return binaryContent;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        return Optional.ofNullable(load().get(id));
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, BinaryContent> data = load();
        data.remove(id);
        saveMapToFile(data);
    }
}
