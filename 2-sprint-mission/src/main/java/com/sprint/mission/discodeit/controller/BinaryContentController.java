package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;

  @GetMapping("/{binaryContentId}")
  public ResponseEntity<BinaryContentDto.Response> find(
      @PathVariable UUID binaryContentId) {
    BinaryContentDto.Response binaryContent = binaryContentService.findById(binaryContentId);
    return ResponseEntity.ok(binaryContent);
  }

  @GetMapping
  public ResponseEntity<List<BinaryContentDto.Response>> findAllByIdIn(
      @Valid @RequestParam List<UUID> binaryContentIds) {
    List<BinaryContentDto.Response> binaryContents = binaryContentService.findAllByIdIn(
        binaryContentIds);
    return ResponseEntity.ok(binaryContents);
  }

  @GetMapping("/{binaryContentId}/download")
  public ResponseEntity<?> download(
      @PathVariable UUID binaryContentId) {
    BinaryContentDto.Response responseDto = binaryContentService.findById(binaryContentId);
    return binaryContentStorage.download(responseDto);
  }
}