package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
public class FileChannelRepository implements ChannelRepository {
    private final String CHANNEL_FILE = "channels.ser";

    private Map<UUID, Channel> loadChannels() {
        File file = new File(CHANNEL_FILE);

        if(!file.exists()) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("채널 로드실패", e);
        }
    }

    private void saveChannel(Map<UUID, Channel> channels) {
        File file = new File(CHANNEL_FILE);

        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(channels);
        } catch (IOException e) {
            throw new RuntimeException("채널 저장실패", e);
        }
    }

    @Override
    public Channel save(Channel channel) {
        Map<UUID, Channel> channels = loadChannels();
        channels.put(channel.getId(),channel);
        saveChannel(channels);
        return channel;
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(loadChannels().get(id));
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(loadChannels().values());
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, Channel> channels = loadChannels();
        if(channels.containsKey(id)) {
            channels.remove(id);
            saveChannel(channels);
        }
    }

}
