package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.RedirectApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping
public class RedirectController implements RedirectApi {

  @Override
  @GetMapping("/login")
  public ResponseEntity<Void> loginRedirect(HttpServletRequest request) {
    log.debug("[REDIRECT_TO_HOME_SUCCESS] 리다이렉트 요청 시작 - 현재 URL=\"{}\"",
        request.getRequestURI());

    ResponseEntity<Void> response = ResponseEntity.status(HttpStatus.FOUND)
        .header("Location", "/")
        .build();

    log.info("[REDIRECT_TO_HOME_SUCCESS] 리다이렉트 요청 성공 - 리다이렉트 URL=\"{}\"",
        response.getHeaders().getLocation());

    return response;
  }

}
