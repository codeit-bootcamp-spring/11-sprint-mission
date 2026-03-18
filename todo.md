# Validation
    - 이거는 DTO에서 하는게 맞지 않나?
    - 내부 로직에서 해야하는가?

# ArrayList? ConcurrentHashMap?
    - Lock을 걸면 그냥 HashMap을 사용해도 되는거 아닌가? 
    - 내부 구조는 ArrayList이면 안되는건가?
    - 복사가 어디까지 전파되는지 (인덱스는 unmodified가 아니기때문에 위험한거 아닌가?)
    - 또한 동시성 문제가 현재 일어날 수 있는 부분인가?

# User 기능 추가
    - 가입한 채널 조회 
    - 작성한 댓글 조회
    - 유저 정보 보기
    - 유저 정보 수정

# 피드백 반영
    - 예외처리
    - 테스트

# 추가 요구사항
    O- UserService 고도화
    O- AuthService 고도화

    - ChannelService 고도화
        O- create
        - find
        - findAll
        O- update
        - delete

    - Message Service 고도화
    - ReadStatusService 구현
    - UserStatusService 고도화
    - BinaryContentService 구현


// Question
// 궁금했던 점
// - findByEmail(dto.username())이 더 나은 것일까?
// - 현재 아래와 같이 작성한 이유는 targetEmail에 대해서 어떤 작업이 추가적으로 일어날 수도 있다는 가정하에 (toLower 같은)
// - 코드 수정을 더 용이하게 할 수 있을 것 같아서 분리해서 작성

///
# 기본 요구사항

## Spring 프로젝트 초기화
- [x] Spring Initializr를 통해 zip 파일을 다운로드하세요.
    - [x] 빌드 시스템은 Gradle - Groovy를 사용합니다.
    - [x] 언어는 Java 17를 사용합니다.
    - [x] Spring Boot의 버전은 3.4.0입니다. *(참고: 현재 build.gradle 3.5.11로 적용)*
    - [x] GroupId는 `com.sprint.mission`입니다.
    - [x] ArtifactId와 Name은 `discodeit`입니다.
    - [x] packaging 형식은 Jar입니다.
    - [x] Dependency를 추가합니다.
        - [x] Lombok
        - [x] Spring Web
- [x] zip 파일을 압축해제하고 원래 진행 중이던 프로젝트에 붙여넣기하세요. 일부 파일은 덮어쓰기할 수 있습니다.
- [x] `application.properties` 파일을 `yaml` 형식으로 변경하세요. (`application.yaml` 적용 완료)
- [x] `DiscodeitApplication`의 main 메서드를 실행하고 로그를 확인해보세요.

## Bean 선언 및 테스트
- [x] `File*Repository` 구현체를 Repository 인터페이스의 Bean으로 등록하세요. (`@Repository` 어노테이션 적용 완료)
- [x] `Basic*Service` 구현체를 Service 인터페이스의 Bean으로 등록하세요. (`@Service` 어노테이션 적용 완료)
- [ ] `JavaApplication`에서 테스트했던 코드를 `DiscodeitApplication`에서 테스트해보세요.
    - [ ] `JavaApplication` 의 main 메소드를 제외한 모든 메소드를 `DiscodeitApplication` 클래스로 복사하세요.
    - [ ] `JavaApplication`의 main 메소드에서 Service를 초기화하는 코드를 Spring Context를 활용하여 대체하세요.
    - [ ] `JavaApplication`의 main 메소드의 셋업, 테스트 부분의 코드를 `DiscodeitApplication` 클래스로 복사하세요.

## Spring 핵심 개념 이해하기
- [x] `JavaApplication`과 `DiscodeitApplication`에서 Service를 초기화하는 방식의 차이에 대해 다음의 키워드를 중심으로 정리해보세요. *(개념 정리 과제)*
    - JavaApplication에서는 직접 객체를 생성하고 주입해야했지만, DiscodeitApplication은 현재 스프링 컨테이너가 객체를 대신 생성(Singleton)하고 주입(Dependncy Injection)을 해주고 있다. 
    - 스프링 컨테이너에 의해서 생성된 빈 (Bean)객체들은 생성 후 각각의 의존성에 맞게 주입되고 있다.

