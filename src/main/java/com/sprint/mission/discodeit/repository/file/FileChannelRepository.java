package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.config.FileLockProvider;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
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
import java.util.Optional;
import java.util.UUID;

public class FileChannelRepository implements ChannelRepository {

  private final String directory;
  private final String filePath;
  private final Map<UUID, Channel> store;
  private final FileLockProvider fileLockProvider;
  private final ReadStatusRepository readStatusRepository;

  // 생성자에서 디렉터리 경로를 받아옴
  public FileChannelRepository(String directory, FileLockProvider fileLockProvider,
      ReadStatusRepository readStatusRepository) {  // 추가
    this.directory = directory;
    this.fileLockProvider = fileLockProvider;
    this.readStatusRepository = readStatusRepository;  // 추가
    this.filePath = directory + File.separator + "channel.db";
    this.store = load();
  }

  // 파일에서 데이터를 읽어오는 메서드
  @SuppressWarnings("Unchecked")
  private Map<UUID, Channel> load() {
    java.util.concurrent.locks.ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
      return (Map<UUID, Channel>) ois.readObject();
    } catch (FileNotFoundException | EOFException e) {
      return new HashMap<>();
    } catch (Exception e) {
      e.printStackTrace();
      return new HashMap<>();
    } finally {
      lock.unlock();
    }
  }

  //메모리의 store를 파일로 저장하는 메서드
  private void saveToFile() {
    java.util.concurrent.locks.ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
      oos.writeObject(store);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      lock.unlock();
    }
  }

  @Override
  public Channel save(Channel channel) {
    store.put(channel.getId(), channel);
    saveToFile();
    return channel;
  }

  @Override
  public Optional<Channel> findById(UUID id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Optional<Channel> findByName(String name) {
    return store.values().stream()
        .filter(channel -> channel.getName().equals(name))
        .findFirst();
  }


  @Override
  public void deleteById(UUID id) {
    store.remove(id);
    saveToFile();
  }

  @Override
  public List<Channel> findAll() {
    return new ArrayList<>(store.values());
  }

  @Override
  public List<Channel> findAllByIsPrivate(boolean isPrivate) {
    List<Channel> result = new ArrayList<>();
    for (Channel ch : store.values()) {
      if (ch.isPrivate() == isPrivate) {
        result.add(ch);
      }
    }
    return result;
  }

  @Override
  public List<Channel> findAllPrivateByUserId(UUID userId) {
    List<UUID> channelIds = readStatusRepository.findAllByUserId(userId)
        .stream()
        .map(ReadStatus::getChannelId)
        .toList();

    return store.values().stream()
        .filter(ch -> ch.isPrivate() && channelIds.contains(ch.getId()))
        .toList();
  }

  @Override
  public void delete(UUID id) {
    store.remove(id);
    saveToFile();
  }
}
