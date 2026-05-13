package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.exception.StorageException;
import jakarta.annotation.PostConstruct;
import com.sprint.mission.discodeit.exception.NotFoundException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {

    private final Path root;

    public LocalBinaryContentStorage(
            @Value("${discodeit.storage.local.root-path}") String rootPath
    ) {
        this.root = Paths.get(rootPath);
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new StorageException("스토리지 루트 디렉토리 생성에 실패했습니다.", e);
        }
    }

    @Override
    public UUID put(UUID id, byte[] bytes) {
        try {
            Files.write(resolvePath(id), bytes);
            return id;
        } catch (IOException e) {
            throw new StorageException("바이너리 데이터 저장에 실패했습니다.", e);
        }
    }

    @Override
    public InputStream get(UUID id) {
        try {
            return Files.newInputStream(resolvePath(id));
        } catch (IOException e) {
            throw new StorageException("바이너리 데이터 조회에 실패했습니다.", e);
        }
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
        Path path = resolvePath(binaryContentDto.id());

        if (!Files.exists(path)) {
            throw new NotFoundException("파일을 찾을 수 없습니다");
        }

        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + binaryContentDto.fileName() + "\""
                    )
                    .contentType(MediaType.parseMediaType(binaryContentDto.contentType()))
                    .contentLength(binaryContentDto.size())
                    .body(resource);
        }
    private Path resolvePath(UUID id) {
        return root.resolve(id.toString());
    }

    @Override
    public void delete(UUID id) {
        try {
            Files.deleteIfExists(resolvePath(id));
        } catch (IOException e) {
            throw new StorageException("바이너리 데이터 삭제에 실패했습니다.", e);
        }
    }
}