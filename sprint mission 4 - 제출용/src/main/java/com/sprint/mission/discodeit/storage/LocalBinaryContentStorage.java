package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.exception.DiscodeitNotFoundException;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@Conditional(LocalStorageCondition.class)
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root;

  public LocalBinaryContentStorage(
      @Value("${discodeit.storage.local.root-path}") String rootPath
  ) {
    this.root = Path.of(rootPath);
  }

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new RuntimeException("로컬 저장소 초기화에 실패했습니다.", e);
    }
  }

  @Override
  public UUID put(UUID id, byte[] bytes) {
    Path path = resolvePath(id);
    try {
      Files.write(path, bytes);
      return id;
    } catch (IOException e) {
      throw new RuntimeException("파일 저장에 실패했습니다. id=" + id, e);
    }
  }

  @Override
  public InputStream get(UUID id) {
    Path path = resolvePath(id);
    try {
      if (!Files.exists(path)) {
        throw DiscodeitNotFoundException.binaryContent(id);
      }
      return Files.newInputStream(path);
    } catch (IOException e) {
      throw new RuntimeException("파일 조회에 실패했습니다. id=" + id, e);
    }
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {
    InputStream inputStream = get(binaryContentDto.id());
    InputStreamResource resource = new InputStreamResource(inputStream);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + binaryContentDto.fileName() + "\""
        )
        .header(HttpHeaders.CONTENT_TYPE, binaryContentDto.contentType())
        .contentLength(binaryContentDto.size())
        .body(resource);
  }
  
  @Override
  public void delete(UUID id) {
    Path path = resolvePath(id);
    try {
      Files.deleteIfExists(path);
    } catch (IOException e) {
      throw new RuntimeException("파일 삭제에 실패했습니다. id=" + id, e);
    }
  }

  private Path resolvePath(UUID id) {
    return root.resolve(id.toString());
  }
}
