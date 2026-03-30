package com.sprint.mission.dicordeit.repository.file;

import com.sprint.mission.dicordeit.entity.Channel;
import com.sprint.mission.dicordeit.repository.ChannelRepository;

import java.io.*;
import java.util.*;

public class FileChannelRepository implements ChannelRepository {

    private static final String FILE_PATH = "channels.dat";

    private Map<UUID, Channel> load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return new HashMap<>();

        try (ObjectInputStream ois =
                new ObjectInputStream(new FileInputStream(file))) {

            return (Map<UUID, Channel>) ois.readObject();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void saveAll(Map<UUID, Channel> data) {
        try (ObjectOutputStream oos =
                new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {

            oos.writeObject(data);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void save(Channel channel) {
        Map<UUID, Channel> data = load();
        data.put(channel.getId(), channel);
        saveAll(data);
    }

    @Override
    public Channel findById(UUID id) {
        return load().get(id);
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(load().values());
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, Channel> data = load();
        data.remove(id);
        saveAll(data);
    }

}
