# 단위 테스트 정리

## 1. 스프링 부트 단위 테스트 핵심 철학
* **목표:** 외부 의존성(DB, API 등)을 배제하고 '순수 비즈니스 로직'만 빠르고 독립적으로 검증.
* **핵심 스택 (The Trinity):** JUnit 5(실행/생명주기), Mockito(가짜 객체/행위 검증), AssertJ(직관적인 상태 검증).
* **F.I.R.S.T 원칙:** 빠르고(Fast), 독립적이며(Independent), 반복 가능하고(Repeatable), 자가 검증적이며(Self-Validating), 적시에(Timely) 작성되어야 함.

---

## 2. 실무 아키텍처 관점의 핵심 인사이트 (Deep Engineering)

### A. Mapper와 Repository의 분리
* **안티 패턴:** Mapper 내부에서 Repository를 주입받아 엔티티를 조회하는 것. (N+1 문제, 영속성 컨텍스트 관리 난해, SRP 위배)
* **정석:** Service 레이어에서 필요한 모든 데이터를 조회한 뒤, 매퍼에는 '순수 객체'들만 파라미터로 넘겨 조립(`toDto(User user, Team team)`)해야 함.

### B. Controller Validation vs Service Validation (심층 방어)
* **문제:** 컨트롤러에서 `@Size(min=2)` 등으로 막아준다고 해서 서비스 레이어의 검증을 생략해도 되는가?
* **결론:** 안 됨. 컨트롤러는 수많은 진입점(웹, 스케줄러, 메시지 큐) 중 하나일 뿐임.
* **설계:** '형식 검증'은 컨트롤러가 하더라도, "프라이빗 채널은 무조건 2명 이상이어야 한다"는 '비즈니스 도메인 규칙'은 반드시 서비스 레이어에 작성하고 테스트해야 시스템이 견고해짐.

### C. 페이징 처리 시 Page vs Slice
* `Page`: 전체 개수(totalElements)를 알기 위해 `COUNT` 쿼리가 추가로 발생.
* `Slice` (무한 스크롤/커서 기반): 다음 페이지 존재 여부(hasNext)만 알면 되므로 `COUNT` 쿼리가 없음. 따라서 Slice를 반환하는 `PageResponse` DTO를 모킹할 때는 `totalElements` 필드에 `null`을 넣는 것이 논리적으로 맞음.

---

## 3. Mockito & JUnit 심화 기술 정리

### A. Mockito 기본 동작 및 주입 원리
* **Default Return:** Mock 객체는 Stubbing(`given`)을 생략해도 에러가 나지 않고 기본값(`false`, `0`, `null`, 빈 컬렉션 등)을 반환함. 하지만 가독성과 의도 전달을 위해 `willReturn(false)` 등을 명시하는 것이 좋음.
* **`@InjectMocks`의 비밀:** 선언된 `@Mock` 객체들만 생성자에 주입하며, 누락된 의존성은 `null`로 주입함. 호출되지 않으면 에러가 안 나지만, 유지보수를 위해 클래스 내 모든 의존성을 깡통(`@Mock`)으로라도 명시하는 것이 Best Practice.

### B. 동적 모킹: `willReturn` vs `willAnswer`
* `willReturn`: 고정된 정적 값을 반환할 때 사용.
* `willAnswer`: 파라미터로 넘어온 객체의 값을 꺼내어 가공하거나 상태를 변경해야 할 때 사용.
    * *활용 1:* JPA `@GeneratedValue` 시뮬레이션 (`ReflectionTestUtils`를 이용해 강제로 ID 세팅)
    * *활용 2:* 동적 DTO 변환 (파라미터로 들어온 채널의 실제 `ChannelType`을 꺼내어 DTO 생성)

### C. void 메서드 모킹 문법 차이
* **`given(객체.메서드()).willAnswer()`**: 반환값이 있는 메서드 전용 (정방향).
* **`willAnswer().given(객체).메서드()`**: `void` 메서드이거나 `@Spy` 객체를 다룰 때 반드시 사용해야 하는 도치법 문법.

