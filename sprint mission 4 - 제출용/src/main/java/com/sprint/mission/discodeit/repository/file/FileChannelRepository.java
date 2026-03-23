package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileChannelRepository implements ChannelRepository {
    private Map<UUID, Channel> data = new ConcurrentHashMap<>();
    private Map<UUID, Channel> data_at = new ConcurrentHashMap<>();

    private final String fileDirectory;
    private final String filePath;

    public FileChannelRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        this.fileDirectory = fileDirectory;
        this.filePath = fileDirectory + "Channel.ser";
        new File(fileDirectory).mkdirs(); // 디렉토리 없으면 생성
        loadFromFile();
    }

    private void saveToFile(){
        File change = new File(filePath);
        File temp = new File(filePath+".temp");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp))) {
            oos.writeObject(data);
            temp.renameTo(change);
        } catch (IOException e) {
            temp.delete();
            e.printStackTrace();
        }
    }
    private void loadFromFile(){
        File file = new File(filePath);
        if (!file.exists()) return;

        try (FileInputStream fis = new FileInputStream(filePath);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            this.data = new ConcurrentHashMap<>((Map<UUID, Channel>) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Channel create(Channel channel) {
        data.put(channel.getId(), channel);
        saveToFile();
        return channel;
    }

    @Override
    public Channel read(UUID id) { return data.get(id); }

    @Override
    public List<Channel> readAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Channel update(Channel channel) {
        data.put(channel.getId(), channel);
        saveToFile();
        return channel;
    }

    @Override
    public void delete(UUID id) {
        data_at =new ConcurrentHashMap<>();
        data_at.put(id, data.get(id));
        data.remove(id);
        saveToFile();
    }

    @Override
    public void restore(UUID id) {
        if (data_at == null || data_at.get(id) == null) {
            throw new IllegalArgumentException("복구할 데이터가 없습니다.");
        }
        data.put(id, data_at.get(id));
        data_at.remove(id);
        saveToFile();
    }

    @Override
    public boolean existsByChannelName(String channelName) {
        return data.values().stream()
                .filter(c -> c.getChannelName() != null)
                .anyMatch(c -> c.getChannelName().equals(channelName));
    }

    @Override
    public boolean existsByChannelNameExcluding(String channelName, UUID excludeId) {
        return data.values().stream()
                .filter(c -> !c.getId().equals(excludeId))
                .filter(c -> c.getChannelName() != null)
                .anyMatch(c -> c.getChannelName().equals(channelName));
    }
}
