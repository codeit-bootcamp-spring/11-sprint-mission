package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

@Tag(name = "BinaryContent", description = "파일 메타정보 API")
public interface BinaryContentApi {

    @Operation(summary = "파일 메타정보 생성")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "파일 메타정보가 성공적으로 생성됨",
                    content = @Content(schema = @Schema(implementation = BinaryContentDto.class))
            )
    })
    ResponseEntity<BinaryContentDto> create(
            @Parameter(description = "생성할 파일 메타정보") BinaryContentCreateRequest request
    );

    @Operation(summary = "파일 메타정보 조회")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "파일 메타정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = BinaryContentDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "파일 메타정보를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "BinaryContent with id {binaryContentId} not found"))
            )
    })
    ResponseEntity<BinaryContentDto> find(
            @Parameter(description = "조회할 파일 ID") UUID binaryContentId
    );

    @Operation(summary = "파일 다운로드")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "파일 다운로드 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "파일 메타정보를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "BinaryContent with id {binaryContentId} not found"))
            )
    })
    ResponseEntity<?> download(
            @Parameter(description = "다운로드할 파일 ID") UUID binaryContentId
    );

    @Operation(summary = "파일 메타정보 삭제")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "파일 메타정보 삭제 성공"),
            @ApiResponse(
                    responseCode = "404",
                    description = "파일 메타정보를 찾을 수 없음",
                    content = @Content(examples = @ExampleObject(value = "BinaryContent with id {binaryContentId} not found"))
            )
    })
    ResponseEntity<Void> delete(
            @Parameter(description = "삭제할 파일 ID") UUID binaryContentId
    );
}