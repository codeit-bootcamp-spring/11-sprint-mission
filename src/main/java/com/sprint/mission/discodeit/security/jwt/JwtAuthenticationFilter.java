package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    String accessToken = resolveToken(request);

    if (accessToken != null) {
      try {
        authenticate(accessToken, request);
      } catch (RuntimeException e) {
        log.debug("JWT 인증 처리 중 오류가 발생하여 인증되지 않은 요청으로 처리합니다: {}", e.getMessage());
        SecurityContextHolder.clearContext();
      }
    }

    filterChain.doFilter(request, response);
  }

  private void authenticate(String accessToken, HttpServletRequest request) {
    if (!jwtTokenProvider.validateToken(accessToken)
        || jwtTokenProvider.getTokenType(accessToken) != TokenType.ACCESS) {
      return;
    }

    DiscodeitUserDetails userDetails = toUserDetails(accessToken);

    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        userDetails,
        null,
        userDetails.getAuthorities()
    );
    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private String resolveToken(HttpServletRequest request) {
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
      return authorizationHeader.substring(BEARER_PREFIX.length());
    }
    return null;
  }

  private DiscodeitUserDetails toUserDetails(String accessToken) {
    UserDto userDto = new UserDto(
        jwtTokenProvider.getUserId(accessToken),
        jwtTokenProvider.getUsername(accessToken),
        jwtTokenProvider.getEmail(accessToken),
        null,
        null,
        jwtTokenProvider.getRole(accessToken)
    );
    return new DiscodeitUserDetails(userDto, "");
  }
}
