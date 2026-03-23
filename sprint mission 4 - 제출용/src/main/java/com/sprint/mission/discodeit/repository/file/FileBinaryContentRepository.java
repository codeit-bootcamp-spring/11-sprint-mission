package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileBinaryContentRepository implements BinaryContentRepository {
    private Map<UUID, BinaryContent> data = new HashMap<>();

    private void saveToFile() {
        File change = new File("BinaryContent.ser");
        File temp = new File("BinaryContent.ser.temp");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp))) {
            oos.writeObject(data);
            temp.renameTo(change);
        } catch (IOException e) {
            temp.delete();
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        File file = new File("BinaryContent.ser");
        if (!file.exists()) return;
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            this.data = (Map<UUID, BinaryContent>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public FileBinaryContentRepository() {
        this.data = new HashMap<>();
        loadFromFile();
    }

    @Override
    public BinaryContent create(BinaryContent binaryContent){
        data.put(binaryContent.getId(), binaryContent);
        saveToFile();
        return binaryContent;
    }

    @Override
    public BinaryContent readById(UUID id){
        return data.get(id);
    }

    @Override
    public BinaryContent readByUserId(UUID userId){
        return data.values().stream()
                .filter(binaryContent -> binaryContent.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<BinaryContent> readAllByMessageId(UUID messageId){
        return data.values().stream()
                .filter(binaryContent -> binaryContent.getMessageId().equals(messageId))
                .toList();
    }

    @Override
    public void delete(UUID id){
        data.remove(id);
        saveToFile();
    }

    @Override
    public void deleteByMessageId(UUID messageId){
        data.values().removeIf(binaryContent -> binaryContent.getMessageId().equals(messageId));
        saveToFile();
    }

    @Override
    public void deleteByUserId(UUID userId){
        data.values().removeIf(binaryContent -> binaryContent.getUserId().equals(userId));
        saveToFile();
    }
}
