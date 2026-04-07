package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.baseentity.BaseEntity;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;

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

public abstract class CommonFileRepository<T extends BaseEntity> {

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
                throw new BusinessException(ErrorCode.DIRECTORY_CREATION_FAILED);
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
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
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
            throw new BusinessException(ErrorCode.FILE_LOAD_FAILED);
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
                                throw new BusinessException(ErrorCode.FILE_LOAD_FAILED);
                            }
                        })
                        .toList();
            } catch (IOException e) {
                throw new BusinessException(ErrorCode.FILE_LOAD_FAILED);
            }
        } else {
            return new ArrayList<>();
        }
    }

    public boolean deleteById(UUID id) {
        Path path = filePath(id);
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED);
        }
    }

}
