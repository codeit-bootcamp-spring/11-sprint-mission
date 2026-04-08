package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;

import java.io.*;
import java.util.*;

public class FileBinaryContentRepository implements BinaryContentRepository {

  private final File file;
  private final Map<UUID, BinaryContent> data;

  public FileBinaryContentRepository(String path) {
    this.file = new File(path);
    this.data = load();
  }

  @Override
  public BinaryContent save(BinaryContent binaryContent) {
    data.put(binaryContent.getId(), binaryContent);
    saveToFile();
    return binaryContent;
  }

  @Override
  public BinaryContent findById(UUID id) {
    return data.get(id);
  }

  @Override
  public List<BinaryContent> findAll() {
    return new ArrayList<>(data.values());
  }

  @Override
  public void delete(UUID id) {
    data.remove(id);
    saveToFile();
  }

  @SuppressWarnings("unchecked")
  private Map<UUID, BinaryContent> load() {
    if (!file.exists() || file.length() == 0) {
      return new HashMap<>();
    }

    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
      Object obj = ois.readObject();
      if (obj instanceof Map<?, ?>) {
        return (Map<UUID, BinaryContent>) obj;
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
