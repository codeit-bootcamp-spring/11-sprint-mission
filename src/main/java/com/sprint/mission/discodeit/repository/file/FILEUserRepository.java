package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FILEUserRepository implements UserRepository {

  private final FileSaveLoad<User> saveLoad;
  private final Path directory;

  public FILEUserRepository(@Value("${discodeit.repository.file-dir}") String path,
      FileSaveLoadFactory factory) {
    this.saveLoad = factory.createSaveLoad();
    this.directory = Path.of(path + "/Users/");
  }


  @Override
  public boolean saveUser(User user) {

    if (user == null) {
      return false;
    }
    save(idToPath(user.getId()), user);
    return true;
  }

  @Override
  public Optional<User> getUser(UUID userId) {

    Map<UUID, User> users = load(directory);
    return Optional.ofNullable(users.get(userId));
  }

  @Override
  public List<User> getAllUser() {

    Map<UUID, User> users = load(directory);
    return users.values().stream().toList();

  }

  @Override
  public Optional<User> getUserByNickname(String nickname) {

    Map<UUID, User> users = load(directory);
    return users.values().stream().filter(user -> user.getNickname().equals(nickname)).findFirst();
  }

  @Override
  public boolean deleteUser(UUID userId) {
    if (!isExistUser(userId)) {
      return false;
    }
    try {
      Files.deleteIfExists(idToPath(userId));
    } catch (IOException e) {
      return false;

    }
    return true;

  }

  @Override
  public boolean isExistUserByNickname(String nickname) {
    Map<UUID, User> users = load(directory);
    return users.values().stream().anyMatch(user -> user.getNickname().equals(nickname));
  }

  @Override
  public boolean isExistUserByEmail(String email) {

    Map<UUID, User> users = load(directory);
    return users.values().stream().anyMatch(user -> user.getEmail().equals(email));
  }

  @Override
  public boolean isExistUser(UUID userId) {
    Map<UUID, User> users = load(directory);
    return users.containsKey(userId);
  }


  private Map<UUID, User> load(Path directory) {
    return saveLoad.load(directory);
  }

  private void save(Path filePath, User user) {
    saveLoad.save(filePath, user);

  }

  private Path idToPath(UUID userId) {

    return directory.resolve(userId + ".dat");

  }


}