## Lombok 적용
- [x] 도메인 모델의 getter 메소드를 `@Getter`로 대체해보세요. (`BaseEntity`, `User`, `Channel` 등에 적용 완료)
- [x] `Basic*Service`의 생성자를 `@RequiredArgsConstructor`로 대체해보세요. (모든 `Basic*Service`에 적용 완료)

## 비즈니스 로직 고도화
- [x] 다음의 기능 요구 사항을 구현하세요. (새로운 도메인 `ReadStatus`, `UserStatus` 추가, DTO 적용 및 서비스 고도화 완료)

---

# 추가 기능 요구사항

## 시간 타입 변경하기
- [x] 시간을 다루는 필드의 타입은 `Instant`로 통일합니다. (기존에 사용하던 Long보다 가독성이 뛰어나며, 시간대(Time Zone) 변환과 정밀한 시간 연산이 가능해 확장성이 높습니다.)

## 새로운 도메인 추가하기
도메인 모델 간 참조 관계를 참고하세요.
- [x] 공통: 앞서 정의한 도메인 모델과 동일하게 공통 필드(id, createdAt, updatedAt)를 포함합니다.
- [x] `ReadStatus`: 사용자가 채널 별 마지막으로 메시지를 읽은 시간을 표현하는 도메인 모델입니다.
- [x] `UserStatus`: 사용자 별 마지막으로 확인된 접속 시간을 표현하는 도메인 모델입니다.
    - [x] 마지막 접속 시간을 기준으로 현재 로그인한 유저로 판단할 수 있는 메소드를 정의하세요. (마지막 접속 시간이 현재 시간으로부터 5분 이내이면 현재 접속 중인 유저로 간주합니다.)
- [x] `BinaryContent`: 이미지, 파일 등 바이너리 데이터를 표현하는 도메인 모델입니다.
    - [x] 수정 불가능한 도메인 모델로 간주합니다. 따라서 `updatedAt` 필드는 정의하지 않습니다.
    - [x] `User`, `Message` 도메인 모델과의 의존 관계 방향성을 잘 고려하여 `id` 참조 필드를 추가하세요.
- [x] 각 도메인 모델 별 레포지토리 인터페이스를 선언하세요.

## UserService 고도화
- **create**
    - [x] 선택적으로 프로필 이미지를 같이 등록할 수 있습니다.
    - [x] DTO를 활용해 파라미터를 그룹화합니다.
    - [x] `username`과 `email`은 다른 유저와 같으면 안됩니다.
    - [x] `UserStatus`를 같이 생성합니다.
- **find, findAll**
    - [x] 사용자의 온라인 상태 정보를 같이 포함하세요.
    - [x] 패스워드 정보는 제외하세요.
- **update**
    - [x] 선택적으로 프로필 이미지를 대체할 수 있습니다.
    - [x] DTO를 활용해 파라미터를 그룹화합니다.
- **delete**
    - [x] 관련된 도메인도 같이 삭제합니다. (BinaryContent, UserStatus)
- [x] 의존성: 같은 레이어 간 의존성 주입은 피하고, Repository 의존성을 주입합니다.

## AuthService 구현
- **login**
    - [x] `username`, `password`과 일치하는 유저가 있는지 확인합니다.
        - [x] 일치하면 유저 정보 반환, 일치하지 않으면 예외 발생
    - [x] DTO를 활용해 파라미터를 그룹화합니다.
- [x] 의존성: Repository 의존성을 주입합니다.

