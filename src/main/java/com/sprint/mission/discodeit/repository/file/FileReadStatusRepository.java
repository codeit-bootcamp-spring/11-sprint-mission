package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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
public class FileReadStatusRepository implements ReadStatusRepository {

    private final Path directory;

    public FileReadStatusRepository() {
        this.directory = Paths.get(System.getProperty("user.dir"), "data", "read-statuses");
        initDirectory(this.directory);
    }

    private void initDirectory(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException("ReadStatus 폴더 생성 중 오류가 발생했습니다.", e);
            }
        }
    }

    @Override
    public void save(ReadStatus readStatus) {
        Path filePath = directory.resolve(readStatus.getId().toString().concat(".ser"));
        try (
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(readStatus);
        } catch (IOException e) {
            throw new RuntimeException("ReadStatus 파일 저장 실패.", e);
        }
    }

    @Override
    public ReadStatus findById(UUID id) {
        Path filePath = directory.resolve(id.toString().concat(".ser"));
        if (!Files.exists(filePath)) {
            return null;
        }

        try (
                FileInputStream fis = new FileInputStream(filePath.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            ReadStatus readStatus = (ReadStatus) ois.readObject();
            if (readStatus.isDeleted()) {
                return null;
            }
            return readStatus;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("ReadStatus 파일 불러오기 실패.", e);
        }
    }

    @Override
    public List<ReadStatus> findAll() {
        if (!Files.exists(directory)) return new ArrayList<>();

        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .filter(path -> path.toString().endsWith(".ser"))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (ReadStatus) ois.readObject();
                        } catch (Exception e) {
                            throw new RuntimeException("개별 ReadStatus 파일 읽기 실패", e);
                        }
                    })
                    .filter(readStatus -> !readStatus.isDeleted())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("ReadStatus 목록 조회 중 오류 발생", e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        ReadStatus readStatus = findById(id);
        if (readStatus != null) {
            readStatus.softDelete();
            save(readStatus);
        }
    }
}