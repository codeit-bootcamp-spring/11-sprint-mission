package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.util.FileLockProvider;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@ConditionalOnProperty(prefix = "discodeit.repository", name = "type", havingValue = "file")
public class FileReadStatusRepository implements ReadStatusRepository {

  private final FileLockProvider fileLockProvider;
  private final Path filePath = Paths.get("ReadStatus.ser");

  public FileReadStatusRepository(FileLockProvider fileLockProvider) {
    this.fileLockProvider = fileLockProvider;
  }

  private void saveAll(List<ReadStatus> list) {
    try (ObjectOutputStream oos = new ObjectOutputStream(
        new FileOutputStream("read_status.ser"))) {
      oos.writeObject(list);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<ReadStatus> loadAll() {
    File file = new File("read_status.ser");
    if (!file.exists()) {
      return new ArrayList<>();
    }
    try (ObjectInputStream ois = new ObjectInputStream(
        new FileInputStream(file))) {
      return (List<ReadStatus>) ois.readObject();
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  @Override
  public void save(ReadStatus readStatus) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      List<ReadStatus> list = loadAll();
      list.removeIf(rs -> rs.getId().equals(readStatus.getId()));
      list.add(readStatus);
      saveAll(list);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public List<ReadStatus> findByChannelId(UUID channelId) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadAll().stream().filter(rs ->
          rs.getChannelId().equals(channelId)).toList();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public List<ReadStatus> findByUserId(UUID userId) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadAll().stream().filter(rs ->
          rs.getUserId().equals(userId)).toList();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void deleteByChannelId(UUID channelId) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      List<ReadStatus> list = loadAll();
      list.removeIf(rs ->
          rs.getChannelId().equals(channelId));
      saveAll(list);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public ReadStatus findById(UUID id) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadAll().stream().filter(rs ->
          rs.getId().equals(id)).findFirst().orElse(null);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void delete(UUID id) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      List<ReadStatus> list = loadAll();
      list.removeIf(rs -> rs.getId().equals(id));
      saveAll(list);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean existsByUserIdAndChannelId(UUID userId, UUID channelId) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadAll().stream().anyMatch(rs ->
          rs.getUserId().equals(userId) &&
              rs.getChannelId().equals(channelId));
    } finally {
      lock.unlock();
    }
  }
}
