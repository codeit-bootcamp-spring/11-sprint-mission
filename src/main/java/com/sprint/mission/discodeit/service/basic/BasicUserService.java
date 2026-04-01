package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final BinaryContentRepository binaryContentRepository;
    private final UserStatusRepository userStatusRepository;
    private final MessageRepository messageRepository;
    private final UserChannelRepository userChannelRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    public List<UserDto> findAllUserDtos() {
        return userRepository.findAll().stream()
                .map(user -> {
                    // UserStatus 조회 로직
                    boolean isOnline = userStatusRepository.findByUserId(user.getId())
                            .map(status -> "ONLINE".equals(status.calculateCurrentStatus()))
                            .orElse(false);

                    return new UserDto(
                            user.getId(),
                            user.getCreateAt(),
                            user.getUpdatedAt(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getProfileId(),
                            isOnline
                    );
                })
                .toList();
    }

    // 회원가입
    @Override
    public SignUpResponseDTO signUp(
            SignUpRequestDTO dto
    ) {
        // username & email 중복확인
        validateDuplicateEmailAndUsername(dto.email(), dto.username());

        // 새로운 유저 생성
        User newUser = User.create(dto.username(), dto.email(), dto.password(), dto.profileId());
        User savedUser = userRepository.save(newUser);

        // 새로운 유저 상태 정보 생성
        UserStatus userStatus = UserStatus.create(newUser.getId());
        userStatusRepository.save(userStatus);

        return SignUpResponseDTO.from(newUser, userStatus);
    }

    // 유저 조회 (아이디 기반)
    @Override
    public FindUserByIdResponseDTO findUser(
            UUID userId
    ) {
        // 해당 유저가 있는지 찾기
        User user = findUserById(userId);

        // 있다면 해당 유저 아이디로 UserStatus 검색
        UserStatus userStatus = findUserStatusByUserId(user.getId());

        return FindUserByIdResponseDTO.from(user, userStatus);
    }


    // 유저 전체 조회
    @Override
    public FindAllUserResponseDTO findAllUser() {
        List<User> userList = userRepository.findAll();

        List<FindUserByIdResponseDTO> dtoList = userList.stream()
                .map(user -> {
                    UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                            .orElseGet(() -> UserStatus.create(user.getId())); // 만약 데이터가 현재 맞지 않는다면 이렇게 임시로 OFFLINE으로 반환 (데이터 불일치 문제 임시 해결)
                    return FindUserByIdResponseDTO.from(user, userStatus);
                })
                .toList();

        return FindAllUserResponseDTO.from(dtoList);
    }

    // 유저 정보 수정
    @Override
    public UpdateUserInfoResponseDTO updateUserInfo(
            UUID id,
            UpdateUserInfoRequestDTO dto
    ) {
        // 원래 업데이트 호출이 가능한 유저인지 검증을 해야하는데, 이거는 일단 X
        User user = findUserById(id);

        // email / username이 변경되었을 경우 중복 방지
        validateDuplicateEmailAndUsernameForUpdate(user, dto.email(), dto.username());

        // profileId가 정말 데이터베이스에 저장되었는지 확인해야하는 로직이 추가적으로 필요할 것으로 생각됨
        user.updateUserInfo(dto.username(), dto.email(), dto.password(), dto.profileId());

        User savedUser = userRepository.save(user);

        UserStatus userStatus = findUserStatusByUserId(savedUser.getId()); // 여기서 userstatus 정보를 갱신해야할까? 그런 것 같은데?

        return UpdateUserInfoResponseDTO.from(savedUser, userStatus);
    }

    @Override
    public void deleteUser(
            UUID userId
    ) {
        // 유저 조회
        User user = findUserById(userId);

        // 프로필 사진 삭제
        if (user.getProfileId() != null) {
            binaryContentRepository.deleteById(user.getProfileId());
        }

        // 유저 Status도 삭제
        userStatusRepository.findByUserId(user.getId())
                .ifPresent(status -> userStatusRepository.deleteById(status.getId()));

        // 유저가 들어가있는 채널도 삭제
        userChannelRepository.findAllByUserId(userId)
                .forEach(uc -> userChannelRepository.deleteById(uc.getId()));

        // - 근데 여기는 정말 정말 힘든게 방장을 위임해야되긴하는데 이게 참 어렵네 일단 배제하고 구현해보자

        // - 메세지 안에 들어가있는 사진도 삭제
        messageRepository.findAllByUserId(userId).forEach(m -> {
                    if (m.getAttachmentIds() != null) {
                        m.getAttachmentIds().forEach(binaryContentRepository::deleteById);
                    }
                    messageRepository.deleteById(m.getId());
        });

        // ReadStatus 삭제
        readStatusRepository.findByUserId(userId)
                .forEach(rs -> readStatusRepository.deleteById(rs.getId()));

        userRepository.deleteById(userId);
    }

    // 유틸
    // 이메일 / 유저네임 중복 체크
    private void validateDuplicateEmailAndUsername(String email, String username) {
        userRepository.findByEmail(email)
                .ifPresent(user -> {throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);});

        userRepository.findByUsername(username)
                .ifPresent(user -> {throw new BusinessException(ErrorCode.USER_USERNAME_DUPLICATE);});
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private UserStatus findUserStatusByUserId(UUID userId) {
        return userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));
    }



    private void validateDuplicateEmailAndUsernameForUpdate(User currentUser, String newEmail, String newUsername) {
        // 이메일이 변경되었을 때만 중복 검사 수행
        if (!currentUser.getEmail().equals(newEmail)) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
            }
        }

        // 유저네임이 변경되었을 때만 중복 검사 수행
        if (!currentUser.getUsername().equals(newUsername)) {
            if (userRepository.findByUsername(newUsername).isPresent()) {
                throw new BusinessException(ErrorCode.USER_USERNAME_DUPLICATE);
            }
        }
    }
}
