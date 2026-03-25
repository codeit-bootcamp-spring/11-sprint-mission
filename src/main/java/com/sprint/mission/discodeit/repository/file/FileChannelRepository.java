package com.sprint.mission.discodeit.repository.file;

import org.springframework.stereotype.Repository;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class FileChannelRepository implements ChannelRepository {

    private final Path directory;

    public FileChannelRepository(String rootDirectory) {
        this.directory = Path.of(rootDirectory, "channels");
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Channel save(Channel channel) {
        Path filePath = directory.resolve(channel.getId() + ".ser");

        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(filePath.toFile()))) {
            out.writeObject(channel);
            return channel;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private UUID getId() {
        return UUID.randomUUID();
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        Path filePath = directory.resolve(id + ".ser");
        if (!Files.exists(filePath)) {
            return Optional.empty();
        }
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(filePath.toFile()))) {
            return Optional.of((Channel) in.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Channel> findAll() {
        List<Channel> channels = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.ser")) {
            for (Path filePath : stream) {
                try (ObjectInputStream in = new ObjectInputStream(
                        new FileInputStream(filePath.toFile()))) {
                    channels.add((Channel) in.readObject());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    return channels;
    }

    @Override
    public void delete(UUID id) {
        Path filePath = directory.resolve(id + ".ser");

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createDicectory() {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}