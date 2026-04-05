package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Entity;


import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor

public class FileSaveLoad<T extends Entity> {


  private final FileLockProvider fileLockProvider;


  public Map<UUID, T> load(Path directory) {
    if (Files.exists(directory)) {

      ReentrantLock lock = fileLockProvider.getLock(directory);
      lock.lock();

      try (Stream<Path> stream = Files.list(directory)) {
        Map<UUID, T> map;

        map = stream.map(path -> {
              try (
                  FileInputStream fis = new FileInputStream(path.toFile());
                  ObjectInputStream ois = new ObjectInputStream(fis)
              ) {
                Object data = ois.readObject();
                return (T) data;
              } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
              }
            })
            .collect(Collectors.toMap(
                T -> (T instanceof UserStatus) ? ((UserStatus) T).getUserId() : T.getId(),
                Function.identity()
            ));
        return map;
      } catch (IOException e) {
        throw new RuntimeException(e);
      } finally {
        lock.unlock();
      }
    } else {
      return new HashMap<>();
    }
  }

  public void save(Path filePath, T typeParam) {

    ReentrantLock lock = fileLockProvider.getLock(filePath);
    lock.lock();

    try {
      Files.createDirectories(filePath.getParent());

      try (
          FileOutputStream fos = new FileOutputStream(filePath.toFile());
          ObjectOutputStream oos = new ObjectOutputStream(fos)
      ) {

        oos.writeObject(typeParam);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      lock.unlock();
    }

  }


}
