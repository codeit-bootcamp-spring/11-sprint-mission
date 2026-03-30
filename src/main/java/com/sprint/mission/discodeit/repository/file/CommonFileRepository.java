package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.baseentity.Common;
import com.sprint.mission.discodeit.exception.repository.DirectoryCreationException;
import com.sprint.mission.discodeit.exception.repository.FileDeleteException;
import com.sprint.mission.discodeit.exception.repository.FileLoadException;
import com.sprint.mission.discodeit.exception.repository.FileSaveException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class CommonFileRepository<T extends Common> {

    private final Path directory;
    private final Class<T> type;

    public CommonFileRepository(String dirName, Class<T> type, String basedir) {
        this.directory = Paths.get(System.getProperty("user.dir"), basedir, dirName);
        this.type = type;
        init();
    }

    private void init() {
        if(!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new DirectoryCreationException(directory, e);
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
            throw new FileSaveException(path, e);
        }
    }

    public Optional<T> findById(UUID id) {
        Path path = filePath(id);

        if (!Files.exists(path)) {
            return Optional.empty();
        }

        try (
                FileInputStream fis = new FileInputStream(path.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis);
        ) {
            Object obj = ois.readObject();
            return Optional.of(type.cast(obj));
        } catch (IOException | ClassNotFoundException e) {
            throw new FileLoadException(path, e);
        }
    }

    public List<T> findAll() {
        if(Files.exists(directory)) {
            try (
                    Stream<Path> paths = Files.list(directory);
            ){
                return paths
                        .filter(path -> path.getFileName().toString().endsWith(".ser"))
                        .map(path -> {
                            try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis);
                            ) {
                                return type.cast(ois.readObject());
                            } catch (IOException | ClassNotFoundException e) {
                                throw new FileLoadException(path, e);
                            }
                        })
                        .toList();
            } catch (IOException e) {
                throw new FileLoadException(directory, e);
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
            throw new FileDeleteException(path, e);
        }
    }

}
