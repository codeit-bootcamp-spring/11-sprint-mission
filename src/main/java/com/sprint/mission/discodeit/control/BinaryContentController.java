package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

  private final BinaryContentService binaryContentService;

  @GetMapping("/{binaryContentId}")
  public ResponseEntity<BinaryContentResponse> findBinaryContent(
      @PathVariable UUID binaryContentId) {
    BinaryContent content = binaryContentService.find(binaryContentId);
    return ResponseEntity.ok(BinaryContentResponse.from(content));
  }

  @GetMapping
  public ResponseEntity<List<BinaryContentResponse>> findMultipleBinaryContents(
      @RequestParam("binaryContentIds") List<UUID> binaryContentIds
  ) {
    List<BinaryContent> contents = binaryContentService.findAllByIdIn(binaryContentIds);
    return ResponseEntity.ok(contents.stream()
        .map(BinaryContentResponse::from)
        .toList());
  }
}