---

## 4. 6대 핵심 메서드별 단위 테스트 공략법

### ① BasicUserService.create (기본 생성 및 ID 모킹)
* **Optional 분기:** 프로필 이미지 유무에 따른 로직 분기 테스트.
* **행위 차단 검증:** 프로필이 없을 때 Storage 호출 로직이 `never()`로 완벽히 차단되는지 검증.
* **ID 시뮬레이션:** `willAnswer` + `ReflectionTestUtils`로 JPA가 DB 저장 시 발급하는 ID 흉내내기.

### ② BasicUserService.update (더티 체이킹 검증)
* **상태 검증:** `userRepository.save()`가 없어도, 로직 마지막에 Mapper로 넘어가는 파라미터를 `ArgumentCaptor<User>`로 낚아채어 엔티티 내부 필드 변경(Dirty Checking) 여부 검증.
* **조건부 검증:** 기존 이메일과 동일할 경우 DB 중복 조회(`existsBy`)가 실행되지 않음을 `never()`로 확인.

### ③ BasicChannelService.create[Private] (다건 인서트 및 제네릭 캡처)
* **제네릭 캡처:** `List<ReadStatus>`처럼 제네릭이 포함된 타입은 필드 레벨에 `@Captor` 어노테이션을 선언해야 안전하게 캡처 가능.
* **AssertJ 컬렉션 추출:** `assertThat(list).extracting("user").containsExactlyInAnyOrder(...)`를 통해 반복문 없이 컬렉션 내부 객체의 상태를 직관적으로 검증.

### ④ BasicChannelService.findAllByUserId (멀티 레포지토리 연계)
* **스트림 가공 검증:** A 레포지토리에서 조회한 결과를 Stream으로 가공(ID 추출)하여 B 레포지토리의 IN 쿼리로 넘길 때, 정확한 파라미터 리스트가 넘어갔는지 `ArgumentCaptor`로 검증.
* **동적 Mapper 모킹:** `willAnswer`를 통해 넘어온 엔티티의 실제 `Type`(Public/Private)을 DTO에 그대로 반영하도록 모킹.

### ⑤ BasicChannelService.delete (Cascading Void 메서드 검증)
* **행위 검증:** `void` 메서드이므로 `times(1)`을 명시하여 각 Repository의 delete 메서드가 정확히 1번씩 호출되었는지 검증.
* **순서 보장 (InOrder):** 외래 키(FK) 참조 무결성 제약조건으로 인해 '자식(Message, ReadStatus) -> 부모(Channel)' 순서로 삭제가 일어나는지 `InOrder` 객체를 통해 엄격하게 검증. (DB 에러 방지)

### ⑥ BasicMessageService.findAllByChannelId (페이징, 커서, 시간 통제)
* **인터페이스 모킹:** `Pageable`과 `Slice`는 직접 생성 불가. Spring Data의 `SliceImpl`을 사용하여 가짜 페이징 응답 생성.
* **시간 통제 (Time Assertions):** `Optional.orElse(Instant.now())` 로직 검증 시, 시점 차이로 인한 에러를 막기 위해 넘어간 시간을 캡처하고 `isCloseTo(Instant.now(), within(1, ChronoUnit.SECONDS))`로 오차 허용 검증.
* **커서 추출:** 마지막 데이터의 생성 시간이 다음 응답(PageResponse)의 커서(nextCursor) 파라미터로 정확히 추출되어 넘어갔는지 행위 검증.

---

# 1. BDDMockito: 가짜 객체 조작 함수 (Stubbing)

가짜 객체(Mock)가 특정 상황에서 우리가 원하는 대로 행동하도록 지시(Stubbing)하는 함수들입니다.  
`org.mockito.BDDMockito` 클래스의 정적(static) 메서드들을 사용합니다.

---

## ① given(...).willReturn(...) / willThrow(...)

### 동작
목 객체의 특정 메서드가 호출되었을 때, 어떤 값을 반환할지(Return) 또는 어떤 예외를 던질지(Throw) 정의합니다.

