## Spring Security 환경설정

- [X] 프로젝트에 Spring Security 의존성을 추가하세요.
- [X] Security 설정 클래스를 생성하세요.
  - 패키지명: com.sprint.mission.discodeit.config
  - 클래스명: SecurityConfig
- [X] SecurityFilterChain Bean을 선언하세요.
  - [X] 가장 기본적인 SecurityFilterChain을 등록하고, 이때 등록되는 필터 목록을 디버깅해보세요. 필터 목록은 PR에 첨부하세요.
    ```java
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.build();
    }
    ```
    ```text
    Security filter chain: [
    DisableEncodeUrlFilter
    WebAsyncManagerIntegrationFilter
    SecurityContextHolderFilter
    HeaderWriterFilter
    CsrfFilter
    LogoutFilter
    RequestCacheAwareFilter
    SecurityContextHolderAwareRequestFilter
    AnonymousAuthenticationFilter
    ExceptionTranslationFilter
    ]
    ```
- [X] 개발 환경에서 Spring Security 모듈의 로깅 레벨을 trace로 설정하세요.
  - 각 요청마다 통과하는 필터 목록을 확인할 수 있습니다.

---

## CSRF 보호 설정하기

디스코드잇은 CSR 방식이기 때문에 CSRF 토큰은 다음과 같이 처리합니다.

- 클라이언트에서 페이지가 로드될 때 CSRF 토큰 발급 API를 명시적으로 호출
- 서버는 CSRF 토큰을 응답 헤더(Set-Cookie)를 통해 쿠키에 저장
- 클라이언트에서 매 요청마다 쿠키에 저장된 CSRF 토큰을 헤더(X-XSRF-TOKEN)에 포함
- 서버는 요청 헤더에 포함된 두 토큰 값(X-XSRF-TOKEN, Cookie)을 비교해 유효성 검증

- [X] CsrfTokenRepository 구현체를 CookieCsrfTokenRepository로 설정하세요.  
  - 디폴트 구현체는 HttpSessionCsrfTokenRepository입니다.

```java
http
    .csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    );
```

- [X] 클라이언트에서 쿠키에 저장된 CSRF 토큰에 접근해야 하므로 HttpOnly를 false로 설정하세요.

- [X] CsrfTokenRequestHandler 컴포넌트를 대체하세요.  
  - 디폴트 구현체는 XorCsrfTokenRequestAttributeHandler입니다.  
  - CSR + SPA 환경에 적합한 구현체를 정의하세요.

```java
public class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        this.xor.handle(request, response, csrfToken);
        csrfToken.get();
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());
        return (StringUtils.hasText(headerValue) ? this.plain : this.xor)
                .resolveCsrfTokenValue(request, csrfToken);
    }
}
```

```java
http
    .csrf(csrf -> csrf
        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler())
    );
```

- [X] CSRF 토큰을 발급하는 API를 구현하세요.

  - 엔드포인트: GET /api/auth/csrf-token  
  - 요청: 없음  
  - 응답: 203 Void  

```java
@GetMapping("csrf-token")
public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
    String tokenValue = csrfToken.getToken();
    log.debug("CSRF 토큰 요청: {}", tokenValue);
    return ResponseEntity.noContent().build();
}
```

---

- [X] CsrfToken을 메서드 인자로 선언하면 HandlerMethodArgumentResolver를 통해 자동 주입됩니다.

- [X] GET 요청에서는 CSRF 토큰이 자동 생성되지 않으므로, 명시적으로 토큰을 호출하세요.

## 회원가입

- [X] 회원가입 API 스펙은 유지합니다.

  - 엔드포인트: POST /api/users  
  - 요청: Body UserCreateRequest, MultipartFile  
  - 응답: 200 UserDto  

- [X] 회원가입 시 비밀번호는 PasswordEncoder를 통해 해시로 저장하세요.  
  - PasswordEncoder의 구현체는 BCryptPasswordEncoder를 활용하세요.

---

## 인증 - 로그인

- [X] formLogin 을 기본값으로 활성화하고, 추가된 필터를 확인해보세요.

```java
http
    .formLogin(Customizer.withDefaults());
```

- [X] Spring Security의 formLogin 인증 흐름은 그대로 유지하면서 필요한 부분만 대체합니다.

  - UserDetails  
  - UserDetailsService  
  - PasswordEncoder (BCryptPasswordEncoder)  
  - AuthenticationSuccessHandler  
  - AuthenticationFailureHandler  

- [X] 각 컴포넌트의 기본 구현체가 무엇인지 디버깅해보세요.

- [X] 로그인을 처리할 URL을 /api/auth/login 으로 설정하세요.

```java
http
    .formLogin(login -> login
        .loginProcessingUrl("/api/auth/login")
    );
```

