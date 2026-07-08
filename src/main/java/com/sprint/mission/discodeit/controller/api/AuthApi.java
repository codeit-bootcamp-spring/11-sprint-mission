package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.RoleUpdateRequest;
import com.sprint.mission.discodeit.dto.data.JwtDto;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.security.web.csrf.CsrfToken;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletResponse;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

  @Operation(summary = "CSRF 토큰 발급")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "203", description = "CSRF 토큰 발급 성공")
  })
  ResponseEntity<Void> getCsrfToken(
      @Parameter(hidden = true) CsrfToken csrfToken
  );

  @Operation(summary = "사용자 역할 변경")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "사용자 역할 변경 성공",
          content = @Content(schema = @Schema(implementation = UserDto.class))
      ),
      @ApiResponse(
          responseCode = "403",
          description = "권한 부족"
      )
  })
  ResponseEntity<UserDto> updateRole(
      @RequestBody @Valid RoleUpdateRequest request
  );

  @Operation(summary = "Access Token 재발급")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200",
          description = "Access Token 재발급 성공",
          content = @Content(schema = @Schema(implementation = JwtDto.class))
      ),
      @ApiResponse(
          responseCode = "401",
          description = "Refresh Token이 없거나 유효하지 않음"
      )
  })
  ResponseEntity<JwtDto> refresh(
      @Parameter(hidden = true)
      @CookieValue(name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false)
      String refreshToken,

      @Parameter(hidden = true)
      HttpServletResponse response
  );
} 