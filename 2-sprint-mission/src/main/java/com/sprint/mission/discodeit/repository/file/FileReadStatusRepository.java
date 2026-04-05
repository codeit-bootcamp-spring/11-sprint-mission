package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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
public class FileReadStatusRepository implements ReadStatusRepository {
    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    public FileReadStatusRepository(
            @Value("${discodeit.repository.file-directory:.discodeit}") String baseDir) {
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), baseDir, ReadStatus.class.getSimpleName());
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

    private void saveToFile(ReadStatus readStatus) {
        Path path = resolvePath(readStatus.getId());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            oos.writeObject(readStatus);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }

    private ReadStatus loadFromFile(Path path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return (ReadStatus) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new BusinessException(ErrorCode.FILE_READ_FAILED);
        }
    }

    @Override
    public ReadStatus save(ReadStatus readStatus) {
        saveToFile(readStatus);
        return readStatus;
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        Path path = resolvePath(id);
        if (Files.notExists(path)) {
            return Optional.empty();
        }
        return Optional.of(loadFromFile(path));
    }

    @Override
    public List<ReadStatus> findAll() {
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