### 실무 포인트
예외를 던지게 조작하여 애플리케이션의 에러 처리(예: `GlobalExceptionHandler`) 로직이나 롤백 로직을 테스트할 때 아주 유용합니다.

```java
// 1. 정상적인 값 반환 지시
given(userRepository.findByEmail("test@email.com"))
        .willReturn(Optional.of(new User("test@email.com")));

// 2. 예외 발생 지시 (데이터베이스 장애 상황 등 시뮬레이션)
given(userRepository.save(any(User.class)))
        .willThrow(new DataAccessException("DB 연결 끊김") {});

// 3. 체이닝 (Chaining): 첫 번째 호출은 A 반환, 두 번째 호출은 B 반환
given(userRepository.count())
        .willReturn(0L)  // 첫 호출 시 0 반환
        .willReturn(1L); // 두 번째 호출 시 1 반환
```

---

## ② 매개변수 매처(Argument Matchers): any(), eq()

### 동작
`given`이나 `verify`를 사용할 때, 정확히 어떤 값이 들어올지 모를 때(혹은 상관없을 때) 사용하는 함수입니다.

### 실무 포인트 (매우 중요)
인자가 2개 이상일 때, 하나라도 Matcher(`any()`)를 썼다면 나머지 인자도 무조건 Matcher(`eq()`)로 감싸주어야 합니다.  
그렇지 않으면 `InvalidUseOfMatchersException` 에러가 발생합니다.

```java
// [GOOD] any(클래스타입.class)를 사용하여 어떤 유저 객체가 오든 통과시킴
given(userRepository.save(any(User.class))).willReturn(mockUser);

// [BAD] 섞어 쓰면 에러 발생!
given(userService.update(anyLong(), "새이름")).willReturn(true); // 에러!

// [GOOD] 섞어 쓸 때는 명시적인 값도 eq()로 감싸주어야 함
given(userService.update(anyLong(), eq("새이름"))).willReturn(true);
```

---

# 2. Mockito: 행위 검증 함수 (Verification)

메서드가 반환값이 없는 `void`이거나, 특정 로직이 의도한 횟수만큼 잘 실행되었는지 확인할 때 사용하는 `org.mockito.Mockito`의 함수들입니다.

---

## ① verify(mock, mode).method()

### 동작
목 객체의 특정 메서드가 호출되었는지 확인합니다.  
`mode`를 통해 호출 횟수를 아주 디테일하게 검증할 수 있습니다.

```java
// 1. 기본 검증: 정확히 1번 호출되었는가? (times(1)이 생략된 형태)
verify(userRepository).deleteById(1L);

// 2. 횟수 지정 검증
verify(userRepository, times(3)).save(any()); // 정확히 3번 호출
verify(userRepository, atLeast(1)).save(any()); // 최소 1번 이상 호출
verify(userRepository, atMost(5)).save(any()); // 최대 5번 이하 호출

// 3. 호출되지 않음 검증 (아주 중요!)
verify(userRepository, never()).deleteAll(); // 절대 호출되지 않았음
```

---

## ② ArgumentCaptor<T> 와 capture()

### 동작
메서드가 호출될 때 넘어간 '파라미터(인자) 객체' 자체를 낚아채어(Capture) 저장해둡니다.

### 실무 포인트
더티 체이킹(Dirty Checking)으로 엔티티 내부의 값이 변했는지 확인하거나, Mapper로 전달되는 DTO의 내부 상태를 뜯어볼 때 필수적입니다.

```java
// 1. 캡처 객체 생성 (보통 클래스 상단에 @Captor로 선언하지만, 지역 변수로도 가능)
ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

// 2. save가 호출될 때 넘어간 User 객체를 낚아챔
verify(userRepository).save(userCaptor.capture());

// 3. 낚아챈 객체를 꺼내어 AssertJ로 상태 검증
User capturedUser = userCaptor.getValue(); // 여러 번 호출됐다면 getAllValues() 사용
assertThat(capturedUser.getName()).isEqualTo("업데이트된이름");
```

---

