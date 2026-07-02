package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.dto.auth.JwtDto;
import com.sprint.mission.discodeit.dto.user.RoleUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.dto.JwtInformation;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

  public AuthController(UserService userService, JwtTokenProvider jwtTokenProvider,
      JwtRegistry jwtRegistry) {
    this.userService = userService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.jwtRegistry = jwtRegistry;
  }

  @PostMapping("/refresh")
  public ResponseEntity<JwtDto> refresh(
      @CookieValue(name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
      HttpServletResponse response) {
    if (!StringUtils.hasText(refreshToken)
        || !jwtTokenProvider.validateToken(refreshToken)
        || !jwtTokenProvider.isRefreshToken(refreshToken)
        || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new RefreshTokenInvalidException();
    }

    JwtTokenProvider.TokenPair tokenPair = jwtTokenProvider.reissueTokens(refreshToken);
    UUID userId = jwtTokenProvider.getUserId(tokenPair.refreshToken());

    JwtInformation newJwtInformation = new JwtInformation(
        userId,
        tokenPair.accessToken(),
        tokenPair.refreshToken(),
        jwtTokenProvider.getExpiration(tokenPair.refreshToken())
    );
    jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

    ResponseCookie refreshTokenCookie = jwtTokenProvider.createRefreshTokenCookie(
        tokenPair.refreshToken());
    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    UserDto userDto = userService.readUser(userId);
    JwtDto jwtDto = new JwtDto(userDto, tokenPair.accessToken());

    return ResponseEntity.ok(jwtDto);
  }

  @GetMapping("/csrf-token")
  public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/role")
  public ResponseEntity<UserDto> updateRole(@RequestBody RoleUpdateRequest request) {
    return ResponseEntity.ok(userService.updateRole(request));
  }
}
