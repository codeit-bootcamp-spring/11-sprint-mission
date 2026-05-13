package com.sprint.mission.discodeit.controller.api;


import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.PathVariable;


@Tag(name = "BinaryContent", description = "첨부파일 API")

public interface BinaryContentApi {


  @Operation(summary = "첨부파일 목록 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "첨부파일 목록 조회 성공",
          content = @Content(array = @ArraySchema(arraySchema = @Schema(implementation = BinaryContentDto.class))))
  })
  ResponseEntity<List<BinaryContentDto>> findBinaryContentList(
      @Parameter(description = "첨부파일 아이디들") List<UUID> binaryContentIds);


  @Operation(summary = "첨부파일 조회")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "조회 성공",
          content = @Content(schema = @Schema(implementation = BinaryContentDto.class))),

      @ApiResponse(responseCode = "404", description = "첨부파일을 찾을 수 없음",
          content = @Content(schema = @Schema(implementation = ErrorResponse.class)))

  })
  ResponseEntity<BinaryContentDto> findBinaryContent(
      @Parameter(description = "첨부파일 아이디") UUID binaryContentId);


  @Operation(summary = "파일 다운로드")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "파일 다운로드 성공")
  })
  ResponseEntity<?> downloadBinaryContent(@PathVariable UUID binaryContentId);


}
