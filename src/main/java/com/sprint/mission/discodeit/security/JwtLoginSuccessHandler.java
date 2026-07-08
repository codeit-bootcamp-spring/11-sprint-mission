package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.data.JwtDto;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserMapper userMapper;
  private final ObjectMapper objectMapper;
  private final JwtRegistry jwtRegistry;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) throws IOException, ServletException {
    // 인증된 principal을 프로젝트 전용 UserDetails로 변환함
    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) authentication.getPrincipal();
    User user = userDetails.getUser();

    // access token은 응답 body로 내려줌
    String accessToken = jwtTokenProvider.generateAccessToken(user);

    // refresh token은 HttpOnly 쿠키로 내려줌
    String refreshToken = jwtTokenProvider.generateRefreshToken(user);

    // 로그인 성공 시 발급한 JWT 정보를 registry에 등록함
    jwtRegistry.registerJwtInformation(new JwtInformation(
        user.getId(),
        accessToken,
        refreshToken,
        jwtTokenProvider.getExpirationTime(accessToken),
        jwtTokenProvider.getExpirationTime(refreshToken)
    ));

    response.addCookie(jwtTokenProvider.createRefreshTokenCookie(refreshToken));

    UserDto userDto = userMapper.toDto(user);
    JwtDto jwtDto = new JwtDto(userDto, accessToken);

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    objectMapper.writeValue(response.getWriter(), jwtDto);
  }
}