# 3. AssertJ: 상태 검증 함수 (Assertions)

테스트의 최종 결과를 확인하는 부분입니다.  
`org.assertj.core.api.Assertions`의 함수들을 사용하며, 영어 문장처럼 술술 읽히는 체이닝(Fluent API)을 제공합니다.

---

## ① assertThat(...) 의 기본 체이닝

### 동작
결과값을 던져주고, 그 뒤에 `.is...` 메서드를 붙여 조건을 검사합니다.

```java
User result = userService.getUser(1L);

assertThat(result)
        .isNotNull()                     // null이 아니어야 하고
        .isInstanceOf(User.class)        // User 클래스의 인스턴스여야 하고
        .hasFieldOrPropertyWithValue("name", "Gemini"); // name 필드 값이 Gemini여야 함

assertThat(result.getAge())
        .isGreaterThan(20)               // 20보다 커야 하고 (초과)
        .isLessThanOrEqualTo(30);        // 30보다는 작거나 같아야 함 (이하)
```

---

## ② 컬렉션(List, Set) 특화 검증 함수

### 동작
리스트 내부의 요소를 반복문 없이 아주 직관적이고 강력하게 검증합니다.

```java
List<String> names = List.of("Alice", "Bob", "Charlie");

assertThat(names)
        .isNotEmpty()
        .hasSize(3)                           // 사이즈가 정확히 3인지
        .contains("Alice", "Bob")             // 이 값들을 포함하는지 (순서 무관, 다른 값 섞여도 OK)
        .containsExactly("Alice", "Bob", "Charlie") // 이 값들만, 이 '순서'대로 들어있는지
        .containsExactlyInAnyOrder("Bob", "Charlie", "Alice"); // 이 값들만 들어있는지 (순서 무관)
```

---

## ③ extracting(...) (컬렉션 심화)

### 동작
객체 리스트에서 특정 필드만 쏙 뽑아내어 새로운 리스트처럼 검증합니다.

### 실무 포인트
DTO 리스트를 반환하는 `findAll` 류의 API를 검증할 때 코드를 절반 이상 줄여주는 마법의 함수입니다.

```java
List<User> users = List.of(new User("A"), new User("B"));

// 반복문을 돌며 필드를 꺼낼 필요 없이, 이름만 추출해서 한 번에 검증!
assertThat(users)
        .extracting("name") 
        .containsExactly("A", "B");

// 타입 안정성을 위해 람다식 사용 권장
assertThat(users)
        .extracting(User::getName)
        .containsExactly("A", "B");
```

---

## ④ assertThatThrownBy(...) (예외 검증)

### 동작
람다식 안에서 실행된 코드 때문에 예외가 발생하는지, 그리고 그 예외의 종류와 메시지가 정확한지 검증합니다.

```java
assertThatThrownBy(() -> userService.getUser(999L))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("찾을 수 없습니다") // 에러 메시지에 이 단어가 포함되어 있는지
        .hasFieldOrPropertyWithValue("errorCode", "U-001"); // 커스텀 예외의 특정 필드값 검증
```

---

# 4. JUnit 5: 생명주기 및 구조 제어 어노테이션(함수)

테스트 코드 자체를 실행하고, 준비하고, 구조화하는 함수적 역할을 하는 어노테이션들입니다.

---

## ① @BeforeEach / @AfterEach

### 동작
각 `@Test` 메서드가 실행되기 '직전'과 '직후'에 무조건 한 번씩 실행되는 함수를 정의합니다.

### 실무 포인트
목 객체를 초기화하거나, 테스트용 더미 데이터를 세팅하고 지울 때 사용합니다.

```java
class UserServiceTest {
    
    User dummyUser;

    @BeforeEach
    void setUp() {
        // 모든 테스트가 실행되기 전에 동일한 초기 상태를 만들어 둠
        dummyUser = new User("기본유저"); 
    }

    @AfterEach
    void tearDown() {
        // 테스트가 끝나면 사용했던 리소스를 정리 (DB 초기화 등)
        dummyUser = null; 
    }
}
```