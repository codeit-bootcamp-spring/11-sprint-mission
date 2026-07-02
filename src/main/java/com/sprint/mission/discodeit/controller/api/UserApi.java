package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.controller.api.examples.UserExamples;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "User API")
public interface UserApi {

  @Operation(summary = "Create user")
  @ApiResponses({
      @ApiResponse(
          responseCode = "201",
          description = "User created successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Validation error (fields contain details)",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = UserExamples.ERROR_400))
      ),
      @ApiResponse(
          responseCode = "409",
          description = "Duplicate username or email",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(name = "USER_001", description = "Duplicate username", value = UserExamples.ERROR_409_USER_001),
                  @ExampleObject(name = "USER_002", description = "Duplicate email", value = UserExamples.ERROR_409_USER_002)
              })
      )
  })
  ResponseEntity<UserResponse> create(
      @RequestPart UserCreateRequest userCreateRequest,
      @RequestPart(required = false) MultipartFile profile
  );

  @Operation(summary = "Update user")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "User updated successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserResponse.class))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Validation error (fields contain details)",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = UserExamples.ERROR_400))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "User not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = UserExamples.ERROR_404_USER_003))
      ),
      @ApiResponse(
          responseCode = "409",
          description = "Duplicate username or email",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(name = "USER_001", description = "Duplicate username", value = UserExamples.ERROR_409_USER_001),
                  @ExampleObject(name = "USER_002", description = "Duplicate email", value = UserExamples.ERROR_409_USER_002)
              })
      )
  })
  ResponseEntity<UserResponse> update(
      @Parameter(description = "User ID") @PathVariable UUID userId,
      @RequestPart(required = false) UserUpdateRequest userUpdateRequest,
      @RequestPart(required = false) MultipartFile profile
  );

  @Operation(summary = "Delete user")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "User deleted successfully"),
      @ApiResponse(
          responseCode = "404",
          description = "User not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = UserExamples.ERROR_404_USER_003))
      )
  })
  ResponseEntity<Void> delete(
      @Parameter(description = "User ID") @PathVariable UUID userId
  );

  @Operation(summary = "Find all users")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Users retrieved successfully",
          content = @Content(mediaType = "application/json",
              array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)))
      )
  })
  ResponseEntity<List<UserResponse>> findAll();

}