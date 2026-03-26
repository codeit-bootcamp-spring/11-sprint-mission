# Discodeit - Chat Service Architecture Practice

간단한 채팅 서비스 도메인을 구현하면서  
**데이터 저장 방식과 애플리케이션 구조를 점진적으로 개선하는 프로젝트**

이 프로젝트는 다음 아키텍처 개선 과정을 통해 발전합니다.

```
In-Memory Storage (JCF)
        ↓
File Persistence (Serialization)
        ↓
Repository Pattern (Separation of Concerns)
```

---

# Project Goals

이 프로젝트를 통해 다음 개념을 학습합니다.

- 도메인 모델 설계
- Service Layer 설계
- Java Collections Framework 활용
- File I/O 기반 데이터 영속화
- 객체 직렬화 / 역직렬화
- Repository Pattern
- 관심사 분리 (Separation of Concerns)
- 의존성 주입 (Dependency Injection)

---

# Tech Stack

| Technology | Description |
|---|---|
| Java 17 | Programming Language |
| Gradle | Build Tool |
| Java Collections Framework | In-memory Data Storage |
| File I/O | Data Persistence |
| Serialization | Object Persistence |

---

# Architecture

프로젝트는 다음과 같은 레이어 구조를 갖습니다.

```
Application
     │
Service Layer
     │
Repository Layer
     │
Storage (JCF / File)

+ DTO Layer (Request / Response)
```

### Layer Responsibilities

| Layer | Responsibility |
|---|---|
| Application | 프로그램 실행 |
| Service | 비즈니스 로직 처리 |
| Repository | 데이터 저장 및 조회 |
| Storage | 실제 데이터 저장 방식 |

---

# Domain Model

프로젝트는 다음 세 가지 핵심 도메인으로 구성됩니다.

### User

```
User
 ├ id : UUID
 ├ name : String
 ├ createdAt : Long
 └ updatedAt : Long
```

---

### Channel

```
Channel
 ├ id : UUID
 ├ name : String
 ├ createdAt : Long
 └ updatedAt : Long
```

---

### Message

```
Message
 ├ id : UUID
 ├ userId : UUID
 ├ channelId : UUID
 ├ content : String
 ├ createdAt : Long
 └ updatedAt : Long
```

---

### ReadStatus
```
ReadStatus
 ├ id : UUID
 ├ userId : UUID
 ├ channelId : UUID
 └ lastReadAt : Instant
```
사용자가 특정 채널에서 마지막으로 읽은 메시지 시점을 관리합니다.

---

### UserStatus
```
UserStatus
 ├ id : UUID
 ├ userId : UUID
 └ lastSeenAt : Instant
```
사용자의 접속 상태를 관리합니다.
현재시간 - lastSeenAt ≤ 5분 → Online

---

### BinaryContent
```
BinaryContent
├ id : UUID
├ fileName : String
├ contentType : String
├ bytes : byte[]
└ createdAt : Instant
```
이미지 및 파일과 같은 첨부 데이터를 저장합니다.
- Message 첨부파일
- User 프로필 이미지

---

Message 생성 시 다음 검증이 수행됩니다.

```
1. User 존재 여부 확인
2. Channel 존재 여부 확인
```

---

# Storage Evolution

이 프로젝트는 **데이터 저장 방식을 단계적으로 개선하는 과정**을 포함합니다.

---

## 1. In-Memory Storage (JCF)

초기 구현에서는 데이터를 **Java Collections Framework**를 사용하여  
메모리에 저장했습니다.

```java
Map<UUID, User> data = new HashMap<>();
```

### 특징

- 빠른 데이터 접근
- 프로그램 종료 시 데이터 삭제

### 문제점

```
데이터 영속성 없음
```

프로그램이 종료되면 모든 데이터가 사라집니다.

---

## 2. File Persistence

데이터를 유지하기 위해 **File I/O + 객체 직렬화**를 도입했습니다.

```
Java Object
   ↓
Serialization
   ↓
File (.ser)
```

사용 기술

```
ObjectOutputStream
ObjectInputStream
```

---

## 3. Service Layer Enhancement 

Spring 기반으로 애플리케이션을 개선하고,
DTO와 새로운 도메인을 도입하여 비즈니스 로직을 고도화했습니다.

### DTO 적용

기존 엔티티 기반 파라미터 전달 방식에서 DTO 기반 구조로 개선했습니다.

```
Controller / Application
↓
DTO
↓
Service
```

### 장점

- 파라미터 그룹화
- 불필요한 데이터 노출 방지
- 유지보수성 향상

### Service 고도화

각 서비스는 다음과 같이 개선되었습니다.

| Service | 주요 기능 |
|---|---|
| UserService | 프로필 이미지, 온라인 상태 포함 |
| ChannelService | PUBLIC / PRIVATE 채널 분리 |
| MessageService | 첨부파일 처리 |
| ReadStatusService | 읽음 상태 관리 |
| UserStatusService | 접속 상태 관리 |
| BinaryContentService | 파일 저장 |

### 의존성 구조 개선

