package com.sprint.mission.admin;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAdminServer // Spring boot admin
public class AdminApplication {

  public static void main(String[] args) {
    SpringApplication.run(AdminApplication.class, args);
  }

}
