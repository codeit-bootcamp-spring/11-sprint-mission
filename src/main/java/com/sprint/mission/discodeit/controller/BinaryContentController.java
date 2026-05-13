package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
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
    log.debug("GET /api/binaryContents/{} 조회 요청", binaryContentId);
    BinaryContentDto response = binaryContentService.findById(binaryContentId);
    log.info("GET /api/binaryContents/{} 조회 정상 처리 완료", binaryContentId);
    return response;
  }

  @GetMapping
  public List<BinaryContentDto> getMultipleBinaryContent(
      @RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
    return binaryContentService.findAllByIdIn(binaryContentIds);
  }

  @GetMapping("/{binaryContentId}/download")
  public ResponseEntity<?> download(@PathVariable UUID binaryContentId) {
    log.debug("GET /api/binaryContents/{}/download 요청", binaryContentId);
    BinaryContentDto dto = binaryContentService.findById(binaryContentId);
    ResponseEntity<?> response = binaryContentStorage.download(dto);
    log.info("GET /api/binaryContents/{}/download 응답 완료", binaryContentId);
    return response;
  }
}
