package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.ChannelResponse;
import com.sprint.mission.discodeit.dto.channel.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.channel.ChannelOperationException;
import com.sprint.mission.discodeit.exception.channel.InvalidChannelRequestException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
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
public class BasicChannelService implements ChannelService {


    // Service끼리 순환 참조하던걸 Repository로 변경
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;

    @Override
    public UUID createPublicChannel(PublicChannelCreateRequest request) {
        if (request.getAdminId() == null) {
            throw new InvalidChannelRequestException("공개 채널 생성 실패: 관리자(Admin) ID가 누락되었습니다.");
        }

        Channel channel = new Channel(request.getChannelName(), request.getAdminId());
        channelRepository.save(channel);

        // 어드민 유저의 참여 채널목록 업데이트
        User admin = userRepository.findById(request.getAdminId());
        if (admin != null) {
            admin.joinChannel(channel.getId());
            userRepository.save(admin);
        } else {
            throw new UserNotFoundException("공개 채널 생성 실패: 해당 관리자 ID(" + request.getAdminId() + ")를 가진 유저를 찾을 수 없습니다.");
        }

        return channel.getId();
    }

    @Override
    public UUID createPrivateChannel(PrivateChannelCreateRequest request) {
        if (request.getAdminId() == null) {
            throw new InvalidChannelRequestException("비공개 채널 생성 실패: 관리자(Admin) ID가 누락되었습니다.");
        }
        if (request.getMemberId() == null || request.getMemberId().isEmpty()) {
            throw new InvalidChannelRequestException("비공개 채널 생성 실패: 초대할 멤버 ID 목록이 비어있습니다.");
        }

        // 어드민 존재 여부 확인
        if (userRepository.findById(request.getAdminId()) == null) {
            throw new UserNotFoundException("비공개 채널 생성 실패: 해당 관리자 ID(" + request.getAdminId() + ")를 가진 유저를 찾을 수 없습니다.");
        }

        // channelName은 null
        Channel channel = new Channel(null, request.getAdminId());

        request.getMemberId().forEach(memberId -> {
            if (!memberId.equals(request.getAdminId())) { // 방장은 생성자에서 이미 추가됨
                if(userRepository.findById(memberId) == null) {
                    throw new UserNotFoundException("비공개 채널 생성 실패: 초대하려는 유저 ID(" + memberId + ")를 찾을 수 없습니다.");
                }
                channel.addMember(memberId);
            }
        });
        channelRepository.save(channel);

        for (UUID userId : channel.getMemberId()) {
            // 채널 멤버들 참여 채널목록 업데이트
            User user = userRepository.findById(userId);
            if (user != null) {
                user.joinChannel(channel.getId());
                userRepository.save(user);
            }

            // 채널 멤버들의 이 채널에 대한 ReadStatus 생성
            ReadStatus readStatus = new ReadStatus(userId, channel.getId());
            readStatusRepository.save(readStatus);
        }
        return channel.getId();
    }

    private ChannelResponse convertToResponse(Channel channel) {
        boolean isPrivate = channel.getChannelName() == null;

        java.time.Instant lastMessageTime = messageRepository.findAll().stream()
                .filter(m -> m.getChannelId().equals(channel.getId()))
                .map(m -> m.getUpdatedAt())
                .max(java.util.Comparator.naturalOrder())
                .orElse(null);

        // PRIVATE 채널인 경우 참여한 User의 id
        List<UUID> memberIds = isPrivate ? new ArrayList<>(channel.getMemberId()) : null;

        return new ChannelResponse(
                channel.getId(),
                channel.getChannelName(),
                channel.getAdminId(),
                isPrivate,
                lastMessageTime,
                memberIds
        );
    }

    @Override
    public ChannelResponse read(UUID channelId) {
        Channel channel = channelRepository.findById(channelId);
        if (channel != null) {
            return convertToResponse(channel);
        } else {
            throw new ChannelNotFoundException("조회할 채널을 찾을 수 없습니다. (ID: " + channelId + ")");
        }
    }

