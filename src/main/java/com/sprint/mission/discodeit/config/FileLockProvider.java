package com.sprint.mission.discodeit.config;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class FileLockProvider {

  private final Map<Path, ReentrantLock> locks = new ConcurrentHashMap<>();

  public ReentrantLock getLock(String path) {
    return locks.computeIfAbsent(Path.of(path), k -> new ReentrantLock());
  }
}