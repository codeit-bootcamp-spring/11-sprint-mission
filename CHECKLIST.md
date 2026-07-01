# 미션9 - Spring Security 체크리스트 (5일 계획)

> 전체 기술 설계는 `C:\Users\HOME\.claude\plans\vectorized-fluttering-fern.md` 참고.
> 이 파일은 개인 작업 추적용 (`.gitignore`에 `*.md` 포함되어 있어 커밋 안 됨).
> 💾 표시는 커밋 시점 — "빌드되는 상태 + 하나의 목적으로 묶이는 단위" 기준.

---

## Day 1 (월, 6/22) — Security 환경설정 + CSRF

- [x] `spring-boot-starter-security` 의존성 추가
- [x] `config.SecurityConfig` 클래스 생성
- [x] `SecurityFilterChain` Bean 선언 (`http.build()`만 있는 기본형으로 시작)
- [x] 기본 필터 목록 디버깅하고 PR에 첨부할 내용 정리
- [x] dev 프로필에 `logging.level.org.springframework.security: trace` 설정

💾 **커밋 1**: `chore: spring security 의존성 추가 및 기본 SecurityFilterChain 등록`

- [x] `CookieCsrfTokenRepository.withHttpOnlyFalse()` 설정
- [x] `SpaCsrfTokenRequestHandler` 구현 후 `csrfTokenRequestHandler`로 대체
- [x] `GET /api/auth/csrf-token` API 구현

💾 **커밋 2**: `feat: SPA용 CSRF 토큰 발급 설정 및 csrf-token API 추가`

## Day 2 (화, 6/23) — 회원가입 + 로그인 기반 구조

- [x] 회원가입 비밀번호를 `BCryptPasswordEncoder`로 해싱하여 저장 (API 스펙은 유지)
  - 범위 확장: 사용자 정보 수정(`update`) 시 비밀번호 변경도 같이 해싱 처리
  - (별도 이슈) `build.gradle` 테스트 소스셋 Lombok 미적용 + `src/test/resources/schema.sql` 빈 파일 → 테스트 실행 자체가 안 되던 기존 버그 발견, 별도 커밋으로 수정

💾 **커밋 3**: `feat: 회원가입 비밀번호 BCrypt 해싱 적용`

- [x] `formLogin` 기본값 활성화 후 추가된 필터 확인
- [x] 로그인 처리 URL을 `/api/auth/login`으로 설정 (`loginProcessingUrl`)
- [x] `DiscodeitUserDetailsService` 구현 및 `UserDetailsService` 대체
- [x] `DiscodeitUserDetails` 구현 및 `UserDetails` 대체
- [x] `LoginSuccessHandler` 구현 (200 + `UserDto`) 및 대체
- [x] `LoginFailureHandler` 구현 (401 + `ErrorResponse`) 및 대체
- [x] 기존 로그인 코드 제거: `AuthApi.login`, `AuthController.login`, `AuthService.login`, `LoginRequest`

💾 **커밋 4**: `feat: formLogin 기반 로그인 흐름으로 교체 (UserDetails/SuccessHandler/FailureHandler)`
> 위 7개는 한 묶음 — UserDetails/Service/Handler가 서로 의존하고, 기존 로그인 코드는 새 흐름이 완성된 뒤에야 지울 수 있어서 쪼개면 중간에 빌드가 깨짐.

## Day 3 (수, 6/24) — 세션 조회 / 로그아웃 / 권한 정의

- [x] `GET /api/auth/me` API 구현 (`@AuthenticationPrincipal`로 `UserDto` 조회)

💾 **커밋 5**: `feat: 인증된 사용자 정보 조회 API(/api/auth/me) 추가`

- [x] 로그아웃 처리 URL을 `/api/auth/logout`으로 설정 (`logoutUrl`)
- [x] `LogoutSuccessHandler`를 `HttpStatusReturningLogoutSuccessHandler`(204)로 대체
- [x] `deleteCookies("JSESSIONID")` 추가 (기존 체크리스트 누락분, 설계서 6단계 기준으로 보강)
- [x] (버그 수정, 같은 커밋에 묶음) 커밋4에서 빠졌던 `LoginFailureHandler` 연결(`formLogin.failureHandler(...)`) 추가

💾 **커밋 6**: `feat: 로그아웃 처리 URL 및 LogoutSuccessHandler 설정 (LoginFailureHandler 연결 수정 포함)`
> `./gradlew build` 확인: 컴파일 성공, 테스트 61개 실패(Day3 때와 동일한 기존 이슈 — 시큐리티 필터로 인증/CSRF 없는 슬라이스·통합 테스트 차단 + flaky `UserStatusRepositoryTest` 1개), 오늘 변경으로 인한 새 실패 없음.

- [x] 권한 정의: `Role`(ADMIN / CHANNEL_MANAGER / USER) enum 생성
- [x] `users` 테이블에 `role` 컬럼 추가 (schema.sql)
- [x] 회원가입 시 기본 권한 `USER`로 설정
- [x] `DiscodeitUserDetails.getAuthorities()` 구현 (방법 A: `UserDto`는 안 건드리고 `DiscodeitUserDetails`에 `Role` 필드 직접 추가 — `UserDto.role`은 커밋8에서 추가 예정)
- [x] `entity/User.java` — `changeRole(Role)` 메서드 추가

