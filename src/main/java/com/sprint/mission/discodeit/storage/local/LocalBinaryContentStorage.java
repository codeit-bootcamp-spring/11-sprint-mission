package com.sprint.mission.discodeit.storage.local;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import com.sprint.mission.discodeit.storage.DownloadResult;
import com.sprint.mission.discodeit.util.FileLockProvider;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "local")
@Component
public class LocalBinaryContentStorage implements BinaryContentStorage {

  private final Path root;
  private final FileLockProvider fileLockProvider;

  public LocalBinaryContentStorage(@Value("${discodeit.storage.local.root-path}") String rootPath,
      FileLockProvider fileLockProvider) {
    this.root = Paths.get(rootPath);
    this.fileLockProvider = fileLockProvider;
  }

  @PostConstruct
  public void init() {
    try {
      Files.createDirectories(root);
    } catch (IOException e) {
      throw new RuntimeException("failed to create root path dir. ❌", e);
    }
  }

  @Override
  public UUID put(UUID id, byte[] bytes) {
    Path path = this.resolvePath(id);
    ReentrantLock lock = this.fileLockProvider.getLock(path);
    lock.lock();
    try (OutputStream os = Files.newOutputStream(path)) {
      os.write(bytes);
    } catch (IOException e) {
      throw new RuntimeException("failed to store binary content id: " + id + ". ❌", e);
    } finally {
      lock.unlock();
    }
    return id;
  }

  @Override
  public InputStream get(UUID id) {
    Path path = this.resolvePath(id);
    try {
      return Files.newInputStream(path);
    } catch (IOException e) {
      return null;
    }
  }

  @Override
  public DownloadResult download(BinaryContentResponse dto) {
    InputStream inputStream = this.get(dto.id());
    Resource resource = new InputStreamResource(inputStream);
    return new DownloadResult.Stream(resource, dto.fileName(), dto.contentType(), dto.size());
  }

  private Path resolvePath(UUID id) {
    return root.resolve(id.toString());
  }
}
