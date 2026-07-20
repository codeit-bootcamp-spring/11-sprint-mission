package com.sprint.mission.discodeit.storage.local;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
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
  // 이 메서드를 사용하는 트랜잭션 메서드는 바이너리 데이터 처리로 인한 DB 시간까지 같이 점유
  // 이벤트 리스너에서 커밋 성공시에만 호출하도록 변경하여 트랜잭션 점유 감소
  @Override
  public UUID put(UUID id, byte[] bytes) {
    log.info("BinaryContent id={}, 스레드 이름={}", id, Thread.currentThread().getName());
    // 비동기 처리 간 성능 차이를 비교하기 위해 의도적으로 Thread.sleep() 사용하여 지연 발생(테스트 후 제거 또는 주석 처리)
//    try {
//      // 3초간 지연
//      Thread.sleep(3000);
//    } catch (InterruptedException e) {
//      // 현재 스레드가 sleep 중 다른 스레드에서 이 스레드를 interrupt()로 깨울 시 InterruptedException 발생
//
//      // 현재 스레드가 interrupt()를 받았는지 확인 → true(명시하지 않으면 false)
//      // true일 경우 중단 요청 flag값이 true(실제 중단되지 않음)
//      Thread.currentThread().interrupt();
//
//      throw new RuntimeException("Thread interrupted while simulating delay", e);
//    }

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
  public ResponseEntity<Resource> download(BinaryContentDto dto) {
    Resource resource = new InputStreamResource(get(dto.id()));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + dto.fileName() + "\"")
        .header(HttpHeaders.CONTENT_TYPE, dto.contentType())
        .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(dto.size()))
        .body(resource);
  }

}
