package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.RefreshToken;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.repository.RefreshTokenRepository;
import com.sprint.mission.discodeit.service.RefreshTokenService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class BasicRefreshTokenService implements RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  public void saveRefreshToken(String token, UUID userId, Instant expiresAt) {
    refreshTokenRepository.findByUserId(userId)
        .ifPresent(refreshTokenRepository::delete);

    RefreshToken refreshToken = new RefreshToken(token, userId, expiresAt, false);
    refreshTokenRepository.save(refreshToken);
  }

  @Override
  public UUID validateAndGetUserId(String token) {
    RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
        .orElseThrow(RefreshTokenInvalidException::new);

    if (refreshToken.isExpired()) {
      refreshTokenRepository.delete(refreshToken);
      throw new RefreshTokenInvalidException();
    }

    return refreshToken.getUserId();
  }

  @Override
  public void rotateRefreshToken(String oldToken, String newToken, UUID userId, Instant newExpiresAt) {
    RefreshToken refreshToken = refreshTokenRepository.findByToken(oldToken)
        .orElseThrow(RefreshTokenInvalidException::new);

    refreshTokenRepository.delete(refreshToken);

    RefreshToken newRefreshToken = new RefreshToken(newToken, userId, newExpiresAt, true);
    refreshTokenRepository.save(newRefreshToken);
  }

  @Override
  public void invalidate(String token) {
    refreshTokenRepository.findByToken(token)
        .ifPresent(refreshTokenRepository::delete);
  }
}