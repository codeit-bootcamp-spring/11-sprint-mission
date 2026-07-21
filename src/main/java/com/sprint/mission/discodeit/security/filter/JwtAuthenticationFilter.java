package com.sprint.mission.discodeit.security.filter;

import com.sprint.mission.discodeit.dto.user.UserDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.Role;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);

    if (jwtTokenProvider.validateAccessToken(token)
        && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
      try {
        String username = jwtTokenProvider.getSubject(token);
        String role = jwtTokenProvider.getRole(token);
        UUID userId = jwtTokenProvider.getUserId(token);

        UserDto dummyDto = new UserDto(userId, username, null, null, null,
            Role.valueOf(role.replace("ROLE_", "")));
        DiscodeitUserDetails userDetails = new DiscodeitUserDetails(dummyDto, null);

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
            );
        SecurityContextHolder.getContext().setAuthentication(authentication);
      } catch (ParseException e) {
        log.warn("JWT 토큰 파싱 중 오류가 발생했습니다. {}", e.getMessage());
      }
    }
    filterChain.doFilter(request, response);
  }
}