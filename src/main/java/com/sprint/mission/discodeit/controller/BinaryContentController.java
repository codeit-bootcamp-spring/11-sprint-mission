package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequiredArgsConstructor
@RequestMapping("/api/binaryContents")
public class BinaryContentController {

  private final BinaryContentService binaryContentService;
  private final BinaryContentStorage binaryContentStorage;

  @Operation(summary = "첨부 파일 조회", operationId = "findById")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "첨부 파일 조회 성공"),
      @ApiResponse(responseCode = "404", description = "첨부 파일을 찾을 수 없음")
  })
  @GetMapping("/{binaryContentId}")
  public ResponseEntity<BinaryContentDto> find(@PathVariable UUID binaryContentId) {
    BinaryContentDto binaryContentDto = binaryContentService.findById(binaryContentId);
    return ResponseEntity.ok(binaryContentDto);
  }

  @Operation(summary = "여러 첨부 파일 조회", operationId = "findAllByIdIn")
  @ApiResponse(responseCode = "200", description = "첨부 파일 목록 조회 성공")
  @GetMapping
  public ResponseEntity<List<BinaryContentDto>> findAllIdIn(
      @RequestParam("binaryContentIds") List<UUID> binaryContentIds) {
    List<BinaryContentDto> binaryContentDtoList = binaryContentService.findAllByIdIn(
        binaryContentIds);
    return ResponseEntity.ok(binaryContentDtoList);
  }

  @Operation(summary = "파일 다운로드", operationId = "download")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "파일 다운로드 성공"),
      @ApiResponse(responseCode = "404", description = "파일을 찾을 수 없음")
  })
  @GetMapping("/{binaryContentId}/download")
  public ResponseEntity<?> download(@PathVariable UUID binaryContentId) {
    log.debug("download 시작 - 입력값: {}", binaryContentId);

    BinaryContentDto binaryContentDto = binaryContentService.findById(binaryContentId);

    log.info("파일 다운로드 완료 - binaryContentId: {}", binaryContentId);
    return binaryContentStorage.download(binaryContentDto);
  }
}
