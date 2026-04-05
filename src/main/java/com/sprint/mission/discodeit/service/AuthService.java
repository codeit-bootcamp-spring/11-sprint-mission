package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.authDto.AuthDto;
import com.sprint.mission.discodeit.dto.userdto.CreatedUserDto;

public interface AuthService {

  CreatedUserDto login(AuthDto authDto);


}
