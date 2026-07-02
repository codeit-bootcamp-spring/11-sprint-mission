package com.sprint.mission.discodeit.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.ErrorResponse;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.security.handler.LoginFailureHandler;
import com.sprint.mission.discodeit.security.handler.LoginSuccessHandler;
import com.sprint.mission.discodeit.security.handler.SpaCsrfTokenRequestHandler;
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
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final LoginSuccessHandler loginSuccessHandler;
  private final LoginFailureHandler loginFailureHandler;
  private final ObjectMapper objectMapper;

  @Value("${remember-me.key}")
  private String rememberMeKey;

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

  // 동시 로그인 제한, 세션 조회, 인증 무효화
  // 실제 세션이 아닌 SessionInformation을 관리
  @Bean
  public SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  // SessionInformation.expireNow()는 실제 세션을 만료시키는게 아니라 해당 세션을 인증으로 두지 않음(인증 무효화)
  // 즉 timeout이 작동 가능하고 timeout 초과 시 세션이 만료되어 해당 세션을 destroy하는 역할
  // 지정하지 않으면 SessionRegistry에서 실제 세션이 destroy 된지 알 수 없기 때문에 그 세션 정보(SessionInformation)를 계속 가지고 있게 됨
  @Bean
  public HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      UserDetailsService userDetailsService)
      throws Exception {
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
            // 로그인 성공 시 loginSuccessHandler 호출
            .successHandler(loginSuccessHandler)
            // 로그인 실패 시 loginFailureHandler 호출
            .failureHandler(loginFailureHandler)
        )
        .logout(logout -> logout
            // 로그아웃 처리 URL 지정
            .logoutUrl("/api/auth/logout")
            // 로그아웃 시 HttpStatusReturningLogoutSuccessHandler 호출(204 반환)
            .logoutSuccessHandler(
                new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
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
        .sessionManagement(management -> management
            .sessionConcurrency(concurrency -> concurrency
                // 동시 요청 제한을 1로 지정
                .maximumSessions(1)
                // true면 새 로그인 불가, false면 새 로그인 허용하되 기존 세션 만료
                .maxSessionsPreventsLogin(false)
                // 로그인 사용자/세션 정보를 sessionRegistry로 관리
                .sessionRegistry(sessionRegistry()))
        )
        .rememberMe(remember -> remember
            // 서버 재시작 시에도 로그인이 유지되도록 설정하는 고정 키
            .key(rememberMeKey)
            // 로그인 시 remember-me 파라미터 이름
            .rememberMeParameter("remember-me")
            // 토큰 유효기간(7일, 60초 60분 1일 7일)
            .tokenValiditySeconds(60 * 60 * 24 * 7)
            .userDetailsService(userDetailsService));

    return http.build();
  }

}

