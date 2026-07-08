package com.sprint.mission.discodeit.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.security.auth.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.handler.JwtLoginSuccessHandler;
import com.sprint.mission.discodeit.security.handler.JwtLogoutHandler;
import com.sprint.mission.discodeit.security.handler.LoginFailureHandler;
import com.sprint.mission.discodeit.security.handler.SpaCsrfTokenRequestHandler;
import com.sprint.mission.discodeit.security.jwt.filter.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.jwt.provider.JwtTokenProvider;
import com.sprint.mission.discodeit.security.jwt.registry.InMemoryJwtRegistry;
import com.sprint.mission.discodeit.security.jwt.registry.JwtRegistry;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final LoginFailureHandler loginFailureHandler;
  private final ObjectMapper objectMapper;

  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy
  ) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);

    return handler;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    // ROLE_ADMIN > ROLE_CHANNEL_MANAGER > ROLE_USER
    // 관리자는 채널 매니저, 사용자 권한을 포함함(PUBLIC 채널 생성/수정/삭제 권한 포함)
    // 채널 매니저는 사용자 권한을 포함함
    return RoleHierarchyImpl.fromHierarchy("""
        ROLE_ADMIN > ROLE_CHANNEL_MANAGER
        ROLE_CHANNEL_MANAGER > ROLE_USER
        """);
  }

  // JWT 인증 필터
  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(
      JwtTokenProvider jwtTokenProvider,
      JwtRegistry jwtRegistry,
      DiscodeitUserDetailsService userDetailsService
  ) {
    return new JwtAuthenticationFilter(jwtTokenProvider, jwtRegistry, userDetailsService);
  }

  @Bean
  public JwtRegistry jwtRegistry(
      @Value("${jwt.max-active-login}") int maxActiveJwtCount) {
    return new InMemoryJwtRegistry(maxActiveJwtCount);
  }

  @Bean
  public SecurityFilterChain filterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      JwtLoginSuccessHandler jwtLoginSuccessHandler,
      JwtLogoutHandler jwtLogoutHandler) throws Exception {
    http
        .csrf((csrf) -> csrf
            // CSRF Token Repository 구현체를 Cookie Csrf Token Repository로 설정(Default는 Http Session Csrf...)
            // js에 접근 가능하도록 HttpOnly 옵션을 false로 설정
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            // CSRF Token 요청 핸들러 구현체를 커스텀 핸들러로 설정(Default는 XORCsrfTokenRequestAttributeHandler)
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
        )
        .formLogin(login -> login
            // 로그인 요청을 처리하는 URL 지정
            .loginProcessingUrl("/api/auth/login")
            // 로그인 성공 시 loginSuccessHandler 호출 → jwtLoginSuccessHandler 호출
            .successHandler(jwtLoginSuccessHandler)
            // 로그인 실패 시 loginFailureHandler 호출
            .failureHandler(loginFailureHandler)
        )
        .logout(logout -> logout
            // 로그아웃 처리 URL 지정
            .logoutUrl("/api/auth/logout")
            // 로그아웃 시 HttpStatusReturningLogoutSuccessHandler 호출(204 반환)
            .logoutSuccessHandler(
                new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            .addLogoutHandler(jwtLogoutHandler)
        )
        .authorizeHttpRequests(auth -> auth
                // .permitAll() : 인증 없이 접근 가능
                // SPA, 정적 리소스
                .requestMatchers("/", "/index.html", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/assets/**").permitAll()
                // Spring Security 기본 로그인 페이지
                .requestMatchers("/login").permitAll()
                // 현재 로그인 유저가 있는지 확인(없으면 401에러 반환)
                .requestMatchers(HttpMethod.GET, "/api/auth/me").permitAll()
                // Csrf Token 발급
                .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
                // 회원가입
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                // 로그인
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                // 로그아웃
                .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
                // Refresh Token
                .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
                // Swagger
                .requestMatchers("swagger-ui.html", "swagger-ui/**", "v3/api-docs/**").permitAll()
                // Actuator
                .requestMatchers("/actuator/**").permitAll()
//              // 그 외의 모든 요청은 인증된 사용자만 가능
                .anyRequest().authenticated()
        )
        // 예외 처리
        .exceptionHandling(ex -> ex
            // 인증 안됨 → 401
            .authenticationEntryPoint((request, response, authenticationException) -> {
              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
              response.setContentType("application/json");
              response.setCharacterEncoding("UTF-8");

              ErrorResponse errorResponse = ErrorResponse.of(
                  ErrorCode.UNAUTHORIZED,
                  HttpServletResponse.SC_UNAUTHORIZED,
                  authenticationException);

              objectMapper.writeValue(response.getWriter(), errorResponse);
            })
            // 권한 없음 → 403
            .accessDeniedHandler((request, response, accessDeniedException) -> {
              response.setStatus(HttpServletResponse.SC_FORBIDDEN);
              response.setContentType("application/json");
              response.setCharacterEncoding("UTF-8");

              ErrorResponse errorResponse = ErrorResponse.of(
                  ErrorCode.FORBIDDEN,
                  HttpServletResponse.SC_FORBIDDEN,
                  accessDeniedException);

              objectMapper.writeValue(response.getWriter(), errorResponse);
            })
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

}

