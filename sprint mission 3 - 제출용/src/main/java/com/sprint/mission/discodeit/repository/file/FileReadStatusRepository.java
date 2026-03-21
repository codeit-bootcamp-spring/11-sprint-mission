package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileReadStatusRepository implements ReadStatusRepository {
    private Map<UUID, ReadStatus> data = new HashMap<>();

    private void saveToFile() {
        File change = new File("ReadStatus.ser");
        File temp = new File("ReadStatus.ser.temp");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp))) {
            oos.writeObject(data);
            temp.renameTo(change);
        } catch (IOException e) {
            temp.delete();
            e.printStackTrace();
        }
    }

    private void loadFromFile() {
        File file = new File("ReadStatus.ser");
        if (!file.exists()) return;
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            this.data = (Map<UUID, ReadStatus>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public FileReadStatusRepository(){
        this.data = new HashMap<>();
        loadFromFile();
    }

    @Override
    public ReadStatus create(ReadStatus readStatus){
        data.put(readStatus.getId(), readStatus);
        saveToFile();
        return readStatus;
    }

    @Override
    public ReadStatus read(UUID id){
        return data.get(id);
    }

    @Override
    public ReadStatus readByUserIdAndChannelId(UUID userId, UUID channelId){
        return data.values().stream()
                .filter(readStatus -> readStatus.getUserId().equals(userId) && readStatus.getChannelId().equals(channelId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<ReadStatus> readAllByChannelId(UUID channelId){
        return data.values().stream()
                .filter(readStatus -> readStatus.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public ReadStatus update(UUID userId, UUID channelId, Instant lastMessageReadAt){
        ReadStatus readStatus = readByUserIdAndChannelId(userId, channelId);
        if(readStatus == null){
            throw new IllegalArgumentException("존재하지 않는 ReadStatus입니다.");
        }
        readStatus.updateLastMessageReadAt(lastMessageReadAt);
        saveToFile();
        return readStatus;
    }

    @Override
    public void deleteByChannelId(UUID channelId){
        data.values().removeIf(readStatus -> readStatus.getChannelId().equals(channelId));
        saveToFile();
    }

    @Override
    public void deleteByUserId(UUID userId){
        data.values().removeIf(readStatus -> readStatus.getUserId().equals(userId));
        saveToFile();
    }
}
