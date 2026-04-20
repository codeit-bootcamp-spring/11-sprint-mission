package com.sprint.mission.discodeit.service.impl;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentStorage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root;

  public LocalBinaryContentStorage(@Value("${discodeit.storage.local.root-path}") String rootPath) {
    this.root = Paths.get(rootPath);
  }

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new IllegalStateException("스토리지 루트 디렉토리 생성 실패", e);
    }
  }

  @Override
  public void put(UUID id, byte[] bytes) {
    Path path = root.resolve(id.toString());
    try (OutputStream os = Files.newOutputStream(path)) {
      os.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException("파일 저장 실패: " + id, e);
    }
  }

  @Override
  public InputStream get(UUID id) {
    try {
      return Files.newInputStream(root.resolve(id.toString()));
    } catch (IOException e) {
      throw new RuntimeException("파일 조회 실패: " + id, e);
    }
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentDto dto) {
    Resource resource = new InputStreamResource(get(dto.id()));
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + dto.fileName() + "\"")
        .header(HttpHeaders.CONTENT_TYPE, dto.contentType())
        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(dto.size()))
        .body(resource);
  }
}