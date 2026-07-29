package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.csrf.SpaCsrfTokenRequestHandler;
import com.sprint.mission.discodeit.security.exception.DiscodeitAccessDeniedHandler;
import com.sprint.mission.discodeit.security.exception.DiscodeitAuthenticationEntryPoint;
import com.sprint.mission.discodeit.security.jwt.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.handler.JwtLoginSuccessHandler;
import com.sprint.mission.discodeit.security.jwt.handler.JwtLogoutHandler;
import com.sprint.mission.discodeit.security.login.LoginFailureHandler;
import com.sprint.mission.discodeit.security.util.DiscodeitUserDetailService;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

  private final JwtLoginSuccessHandler jwtLoginSuccessHandler;
  private final LoginFailureHandler loginFailureHandler;
  private final DiscodeitAuthenticationEntryPoint authenticationEntryPoint;
  private final DiscodeitAccessDeniedHandler accessDeniedHandler;
  private final JwtTokenProvider jwtTokenProvider;
  private final DiscodeitUserDetailService userDetailService;
  private final JwtLogoutHandler jwtLogoutHandler;
  private final JwtRegistry jwtRegistry;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter)
      throws Exception {
    http.csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
        .ignoringRequestMatchers(
            "/api/auth/login", // 로그인 자체는 csrf 토큰이 없는 상태에서 호출됨
            "/api/users" // 회원가입은 사전 토큰 발급 불가
        ));

    http.formLogin(login -> login
        .loginProcessingUrl("/api/auth/login")
        .successHandler(jwtLoginSuccessHandler)
        .failureHandler(loginFailureHandler));

    http.logout(logout -> logout
        .logoutUrl("/api/auth/logout")
        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
        .addLogoutHandler(jwtLogoutHandler));

    http.authorizeHttpRequests(auth -> auth
        .requestMatchers("/ws/**").permitAll()
        .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**", "/favicon.ico")
        .permitAll()
        .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/logout").permitAll()
        .requestMatchers(HttpMethod.POST, "/api/auth/refresh").permitAll()
        .requestMatchers("/docs/**", "/api-docs/**", "/swagger-ui/**", "/actuator/**").permitAll()
        .anyRequest().authenticated());

    http.exceptionHandling(ex -> ex
        .authenticationEntryPoint(authenticationEntryPoint)
        .accessDeniedHandler(accessDeniedHandler));

    http.sessionManagement(management -> management
        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public static RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.fromHierarchy(
        "ROLE_ADMIN > ROLE_CHANNEL_MANAGER\n" +
            "ROLE_CHANNEL_MANAGER > ROLE_USER"
    );
  }

  @Bean
  static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
      RoleHierarchy roleHierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(roleHierarchy);
    return handler;
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter() {
    return new JwtAuthenticationFilter(jwtTokenProvider, userDetailService, jwtRegistry);
  }

}
