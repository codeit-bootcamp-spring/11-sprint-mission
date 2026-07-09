package com.sprint.mission.discodeit.security.filter;

import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final DiscodeitUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());
        if (jwtTokenProvider.validateToken(accessToken)
                && jwtRegistry.hasActiveJwtInformationByAccessToken(accessToken)) {
            try {
                UUID userId = UUID.fromString(jwtTokenProvider.getSubject(accessToken));
                DiscodeitUserDetails userDetails = userDetailsService.loadUserById(userId);

                UsernamePasswordAuthenticationToken authentication =
                        UsernamePasswordAuthenticationToken.authenticated(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (IllegalArgumentException | UsernameNotFoundException ignored) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
