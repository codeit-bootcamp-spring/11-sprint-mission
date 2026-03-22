package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class FileChannelRepository implements ChannelRepository {

    private final Path directory;

    public FileChannelRepository() {
        this.directory = Paths.get(System.getProperty("user.dir"), "data", "channels");
        initDirectory(this.directory);
    }

    private void initDirectory(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException("폴더 생성 중 오류가 발생했습니다.", e);
            }
        }
    }

    // 저장 saveToFile
    @Override
    public void save(Channel channel) {
        Path filePath = directory.resolve(channel.getId().toString().concat(".ser"));
        try (
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(channel);
        } catch (IOException e) {
            throw new RuntimeException("Channel 파일 저장 실패.", e);
        }
    }

    // 조회 loadFromFile 역할
    @Override
    public Channel findById(UUID id) {
        Path filePath = directory.resolve(id.toString().concat(".ser"));
        if (!Files.exists(filePath)) {
            return null;
        }

        try (
                FileInputStream fis = new FileInputStream(filePath.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            Channel channel = (Channel) ois.readObject();
            if (channel.isDeleted()) {
                return null;
            }
            return channel;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Channel 파일 불러오기 실패.", e);
        }
    }

    // 전체 조회 readAll
    @Override
    public List<Channel> findAll() {
        if (!Files.exists(directory)) return new ArrayList<>();

        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .filter(path -> path.toString().endsWith(".ser"))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (Channel) ois.readObject();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .filter(channel -> !channel.isDeleted())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("채널 목록 조회 중 오류 발생", e);
        }
    }

    // 삭제 delete
    @Override
    public void deleteById(UUID id) {
        Channel channel = findById(id);
        if (channel != null) {
            channel.softDelete();
            save(channel);
        }
    }
}
