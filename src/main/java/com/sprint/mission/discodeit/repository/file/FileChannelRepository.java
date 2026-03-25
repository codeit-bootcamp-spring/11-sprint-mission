package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
// application.yaml의 설정값에 따라 Bean을 설정 / name : 설정값의 이름, havingValue : type 지정
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileChannelRepository implements ChannelRepository {
    private final Map<UUID, Channel> channels = new HashMap<>();

    private final String fileDirectory;
    private final String fileName = "/channels.ser";
    private final File file;

    public FileChannelRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        this.fileDirectory = fileDirectory;
        this.file = new File(fileDirectory + fileName);
        load();
    }

    // 저장 메서드 save(직렬화)
    public void save() {
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(channels);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 불러오기 메서드 load(역직렬화)
    public void load() {
        if (!file.exists()) return;
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            Map<UUID, Channel> loadChannels = (Map<UUID, Channel>) ois.readObject();
            channels.clear(); // 한 번 비우고
            channels.putAll(loadChannels); // 불러온다.(기존에 있던 데이터까지 같이 로드될 수 있기 때문에)
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    // 채널명으로 UUID 호출
    public UUID findIdByName(String name) {
        for (Map.Entry<UUID, Channel> channel : channels.entrySet()) {
            if (channel.getValue().getName().equals(name)) {
                return channel.getKey();
            }
        }
        return null;
    }

    @Override
    public void insert(Channel channel) {
        channels.put(channel.getId(), channel);
        save();
    }

    @Override
    public Channel findById(UUID id) {
        Channel channel = channels.get(id);
        if (channel == null) {
            throw new NoSuchElementException("해당 채널은 존재하지 않습니다. id : " + id);
        }
        return channel;
    }

    @Override
    public List<Channel> findAll() {
        return this.channels.values().stream().toList();
    }

    @Override
    public void update(Channel channel) {
        channels.put(channel.getId(), channel);
        save();
    }

    @Override
    public void delete(UUID id) {
        channels.remove(id);
        save();
    }
}
