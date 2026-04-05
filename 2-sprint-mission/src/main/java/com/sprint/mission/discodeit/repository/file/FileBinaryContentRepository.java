package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
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
public class FileBinaryContentRepository implements BinaryContentRepository {
    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    public FileBinaryContentRepository(
            @Value("${discodeit.repository.file-directory:.discodeit}") String baseDir) {
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), baseDir, BinaryContent.class.getSimpleName());
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

    private void saveToFile(BinaryContent binaryContent) {
        Path path = resolvePath(binaryContent.getId());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            oos.writeObject(binaryContent);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }
    }

    private BinaryContent loadFromFile(Path path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return (BinaryContent) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new BusinessException(ErrorCode.FILE_READ_FAILED);
        }
    }

    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        saveToFile(binaryContent);
        return binaryContent;
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        Path path = resolvePath(id);
        if (Files.notExists(path)) {
            return Optional.empty();
        }
        return Optional.of(loadFromFile(path));
    }

    @Override
    public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
        return ids.stream()
                .map(this::resolvePath)          // ID를 경로(Path)로 변환
                .filter(Files::exists)           // 해당 경로에 파일이 실제로 존재하는지 확인
                .map(this::loadFromFile)         // 파일 읽기 (역직렬화)
                .collect(Collectors.toList());
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