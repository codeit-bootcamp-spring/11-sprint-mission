package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
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
public class FileUserRepository implements UserRepository {

  private final FileLockProvider fileLockProvider;
  private final Path filePath = Paths.get("user.ser");

  public FileUserRepository(FileLockProvider fileLockProvider) {
    this.fileLockProvider = fileLockProvider;
  }

  private void saveUsers(List<User> users) {
    try (FileOutputStream fos = new FileOutputStream("user.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
    ) {
      oos.writeObject(users);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<User> loadUsers() {
    File file = new File("user.ser");

    if (!file.exists()) {
      return new ArrayList<>();
    }

    try (FileInputStream fis = new FileInputStream("user.ser");
        ObjectInputStream ois = new ObjectInputStream(fis)) {
      return (List<User>) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  @Override
  public void save(User user) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      List<User> users = loadUsers();
      users.removeIf(u -> u.getId().equals(user.getId()));
      users.add(user);
      saveUsers(users);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Optional<User> findById(UUID id) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadUsers().stream()
          .filter(user -> user.getId().equals(id))
          .findFirst();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public List<User> findAll() {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadUsers();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void delete(UUID id) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      List<User> users = loadUsers();
      users.removeIf(user -> user.getId().equals(id));
      saveUsers(users);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean existsByUsername(String name) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadUsers().stream().anyMatch(user ->
          user.getUsername().equals(name));
    } finally {
      lock.unlock();
    }
  }

  @Override
  public boolean existsByEmail(String email) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadUsers().stream().anyMatch(user ->
          user.getEmail().equals(email));
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Optional<User> findByUsername(String username) {
    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try {
      return loadUsers().stream().filter(user ->
          user.getUsername().equals(username)).findFirst();
    } finally {
      lock.unlock();
    }
  }
}