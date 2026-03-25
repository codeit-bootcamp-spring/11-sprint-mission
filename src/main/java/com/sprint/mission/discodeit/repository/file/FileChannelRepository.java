package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileChannelRepository implements ChannelRepository {

    private static final String FILE_PATH = "channel.ser";

    @SuppressWarnings("unchecked")
    private Map<UUID, Channel> load() {
        if (Files.exists(Paths.get(FILE_PATH))) {
            try (
                    FileInputStream fis = new FileInputStream(FILE_PATH);
                    ObjectInputStream ois = new ObjectInputStream(fis);
            ) {
                return (Map<UUID, Channel>) ois.readObject();

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException("파일에서 채널 데이터 불러오기 실패", e);
            }
        }
        else {
            return new HashMap<>();
        }
    }

    private void saveMapToFile(Map<UUID, Channel> data) {
        try (
                FileOutputStream fos = new FileOutputStream(FILE_PATH);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }

    @Override
    public Channel save(Channel channel) {
        Map<UUID, Channel> data = load();
        data.put(channel.getId(), channel);
        saveMapToFile(data);
        return channel;
    }

    @Override
    public Map<UUID, Channel> findAll() {
        return load();
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        Map<UUID, Channel> data = load();
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public void delete(UUID id) {
        Map<UUID, Channel> data = load();
        if (data.remove(id) == null) {
            throw new IllegalArgumentException("[channel] 없는 id 입니다.");
        }
        saveMapToFile(data);
    }
}
