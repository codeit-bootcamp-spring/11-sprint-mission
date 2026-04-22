package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binaryContent.BinaryContentDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.mapper.BinaryContentMapper;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
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
  private final BinaryContentMapper binaryContentMapper;
  private final BinaryContentStorage binaryContentStorage;

  // GET /api/binaryContents/{id} - 200 OK
  @GetMapping("/{id}")
  public ResponseEntity<BinaryContentDto> find(@PathVariable UUID id) {
    BinaryContent binaryContent = binaryContentService.find(id);
    BinaryContentDto binaryContentDto = binaryContentMapper.toDto(binaryContent);
    return ResponseEntity.ok(binaryContentDto);
  }

  // GET /api/binaryContents?ids=1,2,3 - 200 OK
  @GetMapping
  public ResponseEntity<List<BinaryContentDto>> findAllByIdIn(@RequestParam List<UUID> ids) {
    List<BinaryContentDto> binaryContentDtos = binaryContentService.findAllByIdIn(ids).stream()
        .map(binaryContentMapper::toDto)
        .toList();

    return ResponseEntity.ok(binaryContentDtos);
  }

  // GET /api/binaryContents/{binaryContentId}/download
  @GetMapping("/{binaryContentId}/download")
  public ResponseEntity<?> download(@PathVariable UUID binaryContentId) {
    BinaryContent binaryContent = binaryContentService.find(binaryContentId);
    BinaryContentDto binaryContentDto = binaryContentMapper.toDto(binaryContent);
    return binaryContentStorage.download(binaryContentDto);
  }
}
