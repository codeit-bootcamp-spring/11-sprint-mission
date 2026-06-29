package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.auth.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.auth.LoginFailureHandler;
import com.sprint.mission.discodeit.auth.LoginSuccessHandler;
import com.sprint.mission.discodeit.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final LoginSuccessHandler loginSuccessHandler;
    private final LoginFailureHandler loginFailureHandler;
    private final DiscodeitUserDetailsService discodeitUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, Environment env, SessionRegistry sessionRegistry) throws Exception {

        // 현재 실행 중인 프로필이 "test"인지 확인합니다.
        boolean isTestProfile = Arrays.asList(env.getActiveProfiles()).contains("test");

        // CSRF 설정
        if (isTestProfile) {
            http.csrf(csrf -> csrf.disable());
        } else {
            http.csrf((csrf) -> csrf
                    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
            );
        }

        // formLogin 설정
        http.formLogin((login) -> login
                .loginProcessingUrl("/api/auth/login")
                .successHandler(loginSuccessHandler)
                .failureHandler(loginFailureHandler)
        );

        // logout 설정
        http.logout((logout) -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
        );

        // 인가 설정
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .requestMatchers("/", "/index.html", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/assets/**", "/static/**", "/css/**", "/js/**").permitAll()
                .requestMatchers(
                        "/api/auth/csrf-token",
                        "/api/users",
                        "/api/auth/login",
                        "/api/auth/logout").permitAll()
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/**").permitAll()
                .anyRequest().authenticated()
        );

        // 권한 예외 처리
        http.exceptionHandling((ex) -> ex
                // 인증되지 않은 사용자
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\": \"로그인이 필요한 서비스입니다.\"}");
                })

                // 인증은 되었으나 권한이 부족한 사용자
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"message\": \"해당 기능을 사용할 권한이 없습니다.\"}");
                })
        );

        // 세션 설정
        http.sessionManagement((session) -> session
                .sessionConcurrency((concurrency) -> concurrency
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                        .sessionRegistry(sessionRegistry)
                )
        );

        // 로그인 유지 설정
        http.rememberMe((rememberMe) -> rememberMe
                .key("discodeit-remember-me-secret-key-2026")
                .rememberMeParameter("remember-me")
                .tokenValiditySeconds(60 * 60 * 24 * 30)
                .userDetailsService(discodeitUserDetailsService)
        );

        return http.build();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role(Role.ADMIN.name()).implies(Role.CHANNEL_MANAGER.name())
                .role(Role.CHANNEL_MANAGER.name()).implies(Role.USER.name())
                .build();
    }

    // RoleHierarchy를 '메서드 보안(@PreAuthorize)'에 적용하기 위한 연결 설정
    @Bean
    static MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    // 톰캣 세션 만료시 sessionRegistry 자동으로 유저를 지우기 위함
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
