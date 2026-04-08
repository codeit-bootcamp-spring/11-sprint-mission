package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/binary-contents")
@RequiredArgsConstructor
public class BinaryContentController {

  private final BinaryContentService binaryContentService;

  // BinaryContent create()는 직접 생성 X -> User/Message에서 생성

  @GetMapping("/{binaryContentId}")
  public ResponseEntity<BinaryContentResponse> findById(
      // find -> findById 변수명 변경, 동사형이라서 다시 원복
      @PathVariable UUID binaryContentId
  ) {
    return ResponseEntity.ok(
        binaryContentService.findEntitybyId(binaryContentId));
  }

  @GetMapping
  public ResponseEntity<List<BinaryContentResponse>> findAllByIdIn(
      @RequestParam List<UUID> ids) {
    return ResponseEntity.ok(binaryContentService.findAllByIdIn(ids));
  }

  @DeleteMapping("/{binaryContentId}")
  public ResponseEntity<Void> delete(@PathVariable UUID binaryContentId) {
    binaryContentService.delete(binaryContentId);
    return ResponseEntity.noContent().build(); // 204 반환
  }
}
