package com.sprint.mission.discodeit.controller;

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

  // GET /api/binaryContents/{id} - 200 OK
  @GetMapping("/{id}")
  public ResponseEntity<BinaryContent> read(@PathVariable UUID id) {
    return ResponseEntity.ok(binaryContentService.read(id));
  }

  // GET /api/binaryContents?ids=1,2,3 - 200 OK
  @GetMapping
  public ResponseEntity<List<BinaryContent>> readAllByIdIn(@RequestParam List<UUID> ids) {
    return ResponseEntity.ok(binaryContentService.readAllByIdIn(ids));
  }
}
