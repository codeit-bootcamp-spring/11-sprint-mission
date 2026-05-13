package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
// name이 "local"일때만 이 Bean을 등록
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root;

  public LocalBinaryContentStorage(
      @Value("${discodeit.storage.local.root-path}") String rootPath) {
    this.root = Path.of(rootPath);
  }

  @PostConstruct // 빈 생성 직후 호출(생성자)
  public void init() {
    try {
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new IllegalStateException("스토리지 루트 디렉토리 생성 실패", e);
    }
  }

  private Path resolvePath(UUID id) {
    return root.resolve(id.toString());
  }

  // 저장
  @Override
  public UUID put(UUID id, byte[] bytes) {
    Path path = resolvePath(id);
    try (OutputStream os = Files.newOutputStream(path)) {
      os.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException("파일 저장 실패 : " + id, e);
    }
    return id; // BinaryContentId
  }

  // 조회
  @Override
  public InputStream get(UUID id) {
    try {
      return Files.newInputStream(resolvePath(id));
    } catch (IOException e) {
      throw new RuntimeException("파일 조회 실패 : " + id, e);
    }
  }

  // 다운로드 API
  @Override
  public ResponseEntity<?> download(BinaryContentDto dto) {
    Resource resource = new InputStreamResource(get(dto.id()));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + dto.fileName() + "\"")
        .header(HttpHeaders.CONTENT_TYPE, dto.contentType())
        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(dto.size()))
        .body(resource);
  }

}
