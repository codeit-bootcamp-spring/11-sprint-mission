package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.util.FileLockProvider;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
@ConditionalOnProperty(prefix = "discodeit.repository", name = "type", havingValue = "file")
public class FileMessageRepository implements MessageRepository {

  private final FileLockProvider fileLockProvider;
  private final Path filepath = Paths.get("message.ser");

  public FileMessageRepository(FileLockProvider fileLockProvider) {
    this.fileLockProvider = fileLockProvider;
  }

  private void saveMessages(List<Message> messages) {
    try (FileOutputStream fos = new FileOutputStream("message.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fos);
    ) {
      oos.writeObject(messages);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private List<Message> loadMessages() {
    File file = new File("message.ser");

    if (!file.exists()) {
      return new ArrayList<>();
    }

    try (FileInputStream fis = new FileInputStream("message.ser");
        ObjectInputStream ois = new ObjectInputStream(fis)) {
      return (List<Message>) ois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  @Override
  public void save(Message message) {
    ReentrantLock lock = fileLockProvider.getLock(filepath);
    lock.lock();
    try {
      List<Message> messages = loadMessages();
      messages.removeIf(m -> m.getId().equals(message.getId()));
      messages.add(message);
      saveMessages(messages);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Message findById(UUID id) {
    ReentrantLock lock = fileLockProvider.getLock(filepath);
    lock.lock();
    try {
      return loadMessages().stream()
          .filter(message -> message.getId().equals(id))
          .findFirst().orElse(null);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public List<Message> findAll() {
    ReentrantLock lock = fileLockProvider.getLock(filepath);
    lock.lock();
    try {
      return loadMessages();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void delete(UUID id) {
    ReentrantLock lock = fileLockProvider.getLock(filepath);
    lock.lock();
    try {
      List<Message> messages = loadMessages();
      messages.removeIf(message -> message.getId().equals(id));
      saveMessages(messages);
    } finally {
      lock.unlock();
    }
  }

  @Override
  public List<Message> findByChannelId(UUID channelId) {
    ReentrantLock lock = fileLockProvider.getLock(filepath);
    lock.lock();
    try {
      return loadMessages().stream()
          .filter(message ->
              message.getChannelId().equals(channelId)).toList();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public void deleteByChannelId(UUID channelId) {
    ReentrantLock lock = fileLockProvider.getLock(filepath);
    lock.lock();
    try {
      List<Message> messages = loadMessages();
      messages.removeIf(message ->
          message.getChannelId().equals(channelId));
      saveMessages(messages);
    } finally {
      lock.unlock();
    }
  }
}