## ChannelService 고도화
- **create**
    - [x] PRIVATE 채널과 PUBLIC 채널을 생성하는 메소드를 분리합니다.
    - [x] 분리된 각각의 메소드를 DTO를 활용해 파라미터를 그룹화합니다.
    - PRIVATE 채널 생성 시:
        - [x] 채널에 참여하는 User의 정보를 받아 User 별 ReadStatus 정보를 생성합니다.
        - [x] name과 description 속성은 생략합니다.
    - [x] PUBLIC 채널을 생성할 때에는 기존 로직을 유지합니다.
- **find**
    - [x] 해당 채널의 가장 최근 메시지의 시간 정보를 포함합니다.
    - [x] PRIVATE 채널인 경우 참여한 User의 id 정보를 포함합니다.
- **findAll**
    - [x] 특정 User가 볼 수 있는 Channel 목록을 조회하도록 조건 추가 및 메소드명 변경 (`findAllByUserId`)
    - [x] PUBLIC 채널 목록은 전체 조회, PRIVATE 채널은 조회한 User가 참여한 채널만 조회
- **update**
    - [x] DTO를 활용해 파라미터를 그룹화합니다.
    - [x] PRIVATE 채널은 수정할 수 없습니다.
- **delete**
    - [x] 관련된 도메인도 같이 삭제합니다. (Message, ReadStatus)
- [x] 의존성: Repository 의존성을 주입합니다.

## MessageService 고도화
- **create**
    - [x] 선택적으로 여러 개의 첨부파일을 같이 등록할 수 있습니다.
    - [x] DTO를 활용해 파라미터를 그룹화합니다.
- **findAll**
    - [x] 특정 Channel의 Message 목록을 조회하도록 메소드명 변경 (`getMessagesByChannel` 로 구현됨)
- **update**
    - [x] DTO를 활용해 파라미터를 그룹화합니다.
- **delete**
    - [x] 관련된 도메인도 같이 삭제합니다. (첨부파일)
- [x] 의존성: Repository 의존성을 주입합니다.

## ReadStatusService 구현
- **create**
    - [x] DTO를 활용해 파라미터를 그룹화합니다.
    - [x] 관련된 Channel이나 User가 존재하지 않으면 예외를 발생시킵니다.
    - [x] 같은 Channel과 User와 관련된 객체가 이미 존재하면 예외를 발생시킵니다.
- **find**
    - [x] `id`로 조회합니다.
- **findAllByUserId**
    - [x] `userId`를 조건으로 조회합니다.
- **update**
    - [x] DTO/`id`를 활용해 업데이트 합니다.
- **delete**
    - [x] `id`로 삭제합니다.
- [x] 의존성: Repository 의존성을 주입합니다.

## UserStatusService 고도화
- **create**
    - [x] DTO를 활용해 파라미터를 그룹화합니다.
    - [x] 관련된 User가 존재하지 않거나, 이미 객체가 존재하면 예외를 발생시킵니다.
- **find**
    - [x] `id`로 조회합니다.
- **findAll**
    - [x] 모든 객체를 조회합니다.
- **update**
    - [x] DTO를 활용해 파라미터를 그룹화합니다.
- **updateByUserId**
    - [x] `userId` 로 특정 User의 객체를 업데이트합니다.
- **delete**
    - [x] `id`로 삭제합니다.
- [x] 의존성: Repository 의존성을 주입합니다.

## BinaryContentService 구현
- **create**
    - [x] DTO를 활용해 파라미터를 그룹화합니다.
- **find**
    - [x] `id`로 조회합니다.
- **findAllByIdIn**
    - [x] `id` 목록으로 조회합니다.
- **delete**
    - [x] `id`로 삭제합니다.
- [x] 의존성: Repository 의존성을 주입합니다.

## 새로운 도메인 Repository 구현체 구현
- [x] 지금까지 인터페이스로 설계한 각각의 Repository를 JCF, File로 각각 구현하세요. (현재 FileRepository 구현은 완료되어 있습니다.)
- **현재 FileRepository와 Service 로직을 리팩토링하고 고도화하는데 집중하여 JCF는 따로 구현하지 못했습니다.**