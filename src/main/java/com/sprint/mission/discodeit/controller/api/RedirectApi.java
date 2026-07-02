package com.sprint.mission.discodeit.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

public interface RedirectApi {

  @GetMapping("/login")
  ResponseEntity<Void> loginRedirect(HttpServletRequest request);
}
