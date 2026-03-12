package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.control.ChannelControl;
import com.sprint.mission.discodeit.control.MainControl;
import com.sprint.mission.discodeit.control.MessageControl;
import com.sprint.mission.discodeit.control.UserControl;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.menu.MainMenu;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.repository.file.FileChannelRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.repository.file.FileUserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Scanner;
import java.util.UUID;

@SpringBootApplication
public class DiscodeitApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class);

		UserService userService = context.getBean(BasicUserService.class);
		ChannelService channelService = context.getBean(BasicChannelService.class);
		MessageService messageService = context.getBean(BasicMessageService.class);

		Scanner input = new Scanner(System.in);

		UUID currentUserId = null;

		while(true) {
			try {
				MainMenu mainMenu = MainControl.selectMainMenu(input);

				switch (mainMenu) {
					case USER:
						UUID id = UserControl.controlUserMenu(input, userService);

						if (id != null) {
							currentUserId = id;

							User nowUser = userService.readUser(currentUserId);
							System.out.println("로그인 유저 : " + nowUser.getUsername());
						}
						break;
					case CHANNEL:
						ChannelControl.controlChannelMenu(input, channelService, currentUserId);
						break;
					case MESSAGE:
						if (currentUserId == null) {
							System.out.println("메시지 기능을 사용하려면  먼저 유저 생성이 필요합니다.");
							continue;
						}

						MessageControl.controlMessageMenu(
								input,
								messageService,
								userService,
								channelService,
								currentUserId
						);
						break;
					case EXIT:
						System.out.println("프로그램을 종료합니다.");
						return;
				}
			} catch (Exception e) {
				System.out.println("오류 발생 : " + e.getMessage());
			}
		}
	}

}
