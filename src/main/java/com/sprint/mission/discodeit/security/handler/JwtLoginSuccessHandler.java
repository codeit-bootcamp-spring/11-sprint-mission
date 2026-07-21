package com.sprint.mission.discodeit.security.handler;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.exception.auth.JwtGenerationFailedException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.JwtDto;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;
  private final JwtRegistry jwtRegistry;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {

    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) authentication.getPrincipal();

    try {
      String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
      String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

      JwtInformation jwtInfo = new JwtInformation(
          userDetails.getUserDto().id(),
          accessToken,
          refreshToken,
          jwtTokenProvider.getExpiration(refreshToken)
      );
      jwtRegistry.registerJwtInformation(jwtInfo);

      ResponseCookie responseCookie = ResponseCookie.from(
              JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
          .httpOnly(true)
          .path("/api/auth")
          //.secure(true)
          .maxAge(jwtTokenProvider.getRefreshTokenExpiration() / 1000)
          .build();

      response.addHeader(SET_COOKIE, responseCookie.toString());
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");

      JwtDto jwtDto = new JwtDto(userDetails.getUserDto(), accessToken);
      response.getWriter().write(objectMapper.writeValueAsString(jwtDto));
    } catch (ParseException e) {
      throw new JwtGenerationFailedException();
    }
  }
}
