package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentInfoDto;
import com.sprint.mission.discodeit.dto.binarycontentdto.CreateBinaryContentDto;
import com.sprint.mission.discodeit.dto.binarycontentdto.CreateProfileImgDto;
import com.sprint.mission.discodeit.dto.binarycontentdto.FindBinaryContetnInfo;

import java.util.List;
import java.util.UUID;

public interface BinaryContentService {

    BinaryContentInfoDto create(CreateBinaryContentDto createBinaryContentDto);
    BinaryContentInfoDto createProfileImg(CreateProfileImgDto createProfileImgDto);
    BinaryContentInfoDto find(FindBinaryContetnInfo findBinaryContetnInfo);
    List<BinaryContentInfoDto> findAll();
    List<BinaryContentInfoDto> findAllByUserId(UUID userId);
    boolean delete(UUID binaryContentId);













}
