package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "BinaryContent")
@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;

  @GetMapping("/{binaryContentId}")
  public BinaryContentDto getBinaryContent(
      @PathVariable UUID binaryContentId) {
    return binaryContentService.findById(binaryContentId);
  }

  @GetMapping
  public List<BinaryContentDto> getMultipleBinaryContent(
      @RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
    return binaryContentService.findAllByIdIn(binaryContentIds);
  }

  @GetMapping("/{binaryContentId}/download")
  public ResponseEntity<?> download(@PathVariable UUID binaryContentId) {
    BinaryContentDto dto = binaryContentService.findById(binaryContentId);

    return binaryContentStorage.download(dto);
  }
}
