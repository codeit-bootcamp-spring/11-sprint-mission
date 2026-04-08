package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;

import java.io.*;
import java.util.*;

public class FileUserStatusRepository implements UserStatusRepository {

  private final File file;
  private final Map<UUID, UserStatus> data;

  public FileUserStatusRepository(String path) {
    this.file = new File(path);
    this.data = load();
  }

  @Override
  public UserStatus save(UserStatus userStatus) {
    data.put(userStatus.getId(), userStatus);
    saveToFile();
    return userStatus;
  }

  @Override
  public UserStatus findById(UUID id) {
    return data.get(id);
  }

  @Override
  public List<UserStatus> findAll() {
    return new ArrayList<>(data.values());
  }

  @Override
  public void delete(UUID id) {
    data.remove(id);
    saveToFile();
  }

  @Override
  public UserStatus findByUserId(UUID userId) {
    return data.values().stream()
        .filter(userStatus ->
            userStatus.getUserId().equals(userId))
        .findFirst()
        .orElse(null);
  }

  @SuppressWarnings("unchecked")
  private Map<UUID, UserStatus> load() {
    if (!file.exists() || file.length() == 0) {
      return new HashMap<>();
    }

    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
      Object obj = ois.readObject();
      if (obj instanceof Map<?, ?>) {
        return (Map<UUID, UserStatus>) obj;
      }
      return new HashMap<>();
    } catch (EOFException e) {
      return new HashMap<>();
    } catch (Exception e) {
      throw new IllegalStateException("BinaryContent 데이터 로드 실패 : " + file.getAbsolutePath(), e);
    }
  }

  private void saveToFile() {
    File parent = file.getParentFile();
    if (parent == null && !parent.exists()) {
      parent.mkdirs();
    }

    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
      oos.writeObject(data);
    } catch (Exception e) {
      throw new IllegalStateException("BinaryContent 데이터 저장 실패 : " + file.getAbsolutePath(), e);
    }
  }
}
