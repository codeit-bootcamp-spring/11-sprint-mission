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

    // create
    // 선택적으로 프로필 사진을 같이 첨부할 수 있음
    // - 처음에는 여기서 만드는 걸로 구현을 했는데, 로그인 시 굳이 여기서 만들 필요가 없다고 판단
    // - 만들어진 profileId, 즉 BinaryContentId를 받아서 연결만 해주는 느낌으로 리팩토링
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

    // 고민했던 포인트
    // - 단 건 조회 중 하나라도 오류가 나면 에러를 보내는 건 안 될 것 같은데
    // - 그렇다면
    // - 1. 포함시키지 않는다
    // - 2. 아래와 같은 상황이면 특정 값으로 강제로 보낸다
    // - 3. 현재는 전체가 오류난다 (데이터 불일치 시)
    @Override
    public FindAllUserResponseDTO findAllUser() {
        List<User> userList = userRepository.findAll();

        List<FindUserByIdResponseDTO> dtoList = userList.stream()
                .map(user -> {
                    UserStatus userStatus = userStatusRepository.findByUserId(user.getId())
                            .orElseGet(() -> UserStatus.create(user.getId())); // 만약 데이터가 현재 맞지 않는다면 이렇게 임시로 OFFLINE으로 반환
                    return FindUserByIdResponseDTO.from(user, userStatus);
                })
                .toList();

        return FindAllUserResponseDTO.from(dtoList);
    }

    // 아하 지금 유저 정보 자체를 업데이트 하도록 수정해야함
    // - 현재는 프로필만 수정하도록 되어있음
    @Override
    public UpdateUserInfoResponseDTO updateUserInfo(
            UpdateUserInfoRequestDTO dto
    ) {
        // 해당 유저가 있는지 확인
        // - 근데 본인인지 확인을 해야하는데? 이건 로그인에서 하는게 맞을 것 같다 - 나중에 추후 도입 (원래는 requestUserId로 처리하려고 했으나 패스)
        User user = findUserById(dto.id());

        // username / email // profileID는 null로 받거나 binaryContent에서 해당 아이디를 받아서 오도록 처리할 것이기 때문에 추가 검증은 X
        // - 만약 한다면 profileId가 정말 데이터베이스에 저장되었는지 확인해야하는 로직이 추가적으로 필요할 것으로 생각됨
        validateDuplicateEmailAndUsernameForUpdate(user, dto.email(), dto.username());

        // 실행
        user.updateUserInfo(dto.username(), dto.email(), dto.password(), dto.profileId());
        // - 여기서 고민점:
        // -- 프로필 사진 등록 로직을 애초에 따로 빼는게 맞지 않을까?
        // -- 위에서 등록을 위해서 프로필 사진 입력도 그렇고
        // -- 나중에 따로 프로필 사진을 만들어서 profileId로 넣어주는게 더 좋을 것 같다고 일단 판단
        // -- 나중에 updateUserProfile을 호출하는 Controller에서 프로필 사진 생성 후 id를 반환 받는 방식으로 구현하도록 주의하자
        // -- 그러면 회원가입도 그렇게 해야하는거 아닌가 싶지만! 일단은 패스
        User savedUser = userRepository.save(user);

        // 상태 정보 단순 조회
        UserStatus userStatus = findUserStatusByUserId(savedUser.getId());

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

        // 유저가 작성한 메세지 삭제 - 멘토님이 말씀하신 논리적 삭제 / 물리적 삭제 시간이 있으면 추가 고려 혹은 다음 미션 때 개선안으로 생각해보자
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

        // 항상 주체 삭제를 마지막에 하는 것이 좋다고 함
        userRepository.deleteById(userId);
    }

    // 유틸?
    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private UserStatus findUserStatusByUserId(UUID userId) {
        return userStatusRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_STATUS_NOT_FOUND));
    }

    private void validateDuplicateEmailAndUsername(String email, String username) {
        Optional<User> emailResult = userRepository.findByEmail(email);
        if (emailResult.isPresent()) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }

        Optional<User> usernameResult = userRepository.findByUsername(username);
        if (usernameResult.isPresent()) {
            throw new BusinessException(ErrorCode.USER_USERNAME_DUPLICATE);
        }
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
