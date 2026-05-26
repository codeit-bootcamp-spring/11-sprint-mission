package com.sprint.mission.discodeit.storage.local;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.exception.binarycontent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.binarycontent.FileOperationException;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root;

  public LocalBinaryContentStorage(
      @Value("${discodeit.storage.local.root-path}") String rootPath) {
    this.root = Paths.get(rootPath);
  }

  @PostConstruct
  public void init() {
    try {
      if (!Files.exists(root)) {
        Files.createDirectories(root);
      }
    } catch (IOException e) {
      throw FileOperationException.directoryCreationFailed();
    }
  }


  @Override
  public UUID put(UUID id, byte[] bytes) {
    Path path = resolvePath(id);
    try {
      if (Files.exists(path)) {
        throw FileOperationException.alreadyExists();
      }
      Files.write(path, bytes);
      return id;
    } catch (IOException e) {
      throw FileOperationException.saveFailed();
    }
  }

  @Override
  public InputStream get(UUID id) {
    Path path = resolvePath(id);
    if (Files.notExists(path)) {
      throw BinaryContentNotFoundException.withId(id);
    }
    try {
      return Files.newInputStream(path);
    } catch (IOException e) {
      throw FileOperationException.readFailed();
    }
  }

  private Path resolvePath(UUID id) {
    return root.resolve(id.toString());
  }

  @Override
  public ResponseEntity<?> download(BinaryContentDto.Response dto) {
    InputStream inputStream = get(dto.id());
    Resource resource = new InputStreamResource(inputStream);

    return ResponseEntity
        .status(HttpStatus.OK)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + dto.fileName() + "\"")
        .header(HttpHeaders.CONTENT_TYPE, dto.contentType())
        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(dto.size()))
        .body(resource);
  }

  @Override
  public void delete(UUID id) {
    try {
      Files.deleteIfExists(resolvePath(id));
    } catch (IOException e) {
      log.error("디스크 파일 삭제 실패: id={}", id, e);
      throw FileOperationException.deleteFailed();
    }
  }
}