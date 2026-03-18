package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileMessageRepository implements MessageRepository {
    private Map<UUID, Message> data;
    private Map<UUID, Message> data_at;
    private void saveToFile(){
        File change = new File("Message.ser");
        File temp = new File("Message.ser.temp");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp))) {
            oos.writeObject(data);
            temp.renameTo(change);
        } catch (IOException e) {
            temp.delete();
            e.printStackTrace();
        }
    }
    private void loadFromFile(){
        File file = new File("Message.ser");
        if (!file.exists()) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            this.data = (Map<UUID, Message>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public FileMessageRepository() {
        this.data = new HashMap<>();
        loadFromFile();
    }

    @Override
    public Message create(Message message) {
        data.put(message.getId(), message);
        saveToFile();
        return message;
    }

    @Override
    public Message read(UUID id) {
        return data.get(id);
    }
    // 여기서 메세지는 보낸사람, 받는 사람 포함임

    @Override
    public List<Message> readAll(){
        return new ArrayList<>(data.values());
    }

    public List<Message> readAllByChannelId(UUID channelId){
        return data.values().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .toList();
    }


    @Override
    public void delete(UUID id) {
        data_at = new HashMap<>();
        data_at.put(id, data.get(id));
        data.remove(id);
        saveToFile();
    }

    @Override
    public void deleteAllByChannelId(UUID channelId){
        data.entrySet().removeIf(entry -> entry.getValue().getChannelId().equals(channelId));
    }
    @Override
    public String toString() {
        return data.toString();
    }
}
