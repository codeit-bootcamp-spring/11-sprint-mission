package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileChannelRepository implements ChannelRepository {
    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    public FileChannelRepository(
            @Value("${discodeit.repository.file-directory:.discodeit}") String baseDir) {
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), baseDir, Channel.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) {
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.FILE_DIRECTORY_CREATION_FAILED);
            }
        }
    }

    private Path resolvePath(UUID id) {
        return DIRECTORY.resolve(id + EXTENSION);
    }

    private void saveToFile(Channel channel) {
        Path path = resolvePath(channel.getId());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            oos.writeObject(channel);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }

    private Channel loadFromFile(Path path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return (Channel) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new BusinessException(ErrorCode.FILE_READ_FAILED);
        }
    }

    @Override
    public Channel save(Channel channel) {
        saveToFile(channel);
        return channel;
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        Path path = resolvePath(id);
        if (Files.notExists(path)) {
            return Optional.empty();
        }
        return Optional.of(loadFromFile(path));
    }

    @Override
    public List<Channel> findAll() {
        try (var pathStream = Files.list(DIRECTORY)) {
            return pathStream
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(this::loadFromFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_DIRECTORY_READ_FAILED);
        }
    }

    @Override
    public boolean existsById(UUID id) {
        return Files.exists(resolvePath(id));
    }

    @Override
    public void deleteById(UUID id) {
        Path path = resolvePath(id);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
        }
    }
}