package com.sprint.mission.discodeit.repository.file;

import org.springframework.stereotype.Repository;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.util.*;
import java.nio.file.*;

import static java.nio.file.Files.createDirectory;


public class FileUserRepository implements UserRepository {

    private final Path baseDir;

    public FileUserRepository(String rootDirectory) {
        this.baseDir = Path.of(rootDirectory, "users");
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public User save(User user) {
        Path filePath = baseDir.resolve(user.getId() + ".ser");

        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream((filePath.toFile())))) {
            out.writeObject(user);
            return user;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<User> findById(UUID id) {
        Path filePath = baseDir.resolve(id + ".ser");
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(filePath.toFile()))) {
            return Optional.of((User) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir, "*.ser")) {
            for (Path filePath : stream) {
                try (ObjectInputStream in = new ObjectInputStream(
                        new FileInputStream(filePath.toFile()))) {
                    users.add((User) in.readObject());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    @Override
    public void delete(UUID id) {
        Path filePath = baseDir.resolve(id + ".ser");
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDirectory() {
    try {
        Files.createDirectories(baseDir);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
    }
    @Override
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return findAll().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<User> findByUsernameAndPassword(String username, String password) {
        return findAll().stream()
                .filter(user -> user.getUsername().equals(username)
                        && user.getPassword().equals(password))
                .findFirst();
    }
}