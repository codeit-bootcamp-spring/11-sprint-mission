package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {

    private final Path root;

    public LocalBinaryContentStorage(
            @Value("${discodeit.storage.local.root-path}") String rootPath) {
        this.root = Paths.get(rootPath);
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
            log.info("Storage initialized at {}", root.toAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Storage 초기화 실패", e);
        }
    }

    private Path resolvePath(UUID id) {
        return root.resolve(id.toString());
    }

    @Override
    public UUID put(UUID id, byte[] bytes) {
        try {
            Files.write(resolvePath(id), bytes);
            return id;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패: " + id, e);
        }
    }

    @Override
    public InputStream get(UUID id) {
        try {
            return Files.newInputStream(resolvePath(id));
        } catch (IOException e) {
            throw new RuntimeException("파일 로드 실패: " + id, e);
        }
    }

    @Override
    public ResponseEntity<?> download(BinaryContentDto dto) {
        InputStream inputStream = get(dto.id());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(dto.fileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .contentType(MediaType.parseMediaType(dto.contentType()))
                .contentLength(dto.fileSize())
                .body(new InputStreamResource(inputStream));
    }
}