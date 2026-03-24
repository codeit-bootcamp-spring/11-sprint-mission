package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.binary.BinaryFile;
import com.sprint.mission.discodeit.dto.channeldto.ChannelMemberDto;
import com.sprint.mission.discodeit.dto.channeldto.CreatePrivateChannelDto;
import com.sprint.mission.discodeit.dto.channeldto.CreatePublicChannelDto;
import com.sprint.mission.discodeit.dto.channeldto.UpdateChannelDto;
import com.sprint.mission.discodeit.dto.messagedto.CreateMessageDto;
import com.sprint.mission.discodeit.dto.messagedto.UpdateMessageDto;
import com.sprint.mission.discodeit.dto.readstatusdto.CreateReadStatusDto;
import com.sprint.mission.discodeit.dto.userdto.CreateUserDto;
import com.sprint.mission.discodeit.dto.userdto.UpdateUserDto;
import com.sprint.mission.discodeit.dto.userstatusdto.CreateUserStatusDto;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {




	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(DiscodeitApplication.class, args);


	}

}
