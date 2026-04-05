package com.sprint.mission.discodeit.repository.file;

import org.springframework.stereotype.Repository;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.Files.createDirectory;


public class FileMessageRepository implements MessageRepository {

    private final Path baseDir;

    public FileMessageRepository(String rootDirectory) {
        this.baseDir = Path.of(rootDirectory, "messages");
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message save(Message message) {
        Path filePath = baseDir.resolve(message.getId() + ".ser");
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            out.writeObject(message);
            return message;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Message> findById(UUID id) {
        Path filePath = baseDir.resolve(id + ".ser");
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
            return Optional.of((Message) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Message> findAll() {
        List<Message> messages = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(baseDir, "*.ser")) {
            for (Path filePath : stream) {
                try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath.toFile()))) {
                    messages.add((Message) in.readObject());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return messages;
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

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        return findAll().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .toList();
    }
}