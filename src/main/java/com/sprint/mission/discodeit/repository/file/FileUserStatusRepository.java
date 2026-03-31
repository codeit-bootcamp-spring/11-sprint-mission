package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.util.FileLockProvider;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileUserStatusRepository implements UserStatusRepository {

  private final String STATUS_FILE = "userStatus.ser";
  private final File file;
  private final Map<UUID, UserStatus> userStatusMap;

  private final FileLockProvider fileLockProvider;

  public FileUserStatusRepository(
      @Value("${discodeit.repository.file-directory}") String fileDirectory,
      FileLockProvider fileLockProvider
  ) {
    this.fileLockProvider = fileLockProvider;
    File dir = new File(fileDirectory);

    if (!dir.exists()) {
      dir.mkdirs();
    }
    this.file = new File(dir, STATUS_FILE);
    this.userStatusMap = loadStatus();
  }

  private Map<UUID, UserStatus> loadStatus() {
    if (!file.exists()) {
      return new HashMap<>();
    }

    Path path = file.toPath();
    ReentrantLock lock = fileLockProvider.getLock(path);
    lock.lock();

    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
      return (Map<UUID, UserStatus>) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException("콘텐츠 로드실패", e);
    } finally {
      lock.unlock();
    }
  }

  private void saveContents() {
    Path path = file.toPath();
    ReentrantLock lock = fileLockProvider.getLock(path);

    lock.lock();
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
      oos.writeObject(userStatusMap);
    } catch (IOException e) {
      throw new RuntimeException("유저 저장실패", e);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void save(UserStatus status) {
    userStatusMap.put(status.getId(), status);
    saveContents();
  }

  @Override
  public Optional<UserStatus> findById(UUID id) {
    return Optional.ofNullable(userStatusMap.get(id));
  }

  @Override
  public Optional<UserStatus> findByUserId(UUID userId) {
    return userStatusMap.values().stream()
        .filter(status -> status.getUserId().equals(userId))
        .findFirst();
  }

  @Override
  public List<UserStatus> findAll() {
    return new ArrayList<>(userStatusMap.values());
  }

  @Override
  public void deleteByUserId(UUID userId) {
    userStatusMap.entrySet()
        .removeIf(entry -> entry.getValue().getUserId().equals(userId));
    saveContents();
  }

}
