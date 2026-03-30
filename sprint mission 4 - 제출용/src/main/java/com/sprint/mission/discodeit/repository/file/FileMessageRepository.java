package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileMessageRepository implements MessageRepository {

  private Map<UUID, Message> data = new ConcurrentHashMap<>();
  private Map<UUID, Message> data_at = new ConcurrentHashMap<>();

  private final String fileDirectory;
  private final String filePath;
  private final FileLockProvider fileLockProvider;

  public FileMessageRepository(
      @Value("${discodeit.repository.file-directory}") String fileDirectory,
      FileLockProvider fileLockProvider) {
    this.fileDirectory = fileDirectory;
    this.filePath = fileDirectory + "Message.ser";
    this.fileLockProvider = fileLockProvider;
    new File(fileDirectory).mkdirs(); // 디렉토리 없으면 생성
    loadFromFile();
  }

  private void saveToFile() {
    Path path = Paths.get(filePath);
    ReentrantLock lock = fileLockProvider.getLock(path);
    lock.lock();
    try {
      File change = new File(filePath);
      File temp = new File(filePath + ".temp");

      try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp))) {
        oos.writeObject(data);
        Files.move(temp.toPath(), change.toPath(), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        temp.delete();
        e.printStackTrace();
      }
    } finally {
      lock.unlock();
    }

  }

  private void loadFromFile() {
    Path path = Paths.get(filePath);
    ReentrantLock lock = fileLockProvider.getLock(path);
    lock.lock();
    try {
      File file = new File(filePath);
      if (!file.exists()) {
        return;
      }
      try (FileInputStream fis = new FileInputStream(file);
          ObjectInputStream ois = new ObjectInputStream(fis)) {
        this.data = new ConcurrentHashMap<>((Map<UUID, Message>) ois.readObject());
      } catch (IOException | ClassNotFoundException e) {
        e.printStackTrace();
      }
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Message create(Message message) {
    data.put(message.getId(), message);
    saveToFile();
    return message;
  }

  @Override
  public Message read(UUID id) {
    return data.get(id);
  }
  // 여기서 메세지는 보낸사람, 받는 사람 포함임

  @Override
  public List<Message> readAll() {
    return new ArrayList<>(data.values());
  }

  @Override
  public List<Message> readAllByChannelId(UUID channelId) {
    return data.values().stream()
        .filter(message -> message.getChannelId().equals(channelId))
        .toList();
  }

  @Override
  public Message update(Message message) {
    data.put(message.getId(), message);
    saveToFile();
    return message;
  }


  @Override
  public void delete(UUID id) {
    data_at = new ConcurrentHashMap<>();
    data_at.put(id, data.get(id));
    data.remove(id);
    saveToFile();
  }

  @Override
  public void deleteAllByChannelId(UUID channelId) {
    data.entrySet().removeIf(entry -> entry.getValue().getChannelId().equals(channelId));
    saveToFile();
  }

  @Override
  public String toString() {
    return data.toString();
  }

}
