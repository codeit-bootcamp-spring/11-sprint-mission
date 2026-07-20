package com.sprint.mission.discodeit.security.filter;

import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(BEARER_PREFIX.length());

    if (jwtTokenProvider.validateAccessToken(token)
        && jwtRegistry.hasActiveJwtInformationByAccessToken(token)) {
      UUID userId = UUID.fromString(jwtTokenProvider.getSubject(token));
      String username = jwtTokenProvider.getUsername(token);
      Role role = Role.valueOf(jwtTokenProvider.getRole(token));

      /*
       * Rebuilt from token claims only, so id/username/role/online are the only
       * trustworthy fields (online=true is already guaranteed by the
       * hasActiveJwtInformationByAccessToken check above). email/profile are
       * always null since we skip the DB — fetch them from UserService explicitly
       * if a caller ever needs them instead of reading them off the principal.
       */
      UserResponse user = new UserResponse(userId, username, null, null, true, role);
      DiscodeitUserDetails userDetails = new DiscodeitUserDetails(user, "");

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities()
          );
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }
}