- [X] UserDetailsService 컴포넌트를 대체하세요.  
  - 디폴트 구현체는 InMemoryUserDetailsManager입니다.

```java
@Service
@RequiredArgsConstructor
public class DiscodeitUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ...
    }
}
```

- [X] 디스코드잇 DB의 사용자 정보를 기반으로 UserDetails 객체를 생성하세요.

- [X] UserDetails 컴포넌트를 대체하세요.  
  - 디폴트 구현체는 org.springframework.security.core.userdetails.User입니다.

```java
@Getter
@RequiredArgsConstructor
public class DiscodeitUserDetails implements UserDetails {

    private final UserDto userDto;
    private final String password;

    ...
}
```

- [X] UserDto와 비밀번호 정보를 포함하도록 구현하세요.  
- [X] DiscodeitUserDetailsService에서 해당 객체를 생성하여 반환하세요.

- [X] AuthenticationSuccessHandler 컴포넌트를 대체하세요.  
  - 디폴트 구현체는 SavedRequestAwareAuthenticationSuccessHandler입니다.

```java
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {
        ...
    }
}
```

- [X] 인증 성공 시 200 UserDto로 응답하세요.

```java
http
    .formLogin(login -> login
        ...
        .successHandler(loginSuccessHandler)
    );
```

- [X] AuthenticationFailureHandler 컴포넌트를 대체하세요.  
  - 디폴트 구현체는 SimpleUrlAuthenticationFailureHandler입니다.

```java
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        ...
    }
}
```

- [X] 인증 실패 시 401 ErrorResponse로 응답하세요.

```java
http
    .formLogin(login -> login
        ...
        .failureHandler(loginFailureHandler)
    );
```

- [X] SecurityFilterChain에서 로그인 처리를 수행하도록 기존 로그인 코드를 제거하세요.

  - AuthApi.login  
  - AuthController.login  
  - AuthService.login  
  - LoginRequest  

---

## 인증 - 세션을 활용한 현재 사용자 정보 조회

- [X] 세션 ID를 통해 사용자의 기본 정보(UserDto)를 가져올 수 있도록 API를 정의하세요.

  - 엔드포인트: GET /api/auth/me  
  - 요청: Header(자동 포함) Cookie: JSESSIONID=…  
  - 응답: 200 UserDto  

```java
@GetMapping("/me")
public ResponseEntity<UserDto> getCurrentUser(
        @AuthenticationPrincipal DiscodeitUserDetails userDetails) {

    return ResponseEntity.ok(userDetails.getUserDto());
}
```

- [X] SecurityFilterChain의 인증 결과를 기반으로 @AuthenticationPrincipal을 통해 사용자 정보를 조회하세요.

---

## 인증 - 로그아웃

- Spring Security의 logout 흐름은 그대로 유지하면서 필요한 부분만 대체합니다.

- 이번 미션에서는 2가지 요소를 대체합니다.

  - Logout 처리 URL
  - LogoutSuccessHandler

- [X] 로그아웃을 처리할 url을  /api/auth/logout로 설정하세요.

```java
http
    .logout(logout -> logout
        .logoutUrl("/api/auth/logout")
    )
```

- [X] LogoutSuccessHandler 컴포넌트를 대체하세요.

  - 디폴트 구현체는 SimpleUrlLogoutSuccessHandler입니다.

  - HttpStatusReturningLogoutSuccessHandler로 대체하세요.

```java
http
    .logout(logout -> logout
        ...
        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
    )
```

  - 204 Void 응답을 반환하세요.

---

## 인가 - 권한 정의

- [X] 다음과 같이 권한을 정의하세요.

- 관리자: ADMIN
- 채널 매니저: CHANNEL_MANAGER
- 일반 사용자: USER

- [X] 데이터베이스 스키마를 변경하세요.

```sql
CREATE TABLE users
(
    ...
    role varchar(20) NOT NULL
);

ALTER TABLE users
    ADD role varchar(20) NOT NULL;
```

- [X] 회원 가입 시 모든 사용자는 USER 권한을 기본 권한으로 설정하세요.

- [X] 사용자 권한을 수정하는 API를 구현하세요.

- API 스펙  
  - 엔드포인트: PUT /api/auth/role  
  - 요청: Body UserRoleUpdateRequest  
  - 응답: 200 UserDto  

- [X] 애플리케이션 실행 시 ADMIN 권한을 가진 어드민 계정이 초기화되도록 구현하세요.

- 어드민 계정이 없는 경우에만 초기화하세요.

- [X] DiscodietUserDetails.getAuthorities를 수정하세요.

---

## 인가 - 권한 적용

- [X] authorizeHttpRequests를 활성화하고, 모든 요청을 인증하도록 설정하세요.

```java
http
    .authorizeHttpRequests(auth -> auth
        .anyRequest().authenticated()
    )
```

