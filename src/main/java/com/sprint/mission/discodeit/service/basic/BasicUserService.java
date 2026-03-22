package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.dto.user.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.binaryContent.BinaryContentNotFoundException;
import com.sprint.mission.discodeit.exception.user.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {


    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UUID create(UserCreateRequest request) {

        // userName, email 중복 검사
        userRepository.findAll().stream()
                .filter(user -> user.getUserName().equals(request.getUserName()))
                .findAny()
                .ifPresent(u -> { throw new UserAlreadyExistsException("이미 사용 중인 닉네임입니다."); });

        userRepository.findAll().stream()
                .filter(user -> user.getEmail().equals(request.getEmail()))
                .findAny()
                .ifPresent(u -> { throw new UserAlreadyExistsException("이미 등록된 이메일입니다."); });

        if (request.getProfileId() != null && binaryContentRepository.findById(request.getProfileId()) == null) {
            throw new BinaryContentNotFoundException("프로필 이미지를 찾을 수 없습니다.");
        }

        User newUser = new User(
                request.getUserName(),
                request.getEmail(),
                request.getPassword()
        );

        // 선택적 프로필 이미지 등록
        if (request.getProfileId() != null) {
            newUser.updateProfileId(request.getProfileId());
        }

        // 유저 저장
        userRepository.save(newUser);

        // UserStatus 생성
        UserStatus status = new UserStatus(newUser.getId());
        userStatusRepository.save(status);

        return newUser.getId();
    }

    @Override
    public UserResponse read(UUID userId) {
        User user = userRepository.findById(userId);

        if (user == null) {
            throw new UserNotFoundException("조회할 유저를 찾을 수 없습니다. (userId: " + userId + ")");
        }
        // 온라인 여부
        UserStatus status = userStatusRepository.findById(userId);
        boolean isOnline = (status != null) && status.isOnline();

        // DTO(UserResponse)로 반환 (패스워드 제외)
        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                user.getProfileId(),
                isOnline,
                user.getJoinedChannelId()
        );
    }

    @Override
    public List<UserResponse> readAll() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            return new ArrayList<>();
        }

        // 모든 유저를 하나씩 UserResponse로 변환해서 리스트로 만듦
        return users.stream()
                .map(user -> {
                    // 온라인 여부
                    UserStatus status = userStatusRepository.findById(user.getId());
                    boolean isOnline = (status != null) && status.isOnline();

                    // DTO(UserResponse)로 반환 (패스워드 제외)
                    return new UserResponse(
                            user.getId(),
                            user.getUserName(),
                            user.getEmail(),
                            user.getProfileId(),
                            isOnline,
                            user.getJoinedChannelId()
                    );
                })
                .toList();
    }

    @Override
    public void update(UUID userId, UserUpdateRequest request) {

        // 수정 대상 탐색
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new UserNotFoundException("수정할 유저를 찾을 수 없습니다. (userId: " + userId + ")");
        }

        // userName, email, password
        user.update(request.getUserName(), request.getEmail(), request.getPassword());

        // 프로필 이미지, profileId 값이 있을때만
        if (request.getProfileId() != null) {
            user.updateProfileId(request.getProfileId());
        }

        // 저장
        userRepository.save(user);
    }

    @Override
    public void delete(UUID userId) {
        User user = userRepository.findById(userId);

        if (user == null) {
            throw new UserNotFoundException("삭제할 유저를 찾을 수 없습니다. (userId: " + userId + ")");
        }
        // userStatus(온라인여부) 삭제
        userStatusRepository.deleteById(userId);

        // BinaryContent(프로필 이미지) 삭제
        if (user.getProfileId() != null) {
            binaryContentRepository.deleteById(user.getProfileId());
        }

        // 유저가 작성한 메시지 삭제
        messageRepository.findAll().stream()
                .filter(message -> message.getSenderId().equals(userId))
                .forEach(message -> messageRepository.deleteById(message.getId()));

        // 유저가 속한 채널에서 유저 제외
        List<UUID> joinedChannels = new ArrayList<>(user.getJoinedChannelId());
        joinedChannels.forEach(channelId -> {
            if (channelId != null) {
                Channel channel = channelRepository.findById(channelId);
                if (channel != null) {
                    channel.removeMember(userId);
                    channelRepository.save(channel);
                }
            }
        });

        // 유저가 관리자인 채널을 삭제
        List<UUID> channelsToDelete = channelRepository.findAll().stream()
                .filter(channel -> channel.getAdminId().equals(userId))
                .map(Channel::getId)
                .toList();

        for (UUID channelId : channelsToDelete) {
            // 채널 삭제 로직 중복 방지를 위해 채널 멤버 제외 및 메시지 삭제를 처리
            Channel channel = channelRepository.findById(channelId);
            if (channel != null) {
                List<UUID> disconnectMembers = new ArrayList<>(channel.getMemberId());
                disconnectMembers.forEach(memberId -> {
                    if (memberId != null) {
                        User member = userRepository.findById(memberId);
                        if (member != null) {
                            member.leaveChannel(channelId);
                            userRepository.save(member);
                        }
                    }
                });

                // 채널의 모든 메시지 삭제
                messageRepository.findAll().stream()
                        .filter(message -> message.getChannelId().equals(channelId))
                        .forEach(message -> messageRepository.deleteById(message.getId()));

                channelRepository.deleteById(channelId);
            }
        }

        // 유저 최종 삭제
        userRepository.deleteById(userId);
    }






    @Override
    public void setChannelService(ChannelService channelService) {}

    @Override
    public void setMessageService(MessageService messageService) {}
}
