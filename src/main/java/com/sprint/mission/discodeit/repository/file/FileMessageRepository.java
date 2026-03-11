package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
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
public class FileMessageRepository implements MessageRepository {
    private Path directory;

    public FileMessageRepository() {
        init();
    }

    @Override
    public void init() {
        directory = Paths.get(System.getProperty("user.dir"), "data", "messages");
        if(!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create directory: " + directory, e);
            }
        }
    }

    public Path filePath(UUID id) {
        return directory.resolve(id.toString() + ".ser");
    }

    @Override
    public void save(Message message) {
        Path path = filePath(message.getId());
        try (
                FileOutputStream fos = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(message);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save: " + path, e);
        }
    }

    @Override
    public Message load(UUID id) {
        Path path = filePath(id);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Message Not Found");
        }
        try (
                FileInputStream fis = new FileInputStream(path.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis);
        ) {
            return (Message) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load: " + path, e);
        }
    }

    @Override
    public List<Message> loadAll() {
        if(Files.exists(directory)) {
            try (
                    Stream<Path> paths = Files.list(directory);
            ){
                return paths
                        .map(path -> {
                            try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis);
                            ) {
                                return (Message) ois.readObject();
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException("Failed to load: " + path, e);
                            }
                        })
                        .toList();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load every Message", e);
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void delete(Message message) {
        Path path = filePath(message.getId());
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete: " + path, e);
        }
    }
}
