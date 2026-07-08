package com.sprint.mission.discodeit.security.jwt;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.jwt.JwtDto;
import com.sprint.mission.discodeit.dto.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
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
      Authentication authentication) throws IOException, ServletException {

    DiscodeitUserDetails details = (DiscodeitUserDetails) authentication.getPrincipal();

    String accessToken = jwtTokenProvider.generateAccessToken(details);
    String refreshToken = jwtTokenProvider.generateRefreshToken(details);

    //registry 등록
    JwtInformation jwtInformation = new JwtInformation(
        details.getUserDto(), accessToken, refreshToken);
    jwtRegistry.registerJwtInformation(jwtInformation);

    ResponseCookie refreshCookie = ResponseCookie.from(
            JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(true)          // JS 접근 불가
        .secure(false)            // HTTPS
        .path("/")
        .maxAge(Duration.ofDays(14))  //2주
        .sameSite("Strict")      // CSRF 완화
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    JwtDto body = new JwtDto(details.getUserDto(), accessToken);
    objectMapper.writeValue(response.getWriter(), body);
  }


}
