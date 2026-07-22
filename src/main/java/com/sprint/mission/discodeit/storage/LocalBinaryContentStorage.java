package com.sprint.mission.discodeit.storage;


import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentDto;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
@RequiredArgsConstructor

public class LocalBinaryContentStorage implements BinaryContentStorage {


  @Value("${discodeit.storage.local.root-path}")
  private String rootPath;
  private Path root;


  //root 초기화
  @PostConstruct

  public void init() {
    this.root = Path.of(this.rootPath);
  }


  @Override
  public UUID put(UUID id, byte[] bytes) {

    //시간 체크
//    try {
//      Thread.sleep(3000);
//    } catch (InterruptedException e) {
//      Thread.currentThread().interrupt();
//      throw new RuntimeException("Thread interrupted while simulating delay", e);
//    }

    Path filePath = resolvePath(id);
    try {
      Files.createDirectories(filePath.getParent());
      Files.write(filePath, bytes);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return id;
  }

  @Override
  public InputStream get(UUID id) {

    Path filePath = resolvePath(id);

    try {
      return Files.newInputStream(filePath);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentDto binaryContentDto) {

    Resource resource = new InputStreamResource(get(binaryContentDto.id()));

    return ResponseEntity.ok()
        .header("Content-Disposition",
            "attachment; filename=\"" + binaryContentDto.fileName() + "\"")
        .body(resource);
  }


  private Path resolvePath(UUID id) {

    return root.resolve(id.toString() + ".dat");

  }
}
