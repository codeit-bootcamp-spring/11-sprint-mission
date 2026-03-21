package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.channel.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;
    private final UserChannelRepository userChannelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;
    private final BinaryContentRepository binaryContentRepository;

    // 채널 생성
    @Override
    public ChannelResponseDTO createPublicChannel(
            CreatePublicChannelRequestDTO dto
    ) {
        Channel newChannel = Channel.create(
                ChannelType.PUBLIC,
                dto.name(),
                dto.description(),
                dto.requestUserId()
        );
        Channel savedChannel = channelRepository.save(newChannel);

        setupUserInChannel(dto.requestUserId(), savedChannel.getId(), UserChannelRole.MASTER);

        return makeChannelResponseDTO(savedChannel);
    }

    @Override
    public ChannelResponseDTO createPrivateChannel(
            CreatePrivateChannelRequestDTO dto
    ) {
        Channel newChannel = Channel.create(
                ChannelType.PRIVATE,
                null,
                null,
                dto.requestUserId()
        );

        Channel savedChannel = channelRepository.save(newChannel);

        // 관계 매핑
        Set<UUID> userList = dto.userList() != null ? dto.userList() : Collections.emptySet(); // null 방지 (만약 유저 리스트를 받지 않을 수도 있으므로)
        Set<UUID> participants = new HashSet<>(userList); // 유저 리스트
        participants.add(dto.requestUserId()); // 방장 포함

        for (UUID userId : participants) {
            UserChannelRole role = userId.equals(dto.requestUserId()) ? UserChannelRole.MASTER : UserChannelRole.NORMAL;
            setupUserInChannel(userId, savedChannel.getId(), role);
        }

        return makeChannelResponseDTO(savedChannel);
    }

    // 특정 채널 조회 (find)
    @Override
    public ChannelResponseDTO getChannels(
            UUID channelId
    ) {
        // 해당 채널 초회
        Channel channel = getChannel(channelId);
        return makeChannelResponseDTO(channel);
    }

    // 채널 전체 조회 (findAll)
    @Override
    public FindChannelsResponseDTO findAllByUserId(UUID userId) {
        // 전체 체널 조회
        List<Channel> allChannels = channelRepository.findAll();

        // 내가 현재 속해있는 모든 채널 조회
        List<UUID> myJoinedChannelIds = userChannelRepository.findAllByUserId(userId).stream()
                .map(UserChannel::getChannelId)
                .toList();

        List<ChannelResponseDTO> result = new ArrayList<>();
        for (Channel channel : allChannels) {
            boolean isVisible = false;

            if (!channel.isPrivate()) {
                isVisible = true;
            } else if (channel.isPrivate() && myJoinedChannelIds.contains(channel.getId())) {
                isVisible = true;
            }

            if (isVisible) {
                result.add(makeChannelResponseDTO(channel));
            }
        }

        return FindChannelsResponseDTO.from(result);
    }

    // 내 채널 목록 조회 - 미완성
    @Override
    public FindChannelsResponseDTO getUserChannels(UUID userId) {
        // 해당 유저가 포함된 유저채널 관계 가져오기
        List<UserChannel> allByUserId = userChannelRepository.findAllByUserId(userId); // O(1)

        // 가져온 유저채널 관계로 채널 가져오기
        List<Channel> myChannelList = new ArrayList<>();
        for (UserChannel uc : allByUserId) { // O(K)
            myChannelList.add(getChannel(uc.getChannelId()));
        }

        List<ChannelResponseDTO> result = new ArrayList<>();
        for (Channel channel : myChannelList) {
            result.add(makeChannelResponseDTO(channel));
        }

        return FindChannelsResponseDTO.from(result);
    }

    // 채널 정보 수정
    @Override
    public ChannelResponseDTO updateChannel(
            UpdateChannelRequestDTO dto
    ) {
        // 여기서 고민한 점 빠르게 방장인지 아닌지 체크하는게 좋은 것 같음

        // 채널 존재 유무 확인
        Channel channel = getChannel(dto.channelId());

        if (!channel.isMaster(dto.requestUserId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }

        // PRIVATE 채널은 수정 불가능
        if (channel.isPrivate()) {
            throw new BusinessException(ErrorCode.PRIVATE_NOT_UPDATE);
        }

        // 채널 수정 권한 확인은 안에서
        channel.updateInfo(dto.name(), dto.description(), dto.requestUserId());

        // 수정된 채널 업데이트
        Channel savedChannel  = channelRepository.save(channel);

        return makeChannelResponseDTO(savedChannel);
    }

    // 채널 삭제
    @Override
    public void deleteChannel(
            DeleteChannelRequestDTO dto
    ) {
        // 채널 존재 유무 확인
        Channel channel = getChannel(dto.channelId());

        // 채널에 대한 권환 확인
        channel.verifyChannelUpdate(dto.requestUserId());

        // 유저 - 채널 관계 삭제
        List<UserChannel> list = userChannelRepository.findAllByChannelId(channel.getId()); // O(1)
        for (UserChannel uc : list) { // O(K)
            userChannelRepository.deleteById(uc.getId());
        }

        // 채널 - 메세지 삭제
        List<Message> messageList = messageRepository.findAllByChannelId(channel.getId()); // ForEach 리팩토링,,, - . -
        for (Message m : messageList) {
            if (m.getAttachmentIds() != null) {
                for (UUID attachmentId : m.getAttachmentIds()) {
                    binaryContentRepository.deleteById(attachmentId);
                }
            }
            messageRepository.deleteById(m.getId());
        }

        // 채널 - ReadStatus 삭제
        List<ReadStatus> readStatuses = readStatusRepository.findByChannelId(channel.getId());
        for (ReadStatus rs : readStatuses) {
            readStatusRepository.deleteById(rs.getId());
        }

        // 채널 삭제
        channelRepository.deleteById(channel.getId());
    }

    // ======================= Custom =========================

    // 채널 가입 - 미완성
    @Override
    public void joinChannel(
            JoinChannelRequestDTO dto
    ) {
        // 채널 유무 확인
        Channel channel = getChannel(dto.channelId());

        // 유저 확인
        User user = userRepository.findById(dto.requestUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 이미 가입된 상태인지 체크 - 좀만 더 고민
        Optional<UserChannel> result = userChannelRepository.findByUserIdAndChannelId(user.getId(), channel.getId());
        if (result.isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_JOIN_CHANNEL);
        }

        setupUserInChannel(user.getId(), channel.getId(), UserChannelRole.NORMAL);
    }

    // 채널 탈퇴 - 미완성
    @Override
    public void leaveChannel(
            LeaveChannelRequestDTO dto
    ) {
        // 관계가 있는지 확인 - 이거 바로 위에 거랑 비슷하지만 반대되되는 개념
        UserChannel mapping = userChannelRepository.findByUserIdAndChannelId(dto.requestUserId(), dto.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS));

        // 채널 존재 유무 확인 - 근데 관계가 있다면 채널이 존재하는거라고 생각해도 되지 않을까? - 근데 방장 때문에 필요하구나
        Channel channel = getChannel(dto.channelId());

        // 방장은 채널을 함부로 나갈 수 없음 (그냥 이건 내가 정한 정책) - 단, 채널을 삭제하거나 방장을 위임하면 됨(위임은 구현 X)
        if (channel.isMaster(dto.requestUserId())) {
            throw new BusinessException(ErrorCode.MASTER_NOT_LEAVE);
        }

        // 자 이제 이걸 어떻게 처리해야할까? - ReadStatus 삭제!!

        userChannelRepository.deleteById(mapping.getId());
    }


    // 유틸
    private void setupUserInChannel(UUID userId, UUID channelId, UserChannelRole userChannelRole) {
        UserChannel masterMapping = UserChannel.create(userId, channelId, userChannelRole);
        userChannelRepository.save(masterMapping);

        ReadStatus readStatus = ReadStatus.create(userId, channelId);
        readStatusRepository.save(readStatus);
    }

    private Channel getChannel(UUID channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
        return channel;
    }

    // 현재 여기서 성능 문제 생길 것 같음
    // - 마지막 메세지를 찾는데 시간이 너무 오래 걸림 - 단건 채널 조회는 딱히 상관 없을지 몰라도 다건 조회에서 문제 발생할 것 같음 (10 -> 100000)
    // - 따로 표시해두는게 좋을 듯 - 추후 적용
    private ChannelResponseDTO makeChannelResponseDTO(Channel channel) {
        Instant recentMessageTime = channel.getRecentMessageTime();

        // PRIVATE인 경우 참여자 ID 목록 추출
        List<UUID> participantIds = new ArrayList<>();
        if (channel.isPrivate()) {
            participantIds = userChannelRepository.findAllByChannelId(channel.getId()).stream()
                    .map(UserChannel::getUserId)
                    .toList();
        }

        ChannelResponseDTO dto = ChannelResponseDTO.from(
                channel,
                recentMessageTime,
                participantIds
        );

        return dto;
    }

}
