@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;

    @Override
    public User create(UserCreateRequest request, Optional<BinaryContentCreateRequest> profileRequest) {
        // 1. 중복 체크 (FileUserRepository 활용)
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        // 2. 유저 생성 및 저장
        User user = new User(request.getUsername(), request.getEmail(), request.getPassword());
        userRepository.save(user);

        // 3. 유저 상태(UserStatus) 동시 생성 및 저장
        UserStatus userStatus = new UserStatus(user.getId());
        userStatusRepository.save(userStatus);

        return user;
    }

    @Override
    public UserDto find(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        UserStatus userStatus = userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Status not found"));

        return convertToDto(user, userStatus);
    }

    // 4. 5분 온라인 규칙 및 DTO 변환 로직
    private UserDto convertToDto(User user, UserStatus userStatus) {
        // 시간 비교 로직 수행
        boolean isOnline = Duration.between(userStatus.getLastSeen(), Instant.now()).toMinutes() < 5;

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .online(isOnline) // 계산된 결과 주입
                .build();
    }

    // 나머지 findAll, update, delete 메서드 구현...
}