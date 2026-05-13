package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.response.BinaryContentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "BinaryContent", description = "첨부파일 API")
public interface BinaryContentApi {

  @Operation(summary = "여러 첨부 파일 조회")
  @ApiResponse(responseCode = "200", description = "첨부 파일 목록 조회 성공")
  @GetMapping
  ResponseEntity<List<BinaryContentDto>> findAllByIdIn(
      @Parameter(description = "조회할 첨부 파일 ID 목록")
      @RequestParam List<UUID> binaryContentIds);

  @Operation(summary = "첨부 파일 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "첨부 파일 조회 성공"),
      @ApiResponse(responseCode = "404", description = "첨부 파일을 찾을 수 없음")
  })
  @GetMapping(value = "/{binaryContentId}")
  ResponseEntity<BinaryContentDto> find(
      @Parameter(description = "조회할 첨부 파일 ID")
      @PathVariable(value = "binaryContentId") UUID id);

  @Operation(summary = "파일 다운로드")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "다운로드 성공"),
      @ApiResponse(responseCode = "404", description = "파일 없음")
  })
  @GetMapping(value = "{binaryContentId}/download")
  ResponseEntity<?> download(@PathVariable UUID binaryContentId);

}
