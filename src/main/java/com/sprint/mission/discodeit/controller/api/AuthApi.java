package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.response.UserDto;
import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetails;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "인증 API")
public interface AuthApi {

  @GetMapping("/csrf-token")
  ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken);

  @GetMapping("/me")
  ResponseEntity<UserDto> me(
      @AuthenticationPrincipal DiscodeitUserDetails userDetails
  );

  @PutMapping("/role")
  ResponseEntity<UserDto> updateRole(
      @RequestBody UserRoleUpdateRequest dto
  );
}