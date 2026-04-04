package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.UserCreateRequest;
import com.sprint.mission.discodeit.dto.UserReadDto;
import com.sprint.mission.discodeit.dto.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    // Create
    @Override
    public User create(UserCreateRequest dto) {
        // 이름 중복체크
        userRepository.findAll().stream()
                .filter(user -> user.getUsername().equals(dto.username()))
                .findFirst()
                .ifPresent(user -> {
                    throw new IllegalArgumentException("이미 존재하는 이름입니다.");
                });

        // 이메일 중복체크 // 조건 -> 탐색 -> 이미 있으면 예외를 날림
        userRepository.findAll().stream()
                .filter(user -> user.getEmail().equals(dto.email()))
                .findFirst()
                .ifPresent(user -> {
                    throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
                });


        // 유저 생성(이름, 이메일 비밀번호)
        User user = User.create(dto.username(), dto.email(), dto.password());

        // user -> binaryContent(profileImg) -> userStatus 순으로 레포지토리에 저장
        userRepository.insert(user);

        // 프로필 이미지 등록(선택)
        if (dto.bytes() != null) {
            BinaryContent profileimageURL = BinaryContent.userProfileImage(user.getId(), dto.bytes(), dto.fileName(), dto.fileType());
            binaryContentRepository.insert(profileimageURL);
            user.updateProfileId(profileimageURL.getId());
        }

        // 유저 상태 생성
        // UserStatusService를 사용하면 같은 레이어(여기서는 Service)간에 순환 참조가 생기므로 UserStatusService.create 사용 X
        UserStatus userStatus = new UserStatus(user.getId(), Instant.now());
        userStatusRepository.insert(userStatus);
        return user;
    }


    // Read
    @Override
    public UserReadDto find(UUID id) {
        User user = userRepository.findById(id);
        UserStatus userStatus = userStatusRepository.findByUserId(id);

        return new UserReadDto(
                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getUsername(),
                user.getEmail(),
                user.getProfileId(),
                // 현재 js에서 boolean으로 true면 'online : 온라인' / false면 'offline : 오프라인'을 반환
                // ONLINE(Enum)일 경우 true를 그렇지 않으면 false를 반환
                userStatusRepository.findByUserId(user.getId()).status() == User.Status.ONLINE
        );

    }

    // 모든 사용자를 조회
    @Override
    public List<UserReadDto> findAll() {
        return userRepository.findAll().stream()
                .map(user -> new UserReadDto(
                        user.getId(),
                        user.getCreatedAt(),
                        user.getUpdatedAt(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getProfileId(),
                        // 현재 js에서 boolean으로 true면 'online : 온라인' / false면 'offline : 오프라인'을 반환
                        // ONLINE(Enum)일 경우 true를 그렇지 않으면 false를 반환
                        userStatusRepository.findByUserId(user.getId()).status() == User.Status.ONLINE

                )).toList();
    }


    // Update
    // 같은 키, 다른 Value를 put 하면 키는 그대로, Value만 갱신된다.
    @Override
    public User update(UUID id, UserUpdateRequest dto) {
        User user = userRepository.findById(id);

        // 프로필 이미지 update
        if (dto.bytes() != null) {
            if (user.getProfileId() != null) {
                binaryContentRepository.delete(user.getProfileId());
            }

            BinaryContent profileimageURL = BinaryContent.userProfileImage(user.getId(), dto.bytes(), dto.fileName(), dto.fileType());

            binaryContentRepository.insert(profileimageURL);
            user.updateProfileId(profileimageURL.getId());
        }

        // 이름, 이메일, 패스워드 update
        if (dto.newName() != null) {
            user.updateName(dto.newName());
        }
        if (dto.newEmail() != null) {
            user.updateEmail(dto.newEmail());
        }
        if (dto.newPassword() != null) {
            user.updatePassword(dto.newPassword());
        }

        userRepository.update(user);

        return user;
    }

    // Delete
    @Override
    // 기존 User만 삭제
    // 고도화 이후 : User, UserStatus, 프로필 이미지 삭제
    public void delete(UUID id) {
        User user = userRepository.findById(id);

        // user의 상태 삭제
        userStatusRepository.deleteByUserId(id);

        // user의 프로필 이미지 삭제
        if (user.getProfileId() != null) {
            binaryContentRepository.delete(user.getProfileId());
        }

        // user 삭제
        userRepository.delete(id);
    }
}
