package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.controller.api.examples.BinaryContentExamples;
import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentResponse;
import com.sprint.mission.discodeit.dto.common.RestResponse;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "BinaryContent", description = "Binary Content API")
public interface BinaryContentApi {

    @Operation(summary = "Find binary content by ID")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Binary content retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BinaryContentResponse.class),
                            examples = @ExampleObject(value = BinaryContentExamples.FIND_BY_ID_200))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Binary content not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "BINARY_CONTENT_001", value = BinaryContentExamples.ERROR_404_BINARY_CONTENT_001))
            )
    })
    ResponseEntity<RestResponse<BinaryContentResponse>> findById(
            @Parameter(description = "Binary content ID") @PathVariable UUID binaryContentId
    );

    @Operation(summary = "Find binary contents by IDs")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Binary contents retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BinaryContentResponse.class),
                            examples = @ExampleObject(value = BinaryContentExamples.FIND_ALL_BY_IDS_200))
            )
    })
    ResponseEntity<RestResponse<List<BinaryContentResponse>>> findAllByIdIn(
            @Parameter(description = "Binary content IDs") @RequestParam List<UUID> binaryContentIds
    );
}