    @Override
    public List<ChannelResponse> findAllByUserId(UUID userId) {
        List<Channel> channels = channelRepository.findAll();
        if (channels.isEmpty()) {
            System.out.println("채널이 존재하지 않습니다.");
            return new ArrayList<>();
        }

        return channels.stream()
                .filter(channel -> {
                    boolean isPrivate = channel.getChannelName() == null;
                    return !isPrivate || channel.getMemberId().contains(userId);
                })
                .map(this::convertToResponse)
                .toList();
    }

    @Override
    public void update(UUID channelId, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(channelId);

        if (channel == null) {
            throw new ChannelNotFoundException("수정할 채널을 찾을 수 없습니다. (ID: " + channelId + ")");
        }

        if (channel.getChannelName() == null) {
            throw new ChannelOperationException("비공개 채널은 정보를 수정할 수 없습니다.");
        }

        channel.update(request.getChannelName(), request.getAdminId());
        channelRepository.save(channel);
    }

    @Override
    public void delete(UUID channelId) {
        if (channelId == null) throw new InvalidChannelRequestException("삭제할 채널 ID가 누락되었습니다.");

        Channel channel = channelRepository.findById(channelId);
        if (channel == null) {
            throw new ChannelNotFoundException("삭제할 채널을 찾을 수 없습니다. (ID: " + channelId + ")");
        }

        // 멤버 연결 해제
        List<UUID> disconnectMembers = new ArrayList<>(channel.getMemberId());
        disconnectMembers.forEach(userId -> {
            if (userId != null) {
                User user = userRepository.findById(userId);
                if (user != null) {
                    user.leaveChannel(channelId);
                    userRepository.save(user);
                }
            }
        });

        // 관련된 Message 데이터 같이 삭제
        // MessageRepository에 해당 채널 ID를 가진 메시지 다 지움
        messageRepository.findAll().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .forEach(message -> messageRepository.deleteById(message.getId()));

        // 관련된 ReadStatus 데이터 같이 삭제
        // ReadStatusRepository에 해당 채널 ID를 가진 상태값을 다 지움
        readStatusRepository.findAll().stream()
                .filter(rs -> rs.getChannelId().equals(channelId))
                .forEach(rs -> readStatusRepository.deleteById(rs.getId()));

        // 최종 채널 삭제
        channelRepository.deleteById(channelId);
    }

    @Override
    public void deleteChannelByAdmin(UUID adminId) {
        List<UUID> channelsToDelete = channelRepository.findAll().stream()
                .filter(channel -> channel.getAdminId().equals(adminId))
                .map(Channel::getId)
                .toList();

        for (UUID channelId : channelsToDelete) {
            this.delete(channelId);
        }
    }

    @Override
    public void addUserToChannel(UUID userId, UUID channelId) {
        if (userId == null || channelId == null) {
            throw new InvalidChannelRequestException("채널에 유저를 추가하기 위한 정보가 누락되었습니다.");
        }

        Channel channel = channelRepository.findById(channelId);
        if (channel == null) throw new ChannelNotFoundException("채널을 찾을 수 없습니다. (ID: " + channelId + ")");

        User user = userRepository.findById(userId);
        if (user == null) throw new UserNotFoundException("유저를 찾을 수 없습니다. (ID: " + userId + ")");


        channel.addMember(userId);
        channelRepository.save(channel);

        user.joinChannel(channelId);
        userRepository.save(user);

    }

    @Override
    public void removeUserFromChannel(UUID userId, UUID channelId) {
        if (userId == null || channelId == null) {
            throw new InvalidChannelRequestException("채널에서 유저를 제거하기 위한 정보가 누락되었습니다.");
        }

        Channel channel = channelRepository.findById(channelId);
        if (channel == null) throw new ChannelNotFoundException("채널을 찾을 수 없습니다. (ID: " + channelId + ")");

        User user = userRepository.findById(userId);
        if (user == null) throw new UserNotFoundException("유저를 찾을 수 없습니다. (ID: " + userId + ")");

        channel.removeMember(userId);
        channelRepository.save(channel);

        user.leaveChannel(channelId);
        userRepository.save(user);

    }

    @Override
    public void setMessageService(MessageService messageService) {}

    @Override
    public void setUserService(UserService userService) {}
}
