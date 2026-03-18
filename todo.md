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