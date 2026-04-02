package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.authDto.AuthDto;
import com.sprint.mission.discodeit.dto.userdto.UserInfoDto;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.service.DiffPasswordException;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.UserStatusRepository;
import com.sprint.mission.discodeit.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;

    @Override
    public UserInfoDto login(AuthDto authDto) throws DiffPasswordException {

        User user = userRepository.getUserByNickname(authDto.nickName()).orElseThrow(IllegalArgumentException::new);
        UserInfoDto userInfo;
        if (user.checkSamePassword(authDto.password())) {



            userInfo = new UserInfoDto(
                    user.getId(),
                    user.getNickname(),
                    user.getEmail(),
                    binaryContentRepository.getBinaryContent(user.getProfileId()).orElseThrow().getId(),
                    userStatusRepository.getUserStatus(user.getId()).orElseThrow(IllegalArgumentException::new)

            );

            user.updateUpdatedAt();
            userRepository.saveUser(user);


        } else throw new DiffPasswordException();

        return userInfo;
    }


    






}













