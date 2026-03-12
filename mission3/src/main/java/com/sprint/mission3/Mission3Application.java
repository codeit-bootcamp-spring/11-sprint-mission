package com.sprint.mission3;

import com.sprint.mission3.service.MissionService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class Mission3Application {

	public static void main(String[] args) {
				ConfigurableApplicationContext context = SpringApplication.run(Mission3Application.class, args);

	 		    MissionService missionService = context.getBean(MissionService.class);

			    missionService.run();
	}
}