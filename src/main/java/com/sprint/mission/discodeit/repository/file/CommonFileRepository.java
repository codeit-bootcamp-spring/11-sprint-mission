package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Common;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class CommonFileRepository<T extends Common> {

    private Path directory;
    private String dirName;
    private Class<T> type;

    public CommonFileRepository(String dirName, Class<T> type) {
        this.dirName = dirName;
        this.type = type;
        init();
    }

    public void init() {
        directory = Paths.get(System.getProperty("user.dir"), "data", dirName);
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

    public void save(T obj) {
        Path path = filePath(obj.getId());
        try (
                FileOutputStream fos = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(obj);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save: " + path, e);
        }
    }

    public T findById(UUID id) {
        Path path = filePath(id);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException(type.getSimpleName() + " Not Found");
        }
        try (
                FileInputStream fis = new FileInputStream(path.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis);
        ) {
            Object obj = ois.readObject();
            return type.cast(obj);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to load: " + path, e);
        }
    }

    public List<T> findAll() {
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
                                return type.cast(ois.readObject());
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException("Failed to load: " + path, e);
                            }
                        })
                        .toList();
            } catch (IOException e) {
                throw new RuntimeException("Failed to load every " + type.getSimpleName(), e);
            }
        } else {
            return new ArrayList<>();
        }
    }

    public void delete(T obj) {
        Path path = filePath(obj.getId());
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete: " + path, e);
        }
    }

}
