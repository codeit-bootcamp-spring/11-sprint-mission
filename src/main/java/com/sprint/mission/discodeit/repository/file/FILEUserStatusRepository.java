package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.UserStatusRepository;

import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")

public class FILEUserStatusRepository implements UserStatusRepository {

  //UserStatus는 저장시 UserId를 키로 하므로 saveLoad를 안쓰고 새로 만듬
  private final Path directory;
  private final FileSaveLoad<UserStatus> saveLoad;


  public FILEUserStatusRepository(@Value("${discodeit.repository.file-dir}") String path,
      FileSaveLoadFactory factory) {
    this.directory = Path.of(path + "/UserStatuses/");
    this.saveLoad = factory.createSaveLoad();

  }

  @Override
  public boolean saveUserStatus(UserStatus userStatus) {

    if (userStatus == null) {
      return false;
    }

    save(idToPath(userStatus.getUserId()), userStatus);
    return true;
  }

  @Override
  public Optional<UserStatus> getUserStatus(UUID userId) {
    Map<UUID, UserStatus> userStatuses = load(directory);
    return Optional.ofNullable(userStatuses.get(userId));

  }

  @Override
  public List<UserStatus> getAllUserStatus() {
    Map<UUID, UserStatus> userStatuses = load(directory);
    return userStatuses.values().stream().toList();
  }

  @Override
  public boolean deleteUserStatus(UUID userId) {
    if (!isExistUserStatus(userId)) {
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
  public boolean isExistUserStatus(UUID userId) {
    Map<UUID, UserStatus> userStatuses = load(directory);
    return userStatuses.containsKey(userId);
  }

  private Map<UUID, UserStatus> load(Path directory) {

    return saveLoad.load(directory);

  }

  private void save(Path filePath, UserStatus userStatus) {

    saveLoad.save(filePath, userStatus);
  }


  private Path idToPath(UUID userStatusId) {

    return directory.resolve(userStatusId + ".dat");

  }


}
