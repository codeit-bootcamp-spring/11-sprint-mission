# Sprint 7 Code Review

## 기준

- 공통 문서: `공통 docs/모범답안.md`
- TDD 기준: `공통 docs/tdd-guide.md`
- PR 범위: 프로파일 설정, 로그 관리, 예외 처리 고도화, Validation, Actuator, 서비스/레포지토리/컨트롤러/통합 테스트

## 주요 리뷰 결과

### P0. 전체 테스트가 독립 실행되지 않음

`DiscodeitApplicationTests`가 `test` 프로파일을 활성화하지 않아 기본 `dev` 프로파일로 실행되고, 로컬 PostgreSQL에 연결을 시도했다. 실제 `sh gradlew test` 실행 결과 `Connection to localhost:5432 refused`로 실패했다.

테스트는 외부 로컬 DB 상태에 의존하면 안 된다. 통합 테스트와 컨텍스트 로드 테스트는 `@ActiveProfiles("test")`를 명시하고 H2 기반 `application-test.yaml`을 사용해야 한다.

### P1. PRIVATE 채널 생성 시 참여자 검증 누락

`findAllById(participantIds)`는 존재하지 않는 ID를 조용히 누락한다. 요청한 참여자 중 일부가 없어도 PRIVATE 채널이 생성되면, 이후 참여자 목록과 권한 판단이 깨진다.

요청 참여자 수와 조회된 사용자 수를 비교하고, 누락된 ID가 있으면 커스텀 예외로 실패시켜야 한다.

### P1. 커스텀 예외 전환 미완료

일부 서비스와 스토리지 계층에 `NoSuchElementException`, `IllegalArgumentException`, `RuntimeException`이 남아 있었다. 요구사항은 기존 예외를 `DiscodeitException` 계층으로 대체하고 `ErrorResponse`로 일관되게 반환하는 것이다.

파일 메타데이터 없음, 파일 IO 실패, 사용자 상태 없음/중복 같은 케이스도 모두 도메인 커스텀 예외로 표현해야 한다.

### P1. 예외 응답 정보 부족

`DiscodeitException`에 `timestamp`, `details`가 없고, `ErrorResponse`에도 `exceptionType`, `details`가 없었다. 운영 환경에서는 "어떤 ID를 조회하다 실패했는지"가 로그와 응답 details에 남아야 원인 추적 비용이 낮다.

### P2. 로그 설정 불일치

로그 파일 경로가 요구사항의 `.logs`가 아니라 `logs`였다. 또한 운영 프로파일에서 root 레벨이 `WARN`으로 되어 있어 프로젝트 로그 `info` 기준과 어긋났다.

### P2. 테스트가 요구사항의 핵심 정책을 충분히 고정하지 못함

서비스 테스트는 일부 정상/실패 케이스가 빠져 있고, 통합 테스트 패키지가 없다. TDD 가이드 기준으로는 다음을 더 고정해야 한다.

- 실패 시 다음 협력자를 호출하지 않는지
- 저장/삭제/파일쓰기 같은 부수효과가 정확히 발생하거나 발생하지 않는지
- PRIVATE 채널 참여자 누락처럼 DB API 특성상 조용히 통과하는 실패 조건
- 커서 기반 메시지 조회의 정렬과 next cursor

## TDD 조언

테스트 이름을 구현 메서드가 아니라 정책 문장으로 쪼개면 좋다.

- `PRIVATE 채널 생성 시 요청한 참여자가 모두 존재해야 한다`
- `사용자명이 중복되면 유저와 프로필 파일을 저장하지 않는다`
- `채널이 없으면 메시지를 저장하지 않는다`
- `파일 저장에 실패하면 메타데이터도 커밋하지 않는다`
- `컨텍스트 로드 테스트는 test 프로파일의 H2 DB로 실행된다`

서비스 테스트에서는 mapper가 준 DTO를 확인하는 것보다 repository/storage 호출 여부를 검증하는 편이 더 가치 있다. 도메인 객체 테스트는 mock 없이 생성 규칙과 상태 변화를 빠르게 고정하고, 서비스 테스트는 조회, 예외 변환, 부수효과 흐름에 집중한다.

## 리팩토링 방향

1. 테스트 프로파일을 명시해 전체 테스트를 독립 실행 가능하게 만든다.
2. `DiscodeitException`에 `timestamp`, `details`를 추가하고 `ErrorResponse`에 `exceptionType`, `details`를 포함한다.
3. 남아 있는 일반 예외를 도메인 커스텀 예외로 전환한다.
4. PRIVATE 채널 생성 시 참여자 누락을 검증한다.
5. 로그 파일 경로와 프로파일별 로그 레벨을 요구사항과 맞춘다.
