package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FileBinaryContentRepository implements BinaryContentRepository {

    private final Path baseDir;

    public FileBinaryContentRepository(String rootDirectory) {
        this.baseDir = Path.of(rootDirectory, "binary-content");
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BinaryContent save(BinaryContent binaryContent) {
        Path path = baseDir.resolve(binaryContent.getId() + ".ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            oos.writeObject(binaryContent);
            return binaryContent;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<BinaryContent> findById(UUID id) {
        Path path = baseDir.resolve(id + ".ser");
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return Optional.of((BinaryContent) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<BinaryContent> findAll() {
        try {
            List<BinaryContent> result = new ArrayList<>();
            Files.list(baseDir)
                    .filter(path -> path.toString().endsWith(".ser"))
                    .forEach(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
                            result.add((BinaryContent) ois.readObject());
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