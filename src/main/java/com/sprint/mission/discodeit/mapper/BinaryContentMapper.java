package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BinaryContentMapper {

  private final BinaryContentStorage binaryContentStorage;

  public BinaryContentDto toDto(BinaryContent binaryContent) {

    if (binaryContent == null) {
      return null;
    }

    byte[] b;
    try (InputStream st = binaryContentStorage.get(binaryContent.getId())) {
      b = st.readAllBytes();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    return new BinaryContentDto(

        binaryContent.getId(),
        binaryContent.getFileName(),
        binaryContent.getSize(),
        binaryContent.getContentType(),
        b

    );

  }
}

