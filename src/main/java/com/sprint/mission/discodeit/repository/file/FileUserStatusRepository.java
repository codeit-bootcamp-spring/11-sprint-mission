package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class FileUserStatusRepository implements UserStatusRepository {

    private final Path baseDir;

    public FileUserStatusRepository(String rootDirectory) {
        this.baseDir = Path.of(rootDirectory, "user-status");
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public UserStatus save(UserStatus userStatus) {
        Path path = baseDir.resolve(userStatus.getId() + ".ser");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            oos.writeObject(userStatus);
            return userStatus;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<UserStatus> findById(UUID id) {
        Path path = baseDir.resolve(id + ".ser");
        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return Optional.of((UserStatus) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserStatus> findAll() {
        try {
            List<UserStatus> result = new ArrayList<>();
            Files.list(baseDir)
                    .filter(path -> path.toString().endsWith(".ser"))
                    .forEach(path -> {
                        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
                            result.add((UserStatus) ois.readObject());
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
    public Optional<UserStatus> findByUserId(UUID userId) {
        return findAll().stream()
                .filter(userStatus -> userStatus.getUserId().equals(userId))
                .findFirst();
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