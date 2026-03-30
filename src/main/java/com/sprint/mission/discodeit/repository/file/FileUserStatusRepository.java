package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
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
@Profile("repo-file")
public class FileUserStatusRepository implements UserStatusRepository {

    private final Path directory;

    public FileUserStatusRepository(
            @Value("${discodeit.repository.file-directory}") String baseDirectory
    ) {
        this.directory = Paths.get(baseDirectory, "user-statuses");
        initDirectory(this.directory);
    }

    private void initDirectory(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (IOException e) {
                throw new RuntimeException("UserStatus 폴더 생성 중 오류가 발생했습니다.", e);
            }
        }
    }

    @Override
    public void save(UserStatus userStatus) {
        Path filePath = directory.resolve(userStatus.getId().toString().concat(".ser"));
        try (
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(userStatus);
        } catch (IOException e) {
            throw new RuntimeException("UserStatus 파일 저장 실패.", e);
        }
    }

    @Override
    public UserStatus findById(UUID id) {
        Path filePath = directory.resolve(id.toString().concat(".ser"));
        if (!Files.exists(filePath)) {
            return null;
        }

        try (
                FileInputStream fis = new FileInputStream(filePath.toFile());
                ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            UserStatus userStatus = (UserStatus) ois.readObject();
            if (userStatus.isDeleted()) {
                return null;
            }
            return userStatus;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("UserStatus 파일 불러오기 실패.", e);
        }
    }

    @Override
    public List<UserStatus> findAll() {
        if (!Files.exists(directory)) return new ArrayList<>();

        try (Stream<Path> paths = Files.list(directory)) {
            return paths
                    .filter(path -> path.toString().endsWith(".ser"))
                    .map(path -> {
                        try (
                                FileInputStream fis = new FileInputStream(path.toFile());
                                ObjectInputStream ois = new ObjectInputStream(fis)
                        ) {
                            return (UserStatus) ois.readObject();
                        } catch (Exception e) {
                            throw new RuntimeException("개별 UserStatus 파일 읽기 실패", e);
                        }
                    })
                    .filter(userStatus -> !userStatus.isDeleted())
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("UserStatus 목록 조회 중 오류 발생", e);
        }
    }

    @Override
    public void deleteById(UUID id) {
        UserStatus userStatus = findById(id);
        if (userStatus != null) {
            userStatus.softDelete();
            save(userStatus);
        }
    }
}