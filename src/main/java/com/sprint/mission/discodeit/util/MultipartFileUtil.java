package com.sprint.mission.discodeit.util;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

public class MultipartFileUtil {

  public static Optional<BinaryContentCreateRequest> toCreateRequest(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      return Optional.empty();
    }
    try {
      return Optional.of(new BinaryContentCreateRequest(
          file.getBytes(),
          file.getOriginalFilename(),
          file.getContentType(),
          file.getSize()
      ));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static List<BinaryContentCreateRequest> toCreateRequests(List<MultipartFile> files) {
    if (files == null || files.isEmpty()) {
      return new ArrayList<>();
    }
    return files.stream()
        .flatMap(file -> toCreateRequest(file).stream())
        .toList();
  }
}

