package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.util.FileLockProvider;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(prefix = "discodeit.repository", name = "type", havingValue = "file")
public class FileBinaryContentRepository implements BinaryContentRepository {

  private final FileLockProvider fileLockProvider;
  private final Path filePath = Paths.get("BinaryContent.ser");

  public FileBinaryContentRepository(FileLockProvider fileLockProvider) {
    this.fileLockProvider = fileLockProvider;
  }

  private void saveAll(List<BinaryContent> list) {
    try (ObjectOutputStream oos = new ObjectOutputStream(
        new FileOutputStream("binary_content.ser"))) {
      oos.writeObject(list);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<BinaryContent> loadAll() {
    File file = new File("binary_content.ser");
    if (!file.exists()) {
      return new ArrayList<>();
    }
    try (ObjectInputStream ois = new ObjectInputStream(
        new FileInputStream(file))) {
      return (List<BinaryContent>) ois.readObject();
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  @Override
  public BinaryContent save(BinaryContent binaryContent) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      List<BinaryContent> list = loadAll();
      list.removeIf(bc -> bc.getId().equals(binaryContent.getId()));
      list.add(binaryContent);
      saveAll(list);
      return binaryContent;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void delete(UUID id) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      List<BinaryContent> list = loadAll();
      list.removeIf(bc -> bc.getId().equals(id));
      saveAll(list);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Optional<BinaryContent> findById(UUID id) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadAll().stream().filter(bc ->
          bc.getId().equals(id)).findFirst();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public List<BinaryContent> findAllByIdIn(List<UUID> ids) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadAll().stream().filter(bc ->
          ids.contains(bc.getId())).toList();
    } finally {
      lock.unlock();
    }
  }
}
