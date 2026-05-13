package com.sprint.mission.discodeit.storage;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "discodeit.storage.type", havingValue = "test")
public class TestBinaryContentStorage implements BinaryContentStorage {

  private final Map<UUID, byte[]> store = new ConcurrentHashMap<>();

  @Override
  public UUID put(UUID id, byte[] bytes) {
    store.put(id, bytes);
    return id;
  }

  @Override
  public InputStream get(UUID id) {
    return new ByteArrayInputStream(store.getOrDefault(id, new byte[0]));
  }

  @Override
  public ResponseEntity<Resource> download(BinaryContentDto dto) {
    byte[] data = store.getOrDefault(dto.id(), new byte[0]);
    return ResponseEntity.ok().body(new ByteArrayResource(data));
  }
}
