package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.config.FileLockProvider;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class FileMessageRepository implements MessageRepository {

  private final String directory;
  private final String filePath;
  private final Map<UUID, Message> store;
  private final FileLockProvider fileLockProvider;

  public FileMessageRepository(String directory, FileLockProvider fileLockProvider) {
    this.directory = directory;
    this.fileLockProvider = fileLockProvider;
    this.filePath = directory + File.separator + "message.db";
    this.store = load();
  }

  @SuppressWarnings("unchecked")
  private Map<UUID, Message> load() {
    java.util.concurrent.locks.ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
      return (Map<UUID, Message>) ois.readObject();
    } catch (FileNotFoundException | EOFException e) {
      return new HashMap<>();
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap<>();
    } finally {
      lock.unlock();
    }
  }

  // 메모리의 store를 파일로 저장하는 메서드
  private void saveToFile() {
    java.util.concurrent.locks.ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
      oos.writeObject(store);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Message save(Message message) {
    store.put(message.getId(), message);
    saveToFile();
    return message;
  }

  @Override
  public Optional<Message> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<Message> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public void delete(UUID id) {
    store.remove(id);
    saveToFile();
  }

  @Override
  public List<Message> findByAuthorId(UUID authorId) {
    List<Message> result = new ArrayList<>();
    for (Message message : store.values()) {
      if (message.getAuthorId().equals(authorId)) {
        result.add(message);
      }
    }
    return result;
  }

  @Override
  public List<Message> findByChannelId(UUID channelId) {
    List<Message> result = new ArrayList<>();
    for (Message message : store.values()) {
      if (message.getChannelId().equals(channelId)) {
        result.add(message);
      }
    }
    return result;
  }

  @Override
  public Optional<Instant> findLatestCreatedAtByChannelId(UUID channelId) {
    return store.values().stream()
        .filter(m -> m.getChannelId().equals(channelId))
        .map(Message::getCreatedAt)
        .max(Comparator.naturalOrder());
  }

  @Override
  public void deleteAllByChannelId(UUID channelId) {
    store.entrySet().removeIf(entry -> entry.getValue().getChannelId().equals(channelId));
    saveToFile();
  }

  @Override
  public void deleteById(UUID id) {
    store.remove(id);
    saveToFile();
  }

}