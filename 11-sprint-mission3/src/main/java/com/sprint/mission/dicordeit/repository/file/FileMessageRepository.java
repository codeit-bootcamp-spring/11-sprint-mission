package com.sprint.mission.dicordeit.repository.file;

import com.sprint.mission.dicordeit.entity.Message;
import com.sprint.mission.dicordeit.repository.MessageRepository;

import java.io.*;
import java.util.*;


public class FileMessageRepository implements MessageRepository {

    private static final String FILE_PATH = "messages.dat";

    private Map<UUID, Message> load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new HashMap<>();

        try (ObjectInputStream ois =
                new ObjectInputStream(new FileInputStream(file))) {

            return (Map<UUID, Message>) ois.readObject();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void saveAll(Map<UUID, Message> data) {
        try (ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {

            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void save(Message message) {
        Map<UUID, Message> data = load();
        data.put(message.getId(), message);
        saveAll(data);
    }

    @Override
    public Message findById(UUID id) {
        return load().get(id);
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(load().values());
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, Message> data = load();
        data.remove(id);
        saveAll(data);
    }

}
