package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.controller.api.examples.UserExamples;
import com.sprint.mission.discodeit.dto.common.RestResponse;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.dto.userstatus.UserStatusResponse;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
              schema = @Schema(implementation = UserResponse.class),
              examples = @ExampleObject(value = UserExamples.CREATE_201))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Validation error",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(name = "USER_001", description = "Missing nickname", value = UserExamples.ERROR_400_USER_001),
                  @ExampleObject(name = "USER_002", description = "Missing username", value = UserExamples.ERROR_400_USER_002),
                  @ExampleObject(name = "USER_004", description = "Missing email", value = UserExamples.ERROR_400_USER_004),
                  @ExampleObject(name = "USER_005", description = "Invalid email format", value = UserExamples.ERROR_400_USER_005),
                  @ExampleObject(name = "USER_007", description = "Missing password", value = UserExamples.ERROR_400_USER_007),
                  @ExampleObject(name = "USER_008", description = "Password too short", value = UserExamples.ERROR_400_USER_008),
                  @ExampleObject(name = "USER_009", description = "Missing phone number", value = UserExamples.ERROR_400_USER_009),
                  @ExampleObject(name = "USER_010", description = "Invalid phone number format", value = UserExamples.ERROR_400_USER_010)
              })
      ),
      @ApiResponse(
          responseCode = "409",
          description = "Duplicate resource",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(name = "USER_003", description = "Duplicate username", value = UserExamples.ERROR_409_USER_003),
                  @ExampleObject(name = "USER_006", description = "Duplicate email", value = UserExamples.ERROR_409_USER_006)
              })
      )
  })
  ResponseEntity<RestResponse<UserResponse>> create(
      @RequestPart UserCreateRequest userCreateRequest,
      @RequestPart(required = false) MultipartFile profile
  );

  @Operation(summary = "Update user")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "User updated successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserResponse.class),
              examples = @ExampleObject(value = UserExamples.UPDATE_200))
      ),
      @ApiResponse(
          responseCode = "400",
          description = "Validation error",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(name = "USER_005", description = "Invalid email format", value = UserExamples.ERROR_400_USER_005),
                  @ExampleObject(name = "USER_008", description = "Password too short", value = UserExamples.ERROR_400_USER_008),
                  @ExampleObject(name = "USER_010", description = "Invalid phone number format", value = UserExamples.ERROR_400_USER_010)
              })
      ),
      @ApiResponse(
          responseCode = "404",
          description = "User not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(name = "USER_011", value = UserExamples.ERROR_404_USER_011))
      ),
      @ApiResponse(
          responseCode = "409",
          description = "Duplicate resource",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = {
                  @ExampleObject(name = "USER_003", description = "Duplicate username", value = UserExamples.ERROR_409_USER_003),
                  @ExampleObject(name = "USER_006", description = "Duplicate email", value = UserExamples.ERROR_409_USER_006)
              })
      )
  })
  ResponseEntity<RestResponse<UserResponse>> update(
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
              examples = @ExampleObject(name = "USER_011", value = UserExamples.ERROR_404_USER_011))
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
              schema = @Schema(implementation = UserResponse.class),
              examples = @ExampleObject(value = UserExamples.FIND_ALL_200))
      )
  })
  ResponseEntity<RestResponse<List<UserResponse>>> findAll();

  @Operation(summary = "Update user online status")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "User status updated successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserStatusResponse.class),
              examples = @ExampleObject(value = UserExamples.UPDATE_STATUS_200))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "User or status not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(name = "USER_011", value = UserExamples.ERROR_404_USER_011))
      )
  })
  ResponseEntity<RestResponse<UserStatusResponse>> updateUserStatus(
      @Parameter(description = "User ID") @PathVariable UUID userId
  );
}