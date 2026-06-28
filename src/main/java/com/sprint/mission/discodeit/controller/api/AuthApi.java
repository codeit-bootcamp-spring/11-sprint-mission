package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.controller.api.examples.AuthExamples;
import com.sprint.mission.discodeit.dto.auth.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;

@Tag(name = "Auth", description = "Authentication API")
public interface AuthApi {

  @Operation(summary = "Get CSRF Token")
  @ApiResponses({
      @ApiResponse(
          responseCode = "203",
          description = "CSRF token issued successfully"
      )
  })
  ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken);

  @Operation(summary = "Get current user")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "Current user retrieved successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserResponse.class))
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Not authenticated",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = AuthExamples.ERROR_401_AUTH_002))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "User not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = AuthExamples.ERROR_404_USER_001))
      )
  })
  ResponseEntity<UserResponse> me(@AuthenticationPrincipal DiscodeitUserDetails userDetails);

  @Operation(summary = "Update user role")
  @ApiResponses({
      @ApiResponse(
          responseCode = "200",
          description = "User role updated successfully",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = UserResponse.class))
      ),
      @ApiResponse(
          responseCode = "404",
          description = "User not found",
          content = @Content(mediaType = "application/json",
              schema = @Schema(implementation = ErrorResponse.class),
              examples = @ExampleObject(value = AuthExamples.ERROR_404_USER_001))
      )
  })
  ResponseEntity<UserResponse> updateRole(UserRoleUpdateRequest request);
}
