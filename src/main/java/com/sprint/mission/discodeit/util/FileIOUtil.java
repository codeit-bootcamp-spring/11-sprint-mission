package com.sprint.mission.discodeit.util;

import com.sprint.mission.discodeit.entity.Identifiable;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class FileIOUtil<T extends Serializable & Identifiable> {
    private final Class<T> type;
    private final Path dir;
    private static String rootDirectory;
    private static String ddlAuto;

    public FileIOUtil(Class<T> type) {
        this.type = type;
        this.dir = Paths.get(rootDirectory, type.getSimpleName().toLowerCase());
        this.init(dir);
    }

    public static void setInitValue(String rootDirectory, String ddlAuto) {
        FileIOUtil.rootDirectory = rootDirectory;
        FileIOUtil.ddlAuto = ddlAuto;
    }

    private void init(Path dir) {
        try {
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
                return;
            }

            if ("create".equalsIgnoreCase(ddlAuto)) {
                try (Stream<Path> paths = Files.list(dir)) {
                    paths.forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("failed to create " + dir + ". ❌", e);
        }
    }

    private Path uuidToPath(UUID id) {
        return this.dir.resolve(id.toString());
    }

    public void save(T data) {
        Path path = uuidToPath(data.getId());
        try (
                FileOutputStream fos = new FileOutputStream(path.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException("failed to save data to " + path + ". ❌", e);
        }
    }

    public T findById(UUID id) {
        return findByPath(uuidToPath(id));
    }

    private T findByPath(Path path) {
        try (
                FileInputStream fis = new FileInputStream(path.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            Object data = ois.readObject();
            if (this.type.isInstance(data)) return this.type.cast(data);
            throw new IllegalStateException("loaded object type mismatch. expected: " + this.type.getSimpleName() + ", actual: " + data.getClass().getSimpleName() + " ❌");
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("failed to load data from " + path + ". ❌", e);
        }
    }

    public List<T> findAll() {
        if (!Files.exists(this.dir)) return new ArrayList<>();
        try (Stream<Path> paths = Files.list(this.dir)) {
            return paths
                    .map(this::findByPath)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("failed to load all data from " + this.dir + ". ❌", e);
        }
    }

    public void delete(T data) {
        Path path = uuidToPath(data.getId());
        try {
            Files.delete(path);
        } catch (IOException e) {
            throw new RuntimeException("failed to delete data from " + path + ". ❌", e);
        }
    }
}
