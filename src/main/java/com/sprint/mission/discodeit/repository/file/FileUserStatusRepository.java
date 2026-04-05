package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
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
public class FileUserStatusRepository implements UserStatusRepository {

  private final FileLockProvider fileLockProvider;
  private final Path filePath = Paths.get("user_status.ser");

  public FileUserStatusRepository(FileLockProvider fileLockProvider) {
    this.fileLockProvider = fileLockProvider;
  }

  private void saveAll(List<UserStatus> list) {
    try (ObjectOutputStream oos = new ObjectOutputStream(
        new FileOutputStream("user_status.ser"))) {
      oos.writeObject(list);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<UserStatus> loadAll() {
    File file = new File("user_status.ser");
    if (!file.exists()) {
      return new ArrayList<>();
    }
    try (ObjectInputStream ois = new ObjectInputStream(
        new FileInputStream(file))) {
      return (List<UserStatus>) ois.readObject();
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  @Override
  public UserStatus save(UserStatus userStatus) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      List<UserStatus> list = loadAll();
      list.removeIf(us -> us.getId().equals(userStatus.getId()));
      list.add(userStatus);
      saveAll(list);
      return userStatus;
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Optional<UserStatus> findByUserId(UUID userId) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadAll().stream().filter(us ->
          us.getUserId().equals(userId)).findFirst();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void deleteByUserId(UUID userId) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      List<UserStatus> list = loadAll();
      list.removeIf(us -> us.getUserId().equals(userId));
      saveAll(list);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Optional<UserStatus> findById(UUID id) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadAll().stream().filter(us ->
          us.getId().equals(id)).findFirst();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public List<UserStatus> findAll() {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadAll();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void delete(UUID id) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      List<UserStatus> list = loadAll();
      list.removeIf(us -> us.getId().equals(id));
      saveAll(list);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean existsByUserId(UUID userId) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadAll().stream().anyMatch(us ->
          us.getUserId().equals(userId));
    } finally {
      lock.unlock();
    }
  }
}
