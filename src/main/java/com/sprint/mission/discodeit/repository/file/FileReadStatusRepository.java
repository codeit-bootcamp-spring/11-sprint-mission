package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;

import java.io.*;
import java.util.*;

public class FileReadStatusRepository implements ReadStatusRepository {

  private final File file;
  private final Map<UUID, ReadStatus> data;

  public FileReadStatusRepository(String path) {
    this.file = new File(path);
    this.data = load();
  }

  @Override
  public ReadStatus save(ReadStatus readStatus) {
    data.put(readStatus.getId(), readStatus);
    saveToFile();
    return readStatus;
  }

  @Override
  public ReadStatus findById(UUID id) {
    return data.get(id);
  }

  @Override
  public List<ReadStatus> findAll() {
    return new ArrayList<>(data.values());
  }

  @Override
  public void delete(UUID id) {
    data.remove(id);
    saveToFile();
  }

  @Override
  public ReadStatus findByUserIdAndChannelId(UUID id, UUID channelId) {
    return data.values().stream()
        .filter(readStatus ->
            readStatus.getUserId().equals(id) &&
                readStatus.getChannelId().equals(channelId)
        ).findFirst()
        .orElse(null);
  }

  @Override
  public List<ReadStatus> findByUserId(UUID id) {
    return data.values().stream()
        .filter(readStatus ->
            readStatus.getUserId().equals(id))
        .toList();
  }

  @Override
  public List<ReadStatus> findByChannelId(UUID channelId) {
    return data.values().stream()
        .filter(readStatus ->
            readStatus.getChannelId().equals(channelId))
        .toList();
  }

  @SuppressWarnings("unchecked")
  private Map<UUID, ReadStatus> load() {
    if (file.exists() || file.length() == 0) {
      return new HashMap<>();
    }

    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
      Object obj = ois.readObject();
      if (obj instanceof Map<?, ?>) {
        return (Map<UUID, ReadStatus>) obj;
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
    if (parent != null && !parent.exists()) {
      parent.mkdirs();
    }

    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
      oos.writeObject(data);
    } catch (Exception e) {
      throw new IllegalStateException("BinaryContent 데이터 저장 실패 : " + file.getAbsolutePath(), e);
    }
  }
}
