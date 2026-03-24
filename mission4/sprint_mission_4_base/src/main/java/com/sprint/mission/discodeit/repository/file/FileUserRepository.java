package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
@Repository
public class FileUserRepository implements UserRepository {
    private final Path DIRECTORY;
    private final String EXTENSION = ".ser";
    private final FileStorageHelper fileStorageHelper; // 1. 헬퍼 추가

    public FileUserRepository(
            @Value("${discodeit.repository.file-directory:data}") String fileDirectory,
            FileStorageHelper fileStorageHelper // 2. 주입 받기
    ) {
        this.fileStorageHelper = fileStorageHelper;
        this.DIRECTORY = Paths.get(System.getProperty("user.dir"), fileDirectory, User.class.getSimpleName());
        // ... 디렉토리 생성 로직 생략 ...
    }

    @Override
    public User save(User user) {
        Path path = resolvePath(user.getId());
        fileStorageHelper.write(path, user); // 3. 헬퍼 사용 (try-with-resources 제거됨)
        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        Path path = resolvePath(id);
        if (Files.exists(path)) {
            User user = fileStorageHelper.read(path); // 4. 헬퍼 사용
            return Optional.ofNullable(user);
        }
        return Optional.empty();
    }

}