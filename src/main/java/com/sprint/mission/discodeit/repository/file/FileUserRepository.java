package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.config.FileLockProvider;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class FileUserRepository implements UserRepository {

  private final String directory;
  private final String filePath;
  private final Map<UUID, User> store;
  private FileLockProvider fileLockProvider = null; // 락 관리 객체 추가

  public FileUserRepository(String fileDirectory, FileLockProvider fileLockProvider) {
    this.directory = fileDirectory;
    this.filePath = directory + File.separator + "user.db";
    this.fileLockProvider = fileLockProvider;
    this.store = load();
  }

  @SuppressWarnings("unchecked")
  private Map<UUID, User> load() {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
      return (Map<UUID, User>) ois.readObject();
    } catch (FileNotFoundException | EOFException e) {
      return new HashMap<>();
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap<>();
    } finally {
      lock.unlock();
    }

  }

  // 저장
  @Override
  public User save(User user) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      store.put(user.getId(), user);
      saveToFile();
      return user;
    } finally {
      lock.unlock();
    }

  }

  @Override
  public Optional<User> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Optional<User> findByUsername(String username) {
    return store.values().stream()
        .filter(user -> Objects.equals(user.getUsername(), username))
        .findFirst();
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return store.values().stream()
        .filter(user -> Objects.equals(user.getEmail(), email))
        .findFirst();
  }

  @Override
  public List<User> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public boolean existsByUsername(String username) {
    return store.values().stream()
        .anyMatch(user -> Objects.equals(user.getUsername(), username));
  }

  @Override
  public boolean existsByEmail(String email) {
    return store.values().stream()
        .anyMatch(user -> Objects.equals(user.getEmail(), email));
  }

  @Override
  public void deleteById(UUID id) {
    store.remove(id);
    saveToFile();
  }

  private void saveToFile() {
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
      oos.writeObject(store);
    } catch (IOException e) {
      throw new RuntimeException("User DB 파일 저장 오류: " + e.getMessage(), e);
    }
  }
}