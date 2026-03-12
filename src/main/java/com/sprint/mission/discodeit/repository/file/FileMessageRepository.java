package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.util.*;

public class FileMessageRepository implements MessageRepository {
    private final String MESSAGE_FILE = "messages.ser";

    private Map<UUID, Message> loadMessages() {
        File file = new File(MESSAGE_FILE);

        if(!file.exists()) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<Message> list = (List<Message>) ois.readObject();
            Map<UUID, Message> map = new HashMap<>();
            for(Message m : list) {
                map.put(m.getId(), m);
            }
            return map;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("매세지 로드실패", e);
        }
    }

    private void saveMessage(Map<UUID, Message> messageMap) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MESSAGE_FILE))) {
            oos.writeObject(new ArrayList<>(messageMap.values()));
        } catch (IOException e) {
            throw new RuntimeException("메세지 저장실패", e);
        }
    }

    @Override
    public Message save(Message message) {
        Map<UUID, Message> messages = loadMessages();
        messages.put(message.getId(), message);
        saveMessage(messages);
        return message;
    }

    @Override
    public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(loadMessages().get(id));
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(loadMessages().values());
    }

    @Override
    public void delete(Message message) {
        Map<UUID, Message> messages = loadMessages();
        if(messages.remove(message.getId()) != null) {
            saveMessage(messages);
        }
    }

}
