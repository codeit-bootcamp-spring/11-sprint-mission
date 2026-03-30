package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileBinaryContentRepository implements BinaryContentRepository {

  private Map<UUID, BinaryContent> data = new ConcurrentHashMap<>();
  private final String fileDirectory;
  private final String filePath;
  private final FileLockProvider fileLockProvider;

  public FileBinaryContentRepository(
      @Value("${discodeit.repository.file-directory}") String fileDirectory,
      FileLockProvider fileLockProvider) {
    this.fileDirectory = fileDirectory;
    this.filePath = fileDirectory + "BinaryContent.ser";
    this.fileLockProvider = fileLockProvider;
    new File(fileDirectory).mkdirs(); // 디렉토리 없으면 생성
    loadFromFile();
  }


  private void saveToFile() {
    Path path = Paths.get(filePath);
    ReentrantLock lock = fileLockProvider.getLock(path);
    lock.lock();
    try {
      File change = new File(filePath);
      File temp = new File(filePath + ".temp");

      try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp))) {
        oos.writeObject(data);
        Files.move(temp.toPath(), change.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        temp.delete();
        e.printStackTrace();
      }
    } finally {
      lock.unlock();
    }
  }

  private void loadFromFile() {
    Path path = Paths.get(filePath);
    ReentrantLock lock = fileLockProvider.getLock(path);
    lock.lock();
    try {
      File file = new File(filePath);
      if (!file.exists()) {
        return;
      }
      try (FileInputStream fis = new FileInputStream(file);
          ObjectInputStream ois = new ObjectInputStream(fis)) {
        this.data = new ConcurrentHashMap<>((Map<UUID, BinaryContent>) ois.readObject());
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    } finally {
      lock.unlock();
    }

  }


  @Override
  public BinaryContent create(BinaryContent binaryContent) {
    data.put(binaryContent.getId(), binaryContent);
    saveToFile();
    return binaryContent;
  }

  @Override
  public BinaryContent readById(UUID id) {
    return data.get(id);
  }

  @Override
  public BinaryContent readByUserId(UUID userId) {
    return data.values().stream()
        .filter(binaryContent -> binaryContent.getUserId().equals(userId))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<BinaryContent> readAllByMessageId(UUID messageId) {
    return data.values().stream()
        .filter(binaryContent -> binaryContent.getMessageId().equals(messageId))
        .toList();
  }

  @Override
  public void delete(UUID id) {
    data.remove(id);
    saveToFile();
  }

  @Override
  public void deleteByMessageId(UUID messageId) {
    data.values().removeIf(binaryContent -> binaryContent.getMessageId().equals(messageId));
    saveToFile();
  }

  @Override
  public void deleteByUserId(UUID userId) {
    data.values().removeIf(binaryContent -> binaryContent.getUserId().equals(userId));
    saveToFile();
  }
}