💾 **커밋 7**: `feat: Role 기반 권한 모델 도입 (enum, 컬럼, 기본권한, GrantedAuthority 매핑)`
> ✅ **해결 (6/26)**: `contextLoads()` 실패 원인은 Windows 로컬 PostgreSQL 18이 5432 포트를 먼저 점유해서 앱이 Docker 컨테이너 대신 로컬 DB에 연결하고 있었음. `application-dev.yaml` 포트를 5433으로 변경 후 통과 확인 (`./gradlew build` 62개 → 61개).

💾 **추가 커밋**: `chore: 개발 DB 포트를 5433으로 변경 (로컬 PostgreSQL 18 포트 충돌 방지)`

- [x] 사용자 권한 수정 API 구현: `PUT /api/auth/role` → 200 `UserDto`
- [x] 앱 실행 시 ADMIN 계정 초기화 (이미 있으면 건너뜀)

> 📌 결정 기록: `UserDto.role` 추가로 `DiscodeitUserDetails`의 별도 `Role` 필드와 중복 생김 — 지금은 정리 안 하고 그대로 둠. 동작에는 차이 없음.

> ⏩ **커밋8 작업 순서**:
> 1. `dto/request/UserRoleUpdateRequest.java` 신규 (`record(UUID userId, Role newRole)`)
> 2. `dto/data/UserDto.java` — `role` 필드 추가 (6번째)
> 3. 테스트 5개 `new UserDto(...)` 수정 (`BasicUserServiceTest`, `BasicMessageServiceTest`, `UserControllerTest`, `MessageControllerTest`, `ChannelControllerTest`)
> 4. `repository/UserRepository.java` — `existsByRole(Role)` 추가
> 5. `service/AuthService.java`/`BasicAuthService.java` — `updateRole` 구현
> 6. `controller/AuthController.java`/`AuthApi.java` — `PUT /role` 엔드포인트 (Swagger 포함)
> 7. `config/AdminAccountInitializer.java` 신규 (`ApplicationRunner`) — `UserStatus`도 같이 생성 필요 (`UserMapper.online` 매핑이 `status` null이면 NPE)
> 8. `application.yaml`/`.env`/`.env.example`/`docker-compose.yml` — `discodeit.admin.*` 환경변수 (`ADMIN_USERNAME`/`ADMIN_EMAIL`/`ADMIN_PASSWORD`)

💾 **커밋 8**: `feat: 사용자 권한 수정 API 및 ADMIN 계정 초기화 추가`

## Day 4 (목, 6/25) — 권한 적용 + 세션 관리 고도화

- [x] `authorizeHttpRequests` 활성화, `anyRequest().authenticated()`
- [x] 인증 제외 대상 설정: csrf-token / 회원가입 / 로그인 / 로그아웃 / Swagger / Actuator
- [x] `@EnableMethodSecurity` 활성화
- [x] `RoleHierarchy` 정의 (ADMIN > CHANNEL_MANAGER > USER) + `methodSecurityExpressionHandler` Bean

💾 **커밋 9**: `feat: authorizeHttpRequests 인가 규칙 및 RoleHierarchy 설정`

- [x] 퍼블릭 채널 생성/수정/삭제 → `CHANNEL_MANAGER` 권한 필요
- [x] 사용자 권한 수정 → `ADMIN` 권한 필요
- [x] 권한 없을 때 403 응답 처리 (`AuthenticationEntryPoint`/`AccessDeniedHandler` + `GlobalExceptionHandler`)

💾 **커밋 10**: `feat: 메서드 단위 권한 검사(@PreAuthorize) 및 403 응답 처리 추가`

- [x] 동시 로그인 차단 (`sessionConcurrency`, `maximumSessions(1)`)
- [x] `DiscodeitUserDetails.equals()`/`hashCode()` 오버라이드
- [x] 권한 변경된 사용자의 세션 무효화 (`SessionRegistry`)

💾 **커밋 11**: `feat: SessionRegistry 기반 동시 로그인 차단 및 권한 변경 시 세션 무효화`

- [x] `UserStatus` 엔티티 및 관련 코드 전부 삭제 → `SessionRegistry` 기반 온라인 판정으로 리팩토링
- [x] `HttpSessionEventPublisher` Bean 등록

💾 **커밋 12**: `refactor: UserStatus 제거하고 SessionRegistry 기반 온라인 판정으로 전환`
> ErrorCode/GlobalExceptionHandler의 switch가 exhaustive라서 UserStatus 삭제와 동시에 수정해야 함 — 절대 분리해서 커밋하지 않기.

## Day 5 (금, 6/26) — RememberMe + 권한 고도화 + 테스트/마무리

- [x] `remember-me` 파라미터 처리 (`rememberMe` 설정)
- [x] `JSESSIONID` 삭제 후 새로고침 시에도 remember-me로 인증 유지되는지 확인

💾 **커밋 13**: `feat: remember-me 로그인 유지 기능 추가`

- [ ] SpEL: 사용자 정보 수정/삭제는 본인만 가능
- [ ] SpEL: 메시지 수정/삭제는 작성자만 가능 (`messageSecurity` Bean)

💾 **커밋 14**: `feat: SpEL 기반 리소스 소유자 검증 추가 (사용자 본인/메시지 작성자)`

- [ ] 테스트 정리: `UserDto`에 `role` 추가된 부분 전수 반영, `UserStatus` 관련 테스트 삭제, Security 적용으로 깨지는 슬라이스 테스트에 `@WithMockUser`/`addFilters=false` 보강
- [ ] `./gradlew test` 전체 통과 확인
- [ ] curl 시나리오로 end-to-end 수동 검증 (csrf-token → 회원가입 → 로그인 → me → 권한 체크 → 로그아웃)

💾 **커밋 15**: `test: Security 적용에 따른 테스트 보강 및 정리`