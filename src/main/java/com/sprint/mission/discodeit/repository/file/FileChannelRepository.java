package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@ConditionalOnProperty(prefix = "discodeit.repository", name = "type", havingValue = "file")
public class FileChannelRepository implements ChannelRepository {
    private void saveChannels(List<Channel> channels) {
        try (FileOutputStream fos = new FileOutputStream("channel.ser");
             ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(channels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Channel> loadChannels() {
        File file = new File("channel.ser");

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileInputStream fis = new FileInputStream("channel.ser");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (List<Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public void save(Channel channel) {
        List<Channel> channels = loadChannels();
        channels.removeIf(c -> c.getId().equals(channel.getId()));
        channels.add(channel);
        saveChannels(channels);
    }

    @Override
    public Channel findById(UUID id) {
        return loadChannels().stream()
                .filter(channel -> channel.getId().equals(id))
                .findFirst().orElse(null);
    }

    @Override
    public List<Channel> findAll() {
        return loadChannels();
    }

    @Override
    public void delete(UUID id) {
        List<Channel> channels = loadChannels();
        channels.removeIf(channel -> channel.getId().equals(id));
        saveChannels(channels);
    }
}