- [X] 다음의 요청은 인증하지 않도록 설정하세요.

```java
http
    .authorizeHttpRequests(auth -> auth
        ...
        .requestMatchers(...).permitAll()
    )
```

- Csrf Token 발급
- 회원가입
- 로그인
- 로그아웃
- API가 아닌 요청(Swagger, Actuator 등)

- [X] Method Security를 활성화하세요.

```java
...
@EnableMethodSecurity
public class SecurityConfig {...}
```

- [X] Service의 메소드 별로 아래의 조건에 맞게 권한을 수정하세요.

- 퍼블릭 채널 생성, 수정, 삭제는 CHANNEL_MANAGER 권한을 가져야합니다.
- 사용자 권한 수정은 ADMIN 권한을 가져야합니다.

- [X] 적절한 권한이 없는 경우 403 응답을 반환하세요.

- SecurityFilterChain

```java
http
    .exceptionHandling(ex -> ex
        .authenticationEntryPoint(...)
        .accessDeniedHandler(...)
    )
```

- GlobalExceptionHandler

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {...}
```

- [X] RoleHierarchy를 활용해 권한의 계층 구조를 정의하세요.

- 관리자 > 채널 매니저 > 일반 사용자
- 관리자 권한은 채널 매니저, 일반 사용자 권한을 포함합니다.
- 채널 매니저 권한은 일반 사용자 권한을 포함합니다.

```java
@Bean
public RoleHierarchy roleHierarchy() {...}

@Bean
static MethodSecurityExpressionHandler methodSecurityExpressionHandler(
    RoleHierarchy roleHierarchy) {
  DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
  handler.setRoleHierarchy(roleHierarchy);
  return handler;
}
```

---

## 세션 관리 고도화

- [X] 동일한 계정으로 동시 로그인할 수 없도록 설정하세요.

- sessionConcurrency 설정을 활용하세요.

```java
http
    .sessionManagement(management -> management
        .sessionConcurrency(concurrency -> concurrency
            ...
        )
    )
```

- 세션의 동일성을 보장하기 위해 DiscodeitUserDetails의 equals(), hashcode() 메소드를 오버라이딩하세요.

- 공식 문서

If you are using a custom implementation of UserDetails, ensure you override the equals() and hashCode() methods. The default SessionRegistry implementation in Spring Security relies on an in-memory Map that uses these methods to correctly identify and manage user sessions. Failing to override them may lead to issues where session tracking and user comparison behave unexpectedly.

- [X] 권한이 변경된 사용자가 로그인 상태라면 세션을 무효화하세요.

- sessionRegistry를 활용하세요.

```java
@Bean
public SecurityFilterChain filterChain(
      ...
    HttpSecurity http,
    SessionRegistry sessionRegistry
    ) {
    http
        .sessionManagement(management -> management
            .sessionConcurrency(concurrency -> concurrency
                ...
                .sessionRegistry(sessionRegistry)
            )
        )
    ...
}
```

```java
@Bean
public SessionRegistry sessionRegistry() {...}
```

- httpSessionEventPublisher: HttpSession이 만료된 경우 이벤트를 통해 SessionRegistry의 SessionInformation도 자동으로 만료하기 위해 필요한 Bean입니다.

```java
@Service
public class BasicAuthService implements AuthService {
  ...
  private final SessionRegistry sessionRegistry;
  ...
}
```

- [ ] UserStatus 엔티티 대신 SessionRegistry를 활용해 사용자의 로그인 여부를 판단하도록 리팩토링하세요.

- UserStatus 엔티티와 관련된 코드는 모두 삭제하세요.

- (로그아웃처럼) HttpSession 만료 시 SessionRegistry의 SessionInformation도 자동으로 만료 처리할 수 있도록 HttpSessionEventPublisher를 Bean으로 등록합니다.

```java
@Bean
public HttpSessionEventPublisher httpSessionEventPublisher() {
  return new HttpSessionEventPublisher();
}
```

---

## 로그인 고도화 - RememberMe

- [X] 로그인 요청 파라미터(remember-me)가 true인 경우 세션이 무효화되어도 자동으로 다시 로그인되도록 하세요.

- 로그인 화면에서 로그인 유지 체크 후 로그인하면 remember-me 파라미터가 true로 설정되어 요청합니다.

- remeberMe 설정을 활용하세요.

```java
http
    .rememberMe(...)
```

- 로그인 상태에서 JESSIONID 쿠키를 삭제 후 새로고침했을 때 인증 상태가 유지 되는지 확인해보세요.

---

## 권한 적용 고도화

- [X] SpEL을 활용해 Method Security 기반 리소스 보호 정책을 강화해보세요.

- 사용자 정보 수정, 삭제는 본인만 할 수 있습니다.
- 메시지 수정, 삭제는 해당 메시지를 작성한 사람만 할 수 있습니다.