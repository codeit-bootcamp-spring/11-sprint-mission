package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.LoginFailureHandler;
import com.sprint.mission.discodeit.security.SpaCsrfTokenRequestHandler;
import com.sprint.mission.discodeit.security.filter.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.handler.JwtLoginSuccessHandler;
import com.sprint.mission.discodeit.security.handler.JwtLogoutHandler;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

  private final HandlerExceptionResolver exceptionResolver;

  public SecurityConfig(
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
    this.exceptionResolver = exceptionResolver;
  }

  @Bean
  public SecurityFilterChain filter(HttpSecurity http,
      JwtAuthenticationFilter jwtAuthenticationFilter,
      JwtLoginSuccessHandler jwtLoginsuccessHandler,
      LoginFailureHandler loginfailureHandler,
      JwtLogoutHandler jwtLogoutHandler)
      throws Exception {

    http
        .csrf(csrf -> csrf
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) // csrf 보호 설정
            .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())) // 날 것의 토큰 처리

        .formLogin(login -> login
            .loginProcessingUrl("/api/auth/login")
            .usernameParameter("username")
            .passwordParameter("password")
            .successHandler(jwtLoginsuccessHandler)
            .failureHandler(loginfailureHandler))

        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

        .logout(logout -> logout
            .logoutUrl("/api/auth/logout")
            .logoutSuccessHandler(
                new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            .addLogoutHandler(jwtLogoutHandler))

        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.GET, "/api/auth/csrf-token").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
            .requestMatchers("/api/auth/login").permitAll()
            .requestMatchers("/api/auth/logout").permitAll()
            .requestMatchers("/api/auth/refresh").permitAll()
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "swaggger-ui.html").permitAll()
            .requestMatchers("/", "/assets/**", "/favicon.ico", "/index.html").permitAll()
            .requestMatchers("/actuator/**").permitAll()
            .requestMatchers("/error").permitAll()
            .anyRequest().authenticated())

        .exceptionHandling(ex -> ex
            // 로그인을 하지 않은 상태에서 접근한 경우 (401)
            .authenticationEntryPoint((request, response, authException) ->
                exceptionResolver.resolveException(request, response, null, authException))
            // 경로에 대한 권한이 없을 경우 (403)
            .accessDeniedHandler(((request, response, accessDeniedException) ->
                exceptionResolver.resolveException(request, response, null,
                    accessDeniedException))))

        .sessionManagement(management -> management
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public RoleHierarchy roleHierarchy() {
    RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
    hierarchy.setHierarchy("ROLE_ADMIN > ROLE_CHANNEL_MANAGER\n" +
        "ROLE_CHANNEL_MANAGER > ROLE_USER");
    return hierarchy;
  }

  @Bean
  public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy hierarchy) {
    DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
    handler.setRoleHierarchy(hierarchy);
    return handler;
  }

  @Bean
  public JwtAuthenticationFilter jwtAuthenticationFilter(
      JwtTokenProvider jwtTokenProvider, DiscodeitUserDetailsService userDetailsService
  ) {
    return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
  }
}
