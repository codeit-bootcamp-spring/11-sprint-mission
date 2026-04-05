package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
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
  public ResponseEntity<BinaryContentDto> getBinaryContent(
      @PathVariable UUID binaryContentId) {
    BinaryContentDto content =
        binaryContentService.findById(binaryContentId);
    return ResponseEntity.ok(content);
  }

  @GetMapping
  public List<BinaryContentDto> getMultipleBinaryContent(
      @RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
    return binaryContentService.findAllByIdIn(binaryContentIds);
  }
}
