package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.data.JwtDto;
import com.sprint.mission.discodeit.dto.data.JwtRefreshResult;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.security.JwtInformation;
import com.sprint.mission.discodeit.security.JwtRegistry;
import com.sprint.mission.discodeit.service.JwtAuthService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BasicJwtAuthService implements JwtAuthService {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final UserMapper userMapper;
  private final JwtRegistry jwtRegistry;

  @Override
  @Transactional(readOnly = true)
  public JwtRefreshResult refresh(String refreshToken) {
    // refresh token 쿠키가 없으면 인증 실패 처리함
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new BadCredentialsException("Refresh token is missing.");
    }

    // refresh token 유효성 검증함
    if (!jwtTokenProvider.validateToken(refreshToken)) {
      throw new BadCredentialsException("Invalid refresh token.");
    }

    // registry에 등록된 refresh token인지 확인함
    if (!jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
      throw new BadCredentialsException("Inactive refresh token.");
    }

    UUID userId = jwtTokenProvider.getUserId(refreshToken);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> UserNotFoundException.withId(userId));

    String newAccessToken = jwtTokenProvider.generateAccessToken(user);
    String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

    jwtRegistry.rotateJwtInformation(
        refreshToken,
        new JwtInformation(
            user.getId(),
            newAccessToken,
            newRefreshToken,
            jwtTokenProvider.getExpirationTime(newAccessToken),
            jwtTokenProvider.getExpirationTime(newRefreshToken)
        )
    );

    UserDto userDto = userMapper.toDto(user);
    JwtDto jwtDto = new JwtDto(userDto, newAccessToken);

    return new JwtRefreshResult(jwtDto, newRefreshToken);
  }
}