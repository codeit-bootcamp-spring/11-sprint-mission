package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileChannelRepository implements ChannelRepository {
    private final String CHANNEL_FILE = "channels.ser";
    private final File file;
    private final Map<UUID, Channel> channelMap;

    public FileChannelRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        File dir = new File(fileDirectory);

        if(!dir.exists()) {
            dir.mkdirs();
        }

        this.file = new File(dir, CHANNEL_FILE);

        this.channelMap = loadChannels();
    }

    private Map<UUID, Channel> loadChannels() {

        if(!file.exists()) {
            return new HashMap<>();
        }
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("채널 로드실패", e);
        }
    }

    private void saveChannel() {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(channelMap);
        } catch (IOException e) {
            throw new RuntimeException("채널 저장실패", e);
        }
    }

    @Override
    public Channel save(Channel channel) {
        channelMap.put(channel.getId(),channel);
        saveChannel();
        return channel;
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(channelMap.get(id));
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(channelMap.values());
    }

    @Override
    public void delete(UUID id) {
        if(channelMap.containsKey(id)) {
            channelMap.remove(id);
            saveChannel();
        }
    }

}