```
Before
Service → Service (강한 결합)
```

```
After
Service → Repository (느슨한 결합)
```

순환 참조를 방지하고 구조를 단순화 했습니다.

---

# Serialization

직렬화는 **Java 객체를 byte 배열로 변환하여 저장하는 과정**입니다.

```
Java Object
   ↓
byte[]
   ↓
File
```

역직렬화

```
File
 ↓
byte[]
 ↓
Java Object
```

---

### Serializable

모든 Entity는 다음 인터페이스를 구현합니다.

```java
implements Serializable
```

이는 **Marker Interface**로  
객체가 직렬화 가능하다는 사실을 JVM에 알립니다.

---

### serialVersionUID

```java
private static final long serialVersionUID = 1L;
```

클래스 변경 시 발생할 수 있는  
직렬화 호환성 문제를 방지하기 위해 선언합니다.

---

# Repository Pattern

초기 구조에서는 Service가 **저장 로직을 직접 처리**했습니다.

```
Service
 └ HashMap 조작
```

이 구조는 다음 문제를 발생시킵니다.

```
비즈니스 로직 + 저장 로직 혼합
```

이를 해결하기 위해 **Repository 패턴을 도입했습니다.**

---

## 개선된 구조

```
Service → Repository → Storage
```

### 역할 분리

| Layer | Responsibility |
|---|---|
| Service | 비즈니스 로직 |
| Repository | 데이터 저장 |
| Storage | 실제 저장 방식 |

---

### Repository Interface

```
UserRepository
ChannelRepository
MessageRepository
ReadStatusRepository
UserStatusRepository
BinaryContentRepository
```

기본 CRUD

```
save()
findById()
findAll()
delete()
```

---

# Storage Comparison

| Storage Type | Data Location | Persistence | Responsibility |
|---|---|---|---|
| JCF | Memory | X | 빠른 데이터 처리 |
| File | File System | O | 데이터 영속성 |
| Repository | Layer Structure | O | 관심사 분리 |

---

# Repository Selection Strategy (Spring + YAML)

이 프로젝트는 Repository 구현체를 코드 수정 없이
**application.yaml 설정 값으로 선택할 수 있도록 설계되었습니다.**

## 설정 방식

```yaml
discodeit:
  repository:
    type: jcf  # jcf | file
    file-directory: data 
```

---

## 동작 방식

| 설정 값 | 동작 |
|---|---|
| jcf | 메모리 기반 Repository 사용 |
| file | 파일 기반 Repository 사용 |

---

## 구조

```
Service → Repository (interface) → JCFRepository or FileRepository 
```

---

## 특징

- 코드 수정 없이 저장 방식 변경 가능
- 환경별 설정 분리 가능 (dev / prod)
- 확장에 유리한 구조

---

## 파일 저장 경로 설정

```
discodeit:
  repository:
    file-directory: data 
```

ex) data/user.ser, data/messages.ser

---

## 설계 의도

이 구조는 다음을 달성합니다.
- DIP (Dependency Inversion Principle)
- 구현체 교체 가능 구조
- 테스트 / 운영 환경 분리 가능

"코드는 그대로, 설정만 바꿔서 동작 변경"

---

# Design Decisions

이 프로젝트에서는 기능 구현뿐 아니라  
**구조의 확장성, 유지보수성, 관심사 분리**를 고려하여 설계를 진행했습니다.

---

## 1. Service와 Repository를 분리한 이유

초기 구조에서는 Service가 비즈니스 로직과 저장 로직을 함께 처리했습니다.

``` id="b4q9c3"
Service
 └ 데이터 저장/조회 직접 처리
```

이 방식은 구현이 단순하지만, 
저장 방식이 바뀔 때마다 Service 코드도 함께 수정해야 한다는 문제가 있습니다.

이를 해결하기 위하여 Repository 계층을 분리했습니다.
```
Service → Repository → Storage
```

이렇게 분리하면:
- Service는 비즈니스 로직에 집중할 수 있고
- Repository는 저장/조회 책임만 담당하며
- 저장방식(JCF/File/DB...)이 바뀌어도 Service는 그대로 유지할 수 있습니다.

--- 

## 2. 인터페이스 기반으로 설계한 이유

Repository와 Service를 인터페이스로 먼저 설계한 이유는
구현체를 유연하게 교체할 수 있도록 하기 위함입니다.

예를 들어, Repository는 다음과 같이 교체 가능합니다.
```
UserRepository
 ├ JCFUserRepository
 └ FileUserRepository
```

이 구조를 통해 의존성 역전 원칙(DIP)을 적용한 구조입니다.
- 구현체 교체가 쉬워지고
- 테스트가 유리해지며
- 확장성이 높아집니다.

---

## 3. DTO를 도입한 이유
초기에는 엔티티를 직접 Service 파라미터로 전달했지만,
기능이 복잡해질수록 필요한 데이터와 불필요한 데이터가 섞이기 시작했습니다.

