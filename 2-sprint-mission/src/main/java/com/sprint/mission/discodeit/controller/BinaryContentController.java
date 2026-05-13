package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/binaryContents")
@RequiredArgsConstructor
public class BinaryContentController {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;

  @GetMapping("/{binaryContentId}")
  public ResponseEntity<BinaryContentDto.Response> find(
      @PathVariable UUID binaryContentId) {
    log.debug("바이너리 컨텐츠 단건 조회 요청: id={}", binaryContentId);
    BinaryContentDto.Response binaryContent = binaryContentService.findById(binaryContentId);
    log.debug("바이너리 컨텐츠 단건 조회 응답: {}", binaryContent);
    return ResponseEntity.ok(binaryContent);
  }

  @GetMapping
  public ResponseEntity<List<BinaryContentDto.Response>> findAllByIdIn(
      @Valid @RequestParam List<UUID> binaryContentIds) {
    log.debug("바이너리 콘텐츠 다건 조회 요청: ids={}", binaryContentIds);
    List<BinaryContentDto.Response> binaryContents = binaryContentService.findAllByIdIn(
        binaryContentIds);
    log.debug("바이너리 콘텐츠 다건 조회 응답: {}건", binaryContents.size());
    return ResponseEntity.ok(binaryContents);
  }

  @GetMapping("/{binaryContentId}/download")
  public ResponseEntity<?> download(
      @PathVariable UUID binaryContentId) {
    log.info("파일 다운로드 요청: binaryContentId={}", binaryContentId);
    BinaryContentDto.Response responseDto = binaryContentService.findById(binaryContentId);

    ResponseEntity<?> response = binaryContentStorage.download(responseDto);
    log.debug("파일 다운로드 응답: contentType={}, contentLength={}",
        response.getHeaders().getContentType(), response.getHeaders().getContentLength());

    return response;
  }
}