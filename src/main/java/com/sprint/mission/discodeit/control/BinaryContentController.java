package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;

  @GetMapping("/{binaryContentId}")
  public ResponseEntity<BinaryContentDto> findBinaryContent(
      @PathVariable UUID binaryContentId) {
    BinaryContentDto content = binaryContentService.find(binaryContentId);
    return ResponseEntity.ok(content);
  }

  @GetMapping
  public ResponseEntity<List<BinaryContentDto>> findMultipleBinaryContents(
      @RequestParam("binaryContentIds") List<UUID> binaryContentIds
  ) {
    return ResponseEntity.ok(binaryContentService.findAllByIdIn(binaryContentIds));
  }

  @GetMapping("/{binaryContentId}/download")
  public ResponseEntity<Resource> downloadFile(@PathVariable UUID binaryContentId) {
    log.info("파일 다운로드 요청 수신 - binaryContentId: {}", binaryContentId);
    ResponseEntity<Resource> response = binaryContentService.download(binaryContentId);
    log.info("파일 다운로드 응답 완료 - binaryContentId: {}", binaryContentId);
    return response;
  }
}
