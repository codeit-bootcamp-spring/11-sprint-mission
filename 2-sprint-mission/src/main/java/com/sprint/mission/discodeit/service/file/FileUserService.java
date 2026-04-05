/*
package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

public class FileUserService implements UserService {
    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";

    public FileUserService() {
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), "file-data-map", User.class.getSimpleName());
        if (Files.notExists(DIRECTORY)) {
            try {
                Files.createDirectories(DIRECTORY);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory: " + DIRECTORY, e);
            }
        }
    }

    private Path resolvePath(UUID id) {
        return DIRECTORY.resolve(id + EXTENSION);
    }

    // 직렬화
    private void saveToFile(User user) {
        Path path = resolvePath(user.getId());
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path.toFile()))) {
            oos.writeObject(user);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file: " + path, e);
        }
    }

    // 역직렬화
    private User loadFromFile(Path path) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path.toFile()))) {
            return (User) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to read file: " + path, e);
        }
    }

    @Override
    public User create(String username, String nickname, String description, String email, String password, UUID profileImageId) {
        User user = new User(username, nickname, description, email, password, profileImageId);
        saveToFile(user);
        return user;
    }

    @Override
    public User findById(UUID id) {
        Path path = resolvePath(id);
        if (Files.notExists(path)) {
            throw new NoSuchElementException("User with id " + id + " not found");
        }
        return loadFromFile(path);
    }

    @Override
    public List<User> findAll() {
        try (var pathStream = Files.list(DIRECTORY)) {
            return pathStream
                    .filter(path -> path.toString().endsWith(EXTENSION))
                    .map(this::loadFromFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read directory: " + DIRECTORY, e);
        }
    }

    @Override
    public User update(UUID id, String username, String nickname, String description, String email, String password, UUID profileImageId) {
        User user = findById(id);
        user.update(username, nickname, description, email, password);
        user.updateProfileImage(profileImageId);
        saveToFile(user);
        return user;
    }

    @Override
    public void delete(UUID id) {
        findById(id);
        Path path = resolvePath(id);

        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + path, e);
        }
    }
}*/
