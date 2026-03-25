package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class FileReadStatusRepository implements ReadStatusRepository {

    private final Path baseDir;

    public FileReadStatusRepository(String rootDirectory) {
        this.baseDir = Path.of(rootDirectory, "read-status");
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ReadStatus save(ReadStatus readStatus) {
        Path path = baseDir.resolve(readStatus.getId() + ".ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            oos.writeObject(readStatus);
            return readStatus;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ReadStatus> findById(UUID id) {
        Path path = baseDir.resolve(id + ".ser");
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return Optional.of((ReadStatus) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ReadStatus> findAll() {
        try {
            List<ReadStatus> result = new ArrayList<>();
            Files.list(baseDir)
                    .filter(path -> path.toString().endsWith(".ser"))
                    .forEach(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
                            result.add((ReadStatus) ois.readObject());
                        } catch (IOException | ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    });
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(UUID id) {
        try {
            Files.deleteIfExists(baseDir.resolve(id + ".ser"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}