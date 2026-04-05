package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.readstatusdto.CreateReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatusdto.ReadStatusInfoDto;
import com.sprint.mission.discodeit.dto.readstatusdto.UpdateReadStatus;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.service.AlreadyExistException;
import com.sprint.mission.discodeit.exception.service.NonExistException;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicReadStatusService implements ReadStatusService {

  private final ReadStatusRepository readStatusRepository;
  private final UserRepository userRepository;
  private final ChannelRepository channelRepository;

  @Override
  public ReadStatusInfoDto create(CreateReadStatusDto createReadStatusDto) {

    ReadStatus readStatus = new ReadStatus(

        createReadStatusDto.userId(),
        createReadStatusDto.channelId(),
        createReadStatusDto.lastReadAt()
    );

    //유효 유저, 채널인지
    if (!userRepository.isExistUser(createReadStatusDto.userId())) {
      throw new NonExistException("존재하는 유저 아이디가 아닙니다.");
    }
    if (!channelRepository.isExistChannel(createReadStatusDto.channelId())) {
      throw new NonExistException("존재하는 채널 아이디가 아닙니다");
    }

    //존재하는 스테이터스 인지 체크

    if (readStatusRepository.isExist(createReadStatusDto.userId(),
        createReadStatusDto.channelId())) {
      throw new AlreadyExistException("이미 존재하는 유저와 채널의 읽기 상태입니다");
    }

    readStatusRepository.save(readStatus);

    return statusToInfoDto(readStatus);

  }

  @Override
  public ReadStatusInfoDto find(CreateReadStatusDto createReadStatusDto) {

    return statusToInfoDto(
        readStatusRepository.get(createReadStatusDto.userId(), createReadStatusDto.channelId())
            .orElseThrow());

  }

  @Override
  public List<ReadStatusInfoDto> findAllById(UUID userId) {
    if (!userRepository.isExistUser(userId)) {
      throw new NonExistException("존재하는 유저 아이디가 아닙니다.");
    }

    return readStatusRepository.getAllByUserId(userId).stream()
        .map(this::statusToInfoDto)
        .toList();
  }

  @Override
  public ReadStatusInfoDto update(UUID readStatusId, UpdateReadStatus updateReadStatusDto) {

    ReadStatus readStatus = readStatusRepository.get(readStatusId)
        .orElseThrow(() -> new NonExistException("존재하는 읽기 상태가 아닙니다."));

    readStatus.updateLastReadAt(updateReadStatusDto.newLastReadAt());

    readStatusRepository.save(readStatus);

    return statusToInfoDto(readStatus);

  }

  @Override
  public boolean delete(CreateReadStatusDto createReadStatusDto) {

    //유효 유저, 채널인지
    if (!userRepository.isExistUser(createReadStatusDto.userId())) {
      throw new NonExistException("존재하는 유저 아이디가 아닙니다.");
    }
    if (!channelRepository.isExistChannel(createReadStatusDto.channelId())) {
      throw new NonExistException("존재하는 채널 아이디가 아닙니다");
    }

    //존재하는 스테이터스 인지 체크

    if (readStatusRepository.isExist(createReadStatusDto.userId(),
        createReadStatusDto.channelId())) {
      throw new AlreadyExistException("이미 존재하는 유저와 채널의 읽기 상태입니다");
    }

    readStatusRepository.delete(createReadStatusDto.userId(), createReadStatusDto.channelId());
    return true;


  }


  ReadStatusInfoDto statusToInfoDto(ReadStatus readStatus) {
    return new ReadStatusInfoDto(

        readStatus.getId(),
        readStatus.getCreatedAt(),
        readStatus.getLastReadAt(),
        readStatus.getUserId(),
        readStatus.getChannelId(),
        readStatus.getLastReadAt()
    );


  }

}
