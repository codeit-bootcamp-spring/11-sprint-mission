package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.JwtRefreshResult;
import com.sprint.mission.discodeit.exception.jwt.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.JwtService;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BasicJwtService implements JwtService {

  private final JwtTokenProvider jwtTokenProvider;
  private final DiscodeitUserDetailsService userDetailsService;
  private final JwtRegistry jwtRegistry;

  @Override
  @Transactional
  public JwtRefreshResult refreshJwtSession(String refreshToken) {
    if (refreshToken == null ||
        !jwtTokenProvider.validateToken(refreshToken) ||
        !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new RefreshTokenInvalidException();
    }

    String subject = jwtTokenProvider.getSubject(refreshToken);
    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) userDetailsService.loadUserByUsername(
        subject);

    String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    Instant newExpiration = jwtTokenProvider.getExpiration(newRefreshToken);
    JwtInformation newJwtInformation = new JwtInformation(
        userDetails.getUserDto().id(),
        newAccessToken,
        newRefreshToken,
        newExpiration
    );
    jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

    return new JwtRefreshResult(newAccessToken, newRefreshToken, userDetails.getUserDto());
  }
}