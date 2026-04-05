package com.sprint.mission.discodeit.repository.file;


import com.sprint.mission.discodeit.entity.Entity;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FileSaveLoadFactory {


  private final FileLockProvider fileLockProvider;

  public <T extends Entity> FileSaveLoad<T> createSaveLoad() {
    return new FileSaveLoad<>(fileLockProvider);
  }
}