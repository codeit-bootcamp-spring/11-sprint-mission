package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.service.dto.binarycontent.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class LocalBinaryContentStorage implements BinaryContentStorage {

    private final Path root;

    public LocalBinaryContentStorage(Path root) {
        this.root = root;
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new RuntimeException("저장소 디렉토리를 생성할 수 없어요: " + root, e);
        }
    }

    @Override
    public UUID put(UUID id, byte[] data) {
        try {
            Files.write(resolvePath(id), data == null ? new byte[0] : data);
            return id;
        } catch (IOException e) {
            throw new RuntimeException("파일 저장에 실패했어요: " + id, e);
        }
    }

    @Override
    public InputStream get(UUID id) {
        try {
            return Files.newInputStream(resolvePath(id));
        } catch (IOException e) {
            throw new RuntimeException("파일을 읽을 수 없어요: " + id, e);
        }
    }

    @Override
    public ResponseEntity<Resource> download(BinaryContentDto dto) {
        InputStream inputStream = get(dto.id());
        Resource resource = new InputStreamResource(inputStream);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resolveFileName(dto.fileName(), dto.id()) + "\"")
                .contentType(MediaType.parseMediaType(resolveContentType(dto.contentType())))
                .contentLength(dto.size())
                .body(resource);
    }

    private Path resolvePath(UUID id) {
        return root.resolve(id.toString());
    }

    private String resolveContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        return contentType;
    }

    private String resolveFileName(String fileName, UUID fallbackId) {
        if (fileName == null || fileName.isBlank()) {
            return fallbackId + ".bin";
        }
        return fileName;
    }
}
