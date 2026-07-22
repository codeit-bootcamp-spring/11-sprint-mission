package com.sprint.mission.discodeit.service.jwt;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.dto.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.dto.JwtSessionResult;
import com.sprint.mission.discodeit.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final UserService userService;

  public JwtSessionResult createJwtSession(UUID userId, String username) {
    String accessToken = jwtTokenProvider.createAccessToken(userId, username);
    String refreshToken = jwtTokenProvider.createRefreshToken(userId, username);

    jwtRegistry.invalidateJwtInformationByUserId(userId);
    jwtRegistry.registerJwtInformation(new JwtInformation(
        userId,
        accessToken,
        refreshToken,
        jwtTokenProvider.getExpiration(refreshToken)
    ));

    UserDto userDto = userService.readUser(userId);
    log.info("로그인 세션 생성 - userId: {}", userId);
    return new JwtSessionResult(userDto, accessToken, refreshToken);
  }

  public JwtSessionResult refreshJwtSession(String refreshToken) {
    if (!StringUtils.hasText(refreshToken)
        || !jwtTokenProvider.validateToken(refreshToken)
        || !jwtTokenProvider.isRefreshToken(refreshToken)
        || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      log.warn("토큰 재발급 실패 - 유효하지 않은 refresh token");
      throw new RefreshTokenInvalidException();
    }

    UUID userId = jwtTokenProvider.getUserId(refreshToken);
    String username = jwtTokenProvider.getUsername(refreshToken);

    String newAccessToken = jwtTokenProvider.createAccessToken(userId, username);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, username);

    JwtInformation newJwtInformation = new JwtInformation(
        userId,
        newAccessToken,
        newRefreshToken,
        jwtTokenProvider.getExpiration(newRefreshToken)
    );
    jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

    UserDto userDto = userService.readUser(userId);
    return new JwtSessionResult(userDto, newAccessToken, newRefreshToken);
  }

  public void logoutJwtSession(String refreshToken) {
    if (!StringUtils.hasText(refreshToken)
        || !jwtTokenProvider.validateToken(refreshToken)
        || !jwtTokenProvider.isRefreshToken(refreshToken)) {
      log.warn("로그아웃 요청 - 유효하지 않은 refresh token (이미 만료되었거나 위조됨, 정상 흐름으로 간주)");
      return;
    }

    UUID userId = jwtTokenProvider.getUserId(refreshToken);
    jwtRegistry.invalidateJwtInformationByUserId(userId);
    log.info("로그아웃 처리 완료 - userId: {}", userId);
  }
}