이를 해결하기 위하여 DTO를 도입했습니다.
```
Application / Controller
        ↓
      DTO
        ↓
     Service
```
DTO를 사용하면:
- 요청/응답 구조를 명확히 나눌 수 있고
- 엔티티 내부 구조를 외부에 직접 노출하지 않으며
- 파라미터를 목적에 맞게 그룹화할 수 있습니다.

---

## 4. 새로운 도메인을 분리한 이유
ReadStatus, UserStatus, BinaryContent는 
기존 User, Channel, Message에 직접 넣지 않고 별도 도메인으로 분리했습니다.

이유는 각 도메인의 책임을 명확하게 하기 위해서입니다.
- ReadStatus → 사용자별 채널 읽음 상태 관리
- UserStatus → 사용자 접속 상태 관리
- BinaryContent → 파일 / 이미지 저장 관리

이렇게 분리하면 도메인 간 결합도를 낮추고,
기능별 변경이 다른 도메인에 미치는 영향을 줄일 수 있습니다.

---

## 5. YAML 기반 설정을 도입한 이유
Repository 구현체를 Java 코드에서 직접 바꾸는 대신
application.yaml 설정으로 선택할 수 있도록 구성했습니다.

```
discodeit:
  repository:
    type: jcf
    file-directory: data
```
이 방식의 장점은 다음과 같습니다.
- 코드 수정 없이 저장 전략 변경 가능
- 실행 환경에 따라 다른 설정 적용 가능
- 설정과 구현을 분리하여 유지보수성 향상

--- 

## 6. 최종적으로 얻은 것
이 구조를 통해 다음과 같은 방향으로 확장 가능한 기반을 만들었습니다.
- JCF → File → Database 로 저장소 확장
- Spring Bean / DI 기반 구조 유지
- Service / Repository / DTO 역할 명확화
- 유지보수성과 테스트 용이성 향상

---

# Project Structure

```
com.sprint.mission.discodeit

config
├ AppConfig
└ RepositoryProperties

dto
├ BinaryContentCreateRequest
├ BinaryContentResponse
├ ChannelResponse
├ ChannelUpdateRequest
├ LoginRequest
├ MessageCreateRequest
├ MessageResponse
├ MessageUpdateRequest
├ PrivateChannelCreateRequest
├ PublicChannelCreateRequest
├ ReadStatusCreateRequest
├ ReadStatusResponse
├ ReadStatusUpdateRequest
├ UserCreateRequest
├ UserDto
├ UserStatusCreateRequest
├ UserStatusResponse
├ UserStatusUpdateRequest
└ UserUpdateRequest

entity
├ BaseEntity
├ BinaryContent
├ Channel
├ ChannelType
├ Message
├ ReadStatus
├ User
└ UserStatus

repository
 ├ (interface)
 │   ├ UserRepository
 │   ├ ChannelRepository
 │   ├ MessageRepository
 │   ├ ReadStatusRepository
 │   ├ UserStatusRepository
 │   └ BinaryContentRepository
 │
 ├ jcf
 │   ├ JCFUserRepository
 │   ├ JCFChannelRepository
 │   ├ JCFMessageRepository
 │   ├ JCFReadStatusRepository
 │   ├ JCFUserStatusRepository
 │   └ JCFBinaryContentRepository
 │
 └ file
     ├ FileUserRepository
     ├ FileChannelRepository
     ├ FileMessageRepository
     ├ FileReadStatusRepository
     ├ FileUserStatusRepository
     └ FileBinaryContentRepository

service
 ├ (interface)
 │   ├ UserService
 │   ├ ChannelService
 │   ├ MessageService
 │   ├ ReadStatusService
 │   ├ UserStatusService
 │   ├ BinaryContentService
 │   └ AuthService
 │
 └ basic
     ├ BasicUserService
     ├ BasicChannelService
     ├ BasicMessageService
     ├ BasicReadStatusService
     ├ BasicUserStatusService
     ├ BasicBinaryContentService
     └ BasicAuthService

DiscodeitApplication
```

---

# Key Learnings

이 프로젝트를 통해 다음 개념을 이해했습니다.

### 1. 데이터 저장 방식

```
Memory → File Persistence
```

데이터 영속성 개념 이해

---

### 2. 객체 직렬화

Java 객체를 파일로 저장하는 방법

---

### 3. Repository Pattern

저장 로직을 분리하여  
코드 유지보수성을 향상

---

### 4. 관심사 분리

```
Service → Business Logic
Repository → Persistence
```

---

### 5. DTO  패턴

엔티티와 외부 계층을 분리하여
데이터 전달 구조를 개선

---

### 6. 서비스 설계 확장

단순 CRUD를 넘어
- 도메인 간 관계 관리
- 첨부파일 처리
- 읽음 상태 관리

등 실제 서비스에 가까운 구조 경험

---

### 7. Spring 기반 구조 이해

- Bean 등록
- Dependency Injection
- IoC Container

기존 수동 객체 생성 방식에서 
Spring 기반 구조로 전환

---

# Future Improvements

- Database 기반 저장소 (JPA)
- Spring Boot 기반 REST API
- 실제 채팅 서버 구현

---

# References

- Effective Java
- Java Serialization Documentation
