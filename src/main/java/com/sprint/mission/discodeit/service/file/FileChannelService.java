package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.io.*;
import java.util.*;

public class FileChannelService {

    private final File file;

    public FileChannelService() {
        this.file = new File("channels.ser");
    }


    public Channel create(String name, String description) {
        Map<UUID, Channel> data = load();
        Channel channel = new Channel(name, description);
        data.put(channel.getId(), channel);
        save(data);
        return channel;
    }


    public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(load().get(id));
    }


    public List<Channel> findAll() {
        return new ArrayList<>(load().values());
    }


    public Channel update(UUID id, String name, String description) {
        Map<UUID, Channel> data = load();
        Channel channel = data.get(id);

        if (channel == null) {
            return null;
        }

        channel.update(name, description);
        save(data);
        return channel;
    }


    public void delete(UUID id) {
        Map<UUID, Channel> data = load();
        data.remove(id);
        save(data);
    }

    private void save(Map<UUID, Channel> data) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<UUID, Channel> load() {
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, Channel>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}