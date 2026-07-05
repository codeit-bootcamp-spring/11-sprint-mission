package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.auth.TokenRefreshResult;
import com.sprint.mission.discodeit.dto.auth.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.auth.InvalidRefreshTokenException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.AuthService;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class BasicAuthService implements AuthService {

  private final UserRepository userRepository;
  private final UserMapper mapper;
  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

  @Override
  public TokenRefreshResult refresh(String refreshToken) {
    if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
      throw InvalidRefreshTokenException.withToken(refreshToken != null ? refreshToken : "");
    }

    UUID userId = UUID.fromString(jwtTokenProvider.getSubject(refreshToken));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));
    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(mapper.toResponse(user),
        user.getPassword());

    String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
    Instant expiration = jwtTokenProvider.getExpiration(newRefreshToken);

    log.info("auth refresh success: userId={}", userId);
    return new TokenRefreshResult(userDetails.getUser(), newAccessToken, newRefreshToken, expiration);
  }

  @PreAuthorize("hasRole('ADMIN')")
  @Transactional
  @Override
  public UserResponse updateRole(UserRoleUpdateRequest request) {
    return updateRoleInternal(request);
  }

  @Transactional
  @Override
  public UserResponse updateRoleInternal(UserRoleUpdateRequest request) {
    log.debug("auth update-role trial: userId={}, newRole={}", request.userId(), request.newRole());
    User user = userRepository.findById(request.userId())
        .orElseThrow(() -> UserNotFoundException.withId(request.userId()));
    user.updateRole(request.newRole());
    jwtRegistry.invalidateJwtInformationByUserId(user.getId());
    log.info("auth update-role success: userId={}, newRole={}", user.getId(), user.getRole());
    return mapper.toResponse(user);
  }
}